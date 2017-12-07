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

import java.io.File;
import javax.management.ObjectName;
import javax.naming.NamingException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.test.JBossTestCase;
import org.jboss.util.file.Files;
import org.jboss.util.platform.Java;

/** Tests of reployment of bad deployment packages
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 87534 $
 */

public class UndeployBrokenPackageUnitTestCase extends JBossTestCase 
{
   public UndeployBrokenPackageUnitTestCase(String name)
   {
      super(name);
   }

   public void testBrokenPackageRedeployment() throws Exception
   {
      getLog().info("+++ testBrokenPackageRedeployment");
      String testPackage = "undeploybroken.jar";
      String missingDatasource = "test-service.xml";
      ObjectName entityAName = new ObjectName("jboss.j2ee:service=EJB,jndiName=EntityA");
      ObjectName entityBName = new ObjectName("jboss.j2ee:service=EJB,jndiName=EntityB");
      getLog().info("testPackage is : " + testPackage);
      try 
      {
         try 
         {
            deploy(testPackage);
            fail("test package " + testPackage + " deployed successfully without needed datasource!");
         }
         catch (DeploymentException e)
         {
            log.info("caught exception as expected", e);
         } // end of try-catch
         undeploy(testPackage);
         getLog().info("Undeployed testPackage");
         deploy(missingDatasource);
         getLog().info("Deployed missing datasource");
         deploy(testPackage);
      }
      finally
      {
         try 
         {
            undeploy(testPackage);
         }
         catch (Throwable e)
         {
         } // end of try-catch
         try 
         {
            undeploy(missingDatasource);
         }
         catch (Throwable e)
         {
         } // end of try-catch
         
      } // end of try-catch
      try 
      {
         getInitialContext().lookup("EntityA");
         fail("EntityA found after undeployment");
      }
      catch (NamingException e)
      {
         log.info("caught exception as expected", e);
      } // end of try-catch
      try 
      {
         getInitialContext().lookup("EntityB");
         fail("EntityB found after undeployment");
      }
      catch (NamingException e)
      {
         log.info("caught exception as expected", e);
      } // end of try-catch
      assertTrue("EntityA mbean is registered!", !getServer().isRegistered(entityAName));
      assertTrue("EntityB mbean is registered!", !getServer().isRegistered(entityBName));

   }

   /** Deploy an ejb that has an invalid ejb-jar.xml descriptor and then
    reploy a valid version after undeploying the invalid jar.
   JBAS-6773 - temporarily comment out test case to avoid VM crash so
   	full test runs can complete for results for 5.1.0.CR1 release
   public void testBadEjbRedeployment() throws Exception
   {
      getLog().info("+++ testBadEjbRedeployment");
      String testPackage = "ejbredeploy.jar";
      // Move the bad jar into ejbredeploy.jar
      String deployDir = System.getProperty("jbosstest.deploy.dir");
      if (deployDir == null)
      {
         deployDir = "output/lib";
      }
      File thejar = new File(deployDir, "ejbredeploy.jar");
      File badjar = new File(deployDir, "ejbredeploy-bad.jar");
      File goodjar = new File(deployDir, "ejbredeploy-good.jar");
      
      assertTrue("badjar exists", badjar.exists());
      assertTrue("goodjar exists", goodjar.exists());

      try
      {
         if (thejar.exists())
            assertTrue(thejar.delete());

         Files.copy(badjar, thejar);
         getLog().info("Deploying testPackage: " + testPackage);
         try 
         {
            deploy(testPackage);
            fail("test package " + testPackage + " deployed successfully with bad descriptor!");
         }
         catch (Exception e)
         {
            log.info("caught exception as expected", e);
         }

         // JBAS-5953, conditionally delay this test on Sun 1.6 JVM
         // The deployed jar is still used by VFS that keeps files open for a while
         // and overwriting the jar on this particular VM combination causes crashes
         boolean sunVM = System.getProperty("java.vm.vendor").indexOf("Sun") > -1;
         if (Java.isVersion(Java.VERSION_1_6) && sunVM)
         {
            // delay the file overwrite until the vfs reaper closes it
            getLog().info("Delaying file overwrite...");
            Thread.sleep(10000);
         }
         //assertTrue(thejar.delete()); // TODO - this should work
         Files.copy(goodjar, thejar);
         getLog().info("Redeploying testPackage: " + testPackage);
         
         redeploy(testPackage);
         Object home = getInitialContext().lookup("EntityA");
         getLog().info("Found EntityA home: " + home);
      }
      finally
      {
         undeploy(testPackage);
      }
   }

*/
   /** Deploy an ejb that has an invalid ejb-jar.xml descriptor and then
    deploy a completely unrelated service to test that the failed deployment
    does not prevent deployment of the unrelated service.
    */
   public void testBadSideAffects() throws Exception
   {
      getLog().info("+++ testBadSideAffects");
      getLog().info("Deploying testPackage: ejbredeploy-bad.jar");
      try
      {
         try
         {
            deploy("ejbredeploy-bad.jar");
            fail("test package deployed successfully with bad descriptor!");
         }
         catch (DeploymentException e)
         {
            log.info("caught exception as expected", e);
         }

         getLog().info("Deploying testPackage: test-service.xml");
         deploy("test-service.xml");
         getLog().info("Deployed test-service.xml");
         ObjectName serviceName = new ObjectName("jboss.test:service=TestService,test=jmx");
         assertTrue("test-service.xml mbean is registered", getServer().isRegistered(serviceName));
      }
      finally
      {
         try
         {
            undeploy("test-service.xml");
         }
         catch(Throwable t)
         {
         }
         try
         {
            undeploy("ejbredeploy-bad.jar");
         }
         catch(Throwable t)
         {
         }
      }
   }

}// UndeployBrokenPackageUnitTestCase

