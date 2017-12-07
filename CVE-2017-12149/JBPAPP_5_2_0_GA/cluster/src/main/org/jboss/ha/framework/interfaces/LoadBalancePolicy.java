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
package org.jboss.ha.framework.interfaces;

import org.jboss.invocation.Invocation;

/**
 * Extends the {@link org.jboss.ha.client.loadbalance.LoadBalancePolicy parent interface}
 * by adding support for passing in a legacy (i.e. non-AOP) {@link Invocation}
 * as an aid in making the choice of targets. 
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 */
public interface LoadBalancePolicy extends org.jboss.ha.client.loadbalance.LoadBalancePolicy
{
   /** The serialVersionUID
    * @since 1.3.4.2
    */ 
   static final long serialVersionUID = -5071668971774090555L;
   /**
    * Initialize the policy with a reference to its parent stub. the load-balancing policy
    * implementation can use HARMIClient data to take its decision
    * @param father The stub that owns the policy
    */   
   public void init (HARMIClient father);

   /**
    * Called when the stub wishes to know on which node the next invocation must
    * be performed.
    * @param clusterFamily A list of potential target nodes
    * @param routingDecision The actual invocation object if the policy wants
    * to have some kind of invocation-based routing strategy
    * @return The selected target for the next invocation
    */   
   public Object chooseTarget (FamilyClusterInfo clusterFamily, Invocation routingDecision);
}
