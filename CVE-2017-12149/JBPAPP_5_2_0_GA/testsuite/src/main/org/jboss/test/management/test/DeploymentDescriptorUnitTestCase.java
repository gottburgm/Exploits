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
package org.jboss.test.management.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * Verify the deployment descriptors are exposed for the various
 * deployed module types.
 * 
 * Currently using existing deployments from ./deploy rathen than
 * creating dummy deployments. If those change too often, we need
 * to add some test deployments here.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class DeploymentDescriptorUnitTestCase extends JBossTestCase
{
   public DeploymentDescriptorUnitTestCase(String name)
   {
      super(name);
   }

   public void testNop() throws Exception
   {
      // remove this when the tests are re-enabled
   }
   
// NYI - see JBAS-5545
//   /**
//    * A -service.xml module (legacy-jboss-service.xml)
//    */
   public void testGetServiceXmlDescriptor() throws Exception
   {
      // conf/jboss-service.xml is deployed before deployers can kick in
      String moduleName = "transaction-service.xml";
      String moduleType = "ServiceModule";

      String descriptor = getDescriptor(moduleType, moduleName);
      assertTrue("Empty or null deployment descriptor: " + descriptor + " for module: " + moduleName,
            descriptor != null && !descriptor.equals(""));
   }
//
//   /**
//    * A .sar module (deploy/jbossweb-tomcat55.sar)
//    */
//   public void testGetSarDescriptor() throws Exception
//   {
//      String moduleName = "jbossweb-tomcat55.sar";
//      String moduleType = "ServiceModule";
//
//      String descriptor = getDescriptor(moduleType, moduleName);
//      assertTrue("Empty or null deployment descriptor: " + descriptor + " for module: " + moduleName,
//            descriptor != null && !descriptor.equals(""));
//   }
//
//   /**
//    * A -deployer.xml module (deploy/ejb-deployer.xml)
//    */
//   public void testGetDeployerXmlDescriptor() throws Exception
//   {
//      String moduleName = "ejb-deployer.xml";
//      String moduleType = "ServiceModule";
//
//      String descriptor = getDescriptor(moduleType, moduleName);
//      assertTrue("Empty or null deployment descriptor: " + descriptor + " for module: " + moduleName,
//            descriptor != null && !descriptor.equals(""));
//   }
//
//   /**
//    * A .deployer module (deploy/jboss-aop.deployer)
//    */
//   public void testGetDeployerDescriptor() throws Exception
//   {
//      String moduleName = "jboss-aop.deployer";
//      String moduleType = "ServiceModule";
//
//      String descriptor = getDescriptor(moduleType, moduleName);
//      assertTrue("Empty or null deployment descriptor: " + descriptor + " for module: " + moduleName,
//            descriptor != null && !descriptor.equals(""));
//   }
   
   /**
    * Get the "deploymentDescriptor" attribute for a deployed module
    */
   private String getDescriptor(String moduleType, String moduleName) throws Exception
   {
      ObjectName target = new ObjectName(
            "jboss.management.local:J2EEServer=Local,j2eeType=" + moduleType + ",name=" + moduleName);
      
      getLog().debug("Getting 'deploymentDescriptor' attribute from " + target);
      
      return (String)getServer().getAttribute(target, "deploymentDescriptor");
   }
}