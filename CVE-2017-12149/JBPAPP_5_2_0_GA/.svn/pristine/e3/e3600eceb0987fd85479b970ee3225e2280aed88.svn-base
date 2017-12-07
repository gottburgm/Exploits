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
package org.jboss.test.jmx.compliance.registration;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.test.jmx.compliance.registration.support.RegistrationAware;

import junit.framework.TestCase;

public class RegistrationTEST extends TestCase
{
   public RegistrationTEST(String s)
   {
      super(s);
   }

   public void testSimpleRegistration()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         RegistrationAware ra = new RegistrationAware();
         ObjectName name = new ObjectName("test:key=value");

         server.registerMBean(ra, name);

         assertTrue("preRegister", ra.isPreRegisterCalled());
         assertTrue("postRegister", ra.isPostRegisterCalled());
         assertTrue("postRegisterRegistrationDone", ra.isPostRegisterRegistrationDone());
         assertEquals(name, ra.getRegisteredObjectName());

         server.unregisterMBean(name);

         assertTrue("preDeRegister", ra.isPreDeRegisterCalled());
         assertTrue("postDeRegister", ra.isPostDeRegisterCalled());
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException");
      }
      catch (MBeanRegistrationException e)
      {
         fail("strange MBeanRegistrationException linked to: " + e.getTargetException().getMessage());
      }
      catch (Exception e)
      {
         fail("something else went wrong: " + e.getMessage());
      }
   }

   public void testDuplicateRegistration()
   {

      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:key=value");

         RegistrationAware original = new RegistrationAware();
         RegistrationAware ra = new RegistrationAware();

         server.registerMBean(original, name);

         try
         {
            server.registerMBean(ra, name);
            fail("expected a InstanceAlreadyExistsException");
         }
         catch (InstanceAlreadyExistsException e)
         {
         }

         assertTrue("preRegister", ra.isPreRegisterCalled());
         assertTrue("postRegister", ra.isPostRegisterCalled());
         assertTrue("postRegisterRegistrationDone", !ra.isPostRegisterRegistrationDone());
         assertTrue("preDeRegister", !ra.isPreDeRegisterCalled());
         assertTrue("postDeRegister", !ra.isPostDeRegisterCalled());
         assertEquals(name, ra.getRegisteredObjectName());

         server.unregisterMBean(name);
      }
      catch (Exception e)
      {
         fail("got an unexpected exception: " + e.getMessage());
      }
   }

}
