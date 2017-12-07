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
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import junit.framework.Test;

import org.apache.catalina.Session;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.testutil.CacheConfigTestSetup;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.mocks.BasicRequestHandler;
import org.jboss.test.cluster.web.mocks.InvalidateSessionRequestHandler;
import org.jboss.test.cluster.web.mocks.RemoveAttributesRequestHandler;
import org.jboss.test.cluster.web.mocks.SetAttributesRequestHandler;
import org.jboss.test.cluster.web.notification.SessionSpecListenerAttribute;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Tests that references to cached sessions and attributes are released. 
 * 
 * @author Brian Stansberry
 */
public class ClusteredSessionMemoryLeakTestCase extends JBossTestCase
{
   protected static PojoCache[] pojoCaches = new PojoCache[2];

   protected static long testId = System.currentTimeMillis();
   
   public static final String KEY = "Key";
   public static final Set<String> KEYS = new HashSet<String>();
   static
   {
      KEYS.add(KEY);
   }
   
   protected Logger log = Logger.getLogger(getClass());   
   
   protected Set<JBossCacheManager> managers = new HashSet<JBossCacheManager>();
   
   public ClusteredSessionMemoryLeakTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      File root = new File(tmpDir, ClusteredSessionMemoryLeakTestCase.class.getSimpleName());
      root.mkdirs();
      root.deleteOnExit();
      return CacheConfigTestSetup.getTestSetup(ClusteredSessionMemoryLeakTestCase.class, pojoCaches, false, root.getAbsolutePath(), true, false);
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      for (JBossCacheManager manager : managers)    
      {
         try
         {
            manager.stop();
         }
         catch (RuntimeException ignored)
         {
            log.debug("tearDown(): Caught exception cleaning up manager -- " + ignored.getLocalizedMessage()); 
         }
      }
      managers.clear();
      
      SessionSpecListenerAttribute.invocations.clear();
      
