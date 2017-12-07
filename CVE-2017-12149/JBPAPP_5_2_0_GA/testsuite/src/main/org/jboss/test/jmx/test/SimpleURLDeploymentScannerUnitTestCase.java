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
package org.jboss.test.jmx.test;

import java.net.URL;

import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * SimpleURLDeploymentScannerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class SimpleURLDeploymentScannerUnitTestCase extends AbstractURLDeploymentScannerTest
{
   ObjectName test = ObjectNameFactory.create("test:name=DefaultPkgService");
   String module = "defaultpkg.sar";
   
   public void testSomething() throws Exception
   {
      hotDeploy(module);
      try
      {
         assertEquals(new Integer(ServiceMBean.STARTED), getServer().getAttribute(test, "State"));
      }
      finally
      {
         hotUndeploy(module);
      }
      assertFalse(getServer().isRegistered(test));
   }
   
   /**
    * Test JBAS-3118 feature 
    */
   public void testSuspendResumeDeployment() throws Exception
   {
      // tell the scanner to ignore the deployment
      URL deployment = getTargetURL(module);
      suspendDeployment(deployment);
      
      try
      {
         // now deploy, but expect that nothing happens at this point 
         hotDeploy(module);
         assertFalse(getServer().isRegistered(test));
         
         // tell the scanner to resume the deployment,
         // wait and see if it was actually deployed
         resumeDeployment(deployment, false);
         super.sleep(2000);
         assertEquals(new Integer(ServiceMBean.STARTED), getServer().getAttribute(test, "State"));
         
         // suspend again and undeploy, the module shouldn't be undeployed
         suspendDeployment(deployment);
         hotUndeploy(module);
         assertEquals(new Integer(ServiceMBean.STARTED), getServer().getAttribute(test, "State"));
         
         // tell the scanner to resume the deployment,
         // wait and see if it was actually undeployed
         resumeDeployment(deployment, false);
         super.sleep(2000);
         assertFalse(getServer().isRegistered(test));
      }
      catch (Exception ignore)
      {
         hotUndeploy(module);
      }
   }
   
   public static Test suite() throws Exception
   {
      return getTestSuite(SimpleURLDeploymentScannerUnitTestCase.class);
   }
   
   public SimpleURLDeploymentScannerUnitTestCase(String name)
   {
      super(name);
   }
}
