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
package org.jboss.test.deployment;

import org.jboss.deployment.spi.DeploymentManagerImpl;
import org.jboss.deployment.spi.DeploymentMetaData;
import org.jboss.deployment.spi.JarUtils;
import org.jboss.deployment.spi.TargetModuleIDImpl;
import org.jboss.deployment.spi.factories.DeploymentFactoryImpl;
import org.jboss.test.JBossTestCase;
import org.jboss.util.UnreachableStatementException;

import javax.ejb.CreateException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.jar.JarOutputStream;

/** 
 * Deployment API JSR-88 tests
 *
 * @author Thomas.Diesler@jboss.org
 * @author Fabiano C. de Oliveira (JBAS-1995)
 * @author Scott.stark@jboss.org
 * @version $Revision: 81036 $
 */
public class BaseDeploymentTestCase extends JBossTestCase
{
   private static final String WAR_JBOSS_FILE = "WEB-INF/jboss-web.xml";

   private static final String JAR_JBOSS_FILE = "META-INF/jboss.xml";

   private static final String EAR_JBOSS_FILE = "META-INF/jboss-app.xml";

   private DeploymentFactory factory;
   private String targetType;

   public BaseDeploymentTestCase(String name, String targetType)
   {
      super(name);
      this.targetType = targetType;
   }

   protected String getTargetDescription()
   {
      return "No target specified";
   }

   /** Get the DeploymentManager
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      DeploymentFactoryImpl.register();
      DeploymentFactoryManager dfManager = DeploymentFactoryManager.getInstance();
      DeploymentFactory[] factories = dfManager.getDeploymentFactories();
      assertTrue("No DeploymentFactory available", factories.length > 0);
      factory = factories[0];
   }

   /** Check DeploymentManager
    */
   public void testDeploymentManager() throws Exception
   {
      DeploymentManager manager = getDeploymentManager();

      assertNotNull("No deployment manager", manager);

      Target target = manager.getTargets()[0];
      assertEquals(getTargetDescription(), target.getDescription());
   }

   /** Distribute a web app
    */
   public void testDistributeWebApp() throws Exception
   {
      ProgressObject progress = jsr88Deployment("deployment-web.war");
      try
      {
         assertServletAccess("custom-context");
      }
      finally
      {
         jsr88Undeploy(progress.getResultTargetModuleIDs());
      }
      try
      {
         assertServletAccess("custom-context");
         fail("Test deployment not undeployed");
      }
      catch (IOException e)
      {
         // ignore
      }
   }

   /** Distribute a bad web app
    */
   public void testBadWar() throws Exception
   {
      ProgressObject progress = null;
      try
      {
         progress = jsr88Deployment("baddeployment-web.war");
      }
      catch (java.lang.Throwable e)
      {
         return; // Done
      }
      if (progress != null)
      {
         DeploymentStatus state = progress.getDeploymentStatus();
         jsr88Undeploy(progress.getResultTargetModuleIDs());
         if (state.getState() != StateType.FAILED)
            fail("Test deployment deployement should have failed");
      }
      return; // Done
   }

   /** 
    * Distribute a EJB app
    */
   public void testDistributeEjbApp() throws Exception
   {
      ProgressObject progress = jsr88Deployment("deployment-ejb.jar");
      try
      {
          assertEjbEchoAccess();
      }
      finally
      {
          jsr88Undeploy(progress.getResultTargetModuleIDs());
      }
      try
      {
         assertEjbEchoAccess();
         fail("Test deployment not undeployed");
      }
      catch (Exception e)
      {
         // ignore
      }
   }

   public void testDistributeEARApp() throws Exception
   {
      ProgressObject progress = jsr88Deployment("deployment-ear.ear");
      try
      {
         assertServletAccess("custom-context");
         assertEjbEchoAccess();
      }
      finally
      {
         jsr88Undeploy(progress.getResultTargetModuleIDs());
      }
      try
      {
         assertServletAccess("custom-context");
         fail("Test deployment not undeployed");
      }
      catch (Exception e)
      {
         // ignore
      }

      try
      {
         assertEjbEchoAccess();
         fail("Test deployment not undeployed");
      }
      catch (Exception e)
      {
         // ignore
      }
   }

