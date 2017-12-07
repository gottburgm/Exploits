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

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceImpl;

import junit.framework.TestCase;

/**
 * Tests of ServiceBinding.
 * 
 * @author Brian Stansberry
 * @version $Revision: 106766 $
 */
public class ServiceBindingUnitTestCase extends TestCase
{

   /**
    * Create a new ServiceBindingUnitTestCase.
    * 
    * @param name
    */
   public ServiceBindingUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testNullMetadata() throws Exception
   {
      try
      {
         new ServiceBinding(null, "host", 0);
         fail("null metadata should fail");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testNullServiceName() throws Exception
   {
      try
      {
         new ServiceBinding(new ServiceBindingMetadata(), "host", 0);
         fail("null serviceName should fail");
      }
      catch (IllegalStateException good) {}
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBinding#getServiceBindingValueSource()}.
    */
   public void testServiceBindingValueSource() throws Exception
   {
      ServiceBindingMetadata metadata = new ServiceBindingMetadata("svc");
      String className = XSLTServiceBindingValueSourceImpl.class.getName();
      metadata.setServiceBindingValueSourceClassName(className);
      ServiceBinding binding = new ServiceBinding(metadata, "localhost", 1);
      assertEquals(className, binding.getServiceBindingValueSourceClassName());
      assertTrue(binding.getServiceBindingValueSource() instanceof XSLTServiceBindingValueSourceImpl);
      
      MockServiceBindingValueSource mock = new MockServiceBindingValueSource();
      metadata.setServiceBindingValueSource(mock);
      Object config = new Object();
      metadata.setServiceBindingValueSourceConfig(config);
      binding = new ServiceBinding(metadata, "localhost", 1);
      assertSame(mock, binding.getServiceBindingValueSource());
      assertEquals(mock.getClass().getName(), binding.getServiceBindingValueSourceClassName());
      assertSame(config, binding.getServiceBindingValueSourceConfig());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBinding#getOffsetBinding(int)}.
    */
   public void testGetOffsetBinding() throws Exception
   {
      ServiceBindingMetadata metadata = new ServiceBindingMetadata("svc", "binding", "192.168.0.2", 1, false, true);
      String className = XSLTServiceBindingValueSourceImpl.class.getName();
      metadata.setServiceBindingValueSourceClassName(className);
      
      ServiceBinding binding = new ServiceBinding(metadata, "192.168.0.2", 10);
      assertEquals(metadata.getServiceName(), binding.getServiceName());
      assertEquals(metadata.getBindingName(), binding.getBindingName());
      assertEquals(metadata.getHostName(), binding.getHostName());
      assertEquals(InetAddress.getByName(metadata.getHostName()), binding.getBindAddress());
      
      assertEquals(1, metadata.getPort());
      assertEquals(11, binding.getPort());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBinding#getOffsetBinding(int, String)}.
    */
   public void testGetOffsetBindingWithHost() throws Exception
   {
      ServiceBindingMetadata metadata = new ServiceBindingMetadata("svc", "binding", "192.168.0.2", 1, false, true);
      ServiceBinding binding = new ServiceBinding(metadata, "192.168.0.3", 10);
      assertEquals(metadata.getServiceName(), binding.getServiceName());
      assertEquals(metadata.getBindingName(), binding.getBindingName());
      assertEquals(metadata.getHostName(), binding.getHostName());
      assertEquals(InetAddress.getByName(metadata.getHostName()), binding.getBindAddress());
      
      assertEquals(1, metadata.getPort());
      assertEquals(11, binding.getPort());
      
      metadata = new ServiceBindingMetadata("svc", "binding", "192.168.0.2", 1, false, false);
      binding =  new ServiceBinding(metadata, "192.168.0.3", 10);
      assertEquals(metadata.getServiceName(), binding.getServiceName());
      assertEquals(metadata.getBindingName(), binding.getBindingName());
      assertEquals("192.168.0.3", binding.getHostName());
      assertEquals(InetAddress.getByName("192.168.0.3"), binding.getBindAddress());
      
      assertEquals(1, metadata.getPort());
      assertEquals(11, binding.getPort());
   }
   
   /**
    * Tests that the rules for using the default host name vs the metadata's host
    * name are respected
    */
   public void testHostName()  throws Exception
   {
      ServiceBindingMetadata metadata = new ServiceBindingMetadata("A", "A");
      ServiceBinding binding = new ServiceBinding(metadata, "192.168.0.2", 1);
      assertEquals("192.168.0.2", binding.getHostName());
      metadata = new ServiceBindingMetadata();
      metadata.setServiceName("A");
      metadata.setHostName("127.0.0.1");
      binding = new ServiceBinding(metadata, "192.168.0.2", 1);
      assertEquals("127.0.0.1", binding.getHostName());
      metadata.setFixedHostName(false);
      binding = new ServiceBinding(metadata, "192.168.0.2", 1);
      assertEquals("192.168.0.2", binding.getHostName());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBinding#equals(java.lang.Object)}.
    */
   public void testEquals() throws Exception
   {
      ServiceBinding bindingAA0 = new ServiceBinding(new ServiceBindingMetadata("A", "A"), "192.168.0.2", 1);
      ServiceBinding bindingAA1 = new ServiceBinding(new ServiceBindingMetadata("A", "A"), "192.168.0.2", 2);
      ServiceBinding bindingAA2 = new ServiceBinding(new ServiceBindingMetadata("A", "A"), "192.168.0.1", 1);
      ServiceBinding bindingAB = new ServiceBinding(new ServiceBindingMetadata("A", "B"), "192.168.0.2", 1);
      ServiceBinding bindingBA = new ServiceBinding(new ServiceBindingMetadata("B", "A"), "192.168.0.2", 1);
      ServiceBinding bindingAnull = new ServiceBinding(new ServiceBindingMetadata("A", null), "192.168.0.2", 1);
      ServiceBinding bindingBnull = new ServiceBinding(new ServiceBindingMetadata("B", null), "192.168.0.2", 1);
      ServiceBinding bindingBnull1 = new ServiceBinding(new ServiceBindingMetadata("B", null), "192.168.0.2", 2);
      
      assertEquals(bindingAA0, bindingAA1);
      assertEquals(bindingAA0, bindingAA2);
      assertFalse(bindingAA0.equals(bindingAB));
      assertFalse(bindingAA0.equals(bindingBA));
      assertFalse(bindingAB.equals(bindingBA));
      assertFalse(bindingAA0.equals(bindingAnull));
      assertFalse(bindingBA.equals(bindingBnull));
      assertFalse(bindingAnull.equals(bindingBnull));
      assertEquals(bindingBnull, bindingBnull1);
      
      assertFalse(bindingAA0.equals(new Object()));
      
      assertFalse(bindingAA0.equals(null));
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBinding#hashCode()}.
    */
   public void testHashCode() throws Exception
   {
      ServiceBinding bindingAA0 = new ServiceBinding(new ServiceBindingMetadata("A", "A"), "192.168.0.2", 1);
      ServiceBinding bindingAA1 = new ServiceBinding(new ServiceBindingMetadata("A", "A"), "192.168.0.2", 2);
      ServiceBinding bindingAA2 = new ServiceBinding(new ServiceBindingMetadata("A", "A"), "192.168.0.1", 1);
      ServiceBinding bindingAB = new ServiceBinding(new ServiceBindingMetadata("A", "B"), "192.168.0.2", 1);
      ServiceBinding bindingBA = new ServiceBinding(new ServiceBindingMetadata("B", "A"), "192.168.0.2", 1);
      ServiceBinding bindingAnull = new ServiceBinding(new ServiceBindingMetadata("A", null), "192.168.0.2", 1);
      ServiceBinding bindingBnull = new ServiceBinding(new ServiceBindingMetadata("B", null), "192.168.0.2", 1);
      ServiceBinding bindingBnull1 = new ServiceBinding(new ServiceBindingMetadata("B", null), "192.168.0.2", 2);
      
      assertEquals(bindingAA0.hashCode(), bindingAA1.hashCode());
      assertEquals(bindingAA0.hashCode(), bindingAA2.hashCode());
      assertFalse(bindingAA0.hashCode() == bindingAB.hashCode());
      assertFalse(bindingAA0.hashCode() == bindingBA.hashCode());
      
      // A quirk of the values means these have same hashcode 
      assertEquals(bindingAB.hashCode(), bindingBA.hashCode());
      
      assertFalse(bindingAA0.hashCode() == bindingAnull.hashCode());
      assertFalse(bindingBA.hashCode() == bindingBnull.hashCode());
      assertFalse(bindingAnull.hashCode() == bindingBnull.hashCode());
      assertEquals(bindingBnull.hashCode(), bindingBnull1.hashCode());
   }

}
