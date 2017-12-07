/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ha.framework.interfaces;

import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;

/**
 * Root transaction sticky load balance policy class that checks whether there's
 * a sticky target associated with the current invocation and based on that 
 * returns the associated target or delegates to the given load balance policy 
 * to choose a new target if there's no target associated with the invocation.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class TransactionSticky implements LoadBalancePolicy
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -8750524198817324850L;

   private static final Logger log = Logger.getLogger(TransactionSticky.class);
   
   private transient boolean trace;
   
   private final LoadBalancePolicy delegateLoadBalancePolicy;
   
   public TransactionSticky(LoadBalancePolicy delegate)
   {
      delegateLoadBalancePolicy = delegate;
      
      if (trace)
      {
         log.trace("Transaction sticky load balance policy delegates to: " + delegateLoadBalancePolicy);
      }
   }

   /**
    * This method returns either, a new target based on RoundRobin policy, or 
    * if there's a ongoing transaction, the target associated with that 
    * transaction.
    *
    * @param familyClusterInfo cluster family information
    * @param invocation current invocation
    * @return a new target or the target associated with the transaction
    */
   public Object chooseTarget(FamilyClusterInfo clusterFamily, Invocation routingDecision)
   {
      trace = log.isTraceEnabled();
      Object txStickyTarget = routingDecision.getTransientValue("TX_STICKY_TARGET");
      if (txStickyTarget != null && clusterFamily.getTargets().contains(txStickyTarget))
      {
         if (trace) 
         {
            log.trace("Transaction bound target exists: " + txStickyTarget);
         } 
         
         return txStickyTarget;
      }

      return chooseNewTarget(clusterFamily, routingDecision);
   }   
   
   public void init(HARMIClient father)
   {
      delegateLoadBalancePolicy.init(father);
   }

   public Object chooseTarget(FamilyClusterInfo clusterFamily)
   {
      return delegateLoadBalancePolicy.chooseTarget(clusterFamily);
   }
   
   /**
    * Choses a new target based on delegate load balance policy.
    *
    * @param familyClusterInfo cluster family information
    * @param invocation current invocation
    * @return a new target
    */
   protected Object chooseNewTarget(FamilyClusterInfo familyClusterInfo, Invocation invocation)
   {
      Object newTarget = delegateLoadBalancePolicy.chooseTarget(familyClusterInfo, invocation);
      
      if (trace) 
      {
         log.trace("New target chosen: " + newTarget);
      }
      
      invocation.getTransientPayload().put("TX_STICKY_TARGET", newTarget);

      return newTarget;
   }
}
