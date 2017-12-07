/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.web.deployers;

// $Id: AbstractWarDeployment.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb.EjbUtil50;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferencesMetaData;
import org.jboss.metadata.serviceref.ServiceReferenceHandler;
import org.jboss.metadata.serviceref.VirtualFileAdaptor;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.naming.Util;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityConstants;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.web.WebApplication;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.virtual.VFSUtils;
import org.omg.CORBA.ORB;

/**
An abstract web app deployment bean. Subclasses implement:

- init(Object) to initialize from the deployment configuration java bean passed
in from the AbstractWarDeployer instance.
- performDeploy(WebApplication webApp, String warUrl) to translate the
WebApplication data into a running web application. This is called when the
AbstractWarDeployment is started.
- performUndeploy(WebApplication webApp, String warUrl) to remove the application
corresponding to the WebApplication data. This is called when the
AbstractWarDeployment is stopped.

The one thing to be aware of is the relationship between the thread context
class loader and the JNDI ENC context. Any method that attempts to access
the JNDI ENC context must have the ClassLoader in the WebApplication returned
from the {@link #performDeploy(WebApplication, String, WebDescriptorParser) performDeploy} as its thread
context ClassLoader or else the lookup for java:comp/env will fail with a
name not found exception, or worse, it will receive some other web application
ENC context.
TODO: the enc should be managed outside of the container without relying
on the TCL behavior.

@author  Scott.Stark@jboss.org
@author Anil.Saldhana@redhat.com
@author adrian@jboss.org
@version $Revision: 85945 $
*/
public abstract class AbstractWarDeployment
{
   public static final String ERROR = "org.jboss.web.AbstractWebContainer.error";
   protected Logger log;

   protected MBeanServer server;
   protected MainDeployerStructure mainDeployer;
   // servlet context attributes
   protected Kernel kernel;
   protected DeploymentUnit unit;

   /**
    * The parent class loader first model flag
    */
   protected boolean java2ClassLoadingCompliance = false;
   /**
    * A flag indicating if war archives should be unpacked
    */
   protected boolean unpackWars = true;
   /**
    * If true, ejb-links that don't resolve don't cause an error (fallback to
    * jndi-name)
    */
   protected boolean lenientEjbLink = false;

   /**
    * The default security-domain name to use
    */
   protected String defaultSecurityDomain;

   /** The Security PolicyRegistration Name **/
   protected String policyRegistrationName;

   /** The Security PolicyRegistration **/
   protected PolicyRegistration policyRegistration;

   /** The security management name */
   protected String securityManagementName;

   /** The security management */
   protected ISecurityManagement securityManagement;

   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;

