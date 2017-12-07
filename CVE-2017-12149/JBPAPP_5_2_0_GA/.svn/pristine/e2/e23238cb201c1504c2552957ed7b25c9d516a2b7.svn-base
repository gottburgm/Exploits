/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.defaultcfg.clusteredentity.test;

import javax.management.MBeanServerConnection;

import org.jboss.test.cluster.testutil.DBSetup;

import junit.framework.Test;

/**
 * Tests EJB redeployment when entities with @Lob fields are stored
 * in the second level cache.
 *
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class EntityQueryRedeployUnitTestCase
extends EntityClassloaderTestBase
{
   public EntityQueryRedeployUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testRedeploy() throws Exception
   {
      // Set things up with the default region
      queryTest(true, true, false, false, false);
      // Now get the named query regions active
      queryTest(false, true, true, true, false);
      
      redeploy();
      
      // Redo the test, but no entity creation
      queryTest(false, true, false, false, false);
      queryTest(false, true, true, true, false);
   }
   
   private void redeploy() throws Exception
   {
      // Get rid of our old sfsb and make sure its listener is removed from cache
      sfsb1.remove(false);
      
      MBeanServerConnection[] adaptors = getAdaptors();
      undeploy(adaptors[1], getEarName() + ".ear");
      
      sleep(2000);
      
      deploy(adaptors[1], getEarName() + ".ear");
      
      sleep(2000);
      
      // Get the SFSB again
      sfsb1 = getEntityQueryTest(System.getProperty("jbosstest.cluster.node1"));
   }
   
   
   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(EntityQueryRedeployUnitTestCase.class, EAR_NAME + ".ear");
   }
}
