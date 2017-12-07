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

import java.util.HashSet;

import junit.framework.Test;
import org.jboss.test.deployers.AbstractDeploymentTest;
import org.jboss.managed.api.ManagedDeployment;

/**
 * A test that deploys everything in a JavaEE 5 EAR without using an
 * application.xml descriptor.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class EARNoAppXmlDeploymentUnitTestCase extends AbstractDeploymentTest
{

   /**
    * Test that a javaee5 style ear without an application.xml deploys
    */
   public void testDescriptorLessEAR() throws Exception
   {
      final HashSet expected = new HashSet();
      expected.add(earNoAppXml);
      expected.add(bean1Deployment);
      expected.add(web1Deployment);
      expected.add(rar1Deployment);
      expected.add(rarjar1Deployment);
      expected.add(client1Deployment);

      ManagedDeployment topInfo = assertDeployed(earNoAppXml);
      AbstractDeploymentTest.CheckExpectedDeploymentInfoVisitor visitor = new CheckExpectedDeploymentInfoVisitor(expected);
      visitor.start(topInfo);
      assertTrue("Expected subdeployments: " + expected, expected.isEmpty());
   }

   public EARNoAppXmlDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getManagedDeployment(EARNoAppXmlDeploymentUnitTestCase.class, earNoAppXml);
   }
}