   public AbstractWarDeployment()
   {
      log = Logger.getLogger(getClass());
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public void setDeploymentUnit(DeploymentUnit unit)
   {
      this.unit = unit;
   }

   /**
    * Utility method that builds a string url based on the ServerConfig.SERVER_HOME_URL system
    * property and the input url. If the input url is under the SERVER_HOME_URL, the SERVER_HOME_URL
    * prefix is replaced with ".../".
    * @param warUrl
    * @return the possibly shorted war url string.
    */
   public static String shortWarUrlFromServerHome(String warUrl)
   {
      String serverHomeUrl = System.getProperty(org.jboss.bootstrap.spi.ServerConfig.SERVER_HOME_URL);

      if (warUrl == null || serverHomeUrl == null)
         return warUrl;

      if (warUrl.startsWith(serverHomeUrl))
         return ".../" + warUrl.substring(serverHomeUrl.length());
      else
         return warUrl;
   }

   /**
    * Initialize the deployment using an instance specific configuration object.
    * @param containerConfig
    * @throws Exception
    */
   public abstract void init(Object containerConfig) throws Exception;

   public MBeanServer getServer()
   {
      return server;
   }

   public void setServer(MBeanServer server)
   {
      this.server = server;
   }

   public MainDeployerStructure getMainDeployer()
   {
      return mainDeployer;
   }

   public void setMainDeployer(MainDeployerStructure mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   /**
    * Get the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public boolean getJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }

   /**
    * Set the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @param flag true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      java2ClassLoadingCompliance = flag;
   }

   /**
    * Get the flag indicating if war archives should be unpacked. This may need
    * to be set to false as long extraction paths under deploy can show up as
    * deployment failures on some platforms.
    * @return true is war archives should be unpacked
    * @jmx.managed-attribute
    */
   public boolean getUnpackWars()
   {
      return unpackWars;
   }

   /**
    * Get the flag indicating if war archives should be unpacked. This may need
    * to be set to false as long extraction paths under deploy can show up as
    * deployment failures on some platforms.
    * @param flag , true is war archives should be unpacked
    * @jmx.managed-attribute
    */
   public void setUnpackWars(boolean flag)
   {
      this.unpackWars = flag;
   }

   /**
    * Get the flag indicating if ejb-link errors should be ignored in favour of
    * trying the jndi-name in jboss-web.xml
    * @return a <code>boolean</code> value
    * @jmx.managed-attribute
    */
   public boolean getLenientEjbLink()
   {
      return lenientEjbLink;
   }

   /**
    * Set the flag indicating if ejb-link errors should be ignored in favour of
    * trying the jndi-name in jboss-web.xml
    * @jmx.managed-attribute
    */
   public void setLenientEjbLink(boolean flag)
   {
      lenientEjbLink = flag;
   }

   /**
    * Get the default security domain implementation to use if a war does not
    * declare a security-domain.
    * @return jndi name of the security domain binding to use.
    * @jmx.managed-attribute
    */
   public String getDefaultSecurityDomain()
   {
      if (defaultSecurityDomain == null)
         throw new IllegalStateException("Default Security Domain is null");
      return defaultSecurityDomain;
   }

   /**
    * Set the default security domain implementation to use if a war does not
    * declare a security-domain.
    * @param defaultSecurityDomain - jndi name of the security domain binding to
    * use.
    * @jmx.managed-attribute
    */
   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

   protected PersistenceUnitDependencyResolver getPersistenceUnitDependencyResolver()
   {
      return persistenceUnitDependencyResolver;
   }

   @Inject
   public void setPersistenceUnitDependencyResolver(PersistenceUnitDependencyResolver resolver)
   {
      this.persistenceUnitDependencyResolver = resolver;
   }

   /**
    * Get the Policy Registration Name
    * @return
    */
   public String getPolicyRegistrationName()
   {
      return policyRegistrationName;
   }

   /**
    * Set the Policy Registration Name
    * @param policyRegistrationName
    */
   public void setPolicyRegistrationName(String policyRegistrationName)
   {
      this.policyRegistrationName = policyRegistrationName;
   }

   /**
    * Get the PolicyRegistration Bean
    * @return
    */
   public PolicyRegistration getPolicyRegistration()
   {
      return policyRegistration;
   }

   /**
    * Set the PolicyRegistration Bean
    * @param policyRegistration
    */
   public void setPolicyRegistration(PolicyRegistration policyRegistration)
   {
      this.policyRegistration = policyRegistration;
   }

   /**
    * Get the securityManagement.
    *
    * @return the securityManagement.
    */
   public String getSecurityManagementName()
   {
      return securityManagementName;
   }

   /**
    * Set the securityManagement.
    *
    * @param securityManagement the securityManagement.
    */
   public void setSecurityManagementName(String securityManagement)
   {
      this.securityManagementName = securityManagement;
   }

   /**
    * Get the securityManagement.
    *
    * @return the securityManagement.
    */
   public ISecurityManagement getSecurityManagement()
   {
      return securityManagement;
   }

   /**
    * Set the securityManagement.
    *
    * @param securityManagement the securityManagement.
    */
   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   /**
    * A template pattern implementation of the deploy() method. This method
    * calls the {@link #performDeploy(WebApplication, String,
      * WebDescriptorParser) performDeploy()} method to perform the container
    * specific deployment steps and registers the returned WebApplication in the
    * deployment map. The steps performed are:
    *
    * ClassLoader appClassLoader = thread.getContextClassLoader();
    * URLClassLoader warLoader = URLClassLoader.newInstance(empty,
    * appClassLoader); thread.setContextClassLoader(warLoader);
    * WebDescriptorParser webAppParser = ...; WebMetaData metaData =
    * di.metaData; // Create JACC permissions, contextID, etc. ...
    * WebApplication warInfo = new WebApplication(metaData);
    * performDeploy(warInfo, warUrl, webAppParser); deploymentMap.put(warUrl,
    * warInfo); thread.setContextClassLoader(appClassLoader);
    *
    * The subclass performDeploy() implementation needs to invoke
    * processEnc(loader, warInfo) to have the JNDI
    * java:comp/env namespace setup before any web app component can access this
    * namespace.
    *
    * Also, an MBean for each servlet deployed should be created and its JMX
    * ObjectName placed into the DeploymentInfo.mbeans list so that the JSR77
    * layer can create the approriate model view. The servlet MBean needs to
    * provide access to the min, max and total time in milliseconds. Expose this
    * information via MinServiceTime, MaxServiceTime and TotalServiceTime
    * attributes to integrate seemlessly with the JSR77 factory layer.
    * @param unit The deployment info that contains the context-root element value
    * from the J2EE application/module/web application.xml descriptor. This may
    * be null if war was is not being deployed as part of an enterprise
    * application. It also contains the URL of the web application war.
    */
   public synchronized WebApplication start(DeploymentUnit unit, JBossWebMetaData metaData) throws Exception
   {
      Thread thread = Thread.currentThread();
      ClassLoader appClassLoader = thread.getContextClassLoader();
      WebApplication webApp = null;
      try
      {
         // Create a classloader for the war to ensure a unique ENC
         ClassLoader warLoader = unit.getClassLoader();
         thread.setContextClassLoader(warLoader);
         String webContext = metaData.getContextRoot();

         // Get the war URL
         URL warUrl = unit.getAttachment("org.jboss.web.expandedWarURL", URL.class);
         if (warUrl == null && unit instanceof VFSDeploymentUnit)
         {
            VFSDeploymentUnit vdu = VFSDeploymentUnit.class.cast(unit);
            warUrl = VFSUtils.getRealURL(vdu.getRoot());
         }

         // Dynamic WebMetaData deployments might not provide an URL
         // We use the DEploymentUnit name as identifier instead.
         // The JAXWS Endpoint API for example does this.
         String warURLString = (warUrl != null ? warUrl.toExternalForm() : unit.getName());

         // Strip any jar: url syntax. This should be be handled by the vfs
         if (warURLString.startsWith("jar:"))
            warURLString = warURLString.substring(4, warURLString.length() - 2);

         log.debug("webContext: " + webContext);
         log.debug("warURL: " + warURLString);

         // Register the permissions with the JACC layer
         String contextID = metaData.getJaccContextID();
         if (contextID == null)
            contextID = unit.getSimpleName();
         metaData.setJaccContextID(contextID);

         webApp = new WebApplication(metaData);
         webApp.setClassLoader(warLoader);
         webApp.setDeploymentUnit(unit);
         performDeploy(webApp, warURLString);
      }
      finally
      {
         thread.setContextClassLoader(appClassLoader);
      }
      return webApp;
   }

   /**
    * A template pattern implementation of the undeploy() method. This method
    * calls the {@link #performUndeploy(String, WebApplication)
    * performUndeploy()} method to perform the container specific undeployment
    * steps and unregisters the the warUrl from the deployment map.
    */
   public synchronized void stop(DeploymentUnit di, WebApplication webApp) throws Exception
   {
      URL warURL = webApp.getURL();
      String warUrl = warURL.toString();
      performUndeploy(webApp, warUrl);
   }

   /**
    * This method is called by the start() method template and must be
    * overriden by subclasses to perform the web container specific deployment
    * steps.
    * @param webApp The web application information context. This contains the
    * metadata such as the context-root element value from the J2EE
    * application/module/web application.xml descriptor and virtual-host.
    * @param warUrlStr The string for the URL of the web application war.
    */
   protected abstract void performDeploy(WebApplication webApp, String warUrlStr) throws Exception;

   /**
    * Called as part of the stop() method template to ask the subclass for
    * perform the web container specific undeployment steps.
    * @param webApp The web application information context. This contains the
    * metadata such as the context-root element value from the J2EE
    * application/module/web application.xml descriptor and virtual-host.
    * @param warUrlStr The string for the URL of the web application war.
    */
   protected abstract void performUndeploy(WebApplication webApp, String warUrlStr) throws Exception;

   /**
    * This method is invoked from within subclass performDeploy() method
    * implementations when they invoke WebDescriptorParser.parseWebAppDescriptors().
    * @param loader the ClassLoader for the web application. May not be null.
    * @param metaData the WebMetaData from the WebApplication object passed to
    * the performDeploy method.
    */
   protected void processEnc(ClassLoader loader, WebApplication webApp) throws Exception
   {
      if (loader == null)
         throw new IllegalArgumentException("Classloader passed to process ENC refs is null");
      log.debug("AbstractWebContainer.parseWebAppDescriptors, Begin");
      InitialContext iniCtx = new InitialContext();
      Context envCtx = null;
      Thread currentThread = Thread.currentThread();
      ClassLoader currentLoader = currentThread.getContextClassLoader();
      JBossWebMetaData metaData = webApp.getMetaData();
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
         currentThread.setContextClassLoader(loader);
         // webApp.setENCLoader(loader);
         envCtx = (Context)iniCtx.lookup("java:comp");

         // TODO: inject the ORB
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
            log.debug("Unable to retrieve orb" + t.toString());
         }

         // TODO: injection, Add a link to the global transaction manager
         envCtx.bind("UserTransaction", new LinkRef("UserTransaction"));
         log.debug("Linked java:comp/UserTransaction to JNDI name: UserTransaction");
         envCtx = envCtx.createSubcontext("env");
         processEncReferences(webApp, envCtx);
      }
      finally
      {
         currentThread.setContextClassLoader(currentLoader);
      }

      String securityDomain = metaData.getSecurityDomain();
      log.debug("linkSecurityDomain");
      linkSecurityDomain(securityDomain, envCtx);
      log.debug("AbstractWebContainer.parseWebAppDescriptors, End");
   }

