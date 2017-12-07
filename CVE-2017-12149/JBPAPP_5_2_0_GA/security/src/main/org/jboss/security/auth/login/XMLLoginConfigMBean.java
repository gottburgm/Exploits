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

import java.io.IOException;
import java.net.URL;

import javax.management.ObjectName;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.system.ServiceMBean;

/** The managment bean interface for the XML based JAAS login configuration
 object.

@author  Scott.Stark@jboss.org
@version $Revision: 85945 $
 */
public interface XMLLoginConfigMBean extends ServiceMBean
{
   /** Default ObjectName
    */
   public static final ObjectName OBJECT_NAME = 
      ObjectNameFactory.create("jboss.security:service=XMLLoginConfig");
   
   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public URL getConfigURL();
   /** Set the URL of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public void setConfigURL(URL configURL);

   /** Set the resource name of the XML login configuration file that should
    be loaded by this mbean on startup.
    */
   public void setConfigResource(String resourceName) throws IOException;

   /** Get whether the login config xml document is validated againsts its DTD
    */
   public boolean getValidateDTD();
   /** Set whether the login config xml document is validated againsts its DTD
    */
   public void setValidateDTD(boolean flag);
   
   /**
    * Get the Application Policy given the domain name
    */
   public ApplicationPolicy getApplicationPolicy(String domainName);

   /** Get the XML based configuration given the Configuration it should
    delegate to when an application cannot be found.
    */
   public Configuration getConfiguration(Configuration prevConfig);
   
   /**
    * Add an Application Policy given a domain name to the configuration
    * @param appName
    * @param aPolicy
    */
   public void addApplicationPolicy(String appName, ApplicationPolicy aPolicy);

   /** Add an application login configuration. Any existing configuration for
    the given appName will be replaced.
    @deprecated
    */
   public void addAppConfig(String appName, AppConfigurationEntry[] entries);
   /** Remove an application login configuration.
    */
   public void removeAppConfig(String appName);

   /** Load the login configuration information from the given config URL.
    * @param configURL A URL to an XML or Sun login config file.
    * @return An array of the application config names loaded
    * @throws Exception on failure to load the configuration
    */ 
   public String[] loadConfig(URL configURL) throws Exception;
   /** Remove the given login configurations. This invokes removeAppConfig
    * for each element of appNames.
    * 
    * @param appNames the names of the login configurations to remove. 
    */ 
   public void removeConfigs(String[] appNames);

   /** Display the login configuration for the given application.
    */
   public String displayAppConfig(String appName);
}

