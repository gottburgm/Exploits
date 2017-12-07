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

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.ServiceBindingSet;

/**
 * A ServiceBindingSetUnitTestCase.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class ServiceBindingSetUnitTestCase extends TestCase
{
   private static final String A = "A";
   private static final String B = "B";
   
   private static ServiceBindingMetadata AA;
   private static ServiceBindingMetadata AB;
   private static ServiceBindingMetadata Anull;

   private Set<ServiceBindingMetadata> bindings = new HashSet<ServiceBindingMetadata>();
   
   /**
    * Create a new ServiceBindingSetUnitTestCase.
    * 
    * @param name
    */
   public ServiceBindingSetUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      AA = new ServiceBindingMetadata(A, A, "localhost", 1, false, false);
      AB = new ServiceBindingMetadata(A, B, "localhost", 1, false, false);
      Anull = new ServiceBindingMetadata(A, null, "localhost", 1, false, false);
      
      bindings.addAll(Arrays.asList(AA, AB, Anull));
   }
   
   public void testBasicConstructor() throws UnknownHostException
   {
      ServiceBindingSet set = new ServiceBindingSet(A, new HashSet<ServiceBindingMetadata>(bindings));
      
      assertEquals(A, set.getName());
      
      for (ServiceBindingMetadata binding : set.getOverrideBindings())
      {
         assertEquals(1, binding.getPort());
         assertTrue(bindings.remove(binding));
      }
      
      assertEquals(0, bindings.size());
      
      assertNull(set.getDefaultHostName());
      assertEquals(0, set.getPortOffset());
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.ServiceBindingSet#PortOffsetServiceBindingSet(java.util.Set, int)}.
    * @throws UnknownHostException 
    */
   public void testOffsetConstructor() throws UnknownHostException
   {
      ServiceBindingSet set = new ServiceBindingSet(A, 5);
      
      assertEquals(A, set.getName());
      
      assertEquals(0, set.getOverrideBindings().size());
      
      assertNull(set.getDefaultHostName());
      assertEquals(5, set.getPortOffset());
   }
   
   public void testOffsetConstructorWithDefaultHost() throws UnknownHostException
   {
      ServiceBindingSet set = new ServiceBindingSet(A, "192.168.0.10", 5, new HashSet<ServiceBindingMetadata>(bindings));
      
      assertEquals(A, set.getName());
      
      for (ServiceBindingMetadata binding : set.getOverrideBindings())
      {
         assertEquals(1, binding.getPort());
         assertTrue(bindings.remove(binding));
      }
      
      assertEquals(0, bindings.size());
      
      assertEquals("192.168.0.10", set.getDefaultHostName());
      assertEquals(5, set.getPortOffset());
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.ServiceBindingSet#PortOffsetServiceBindingSet(java.util.Set, int, java.util.Set)}.
    * @throws UnknownHostException 
    */
   public void testOverrideConstructor() throws UnknownHostException
   {
      ServiceBindingSet set = new ServiceBindingSet(A, new HashSet<ServiceBindingMetadata>(bindings));
      for (ServiceBindingMetadata binding : set.getOverrideBindings())
      {         
         assertTrue(bindings.remove(binding));
         
      }
      
      assertEquals(0, bindings.size());
      
      assertEquals(null, set.getDefaultHostName());
      assertEquals(0, set.getPortOffset());
   }

}
