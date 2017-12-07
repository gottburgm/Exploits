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
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EmptyMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.PassivationConfig;
import org.jboss.metadata.web.jboss.ReplicationConfig;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.metadata.web.jboss.SnapshotMode;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Unit tests of session count management.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 92250 $
 */
public class JBossCacheManagerConfigurationUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(JBossCacheManagerConfigurationUnitTestCase.class);
   
   private static long testCount = System.currentTimeMillis();

   private Set<PojoCache> caches = new HashSet<PojoCache>();
   
   /**
    * Create a new SessionCountUnitTestCase.
    * 
    * @param name
    */
   public JBossCacheManagerConfigurationUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      SessionTestUtil.clearDistributedCacheManagerFactory();
      
      for (PojoCache cache : caches)
      { 
         if (CacheStatus.STARTED.equals(cache.getCache().getCacheStatus()))
         {
            // Try to clean up so we avoid loading sessions 
            // from storage in later tests
            try
            {
               log.info("Removing /JSESSION from " + cache.getCache().getLocalAddress());
               cache.getCache().removeNode(Fqn.fromString("/JSESSION"));
            }
            catch (Exception e)
            {
               log.error("Cache " + cache + ": " + e.getMessage(), e);
            }
         }
         
         try
         {
            cache.stop();
            cache.destroy();
         }
         catch (Exception e)
         {
            log.error("Cache " + cache + ": " + e.getMessage(), e);
         }
      }
      
      caches.clear();
   }
   
   public void testUseJK() throws Exception
   {
      log.info("Enter testUseJK");
      
      ++testCount;
      JBossCacheManager jbcm = SessionTestUtil.createManager("test" + testCount, 5, true, null, false, false, null, caches);
      PojoCache cache = SessionTestUtil.getDistributedCacheManagerFactoryPojoCache();
      JBossWebMetaData webMetaData = createWebMetaData(null, null, null, null, null);
      jbcm.init("test.war", webMetaData);      
      jbcm.start();

      assertFalse("With no config, not using JK", jbcm.getUseJK());
      
      cleanupManager(jbcm, cache);
      
      jbcm = SessionTestUtil.createManager("test" + ++testCount, 5, true, null, false, false, null, caches);
      cache = SessionTestUtil.getDistributedCacheManagerFactoryPojoCache();
      
      webMetaData = createWebMetaData(null, null, null, null, Boolean.TRUE);
      jbcm.init("test.war", webMetaData);      
      jbcm.start();

      assertTrue("With no jvmRoute but a config, using JK", jbcm.getUseJK());
      
      cleanupManager(jbcm, cache);
      
      jbcm = SessionTestUtil.createManager("test" + ++testCount, 5, true, null, false, false, "test", caches);
      cache = SessionTestUtil.getDistributedCacheManagerFactoryPojoCache();
      
      webMetaData = createWebMetaData(null, null, null, null, null);
      jbcm.init("test.war", webMetaData);      
      jbcm.start();

      assertTrue("With jvmRoute set, using JK", jbcm.getUseJK());
      
      cleanupManager(jbcm, cache);
      
      jbcm = SessionTestUtil.createManager("test" + ++testCount, 5, true, null, false, false, "test", caches);
      cache = SessionTestUtil.getDistributedCacheManagerFactoryPojoCache();
      
      webMetaData = createWebMetaData(null, null, null, null, Boolean.FALSE);
      jbcm.init("test.war", webMetaData);      
      jbcm.start();

      assertFalse("With a jvmRoute but config=false, not using JK", jbcm.getUseJK());
      
      cleanupManager(jbcm, cache);      
   }
   
   public void testSnapshot() throws Exception
   {
      log.info("Enter testSnapshot");
      
      ++testCount;
      JBossCacheManager jbcm = SessionTestUtil.createManager("test" + testCount, 5, true, null, false, false, null, caches);
      PojoCache cache = SessionTestUtil.getDistributedCacheManagerFactoryPojoCache();
      
      JBossWebMetaData webMetaData = createWebMetaData(null, null, null, null, null);
      jbcm.init("test.war", webMetaData);      
      jbcm.start();

      assertEquals("With no config, using instant", SnapshotMode.INSTANT, jbcm.getSnapshotMode());
      
      cleanupManager(jbcm, cache);
      
      jbcm = SessionTestUtil.createManager("test" + ++testCount, 5, true, null, false, false, null, caches);
      cache = SessionTestUtil.getDistributedCacheManagerFactoryPojoCache();
      
      webMetaData = createWebMetaData(null, null, null, null, Boolean.TRUE);
      webMetaData.getReplicationConfig().setSnapshotMode(SnapshotMode.INTERVAL);
      webMetaData.getReplicationConfig().setSnapshotInterval(new Integer(2));
      jbcm.init("test.war", webMetaData);      
      jbcm.start();

      assertEquals("With config, using interval", SnapshotMode.INTERVAL, jbcm.getSnapshotMode());
      assertEquals("With config, using 2 second interval", 2, jbcm.getSnapshotInterval());
      
      cleanupManager(jbcm, cache);
      
   }
   
   private void cleanupManager(JBossCacheManager mgr, PojoCache cache) throws Exception
   {
      mgr.stop();
      cache.stop();
      cache.destroy();
   }
   
   private JBossWebMetaData createWebMetaData(Integer maxSessions, 
                                              Boolean passivation,
                                              Integer maxIdle, 
                                              Integer minIdle,
                                              Boolean useJK)
   {
      JBossWebMetaData webMetaData = new JBossWebMetaData();
      webMetaData.setDistributable(new EmptyMetaData());
      webMetaData.setMaxActiveSessions(maxSessions);
      PassivationConfig pcfg = new PassivationConfig();
      pcfg.setUseSessionPassivation(passivation);
      pcfg.setPassivationMaxIdleTime(maxIdle);
      pcfg.setPassivationMinIdleTime(minIdle);
      webMetaData.setPassivationConfig(pcfg);
      ReplicationConfig repCfg = new ReplicationConfig();
      repCfg.setReplicationGranularity(ReplicationGranularity.SESSION);
      repCfg.setReplicationTrigger(ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET);
      repCfg.setUseJK(useJK);
      webMetaData.setReplicationConfig(repCfg);
      return webMetaData;
   }
}
