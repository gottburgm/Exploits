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
package org.jboss.test.jbossmessaging.test;

import java.util.Arrays;
import java.math.BigInteger;
import javax.jms.DeliveryMode;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Tests of sending/receiving all jms message types to/from a queue
 * 
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
public class MessageTypesUnitTestCase extends JBossJMSTestCase
{
   static String QUEUE_FACTORY = "ConnectionFactory";
   static String TEST_QUEUE = "queue/testQueue";

   private Context context;
   private QueueConnection queueConnection;
   private QueueSession session;
   private QueueSender sender;
   private QueueReceiver receiver;

   public MessageTypesUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testMapMessage() throws Exception
   {
      log.info("+++ testMapMessage");
      MapMessage sent = session.createMapMessage();
      sent.setBoolean("Boolean", true);
      sent.setByte("Byte", (byte) 1);
      sent.setBytes("Bytes", "Bytes".getBytes());
      sent.setChar("Char", 'c');
      sent.setShort("Short", (short) 31415);
      sent.setInt("Int", 314159);
      sent.setLong("Long", 3141592653589793238L);
      sent.setDouble("Double", 3.1415926535897932384626433832795);
      sent.setFloat("Float", 3.141f);
      sent.setObject("Object", "31415926535897932384626433832795");
      sent.setString("String", "31415926535897932384626433832795");

      MapMessage recv = (MapMessage) sendRecMsg(sent);
      log.debug("recv: "+recv);
      assertTrue("Boolean == true", recv.getBoolean("Boolean") == true);
      assertTrue("Byte == 1", recv.getByte("Byte") == 1);
      assertTrue("Bytes == Bytes[]",
         Arrays.equals(recv.getBytes("Bytes"), "Bytes".getBytes()));
      assertTrue("Char == c", recv.getChar("Char") == 'c');
      assertTrue("Short == 314159", recv.getShort("Short") == 31415);
      assertTrue("Int == 314159", recv.getInt("Int") == 314159);
      assertTrue("Long == 3141592653589793238L",
         recv.getLong("Long") == 3141592653589793238L);
      assertTrue("Double == 3.1415926535897932384626433832795",
         recv.getDouble("Double") == 3.1415926535897932384626433832795);
      assertTrue("Float == true", recv.getFloat("Float") == 3.141f);
      assertTrue("Object == 31415926535897932384626433832795",
         recv.getObject("Object").equals("31415926535897932384626433832795"));
      assertTrue("String == 31415926535897932384626433832795",
         recv.getString("String").equals("31415926535897932384626433832795"));
   }
   public void testTextMessage() throws Exception
   {
      log.info("+++ testTextMessage");
      String text = "A multiline text msg.\nSecond line.\n";
      TextMessage sent = session.createTextMessage(text);
      TextMessage recv = (TextMessage) sendRecMsg(sent);
      log.debug("recv: "+recv);
      assertTrue(recv.getText().equals(text));
   }
   public void testMessage() throws Exception
   {
      log.info("+++ testMessage");
      Message sent = session.createMessage();
      sent.setBooleanProperty("Boolean", true);
      sent.setByteProperty("Byte", (byte) 1);
      sent.setShortProperty("Short", (short) 31415);
      sent.setIntProperty("Int", 314159);
      sent.setLongProperty("Long", 3141592653589793238L);
      sent.setDoubleProperty("Double", 3.1415926535897932384626433832795);
      sent.setFloatProperty("Float", 3.141f);
      sent.setObjectProperty("Object", "31415926535897932384626433832795");
      sent.setStringProperty("String", "31415926535897932384626433832795");

      Message recv = sendRecMsg(sent);
      log.debug("recv: "+recv);
      assertTrue("Boolean == true", recv.getBooleanProperty("Boolean") == true);
      assertTrue("Byte == 1", recv.getByteProperty("Byte") == 1);
      assertTrue("Short == 314159", recv.getShortProperty("Short") == 31415);
      assertTrue("Int == 314159", recv.getIntProperty("Int") == 314159);
      assertTrue("Long == 3141592653589793238L",
         recv.getLongProperty("Long") == 3141592653589793238L);
      assertTrue("Double == 3.1415926535897932384626433832795",
         recv.getDoubleProperty("Double") == 3.1415926535897932384626433832795);
      assertTrue("Float == true", recv.getFloatProperty("Float") == 3.141f);
      assertTrue("Object == 31415926535897932384626433832795",
         recv.getObjectProperty("Object").equals("31415926535897932384626433832795"));
      assertTrue("String == 31415926535897932384626433832795",
         recv.getStringProperty("String").equals("31415926535897932384626433832795"));
   }
   public void testBytesMessage() throws Exception
   {
      log.info("+++ testBytesMessage");
      BytesMessage sent = session.createBytesMessage();
      sent.writeBoolean(true);
      sent.writeByte((byte) 1);
      byte[] testBytes = "Bytes".getBytes();
      sent.writeBytes(testBytes);
      sent.writeChar('c');
      sent.writeShort((short) 31415);
      sent.writeInt(314159);
      sent.writeLong(3141592653589793238L);
      sent.writeDouble(3.1415926535897932384626433832795);
      sent.writeFloat(3.141f);
      sent.writeObject("31415926535897932384626433832795");
      sent.writeUTF("31415926535897932384626433832795");

      BytesMessage recv = (BytesMessage) sendRecMsg(sent);
      log.debug("recv: "+recv);
      assertTrue("Boolean == true", recv.readBoolean() == true);
      assertTrue("Byte == 1", recv.readByte() == 1);
      byte[] bytes = new byte[testBytes.length];
      recv.readBytes(bytes); 
      assertTrue("Bytes == Bytes[]",
         Arrays.equals(bytes, testBytes));
      assertTrue("Char == c", recv.readChar() == 'c');
      assertTrue("Short == 314159", recv.readShort() == 31415);
      assertTrue("Int == 314159", recv.readInt() == 314159);
      assertTrue("Long == 3141592653589793238L",
         recv.readLong() == 3141592653589793238L);
      assertTrue("Double == 3.1415926535897932384626433832795",
         recv.readDouble() == 3.1415926535897932384626433832795);
      assertTrue("Float == true", recv.readFloat() == 3.141f);
      assertTrue("Object == 31415926535897932384626433832795",
         recv.readUTF().equals("31415926535897932384626433832795"));
      assertTrue("String == 31415926535897932384626433832795",
         recv.readUTF().equals("31415926535897932384626433832795"));
   }
   public void testObjectMessage() throws Exception
   {
      log.info("+++ testObjectMessage");
      BigInteger data = new BigInteger("31415926535897932384626433832795", 10);
      ObjectMessage sent = session.createObjectMessage(data);
      ObjectMessage recv = (ObjectMessage) sendRecMsg(sent);
      log.debug("recv: "+recv);
      BigInteger data2 = (BigInteger) recv.getObject();
      assertTrue("BigInteger == BigInteger2", data2.equals(data));
   }
   public void testStreamMessage() throws Exception
   {
      log.info("+++ testStreamMessage");
      StreamMessage sent = session.createStreamMessage();
      sent.writeBoolean(true);
      sent.writeByte((byte) 1);
      byte[] testBytes = "Bytes".getBytes();
      sent.writeBytes(testBytes);
      sent.writeChar('c');
      sent.writeShort((short) 31415);
      sent.writeInt(314159);
      sent.writeLong(3141592653589793238L);
      sent.writeDouble(3.1415926535897932384626433832795);
      sent.writeFloat(3.141f);
      sent.writeObject("31415926535897932384626433832795");
      sent.writeString("31415926535897932384626433832795");

      StreamMessage recv = (StreamMessage) sendRecMsg(sent);
      log.debug("recv: "+recv);
      assertTrue("Boolean == true", recv.readBoolean() == true);
      assertTrue("Byte == 1", recv.readByte() == 1);
      // Quirky spec behavior requires a read past the end of the byte[] field
      byte[] bytes = new byte[testBytes.length];
      recv.readBytes(bytes);
      assertTrue(recv.readBytes(bytes) < 0);
      assertTrue("Bytes == Bytes[]",
         Arrays.equals(bytes, testBytes));
      char c = recv.readChar();
      assertTrue("Char == c", c == 'c');
      assertTrue("Short == 314159", recv.readShort() == 31415);
      assertTrue("Int == 314159", recv.readInt() == 314159);
      assertTrue("Long == 3141592653589793238L",
         recv.readLong() == 3141592653589793238L);
      assertTrue("Double == 3.1415926535897932384626433832795",
         recv.readDouble() == 3.1415926535897932384626433832795);
      assertTrue("Float == true", recv.readFloat() == 3.141f);
      assertTrue("Object == 31415926535897932384626433832795",
         recv.readObject().equals("31415926535897932384626433832795"));
      assertTrue("String == 31415926535897932384626433832795",
         recv.readString().equals("31415926535897932384626433832795"));
   }

   protected void setUp() throws Exception
   {
       // call setUp() in superclass
       super.setUp() ;
       
       JMSDestinationsUtil.setupBasicDestinations();

      context = new InitialContext();
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();
      queueConnection.start();
      session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);
      sender = session.createSender(queue);
      receiver = session.createReceiver(queue);

      log.debug("Connection to jms established.");
   }

   protected void tearDown() throws Exception
   {
      sender.close();
      receiver.close();
      session.close();
      queueConnection.close();
      
      JMSDestinationsUtil.destroyDestinations();

      // call tearDown() in superclass
      super.tearDown() ;
   }

   private Message sendRecMsg(Message in) throws Exception
   {
      sender.send(in, DeliveryMode.NON_PERSISTENT, 4, 0);
      Message out = receiver.receive(5000);
      return out;
   }

}
