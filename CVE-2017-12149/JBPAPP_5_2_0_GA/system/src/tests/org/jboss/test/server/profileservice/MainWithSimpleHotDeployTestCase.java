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
package org.jboss.test.server.profileservice;

import java.io.File;
import java.security.CodeSource;

import org.jboss.Main;
import org.jboss.bootstrap.microcontainer.ServerImpl;
import org.jboss.bootstrap.spi.Server;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.test.BaseTestCase;

/**
 * Test of the jboss main loading a bootstrap configuration via the ProfileService
 * and additional service via a simple hot deployment service.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */
public class MainWithSimpleHotDeployTestCase extends BaseTestCase
{
   public MainWithSimpleHotDeployTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------

   /* (non-Javadoc)
    * @see org.jboss.test.AbstractTestCase#configureLogging()
    */
   @Override
   protected void configureLogging()
   {
      //enableTrace("org.jboss.kernel");
   }

   /**
    * Test the startup of the org.jboss.Main entry point using the "default"
    * profile and bootstrap deployer-beans.xml search logic.
    * @throws Exception
    */
   public void testDefaultStartup() throws Exception
   {
      String deployPrefix = "";
      // If jbosstest.deploy.dir is not defined fail
      String deployDirEnv = System.getenv("jbosstest.deploy.dir");
      String deployDirProp = System.getProperty("jbosstest.deploy.dir");
      if( deployDirProp == null && deployDirEnv != null )
      {
         System.setProperty("jbosstest.deploy.dir", deployDirEnv);
         deployDirProp = deployDirEnv;
      }
      String supportDirEnv = System.getenv("jbosstest.support.dir");
      String supportDirProp = System.getProperty("jbosstest.support.dir");
      if( supportDirProp == null && supportDirEnv != null )
      {
         System.setProperty("jbosstest.support.dir", supportDirEnv);
         supportDirProp = supportDirEnv;
      }

      if( supportDirProp == null )
      {
         // If these have not been set, assume running inside eclipse from the system folder 
         File resourcesDir = new File("output/eclipse-resources");
         File classesDir = new File("output/eclipse-test-classes");
         deployDirProp = resourcesDir.toURL().toExternalForm();
         supportDirProp = classesDir.toURL().toExternalForm();
         System.setProperty("jbosstest.deploy.dir", deployDirProp);
         System.setProperty("jbosstest.support.dir", supportDirProp);
         deployPrefix = "tests/bootstrap/defaulthotdeploy/";
      }
      assertNotNull("jbosstest.support.dir != null", supportDirProp);
      assertNotNull("jbosstest.deploy.dir != null", deployDirProp);
      // Set the deploy prefix 
      

      String[] args = {"-c", "defaulthotdeploy", "-Djboss.server.deployerBeansPrefix="+deployPrefix};
      Main main = new Main();
      main.boot(args);
      Server server = main.getServer();
      assertTrue("Server", server instanceof ServerImpl);
      ServerImpl serverImpl = (ServerImpl) server;

      // Validate that the expected deployment beans exist
      Kernel kernel = serverImpl.getKernel();
      assertInstalled(kernel, "ProfileService");
      assertInstalled(kernel, "MainDeployer");
      assertInstalled(kernel, "BeanDeployer");
      assertInstalled(kernel, "VFSDeploymentScanner");
      KernelRegistry registry = kernel.getRegistry();
      KernelRegistryEntry entry = registry.getEntry("VFSDeploymentScanner");
      /** TODO DeploymentScanner scanner = (DeploymentScanner) entry.getTarget();
      synchronized( scanner )
      {
         while( scanner.getScanCount() <= 0 )
            scanner.wait(10000);
      }
      log.info("Notified of scan: "+scanner.getScanCount());
      */

      // Expected hot deployments
      assertInstalled(kernel, "VFSClassLoader");
      assertInstalled(kernel, "TestBean");
      assertInstalled(kernel, "VFSClassLoader-unpacked");
      assertInstalled(kernel, "TestBean-unpacked");
      entry = registry.getEntry("TestBean");
      Object testBean = entry.getTarget();
      CodeSource testBeanCS = testBean.getClass().getProtectionDomain().getCodeSource();
      log.info("TestBean.CS: "+testBeanCS);
      log.info("TestBean.ClassLoader: "+testBean.getClass().getClassLoader());
      

      // Shutdown
      main.shutdown();
   }

   private void assertInstalled(Kernel kernel, String name)
   {
      KernelRegistry registry = kernel.getRegistry();
      KernelRegistryEntry entry = registry.getEntry(name);
      assertEquals(name+" Installed", ControllerState.INSTALLED, entry.getState());      
   }
}
