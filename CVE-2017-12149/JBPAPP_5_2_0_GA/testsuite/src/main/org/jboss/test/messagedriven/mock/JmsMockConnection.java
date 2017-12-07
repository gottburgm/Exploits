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
package org.jboss.test.messagedriven.mock;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * A JmsMockConnection.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public abstract class JmsMockConnection implements JmsMockObject, Connection, Destination
{

   public void close() throws JMSException
   {
      // TODO Auto-generated method stub
      
   }

   public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getClientID() throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ExceptionListener getExceptionListener() throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ConnectionMetaData getMetaData() throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setClientID(String clientID) throws JMSException
   {
      // TODO Auto-generated method stub
      
   }

   public void setExceptionListener(ExceptionListener listener) throws JMSException
   {
      // TODO Auto-generated method stub
      
   }

   public void start() throws JMSException
   {
      // TODO Auto-generated method stub
      
   }

   public void stop() throws JMSException
   {
      // TODO Auto-generated method stub
      
   }
   
   
   
}
