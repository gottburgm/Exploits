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
package org.jboss.embedded.tutorial;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.tutorial.junit.EasierEjbTestCase;
import org.jboss.embedded.tutorial.junit.EjbTestCase;
import org.jboss.embedded.tutorial.junit.JarByResourceTestCase;
import org.jboss.embedded.tutorial.junit.MdbTestCase;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class AllTests extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("All Tests");

      suite.addTest(EasierEjbTestCase.suite());
      suite.addTest(EjbTestCase.suite());
      suite.addTest(JarByResourceTestCase.suite());
      suite.addTest(MdbTestCase.suite());

      return new TestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            Bootstrap.getInstance().bootstrap();
            super.setUp();
         }

         @Override
         protected void tearDown() throws Exception
         {
            Bootstrap.getInstance().shutdown();
            super.tearDown();
         }
      };

   }
}