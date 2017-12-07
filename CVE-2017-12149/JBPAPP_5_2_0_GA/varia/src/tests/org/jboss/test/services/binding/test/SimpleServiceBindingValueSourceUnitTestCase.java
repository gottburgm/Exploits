/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.services.binding.impl.SimpleServiceBindingValueSourceImpl;

/**
 * Unit tests of {@link SimpleServiceBindingValueSourceImpl}.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class SimpleServiceBindingValueSourceUnitTestCase extends ServiceBindingTestBase
{ 
   private SimpleServiceBindingValueSourceImpl testee;
   private InetAddress address;

   /**
    * Create a new SimpleServiceBindingValueSourceUnitTestCase.
    * 
    * @param name   name of the test
    */
   public SimpleServiceBindingValueSourceUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      testee = new SimpleServiceBindingValueSourceImpl();
      address = InetAddress.getByName(HOST);
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.SimpleServiceBindingValueSourceImpl#getIntServiceBindingValue(org.jboss.bindings.ServiceBinding)}.
    */
   public void testGetIntServiceBindingValue()
   {
      assertEquals(PORT, testee.getIntServiceBindingValue(binding));
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.SimpleServiceBindingValueSourceImpl#getInetAddressServiceBindingValue(org.jboss.bindings.ServiceBinding)}.
    */
   public void testGetInetAddressServiceBindingValue()
   {
      assertEquals(address, testee.getInetAddressServiceBindingValue(binding));
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.SimpleServiceBindingValueSourceImpl#getServiceBindingValue(org.jboss.bindings.ServiceBinding, java.lang.Object[])}.
    * @throws Exception 
    */
   public void testGetServiceBindingValue() throws Exception
   {
      Object[] params = null;
      assertEquals("Correct generic value", new Integer(PORT), testee.getServiceBindingValue(binding, params));
   }
   
   public void testGetServiceBindingValueBadParam() throws Exception
   {
      try
      {
         testee.getServiceBindingValue(binding, new Object());
         fail("Should fail passing a param");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testUnknownConfigObject() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(new Object());
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertEquals(PORT, testee.getIntServiceBindingValue(binding));
   }

}
