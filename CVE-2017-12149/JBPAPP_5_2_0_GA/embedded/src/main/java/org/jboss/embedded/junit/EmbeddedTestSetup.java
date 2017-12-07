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
import junit.framework.TestSuite;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.DeploymentGroup;
import org.jboss.deployers.spi.DeploymentException;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class EmbeddedTestSetup extends TestSetup
{
   public EmbeddedTestSetup(Test test)
   {
      super(test);
   }

   protected void setUp()
   {
      testSetup();
   }

   public static void testSetup()
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

   public static void testTearDown()
   {
      
   }

   protected void tearDown()
   {
   }


   /**
    * Returns a wrapper that will only bootstrap embedded if VM hasn't already bootstrapped
    *
    * @param testClass
    * @return
    */
   public static Test getWrapper(Class testClass)
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(testClass);

      // setup test so that embedded JBoss is started/stopped once for all tests here.
      return new EmbeddedTestSetup(suite);
   }

   /**
    * Returns a wrapper that will only bootstrap embedded if VM hasn't already bootstrapped
    * Deploys items represented in classpath as staed in DeploymentGroup.addClasspath
    *
    * @param testClass
    * @param classpaths
    * @return
    */
   public static Test deployClasspath(Class testClass, final String classpaths)
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(testClass);

      return new EmbeddedTestSetup(suite)
      {
         DeploymentGroup group;

         @Override
         protected void setUp()
         {
            super.setUp();
            try
            {
               group = Bootstrap.getInstance().createDeploymentGroup();
               group.addClasspath(classpaths);
               group.process();
            }
            catch (DeploymentException e)
            {
               throw new RuntimeException("Unable to deploy classpath: " + classpaths, e);
            }
         }

         @Override
         protected void tearDown()
         {
            try
            {
               group.undeploy();
            }
            catch (DeploymentException e)
            {
               throw new RuntimeException("Unable to undeploy classpath: " + classpaths, e);
            }
            super.tearDown();
         }
      };
   }

   /**
    * Returns a wrapper that will only bootstrap embedded if VM hasn't already bootstrapped
    * Deploys items represented in resource list
    *
    * @param testClass
    * @param resources
    * @return
    */
   public static Test deployResource(Class testClass, final String... resources)
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(testClass);

      return new EmbeddedTestSetup(suite)
      {
         DeploymentGroup group;

         @Override
         protected void setUp()
         {
            super.setUp();
            try
            {
               group = Bootstrap.getInstance().createDeploymentGroup();
               for (String resource : resources)
               {
                  group.addResource(resource);
               }
               group.process();
            }
            catch (DeploymentException e)
            {
               String message = "Unable to deploy resources:";
               for (String resource : resources)
               {
                  message += " " + resource;
               }
               throw new RuntimeException(message, e);
            }
         }

         @Override
         protected void tearDown()
         {
            try
            {
               group.undeploy();
            }
            catch (DeploymentException e)
            {
               throw new RuntimeException("Unable to undeploy resources: " + resources, e);
            }
            super.tearDown();
         }
      };
   }
}
