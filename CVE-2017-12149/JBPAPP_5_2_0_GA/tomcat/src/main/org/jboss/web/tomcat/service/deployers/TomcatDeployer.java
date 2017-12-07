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
package org.jboss.web.tomcat.service.deployers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.web.jboss.JBoss50WebMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.Web25MetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.web.deployers.AbstractWarDeployer;
import org.jboss.web.deployers.AbstractWarDeployment;
import org.jboss.web.tomcat.service.session.SessionIDGenerator;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;

/**
 * A concrete implementation of the AbstractWarDeployer that creates {@link #TomcatDeployment} instances as the web
 * application bean representation.
 * 
 * @see #getDeployment(VFSDeploymentUnit, WebMetaData)
 * 
 * @author Scott.Stark@jboss.org
 * @author Costin Manolache
 * @author Wonne.Keysers@realsoftware.be
 * @author Dimitris.Andreadis@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 91181 $
 * @see org.jboss.web.deployers.AbstractWarDeployer
 */
public class TomcatDeployer extends AbstractWarDeployer implements TomcatDeployerMBean
{
   // default object name
   public static final ObjectName TOMCAT_SERVICE_NAME = ObjectNameFactory.create("jboss.web:service=WebServer");

   // Constants -----------------------------------------------------
   public static final String NAME = "TomcatDeployer";

   /** The web app context implementation class */
   private String contextClassName = "org.apache.catalina.core.StandardContext";

   /**
    * Unmarshall factory used for parsing shared web.xml.
    */
   private static final UnmarshallerFactory factory = UnmarshallerFactory.newInstance();

   /**
    * Configurable map of tomcat authenticators Keyed in by the http auth method that gets plugged into the Context
    * Config and then into the StandardContext
    */
   private Properties authenticators = null;

   /**
    * Domain for tomcat6 mbeans
    */
   private String catalinaDomain = "Catalina";

   /**
    * The fully qualified name of the class that will be used for session management if <tt>distributable</tt> is set
    * to true.
    */
   protected String managerClass = "org.jboss.web.tomcat.service.session.JBossCacheManager";
   
   /** Whether to override the Manager impl for distributable webapps */
   private boolean overrideDistributableManager = true;

   /**
    * A flag indicating if the JBoss Loader should be used
    */
   private boolean useJBossWebLoader = false; // set to false as it was in war-deployers-jboss-beans.xml

   /**
    * JBAS-3358: Work directory shouldn't be deleted on Context Destroy
    */
   private boolean deleteWorkDirOnContextDestroy = false;

   /**
    * JBAS-2283: Provide custom header based auth support
    */
   private String httpHeaderForSSOAuth = null;

   private String sessionCookieForSSOAuth = null;

   /**
    * Shared metaData.
    */
   private JBossWebMetaData sharedMetaData = null;

   /**
    * The server xml configuration file name
    */
   private String serverConfigFile = "server.xml";

   /**
    * Get the request attribute name under which the JAAS Subject is store
    */
   private String subjectAttributeName = null;

   /**
    * Flag indicating whether web-app specific context xmls may set the privileged flag.
    */
   private boolean allowSelfPrivilegedWebApps = false;

   /** The service used to flush authentication cache on session invalidation. */
   private JaasSecurityManagerServiceMBean secMgrService;

   /** The AbstractWarDeployment implementation class */
   private String deploymentClass = null;

   /** The classloader for the Tomcat SAR */
   private ClassLoader serviceClassLoader = null;

   /** The JBoss Security Manager Wrapper */
   private String securityManagement;

   /** FQN of the SecurityContext Class */
   private String securityContextClassName;

   /** */
   private String[] filteredPackages;

   private boolean runtimeLifecycleCoupled = false;

   private String policyRegistrationName;

   public TomcatDeployer()
   {
   }

   public String getName()
   {
      return NAME;
   }

   public String getManagerClass()
   {
      return managerClass;
   }

   public void setManagerClass(String managerClass)
   {
      this.managerClass = managerClass;
   }

   /**
    * Gets whether the session <code>Manager</code> implementation for 
    * distributable webapps should be overridden with an instance of 
    * {@link #getManagerClass()}.
    * <p>
    * Setting this to <code>false</code> allows custom configuration of
    * a manager via a <code>context.xml</code>. Default is <code>true</code>.
    * </p>
    * 
    * @return <code>true</code> if the manager should be overridden,
    *         <code>false</code> if the existing manager should be retained.
    */
   public boolean getOverrideDistributableManager()
   {
      return this.overrideDistributableManager;
   }

