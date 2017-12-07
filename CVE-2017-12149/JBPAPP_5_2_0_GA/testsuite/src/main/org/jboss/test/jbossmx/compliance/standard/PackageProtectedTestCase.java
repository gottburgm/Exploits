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
package org.jboss.test.jbossmx.compliance.standard;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.test.jbossmx.compliance.TestCase;
import org.jboss.test.jbossmx.compliance.standard.support.PackageProtectedMBean;
import org.jboss.test.jbossmx.compliance.standard.support.PackageProtectedMBeanFactory;

/**
 * Test a standard mbean with a package protected class implementation.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class PackageProtectedTestCase extends TestCase
{
   public PackageProtectedTestCase(String s)
   {
      super(s);
   }

   /**
    * JBAS-1704
    */
   public void testRegisterPackageProtectedClassImpl()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName objectName = new ObjectName("test:key=value");
         PackageProtectedMBean mbean = PackageProtectedMBeanFactory.createMBean("TestMBean");
         
         server.registerMBean(mbean, objectName);
         
         String name = (String)server.getAttribute(objectName, "Name");
         assertEquals("TestMBean", name);
         
         server.setAttribute(objectName, new Attribute("Name", "TestMBeanX"));
         name = (String)server.getAttribute(objectName, "Name");
         assertEquals("TestMBeanX", name);
         
         String message = "hello";
         String reply = (String)server.invoke(
               objectName,
               "echo",
               new Object[] { message },
               new String[] { "java.lang.String" });
         
         assertEquals(message, reply);
         
         server.unregisterMBean(objectName);
      }
      catch (Exception e)
      {
         log.debug("Unexpected Exception", e);
         fail("Caught unexpected exception");
      }
   }
}
