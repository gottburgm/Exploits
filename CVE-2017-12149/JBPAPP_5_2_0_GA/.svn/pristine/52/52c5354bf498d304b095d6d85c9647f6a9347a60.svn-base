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

package org.jboss.test.services.binding.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.ServiceBindingSet;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceImpl;
import org.jboss.services.binding.managed.ServiceBindingManagementObject;

/**
 * Unit tests for {@link ServiceBindingManagementObject}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ServiceBindingManagmentObjectUnitTestCase extends TestCase
{
   private static final String A = "A";
   private static final String B = "B";
   private static final String C = "C";
   
   private static ServiceBindingMetadata AA;
   private static ServiceBindingMetadata AB;
   private static ServiceBindingMetadata Anull;
   private static ServiceBindingMetadata BA;
   
   private static ServiceBindingSet SET_A;   
   private static ServiceBindingSet SET_B;   
   private static ServiceBindingSet SET_C;
   
   private Set<ServiceBindingMetadata> bindings = new HashSet<ServiceBindingMetadata>();
   private Set<ServiceBindingSet> bindingSets = new HashSet<ServiceBindingSet>();
   
   /**
    * Create a new ServiceBindingManagmentObjectUnitTestCase.
    * 
    * @param name
    */
   public ServiceBindingManagmentObjectUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      AA = new ServiceBindingMetadata(A, A, null, 1, false, false);
      bindings.add(AA);
      AB = new ServiceBindingMetadata(A, B, null, 1, false, false);
      bindings.add(AB);
      Anull = new ServiceBindingMetadata(A, null, null, 1, false, false);
      bindings.add(Anull);
      
      // This one doesn't go in the standard bindings set
      BA = new ServiceBindingMetadata(B, A, null, 1, false, false);
      
      SET_A = new ServiceBindingSet(A);
      SET_A.setDefaultHostName("localhost");
      bindingSets.add(SET_A);
      SET_B = new ServiceBindingSet(B);
      SET_B.setDefaultHostName("localhost");
      bindingSets.add(SET_B);
      SET_C = new ServiceBindingSet(C);
      SET_C.setDefaultHostName("localhost");
      bindingSets.add(SET_C);
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   /**
    * Test method for {@link org.jboss.services.binding.managed.ServiceBindingManagementObject#setActiveBindingSetName(java.lang.String)}.
    */
   public void testSetActiveBindingSetName() throws Exception
   {
      ServiceBindingManagementObject testee = new ServiceBindingManagementObject("test", bindingSets, bindings);
      testee.start();
      assertEquals("test", testee.getActiveBindingSetName());
      assertEquals("test", testee.getServiceBindingManager().getServerName());
      testee.setActiveBindingSetName("changed");
      assertEquals("changed", testee.getActiveBindingSetName());
      assertEquals("changed", testee.getServiceBindingManager().getServerName());
      
      try
      {
         testee.setActiveBindingSetName(null);
         fail("Null activeBindingSetName allowed");
      }
      catch (IllegalArgumentException good) {}
   }

   /**
    * Test method for {@link ServiceBindingManagementObject#setBindingSets(java.util.Set)}.
    * 
    * This is basically a duplicate of the same test in PojoServiceBindingStoreUnitTestCase.
    */
   public void testSetBindingSets() throws Exception
   {
      // THIS IS BAS
      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB, Anull));
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      ServiceBindingSet setA = new ServiceBindingSet(A, null, 10);
      sbs.add(setA);
      ServiceBindingSet setB = new ServiceBindingSet(B, "localhost", 20);      
      sbs.add(setB);
      
      ServiceBindingManagementObject testee = new ServiceBindingManagementObject("test", sbs, set);
      testee.start();
      
      Set<ServiceBindingSet> updated = new HashSet<ServiceBindingSet>(testee.getBindingSets());
      
      Set<ServiceBindingMetadata> overrides = new HashSet<ServiceBindingMetadata>();
      overrides.add(BA);
      ServiceBindingSet newSet = new ServiceBindingSet(C, "192.168.0.10", 30, overrides);
      updated.add(newSet);
      ServiceBindingSet replaced = new ServiceBindingSet(B, "localhost", 50);
      updated.remove(setB);
      updated.add(replaced);
      assertEquals(3, updated.size());
      
      testee.setBindingSets(updated);
      
      Set<ServiceBindingSet> result = testee.getBindingSets();
      assertNotNull(result);
      assertTrue("has setA", result.contains(setA));
      assertTrue("has setB", result.contains(replaced));
      assertTrue("has newSet", result.contains(newSet));
      
      Set<ServiceBinding> bindings = testee.getServiceBindings().get(C);
      assertNotNull(bindings);
      Map<String, ServiceBinding> byFQN = new HashMap<String, ServiceBinding>();
      for (ServiceBinding binding : bindings)
      {
         byFQN.put(binding.getFullyQualifiedName(), binding);
      }
      
      ServiceBinding aa = byFQN.get(AA.getFullyQualifiedName());
      assertNotNull(aa);
      assertEquals(AA.getServiceName(), aa.getServiceName());
      assertEquals(AA.getBindingName(), aa.getBindingName());
      assertEquals(AA.getDescription(), aa.getDescription());
      assertEquals("192.168.0.10", aa.getHostName());
      assertEquals(AA.getPort() + 30, aa.getPort());
      
      ServiceBinding ab = byFQN.get(AB.getFullyQualifiedName());
      assertNotNull(aa);
      assertEquals(AB.getServiceName(), ab.getServiceName());
      assertEquals(AB.getBindingName(), ab.getBindingName());
      assertEquals(AB.getDescription(), ab.getDescription());
      assertEquals("192.168.0.10", ab.getHostName());
      assertEquals(AB.getPort() + 30, ab.getPort());
      
      ServiceBinding anull = byFQN.get(Anull.getFullyQualifiedName());
      assertNotNull(anull);
      assertEquals(Anull.getServiceName(), anull.getServiceName());
      assertEquals(Anull.getBindingName(), anull.getBindingName());
      assertEquals(Anull.getDescription(), anull.getDescription());
      assertEquals("192.168.0.10", anull.getHostName());
      assertEquals(Anull.getPort() + 30, anull.getPort());
      
      ServiceBinding newOne = byFQN.get(BA.getFullyQualifiedName());
      assertNotNull(newOne);
      assertEquals(BA.getServiceName(), newOne.getServiceName());
      assertEquals(BA.getBindingName(), newOne.getBindingName());
      assertEquals(BA.getDescription(), newOne.getDescription());
      assertEquals("192.168.0.10", newOne.getHostName());
      assertEquals(BA.getPort() + 30, newOne.getPort());
      
      bindings = testee.getServiceBindings().get(B);
      assertNotNull(bindings);
      byFQN = new HashMap<String, ServiceBinding>();
      for (ServiceBinding binding : bindings)
      {
         byFQN.put(binding.getFullyQualifiedName(), binding);
      }
      
      aa = byFQN.get(AA.getFullyQualifiedName());
      assertNotNull(aa);
      assertEquals(AA.getServiceName(), aa.getServiceName());
      assertEquals(AA.getBindingName(), aa.getBindingName());
      assertEquals(AA.getDescription(), aa.getDescription());
      assertEquals("localhost", aa.getHostName());
      assertEquals(AA.getPort() + 50, aa.getPort());
      
      ab = byFQN.get(AB.getFullyQualifiedName());
      assertNotNull(aa);
      assertEquals(AB.getServiceName(), ab.getServiceName());
      assertEquals(AB.getBindingName(), ab.getBindingName());
      assertEquals(AB.getDescription(), ab.getDescription());
      assertEquals("localhost", ab.getHostName());
      assertEquals(AB.getPort() + 50, ab.getPort());
      
      anull = byFQN.get(Anull.getFullyQualifiedName());
      assertNotNull(anull);
      assertEquals(Anull.getServiceName(), anull.getServiceName());
      assertEquals(Anull.getBindingName(), anull.getBindingName());
      assertEquals(Anull.getDescription(), anull.getDescription());
      assertEquals("localhost", anull.getHostName());
      assertEquals(Anull.getPort() + 50, anull.getPort());
   }

   /**
    * Test method for {@link org.jboss.services.binding.managed.ServiceBindingManagementObject#setStandardBindings(java.util.Set)}.
    *
    * This is basically a duplicate of the same test in PojoServiceBindingStoreUnitTestCase.
    */
   public void testSetStandardBindings() throws Exception
   {
      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB, Anull));
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      ServiceBindingSet setB = new ServiceBindingSet(B, "localhost", 20);      
      sbs.add(setB);     
      ServiceBindingSet setC = new ServiceBindingSet(C, "192.168.0.10", 30);
      sbs.add(setC);
      
      ServiceBindingManagementObject testee = new ServiceBindingManagementObject("test", sbs, set);
      testee.start();
      
      Set<ServiceBindingMetadata> updatedSet = 
         new HashSet<ServiceBindingMetadata>(testee.getStandardBindings());
      assertEquals(3, updatedSet.size());
      ServiceBindingMetadata updated = new ServiceBindingMetadata(AA);
      updated.setPort(9999);
      updated.setDescription("updated");
      updatedSet.remove(AA);
      updatedSet.add(updated);
      updatedSet.add(BA);
      assertEquals(4, updatedSet.size());
      
      testee.setStandardBindings(updatedSet);
      
      Set<ServiceBindingMetadata> result = testee.getStandardBindings();
      assertNotNull(result);
      assertTrue("has updated", result.contains(updated));
      assertTrue("has AB", result.contains(AB));
      assertTrue("has Anull", result.contains(Anull));
      assertTrue("has BA", result.contains(BA));
      
      for (ServiceBindingSet bindingSet : sbs)
      {
         String setName = bindingSet.getName();
         Set<ServiceBinding> bindings = testee.getServiceBindings().get(setName);
         assertNotNull(bindings);
         assertEquals(4, bindings.size());
         Map<String, ServiceBinding> byFQN = new HashMap<String, ServiceBinding>();
         for (ServiceBinding binding : bindings)
         {
            byFQN.put(binding.getFullyQualifiedName(), binding);
         }
         
         ServiceBinding aa = byFQN.get(updated.getFullyQualifiedName());
         assertNotNull(aa);
         assertEquals(setName + "/updated/serviceName", updated.getServiceName(), aa.getServiceName());
         assertEquals(setName + "/updated/bindingName", updated.getBindingName(), aa.getBindingName());
         assertEquals(setName + "/updated/description", updated.getDescription(), aa.getDescription());
         assertEquals(setName + "/updated/hostName", bindingSet.getDefaultHostName(), aa.getHostName());
         assertEquals(setName + "/updated/port", updated.getPort() + bindingSet.getPortOffset(), aa.getPort());
         
         ServiceBinding ab = byFQN.get(AB.getFullyQualifiedName());
         assertNotNull(aa);
         assertEquals(setName + "/AB/serviceName", AB.getServiceName(), ab.getServiceName());
         assertEquals(setName + "/AB/bindingName", AB.getBindingName(), ab.getBindingName());
         assertEquals(setName + "/AB/description", AB.getDescription(), ab.getDescription());
         assertEquals(setName + "/AB/hostName", bindingSet.getDefaultHostName(), ab.getHostName());
         assertEquals(setName + "/AB/port", AB.getPort() + bindingSet.getPortOffset(), ab.getPort());
         
         ServiceBinding anull = byFQN.get(Anull.getFullyQualifiedName());
         assertNotNull(anull);
         assertEquals(setName + "/Anull/serviceName", Anull.getServiceName(), anull.getServiceName());
         assertEquals(setName + "/Anull/bindingName", Anull.getBindingName(), anull.getBindingName());
         assertEquals(setName + "/Anull/description", Anull.getDescription(), anull.getDescription());
         assertEquals(setName + "/Anull/hostName", bindingSet.getDefaultHostName(), anull.getHostName());
         assertEquals(setName + "/Anull/port", Anull.getPort() + bindingSet.getPortOffset(), anull.getPort());
         
         ServiceBinding newOne = byFQN.get(BA.getFullyQualifiedName());
         assertNotNull(newOne);
         assertEquals(setName + "/BA/serviceName", BA.getServiceName(), newOne.getServiceName());
         assertEquals(setName + "/BA/bindingName", BA.getBindingName(), newOne.getBindingName());
         assertEquals(setName + "/BA/description", BA.getDescription(), newOne.getDescription());
         assertEquals(setName + "/BA/hostName", bindingSet.getDefaultHostName(), newOne.getHostName());
         assertEquals(setName + "/BA/port", BA.getPort() + bindingSet.getPortOffset(), newOne.getPort());
      }
   }
   
   /**
    * The objects returned via a management console don't include the
    * value source class or config; the ServiceBindingManagementObject is
    * responsible for preserving objects associated with the existing configs.
    * This is a test of that for the standard bindings.
    * 
    * @throws Exception
    */
   public void testPreserveValueSourceStandardBinding() throws Exception
   {

      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB));
      ServiceBindingMetadata value = new ServiceBindingMetadata("value-source", null, null, 25);
      // Add value-source configs we expect to retain!
      value.setServiceBindingValueSourceClassName(XSLTServiceBindingValueSourceImpl.class.getName());
      value.setServiceBindingValueSourceConfig(new XSLTServiceBindingValueSourceConfig("test"));
      set.add(value);
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      ServiceBindingSet setB = new ServiceBindingSet(B, "localhost", 0);      
      sbs.add(setB); 
      
      ServiceBindingManagementObject testee = new ServiceBindingManagementObject("test", sbs, set);
      testee.start();
      
      set = testee.getStandardBindings();
      assertEquals(3, set.size());
      boolean found = false;
      for (ServiceBindingMetadata md : set)
      {
         if ("value-source".equals(md.getFullyQualifiedName()))
         {
            found = true;
            assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
            Object config = md.getServiceBindingValueSourceConfig();
            assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
            assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
            break;
         }
      }
      assertTrue(found);
      
      Map<String, Set<ServiceBinding>> bindingsMap = testee.getServiceBindings();
      assertNotNull(bindingsMap);
      Set<ServiceBinding> bindings = bindingsMap.get(B);
      assertNotNull(bindings);
      found = false;
      for (ServiceBinding md : bindings)
      {
         if ("value-source".equals(md.getFullyQualifiedName()))
         {
            found = true;
            assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
            Object config = md.getServiceBindingValueSourceConfig();
            assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
            assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
            break;
         }
      }
      assertTrue(found);
      
      
      set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB));
      set.add(new ServiceBindingMetadata("value-source", null, null, 25));
      // KEY POINT IN THE WHOLE TEST: we don't configure the value source stuff
      
      testee.setStandardBindings(set);
      
      set = testee.getStandardBindings();
      assertEquals(3, set.size());
      found = false;
      for (ServiceBindingMetadata md : set)
      {
         if ("value-source".equals(md.getFullyQualifiedName()))
         {
            found = true;
            assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
            Object config = md.getServiceBindingValueSourceConfig();
            assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
            assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
            break;
         }
      }
      assertTrue(found);
      
      bindingsMap = testee.getServiceBindings();
      assertNotNull(bindingsMap);
      bindings = bindingsMap.get(B);
      assertNotNull(bindings);
      found = false;
      for (ServiceBinding md : bindings)
      {
         if ("value-source".equals(md.getFullyQualifiedName()))
         {
            found = true;
            assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
            Object config = md.getServiceBindingValueSourceConfig();
            assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
            assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
            break;
         }
      }
      assertTrue(found);
   }
   
   /**
    * The objects returned via a management console don't include the
    * value source class or config; the ServiceBindingManagementObject is
    * responsible for preserving objects associated with the existing configs.
    * This is a test of that for ServiceBindingMetadata in a ServiceBindingSet's
    * override set.
    * 
    * @throws Exception
    */
   public void testPreserveValueSourceOverrideBinding() throws Exception
   {

      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB));

      ServiceBindingMetadata value = new ServiceBindingMetadata("value-source", null, null, 25);
      // Add value-source configs we expect to retain!
      value.setServiceBindingValueSourceClassName(XSLTServiceBindingValueSourceImpl.class.getName());
      value.setServiceBindingValueSourceConfig(new XSLTServiceBindingValueSourceConfig("test"));
      Set<ServiceBindingMetadata> overrides = new HashSet<ServiceBindingMetadata>();
      overrides.add(value);
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      ServiceBindingSet setB = new ServiceBindingSet(B, "localhost", 0, overrides);
      sbs.add(setB); 
      
      ServiceBindingManagementObject testee = new ServiceBindingManagementObject("test", sbs, set);
      testee.start();
      
      sbs = testee.getBindingSets();
      assertEquals(1, sbs.size());

      boolean found = false;
      for (ServiceBindingSet sb : sbs)
      {
         for (ServiceBindingMetadata md : sb.getOverrideBindings())
         {
            if ("value-source".equals(md.getFullyQualifiedName()))
            {
               found = true;
               assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
               Object config = md.getServiceBindingValueSourceConfig();
               assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
               assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
               break;
            }
         }
         assertTrue(found);
      }
      
      Map<String, Set<ServiceBinding>> bindingsMap = testee.getServiceBindings();
      assertNotNull(bindingsMap);
      Set<ServiceBinding> bindings = bindingsMap.get(B);
      assertNotNull(bindings);
      found = false;
      for (ServiceBinding md : bindings)
      {
         if ("value-source".equals(md.getFullyQualifiedName()))
         {
            found = true;
            assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
            Object config = md.getServiceBindingValueSourceConfig();
            assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
            assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
            break;
         }
      }
      assertTrue(found);
      
      
      overrides = new HashSet<ServiceBindingMetadata>();
      overrides.add(new ServiceBindingMetadata("value-source", null, null, 25));
      // KEY POINT IN THE WHOLE TEST: we don't configure the value source stuff
      
      setB = new ServiceBindingSet(B, "localhost", 0, overrides);
      sbs = new HashSet<ServiceBindingSet>();
      sbs.add(setB);
      testee.setBindingSets(sbs);
           
      sbs = testee.getBindingSets();
      assertEquals(1, sbs.size());

      found = false;
      for (ServiceBindingSet sb : sbs)
      {
         for (ServiceBindingMetadata md : sb.getOverrideBindings())
         {
            if ("value-source".equals(md.getFullyQualifiedName()))
            {
               found = true;
               assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
               Object config = md.getServiceBindingValueSourceConfig();
               assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
               assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
               break;
            }
         }
         assertTrue(found);
      }
      
      bindingsMap = testee.getServiceBindings();
      assertNotNull(bindingsMap);
      bindings = bindingsMap.get(B);
      assertNotNull(bindings);
      found = false;
      for (ServiceBinding md : bindings)
      {
         if ("value-source".equals(md.getFullyQualifiedName()))
         {
            found = true;
            assertEquals(XSLTServiceBindingValueSourceImpl.class.getName(), md.getServiceBindingValueSourceClassName());
            Object config = md.getServiceBindingValueSourceConfig();
            assertTrue(config instanceof XSLTServiceBindingValueSourceConfig);
            assertEquals("test", ((XSLTServiceBindingValueSourceConfig) config).getXslt());
            break;
         }
      }
      assertTrue(found);
   }

}
