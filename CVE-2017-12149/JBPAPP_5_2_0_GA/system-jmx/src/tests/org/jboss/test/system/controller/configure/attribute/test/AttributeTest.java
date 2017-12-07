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
package org.jboss.test.system.controller.configure.attribute.test;

import javax.management.ObjectName;

import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.BrokenDynamicMBeanAttributes;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * AttributeTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AttributeTest extends AbstractControllerTest
{
   public AttributeTest(String name)
   {
      super(name);
   }
   
   public void testAttributeNone() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertNull(simple.getAttribute1());
         assertNull(simple.getAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testAttributeOne() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals("value1", simple.getAttribute1());
         assertNull(simple.getAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testAttributeTwo() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals("value1", simple.getAttribute1());
         assertEquals("value2", simple.getAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testAttributeNoValue() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertNull(simple.getAttribute1());
         assertNull(simple.getAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testAttributeEmptyValue() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertNull(simple.getAttribute1());
         assertNull(simple.getAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testAttributeNoName() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, RuntimeException.class);
   }
   
   public void testAttributeEmptyName() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, RuntimeException.class);
   }
   
   public void testAttributeBrokenGetAttributes() throws Exception
   {
      assertDeployFailure(BrokenDynamicMBeanAttributes.OBJECT_NAME, Error.class);
   }
   
   public void testAttributeNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, RuntimeException.class);
   }
   
   public void testAttributeBrokenSetAttribute() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }
}
