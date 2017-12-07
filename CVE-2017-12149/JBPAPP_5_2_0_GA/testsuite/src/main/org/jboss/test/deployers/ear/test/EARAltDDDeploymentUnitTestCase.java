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
package org.jboss.test.deployers.ear.test;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import org.jboss.test.deployers.AbstractDeploymentTest;
import org.jboss.test.deployers.ejb.bean1.Bean1Home;
import org.jboss.test.deployers.ejb.bean1.Bean1Remote;

/**
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 85526 $
 */
public class EARAltDDDeploymentUnitTestCase extends AbstractDeploymentTest
{
   public static Test suite() throws Exception
   {
      return getManagedDeployment(EARAltDDDeploymentUnitTestCase.class, earAltDDDeployment);
   }

   public EARAltDDDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public void testEARDeployment() throws Exception
   {
/* TODO: this doesn't work currently
 * java.io.NotSerializableException: java.io.ByteArrayInputStream
 * ...
    at org.jboss.test.deployers.AbstractDeploymentTest.invokeMainDeployer(AbstractDeploymentTest.java:80)
    at org.jboss.test.deployers.AbstractDeploymentTest.getDeploymentUnit(AbstractDeploymentTest.java:111)
    at org.jboss.test.deployers.AbstractDeploymentTest.assertDeployed(AbstractDeploymentTest.java:86)
    at org.jboss.test.deployers.ear.test.EARAltDDDeploymentUnitTestCase.testEARDeployment(EARAltDDDeploymentUnitTestCase.java:58)

      final HashSet<String> expected = new HashSet<String>();
      expected.add(earAltDDDeployment);
      expected.add(bean1Deployment);
      
      DeploymentUnit topInfo = assertDeployed(earAltDDDeployment);
      CheckExpectedDeploymentInfoVisitor visitor = new CheckExpectedDeploymentInfoVisitor(expected);
      visitor.start(topInfo);
      assertTrue("Expected subdeployments: " + expected, expected.isEmpty());
*/      
      Object o = new InitialContext().lookup("Bean1EJB");
      Bean1Home home = (Bean1Home) PortableRemoteObject.narrow(o, Bean1Home.class);
      Bean1Remote bean = home.create();
      assertEquals("bean1-altdd.xml", bean.getEnvEntry("dd-file"));
   }
}
