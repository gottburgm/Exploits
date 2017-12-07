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
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;
import org.jboss.util.id.GUID;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class DistributedPOJOState extends StateManager implements DistributedState, java.io.Externalizable
{
   private static final long serialVersionUID = 7640633352012924284L;

   /**
    * Logging instance
    */
   private static Logger log = Logger.getLogger(DistributedPOJOState.class);

   protected String classname;
   protected HashMap fieldMap;
   transient protected TransactionManager tm;
   transient protected WeakReference advisedRef;
   transient protected TransactionLocal txState = new TransactionLocal();
   transient protected SynchronizationManager synchManager;
   transient protected DistributedVersionManager versionManager;

   public DistributedPOJOState() {}

   public DistributedPOJOState(GUID daguid, long datimeout, Advised advised, DistributedVersionManager versionManager, SynchronizationManager synchManager)
      throws Exception
   {
      super(daguid, datimeout);
      this.fieldMap = new HashMap();
      this.classname = advised.getClass().getName();
      InitialContext ctx = new InitialContext();
      this.tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
      this.synchManager = synchManager;
      this.versionManager = versionManager;
      this.advisedRef = new WeakReference(advised);
   }

   public InstanceAdvised getObject() 
   {
      if (advisedRef != null)
      {
         return (InstanceAdvised)advisedRef.get();
      }
      return null; 
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof DistributedPOJOState)) return false;
      DistributedPOJOState pojo = (DistributedPOJOState)obj;
      return guid.equals(pojo.guid);
   }

   public int hashCode()
   {
      return guid.hashCode();

   }

   public InstanceAdvised buildObject(SynchronizationManager manager, DistributedVersionManager versionManager)
      throws Exception
   {
      log.trace("building a " + classname + " of guid " + guid);
      this.versionManager = versionManager;
      this.synchManager = manager;
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(classname);
      Advised advised = (Advised)clazz.newInstance();
      this.advisedRef = new WeakReference(advised);
      versionManager.addVersioning(this, advised);
      manager.putState(guid, this);
      manager.putObject(guid, advised);

      Iterator it = fieldMap.values().iterator();
      while(it.hasNext())
      {
         DistributedFieldUpdate update = (DistributedFieldUpdate)it.next();
         ClassAdvisor advisor = (ClassAdvisor)advised._getAdvisor();
         log.trace("build field " + advisor.getAdvisedFields()[update.getFieldIndex()].getName());
         Object val = update.getNonDereferencedValue();
         if (val != null && (val instanceof VersionReference))
         {
            VersionReference ref = (VersionReference)val;
            log.trace("VersionReference.guid: " + ref.getGUID() + " for field " + advisor.getAdvisedFields()[update.getFieldIndex()].getName());
            val = manager.getObject(ref.getGUID());
            if (val == null)
            {
               DistributedState fieldVal = manager.getState(ref.getGUID());
               val = fieldVal.buildObject(manager, versionManager);
            }
            ref.set((InstanceAdvised)val);
         }
      }
      return advised;
   }

   public HashMap getTxState()
   {
      return (HashMap)txState.get();
   }

   public HashMap getTxState(Transaction tx)
   {
      return (HashMap)txState.get(tx);
   }

   public Object fieldRead(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      acquireReadLock();
      try
      {
         org.jboss.aop.joinpoint.FieldReadInvocation fieldInvocation = (org.jboss.aop.joinpoint.FieldReadInvocation)invocation;
         Integer index = new Integer(fieldInvocation.getIndex());
         HashMap map = getTxState();
         if (map == null)
         {
            map = fieldMap;
         }
         DistributedFieldUpdate update = (DistributedFieldUpdate)map.get(index);
         Object val = update.getValue();
         return val;
      }
      finally
      {
         releaseReadLock();
      }
   }

   public Object fieldWrite(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      org.jboss.aop.joinpoint.FieldWriteInvocation fieldInvocation = (org.jboss.aop.joinpoint.FieldWriteInvocation)invocation;
      Integer index = new Integer(fieldInvocation.getIndex());
      Object val = fieldInvocation.getValue();

      if (val instanceof Advised)
      {
         Advised advisedValue = (Advised)val;
         val = versionManager.makeVersioned(advisedValue);
      }

      Transaction tx = tm.getTransaction();
      if (tx == null)
      {
         acquireWriteLock();
         try
         {
            // REVISIT: Handle exception
            DistributedFieldUpdate update = (DistributedFieldUpdate)fieldMap.get(index);
            long versionId = update.getVersionId() + 1;
            update.setVersionId(versionId);
            update.setValue(val);
            HashMap fieldUpdates = new HashMap();
            fieldUpdates.put(index, update);
            synchManager.noTxUpdate(new DistributedPOJOUpdate(guid, fieldUpdates));
            return null;
         }
         finally
         {
            releaseWriteLock();
         }
      }

      acquireReadLock();
      try
      {
         HashMap map = (HashMap)txState.get();
         if (map == null)
         {
            map = new HashMap();
            DistributedFieldUpdate update = (DistributedFieldUpdate)fieldMap.get(index);
            DistributedFieldUpdate newUpdate = new DistributedFieldUpdate(val, update.getVersionId() + 1, index.intValue());
            synchManager.registerUpdate(tx, this);
            map.put(index, newUpdate);
            txState.set(tx, map);
         }
         else
         {
            DistributedFieldUpdate newUpdate = (DistributedFieldUpdate)map.get(index);
            if (newUpdate == null)
            {
               DistributedFieldUpdate update = (DistributedFieldUpdate)fieldMap.get(index);
               newUpdate = new DistributedFieldUpdate(val, update.getVersionId() + 1, index.intValue());
               map.put(index, newUpdate);
            }
            else
            {
               newUpdate.setValue(val);
            }
         }
      }
      finally
      {
         releaseReadLock();
      }

      return null;
   }

   public DistributedUpdate createTxUpdate(Transaction tx)
   {
      HashMap state = getTxState(tx);
      return new DistributedPOJOUpdate(guid, state);
   }

   public void checkOptimisticLock(Transaction tx)
   {
      // NOTE THIS CODE ASSUMES THAT A WRITELOCK HAS BEEN ACQUIRED!!!!
      HashMap state = getTxState(tx);
      Iterator it = state.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         Integer index = (Integer)entry.getKey();
         DistributedFieldUpdate update = (DistributedFieldUpdate)entry.getValue();
         DistributedFieldUpdate orig = (DistributedFieldUpdate)fieldMap.get(index);
         if (update.getVersionId() <= orig.getVersionId())
         {
            Advised advised = null;
            if (advisedRef != null)
            {
               advised = (Advised)advisedRef.get();
            }
            if (advised != null)
            {
               ClassAdvisor advisor = (ClassAdvisor)advised._getAdvisor();
               Field field = advisor.getAdvisedFields()[index.intValue()];
               throw new OptimisticLockFailure("optimistic lock failure for field " + field.getName()
                                               + " of class " + field.getDeclaringClass().getName());
            }
         }
      }
   }

   public void mergeState(Transaction tx) throws Exception
   {
      HashMap newState = getTxState(tx);
      mergeState(newState);
   }

   public void mergeState(DistributedUpdate update) throws Exception
   {
      HashMap newState = ((DistributedPOJOUpdate)update).fieldUpdates;
      mergeState(newState);
   }

   public void mergeState(HashMap newState) throws Exception
   {
      // NOTE THIS CODE ASSUMES THAT A WRITELOCK HAS BEEN ACQUIRED!!!!
      Iterator it = newState.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         DistributedFieldUpdate update = (DistributedFieldUpdate)entry.getValue();
         if (update.getNonDereferencedValue() instanceof VersionReference)
         {
            VersionReference ref = (VersionReference)update.getNonDereferencedValue();
            if (ref.get() == null) ref.set((InstanceAdvised)synchManager.getObject(ref.getGUID()));
         }
      }
      fieldMap.putAll(newState); // overwrite old state
   }

   public void writeExternal(java.io.ObjectOutput out)
      throws java.io.IOException
   {
      super.writeExternal(out);
      out.writeObject(classname);
      out.writeObject(fieldMap);
   }

   public void readExternal(java.io.ObjectInput in)
      throws java.io.IOException, ClassNotFoundException
   {
      super.readExternal(in);
      this.classname = (String)in.readObject();
      this.fieldMap = (HashMap)in.readObject();
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
   }

}
