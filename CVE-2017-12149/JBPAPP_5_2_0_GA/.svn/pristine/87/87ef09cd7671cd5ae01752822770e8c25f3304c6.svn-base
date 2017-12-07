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
package org.jboss.proxy;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.jboss.ha.framework.interfaces.FamilyClusterInfo;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvokerProxyHA;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * Parent transaction sticky interceptor that encapsulates sticky target map 
 * and exposes operations on this map.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public abstract class AbstractTransactionStickyInterceptor extends Interceptor
{
   private static final Map txStickyTargets = Collections.synchronizedMap(new WeakHashMap());
   
   private FamilyClusterInfo familyClusterInfo;
   
   protected final Logger log = Logger.getLogger(getClass());
   
   protected static boolean trace = false;
   
   public AbstractTransactionStickyInterceptor()
   {
      trace = log.isTraceEnabled();
   }
   
   /**
    * Called at the beginning of the invocation to check whether the current tpc
    * is already present in the tx sticky target. If it is, get the chosen 
    * target associated to it and add it to the invocation transient payload so 
    * that the load balance policy can choose the right target  as long as the 
    * target is available in the cluster family. Otherwise, invocation needs to 
    * be halted because a previous invocation within the transaction succeeded 
    * (tx sticky target was set), so we can't failover to a different node.
    * 
    * @param invocation Invocation object where transaction sticky will be set 
    * if present.
    */
   protected void putIfExistsTransactionTarget(Invocation invocation) throws Exception
   {
      Object tpc = getTransactionPropagationContext();
      
      if (tpc != null)
      {
         if (trace)
         {
            log.trace("In the proxy, transaction propagation context (tpc) is " + tpc);
            log.trace("Contains key returns " + txStickyTargets.containsKey(tpc));
         }

         Object stickyTarget = txStickyTargets.get(tpc);
            
         if (stickyTarget != null)
         {            
            if (getFamilyClusterInfo(invocation).getTargets().contains(stickyTarget))
            {
               if (trace) 
               {
                  log.trace("Put transaction bound target into transient payload: " + stickyTarget);                  
               }
               
               invocation.getTransientPayload().put("TX_STICKY_TARGET", stickyTarget);                  
            }
            else
            {
               throw new ServiceUnavailableException("Transaction sticky target is no longer available, so invocation needs to be halted");
            }
         }
      }
   }
   
   /**
    * Method called to remember the sticky target associated with a transaction
    * context. 
    * 
    * @param invocation Invocation object from which the TX_STICKY_TARGET 
    * transient value comes from.
    * @param tpc Transaction propagation context.
    */
   protected void rememberTransactionTarget(Invocation invocation, Object tpc)
   {
      if (trace)
      {
         log.trace("After reaching the server, transaction propagation context (tpc) is " + tpc);
      }
      
      Object stickyTarget = invocation.getTransientValue("TX_STICKY_TARGET");
      
      if (stickyTarget != null)
      {
         if (trace)
         {
            log.trace("Remember transaction bound target [" + stickyTarget + "] for tpc " + tpc);
         }

         txStickyTargets.put(tpc, stickyTarget);
         
         /* Put it in invoker proxy txFailoverAuthorizations to avoid failover being
          * allowed if the 1st EJB invocation within user trasnsaction fails with 
          * GenericClusteringException.COMPLETED_NO */
         InvokerProxyHA proxy = (InvokerProxyHA)invocation.getInvocationContext().getInvoker();
         proxy.forbidTransactionFailover(tpc);
      }
   }   

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      trace = log.isTraceEnabled();
   }
   
   protected Object getTransactionPropagationContext()
   {
      TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactoryClientSide();
      if (trace)
      {
         log.trace("Using tpc factory " + tpcFactory);
      }      
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }
   
   protected synchronized FamilyClusterInfo getFamilyClusterInfo(Invocation invocation) throws Exception
   {
      if (familyClusterInfo == null)
      {
         InvokerProxyHA proxy = (InvokerProxyHA)invocation.getInvocationContext().getInvoker();
         familyClusterInfo = proxy.getFamilyClusterInfo();
      }
      
      return familyClusterInfo;
   }   
}
