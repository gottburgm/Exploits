/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.web.tomcat.service.deployers;

// $Id: JBossContextConfig.java 97419 2009-12-03 17:06:50Z scott.stark@jboss.org $

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.SessionCookie;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.util.IntrospectionUtils;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.AuthConstraintMetaData;
import org.jboss.metadata.web.spec.DispatcherType;
import org.jboss.metadata.web.spec.ErrorPageMetaData;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.JspConfigMetaData;
import org.jboss.metadata.web.spec.JspPropertyGroup;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.LocaleEncodingMetaData;
import org.jboss.metadata.web.spec.LocaleEncodingsMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;
import org.jboss.metadata.web.spec.MimeMappingMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;
import org.jboss.metadata.web.spec.TaglibMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionsMetaData;
import org.jboss.metadata.web.spec.WelcomeFileListMetaData;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.virtual.VirtualFile;
import org.jboss.web.tomcat.metadata.ContextMetaData;
import org.jboss.web.tomcat.metadata.ParameterMetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;

@SuppressWarnings("unchecked")
public class JBossContextConfig extends ContextConfig
{
   public static ThreadLocal<JBossWebMetaData> metaDataLocal = new ThreadLocal<JBossWebMetaData>();

   public static ThreadLocal<JBossWebMetaData> metaDataShared = new ThreadLocal<JBossWebMetaData>();

   public static ThreadLocal<DeployerConfig> deployerConfig = new ThreadLocal<DeployerConfig>();

   public static ThreadLocal<Kernel> kernelLocal = new ThreadLocal<Kernel>();
   public static ThreadLocal<DeploymentUnit> deploymentUnitLocal = new ThreadLocal<DeploymentUnit>();

   private static Logger log = Logger.getLogger(JBossContextConfig.class);

   private boolean runDestroy = false;

   /**
    * <p>
    * Creates a new instance of {@code JBossContextConfig}.
    * </p>
    */
   public JBossContextConfig()
   {
      super();
      try
      {
         Map authMap = this.getAuthenticators();
         if (authMap.size() > 0)
            customAuthenticators = authMap;
      }
      catch (Exception e)
      {
         log.debug("Failed to load the customized authenticators", e);
      }
      runDestroy = deployerConfig.get().isDeleteWorkDirs();
   }

   @Override
   protected void applicationWebConfig()
   {
      processWebMetaData(metaDataLocal.get());
      processContextParameters();
   }

   @Override
   protected void defaultWebConfig()
   {
      processWebMetaData(metaDataShared.get());

      ServletContext servletContext = context.getServletContext();
      Kernel kernel = kernelLocal.get();
      DeploymentUnit unit = deploymentUnitLocal.get();
      log.debug("Setting MC attributes, kernel: " + kernel + ", unit: " + unit);
      servletContext.setAttribute(KernelConstants.KERNEL_NAME, kernel);
      servletContext.setAttribute(DeploymentUnit.class.getName(), unit);
   }

