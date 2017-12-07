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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServerConnection;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.test.JBossTestCase;

/**
 * Abstract deployment test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 77242 $
 */
@Deprecated
public class OldAbstractDeploymentTest extends JBossTestCase
{
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
   
   protected <T> T invokeMainDeployer(String methodName, Object[] args, String[] sig, Class<T> clazz) throws Exception
   {
      if (clazz == null)
         throw new IllegalArgumentException("Null class.");

      MBeanServerConnection server = getServer();
      Object result = server.invoke(MainDeployerMBean.OBJECT_NAME, methodName, args, sig);
      return clazz.cast(result);
   }

   protected DeploymentUnit assertDeployed(String deployment) throws Exception
   {
      DeploymentUnit result = getDeploymentUnit(deployment);
      assertNotNull("Unable to retrieve deployment info for " + deployment, result);
      return result;
   }

   protected void assertDeployed(String deployment, Set expected) throws Exception
   {
      DeploymentUnit topInfo = assertDeployed(deployment);
      CheckExpectedDeploymentInfoVisitor visitor = new CheckExpectedDeploymentInfoVisitor(expected);
      visitor.start(topInfo);
      assertTrue("Expected subdeployments: " + expected, expected.isEmpty());
   }

   @Deprecated
   protected void assertNotDeployed(String deployment) throws Exception
   {
      DeploymentUnit result = getDeploymentUnit(deployment);
      assertNull("Should not be deployed " + result, result);
   }
   
   protected DeploymentUnit getDeploymentUnit(String deployment) throws Exception
   {
      URL deployURL = getDeployURL(deployment);
      String[] sig = { URL.class.getName() };
      Object[] args = {deployURL};
      return invokeMainDeployer("getDeploymentUnit", args, sig, DeploymentUnit.class);
   }
   
   protected boolean isDeployed(String deployment) throws Exception
   {
      URL deployURL = getDeployURL(deployment);
      String[] sig = { URL.class.getName() };
      Object[] args = {deployURL};
      return invokeMainDeployer("isDeployed", args, sig, Boolean.class);
   }

   protected void assertNoChildContexts(String deployment) throws Exception
   {
      DeploymentUnit unit = getDeploymentUnit(deployment);
      assertChildContexts(unit);
   }

   protected void assertChildContexts(String deployment, String... paths) throws Exception
   {
      DeploymentUnit unit = getDeploymentUnit(deployment);
      assertChildContexts(unit, paths);
   }

   protected void assertChildContexts(DeploymentUnit unit, String... paths)
   {
      List<String> expected = new ArrayList<String>();
      if (paths != null)
      {
         for (String path : paths)
            expected.add(path);
      }
      List<DeploymentUnit> children = unit.getChildren();
      assertNotNull(children);
      assertEquals("Expected " + expected + " got " + simplePrint(children), expected.size(), children.size());

      for (String path : expected)
      {
         boolean found = false;
         for (DeploymentUnit child : children)
         {
            if (path.equals(child.getRelativePath()))
               found = true;
         }
         if (found == false)
            fail("Expected " + path + " in " + children);
      }
   }

   protected static String simplePrint(List<DeploymentUnit> children)
   {
      StringBuilder builder = new StringBuilder();
      boolean first = false;
      builder.append("[");
      for (DeploymentUnit child : children)
      {
         if (first == false)
            first = true;
         else
            builder.append(", ");
         builder.append(child.getRelativePath());
      }
      builder.append("]");
      return builder.toString();
   }

   public OldAbstractDeploymentTest(String test)
   {
      super(test);
   }
   
   public static class DeploymentInfoVisitor
   {
      public void start(DeploymentUnit topLevel)
      {
         doVisit(topLevel);
      }
      
      protected void doVisit(DeploymentUnit info)
      {
         visit(info);

         List<DeploymentUnit> subDeployments = info.getChildren();
         if (subDeployments == null || subDeployments.size() == 0)
            return;

         for (DeploymentUnit child : subDeployments)
         {
            doVisit(child);
         }
      }
      
      public void visit(DeploymentUnit info)
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
      
      public void visit(DeploymentUnit info)
      {
         String shortName = shortNameFromDeploymentName(info.getName());
         log.info("Found deployment " + shortName);
         boolean found = expected.remove(shortName);
         if (found == false)
            fail(shortName + " not expected, or duplicate?");
         else
         {
            boolean deployed;
            try
            {
               deployed = isDeployed(info.getName());
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
            assertTrue("Should be fully deployed: " + shortName, deployed);
         }
      }
   }
   /**
    * A utility method that takes a deployment unit name and strips it down to the base war
    * name without the .war suffix.
    * @param name - the DeploymentUnit name.
    * @return the short name
    */
   public static String shortNameFromDeploymentName(String name)
   {
      String shortName = name.trim();
      String[] parts = name.split("/|!");
      if( parts.length > 1 )
      {
         shortName = parts[parts.length-1];
      }
      return shortName;
   }

}
