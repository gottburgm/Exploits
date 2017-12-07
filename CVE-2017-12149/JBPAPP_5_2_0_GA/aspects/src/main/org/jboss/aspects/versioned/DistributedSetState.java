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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class DistributedSetState extends CollectionStateManager implements Set, DistributedState, java.io.Externalizable
{
   private static final long serialVersionUID = -6272170697169509758L;

   private static HashMap setMethodMap;

   protected static Logger log = Logger.getLogger(DistributedSetState.class);
   static
   {
      try
      {
         setMethodMap = new HashMap();
         Method[] methods = Set.class.getDeclaredMethods();
         for (int i = 0; i < methods.length; i++)
         {
            long hash = org.jboss.aop.util.MethodHashing.methodHash(methods[i]);
            setMethodMap.put(new Long(hash), methods[i]);
         }
         
      }
      catch (Exception ignored)
      {
         ignored.printStackTrace();
      }
   }

   protected volatile long versionId;
   protected HashSet updates;
   protected String classname;
   transient protected Set base;
   transient protected TransactionLocal txState = new TransactionLocal();
   transient protected TransactionLocal txVersion = new TransactionLocal();
   transient protected DistributedVersionManager versionManager;
   transient protected SynchronizationManager synchManager;
   transient protected TransactionManager tm;
   transient protected ClassProxy proxy;

   /**
    * For serialization
    */
   public DistributedSetState() {}


   public DistributedSetState(GUID guid, long timeout, ClassProxy proxy, Set obj, DistributedVersionManager versionManager, SynchronizationManager synchManager) 
      throws Exception
   {
      super(guid, timeout, setMethodMap);
      this.base = obj;
      this.classname = obj.getClass().getName();
      this.versionManager = versionManager;
      this.synchManager = synchManager;
      this.proxy = proxy;
      InitialContext ctx = new InitialContext();
      this.tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
      this.updates = createSetUpdates(base);
   }

   public HashMap getMethodMap()
   {
      return ClassProxyFactory.getMethodMap(base.getClass().getName());
   }

   public InstanceAdvised getObject() { return proxy; }

   // The Guts

   protected Set getCurrentState(boolean forUpdate) throws Exception
   {
      Transaction tx = tm.getTransaction();
      if (tx == null) 
      {
         if (forUpdate) versionId++;
         return base;
      }

      Set state = (Set)txState.get(tx);
      if (state == null && forUpdate)
      {
         state = (Set)base.getClass().newInstance();
         state.addAll(base);
         txState.set(tx, state);
         long newId = versionId + 1;
         synchManager.registerUpdate(tx, this);
         txVersion.set(tx, new Long(newId));
         return state;
      }
      return base;
   }

   
   protected HashSet createSetUpdates(Set state)
   {
      HashSet setUpdates = new HashSet(state.size());
      Iterator it = state.iterator();
      while (it.hasNext())
      {
         Object obj = it.next();
         if (versionManager.isVersioned(obj))
         {
            setUpdates.add(new VersionReference(VersionManager.getGUID((InstanceAdvised)obj)));
         }
         else
         {
            setUpdates.add(obj);
         }
      }
      return setUpdates;
   }

   public DistributedUpdate createTxUpdate(Transaction tx)
   {
      Set state = (Set)txState.get(tx);
      long newId = ((Long)txVersion.get(tx)).longValue();
      DistributedSetUpdate update = new DistributedSetUpdate(guid, createSetUpdates(state), newId);
      return update;
   }

   public InstanceAdvised buildObject(SynchronizationManager manager, DistributedVersionManager versionManager)
      throws Exception
   {
      log.trace("building a Set");
      this.versionManager = versionManager;
      this.synchManager = manager;
      log.trace("DistributedSetState: classname: " + classname);
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(classname);
      base = (Set)clazz.newInstance();
      Iterator it = updates.iterator();
      while (it.hasNext())
      {
         Object val = it.next();
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
         base.add(val);
      }
      proxy = versionManager.addSetVersioning(base, this);
      return proxy;
   }

   public void checkOptimisticLock(Transaction tx)
   {
      // NOTE THIS CODE ASSUMES THAT A WRITELOCK HAS BEEN ACQUIRED!!!!
      Long version = (Long)txVersion.get(tx);
      if (version.longValue() <= versionId) 
         throw new OptimisticLockFailure("optimistic lock failure for set");
   }
   
   public void mergeState(Transaction tx) throws Exception
   {
      // NOTE THIS CODE ASSUMES THAT A WRITELOCK HAS BEEN ACQUIRED!!!!
      Set current = (Set)txState.get(tx);
      base = current;
      Long version = (Long)txVersion.get(tx);
      versionId = version.longValue();
   }

   public void mergeState(DistributedUpdate update) throws Exception
   {
      DistributedSetUpdate setUpdate = (DistributedSetUpdate)update;
      this.versionId = setUpdate.versionId;
      base.clear();
      Iterator it = setUpdate.setUpdates.iterator();
      while (it.hasNext())
      {
         Object val = it.next();
         if (val instanceof VersionReference)
         {
            VersionReference ref = (VersionReference)val;
            val = synchManager.getObject(ref.getGUID());
            ref.set((InstanceAdvised)val);
         }
         base.add(val);
      }
      updates = setUpdate.setUpdates;
   }

   // java.util.Set wrap

   public boolean add(Object val)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            val = versionManager.makeVersioned(val);
            Set state = getCurrentState(true);
            return state.add(val);
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

   public boolean addAll(Collection c)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(true);
            Object[] copy = c.toArray();
            for (int i = 0; i < copy.length; i++)
            {
               Object item = versionManager.makeVersioned(copy[i]);
               state.add(item);
            }
            return true;
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

   public void clear()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(true);
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

   public boolean contains(Object o)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(false);
            return state.contains(o);
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
   public boolean containsAll(Collection c)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(false);
            return state.containsAll(c);
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
            Set state = getCurrentState(false);
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
   public int hashCode()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(false);
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
            Set state = getCurrentState(false);
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
   public Iterator iterator()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(false);
            return state.iterator();
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
   public boolean remove(Object o)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(true);
            return state.remove(o);
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
   public boolean removeAll(Collection c)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(true);
            return state.removeAll(c);
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
   public boolean retainAll(Collection c)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(true);
            return state.retainAll(c);
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
            Set state = getCurrentState(false);
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
   public Object[] toArray()
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(false);
            return state.toArray();
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
   public Object[] toArray(Object[] a)
   {
      try
      {
         lock.readLock().acquire();
         try
         {
            Set state = getCurrentState(false);
            return state.toArray(a);
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
      this.updates = (HashSet)in.readObject();
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
      this.methodMap = setMethodMap;
   }

}
