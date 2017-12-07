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

import junit.framework.TestCase;

import org.jboss.services.binding.ServiceBindingMetadata;

/**
 * @author Brian Stansberry
 *
 */
public class ServiceBindingMetadataTestCase extends TestCase
{
   private static final String S = "S";
   private static final String B = "B";
   private static final String H = "H";
   private static final String FQN = S + ":" + B;
   
   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#ServiceBindingMetadata(java.lang.String)}.
    */
   public void testServiceBindingMetadataString()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata(S);
      assertEquals(S, md.getServiceName());
      
      assertFalse(md.isFixedHostName());
      assertFalse(md.isFixedPort());
    
      try
      {
         String svcName = null;
         md = new ServiceBindingMetadata(svcName);
         fail("null serviceName allowed");
      }
      catch (IllegalArgumentException good) {}
   }
   
   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#ServiceBindingMetadata(java.lang.String, java.lang.String)}.
    */
   public void testServiceBindingMetadataStringString()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata(S, B);
      assertEquals(S, md.getServiceName());
      assertEquals(B, md.getBindingName());
      
      assertFalse(md.isFixedHostName());
      assertFalse(md.isFixedPort());
      
      md = new ServiceBindingMetadata(S, null);
      assertEquals(S, md.getServiceName());
      assertEquals(null, md.getBindingName());
      
      assertFalse(md.isFixedHostName());
      assertFalse(md.isFixedPort());
    
      try
      {
         md = new ServiceBindingMetadata(null, B);
         fail("null serviceName allowed");
      }
      catch (IllegalArgumentException good) {}
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#ServiceBindingMetadata(java.lang.String, java.lang.String, java.lang.String, int)}.
    */
   public void testServiceBindingMetadataStringStringStringInt()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata(S, B, H, 1);
      assertEquals(S, md.getServiceName());
      assertEquals(B, md.getBindingName());
      assertEquals(H, md.getHostName());
      assertEquals(1, md.getPort());
      assertFalse(md.isFixedPort());
      assertTrue(md.isFixedHostName());
      
      md = new ServiceBindingMetadata(S, null, null, 1);
      assertEquals(S, md.getServiceName());
      assertEquals(null, md.getBindingName());
      assertEquals(null, md.getHostName());
      assertEquals(1, md.getPort());
      assertFalse(md.isFixedPort());
      assertFalse(md.isFixedHostName());
    
      try
      {
         md = new ServiceBindingMetadata(null, B, H, 1);
         fail("null serviceName allowed");
      }
      catch (IllegalArgumentException good) {}
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#ServiceBindingMetadata(java.lang.String, java.lang.String, java.lang.String, int, boolean, boolean)}.
    */
   public void testServiceBindingMetadataStringStringStringIntBooleanBoolean()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata(S, B, H, 1, true, true);
      assertEquals(S, md.getServiceName());
      assertEquals(B, md.getBindingName());
      assertEquals(H, md.getHostName());
      assertEquals(1, md.getPort());
      assertTrue(md.isFixedPort());
      assertTrue(md.isFixedHostName());
      
      md = new ServiceBindingMetadata(S, null, null, 1, true, true);
      assertEquals(S, md.getServiceName());
      assertEquals(null, md.getBindingName());
      assertEquals(null, md.getHostName());
      assertEquals(1, md.getPort());
      assertTrue(md.isFixedPort());
      assertTrue(md.isFixedHostName());
    
      try
      {
         md = new ServiceBindingMetadata(null, B, H, 1, true, true);
         fail("null serviceName allowed");
      }
      catch (IllegalArgumentException good) {}
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setServiceName(java.lang.String)}.
    */
   public void testSetServiceName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      md.setServiceName(S);
      assertEquals(S, md.getServiceName());
    
      try
      {
         md.setServiceName(null);
         fail("null serviceName allowed");
      }
      catch (IllegalArgumentException good) {}
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setBindingName(java.lang.String)}.
    */
   public void testSetBindingName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      md.setBindingName(B);
      assertEquals(B, md.getBindingName());
      md.setBindingName(null);
      assertEquals(null, md.getBindingName());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#getFullyQualifiedName()}.
    */
   public void testGetFullyQualifiedName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata(S, B);
      assertEquals(FQN, md.getFullyQualifiedName());
      
      md = new ServiceBindingMetadata(S, null);
      assertEquals(S,md.getFullyQualifiedName());
      
      md = new ServiceBindingMetadata();
      try
      {
         md.getFullyQualifiedName();
         fail("getFullyQualifiedName should fail with no serviceName set");
      }
      catch (IllegalStateException good) {}
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setHostName(java.lang.String)}.
    */
   public void testSetHostName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      md.setHostName(H);
      assertEquals(H, md.getHostName());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setPort(int)}.
    */
   public void testSetPort()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      md.setPort(2);
      assertEquals(2, md.getPort());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setServiceBindingValueSource(org.jboss.services.binding.ServiceBindingValueSource)}.
    */
   public void testSetServiceBindingValueSource()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      MockServiceBindingValueSource mock = new MockServiceBindingValueSource();
      md.setServiceBindingValueSource(mock);
      assertSame(mock, md.getServiceBindingValueSource());
      assertEquals(mock.getClass().getName(), md.getServiceBindingValueSourceClassName());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setServiceBindingValueSourceClassName(java.lang.String)}.
    */
   public void testSetServiceBindingValueSourceClassName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      md.setServiceBindingValueSourceClassName(S);
      assertEquals(S, md.getServiceBindingValueSourceClassName());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setServiceBindingValueSourceConfig(java.lang.Object)}.
    */
   public void testSetServiceBindingValueSourceConfig()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      Object config = new Object();
      md.setServiceBindingValueSourceConfig(config);
      assertSame(config, md.getServiceBindingValueSourceConfig());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setFixedPort(boolean)}.
    */
   public void testSetFixedPort()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      assertFalse(md.isFixedPort());
      md.setFixedPort(true);
      assertTrue(md.isFixedPort());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#setFixedHostName(boolean)}.
    */
   public void testSetFixedHostName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      assertFalse(md.isFixedHostName());
      md.setFixedHostName(true);
      assertTrue(md.isFixedHostName());
   }
   
   /**
    * Tests combination of setting hostName and fixedHostName properties
    */
   public void testFixedHostName()
   {
      ServiceBindingMetadata md = new ServiceBindingMetadata();
      assertFalse(md.isFixedHostName());
      md.setHostName("192.168.0.1");
      assertTrue(md.isFixedHostName());
      md.setFixedHostName(false);
      assertFalse(md.isFixedHostName());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingMetadata#equals(java.lang.Object)}.
    */
   public void testEqualsObject()
   {
      ServiceBindingMetadata md1 = new ServiceBindingMetadata();
      ServiceBindingMetadata md2 = new ServiceBindingMetadata();
      assertFalse(md1.equals(md2));
      
      md1 = new ServiceBindingMetadata(S, B);
      md2 = new ServiceBindingMetadata(S, null);
      assertFalse(md1.equals(md2));
      assertFalse(md2.equals(md1));
      
      md2 = new ServiceBindingMetadata(B, B);
      assertFalse(md1.equals(md2));
      assertFalse(md2.equals(md1));
      
      md2 = new ServiceBindingMetadata(S, B);
      md2.setHostName(H);
      md2.setPort(10);
      md2.setServiceBindingValueSource(new MockServiceBindingValueSource());
      md2.setServiceBindingValueSourceConfig(new Object());
      assertTrue(md1.equals(md2));
      assertTrue(md2.equals(md1));
   }
   
   /**
    * Test method for {@link ServiceBindingMetadata#compareTo(ServiceBindingMetadata)}
    */
   public void testCompareTo()
   {
      ServiceBindingMetadata md1 = new ServiceBindingMetadata();
      ServiceBindingMetadata md2 = new ServiceBindingMetadata();
      try
      {
         md1.compareTo(md2);
         fail("compareTo should fail with unset serviceName");
      }
      catch (IllegalStateException good) {}
      
      md1 = new ServiceBindingMetadata(S, B);
      md2 = new ServiceBindingMetadata(S, null);
      assertTrue(md1.compareTo(md2) > 0);
      assertTrue(md2.compareTo(md1) < 0);
      
      md2 = new ServiceBindingMetadata(B, B);
      assertTrue(md1.compareTo(md2) > 0);
      assertTrue(md2.compareTo(md1) < 0);
      
      md2 = new ServiceBindingMetadata(S, B);
      md2.setHostName(H);
      md2.setPort(10);
      md2.setServiceBindingValueSource(new MockServiceBindingValueSource());
      md2.setServiceBindingValueSourceConfig(new Object());
      assertEquals(0, md1.compareTo(md2));
      assertEquals(0, md2.compareTo(md1));
      
   }

}
