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

import org.jboss.aop.InstanceAdvised;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;
import org.jboss.util.id.GUID;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class DistributedMapState extends CollectionStateManager implements Map, DistributedState, java.io.Externalizable
{
   private static final long serialVersionUID = -5397547850033533784L;

   private static HashMap mapMethodMap;

   protected static Logger log = Logger.getLogger(DistributedMapState.class);
   static
   {
      try
      {
         mapMethodMap = new HashMap();
         Method[] methods = Map.class.getDeclaredMethods();
         for (int i = 0; i < methods.length; i++)
         {
            long hash = org.jboss.aop.util.MethodHashing.methodHash(methods[i]);
            mapMethodMap.put(new Long(hash), methods[i]);
         }

      }
      catch (Exception ignored)
      {
         ignored.printStackTrace();
      }
   }

   protected volatile long versionId;
   protected HashMap updates;
   protected String classname;
   transient protected Map base;
   transient protected TransactionLocal txState = new TransactionLocal();
   transient protected TransactionLocal txVersion = new TransactionLocal();
   transient protected DistributedVersionManager versionManager;
   transient protected SynchronizationManager synchManager;
   transient protected TransactionManager tm;
   transient protected ClassProxy proxy;

   /**
    * For serialization
    */
   public DistributedMapState() {}


   public DistributedMapState(GUID guid, long timeout, ClassProxy proxy, Map obj, DistributedVersionManager versionManager, SynchronizationManager synchManager)
      throws Exception
   {
      super(guid, timeout, mapMethodMap);
      this.base = obj;
      this.classname = obj.getClass().getName();
      this.versionManager = versionManager;
      this.synchManager = synchManager;
      this.proxy = proxy;
      InitialContext ctx = new InitialContext();
      this.tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
      this.updates = createMapUpdates(base);
   }

   public HashMap getMethodMap()
   {
      return ClassProxyFactory.getMethodMap(base.getClass().getName());
   }

   public InstanceAdvised getObject() { return proxy; }

   // The Guts

   protected Map getCurrentState(boolean forUpdate) throws Exception
   {
      Transaction tx = tm.getTransaction();
      if (tx == null)
      {
         if (forUpdate) versionId++;
         return base;
      }

      Map state = (Map)txState.get(tx);
      if (state == null && forUpdate)
      {
         state = (Map)base.getClass().newInstance();
         state.putAll(base);
         txState.set(tx, state);
         long newId = versionId + 1;
         synchManager.registerUpdate(tx, this);
         txVersion.set(tx, new Long(newId));
         return state;
      }
      return base;
   }


   protected HashMap createMapUpdates(Map state)
   {
      HashMap mapUpdates = new HashMap();
      Iterator it = state.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         Object obj = entry.getValue();
         if (versionManager.isVersioned(obj))
         {
            mapUpdates.put(entry.getKey(), new VersionReference(VersionManager.getGUID((InstanceAdvised)obj)));
         }
         else
         {
            mapUpdates.put(entry.getKey(), obj);
         }
      }
      return mapUpdates;
   }

   public DistributedUpdate createTxUpdate(Transaction tx)
   {
      Map state = (Map)txState.get(tx);
      long newId = ((Long)txVersion.get(tx)).longValue();
      DistributedMapUpdate update = new DistributedMapUpdate(guid, createMapUpdates(state), newId);
      return update;
   }

   public InstanceAdvised buildObject(SynchronizationManager manager, DistributedVersionManager versionManager)
      throws Exception
   {
      log.trace("building a Map");
      this.versionManager = versionManager;
      this.synchManager = manager;
      log.trace("DistributedMaptState: classname: " + classname);
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(classname);
      base = (Map)clazz.newInstance();
      Iterator it = updates.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         Object val = entry.getValue();
         if (val instanceof VersionReference)
         {
            VersionReference ref = (VersionReference)val;
            val = manager.getObject(ref.getGUID());
            if (val == null)
            {
               DistributedState fieldVal = manager.getState(ref.getGUID());
               val = fieldVal.buildObject(manager, versionManager);
               ref.set((InstanceAdvised)val);
            }
         }
         base.put(entry.getKey(), val);
      }
      proxy = versionManager.addMapVersioning(base, this);
      return proxy;
   }

   public void checkOptimisticLock(Transaction tx)
   {
      // NOTE THIS CODE ASSUMES THAT A WRITELOCK HAS BEEN ACQUIRED!!!!
      Long version = (Long)txVersion.get(tx);
      if (version.longValue() <= versionId)
         throw new OptimisticLockFailure("optimistic lock failure for list");
   }

   public void mergeState(Transaction tx) throws Exception
   {
      // NOTE THIS CODE ASSUMES THAT A WRITELOCK HAS BEEN ACQUIRED!!!!
      Map current = (Map)txState.get(tx);
      base = current;
      Long version = (Long)txVersion.get(tx);
      versionId = version.longValue();
   }

   public void mergeState(DistributedUpdate update) throws Exception
   {
      DistributedMapUpdate mapUpdate = (DistributedMapUpdate)update;
      this.versionId = mapUpdate.versionId;
      base.clear();
      Iterator it = mapUpdate.mapUpdates.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         Object val = entry.getValue();
         if (val instanceof VersionReference)
         {
            VersionReference ref = (VersionReference)val;
            val = synchManager.getObject(ref.getGUID());
            ref.set((InstanceAdvised)val);
         }
         base.put(entry.getKey(), val);
      }
      updates = mapUpdate.mapUpdates;
   }

   // java.util.Map wrap

   public void clear()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(true);
            state.clear();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public boolean containsKey(Object o)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.containsKey(o);
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public boolean containsValue(Object o)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.containsKey(o);
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public Set entrySet()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.entrySet();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public boolean equals(Object o)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.equals(o);
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public Object get(Object o)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.get(o);
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public int hashCode()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.hashCode();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }
   public boolean isEmpty()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.isEmpty();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public Set keySet()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.keySet();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public Object put(Object key, Object val)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            val = versionManager.makeVersioned(val);
            Map state = getCurrentState(true);
            return state.put(key, val);
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public void putAll(Map c)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(true);
            Iterator it = state.entrySet().iterator();
            while (it.hasNext())
            {
               Map.Entry entry = (Map.Entry)it.next();
               Object val = versionManager.makeVersioned(entry.getValue());
               state.put(entry.getKey(), val);
            }
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }
   // REVISIT: On the remove stuff, we need to decide what happens
   // does the object removed get unversioned?  How is this handled
   // within a transaction?
   //
   public Object remove(Object key)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(true);
            return state.remove(key);
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public int size()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.size();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public Collection values()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Map state = getCurrentState(false);
            return state.values();
         }
         finally
         {
            lock.readLock().release();
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   public void writeExternal(java.io.ObjectOutput out)
      throws java.io.IOException
   {
      super.writeExternal(out);
      out.writeLong(versionId);
      out.writeObject(updates);
      out.writeObject(classname);
   }

   public void readExternal(java.io.ObjectInput in)
      throws java.io.IOException, ClassNotFoundException
   {
      super.readExternal(in);
      versionId = in.readLong();
      this.updates = (HashMap)in.readObject();
      this.classname = (String)in.readObject();
      try
      {
         InitialContext ctx = new InitialContext();
         this.tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
      this.txState = new TransactionLocal();
      this.txVersion = new TransactionLocal();
      this.methodMap = mapMethodMap;
   }

}
