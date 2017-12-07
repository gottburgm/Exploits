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
package org.jboss.test.cluster.clusteredentity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheManager;
import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeVisited;
import org.jboss.cache.notifications.event.NodeVisitedEvent;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ha.framework.server.CacheManagerLocator;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 96150 $
 */
@Stateful
@Remote(EntityTest.class)
@RemoteBinding(jndiBinding="EntityTestBean/remote")
public class EntityTestBean implements EntityTest
{
   private static final Logger log = Logger.getLogger(EntityTestBean.class);
   
   @PersistenceContext
   private EntityManager manager;
   
   private String cacheConfigName;
   
   private transient Cache<Object, Object> cache;
   
   private transient MyListener listener;

   public EntityTestBean()
   {
   }
   
   public void getCache(String cacheConfigName)
   {
      this.cacheConfigName = cacheConfigName;

      try
      {
         if (cache == null && cacheConfigName != null)
         {
            CacheManager cm = CacheManagerLocator.getCacheManagerLocator().getCacheManager(null);
            cache = cm.getCache(cacheConfigName, true);
            cache.start();
         }
         
         if (listener == null)
         {
            listener = new MyListener();
            cache.addCacheListener(listener);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public Customer createCustomer()
   {
      System.out.println("CREATE CUSTOMER");
      try
      {
         listener.clear();
         
         Customer customer = new Customer();
         customer.setId(new Integer(1));
         customer.setName("JBoss");
         Set<Contact> contacts = new HashSet<Contact>();
         
         Contact kabir = new Contact();
         kabir.setId(new Integer(1));
         kabir.setCustomer(customer);
         kabir.setName("Kabir");
         kabir.setTlf("1111");
         contacts.add(kabir);
         
         Contact bill = new Contact();
         bill.setId(new Integer(2));
         bill.setCustomer(customer);
         bill.setName("Bill");
         bill.setTlf("2222");
         contacts.add(bill);

         customer.setContacts(contacts);

         manager.persist(customer);
         return customer;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         System.out.println("CREATE CUSTOMER -  END");         
      }
   }

   public Customer findByCustomerId(Integer id)
   {
      System.out.println("FIND CUSTOMER");         
      listener.clear();
      try
      {
         Customer customer = manager.find(Customer.class, id);
         
         return customer;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         System.out.println("FIND CUSTOMER -  END");         
      }
   }
   
   public String loadedFromCache()
   {
      System.out.println("CHECK CACHE");         
      try
      {
         System.out.println("Visited: " + listener.visited);
         if (!listener.visited.contains("Customer#1"))
            return "Customer#1 was not in cache";
         if (!listener.visited.contains("Contact#1"))
            return "Contact#1 was not in cache";
         if (!listener.visited.contains("Contact#2"))
            return "Contact2#1 was not in cache";
         if (!listener.visited.contains("Customer.contacts#1"))
            return "Customer.contacts#1 was not in cache";
         return null;
      }
      finally
      {
         System.out.println("CHECK CACHE -  END");         
      }
      
   }
   
   @PreDestroy
   @Remove
   public void cleanup()
   {
      try
      {         
         if (cache != null && listener != null)
         {
            cache.removeCacheListener(listener);
         }
      }
      catch (Exception e)
      {
         log.error("Caught exception in cleanup", e);
      }
      
      try
      {
         if (manager != null)
         {
            Customer c = findByCustomerId(new Integer(1));
            if (c != null)
            {
               Set contacts = c.getContacts();
               for (Iterator it = contacts.iterator(); it.hasNext();)
                  manager.remove(it.next());
               c.setContacts(null);
               manager.remove(c);
            }
         }
      }
      catch (Exception e)
      {
         log.error("Caught exception in cleanup", e);
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

   @CacheListener
   public class MyListener
   {
      HashSet<String> visited = new HashSet<String>(); 
      
      public void clear()
      {
         visited.clear();
      }
      
      @NodeVisited
      public void nodeVisited(NodeVisitedEvent event)
      {
         if (!event.isPre())
         {
            Fqn fqn = event.getFqn();
            System.out.println("MyListener - Visiting node " + fqn.toString());
            String name = fqn.toString();
            String token = ".clusteredentity.";
            int index = name.indexOf(token);
            if (index > -1)
            {
               index += token.length();
               name = name.substring(index);
               System.out.println("MyListener - recording visit to " + name);
               visited.add(name);
            }
         }
      }
   }
}