   protected void processEncReferences(WebApplication webApp, Context envCtx) throws ClassNotFoundException, NamingException
   {
      DeploymentUnit unit = webApp.getDeploymentUnit();
      JBossWebMetaData metaData = webApp.getMetaData();
      EnvironmentEntriesMetaData envEntries = metaData.getEnvironmentEntries();
      log.debug("addEnvEntries");
      addEnvEntries(envEntries, envCtx);
      ResourceEnvironmentReferencesMetaData resourceEnvRefs = metaData.getResourceEnvironmentReferences();
      log.debug("linkResourceEnvRefs");
      linkResourceEnvRefs(resourceEnvRefs, envCtx);
      ResourceReferencesMetaData resourceRefs = metaData.getResourceReferences();
      log.debug("linkResourceRefs");
      linkResourceRefs(resourceRefs, envCtx);
      log.debug("linkMessageDestinationRefs");
      MessageDestinationReferencesMetaData msgRefs = metaData.getMessageDestinationReferences();
      linkMessageDestinationRefs(unit, msgRefs, envCtx);
      EJBReferencesMetaData ejbRefs = metaData.getEjbReferences();
      log.debug("linkEjbRefs");
      linkEjbRefs(unit, ejbRefs, envCtx);
      EJBLocalReferencesMetaData ejbLocalRefs = metaData.getEjbLocalReferences();
      log.debug("linkEjbLocalRefs");
      linkEjbLocalRefs(unit, ejbLocalRefs, envCtx);
      log.debug("linkServiceRefs");
      ServiceReferencesMetaData serviceRefs = metaData.getServiceReferences();
      linkServiceRefs(unit, serviceRefs, envCtx);
   }

