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
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.web.mocks.BasicRequestHandler;
import org.jboss.test.cluster.web.mocks.InvalidateSessionRequestHandler;
import org.jboss.test.cluster.web.mocks.RemoveAttributesRequestHandler;
import org.jboss.test.cluster.web.mocks.SetAttributesRequestHandler;
import org.jboss.test.cluster.web.notification.SessionSpecListenerAttribute;
import org.jboss.test.cluster.web.persistent.PersistentSessionTestUtil;
import org.jboss.test.cluster.web.persistent.SimplePersistentStoreTestSetup;
import org.jboss.web.tomcat.service.session.persistent.DataSourcePersistentManager;

/**
 * Tests that references to cached sessions and attributes are released. 
 * 
 * @author Brian Stansberry
 */
public class PersistentStoreMemoryLeakTestCase extends JBossTestCase
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
   
   protected Set<DataSourcePersistentManager> managers = new HashSet<DataSourcePersistentManager>();
   
   public PersistentStoreMemoryLeakTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      String dbAddress = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
      return SimplePersistentStoreTestSetup.getDeploySetup(PersistentStoreMemoryLeakTestCase.class, dbAddress, DBSetupDelegate.DEFAULT_PORT);
   }

   @Override
   protected void tearDown() throws Exception
   {
      cleanHeap();
      
      super.tearDown();
      
      for (DataSourcePersistentManager manager : managers)    
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
      DataSourcePersistentManager[] mgrs = getCacheManagers(warname, 1800, 1);
      DataSourcePersistentManager mgr0 = mgrs[0];
      DataSourcePersistentManager mgr1 = mgrs[1];
      
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("initial request");
      SessionTestUtil.invokeRequest(mgr0, setHandler, null);      
      
      String sessionId = setHandler.getSessionId();
      WeakReference<Session> session0A = new WeakReference<Session>(mgr0.findSession(sessionId));      
      SessionTestUtil.cleanupPipeline(mgr0);
      assertNotNull(session0A.get());
      
      // Modify attribute request
      setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("Modify attribute request");
      SessionTestUtil.invokeRequest(mgr0, setHandler, sessionId);      
      SessionTestUtil.cleanupPipeline(mgr0);
      
      cleanHeap();
      assertAttributeCount(1);
      
      // Passivate
      Thread.sleep(1100);
      
      log.info("passivate node 0");
      mgr0.backgroundProcess();
      log.info("passivate node 1");
      mgr1.backgroundProcess();
      
      cleanHeap();
      assertAttributeCount(0);
      assertNullReference(session0A);
      
      // Remove attribute request
      RemoveAttributesRequestHandler removeHandler = new RemoveAttributesRequestHandler(KEYS, false);
      log.info("remove request");
      SessionTestUtil.invokeRequest(mgr0, removeHandler, sessionId);
      
      WeakReference<Session> session0B = new WeakReference<Session>(mgr0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr0);
      cleanHeap();
      assertAttributeCount(0);
      assertNotNull(session0B.get());
      
      // Failover request
      setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("failover request");
      SessionTestUtil.invokeRequest(mgr1, setHandler, sessionId);
      
      WeakReference<Session> session1A = new WeakReference<Session>(mgr1.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr1);
      assertNotNull(session1A.get());
      assertAttributeCount(1);
      
      // Passivate
      Thread.sleep(1100);
      
      log.info("passivate node 0");
      mgr0.backgroundProcess();
      log.info("passivate node 1");
      mgr1.backgroundProcess();
      
      cleanHeap();
      assertAttributeCount(0);
      assertNullReference(session0B);
      assertNullReference(session1A);
      
      // Reactivate
      BasicRequestHandler getHandler = new BasicRequestHandler(KEYS, false);
      log.info("activate node 1");
      SessionTestUtil.invokeRequest(mgr1, getHandler, sessionId);
      
      WeakReference<Session> session1B = new WeakReference<Session>(mgr1.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr1);
      assertNotNull(session1B.get());
      assertAttributeCount(1);      
      
      // Fail back 
      getHandler = new BasicRequestHandler(KEYS, false);
      log.info("fail back request");
      SessionTestUtil.invokeRequest(mgr0, getHandler, sessionId);
      
      WeakReference<Session> session0C = new WeakReference<Session>(mgr0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr0);
      assertNotNull(session0C.get());
      assertAttributeCount(2);   
      
      // Invalidate session
      InvalidateSessionRequestHandler invalidateHandler = new InvalidateSessionRequestHandler(KEYS, false);
      log.info("invalidate request");
      SessionTestUtil.invokeRequest(mgr0, invalidateHandler, sessionId);
      SessionTestUtil.cleanupPipeline(mgr0);
      
      cleanHeap();
      assertNotNull(session1B.get());
      assertNullReference(session0C);
      assertAttributeCount(1);
      
      // Make mgr1 aware of the invalidation
      getHandler = new BasicRequestHandler(KEYS, false);
      SessionTestUtil.invokeRequest(mgr1, getHandler, sessionId);
      
      cleanHeap();
      assertNullReference(session1B);
      assertAttributeCount(0);
   }
   
   public void testSessionExpiration() throws Exception
   {
      log.info("++++ Starting testSessionExpiration ++++");
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 2 secs and a maxIdle of 10 (don't passivate)
      DataSourcePersistentManager[] mgrs = getCacheManagers(warname, 2, 10);
      DataSourcePersistentManager mgr0 = mgrs[0];
      DataSourcePersistentManager mgr1 = mgrs[1];
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("initial request");
      SessionTestUtil.invokeRequest(mgr0, setHandler, null);
      assertAttributeCount(1);        
      
      String sessionId = setHandler.getSessionId();
      WeakReference<Session> session0A = new WeakReference<Session>(mgr0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr0);
      assertNotNull(session0A.get());
      assertAttributeCount(1);        
      
      // Failover request
      setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      log.info("fail over request");
      SessionTestUtil.invokeRequest(mgr1, setHandler, sessionId);
      assertNotNull(setHandler.getCheckedAttributes().get(KEY));
      assertEquals(Attribute.COUNT -1, ((Attribute) setHandler.getCheckedAttributes().get(KEY)).getCount());
      
      WeakReference<Session> session1A = new WeakReference<Session>(mgr1.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr1);
      
      cleanHeap();
      assertNotNull(session1A.get());
      assertAttributeCount(2);
      
      // Expire
      Thread.sleep(2100);
      
      log.info("expire node 0");
      mgr0.backgroundProcess();
      log.info("expire node 1");
      mgr1.backgroundProcess();
      
      cleanHeap();
      assertNullReference(session0A);
      assertNullReference(session1A);
      assertAttributeCount(0);
   }

   private void cleanHeap()
   {
      System.gc(); 
      System.runFinalization();
      System.gc(); 
      System.runFinalization();
      System.gc();
      log.debug("Heap cleaned");
   }
   
   private void assertNullReference(WeakReference<?> ref)
   {
      long limit = System.currentTimeMillis() + 2500;
      while (ref.get() != null && System.currentTimeMillis() < limit)
      {
         cleanHeap();
         if (ref.get() != null)
         {
            try
            {
               Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
               log.warn("interrupted");
               break;
            }
         }
      }
      assertNull(ref.get());
   }
   
   private void assertAttributeCount(int expected)
   {
      long limit = System.currentTimeMillis() + 2500;
      while (Attribute.attributeCount() > expected && System.currentTimeMillis() < limit)
      {
         cleanHeap();
         if (Attribute.attributeCount() > expected)
         {
            try
            {
               Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
               log.warn("interrupted");
               break;
            }
         }
      }
      assertEquals(expected, Attribute.attributeCount());
   }
   
   public void testUndeploy() throws Exception
   {
      log.info("++++ Starting testUndeploy ++++");
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and no maxIdle
      DataSourcePersistentManager[] mgrs = getCacheManagers(warname, 1800, -1);
      DataSourcePersistentManager mgr0 = mgrs[0];
      DataSourcePersistentManager mgr1 = mgrs[1];
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(getAttributeMap(), false);
      SessionTestUtil.invokeRequest(mgr0, setHandler, null); 
      assertAttributeCount(1);  
      
      String sessionId = setHandler.getSessionId();
      WeakReference<Session> session0A = new WeakReference<Session>(mgr0.findSession(sessionId));
      SessionTestUtil.cleanupPipeline(mgr0);
      assertNotNull(session0A.get());
      
      mgr0.stop();         
      mgr1.stop();    
      
      cleanHeap();
      assertNull(session0A.get());
      assertAttributeCount(0);
   }
   
   protected DataSourcePersistentManager[] getCacheManagers(String warname, int maxInactive, int maxIdle)
      throws Exception
   {
      DataSourcePersistentManager mgr0 = PersistentSessionTestUtil.createManager(warname, maxInactive, null);
      PersistentSessionTestUtil.configureManager(mgr0, getReplicationGranularity(), getReplicationTrigger(), -1, maxIdle > 0, maxIdle, -1 ,false, 0);
      this.managers.add(mgr0);
      mgr0.start();
      
      DataSourcePersistentManager mgr1 = PersistentSessionTestUtil.createManager(warname, maxInactive, null);
      PersistentSessionTestUtil.configureManager(mgr1, getReplicationGranularity(), getReplicationTrigger(), -1, true, maxIdle, -1 ,false, 0);
      this.managers.add(mgr1);
      mgr1.start();
      
      return new DataSourcePersistentManager[]{mgr0, mgr1};
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
         log.info("Stored ref to Attribute@" + System.identityHashCode(a));
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

      @Override
      protected void finalize() throws Throwable
      {
         log.info("Attribute@" + System.identityHashCode(this) + " finalized");
         super.finalize();
      }
      
      

   }

}
