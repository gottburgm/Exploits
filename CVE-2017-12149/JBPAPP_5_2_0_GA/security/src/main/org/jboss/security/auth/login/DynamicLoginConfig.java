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
package org.jboss.security.auth.login;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.PolicyConfig;
import org.jboss.system.ServiceMBeanSupport;

/** A security config mbean that loads an xml login configuration using the
 XMLLoginConfig.loadConfig(URL config) operation on start, and unloads
 the contained login module configurations on stop.

 <server>
   <mbean code="org.jboss.security.auth.login.DynamicLoginConfig"
      name="...">
      <attribute name="AuthConfig">login-config.xml</attribute>
      <!-- The service which supports dynamic processing of login-config.xml
         configurations.
      -->
      <depends optional-attribute-name="LoginConfigService">
         jboss.security:service=XMLLoginConfig
      </depends>
      <!-- Optionally specify the security mgr service to use when
         this service is stopped to flush the auth caches of the domains
         registered by this service.
      -->
      <depends optional-attribute-name="SecurityManagerService">
         jboss.security:service=JaasSecurityManager
      </depends>
   </mbean>
 </server>
 
 @see org.jboss.security.auth.login.XMLLoginConfig

 @author Scott.Stark@jboss.org
 @author Anil.Saldhana@jboss.org
 @version $Revision: 85945 $
 */