      Attribute.clear();
      System.gc();
   }
   
   protected ReplicationGranularity getReplicationGranularity()
   {
      return ReplicationGranularity.SESSION;
   }
   
   protected ReplicationTrigger getReplicationTrigger()
   {
      return ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET;
   }
   
   public void testSessionLifecycle() throws Exception
   {
      log.info("++++ Starting testSessionLifecycle ++++");
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins maxUnreplicated of 0
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800, 1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("initial request");
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);      
      
      String sessionId = setHandler.getSessionId();
      WeakReference<Session> session0A = new WeakReference<Session>(jbcm0.findSession(sessionId));      
      SessionTestUtil.cleanupPipeline(jbcm0);
      assertNotNull(session0A.get());
      
      // Modify attribute request
      setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("Modify attribute request");
      SessionTestUtil.invokeRequest(jbcm0, setHandler, sessionId);      
      SessionTestUtil.cleanupPipeline(jbcm0);
      
      System.gc(); 
      System.runFinalization();
      assertEquals(1, Attribute.attributeCount());
      
      // Passivate
      Thread.sleep(1100);
      
      log.info("passivate node 0");
      jbcm0.backgroundProcess();
      log.info("passivate node 1");
      jbcm1.backgroundProcess();
      
      System.gc(); 
      System.runFinalization();
      assertEquals(0, Attribute.attributeCount());
      assertNull(session0A.get());
      
      // Remove attribute request
      RemoveAttributesRequestHandler removeHandler = new RemoveAttributesRequestHandler(KEYS, false);
      log.info("remove request");
      SessionTestUtil.invokeRequest(jbcm0, removeHandler, sessionId);
      
      WeakReference<Session> session0B = new WeakReference<Session>(jbcm0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm0);
      System.gc(); 
      System.runFinalization();
      assertEquals(0, Attribute.attributeCount());
      assertNotNull(session0B.get());
      
      // Failover request
      setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("failover request");
      SessionTestUtil.invokeRequest(jbcm1, setHandler, sessionId);
      
      WeakReference<Session> session1A = new WeakReference<Session>(jbcm1.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm1);
      assertNotNull(session1A.get());
      assertEquals(1, Attribute.attributeCount());
      
      // Passivate
      Thread.sleep(1100);
      
      log.info("passivate node 0");
      jbcm0.backgroundProcess();
      log.info("passivate node 1");
      jbcm1.backgroundProcess();
      
      System.gc(); 
      System.runFinalization();
      assertEquals(0, Attribute.attributeCount());
      assertNull(session0B.get());
      assertNull(session1A.get());
      
      // Reactivate
      BasicRequestHandler getHandler = new BasicRequestHandler(KEYS, false);
      log.info("activate node 1");
      SessionTestUtil.invokeRequest(jbcm1, getHandler, sessionId);
      
      WeakReference<Session> session1B = new WeakReference<Session>(jbcm1.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm1);
      assertNotNull(session1B.get());
      assertEquals(1, Attribute.attributeCount());      
      
      // Fail back 
      getHandler = new BasicRequestHandler(KEYS, false);
      log.info("fail back request");
      SessionTestUtil.invokeRequest(jbcm0, getHandler, sessionId);
      
      WeakReference<Session> session0C = new WeakReference<Session>(jbcm0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm0);
      assertNotNull(session0C.get());
      assertEquals(2, Attribute.attributeCount());   
      
      // Invalidate session
      InvalidateSessionRequestHandler invalidateHandler = new InvalidateSessionRequestHandler(KEYS, false);
      log.info("invalidate request");
      SessionTestUtil.invokeRequest(jbcm0, invalidateHandler, sessionId);
      SessionTestUtil.cleanupPipeline(jbcm0);
      
      System.gc(); 
      System.runFinalization();
      assertEquals(0, Attribute.attributeCount());
      assertNull(session1B.get());
      assertNull(session0C.get());
   }
   
   public void testSessionExpiration() throws Exception
   {
      log.info("++++ Starting testSessionExpiration ++++");
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 2 secs and a maxIdle of 10 (don't passivate)
      JBossCacheManager[] mgrs = getCacheManagers(warname, 2, 10);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("initial request");
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      assertEquals(1, Attribute.attributeCount());        
      
      String sessionId = setHandler.getSessionId();
      WeakReference<Session> session0A = new WeakReference<Session>(jbcm0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm0);
      assertNotNull(session0A.get());
      assertEquals(1, Attribute.attributeCount());        
      
      // Failover request
      setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("fail over request");
      SessionTestUtil.invokeRequest(jbcm1, setHandler, sessionId);
      assertNotNull(setHandler.getCheckedAttributes().get(KEY));
      assertEquals(Attribute.COUNT -1, ((Attribute) setHandler.getCheckedAttributes().get(KEY)).getCount());
      
      WeakReference<Session> session1A = new WeakReference<Session>(jbcm1.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm1);
      
      System.gc(); 
      System.runFinalization();
      assertNotNull(session1A.get());
      assertEquals(2, Attribute.attributeCount());
      
      // Expire
      Thread.sleep(2100);
      
      log.info("expire node 0");
      jbcm0.backgroundProcess();
      log.info("expire node 1");
      jbcm1.backgroundProcess();
      
      System.gc(); 
      System.runFinalization();
      assertNull(session0A.get());
      assertNull(session1A.get());
      assertEquals(0, Attribute.attributeCount());
   }
   
   public void testUndeploy() throws Exception
   {
      log.info("++++ Starting testUndeploy ++++");
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and no maxIdle
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800, -1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null); 
      assertEquals(1, Attribute.attributeCount());  
      
      String sessionId = setHandler.getSessionId();
      WeakReference<Session> session0A = new WeakReference<Session>(jbcm0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(jbcm0);
      assertNotNull(session0A.get());
      
      jbcm0.stop();         
      jbcm1.stop();    
      
      System.gc(); 
      System.runFinalization();
      assertEquals(0, Attribute.attributeCount());
      assertNull(session0A.get());
   }
   
   protected JBossCacheManager[] getCacheManagers(String warname, int maxInactive, int maxIdle)
      throws Exception
   {
      JBossCacheManager jbcm0 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[0], null);
      JBossWebMetaData metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, maxIdle > 0, maxIdle, -1 ,false, 0);
      jbcm0.init(warname, metadata);
      this.managers.add(jbcm0);
      jbcm0.start();
      
      JBossCacheManager jbcm1 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[1], null);
      metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, true, maxIdle, -1 ,false, 0);
      jbcm1.init(warname, metadata);
      this.managers.add(jbcm1);
      jbcm1.start();
      
      return new JBossCacheManager[]{jbcm0, jbcm1};
   }
   
   private static Map<String, Object> getAttributeMap()
   {
      Object val = Attribute.newAttribute();
      return Collections.singletonMap(KEY, val);
   }
   
   /** Class that keeps track of all its instances */
   private static class Attribute implements Serializable
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 1L;
      
      private static final Logger log = Logger.getLogger(Attribute.class);
      
      public static int COUNT = 0;
      
      private static final WeakHashMap<Attribute, String> attributes = new WeakHashMap<Attribute, String>();
      
      private final int count;
      
      static Attribute newAttribute()
      {
         log.info("Attribute: new Attribute");
         return getAttribute(++COUNT);
      }      
      
      private static Attribute getAttribute(int count)
      {
         Attribute a = new Attribute(count);
         attributes.put(a,  "value");
         return a;
      }
      
      private static void clear()
      {
         attributes.clear();
      }
      
      private Attribute(int count)
      {
         this.count = count;
      }
      
      static int attributeCount()
      {
         return attributes.size();
      }

      public int getCount()
      {
         return count;
      }
      
      private Object writeReplace() throws ObjectStreamException
      {
         log.info("Attribute: serialized");
         return new SerializedForm(count);
      }
      
      private static class SerializedForm implements Serializable
      {
         /** The serialVersionUID */
         private static final long serialVersionUID = 1L;
         
         private final int count;
         
         private SerializedForm(int count)
         {
            this.count = count;
         }
         
         private Object readResolve() throws ObjectStreamException
         {
            log.info("Attribute: deserialized");
            return getAttribute(count);
         }
      }

   }

}
