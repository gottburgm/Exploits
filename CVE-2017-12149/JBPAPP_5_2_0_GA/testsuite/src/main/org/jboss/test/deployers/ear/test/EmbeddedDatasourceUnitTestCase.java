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
 * JBAS-4548. Tests proper deployment of an ear that includes EJB2 entities 
 * and the datasource they rely upon.
 * 
 * @author <a href="bstansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85526 $
 */
public class EmbeddedDatasourceUnitTestCase extends AbstractDeploymentTest
{
   public static final String jbas4548_ear = "testdeployers-jbas4548.ear";
   public static final String jbas4548_ds = "testdeployers-jbas4548-ds.xml";
   public static final String jbas4548_ejb = "testdeployers-jbas4548ejb.jar";
   
   public void testEARDeployment() throws Exception
   {
      final HashSet expected = new HashSet();
      expected.add(jbas4548_ear);
      expected.add(jbas4548_ds);
      expected.add(jbas4548_ejb);
      
      ManagedDeployment topInfo = assertDeployed(jbas4548_ear);
      CheckExpectedDeploymentInfoVisitor visitor = new AbstractDeploymentTest.CheckExpectedDeploymentInfoVisitor(expected);
      visitor.start(topInfo);
      assertTrue("Expected subdeployments: " + expected, expected.isEmpty());
   }
   
   public EmbeddedDatasourceUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getManagedDeployment(EmbeddedDatasourceUnitTestCase.class, jbas4548_ear);
   }
}
