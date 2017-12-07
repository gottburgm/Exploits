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
package org.jboss.test.system.controller;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.AssertionFailedError;

import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.AbstractSystemTest;
import org.jboss.test.system.controller.legacy.OldServiceController;

/**
 * ControllerTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class OldControllerTestDelegate extends ControllerTestDelegate
{
   public OldControllerTestDelegate(Class clazz)
   {
      super(clazz);
   }
   
   public ServiceControllerMBean createServiceController()
   {
      return new OldServiceController();
   }
   
   public void assertMBeanFailed(ObjectName name, boolean registered) throws Exception
   {
      MBeanServer server = getServer();
      if (registered == false && server.isRegistered(name))
         throw new AssertionFailedError(name + " should not be registered after a failure");
      if (registered && server.isRegistered(name) == false)
         throw new AssertionFailedError(name + " should be registered after a failure");
   }

   public List<ObjectName> assertMaybeDeployFailure(URL url, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      if (expected != null)
      {
         try
         {
            deploy(url, true);
            throw new AssertionFailedError("Should have got a " + expected.getName());
         }
         catch (Throwable throwable)
         {
            AbstractSystemTest.checkThrowableDeep(expected, throwable);
            return Collections.emptyList();
         }
      }
      else
      {
         return super.assertMaybeDeployFailure(url, name, expected);
      }
   }
   
   public void assertMaybeParseFailure(URL url, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      assertInitialDeployFailure(url, name, expected);
   }
}
