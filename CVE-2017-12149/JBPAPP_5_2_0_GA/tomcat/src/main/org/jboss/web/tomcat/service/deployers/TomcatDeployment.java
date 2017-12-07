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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.security.SecurityUtil;
import org.jboss.virtual.VirtualFile;
import org.jboss.web.WebApplication;
import org.jboss.web.deployers.AbstractWarDeployment;
import org.jboss.web.tomcat.security.JaccContextValve;
import org.jboss.web.tomcat.security.RunAsListener;
import org.jboss.web.tomcat.security.SecurityAssociationValve;
import org.jboss.web.tomcat.security.SecurityContextEstablishmentValve;
import org.jboss.web.tomcat.service.TomcatInjectionContainer;
import org.jboss.web.tomcat.service.WebCtxLoader;
import org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve;
import org.jboss.web.tomcat.service.session.AbstractJBossManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.omg.CORBA.ORB;

/**
 * A tomcat web application deployment.
 * 
 * @author Scott.Stark@jboss.org
 * @author Costin Manolache
 * @author adrian@jboss.org
 * @version $Revision: 111877 $
 */
public class TomcatDeployment extends AbstractWarDeployment
{
   private static final Logger log = Logger.getLogger(TomcatDeployment.class);

   /**
    * The name of the war level context configuration descriptor
    */
   private static final String CONTEXT_CONFIG_FILE = "WEB-INF/context.xml";

   private DeployerConfig config;

   private final String[] javaVMs = { " jboss.management.local:J2EEServer=Local,j2eeType=JVM,name=localhost" };

   private final String serverName = "jboss";

   private final HashMap vhostToHostNames = new HashMap();

   private ORB orb = null;

   public ORB getORB()
   {
      return orb;
   }

   public void setORB(ORB orb)
   {
      this.orb = orb;
   }

   @Override
   public void init(Object containerConfig) throws Exception
   {
      this.config = (DeployerConfig)containerConfig;
      super.setJava2ClassLoadingCompliance(config.isJava2ClassLoadingCompliance());
      super.setUnpackWars(config.isUnpackWars());
      super.setLenientEjbLink(config.isLenientEjbLink());
      super.setDefaultSecurityDomain(config.getDefaultSecurityDomain());
   }

   @Override
   protected void performDeploy(WebApplication webApp, String warUrl) throws Exception
   {
      // Decode the URL as tomcat can't deal with paths with escape chars
      warUrl = URLDecoder.decode(warUrl, "UTF-8");
      webApp.setDomain(config.getCatalinaDomain());
      JBossWebMetaData metaData = webApp.getMetaData();
      String hostName = null;
      // Get any jboss-web/virtual-hosts
      List<String> vhostNames = metaData.getVirtualHosts();
      // Map the virtual hosts onto the configured hosts
      Iterator hostNames = mapVirtualHosts(vhostNames);
      if (hostNames.hasNext())
      {
         hostName = hostNames.next().toString();
      }
      else
      {
         hostNames = getDefaultHosts();
         if (hostNames.hasNext())
         {
            hostName = hostNames.next().toString();
         }
      }
      performDeployInternal(webApp, hostName, warUrl);
      while (hostNames.hasNext())
      {
         String additionalHostName = hostNames.next().toString();
         performDeployInternal(webApp, additionalHostName, warUrl);
      }
   }

