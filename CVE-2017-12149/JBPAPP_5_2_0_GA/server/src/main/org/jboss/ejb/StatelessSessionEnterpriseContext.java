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

import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.xml.rpc.handler.MessageContext;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;

/**
 * The enterprise context for stateless session beans.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 81030 $
 */
public class StatelessSessionEnterpriseContext
   extends EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   EJBObject ejbObject;
   EJBLocalObject ejbLocalObject;
   MessageContext soapMessageContext;
   SessionContext ctx;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public StatelessSessionEnterpriseContext(Object instance, Container con)
      throws Exception
   {
      super(instance, con);
      ctx = new SessionContextImpl();

      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_SET_SESSION_CONTEXT);
         ((SessionBean)instance).setSessionContext(ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_CREATE);
         Method ejbCreate = instance.getClass().getMethod("ejbCreate", new Class[0]);
         ejbCreate.invoke(instance, new Object[0]);
      } catch (InvocationTargetException e) 
      {
         Throwable ex = e.getTargetException();
         if (ex instanceof EJBException)
            throw (Exception)ex;
         else if (ex instanceof RuntimeException)
            throw new EJBException((Exception)ex); // Transform runtime exception into what a bean *should* have thrown
         else if (ex instanceof Exception)
            throw (Exception)ex;
         else
            throw (Error)ex;
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }
   
   // Public --------------------------------------------------------
   
   public void setEJBObject(EJBObject eo) {
      ejbObject = eo;
   }
   
   public EJBObject getEJBObject() {
      return ejbObject;
   }
   
   public void setEJBLocalObject(EJBLocalObject eo) {
      ejbLocalObject = eo;
   }
   
   public EJBLocalObject getEJBLocalObject() {
      return ejbLocalObject;
   }
   
   public SessionContext getSessionContext() {
      return ctx;
   }

   public void setMessageContext(MessageContext msgContext)
   {
      this.soapMessageContext = msgContext;
   }

   // EnterpriseContext overrides -----------------------------------
   
   public void discard() throws RemoteException
   {
      ((SessionBean)instance).ejbRemove();
   }
   
   public EJBContext getEJBContext()
   {
      return ctx;
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   
   protected class SessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {
      public EJBHome getEJBHome()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBHome",
                 IN_SET_SESSION_CONTEXT | IN_EJB_CREATE | IN_EJB_REMOVE |
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);

         return super.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBLocalHome",
                 IN_SET_SESSION_CONTEXT | IN_EJB_CREATE | IN_EJB_REMOVE |
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);

         return super.getEJBLocalHome();
      }

      public EJBObject getEJBObject()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBObject",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);

         if (((StatelessSessionContainer)con).getProxyFactory()==null)
            throw new IllegalStateException( "No remote interface defined." );
         
         if (ejbObject == null)
         {
            EJBProxyFactory proxyFactory = con.getProxyFactory();
            if(proxyFactory == null)
            {
               String defaultInvokerName = con.getBeanMetaData().
                  getContainerConfiguration().getDefaultInvokerName();
               proxyFactory = con.lookupProxyFactory(defaultInvokerName);
            }
            ejbObject = (EJBObject) proxyFactory.getStatelessSessionEJBObject(); 
         } 	
    
         return ejbObject;
      }
      
      public Object getBusinessObject(Class businessInterface) throws IllegalStateException
      {
         throw new RuntimeException("NOT IMPLEMENTED");
      }
      
      public Class getInvokedBusinessInterface() throws IllegalStateException
      {
         throw new RuntimeException("NOT IMPLEMENTED");
      }

      public EJBLocalObject getEJBLocalObject()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBLocalObject",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);

         if (con.getLocalHomeClass()==null)
            throw new IllegalStateException( "No local interface for bean." );
         if (ejbLocalObject == null) {
            ejbLocalObject = ((StatelessSessionContainer)con).getLocalProxyFactory().getStatelessSessionEJBLocalObject(); 
         }
         return ejbLocalObject;
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         AllowedOperationsAssociation.assertAllowedIn("getTimerService",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
         return new TimerServiceWrapper(this, super.getTimerService());
      }

      public Principal getCallerPrincipal()
      {
         AllowedOperationsAssociation.assertAllowedIn("getCallerPrincipal",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
         return super.getCallerPrincipal();
      }

      public boolean getRollbackOnly()
      {
         AllowedOperationsAssociation.assertAllowedIn("getRollbackOnly",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
         return super.getRollbackOnly();
      }

      public void setRollbackOnly()
      {
         AllowedOperationsAssociation.assertAllowedIn("setRollbackOnly",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
         super.setRollbackOnly();
      }

      public boolean isCallerInRole(String id)
      {
         AllowedOperationsAssociation.assertAllowedIn("isCallerInRole",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
         return super.isCallerInRole(id);
      }

      public UserTransaction getUserTransaction()
      {
         AllowedOperationsAssociation.assertAllowedIn("getUserTransaction",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
         final UserTransaction ut = super.getUserTransaction();
         return new UserTransaction()
         {
            public void begin() throws NotSupportedException, SystemException
            {
               checkUserTransactionMethods();
               ut.begin();
            }

            public void commit() throws RollbackException,
               HeuristicMixedException,
               HeuristicRollbackException,
               SecurityException,
               IllegalStateException,
               SystemException
            {
               checkUserTransactionMethods();
               ut.commit();
            }

            public void rollback() throws IllegalStateException, SecurityException, SystemException
            {
               checkUserTransactionMethods();
               ut.rollback();
            }

            public void setRollbackOnly() throws IllegalStateException, SystemException
            {
               checkUserTransactionMethods();
               ut.setRollbackOnly();
            }

            public int getStatus() throws SystemException
            {
               checkUserTransactionMethods();
               return ut.getStatus();
            }

            public void setTransactionTimeout(int seconds) throws SystemException
            {
               checkUserTransactionMethods();
               ut.setTransactionTimeout(seconds);
            }
         };
      }

      private void checkUserTransactionMethods()
      {
         AllowedOperationsAssociation.assertAllowedIn("UserTransaction methods",
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
      }

      public MessageContext getMessageContext() throws IllegalStateException
      {
         AllowedOperationsAssociation.assertAllowedIn("getMessageContext",
                 IN_SERVICE_ENDPOINT_METHOD);
         return soapMessageContext;
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
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
      }
   }
}