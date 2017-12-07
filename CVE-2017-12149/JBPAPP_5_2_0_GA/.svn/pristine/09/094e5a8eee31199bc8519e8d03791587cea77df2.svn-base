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

import junit.framework.Test;

import org.jboss.test.cluster.testutil.DBSetup;

/**
 * Tests caching of queries involving custom types.
 *
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class EntityQueryUnitTestCase
extends EntityClassloaderTestBase
{
   private static boolean firstNamedRegionTest = true;
   
   public EntityQueryUnitTestCase(String name)
   {
      super(name);
   }

   public void testManualQueryDefaultRegion() throws Exception
   {
      log.info("+++ start testManualQueryDefaultRegion");
      queryTest(true, false, false, false, false);
      log.info("+++ end testManualQueryDefaultRegion");
   }
   
   public void testManualQueryNamedRegion() throws Exception
   {      
      log.info("+++ start testManualQueryNamedRegion");
      try
      {
         queryTest(true, false, true, firstNamedRegionTest, false);
      }
      finally
      {
         firstNamedRegionTest = false;
      }
      log.info("+++ end testManualQueryNamedRegion");
   }
   
   public void testNamedQueryDefaultRegion() throws Exception
   {    
      log.info("+++ start testNamedQueryDefaultRegion");
      queryTest(true, true, false, false, false);
      log.info("+++ end testNamedQueryDefaultRegion");
   }
   
   public void testNamedQueryNamedRegion() throws Exception
   {      
      log.info("+++ start testNamedQueryNamedRegion");
      try
      {
         queryTest(true, true, true, firstNamedRegionTest, false);
      }
      finally
      {
         firstNamedRegionTest = false;
      }
      log.info("+++ start testNamedQueryNamedRegion");
   }
   
   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(EntityQueryUnitTestCase.class, 
                               EAR_NAME + ".ear");
   }
}
