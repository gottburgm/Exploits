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


import static org.jboss.test.cluster.testutil.SessionTestUtil.createManager;
import static org.jboss.test.cluster.testutil.SessionTestUtil.createWebMetaData;
import static org.jboss.test.cluster.testutil.SessionTestUtil.getAttributeValue;
import static org.jboss.test.cluster.testutil.SessionTestUtil.invokeRequest;
import static org.jboss.test.cluster.testutil.SessionTestUtil.sleepThread;

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
import org.jboss.test.cluster.web.mocks.InvalidateSessionRequestHandler;
import org.jboss.test.cluster.web.mocks.SetAttributesRequestHandler;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Unit tests of failover with buddy replication
 * 
 * @author Brian Stansberry
 */
public class BuddyReplicationFailoverUnitTestCase extends TestCase
{
   protected static PojoCache[] pojoCaches = new PojoCache[4];

   protected static long testId = System.currentTimeMillis();
   
   protected Logger log = Logger.getLogger(getClass());   
   
   protected Set<JBossCacheManager> managers = new HashSet<JBossCacheManager>();
   
   public BuddyReplicationFailoverUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      File root = new File(tmpDir, BuddyReplicationFailoverUnitTestCase.class.getSimpleName());
      root.mkdirs();
      root.deleteOnExit();
      return CacheConfigTestSetup.getTestSetup(BuddyReplicationFailoverUnitTestCase.class, pojoCaches, false, root.getAbsolutePath(), false, false);
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
   
   public void testInvalidateOnFailoverToBackup() throws Exception
   {
      log.info("++++ Starting testInvalidateOnFailoverToBackup ++++");
      
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and a maxIdle of 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800000, 1, -1);
      
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(0)), false);
      invokeRequest(mgrs[3], setHandler, null);
      
      String id = setHandler.getSessionId();     
      
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(1)), false);
      invokeRequest(mgrs[3], setHandler, id);      
      assertEquals(getAttributeValue(0), setHandler.getCheckedAttributes().get("count"));
      
      sleepThread(1100); 
      
      mgrs[0].backgroundProcess();  
      mgrs[1].backgroundProcess();
      mgrs[2].backgroundProcess();
      mgrs[3].backgroundProcess();
      
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(2)), false);
      invokeRequest(mgrs[3], setHandler, id);      
      assertEquals(getAttributeValue(1), setHandler.getCheckedAttributes().get("count"));
      
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(3)), false);
      invokeRequest(mgrs[3], setHandler, id);      
      assertEquals(getAttributeValue(2), setHandler.getCheckedAttributes().get("count"));
      
      // Invalidate on the failover request
      InvalidateSessionRequestHandler invalidationHandler = new InvalidateSessionRequestHandler(Collections.singleton("count"), false);
      invokeRequest(mgrs[0], invalidationHandler, id);      
      assertEquals(getAttributeValue(3), invalidationHandler.getCheckedAttributes().get("count"));
      
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(0)), false);
      invokeRequest(mgrs[0], invalidationHandler, id);      
      assertNull(setHandler.getCheckedAttributes().get("count"));
   }
   
   public void testFailoverAndFailBack() throws Exception
   {
      log.info("++++ Starting testFailoverAndFailBack ++++");
      
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and no maxIdle
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800000, -1, -1);
      
      log.info("managers created");
      log.info("creating session");
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(0)), false);
      invokeRequest(mgrs[0], setHandler, null);
      
      String id = setHandler.getSessionId();     
      
      // Modify
      log.info("modifying session");
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(1)), false);
      invokeRequest(mgrs[0], setHandler, id);      
      assertEquals(getAttributeValue(0), setHandler.getCheckedAttributes().get("count"));    
      
      // Failover and modify
      log.info("failing over");
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(2)), false);
      invokeRequest(mgrs[3], setHandler, id);      
      assertEquals(getAttributeValue(1), setHandler.getCheckedAttributes().get("count"));       
      
      // Modify
      log.info("modifying session");
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(3)), false);
      invokeRequest(mgrs[3], setHandler, id);      
      assertEquals(getAttributeValue(2), setHandler.getCheckedAttributes().get("count"));     
      
      // Failback and modify
      log.info("failing back");
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(4)), false);
      invokeRequest(mgrs[0], setHandler, id);      
      assertEquals(getAttributeValue(3), setHandler.getCheckedAttributes().get("count"));  
      
      // Invalidate 
      log.info("invalidating");
      InvalidateSessionRequestHandler invalidationHandler = new InvalidateSessionRequestHandler(Collections.singleton("count"), false);
      invokeRequest(mgrs[0], invalidationHandler, id);      
      assertEquals(getAttributeValue(4), invalidationHandler.getCheckedAttributes().get("count"));
      
      // Reestablish
      log.info("re-establishing");
      setHandler = new SetAttributesRequestHandler(Collections.singletonMap("count", getAttributeValue(0)), false);
      invokeRequest(mgrs[0], invalidationHandler, id);      
      assertNull(setHandler.getCheckedAttributes().get("count"));      
   }
   
   protected JBossCacheManager[] getCacheManagers(String warname, int maxInactive, int maxIdle, int maxUnreplicated)
      throws Exception
   {
      JBossCacheManager[] result = new JBossCacheManager[pojoCaches.length];
      for (int i = 0; i < pojoCaches.length; i++)
      {
         JBossCacheManager jbcm = createManager(warname, maxInactive, pojoCaches[i], null);
         JBossWebMetaData metadata = createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, true, maxIdle, -1, true, maxUnreplicated);
         jbcm.init(warname, metadata);
         this.managers.add(jbcm);
         jbcm.start();
         result[i] = jbcm;
      }
      
      return result;
   }

}
