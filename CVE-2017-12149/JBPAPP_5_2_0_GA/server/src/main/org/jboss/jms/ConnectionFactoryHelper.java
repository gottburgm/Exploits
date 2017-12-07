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
package org.jboss.jms;

import javax.jms.JMSException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnectionFactory;

import org.jboss.logging.Logger;

/**
 * A helper for creating connections from jms connection factories.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public class ConnectionFactoryHelper
{
   /** Class logger. */
   private static Logger log = Logger.getLogger(ConnectionFactoryHelper.class);

   /**
    * Create a connection from the given factory.  An XA connection will
    * be created if possible.
    *
    * @param factory     An object that implements ConnectionFactory,
    *                    XAQConnectionFactory
    * @param username    The username to use or null for no user.
    * @param password    The password for the given username or null if no
    *                    username was specified.
    * @return            A queue connection.
    *                    
    * @throws JMSException                Failed to create connection.
    * @throws IllegalArgumentException    Factory is null or invalid.
    */
   public static Connection createConnection(final Object factory, final String username, final String password)
         throws JMSException
   {
      if (factory == null)
         throw new IllegalArgumentException("factory is null");

      log.debug("using connection factory: " + factory);
      log.debug("using username/password: " + String.valueOf(username) + "/-- not shown --");

      Connection connection;

      if (factory instanceof XAConnectionFactory)
      {
         XAConnectionFactory qFactory = (XAConnectionFactory) factory;
         if (username != null)
            connection = qFactory.createXAConnection(username, password);
         else
            connection = qFactory.createXAConnection();

         log.debug("created XAConnection: " + connection);
      }
      else if (factory instanceof ConnectionFactory)
      {
         ConnectionFactory qFactory = (ConnectionFactory) factory;
         if (username != null)
            connection = qFactory.createConnection(username, password);
         else
            connection = qFactory.createConnection();

         log.debug("created Connection: " + connection);
      }
      else
      {
         throw new IllegalArgumentException("factory is invalid");
      }

      return connection;
   }

   /**
    * Create a connection from the given factory.  An XA connection will
    * be created if possible.
    *
    * @param factory     An object that implements QueueConnectionFactory,
    *                    XAQueueConnectionFactory
    * @return            A queue connection.
    *
    * @throws JMSException                Failed to create connection.
    * @throws IllegalArgumentException    Factory is null or invalid.
    */
   public static Connection createConnection(final Object factory) throws JMSException
   {
      return createConnection(factory, null, null);
   }

   /**
    * Create a queue connection from the given factory.  An XA connection will
    * be created if possible.
    *
    * @param factory     An object that implements QueueConnectionFactory,
    *                    XAQueueConnectionFactory
    * @param username    The username to use or null for no user.
    * @param password    The password for the given username or null if no
    *                    username was specified.
    * @return            A queue connection.
    *                    
    * @throws JMSException                Failed to create connection.
    * @throws IllegalArgumentException    Factory is null or invalid.
    */
   public static QueueConnection createQueueConnection(final Object factory, final String username,
         final String password) throws JMSException
   {
      if (factory == null)
         throw new IllegalArgumentException("factory is null");

      log.debug("using connection factory: " + factory);
      log.debug("using username/password: " + String.valueOf(username) + "/-- not shown --");

      QueueConnection connection;

      if (factory instanceof XAQueueConnectionFactory)
      {
         XAQueueConnectionFactory qFactory = (XAQueueConnectionFactory) factory;
         if (username != null)
            connection = qFactory.createXAQueueConnection(username, password);
         else
            connection = qFactory.createXAQueueConnection();

         log.debug("created XAQueueConnection: " + connection);
      }
      else if (factory instanceof QueueConnectionFactory)
      {
         QueueConnectionFactory qFactory = (QueueConnectionFactory) factory;
         if (username != null)
            connection = qFactory.createQueueConnection(username, password);
         else
            connection = qFactory.createQueueConnection();

         log.debug("created QueueConnection: " + connection);
      }
      else
         throw new IllegalArgumentException("factory is invalid");

      return connection;
   }

   /**
    * Create a queue connection from the given factory.  An XA connection will
    * be created if possible.
    *
    * @param factory     An object that implements QueueConnectionFactory,
    *                    XAQueueConnectionFactory
    * @return            A queue connection.
    *
    * @throws JMSException                Failed to create connection.
    * @throws IllegalArgumentException    Factory is null or invalid.
    */
   public static QueueConnection createQueueConnection(final Object factory) throws JMSException
   {
      return createQueueConnection(factory, null, null);
   }

   /**
    * Create a topic connection from the given factory.  An XA connection will
    * be created if possible.
    *
    * @param factory     An object that implements TopicConnectionFactory,
    *                    XATopicConnectionFactory
    * @param username    The username to use or null for no user.
    * @param password    The password for the given username or null if no
    *                    username was specified.
    * @return            A topic connection.
    *                    
    * @throws JMSException                Failed to create connection.
    * @throws IllegalArgumentException    Factory is null or invalid.
    */
   public static TopicConnection createTopicConnection(final Object factory, final String username,
         final String password) throws JMSException
   {
      if (factory == null)
         throw new IllegalArgumentException("factory is null");

      log.debug("using connection factory: " + factory);
      log.debug("using username/password: " + String.valueOf(username) + "/-- not shown --");

      TopicConnection connection;

      if (factory instanceof XATopicConnectionFactory)
      {
         XATopicConnectionFactory tFactory = (XATopicConnectionFactory) factory;
         if (username != null)
            connection = tFactory.createXATopicConnection(username, password);
         else
            connection = tFactory.createXATopicConnection();

         log.debug("created XATopicConnection: " + connection);
      }
      else if (factory instanceof TopicConnectionFactory)
      {
         TopicConnectionFactory tFactory = (TopicConnectionFactory) factory;
         if (username != null)
            connection = tFactory.createTopicConnection(username, password);
         else
            connection = tFactory.createTopicConnection();

         log.debug("created TopicConnection: " + connection);
      }
      else
         throw new IllegalArgumentException("factory is invalid");

      return connection;
   }

   /**
    * Create a topic connection from the given factory.  An XA connection will
    * be created if possible.
    *
    * @param factory     An object that implements TopicConnectionFactory,
    *                    XATopicConnectionFactory
    * @return            A topic connection.
    *                    
    * @throws JMSException                Failed to create connection.
    * @throws IllegalArgumentException    Factory is null or invalid.
    */
   public static TopicConnection createTopicConnection(final Object factory) throws JMSException
   {
      return createTopicConnection(factory, null, null);
   }
}
