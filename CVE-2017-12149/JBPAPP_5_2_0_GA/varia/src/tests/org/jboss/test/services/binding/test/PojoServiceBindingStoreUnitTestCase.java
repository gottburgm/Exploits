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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.services.binding.DuplicateServiceException;
import org.jboss.services.binding.NoSuchBindingException;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.PojoServiceBindingStore;
import org.jboss.services.binding.impl.ServiceBindingSet;

/**
 * Tests of PojoServiceBindingStore.
 * 
 * @author Brian Stansberry
 * @version $Revision: 88905 $
 */
public class PojoServiceBindingStoreUnitTestCase extends TestCase
{
   private static final String A = "A";
   private static final String B = "B";
   private static final String C = "C";
   private static final String D = "D";
   
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
    * Create a new PojoServiceBindingStoreUnitTestCase.
    * 
    * @param name
    */
   public PojoServiceBindingStoreUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
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
   
   private static ServiceBinding getServiceBinding(ServiceBindingMetadata md, ServiceBindingSet set) throws UnknownHostException
   {
      return new ServiceBinding(md, set.getDefaultHostName(), set.getPortOffset());
   }
   
   
   public void testGetServiceBinding() throws Exception
   {
      PojoServiceBindingStore store = new PojoServiceBindingStore();      
      store.setServiceBindingSets(bindingSets);
      store.setStandardBindings(bindings);
      store.start();
      
      assertEquals(getServiceBinding(AA, SET_A), store.getServiceBinding(A, A, A));      
      assertEquals(getServiceBinding(AA, SET_B), store.getServiceBinding(B, A, A));     
      assertEquals(getServiceBinding(AA, SET_C), store.getServiceBinding(C, A, A));
      
      assertEquals(getServiceBinding(AB, SET_A), store.getServiceBinding(A, A, B));      
      assertEquals(getServiceBinding(AB, SET_B), store.getServiceBinding(B, A, B));     
      assertEquals(getServiceBinding(AB, SET_C), store.getServiceBinding(C, A, B));
      
      assertEquals(getServiceBinding(Anull, SET_A), store.getServiceBinding(A, A, null));      
      assertEquals(getServiceBinding(Anull, SET_B), store.getServiceBinding(B, A, null));     
      assertEquals(getServiceBinding(Anull, SET_C), store.getServiceBinding(C, A, null));
      
      try
      {
         store.getServiceBinding(D, A, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      try
      {
         store.getServiceBinding(A, B, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      try
      {
         store.getServiceBinding(A, B, null);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
   }
   
   public void testAddServiceBinding() throws Exception
   {
      PojoServiceBindingStore store = new PojoServiceBindingStore();      
      store.setServiceBindingSets(bindingSets);
      store.setStandardBindings(bindings);
      store.start();
      
      ServiceBindingMetadata new1 = new ServiceBindingMetadata(B, A, "localhost", 1, false, false);
      store.addServiceBinding(A, new1);
      store.addServiceBinding(B, new1);
      store.addServiceBinding(C, new1);
      
      assertEquals(getServiceBinding(new1, SET_A), store.getServiceBinding(A, B, A));      
      assertEquals(getServiceBinding(new1, SET_B), store.getServiceBinding(B, B, A));      
      assertEquals(getServiceBinding(new1, SET_C), store.getServiceBinding(C, B, A));
      
      ServiceBindingMetadata new2 = new ServiceBindingMetadata(B, A, "localhost", 2, false, false);
      try
      {
         store.addServiceBinding(D, new2);      
         fail("add for unknown binding set succeeded");
      }
      catch (IllegalArgumentException good) {}
      
      try
      {
         store.addServiceBinding(A, new2); 
         fail("duplicate add succeeded");
      }
      catch (DuplicateServiceException good) {}
   }
   
   public void testRemoveServiceBinding() throws Exception
   {
      PojoServiceBindingStore store = new PojoServiceBindingStore();      
      store.setServiceBindingSets(bindingSets);
      store.setStandardBindings(bindings);
      store.start();
      
      store.removeServiceBinding(A, AA);
      
      try
      {
         store.getServiceBinding(A, A, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      store.removeServiceBinding(B, AA);
      
      try
      {
         store.getServiceBinding(B, A, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      store.removeServiceBinding(A, Anull);
      
      try
      {
         store.getServiceBinding(A, A, null);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      store.removeServiceBinding(B, Anull);
      
      try
      {
         store.getServiceBinding(B, A, null);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      ServiceBindingMetadata new1 = new ServiceBindingMetadata(B, A, "localhost", 1, false, false);
      store.removeServiceBinding(A, new1);
      store.removeServiceBinding(A, BA);
   }
   
   
   public void testAddServiceBindingToAll() throws Exception
   {
      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB, Anull));
      
      Set<ServiceBindingSet> ourSets = new HashSet<ServiceBindingSet>();
      ServiceBindingSet newSetA = new ServiceBindingSet(A, null, 10, set);
      ourSets.add(newSetA);  
      ServiceBindingSet newSetB = new ServiceBindingSet(B, "localhost", 20, set);
      ourSets.add(newSetB);  
      ServiceBindingSet newSetC = new ServiceBindingSet(C, "192.168.0.10", 30, set);
      ourSets.add(newSetC);
      
      PojoServiceBindingStore store = new PojoServiceBindingStore();
      store.setServiceBindingSets(ourSets);
      store.start();
      
      ServiceBindingMetadata new1 = new ServiceBindingMetadata(B, A, "192.168.0.22", 1, false, true);
      store.addServiceBinding(new1);
      
      InetAddress address = InetAddress.getByName("192.168.0.22");
      
      ServiceBinding got = store.getServiceBinding(A, B, A);
      assertEquals(getServiceBinding(new1, newSetA), got);    
      assertEquals(11, got.getPort());
      assertEquals("192.168.0.22", got.getHostName());
      assertEquals(address, got.getBindAddress());
      
      got = store.getServiceBinding(B, B, A);
      assertEquals(getServiceBinding(new1, newSetB), got);    
      assertEquals(21, got.getPort());
      assertEquals("192.168.0.22", got.getHostName());
      assertEquals(address, got.getBindAddress());
      
      got = store.getServiceBinding(C, B, A);
      assertEquals(getServiceBinding(new1, newSetC), got);
      assertEquals(31, got.getPort());
      assertEquals("192.168.0.22", got.getHostName());
      assertEquals(address, got.getBindAddress());
      
      ServiceBindingMetadata new2 = new ServiceBindingMetadata(B, A, "localhost", 2, false, false);
      try
      {
         store.addServiceBinding(new2); 
         fail("duplicate add succeeded");
      }
      catch (DuplicateServiceException good) {}
      
      ServiceBindingMetadata new3 = new ServiceBindingMetadata(C, C, null, 3, false, false);
      store.addServiceBinding(new3);
      
      got = store.getServiceBinding(A, C, C);
      assertEquals(getServiceBinding(new3, newSetA), got);    
      assertEquals(13, got.getPort());
      assertNull(got.getHostName());
      assertEquals(InetAddress.getByName(null), got.getBindAddress());
      
      got = store.getServiceBinding(B, C, C);
      assertEquals(getServiceBinding(new3, newSetB), got);    
      assertEquals(23, got.getPort());
      assertEquals("localhost", got.getHostName());
      assertEquals(InetAddress.getByName("localhost"), got.getBindAddress());
      
      got = store.getServiceBinding(C, C, C);
      assertEquals(getServiceBinding(new3, newSetC), got);    
      assertEquals(33, got.getPort());
      assertEquals("192.168.0.10", got.getHostName());
      assertEquals(InetAddress.getByName("192.168.0.10"), got.getBindAddress());
      
   }
   
   public void testRemoveServiceBindingFromAll() throws Exception
   {
      PojoServiceBindingStore store = new PojoServiceBindingStore();      
      store.setServiceBindingSets(bindingSets);
      store.setStandardBindings(bindings);
      store.start();
      
      store.removeServiceBinding(AA);
      
      try
      {
         store.getServiceBinding(A, A, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      store.removeServiceBinding(B, AA);
      
      try
      {
         store.getServiceBinding(B, A, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      try
      {
         store.getServiceBinding(C, A, A);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      String nullA = null;
      store.removeServiceBinding(A, nullA);
      
      try
      {
         store.getServiceBinding(A, A, null);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      try
      {
         store.getServiceBinding(B, A, null);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      try
      {
         store.getServiceBinding(C, A, null);
         fail("invalid");
      }
      catch (NoSuchBindingException e) {}
      
      ServiceBindingMetadata new1 = new ServiceBindingMetadata(B, A, "localhost", 1, false, false);
      store.removeServiceBinding(new1);
      store.removeServiceBinding(B, A);
   }
   
   public void testDefaultDefaults() throws Exception
   {
      PojoServiceBindingStore store = new PojoServiceBindingStore();      
      store.setServiceBindingSets(bindingSets);
      store.setStandardBindings(bindings);
      store.start();
      
      String[] names = {A, B, C};
      
      for (String name :names)
      {
         assertEquals("localhost", store.getDefaultHostName(name));
         assertEquals(0, store.getDefaultPortOffset(name));
      }
   }
   
   public void testDefaults() throws Exception
   {
      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB, Anull));
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      sbs.add(new ServiceBindingSet(A, null, 10, set));
      sbs.add(new ServiceBindingSet(B, "localhost", 20, set));      
      sbs.add(new ServiceBindingSet(C, "192.168.0.10", 30, set));
      
      PojoServiceBindingStore store = new PojoServiceBindingStore();
      store.setServiceBindingSets(sbs);
      store.start();
      
      assertNull(store.getDefaultHostName(A));
      assertEquals(10, store.getDefaultPortOffset(A));
      
      assertEquals("localhost", store.getDefaultHostName(B));
      assertEquals(20, store.getDefaultPortOffset(B));
      
      assertEquals("192.168.0.10", store.getDefaultHostName(C));
      assertEquals(30, store.getDefaultPortOffset(C));
   }
   
   public void testGetServiceBindings() throws Exception
   {
      PojoServiceBindingStore store = new PojoServiceBindingStore();      
      store.setServiceBindingSets(bindingSets);
      store.setStandardBindings(bindings);
      store.start();
      
      String[] servers = {A, B, C};
      for (String server : servers)
      {
         Set<ServiceBinding> set = store.getServiceBindings(server);
         assertEquals(bindings.size(), set.size());
         for (ServiceBinding binding : set)
         {
            ServiceBindingMetadata metadata = new ServiceBindingMetadata(binding);
            assertTrue(server + " includes " + metadata, bindings.contains(metadata));
         }
      }      
   }
   
   public void testSetStandardBindings() throws Exception
   {
      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB, Anull));
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      ServiceBindingSet setB = new ServiceBindingSet(B, "localhost", 20);      
      sbs.add(setB);     
      ServiceBindingSet setC = new ServiceBindingSet(C, "192.168.0.10", 30);
      sbs.add(setC);
      
      PojoServiceBindingStore store = new PojoServiceBindingStore(sbs, set);
      store.start();
      
      Set<ServiceBindingMetadata> updatedSet = 
         new HashSet<ServiceBindingMetadata>(store.getStandardBindings());
      assertEquals(3, updatedSet.size());
      ServiceBindingMetadata updated = new ServiceBindingMetadata(AA);
      updated.setPort(9999);
      updated.setDescription("updated");
      updatedSet.remove(AA);
      updatedSet.add(updated);
      updatedSet.add(BA);
      assertEquals(4, updatedSet.size());
      
      store.setStandardBindings(updatedSet);
      
      Set<ServiceBindingMetadata> result = store.getStandardBindings();
      assertNotNull(result);
      assertTrue("has updated", result.contains(updated));
      assertTrue("has AB", result.contains(AB));
      assertTrue("has Anull", result.contains(Anull));
      assertTrue("has BA", result.contains(BA));
      
      for (ServiceBindingSet bindingSet : sbs)
      {
         String setName = bindingSet.getName();
         Set<ServiceBinding> bindings = store.getServiceBindings(setName);
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
   
   public void testSetServiceBindingSets() throws Exception
   {
      Set<ServiceBindingMetadata> set = new HashSet<ServiceBindingMetadata>();
      set.addAll(Arrays.asList(AA, AB, Anull));
      
      Set<ServiceBindingSet> sbs = new HashSet<ServiceBindingSet>();
      ServiceBindingSet setA = new ServiceBindingSet(A, null, 10);
      sbs.add(setA);
      ServiceBindingSet setB = new ServiceBindingSet(B, "localhost", 20);      
      sbs.add(setB);
      
      PojoServiceBindingStore store = new PojoServiceBindingStore(sbs, set);
      store.start();
      
      Set<ServiceBindingSet> updated = new HashSet<ServiceBindingSet>(store.getServiceBindingSets());
      
      Set<ServiceBindingMetadata> overrides = new HashSet<ServiceBindingMetadata>();
      overrides.add(BA);
      ServiceBindingSet newSet = new ServiceBindingSet(C, "192.168.0.10", 30, overrides);
      updated.add(newSet);
      ServiceBindingSet replaced = new ServiceBindingSet(B, "localhost", 50);
      updated.remove(setB);
      updated.add(replaced);
      assertEquals(3, updated.size());
      
      store.setServiceBindingSets(updated);
      
      Set<ServiceBindingSet> result = store.getServiceBindingSets();
      assertNotNull(result);
      assertTrue("has setA", result.contains(setA));
      assertTrue("has setB", result.contains(replaced));
      assertTrue("has newSet", result.contains(newSet));
      
      Set<ServiceBinding> bindings = store.getServiceBindings(C);
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
      
      bindings = store.getServiceBindings(B);
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

}