   protected void processWebMetaData(JBossWebMetaData metaData)
   {
      if (context instanceof StandardContext)
      {
         ((StandardContext)context).setReplaceWelcomeFiles(true);
      }

      // Metadata complete
      context.setIgnoreAnnotations(metaData.isMetadataComplete());

      // SetPublicId
      if (metaData.is25())
         context.setPublicId("/javax/servlet/resources/web-app_2_5.dtd");
      else if (metaData.is24())
         context.setPublicId("/javax/servlet/resources/web-app_2_4.dtd");
      else if (metaData.is23())
         context.setPublicId(org.apache.catalina.startup.Constants.WebDtdPublicId_23);
      else
         context.setPublicId(org.apache.catalina.startup.Constants.WebDtdPublicId_22);

      // processContextParameters() provides a merged view of the context params

      // Display name
      DescriptionGroupMetaData dg = metaData.getDescriptionGroup();
      if (dg != null)
      {
         String displayName = dg.getDisplayName();
         if (displayName != null)
         {
            context.setDisplayName(displayName);
         }
      }

      // Distributable
      if (metaData.getDistributable() != null)
         context.setDistributable(true);

      // Error pages
      List<ErrorPageMetaData> errorPages = metaData.getErrorPages();
      if (errorPages != null)
      {
         for (ErrorPageMetaData value : errorPages)
         {
            org.apache.catalina.deploy.ErrorPage errorPage = new org.apache.catalina.deploy.ErrorPage();
            errorPage.setErrorCode(value.getErrorCode());
            errorPage.setExceptionType(value.getExceptionType());
            errorPage.setLocation(value.getLocation());
            context.addErrorPage(errorPage);
         }
      }

      // Filter definitions
      FiltersMetaData filters = metaData.getFilters();
      if (filters != null)
      {
         for (FilterMetaData value : filters)
         {
            org.apache.catalina.deploy.FilterDef filterDef = new org.apache.catalina.deploy.FilterDef();
            filterDef.setFilterName(value.getName());
            filterDef.setFilterClass(value.getFilterClass());
            if (value.getInitParam() != null)
               for (ParamValueMetaData param : value.getInitParam())
               {
                  filterDef.addInitParameter(param.getParamName(), param.getParamValue());
               }
            context.addFilterDef(filterDef);
         }
      }

      // Filter mappings
      List<FilterMappingMetaData> filtersMappings = metaData.getFilterMappings();
      if (filtersMappings != null)
      {
         for (FilterMappingMetaData value : filtersMappings)
         {
            org.apache.catalina.deploy.FilterMap filterMap = new org.apache.catalina.deploy.FilterMap();
            filterMap.setFilterName(value.getFilterName());
            List<String> servletNames = value.getServletNames();
            if (servletNames != null)
            {
               for (String name : servletNames)
                  filterMap.addServletName(name);
            }
            List<String> urlPatterns = value.getUrlPatterns();
            if (urlPatterns != null)
            {
               for (String pattern : urlPatterns)
                  filterMap.addURLPattern(pattern);
            }
            List<DispatcherType> dispatchers = value.getDispatchers();
            if (dispatchers != null)
            {
               for (DispatcherType type : dispatchers)
                  filterMap.setDispatcher(type.name());
            }
            context.addFilterMap(filterMap);
         }
      }

      // Listeners
      List<ListenerMetaData> listeners = metaData.getListeners();
      if (listeners != null)
      {
         for (ListenerMetaData value : listeners)
         {
            context.addApplicationListener(value.getListenerClass());
         }
      }

      // Login configuration
      LoginConfigMetaData loginConfig = metaData.getLoginConfig();
      if (loginConfig != null)
      {
         org.apache.catalina.deploy.LoginConfig loginConfig2 = new org.apache.catalina.deploy.LoginConfig();
         loginConfig2.setAuthMethod(loginConfig.getAuthMethod());
         loginConfig2.setRealmName(loginConfig.getRealmName());
         if (loginConfig.getFormLoginConfig() != null)
         {
            loginConfig2.setLoginPage(loginConfig.getFormLoginConfig().getLoginPage());
            loginConfig2.setErrorPage(loginConfig.getFormLoginConfig().getErrorPage());
         }
         context.setLoginConfig(loginConfig2);
      }

      // MIME mappings
      List<MimeMappingMetaData> mimes = metaData.getMimeMappings();
      if (mimes != null)
      {
         for (MimeMappingMetaData value : mimes)
         {
            context.addMimeMapping(value.getExtension(), value.getMimeType());
         }
      }

      // Security constraints
      List<SecurityConstraintMetaData> scs = metaData.getSecurityContraints();
      if (scs != null)
      {
         for (SecurityConstraintMetaData value : scs)
         {
            org.apache.catalina.deploy.SecurityConstraint constraint = new org.apache.catalina.deploy.SecurityConstraint();
            TransportGuaranteeType tg = value.getTransportGuarantee();
            constraint.setUserConstraint(tg.name());
            AuthConstraintMetaData acmd = value.getAuthConstraint();
            constraint.setAuthConstraint(acmd != null);
            if (acmd != null)
            {
               if (acmd.getRoleNames() != null)
                  for (String role : acmd.getRoleNames())
                  {
                     constraint.addAuthRole(role);
                  }
            }
            WebResourceCollectionsMetaData wrcs = value.getResourceCollections();
            if (wrcs != null)
            {
               for (WebResourceCollectionMetaData wrc : wrcs)
               {
                  org.apache.catalina.deploy.SecurityCollection collection2 = new org.apache.catalina.deploy.SecurityCollection();
                  collection2.setName(wrc.getName());
                  List<String> methods = wrc.getHttpMethods();
                  if (methods != null)
                  {
                     for (String method : wrc.getHttpMethods())
                     {
                        collection2.addMethod(method);
                     }
                  }
                  List<String> patterns = wrc.getUrlPatterns();
                  if (patterns != null)
                  {
                     for (String pattern : patterns)
                     {
                        collection2.addPattern(pattern);
                     }
                  }
                  constraint.addCollection(collection2);
               }
            }
            context.addConstraint(constraint);
         }
      }

      // Security roles
      SecurityRolesMetaData roles = metaData.getSecurityRoles();
      if (roles != null)
      {
         for (SecurityRoleMetaData value : roles)
         {
            context.addSecurityRole(value.getRoleName());
         }
      }

      // Servlet
      JBossServletsMetaData servlets = metaData.getServlets();
      if (servlets != null)
      {
         for (JBossServletMetaData value : servlets)
         {
            org.apache.catalina.Wrapper wrapper = context.createWrapper();
            wrapper.setName(value.getName());
            wrapper.setServletClass(value.getServletClass());
            if (value.getJspFile() != null)
            {
               wrapper.setJspFile(value.getJspFile());
            }
            wrapper.setLoadOnStartup(value.getLoadOnStartup());
            if (value.getRunAs() != null)
            {
               wrapper.setRunAs(value.getRunAs().getRoleName());
            }
            List<ParamValueMetaData> params = value.getInitParam();
            if (params != null)
            {
               for (ParamValueMetaData param : params)
               {
                  wrapper.addInitParameter(param.getParamName(), param.getParamValue());
               }
            }
            SecurityRoleRefsMetaData refs = value.getSecurityRoleRefs();
            if (refs != null)
            {
               for (SecurityRoleRefMetaData ref : refs)
               {
                  wrapper.addSecurityReference(ref.getRoleName(), ref.getRoleLink());
               }
            }
            context.addChild(wrapper);
         }
      }

      // Servlet mapping
      List<ServletMappingMetaData> smappings = metaData.getServletMappings();
      if (smappings != null)
      {
         for (ServletMappingMetaData value : smappings)
         {
            List<String> urlPatterns = value.getUrlPatterns();
            if (urlPatterns != null)
            {
               for (String pattern : urlPatterns)
                  context.addServletMapping(pattern, value.getServletName());
            }
         }
      }

      // JSP mappings
      JspConfigMetaData config = metaData.getJspConfig();
      if (config != null)
      {
         List<JspPropertyGroup> groups = config.getPropertyGroups();
         if (groups != null)
         {
            for (JspPropertyGroup group : groups)
            {
               for (String pattern : group.getUrlPatterns())
               {
                  context.addJspMapping(pattern);
               }
            }
         }
         // Taglib
         List<TaglibMetaData> taglibs = config.getTaglibs();
         if (taglibs != null)
         {
            for (TaglibMetaData taglib : taglibs)
            {
               context.addTaglib(taglib.getTaglibUri(), taglib.getTaglibLocation());
            }
         }
      }

      // Locale encoding mapping
      LocaleEncodingsMetaData locales = metaData.getLocalEncodings();
      if (locales != null)
      {
         for (LocaleEncodingMetaData value : locales.getMappings())
         {
            context.addLocaleEncodingMappingParameter(value.getLocale(), value.getEncoding());
         }
      }

      // Welcome files
      WelcomeFileListMetaData welcomeFiles = metaData.getWelcomeFileList();
      if (welcomeFiles != null)
      {
         for (String value : welcomeFiles.getWelcomeFiles())
            context.addWelcomeFile(value);
      }

      // Session timeout
      SessionConfigMetaData scmd = metaData.getSessionConfig();
      if (scmd != null)
      {
         context.setSessionTimeout(scmd.getSessionTimeout());
      }
   }

