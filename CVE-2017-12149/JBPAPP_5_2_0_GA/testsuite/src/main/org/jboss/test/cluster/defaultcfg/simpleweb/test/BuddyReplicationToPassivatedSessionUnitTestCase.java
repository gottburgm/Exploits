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

package org.jboss.test.cluster.defaultcfg.simpleweb.test;

import java.io.File;

import junit.framework.Test;

import org.jboss.test.cluster.testutil.CacheConfigTestSetup;

/**
 * ReplicationToPassivatedSessionUnitTestCase with buddy replication enabled.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class BuddyReplicationToPassivatedSessionUnitTestCase extends ReplicationToPassivatedSessionUnitTestCase
{

   /**
    * Create a new BuddyReplicationToPassivatedSessionUnitTestCase.
    * 
    * @param name
    */
   public BuddyReplicationToPassivatedSessionUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      File root = new File(tmpDir, BuddyReplicationToPassivatedSessionUnitTestCase.class.getSimpleName());
      root.mkdirs();
      root.deleteOnExit();
      return CacheConfigTestSetup.getTestSetup(BuddyReplicationToPassivatedSessionUnitTestCase.class, pojoCaches, false, root.getAbsolutePath(), false, false);
   }

}
