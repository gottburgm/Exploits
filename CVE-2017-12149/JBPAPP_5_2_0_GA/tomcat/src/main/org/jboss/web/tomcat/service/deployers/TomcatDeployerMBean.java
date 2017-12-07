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

import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.web.AbstractWebContainerMBean;
import org.jboss.web.deployers.AbstractWarDeployerMBean;

/**
 * An implementation of the AbstractWebContainer for the Jakarta Tomcat5 servlet container. It has no code dependency on
 * tomcat - only the new JMX model is used. <p/> Tomcat5 is organized as a set of mbeans - just like jboss.
 * 
 * @author Scott.Stark@jboss.org
 * @author Costin Manolache
 * @version $Revision: 109248 $
 * @see AbstractWebContainerMBean
 */
public interface TomcatDeployerMBean extends AbstractWarDeployerMBean
{
   /** JMX notification type to signal after-start connector event */
   public final String TOMCAT_CONNECTORS_STARTED = "jboss.tomcat.connectors.started";

   /** JMX notification type to signal after-start connector event */
   public final String TOMCAT_CONNECTORS_STOPPED = "jboss.tomcat.connectors.stopped";

   /**
    * @return the jmx domain for the tomcat management mbeans
    */
   public String getDomain();

   /**
    * The most important attribute - defines the managed domain. A catalina instance (engine) corresponds to a JMX
    * domain, that's how we know where to deploy webapps.
    * 
    * @param domainName the jmx domain under which tc registers
    */
   public void setDomain(String domainName);

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public void setSessionIdAlphabet(String sessionIdAlphabet);

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public String getSessionIdAlphabet();

   /**
    * Get the JBoss UCL use flag
    *
    * @deprecated not used any more, see JBAS-6914
    */
   @Deprecated
   public boolean getUseJBossWebLoader();

   /**
    * Set the JBoss UCL use flag
    *
    * @deprecated not used any more, see JBAS-6914
    */
   @Deprecated
   public void setUseJBossWebLoader(boolean flag);

   public String getManagerClass();

   public void setManagerClass(String managerClass);

   /** */
   public String getContextMBeanCode();

   /** */
   public void setContextMBeanCode(String className);

   /**
    * Get the name of the external tomcat server configuration file.
    * 
    * @return the config file name, server.xml for example
    */
   public String getConfigFile();

   /**
    * Set the name of the external tomcat server configuration file.
    * 
    * @param configFile - the config file name, server.xml for example
    */
   public void setConfigFile(String configFile);

   /**
    * Get the request attribute name under which the JAAS Subject is store
    */
   public String getSubjectAttributeName();

   /**
    * Set the request attribute name under which the JAAS Subject will be stored when running with a security mgr that
    * supports JAAS. If this is empty then the Subject will not be store in the request.
    * 
    * @param name the HttpServletRequest attribute name to store the Subject
    */
   public void setSubjectAttributeName(String name);

   /**
    * Get whether web-apps are able to control the privileged flag
    */
   public boolean isAllowSelfPrivilegedWebApps();

   /**
    * Set whether web-apps are able to control the privileged flag
    */
   public void setAllowSelfPrivilegedWebApps(boolean flag);

   /**
    * Set the SecurityManagerService binding. This is used to flush any associated authentication cache on session
    * invalidation.
    * 
    * @param mgr the JaasSecurityManagerServiceMBean
    */
   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr);

   /**
    * 
    * @return
    */
   public String[] getFilteredPackages();

   /**
    * 
    * @param pkgs
    */
   public void setFilteredPackages(String[] pkgs);

   /**
    * <p>
    * Obtain the value of the {@code httpHeaderForSSOAuth} property. This property is used to indicate what request
    * header ids will be used to retrieve the SSO identity set by a third party security system.
    * </p>
    * 
    * @return the value of the {@code httpHeaderForSSOAuth} property.
    */
   public String getHttpHeaderForSSOAuth();

   /**
    * <p>
    * Set the value of the {@code httpHeaderForSSOAuth} property. This property is used to indicate what request header
    * ids will be used to retrieve the SSO identity set by a third party security system.
    * </p>
    * 
    * @param httpHeaderForSSOAuth a {@code String} containing the request header ids separated by comma.
    */
   public void setHttpHeaderForSSOAuth(String httpHeaderForSSOAuth);

   /**
    * <p>
    * Obtain the value of the {@code sessionCookieForSSOAuth} property. This property is used to indicate the names of
    * the SSO cookies that may be present in the request object.
    * </p>
    * 
    * @return the value of the {@code sessionCookieForSSOAuth} property.
    */
   public String getSessionCookieForSSOAuth();

   /**
    * <p>
    * Set the value of the {@code sessionCookieForSSOAuth} property. This property is used to indicate the names of the
    * SSO cookies that may be present in the request object.
    * </p>
    * 
    * @param sessionCookieForSSOAuth a {@code String} containing the names (separated by comma) of the SSO cookies.
    */
   public void setSessionCookieForSSOAuth(String sessionCookieForSSOAuth);

   /**
    * <p>
    * Obtain the value of the {@code deleteWorkDirOnContextDestroy} property. This property is used to indicate 
    * if the workdirs will be deleted when undeploying contexts.
    * </p>
    * 
    * @return the value of the {@code deleteWorkDirOnContextDestroy} property.
    */
   public boolean getDeleteWorkDirOnContextDestroy();

   /**
    * <p>
    * Set the value of the {@code deleteWorkDirOnContextDestroy} property. This property is used to indicate 
    * if the workdirs will be deleted when undeploying contexts.
    * </p>
    * 
    * @param deleteFlag a {@code boolean} containing the value of the {@code deleteWorkDirOnContextDestroy} property.
    */
   public void setDeleteWorkDirOnContextDestroy(boolean deleteFlag);
   
}
