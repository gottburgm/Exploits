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
package org.jboss.test.jbossmx.compliance.notcompliant;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jboss.test.jbossmx.compliance.TestCase;
import org.jboss.test.jbossmx.compliance.notcompliant.support.DynamicAndStandard;
import org.jboss.test.jbossmx.compliance.notcompliant.support.OverloadedAttribute1;
import org.jboss.test.jbossmx.compliance.notcompliant.support.OverloadedAttribute2;
import org.jboss.test.jbossmx.compliance.notcompliant.support.OverloadedAttribute3;

public class NCMBeanTestCase
   extends TestCase
{
   public NCMBeanTestCase(String s)
   {
      super(s);
   }

   public void testOverloadedAttribute1()
   {
      registerAndTest(new OverloadedAttribute1(), true);
   }

   public void testOverloadedAttribute2()
   {
      // according to spec this is not a problem
      registerAndTest(new OverloadedAttribute2(), false);
   }

   public void testOverloadedAttribute3()
   {
      registerAndTest(new OverloadedAttribute3(), true);
   }

   public void testMixedDynamicStandard()
   {
      // according to the spec this is not a problem any more
      registerAndTest(new DynamicAndStandard(), false);
   }

   public void testNoConstructor()
   {
      registerAndTest(new NoConstructor(), true);
   }

   private void registerAndTest(Object mbean, boolean shouldFail)
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.registerMBean(mbean, new ObjectName("test:foo=bar"));
         if (shouldFail)
            fail("expected a NotCompliantMBeanException for " + mbean.getClass().getName());
      }
      catch (NotCompliantMBeanException e)
      {
         if (shouldFail == false)
            fail("NotCompliantMBeanException for " + mbean.getClass().getName());
      }
      catch (Exception e)
      {
         fail("unexpected exception when registering " + mbean.getClass().getName() + ": " + e.getMessage());
      }
   }
}
