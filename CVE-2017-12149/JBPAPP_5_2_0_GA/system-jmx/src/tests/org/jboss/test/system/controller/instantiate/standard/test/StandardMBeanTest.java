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
package org.jboss.test.system.controller.instantiate.standard.test;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jboss.system.ConfigurationException;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * StandardMBeanTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class StandardMBeanTest extends AbstractControllerTest
{
   public StandardMBeanTest(String name)
   {
      super(name);
   }
   
   public void testStandardMBean() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      assertUninstall(name);
   }
   
   public void testStandardMBeanInterface() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      String result = (String) getServer().invoke(name, "echoReverse", new Object[] { "12345" }, new String[] { String.class.getName() });
      assertEquals("54321", result);
      assertUninstall(name);
   }

   public void testStandardMBeanInterfaceNotFound() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }

   public void testStandardMBeanInterfaceNotImplemented() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, NotCompliantMBeanException.class);
   }
   
   public void testStandardMBeanCodeMissing() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ConfigurationException.class);
   }
   
   public void testStandardMBeanCodeEmpty() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ConfigurationException.class);
   }

   public void testStandardMBeanCodeClassNotFound() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }
   
   public void testStandardMBeanAbstractClass() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, InstantiationException.class);
   }

   public void testStandardMBeanConstructorError() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }
   
   public void testStandardMBeanConstructorException() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, RuntimeException.class);
   }
   
   public void testStandardMBeanConstructorTypeNotFound() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }
   
   public void testStandardMBeanConstructorInvalidType() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ConfigurationException.class);
   }

   public void testStandardMBeanConstructorInvalidValue() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, NumberFormatException.class);
   }
}