public class DynamicLoginConfig extends ServiceMBeanSupport
   implements DynamicLoginConfigMBean
{
   /** The JAAS login config file resource to load */
   private String authConf = "login-config.xml";
   /** The name of the XMLLoginConfig to use to load the login configs */
   private ObjectName loginConfigService;
   /** The name of the SecurityMgrService to use for cache flushes */
   private ObjectName securityMgrService;
   /** The names of the login module configs loaded during start */
   private String[] configNames;
   private PolicyConfig config;

   public DynamicLoginConfig()
   {
   }

   public String getName()
   {
      return "Dynamic JAAS Login Config";
   }

   public PolicyConfig getPolicyConfig()
   {
      return config;
   }
   public void setPolicyConfig(PolicyConfig config)
   {
      this.config = config;
   }

   public ObjectName getLoginConfigService()
   {
      return loginConfigService;
   }
   /** Get the XMLLoginConfig service to use for loading. This service must
    * support a String[] loadConfig(URL) operation to load the configurations. 
    * 
    * @param serviceName - the XMLLoginConfig service name.
    */ 
   public void setLoginConfigService(ObjectName serviceName)
   {
      this.loginConfigService = serviceName;
   }

   public ObjectName getSecurityManagerService()
   {
      return securityMgrService;
   }
   /** Set the SecurityManagerService used to flush the registered security
    * domains. This service must support an flushAuthenticationCache(String)
    * operation to flush the case for the argument security domain. Setting
    * this triggers the flush of the authentication caches when the service
    * is stopped.
    * @param serviceName - the SecurityManagerService service name.
    */
   public void setSecurityManagerService(ObjectName serviceName)
   {
      this.securityMgrService = serviceName;
   }

   /** Get the resource path to the JAAS login configuration file to use.
    */
   public String getAuthConfig()
   {
      return authConf;
   }

   /** Set the resource path to the JAAS login configuration file to use.
    The default is "login-config.xml".
    */
   public void setAuthConfig(String authConf)
   {
      this.authConf = authConf;
   }

   /** Go through the registered login config names and flush the auth
    * caches if there is a registered SecurityManagerService.
    * 
    * @throws Exception
    */ 
   public void flushAuthenticationCaches() throws Exception
   {
      if( this.securityMgrService != null && server.isRegistered(securityMgrService))
      {
         int count = configNames == null ? 0 : configNames.length;
         String[] sig = {String.class.getName()};
         for(int n = 0; n < count; n ++)
         {
            Object[] args = {configNames[n]};
            server.invoke(securityMgrService, "flushAuthenticationCache", args, sig);         
            log.debug("Flushed domain: "+configNames[n]);
         }
      }
   }

   /** Start the service. This entails loading the AuthConf file contents
    * using the LoginConfigService.
    */
   protected void startService() throws Exception
   {
      if( config != null )
      {
         log.debug("Using embedded config");
         Set names = config.getConfigNames();
         Iterator iter = names.iterator();
         MBeanServer server = super.getServer();
         while( iter.hasNext() )
         {
            String name = (String) iter.next();
            ApplicationPolicy aPolicy = config.get(name);
            if(aPolicy == null)
               throw new IllegalStateException("Application Policy is null for "+name);
            
            AuthenticationInfo info = (AuthenticationInfo)aPolicy.getAuthenticationInfo();
            if(info == null)
              throw new IllegalStateException("Authentication Info is null for " + name);
            AppConfigurationEntry[] entry = info.getAppConfigurationEntry();
            // addAppConfig(String, AppConfigurationEntry[]);
            //Object[] args = {name, entry};
            //String[] sig = {String.class.getName(), entry.getClass().getName()};
            Object[] args = {name, aPolicy};
            String[] sig = {String.class.getName(), aPolicy.getClass().getName()};
            //server.invoke(loginConfigService, "addAppConfig", args, sig);
            server.invoke(loginConfigService, "addApplicationPolicy", args, sig);
         }
         configNames = new String[names.size()];
         names.toArray(configNames);
      }
      else
      {
         //JBAS-3422: Ensure that the AuthConf is neither null nor default login-config.xml 
         if( authConf== null || authConf.length() == 0)
            throw new IllegalStateException("AuthConf is null. Please " +
                  "configure an appropriate config resource");
          
         // Look for the authConf as resource
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         URL loginConfig = loader.getResource(authConf);
         if(loginConfig == null)
         {
           try
           {
             //JBAS-3210: Allow an absolute url
             loginConfig = new URL(authConf);
           }
           catch(Exception e)
           {
             loginConfig = null;
           }
         }
         if( loginConfig != null )
         {
            validateAuthConfigURL(loginConfig);
            log.debug("Using JAAS AuthConfig: "+loginConfig.toExternalForm());
            MBeanServer server = super.getServer();
            Object[] args = {loginConfig};
            String[] sig = {URL.class.getName()};
            configNames = (String[]) server.invoke(loginConfigService,
               "loadConfig", args, sig);
            int count = configNames == null ? 0 : configNames.length;
            for(int n = 0; n < count; n ++)
            {
               log.debug("Loaded config: "+configNames[n]);
            }
         }
         else
         {
            throw new DeploymentException("Failed to find authConf as resource: "+authConf); 
         }
      }
   }

   /** Start the service. This entails unloading the AuthConf file contents
    * using the LoginConfigService.
    */
   protected void stopService() throws Exception
   {
      MBeanServer server = super.getServer();
      flushAuthenticationCaches();
      if( configNames != null && configNames.length > 0 )
      {
         Object[] args = {configNames};
         String[] sig = {configNames.getClass().getName()};
         server.invoke(loginConfigService, "removeConfigs", args, sig);
      }
   }
   
   /**
    * Ensure that the AuthConfig resource is not defaulting to
    * the default login-config in the conf directory
    * @param url
    * @throws Exception
    */
   private void validateAuthConfigURL(URL url) throws Exception
   {
      String msg = "AuthConfig is defaulting to conf/login-config.xml. " +
            "Please check your archive.";
      XMLLoginConfigMBean xmlConfig = null;
      try
      {
         xmlConfig = (XMLLoginConfigMBean) MBeanProxy.get(XMLLoginConfigMBean.class, 
               XMLLoginConfigMBean.OBJECT_NAME, server);
         if(xmlConfig.getConfigURL().sameFile(url))
            throw new IllegalStateException(msg);
      }
      finally
      {
         //Clear the proxy
         xmlConfig = null;
      } 
   }
}
