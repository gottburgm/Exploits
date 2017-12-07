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
package org.jboss.test.deployers.jbas2904.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import javax.management.ObjectName;

import junit.framework.Test;
import org.jboss.test.deployers.AbstractDeploymentTest;
import org.jboss.managed.api.ManagedDeployment;

/**
 * A test for JBAB2904
 * 
 * @author <a href="vicky.kak@jboss.com">Vicky Kak</a>
 * @version $Revision: 85526 $
 */
public class EARDeploymentUnitTestCase extends AbstractDeploymentTest
{

   public EARDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getManagedDeployment(EARDeploymentUnitTestCase.class,
            "testdeployers-jbas2904.ear");
   }

   public void testEARDeployment() throws Exception
   {
      ManagedDeployment topInfo = assertDeployed("testdeployers-jbas2904.ear");
      // Expected ordering of the subdeployments
      List<ManagedDeployment> actualModules = topInfo.getChildren();
      List<String> expectedList = new ArrayList<String>(actualModules.size());
      System.out.println("Expected Deployment Ordering -------> ");
      for (ManagedDeployment child : actualModules)
      {
         String moduleName = child.getSimpleName();
         expectedList.add(moduleName);
         // System.out.println(" "+moduleName+" lastDeployed
         // :"+child.lastDeployed);
      }
      // Expected Deployment Ordering Array
      Object expectedModules[] = expectedList.toArray();

      Deployments deployment1 = new Deployments(
            "Atestdeployerorder-jbas2904.sar",
            getMBeanLastDeployment(new ObjectName("jboss.jbas2904.test.deployers:service=MBean1"))
      );
      Deployments deployment2 = new Deployments(
            "Btestdeployerorder-jbas2904.sar",
            getMBeanLastDeployment(new ObjectName("jboss.jbas2904.test.deployers:service=MBean2"))
      );
      List<Deployments> precisionDeployments = new ArrayList<Deployments>();
      precisionDeployments.add(deployment1);
      precisionDeployments.add(deployment2);
      DeploymentInfoComparator comparator = new DeploymentInfoComparator();
      Collections.sort(precisionDeployments, comparator);
      // Actual Deployment Ordering Array
      Object preciseModulesExpected[] = precisionDeployments.toArray();
      assertEquals(expectedModules.length, preciseModulesExpected.length);

      System.out.println("Final   Deployment Ordering ------->");
      for (int count = 0; count < preciseModulesExpected.length; count++)
      {
         Deployments deployment = (Deployments) preciseModulesExpected[count];
         System.out.println("	" + deployment.getDuName() + " : " + deployment.getLastDeployed());
         String expectedModule = ((Deployments) preciseModulesExpected[count]).getDuName();
         assertEquals(expectedModules[count], expectedModule);
      }
   }

   private String getModuleDeployed(String tempUrl)
   {
      StringTokenizer st = new StringTokenizer(tempUrl, "/");
      String moduleName = "";
      while (st.hasMoreTokens())
      {
         moduleName = st.nextToken();
      }
      return moduleName;
   }

   private long getMBeanLastDeployment(ObjectName objName) throws Exception
   {
      String method = "getLastDeployed";
      Object args[] = {};
      String[] sig = {};
      return (Long) invoke(objName, method, args, sig);
   }
}

class Deployments
{
   private String duName;

   private long lastDeployed;

   public Deployments(String duName, long lastDeployed)
   {
      this.duName = duName;
      this.lastDeployed = lastDeployed;
   }

   public String getDuName()
   {
      return duName;
   }

   public long getLastDeployed()
   {
      return lastDeployed;
   }
}

class DeploymentInfoComparator implements Comparator<Deployments>
{
   public int compare(Deployments o1, Deployments o2)
   {
      long deployedTime1 = o1.getLastDeployed();
      long deployedTime2 = o2.getLastDeployed();
      long timediff = deployedTime1 - deployedTime2;
      int retVal = 0;
      if (timediff > 0)
         retVal = 1;
      else if (timediff < 0)
         retVal = -1;
      return retVal;
   }
}