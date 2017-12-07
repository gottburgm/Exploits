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
package org.jboss.test.cluster.cache.bean;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.lock.IsolationLevel;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 * Proxy to the TreeCache MBean. Mainly used to be able to transport transactions from a test
 * client to a TreeCache.
 *
 * @version $Revision: 81036 $
 * @ejb.bean type="Stateful"
 * name="test/TreeCacheTester"
 * jndi-name="ejb/test/TreeCacheTester"
 * view-type="remote"
 * @ejb.transaction type="Supports"
 */
public class TreeCacheTesterBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 171897143867111119L;
   Cache cache = null;

   /**
    * @throws CreateException
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
      log("I'm being created");
   }

   /**
    * @param cluster_name
    * @param props
    * @param caching_mode
    * @throws CreateException
    * @ejb.create-method
    */
   public void ejbCreate(String cluster_name,
                         String props,
                         int caching_mode) throws CreateException
   {
      try {
//            cache=new TreeCache(cluster_name, props, 10000);
         Configuration config = new Configuration();
         config.setClusterName(cluster_name);
         config.setClusterConfig(props);
         config.setCacheMode(Configuration.legacyModeToCacheMode(caching_mode));
         config.setTransactionManagerLookupClass("org.jboss.cache.JBossTransactionManagerLookup");
         
         //cache = DefaultCacheFactory.getInstance().createCache(config);
         
      } catch (Exception e) {
         throw new CreateException(e.toString());
      }
   }


//    /**
//     *
//     * @param name
//     * @ejb.create-method
//     */
//    public void ejbCreate(String name) throws CreateException {
//        MBeanServer server=null;
//        ObjectName cache_service;
//
//        try {
//            this.name=name;
//            cache_service=ObjectName.getInstance(name);
//
//            // is this the right way to get hold of the JBoss MBeanServer ?
//            List servers=MBeanServerFactory.findMBeanServer(null);
//            if(servers == null || servers.size() == 0)
//                throw new CreateException("TreeCacheTesterBean.ejbCreate(): no MBeanServers found");
//            server=(MBeanServer)servers.get(0);
//            cache=(TreeCacheMBean)MBeanProxy.create(TreeCacheMBean.class, cache_service, server);
//        }
//        catch(Exception ex) {
//            throw new CreateException(ex.toString());
//        }
//    }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
      log("I'm being removed");
      if (cache != null) {
         cache.stop();
         cache.destroy();
         cache = null;
      }
   }

   public void setSessionContext(SessionContext ctx)
   {
   }


   /**
    * @return
    * @ejb.interface-method
    */
   public Vector getMembers()
   {
      List members = cache.getMembers();
      return members == null ? null : new Vector(members);
   }


   /**
    * @param mode
    * @ejb.interface-method
    */
   public void setCacheMode(int mode) throws Exception
   {
      cache.getConfiguration().setCacheMode(Configuration.legacyModeToCacheMode(mode));
   }


   /**
    * @param level
    * @ejb.interface-method
    */
   public void setIsolationLevel(IsolationLevel level)
   {
      cache.getConfiguration().setIsolationLevel(level);
   }


   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public Set getKeys(String fqn) throws CacheException
   {
      Node node = cache.getRoot().getChild(Fqn.fromString(fqn));
      return node == null ? null : node.getKeys();
   }

   /**
    * @param fqn
    * @param key
    * @return
    * @ejb.interface-method
    */
   public Object get(String fqn, String key) throws CacheException {
      return cache.get(Fqn.fromString(fqn), key);
   }

   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public boolean exists(String fqn)
   {
      return cache.getRoot().hasChild(Fqn.fromString(fqn));
   }

   /**
    * @param fqn
    * @param data
    * @throws Exception
    * @ejb.interface-method
    */
   public void put(String fqn, Map data) throws Exception
   {
      cache.put(Fqn.fromString(fqn), data);
   }

   /**
    * @param fqn
    * @param key
    * @param value
    * @return
    * @throws Exception
    * @ejb.interface-method
    */
   public Object put(String fqn, String key, Object value) throws Exception
   {
      return cache.put(Fqn.fromString(fqn), key, value);
   }

   /**
    * @param fqn
    * @throws Exception
    * @ejb.interface-method
    */
   public void remove(String fqn) throws Exception
   {
      cache.removeNode(Fqn.fromString(fqn));
   }

   /**
    * @param fqn
    * @param key
    * @return
    * @throws Exception
    * @ejb.interface-method
    */
   public Object remove(String fqn, String key) throws Exception
   {
      return cache.remove(Fqn.fromString(fqn), key);
   }

//   /**
//    * @param fqn
//    * @ejb.interface-method
//    */
//   public void releaseAllLocks(String fqn)
//   {
//      cache.releaseAllLocks(fqn);
//   }
//
//   /**
//    * @param fqn
//    * @return
//    * @ejb.interface-method
//    */
//   public String print(String fqn)
//   {
//      return cache.print(fqn);
//   }

   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public Set getChildrenNames(String fqn) throws CacheException
   {
      Node node = cache.getRoot().getChild(Fqn.fromString(fqn));
      return (node == null ? null : node.getChildrenNames());
   }

//   /**
//    * @return
//    * @ejb.interface-method
//    */
//   public String printDetails()
//   {
//      return cache.printDetails();
//   }
//
//   /**
//    * @return
//    * @ejb.interface-method
//    */
//   public String printLockInfo()
//   {
//      return cache.printLockInfo();
//   }

   /**
    * @ejb.interface-method
    * @param members
    * @param method
    * @param args
    * @param synchronous
    * @param exclude_self
    * @param timeout
    * @return
    * @throws Exception
    */
//    public List callRemoteMethods(Vector members, Method method, Object[] args,
//                                  boolean synchronous, boolean exclude_self,
//                                  long timeout) throws Exception {
//        return cache.callRemoteMethods(members, method, args, synchronous,
//                                       exclude_self, timeout);
//    }

   /**
    * @param members
    * @param method_name
    * @param types
    * @param args
    * @param synchronous
    * @param exclude_self
    * @param timeout
    * @return
    * @throws Exception
    * @ejb.interface-method
    */
//    public List callRemoteMethods(Vector members, String method_name, Class[] types,
//                                  Object[] args, boolean synchronous,
//                                  boolean exclude_self, long timeout) throws Exception {
//        return cache.callRemoteMethods(members, method_name, types, args,
//                                       synchronous, exclude_self, timeout);
//    }

   private void log(String msg)
   {
      System.out.println("-- [" + Thread.currentThread().getName() + "]: " + msg);
   }

}