   protected void performDeployInternal(WebApplication webApp, String hostName, String warUrlStr) throws Exception
   {
      JBossWebMetaData metaData = webApp.getMetaData();
      String ctxPath = metaData.getContextRoot();
      if (ctxPath.equals("/") || ctxPath.equals("/ROOT") || ctxPath.equals(""))
      {
         log.debug("deploy root context=" + ctxPath);
         ctxPath = "/";
         metaData.setContextRoot(ctxPath);
      }

      log.info("deploy, ctxPath=" + ctxPath);

      URL warUrl = new URL(warUrlStr);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      metaData.setContextLoader(loader);

      StandardContext context = (StandardContext)Class.forName(config.getContextClassName()).newInstance();

      DeploymentUnit depUnit = webApp.getDeploymentUnit();
      TomcatInjectionContainer injectionContainer = new TomcatInjectionContainer(webApp, depUnit, context, getPersistenceUnitDependencyResolver());

      Loader webLoader = depUnit.getAttachment(Loader.class);
      if (webLoader == null)
         webLoader = getWebLoader(depUnit, metaData, loader, warUrl, injectionContainer);

      webApp.setName(warUrl.getPath());
      webApp.setClassLoader(loader);
      webApp.setURL(warUrl);

      String objectNameS = config.getCatalinaDomain() + ":j2eeType=WebModule,name=//" + ((hostName == null) ? "localhost" : hostName) + ctxPath
            + ",J2EEApplication=none,J2EEServer=none";

      ObjectName objectName = new ObjectName(objectNameS);

      if (Registry.getRegistry(null, null).getMBeanServer().isRegistered(objectName))
         throw new DeploymentException("Web mapping already exists for deployment URL " + warUrlStr);

      Registry.getRegistry(null, null).registerComponent(context, objectName, config.getContextClassName());

      context.setConfigFile(CONTEXT_CONFIG_FILE);
      context.setInstanceManager(injectionContainer);
      context.setDefaultContextXml("context.xml");
      context.setDefaultWebXml("conf/web.xml");
      context.setPublicId(metaData.getPublicID());

      String docBase = depUnit.getAttachment("org.jboss.web.explicitDocBase", String.class);
      if (docBase == null)
         docBase = warUrl.getFile();

      context.setDocBase(docBase);

      // If there is an alt-dd set it
      if (metaData.getAlternativeDD() != null)
      {
         log.debug("Setting altDDName to: " + metaData.getAlternativeDD());
         context.setAltDDName(metaData.getAlternativeDD());
      }
      context.setJavaVMs(javaVMs);
      context.setServer(serverName);
      context.setSaveConfig(false);

      if (webLoader != null)
      {
         context.setLoader(webLoader);
      }
      else
      {
         context.setParentClassLoader(loader);
      }
      context.setDelegate(webApp.getJava2ClassLoadingCompliance());

      // Javac compatibility whenever possible
      String[] jspCP = getCompileClasspath(loader);
      StringBuffer classpath = new StringBuffer();
      for (int u = 0; u < jspCP.length; u++)
      {
         String repository = jspCP[u];
         if (repository == null)
            continue;
         if (repository.startsWith("file://"))
            repository = repository.substring(7);
         else if (repository.startsWith("file:"))
            repository = repository.substring(5);
         else
            continue;
         if (repository == null)
            continue;
         // ok it is a file. Make sure that is is a directory or jar file
         File fp = new File(repository);
         if (!fp.isDirectory())
         {
            // if it is not a directory, try to open it as a zipfile.
            try
            {
               // avoid opening .xml files
               if (fp.getName().toLowerCase().endsWith(".xml"))
                  continue;

               ZipFile zip = new ZipFile(fp);
               zip.close();
            }
            catch (IOException e)
            {
               continue;
            }

         }
         if (u > 0)
            classpath.append(File.pathSeparator);
         classpath.append(repository);
      }
      context.setCompilerClasspath(classpath.toString());

      // Set the session cookies flag according to metadata
      switch (metaData.getSessionCookies())
      {
         case JBossWebMetaData.SESSION_COOKIES_ENABLED:
            context.setCookies(true);
            log.debug("Enabling session cookies");
            break;
         case JBossWebMetaData.SESSION_COOKIES_DISABLED:
            context.setCookies(false);
            log.debug("Disabling session cookies");
            break;
         default:
            log.debug("Using session cookies default setting");
      }

      String metaDataSecurityDomain = metaData.getSecurityDomain();
      if (metaDataSecurityDomain != null)
         metaDataSecurityDomain = metaDataSecurityDomain.trim();
      
      // Add a valve to establish security context
      SecurityContextEstablishmentValve scevalve = new SecurityContextEstablishmentValve(metaDataSecurityDomain, SecurityUtil.unprefixSecurityDomain(config
            .getDefaultSecurityDomain()), SecurityActions.loadClass(config.getSecurityContextClassName()), getSecurityManagement());
      context.addValve(scevalve);

      // Add a valve to estalish the JACC context before authorization valves
      Certificate[] certs = null;
      CodeSource cs = new CodeSource(warUrl, certs);
      JaccContextValve jaccValve = new JaccContextValve(metaData, cs);
      context.addValve(jaccValve);

      // Set listener
      context.setConfigClass("org.jboss.web.tomcat.service.deployers.JBossContextConfig");
      context.addLifecycleListener(new EncListener(loader, webLoader, injectionContainer, webApp));

      // Pass the metadata to the RunAsListener via a thread local
      RunAsListener.metaDataLocal.set(metaData);
      JBossContextConfig.metaDataLocal.set(metaData);
      JBossContextConfig.metaDataShared.set(config.getSharedMetaData());
      JBossContextConfig.deployerConfig.set(config);

      JBossContextConfig.kernelLocal.set(kernel);
      JBossContextConfig.deploymentUnitLocal.set(unit);
      try
      {
         // Start it
         context.start();
         // Build the ENC
      }
      catch (Exception e)
      {
         context.destroy();
         DeploymentException.rethrowAsDeploymentException("URL " + warUrlStr + " deployment failed", e);
      }
      finally
      {
         RunAsListener.metaDataLocal.set(null);
         JBossContextConfig.metaDataLocal.set(null);
         JBossContextConfig.metaDataShared.set(null);
         JBossContextConfig.deployerConfig.set(null);

         JBossContextConfig.kernelLocal.set(null);
         JBossContextConfig.deploymentUnitLocal.set(null);
      }
      if (context.getState() != 1)
      {
         context.destroy();
         throw new DeploymentException("URL " + warUrlStr + " deployment failed");
      }

      // Clustering
      if (config.getOverrideDistributableManager() && metaData.getDistributable() != null)
      {
         // Try to initate clustering, fallback to standard if no clustering is
         // available
         try
         {
            AbstractJBossManager manager = null;
            String managerClassName = config.getManagerClass();
            Class managerClass = Thread.currentThread().getContextClassLoader().loadClass(managerClassName);
            manager = (AbstractJBossManager)managerClass.newInstance();
            String name = "//" + ((hostName == null) ? "localhost" : hostName) + ctxPath;
            manager.init(name, metaData);

            server.setAttribute(objectName, new Attribute("manager", manager));

            log.debug("Enabled clustering support for ctxPath=" + ctxPath);
         }
         catch (ClusteringNotSupportedException e)
         {
            // JBAS-3513 Just log a WARN, not an ERROR
            log.warn("Failed to setup clustering, clustering disabled. ClusteringNotSupportedException: " + e.getMessage());
         }
         catch (NoClassDefFoundError ncdf)
         {
            // JBAS-3513 Just log a WARN, not an ERROR
            log.debug("Classes needed for clustered webapp unavailable", ncdf);
            log.warn("Failed to setup clustering, clustering disabled. NoClassDefFoundError: " + ncdf.getMessage());
         }
         catch (Throwable t)
         {
            // TODO consider letting this through and fail the deployment
            log.error("Failed to setup clustering, clustering disabled. Exception: ", t);
         }
      }

      /*
       * Add security association valve after the authorization valves so that the authenticated user may be associated
       * with the request thread/session.
       */
      SecurityAssociationValve valve = new SecurityAssociationValve(metaData, config.getSecurityManagerService());
      valve.setSubjectAttributeName(config.getSubjectAttributeName());
      server.invoke(objectName, "addValve", new Object[] { valve }, new String[] { "org.apache.catalina.Valve" });

      /*
       * TODO: Retrieve the state, and throw an exception in case of a failure Integer state = (Integer)
       * server.getAttribute(objectName, "state"); if (state.intValue() != 1) { throw new DeploymentException("URL " +
       * warUrl + " deployment failed"); }
       */

      webApp.setAppData(objectName);

      /*
       * TODO: Create mbeans for the servlets ObjectName servletQuery = new ObjectName (config.getCatalinaDomain() +
       * ":j2eeType=Servlet,WebModule=" + objectName.getKeyProperty("name") + ",*"); Iterator iterator =
       * server.queryMBeans(servletQuery, null).iterator(); while (iterator.hasNext()) {
       * di.mbeans.add(((ObjectInstance)iterator.next()).getObjectName()); }
       */

      log.debug("Initialized: " + webApp + " " + objectName);
   }

