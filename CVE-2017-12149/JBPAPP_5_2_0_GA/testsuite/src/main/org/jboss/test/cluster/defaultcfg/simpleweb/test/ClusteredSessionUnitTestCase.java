/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009 Red Hat Middleware, Inc. and individual contributors
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

import junit.framework.TestCase;

import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.test.cluster.testutil.JGroupsSystemPropertySupport;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.mocks.MockDistributedCacheManagerFactory;
import org.jboss.web.tomcat.service.session.ClusteredSession;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Unit tests of {@link ClusteredSession}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ClusteredSessionUnitTestCase extends TestCase
{
   /**
    * Create a new ClusteredSessionUnitTestCase.
    * 
    * @param name
    */
   public ClusteredSessionUnitTestCase(String name)
   {
      super(name);
   }
   
   /**
    * Validates the behavior of isOutdated() with respect to returning
    * true until a creation time is set.
    * <p>
    * Note: the use of creation time is a convenience; it's just a field that
    * isn't set at construction but rather after the session is either loaded
    * from the distributed cache or is added as a brand new session.
    * 
    * @throws Exception
    */
   public void testNewSessionIsOutdated() throws Exception
   {
      JBossCacheManager mgr = new JBossCacheManager(new MockDistributedCacheManagerFactory());
      SessionTestUtil.setupContainer("test", null, mgr);
      mgr.start();
      
      mgr.setReplicationGranularity(ReplicationGranularity.SESSION);
      ClusteredSession sess = (ClusteredSession) mgr.createEmptySession();
      assertTrue(sess.isOutdated());
      sess.setCreationTime(System.currentTimeMillis());
      assertFalse(sess.isOutdated());
      
      mgr.setReplicationGranularity(ReplicationGranularity.ATTRIBUTE);
      sess = (ClusteredSession) mgr.createEmptySession();
      assertTrue(sess.isOutdated());
      sess.setCreationTime(System.currentTimeMillis());
      assertFalse(sess.isOutdated());
      
      mgr.setReplicationGranularity(ReplicationGranularity.FIELD);
      sess = (ClusteredSession) mgr.createEmptySession();
      assertTrue(sess.isOutdated());
      sess.setCreationTime(System.currentTimeMillis());
      assertFalse(sess.isOutdated());
   }

}
