/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.defaultcfg.profileservice.test;

import java.net.URL;

import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.test.profileservice.test.AbstractProfileServiceTest;

/**
 * Profile service DeploymentManager tests in a clustered environment
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class ClusteredDeployUnitTestCase extends AbstractProfileServiceTest
   implements ProgressListener
{
   private ProgressEvent eventInfo;
   private long eventCount = 0;

   public ClusteredDeployUnitTestCase(String name)
   {
      super(name);
   }

   public void progressEvent(ProgressEvent eventInfo)
   {
      eventCount ++;
      this.eventInfo = eventInfo;
      getLog().debug(eventInfo);
   }

   @Override
   protected String getProfileName()
   {
      return "all";
   }

   public void testWarDeployment()
      throws Exception
   {
      DeploymentManager mgtView = getDeploymentManager();
      URL contentURL = super.getDeployURL("testWarDeployment.war");
      DeploymentProgress progress = mgtView.distribute("testWarDeployment.war", contentURL, true);
      assertEquals("DeploymentProgress.getDeploymentTargets", 2, progress.getDeploymentTargets().size());
   }
   public void testEarDeployment()
      throws Exception
   {
      DeploymentManager mgtView = getDeploymentManager();
      URL contentURL = super.getDeployURL("testEarDeployment.ear");
      DeploymentProgress progress = mgtView.distribute("testEarDeployment.ear", contentURL, true);
      assertEquals("DeploymentProgress.getDeploymentTargets", 2, progress.getDeploymentTargets().size());
   }
   public void testMCBeansDeployment()
      throws Exception
   {
      URL contentURL = super.getDeployURL("testMCBeansDeployment.beans");

      // Distribute the content
      DeploymentManager mgtView = getDeploymentManager();
      DeploymentProgress progress = mgtView.distribute("testMCBeansDeployment.beans", contentURL, true);
      assertEquals("DeploymentProgress.getDeploymentTargets", 2, progress.getDeploymentTargets().size());
      progress.addProgressListener(this);
      progress.run();
      DeploymentStatus status = progress.getDeploymentStatus();
      assertTrue("DeploymentStatus.isCompleted", status.isCompleted());
      // It should not be running yet
      assertFalse("DeploymentStatus.isRunning", status.isRunning());
      assertFalse("DeploymentStatus.isFailed", status.isFailed());

      // Now start the deployment
      String[] names = {"testMCBeansDeployment.beans"};
      progress = mgtView.start(names);
      assertEquals("DeploymentProgress.getDeploymentTargets", 2, progress.getDeploymentTargets().size());
      progress.addProgressListener(this);
      progress.run();
      status = progress.getDeploymentStatus();
      assertTrue("DeploymentStatus.isCompleted", status.isCompleted());
      assertTrue("DeploymentStatus.isRunning", status.isRunning());
      assertFalse("DeploymentStatus.isFailed", status.isFailed());      
   }
   public void testSarDeployment()
      throws Exception
   {
      DeploymentManager mgtView = getDeploymentManager();
      URL contentURL = super.getDeployURL("testSarDeployment.sar");
      DeploymentProgress progress = mgtView.distribute("testSarDeployment.sar", contentURL, true);
      assertEquals("DeploymentProgress.getDeploymentTargets", 2, progress.getDeploymentTargets().size());
   }
}
