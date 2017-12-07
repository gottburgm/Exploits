/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.jboss.cache.Cache;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.buddyreplication.NextMemberBuddyLocatorConfig;
import org.jboss.cache.config.BuddyReplicationConfig;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.ConfigurationRegistry;
import org.jboss.cache.config.BuddyReplicationConfig.BuddyLocatorConfig;
import org.jboss.cache.config.Configuration.CacheMode;
import org.jboss.cache.jmx.CacheJmxWrapper;
import org.jboss.cache.notifications.event.EventImpl;
import org.jboss.cache.notifications.event.Event.Type;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.cache.pojo.PojoCacheFactory;
import org.jboss.cache.pojo.jmx.PojoCacheJmxWrapper;
import org.jboss.cache.transaction.BatchModeTransactionManager;
import org.jboss.ha.cachemanager.CacheManager;
import org.jboss.ha.cachemanager.DependencyInjectedConfigurationRegistry;
import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.testutil.DelegatingMockCache;
import org.jboss.test.cluster.testutil.MockTransactionManagerLookup;
import org.jboss.test.cluster.web.mocks.MockEngine;
import org.jboss.test.cluster.web.mocks.MockHost;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.web.tomcat.service.sso.ClusteredSingleSignOn;
import org.jboss.web.tomcat.service.sso.jbc.JBossCacheSSOClusterManager;
import org.jboss.web.tomcat.service.sso.spi.FullyQualifiedSessionId;
import org.jgroups.Address;
import org.jgroups.View;
import org.jgroups.ViewId;
import org.jgroups.stack.IpAddress;

/**
 * Test of the JBossCacheSSOClusterManager class.
 * 
 * @author Brian Stansberry
 */
public class TreeCacheSSOClusterManagerUnitTestCase extends JBossTestCase
{
   /**
    * Default global value for the cacheName property
    */
   public static final String CACHE_OBJECT_NAME = "jboss.cache:service=ClusteredSSOCache";
   
   private static IpAddress LOCAL_ADDRESS;
   private static IpAddress REMOTE_ADDRESS;
   
   private String bindAddress;
   private MBeanServer mbeanServer;
   private CacheManager cacheManager;
   
   public TreeCacheSSOClusterManagerUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      bindAddress = System.getProperty("jgroups.bind.address");
      System.setProperty("jgroups.bind.address", "127.0.0.1");
      
      LOCAL_ADDRESS  = new IpAddress("127.0.0.1", 11111);
      REMOTE_ADDRESS = new IpAddress("192.168.0.1", 11111);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (bindAddress == null)
         System.clearProperty("jgroups.bind.address");
      else
         System.setProperty("jgroups.bind.address", bindAddress);
      
      if (cacheManager != null)
      {
         cacheManager.stop();
         cacheManager = null;
      }
      
