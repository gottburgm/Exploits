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
package org.jboss.test.jca.test;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.adminobject.TestImplementation;
import org.jboss.test.jca.adminobject.TestImplementation2;
import org.jboss.test.jca.adminobject.TestInterface;
import org.jboss.test.jca.adminobject.TestInterface2;

/**
 * Inflow Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class AdminObjectUnitTestCase extends JBossTestCase
{

   public AdminObjectUnitTestCase (String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(AdminObjectUnitTestCase.class, "testadminobject.rar");
   }

   public void testAdminObject() throws Throwable
   {
      //assertNotBound();
      deploy("testadminobject-service.xml");
      try
      {
         Object obj = getInitialContext().lookup("test/jca/TestInterface");
         assertTrue("Instanceof TestInterface", obj instanceof TestInterface);
         assertTrue("Instanceof TestImplementation", obj instanceof TestImplementation);
         TestImplementation impl = (TestImplementation) obj;
         assertEquals("StringValue", impl.getStringProperty());
         assertEquals(new Integer(123), impl.getIntegerProperty());

         obj = getInitialContext().lookup("test/jca/TestInterface2");
         assertTrue("Instanceof TestInterface2", obj instanceof TestInterface2);
         assertTrue("Instanceof TestImplementation2", obj instanceof TestImplementation2);
         TestImplementation2 impl2 = (TestImplementation2) obj;
         assertEquals(new Integer(456), impl2.getStringProperty());
      }
      finally
      {
         undeploy("testadminobject-service.xml");
      }
      //assertNotBound();
   }

   protected void assertNotBound() throws Exception
   {
      try
      {
         getInitialContext().lookup("test/jca/TestInterface");
         fail("test/jca/TestInterface is bound");
      }
      catch (Exception expected)
      {
      }

      try
      {
         getInitialContext().lookup("test/jca/TestInterface2");
         fail("test/jca/TestInterface2 is bound");
      }
      catch (Exception expected)
      {
      }
   }
}
