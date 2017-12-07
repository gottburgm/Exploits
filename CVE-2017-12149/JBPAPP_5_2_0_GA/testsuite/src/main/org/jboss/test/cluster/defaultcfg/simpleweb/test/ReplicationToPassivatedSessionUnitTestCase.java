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

package org.jboss.test.cluster.defaultcfg.simpleweb.test;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.test.cluster.testutil.CacheConfigTestSetup;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.mocks.SetAttributesRequestHandler;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Unit tests of session expiration
 * 
 * @author Brian Stansberry
 */
public class ReplicationToPassivatedSessionUnitTestCase extends TestCase
{
   protected static PojoCache[] pojoCaches = new PojoCache[2];

   protected static long testId = System.currentTimeMillis();
   
   protected Logger log = Logger.getLogger(getClass());   
   
   protected Set<JBossCacheManager> managers = new HashSet<JBossCacheManager>();
   
   public ReplicationToPassivatedSessionUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      File root = new File(tmpDir, ReplicationToPassivatedSessionUnitTestCase.class.getSimpleName());
      root.mkdirs();
      root.deleteOnExit();
      return CacheConfigTestSetup.getTestSetup(ReplicationToPassivatedSessionUnitTestCase.class, pojoCaches, false, root.getAbsolutePath(), true, false);
   }

   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {         
         SessionTestUtil.clearDistributedCacheManagerFactory();
         
         for (JBossCacheManager manager : managers)      
            manager.stop();
         
         managers.clear();
      }
   }
   
   protected ReplicationGranularity getReplicationGranularity()
   {
      return ReplicationGranularity.SESSION;
   }
   
   protected ReplicationTrigger getReplicationTrigger()
   {
      return ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET;
   }
   
   public void testReplicationToPassivatedSession() throws Exception
   {
      log.info("++++ Starting testReplicationToPassivatedSession ++++");
      
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and a maxIdle of 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800000, 1, -1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      Object value = "0";
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", value), false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      String id = setHandler.getSessionId();
      
      SessionTestUtil.sleepThread(1100); 
      
      jbcm0.backgroundProcess();
      jbcm1.backgroundProcess();     
      
      value = "1";
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", value), false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, id);
      
      assertEquals("0", setHandler.getCheckedAttributes().get("count"));
      
      value = "2";
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", value), false);
      SessionTestUtil.invokeRequest(jbcm1, setHandler, id);
      
      assertEquals("1", setHandler.getCheckedAttributes().get("count"));
   }
   
   public void testFailoverToPassivatedSession() throws Exception
   {
      log.info("++++ Starting testFailoverToPassivatedSession ++++");
      
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and a maxIdle of 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800000, 1, -1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      Object value = "0";
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", value), false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      String id = setHandler.getSessionId();
      
      SessionTestUtil.sleepThread(1100); 
      
      jbcm0.backgroundProcess();
      jbcm1.backgroundProcess();     
      
      value = "1";
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", value), false);
      SessionTestUtil.invokeRequest(jbcm1, setHandler, id);
      
      assertEquals("0", setHandler.getCheckedAttributes().get("count"));
   }
   
   protected JBossCacheManager[] getCacheManagers(String warname, int maxInactive, int maxIdle, int maxUnreplicated)
      throws Exception
   {
      JBossCacheManager jbcm0 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[0], null);
      JBossWebMetaData metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, true, maxIdle, -1, true, maxUnreplicated);
      jbcm0.init(warname, metadata);
      this.managers.add(jbcm0);
      jbcm0.start();
      
      JBossCacheManager jbcm1 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[1], null);
      metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, true, maxIdle, -1, true, -1);
      jbcm1.init(warname, metadata);
      this.managers.add(jbcm1);
      jbcm1.start();
      
      return new JBossCacheManager[]{jbcm0, jbcm1};
   }

}
