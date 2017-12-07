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
package org.jboss.test.cluster.clusteredentity.classloader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheManager;
import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeCreated;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodeVisited;
import org.jboss.cache.notifications.event.NodeCreatedEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodeVisitedEvent;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ha.framework.server.CacheManagerLocator;
import org.jboss.logging.Logger;

/**
 * Comment
 * 
 * @author Brian Stansberry
 * @version $Revision: 60233 $
 */
@Stateful
@Remote(EntityQueryTest.class)
@RemoteBinding(jndiBinding="EntityQueryTestBean/remote")
public class EntityQueryTestBean implements EntityQueryTest
{
   private static final Logger log = Logger.getLogger(EntityQueryTestBean.class);
   
   @PersistenceContext
   private EntityManager manager;
   
   private String cacheConfigName;
   
   private transient Cache<Object, Object> cache;
   
   private MyListener listener;

   public EntityQueryTestBean()
   {      
   }
   
   public void getCache(boolean optimistic)
   {
      if (optimistic)
         cacheConfigName = "optimistic-shared";
      else
         cacheConfigName = "pessimistic-shared";

      try
      {
         //Just to initialise the cache with a listener
         Cache<Object, Object> cache = getCache();
         listener = new MyListener();
         cache.addCacheListener(listener);         
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public void updateAccountBranch(Integer id, String branch)
   {
      Account account = (Account) manager.find(Account.class, id);
      account.setBranch(branch);
   }
   
   public int getCountForBranch(String branch, boolean useNamed, boolean useRegion)
   {
      if (useNamed)
         return getCountForBranchViaNamedQuery(branch, useRegion);
      else
         return getCountForBranchViaLocalQuery(branch, useRegion);
   }
   
   private int getCountForBranchViaLocalQuery(String branch, boolean useRegion)
   {
      Query query = manager.createQuery("select account from Account as account where account.branch = ?1");
      query.setParameter(1, branch);
      if (useRegion)
      {
         query.setHint("org.hibernate.cacheRegion", "AccountRegion");
      }
      query.setHint("org.hibernate.cacheable", Boolean.TRUE);
      return query.getResultList().size();
      
   }
   
   private int getCountForBranchViaNamedQuery(String branch, boolean useRegion)
   {
      String queryName = useRegion ? "account.bybranch.namedregion"
                                   : "account.bybranch.default";
      Query query = manager.createNamedQuery(queryName);
      query.setParameter(1, branch);
      return query.getResultList().size();      
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.clusteredentity.EntityQueryTest#createAccount(org.jboss.ejb3.test.clusteredentity.AccountHolderPK, Integer, Integer)
    */
   public void createAccount(AccountHolderPK pk, Integer id, Integer openingBalance, String branch)
   {
      Account account = new Account();
      account.setId(id);
      account.setAccountHolder(pk);
      account.setBalance(openingBalance);
      account.setBranch(branch);
      manager.persist(account);
   }
   
   public void updateAccountBalance(Integer id, Integer newBalance)
   {
      Account account = (Account) manager.find(Account.class, id);
      account.setBalance(newBalance);
   }
   
   public String getBranch(AccountHolderPK pk, boolean useNamed, boolean useRegion)
   {
      if (useNamed)
         return getBranchViaNamedQuery(pk, useRegion);
      else
         return getBranchViaLocalQuery(pk, useRegion);
   }
   
   private String getBranchViaLocalQuery(AccountHolderPK pk, boolean useRegion)
   {
      Query query = manager.createQuery("select account.branch from Account as account where account.accountHolder = ?1");
      query.setParameter(1, pk);
      if (useRegion)
      {
         query.setHint("org.hibernate.cacheRegion", "AccountRegion");
      }
      query.setHint("org.hibernate.cacheable", Boolean.TRUE);
      return (String) query.getResultList().get(0);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.clusteredentity.EntityQueryTest#getPostCodeViaNamedQuery(org.jboss.ejb3.test.clusteredentity.AccountHolderPK, boolean)
    */
   private String getBranchViaNamedQuery(AccountHolderPK pk, boolean useRegion)
   {
      String queryName = useRegion ? "account.branch.namedregion"
                                   : "account.branch.default";
      Query query = manager.createNamedQuery(queryName);
      query.setParameter(1, pk);
      return (String) query.getResultList().get(0);
   }
   public int getTotalBalance(AccountHolderPK pk, boolean useNamed, boolean useRegion)
   {
      if (useNamed)
         return getTotalBalanceViaNamedQuery(pk, useRegion);
      else
         return getTotalBalanceViaLocalQuery(pk, useRegion);
   }
   
   private int getTotalBalanceViaLocalQuery(AccountHolderPK pk, boolean useRegion)
   {
      Query query = manager.createQuery("select account.balance from Account as account where account.accountHolder = ?1");
      query.setParameter(1, pk);
      query.setHint("org.hibernate.cacheable", Boolean.TRUE);
      return totalBalances(query);
   }
   
   private int getTotalBalanceViaNamedQuery(AccountHolderPK pk, boolean useRegion)
   {
      String queryName = useRegion ? "account.totalbalance.namedregion"
                                   : "account.totalbalance.default";
      Query query = manager.createNamedQuery(queryName);
      query.setParameter(1, pk);
      return totalBalances(query);
   }
   
   private int totalBalances(Query balanceQuery)
   {
      List results = balanceQuery.getResultList();
      int total = 0;
      if (results != null)
      {
         for (Iterator it = results.iterator(); it.hasNext();)
         {            
            total += ((Integer) it.next()).intValue();
            System.out.println("Total = " + total);
         }
      }
      return total;      
   }
   
   public boolean getSawRegionModification(String regionName)
   {
      return getSawRegion(regionName, listener.modified);
   }
   
   public boolean getSawRegionAccess(String regionName)
   {
      return getSawRegion(regionName, listener.accessed);
   }
   
   private boolean getSawRegion(String regionName, Set<Fqn<String>> sawEvent)
   {
      boolean saw = false;      
      for (Iterator<Fqn<String>> it = sawEvent.iterator(); it.hasNext();)
      {
         Fqn<String> modified = (Fqn<String>) it.next();
         if (modified.toString().indexOf(regionName) > -1)
         {
            it.remove();
            saw = true;
         }
      }
      return saw;
      
   }
   
   public void cleanup()
   {
      internalCleanup();
   }
   
   private void internalCleanup()
   {  
      if (manager != null)
      {
         Query query = manager.createQuery("select account from Account as account");
         List accts = query.getResultList();
         if (accts != null)
         {
            for (Iterator it = accts.iterator(); it.hasNext();)
            {
               try
               {
                  Account acct = (Account) it.next();
                  log.info("Removing " + acct);
                  manager.remove(acct);
               }
               catch (Exception ignored) {}
            }
         }
      }      
   }
   
   @PreDestroy
   @Remove
   public void remove(boolean removeEntities)
   {
      if (removeEntities)
      {
         try
         {
            internalCleanup();
         }
         catch (Exception e)
         {
            log.error("Caught exception in remove", e);
         }
      }
      
      try
      {
         listener.clear();
         getCache().removeCacheListener(listener);         
      }
      catch (Exception e)
      {
        log.error("Caught exception in remove", e);
      }
      
      try
      {
         if (cache != null)
            CacheManagerLocator.getCacheManagerLocator().getCacheManager(null).releaseCache(cacheConfigName);
      }
      catch (Exception e)
      {
         log.error("Caught exception releasing cache", e);
      }
   }

   private Cache<Object, Object> getCache() throws Exception
   {
      if (cache == null && cacheConfigName != null)
      {
         CacheManager cm = CacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
         cache = cm.getCache(cacheConfigName, true);
         cache.start();
      }
      return cache;
   }

   @CacheListener
   public class MyListener
   {
      HashSet<Fqn<String>> modified = new HashSet<Fqn<String>>(); 
      HashSet<Fqn<String>> accessed = new HashSet<Fqn<String>>();
      
      public void clear()
      {
         modified.clear();
         accessed.clear();
      }
      
      @NodeModified
      public void nodeModified(NodeModifiedEvent event)
      {
         if (!event.isPre())
         {
            Fqn<String> fqn = event.getFqn();
            System.out.println("MyListener - Modified node " + fqn.toString());
            modified.add(fqn);
         }
      }

      @NodeCreated
      public void nodeCreated(NodeCreatedEvent event)
      {   
         if (!event.isPre())
         {
            Fqn<String> fqn = event.getFqn();
            System.out.println("MyListener - Created node " + fqn.toString());
            modified.add(fqn);
         }
      }   

      @NodeVisited
      public void nodeVisited(NodeVisitedEvent event)
      {   
         if (!event.isPre())
         {
            Fqn<String> fqn = event.getFqn();
            System.out.println("MyListener - Visited node " + fqn.toString());
            accessed.add(fqn); 
         }
      }    
      
   }
}