   /**
    * 
    * @throws Exception
    */ 
   public void testListStartStopModules() throws Exception
   {
      TargetModuleIDImpl child = null;
      TargetModuleIDImpl parent = null;

      // Get the deployment manager and the distribution targets
      DeploymentManager manager = getDeploymentManager();
      Target[] targets = manager.getTargets();
      assertEquals(1, targets.length);

      TargetModuleID[] modules = manager.getRunningModules(ModuleType.EAR, manager.getTargets());
      assertNull("no modules Available", modules);

      ProgressObject parentProgress = jsr88Deployment("deployment-ear.ear");
      assertServletAccess("custom-context");
      assertEjbEchoAccess();

      modules = manager.getRunningModules(ModuleType.EAR, manager.getTargets());
      assertNotNull(modules);
      assertEquals("one EAR module in the server", modules.length, 1);

      parent = (TargetModuleIDImpl) modules[0];
      assertTrue("wrong state", parent.isRunning());
      assertEquals("wrong type", parent.getModuleType(), ModuleType.EAR);
      assertEquals("EAR module have a jar and a war", parent.getChildTargetModuleID().length, 2);

      child = (TargetModuleIDImpl) parent.getChildTargetModuleID()[0];
      assertTrue("wrong state", child.isRunning());
      assertTrue("wrong type", child.getModuleType().equals(ModuleType.EJB) || child.getModuleType().equals(ModuleType.WAR));
      assertEquals("child have no child", child.getChildTargetModuleID().length, 0);

      child = (TargetModuleIDImpl) parent.getChildTargetModuleID()[1];
      assertTrue("wrong state", child.isRunning());
      assertTrue("wrong type " + child.getModuleType(), child.getModuleType().equals(ModuleType.EJB) || child.getModuleType().equals(ModuleType.WAR));
      assertEquals("child have no child", child.getChildTargetModuleID().length, 0);

      parentProgress = manager.stop(new TargetModuleID[] { parent });
      waitForCompletion(parentProgress.getDeploymentStatus());

      modules = manager.getNonRunningModules(ModuleType.EAR, manager.getTargets());
      assertNotNull(modules);
      assertEquals("one EAR module in the server", modules.length, 1);

      parent = (TargetModuleIDImpl) modules[0];
      assertFalse("wrong state", parent.isRunning());
      assertEquals("wrong type", parent.getModuleType(), ModuleType.EAR);
      assertEquals("EAR module have a jar and a war", parent.getChildTargetModuleID().length, 2);

      parentProgress = manager.start(new TargetModuleID[]{ parent });
      waitForCompletion(parentProgress.getDeploymentStatus());

      modules = manager.getRunningModules(ModuleType.EAR, manager.getTargets());
      assertNotNull(modules);
      assertEquals("one EAR module in the server", modules.length, 1);

      parent = (TargetModuleIDImpl) modules[0];
      assertTrue("wrong state", parent.isRunning());
      assertEquals("wrong type", parent.getModuleType(), ModuleType.EAR);
      assertEquals("EAR module have a jar and a war", parent.getChildTargetModuleID().length, 2);
      parentProgress = manager.undeploy(new TargetModuleID[]{ parent });
      waitForCompletion(parentProgress.getDeploymentStatus());

      modules = manager.getAvailableModules(ModuleType.EAR, manager.getTargets());
      assertNull("EAR must not be available", modules);

      try
      {
         assertServletAccess("custom-context");
         fail("Test deployment not undeployed");
      }
      catch (Exception e)
      {
         // ignore
      }

      try
      {
         assertEjbEchoAccess();
         fail("Test deployment not undeployed");
      }
      catch (Exception e)
      {
         // ignore
      }
   }

   /**
    * Obtain a DeploymentManager using a deployURI of the form
    * http://org.jboss.deployment/jsr88?targetType=targetType
    * Valid targetType values are:
    * jmx - Use the JMXTarget RMIAdaptor based deployment target
    * remote - Use the StreamingTarget remoting based deployment target
    * @return JSR88 DeploymentManager
    * @throws Exception
    */
   protected DeploymentManager getDeploymentManager()
      throws Exception
   {
      // Get the deployment manager and the distribution targets
      String mgrURI = DeploymentManagerImpl.DEPLOYER_URI+"?targetType="+targetType;
      DeploymentManager manager = factory.getDeploymentManager(mgrURI, null, null);
      return manager;
   }
   private void jsr88Undeploy(TargetModuleID[] resultTargetModuleIDs) throws Exception
   {
      DeploymentManager manager = getDeploymentManager();
      Target[] targets = manager.getTargets();
      assertEquals(1, targets.length);

      ProgressObject progress = manager.stop(resultTargetModuleIDs);
      DeploymentStatus status = progress.getDeploymentStatus();
      waitForCompletion(status);

      // Check the webapp is undeployed
      assertEquals(status.getMessage(), StateType.COMPLETED, status.getState());

      progress = manager.undeploy(resultTargetModuleIDs);
      status = progress.getDeploymentStatus();
      waitForCompletion(status);
      assertEquals(status.getMessage(), StateType.COMPLETED, status.getState());
   }

