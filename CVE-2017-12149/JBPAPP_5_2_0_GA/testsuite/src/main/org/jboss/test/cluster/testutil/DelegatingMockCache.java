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
package org.jboss.test.cluster.testutil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;
import org.jboss.cache.InvocationContext;
import org.jboss.cache.Node;
import org.jboss.cache.NodeNotExistsException;
import org.jboss.cache.Region;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.interceptors.base.CommandInterceptor;
import org.jgroups.Address;

/**
 * Cache impl that delegates all calls to a passed in object.
 * Meant to provide a base for overriding methods.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 86278 $
 */
public class DelegatingMockCache<K, V> implements Cache<K, V>
{

   private final Cache<K, V> delegate;
   
   public DelegatingMockCache(Cache<K, V> delegate) 
   {
      super();
      this.delegate = delegate;
   }
   
   public void addInterceptor(CommandInterceptor arg0, Class arg1)
   {
      delegate.addInterceptor(arg0, arg1);      
   }

   public void addInterceptor(CommandInterceptor arg0, int arg1)
   {
      delegate.addInterceptor(arg0, arg1);      
   }

   public void removeInterceptor(int arg0)
   {
      delegate.removeInterceptor(arg0);      
   }

   public void startBatch()
   {
      delegate.startBatch();    
   }

   public void endBatch(boolean arg0)
   {
      delegate.endBatch(arg0);      
   }

   public void removeInterceptor(Class arg0)
   {
      // TODO Auto-generated method stub
      
   }
   
//   public void addInterceptor(Interceptor arg0, int arg1)
//   {
//      delegate.addInterceptor(arg0, arg1);
//   }
//
//   public void addInterceptor(Interceptor arg0, Class arg1)
//   {
//      delegate.addInterceptor(arg0, arg1);
//   }
//
//   public boolean exists(Fqn arg0)
//   {
//      return delegate.exists(arg0);
//   }
//
//   public boolean exists(String arg0)
//   {
//      return delegate.exists(arg0);
//   }

//   public void fetchPartialState(List arg0, Fqn arg1) throws Exception
//   {
//      delegate.fetchPartialState(arg0, arg1);
//   }
//
//   public void fetchPartialState(List arg0, Fqn arg1, Fqn arg2) throws Exception
//   {
//      delegate.fetchPartialState(arg0, arg1, arg2);
//   }

//   public BuddyManager getBuddyManager()
//   {
//      return delegate.getBuddyManager();
//   }
//
//   public CacheLoaderManager getCacheLoaderManager()
//   {
//      return delegate.getCacheLoaderManager();
//   }
//
//   public Set getChildrenNames(Fqn arg0)
//   {
//      return delegate.getChildrenNames(arg0);
//   }
//
//   public Set getChildrenNames(String arg0)
//   {
//      return delegate.getChildrenNames(arg0);
//   }
//
//   public String getClusterName()
//   {
//      return delegate.getClusterName();
//   }
//
//   public GlobalTransaction getCurrentTransaction()
//   {
//      return delegate.getCurrentTransaction();
//   }
//
//   public GlobalTransaction getCurrentTransaction(Transaction arg0, boolean arg1)
//   {
//      return delegate.getCurrentTransaction(arg0, arg1);
//   }
//
//   public List getInterceptorChain()
//   {
//      return delegate.getInterceptorChain();
//   }
//
//   public Set getInternalFqns()
//   {
//      return delegate.getInternalFqns();
//   }

//   public Map getLockTable()
//   {
//      return delegate.getLockTable();
//   }

//   public Marshaller getMarshaller()
//   {
//      return delegate.getMarshaller();
//   }
//
//   public Notifier getNotifier()
//   {
//      return delegate.getNotifier();
//   }
//
//   public int getNumberOfAttributes()
//   {
//      return delegate.getNumberOfAttributes();
//   }
//
//   public int getNumberOfLocksHeld()
//   {
//      return delegate.getNumberOfLocksHeld();
//   }
//
//   public int getNumberOfNodes()
//   {
//      return delegate.getNumberOfNodes();
//   }
//
//   public RPCManager getRPCManager()
//   {
//      return delegate.getRPCManager();
//   }
//
//   public RegionManager getRegionManager()
//   {
//      return delegate.getRegionManager();
//   }
//
//   public NodeSPI getRoot()
//   {
//      return delegate.getRoot();
//   }
//
//   public StateTransferManager getStateTransferManager()
//   {
//      return delegate.getStateTransferManager();
//   }
//
//   public TransactionManager getTransactionManager()
//   {
//      return delegate.getTransactionManager();
//   }
//
//   public TransactionTable getTransactionTable()
//   {
//      return delegate.getTransactionTable();
//   }
//
//   public GravitateResult gravitateData(Fqn arg0, boolean arg1)
//   {
//      return delegate.gravitateData(arg0, arg1);
//   }
//
//   public NodeSPI peek(Fqn arg0, boolean arg1)
//   {
//      return delegate.peek(arg0, arg1);
//   }
//
//   public NodeSPI peek(Fqn arg0, boolean arg1, boolean arg2)
//   {
//      return delegate.peek(arg0, arg1, arg2);
//   }
//
//   public void removeInterceptor(int arg0)
//   {
//      delegate.removeInterceptor(arg0);
//   }
//
//   public void removeInterceptor(Class arg0)
//   {
//      delegate.removeInterceptor(arg0);
//   }

