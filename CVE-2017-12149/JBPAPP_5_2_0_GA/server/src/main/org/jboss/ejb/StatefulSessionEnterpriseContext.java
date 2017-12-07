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

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Date;

import javax.ejb.EJBLocalObject;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;


/**
 * The enterprise context for stateful session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 81030 $
 */
public class StatefulSessionEnterpriseContext
   extends EnterpriseContext
   implements Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private EJBObject ejbObject;
   private EJBLocalObject ejbLocalObject;
   private SessionContext ctx;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public StatefulSessionEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ctx = new StatefulSessionContextImpl();
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_SET_SESSION_CONTEXT);
         ((SessionBean)instance).setSessionContext(ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   // Public --------------------------------------------------------

   public void discard() throws RemoteException
   {
      // Do nothing
   }

   public EJBContext getEJBContext()
   {
      return ctx;
   }

   /**
    * During activation of stateful session beans we replace the instance
    * by the one read from the file.
    */
   public void setInstance(Object instance)
   {
      this.instance = instance;
      try
      {
         ((SessionBean)instance).setSessionContext(ctx);
      }
      catch (Exception x)
      {
         log.error("Failed to setSessionContext", x);
      }
   }

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
    
   public SessionContext getSessionContext()
   {
      return ctx;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private void writeObject(ObjectOutputStream out)
      throws IOException, ClassNotFoundException
   {
      // No state
   }
    
   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      // No state
   }

   // Inner classes -------------------------------------------------

   protected class StatefulSessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {

      public EJBHome getEJBHome()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBHome",
                 IN_SET_SESSION_CONTEXT |
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBLocalHome",
                 IN_SET_SESSION_CONTEXT |
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.getEJBLocalHome();
      }

      /** Get the Principal for the current caller. This method
       cannot return null according to the ejb-spec.
       */
      public Principal getCallerPrincipal()
      {
         AllowedOperationsAssociation.assertAllowedIn("getCallerPrincipal",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.getCallerPrincipal();
      }

      public boolean isCallerInRole(String id)
      {
         AllowedOperationsAssociation.assertAllowedIn("isCallerInRole",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.isCallerInRole(id);
      }

      public EJBObject getEJBObject()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBObject",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         if (((StatefulSessionContainer)con).getRemoteClass()==null)
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
            ejbObject = (EJBObject) proxyFactory.getStatefulSessionEJBObject(id);
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
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         if (con.getLocalHomeClass()==null)
            throw new IllegalStateException( "No local interface for bean." );
         if (ejbLocalObject == null)
         {
            ejbLocalObject = ((StatefulSessionContainer)con).getLocalProxyFactory().getStatefulSessionEJBLocalObject(id);
         }
         return ejbLocalObject;
      }

      public boolean getRollbackOnly()
      {
         AllowedOperationsAssociation.assertAllowedIn("getRollbackOnly",
                 IN_BUSINESS_METHOD | IN_AFTER_BEGIN | IN_BEFORE_COMPLETION);

         return super.getRollbackOnly();
      }

      public void setRollbackOnly()
      {
         AllowedOperationsAssociation.assertAllowedIn("setRollbackOnly",
                 IN_BUSINESS_METHOD | IN_AFTER_BEGIN | IN_BEFORE_COMPLETION);

         super.setRollbackOnly();
      }

      public UserTransaction getUserTransaction()
      {
         AllowedOperationsAssociation.assertAllowedIn("getUserTransaction",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD);

         return super.getUserTransaction();
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         throw new IllegalStateException("getTimerService should not be access from a stateful session bean");
      }

      public MessageContext getMessageContext() throws IllegalStateException
      {
         AllowedOperationsAssociation.assertAllowedIn("getMessageContext",
                 NOT_ALLOWED);
         return null;
      }

      public Object getPrimaryKey()
      {
         return id;
      }
   }
}
