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

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.jboss.aop.InstanceAdvised;
import org.jboss.aop.util.PayloadKey;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public abstract class StateManager implements java.io.Externalizable
{
   public static final String STATE_MANAGER = "STATE_MANAGER";
   protected static Logger log = Logger.getLogger(StateManager.class);


   public static StateManager getStateManager(InstanceAdvised obj)
   {
      return (StateManager)obj._getInstanceAdvisor().getMetaData().getMetaData(STATE_MANAGER, STATE_MANAGER);
   }

   public static void setStateManager(InstanceAdvised obj, StateManager manager)
   {
      obj._getInstanceAdvisor().getMetaData().addMetaData(STATE_MANAGER, STATE_MANAGER, manager, PayloadKey.TRANSIENT);
   }

   protected GUID guid;
   protected long timeout;
   transient protected ReadWriteLock lock = new WriterPreferenceReadWriteLock();

   public StateManager() {}

   public StateManager(GUID guid, long timeout)
   {
      this.guid = guid;
      this.timeout = timeout;
   }

   public ReadWriteLock getLock() { return lock; }
   public GUID getGUID() { return guid; }

   public void acquireWriteLock()
   {
      try
      {
         if (!lock.writeLock().attempt(timeout))
         {
            throw new LockAttemptFailure("failed to acquire write lock: " + guid);
         }
         log.trace("writelock acquired for: " + guid);
      }
      catch (InterruptedException ignored)
      {
         throw new LockAttemptFailure("failed to acquire write lock: " + guid);
      }
   }

   public void acquireReadLock()
   {
      try
      {
         if (!lock.readLock().attempt(timeout))
         {
            throw new LockAttemptFailure("failed to acquire read lock: " + guid);
         }
         log.trace("readlock acquired for: " + guid);
      }
      catch (InterruptedException ignored)
      {
         throw new LockAttemptFailure("failed to acquire read lock: " + guid);
      }
   }

   public void releaseReadLock()
   {
      lock.readLock().release();
      log.trace("readlock released for: " + guid);
}
   public void releaseWriteLock()
   {
      lock.writeLock().release();
      log.trace("writelock released for: " + guid);
   }

   public void writeExternal(java.io.ObjectOutput out)
      throws java.io.IOException
   {
      out.writeObject(guid);
      out.writeLong(timeout);
   }

   public void readExternal(java.io.ObjectInput in)
      throws java.io.IOException, ClassNotFoundException
   {
      this.guid = (GUID)in.readObject();
      this.timeout = in.readLong();
      this.lock = new WriterPreferenceReadWriteLock();
   }


}
