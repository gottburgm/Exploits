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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * DependsAttributeTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class DependsListAttributeTest extends AbstractControllerTest
{
   static ObjectName DEPENDS1 = ObjectNameFactory.create("jboss.test:type=depends1");
   static ObjectName[] DEPENDSLIST1 = new ObjectName[] { DEPENDS1 };
   static ObjectName DEPENDS2 = ObjectNameFactory.create("jboss.test:type=depends2");
   static ObjectName[] DEPENDSLIST2 = new ObjectName[] { DEPENDS2 };
   static ObjectName[] DEPENDSLIST = new ObjectName[] { DEPENDS1, DEPENDS2 };
   
   public DependsListAttributeTest(String name)
   {
      super(name);
   }
   
   protected void assertEquals(ObjectName[] expected, Collection<ObjectName> actual)
   {
      List<ObjectName> expectedList = Arrays.asList(expected);
      assertEquals(expectedList, actual);
   }
   
   public void testDependsListAttributeNone() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertNull(simple.getObjectNamesAttribute1());
         assertNull(simple.getObjectNamesAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testDependsListAttributeOne() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST1, simple.getObjectNamesAttribute1());
         assertNull(simple.getObjectNamesAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testDependsListAttributeTwo() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST1, simple.getObjectNamesAttribute1());
         assertEquals(DEPENDSLIST2, simple.getObjectNamesAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testDependsListAttributeMultiple() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST, simple.getObjectNamesAttribute1());
         assertNull(simple.getObjectNamesAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testDependsListAttributeNested() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST1, simple.getObjectNamesAttribute1());
         assertNull(simple.getObjectNamesAttribute2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testDependsListAttributeNoValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testDependsListAttributeEmptyValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testDependsListAttributeInvalidValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testDependsListAttributeUnknownElement() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }

   public void testDependsListAttributePatternValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testDependsListAttributeNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, RuntimeException.class);
   }
   
   public void testDependsListAttributeBrokenSetAttribute() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }
}
