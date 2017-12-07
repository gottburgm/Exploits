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
package org.jboss.test.system.controller.configure.value.dependslist.test;

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
 * DependencyListValueTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class DependencyListValueTest extends AbstractControllerTest
{
   static ObjectName DEPENDS1 = ObjectNameFactory.create("jboss.test:type=depends1");
   static ObjectName[] DEPENDSLIST1 = new ObjectName[] { DEPENDS1 };
   static ObjectName DEPENDS2 = ObjectNameFactory.create("jboss.test:type=depends2");
   static ObjectName[] DEPENDSLIST2 = new ObjectName[] { DEPENDS1, DEPENDS2 };
   
   public DependencyListValueTest(String name)
   {
      super(name);
   }
   
   protected void assertEquals(ObjectName[] expected, Collection<ObjectName> actual)
   {
      List<ObjectName> expectedList = Arrays.asList(expected);
      assertEquals(expectedList, actual);
   }
   
   public void testNone() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(new ObjectName[0], simple.getObjectNames());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testOne() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST1, simple.getObjectNames());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testTwo() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST2, simple.getObjectNames());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testNested() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDSLIST1, simple.getObjectNames());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testNoValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testEmptyValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testInvalidValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testUnknownElement() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }

   public void testPatternValue() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, MalformedObjectNameException.class);
   }
   
   public void testBrokenSetAttribute() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }
}
