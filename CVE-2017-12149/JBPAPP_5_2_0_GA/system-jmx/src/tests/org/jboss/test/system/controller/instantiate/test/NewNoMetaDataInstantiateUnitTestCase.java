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
package org.jboss.test.system.controller.instantiate.test;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.system.ServiceController;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * NewNoMetaDataInstantiateUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class NewNoMetaDataInstantiateUnitTestCase extends AbstractControllerTest
{
   public static Test suite()
   {
      return suite(NewNoMetaDataInstantiateUnitTestCase.class);
   }

   public NewNoMetaDataInstantiateUnitTestCase(String name)
   {
      super(name);
   }

   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      return getNewControllerDelegate(clazz);
   }
   
   public void testNoMetaData() throws Throwable
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      Simple test = new Simple();

      ServiceController serviceController = (ServiceController) getController();
      serviceController.install(name, test);
      try
      {
         assertServiceConfigured(name);
         
         MBeanServer server = getServer();
         Object instance = server.getAttribute(name, "Instance");
         assertNotNull(instance);
         assertTrue(test == instance);
      }
      finally
      {
         try
         {
            serviceController.remove(name);
         }
         catch (Exception ignored)
         {
         }
      }
   }
   
   public void testNoMetaDataNoName() throws Throwable
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;

      Simple test = new Simple();
      
      ServiceController serviceController = (ServiceController) getController();
      try
      {
         serviceController.install(null, test);
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      
      assertNoService(name);
   }
   
   public void testNoMetaDataNoObject() throws Throwable
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;

      ServiceController serviceController = (ServiceController) getController();
      try
      {
         serviceController.install(name, null);
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
   }
}
