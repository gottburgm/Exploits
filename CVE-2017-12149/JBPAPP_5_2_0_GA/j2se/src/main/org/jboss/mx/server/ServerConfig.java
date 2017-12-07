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
package org.jboss.mx.server;

import javax.management.MBeanServerBuilder;
import javax.management.ObjectName;

import org.jboss.mx.standardmbean.StandardMBeanDelegateFactory;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.util.Classes;

/**
 * Server configuration.
 * 
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public abstract class ServerConfig
{
   // Constants ---------------------------------------------------
   
   /** The instance */
   private static ServerConfig instance;

   /**
    * The name of the protected implementation domain
    * Pass this object to the registry in the values map as the key and value
    * to register in this domain
    */
   private final static String JMI_DOMAIN = "JMImplementation";

   /** The default domain */
   private final static String DEFAULT_DOMAIN = "DefaultDomain";

   /** The MBeanServerDelegate ObjectName */
   private static final ObjectName mbeanServerDelegateName = ObjectNameFactory.create(JMI_DOMAIN + ":type=MBeanServerDelegate");
   
   /** The server config property */
   public final static String SERVER_CONFIG_CLASS_PROPERTY = "jbossmx.server.config.class";
  
   /** The default server config class */
   public final static String DEFAULT_SERVER_CONFIG_CLASS  = "org.jboss.mx.server.JBossMXServerConfig";
   
   /** The mbean server builder property */
   public final static String MBEAN_SERVER_BUILDER_CLASS_PROPERTY = "javax.management.builder.initial";

   // Attributes --------------------------------------------------

   // Static ------------------------------------------------------

   public static synchronized ServerConfig getInstance()
   {
      if (instance != null)
         return instance;
      instance = (ServerConfig) Classes.instantiate(ServerConfig.class, SERVER_CONFIG_CLASS_PROPERTY, DEFAULT_SERVER_CONFIG_CLASS);
      return instance;
   }
   
   // Constructors ------------------------------------------------

   /**
    * No external construction
    */
   protected ServerConfig()
   {
   }

   // Public ------------------------------------------------------

   /**
    * Get the default domain
    * 
    * @return the default domain
    */
   public String getDefaultDomain()
   {
      return DEFAULT_DOMAIN;
   }

   /**
    * Get the implementation domain
    * 
    * @return the implementation domain
    */
   public String getJMIDomain()
   {
      return JMI_DOMAIN;
   }
   
   /**
    * Get the MBeanServer delegate name
    * 
    * @return the ObjectName of the MBeanServerDelegate
    */
   public ObjectName getMBeanServerDelegateName()
   {
      return mbeanServerDelegateName;
   }

   /**
    * Get the MBeanServer builder
    * 
    * @return the mbeanserver builder
    */
   public MBeanServerBuilder getMBeanServerBuilder()
   {
      String defaultMBeanServerBuilder = getDefaultMBeanServerBuilderClassName();
      return (MBeanServerBuilder) Classes.instantiate(MBeanServerBuilder.class, MBEAN_SERVER_BUILDER_CLASS_PROPERTY, defaultMBeanServerBuilder);
      
   }

   /**
    * Get the default loader repository name
    * 
    * @return the default loader repository name
    */
   public abstract ObjectName getLoaderRepositoryName();

   /**
    * Get the standardmbean delegate factory
    * 
    * @return the factory
    */
   public abstract StandardMBeanDelegateFactory getStandardMBeanDelegateFactory();
   
   // Protected ---------------------------------------------------

   protected abstract String getDefaultMBeanServerBuilderClassName();
   
   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
