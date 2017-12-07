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
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import junit.framework.TestCase;

import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.ha.framework.server.ProtocolStackConfigInfo;
import org.jboss.ha.framework.server.managed.ProtocolStackConfigurationsMapper;
import org.jboss.logging.Logger;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.test.cluster.channelfactory.managed.ManagedObjectTestUtil;
import org.jgroups.Channel;

/**
 * Unit tests for ProtocolStackConfigurationsMapper
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ProtocolStackConfigurationsMapperUnitTestCase extends TestCase
{

   private static final Logger log = Logger.getLogger(ProtocolStackConfigurationsMapper.class);
   
   private JChannelFactory factory;
   private String jgroups_bind_addr;
   private MBeanServer mbeanServer;
   
   /**
    * Create a new ProtocolStackConfigurationsMapperUnitTestCase.
    * 
    * @param name
    */
   public ProtocolStackConfigurationsMapperUnitTestCase(String name)
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
      
      if (factory != null)
      {
         factory.stop();
         factory.destroy();
      }
   }
   
   public void testRoundTrip() throws Exception
   {
      Map<String, ProtocolStackConfigInfo> map = factory.getProtocolStackConfigurations();
      
      ProtocolStackConfigurationsMapper testee = new ProtocolStackConfigurationsMapper();
      
      MetaValue metaValue = testee.createMetaValue(ProtocolStackConfigurationsMapper.TYPE, map);
      ManagedObjectTestUtil.validateProtocolStackConfigurations(metaValue, new String[]{"unshared1", "shared1"});
      
      Map<String, ProtocolStackConfigInfo> restored = testee.unwrapMetaValue(metaValue);
      assertEquals(map.keySet(), restored.keySet());
      
      // FIXME go deeper
   }

}
