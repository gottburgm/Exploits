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
package org.jboss.jms.jndi;

import java.util.Properties;

import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public interface JMSProviderLoaderMBean extends ServiceMBean
{
   /**
    * Set the provider name
    * 
    * @param name the provider name
    */
   void setProviderName(String name);

   /**
    * Get the provider name
    * 
    * @return the provider name
    */
   String getProviderName();

   /**
    * Set the provider adapter class
    * 
    * @param clazz the class name
    */
   void setProviderAdapterClass(String clazz);


   /**
    * Get the provider adapter class
    * 
    * @return the class name
    */
   String getProviderAdapterClass();

   /**
    * Set the provider properties
    * 
    * @param properties the properties
    */
   void setProperties(Properties properties);

   /**
    * Get the provider properties
    * 
    * @return the properties
    */
   Properties getProperties();

   /**
    * Set where the provider adapter is bound into jndi
    * 
    * @param name the provider adapter jndi name
    */
   void setAdapterJNDIName(String name);

   /**
    * Get where the provider adapter is bound into jndi
    * 
    * @return the jndi binding
    */
   String getAdapterJNDIName();

   /**
    * Set the jndi name of the unified connection factory
    * 
    * @param newFactoryRef the jndi name
    */
   void setFactoryRef(String newFactoryRef);

   /**
    * Set the jndi name of the queue connection factory
    * 
    * @param newQueueFactoryRef the jndi name
    */
   void setQueueFactoryRef(String newQueueFactoryRef);

   /**
    * Set the jndi name of the topic connection factory
    * 
    * @param newTopicFactoryRef the jndi name
    */
   void setTopicFactoryRef(String newTopicFactoryRef);

   /**
    * Get the jndi name of the unified connection factory
    * 
    * @return the jndi name
    */
   String getFactoryRef();

   /**
    * Get the jndi name of the queue connection factory
    * 
    * @return the jndi name
    */
   String getQueueFactoryRef();

   /**
    * Get the jndi name of the topic connection factory
    * 
    * @return the jndi name
    */
   String getTopicFactoryRef();
}
