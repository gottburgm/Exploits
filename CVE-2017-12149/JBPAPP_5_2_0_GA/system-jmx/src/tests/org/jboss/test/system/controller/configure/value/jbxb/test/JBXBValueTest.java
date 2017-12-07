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
package org.jboss.test.system.controller.configure.value.jbxb.test;

import javax.management.ObjectName;

import org.jboss.joinpoint.spi.JoinpointException;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.JavaBean;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * JBXBValueTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class JBXBValueTest extends AbstractControllerTest
{
   public JBXBValueTest(String name)
   {
      super(name);
   }

   public void testBasic() throws Exception
   {
      javaBean();
   }

   public void testClassNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }

   public void testAbstractClass() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, JoinpointException.class);
   }

   public void testErrorInConstructor() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }

   public void testNoPropertyName() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IllegalArgumentException.class);
   }

   public void testEmptyPropertyName() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IllegalArgumentException.class);
   }

   public void testPropertyNotFound() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IllegalArgumentException.class);
   }

   public void testNoPropertyEditor() throws Exception
   {
      assertDeployFailure(SimpleMBean.OBJECT_NAME, IllegalArgumentException.class);
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
         assertNull(bean.getProperty1());
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
