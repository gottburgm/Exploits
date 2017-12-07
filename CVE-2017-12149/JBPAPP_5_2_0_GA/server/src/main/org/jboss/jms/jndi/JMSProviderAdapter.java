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

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Properties;

/**
 * JMS Provider Adapter
 *
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public interface JMSProviderAdapter extends Serializable
{
   /**
    * This must return a context which can be closed.
    * 
    * @return the context
    */
   Context getInitialContext() throws NamingException;

   /**
    * Set the provider name
    * 
    * @param name the name
    */
   void setName(String name);

   /**
    * Get the provider name
    * 
    * @return the name
    */
   String getName();
   
   /**
    * Set the properties
    * 
    * @param properties the properties
    */
   void setProperties(Properties properties);
   
   /**
    * Get the properties
    * 
    * @return the properties
    */
   Properties getProperties();
   
   /**
    * Get the jndi binding of the combined connection factory
    * 
    * @return the jndi binding
    */
   String getFactoryRef();

   /**
    * Get the jndi binding of the queue connection factory
    * 
    * @return the jndi binding
    */
   String getQueueFactoryRef();

   /**
    * Get the jndi binding of the topic connection factory
    * 
    * @return the jndi binding
    */
   String getTopicFactoryRef();

   /**
    * Set the jndi binding of the combined connection factory
    * 
    * @param newFactoryRef the jndi binding
    */
   void setFactoryRef(String newFactoryRef);

   /**
    * Set the jndi binding of the queue connection factory
    * 
    * @param newQueueFactoryRef the jndi binding
    */
   void setQueueFactoryRef(String newQueueFactoryRef);

   /**
    * Set the jndi binding of the topic connection factory
    * 
    * @param newTopicFactoryRef the jndi binding
    */
   void setTopicFactoryRef(String newTopicFactoryRef);
}
