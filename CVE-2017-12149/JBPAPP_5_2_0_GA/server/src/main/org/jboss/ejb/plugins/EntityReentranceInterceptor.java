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
import javax.ejb.EJBObject;
import javax.ejb.EJBException;
import javax.transaction.Transaction;
import javax.transaction.Status;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.metadata.EntityMetaData;
import org.jboss.tm.TxUtils;
import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.ejb.plugins.lock.NonReentrantLock;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRInvocation;

/**
 * The role of this interceptor is to check for reentrancy.
 * Per the spec, throw an exception if instance is not marked
 * as reentrant.  We do not check to see if same Tx is
 * accessing object at the same time as we assume that
 * any transactional locks will handle this.
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 *    before changing.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 110102 $
 */
public class EntityReentranceInterceptor
        extends AbstractInterceptor
{
   protected boolean reentrant = false;

   // Public --------------------------------------------------------

   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         EntityMetaData meta = (EntityMetaData) container.getBeanMetaData();
         reentrant = meta.isReentrant();
      }
   }

   protected boolean isTxExpired(Transaction miTx) throws Exception
   {
      return TxUtils.isRollback(miTx);
   }

   public Object invoke(Invocation mi)
           throws Exception
   {
      // We are going to work with the context a lot
      EntityEnterpriseContext ctx = (EntityEnterpriseContext) mi.getEnterpriseContext();
      boolean nonReentrant = !(reentrant || isReentrantMethod(mi));

      // Not a reentrant method like getPrimaryKey
      NonReentrantLock methodLock = ctx.getMethodLock();
      Transaction miTx = ctx.getTransaction();
      boolean locked = false;
      try
      {
         while (!locked)
         {
            if (methodLock.attempt(5000, miTx, nonReentrant))
            {
               locked = true;
            }
            else
            {
               if (isTxExpired(miTx))
               {
                  log.error("Saw rolled back tx=" + miTx);
                  throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
               }
            }
         }
      }
      catch (NonReentrantLock.ReentranceException re)
      {
         if (mi.getType() == InvocationType.REMOTE)
         {
            throw new RemoteException("Reentrant method call detected: "
                    + container.getBeanMetaData().getEjbName() + " "
                    + ctx.getId().toString());
         }
         else
         {
            throw new EJBException("Reentrant method call detected: "
                    + container.getBeanMetaData().getEjbName() + " "
                    + ctx.getId().toString());
         }
      }
      try
      {
         ctx.lock();
         return getNext().invoke(mi);
      }
      finally
      {
         ctx.unlock();
         methodLock.release(nonReentrant);
      }
   }

   // Private ------------------------------------------------------

   private static final Method getEJBHome;
   private static final Method getHandle;
   private static final Method getPrimaryKey;
   private static final Method isIdentical;
   private static final Method remove;

   static
   {
      try
      {
         Class[] noArg = new Class[0];
         getEJBHome = EJBObject.class.getMethod("getEJBHome", noArg);
         getHandle = EJBObject.class.getMethod("getHandle", noArg);
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", noArg);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
         remove = EJBObject.class.getMethod("remove", noArg);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }

   protected boolean isReentrantMethod(Invocation mi)
   {
      // is this a known non-entrant method
      Method m = mi.getMethod();
      if (m != null && (
              m.equals(getEJBHome) ||
              m.equals(getHandle) ||
              m.equals(getPrimaryKey) ||
              m.equals(isIdentical) ||
              m.equals(remove)))
      {
         return true;
      }

      // if this is a non-entrant message to the container let it through
      if (mi instanceof CMRInvocation)
      {
         Entrancy entrancy = ((CMRInvocation) mi).getEntrancy();
         if (entrancy == Entrancy.NON_ENTRANT)
         {
            log.trace("NON_ENTRANT invocation");
            return true;
         }
      }

      return false;
   }

}
