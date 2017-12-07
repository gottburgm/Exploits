/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.deployers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.virtual.VFS;

/**
 * Abstract deployment test.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class AbstractDeploymentTest extends JBossTestCase
{
   protected static Logger staticLog = Logger.getLogger(AbstractDeploymentTest.class);

   public static final String ear1Deployment = "testdeployers-ear1.ear";
   public static final String earAltDDDeployment = "testdeployers-ear-altdd.ear";
   public static final String earAltDDClientDeployment = "testdeployers-ear-altdd-client.ear";
   public static final String earAltDDConnectorDeployment = "testdeployers-ear-altdd-connector.ear";
   public static final String ear1DeploymentUnpacked = "unpacked-ear1.ear";
   public static final String ear2DeploymentUnpacked = "unpacked-ear2.ear";
   public static final String earNoAppXml = "testdeployers-ear-noappxml.ear";
   public static final String bean1Deployment = "testdeployers-bean1ejb.jar";
   public static final String bean1DeploymentUnpacked = "unpacked-bean1ejb.jar";
   public static final String notBean1Deployment = "bean1ejb-not.ajar";
   public static final String notBean1DeploymentUnpacked = "unpacked-bean1ejb-not.ajar";
   public static final String web1Deployment = "testdeployers-web1.war";
   public static final String web1DeploymentUnpacked = "unpacked-web1.war";
   public static final String notWeb1Deployment = "web1-not.awar";
   public static final String notWeb1DeploymentUnpacked = "unpacked-web1-not.awar";
   public static final String rar1Deployment = "testdeployers-mcf1.rar";
   public static final String rarInvalidDeployment = "testdeployers-invalidmcf.rar";
   public static final String rar1DeploymentUnpacked = "unpacked-mcf1.rar";
   public static final String notRar1Deployment = "mcf1-not.arar";
   public static final String notRar1DeploymentUnpacked = "unpacked-mcf1-not.arar";
   public static final String rarjar1Deployment = "testdeployers-mcf1.jar";
   public static final String client1Deployment = "testdeployers-client1.jar";
   public static final String client1DeploymentUnpacked = "unpacked-client1.jar";
   public static final String notClient1Deployment = "client1-not.ajar";
   public static final String notClient1DeploymentUnpacked = "unpacked-client1-not.ajar";
   public static final String ds1Deployment = "testdeployers-mcf1-ds.xml";
   public static final String ds1DeploymentUnpacked = "unpacked-mcf1-ds.xml";
   public static final String ds1DeploymentUnpacked2 = "unpacked2-mcf1-ds.xml";
   public static final String service1Deployment = "testdeployers-1-service.xml";
   public static final String sar1Deployment = "testdeployers-mbean1.sar";
   public static final String sar1DeploymentUnpacked = "unpacked-mbean1.sar";
   public static final String notSar1Deployment = "mbean1-not.asar";
   public static final String notSar1DeploymentUnpacked = "unpacked-mbean1-not.asar";

   /** We use the default profile, defined by DeploymentManager to deploy apps. */
   public static final ProfileKey defaultProfile = new ProfileKey(ProfileKey.DEFAULT); 
   
   protected static Test getManagedDeployment(final Class clazz, final String jarNames) throws Exception
   {
      return getManagedDeployment(clazz, jarNames, false);
   }

   protected static Test getManagedDeployment(final Class clazz, final String jarNames, final boolean copyContent) throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return new JBossTestSetup(clazz, suite)
      {
         private DeploymentManager dm;
         
         Collection<String> deploymentNames = new HashSet<String>();

         protected DeploymentManager getDeploymentManager() throws Exception
         {
            if (dm == null)
            {
               InitialContext ctx = new InitialContext();
               ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
               dm = ps.getDeploymentManager();
               dm.loadProfile(defaultProfile);
               return dm;
            }
            return dm;
         }

         protected void setUp() throws Exception
         {
            super.setUp();
            deploymentException = null;

            try
            {
               this.delegate.init();

               // no secure handling

               if (jarNames == null)
                  return;

               String[] names = jarNames.split(",");
               for (String name : names)
               {
                  DeploymentProgress distribute = getDeploymentManager().distribute(name, getManagedURL(name), copyContent);
                  //distribute.addProgressListener(LOG_PROGRESS_LISTENER);
                  distribute.run();
                  checkProgress(distribute);

                  deploymentNames.addAll(Arrays.asList(distribute.getDeploymentID().getRepositoryNames()));
               }
               
               // Check the resolution of repository names
               assertTrue("resolve repsoitory names", 
                     Arrays.asList(getDeploymentManager().getRepositoryNames(names)).containsAll(deploymentNames));
               
               
               DeploymentProgress start = getDeploymentManager().start(deploymentNames.toArray(new String[ deploymentNames.size()]));
               start.run();
               checkProgress(start);

               staticLog.info("Deployed package: " + deploymentNames);
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
               staticLog.error("Caught exception when trying to deploy : " + jarNames, ex);
            }
         }

         protected void tearDown() throws Exception
         {
            if (jarNames == null)
               return;

            DeploymentProgress stop = getDeploymentManager().stop(deploymentNames.toArray(new String[ deploymentNames.size()]));
            stop.run();
            checkProgress(stop);

            DeploymentProgress undeploy = getDeploymentManager().remove(deploymentNames.toArray(new String[ deploymentNames.size()]));
            undeploy.run();
            checkProgress(undeploy);

            // Clear names
            this.deploymentNames.clear();
            
            // no secure handling

            super.tearDown();
         }
      };
   }

   protected static void checkProgress(DeploymentProgress progress) throws Exception
   {
      DeploymentStatus status = progress.getDeploymentStatus();
      Throwable failure = status.getFailure();
      if (failure != null)
      {
         staticLog.debug(status);
         throw new Exception(failure);
      }
   }

   protected static URL getManagedURL(final String filename) throws MalformedURLException
   {
      // First see if it is already a complete url.
      try
      {
         return new URL(filename);
      }
      catch (MalformedURLException e)
      {
         staticLog.debug(filename + " is not a valid URL, " + e.getMessage());
      }

      // OK, lets see if we can figure out what it might be.
      String deployDir = System.getProperty("jbosstest.deploy.dir");
      if (deployDir == null)
      {
         deployDir = "output/lib";
      }
      File deployFile = new File(deployDir);
      staticLog.debug("Testing file: " + deployFile);
      // try to canonicalize the strings a bit.
      File file = new File(deployFile, filename);
      if (file.exists())
      {
         staticLog.debug(file.getAbsolutePath() + " is a valid file");
         return file.toURL();
      }
      else
      {
         staticLog.debug("File does not exist, creating url: " + deployFile);
         return new URL(deployFile.toURL(), filename);
      }
   }

   protected <T> T invokeMainDeployer(String methodName, Object[] args, String[] sig, Class<T> clazz) throws Exception
   {
      if (clazz == null)
         throw new IllegalArgumentException("Null class.");

      MBeanServerConnection server = getServer();
      Object result = server.invoke(MainDeployerMBean.OBJECT_NAME, methodName, args, sig);
      return clazz.cast(result);
   }

   protected String getProfileName()
   {
      return "profileservice";
   }

   protected ManagementView getManagementView() throws Exception
   {
      InitialContext ctx = getInitialContext();
      ProfileService ps = (ProfileService)ctx.lookup("ProfileService");
      ManagementView activeView = ps.getViewManager();
      activeView.load();
      // Init the VFS to setup the vfs* protocol handlers
      VFS.init();
      return activeView;
   }

   protected ManagedDeployment getDeploymentUnit(String deployment) throws Exception
   {
      ManagementView mv = getManagementView();
      return mv.getDeployment(deployment);
   }

   protected ManagedDeployment assertDeployed(String deployment) throws Exception
   {
      ManagedDeployment result = getDeploymentUnit(deployment);
      assertNotNull("Unable to retrieve deployment info for " + deployment, result);
      return result;
   }

   protected void assertDeployed(String deployment, Set expected) throws Exception
   {
      ManagedDeployment topInfo = assertDeployed(deployment);
      CheckExpectedDeploymentInfoVisitor visitor = new CheckExpectedDeploymentInfoVisitor(expected);
      visitor.start(topInfo);
      assertTrue("Expected subdeployments: " + expected, expected.isEmpty());
   }

   @Deprecated
   protected void assertNotDeployed(String deployment) throws Exception
   {
      ManagedDeployment result = getDeploymentUnit(deployment);
      assertNull("Should not be deployed " + result, result);
   }

   protected boolean isDeployed(String deployment) throws Exception
   {
      ManagementView mv = getManagementView();
      ManagedDeployment md = mv.getDeployment(deployment);
      return (md != null);
   }

   protected void assertNoChildContexts(String deployment) throws Exception
   {
      ManagedDeployment unit = getDeploymentUnit(deployment);
      assertChildContexts(unit);
   }

   protected void assertChildContexts(String deployment, String... paths) throws Exception
   {
      ManagedDeployment unit = getDeploymentUnit(deployment);
      assertChildContexts(unit, paths);
   }

   protected void assertChildContexts(ManagedDeployment unit, String... paths)
   {
      List<String> expected = new ArrayList<String>();
      if (paths != null)
         expected.addAll(Arrays.asList(paths));

      List<ManagedDeployment> children = unit.getChildren();
      assertNotNull(children);
      assertEquals("Expected " + expected + " got " + simplePrint(children), expected.size(), children.size());

      for (String path : expected)
      {
         boolean found = false;
         for (ManagedDeployment child : children)
         {
            if (path.equals(child.getSimpleName()))
               found = true;
         }
         if (found == false)
            fail("Expected " + path + " in " + children);
      }
   }

   protected static String simplePrint(List<ManagedDeployment> children)
   {
      StringBuilder builder = new StringBuilder();
      boolean first = false;
      builder.append("[");
      for (ManagedDeployment child : children)
      {
         if (first == false)
            first = true;
         else
            builder.append(", ");
         builder.append(child.getSimpleName());
      }
      builder.append("]");
      return builder.toString();
   }

   public AbstractDeploymentTest(String test)
   {
      super(test);
   }

   public static class DeploymentInfoVisitor
   {
      public void start(ManagedDeployment topLevel)
      {
         doVisit(topLevel);
      }

      protected void doVisit(ManagedDeployment info)
      {
         visit(info);

         List<ManagedDeployment> subDeployments = info.getChildren();
         if (subDeployments == null || subDeployments.size() == 0)
            return;

         for (ManagedDeployment child : subDeployments)
         {
            doVisit(child);
         }
      }

      public void visit(ManagedDeployment info)
      {
      }
   }

   public class CheckExpectedDeploymentInfoVisitor extends DeploymentInfoVisitor
   {
      protected Set expected;

      public CheckExpectedDeploymentInfoVisitor(Set expected)
      {
         this.expected = expected;
      }

      public void visit(ManagedDeployment info)
      {
         String shortName = info.getSimpleName();
         log.info("Found deployment " + shortName);
         boolean found = expected.remove(shortName);
         if (found == false)
            fail(shortName + " not expected, or duplicate?");
         else if (info.getParent() == null) // only top levels
         {
            boolean deployed;
            try
            {
               deployed = isDeployed(info.getSimpleName());
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
            assertTrue("Should be fully deployed: " + shortName, deployed);
         }
      }
   }

   protected static class LogProgressListener implements ProgressListener
   {
      public void progressEvent(ProgressEvent eventInfo)
      {
         staticLog.fatal(eventInfo);
      }
   }

   private static final ProgressListener LOG_PROGRESS_LISTENER = new LogProgressListener();
}