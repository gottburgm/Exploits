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
package org.jboss.test.security.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.login.Configuration;

import org.jboss.security.auth.login.XMLLoginConfig;
import org.jboss.system.ServiceMBeanSupport;

/** A security config mbean that loads an xml login configuration and
 pushes a XMLLoginConfig instance onto the the config stack managed by
 the SecurityConfigName mbean(default=jboss.security:name=SecurityConfig).
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class SecurityConfig extends ServiceMBeanSupport
   implements SecurityConfigMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private String authConf = "login-config.xml";
   private XMLLoginConfig config = null;
   private ObjectName mainSecurityConfig;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public SecurityConfig()
   {
      setSecurityConfigName("jboss.security:name=SecurityConfig");
   }

   public String getName()
   {
      return "JAAS Login Config";
   }
   public String getSecurityConfigName()
   {
      return mainSecurityConfig.toString();
   }
   public void setSecurityConfigName(String objectName)
   {
      try
      {
         mainSecurityConfig = new ObjectName(objectName);
      }
      catch(Exception e)
      {
         log.error("Failed to create ObjectName", e);
      }
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

   // Public --------------------------------------------------------
   /** Start the service. This entails 
    */
   protected void startService() throws Exception
   {
      // Look for the authConf as resource
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL loginConfig = loader.getResource(authConf);
      if( loginConfig != null )
      {
         log.info("Using JAAS AuthConfig: "+loginConfig.toExternalForm());
         config = new XMLLoginConfig();
         config.setConfigURL(loginConfig);
         config.start();
         MBeanServer server = super.getServer();
         ObjectName name = super.getServiceName();
         Hashtable props = name.getKeyPropertyList();
         props.put("testConfig", "XMLLoginConfig");
         name = new ObjectName(name.getDomain(), props);
         server.registerMBean(config, name);
         Object[] args = {name.toString()};
         String[] sig = {String.class.getName()};
         server.invoke(mainSecurityConfig, "pushLoginConfig", args, sig);
      }
      else
      {
         log.warn("No AuthConfig resource found");
      }
   }

   protected void stopService() throws Exception
   {
      MBeanServer server = super.getServer();
      ObjectName name = super.getServiceName();
      Hashtable props = name.getKeyPropertyList();
      props.put("testConfig", "XMLLoginConfig");
      name = new ObjectName(name.getDomain(), props);
      Object[] args = {};
      String[] sig = {};
      server.invoke(mainSecurityConfig, "popLoginConfig", args, sig);
      server.unregisterMBean(name);
   }
}
