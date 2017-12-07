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

package org.jboss.test.cluster.testutil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Tests for the existence of the problem described at
 * http://wiki.jboss.org/wiki/Wiki.jsp?page=PromiscuousTraffic
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class PromiscuousTrafficTester
{
   private InetAddress goodMulticast;
   private InetAddress badMulticast;
   private InetAddress senderAddress;
   private InetAddress receiverAddress;
   private int mcastPort = 64000;
   private int ttl = 0;
   
   private MulticastSocket sender;
   private MulticastReceiver goodReceiver;
   private MulticastReceiver badReceiver;
   
   private PromiscuousTrafficTester(String[] args) throws Exception
   {
      // Set defaults
      goodMulticast = InetAddress.getByName("229.10.11.12");
      badMulticast = InetAddress.getByName("229.10.11.13");
      senderAddress = InetAddress.getLocalHost();
      receiverAddress = InetAddress.getLocalHost();
      
      // Check what was passed in
      parseArgs(args);
      
      try
      {
         setUpSockets();
      }
      catch (Exception e)
      {
         closeSockets();
         throw e;
      }
   }
   
   private void test() throws Exception
   {
      try
      {
         byte[] data = "Hello".getBytes();
         DatagramPacket datagram = new DatagramPacket(data, 0, data.length, goodMulticast, mcastPort);
         
         System.out.println("Sending 'Hello' on " + goodMulticast + ":" + mcastPort);
         sender.send(datagram);
         
         Thread.sleep(1000);
         
         if ("Hello".equals(goodReceiver.getValue()) == false)
            throw new IllegalStateException("Did not receive 'Hello' as expected; got " + goodReceiver.getValue());
         
         if (badReceiver.getValue() != null)
         {
            System.out.println("Bad news. Detected the Promiscuous Traffic " +
                    "problem. Received " + badReceiver.getValue() + 
                    " on undesired address " + badMulticast); 
         }
         else
         {
            System.out.println("Good news. Did not detect the Promiscuous Traffic problem.");
         }
      }
      finally
      {
         closeSockets();
      }
   }
   
   private void parseArgs(String[] args) throws UnknownHostException
   {
      if (args == null || args.length == 0)
         return;
      
      senderAddress = InetAddress.getByName(args[0]);      
      
      if (args.length > 1)
         receiverAddress = InetAddress.getByName(args[1]);
      else
         receiverAddress = senderAddress;
      
      if (args.length > 2)
         goodMulticast = InetAddress.getByName(args[2]);
      
      if (args.length > 3)
         badMulticast = InetAddress.getByName(args[3]);
      
      if (args.length > 4)
         mcastPort = Integer.parseInt(args[4]);
      
      if (args.length > 5)
         ttl = Integer.parseInt(args[5]);
   }
   
   private void setUpSockets() throws IOException
   {
      sender = new MulticastSocket();
      sender.setInterface(senderAddress);
      sender.setTimeToLive(ttl);
      
      goodReceiver = new MulticastReceiver(goodMulticast);
      goodReceiver.startListener();
      
      badReceiver = new MulticastReceiver(badMulticast);
      badReceiver.startListener();
      
      // Sleep a bit to let the listeners start
      try
      {
         Thread.sleep(250);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }
   
   private void closeSockets()
   {
      if (goodReceiver != null)
         goodReceiver.stopListener();
      
      if (badReceiver != null)
         badReceiver.stopListener();
      
      if (sender != null)
         sender.close();
   }
   
   /**
    * Create and run the PromiscuousTrafficTester.
    * 
    * @param args 0 or more of the following; not all need be present, but none
    *             can be skipped if subsequent values are desired:
    *             [0] Interface sender should bind to (machine name)
    *             [1] Interface receivers should bind to (same as sender interface)
    *             [2] Multicast address on which packets will be sent (229.10.11.12)
    *             [3] Multicast address on which receipt of packet means (229.10.11.13)
    *                 promiscuous traffic problem is present
    *             [4] Multicast port (64000)
    *             [5] TTL of multicast packet (0)
    */
   public static void main(String[] args)
   {
      try
      {
         PromiscuousTrafficTester tester = new PromiscuousTrafficTester(args);
         tester.test();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   private class MulticastReceiver implements Runnable
   {
      private InetAddress group;
      private MulticastSocket socket;
      private String value;
      private Exception exception;
      private Thread thread;
      private boolean stopping;
      
      MulticastReceiver(InetAddress mcastAddr) throws IOException
      {
         group = mcastAddr;
         socket = new MulticastSocket(mcastPort);
         socket.setInterface(receiverAddress);
         socket.joinGroup(mcastAddr);
      }

      
      public void run()
      {
         try
         {
            System.out.println("Listening on address " + group);
            
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            
            value = new String(packet.getData()).trim();
            
            System.out.println(group + ":" + mcastPort + " -- Received " + value);
         }
         catch (Exception e)
         {
            if (!stopping)
            {
               this.exception = e;
               e.printStackTrace(System.out);
            }
         }
      }


      public Exception getException()
      {
         return exception;
      }

      public String getValue()
      {
         return value;
      }
      
      public void startListener()
      {
         thread = new Thread(this);
         thread.start();
      }
      
      public void stopListener()
      {
         stopping = true;
         
         if (thread.isAlive())
         {
            try
            {
               thread.join(50);
            }
            catch (InterruptedException e)
            {               
            }
         
            if (thread.isAlive())
               thread.interrupt();
         }
         
         try
         {
            socket.leaveGroup(group);
         }
         catch (IOException e)
         {
         }
         
         socket.close();
      }
      
   }
}
