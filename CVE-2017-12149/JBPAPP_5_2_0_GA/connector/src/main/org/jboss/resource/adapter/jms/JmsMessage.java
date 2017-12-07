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

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsMessage implements Message
{
   /** The message */
   Message message;
   
   /** The session */
   JmsSession session;

   /**
    * Create a new wrapper
    * 
    * @param message the message
    * @param session the session
    */
   public JmsMessage(Message message, JmsSession session)
   {
      this.message = message;
      this.session = session;
   }

   public void acknowledge() throws JMSException
   {
      session.getSession(); // Check for closed
      message.acknowledge();
   }
   
   public void clearBody() throws JMSException
   {
      message.clearBody();
   }
   
   public void clearProperties() throws JMSException
   {
      message.clearProperties();
   }
   
   public boolean getBooleanProperty(String name) throws JMSException
   {
      return message.getBooleanProperty(name);
   }
   
   public byte getByteProperty(String name) throws JMSException
   {
      return message.getByteProperty(name);
   }

   public double getDoubleProperty(String name) throws JMSException
   {
      return message.getDoubleProperty(name);
   }
   
   public float getFloatProperty(String name) throws JMSException
   {
      return message.getFloatProperty(name);
   }
   
   public int getIntProperty(String name) throws JMSException
   {
      return message.getIntProperty(name);
   }
   
   public String getJMSCorrelationID() throws JMSException
   {
      return message.getJMSCorrelationID();
   }
   
   public byte[] getJMSCorrelationIDAsBytes() throws JMSException
   {
      return message.getJMSCorrelationIDAsBytes();
   }
   
   public int getJMSDeliveryMode() throws JMSException
   {
      return message.getJMSDeliveryMode();
   }
   
   public Destination getJMSDestination() throws JMSException
   {
      return message.getJMSDestination();
   }
   
   public long getJMSExpiration() throws JMSException
   {
      return message.getJMSExpiration();
   }
   
   public String getJMSMessageID() throws JMSException
   {
      return message.getJMSMessageID();
   }
   
   public int getJMSPriority() throws JMSException
   {
      return message.getJMSPriority();
   }
   
   public boolean getJMSRedelivered() throws JMSException
   {
      return message.getJMSRedelivered();
   }
   
   public Destination getJMSReplyTo() throws JMSException
   {
      return message.getJMSReplyTo();
   }
   
   public long getJMSTimestamp() throws JMSException
   {
      return message.getJMSTimestamp();
   }
   
   public String getJMSType() throws JMSException
   {
      return message.getJMSType();
   }
   
   public long getLongProperty(String name) throws JMSException
   {
      return message.getLongProperty(name);
   }
   
   public Object getObjectProperty(String name) throws JMSException
   {
      return message.getObjectProperty(name);
   }
   
   public Enumeration getPropertyNames() throws JMSException
   {
      return message.getPropertyNames();
   }
   
   public short getShortProperty(String name) throws JMSException
   {
      return message.getShortProperty(name);
   }
   
   public String getStringProperty(String name) throws JMSException
   {
      return message.getStringProperty(name);
   }
   
   public boolean propertyExists(String name) throws JMSException
   {
      return message.propertyExists(name);
   }
   
   public void setBooleanProperty(String name, boolean value) throws JMSException
   {
      message.setBooleanProperty(name, value);
   }
   
   public void setByteProperty(String name, byte value) throws JMSException
   {
      message.setByteProperty(name, value);
   }
   
   public void setDoubleProperty(String name, double value) throws JMSException
   {
      message.setDoubleProperty(name, value);
   }
   
   public void setFloatProperty(String name, float value) throws JMSException
   {
      message.setFloatProperty(name, value);
   }
   
   public void setIntProperty(String name, int value) throws JMSException
   {
      message.setIntProperty(name, value);
   }
   
   public void setJMSCorrelationID(String correlationID) throws JMSException
   {
      message.setJMSCorrelationID(correlationID);
   }

   public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException
   {
      message.setJMSCorrelationIDAsBytes(correlationID);
   }

   public void setJMSDeliveryMode(int deliveryMode) throws JMSException
   {
      message.setJMSDeliveryMode(deliveryMode);
   }

   public void setJMSDestination(Destination destination) throws JMSException
   {
      message.setJMSDestination(destination);
   }
   
   public void setJMSExpiration(long expiration) throws JMSException
   {
      message.setJMSExpiration(expiration);
   }
   
   public void setJMSMessageID(String id) throws JMSException
   {
      message.setJMSMessageID(id);
   }
   
   public void setJMSPriority(int priority) throws JMSException
   {
      message.setJMSPriority(priority);
   }
   
   public void setJMSRedelivered(boolean redelivered) throws JMSException
   {
      message.setJMSRedelivered(redelivered);
   }

   public void setJMSReplyTo(Destination replyTo) throws JMSException
   {
      message.setJMSReplyTo(replyTo);
   }

   public void setJMSTimestamp(long timestamp) throws JMSException
   {
      message.setJMSTimestamp(timestamp);
   }
   
   public void setJMSType(String type) throws JMSException
   {
      message.setJMSType(type);
   }
   
   public void setLongProperty(String name, long value) throws JMSException
   {
      message.setLongProperty(name, value);
   }
   
   public void setObjectProperty(String name, Object value) throws JMSException
   {
      message.setObjectProperty(name, value);
   }
   
   public void setShortProperty(String name, short value) throws JMSException
   {
      message.setShortProperty(name, value);
   }
   
   public void setStringProperty(String name, String value) throws JMSException
   {
      message.setStringProperty(name, value);
   }
   
   public int hashCode()
   {
      return message.hashCode();
   }
   
   public boolean equals(Object object)
   {
      if (object != null && object instanceof JmsMessage)
         return message.equals(((JmsMessage) object).message);
      else
         return message.equals(object);
   }
   
   public String toString()
   {
      return message.toString();
   }
}