   private void linkServiceRefs(DeploymentUnit unit, ServiceReferencesMetaData serviceRefs, Context envCtx) throws NamingException
   {
      if (unit instanceof VFSDeploymentUnit)
      {
         VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit)unit;
         ClassLoader loader = unit.getClassLoader();
         UnifiedVirtualFile vfsRoot = new VirtualFileAdaptor(vfsUnit.getRoot());
         for (ServiceReferenceMetaData sref : serviceRefs)
         {
            String refName = sref.getServiceRefName();
            new ServiceReferenceHandler().bindServiceRef(envCtx, refName, vfsRoot, loader, sref);
         }
      }
   }

   protected void addEnvEntries(EnvironmentEntriesMetaData envEntries, Context envCtx) throws ClassNotFoundException, NamingException
   {
      for (EnvironmentEntryMetaData entry : envEntries)
      {
         log.debug("Binding env-entry: " + entry.getName() + " of type: " + entry.getType() + " to value:" + entry.getValue());
         bindEnvEntry(envCtx, entry);
      }
   }

   protected void linkResourceEnvRefs(ResourceEnvironmentReferencesMetaData resourceEnvRefs, Context envCtx) throws NamingException
   {
      for (ResourceEnvironmentReferenceMetaData ref : resourceEnvRefs)
      {
         String resourceName = ref.getJndiName();
         String refName = ref.getResourceEnvRefName();
         if (ref.getType().equals("java.net.URL"))
         {
            try
            {
               log.debug("Binding '" + refName + "' to URL: " + resourceName);
               URL url = new URL(resourceName);
               Util.bind(envCtx, refName, url);
            }
            catch (MalformedURLException e)
            {
               throw new NamingException("Malformed URL:" + e.getMessage());
            }
         }
         else if (resourceName != null)
         {
            log.debug("Linking '" + refName + "' to JNDI name: " + resourceName);
            Util.bind(envCtx, refName, new LinkRef(resourceName));
         }
         else
         {
            throw new NamingException("resource-env-ref: " + refName + " has no valid JNDI binding. Check the jboss-web/resource-env-ref.");
         }
      }
   }

   protected void linkResourceRefs(ResourceReferencesMetaData resourceRefs, Context envCtx) throws NamingException
   {
      for (ResourceReferenceMetaData ref : resourceRefs)
      {
         String jndiName = ref.getJndiName();
         String refName = ref.getResourceName();
         if (ref.getType().equals("java.net.URL"))
         {
            try
            {
               String resURL = ref.getResUrl();
               if (resURL != null)
               {
                  log.debug("Binding '" + refName + "' to URL: " + resURL);
                  URL url = new URL(resURL);
                  Util.bind(envCtx, refName, url);
               }
               else
               {
                  log.debug("Linking '" + refName + "' to URL: " + resURL);
                  LinkRef urlLink = new LinkRef(jndiName);
                  Util.bind(envCtx, refName, urlLink);
               }
            }
            catch (MalformedURLException e)
            {
               throw new NamingException("Malformed URL:" + e.getMessage());
            }
         }
         else if (jndiName != null)
         {
            log.debug("Linking '" + refName + "' to JNDI name: " + jndiName);
            Util.bind(envCtx, refName, new LinkRef(jndiName));
         }
         else
         {
            throw new NamingException("resource-ref: " + refName + " has no valid JNDI binding. Check the jboss-web/resource-ref.");
         }
      }
   }

   protected void linkMessageDestinationRefs(DeploymentUnit unit, MessageDestinationReferencesMetaData msgRefs, Context envCtx) throws NamingException
   {
      for (MessageDestinationReferenceMetaData ref : msgRefs)
      {
         String refName = ref.getName();
         String jndiName = ref.getJndiName();
         String link = ref.getLink();
         if (link != null)
         {
            if (jndiName == null)
            {
               MessageDestinationMetaData messageDestination = EjbUtil50.findMessageDestination(mainDeployer, unit, link);
               if (messageDestination == null)
                  throw new NamingException("message-destination-ref '" + refName + "' message-destination-link '" + link
                        + "' not found and no jndi-name in jboss-web.xml");
               else
               {
                  String linkJNDIName = messageDestination.getJndiName();
                  if (linkJNDIName == null)
                     log.warn("message-destination '" + link + "' has no jndi-name in jboss-web.xml");
                  else
                     jndiName = linkJNDIName;
               }
            }
            else
               log.warn("message-destination-ref '" + refName + "' ignoring message-destination-link '" + link + "' because it has a jndi-name in jboss-web.xml");
         }
         else if (jndiName == null)
            throw new NamingException("message-destination-ref '" + refName + "' has no message-destination-link in web.xml and no jndi-name in jboss-web.xml");
         Util.bind(envCtx, refName, new LinkRef(jndiName));
      }
   }

   protected void linkEjbRefs(DeploymentUnit unit, EJBReferencesMetaData ejbRefs, Context envCtx) throws NamingException
   {
      for (EJBReferenceMetaData ejb : ejbRefs)
      {
         String name = ejb.getName();
         String linkName = ejb.getLink();
         String jndiName = null;

         // use ejb-link if it is specified
         if (linkName != null)
         {
            jndiName = EjbUtil50.findEjbLink(mainDeployer, unit, linkName);

            // if flag does not allow misconfigured ejb-links, it is an error
            if ((jndiName == null) && !(getLenientEjbLink()))
               throw new NamingException("ejb-ref: " + name + ", no ejb-link match");
         }

         // fall through to the jndiName
         if (jndiName == null)
         {
            jndiName = ejb.getJndiName();
            if (jndiName == null)
               throw new NamingException("ejb-ref: " + name + ", no ejb-link in web.xml and no jndi-name in jboss-web.xml");
         }

         log.debug("Linking ejb-ref: " + name + " to JNDI name: " + jndiName);
         Util.bind(envCtx, name, new LinkRef(jndiName));
      }
   }

   protected void linkEjbLocalRefs(DeploymentUnit unit, EJBLocalReferencesMetaData ejbLocalRefs, Context envCtx) throws NamingException
   {
      for (EJBLocalReferenceMetaData ejb : ejbLocalRefs)
      {
         String name = ejb.getName();
         String linkName = ejb.getLink();
         String jndiName = null;

         // use the ejb-link field if it is specified
         if (linkName != null)
         {
            jndiName = EjbUtil50.findLocalEjbLink(mainDeployer, unit, linkName);

            // if flag does not allow misconfigured ejb-links, it is an error
            if ((jndiName == null) && !(getLenientEjbLink()))
               throw new NamingException("ejb-ref: " + name + ", no ejb-link match");
         }

         if (jndiName == null)
         {
            jndiName = ejb.getJndiName();
            if (jndiName == null)
            {
               String msg = null;
               if (linkName == null)
               {
                  msg = "ejb-local-ref: '" + name + "', no ejb-link in web.xml and " + "no local-jndi-name in jboss-web.xml";
               }
               else
               {
                  msg = "ejb-local-ref: '" + name + "', with web.xml ejb-link: '" + linkName + "' failed to resolve to an ejb with a LocalHome";
               }
               throw new NamingException(msg);
            }
         }

         log.debug("Linking ejb-local-ref: " + name + " to JNDI name: " + jndiName);
         Util.bind(envCtx, name, new LinkRef(jndiName));
      }
   }

   /**
    * This creates a java:comp/env/security context that contains a securityMgr
    * binding pointing to an AuthenticationManager implementation and a
    * realmMapping binding pointing to a RealmMapping implementation. If the
    * jboss-web.xml descriptor contained a security-domain element then the
    * bindings are LinkRefs to the jndi name specified by the security-domain
    * element. If there was no security-domain element then the bindings are to
    * NullSecurityManager instance which simply allows all access.
    */
   protected void linkSecurityDomain(String securityDomain, Context envCtx) throws NamingException
   {
      if (securityDomain == null)
      {
         securityDomain = getDefaultSecurityDomain();
         log.debug("No security-domain given, using default: " + securityDomain);
      }

      // JBAS-6060: Tolerate a Security Domain configuration without the java:/jaas prefix
      if (securityDomain.startsWith(SecurityConstants.JAAS_CONTEXT_ROOT) == false)
         securityDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + securityDomain;

      log.debug("Linking security/securityMgr to JNDI name: " + securityDomain);
      Util.bind(envCtx, "security/securityMgr", new LinkRef(securityDomain));
      Util.bind(envCtx, "security/realmMapping", new LinkRef(securityDomain + "/realmMapping"));
      Util.bind(envCtx, "security/authorizationMgr", new LinkRef(securityDomain + "/authorizationMgr"));
      Util.bind(envCtx, "security/security-domain", new LinkRef(securityDomain));
      Util.bind(envCtx, "security/subject", new LinkRef(securityDomain + "/subject"));
   }

   /**
    * A utility method that searches the given loader for the resources:
    * "javax/servlet/resources/web-app_2_3.dtd", "org/apache/jasper/resources/jsp12.dtd",
    * and "javax/ejb/EJBHome.class" and returns an array of URL strings. Any
    * jar: urls are reduced to the underlying <url> portion of the
    * 'jar:<url>!/{entry}' construct.
    */
   public String[] getStandardCompileClasspath(ClassLoader loader)
   {
      String[] jspResources = { "javax/servlet/resources/web-app_2_3.dtd", "org/apache/jasper/resources/jsp12.dtd", "javax/ejb/EJBHome.class" };
      ArrayList tmp = new ArrayList();
      for (int j = 0; j < jspResources.length; j++)
      {
         URL rsrcURL = loader.getResource(jspResources[j]);
         if (rsrcURL != null)
         {
            String url = rsrcURL.toExternalForm();
            if (rsrcURL.getProtocol().equals("jar"))
            {
               // Parse the jar:<url>!/{entry} URL
               url = url.substring(4);
               int seperator = url.indexOf('!');
               url = url.substring(0, seperator);
            }
            tmp.add(url);
         }
         else
         {
            log.warn("Failed to fin jsp rsrc: " + jspResources[j]);
         }
      }
      log.trace("JSP StandardCompileClasspath: " + tmp);
      String[] cp = new String[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }

   /**
    * A utility method that walks up the ClassLoader chain starting at the given
    * loader and queries each ClassLoader for a 'URL[] getURLs()' method from
    * which a complete classpath of URL strings is built.
    */
   public String[] getCompileClasspath(ClassLoader loader)
   {
      HashSet tmp = new HashSet();
      ClassLoader cl = loader;
      while (cl != null)
      {
         URL[] urls = AbstractWarDeployer.getClassLoaderURLs(cl);
         addURLs(tmp, urls);
         cl = cl.getParent();
      }
      try
      {
         URL[] globalUrls = (URL[])server.getAttribute(LoaderRepositoryFactory.DEFAULT_LOADER_REPOSITORY, "URLs");
         addURLs(tmp, globalUrls);
      }
      catch (Exception e)
      {
         log.warn("Could not get global URL[] from default loader repository!", e);
      } // end of try-catch
      log.trace("JSP CompileClasspath: " + tmp);
      String[] cp = new String[tmp.size()];
      tmp.toArray(cp);
      return cp;
   }

   private void addURLs(Set urlSet, URL[] urls)
   {
      for (int u = 0; u < urls.length; u++)
      {
         URL url = urls[u];
         urlSet.add(url.toExternalForm());
      }
   }

   public static void bindEnvEntry(Context ctx, EnvironmentEntryMetaData entry) throws ClassNotFoundException, NamingException
   {
      ClassLoader loader = EnvironmentEntryMetaData.class.getClassLoader();
      Class type = loader.loadClass(entry.getType());
      if (type == String.class)
      {
         Util.bind(ctx, entry.getName(), entry.getValue());
      }
      else if (type == Integer.class)
      {
         Util.bind(ctx, entry.getName(), new Integer(entry.getValue()));
      }
      else if (type == Long.class)
      {
         Util.bind(ctx, entry.getName(), new Long(entry.getValue()));
      }
      else if (type == Double.class)
      {
         Util.bind(ctx, entry.getName(), new Double(entry.getValue()));
      }
      else if (type == Float.class)
      {
         Util.bind(ctx, entry.getName(), new Float(entry.getValue()));
      }
      else if (type == Byte.class)
      {
         Util.bind(ctx, entry.getName(), new Byte(entry.getValue()));
      }
      else if (type == Character.class)
      {
         Object value = null;
         String input = entry.getValue();
         if (input == null || input.length() == 0)
         {
            value = new Character((char)0);
         }
         else
         {
            value = new Character(input.charAt(0));
         }
         Util.bind(ctx, entry.getName(), value);
      }
      else if (type == Short.class)
      {
         Util.bind(ctx, entry.getName(), new Short(entry.getValue()));
      }
      else if (type == Boolean.class)
      {
         Util.bind(ctx, entry.getName(), new Boolean(entry.getValue()));
      }
      else
      {
         // Default to a String type
         Util.bind(ctx, entry.getName(), entry.getValue());
      }
   }
}
