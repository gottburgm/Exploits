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

import javax.jms.BytesMessage;
import javax.jms.JMSException;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsBytesMessage extends JmsMessage implements BytesMessage
{
   /**
    * Create a new wrapper
    * 
    * @param message the message
    * @param session the session
    */
   public JmsBytesMessage(BytesMessage message, JmsSession session)
   {
      super(message, session);
   }
   
   public long getBodyLength() throws JMSException
   {
      return ((BytesMessage) message).getBodyLength();
   }
   
   public boolean readBoolean() throws JMSException
   {
      return ((BytesMessage) message).readBoolean();
   }
   
   public byte readByte() throws JMSException
   {
      return ((BytesMessage) message).readByte();
   }
   
   public int readBytes(byte[] value, int length) throws JMSException
   {
      return ((BytesMessage) message).readBytes(value, length);
   }
   
   public int readBytes(byte[] value) throws JMSException
   {
      return ((BytesMessage) message).readBytes(value);
   }
   
   public char readChar() throws JMSException
   {
      return ((BytesMessage) message).readChar();
   }
   
   public double readDouble() throws JMSException
   {
      return ((BytesMessage) message).readDouble();
   }
   
   public float readFloat() throws JMSException
   {
      return ((BytesMessage) message).readFloat();
   }
   
   public int readInt() throws JMSException
   {
      return ((BytesMessage) message).readInt();
   }
   
   public long readLong() throws JMSException
   {
      return ((BytesMessage) message).readLong();
   }
   
   public short readShort() throws JMSException
   {
      return ((BytesMessage) message).readShort();
   }
   
   public int readUnsignedByte() throws JMSException
   {
      return ((BytesMessage) message).readUnsignedByte();
   }
   
   public int readUnsignedShort() throws JMSException
   {
      return ((BytesMessage) message).readUnsignedShort();
   }
   
   public String readUTF() throws JMSException
   {
      return ((BytesMessage) message).readUTF();
   }
   
   public void reset() throws JMSException
   {
      ((BytesMessage) message).reset();
   }
   
   public void writeBoolean(boolean value) throws JMSException
   {
      ((BytesMessage) message).writeBoolean(value);
   }
   
   public void writeByte(byte value) throws JMSException
   {
      ((BytesMessage) message).writeByte(value);
   }
   
   public void writeBytes(byte[] value, int offset, int length) throws JMSException
   {
      ((BytesMessage) message).writeBytes(value, offset, length);
   }
   
   public void writeBytes(byte[] value) throws JMSException
   {
      ((BytesMessage) message).writeBytes(value);
   }
   
   public void writeChar(char value) throws JMSException
   {
      ((BytesMessage) message).writeChar(value);
   }
   
   public void writeDouble(double value) throws JMSException
   {
      ((BytesMessage) message).writeDouble(value);
   }
   
   public void writeFloat(float value) throws JMSException
   {
      ((BytesMessage) message).writeFloat(value);
   }
   
   public void writeInt(int value) throws JMSException
   {
      ((BytesMessage) message).writeInt(value);
   }
   
   public void writeLong(long value) throws JMSException
   {
      ((BytesMessage) message).writeLong(value);
   }

   public void writeObject(Object value) throws JMSException
   {
      ((BytesMessage) message).writeObject(value);
   }

   public void writeShort(short value) throws JMSException
   {
      ((BytesMessage) message).writeShort(value);
   }

   public void writeUTF(String value) throws JMSException
   {
      ((BytesMessage) message).writeUTF(value);
   }
}
