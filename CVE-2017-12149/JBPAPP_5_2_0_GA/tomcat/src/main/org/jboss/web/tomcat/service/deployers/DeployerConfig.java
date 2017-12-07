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

import javax.management.ObjectName;

import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;

/**
 * The tomcat war deployer configuration passed in from the web container.
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 91181 $
 */
public class DeployerConfig
{
   /**
    * The tomcat sar class loader
    */
   private ClassLoader serviceClassLoader;

   /**
    * The domain used for the tomcat mbeans
    */
   private String catalinaDomain = "Catalina";

   /**
    * The fully qualified name of the class that will be used for session management if <tt>distributable</tt> is set
    * to true.
    */
   private String managerClass = "org.jboss.web.tomcat.service.session.JBossManager";

   /**
    * The web context class to create
    */
   private String contextClassName;

   /**
    * The parent class loader first model flag
    */
   private boolean java2ClassLoadingCompliance = false;

   /**
    * A flag indicating if war archives should be unpacked
    */
   private boolean unpackWars = true;

   /**
    * If true, ejb-links that don't resolve don't cause an error (fallback to jndi-name)
    */
   private boolean lenientEjbLink = false;

   /**
    * The tomcat service JMX object name
    */
   private ObjectName serviceName;

   /**
    * The catalina debug level
    */
   private int debugLevel;

   /**
    * A flag indicating if the JBoss UCL should be used
    */
   private boolean useJBossWebLoader = true;

   /**
    * A flag indicating if the working dir for a war deployment should be delete when the war is undeployed.
    */
   private boolean deleteWorkDirs = true;

   /**
    * Get the request attribute name under which the JAAS Subject is store
    */
   private String subjectAttributeName = null;

   /**
    * The default security-domain name to use
    */
   private String defaultSecurityDomain;

   /** Package names that should be ignored for class loading */
   private String[] filteredPackages;

   /**
    * Shared WebMetaData.
    */
   private JBossWebMetaData sharedMetaData = null;

   /**
    * Flag indicating whether web-app specific context xmls may set the privileged flag.
    */
   private boolean allowSelfPrivilegedWebApps = false;

   /** The service used to flush authentication cache on session invalidation. */
   private JaasSecurityManagerServiceMBean secMgrService;

   /** FQN of the SecurityContext Class */
   private String securityContextClassName;
   
   private boolean overrideDistributableManager = true;

   public ClassLoader getServiceClassLoader()
   {
      return serviceClassLoader;
   }

   public void setServiceClassLoader(ClassLoader serviceClassLoader)
   {
      this.serviceClassLoader = serviceClassLoader;
   }

   public String getManagerClass()
   {
      return managerClass;
   }

   public void setManagerClass(String managerClass)
   {
      this.managerClass = managerClass;
   }

   public String getCatalinaDomain()
   {
      return catalinaDomain;
   }

   public void setCatalinaDomain(String catalinaDomain)
   {
      this.catalinaDomain = catalinaDomain;
   }

   public String getContextClassName()
   {
      return contextClassName;
   }

   public void setContextClassName(String contextClassName)
   {
      this.contextClassName = contextClassName;
   }

   public boolean isJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }

   public void setJava2ClassLoadingCompliance(boolean java2ClassLoadingCompliance)
   {
      this.java2ClassLoadingCompliance = java2ClassLoadingCompliance;
   }

   public boolean isUnpackWars()
   {
      return unpackWars;
   }

   public void setUnpackWars(boolean unpackWars)
   {
      this.unpackWars = unpackWars;
   }

   public boolean isLenientEjbLink()
   {
      return lenientEjbLink;
   }

   public void setLenientEjbLink(boolean lenientEjbLink)
   {
      this.lenientEjbLink = lenientEjbLink;
   }

   public ObjectName getServiceName()
   {
      return serviceName;
   }

   public void setServiceName(ObjectName serviceName)
   {
      this.serviceName = serviceName;
   }

   public int getDebugLevel()
   {
      return debugLevel;
   }

   public void setDebugLevel(int debugLevel)
   {
      this.debugLevel = debugLevel;
   }

   public boolean isUseJBossWebLoader()
   {
      return useJBossWebLoader;
   }

   public void setUseJBossWebLoader(boolean useJBossWebLoader)
   {
      this.useJBossWebLoader = useJBossWebLoader;
   }

   public boolean isDeleteWorkDirs()
   {
      return deleteWorkDirs;
   }

   public void setDeleteWorkDirs(boolean deleteWorkDirs)
   {
      this.deleteWorkDirs = deleteWorkDirs;
   }

   public String getSubjectAttributeName()
   {
      return subjectAttributeName;
   }

   public void setSubjectAttributeName(String subjectAttributeName)
   {
      this.subjectAttributeName = subjectAttributeName;
   }

   /**
    * Get the default security domain implementation to use if a war does not declare a security-domain.
    * 
    * @return jndi name of the security domain binding to use.
    * @jmx:managed-attribute
    */
   public String getDefaultSecurityDomain()
   {
      return defaultSecurityDomain;
   }

   /**
    * Set the default security domain implementation to use if a war does not declare a security-domain.
    * 
    * @param defaultSecurityDomain - jndi name of the security domain binding to use.
    * @jmx:managed-attribute
    */
   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

   public boolean isAllowSelfPrivilegedWebApps()
   {
      return allowSelfPrivilegedWebApps;
   }

   public void setAllowSelfPrivilegedWebApps(boolean allowSelfPrivilegedWebApps)
   {
      this.allowSelfPrivilegedWebApps = allowSelfPrivilegedWebApps;
   }

   public JaasSecurityManagerServiceMBean getSecurityManagerService()
   {
      return secMgrService;
   }

   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr)
   {
      this.secMgrService = mgr;
   }

   public String getSecurityContextClassName()
   {
      return securityContextClassName;
   }

   public void setSecurityContextClassName(String securityContextClassName)
   {
      this.securityContextClassName = securityContextClassName;
   }

   public String[] getFilteredPackages()
   {
      return filteredPackages;
   }

   public void setFilteredPackages(String[] filteredPackages)
   {
      this.filteredPackages = filteredPackages;
   }

   public JBossWebMetaData getSharedMetaData()
   {
      return sharedMetaData;
   }

   public void setSharedMetaData(JBossWebMetaData sharedMetaData)
   {
      this.sharedMetaData = sharedMetaData;
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
      return overrideDistributableManager;
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
   
   
}
