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
package org.jboss.test.system.controller.configure.value.inject.test;

import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceContext;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * NewInjectionValueUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class NewInjectionValueUnitTestCase extends AbstractControllerTest
{
   private static ObjectName OTHER = ObjectNameFactory.create("jboss.test:type=depends");
   
   public static Test suite()
   {
      return suite(NewInjectionValueUnitTestCase.class);
   }

   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      return getNewControllerDelegate(clazz);
   }

   public NewInjectionValueUnitTestCase(String name)
   {
      super(name);
   }

   public void testInjection() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      
      List<ObjectName> names = assertDeploy(name);
      try
      {
         Simple test = getSimple();
         Simple other = getMBean(Simple.class, OTHER, "Instance");
         
         assertEquals(other, test.getSimple());
      }
      finally
      {
         assertUndeploy(name, names);
      }
   }

   public void testInjectionNoBean() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ServiceContext.INSTALLED, null);
   }

   public void testInjectionBeanEmpty() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ServiceContext.INSTALLED, null);
   }

   public void testInjectionNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ServiceContext.INSTALLED, null);
   }

   public void testInjectionWrongType() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, InvalidAttributeValueException.class);
   }

   public void testInjectionProperty() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      
      List<ObjectName> names = assertDeploy(name);
      try
      {
         Simple test = getSimple();
         
         assertEquals("PropertyInjection", test.getAString());
      }
      finally
      {
         assertUndeploy(name, names);
      }
   }

   public void testInjectionPropertyState() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      
      List<ObjectName> names = assertDeploy(name);
      try
      {
         Simple test = getSimple();
         
         assertEquals("Instantiated", test.getAString());
      }
      finally
      {
         assertUndeploy(name, names);
      }
   }

   public void testInjectionPropertyNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, AttributeNotFoundException.class);
   }

   public void testInjectionPropertyWrongType() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, InvalidAttributeValueException.class);
   }
}
