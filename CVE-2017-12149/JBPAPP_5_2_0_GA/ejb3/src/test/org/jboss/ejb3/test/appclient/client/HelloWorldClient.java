/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.test.appclient.client;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jboss.ejb3.test.appclient.HelloWorldService;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class HelloWorldClient
{
   @EJB
   private static HelloWorldService helloWorldService;
   
   @Resource(name="msg")
   private static String msg;
   
   private static String result;
   
   @Resource(mappedName="ConnectionFactory")
   private static ConnectionFactory connectionFactory;
   
   @Resource(name="messageReplier")
   private static Destination destination;
   
   private static int postConstructCalls = 0;
   
   public static int getPostConstructCalls()
   {
      return postConstructCalls;
   }
   
   public static String getResult()
   {
      return result;
   }
   
   public static void main(String args[])
   {
      String name = "unspecified";
      if(args.length > 0)
         name = args[0];
      
      if(helloWorldService == null)
         throw new NullPointerException("helloWorldService is null");
      
      if(msg == null)
         throw new NullPointerException("msg is null");
      
      result = helloWorldService.sayHelloTo(name) + ", " + msg;
      
      testMDB();
   }
   
   public static void testMDB()
   {
      if(connectionFactory == null)
         throw new NullPointerException("connectionFactory is null");
      
      if(destination == null)
         throw new NullPointerException("destination is null");
      
      try
      {
         Connection conn = connectionFactory.createConnection();
         Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         
         Queue replyTo = session.createTemporaryQueue();
         TextMessage msg = session.createTextMessage("Hello world");
         msg.setJMSReplyTo(replyTo);
         
         MessageConsumer consumer = session.createConsumer(replyTo);
         conn.start();
         
         MessageProducer producer = session.createProducer(destination);
         producer.send(destination, msg);
         
         TextMessage reply = (TextMessage) consumer.receive(2000);
         System.out.println("reply = " + reply.getText());
         
         producer.close();
         conn.stop();
         consumer.close();
         session.close();
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static void postConstruct()
   {
      postConstructCalls++;
   }
}
