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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;

import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;

/**
 * Context for message driven beans.
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class MessageDrivenEnterpriseContext
   extends EnterpriseContext
{
   private MessageDrivenContext ctx;

   /**
    * Construct a <tt>MessageDrivenEnterpriseContext</tt>.
    *
    * <p>Sets the MDB context and calls ejbCreate().
    *
    * @param instance   An instance of MessageDrivenBean
    * @param con        The container for this MDB.
    *
    * @throws Exception    EJBException, Error or Exception.  If RuntimeException
    *                      was thrown by ejbCreate it will be turned into an
    *                      EJBException.
    */
   public MessageDrivenEnterpriseContext(Object instance, Container con)
      throws Exception
   {
      super(instance, con);
      
      ctx = new MessageDrivenContextImpl();
      ((MessageDrivenBean)instance).setMessageDrivenContext(ctx);

      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_CREATE);
         Method ejbCreate = instance.getClass().getMethod("ejbCreate", new Class[0]);
         ejbCreate.invoke(instance, new Object[0]);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         
         if (t instanceof RuntimeException) {
            if (t instanceof EJBException) {
               throw (EJBException)t;
            }
            else {
               // Transform runtime exception into what a bean *should* have thrown
               throw new EJBException((RuntimeException)t);
            }
         }
         else if (t instanceof Exception) {
            throw (Exception)t;
         }
         else if (t instanceof Error) {
            throw (Error)t;
         }
         else {
            throw new org.jboss.util.NestedError("Unexpected Throwable", t);
         }
      }
   }

   public MessageDrivenContext getMessageDrivenContext()
   {
      return ctx;
   }

   // EnterpriseContext overrides -----------------------------------

   /**
    * Calls ejbRemove() on the MDB instance.
    */
   public void discard() throws RemoteException
   {
      ((MessageDrivenBean)instance).ejbRemove();
   }

   public EJBContext getEJBContext()
   {
      return ctx;
   }

   /**
    * The EJBContext for MDBs.
    */
   protected class MessageDrivenContextImpl
      extends EJBContextImpl
      implements MessageDrivenContext
   {

      public EJBHome getEJBHome()
      {
         throw new IllegalStateException("getEJBHome should not be access from a message driven bean");
      }

      public EJBLocalHome getEJBLocalHome()
      {
         throw new IllegalStateException("getEJBHome should not be access from a message driven bean");
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         AllowedOperationsAssociation.assertAllowedIn("getTimerService",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return new TimerServiceWrapper(this, super.getTimerService());
      }

      public Principal getCallerPrincipal()
      {
         AllowedOperationsAssociation.assertAllowedIn("getCallerPrincipal",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return super.getCallerPrincipal();
      }

      public boolean isCallerInRole(String id)
      {
         throw new IllegalStateException("isCallerInRole should not be access from a message driven bean");
      }

      public UserTransaction getUserTransaction()
      {
         if (isContainerManagedTx())
            throw new IllegalStateException("getUserTransaction should not be access for container managed Tx");

         AllowedOperationsAssociation.assertAllowedIn("getUserTransaction",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         return super.getUserTransaction();
      }

      /**
       * If transaction type is not Container or there is no transaction
       * then throw an exception.
       *
       * @throws IllegalStateException   If transaction type is not Container,
       *                                 or no transaction.
       */
      public boolean getRollbackOnly()
      {
         if (isUserManagedTx())
            throw new IllegalStateException("getRollbackOnly should not be access for user managed Tx");

         AllowedOperationsAssociation.assertAllowedIn("getRollbackOnly",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         //
         // jason: I think this is lame... but the spec says this is how it is.
         //        I think it would be better to silently ignore... or not so silently
         //        but still continue.
         //
         
         if (!isTxRequired()) {
            throw new IllegalStateException
               ("getRollbackOnly must only be called in the context of a transaction (EJB 2.0 - 15.5.1)");
         }

         return super.getRollbackOnly();
      }

      /**
       * If transaction type is not Container or there is no transaction
       * then throw an exception.
       *
       * @throws IllegalStateException   If transaction type is not Container,
       *                                 or no transaction.
       */
      public void setRollbackOnly()
      {
         if (isUserManagedTx())
            throw new IllegalStateException("setRollbackOnly should not be access for user managed Tx");

         AllowedOperationsAssociation.assertAllowedIn("getRollbackOnly",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         if (!isTxRequired()) {
            throw new IllegalStateException
               ("setRollbackOnly must only be called in the context of a transaction (EJB 2.0 - 15.5.1)");
         }

         super.setRollbackOnly();
      }

      /** Helper to check if the tx type is TX_REQUIRED. */
      private boolean isTxRequired()
      {
         MessageDrivenMetaData md = (MessageDrivenMetaData)con.getBeanMetaData();
         return md.getMethodTransactionType() == MetaData.TX_REQUIRED;
      }
   }

   /**
    * Delegates to the underlying TimerService, after checking access
    */
   public class TimerServiceWrapper implements TimerService
   {

      private EnterpriseContext.EJBContextImpl context;
      private TimerService timerService;

      public TimerServiceWrapper(EnterpriseContext.EJBContextImpl ctx, TimerService timerService)
      {
         this.context = ctx;
         this.timerService = timerService;
      }

      public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(duration, info);
      }

      public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(initialDuration, intervalDuration, info);
      }

      public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(expiration, info);
      }

      public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(initialExpiration, intervalDuration, info);
      }

      public Collection getTimers() throws IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.getTimers");
         return timerService.getTimers();
      }

      private void assertAllowedIn(String timerMethod)
      {
         AllowedOperationsAssociation.assertAllowedIn(timerMethod,
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
      }
   }
}