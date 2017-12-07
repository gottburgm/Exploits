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
package org.jboss.test.jmx.compliance.standard;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import junit.framework.TestCase;

import org.jboss.test.jmx.compliance.standard.support.ArbitraryInterface;
import org.jboss.test.jmx.compliance.standard.support.MBeanRunnable;
import org.jboss.test.jmx.compliance.standard.support.MyRunnable;
import org.jboss.test.jmx.compliance.standard.support.MyStandardMBean;
import org.jboss.test.jmx.compliance.standard.support.NoConstructorsStandardMBean;
import org.jboss.test.jmx.compliance.standard.support.Trivial;
import org.jboss.test.jmx.compliance.standard.support.TrivialMBean;


/**
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */

public class StandardMBeanTEST 
   extends TestCase
{
   public StandardMBeanTEST(String s)
   {
      super(s);
   }

   public void testOverrideManagementInterface()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:test=test");
      server.registerMBean(new MBeanRunnable(), name);
      server.invoke(name, "run", new Object[0], new String[0]);
   }

   public void testSpecifyManagementInterface()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:test=test");
      server.registerMBean(new StandardMBean(new MyRunnable(), Runnable.class), name);
      server.invoke(name, "run", new Object[0], new String[0]);
   }

   public void testDontSpecifyManagementInterface()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:test=test");
      server.registerMBean(new StandardMBean(new Trivial(), null), name);
      server.invoke(name, "doOperation", new Object[] { "arg" }, new String[] { String.class.getName() });
   }

   public void testGetImplementationImplied()
      throws Exception
   {
      StandardMBean std = new MBeanRunnable();
      assertTrue("MBeanRunnable is its own implementation", std == std.getImplementation());
      assertTrue("MBeanRunnable is its own implementation class", std.getClass() == std.getImplementationClass());
   }

   public void testGetImplementationSpecified()
      throws Exception
   {
      Runnable obj = new MyRunnable();
      StandardMBean std = new StandardMBean(obj, Runnable.class);
      assertTrue("MyRunnable is the implementation", obj == std.getImplementation());
      assertTrue("MyRunnable is the implementation class", obj.getClass() == std.getImplementationClass());
   }

   public void testMBeanInterfaceImplied()
      throws Exception
   {
      StandardMBean std = new MBeanRunnable();
      assertTrue("MBeanRunnable has Runnable as a management interface", Runnable.class == std.getMBeanInterface());
   }

   public void testMBeanInterfaceSpecified()
      throws Exception
   {
      Runnable obj = new MyRunnable();
      StandardMBean std = new StandardMBean(obj, Runnable.class);
      assertTrue("MyRunnable has Runnable as a management interface", Runnable.class == std.getMBeanInterface());
   }

   public void testMBeanInterfaceOldStyle()
      throws Exception
   {
      Object obj = new Trivial();
      StandardMBean std = new StandardMBean(obj, null);
      assertTrue("Trivial has TrivialMBean as a management interface", TrivialMBean.class == std.getMBeanInterface());
   }

   public void testMetaData()
      throws Exception
   {
      StandardMBean std = new MyStandardMBean();
      MBeanInfo info = std.getMBeanInfo();
      assertEquals(MyStandardMBean.MBEAN_CLASSNAME, info.getClassName());
      assertEquals(MyStandardMBean.MBEAN_DESCRIPTION, info.getDescription());

      MBeanAttributeInfo[] attributes = info.getAttributes();
      assertEquals(attributes.length, 1);
      assertEquals(MyStandardMBean.MBEAN_ATTRIBUTE_DESCRIPTION + "AnAttribute", attributes[0].getDescription());

      MBeanConstructorInfo[] constructors = info.getConstructors();
      assertEquals(constructors.length, 2);
      for (int i = 0; i < 2; i++)
      {
         if (constructors[i].getSignature().length == 0)
            assertEquals(MyStandardMBean.MBEAN_CONSTRUCTOR_DESCRIPTION + "0", constructors[i].getDescription());
         else
         {
            assertEquals(MyStandardMBean.MBEAN_CONSTRUCTOR_DESCRIPTION + "2", constructors[i].getDescription());
            MBeanParameterInfo[] params = constructors[i].getSignature();
            assertEquals(params.length, 2);
            assertEquals(MyStandardMBean.MBEAN_PARAMETER + "0", params[0].getName());
            assertEquals(MyStandardMBean.MBEAN_PARAMETER + "1", params[1].getName());
            assertEquals(MyStandardMBean.MBEAN_PARAMETER_DESCRIPTION + "0", params[0].getDescription());
            assertEquals(MyStandardMBean.MBEAN_PARAMETER_DESCRIPTION + "1", params[1].getDescription());
         }
      }

      MBeanOperationInfo[] operations = info.getOperations();
      assertEquals(operations.length, 1);
      assertEquals(MyStandardMBean.MBEAN_OPERATION_DESCRIPTION + "anOperation", operations[0].getDescription());
      MBeanParameterInfo[] params = operations[0].getSignature();
      assertEquals(params.length, 2);
      assertEquals(MyStandardMBean.MBEAN_PARAMETER + "anOperation0", params[0].getName());
      assertEquals(MyStandardMBean.MBEAN_PARAMETER + "anOperation1", params[1].getName());
      assertEquals(MyStandardMBean.MBEAN_PARAMETER_DESCRIPTION + "anOperation0", params[0].getDescription());
      assertEquals(MyStandardMBean.MBEAN_PARAMETER_DESCRIPTION + "anOperation1", params[1].getDescription());
      assertEquals(MBeanOperationInfo.ACTION, operations[0].getImpact());
   }

   public void testNoConstructorsMetaData()
      throws Exception
   {
      StandardMBean std = new NoConstructorsStandardMBean();
      MBeanInfo info = std.getMBeanInfo();

      MBeanConstructorInfo[] constructors = info.getConstructors();
      assertEquals(constructors.length, 0);
   }

   public void testCaching()
      throws Exception
   {
      StandardMBean std = new MyStandardMBean();
      MBeanInfo info = std.getMBeanInfo();
      assertTrue("MBeanInfo should be cached", info == std.getMBeanInfo());
   }

   public void testErrors()
      throws Exception
   {
      boolean caught = false;
      try
      {
         new StandardMBean(null, Runnable.class);
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("Expected IllegalArgumentException for null implementation", caught);

      caught = false;
      try
      {
         new StandardMBean(new MyRunnable(), null);
      }
      catch (NotCompliantMBeanException e)
      {
         caught = true;
      }
      assertTrue("Expected NotCompliantMBeanException for null management interface", caught);

      caught = false;
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new MBeanRunnable(true), name);
      }
      catch (NotCompliantMBeanException e)
      {
         caught = true;
      }
      assertTrue("Expected NotCompliantMBeanException for wrong management interface", caught);

      caught = false;
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new MBeanRunnable(0), name);
      }
      catch (NotCompliantMBeanException e)
      {
         caught = true;
      }
      assertTrue("Expected NotCompliantMBeanException for null management interface", caught);
   }
}