   /**
    * <p>
    * Retrieves the map of authenticators according to the settings made available by {@code TomcatService}.
    * </p>
    * 
    * @return a {@code Map} containing the authenticator that must be used for each authentication method.
    * @throws Exception if an error occurs while getting the authenticators.
    */
   protected Map getAuthenticators() throws Exception
   {
      Map authenticators = new HashMap();
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();

      Properties authProps = this.getAuthenticatorsFromJndi();
      if (authProps != null)
      {
         Set keys = authProps.keySet();
         Iterator iter = keys != null ? keys.iterator() : null;
         while (iter != null && iter.hasNext())
         {
            String key = (String)iter.next();
            String authenticatorStr = (String)authProps.get(key);
            Class authClass = tcl.loadClass(authenticatorStr);
            authenticators.put(key, authClass.newInstance());
         }
      }
      if (log.isTraceEnabled())
         log.trace("Authenticators plugged in::" + authenticators);
      return authenticators;
   }

   /**
    * <p>
    * Get the key-pair of authenticators from the JNDI.
    * </p>
    * 
    * @return a {@code Properties} object containing the authenticator class name for each authentication method.
    * @throws NamingException if an error occurs while looking up the JNDI.
    */
   private Properties getAuthenticatorsFromJndi() throws NamingException
   {
      return (Properties)new InitialContext().lookup("TomcatAuthenticators");
   }

