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
package org.jboss.embedded.junit;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.embedded.Bootstrap;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * This base TestCase class will automatically
 * 1. bootstrap Embedded JBoss
 * 2. Call static methods deploy() and undeploy() of the class at setup() and tearDown()
 *
 * If the preProcessedTest() method is used, this creates a TestSetup that will skip step 2 above
 * and instead call the static deploy() method before any test methods are run in the class.  Undeploy() will
 * be called when all test methods of the class are finished.
 *
 * This class was designed so that all tests within the class can be run at once from your IDE, or just one test method.
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class BaseTestCase extends TestCase
{

   public BaseTestCase()
   {
   }

   public BaseTestCase(String string)
   {
      super(string);
   }

   private static HashSet flagged = new HashSet();

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      if (flagged.contains(this.getClass())) return;
      bootstrap();
      try
      {
         Method deploy = this.getClass().getMethod("deploy");
         deploy.invoke(null);
      }
      catch (NoSuchMethodException ignored)
      {
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      if (flagged.contains(this.getClass())) return;
      try
      {
         Method undeploy = this.getClass().getMethod("undeploy");
         undeploy.invoke(null);
      }
      catch (NoSuchMethodException ignored)
      {
      }
      if (System.getProperty("shutdown.embedded.jboss") != null) Bootstrap.getInstance().shutdown();
   }

   private static void bootstrap()
   {
      if (Bootstrap.getInstance().isStarted()) return;

      try
      {
         Bootstrap.getInstance().bootstrap();
      }
      catch (Exception error)
      {
         throw new RuntimeException("Failed to bootstrap", error);
      }
   }

   /**
    * Use this in a static suite() method in your test class to
    * bootstrap and deploy your modules before any tests run.
    *
    *
    * @param testClass
    * @return
    */
   public static Test preProcessedTest(final Class testClass)
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(testClass);

      return new TestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            bootstrap();
            try
            {
               Method deploy = testClass.getMethod("deploy");
               deploy.invoke(null);
            }
            catch (NoSuchMethodException ignored)
            {
            }
            flagged.add(testClass);
         }

         @Override
         protected void tearDown() throws Exception
         {
            try
            {
               Method undeploy = null;
               try
               {
                  undeploy = testClass.getMethod("undeploy");
                  undeploy.invoke(null);
               }
               catch (NoSuchMethodException ignored)
               {
               }
               if (System.getProperty("shutdown.embedded.jboss") != null) Bootstrap.getInstance().shutdown();
               super.tearDown();
            }
            finally
            {
               flagged.remove(testClass);
            }
         }
      };
   }
}
