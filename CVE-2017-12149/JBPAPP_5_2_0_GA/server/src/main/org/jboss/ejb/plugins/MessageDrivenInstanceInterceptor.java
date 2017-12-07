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

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.invocation.Invocation;

import javax.ejb.EJBException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.rmi.RemoteException;
import java.lang.reflect.Method;

/**
 * This container acquires the given instance. This must be used after
 * the EnvironmentInterceptor, since acquiring instances requires a proper
 * JNDI environment to be set.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 81030 $
 */
public class MessageDrivenInstanceInterceptor
      extends AbstractInterceptor
{

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

   /**
    * Message driven beans do not have homes.
    *
    * @throws Error    Not valid for MessageDriven beans.
    */
   public Object invokeHome(final Invocation mi)
         throws Exception
   {
      throw new Error("Not valid for MessageDriven beans");
   }

   // Interceptor implementation --------------------------------------

   public Object invoke(final Invocation mi)
         throws Exception
   {
      // Get context
      MessageDrivenContainer mdc = (MessageDrivenContainer) container;
      InstancePool pool = mdc.getInstancePool();
      EnterpriseContext ctx = null;
      try
      {
         ctx = pool.get();
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new EJBException("Unable to get an instance from the pool", e);
      }

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());

      // Use this context
      mi.setEnterpriseContext(ctx);
      // Set the JACC EnterpriseBean PolicyContextHandler data
      EnterpriseBeanPolicyContextHandler.setEnterpriseBean(ctx.getInstance());

      if (ejbTimeout.equals(mi.getMethod()))
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_TIMEOUT);
      else
         AllowedOperationsAssociation.pushInMethodFlag(IN_BUSINESS_METHOD);

      // There is no need for synchronization since the instance is always
      // fresh also there should never be a tx associated with the instance.
      try
      {
         // Invoke through interceptors
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
            pool.free((EnterpriseContext) mi.getEnterpriseContext());
         }
         else
         {
            pool.discard(ctx);
         }
      }
   }

}

