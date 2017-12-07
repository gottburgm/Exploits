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
package org.jboss.test.cluster.mod_cluster;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Assert;

import org.jboss.test.JBossClusteredTestCase;

/**
 * A ModClusterServiceTestCase.
 * 
 * @author Paul Ferraro
 */
public class ModClusterServiceTestCase extends JBossClusteredTestCase
{
   private static final String ENGINE = "jboss.web";
   private static final int PORT = 8009;
   
   private MockProxy proxy = new MockProxy(2);
   
   /**
    * Create a new ModClusterServiceTestCase.
    * 
    * @param name
    */
   public ModClusterServiceTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      this.proxy.start();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      this.proxy.stop();
      
      super.tearDown();
   }
   
   public void testInfo() throws Exception
   {
      MBeanServerConnection[] servers = this.getAdaptors();
      
      ObjectName name = ObjectName.getInstance("jboss.web:service=ModCluster");
      
      String[] hosts = this.getServers();
      
      for (int i = 0; i < hosts.length; ++i)
      {
         String host = InetAddress.getByName(hosts[i]).getHostAddress();
         
         servers[i].invoke(name, "addProxy", new Object[] { "127.0.0.1", this.proxy.getPort() }, new String[] { String.class.getName(), Integer.TYPE.getName() });
         
         // Wait the duration of at least 1 status interval
         Thread.sleep(20000);
         
         // Proxy should have received:
         // 1. INFO - to establish proxy connectivity
         // 2. CONFIG - to configure proxy
         // 3. STATUS - periodic status
         
         List<Map.Entry<String, Map<String, String>>> requests = new LinkedList<Map.Entry<String, Map<String, String>>>();
         
         int count = this.proxy.getRequests().drainTo(requests);

         Assert.assertEquals(1, count);

         for (Map.Entry<String, Map<String, String>> infoRequest: requests)
         {
            String command = infoRequest.getKey();
            Map<String, String> parameters = infoRequest.getValue();
            
            Assert.assertTrue(command, command.startsWith("INFO"));
            Assert.assertFalse(command.contains("*"));
            Assert.assertTrue(parameters.isEmpty());
         }
         
         requests.clear();
         
         String jvmRoute = hosts[i];
         
         count = this.proxy.getRequests(jvmRoute).drainTo(requests);
         
         Assert.assertTrue(Integer.toString(count), count >= 2);
         
         Map.Entry<String, Map<String, String>> configRequest = requests.get(0);
         String command = configRequest.getKey();
         Map<String, String> parameters = configRequest.getValue();

         Assert.assertTrue(command, command.startsWith("CONFIG"));
         Assert.assertFalse(command.contains("*"));
         Assert.assertEquals(parameters.toString(), 6, parameters.size());
         Assert.assertEquals(String.valueOf(PORT), parameters.get("Port"));
         Assert.assertEquals(host, parameters.get("Host"));
         Assert.assertEquals("ajp", parameters.get("Type"));
         Assert.assertEquals(jvmRoute, parameters.get("JVMRoute"));
         Assert.assertEquals("No", parameters.get("StickySessionForce"));
         Assert.assertEquals("1", parameters.get("Maxattempts"));
         
         for (Map.Entry<String, Map<String, String>> statusRequest: requests.subList(1, requests.size()))
         {
            command = statusRequest.getKey();
            parameters = statusRequest.getValue();
            
            Assert.assertTrue(command, command.startsWith("STATUS"));
            Assert.assertFalse(command.contains("*"));
            Assert.assertEquals(2, parameters.size());
            Assert.assertEquals(jvmRoute, parameters.get("JVMRoute"));
            String value = parameters.get("Load");
            Assert.assertNotNull(value);
            int load = Integer.parseInt(value);
            Assert.assertTrue(0 <= load);
            Assert.assertTrue(load <= 100);
         }
      }
   }
}
