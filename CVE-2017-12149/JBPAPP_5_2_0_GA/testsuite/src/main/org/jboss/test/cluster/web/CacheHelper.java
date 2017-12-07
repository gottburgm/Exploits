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
package org.jboss.test.cluster.web;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.ObjectName;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.buddyreplication.BuddyManager;
import org.jboss.ha.framework.server.CacheManagerLocator;
import org.jboss.ha.framework.server.PojoCacheManager;
import org.jboss.ha.framework.server.PojoCacheManagerLocator;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Helper class to locate and invoke methods on the cache mbeans used by JBossWeb.
 *
 * TODO. Update the DistributedCacheManager SPI to provide the data we use
 * here and use a factory to create the SPI impl rather than directly accessing
 * the cache.
 * 
 * @author Ben Wang  Date: Aug 16, 2005
 * @author Brian Stansberry
 * 
 * @version $Id: CacheHelper.java 87304 2009-04-14 18:26:35Z bstansberry@jboss.com $
 */
public class CacheHelper 
   extends ServiceMBeanSupport
   implements CacheHelperMBean
{   
   public static final String CACHE_CONFIG_PROP = "jbosstest.cluster.web.cache.config";
   public static final String CACHE_TYPE_PROP = "jbosstest.cluster.web.cache.pojo";
   
   public static final ObjectName OBJECT_NAME = 
      ObjectNameFactory.create("jboss.test:service=WebTestCacheHelper");
   
   public static final Integer VERSION_KEY = new Integer(0);   
   
   private String cacheConfigName;
   private Cache cache;
   private boolean usePojoCache;
   
   public CacheHelper()
   {      
   }
   
   public static Cache getCacheInstance(String cacheConfig, boolean usePojoCache)
   {
      try
      {
         if (usePojoCache)
         {
            PojoCacheManager cm = PojoCacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
            return cm.getPojoCache(cacheConfig, true).getCache();
         }
         else
         {
            CacheManager cm = CacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
            return cm.getCache(cacheConfig, true); 
         }
      }
      catch (RuntimeException re)
      {
         throw re;
      }
      catch (Exception e)
      {
         throw new RuntimeException("getCacheInstance: Exception: " +e);
      }
   }
   
   public void setCacheConfigName(String cacheConfigName, boolean usePojoCache)
   {
      if (this.cacheConfigName == null || !this.cacheConfigName.equals(cacheConfigName))
         releaseCache();
      this.cacheConfigName = cacheConfigName;
      this.usePojoCache = usePojoCache;
      getCache();
   }
   
   public Object getSessionVersion(String sessionFqn)
   {
      return getCache().get(Fqn.fromString(sessionFqn), VERSION_KEY);
   }
   
   public Object getBuddySessionVersion(String sessionFqn) throws Exception
   {
      Object result = null;

      Fqn fqn = Fqn.fromString(sessionFqn);
      
      Set buddies = getBuddyBackupRoots();
      for (Iterator iter = buddies.iterator(); iter.hasNext();)
      {
         Node buddy = (Node) iter.next();
         Node session = buddy.getChild(fqn);
         if (session != null)
         {
            result = (Integer) session.get(VERSION_KEY);
            break;
         }
      }

      return result;
   }
   
   public Set getSessionIds(String warFqn) throws Exception
   {
      return getSessionIds(warFqn, true);
   }
   
   public Set getSessionIds(String warFqn, boolean includeBuddies) throws Exception
   {
      Set result = new HashSet();
      
      Fqn fqn = Fqn.fromString(warFqn);
      Node main = getCache().getRoot().getChild(fqn);
      if (main != null)
      {
         result.addAll(main.getChildrenNames());
      }
      
      if (includeBuddies)
      {
         //    Check in the buddy backup tree
         
         Set buddies = getBuddyBackupRoots();
         for (Iterator iter = buddies.iterator(); iter.hasNext();)
         {
            Node buddy = (Node) iter.next();
            Node warRoot = buddy.getChild(fqn);
            if (warRoot != null)
            {
               result.addAll(warRoot.getChildrenNames());
            }
         }
      }
      return result;
   }
   
   public Set getSSOIds() throws Exception
   {
      Set result = new HashSet();
      
      Fqn fqn = Fqn.fromString("/SSO");
      Node main = getCache().getRoot().getChild(fqn);
      if (main != null)
      {
         result.addAll(main.getChildrenNames());
      }
      
      // Check in the buddy backup tree
      
      Set buddies = getBuddyBackupRoots();
      for (Iterator iter = buddies.iterator(); iter.hasNext();)
      {
         Node buddy = (Node) iter.next();
         Node ssoRoot = buddy.getChild(fqn);
         if (ssoRoot != null)
         {
            result.addAll(ssoRoot.getChildrenNames());
         }
      }
      
      return result;
   }
   
   public boolean getCacheHasSSO(String ssoId) throws Exception
   {
      Fqn fqn = Fqn.fromString("/SSO/" + ssoId);
      Node main = getCache().getRoot().getChild(fqn);
      if (main != null)
         return true;
      
      // Check in the buddy backup tree
      
      Set buddies = getBuddyBackupRoots();
      for (Iterator iter = buddies.iterator(); iter.hasNext();)
      {
         Node buddy = (Node) iter.next();
         Node ssoRoot = buddy.getChild(fqn);
         if (ssoRoot != null)
         {
            return true;
         }
      }
      
      return false;
   }
   
   public void startService() throws Exception
   {
      super.startService();
      
      cacheConfigName = System.getProperty(CACHE_CONFIG_PROP);
      String pojo = System.getProperty(CACHE_TYPE_PROP, "false");
      usePojoCache = Boolean.parseBoolean(pojo);
      
      getLog().debug("cacheConfigName=" + cacheConfigName + 
                     " and usePojoCache=" + usePojoCache);
      
      // Force a gc to try to clear weak refs that screw up pojocache tests
      if (usePojoCache)
         System.gc();
   }
   
   public void stopService() throws Exception
   {
      super.stopService();
      releaseCache();
   }
   
   private void releaseCache()
   {
      if (cache != null && cacheConfigName != null)
      {
         if (usePojoCache)
         {
            PojoCacheManager cm = PojoCacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
            cm.releaseCache(cacheConfigName);
         }
         else
         {
            CacheManager cm = CacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
            cm.releaseCache(cacheConfigName); 
         }
         
         cache = null;
      }      
   }
   

   private Set getBuddyBackupRoots()
   {
      Set buddies = null;
      Node buddyRoot = getCache().getRoot().getChild(BuddyManager.BUDDY_BACKUP_SUBTREE_FQN);
      if (buddyRoot != null)
      {
         buddies = buddyRoot.getChildren();
      }
      else
      {
         buddies = Collections.EMPTY_SET;
      }
      return buddies;
   }
   
   private Cache getCache()
   {
      if (cache == null)
      {
         
         getLog().debug("Getting cache: cacheConfigName=" + cacheConfigName + 
                        " and usePojoCache=" + usePojoCache);
         cache = getCacheInstance(cacheConfigName, usePojoCache);
         if (cache.getCacheStatus() != CacheStatus.STARTED)
            cache.start();
      }
      return cache;
   }
}
