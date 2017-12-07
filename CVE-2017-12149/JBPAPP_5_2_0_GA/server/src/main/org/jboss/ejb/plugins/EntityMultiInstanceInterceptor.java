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

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import javax.ejb.EJBException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * @deprecated this interceptor was used with Instance Per Transaction containers which do not use a global cache
 * but cache instances per transaction and always passivate instances at commit time (commit option C).
 * This interceptor used org.jboss.ejb.TxEntityMap to compensate the absence of a real per transaction cache implementation.
 * Now, the differences between IPT and standard container are:
 * <ul>
 *    <li>org.jboss.ejb.plugins.PerTxEntityInstanceCache as the cache implementation;</li>
 *    <li>NoLock as the locking policy;</li>
 *    <li>empty container-cache-conf element.</li>
 * </ul>
 * (alex@jboss.org)
 *
 * The instance interceptors role is to acquire a context representing
 * the target object from the cache.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 81030 $
 */
public class EntityMultiInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
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
	
   // Interceptor implementation --------------------------------------
	
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      // Get context
      EntityContainer ec = (EntityContainer) getContainer();
      EntityEnterpriseContext ctx = (EntityEnterpriseContext) ec.getInstancePool().get();

		// Pass it to the method invocation
      mi.setEnterpriseContext(ctx);

      // Give it the transaction
      ctx.setTransaction(mi.getTransaction());

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());

      AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_HOME);

      Object result;
      try
      {
         // Invoke through interceptors
         result = getNext().invokeHome(mi);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
      
      // No id, means we can put the context back in the pool
      if (ctx.getId() == null)
      {
         ctx.setTransaction(null);
         ec.getInstancePool().free(ctx);
      }
      
      // We are done
      return result;
   }

   public Object invoke(Invocation mi)
      throws Exception
   {

      // The key
      Object key = mi.getId();

      EntityEnterpriseContext ctx = null;
      EntityContainer ec = (EntityContainer) container;
      if (mi.getTransaction() != null)
      {
         //ctx = ec.getTxEntityMap().getCtx(mi.getTransaction(), key);
      }
      if (ctx == null)
      {
         InstancePool pool = ec.getInstancePool();
         try
         {
            ctx = (EntityEnterpriseContext) pool.get();
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
         ctx.setCacheKey(key);
         ctx.setId(key);
         EntityPersistenceManager pm = ec.getPersistenceManager();
         pm.activateEntity(ctx);
      }

      boolean trace = log.isTraceEnabled();
      if( trace ) log.trace("Begin invoke, key="+key);

      // Associate transaction, in the new design the lock already has the transaction from the
      // previous interceptor
      ctx.setTransaction(mi.getTransaction());

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
      // Set the JACC EnterpriseBean PolicyContextHandler data
      EnterpriseBeanPolicyContextHandler.setEnterpriseBean(ctx.getInstance());

      // Set context on the method invocation
      mi.setEnterpriseContext(ctx);

      if (ejbTimeout.equals(mi.getMethod()))
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_TIMEOUT);
      else
         AllowedOperationsAssociation.pushInMethodFlag(IN_BUSINESS_METHOD);

      try
      {
         Object ret = getNext().invoke(mi);
         return ret;
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
         EnterpriseBeanPolicyContextHandler.setEnterpriseBean(null);
      }
   }
}