   public class EncListener implements LifecycleListener
   {
      protected ClassLoader loader;

      protected Loader webLoader;

      protected WebApplication webApp;

      protected JBossWebMetaData metaData;

      protected DeploymentUnit unit;

      protected TomcatInjectionContainer injectionContainer;

      public EncListener(ClassLoader loader, Loader webLoader, TomcatInjectionContainer injectionContainer, WebApplication webApp)
      {
         this.loader = loader;
         this.webLoader = webLoader;
         this.injectionContainer = injectionContainer;
         this.webApp = webApp;
         this.metaData = webApp.getMetaData();
         this.unit = webApp.getDeploymentUnit();
      }

      public void lifecycleEvent(LifecycleEvent event)
      {

         if (event.getType().equals(StandardContext.AFTER_START_EVENT))
         {

            // make the context class loader known to the JBossWebMetaData, ws4ee needs it
            // to instanciate service endpoint pojos that live in this webapp
            metaData.setContextLoader(webLoader.getClassLoader());

            Thread currentThread = Thread.currentThread();
            ClassLoader currentLoader = currentThread.getContextClassLoader();
            try
            {
               // Create a java:comp/env environment unique for the web application
               log.debug("Creating ENC using ClassLoader: " + loader);
               ClassLoader parent = loader.getParent();
               while (parent != null)
               {
                  log.debug(".." + parent);
                  parent = parent.getParent();
               }
               // TODO: The enc should be an input?
               currentThread.setContextClassLoader(webLoader.getClassLoader());
               metaData.setENCLoader(webLoader.getClassLoader());
               InitialContext iniCtx = new InitialContext();
               Context envCtx = (Context)iniCtx.lookup("java:comp");
               // Add ORB/UserTransaction
               ORB orb = null;
               try
               {
                  ObjectName ORB_NAME = new ObjectName("jboss:service=CorbaORB");
                  orb = (ORB)server.getAttribute(ORB_NAME, "ORB");
                  // Bind the orb
                  if (orb != null)
                  {
                     NonSerializableFactory.rebind(envCtx, "ORB", orb);
                     log.debug("Bound java:comp/ORB");
                  }
               }
               catch (Throwable t)
               {
                  log.debug("Unable to retrieve orb: " + t.toString());
               }

               // JTA links
               envCtx.bind("TransactionSynchronizationRegistry", new LinkRef("java:TransactionSynchronizationRegistry"));
               log.debug("Linked java:comp/TransactionSynchronizationRegistry to JNDI name: java:TransactionSynchronizationRegistry");
               envCtx.bind("UserTransaction", new LinkRef("UserTransaction"));
               log.debug("Linked java:comp/UserTransaction to JNDI name: UserTransaction");
               envCtx = envCtx.createSubcontext("env");
               injectionContainer.populateEnc(webLoader.getClassLoader());

               // TODO: this should be bindings in the metadata
               currentThread.setContextClassLoader(webLoader.getClassLoader());
               String securityDomain = metaData.getSecurityDomain();
               log.debug("linkSecurityDomain");
               linkSecurityDomain(securityDomain, envCtx);

            }
            catch (Throwable t)
            {
               log.error("ENC setup failed", t);
               throw new RuntimeException(t);
            }
            finally
            {
               currentThread.setContextClassLoader(currentLoader);

               log.debug("injectionContainer enabled and processing beginning");
               // we need to do this because the classloader is initialize by the web container and
               // the injection container needs the classloader so that it can build up Injectors and ENC populators
               injectionContainer.setClassLoader(webLoader.getClassLoader());
               injectionContainer.processMetadata();
            }
         }
      }

   }

