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
package org.jboss.resource.adapter.jms;

import java.sql.SQLException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.TopicConnection;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.JTATransactionChecker;
import org.jboss.util.NestedSQLException;

/**
 * The the connection factory implementation for the JMS RA.
 *
 * <p>
 * This object will be the QueueConnectionFactory or TopicConnectionFactory
 * which clients will use to create connections.
 * 
 * @author  <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 71790 $</tt>
 */
public class JmsConnectionFactoryImpl 
   implements JmsConnectionFactory, Referenceable
{
   private static final long serialVersionUID = -5135366013101194277L;

   private static final Logger log = Logger.getLogger(JmsConnectionFactoryImpl.class);

   private ManagedConnectionFactory mcf;

   private ConnectionManager cm;

   private Reference reference;

   public JmsConnectionFactoryImpl(final ManagedConnectionFactory mcf,
                                   final ConnectionManager cm) 
   {
      this.mcf = mcf;

      boolean trace = log.isTraceEnabled();
      if (cm == null)
      {
         // This is standalone usage, no appserver
         this.cm = new JmsConnectionManager();
         if (trace)
            log.trace("Created new connection manager");
      }
      else
         this.cm = cm;

      if (trace)
         log.trace("Using ManagedConnectionFactory=" + mcf + ", ConnectionManager=" + cm);
   }

   public void setReference(final Reference reference) 
   {
      this.reference = reference;

      if (log.isTraceEnabled())
         log.trace("Using Reference=" + reference);
   }
    
   public Reference getReference() 
   {
      return reference;
   }
   
   // --- QueueConnectionFactory
   
   public QueueConnection createQueueConnection() throws JMSException 
   {
      QueueConnection qc = new JmsSessionFactoryImpl(mcf, cm, QUEUE);

      if (log.isTraceEnabled())
         log.trace("Created queue connection: " + qc);
      
      return qc;
   }
   
   public QueueConnection createQueueConnection(String userName, String password) 
      throws JMSException 
   {
      JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, QUEUE);
      s.setUserName(userName);
      s.setPassword(password);

      if (log.isTraceEnabled())
         log.trace("Created queue connection: " + s);
      
      return s;
   } 

   // --- TopicConnectionFactory
   
   public TopicConnection createTopicConnection() throws JMSException 
   {
      TopicConnection tc = new JmsSessionFactoryImpl(mcf, cm, TOPIC);

      if (log.isTraceEnabled())
         log.trace("Created topic connection: " + tc);

      return tc;
   }
   
   public TopicConnection createTopicConnection(String userName, String password)
      throws JMSException 
   {
      JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, TOPIC);
      s.setUserName(userName);
      s.setPassword(password);
      
      if (log.isTraceEnabled())
         log.trace("Created topic connection: " + s);

      return s;
   }

   // --- JMS 1.1
   
   public Connection createConnection()
      throws JMSException 
   {
      Connection c = new JmsSessionFactoryImpl(mcf, cm, BOTH);

      if (log.isTraceEnabled())
         log.trace("Created connection: " + c);

      return c;
   }

   public Connection createConnection(String userName, String password)
      throws JMSException
   {
      JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, BOTH);
      s.setUserName(userName);
      s.setPassword(password);
      
      if (log.isTraceEnabled())
         log.trace("Created connection: " + s);

      return s;
   }  
}
