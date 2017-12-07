/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import junit.framework.TestCase;

import org.jboss.ha.framework.server.ChannelInfo;
import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.ha.framework.server.managed.OpenChannelsMapper;
import org.jboss.logging.Logger;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.test.cluster.channelfactory.managed.ManagedObjectTestUtil;
import org.jboss.test.cluster.channelfactory.managed.ManagedObjectTestUtil.ChannelIds;
import org.jgroups.Channel;

/**
 * Unit tests for OpenChannelsMapper
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class OpenChannelsMapperUnitTestCase extends TestCase
{

   private static final Logger log = Logger.getLogger(OpenChannelsMapperUnitTestCase.class);
   
   private JChannelFactory factory;
   private Channel channel1;
   private Channel channel2;
   private Channel channel3;
   private Channel channel4;
   private String jgroups_bind_addr;
   private MBeanServer mbeanServer;
   
   /**
    * Create a new ProtocolStackConfigurationsMapperUnitTestCase.
    * 
    * @param name
    */
   public OpenChannelsMapperUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      String jgroups_bind_addr = System.getProperty("jgroups.bind_addr");
      if (jgroups_bind_addr == null)
      {
         System.setProperty("jbosstest.cluster.node0", System.getProperty("jbosstest.cluster.node0", "localhost"));
      }
      
      mbeanServer = MBeanServerFactory.createMBeanServer("jchannelfactorytest");
      
      factory = new JChannelFactory();
      factory.setMultiplexerConfig("cluster/channelfactory/stacks.xml");
      factory.setAssignLogicalAddresses(false);
      factory.setNodeAddress(InetAddress.getByName("localhost"));
      factory.setNamingServicePort(123);
      factory.setExposeChannels(true);
      factory.setServer(mbeanServer);
      factory.setManageReleasedThreadClassLoader(true);
      factory.create();
      factory.start();
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
      
      if (factory != null)
      {
         factory.stop();
         factory.destroy();
      }
   }
   
   public void testCreateMetaValue() throws Exception
   {
      channel1 = factory.createMultiplexerChannel("unshared1", "no1");
      channel1.connect("channel1");
      channel2 = factory.createMultiplexerChannel("shared1", "no2");
      channel2.connect("channel2");
      
      Set<ChannelInfo> channels = factory.getOpenChannels();
      OpenChannelsMapper testee = new OpenChannelsMapper();
      MetaValue val = testee.createMetaValue(testee.getMetaType(), channels);
      Set<ChannelIds> ids = ManagedObjectTestUtil.validateOpenChannels(val);
      
      for (ChannelIds cid : ids)
      {
         if ("no1".equals(cid.id))
         {
            assertEquals("unshared1", cid.stackName);
            assertEquals("channel1", cid.clusterName);
         }
         else if ("no2".equals(cid.id))
         {
            assertEquals("shared1", cid.stackName);
            assertEquals("channel2", cid.clusterName);            
         }
         else
         {
            fail("unknown id " + cid.id);
         }
      }
   }
}
