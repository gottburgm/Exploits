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
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.StatelessSessionContainer;
import org.jboss.ejb.StatelessSessionEnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;

/**
 * This container acquires the given instance. This must be used after
 * the EnvironmentInterceptor, since acquiring instances requires a proper
 * JNDI environment to be set
 *
 * @author Rickard Oberg
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class StatelessSessionInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   protected StatelessSessionContainer container;

   // Static --------------------------------------------------------

   /** A reference to {@link javax.ejb.TimedObject#ejbTimeout}. */
   protected static final Method ejbTimeout;
   static
   {
      try
      {
         ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
      }
      catch (Exception e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   public void setContainer(final Container container) 
   {
      super.setContainer(container);
      this.container = (StatelessSessionContainer)container;
   }

   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(final Invocation mi) throws Exception
   {
      InstancePool pool = container.getInstancePool();
      StatelessSessionEnterpriseContext ctx = null;
      try
      {
         // Acquire an instance in case the ejbCreate throws a CreateException  
         ctx = (StatelessSessionEnterpriseContext) pool.get();
         mi.setEnterpriseContext(ctx);
         // Dispatch the method to the container
         return getNext().invokeHome(mi);
      }
      finally
      {
         mi.setEnterpriseContext(null);
         // If an instance was created, return it to the pool
         if( ctx != null )
            pool.free(ctx);
      }

   }

   public Object invoke(final Invocation mi) throws Exception
   {
      // Get context
      InstancePool pool = container.getInstancePool();
      StatelessSessionEnterpriseContext ctx = null;
      try
      {
         ctx = (StatelessSessionEnterpriseContext) pool.get();
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         InvocationType type = mi.getType();
         boolean isLocal = (type == InvocationType.LOCAL || type == InvocationType.LOCALHOME);
         if (isLocal)
            throw new EJBException("Unable to get an instance from the pool", e);
         else
            throw new RemoteException("Unable to get an intance from the pool", e);
      }


      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
      // Set the JACC EnterpriseBean PolicyContextHandler data
      EnterpriseBeanPolicyContextHandler.setEnterpriseBean(ctx.getInstance());

      // Use this context
      mi.setEnterpriseContext(ctx);

      // JAXRPC/JAXWS message context
      Object msgContext = mi.getValue(InvocationKey.SOAP_MESSAGE_CONTEXT);

      // Timer invocation
      if (ejbTimeout.equals(mi.getMethod()))
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_TIMEOUT);
      }

      // Service Endpoint invocation
      else if (msgContext != null)
      {
         if (msgContext instanceof javax.xml.rpc.handler.MessageContext)
            ctx.setMessageContext((javax.xml.rpc.handler.MessageContext)msgContext);

         AllowedOperationsAssociation.pushInMethodFlag(IN_SERVICE_ENDPOINT_METHOD);
      }

      // Business Method Invocation
      else
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_BUSINESS_METHOD);
      }

      // There is no need for synchronization since the instance is always fresh also there should
      // never be a tx associated with the instance.
      try
      {
         Object obj = getNext().invoke(mi);
         return obj;

      }
      catch (RuntimeException e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      }
      catch (RemoteException e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      }
      catch (Error e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
         EnterpriseBeanPolicyContextHandler.setEnterpriseBean(null);
         
         // Return context
         if (mi.getEnterpriseContext() != null)
         {
            pool.free(((EnterpriseContext) mi.getEnterpriseContext()));
         }
         else
         {
            pool.discard(ctx);
         }
      }
   }
}