   public Node getNode(Fqn arg0)
   {
      return delegate.getNode(arg0);
   }

   public Node getNode(String arg0)
   {
      return delegate.getNode(arg0);
   }

   public Node getRoot()
   {
      return delegate.getRoot();
   }

   public void addCacheListener(Object arg0)
   {
      delegate.addCacheListener(arg0);
   }

   public void addCacheListener(Fqn arg0, Object arg1)
   {
      //delegate.addCacheListener(arg0, arg1);
   }

   public void clearData(String arg0)
   {
      delegate.clearData(arg0);
   }

   public void clearData(Fqn arg0)
   {
      delegate.clearData(arg0);
   }

   public void create() throws CacheException
   {
      delegate.create();
   }

   public void destroy()
   {
      delegate.destroy();
   }

   public void evict(Fqn arg0)
   {
      delegate.evict(arg0);
   }

   public void evict(Fqn arg0, boolean arg1)
   {
      delegate.evict(arg0, arg1);
   }

   public V get(Fqn arg0, K arg1)
   {
      return delegate.get(arg0, arg1);
   }

   public V get(String arg0, K arg1)
   {
      return delegate.get(arg0, arg1);
   }

   public Set getCacheListeners()
   {
      return delegate.getCacheListeners();
   }

   public Set getCacheListeners(Fqn arg0)
   {
      return new HashSet<Object>();
   }

   public CacheStatus getCacheStatus()
   {
      return delegate.getCacheStatus();
   }

   public Configuration getConfiguration()
   {
      return delegate.getConfiguration();
   }

   public Map getData(Fqn arg0)
   {
      return delegate.getData(arg0);
   }

   public InvocationContext getInvocationContext()
   {
      return delegate.getInvocationContext();
   }

   public Set getKeys(String arg0)
   {
      return delegate.getKeys(arg0);
   }

   public Set getKeys(Fqn arg0)
   {
      return delegate.getKeys(arg0);
   }

   public Address getLocalAddress()
   {
      return delegate.getLocalAddress();
   }

   public List getMembers()
   {
      return delegate.getMembers();
   }

//   public NodeSPI getNode(Fqn arg0)
//   {
//      return delegate.getNode(arg0);
//   }
//
//   public NodeSPI getNode(String arg0)
//   {
//      return delegate.getNode(arg0);
//   }

   public Region getRegion(Fqn arg0, boolean arg1)
   {
      return delegate.getRegion(arg0, arg1);
   }

   public String getVersion()
   {
      return delegate.getVersion();
   }

   public void move(Fqn arg0, Fqn arg1) throws NodeNotExistsException
   {
      delegate.move(arg0, arg1);
   }

   public void move(String arg0, String arg1) throws NodeNotExistsException
   {
      delegate.move(arg0, arg1);
   }

   public void put(Fqn arg0, Map arg1)
   {
      delegate.put(arg0, arg1);
   }

   public void put(String arg0, Map arg1)
   {
      delegate.put(arg0, arg1);
   }

   public V put(Fqn arg0, K arg1, V arg2)
   {
      return delegate.put(arg0, arg1, arg2);
   }

   public V put(String arg0, K arg1, V arg2)
   {
      return delegate.put(arg0, arg1, arg2);
   }

   public void putForExternalRead(Fqn arg0, K arg1, V arg2)
   {
      delegate.putForExternalRead(arg0, arg1, arg2);
   }

   public V remove(Fqn arg0, K arg1)
   {
      return delegate.remove(arg0, arg1);
   }

   public V remove(String arg0, K arg1)
   {
      return delegate.remove(arg0, arg1);
   }

   public void removeCacheListener(Object arg0)
   {
      delegate.removeCacheListener(arg0);
   }

   public void removeCacheListener(Fqn arg0, Object arg1)
   {
      //delegate.removeCacheListener(arg0, arg1);
   }

   public boolean removeNode(Fqn arg0)
   {
      return delegate.removeNode(arg0);
   }

   public boolean removeNode(String arg0)
   {
      return delegate.removeNode(arg0);
   }

   public boolean removeRegion(Fqn arg0)
   {
      return delegate.removeRegion(arg0);
   }

   public void setInvocationContext(InvocationContext arg0)
   {
      delegate.setInvocationContext(arg0);
   }

   public void start() throws CacheException
   {
      delegate.start();
   }

   public void stop()
   {
      delegate.stop();
   }

   public Set<Object> getChildrenNames(Fqn arg0)
   {
      // FIXME just delegate once we use JBC 3.1
      Node n = delegate.getRoot().getChild(arg0);
      return n == null ? Collections.emptySet() : n.getChildrenNames();
   }

   public Set<String> getChildrenNames(String arg0)
   {
      // FIXME just delegate once we use JBC 3.1
      Set<Object> names = getChildrenNames(Fqn.fromString(arg0));
      Set<String> result = new HashSet<String>();
      for (Object name : names)
      {
         result.add(name.toString());
      }
      return result;
   }

   public boolean isLeaf(Fqn arg0)
   {
      // FIXME just delegate once we use JBC 3.1
      return getChildrenNames(arg0).size() == 0;
   }

   public boolean isLeaf(String arg0)
   {
      // FIXME just delegate once we use JBC 3.1
      return isLeaf(Fqn.fromString(arg0));
   }

}
