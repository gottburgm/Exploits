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

import org.jboss.test.JBossTestCase;

/**
 * Test deployments that should be accepted/rejected by the JARDeployer
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class DeployJarUnitTestCase extends JBossTestCase
{
   /**
    * CTOR
    */
   public DeployJarUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Deploy a valid jar file
    */
   public void testDeployValidJar() throws Exception
   {
      String module = "deployjar-valid.jar";
      try
      {
         deploy(module);
      }
      finally
      {
         undeploy(module);
      }
   }

   /**
    * Deploy a valid jar file with a META-INF/subdir/empty-jboss-service.xml file
    * see JBAS-2949
    */
   public void testDeployValidNestedJar() throws Exception
   {
      String module = "deployjar-valid-nested.jar";
      try
      {
         deploy(module);
      }
      finally
      {
         undeploy(module);
      }
   }
   
   /**
    * Deploy an invalid jar file with a META-INF/empty-jboss-service.xml file
    * 
    * This should be rejected as a non-jar deployment (thus waiting for a deployer)
    * because empty-jboss-service.xml matches either the default ".xml" or "jboss-service.xml"
    * defined in in conf/xmdesc/org.jboss.deployment.JARDeployer-xmbean.xml, DescriptorNames.
    */
   public void testDeployInvalidJar() throws Exception
   {
      String module = "deployjar-invalid.jar";
      try
      {
         deploy(module);
         
         // shouldn't reach this point
         fail("Deployed invalid module: " + module);
      }
      catch (Exception e)
      {
         // expected
      }
   }   
}
