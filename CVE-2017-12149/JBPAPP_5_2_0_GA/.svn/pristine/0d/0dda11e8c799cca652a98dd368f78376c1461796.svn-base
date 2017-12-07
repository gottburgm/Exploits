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
package org.jboss.ejb;

import java.util.HashMap;

import org.jboss.monitor.EntityLockMonitor;
import org.jboss.monitor.LockMonitor;
import org.jboss.logging.Logger;
import javax.naming.InitialContext;

/**
 * Manages BeanLocks.  All BeanLocks have a reference count.
 * When the reference count goes to 0, the lock is released from the
 * id -> lock mapping.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 */
public class BeanLockManager
{
   private static final int NUMBER_OF_INSTANCES=40;
   private static Logger log = Logger.getLogger(BeanLockManager.class);

   /** Multiple instances of hashMap to diminish locking contentions.
    *  Rules for accessing this are determined by {@link getHashMap(Object)}*/
   private HashMap map[] = new HashMap[NUMBER_OF_INSTANCES];


   /** The container this manager reports to */
   private Container container;

   /** Reentrancy of calls */
   private boolean reentrant = false;
   private int txTimeout = 5000;
   /** The logging trace flag, only set in ctor */
   private boolean trace;
   public Class lockClass;
   protected LockMonitor monitor = null;

   private BeanLockManager()
   {
       for (int i=0;i<map.length;i++)
       {
           map[i] = new HashMap();
       }
   }
   public BeanLockManager(Container container)
   {
      this();
      this.container = container;
      trace = log.isTraceEnabled();
      try
      {
         InitialContext ctx = new InitialContext();
         EntityLockMonitor elm = (EntityLockMonitor) ctx.lookup(EntityLockMonitor.JNDI_NAME);
         String jndiName = container.getBeanMetaData().getContainerObjectNameJndiName();
         monitor = elm.getEntityLockMonitor(jndiName);
      }
      catch (Exception ignored)
      {
         // Ignore the lack of an EntityLockMonitor
      }
   }

   public LockMonitor getLockMonitor()
   {
      return monitor;
   }

   private HashMap getHashMap(Object id)
   {
       final int mapInUse = id.hashCode()%NUMBER_OF_INSTANCES;
       if (mapInUse>0)
       {
           return map[mapInUse];
       }
       else
       {
           return map[mapInUse*-1];
       }
   }

   /**
    * returns the lock associated with the key passed.  If there is
    * no lock one is created this call also increments the number of
    * references interested in Lock.
    *
    * WARNING: All access to this method MUST have an equivalent
    * removeLockRef cleanup call, or this will create a leak in the map,
    */
   public BeanLock getLock(Object id)
   {
      if (id == null)
         throw new IllegalArgumentException("Attempt to get lock ref with a null object");

      HashMap mapInUse = getHashMap(id);

      synchronized (mapInUse)
      {
        BeanLock lock = (BeanLock) mapInUse.get(id);
        if (lock!=null)
        {
            lock.addRef();
            return lock;
        }
      }

      try
      {
          BeanLock lock2 = (BeanLock)createLock(id);
          synchronized(mapInUse)
          {
              BeanLock lock = (BeanLock) mapInUse.get(id);
              // in case of bad luck, this might happen
              if (lock != null)
              {
                  lock.addRef();
                  return lock;
              }
              mapInUse.put(id, lock2);
              lock2.addRef();
              return lock2;
          }
      }
      catch (Exception e)
      {
          // schrouf: should we really proceed with lock object
          // in case of exception ??
          log.warn("Failed to initialize lock:"+id, e);
          throw new RuntimeException (e);
      }
   }

   private BeanLock createLock(Object id) throws Exception
   {
       BeanLock lock = (BeanLock) lockClass.newInstance();
       lock.setId(id);
       lock.setTimeout(txTimeout);
       lock.setContainer(container);

       return lock;
   }

   public void removeLockRef(Object id)
   {
      if (id == null)
         throw new IllegalArgumentException("Attempt to remove lock ref with a null object");

      HashMap mapInUse = getHashMap(id);

      synchronized(mapInUse)
      {
          BeanLock lock = (BeanLock) mapInUse.get(id);

          if (lock != null)
          {
             try
             {
                lock.removeRef();
                if( trace )
                   log.trace("Remove ref lock:"+lock);
             }
             finally
             {
                // schrouf: ALLWAYS ensure proper map lock removal even in case
                // of exception within lock.removeRef ! There seems to be a bug
                // in the ref counting of QueuedPessimisticEJBLock under certain
                // conditions ( lock.ref < 0 should never happen !!! )
                if (lock.getRefs() <= 0)
                {
                   Object mapLock = mapInUse.remove(lock.getId());
                   if( trace )
                      log.trace("Lock no longer referenced, lock: "+lock);
                }
             }
          }
      }
   }

   public boolean canPassivate(Object id)
   {
      if (id == null)
         throw new IllegalArgumentException("Attempt to passivate with a null object");

      HashMap mapInUse = getHashMap(id);
      synchronized (mapInUse)
      {
          BeanLock lock = (BeanLock) mapInUse.get(id);
          if (lock == null)
             throw new IllegalStateException("Attempt to passivate without a lock");

          return (lock.getRefs() <= 1);
      }
   }

   public void setLockCLass(Class lockClass)
   {
      this.lockClass = lockClass;
   }

   public void setReentrant(boolean reentrant)
   {
      this.reentrant = reentrant;
   }

   public void setContainer(Container container)
   {
      this.container = container;
   }
}