   /**
    * Sets whether the session <code>Manager</code> implementation for 
    * distributable webapps should be overridden with an instance of 
    * {@link #getManagerClass()}.
    * <p>
    * Setting this to <code>false</code> allows custom configuration of
    * a manager via a <code>context.xml</code>.  Default is <code>true</code>.
    * </p>
    * 
    * @param override <code>true</code> if the manager should be overridden,
    *         <code>false</code> if the existing manager should be retained.
    */
   public void setOverrideDistributableManager(boolean override)
   {
      this.overrideDistributableManager = override;
   }

   public ClassLoader getServiceClassLoader()
   {
      return serviceClassLoader;
   }

   public void setServiceClassLoader(ClassLoader serviceClassLoader)
   {
      this.serviceClassLoader = serviceClassLoader;
   }

   public String getDomain()
   {
      return this.catalinaDomain;
   }

   public Properties getAuthenticators()
   {
      return this.authenticators;
   }

   public void setAuthenticators(Properties prop)
   {
      this.authenticators = prop;
      log.debug("Passed set of authenticators=" + prop);
   }

   /**
    * The most important atteribute - defines the managed domain. A catalina instance (engine) corresponds to a JMX
    * domain, that's how we know where to deploy webapps.
    * 
    * @param catalinaDomain the domain portion of the JMX ObjectNames
    */
   public void setDomain(String catalinaDomain)
   {
      this.catalinaDomain = catalinaDomain;
   }

   public void setContextMBeanCode(String className)
   {
      this.contextClassName = className;
   }

   public String getContextMBeanCode()
   {
      return contextClassName;
   }

   public boolean getDeleteWorkDirOnContextDestroy()
   {
      return deleteWorkDirOnContextDestroy;
   }

   public void setDeleteWorkDirOnContextDestroy(boolean deleteFlag)
   {
      this.deleteWorkDirOnContextDestroy = deleteFlag;
   }

   public String getHttpHeaderForSSOAuth()
   {
      return httpHeaderForSSOAuth;
   }

   public void setHttpHeaderForSSOAuth(String httpHeader)
   {
      this.httpHeaderForSSOAuth = httpHeader;
   }

   public String getSessionCookieForSSOAuth()
   {
      return sessionCookieForSSOAuth;
   }

   public void setSessionCookieForSSOAuth(String sessionC)
   {
      this.sessionCookieForSSOAuth = sessionC;
   }

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public void setSessionIdAlphabet(String sessionIdAlphabet)
   {
      SessionIDGenerator.getInstance().setSessionIdAlphabet(sessionIdAlphabet);
   }

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public String getSessionIdAlphabet()
   {
      return SessionIDGenerator.getInstance().getSessionIdAlphabet();
   }

   @Deprecated
   public boolean getUseJBossWebLoader()
   {
      return useJBossWebLoader;
   }

   @Deprecated
   public void setUseJBossWebLoader(boolean flag)
   {
      log.warn("This flag is ignored, see JIRA issue JBAS-6914 on the new MC JBossCL usage.");
      this.useJBossWebLoader = flag;
   }

   public String getConfigFile()
   {
      return serverConfigFile;
   }

   public void setConfigFile(String configFile)
   {
      this.serverConfigFile = configFile;
   }

   @Override
   public String getSubjectAttributeName()
   {
      return this.subjectAttributeName;
   }

   @Override
   public void setSubjectAttributeName(String name)
   {
      this.subjectAttributeName = name;
   }

   public boolean isAllowSelfPrivilegedWebApps()
   {
      return allowSelfPrivilegedWebApps;
   }

