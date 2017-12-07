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
package org.jboss.test.jca.jms;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionFactory;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.Name;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.NonSerializableFactory;

/**
 * WrapperJMSConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class WrapperJMSConnectionFactory extends ServiceMBeanSupport
   implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory, WrapperJMSConnectionFactoryMBean
{
   private String jndiName;
   private String reference;
   private ConnectionFactory delegate; 
   
   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public String getReference()
   {
      return reference;
   }

   public void setReference(String reference)
   {
      this.reference = reference;
   }
   
   @Override
   protected void startService() throws Exception
   {
      InitialContext context = new InitialContext();
      delegate = (ConnectionFactory) context.lookup(reference);
      Name name = context.getNameParser("").parse(jndiName);
      NonSerializableFactory.rebind(name, this, true);
   }
   
   @Override
   protected void stopService() throws Exception
   {
      InitialContext context = new InitialContext();
      context.unbind(jndiName);
      NonSerializableFactory.unbind(jndiName);
   }

   public Connection createConnection() throws JMSException
   {
      return new WrapperConnection(delegate.createConnection());
   }

   public Connection createConnection(String arg0, String arg1) throws JMSException
   {
      return new WrapperConnection(delegate.createConnection(arg0, arg1));
   }

   public QueueConnection createQueueConnection() throws JMSException
   {
      return new WrapperQueueConnection(((QueueConnectionFactory) delegate).createQueueConnection());
   }

   public QueueConnection createQueueConnection(String arg0, String arg1) throws JMSException
   {
      return new WrapperQueueConnection(((QueueConnectionFactory) delegate).createQueueConnection(arg0, arg1));
   }

   public TopicConnection createTopicConnection() throws JMSException
   {
      return new WrapperTopicConnection(((TopicConnectionFactory) delegate).createTopicConnection());
   }

   public TopicConnection createTopicConnection(String arg0, String arg1) throws JMSException
   {
      return new WrapperTopicConnection(((TopicConnectionFactory) delegate).createTopicConnection(arg0, arg1));
   }
   
   public class WrapperConnection implements Connection
   {
      Connection connection;
      
      WrapperConnection(Connection connection)
      {
         this.connection = connection;
      }

      public void close() throws JMSException
      {
         connection.close();
      }

      public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3)
            throws JMSException
      {
         return connection.createConnectionConsumer(arg0, arg1, arg2, arg3);
      }

      public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2,
            ServerSessionPool arg3, int arg4) throws JMSException
      {
         return connection.createDurableConnectionConsumer(arg0, arg1, arg2, arg3, arg4);
      }

      public Session createSession(boolean arg0, int arg1) throws JMSException
      {
         return connection.createSession(arg0, arg1);
      }

      public String getClientID() throws JMSException
      {
         return connection.getClientID();
      }

      public ExceptionListener getExceptionListener() throws JMSException
      {
         return connection.getExceptionListener();
      }

      public ConnectionMetaData getMetaData() throws JMSException
      {
         return connection.getMetaData();
      }

      public void setClientID(String arg0) throws JMSException
      {
         connection.setClientID(arg0);
      }

      public void setExceptionListener(ExceptionListener arg0) throws JMSException
      {
         connection.setExceptionListener(arg0);
      }

      public void start() throws JMSException
      {
         connection.start();
      }

      public void stop() throws JMSException
      {
         connection.stop();
      }
   }
   
   public class WrapperQueueConnection implements QueueConnection
   {
      QueueConnection connection;
      
      WrapperQueueConnection(QueueConnection connection)
      {
         this.connection = connection;
      }

      public void close() throws JMSException
      {
         connection.close();
      }

      public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3)
            throws JMSException
      {
         return connection.createConnectionConsumer(arg0, arg1, arg2, arg3);
      }

      public ConnectionConsumer createConnectionConsumer(Queue arg0, String arg1, ServerSessionPool arg2, int arg3)
            throws JMSException
      {
         return connection.createConnectionConsumer(arg0, arg1, arg2, arg3);
      }

      public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2,
            ServerSessionPool arg3, int arg4) throws JMSException
      {
         return connection.createDurableConnectionConsumer(arg0, arg1, arg2, arg3, arg4);
      }

      public QueueSession createQueueSession(boolean arg0, int arg1) throws JMSException
      {
         return connection.createQueueSession(arg0, arg1);
      }

      public Session createSession(boolean arg0, int arg1) throws JMSException
      {
         return connection.createSession(arg0, arg1);
      }

      public String getClientID() throws JMSException
      {
         return connection.getClientID();
      }

      public ExceptionListener getExceptionListener() throws JMSException
      {
         return connection.getExceptionListener();
      }

      public ConnectionMetaData getMetaData() throws JMSException
      {
         return connection.getMetaData();
      }

      public void setClientID(String arg0) throws JMSException
      {
         connection.setClientID(arg0);
      }

      public void setExceptionListener(ExceptionListener arg0) throws JMSException
      {
         connection.setExceptionListener(arg0);
      }

      public void start() throws JMSException
      {
         connection.start();
      }

      public void stop() throws JMSException
      {
         connection.stop();
      }
   }
   
   public class WrapperTopicConnection implements TopicConnection
   {
      TopicConnection connection;
      
      WrapperTopicConnection(TopicConnection connection)
      {
         this.connection = connection;
      }

      public void close() throws JMSException
      {
         connection.close();
      }

      public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3)
            throws JMSException
      {
         return connection.createConnectionConsumer(arg0, arg1, arg2, arg3);
      }

      public ConnectionConsumer createConnectionConsumer(Topic arg0, String arg1, ServerSessionPool arg2, int arg3)
            throws JMSException
      {
         return connection.createConnectionConsumer(arg0, arg1, arg2, arg3);
      }

      public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2,
            ServerSessionPool arg3, int arg4) throws JMSException
      {
         return connection.createDurableConnectionConsumer(arg0, arg1, arg2, arg3, arg4);
      }

      public Session createSession(boolean arg0, int arg1) throws JMSException
      {
         return connection.createSession(arg0, arg1);
      }

      public TopicSession createTopicSession(boolean arg0, int arg1) throws JMSException
      {
         return connection.createTopicSession(arg0, arg1);
      }

      public String getClientID() throws JMSException
      {
         return connection.getClientID();
      }

      public ExceptionListener getExceptionListener() throws JMSException
      {
         return connection.getExceptionListener();
      }

      public ConnectionMetaData getMetaData() throws JMSException
      {
         return connection.getMetaData();
      }

      public void setClientID(String arg0) throws JMSException
      {
         connection.setClientID(arg0);
      }

      public void setExceptionListener(ExceptionListener arg0) throws JMSException
      {
         connection.setExceptionListener(arg0);
      }

      public void start() throws JMSException
      {
         connection.start();
      }

      public void stop() throws JMSException
      {
         connection.stop();
      }
   }
}