   public Loader getWebLoader(DeploymentUnit unit, JBossWebMetaData metaData, ClassLoader loader, URL rl, TomcatInjectionContainer injectionContainer) throws MalformedURLException
   {
      Loader webLoader;
      
      /*
       * If we are using the jboss class loader we need to augment its path to include the WEB-INF/{lib,classes} dirs or
       * else scoped class loading does not see the war level overrides. The call to setWarURL adds these paths to the
       * deployment UCL.
       */
      List<URL> classpath = unit.getAttachment("org.jboss.web.expandedWarClasspath", List.class);
      if (classpath == null && unit instanceof VFSDeploymentUnit)
      {
         VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit)unit;
         try
         {
            VirtualFile classes = vfsUnit.getFile("WEB-INF/classes");
            // Tomcat can't handle the vfs urls yet
            URL vfsURL = classes.toURL();
            String vfsurl = vfsURL.toString();
            if (vfsurl.startsWith("vfs"))
               vfsURL = new URL(vfsurl.substring(3));
            classpath = new ArrayList<URL>();
            classpath.add(vfsURL);
         }
         catch (Exception ignored)
         {
         }
      }

      WebCtxLoader jbossLoader = new WebCtxLoader(loader, injectionContainer);
      if (classpath != null)
         jbossLoader.setClasspath(classpath);