   public void setAllowSelfPrivilegedWebApps(boolean allowSelfPrivilegedWebApps)
   {
      this.allowSelfPrivilegedWebApps = allowSelfPrivilegedWebApps;
   }

   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr)
   {
      this.secMgrService = mgr;
   }

   public void setPolicyRegistrationName(String policyRegistration)
   {
      this.policyRegistrationName = policyRegistration;
   }

   public void setSecurityManagementName(String securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   public void setSecurityContextClassName(String securityContextClassName)
   {
      this.securityContextClassName = securityContextClassName;
   }

   public String[] getFilteredPackages()
   {
      return filteredPackages;
   }

   public void setFilteredPackages(String[] pkgs)
   {
      this.filteredPackages = pkgs;
   }

   public String getDeploymentClass()
   {
      return deploymentClass;
   }

   public void setDeploymentClass(String deploymentClass)
   {
      this.deploymentClass = deploymentClass;
   }

   /**
    * Gets whether this object should start/stop the JBoss Web runtime during execution of its own start/stop lifecycle
    * callbacks.
    * 
    * @return <code>true</code> if a call to {@link #start()} should trigger a call to {@link #startWebServer()} and a
    *         call to {@link #stop()} should trigger a call to {@link #stopWebServer()}; <code>false</code> if the
    *         webserver runtime lifecycle will be separately managed. Default is <code>false</code>.
    */
   public boolean isRuntimeLifecycleCoupled()
   {
      return runtimeLifecycleCoupled;
   }

   /**
    * Sets whether this object should start/stop the JBoss Web runtime during execution of its own start/stop lifecycle
    * callbacks.
    * 
    * @param coupled <code>true</code> if a call to {@link #start()} should trigger a call to
    *            {@link #startWebServer()} and a call to {@link #stop()} should trigger a call to
    *            {@link #stopWebServer()}; <code>false</code> if the webserver runtime lifecycle will be separately
    *            managed. Default is <code>false</code>.
    */
   public void setRuntimeLifecycleCoupled(boolean coupled)
   {
      runtimeLifecycleCoupled = coupled;
   }

   /**
    * Start the deployer. This sets up the tomcat core.
    */
   @Override
   public void start() throws Exception
   {
      super.start();

      // Parse shared web.xml
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      URL webXml = this.getClass().getClassLoader().getResource("web.xml");
      if (webXml == null)
      {
         webXml = this.getClass().getClassLoader().getResource("conf/web.xml");
      }
      if (webXml == null)
         throw new IllegalStateException("Unable to find shared web.xml or conf/web.xml");

      SchemaBinding schema = JBossXBBuilder.build(Web25MetaData.class);
      Web25MetaData confWebMD = (Web25MetaData) unmarshaller.unmarshal(webXml.toString(), schema);
      sharedMetaData = new JBoss50WebMetaData();
      sharedMetaData.merge(null, confWebMD);
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
   }

   /**
    * Create a tomcat war deployment bean for the deployment unit/metaData.
    * 
    * @param unit - the current web app deployment unit
    * @param metaData - the parsed metdata for the web app deployment
    * @return TomcatDeployment instance
    */
   @Override
   public AbstractWarDeployment getDeployment(DeploymentUnit unit, JBossWebMetaData metaData) throws Exception
   {
      String className = (deploymentClass == null)
            ? "org.jboss.web.tomcat.service.deployers.TomcatDeployment"
            : deploymentClass;
      AbstractWarDeployment deployment = (AbstractWarDeployment) (getClass().getClassLoader().loadClass(className))
            .newInstance();

      DeployerConfig config = new DeployerConfig();
      config.setDefaultSecurityDomain(defaultSecurityDomain);
      config.setSubjectAttributeName(subjectAttributeName);
      config.setServiceClassLoader((getServiceClassLoader() == null)
            ? getClass().getClassLoader()
            : getServiceClassLoader());
      config.setManagerClass(managerClass);
      config.setOverrideDistributableManager(overrideDistributableManager);
      config.setJava2ClassLoadingCompliance(java2ClassLoadingCompliance);
      config.setUnpackWars(unpackWars);
      config.setLenientEjbLink(this.lenientEjbLink);
      config.setCatalinaDomain(catalinaDomain);
      config.setContextClassName(contextClassName);
      config.setServiceName(null);
      config.setSubjectAttributeName(subjectAttributeName);
      config.setUseJBossWebLoader(useJBossWebLoader);
      config.setAllowSelfPrivilegedWebApps(allowSelfPrivilegedWebApps);
      config.setSecurityManagerService(secMgrService);
      config.setFilteredPackages(filteredPackages);
      config.setSharedMetaData(sharedMetaData);
      config.setDeleteWorkDirs(deleteWorkDirOnContextDestroy);

      config.setSecurityContextClassName(securityContextClassName);
      deployment.setSecurityManagementName(securityManagement);
      deployment.setPolicyRegistrationName(policyRegistrationName);

      // Add a dependency on the webserver itself
      List<String> depends = metaData.getDepends();
      if (depends == null)
      {
         depends = new ArrayList<String>();
         metaData.setDepends(depends);
      }
      depends.add(TOMCAT_SERVICE_NAME.getCanonicalName());

      deployment.setServer(super.getServer());
      deployment.init(config);

      return deployment;
   }

   public void create() throws Exception
   {
      // MBeanServer server = MBeanServerLocator.locateJBoss();
      // if (server != null)
      // server.registerMBean(this, OBJECT_NAME);
   }

   public void destroy() throws Exception
   {
      // MBeanServer server = MBeanServerLocator.locateJBoss();
      // if (server != null)
      // server.unregisterMBean(OBJECT_NAME);
   }
}