   private ProgressObject jsr88Deployment(String module)
      throws Exception
   {
      // Get the deployment manager and the distribution targets
      DeploymentManager manager = getDeploymentManager();
      Target[] targets = manager.getTargets();
      assertEquals(1, targets.length);

      File deploymentPlan = createDeploymentPlan(module);

      // Get a pointer to the plain web archive
      log.debug("module=" + module);
      File moduleArchive = new File(new URI(getDeployURL(module).toString()));
      assertTrue(moduleArchive.exists());

      // Deploy the test war
      ProgressObject progress = manager.distribute(targets, moduleArchive, deploymentPlan);
      DeploymentStatus status = progress.getDeploymentStatus();
      waitForCompletion(status);

      assertEquals(status.getMessage(), StateType.COMPLETED, status.getState());

      TargetModuleID[] moduleIDs = progress.getResultTargetModuleIDs();
      progress = manager.start(moduleIDs);
      status = progress.getDeploymentStatus();
      waitForCompletion(status);

      return progress;
   }

   private void assertEjbEchoAccess() throws NamingException, RemoteException, CreateException
   {
      InitialContext initial = new InitialContext();
      Object obj = initial.lookup("deployment/test/Echo");
      EchoHome home = (EchoHome) PortableRemoteObject.narrow(obj, EchoHome.class);
      Echo echo = home.create();

      assertEquals("Wrong EJB return", "Hello!", echo.echo("Hello!"));
   }

   private void waitForCompletion(DeploymentStatus status) throws InterruptedException
   {
      // wait for the deployment to finish
      while (StateType.RUNNING == status.getState())
         Thread.sleep(100);
   }

   private void assertServletAccess(String context) throws IOException
   {
      // Check that we can access the servlet
      URL servletURL = new URL("http://" + getServerHost() + ":8080/" + context);
      BufferedReader br = new BufferedReader(new InputStreamReader(servletURL.openStream()));
      String message = br.readLine();
      assertEquals("Hello World!", message);
   }

   private File createDeploymentPlan(String deploymentFile) throws Exception
   {
      String[] strs = null;

      // Create temp file for deployment plan
      File deploymentPlan = File.createTempFile("deploymentplan", ".zip");
      deploymentPlan.deleteOnExit();

      String jbossFile = getJBossFile(deploymentFile);
      String resourcedir = getResourceURL("deployment/" + jbossFile);
      File jbossDescriptor = new File(new URI(resourcedir));
      assertTrue(jbossDescriptor.exists());

      JarOutputStream jos = new JarOutputStream(new FileOutputStream(deploymentPlan));
      JarUtils.addJarEntry(jos, "!/" + jbossFile, new FileInputStream(jbossDescriptor));

      // Setup deployment plan meta data with propriatary descriptor
      DeploymentMetaData metaData = new DeploymentMetaData(deploymentFile);

      strs = jbossFile.split("/");
      metaData.addEntry(deploymentFile, strs[strs.length - 1]);

      // Add the meta data to the deployment plan
      String metaStr = metaData.toXMLString();

      JarUtils.addJarEntry(jos, DeploymentMetaData.ENTRY_NAME, new ByteArrayInputStream(metaStr.getBytes()));
      jos.flush();
      jos.close();

      return deploymentPlan;
   }

   private String getJBossFile(String deploymentFile)
   {
      if (deploymentFile.endsWith(".war"))
         return WAR_JBOSS_FILE;
      else if (deploymentFile.endsWith(".jar"))
         return JAR_JBOSS_FILE;
      else if (deploymentFile.endsWith(".ear"))
         return EAR_JBOSS_FILE;
      else
         fail("Wrong J2EE Module found...");
      throw new UnreachableStatementException();
   }
}