      if (mbeanServer != null)
      {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
         mbeanServer = null;
      }
   }

   public void testCacheJmxIntegration() throws Exception
   {
      log.debug("+++ testCacheJmxIntegration()");
      
      mbeanServer = MBeanServerFactory.createMBeanServer("cacheJmxTest");
      CacheJmxWrapper wrapper = null;
      try 
      {
         // Register a cache
         wrapper = new CacheJmxWrapper();
         // JBAS-4097 -- don't use a TransactionManagerLookup that will
         // bind DummyTransactionManager into JNDI, as that will screw
         // up other tests
         wrapper.setTransactionManagerLookupClass(MockTransactionManagerLookup.class.getName());
         wrapper.setCacheMode("REPL_SYNC");
         mbeanServer.registerMBean(wrapper, new ObjectName(CACHE_OBJECT_NAME));
         wrapper.create();
         wrapper.start();
         
         // Build up an SSO infrastructure based on LOCAL_ADDRESS         
         JBossCacheSSOClusterManager localSSOManager = new JBossCacheSSOClusterManager();
         
         MockSSOValve localValve = new MockSSOValve(mbeanServer);
         localValve.setCacheConfig(CACHE_OBJECT_NAME);
         localValve.setClusterManager(localSSOManager);
         localSSOManager.setSSOLocalManager(localValve);
         localSSOManager.start();
         
         // Create an SSO that will have two sessions from local valve
         localSSOManager.register("1", "FORM", "Brian", "password");
         
         Manager localSessMgr1 = getSessionManager("A");
         Session sess1 = new MockSession(localSessMgr1, "1");
         localSSOManager.addSession("1", getFullyQualifiedSessionId(sess1));
         
         Manager localSessMgr2 = getSessionManager("B");
         Session sess2 = new MockSession(localSessMgr2, "2");
         localSSOManager.addSession("1", getFullyQualifiedSessionId(sess2));
         
         // Confirm that data is cached properly
         assertEquals("SSO 1 has correct number of sessions", 2, localSSOManager.getSessionCount("1"));   
      }
      finally
      {
         try
         {
            if (wrapper != null)
            {
               wrapper.stop();
               wrapper.destroy();
            }
         }
         catch (Exception ignored)
         {            
         }
      }      
   }

   public void testPojoCacheJmxIntegration() throws Exception
   {
      log.debug("+++ testPojoCacheJmxIntegration()");
      
      mbeanServer = MBeanServerFactory.createMBeanServer("pojoCacheTest");
      PojoCacheJmxWrapper wrapper = null;
      try 
      {
         // Register a cache
         wrapper = new PojoCacheJmxWrapper();
         // JBAS-4097 -- don't use a TransactionManagerLookup that will
         // bind DummyTransactionManager into JNDI, as that will screw
         // up other tests
         wrapper.setTransactionManagerLookupClass(MockTransactionManagerLookup.class.getName());
         wrapper.setCacheMode("REPL_SYNC");
         mbeanServer.registerMBean(wrapper, new ObjectName(CACHE_OBJECT_NAME));
         wrapper.create();
         wrapper.start();
         
         // Build up an SSO infrastructure based on LOCAL_ADDRESS         
         JBossCacheSSOClusterManager localSSOManager = new JBossCacheSSOClusterManager();
         
         MockSSOValve localValve = new MockSSOValve(mbeanServer);
         localValve.setCacheConfig(CACHE_OBJECT_NAME);
         localValve.setClusterManager(localSSOManager);
         localSSOManager.setSSOLocalManager(localValve);
         localSSOManager.start();
         
         // Create an SSO that will have two sessions from local valve
         localSSOManager.register("1", "FORM", "Brian", "password");
         
         Manager localSessMgr1 = getSessionManager("A");
         Session sess1 = new MockSession(localSessMgr1, "1");
         localSSOManager.addSession("1", getFullyQualifiedSessionId(sess1));
         
         Manager localSessMgr2 = getSessionManager("B");
         Session sess2 = new MockSession(localSessMgr2, "2");
         localSSOManager.addSession("1", getFullyQualifiedSessionId(sess2));
         
         // Confirm that data is cached properly
         assertEquals("SSO 1 has correct number of sessions", 2, localSSOManager.getSessionCount("1"));   
      }
      finally
      {
         try
         {
            if (wrapper != null)
            {
               wrapper.stop();
               wrapper.destroy();
            }
         }
         catch (Exception ignored)
         {            
         }
      }
   }

   public void testPojoCacheIntegration() throws Exception
   {
      log.debug("+++ testPojoCacheIntegration()");
      
      CacheManager cacheManager = getCacheManager();

      // Register a cache
      Configuration config = new Configuration();
      // JBAS-4097 -- don't use a TransactionManagerLookup that will
      // bind DummyTransactionManager into JNDI, as that will screw
      // up other tests
      config.setTransactionManagerLookupClass(MockTransactionManagerLookup.class.getName());
      config.setCacheMode("REPL_SYNC");
      PojoCache pc = PojoCacheFactory.createCache(config, false);
      
      cacheManager.registerPojoCache(pc, ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      
      // Build up an SSO infrastructure based on LOCAL_ADDRESS         
      JBossCacheSSOClusterManager localSSOManager = new JBossCacheSSOClusterManager();
      
      MockSSOValve localValve = new MockSSOValve(mbeanServer);
      localValve.setCacheConfig(ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      localValve.setClusterManager(localSSOManager);
      localSSOManager.setSSOLocalManager(localValve);
      localSSOManager.start();
      
      // Create an SSO that will have two sessions from local valve
      localSSOManager.register("1", "FORM", "Brian", "password");
      
      Manager localSessMgr1 = getSessionManager("A");
      Session sess1 = new MockSession(localSessMgr1, "1");
      localSSOManager.addSession("1", getFullyQualifiedSessionId(sess1));
      
      Manager localSessMgr2 = getSessionManager("B");
      Session sess2 = new MockSession(localSessMgr2, "2");
      localSSOManager.addSession("1", getFullyQualifiedSessionId(sess2));
      
      // Confirm that data is cached properly
      assertEquals("SSO 1 has correct number of sessions", 2, localSSOManager.getSessionCount("1"));
   }
   
   // Disabled; buddy replication is acceptable
   /* 
   public void testDisallowBuddyReplication() throws Exception
   {
      log.debug("+++ testDisallowBuddyReplication()");
      buddyReplicationConfigTest(true);
      
      // Flush the cache manager
      cacheManager.stop();
      cacheManager = null;
      
      buddyReplicationConfigTest(false);
   }
   
   
   private void buddyReplicationConfigTest(boolean enabled) throws Exception
   {
      CacheManager cacheManager = getCacheManager();

      // Register a cache          
      Cache cache = new DefaultCacheFactory().createCache(false);
      // JBAS-4097 -- don't use a TransactionManagerLookup that will
      // bind DummyTransactionManager into JNDI, as that will screw
      // up other tests
      cache.getConfiguration().setTransactionManagerLookupClass(MockTransactionManagerLookup.class.getName());
      cache.getConfiguration().setCacheMode("REPL_SYNC");
      // Configure buddy replication
      BuddyReplicationConfig brc = new BuddyReplicationConfig();
      brc.setEnabled(enabled);
      brc.setBuddyPoolName("clusteredsso");
      BuddyLocatorConfig blc = new NextMemberBuddyLocatorConfig();
      brc.setBuddyLocatorConfig(blc);
      cache.getConfiguration().setBuddyReplicationConfig(brc);
      
      cacheManager.registerCache(cache, ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      
      // Build up an SSO infrastructure based on LOCAL_ADDRESS         
      JBossCacheSSOClusterManager localSSOManager = new JBossCacheSSOClusterManager();
      
      MockSSOValve localValve = new MockSSOValve(null);
      localValve.setCacheConfig(ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      localValve.setClusterManager(localSSOManager);
      localSSOManager.setSSOLocalManager(localValve);
      
      try
      {
         localSSOManager.start();
         if (enabled)
         {
            fail("JBossCacheSSOClusterManager should not start with buddy replication enabled");
         }
      }
      catch (LifecycleException le)
      {
         if (!enabled)
         {
            String msg = "Caught exception starting with BR disabled " + le.getLocalizedMessage();
            log.error(msg, le);
            fail(msg);
         }
         // else we shouldn't start
      }
   }
   */
   
   public void testDeadMemberCleanupWithPool() throws Exception
   {
      log.debug("+++ testDeadMemberCleanupWithPool()");
      deadMemberCleanupTest(true);
   }
   
   public void testDeadMemberCleanupWithoutPool() throws Exception
   {
      log.debug("+++ testDeadMemberCleanupWithoutPool()");
      deadMemberCleanupTest(false);
   }
   
   private void deadMemberCleanupTest(boolean usePool) throws Exception
   {       
      mbeanServer = usePool ? MBeanServerFactory.createMBeanServer("deadMemberTest")
                                        : null;

      CacheManager cacheManager = getCacheManager();
      // Register a cache
      Cache<Object, Object> delegate = new DefaultCacheFactory<Object, Object>().createCache(false);
      MockTreeCache cache = new MockTreeCache(delegate);
      // JBAS-4097 -- don't use a TransactionManagerLookup that will
      // bind DummyTransactionManager into JNDI, as that will screw
      // up other tests
      cache.getConfiguration().getRuntimeConfig().setTransactionManager(new BatchModeTransactionManager());
      
      cacheManager.registerCache(cache, ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      
      if (usePool)
      {
         BasicThreadPool pool = new BasicThreadPool();
         mbeanServer.registerMBean(pool, new ObjectName(JBossCacheSSOClusterManager.DEFAULT_THREAD_POOL_NAME));
      }
      
      // Build up an SSO infrastructure based on LOCAL_ADDRESS  
      JBossCacheSSOClusterManager localSSOManager = new JBossCacheSSOClusterManager();
      
      MockSSOValve localValve = new MockSSOValve(mbeanServer);
      localValve.setCacheConfig(ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      localValve.setClusterManager(localSSOManager);
      localSSOManager.setSSOLocalManager(localValve);
      localSSOManager.start();
      
      assertEquals("Thread pool usage as expected", usePool, localSSOManager.isUsingThreadPool());
      
      //  Build up a second SSO infrastructure based on LOCAL_ADDRESS
      // It uses the same mock cache, but we change the cache address
      // so it thinks it's a different address when it starts
      cache.setOurAddress(REMOTE_ADDRESS);
      
      JBossCacheSSOClusterManager remoteSSOManager = new JBossCacheSSOClusterManager();
      
      MockSSOValve remoteValve = new MockSSOValve(mbeanServer);
      remoteValve.setCacheConfig(ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      remoteValve.setClusterManager(remoteSSOManager);
      remoteSSOManager.setSSOLocalManager(localValve);
      remoteSSOManager.start();
      
      
      // Create an SSO that will have sessions from both valves
      localSSOManager.register("1", "FORM", "Brian", "password");
      
      Manager localSessMgr1 = getSessionManager("A");
      Session sess1 = new MockSession(localSessMgr1, "1");
      localSSOManager.addSession("1", getFullyQualifiedSessionId(sess1));
      
      Manager remoteSessMgr1 = getSessionManager("B");
      Session sess2 = new MockSession(remoteSessMgr1, "2");
      remoteSSOManager.addSession("1", getFullyQualifiedSessionId(sess2));
      
      
      // Create another SSO with sessions only from remote
      remoteSSOManager.register("2", "FORM", "Brian", "password");
      
      Manager remoteSessMgr2 = getSessionManager("C");
      Session sess3 = new MockSession(remoteSessMgr2, "3");
      remoteSSOManager.addSession("2", getFullyQualifiedSessionId(sess3));
      
      Manager remoteSessMgr3 = getSessionManager("D");
      Session sess4 = new MockSession(remoteSessMgr3, "4");
      remoteSSOManager.addSession("2", getFullyQualifiedSessionId(sess4));
      
      
      // Create a third SSO that will have sessions from both valves
      // with the same session id
      localSSOManager.register("3", "FORM", "Brian", "password");
      
      Manager localSessMgr2 = getSessionManager("E");
      Session sess5 = new MockSession(localSessMgr2, "5");
      localSSOManager.addSession("3", getFullyQualifiedSessionId(sess5));
      
      Manager remoteSessMgr4 = getSessionManager("E");
      Session sess6 = new MockSession(remoteSessMgr4, "5");
      remoteSSOManager.addSession("3", getFullyQualifiedSessionId(sess6));
      
      
      // Create a fourth SSO that will have two sessions from local valve
      localSSOManager.register("4", "FORM", "Brian", "password");
      
      Manager localSessMgr3 = getSessionManager("F");
      Session sess7 = new MockSession(localSessMgr3, "7");
      localSSOManager.addSession("4", getFullyQualifiedSessionId(sess7));
      
      Manager localSessMgr4 = getSessionManager("G");
      Session sess8 = new MockSession(localSessMgr4, "8");
      localSSOManager.addSession("4", getFullyQualifiedSessionId(sess8));
      
      
      // Create a fifth SSO with sessions only from remote, same session id
      // but different managers
      remoteSSOManager.register("5", "FORM", "Brian", "password");
      
      Manager remoteSessMgr5 = getSessionManager("H");
      Session sess9 = new MockSession(remoteSessMgr5, "9");
      remoteSSOManager.addSession("5", getFullyQualifiedSessionId(sess9));
      
      Manager remoteSessMgr6 = getSessionManager("I");
      Session sess10 = new MockSession(remoteSessMgr6, "9");
      remoteSSOManager.addSession("5", getFullyQualifiedSessionId(sess10));
      
      // Confirm that data is cached properly
      assertEquals("SSO 1 has correct number of sessions", 2, localSSOManager.getSessionCount("1"));
      assertEquals("SSO 1 has correct number of sessions", 2, remoteSSOManager.getSessionCount("1"));
      assertEquals("SSO 2 has correct number of sessions", 2, localSSOManager.getSessionCount("2"));
      assertEquals("SSO 2 has correct number of sessions", 2, remoteSSOManager.getSessionCount("2"));
      assertEquals("SSO 3 has correct number of sessions", 2, localSSOManager.getSessionCount("3"));
      assertEquals("SSO 3 has correct number of sessions", 2, remoteSSOManager.getSessionCount("3"));
      assertEquals("SSO 4 has correct number of sessions", 2, localSSOManager.getSessionCount("4"));
      assertEquals("SSO 4 has correct number of sessions", 2, remoteSSOManager.getSessionCount("4"));
      assertEquals("SSO 5 has correct number of sessions", 2, localSSOManager.getSessionCount("5"));
      assertEquals("SSO 5 has correct number of sessions", 2, remoteSSOManager.getSessionCount("5"));
      
      // Put in a new view with REMOTE_ADDRESS dead
      ViewId viewId = new ViewId(LOCAL_ADDRESS, 1);
      Vector v = new Vector();
      v.add(LOCAL_ADDRESS);
      EventImpl event = new EventImpl();
      event.setNewView(new View(viewId, v));
      event.setType(Type.VIEW_CHANGED);
      event.setPre(false);
      
      localSSOManager.viewChange(event);
      
      // Give the cleanup thread time to finish
      Thread.sleep(2000);
      
      // Confirm that cached data is properly cleaned up
      assertEquals("SSO 1 has correct number of sessions", 1, localSSOManager.getSessionCount("1"));
      assertEquals("SSO 2 has correct number of sessions", 0, localSSOManager.getSessionCount("2"));
      assertEquals("SSO 3 has correct number of sessions", 1, localSSOManager.getSessionCount("3"));
      assertEquals("SSO 4 has correct number of sessions", 2, localSSOManager.getSessionCount("4"));
      assertEquals("SSO 5 has correct number of sessions", 0, localSSOManager.getSessionCount("5"));
   }
   
   /**
    * Test for JBAS-5609 -- confirm that if sessions from different managers
    * but with the same session id are registered, the removal of one leaves
    * the SSO in the proper state.
    */
   public void testSameSessionId() throws Exception
   {
      CacheManager cacheManager = getCacheManager();
      // Register a cache
      Cache<Object, Object> delegate = new DefaultCacheFactory<Object, Object>().createCache(false);
      MockTreeCache cache = new MockTreeCache(delegate);
      // JBAS-4097 -- don't use a TransactionManagerLookup that will
      // bind DummyTransactionManager into JNDI, as that will screw
      // up other tests
      cache.getConfiguration().getRuntimeConfig().setTransactionManager(new BatchModeTransactionManager());
      
      cacheManager.registerCache(cache, ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      
      // Build up an SSO infrastructure based on LOCAL_ADDRESS  
      JBossCacheSSOClusterManager localSSOManager = new JBossCacheSSOClusterManager();
      
      MockSSOValve localValve = new MockSSOValve(mbeanServer);
      localValve.setCacheConfig(ClusteredSingleSignOn.DEFAULT_CACHE_NAME);
      localValve.setClusterManager(localSSOManager);
      localSSOManager.setSSOLocalManager(localValve);
      localSSOManager.start();
      
      
      // Create an SSO that will have sessions from both valves
      localSSOManager.register("1", "FORM", "Brian", "password");
      
      Manager localSessMgr1 = getSessionManager("A");
      Session sess1 = new MockSession(localSessMgr1, "1");
      localSSOManager.addSession("1", getFullyQualifiedSessionId(sess1));
      
      Manager localSessMgr2 = getSessionManager("B");
      Session sess2 = new MockSession(localSessMgr2, "1");
      localSSOManager.addSession("1", getFullyQualifiedSessionId(sess2));
      
      assertEquals(2, localSSOManager.getSessionCount("1"));
      
      localSSOManager.removeSession("1", getFullyQualifiedSessionId(sess2));
      
      assertEquals(1, localSSOManager.getSessionCount("1"));
   }
   
   private CacheManager getCacheManager() throws Exception
   {
      if (cacheManager == null)
      {
         JChannelFactory channelFactory = new JChannelFactory();
         ConfigurationRegistry registry = new DependencyInjectedConfigurationRegistry();
         cacheManager = new CacheManager(registry, channelFactory);
         cacheManager.start();
      }
      return cacheManager;
   }
   
   private Manager getSessionManager(String contextName)
   {
      StandardManager mgr = new StandardManager();
      MockEngine engine = new MockEngine();
      MockHost host = new MockHost();
      engine.addChild(host);
      host.setName("localhost");
      StandardContext container = new StandardContext();
      container.setName(contextName);
      host.addChild(container);
      container.setManager(mgr);
      
      return mgr;
   }
   
   private FullyQualifiedSessionId getFullyQualifiedSessionId(Session session)
   {
      String id = session.getIdInternal();
      Container context = session.getManager().getContainer();
      String contextName = context.getName(); 
      Container host = context.getParent();
      String hostName = host.getName();
      
      return new FullyQualifiedSessionId(id, contextName, hostName);
   }
   
   
   static class MockTreeCache extends DelegatingMockCache
   {
      private IpAddress ourAddress = LOCAL_ADDRESS;
      
      public MockTreeCache(Cache delegate) throws Exception
      {
         super(delegate);
         getConfiguration().setCacheMode(CacheMode.LOCAL);
      }

      @Override
      public Address getLocalAddress()
      {
         return ourAddress;
      }
      
      void setOurAddress(IpAddress address)
      {
         ourAddress = address;
      }

      @Override
      public Vector getMembers()
      {
         Vector v = new Vector();
         v.add(LOCAL_ADDRESS);
         v.add(REMOTE_ADDRESS);
         return v;
      }      
      
   }
   
   /**
    * Override ClusteredSingleSignOn to suppress the empty SSO callbacks
    */
   static class MockSSOValve extends ClusteredSingleSignOn
   {
      private final MBeanServer mbeanServer;
      
      MockSSOValve(MBeanServer server)
      {
         this.mbeanServer = server;         
      }
      
      @Override
      public MBeanServer getMBeanServer()
      {
         return mbeanServer;
      }
      
      @Override
      public void notifySSOEmpty(String ssoId)
      {
         // no-op
      }

      @Override
      public void notifySSONotEmpty(String ssoId)
      {
         // no-op
      }      
   }
   
   static class MockSession extends StandardSession
   {
      private static final long serialVersionUID = 1L;
      
      private String ourId;
      
      MockSession(Manager manager, String id)
      {
         super(manager);
         ourId = id;
      }
      
      @Override
      public String getId()
      {
         return ourId;
      }

      @Override
      public String getIdInternal()
      {
         return ourId;
      }      
      
   }

}
