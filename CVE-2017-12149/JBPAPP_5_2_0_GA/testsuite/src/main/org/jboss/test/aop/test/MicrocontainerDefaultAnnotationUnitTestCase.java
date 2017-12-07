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
package org.jboss.test.aop.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


import junit.framework.Test;
import junit.framework.TestSuite;


import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: MicrocontainerDefaultAnnotationUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

public class MicrocontainerDefaultAnnotationUnitTestCase
        extends JBossTestCase
{
   static boolean deployed = false;
   static int test = 0;

   public MicrocontainerDefaultAnnotationUnitTestCase(String name)
   {
      super(name);
   }

   public void testDefault() throws Exception
   {
      ObjectName testerName = new ObjectName("jboss.aop:name=Bean");
      checkAnnotation(testerName, "Default");
   }

   public void testOverridden() throws Exception
   {
      ObjectName testerName = new ObjectName("jboss.aop:name=Bean2");
      checkAnnotation(testerName, "Overridden");
   }

   private void checkAnnotation(ObjectName on, String expectedValue) throws Exception
   {
      MBeanServerConnection server = getServer();
      String value = (String)server.getAttribute(on, "AnnotationValue");
      assertNotNull(value);
      assertEquals(expectedValue, value);
   }


   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(MicrocontainerDefaultAnnotationUnitTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "aop-mc-defaultannotationtest.jar");
      return setup;
   }

}
