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

package org.jboss.test.cluster.defaultcfg.test;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.logging.Logger;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.mux.MuxChannel;
import org.jgroups.protocols.TP;
import org.jgroups.stack.IpAddress;
import org.jgroups.stack.Protocol;

/**
 * Basic tests of the AS-specific JChannelFactory
 * @author Brian Stansberry
 */
public class JChannelFactoryUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(JChannelFactoryUnitTestCase.class);
   
   private JChannelFactory factory1;
   private JChannelFactory factory2;
   private Channel channel1;
   private Channel channel2;
   private Channel channel3;
   private Channel channel4;
   private String jgroups_bind_addr;
   private MBeanServer mbeanServer;
   
   /**
    * Create a new JChannelFactoryUnitTestCase.
    * 
    * @param name
    */
   public JChannelFactoryUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      jgroups_bind_addr = System.getProperty("jgroups.bind_addr");
      if (jgroups_bind_addr == null)
      {
         System.setProperty("jbosstest.cluster.node0", System.getProperty("jbosstest.cluster.node0", "localhost"));
      }
      
      factory1 = new JChannelFactory();
      factory1.setMultiplexerConfig("cluster/channelfactory/stacks.xml");
      factory1.setAssignLogicalAddresses(false);
      factory1.setNodeAddress(InetAddress.getByName("localhost"));
      factory1.setNamingServicePort(123);
      factory1.setExposeChannels(false);
      factory1.setManageReleasedThreadClassLoader(true);
      factory1.create();
      factory1.start();
      factory2 = new JChannelFactory();
      factory2.setMultiplexerConfig("cluster/channelfactory/stacks.xml");
      factory2.setAssignLogicalAddresses(false);
      factory2.setNodeAddress(InetAddress.getByName("localhost"));
      factory2.setNamingServicePort(456);
      factory2.setNodeName("node1");
      factory2.setExposeChannels(false);
      factory2.setManageReleasedThreadClassLoader(true);
      factory2.create();
      factory2.start();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (mbeanServer != null)
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      
      if (jgroups_bind_addr == null)
         System.clearProperty("jgroups.bind_addr");
      
      if (channel1 != null && channel1.isOpen())
         channel1.close();
      
      if (channel2 != null && channel2.isOpen())
         channel2.close();
      
      if (channel3 != null && channel3.isOpen())
         channel3.close();
      
      if (channel4 != null && channel4.isOpen())
         channel4.close();
      
      if (factory1 != null)
      {
         factory1.stop();
         factory1.destroy();
      }
      if (factory2 != null)
      {
         factory2.stop();
         factory1.destroy();
      }
   }
   
   public void testNoSingletonName() throws Exception
   {
      log.info("+++ testNoSingletonName()");      
      
      // Assert the stack is as expected
      String cfg1 = factory1.getConfig("unshared1");
      assertTrue("no singleton_name in unshared1", cfg1.indexOf("singleton_name") < 0);
      String cfg2 = factory1.getConfig("unshared2");
      assertTrue("no singleton_name in unshared2", cfg2.indexOf("singleton_name") < 0);
      
      channel1 = factory1.createChannel("unshared1");
      assertFalse(channel1 instanceof MuxChannel);
      channel1.connect("test");      
      TP tp1 = getTP((JChannel) channel1);
      
      channel2 = factory2.createChannel("unshared2");
      assertFalse(channel2 instanceof MuxChannel);
      channel2.connect("test");
      TP tp2 = getTP((JChannel) channel2);
      
      assertNotSame(tp1, tp2);
      
      channel3 = factory1.createMultiplexerChannel("unshared1", "test");
      assertFalse(channel3 instanceof MuxChannel);
      // JBAS-7015 use unique name
      //channel3.connect("test");
      channel3.connect("test3");
      TP tp3 = getTP((JChannel) channel3);
      
      //JBAS-7015 -- change assert
      //assertNotSame(tp1, tp3);
      assertSame(tp1, tp3);
      assertNotSame(tp2, tp3);
      
      channel4 = factory1.createMultiplexerChannel("unshared1", "test2");
      assertFalse(channel4 instanceof MuxChannel);
      channel4.connect("test4");
      TP tp4 = getTP((JChannel) channel4);
      
      assertSame(tp3, tp4);
   }
   
   /**
    * Confirms that thread pool configurations are as expected following
    * any massaging by the factory
    */
   public void testThreadPoolConfig() throws Exception
   {
      log.info("+++ testThreadPoolConfig()");      
      
      channel1 = factory1.createChannel("queues");
      channel1.connect("test");
      TP tp1 = getTP((JChannel) channel1);
      
      assertEquals(3000, tp1.getIncomingKeepAliveTime());
      assertEquals(22, tp1.getIncomingMaxPoolSize());
      assertEquals(2, tp1.getIncomingMinPoolSize());
      assertEquals(750, tp1.getIncomingMaxQueueSize());
      assertEquals(4000, tp1.getOOBKeepAliveTime());
      assertEquals(12, tp1.getOOBMaxPoolSize());
      assertEquals(3, tp1.getOOBMinPoolSize());
      assertEquals(75, tp1.getOOBMaxQueueSize());
      
      Executor exec = tp1.getDefaultThreadPool();
      assertNotNull(exec);
      
      exec = tp1.getOOBThreadPool();
      assertNotNull(exec);
      
      // Confirm that the no-pool config doesn't create a pool
      channel2 = factory1.createChannel("nonconcurrent1");
      channel2.connect("test");
      TP tp2 = getTP((JChannel) channel2);
      
      assertFalse(tp2.getDefaultThreadPool() instanceof ThreadPoolExecutor);
      assertFalse(tp2.getOOBThreadPool() instanceof ThreadPoolExecutor);
   }
   
   public void testJmxHandling() throws Exception
   {
      log.info("+++ testJmxHandling()");
      
      mbeanServer = MBeanServerFactory.createMBeanServer("jchannelfactorytest");
      
      ObjectName factoryName1 = new ObjectName("jboss.test:service=TestChannelFactory1");
      ObjectName factoryName2 = new ObjectName("jboss.test:service=TestChannelFactory2");
      
      // destroy the factories so we can start clean
      factory1.stop();
      factory1.destroy();
      factory2.stop();
      factory2.destroy();
      
      factory1.setExposeChannels(true);
      factory1.setExposeProtocols(false);
      
      factory2.setExposeChannels(true);
      factory2.setExposeProtocols(true);
      
      mbeanServer.registerMBean(factory1, factoryName1);
      
      assertSame(mbeanServer, factory1.getServer());
      assertEquals(factoryName1.getDomain(), factory1.getDomain());
      
      mbeanServer.registerMBean(factory2, factoryName2);
      
      factory1.create();
      factory1.start();
      factory2.create();
      factory2.start();
      
      channel1 = factory1.createMultiplexerChannel("shared1", "shared");
      ObjectName chName1 = new ObjectName("jboss.test:type=channel,cluster=shared");
      assertTrue(chName1 + " registered", mbeanServer.isRegistered(chName1));
      ObjectName udpName1 = new ObjectName("jboss.test:type=channel,cluster=shared,protocol=UDP");
      assertFalse(udpName1 + " not registered", mbeanServer.isRegistered(udpName1));
      
      channel2 = factory2.createMultiplexerChannel("unshared1", "unshared");
      ObjectName chName2 = new ObjectName("jboss.test:type=channel,cluster=unshared");
      assertTrue(chName2 + " registered", mbeanServer.isRegistered(chName2));
      ObjectName udpName2 = new ObjectName("jboss.test:type=protocol,cluster=unshared,protocol=UDP");
      assertTrue(udpName2 + " registered", mbeanServer.isRegistered(udpName2));
      
      channel1.connect("shared");
      assertTrue(chName1 + " still registered", mbeanServer.isRegistered(chName1));
      assertTrue(chName2 + " still registered", mbeanServer.isRegistered(chName2));
      
      log.info("closing channel 1");
      
      channel1.close();
      assertFalse(chName1 + " unregistered", mbeanServer.isRegistered(chName1));
      assertTrue(chName2 + " not unregistered", mbeanServer.isRegistered(chName2));
      assertTrue(udpName2 + " not unregistered", mbeanServer.isRegistered(udpName2));
      
      log.info("stopping factory2");
      
      factory2.stop();
      factory2.destroy();
      assertFalse(chName2 + " unregistered", mbeanServer.isRegistered(chName2));
      assertFalse(udpName2 + " unregistered", mbeanServer.isRegistered(udpName2));
   }
   
   public void testLogicalAddressAssignment() throws Exception
   {
      log.info("+++ testLogicalAddressAssignment()");
      
      channel1 = factory1.createChannel("shared1");
      channel1.connect("shared");
      IpAddress addr = (IpAddress) channel1.getLocalAddress();
      assertEquals(null, addr.getAdditionalData());

      factory1.setAssignLogicalAddresses(true);
      factory2.setAssignLogicalAddresses(true);
      
      channel2 = factory1.createChannel("shared2");
      channel2.connect("shared");
      addr = (IpAddress) channel2.getLocalAddress();
      byte[] addlData = addr.getAdditionalData();
      assertNotNull(addlData);
      assertEquals("127.0.0.1:123", new String(addlData));

      channel3 = factory2.createChannel("unshared1");
      channel3.connect("unshared");
      addr = (IpAddress) channel3.getLocalAddress();
      addlData = addr.getAdditionalData();
      assertNotNull(addlData);
      assertEquals("node1",  new String(addlData));
   }
   
   private TP getTP(JChannel channel)
   {
      List<Protocol> protocols = channel.getProtocolStack().getProtocols();
      return (TP) protocols.get(protocols.size() -1);
   }
   
   
}
