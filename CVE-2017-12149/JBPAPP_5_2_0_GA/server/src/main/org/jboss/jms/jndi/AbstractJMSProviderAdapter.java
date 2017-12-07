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

import java.io.Serializable;
import java.util.Properties;

/**
 * An abstract implementaion of {@link JMSProviderAdapter}.  Sub-classes must
 * provide connection names via instance initialzation and provide an 
 * implementaion of {@link #getInitialContext}.
 *
 * 6/22/01 - hchirino - The queue/topic jndi references are now configed via JMX
 *
 * @version <pre>$Revision: 81030 $</pre>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public abstract class AbstractJMSProviderAdapter implements JMSProviderAdapter, Serializable
{
   private static final long serialVersionUID = 3573606612665654983L;

   /** The name of the provider. */
   protected String name;

   /** The properties. */
   protected Properties properties;

   /** The factory name to use. */
   protected String factoryRef;

   /** The queue factory name to use. */
   protected String queueFactoryRef;

   /** The topic factory name to use. */
   protected String topicFactoryRef;

   public void setName(final String name)
   {
      this.name = name;
   }

   public final String getName()
   {
      return name;
   }

   public void setProperties(final Properties properties)
   {
      this.properties = properties;
   }

   public final Properties getProperties()
   {
      return properties;
   }

   public String getFactoryRef()
   {
      if (factoryRef == null)
         throw new IllegalStateException("Combined ConnectionFactory 'FactoryRef' not configured.");
      return factoryRef;
   }

   public String getQueueFactoryRef()
   {
      if (queueFactoryRef == null)
         throw new IllegalStateException("Queue ConnectionFactory 'QueueFactoryRef' not configured.");
      return queueFactoryRef;
   }

   public String getTopicFactoryRef()
   {
      if (topicFactoryRef == null)
         throw new IllegalStateException("Topic ConnectionFactory 'TopicFactoryRef' not configured.");
      return topicFactoryRef;
   }

   public void setFactoryRef(String newFactoryRef)
   {
      factoryRef = newFactoryRef;
   }

   public void setQueueFactoryRef(String newQueueFactoryRef)
   {
      queueFactoryRef = newQueueFactoryRef;
   }

   public void setTopicFactoryRef(String newTopicFactoryRef)
   {
      topicFactoryRef = newTopicFactoryRef;
   }
}