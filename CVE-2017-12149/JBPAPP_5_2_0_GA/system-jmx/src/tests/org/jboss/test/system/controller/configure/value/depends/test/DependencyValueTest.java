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
package org.jboss.test.system.controller.configure.value.depends.test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * DependencyValueTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class DependencyValueTest extends AbstractControllerTest
{
   static ObjectName DEPENDS = ObjectNameFactory.create("jboss.test:type=depends");
   
   public DependencyValueTest(String name)
   {
      super(name);
   }
   
   public void testDependency() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDS, simple.getObjectName());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testDependencyNested() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         assertEquals(DEPENDS, simple.getObjectName());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   public void testExplicitProxyType() throws Exception
   {
      proxyTest();
   }
   
   public void testImplicitProxyType() throws Exception
   {
      proxyTest();
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
   
   public void testNoAttributeInfoType() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, DeploymentException.class);
   }
   
   public void testAttributeInfoTypeNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }
   
   public void testProxyTypeNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }
   
   public void testProxyTypeNotInterface() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IllegalArgumentException.class);
   }
   
   protected void proxyTest() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      try
      {
         Simple simple = getSimple();
         Simple dependency = getMBean(Simple.class, DEPENDS, "Instance");

         assertFalse(simple.isTouched());
         assertFalse(dependency.isTouched());
         
         simple.touchProxy();

         assertFalse(simple.isTouched());
         assertTrue(dependency.isTouched());
      }
      finally
      {
         assertUninstall(name);
      }
   }
   
   protected Simple getDependency() throws Exception
   {
      return getMBean(Simple.class, DEPENDS, "Instance");
   }
}
