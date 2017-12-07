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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import org.jboss.cache.Fqn;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.testutil.CacheConfigTestSetup;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.CacheHelper;
import org.jboss.test.cluster.web.mocks.BasicRequestHandler;
import org.jboss.test.cluster.web.mocks.MutableObject;
import org.jboss.test.cluster.web.mocks.SetAttributesRequestHandler;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Tests of handling of ClusteredSession.maxUnreplicatedInterval.  This base
 * test is run with SESSION granularity.
 * 
 * @author Brian Stansberry
 */
public class SessionBasedMaxUnreplicatedIntervalTestCase extends JBossTestCase
{
   protected static PojoCache[] pojoCaches = new PojoCache[2];

   protected static long testId = System.currentTimeMillis();
   
   protected static boolean useBuddyRepl = Boolean.valueOf(System.getProperty("jbosstest.cluster.web.cache.br")).booleanValue();
   
   protected Logger log = Logger.getLogger(getClass());   
   
   protected Set<JBossCacheManager> managers = new HashSet<JBossCacheManager>();
   
   protected Map<String, Object> allAttributes;
   protected Map<String, Object> immutables;
   protected Map<String, Object> mutables;
   
   public SessionBasedMaxUnreplicatedIntervalTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return CacheConfigTestSetup.getTestSetup(SessionBasedMaxUnreplicatedIntervalTestCase.class, pojoCaches, false, null, !useBuddyRepl, false);
   }

   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      allAttributes = new HashMap<String, Object>();
      immutables = new HashMap<String, Object>();
      mutables = new HashMap<String, Object>();
      
      allAttributes.put("IMMUTABLE", "IMMUTABLE");
      immutables.put("IMMUTABLE", "IMMUTABLE");
      
      MutableObject mo = new MutableObject("MUTABLE");
      allAttributes.put("MUTABLE", mo);
      mutables.put("MUTABLE", mo);
      
      allAttributes = Collections.unmodifiableMap(allAttributes);
      immutables = Collections.unmodifiableMap(immutables);
      mutables = Collections.unmodifiableMap(mutables);
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      SessionTestUtil.clearDistributedCacheManagerFactory();
      
      for (JBossCacheManager manager : managers)      
         manager.stop();
      
      managers.clear();
   }
   
   protected ReplicationGranularity getReplicationGranularity()
   {
      return ReplicationGranularity.SESSION;
   }
   
   protected ReplicationTrigger getReplicationTrigger()
   {
      return ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET;
   }
   
   /**
    * Tests that a maxUnreplicatedInterval of 1 second prevents inadvertent
    * session expiration. Test makes a read-only request after the 
    * maxUnreplicatedInterval has passed, waits long enough for the session
    * to expire on the remote node if the read-only request didn't trigger a 
    * timestamp replication, and then accesses the session on the 2nd node to
    * confirm that the session is still alive.
    * 
    * @throws Exception
    */
   public void testBasicMaxIntervalPreventsExpiration() throws Exception
   {
      log.info("++++ Starting testBasicMaxIntervalPreventsExpiration ++++");
      
      maxIntervalPreventsExpirationTest(false);
   }
   
   /**
    * Tests that the override maxUnreplicatedInterval of 0 prevents inadvertent
    * session expiration. Test makes a read-only request after one second 
    * has passed, waits long enough for the session
    * to expire on the remote node if the read-only request didn't trigger a 
    * timestamp replication, and then accesses the session on the 2nd node to
    * confirm that the session is still alive.
    * 
    * @throws Exception
    */
   public void testZeroMaxIntervalPreventsExpiration() throws Exception
   {
      log.info("++++ Starting testZeroMaxIntervalPreventsExpiration ++++");
      
      maxIntervalPreventsExpirationTest(true);
   }
   
   private void maxIntervalPreventsExpirationTest(boolean testZero) throws Exception
   {
      String warname = String.valueOf(++testId);
      
      int maxUnrep = testZero ? 0 : 1;
      
      // A war with a maxInactive of 3 secs and a maxUnreplicated of 0 or 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 3, maxUnrep);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      // Establish session.
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(allAttributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      validateNewSession(setHandler);
      
      Thread.sleep(1050);
      
      // Now make a request that will not trigger replication unless the interval is exceeded
      BasicRequestHandler getHandler = new BasicRequestHandler(immutables.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm0, getHandler, setHandler.getSessionId());
      
      validateExpectedAttributes(immutables, getHandler);
      
      // Sleep long enough that the session will be expired on other server
      // if previous request didn't keep it alive
      Thread.sleep(2000);
      
      // Fail over and confirm all is well
      getHandler = new BasicRequestHandler(allAttributes.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm1, getHandler, setHandler.getSessionId());
      
      validateExpectedAttributes(allAttributes, getHandler);
   }
   
   /**
    * Tests that setting a maxUnreplicatedInterval of 1 second prevents
    * timestamp replication during read-only requests during that one second. 
    * Test makes a read-only request during the 1 second maxUnreplicatedInterval,
    * which should prevent timestamp replication. Test then waits long enough
    * for the session to be considered expired on the remote node only if the
    * timestamp wasn't replicated. Test fails over to remote node and confirms
    * that the session was expired.
    * 
    * @throws Exception
    */
   public void testMaxIntervalPreventsReplication() throws Exception
   {
      log.info("++++ Starting testMaxIntervalPreventsReplication ++++");
      
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 3 secs and a maxUnreplicated of 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 3, 1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(allAttributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      // Sleep less than the maxUnreplicated time so next request shouldn't trigger timestamp repl
      Thread.sleep(500);
      
      // Now make a request that will not trigger replication unless the interval is exceeded
      BasicRequestHandler getHandler = new BasicRequestHandler(immutables.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm0, getHandler, setHandler.getSessionId());
      
      validateExpectedAttributes(immutables, getHandler);
      
      // Sleep long enough that the session will be expired on other server
      // if previous request didn't keep it alive
      Thread.sleep(2600);
      
      // Fail over and confirm the session was expired
      getHandler = new BasicRequestHandler(allAttributes.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm1, getHandler, setHandler.getSessionId());
      
      validateNewSession(getHandler);
   }
   
   /**
    * Confirms that the "grace period" that maxUnreplicatedInterval adds to the
    * removal of overaged unloaded sessions in remote caches delays their
    * removal.
    * 
    * @throws Exception
    */
   public void testRemoteExpirationGracePeriod() throws Exception
   {
      log.info("++++ Starting testRemoteExpirationGracePeriod ++++");
      
      String warname = String.valueOf(++testId);
      
      JBossCacheManager[] mgrs = getCacheManagers(warname, 3, 2);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      SetAttributesRequestHandler setHandler1 = new SetAttributesRequestHandler(allAttributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler1, null);
      
      Fqn session1Fqn = Fqn.fromString(SessionTestUtil.getSessionFqn(warname, setHandler1.getSessionId()));
      
      SetAttributesRequestHandler setHandler2 = new SetAttributesRequestHandler(allAttributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler2, null);
      
      Fqn session2Fqn = Fqn.fromString(SessionTestUtil.getSessionFqn(warname, setHandler2.getSessionId()));
      
      // Overage the sessions
      Thread.sleep(3010);
      // Try to force out the overaged sessions
      jbcm1.backgroundProcess();
      // Confirm they are still there
      // FIXME -- avoid direct cache operations
      assertNotNull(pojoCaches[1].getCache().get(session1Fqn, CacheHelper.VERSION_KEY));
      assertNotNull(pojoCaches[1].getCache().get(session2Fqn, CacheHelper.VERSION_KEY));
      
      // Access one to prove it gets expired once the manager can see its real timestamp
      BasicRequestHandler getHandler = new BasicRequestHandler(allAttributes.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm1, getHandler, setHandler1.getSessionId());      
      validateNewSession(getHandler);
      
      // Sleep past the grace period
      Thread.sleep(2010);
      // The get restored a new fresh session with the first id, but the 2nd 
      // one is still there and overaged. Try to force it out
      jbcm1.backgroundProcess();
      // FIXME -- avoid direct cache operations
      assertNull(pojoCaches[1].getCache().get(session2Fqn, CacheHelper.VERSION_KEY));
   }
   
   protected JBossCacheManager[] getCacheManagers(String warname, int maxInactive, int maxUnreplicated)
      throws Exception
   {
      JBossCacheManager jbcm0 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[0], null);
      JBossWebMetaData metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), true, maxUnreplicated);
      jbcm0.init(warname, metadata);
      this.managers.add(jbcm0);
      jbcm0.start();
      
      JBossCacheManager jbcm1 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[1], null);
      metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), true, maxUnreplicated);
      jbcm1.init(warname, metadata);
      this.managers.add(jbcm1);
      jbcm1.start();
      
      return new JBossCacheManager[]{jbcm0, jbcm1};
   }
   
   protected void validateExpectedAttributes(Map<String, Object> expected, BasicRequestHandler handler)
   {
      assertFalse(handler.isNewSession());
      
      if (handler.isCheckAttributeNames())
      {
         assertEquals(expected.size(), handler.getAttributeNames().size());
      }
      Map<String, Object> checked = handler.getCheckedAttributes();
      assertEquals(expected.size(), checked.size());
      for (Map.Entry<String, Object> entry : checked.entrySet())
         assertEquals(entry.getKey(), expected.get(entry.getKey()), entry.getValue());
      
   }
   
   protected void validateNewSession(BasicRequestHandler handler)
   {
      assertTrue(handler.isNewSession());
      assertEquals(handler.getCreationTime(), handler.getLastAccessedTime());
      if (handler.isCheckAttributeNames())
      {
         assertEquals(0, handler.getAttributeNames().size());
      }
      Map<String, Object> checked = handler.getCheckedAttributes();
      for (Map.Entry<String, Object> entry : checked.entrySet())
         assertNull(entry.getKey(), entry.getValue());
   }
   

}
