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
package org.jboss.test.cluster.defaultcfg.test;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.ConfigurationRegistry;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.ha.cachemanager.CacheManager;
import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.test.JBossTestCase;

/**
 * Tests CacheManager.
 * 
 * @author Brian Stansberry
 */
public class CacheManagerUnitTestCase extends JBossTestCase
{
   /** A file that includes every configuration element I could think of */
   public static final String DEFAULT_CONFIGURATION_FILE = "cluster/cachemanager/jbc-configs.xml";
   public static final String DEFAULT_STACKS_FILE = "cluster/cachemanager/stacks.xml";
   public static final String DEFAULT_STACK = "test1";
   
   private Set<Cache<Object, Object>> caches = new HashSet<Cache<Object, Object>>();
   private Set<PojoCache> pojoCaches = new HashSet<PojoCache>();
   private String jgroups_bind_addr;

   public CacheManagerUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      String jgroups_bind_addr = System.getProperty("jgroups.bind_addr");
      if (jgroups_bind_addr == null)
      {
         System.setProperty("jbosstest.cluster.node0", System.getProperty("jbosstest.cluster.node0", "localhost"));
      }
   }
   
   protected void tearDown() throws Exception
   {      
      if (jgroups_bind_addr == null)
         System.clearProperty("jgroups.bind_addr");
      
      for (Cache<Object, Object> cache : caches)
      {
         try
         {
            cache.stop();
            cache.destroy();
         }
         catch (Exception e)
         {
            e.printStackTrace(System.out);
         }
      }
      
      for (PojoCache pojoCache : pojoCaches)
      {
         try
         {
            pojoCache.stop();
            pojoCache.destroy();
         }
         catch (Exception e)
         {
            e.printStackTrace(System.out);
         }
      }
      
      super.tearDown();
   }
   
   /**
    * A test that instantiates a CacheRegistry and cycles through all its
    * core configs, creating and releasing a plain JBoss Cache for each.
    * 
    * @throws Exception
    */
   public void testBasic() throws Exception
   {
      JChannelFactory cf = new JChannelFactory();
      cf.setMultiplexerConfig(DEFAULT_STACKS_FILE);
      cf.setExposeChannels(false);
      cf.start();
      CacheManager registry = new CacheManager(DEFAULT_CONFIGURATION_FILE, cf);
      registry.start();
      
      ConfigurationRegistry configRegistry = registry.getConfigurationRegistry();
      
      Set<String> configNames = registry.getConfigurationNames();
      assertEquals(7, configNames.size());
      Set<String> cacheNames = registry.getCacheNames();
      assertEquals(0, cacheNames.size());
      
      for (String configName : configNames)
      {
         assertNull(configName + " not created", registry.getCache(configName, false));
         Cache<Object, Object> cache = registry.getCache(configName, true);         
         caches.add(cache);
         
         // Cache shouldn't be started
         assertEquals(CacheStatus.INSTANTIATED, cache.getCacheStatus());
         cache.create();
         cache.start();
         
         // Config should be a clone
         Configuration rawConfig = configRegistry.getConfiguration(configName);
         Configuration realConfig = cache.getConfiguration();
         assertFalse(rawConfig == realConfig);
         assertEquals(rawConfig.getClusterName(), realConfig.getClusterName());
      }
      
      cacheNames = registry.getCacheNames();
      assertEquals(configNames, cacheNames);
      
      // Test basic releasing of caches
      for (String configName : configNames)
      {
         registry.releaseCache(configName);         
      }
      
      cacheNames = registry.getCacheNames();
      assertEquals(0, cacheNames.size());
      
      // We shouldn't have affected configuration set
      Set<String> configNames2 = registry.getConfigurationNames();
      assertEquals(configNames, configNames2);
      
      // Releasing only checkout of cache should have destroyed it
      for (Iterator<Cache<Object, Object>> it = caches.iterator(); it.hasNext();)
      {
         assertEquals(CacheStatus.DESTROYED, it.next().getCacheStatus());
         it.remove();
      }
      
      // Get cache w/o asking to create returns null
      String configName = configNames.iterator().next();
      assertNull(configName + " not created", registry.getCache(configName, false));
      // Get cache w/ asking to create returns cache
      Cache<Object, Object> cache = registry.getCache(configName, true);
      assertFalse(null == cache);
      caches.add(cache);
      
      cache.create();
      cache.start();
      
      // Test 2 checkouts of the same cache
      Cache<Object, Object> cache2 = registry.getCache(configName, true);      
      assertEquals(cache, cache2);
      
      registry.releaseCache(configName);
      
      // One release does not cause registry to stop cache
      assertEquals(CacheStatus.STARTED, cache.getCacheStatus());

      registry.stop();
      
      // Should still not be stopped
      assertEquals(CacheStatus.STARTED, cache.getCacheStatus());

      registry.releaseCache(configName);

      // Now it should be stopped
      assertEquals(CacheStatus.DESTROYED, cache.getCacheStatus());
      caches.remove(cache);
      
      cacheNames = registry.getCacheNames();
      assertEquals(0, cacheNames.size());
      assertEquals(cacheNames, registry.getConfigurationNames());
   }
   
   /**
    * Same as testBasic() but here we ask for instances of PojoCache.
    * 
    * @throws Exception
    */
   public void testBasicPojo() throws Exception
   {
      JChannelFactory cf = new JChannelFactory();
      cf.setMultiplexerConfig(DEFAULT_STACKS_FILE);
      cf.setExposeChannels(false);
      cf.start();
      CacheManager registry = new CacheManager(DEFAULT_CONFIGURATION_FILE, cf);
      registry.start();
      
      ConfigurationRegistry configRegistry = registry.getConfigurationRegistry();
      
      Set<String> configNames = registry.getConfigurationNames();
      assertEquals(7, configNames.size());
      Set<String> cacheNames = registry.getPojoCacheNames();
      assertEquals(0, cacheNames.size());
      
      for (String configName : configNames)
      {
         assertNull(configName + " not created", registry.getPojoCache(configName, false));
         PojoCache cache = registry.getPojoCache(configName, true);         
         pojoCaches.add(cache);
         
         // Cache shouldn't be started
         assertEquals(CacheStatus.INSTANTIATED, cache.getCache().getCacheStatus());
         cache.create();
         cache.start();
         
         // Config should be a clone
         Configuration rawConfig = configRegistry.getConfiguration(configName);
         Configuration realConfig = cache.getCache().getConfiguration();
         assertFalse(rawConfig == realConfig);
         assertEquals(rawConfig.getClusterName(), realConfig.getClusterName());
      }
      
      cacheNames = registry.getPojoCacheNames();
      assertEquals(configNames, cacheNames);
      
      // Test basic releasing of caches
      for (String configName : configNames)
      {
         registry.releaseCache(configName);         
      }
      
      cacheNames = registry.getPojoCacheNames();
      assertEquals(0, cacheNames.size());
      
      // We shouldn't have affected configuration set
      Set<String> configNames2 = registry.getConfigurationNames();
      assertEquals(configNames, configNames2);
      
      // Releasing only checkout of cache should have destroyed it
      for (Iterator<PojoCache> it = pojoCaches.iterator(); it.hasNext();)
      {
         assertEquals(CacheStatus.DESTROYED, it.next().getCache().getCacheStatus());
         it.remove();
      }
      
      // Get cache w/o asking to create returns null
      String configName = configNames.iterator().next();
      assertNull(configName + " not created", registry.getPojoCache(configName, false));
      // Get cache w/ asking to create returns cache
      PojoCache cache = registry.getPojoCache(configName, true);
      assertFalse(null == cache);
      pojoCaches.add(cache);
      
      cache.create();
      cache.start();
      
      // Test 2 checkouts of the same cache
      PojoCache cache2 = registry.getPojoCache(configName, true);      
      assertEquals(cache, cache2);
      
      registry.releaseCache(configName);
      
      // One release does not cause registry to stop cache
      assertEquals(CacheStatus.STARTED, cache.getCache().getCacheStatus());

      registry.stop();

      // Should still not be stopped
      assertEquals(CacheStatus.STARTED, cache.getCache().getCacheStatus());

      registry.releaseCache(configName);

      // Now it should be stopped
      assertEquals(CacheStatus.DESTROYED, cache.getCache().getCacheStatus());
      caches.remove(cache);
      
      cacheNames = registry.getPojoCacheNames();
      assertEquals(0, cacheNames.size());
      assertEquals(cacheNames, registry.getConfigurationNames());
   }
   
   /**
    * Confirms that the CacheManager can start if no config resource is provided.
    * 
    * @throws Exception
    */
   public void testNullConfigResource() throws Exception
   {
      JChannelFactory cf = new JChannelFactory();
      cf.setMultiplexerConfig(DEFAULT_STACKS_FILE);
      String configResource = null;
      CacheManager registry = new CacheManager(configResource, cf);
      registry.start();
      
      assertEquals("No configs", 0, registry.getConfigurationNames().size());
   }
   
   public void testAliasing() throws Exception
   {
      JChannelFactory cf = new JChannelFactory();
      cf.setMultiplexerConfig(DEFAULT_STACKS_FILE);
      cf.setExposeChannels(false);
      cf.start();
      CacheManager registry = new CacheManager(DEFAULT_CONFIGURATION_FILE, cf);
      registry.start();

      Set<String> configNames = registry.getConfigurationNames();
      assertEquals(7, configNames.size());
      
      assertEquals(0, registry.getCacheNames().size());
      
      assertEquals(0, registry.getPojoCacheNames().size());
      
      Map<String, String> aliases = new HashMap<String, String>();
      aliases.put("alias", DEFAULT_STACK);
      registry.setConfigAliases(aliases);
      
      Map<String, String> registered = registry.getConfigAliases();
      assertEquals(1, registered.size());
      assertEquals(DEFAULT_STACK, registered.get("alias"));
      
      configNames = registry.getConfigurationNames();
      assertEquals(8, configNames.size());
      assertTrue(configNames.contains("alias"));
      
      Cache cache = registry.getCache("alias", true);
      assertNotNull(cache);
      Cache other = registry.getCache(DEFAULT_STACK, false);
      assertEquals(cache, other);
      
      assertEquals(1, registry.getCacheNames().size());
      
      registry.releaseCache(DEFAULT_STACK);
      
      assertEquals(1, registry.getCacheNames().size());
      
      registry.releaseCache("alias");
      
      assertEquals(0, registry.getCacheNames().size());
      
      PojoCache pcache = registry.getPojoCache("alias", true);
      assertNotNull(pcache);
      PojoCache otherPC = registry.getPojoCache(DEFAULT_STACK, false);
      assertEquals(pcache, otherPC);
      
      assertEquals(1, registry.getPojoCacheNames().size());
      
      registry.releaseCache(DEFAULT_STACK);
      
      assertEquals(1, registry.getPojoCacheNames().size());
      
      registry.releaseCache("alias");
      
      assertEquals(0, registry.getPojoCacheNames().size());      
   }
   
   public void testEagerStartCaches() throws Exception
   {
      JChannelFactory cf = new JChannelFactory();
      cf.setMultiplexerConfig(DEFAULT_STACKS_FILE);
      cf.setExposeChannels(false);
      cf.start();
      CacheManager registry = new CacheManager(DEFAULT_CONFIGURATION_FILE, cf);
      
      Set<String> cores = new HashSet<String>();
      cores.add("test1");
      cores.add("test2");
      registry.setEagerStartCaches(cores);
      Set<String> pojos = new HashSet<String>();
      pojos.add("test5");
      pojos.add("test6");
      registry.setEagerStartPojoCaches(pojos);
      
      registry.start();
      
      assertEquals(cores, registry.getCacheNames());
      assertEquals(pojos, registry.getPojoCacheNames());
      
      Set<Cache> caches = new HashSet<Cache>();
      
      for (String name : cores)
      {
         Cache cache = registry.getCache(name, false);
         assertNotNull(cache);
         assertEquals(CacheStatus.STARTED, cache.getCacheStatus());
         caches.add(cache);
      }
      
      for (String name : pojos)
      {
         PojoCache pojocache = registry.getPojoCache(name, false);
         assertNotNull(pojocache);
         Cache cache = pojocache.getCache();
         assertEquals(CacheStatus.STARTED, cache.getCacheStatus());
         caches.add(cache);
      }
      
      for (String name : cores)
      {
         registry.releaseCache(name);
      }
      
      for (String name : pojos)
      {
         registry.releaseCache(name);
      }
      
      for (Cache cache : caches)
      {
         assertEquals(CacheStatus.STARTED, cache.getCacheStatus());         
      }
      
      registry.stop();
      
      for (Cache cache : caches)
      {
         assertEquals(CacheStatus.DESTROYED, cache.getCacheStatus());         
      }
      
   }
}
