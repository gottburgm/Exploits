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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TopicConnection;

/**
 * A marker interface to join topics and queues into one factory.
 *
 * @author  <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <pre>$Revision: 71554 $</pre>
 */
public interface JmsSessionFactory
   extends Connection, TopicConnection, QueueConnection
{
   /** Error message for strict behaviour */
   String ISE = "This method is not applicable inside the application server. See the J2EE spec, e.g. J2EE1.4 Section 6.6";

   /**
    * Add a temporary queue
    * 
    * @param temp the temporary queue
    */
   void addTemporaryQueue(TemporaryQueue temp);

   /**
    * Add a temporary topic
    * 
    * @param temp the temporary topic
    */
   void addTemporaryTopic(TemporaryTopic temp);
   
   /** 
    * Notification that a session is closed
    * 
    * @throws JMSException for any error 
    */
   void closeSession(JmsSession session) throws JMSException;
}
