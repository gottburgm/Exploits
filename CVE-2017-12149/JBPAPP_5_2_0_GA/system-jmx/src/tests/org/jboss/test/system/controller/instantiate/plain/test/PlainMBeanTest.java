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
package org.jboss.test.system.controller.instantiate.plain.test;

import java.util.Collections;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jboss.system.ConfigurationException;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.BrokenDynamicMBean;
import org.jboss.test.system.controller.support.PostDeregisterError;
import org.jboss.test.system.controller.support.PostRegisterError;
import org.jboss.test.system.controller.support.PreDeregisterError;
import org.jboss.test.system.controller.support.PreRegisterError;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * PlainMBeanTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class PlainMBeanTest extends AbstractControllerTest
{
   public PlainMBeanTest(String name)
   {
      super(name);
   }
   
   public void testPlainMBean() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      assertInstall(name);
      assertUninstall(name);
   }
   
   public void testPlainMBeanCodeMissing() throws Exception
   {
      assertMaybeParseFailure(SimpleMBean.OBJECT_NAME, ConfigurationException.class);
   }
   
   public void testPlainMBeanCodeEmpty() throws Exception
   {
      assertMaybeParseFailure(SimpleMBean.OBJECT_NAME, ConfigurationException.class);
   }
   
   public void testPlainMBeanCodeClassNotFound() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }
   
   public void testPlainMBeanNotMBean() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, NotCompliantMBeanException.class);
   }
   
   public void testPlainMBeanAbstractClass() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, NotCompliantMBeanException.class);
   }
   
   public void testPlainMBeanConstructorError() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, Error.class);
   }
   
   public void testPlainMBeanConstructorException() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, RuntimeException.class);
   }
   
   public void testPlainMBeanConstructorTypeNotFound() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ClassNotFoundException.class);
   }
   
   public void testPlainMBeanConstructorInvalidType() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, ConfigurationException.class);
   }
   
   public void testPlainMBeanConstructorInvalidValue() throws Exception
   {
      assertMaybeDeployFailure(SimpleMBean.OBJECT_NAME, NumberFormatException.class);
   }
   
   public void testPlainMBeanGetMBeanInfoError() throws Exception
   {
      assertMaybeDeployFailure(BrokenDynamicMBean.OBJECT_NAME, Error.class);
   }
   
   public void testPlainMBeanPreRegisterError() throws Exception
   {
      assertMaybeDeployFailure(PreRegisterError.OBJECT_NAME, Error.class);
   }
   
   public void testPlainMBeanPostRegisterError() throws Exception
   {
      assertMaybeDeployFailure(PostRegisterError.OBJECT_NAME, Error.class);
   }
   
   public void testPlainMBeanPreDeregisterError() throws Exception
   {
      ObjectName name = PreDeregisterError.OBJECT_NAME;
      assertInstall(name);
      uninstall(Collections.singletonList(name));
      assertNoService(name);
      assertRegistered(name);
   }
   
   public void testPlainMBeanPostDeregisterError() throws Exception
   {
      ObjectName name = PostDeregisterError.OBJECT_NAME;
      assertInstall(name);
      assertUninstall(name);
   }
}
