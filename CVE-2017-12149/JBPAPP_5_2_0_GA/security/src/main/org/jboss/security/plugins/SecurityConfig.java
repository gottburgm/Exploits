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
package org.jboss.security.plugins;

import java.util.Stack;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.login.Configuration;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.security.auth.login.XMLLoginConfig;
import org.jboss.system.ServiceMBeanSupport;

/**
 * The SecurityConfigMBean implementation. This class needs the
 * javax.security.auth.AuthPermission("setLoginConfiguration") to install the javax.security.auth.login.Configuration
 * when running with a security manager.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@JMX(name = "jboss.security:service=SecurityConfig", exposedInterface = SecurityConfigMBean.class)
@ManagementObject(name = "SecurityConfig", componentType = @ManagementComponent(type = "MCBean", subtype = "Security"), 
                  properties = ManagementProperties.EXPLICIT)
public class SecurityConfig extends ServiceMBeanSupport implements SecurityConfigMBean
{
   /** The default Configuration mbean name */
   private String loginConfigName;

   /** The default configuration bean */
   private XMLLoginConfig defaultLoginConfig;

   /** The stack of Configuration mbeans that are active */
   private final Stack<Configuration> loginConfigStack = new Stack<Configuration>();

   /** The MBeanServer */
   private MBeanServer mbeanServer;

   /**
    * Get the name of the mbean that provides the default JAAS login configuration
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The default Configuration bean name")
   public String getLoginConfig()
   {
      return loginConfigName;
   }

   /**
    * Set the name of the mbean that provides the default JAAS login configuration
    */
   public void setLoginConfig(String name) throws MalformedObjectNameException
   {
      this.loginConfigName = name;
   }

   /**
    * <p>
    * Obtains a reference to the {@code MBeanServer} instance.
    * </p>
    * 
    * @return the {@code MBeanServer} that has been injected into this class.
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The MBean server")
   public MBeanServer getMbeanServer()
   {
      if (this.mbeanServer == null)
         return super.getServer();
      return this.mbeanServer;
   }

   /**
    * <p>
    * Injects the {@code MBeanServer} instance that must be used by this class.
    * </p>
    * 
    * @param server a reference to the {@code MBeanServer} to be used.
    */
   public void setMbeanServer(MBeanServer server)
   {
      this.mbeanServer = server;
   }

   /**
    * <p>
    * Obtains a reference to the default login configuration bean.
    * </p>
    * 
    * @return a reference to the default login configuration bean.
    */
   @ManagementProperty(use = {ViewUse.CONFIGURATION}, description = "The default login config bean")
   public XMLLoginConfig getDefaultLoginConfig()
   {
      return defaultLoginConfig;
   }

   /**
    * <p>
    * Injects the default login configuration bean.
    * </p>
    * 
    * @param defaulLoginConfig a reference to the login configuration bean to be used.
    */
   public void setDefaultLoginConfig(XMLLoginConfig defaultLoginConfig)
   {
      this.defaultLoginConfig = defaultLoginConfig;
   }

   /**
    * Start the configuration service by pushing the mbean given by the LoginConfig onto the configuration stack.
    */
   @Override
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   public void startService() throws Exception
   {
      if (this.defaultLoginConfig != null)
         pushLoginConfig(this.defaultLoginConfig);
      else
         pushLoginConfig(this.loginConfigName);
   }

   /**
    * Stop the configuration service by poping the top of the configuration stack.
    */
   @Override
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   public void stopService() throws Exception
   {
      if (this.loginConfigStack.empty() == false)
         popLoginConfig();
   }

   /**
    * <p>
    * Push the configuration obtained from the specified {@code XMLLoginConfig} onto the stack.
    * </p>
    * 
    * @param loginConfig a reference to the {@code XMLLoginConfig} instance.
    */
   public synchronized void pushLoginConfig(XMLLoginConfig loginConfig)
   {
      Configuration prevConfig = null;
      if (!this.loginConfigStack.empty())
         prevConfig = this.loginConfigStack.peek();
      Configuration configuration = loginConfig.getConfiguration(prevConfig);
      Configuration.setConfiguration(configuration);
      this.loginConfigStack.push(configuration);
      log.debug("Installed JAAS configuration: " + configuration);
   }

   /**
    * Push an mbean onto the login configuration stack and install its Configuration as the current instance.
    * 
    * @see javax.security.auth.login.Configuration
    */
   @ManagementOperation(description = "Install the Configuration from the MBean identified by objectName",
         params={@ManagementParameter(name="objectName", description="The identifier of the MBean that contains the Configuration")},
         impact = Impact.WriteOnly)
   public synchronized void pushLoginConfig(String objectName) throws JMException, MalformedObjectNameException
   {
      ObjectName name = new ObjectName(objectName);
      Configuration prevConfig = null;
      if (!this.loginConfigStack.empty())
         prevConfig = this.loginConfigStack.peek();

      this.loginConfigStack.push(installConfig(name, prevConfig));
   }

   /**
    * Pop the current mbean from the login configuration stack and install the previous Configuration as the current
    * instance.
    * 
    * @see javax.security.auth.login.Configuration
    */
   @ManagementOperation(description = "Uninstall the current Configuration", impact = Impact.WriteOnly)
   public synchronized void popLoginConfig() throws JMException
   {
      // remove the current configuration from the stack.
      this.loginConfigStack.pop();
      // if there is a previous configuration, install it as the current instance.
      if (!loginConfigStack.empty())
         Configuration.setConfiguration(this.loginConfigStack.peek());
   }

   /**
    * Obtain the Configuration from the named mbean using its getConfiguration operation and install it as the current
    * Configuration.
    * 
    * @see Configuration.setConfiguration(javax.security.auth.login.Configuration)
    */
   private Configuration installConfig(ObjectName name, Configuration prevConfig) throws JMException
   {
      Object[] args = {prevConfig};
      String[] signature = {"javax.security.auth.login.Configuration"};
      Configuration config = (Configuration) this.getMbeanServer().invoke(name, "getConfiguration", args, signature);
      Configuration.setConfiguration(config);
      log.debug("Installed JAAS Configuration service=" + name + ", config=" + config);
      return config;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.system.ServiceMBeanSupport#getName()
    */
   @Override
   public String getName()
   {
      return "SecurityIntialization";
   }
}
