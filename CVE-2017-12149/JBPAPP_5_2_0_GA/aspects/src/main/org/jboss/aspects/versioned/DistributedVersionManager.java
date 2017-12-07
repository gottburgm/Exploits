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
package org.jboss.aspects.versioned;

import org.jboss.aop.Advised;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.InstanceAdvised;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class DistributedVersionManager extends VersionManager
{
   protected static Logger log = Logger.getLogger(DistributedVersionManager.class);
   protected SynchronizationManager synchManager;
   protected long timeout;

   public DistributedVersionManager(long timeout, SynchronizationManager synchManager)
   {
      this.synchManager = synchManager;
      this.timeout = timeout;
   }

   public boolean isVersioned(Object obj)
   {
      if (!(obj instanceof InstanceAdvised)) return false;
      InstanceAdvised advised = (InstanceAdvised)obj;
      return getGUID(advised) != null;
   }

   public GUID tag(InstanceAdvised advised)
   {
      GUID guid = new GUID();
      org.jboss.aop.metadata.SimpleMetaData metaData = advised._getInstanceAdvisor().getMetaData();
      metaData.addMetaData(VERSION_MANAGER, VERSION_ID, guid);
      return guid;
   }

   public void untag(InstanceAdvised advised)
   {
      org.jboss.aop.metadata.SimpleMetaData metaData = advised._getInstanceAdvisor().getMetaData();
      metaData.removeMetaData(VERSION_MANAGER, VERSION_ID);
   }

   public List makeVersionedList(List list, ArrayList newObjects) throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(list.getClass());
      GUID guid = tag(proxy);
      DistributedListState manager = new DistributedListState(guid, timeout, proxy, list, this, synchManager);
      StateManager.setStateManager(proxy, manager);
      CollectionStateChangeInterceptor interceptor = new CollectionStateChangeInterceptor(manager);
      proxy._getInstanceAdvisor().appendInterceptor(interceptor);
      newObjects.add(manager);
      return (List)proxy;
   }


   public Map makeVersionedMap(Map map, ArrayList newObjects) throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(map.getClass());
      GUID guid = tag(proxy);
      DistributedMapState manager = new DistributedMapState(guid, timeout, proxy, map, this, synchManager);
      StateManager.setStateManager(proxy, manager);
      CollectionStateChangeInterceptor interceptor = new CollectionStateChangeInterceptor(manager);
      proxy._getInstanceAdvisor().appendInterceptor(interceptor);
      newObjects.add(manager);
      return (Map)proxy;
   }


   public Set makeVersionedSet(Set set, ArrayList newObjects) throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(set.getClass());
      GUID guid = tag(proxy);
      DistributedSetState manager = new DistributedSetState(guid, timeout, proxy, set, this, synchManager);
      StateManager.setStateManager(proxy, manager);
      CollectionStateChangeInterceptor interceptor = new CollectionStateChangeInterceptor(manager);
      proxy._getInstanceAdvisor().appendInterceptor(interceptor);
      newObjects.add(manager);
      return (Set)proxy;
   }


   public Object makeVersioned(Object obj)
      throws Exception
   {
      ArrayList newObjects = new ArrayList();
      obj = makeVersioned(obj, newObjects);
      synchManager.createObjects(newObjects);
      return obj;
   }

   Object makeVersioned(Object obj, ArrayList newObjects)
      throws Exception
   {
      // Proxies cannot be versioned
      if (obj instanceof ClassProxy) return obj;

      if (!(obj instanceof Advised))
      {
         if (obj instanceof List)
         {
            List list = (List)obj;
            return makeVersionedList(list, newObjects);
         }
         else if (obj instanceof Map)
         {
            Map map = (Map)obj;
            return makeVersionedMap(map, newObjects);
         }
         else if (obj instanceof Set)
         {
            Set set = (Set)obj;
            return makeVersionedSet(set, newObjects);
         }
         else
         {
            return obj;
         }
      }
      Advised advised = (Advised)obj;
      org.jboss.aop.metadata.SimpleMetaData metaData = advised._getInstanceAdvisor().getMetaData();
      GUID guid;
      synchronized (metaData)
      {
         if (isVersioned(advised)) return obj;
         guid = tag(advised);
      }
      System.out.println("VersionManager: " + guid);
      DistributedPOJOState manager = new DistributedPOJOState(guid, timeout, advised, this, synchManager);
      StateManager.setStateManager(advised, manager);
      StateChangeInterceptor interceptor = new StateChangeInterceptor(manager);
      manager.acquireWriteLock();
      advised._getInstanceAdvisor().appendInterceptor(interceptor);
      try
      {
         Field[] advisedFields = ((ClassAdvisor)advised._getAdvisor()).getAdvisedFields();
         for (int i = 0; i < advisedFields.length; i++)
         {
            Field field = advisedFields[i];
            if (Modifier.isStatic(field.getModifiers())) continue;
            Object fieldVal = field.get(advised);
            if (fieldVal != null)
            {
               if (fieldVal instanceof Advised)
               {
                  Advised fieldAdvised = (Advised)fieldVal;
                  makeVersioned(fieldAdvised, newObjects);
                  fieldVal = new VersionReference(getGUID(fieldAdvised), fieldAdvised);
               }
               else if (fieldVal instanceof List)
               {
                  List list = (List)fieldVal;
                  InstanceAdvised instanceAdvised = (InstanceAdvised)makeVersionedList(list, newObjects);
                  fieldVal = new VersionReference(getGUID(instanceAdvised), instanceAdvised);
               }
               else if (fieldVal instanceof Map)
               {
                  Map map = (Map)fieldVal;
                  InstanceAdvised instanceAdvised = (InstanceAdvised)makeVersionedMap(map, newObjects);
                  fieldVal = new VersionReference(getGUID(instanceAdvised), instanceAdvised);
               }
               else if (fieldVal instanceof Set)
               {
                  Set set = (Set)fieldVal;
                  InstanceAdvised instanceAdvised = (InstanceAdvised)makeVersionedSet(set, newObjects);
                  fieldVal = new VersionReference(getGUID(instanceAdvised), instanceAdvised);
               }
            }
            manager.fieldMap.put(new Integer(i), new DistributedFieldUpdate(fieldVal, 0, i));
         }
         newObjects.add(manager);
         return advised;
      }
      finally
      {
         manager.releaseWriteLock();
      }
   }

   /**
    * This is used by DistributedState.buildObject when the DistributedState object is
    * serialized across the wire and must recreate the object it represents
    */
   public void addVersioning(DistributedPOJOState manager, Advised advised)
   {
      StateManager.setStateManager(advised, manager);
      StateChangeInterceptor interceptor = new StateChangeInterceptor(manager);
      org.jboss.aop.metadata.SimpleMetaData metaData = advised._getInstanceAdvisor().getMetaData();
      metaData.addMetaData(VERSION_MANAGER, VERSION_ID, manager.getGUID());
      advised._getInstanceAdvisor().appendInterceptor(interceptor);
   }

   /**
    * This is used by DistributedState.buildObject when the DistributedState object is
    * serialized across the wire and must recreate the object it represents
    */
   public ClassProxy addListVersioning(List list, DistributedListState manager) throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(list.getClass());
      GUID guid = manager.getGUID();
      org.jboss.aop.metadata.SimpleMetaData metaData = proxy._getInstanceAdvisor().getMetaData();
      metaData.addMetaData(VERSION_MANAGER, VERSION_ID, guid);
      StateManager.setStateManager(proxy, manager);
      CollectionStateChangeInterceptor interceptor = new CollectionStateChangeInterceptor(manager);
      proxy._getInstanceAdvisor().appendInterceptor(interceptor);
      return proxy;
   }

   /**
    * This is used by DistributedState.buildObject when the DistributedState object is
    * serialized across the wire and must recreate the object it represents
    */
   public ClassProxy addMapVersioning(Map map, DistributedMapState manager) throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(map.getClass());
      GUID guid = manager.getGUID();
      org.jboss.aop.metadata.SimpleMetaData metaData = proxy._getInstanceAdvisor().getMetaData();
      metaData.addMetaData(VERSION_MANAGER, VERSION_ID, guid);
      StateManager.setStateManager(proxy, manager);
      CollectionStateChangeInterceptor interceptor = new CollectionStateChangeInterceptor(manager);
      proxy._getInstanceAdvisor().appendInterceptor(interceptor);
      return proxy;
   }

   /**
    * This is used by DistributedState.buildObject when the DistributedState object is
    * serialized across the wire and must recreate the object it represents
    */
   public ClassProxy addSetVersioning(Set set, DistributedSetState manager) throws Exception
   {
      ClassProxy proxy = ClassProxyFactory.newInstance(set.getClass());
      GUID guid = manager.getGUID();
      org.jboss.aop.metadata.SimpleMetaData metaData = proxy._getInstanceAdvisor().getMetaData();
      metaData.addMetaData(VERSION_MANAGER, VERSION_ID, guid);
      StateManager.setStateManager(proxy, manager);
      CollectionStateChangeInterceptor interceptor = new CollectionStateChangeInterceptor(manager);
      proxy._getInstanceAdvisor().appendInterceptor(interceptor);
      return proxy;
   }
}
