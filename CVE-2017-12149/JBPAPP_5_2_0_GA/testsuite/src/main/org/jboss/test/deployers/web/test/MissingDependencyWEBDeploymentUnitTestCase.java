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
package org.jboss.test.deployers.web.test;

import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.client.spi.MissingDependency;
import org.jboss.test.deployers.AbstractDeploymentTest;

/**
 * Test for JBAS-4763.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85526 $
 */
public class MissingDependencyWEBDeploymentUnitTestCase extends AbstractDeploymentTest
{
   public static final String jbas4763Key        = "testdeployers-jbas4763";
   public static final String jbas4763Deployment = jbas4763Key + ".war";
   
   /**
    * Deploys a war with a non-existent dependency declared in jboss-web.xml,
    * checking that the deployment is not fully completed.
    * 
    * @throws Exception
    */
   public void testWEBDeployment() throws Exception
   {
      // As of 2008/01/24 a call to isDeployed() will return true, but the AS 
      // will list the WebModule created from the deployments as being incomplete.
      // So, we test for that. If something changes and isDeployed() no longer
      // returns true, that's a successful test outcome as well.
      if (isDeployed(jbas4763Deployment))
      {
         // This is too white box; we are checking for our key in a context name
         try 
         {
            this.invokeMainDeployer("checkIncompleteDeployments", new Object[]{}, new String[]{}, void.class);
            fail("Call to checkComplete() did not throw exception");
         }
         catch (Throwable good)
         {
            while (!(good instanceof IncompleteDeploymentException) && good.getCause() != null)
               good = good.getCause();
            
            assertTrue(good instanceof IncompleteDeploymentException);
            
            IncompleteDeploymentException ide = (IncompleteDeploymentException) good;
            Map<String, Set<MissingDependency>> missing = ide.getIncompleteDeployments().getContextsMissingDependencies();
            for (String deployment : missing.keySet())
            {
               if (deployment.indexOf(jbas4763Key) > -1)
                  return;
            }
            fail("Contexts missing dependencies did not include " + jbas4763Key);
         }
      }      
   }
   
   public MissingDependencyWEBDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getManagedDeployment(MissingDependencyWEBDeploymentUnitTestCase.class, jbas4763Deployment);
   }
}
