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
package org.jboss.test.system.controller.configure.value.javabean.test;

import java.beans.IntrospectionException;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.BrokenDynamicMBeanAttributeInfoTypeNotFound;
import org.jboss.test.system.controller.support.BrokenDynamicMBeanNoAttributeInfoType;
import org.jboss.test.system.controller.support.JavaBean;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * JavaBeanValueTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class JavaBeanValueTest extends AbstractControllerTest
{
   public JavaBeanValueTest(String name)
   {
      super(name);
   }

   public void testExplicitClass() throws Exception
   {
      javaBean();
   }

   public void testImplicitClass() throws Exception
   {
      javaBean();
   }

   public void testAttributeClassNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }

   public void testAbstractAttributeClass() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, InstantiationException.class);
   }

   public void testErrorInConstructor() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }

   public void testNoAttributeInfoType() throws Exception
   {
      assertDeployFailure(BrokenDynamicMBeanNoAttributeInfoType.OBJECT_NAME, DeploymentException.class);
   }

   public void testAttributeInfoTypeNotFound() throws Exception
   {
      assertDeployFailure(BrokenDynamicMBeanAttributeInfoTypeNotFound.OBJECT_NAME, ClassNotFoundException.class);
   }

   public void testNoPropertyName() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IntrospectionException.class);
   }

   public void testEmptyPropertyName() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IntrospectionException.class);
   }

   public void testPropertyNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IntrospectionException.class);
   }

   public void testNoPropertyEditor() throws Exception
   {
      /* TODO testNoPropertyEditor 
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IntrospectionException.class);
      */
   }

   public void testNoPropertyValue() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      
      assertInstall(name);
      try
      {
         Simple test = getSimple();

         JavaBean bean = test.getJavaBean();
         assertNotNull(bean);
         assertEquals("", bean.getProperty1());
      }
      finally
      {
         assertUninstall(name);
      }
   }

   public void testErrorInProperty() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }
   
   protected void javaBean() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      
      assertInstall(name);
      try
      {
         Simple test = getSimple();

         JavaBean bean = test.getJavaBean();
         assertNotNull(bean);
         assertEquals("property1", bean.getProperty1());
         assertEquals(new Integer(10), bean.getProperty2());
      }
      finally
      {
         assertUninstall(name);
      }
   }
}