   /**
    * Process the context parameters. Let a user application
    * override the sharedMetaData values.
    */
   protected void processContextParameters()
   {
      JBossWebMetaData local = metaDataLocal.get();
      JBossWebMetaData shared = metaDataShared.get();

      Map<String, String> overrideParams = new HashMap<String, String>();

      List<ParamValueMetaData> params = local.getContextParams();
      if (params != null)
      {
         for (ParamValueMetaData param : params)
         {
            overrideParams.put(param.getParamName(), param.getParamValue());
         }
      }
      params = shared.getContextParams();
      if (params != null)
      {
         for (ParamValueMetaData param : params)
         {
            if (overrideParams.get(param.getParamName()) == null)
            {
               overrideParams.put(param.getParamName(), param.getParamValue());
            }
         }
      }

      for (String key : overrideParams.keySet())
      {
         context.addParameter(key, overrideParams.get(key));
      }

   }

   /**
    * Process a "init" event for this Context.
    */
   protected void init()
   {
      context.setConfigured(false);
      ok = true;

      if (!context.getOverride())
      {
         processContextConfig("context.xml", false);
         processContextConfig(getHostConfigPath(org.apache.catalina.startup.Constants.HostContextXml), false);
      }
      // This should come from the deployment unit
      processContextConfig(context.getConfigFile(), true);
   }

   protected void processContextConfig(String resourceName, boolean local)
   {
      ClassLoader oldCl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(this.getClass().getClassLoader());

      ContextMetaData contextMetaData = null;
      try
      {
         SchemaBinding schema = JBossXBBuilder.build(ContextMetaData.class);
         Unmarshaller u = UnmarshallerFactory.newInstance().newUnmarshaller();
         u.setSchemaValidation(false);
         u.setValidation(false);
         u.setEntityResolver(new JBossEntityResolver());
         
         InputStream is = null;
         try
         {
            if (local)
            {
               DeploymentUnit localUnit = deploymentUnitLocal.get();
               if (localUnit instanceof VFSDeploymentUnit)
               {
                  VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit)localUnit;
                  VirtualFile vf = vfsUnit.getFile(resourceName);
                  if (vf != null)
                     is = vf.openStream();
               }
            }

            if (is == null)
               is = getClass().getClassLoader().getResourceAsStream(resourceName);

            if (is != null)
               contextMetaData = ContextMetaData.class.cast(u.unmarshal(is, schema));
         }
         finally
         {
            if (is != null)
            {
               try
               {
                  is.close();
               }
               catch (IOException e)
               {
                  // Ignore
               }
            }
         }
      }
      catch (Exception e)
      {
         log.error("XML error parsing: " + resourceName, e);
         ok = false;
         return;
      }
      finally
      {
         SecurityActions.setContextClassLoader(oldCl);
      }

