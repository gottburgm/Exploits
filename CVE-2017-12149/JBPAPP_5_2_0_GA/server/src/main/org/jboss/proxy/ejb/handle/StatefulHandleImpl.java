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
package org.jboss.proxy.ejb.handle;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Principal;
import java.util.Hashtable;

import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;

import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.PayloadKey;
import org.jboss.logging.Logger;
import org.jboss.naming.NamingContextFactory;
import org.jboss.security.SecurityAssociation;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 84403 $
 */
public class StatefulHandleImpl
   implements Handle
{
   private static final Logger log = Logger.getLogger(StatefulHandleImpl.class);
   
   /** Serial Version Identifier. */
   static final long serialVersionUID = -6324520755180597156L;

   /** A reference to {@link Handle#getEJBObject}. */
   protected static final Method GET_EJB_OBJECT;

   /** The value of our local Invoker.ID to detect when we are local. */
   private Object invokerID = null;

   /**
    * Initialize <tt>Handle</tt> method references.
    */
   static
   {
      try
      {
         GET_EJB_OBJECT = Handle.class.getMethod("getEJBObject", new Class[0]);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }

   /** The identity of the bean. */
   public int objectName;
   public String jndiName;
   public String invokerProxyBinding;
   public Invoker invoker;
   public Object id;

   /** The JNDI env in effect when the home handle was created */
   protected Hashtable jndiEnv;
   
   public StatefulHandleImpl()
   {
      
   }

   /** Create an ejb handle for a stateful session bean.
    * @param objectName - the session container jmx name
    * @param jndiName - the session home ejb name
    * @param invoker - the invoker to request the EJBObject from
    * @param invokerProxyBinding - the type of invoker binding
    * @param id - the session id
    */ 
   public StatefulHandleImpl(
      int objectName,
      String jndiName,
      Invoker invoker,
      String invokerProxyBinding,
      Object id,
      Object invokerID)
   {
      this.jndiName = jndiName;
      this.id = id;
      this.jndiEnv = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
      try
      {
         String property = System.getProperty("org.jboss.ejb.sfsb.handle.V327");
         if (property != null)
         {
            this.invokerProxyBinding = invokerProxyBinding;
            this.invokerID = invokerID;
            this.objectName = objectName;
            this.invoker = invoker;
         }
      }
      catch (AccessControlException ignored)
      {
      }

   }

   /**
    * @return the internal session identifier
    */
   public Object getID()
   {
      return id;
   }

   /**
    * @return the jndi name
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   /**
    * Handle implementation.
    *
    * This differs from Stateless and Entity handles which just invoke
    * standard methods (<tt>create</tt> and <tt>findByPrimaryKey</tt>
    * respectively) on the Home interface (proxy).
    * There is no equivalent option for stateful SBs, so a direct invocation
    * on the container has to be made to locate the bean by its id (the
    * stateful SB container provides an implementation of
    * <tt>getEJBObject</tt>).
    *
    * This means the security context has to be set here just as it would
    * be in the Proxy.
    *
    * @return  <tt>EJBObject</tt> reference.
    *
    * @throws ServerException    Could not get EJBObject.
    */
   public EJBObject getEJBObject() throws RemoteException
   {
      if (invokerProxyBinding != null)
      {
         try
         {
            return getEjbObjectViaInvoker(); 
         }
         catch(Exception e)
         {
            log.debug("Exception reported, try JNDI method to recover EJB object instead", e);
            return getEjbObjectViaJndi();
         }
      }               
      
      return getEjbObjectViaJndi();
   }

   /**
    * Returns wether we are local to the originating container or not. 
    */
   protected boolean isLocal()
   {
      return invokerID != null && invokerID.equals(Invoker.ID);
   }
   
   protected EJBObject getEjbObjectViaInvoker() throws Exception
   {
      if (log.isTraceEnabled())
      {
         log.trace("Using legacy invoker method for getEJBObject() invocation.");
      }
      SecurityActions sa = SecurityActions.UTIL.getSecurityActions();         
      Invocation invocation = new Invocation(
            null,
            GET_EJB_OBJECT,
            new Object[]{id},
            //No transaction set up in here? it will get picked up in the proxy
            null,
            // fix for bug 474134 from Luke Taylor
            sa.getPrincipal(),
            sa.getCredential());

      invocation.setObjectName(new Integer(objectName));
      invocation.setValue(InvocationKey.INVOKER_PROXY_BINDING,
         invokerProxyBinding, PayloadKey.AS_IS);

      // It is a home invocation
      invocation.setType(InvocationType.HOME);

      // Create an invocation context for the invocation
      InvocationContext ctx = new InvocationContext();
      invocation.setInvocationContext(ctx);
      
      // Get the invoker to the target server (cluster or node)

      // Ship it
      if (isLocal())
         return (EJBObject) InvokerInterceptor.getLocal().invoke(invocation);
      else
         return (EJBObject) invoker.invoke(invocation);
   }
   
   protected EJBObject getEjbObjectViaJndi() throws RemoteException
   {
      try
      {
         if (log.isTraceEnabled())
         {
            log.trace("Using JNDI method for getEJBObject() invocation.");
         }
         InitialContext ic = null;
         if( jndiEnv != null )
            ic = new InitialContext(jndiEnv);
         else
            ic = new InitialContext();

         Proxy proxy = (Proxy) ic.lookup(jndiName);

         // call findByPrimary on the target
         InvocationHandler ih = Proxy.getInvocationHandler(proxy);
         return (EJBObject) ih.invoke(proxy, GET_EJB_OBJECT, new Object[] {id});
      }
      catch (RemoteException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw new RemoteException("Error during getEJBObject", t);
      }
   }

   interface SecurityActions
   {
      class UTIL
      {
         static SecurityActions getSecurityActions()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }
      }

      SecurityActions NON_PRIVILEGED = new SecurityActions()
      {
         public Principal getPrincipal()
         {
            return SecurityAssociation.getPrincipal();
         }

         public Object getCredential()
         {
            return SecurityAssociation.getCredential();
         }
      };

      SecurityActions PRIVILEGED = new SecurityActions()
      {
         private final PrivilegedAction getPrincipalAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getPrincipal();
            }
         };

         private final PrivilegedAction getCredentialAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getCredential();
            }
         };

         public Principal getPrincipal()
         {
            return (Principal)AccessController.doPrivileged(getPrincipalAction);
         }

         public Object getCredential()
         {
            return AccessController.doPrivileged(getCredentialAction);
         }
      };

      Principal getPrincipal();

      Object getCredential();
   }
}

