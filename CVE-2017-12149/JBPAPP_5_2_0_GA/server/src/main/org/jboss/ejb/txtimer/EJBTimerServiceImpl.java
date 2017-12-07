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
package org.jboss.ejb.txtimer;

// $Id: EJBTimerServiceImpl.java 112630 2012-02-09 12:35:24Z wolfc $

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerMBean;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionManagerFactory;
import org.jboss.tm.TransactionManagerLocator;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A service that implements this interface provides an Tx aware EJBTimerService.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 112630 $
 * @since 07-Apr-2004
 */
public class EJBTimerServiceImpl extends ServiceMBeanSupport
   implements EJBTimerServiceImplMBean
{
   // Logging support
   private static Logger log = Logger.getLogger(EJBTimerServiceImpl.class);

   /**
    * Used for objects that don't implement javax.ejb.TimedObject but still call getTimerService()
    * According to the CTS, it's allowed (jbcts-381).
    */
   public static TimerService FOR_NON_TIMED_OBJECT = new TimerService()
   {
      public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException,
         IllegalStateException,
         EJBException
      {
         throw new IllegalStateException("The object does not implement javax.ejb.TimedObject interface!");
      }

      public Timer createTimer(long initialDuration, long intervalDuration, Serializable info)
         throws IllegalArgumentException, IllegalStateException, EJBException
      {
         throw new IllegalStateException("The object does not implement javax.ejb.TimedObject interface!");
      }

      public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException,
         IllegalStateException,
         EJBException
      {
         throw new IllegalStateException("The object does not implement javax.ejb.TimedObject interface!");
      }

      public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info)
         throws IllegalArgumentException, IllegalStateException, EJBException
      {
         throw new IllegalStateException("The object does not implement javax.ejb.TimedObject interface!");
      }

      public Collection getTimers() throws IllegalStateException, EJBException
      {
         return Collections.EMPTY_LIST;
      }
   };

   // Attributes
   
   // The object name of the retry policy
   private ObjectName retryPolicyName;
   // The object name of the persistence policy
   private ObjectName persistencePolicyName;
   // The TimerIdGenerator class name
   private String timerIdGeneratorClassName;
   // The TimedObjectInvoker class name
   private String timedObjectInvokerClassName;
   // The TransactionManagerFactory
   private TransactionManagerFactory transactionManagerFactory;
   
   // Plug-ins

   // The tx manager plug-in
   private TransactionManager transactionManager;   
   // The retry policy plug-in
   private RetryPolicy retryPolicy;
   // The persistence policy plug-in
   private PersistencePolicy persistencePolicy;
   // The timerId generator plug-in
   private TimerIdGenerator timerIdGenerator;   
   
   // Maps the timedObjectId to TimerServiceImpl objects
   private Map<TimedObjectId, TimerServiceImpl> timerServiceMap = Collections.synchronizedMap(new HashMap<TimedObjectId, TimerServiceImpl>());

   // A pool of timer that avoid creating one thread per task.
   private Integer threadPoolSize = new Integer(50);

   // Attributes ----------------------------------------------------
   
   /**
    * Get the object name of the retry policy.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getRetryPolicy()
   {
      return retryPolicyName;
   }

   /**
    * Set the object name of the retry policy.
    *
    * @jmx.managed-attribute
    */
   public void setRetryPolicy(ObjectName retryPolicyName)
   {
      this.retryPolicyName = retryPolicyName;
   }

   /**
    * Get the object name of the persistence policy.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getPersistencePolicy()
   {
      return persistencePolicyName;
   }

   /**
    * Set the object name of the persistence policy.
    *
    * @jmx.managed-attribute
    */
   public void setPersistencePolicy(ObjectName persistencePolicyName)
   {
      this.persistencePolicyName = persistencePolicyName;
   }

   /**
    * Get the TimerIdGenerator class name
    *
    * @jmx.managed-attribute
    */
   public String getTimerIdGeneratorClassName()
   {
      return timerIdGeneratorClassName;
   }

   /**
    * Get the TimerIdGenerator class name
    *
    * @jmx.managed-attribute
    */
   public void setTimerIdGeneratorClassName(String timerIdGeneratorClassName)
   {
      this.timerIdGeneratorClassName = timerIdGeneratorClassName;
   }

   /**
    * Get the TimedObjectInvoker class name
    *
    * @jmx.managed-attribute
    */
   public String getTimedObjectInvokerClassName()
   {
      return timedObjectInvokerClassName;
   }

   /**
    * Set the TimedObjectInvoker class name
    *
    * @jmx.managed-attribute
    */
   public void setTimedObjectInvokerClassName(String timedObjectInvokerClassName)
   {
      this.timedObjectInvokerClassName = timedObjectInvokerClassName;
   }

   /**
    * Set the TransactionManagerFactory
    */
   public void setTransactionManagerFactory(TransactionManagerFactory factory)
   {
      this.transactionManagerFactory = factory;
   }

   /**
    * Get the ThreadPoolSize
    *
    * @jmx.managed-attribute
    */
   public Integer getThreadPoolSize()
   {
      return threadPoolSize;
   }

   /**
    * Set the threadPoolSize
    *
    * @jmx.managed-attribute
    */
   public void setThreadPoolSize(Integer threadPoolSize)
   {
      this.threadPoolSize = threadPoolSize;
   }

   // ServiceMBeanSupport Lifecycle ---------------------------------
   
   protected void startService() throws Exception
   {
      // Setup plugins, fall back to safe defaults

      // Get the TransactionManager from the factory, fall-back to the locator
      if (transactionManagerFactory != null)
         transactionManager = transactionManagerFactory.getTransactionManager();
      else
         transactionManager = TransactionManagerLocator.getInstance().locate();

      // Get a proxy to the retry policy
      try
      {
         retryPolicy = (RetryPolicy)MBeanProxyExt.create(RetryPolicy.class, getRetryPolicy(), server);
      }
      catch (Exception e)
      {
         log.error("Cannot obtain the implementation of a RetryPolicy", e);
      }
      
      // Get a proxy to the persistence policy
      try
      {
         // JBPAPP-4681
         if (this.isPersistencePolicyExt(persistencePolicyName))
         {
            persistencePolicy = (PersistencePolicy)MBeanProxyExt.create(PersistencePolicyExt.class, persistencePolicyName, server);  
         }
         else
         {
            persistencePolicy = (PersistencePolicy)MBeanProxyExt.create(PersistencePolicy.class, persistencePolicyName, server);   
         }
         
      }
      catch (Exception e)
      {
         log.warn("Cannot obtain the implementation of a PersistencePolicy, using NoopPersistencePolicy: " + e.toString());
         persistencePolicy = new NoopPersistencePolicy();
      }

      
      
      
      // Get the timerId generator
      try
      {
         Class<?> timerIdGeneratorClass = getClass().getClassLoader().loadClass(timerIdGeneratorClassName);
         timerIdGenerator = (TimerIdGenerator)timerIdGeneratorClass.newInstance();
      }
      catch (Exception e)
      {
         log.warn("Cannot obtain the implementation of a TimerIdGenerator, using BigIntegerTimerIdGenerator: " + e.toString());
         timerIdGenerator = new BigIntegerTimerIdGenerator();
      }
   }
   
   protected void stopService()
   {
      // Cleanup plugins
      transactionManager = null;
      retryPolicy = null;
      persistencePolicy = null;
      timerIdGenerator = null;
   }
   
   // EJBTimerService Operations ------------------------------------
   
   /**
    * Create a TimerService for a given TimedObjectId that lives in a JBoss Container.
    * The TimedObjectInvoker is constructed from the invokerClassName.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The primary key for an instance of a TimedObject, may be null
    * @param container   The Container that is associated with the TimerService
    * @return the TimerService
    */
   public TimerService createTimerService(ObjectName containerId, Object instancePk, Container container)
   {
      TimedObjectInvoker invoker = null;
      try
      {
         TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
         Class<?> invokerClass = getClass().getClassLoader().loadClass(timedObjectInvokerClassName);
         Constructor<?> constr = invokerClass.getConstructor(new Class[]{TimedObjectId.class, Container.class});
         invoker = (TimedObjectInvoker)constr.newInstance(new Object[]{timedObjectId, container});
      }
      catch (Exception e)
      {
         log.error("Cannot create TimedObjectInvoker: " + timedObjectInvokerClassName, e);
         return null;
      }

      return createTimerService(containerId, instancePk, invoker);
   }

   /**
    * Create a TimerService for a given TimedObjectId that is invoked through the given invoker
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @param invoker     The TimedObjectInvoker
    * @return the TimerService
    */
   public TimerService createTimerService(ObjectName containerId, Object instancePk, TimedObjectInvoker invoker)
   {
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      TimerServiceImpl timerService = timerServiceMap.get(timedObjectId);
      if (timerService == null)
      {
         timerService = new TimerServiceImpl(timedObjectId, invoker,
               transactionManager, persistencePolicy, retryPolicy, timerIdGenerator, getThreadPoolSize());
         log.debug("createTimerService: " + timerService);
         timerServiceMap.put(timedObjectId, timerService);
      }
      return timerService;
   }

   /**
    * Get the TimerService for a given TimedObjectId
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param instancePk  The rimary key for an instance of a TimedObject, may be null
    * @return The TimerService, or null if it does not exist
    */
   public TimerService getTimerService(ObjectName containerId, Object instancePk)
   {
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      return timerServiceMap.get(timedObjectId);
   }

   /**
    * Remove the TimerService for a given containerId/pKey (TimedObjectId),
    * along with any persisted timer information.
    * 
    * This should be used for removing the TimerService and Timers
    * associated with a particular entity bean, when it gets removed.
    * 
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The primary key for an instance of a TimedObject, may be null
    */
   public void removeTimerService(ObjectName containerId, Object instancePk)
   {
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      // remove a single timer service
      if (timedObjectId.getInstancePk() != null)
      {
         TimerServiceImpl timerService = (TimerServiceImpl)getTimerService(containerId, instancePk);
         if (timerService != null)
         {
            log.debug("removeTimerService: " + timerService);
            // don't keep persistent state about the timer
            // this is really an entity->remove()
            timerService.shutdown(false);
            timerServiceMap.remove(timedObjectId);
         }
      }      
      else
      {
         // assume we don't want to keep timer state when the container
         // gets undeployed, this is the legacy behaviour
         removeTimerService(containerId, false);
      }
   }

   /**
    * Remove the TimerService for a given containerId.
    * 
    * This should be used to remove the timer service and timers for
    * any type of container (session, entity, message) at the time of
    * undeployment.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param keepState   Flag indicating whether timer persistent state should be kept or removed 
    */
   public void removeTimerService(ObjectName containerId, boolean keepState) throws IllegalStateException
   {
      // remove all timers with the given containerId
      synchronized(timerServiceMap)
      {
         Iterator<Map.Entry<TimedObjectId, TimerServiceImpl>> it = timerServiceMap.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<TimedObjectId, TimerServiceImpl> entry = it.next();
            TimedObjectId key = entry.getKey();
            TimerServiceImpl timerService = entry.getValue();
            if (containerId.equals(key.getContainerId()))
            {
               log.debug("removeTimerService: " + timerService);
               timerService.shutdown(keepState);
               it.remove();
            }
         }
      }
   }
   
   /**
    * Remove the TimerService for a given containerId/pKey (TimedObjectId).
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The primary key for an instance of a TimedObject, may be null
    * @param keepState   Flag indicating whether timer persistent state should be kept or removed 
    */
   public void removeTimerService(ObjectName containerId, Object instancePk, boolean keepState) throws IllegalStateException   
   {
      // remove a single timer service
      TimedObjectId timedObjectId = new TimedObjectId(containerId, instancePk);
      if (timedObjectId.getInstancePk() != null)
      {
         TimerServiceImpl timerService = (TimerServiceImpl)getTimerService(containerId, instancePk);
         if (timerService != null)
         {
            log.debug("removeTimerService: " + timerService);
            timerService.shutdown(false);
            timerServiceMap.remove(timedObjectId);
         }
      }
      // remove all timers with the given containerId
      else
      {
         synchronized(timerServiceMap)
         {
            Iterator<Map.Entry<TimedObjectId, TimerServiceImpl>> it = timerServiceMap.entrySet().iterator();
            while (it.hasNext())
            {
               Map.Entry<TimedObjectId, TimerServiceImpl> entry = it.next();
               TimedObjectId key = (TimedObjectId) entry.getKey();
               TimerServiceImpl timerService = (TimerServiceImpl) entry.getValue();
               if (containerId.equals(key.getContainerId()))
               {
                  log.debug("removeTimerService: " + timerService);
                  timerService.shutdown(keepState);
                  it.remove();
               }
            }
         }
      }      
   }
   
   /**
    * Restore the persisted timers for a given ejb container
    * 
    * @param containerId The ejb container id
    * @param loader      The classloader to use for loading the timers
    */
   public void restoreTimers(ObjectName containerId, ClassLoader loader) throws IllegalStateException
   {
      assert persistencePolicy != null : "persistencePolicy is not set";
      
      // find out all the persisted handles, for the specified container
      List handles = persistencePolicy.listTimerHandles(containerId, loader);
      
      if (handles.isEmpty() == false)
      {
         // first remove the persisted handles from the db
         for (Iterator i = handles.iterator(); i.hasNext(); )
         {
            TimerHandleImpl handle = (TimerHandleImpl)i.next();
            persistencePolicy.deleteTimer(handle.getTimerId(), handle.getTimedObjectId());
         }

         // make a second pass to re-create the timers; use the container
         // itself to retrieve the correct TimerService/ for each handle,
         // then use the standard ejb timer API to recreate the timer
         for (Iterator i = handles.iterator(); i.hasNext(); )
         {
            TimerHandleImpl handle = (TimerHandleImpl)i.next();
            try
            {
               TimedObjectId targetId = handle.getTimedObjectId();
               ContainerMBean container = (ContainerMBean)MBeanProxyExt.create(ContainerMBean.class, containerId, server);               
               TimerService timerService = container.getTimerService(targetId.getInstancePk());

               // JBPAPP-4681
               if (timerService instanceof TimerRestoringTimerService)
               {
                  TimerRestoringTimerService timerRestoringTimerService = (TimerRestoringTimerService) timerService;
                  timerRestoringTimerService.restoreTimer(handle.getFirstTime(), handle.getPeriode(), handle.getNextTimeout(), handle.getInfo(), handle.getTimerId());
               }
               // Fix for JBPAPP-3926
               else if (timerService instanceof PersistentIdTimerService)
               {
                  PersistentIdTimerService persistentIdTimerService = (PersistentIdTimerService) timerService;
                  persistentIdTimerService.createTimer(handle.getFirstTime(), handle.getPeriode(), handle.getInfo(), handle.getTimerId());
               }
               else
               {
                  log.warn("Unable to preserve timerId. Will generate new timerId: " + handle);
                  timerService.createTimer(handle.getFirstTime(), handle.getPeriode(), handle.getInfo());
               }
            }
            catch (Exception e)
            {
               log.warn("Unable to restore timer record: " + handle, e);
            }
         }
      }
   }

   // EJBTimerServiceImplMbean operations ---------------------------
   
   /**
    * List the timers registered with all TimerService objects
    *
    * @jmx.managed-operation
    */
   public String listTimers()
   {
      StringBuffer retBuffer = new StringBuffer();
      synchronized(timerServiceMap)
      {
         Iterator<Map.Entry<TimedObjectId, TimerServiceImpl>> it = timerServiceMap.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<TimedObjectId, TimerServiceImpl> entry = it.next();
            TimedObjectId timedObjectId = (TimedObjectId) entry.getKey();
            retBuffer.append(timedObjectId + "\n");

            TimerServiceImpl timerService = (TimerServiceImpl) entry.getValue();
            Collection col = timerService.getAllTimers();
            for (Iterator iterator = col.iterator(); iterator.hasNext();)
            {
               TimerImpl timer = (TimerImpl) iterator.next();
               TimerHandleImpl handle = new TimerHandleImpl(timer);
               retBuffer.append("   handle: " + handle + "\n");
               retBuffer.append("      " + timer + "\n");
            }
         }
      }
      return retBuffer.toString();
   } 
   
   /**
    * Returns true if the {@link Class} which backs the MBean instance represented by the
    * passed <code>persistencePolicyObjectName</code> is of type {@link PersistencePolicyExt}.
    * Else returns false
    * 
    * @param persistencePolicyObjectName The {@link ObjectName} of the persistence policy MBean
    * @return
    */
   // JBPAPP-4681
   private boolean isPersistencePolicyExt(ObjectName persistencePolicyObjectName)
   {
      if (persistencePolicyObjectName == null)
      {
         return false;
      }
      try
      {
         // get the MBean ObjectInstance from the MBean server
         ObjectInstance persistencePolicyMBeanInstance = this.server.getObjectInstance(persistencePolicyObjectName);
         // get hold of the class name
         String persistencePolicyInstanceClassName = persistencePolicyMBeanInstance.getClassName();

         // load the persistence policy class
         ClassLoader tccl = Thread.currentThread().getContextClassLoader();
         Class<?> persistencePolicyClass = tccl.loadClass(persistencePolicyInstanceClassName);
         // check whether it's of type PersistencePolicyExt
         if (PersistencePolicyExt.class.isAssignableFrom(persistencePolicyClass))
         {
            return true;
         }
      }
      catch (Exception e)
      {
         // ignore
         log.debug("Could not determine whether: " + persistencePolicyObjectName + " implements "
               + PersistencePolicyExt.class + " - will assume that it doesn't");
         return false;
      }
      
      return false;
   }
}