      if (contextMetaData != null)
      {
         try
         {
            if (contextMetaData.getAttributes() != null)
            {
               Iterator<QName> names = contextMetaData.getAttributes().keySet().iterator();
               while (names.hasNext())
               {
                  QName name = names.next();
                  String value = (String)contextMetaData.getAttributes().get(name);
                  // FIXME: This should be done by XB
                  value = StringPropertyReplacer.replaceProperties(value);
                  IntrospectionUtils.setProperty(context, name.getLocalPart(), value);
               }
            }

            TomcatService.addLifecycleListeners(context, contextMetaData.getListeners());

            // Context/Realm
            if (contextMetaData.getRealm() != null)
            {
               context.setRealm((org.apache.catalina.Realm)TomcatService.getInstance(contextMetaData.getRealm(), null));
            }

            // Context/Valve
            TomcatService.addValves(context, contextMetaData.getValves());

            // Context/InstanceListener
            if (contextMetaData.getInstanceListeners() != null)
            {
               Iterator<String> listeners = contextMetaData.getInstanceListeners().iterator();
               while (listeners.hasNext())
               {
                  context.addInstanceListener(listeners.next());
               }
            }

            // Context/Loader
            if (contextMetaData.getLoader() != null)
            {
               // This probably won't work very well in JBoss
               context.setLoader((org.apache.catalina.Loader)TomcatService.getInstance(contextMetaData.getLoader(), "org.apache.catalina.loader.WebappLoader"));
            }

            // Context/Manager
            if (contextMetaData.getManager() != null)
            {
               context.setManager((org.apache.catalina.Manager)TomcatService.getInstance(contextMetaData.getManager(),
                     "org.apache.catalina.session.StandardManager"));
            }

            // Context/Parameter
            if (contextMetaData.getParameters() != null)
            {
               Iterator<ParameterMetaData> parameterMetaDatas = contextMetaData.getParameters().iterator();
               while (parameterMetaDatas.hasNext())
               {
                  ParameterMetaData parameterMetaData = parameterMetaDatas.next();
                  context.addApplicationParameter((org.apache.catalina.deploy.ApplicationParameter)TomcatService.getInstance(parameterMetaData, null));
               }
            }

            // Context/Resources
            if (contextMetaData.getResources() != null)
            {
               context.setResources((javax.naming.directory.DirContext)TomcatService.getInstance(contextMetaData.getResources(),
                     "org.apache.naming.resources.FileDirContext"));
            }

            // Context/SessionCookie
            if (contextMetaData.getSessionCookie() != null)
            {
               SessionCookie sessionCookie = new SessionCookie();
               sessionCookie.setComment(contextMetaData.getSessionCookie().getComment());
               sessionCookie.setDomain(contextMetaData.getSessionCookie().getDomain());
               sessionCookie.setHttpOnly(contextMetaData.getSessionCookie().getHttpOnly());
               sessionCookie.setPath(contextMetaData.getSessionCookie().getPath());
               sessionCookie.setSecure(contextMetaData.getSessionCookie().getSecure());
               context.setSessionCookie(sessionCookie);
            }

         }
         catch (Exception e)
         {
            log.error("Error processing: " + resourceName, e);
            ok = false;
         }
      }
   }

   protected void destroy()
   {
      if (runDestroy)
      {
         super.destroy();
      }
   }

}

