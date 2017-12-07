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
package org.jboss.test.jmx.compliance.notcompliant;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jboss.test.jmx.compliance.notcompliant.support.DynamicAndStandard;
import org.jboss.test.jmx.compliance.notcompliant.support.InterfaceProblems;
import org.jboss.test.jmx.compliance.notcompliant.support.NullDynamic;
import org.jboss.test.jmx.compliance.notcompliant.support.OverloadedAttribute1;
import org.jboss.test.jmx.compliance.notcompliant.support.OverloadedAttribute2;
import org.jboss.test.jmx.compliance.notcompliant.support.OverloadedAttribute3;
import org.jboss.test.jmx.compliance.notcompliant.support.OverloadedAttribute4;
import org.jboss.test.jmx.compliance.notcompliant.support.OverloadedAttribute5;

import junit.framework.TestCase;

public class NCMBeanTEST extends TestCase
{
   public NCMBeanTEST(String s)
   {
      super(s);
   }

   public void testOverloadedAttribute1()
   {
      registerAndTest(new OverloadedAttribute1());
   }

   public void testOverloadedAttribute2()
   {
      registerAndTest(new OverloadedAttribute2());
   }

   public void testOverloadedAttribute3()
   {
      registerAndTest(new OverloadedAttribute3());
   }

   public void testOverloadedAttribute4()
   {
      registerAndTest(new OverloadedAttribute4());
   }

   public void testOverloadedAttribute5()
   {
      registerAndTest(new OverloadedAttribute5());
   }

   public void testMixedDynamicStandard()
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      try
      {
         server.registerMBean(new DynamicAndStandard(), new ObjectName("test:foo=bar"));
         MBeanInfo info = server.getMBeanInfo(new ObjectName("test:foo=bar"));
         assertTrue("A mixed dynamic and standard mbean should be dynamic", 
                    info.getDescription().equals(DynamicAndStandard.DESCRIPTION));
      }
      catch (NotCompliantMBeanException e)
      {
         fail("A mixed dynamic and standardmbean is allowed from jmx 1.1");
      }
      catch (Exception e)
      {
         fail("unexpected exception when registering " + DynamicAndStandard.class.getName() + ": " + e.getMessage());
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(server);
      }
   }

   public void testNoConstructor()
   {
      try
      {
         registerAndDontTest(NoConstructor.getInstance());
      }
      catch (NotCompliantMBeanException e)
      {
         fail("An MBean without a public constructor is allowed from jmx 1.1");
      }
   }

   public void testInterfaceProblems()
   {
      try
      {
         registerAndDontTest(new InterfaceProblems());
      }
      catch (NotCompliantMBeanException e)
      {
         fail("FAILS IN RI: Cannot cope with overriden get/is in interfaces");
      }
   }

   public void testNullDynamic()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:test=test");
      boolean caught = false;
      try
      {
         server.registerMBean(new NullDynamic(), name);
      }
      catch (NotCompliantMBeanException e)
      {
         caught = true;
      }
      assertTrue("Expected NCME for null MBeanInfo", caught);        
   }

   private void registerAndTest(Object mbean)
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      try
      {
         server.registerMBean(mbean, new ObjectName("test:foo=bar"));
         fail("expected a NotCompliantMBeanException for " + mbean.getClass().getName());
      }
      catch (NotCompliantMBeanException e)
      {
         // this is what we want
      }
      catch (Exception e)
      {
         fail("unexpected exception when registering " + mbean.getClass().getName() + ": " + e);
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(server);
      }
   }

   private void registerAndDontTest(Object mbean)
      throws NotCompliantMBeanException
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      try
      {
         server.registerMBean(mbean, new ObjectName("test:foo=bar"));
      }
      catch (NotCompliantMBeanException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         fail("unexpected exception when registering " + mbean.getClass().getName() + ": " + e.getMessage());
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(server);
      }
   }
}