      webLoader = jbossLoader;
      return webLoader;
   }

   /**
    * Called as part of the undeploy() method template to ask the subclass for perform the web container specific
    * undeployment steps.
    */
   @Override
   protected void performUndeploy(WebApplication warInfo, String warUrl) throws Exception
   {
      if (warInfo == null)
      {
         log.debug("performUndeploy, no WebApplication found for URL " + warUrl);
         return;
      }

      log.info("undeploy, ctxPath=" + warInfo.getMetaData().getContextRoot());

      JBossWebMetaData metaData = warInfo.getMetaData();
      String hostName = null;
      // Get any jboss-web/virtual-hosts
      List<String> vhostNames = metaData.getVirtualHosts();
      // Map the virtual hosts onto the configured hosts
      Iterator hostNames = mapVirtualHosts(vhostNames);
      if (hostNames.hasNext())
      {
         hostName = hostNames.next().toString();
      }
      else
      {
         hostNames = getDefaultHosts();
         if (hostNames.hasNext())
         {
            hostName = hostNames.next().toString();
         }
      }
      performUndeployInternal(warInfo, hostName, warUrl);
      while (hostNames.hasNext())
      {
         String additionalHostName = hostNames.next().toString();
         performUndeployInternal(warInfo, additionalHostName, warUrl);
      }

   }

   protected void performUndeployInternal(WebApplication warInfo, String hostName, String warUrlStr) throws Exception
   {
      JBossWebMetaData metaData = warInfo.getMetaData();
      String ctxPath = metaData.getContextRoot();

      // TODO: Need to remove the dependency on MBeanServer
      MBeanServer server = MBeanServerLocator.locateJBoss();
      // If the server is gone, all apps were stopped already
      if (server == null)
         return;

      ObjectName objectName = new ObjectName(config.getCatalinaDomain() + ":j2eeType=WebModule,name=//" + ((hostName == null) ? "localhost" : hostName) + ctxPath
            + ",J2EEApplication=none,J2EEServer=none");

      if (server.isRegistered(objectName))
      {
         // Contexts should be stopped by the host already
         server.invoke(objectName, "destroy", new Object[] {}, new String[] {});
      }
   }

   /**
    * Resolve the input virtual host names to the names of the configured Hosts
    * 
    * @param vhostNames Iterator<String> for the jboss-web/virtual-host elements
    * @return Iterator<String> of the unique Host names
    * @throws Exception
    */
   protected synchronized Iterator mapVirtualHosts(List<String> vhostNames) throws Exception
   {
      if (vhostToHostNames.size() == 0)
      {
         // Query the configured Host mbeans
         String hostQuery = config.getCatalinaDomain() + ":type=Host,*";
         ObjectName query = new ObjectName(hostQuery);
         Set hosts = server.queryNames(query, null);
         Iterator iter = hosts.iterator();
         while (iter.hasNext())
         {
            ObjectName host = (ObjectName)iter.next();
            String name = host.getKeyProperty("host");
            if (name != null)
            {
               vhostToHostNames.put(name, name);
               String[] aliases = (String[])server.invoke(host, "findAliases", null, null);
               int count = aliases != null ? aliases.length : 0;
               for (int n = 0; n < count; n++)
               {
                  vhostToHostNames.put(aliases[n], name);
               }
            }
         }
      }

      // Map the virtual host names to the hosts
      HashSet hosts = new HashSet();
      if (vhostNames != null)
      {
         for (String vhost : vhostNames)
         {
            String host = (String)vhostToHostNames.get(vhost);
            if (host == null)
            {
               log.warn("Failed to map vhost: " + vhost);
               // This will cause a new host to be created
               host = vhost;
            }
            hosts.add(host);
         }
      }
      return hosts.iterator();
   }

   /**
    * Find the default hosts for all existing engines
    */
   protected synchronized Iterator getDefaultHosts() throws Exception
   {
      // Map the virtual host names to the hosts
      HashSet defaultHosts = new HashSet();
      // Query the configured Engine mbeans
      String engineQuery = config.getCatalinaDomain() + ":type=Engine,*";
      ObjectName query = new ObjectName(engineQuery);
      Set engines = server.queryNames(query, null);
      Iterator iter = engines.iterator();
      while (iter.hasNext())
      {
         ObjectName engine = (ObjectName)iter.next();

         String defaultHost = (String)server.getAttribute(engine, "defaultHost");
         if (defaultHost != null)
         {
            defaultHosts.add(defaultHost);
         }
      }
      return defaultHosts.iterator();
   }
   
   /**
    * Traverse the parent chain of the context to reach the Catalina Engine
    * @param context Context of the web application
    * @return
    */
   private Engine getCatalinaEngine(org.apache.catalina.Context context)
   {
	   Container parentContainer = context.getParent();
	   while(parentContainer != null && !(parentContainer instanceof Engine))
		   parentContainer = parentContainer.getParent();
	   return (Engine) parentContainer;
   }

}
