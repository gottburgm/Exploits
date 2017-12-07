/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test;

import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestServices;

/**
 * This test case will use custom.jndi.properties to find the deployer.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class CustomJNDIJBossTestCase extends JBossTestCase
{

   public CustomJNDIJBossTestCase(String name)
   {
      super(name);
   }

   /**
    * Overriden to return CustomJNDIJBossTestServices as the test delegate.
    */
   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      AbstractTestDelegate delegate = new CustomJNDIJBossTestServices(clazz);
      return delegate;
   }

   /**
    * Get a JBossTestSetup that does login and deployment in setUp/tearDown
    *
    * @param test a Test
    * @param jarNames is a comma seperated list of deployments
    */
   public static Test getDeploySetup(final Test test, final String jarNames)
      throws Exception
   {
      CustomJNDIJBossTestSetup wrapper = new CustomJNDIJBossTestSetup(test)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploymentException = null;
            try
            {
               this.delegate.init();

               if (this.getDelegate().isSecureTest())
                  this.delegate.login();

               if (jarNames == null) return;

               // deploy the comma seperated list of jars
               StringTokenizer st = new StringTokenizer(jarNames, ", ");
               while (st != null && st.hasMoreTokens())
               {
                  String jarName = st.nextToken();
                  this.redeploy(jarName);
                  this.getLog().debug("deployed package: " + jarName);
               }
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
            }
         }

         protected void tearDown() throws Exception
         {
            if (jarNames == null) return; //Nothing to Undeploy
             
            // undeploy the comma seperated list of jars
            StringTokenizer st = new StringTokenizer(jarNames, ", ");
            String[] depoyments = new String[st.countTokens()];
            for (int i = depoyments.length - 1; i >= 0; i--)
               depoyments[i] = st.nextToken();
            for (int i = 0; i < depoyments.length; i++)
            {
               String jarName = depoyments[i];
               this.undeploy(jarName);
               this.getLog().debug("undeployed package: " + jarName);
            }

            if (this.getDelegate().isSecureTest())
               this.delegate.logout();
         }
      };
      return wrapper;
   }

   public static Test getDeploySetup(final Class clazz, final String jarName)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, jarName);
   }
}
