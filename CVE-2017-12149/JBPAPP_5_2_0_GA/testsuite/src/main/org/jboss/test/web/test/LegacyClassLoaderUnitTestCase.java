/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.web.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

/**
 * LegacyClassLoaderUnitTestCase.
 * 
 * The cases in this test can be found here:
 * http://www.jboss.com/index.html?module=bb&op=viewtopic&t=137381
 * with CaseG being no configuration at all.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class LegacyClassLoaderUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return new TestSuite(LegacyClassLoaderUnitTestCase.class);
   }

   public LegacyClassLoaderUnitTestCase(String name)
   {
      super(name);
   }

   protected void doTest(String caseName) throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testFromDD = new ObjectName("jboss.test:service=LegacyWebClassLoader");

      deploy("jbossweb-legacy-classloader-" + caseName + ".war");
      try
      {
         assertEquals(caseName, true, server.getAttribute(testFromDD, caseName));
      }
      finally
      {
         undeploy("jbossweb-legacy-classloader-"+ caseName + ".war");
      }
   }

   public void testLegactClassLoader() throws Exception
   {
      deploy("jbossweb-legacy-classloader-fromdd.sar");
      try
      {
         deploy("jbossweb-legacy-classloader-fromod.sar");
         try
         {
            doTest("CaseA");
            doTest("CaseB");
            doTest("CaseC");
            doTest("CaseD");
            doTest("CaseE");
            doTest("CaseF");
            doTest("CaseG");
         }
         finally
         {
            undeploy("jbossweb-legacy-classloader-fromod.sar");
         }
      }
      finally
      {
         undeploy("jbossweb-legacy-classloader-fromdd.sar");
      }
   }
}
