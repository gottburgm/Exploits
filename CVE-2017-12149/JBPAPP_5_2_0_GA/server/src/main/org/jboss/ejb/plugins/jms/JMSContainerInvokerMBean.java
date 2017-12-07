/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb.plugins.jms;

import org.jboss.ejb.plugins.inflow.JBossMessageEndpointFactoryMBean;
import org.jboss.metadata.MessageDrivenMetaData;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 66023 $</tt>
 */
public interface JMSContainerInvokerMBean extends JBossMessageEndpointFactoryMBean
{
   /**
    * Get the minimum pool size
    * 
    * @return the minimum pool size
    */
   int getMinPoolSize();

   /**
    * Set the minimum pool size
    * 
    * @param minPoolSize the size
    */
   void setMinPoolSize(int minPoolSize);

   /**
    * Get the maximum pool size
    * 
    * @return the maximum pool size
    */
   int getMaxPoolSize();

   /**
    * Set the maximum pool size
    * 
    * @param maxPoolSize the size
    */
   void setMaxPoolSize(int maxPoolSize);

   /**
    * Get the keep alive millis
    * 
    * @return the milliseconds
    */
   long getKeepAliveMillis();

   /**
    * Set the keep alive millis
    * 
    * @param keepAlive the milliseconds
    */
   void setKeepAliveMillis(long keepAlive);

   /**
    * Get the maximum number of messages
    * 
    * @return the number of messages
    */
   int getMaxMessages();

   /**
    * Set the maximum number of messages
    * 
    * @param maxMessages the number of messages
    */
   void setMaxMessages(int maxMessages);

   /**
    * Get the message driven metadata
    * 
    * @return the metadata
    */
   MessageDrivenMetaData getMetaData();

   /**
    * Get whether delivery is active
    * 
    * @return true when active
    */
   boolean getDeliveryActive();

   /**
    * Get whether JBossMQ destinations should be constructed
    * when the destination is not in JNDI
    * 
    * @return true to create
    */
   boolean getCreateJBossMQDestination();
}
