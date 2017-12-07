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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.io.IOException;
import java.net.URL;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.system.ServiceMBeanSupport;

/**
 * An MBean for managing a XMLLoginConfigImpl instance.
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 85945 $
 */
@JMX(name = "jboss.security:service=XMLLoginConfig", exposedInterface = XMLLoginConfigMBean.class)
@ManagementObject(name = "XMLLoginConfig", componentType = @ManagementComponent(type = "MCBean", subtype = "Security"),
      properties = ManagementProperties.EXPLICIT)
public class XMLLoginConfig extends ServiceMBeanSupport implements XMLLoginConfigMBean
{
   XMLLoginConfigImpl config;

   public XMLLoginConfig()
   {
      this.config = XMLLoginConfigImpl.getInstance();
   }

   // --- Begin XMLLoginConfigMBean interface methods

   /**
    * Set the URL of the XML login configuration file that should be loaded by this mbean on startup.
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The URL of the login configuration file")
   public URL getConfigURL()
   {
      return this.config.getConfigURL();
   }

   /**
    * Set the URL of the XML login configuration file that should be loaded by this mbean on startup.
    */
   public void setConfigURL(URL configURL)
   {
      this.config.setConfigURL(configURL);
   }

   /**
    * Get whether the login config xml document is validated againsts its DTD
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "Validate or not the login configuration file")
   public boolean getValidateDTD()
   {
      return this.config.getValidateDTD();
   }

   /**
    * Set whether the login config xml document is validated againsts its DTD
    */
   public void setValidateDTD(boolean flag)
   {
      this.config.setValidateDTD(flag);
   }

   /**
    * Set the resource name of the XML login configuration file that should be loaded by this mbean on startup.
    */
   @ManagementOperation(description = "Set the resource name of the login config file", impact = Impact.WriteOnly)
   public void setConfigResource(String resourceName) throws IOException
   {
      this.config.setConfigResource(resourceName);
   }

   /**
    * Add an application policy given a security domain name
    */
   @ManagementOperation(description = "Add an application policy for the specified domain", impact = Impact.WriteOnly)
   public void addApplicationPolicy(String appName, ApplicationPolicy aPolicy)
   {
      this.config.addApplicationPolicy(appName, aPolicy);
   }

   /**
    * Add an application login configuration. Any existing configuration for the given appName will be replaced.
    * 
    * @deprecated
    */
   public void addAppConfig(String appName, AppConfigurationEntry[] entries)
   {
      this.config.addAppConfig(appName, entries);
   }

   /**
    * Remove an application login configuration.
    */
   public void removeAppConfig(String appName)
   {
      this.config.removeAppConfig(appName);
   }

   /**
    * @see XMLLoginConfigMBean#getApplicationPolicy(String)
    */
   @ManagementOperation(description = "Get the application policy for the specified domain", impact = Impact.ReadOnly)
   public ApplicationPolicy getApplicationPolicy(String domainName)
   {
      return (ApplicationPolicy) this.config.getApplicationPolicy(domainName);
   }

   /**
    * Get the XML based configuration given the Configuration it should delegate to when an application cannot be found.
    */
   @ManagementOperation(description = "Get the javax.security.auth.login.Configuration instance", impact = Impact.ReadOnly)
   public Configuration getConfiguration(Configuration prevConfig)
   {
      this.config.setParentConfig(prevConfig);
      return this.config;
   }

   /**
    * Load the login configuration information from the given config URL.
    * 
    * @param configURL A URL to an XML or Sun login config file.
    * @throws Exception on failure to load the configuration
    */
   @ManagementOperation(description = "Load (parse) the login config file", impact = Impact.ReadWrite)
   public String[] loadConfig(URL configURL) throws Exception
   {
      return this.config.loadConfig(configURL);
   }

   @ManagementOperation(description = "Remove the configuration of the specified domains", impact = Impact.WriteOnly)
   public void removeConfigs(String[] appNames)
   {
      int count = appNames == null ? 0 : appNames.length;
      for (int a = 0; a < count; a++)
         removeAppConfig(appNames[a]);
   }

   /**
    * Display the login configuration for the given application.
    */
   @ManagementOperation(description = "Display the configuration for the specified domain", impact = Impact.ReadOnly)
   public String displayAppConfig(String appName)
   {
      StringBuffer buffer = new StringBuffer("<h2>" + appName + " LoginConfiguration</h2>\n");
      AppConfigurationEntry[] appEntry = this.config.getAppConfigurationEntry(appName);
      if (appEntry == null)
         buffer.append("No Entry\n");
      else
      {
         for (int c = 0; c < appEntry.length; c++)
         {
            AppConfigurationEntry entry = appEntry[c];
            buffer.append("LoginModule Class: " + entry.getLoginModuleName());
            buffer.append("\n<br>ControlFlag: " + entry.getControlFlag());
            buffer.append("\n<br>Options:<ul>");
            Map options = entry.getOptions();
            Iterator iter = options.entrySet().iterator();
            while (iter.hasNext())
            {
               Entry e = (Entry) iter.next();
               buffer.append("<li>");
               buffer.append("name=" + e.getKey());
               buffer.append(", value=" + e.getValue());
               buffer.append("</li>\n");
            }
            buffer.append("</ul>\n");
         }
      }
      return buffer.toString();
   }

   // --- End XMLLoginConfigMBean interface methods

   // --- Begin ServiceMBeanSupport overriden methods

   /**
    * Load the configuration
    */
   @Override
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   protected void startService() throws Exception
   {
      this.config.loadConfig();
   }

   /**
    * Clear all configuration entries
    */
   @Override
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   protected void destroyService()
   {
      this.config.clear();
   }

   // --- End ServiceMBeanSupport overriden methods

}
