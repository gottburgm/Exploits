/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009 Red Hat, Inc. and individual contributors
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.test.cluster.testutil.JBossCacheUtil;
import org.jboss.test.cluster.testutil.JGroupsSystemPropertySupport;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.mocks.BasicRequestHandler;
import org.jboss.test.cluster.web.mocks.ConcurrentRequestHandler;
import org.jboss.test.cluster.web.mocks.SetAttributesRequestHandler;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * JBAS-7379. Tests that multiple concurrent failover requests for
 * the same session are handled properly.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class ConcurrentFailoverRequestsTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(ConcurrentFailoverRequestsTestCase.class);
   
   private static long testCount = System.currentTimeMillis();
   
   private final JGroupsSystemPropertySupport jgSupport = new JGroupsSystemPropertySupport();
   private Set<PojoCache> caches = new HashSet<PojoCache>();
   
   private ExecutorService threadPool;
   
   /**
    * Create a new ConcurrentFailoverRequestsTestCase.
    * 
    * @param name
    */
   public ConcurrentFailoverRequestsTestCase(String name)
   {
      super(name);
   } 

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      jgSupport.setUpProperties();
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
         jgSupport.restoreProperties();
         
         if (threadPool != null)
         {
            threadPool.shutdownNow();
         }
         
         SessionTestUtil.clearDistributedCacheManagerFactory();
         
         for (PojoCache cache : caches)
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
               log.error("Cache " + cache.getCache().getLocalAddress() + ": " + e.getMessage(), e);
            }
            
            try
            {
               cache.stop();
               cache.destroy();
            }
            catch (Exception e)
            {
               log.error("Cache " + cache.getCache().getLocalAddress() + ": " + e.getMessage(), e);
            }
            
         }
         
         caches.clear();
      }
   }

   public void testConcurrentFailoverRequests() throws Exception
   {
      ++testCount;
      
      JBossWebMetaData webMetaData = SessionTestUtil.createWebMetaData(100);
      String warName = "test" + testCount;
      JBossCacheManager jbcm0 = SessionTestUtil.createManager(warName, 30, false, null, false, false, null, caches);
      jbcm0.init(warName, webMetaData);      
      jbcm0.start();
      
      JBossCacheManager jbcm1 = SessionTestUtil.createManager(warName, 30, false, null, false, false, null, caches);
      jbcm1.init(warName, webMetaData);      
      jbcm1.start();
      
      Cache[] array = new Cache[caches.size()];
      int index = 0;
      for (PojoCache c : caches)
      {
         array[index] = c.getCache();
         index++;
      }
      JBossCacheUtil.blockUntilViewsReceived(array, 10000);
      
      Object value = "0";
      Map<String, Object> attrs = Collections.unmodifiableMap(Collections.singletonMap("count", value));
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(attrs, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      String id1 = setHandler.getSessionId();
      assertNotNull(id1);
      
      // Add a second session that we can check for replication; this is a proxy
      // for checking that first session has replicated
      setHandler = new SetAttributesRequestHandler(attrs, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      String id2 = setHandler.getSessionId();
      assertNotNull(id1);
      
      assertFalse(id1.equals(id2));
      
      // Ensure replication of session 2 has occurred
      boolean found = false;
      for (int i = 0; i < 10; i++)
      {
         BasicRequestHandler getHandler = new BasicRequestHandler(attrs.keySet(), false);
         SessionTestUtil.invokeRequest(jbcm1, getHandler, id2);
         if (getHandler.getCheckedAttributes() != null && value.equals(getHandler.getCheckedAttributes().get("count")))
         {
            found = true;
            break;
         }
         Thread.sleep(50);            
      }
      assertTrue("sessions replicated", found);
      
      jbcm0.stop();
      
      int THREADS = 10;
      threadPool = Executors.newFixedThreadPool(THREADS);
      
      CountDownLatch startingGun = new CountDownLatch(THREADS + 1);
      CountDownLatch finishedSignal = new CountDownLatch(THREADS);
      ConcurrentRequestHandler concurrentHandler = new ConcurrentRequestHandler();
      Valve pipelineHead = SessionTestUtil.setupPipeline(jbcm1, concurrentHandler);
      Loader[] loaders = new Loader[THREADS];
      
      for (int i = 0; i < loaders.length; i++)
      {
         loaders[i] = new Loader(pipelineHead, concurrentHandler, jbcm1, id1, attrs.keySet(), startingGun, finishedSignal);
         threadPool.execute(loaders[i]);
      }
      
      startingGun.countDown();
      
      assertTrue("loaders completed on time", finishedSignal.await(45, TimeUnit.SECONDS));     
      
      for (int i = 0; i < loaders.length; i++)
      {         
         assertNotNull("got checked attributes for " + i, loaders[i].checkedAttributes);
         assertTrue("checked 'count' attribute for " + i, loaders[i].checkedAttributes.containsKey("count"));
         assertEquals("correct value for " + i, value, loaders[i].checkedAttributes.get("count"));
      }
   }
   
   private static class Loader implements Runnable
   {
      private final Valve pipelineHead;
      private final ConcurrentRequestHandler concurrentHandler;
      private final Manager manager;
      private final String sessionId;
      private final Set<String> attributeKeys;
      private final CountDownLatch startingGun;
      private final CountDownLatch finishedSignal;
      
      private Map<String, Object> checkedAttributes;

      private Loader(Valve pipelineHead, ConcurrentRequestHandler concurrentHandler,
            Manager manager, String sessionId, Set<String> attributeKeys, 
            CountDownLatch startingGun, CountDownLatch finishedSignal)
      {
         this.pipelineHead = pipelineHead;
         this.concurrentHandler = concurrentHandler;
         this.manager = manager;
         this.sessionId = sessionId;
         this.attributeKeys = attributeKeys;
         this.startingGun = startingGun;
         this.finishedSignal = finishedSignal;
      }
      
      public void run()
      {
         try
         {
            BasicRequestHandler getHandler = new BasicRequestHandler(attributeKeys, false);
            concurrentHandler.registerHandler(getHandler);
            Request request = SessionTestUtil.setupRequest(manager, sessionId);
            startingGun.countDown();
            startingGun.await();
            System.out.println("started");
            
            SessionTestUtil.invokeRequest(pipelineHead, request);
            this.checkedAttributes = getHandler.getCheckedAttributes();
            if (this.checkedAttributes != null)
            {
               System.out.println(this.checkedAttributes.keySet());
            }
         }
         catch (Exception e)
         {
            e.printStackTrace(System.out);
         }
         finally
         {
            finishedSignal.countDown();
            
            concurrentHandler.unregisterHandler();
         }
         
      }
      
   }
}
