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

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingManager;
import org.jboss.services.binding.ServiceBindingManager.BindingType;
import org.jboss.services.binding.impl.SimpleServiceBindingValueSourceImpl;
import org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceImpl;

/**
 * Tests of {@link ServiceBindingManager#getServiceBindingValueSource(org.jboss.bindings.ServiceBinding, BindingType)}
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class DefaultServiceBindingValueSourceUnitTestCase extends ServiceBindingTestBase
{
   /**
    * Create a new DefaultServiceBindingValueSourceUnitTestCase.
    * 
    * @param arg0
    */
   public DefaultServiceBindingValueSourceUnitTestCase(String arg0)
   {
      super(arg0);
   }
   
   public void testInjectedSource() throws Exception
   {
      MockServiceBindingValueSource source = new MockServiceBindingValueSource("test");
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertEquals(source, ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.GENERIC));
   }
   
   public void testInjectedSourceClassName() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceClassName(MockServiceBindingValueSource.class.getName());
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.GENERIC) instanceof MockServiceBindingValueSource);
   }
   
   public void testInt() throws Exception
   {
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.INT) instanceof SimpleServiceBindingValueSourceImpl);
   }
   
   public void testInetAddress() throws Exception
   {
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.INETADDRESS) instanceof SimpleServiceBindingValueSourceImpl);
   }
   
   public void testString() throws Exception
   {
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.STRING) instanceof StringReplacementServiceBindingValueSourceImpl);
   }
   
   public void testElement() throws Exception
   {
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.ELEMENT) instanceof StringReplacementServiceBindingValueSourceImpl);
   }
   
   public void testResource() throws Exception
   {
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.RESOURCE) instanceof StringReplacementServiceBindingValueSourceImpl);
   }
   
   public void testURL() throws Exception
   {
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.URL) instanceof StringReplacementServiceBindingValueSourceImpl);
   }
   
   public void testElementXSLT() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(new XSLTServiceBindingValueSourceConfig("test"));
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.ELEMENT) instanceof XSLTServiceBindingValueSourceImpl);
   }
   
   public void testResourceXSLT() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(new XSLTServiceBindingValueSourceConfig("test"));
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.RESOURCE) instanceof XSLTServiceBindingValueSourceImpl);
   }
   
   public void testURLXSLT() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(new XSLTServiceBindingValueSourceConfig("test"));
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertTrue(ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.URL) instanceof XSLTServiceBindingValueSourceImpl);
   }
   
   public void testGeneric() throws Exception
   {
      try
      {
         ServiceBindingManager.getServiceBindingValueSource(binding, BindingType.GENERIC);
         fail("unknown generic should fail");
      }
      catch(IllegalStateException good) {}
   }

}
