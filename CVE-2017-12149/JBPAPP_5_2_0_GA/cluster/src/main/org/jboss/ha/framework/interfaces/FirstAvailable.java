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

import java.util.List;

import org.jboss.invocation.Invocation;

/**
 * LoadBalancingPolicy implementation that always favor the first available target i.e.
 * no load balancing occurs. Nevertheless, the first target is randomly selected.
 * This does not mean that fail-over will not occur if the
 * first member in the list dies. In this case, fail-over will occur, and a new target
 * will become the first member and invocation will continously be invoked on the same
 * new target until its death. Each proxy using this policy will elect its own
 * prefered target: the target is not shared accross the proxy family (for this
 * behaviour please take a look at FirstAvailableIdenticalAllProxies)
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 * @see org.jboss.ha.framework.interfaces.LoadBalancePolicy
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2002/08/24: Sacha Labourey</b>
 * <ol>
 *   <li>Use the target repository</li>
 *   <li>First choice is randomly selected to distribute the initial load</li>
 *   <li>When the list of targets change, we try to keep using the same 
         previously elected target node if it still exists. Previously,
         we were working with the position id of the target node, thus
         if the list order changed, we were switching to another node
         while our prefered node was still up</li>
 * </ol>
 */

public class FirstAvailable implements LoadBalancePolicy
{
   // Constants -----------------------------------------------------
   private static final long serialVersionUID = 2008524502721775114L;

   // Attributes ----------------------------------------------------
   
   protected transient Object electedTarget = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
    // Public --------------------------------------------------------
   
   public void init (HARMIClient father)
   {
      // do not use the HARMIClient in this policy
   }

   public Object chooseTarget (FamilyClusterInfo clusterFamily)
   {
      return chooseTarget(clusterFamily, null);
   }

   public Object chooseTarget (FamilyClusterInfo clusterFamily, Invocation routingDecision)
   {
      List targets = clusterFamily.getTargets ();
      if (targets.size () == 0)
         return null;

      if ( (this.electedTarget != null) && targets.contains (this.electedTarget) )
      {
         return this.electedTarget;
      }
      else
      {
         int cursor = RandomRobin.localRandomizer.nextInt(targets.size());
         this.electedTarget = targets.get(cursor);
         return this.electedTarget;
      }
   }
}
