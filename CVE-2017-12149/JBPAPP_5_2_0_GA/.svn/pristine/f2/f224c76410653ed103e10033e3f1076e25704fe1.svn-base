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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.ha.framework.server.ProtocolStackConfigInfo;
import org.jboss.ha.framework.server.managed.ProtocolDataProtocolStackConfigurator;
import org.jboss.logging.Logger;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.conf.ProtocolData;
import org.jgroups.conf.ProtocolParameter;

/**
 * Tests JChannelFactory's handling of configuration overrides via
 * @[link {@link JChannelFactory#setProtocolStackConfigurations(java.util.Map)}}
 * 
 * @author Brian Stansberry
 */
public class JChannelFactoryOverrideUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(JChannelFactoryOverrideUnitTestCase.class);
   
   private JChannelFactory factory;
   private Channel channel1;
   private Channel channel2;
   private String jgroups_bind_addr;
   
   /**
    * Create a new JChannelFactoryUnitTestCase.
    * 
    * @param name
    */
   public JChannelFactoryOverrideUnitTestCase(String name)
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
      
      factory = new JChannelFactory();
      factory.setAssignLogicalAddresses(false);
      factory.setNodeAddress(InetAddress.getByName("localhost"));
      factory.setNamingServicePort(123);
      factory.setExposeChannels(false);
      factory.setManageReleasedThreadClassLoader(true);
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (jgroups_bind_addr == null)
         System.clearProperty("jgroups.bind_addr");
      
      if (channel1 != null && channel1.isOpen())
         channel1.close();
      
      if (channel2 != null && channel2.isOpen())
         channel2.close();
      
      if (factory != null)
      {
         factory.stop();
         factory.destroy();
      }
   }
   
   public void testOverrideAfterStart() throws Exception
   {
      overrideTest(true);
   }
   
   public void testOverrideBeforeStart() throws Exception
   {
      overrideTest(false);
   }
   
   private void overrideTest(boolean startBeforeOverride) throws Exception
   {
      factory.setMultiplexerConfig("cluster/channelfactory/stacks.xml");  
      if (startBeforeOverride)
      {
         factory.create();
         factory.start();
      }
      
      Map<String, ProtocolStackConfigInfo> origMap = factory.getProtocolStackConfigurations();
      Set<String> origKeys = new HashSet<String>(origMap.keySet());
      ProtocolStackConfigInfo unshared1 = origMap.get("unshared1");
      assertNotNull(unshared1);
      ProtocolData[] origConfig = unshared1.getConfiguration();
      assertNotNull(origConfig);
      // Copy it off so we know it's unchanged for later assertion comparisons
      origConfig = origConfig.clone();
      ProtocolData origTransport = origConfig[0];
      assertNotNull(origTransport);
      ProtocolParameter[] origParams = origTransport.getParametersAsArray();
      ProtocolParameter[] newParams = origParams.clone();      
      ProtocolData newTransport = new ProtocolData(origTransport.getProtocolName(), origTransport.getDescription(), origTransport.getClassName(), newParams);
      ProtocolParameter overrideParam = new ProtocolParameter("max_bundle_size", "50000");
      newTransport.override(new ProtocolParameter[]{overrideParam});
      ProtocolData[] newConfig = origConfig.clone();
      newConfig[0] = newTransport;
      
      ProtocolStackConfigInfo updated = new ProtocolStackConfigInfo(unshared1.getName(), unshared1.getDescription(), new ProtocolDataProtocolStackConfigurator(newConfig));
      
      Map<String, ProtocolStackConfigInfo> newMap = new HashMap<String, ProtocolStackConfigInfo>(origMap);
      newMap.put("unshared1", updated);
    
      ProtocolData[] addedConfig = origConfig.clone();
      ProtocolStackConfigInfo added = new ProtocolStackConfigInfo("added", "added", new ProtocolDataProtocolStackConfigurator(addedConfig));
      newMap.put("added", added);
      
      assertTrue(newMap.containsKey("shared2"));
      newMap.remove("shared2");
      
      factory.setProtocolStackConfigurations(newMap);
      
      if (startBeforeOverride == false)
      {
         factory.create();
         factory.start();
      }
      
      Map<String, ProtocolStackConfigInfo> reread = factory.getProtocolStackConfigurations();
      origKeys.remove("shared2");
      origKeys.add("added");
      assertEquals(origKeys, reread.keySet());
      
      ProtocolStackConfigInfo addedInfo = reread.get("added");
      assertEquals("added", addedInfo.getName());
      assertEquals("added", addedInfo.getDescription());
      ProtocolData[] readAdded = addedInfo.getConfiguration();
      assertEquals(addedConfig.length, readAdded.length);
      for (int i = 0; i < readAdded.length; i++)
      {
         assertEquals(addedConfig[i], readAdded[i]);
         ProtocolParameter[] inputParams = addedConfig[i].getParametersAsArray();
         ProtocolParameter[] outputParams = readAdded[i].getParametersAsArray();
         assertEquals(inputParams.length, outputParams.length);
         @SuppressWarnings("unchecked")
         Map<String, ProtocolParameter> paramMap = readAdded[i].getParameters();
         for (int j = 0; j < inputParams.length; j++)
         {
            ProtocolParameter param = paramMap.get(inputParams[j].getName());
            assertNotNull(param);
            assertEquals(inputParams[j].getValue(), param.getValue());
         }
      }
      
      ProtocolStackConfigInfo updatedInfo = reread.get("unshared1");
      assertEquals("unshared1", updatedInfo.getName());
      ProtocolData[] readUpdated = updatedInfo.getConfiguration();
      assertEquals(origConfig.length, readUpdated.length);
      for (int i = 0; i < readUpdated.length; i++)
      {
         assertEquals(origConfig[i], readUpdated[i]);
         ProtocolParameter[] inputParams = origConfig[i].getParametersAsArray();
         ProtocolParameter[] outputParams = readUpdated[i].getParametersAsArray();
         assertEquals(inputParams.length, outputParams.length);
         @SuppressWarnings("unchecked")
         Map<String, ProtocolParameter> paramMap = readUpdated[i].getParameters();
         for (int j = 0; j < inputParams.length; j++)
         {
            String name = inputParams[j].getName();
            ProtocolParameter param = paramMap.get(name);
            assertNotNull(param);
            if ("max_bundle_size".equals(name))
            {
               assertEquals("50000", param.getValue());
            }
            else
            {
               assertEquals(inputParams[j].getValue(), param.getValue());
            }
         }
      }
      
      // Validate that the overrides actuall affect created channels
      channel1 = factory.createChannel("unshared1");
      assertEquals("50000", ((JChannel) channel1).getProtocolStack().findProtocol("UDP").getProperties().get("max_bundle_size"));
      channel2 = factory.createChannel("added");   
      assertEquals("64000", ((JChannel) channel2).getProtocolStack().findProtocol("UDP").getProperties().get("max_bundle_size"));
      try
      {
         factory.createChannel("shared2");
         fail("should not be able to create a channel for 'shared2'");
      }
      catch (IllegalArgumentException good) {}
      
   }
   
   
}
