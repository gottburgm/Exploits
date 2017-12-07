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
package org.jboss.system;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * BarrierController service.
 * 
 * A service that controls the lifecycle of a secondary mbean
 * (the BarrierMbean) that can be used as a dependency for other
 * services.
 * 
 * Starting and stopping the barrier mbean (and as a result
 * all services depending on it) is performed by listening
 * for any kind of JMX notification. In particular we use
 * the handback object of a notification subscription to
 * qualify the start and stop signals.
 * 
 * Manual control of the barrier is also supported through
 * startBarrier()/stopBarrier() methods.
 * 
 * You may subclass BarrierController and override enableOnStartup()
 * to apply complex logic in deciding whether to initially start
 * the barrier (e.g. query some other mbean). 
 * 
 * @jmx:mbean
 *    extends="org.jboss.system.ListenerServiceMBean"
 *    name="jboss:service=BarrierController"
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81033 $
 */
public class BarrierController extends ListenerServiceMBeanSupport
   implements BarrierControllerMBean
{
   // Private Data --------------------------------------------------
   
   /** The ObjectName of the Barrier MBean */
   private ObjectName barrierName;

   /** The initial state of the barrier */
   private Boolean enableOnStartup;
   
   /** The notification subscription handback string that starts the barrier */ 
   private String startHandback;
   
   /** The notification subscription handback string that stops the barrier */
   private String stopHandback;
   
   /** The dynamic subscriptions flag */
   private Boolean dynamicSubscriptions;
   
   // Protected Data ------------------------------------------------

   /** The controlled Barrier */
   protected Barrier barrier;
   
   // Constructors --------------------------------------------------
   
   /**
    * Default CTOR
    */
   public BarrierController()
   {
      // empty
   }
   
   // Attributes ----------------------------------------------------
   
   /**
    * The controlled barrier StateString.
    * 
    * @jmx.managed-attribute
    */
   public String getBarrierStateString()
   {
      return (barrier != null) ? barrier.getStateString() : null;
   }
   
   /**
    * The controlled barrier ObjectName.
    * 
    * @jmx.managed-attribute
    */
   public void setBarrierObjectName(ObjectName barrierName)
   {
      // set once
      if (this.barrierName == null)
      {
         this.barrierName = barrierName;
      }
   }
   
   /**
    * The controlled barrier ObjectName.
    * 
    * @jmx.managed-attribute
    */
   public ObjectName getBarrierObjectName()
   {
      return barrierName;
   }


   /**
    * The initial state of the barrier.
    * 
    * If set, it overrides the internal call to enableOnStartup()
    * which will never get called.
    * 
    * @jmx.managed-attribute
    */
   public void setBarrierEnabledOnStartup(Boolean enableOnStartup)
   {
      // set once
      if (this.enableOnStartup == null)
      {
         this.enableOnStartup = enableOnStartup;
      }
   }

   /**
    * The initial state of the barrier.
    * 
    * Use the value set through setBarrierEnabledOnStartup()
    * otherwise call the internal enableOnStartup() override
    * to make a decision.
    * 
    * @jmx.managed-attribute
    */
   public Boolean getBarrierEnabledOnStartup()
   {
      if (enableOnStartup == null)
      {
         // setBarrierEnabledOnStartup() not called
         // initialize through enableOnStartup()
         enableOnStartup = enableOnStartup();
      }
      return enableOnStartup;
   }
   
   /**
    * The notification subscription handback string that starts the barrier.
    * 
    * @jmx.managed-attribute
    */   
   public void setStartBarrierHandback(String startHandback)
   {
      // set once
      if (this.startHandback == null)
      {
         this.startHandback = startHandback;
      }
   }

   /**
    * The notification subscription handback string that starts the barrier.
    * 
    * @jmx.managed-attribute
    */   
   public String getStartBarrierHandback()
   {
      return startHandback;
   }

   /**
    * The notification subscription handback string that stops the barrier.
    * 
    * @jmx.managed-attribute
    */   
   public void setStopBarrierHandback(String stopHandback)
   {
      // set once
      if (this.stopHandback == null)
      {
         this.stopHandback = stopHandback;
      }
   }
   
   /**
    * The notification subscription handback string that stops the barrier.
    * 
    * @jmx.managed-attribute
    */   
   public String getStopBarrierHandback()
   {
      return stopHandback;
   }
   
   /**
    * The ability to dynamically subscribe for notifications.
    *
    * @jmx.managed-attribute
    */
   public void setDynamicSubscriptions(Boolean dynamicSubscriptions)
   {
      // set once
      if (this.dynamicSubscriptions == null)
      {
         this.dynamicSubscriptions = dynamicSubscriptions;
      }
   }
   
   /**
    * The ability to dynamically subscribe for notifications.
    *
    * @jmx.managed-attribute
    */   
   public Boolean getDynamicSubscriptions()
   {
      if (dynamicSubscriptions == null)
      {
         dynamicSubscriptions = Boolean.TRUE;
      }
      return dynamicSubscriptions;
   }
   
   // Override ------------------------------------------------------
   
   /**
    * Override this method to apply complex logic whether
    * to start the Barrier service upon startup or not.
    * 
    * This method will be only called once and only if
    * setBarrierEnabledOnStartup(Boolean) has not been called.
    * 
    * The default implementation is to return false.
    */
   protected Boolean enableOnStartup()
   {
      return Boolean.FALSE;
   }
   
   // Lifecycle -----------------------------------------------------
   
   protected void createService() throws Exception
   {
      // create the Barrier
      barrier = new Barrier(getServiceName());
      
      initBarrier();
   }
   
   /**
    * Coordinates between createService() and postRegister(),
    * registering the barrier when both the barrier is
    * created and the mbeanserver is available
    * 
    * @throws Exception
    */
   private void initBarrier() throws Exception
   {
      if (barrier != null && getServer() != null)
      {         
         // register with the MBeanServer
         getServer().registerMBean(barrier, barrierName);
         
         // implicitly call the ServiceController
         barrier.create();
         
         // conditionally start the barrier
         if (getBarrierEnabledOnStartup().booleanValue())
         {
            startBarrier();
         }
         // subscribe for notifications
         subscribe(getDynamicSubscriptions().booleanValue());         
      }
   }
   
   protected void destroyService()
   {
      // unsubscribe for notifications
      unsubscribe();

      try
      {
         // implicitly call the ServiceController
         barrier.destroy();
      
         // remove from MBeanServer
         getServer().unregisterMBean(barrierName);
      }
      catch (Throwable e)
      {
         log.debug("Unexpected error during destroy", e);
      }
      
      // cleanup
      barrier = null;
   }
   
   // ListenerServiceMBeanSupport -----------------------------------
   
   /**
    * Base on the handback object the decision
    * for starting/stopping the barrier
    */ 
   public void handleNotification2(Notification n, Object handback)
   {
      log.debug("Got notification: " + n);
      
      if (startHandback != null && startHandback.equals(handback))
      {
         log.debug("Saw '" + handback + "' handback, starting barrier");
         startBarrier();
      }
      else if (stopHandback != null && stopHandback.equals(handback))
      {
         log.debug("Saw '" + handback + "' handback, stopping barrier");
         stopBarrier();
      }
   }
   
   // Operations ----------------------------------------------------

   /**
    * Manually start the controlled barrier
    * 
    * @jmx:managed-operation
    */
   public void startBarrier()
   {
      try
      {
         // implicitly call the ServiceController
         barrier.start();
      }
      catch (Throwable e)
      {
         log.warn("Failed to start barrier: " + barrierName, e);
      }
   }

   /**
    * Manually stop the controlled barrier
    * 
    * @jmx:managed-operation
    */
   public void stopBarrier()
   {
      try
      {
         // implicitly call the ServiceController
         barrier.stop();
      }
      catch (Throwable e)
      {
         log.warn("Failed to stop barrier: " + barrierName, e);
      }
   }
   
   // Overrides ---------------------------------------------------
   
   @Override
   public void postRegister(Boolean registrationDone)
   {
      super.postRegister(registrationDone);
      
      if (Boolean.TRUE.equals(registrationDone))
      {
         try
         {
            initBarrier();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }
   
   // Inner Class ---------------------------------------------------
   
   /**
    * The controlled barrier MBean interface
    */
   public static interface BarrierMBean
   {
      /** We just want to expose those attributes */
      ObjectName getBarrierController();
      String getStateString();
      int getState();
      
      /** Hook up with the ServiceController */
      void jbossInternalLifecycle(String method) throws Exception;
   }   

   /**
    * The controlled barrier MBean class
    */
   public static class Barrier extends ServiceMBeanSupport implements BarrierMBean
   {
      /** The parent Controller */
      private ObjectName barrierController;
      
      /** CTOR */
      public Barrier(ObjectName barrierController)
      {
         this.barrierController = barrierController;
      }
      
      /** Accessor */
      public ObjectName getBarrierController()
      {
         return barrierController;
      }
   }   
}
