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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import org.apache.catalina.Context;
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
import org.jboss.test.cluster.web.notification.MockClusteredSessionNotificationPolicy;
import org.jboss.test.cluster.web.notification.MockHttpSessionAttributeListener;
import org.jboss.test.cluster.web.notification.MockHttpSessionListener;
import org.jboss.test.cluster.web.notification.SessionSpecListenerAttribute;
import org.jboss.web.tomcat.service.session.JBossCacheManager;

/**
 * Tests of handling of servlet spec notifications. 
 * 
 * @author Brian Stansberry
 */
public class ClusteredSessionNotificationPolicyTestCase extends JBossTestCase
{
   protected static PojoCache[] pojoCaches = new PojoCache[2];

   protected static long testId = System.currentTimeMillis();
   
   protected static boolean useBuddyRepl = Boolean.valueOf(System.getProperty("jbosstest.cluster.web.cache.br")).booleanValue();
   
   protected Logger log = Logger.getLogger(getClass());   
   
   protected Set<JBossCacheManager> managers = new HashSet<JBossCacheManager>();
   
   protected Map<String, Object> allAttributes;
   protected Map<String, Object> immutables;
   protected Map<String, Object> mutables;
   protected Map<String, Object> attributes;
   protected SessionSpecListenerAttribute attribute = new SessionSpecListenerAttribute();
   protected Map<String, Object> newAttributes;
   protected SessionSpecListenerAttribute newAttribute = new SessionSpecListenerAttribute();
   
   protected String origNotificationPolicy;
   
   public ClusteredSessionNotificationPolicyTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      File root = new File(tmpDir, ClusteredSessionNotificationPolicyTestCase.class.getSimpleName());
      root.mkdirs();
      root.deleteOnExit();
      return CacheConfigTestSetup.getTestSetup(ClusteredSessionNotificationPolicyTestCase.class, pojoCaches, false, root.getAbsolutePath(), !useBuddyRepl, false);
   }

   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      origNotificationPolicy = System.getProperty("jboss.web.clustered.session.notification.policy");
      System.setProperty("jboss.web.clustered.session.notification.policy", MockClusteredSessionNotificationPolicy.class.getName());
      
      attributes = new HashMap<String, Object>();
      attributes.put("KEY", attribute);
      attributes = Collections.unmodifiableMap(attributes);
      
      newAttributes = new HashMap<String, Object>();
      newAttributes.put("KEY", newAttribute);
      newAttributes = Collections.unmodifiableMap(newAttributes);
   }

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (origNotificationPolicy != null)
      {
         System.setProperty("jboss.web.clustered.session.notification.policy", origNotificationPolicy);
      }
      else
      {
         System.clearProperty("jboss.web.clustered.session.notification.policy");
      }
      
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
   }
   
   protected ReplicationGranularity getReplicationGranularity()
   {
      return ReplicationGranularity.SESSION;
   }
   
   protected ReplicationTrigger getReplicationTrigger()
   {
      return ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET;
   }
   
   public void testSessionLifecycleWithNotifications() throws Exception
   {
      log.info("++++ Starting testSessionLifecycleWithNotifications ++++");
      sessionLifecycleTest(true);
   }
   
   public void testSessionLifecycleWithoutNotifications() throws Exception
   {
      log.info("++++ Starting testSessionLifecycleWithoutNotifications ++++");
      sessionLifecycleTest(false);
   }
   
   private void sessionLifecycleTest(boolean notify) throws Exception
   {
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins maxUnreplicated of 0
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800, 1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      assertTrue(jbcm0.getNotificationPolicy() instanceof MockClusteredSessionNotificationPolicy);
      MockClusteredSessionNotificationPolicy mcsnp0 = (MockClusteredSessionNotificationPolicy) jbcm0.getNotificationPolicy();
      assertNotNull("capability set", mcsnp0.getClusteredSessionNotificationCapability());
      mcsnp0.setResponse(notify);
      
      assertTrue(jbcm1.getNotificationPolicy() instanceof MockClusteredSessionNotificationPolicy);
      MockClusteredSessionNotificationPolicy mcsnp1 = (MockClusteredSessionNotificationPolicy) jbcm1.getNotificationPolicy();
      assertNotNull("capability set", mcsnp1.getClusteredSessionNotificationCapability());
      mcsnp1.setResponse(notify);
      
      MockHttpSessionListener hsl0 = new MockHttpSessionListener();
      MockHttpSessionAttributeListener hsal0 = new MockHttpSessionAttributeListener();      
      Context ctx = (Context) jbcm0.getContainer();
      ctx.setApplicationLifecycleListeners(new Object[]{ hsl0 });  
      ctx.setApplicationEventListeners(new Object[]{ hsal0 });  
      
      MockHttpSessionListener hsl1 = new MockHttpSessionListener();
      MockHttpSessionAttributeListener hsal1 = new MockHttpSessionAttributeListener();      
      ctx = (Context) jbcm1.getContainer();
      ctx.setApplicationLifecycleListeners(new Object[]{ hsl1 });  
      ctx.setApplicationEventListeners(new Object[]{ hsal1 }); 
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(attributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      validateNewSession(setHandler);
      String sessionId = setHandler.getSessionId();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl0.invocations.size());
         assertEquals(MockHttpSessionListener.Type.CREATED, hsl0.invocations.get(0));
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.ADDED, hsal0.invocations.get(0));
         assertEquals(2, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.BOUND, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(1));
         
         validateNoNotifications(null, null, hsl1, hsal1, null);
         clearNotifications(hsl0, hsal0, null, null, SessionSpecListenerAttribute.invocations);
      }
      
      // Modify attribute request
      setHandler = new SetAttributesRequestHandler(newAttributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, sessionId);
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REPLACED, hsal0.invocations.get(0));
         assertEquals(4, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.BOUND, SessionSpecListenerAttribute.invocations.get(1));
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(2));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(3));
         
         validateNoNotifications(hsl0, null, hsl1, hsal1, null);
         clearNotifications(null, hsal0, null, null, SessionSpecListenerAttribute.invocations);
      }
      
      // Passivate
      Thread.sleep(1100);
      
      jbcm0.backgroundProcess();
      jbcm1.backgroundProcess();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(0));
         
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1, null);
         clearNotifications(null, null, null, null, SessionSpecListenerAttribute.invocations);
      }
      
      // Remove attribute request
      RemoveAttributesRequestHandler removeHandler = new RemoveAttributesRequestHandler(newAttributes.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm0, removeHandler, sessionId);
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REMOVED, hsal0.invocations.get(0));
         assertEquals(3, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(1));
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(2));
         
         validateNoNotifications(hsl0, null, hsl1, hsal1, null);
         clearNotifications(null, hsal0, null, null, SessionSpecListenerAttribute.invocations);
      }
      
      // Failover request
      setHandler = new SetAttributesRequestHandler(attributes, false);
      SessionTestUtil.invokeRequest(jbcm1, setHandler, sessionId);
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl1.invocations.size());
         assertEquals(MockHttpSessionListener.Type.CREATED, hsl1.invocations.get(0));
         assertEquals(1, hsal1.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.ADDED, hsal1.invocations.get(0));
         assertEquals(2, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.BOUND, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(1));
         
         validateNoNotifications(hsl0, hsal0, null, null, null);
         clearNotifications(null, null, hsl1, hsal1, SessionSpecListenerAttribute.invocations);
      }
      
      // Passivate
      Thread.sleep(1100);
      
      jbcm0.backgroundProcess();
      jbcm1.backgroundProcess();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(0));
         
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1, null);
         clearNotifications(null, null, null, null, SessionSpecListenerAttribute.invocations);
      }
      
      // Fail back and invalidate session after changing attribute
      InvalidateSessionRequestHandler invalidateHandler = new InvalidateSessionRequestHandler(newAttributes.keySet(), false);
      SessionTestUtil.invokeRequest(jbcm0, invalidateHandler, sessionId);
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl0.invocations.size());
         assertEquals(MockHttpSessionListener.Type.DESTROYED, hsl0.invocations.get(0));
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REMOVED, hsal0.invocations.get(0));
         assertEquals(3, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(1));
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(2));
         
         validateNoNotifications(null, null, hsl1, hsal1, null);
         clearNotifications(hsl0, hsal0, null, null, SessionSpecListenerAttribute.invocations);
      }
   }
   
   public void testSessionExpirationWithNotifications() throws Exception
   {
      log.info("++++ Starting testSessionExpirationWithNotifications ++++");
      sessionExpirationTest(true);
   }
   
   public void testSessionExpirationWithoutNotifications() throws Exception
   {
      log.info("++++ Starting testSessionExpirationWithoutNotifications ++++");
      sessionExpirationTest(false);
   }
   
   private void sessionExpirationTest(boolean notify) throws Exception
   {
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 2 secs and a maxIdle of 1
      JBossCacheManager[] mgrs = getCacheManagers(warname, 2, 1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      assertTrue(jbcm0.getNotificationPolicy() instanceof MockClusteredSessionNotificationPolicy);
      MockClusteredSessionNotificationPolicy mcsnp0 = (MockClusteredSessionNotificationPolicy) jbcm0.getNotificationPolicy();
      assertNotNull("capability set", mcsnp0.getClusteredSessionNotificationCapability());
      mcsnp0.setResponse(notify);
      
      assertTrue(jbcm1.getNotificationPolicy() instanceof MockClusteredSessionNotificationPolicy);
      MockClusteredSessionNotificationPolicy mcsnp1 = (MockClusteredSessionNotificationPolicy) jbcm1.getNotificationPolicy();
      assertNotNull("capability set", mcsnp1.getClusteredSessionNotificationCapability());
      mcsnp1.setResponse(notify);
      
      MockHttpSessionListener hsl0 = new MockHttpSessionListener();
      MockHttpSessionAttributeListener hsal0 = new MockHttpSessionAttributeListener();      
      Context ctx = (Context) jbcm0.getContainer();
      ctx.setApplicationLifecycleListeners(new Object[]{ hsl0 });  
      ctx.setApplicationEventListeners(new Object[]{ hsal0 }); 
      
      MockHttpSessionListener hsl1 = new MockHttpSessionListener();
      MockHttpSessionAttributeListener hsal1 = new MockHttpSessionAttributeListener();      
      ctx = (Context) jbcm1.getContainer();
      ctx.setApplicationLifecycleListeners(new Object[]{ hsl1 });  
      ctx.setApplicationEventListeners(new Object[]{ hsal1 }); 
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(attributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      validateNewSession(setHandler);
      
      String sessionId = setHandler.getSessionId();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl0.invocations.size());
         assertEquals(MockHttpSessionListener.Type.CREATED, hsl0.invocations.get(0));
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.ADDED, hsal0.invocations.get(0));
         assertEquals(2, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.BOUND, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(1));
         
         validateNoNotifications(null, null, hsl1, hsal1, null);
         clearNotifications(hsl0, hsal0, null, null, SessionSpecListenerAttribute.invocations);         
      }
      
      // Failover request
      setHandler = new SetAttributesRequestHandler(newAttributes, false);
      SessionTestUtil.invokeRequest(jbcm1, setHandler, sessionId);
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl1.invocations.size());
         assertEquals(MockHttpSessionListener.Type.CREATED, hsl1.invocations.get(0));
         assertEquals(1, hsal1.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REPLACED, hsal1.invocations.get(0));
         assertEquals(4, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.BOUND, SessionSpecListenerAttribute.invocations.get(1));
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(2));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(3));
         
         validateNoNotifications(hsl0, hsal0, null, null, null);
         clearNotifications(null, null, hsl1, hsal1, SessionSpecListenerAttribute.invocations);         
      }
      
      // Passivate
      Thread.sleep(1100);
      
      jbcm0.backgroundProcess();
      jbcm1.backgroundProcess();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(2, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(1));
         
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1, null);
         clearNotifications(null, null, null, null, SessionSpecListenerAttribute.invocations);
      }
      
      // Expire
      Thread.sleep(1000);
      
      jbcm0.backgroundProcess();
      jbcm1.backgroundProcess();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl0.invocations.size());
         assertEquals(MockHttpSessionListener.Type.DESTROYED, hsl0.invocations.get(0));
         assertEquals(1, hsl1.invocations.size());
         assertEquals(MockHttpSessionListener.Type.DESTROYED, hsl1.invocations.get(0));
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REMOVED, hsal0.invocations.get(0));
         assertEquals(1, hsal1.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REMOVED, hsal1.invocations.get(0));
         assertEquals(4, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(1));
         assertEquals(SessionSpecListenerAttribute.Type.ACTIVATING, SessionSpecListenerAttribute.invocations.get(2));
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(3));
         
         validateNoNotifications(null, null, null, null, null);
         clearNotifications(hsl0, hsal0, hsl1, hsal1, SessionSpecListenerAttribute.invocations);
      }
   }
   
   public void testUndeployWithNotifications() throws Exception
   {
      log.info("++++ Starting testUndeployWithNotifications ++++");
      undeployTest(true);
   }
   
   public void testUndeployWithoutNotifications() throws Exception
   {
      log.info("++++ Starting testUndeployWithoutNotifications ++++");
      undeployTest(false);
   }
   
   private void undeployTest(boolean notify) throws Exception
   {
      String warname = String.valueOf(++testId);
      
      // A war with a maxInactive of 30 mins and no maxIdle
      JBossCacheManager[] mgrs = getCacheManagers(warname, 1800, -1);
      JBossCacheManager jbcm0 = mgrs[0];
      JBossCacheManager jbcm1 = mgrs[1];
      
      assertTrue(jbcm0.getNotificationPolicy() instanceof MockClusteredSessionNotificationPolicy);
      MockClusteredSessionNotificationPolicy mcsnp0 = (MockClusteredSessionNotificationPolicy) jbcm0.getNotificationPolicy();
      assertNotNull("capability set", mcsnp0.getClusteredSessionNotificationCapability());
      mcsnp0.setResponse(notify);
      
      assertTrue(jbcm1.getNotificationPolicy() instanceof MockClusteredSessionNotificationPolicy);
      MockClusteredSessionNotificationPolicy mcsnp1 = (MockClusteredSessionNotificationPolicy) jbcm1.getNotificationPolicy();
      assertNotNull("capability set", mcsnp1.getClusteredSessionNotificationCapability());
      mcsnp1.setResponse(notify);
      
      MockHttpSessionListener hsl0 = new MockHttpSessionListener();
      MockHttpSessionAttributeListener hsal0 = new MockHttpSessionAttributeListener();      
      Context ctx = (Context) jbcm0.getContainer();
      ctx.setApplicationLifecycleListeners(new Object[]{ hsl0 });  
      ctx.setApplicationEventListeners(new Object[]{ hsal0 });  
      
      MockHttpSessionListener hsl1 = new MockHttpSessionListener();
      MockHttpSessionAttributeListener hsal1 = new MockHttpSessionAttributeListener();      
      ctx = (Context) jbcm1.getContainer();
      ctx.setApplicationLifecycleListeners(new Object[]{ hsl1 });  
      ctx.setApplicationEventListeners(new Object[]{ hsal1 }); 
      
      // Initial request
      SetAttributesRequestHandler setHandler = new SetAttributesRequestHandler(attributes, false);
      SessionTestUtil.invokeRequest(jbcm0, setHandler, null);
      
      validateNewSession(setHandler);
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl0.invocations.size());
         assertEquals(MockHttpSessionListener.Type.CREATED, hsl0.invocations.get(0));
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.ADDED, hsal0.invocations.get(0));
         assertEquals(2, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.BOUND, SessionSpecListenerAttribute.invocations.get(0));
         assertEquals(SessionSpecListenerAttribute.Type.PASSIVATED, SessionSpecListenerAttribute.invocations.get(1));
         
         validateNoNotifications(null, null, hsl1, hsal1, null);
         clearNotifications(hsl0, hsal0, null, null, SessionSpecListenerAttribute.invocations);
         
      }
      
      jbcm0.stop();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         assertEquals(1, hsl0.invocations.size());
         assertEquals(MockHttpSessionListener.Type.DESTROYED, hsl0.invocations.get(0));
         assertEquals(1, hsal0.invocations.size());
         assertEquals(MockHttpSessionAttributeListener.Type.REMOVED, hsal0.invocations.get(0));
         assertEquals(1, SessionSpecListenerAttribute.invocations.size());
         assertEquals(SessionSpecListenerAttribute.Type.UNBOUND, SessionSpecListenerAttribute.invocations.get(0));
         
         validateNoNotifications(null, null, hsl1, hsal1, null);
         clearNotifications(hsl0, hsal0, null, null, SessionSpecListenerAttribute.invocations);
         
         
      }
      
      jbcm1.stop();
      
      if (!notify)
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
      else
      {
         validateNoNotifications(hsl0, hsal0, hsl1, hsal1);
      }
   }
   
   private void validateNoNotifications(MockHttpSessionListener hsl0, MockHttpSessionAttributeListener hsal0,
         MockHttpSessionListener hsl1, MockHttpSessionAttributeListener hsal1)
   {
      validateNoNotifications(hsl0, hsal0, hsl1, hsal1, SessionSpecListenerAttribute.invocations);
   }
   
   private void validateNoNotifications(MockHttpSessionListener hsl0, MockHttpSessionAttributeListener hsal0,
         MockHttpSessionListener hsl1, MockHttpSessionAttributeListener hsal1, List<SessionSpecListenerAttribute.Type> sspalis)
   {
      if (hsl0 != null)
      {
         assertEquals(0, hsl0.invocations.size());
      }
      if (hsal0 != null)
      {
         assertEquals(0, hsal0.invocations.size());
      }
      if (hsl1 != null)
      {
         assertEquals(0, hsl1.invocations.size());
      }
      if (hsal1 != null)
      {
         assertEquals(0, hsal1.invocations.size());
      }
      
      if (sspalis != null)
      {
         assertEquals(0, sspalis.size());         
      }
      
      clearNotifications(hsl0, hsal0, hsl1, hsal1, sspalis);
   }
   
   private void clearNotifications(MockHttpSessionListener hsl0, MockHttpSessionAttributeListener hsal0,
         MockHttpSessionListener hsl1, MockHttpSessionAttributeListener hsal1, List<SessionSpecListenerAttribute.Type> sspalis)
   {      

      if (hsl0 != null)
      {
         hsl0.invocations.clear();
      }
      if (hsal0 != null)
      {
         hsal0.invocations.clear();
      }
      if (hsl1 != null)
      {
         hsl1.invocations.clear();
      }
      if (hsal1 != null)
      {
         hsal1.invocations.clear();
      }
      
      if (sspalis != null)
      {
         sspalis.clear();         
      }
   }
   
   protected JBossCacheManager[] getCacheManagers(String warname, int maxInactive, int maxIdle)
      throws Exception
   {
      JBossCacheManager jbcm0 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[0], null);
      JBossWebMetaData metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, maxIdle > 0, maxIdle, -1 ,false, 0);
      metadata.getReplicationConfig().setSessionNotificationPolicy(MockClusteredSessionNotificationPolicy.class.getName());
      jbcm0.init(warname, metadata);
      this.managers.add(jbcm0);
      jbcm0.start();
      
      JBossCacheManager jbcm1 = SessionTestUtil.createManager(warname, maxInactive, pojoCaches[1], null);
      metadata = SessionTestUtil.createWebMetaData(getReplicationGranularity(), getReplicationTrigger(), -1, true, maxIdle, -1 ,false, 0);
      metadata.getReplicationConfig().setSessionNotificationPolicy(MockClusteredSessionNotificationPolicy.class.getName());
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
