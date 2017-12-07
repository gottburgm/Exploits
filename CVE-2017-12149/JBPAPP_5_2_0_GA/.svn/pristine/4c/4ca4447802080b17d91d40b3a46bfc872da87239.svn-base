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
package org.jboss.mq.il.ha.examples;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * 
 * Helps with manual testing of the HA JMS
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 *
 */
public interface HAJMSClientMBean extends org.jboss.system.ServiceMBean
{
  public abstract String getConnectionException();
  public abstract String getLastMessage() throws JMSException;
  public abstract void publishMessageToTopic(String text) throws JMSException;
  public abstract void sendMessageToQueue(String text) throws JMSException;
  public abstract void connect() throws NamingException, JMSException;
  public abstract void disconnect() throws NamingException, JMSException;
}
