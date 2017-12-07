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
 * new target until its death. Each proxy using this policy will *not* elect its own
 * prefered target: the target *is* shared with all proxies that belong to the same family 
 * (for a different behaviour please take a look at FirstAvailable)
 *
 * @see org.jboss.ha.framework.interfaces.LoadBalancePolicy
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 */
public class FirstAvailableIdenticalAllProxies implements LoadBalancePolicy
{
   // Constants -----------------------------------------------------
   private static final long serialVersionUID = 2910756623413400467L;

   // Attributes ----------------------------------------------------
   
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
      Object target = clusterFamily.getObject ();
      List targets = clusterFamily.getTargets ();

      if (targets.size () == 0)
         return null;
      
      if (target != null && targets.contains (target) )
      {
         return target;
      }
      else
      {
         int cursor = RandomRobin.localRandomizer.nextInt (targets.size());
         target = targets.get(cursor);
         clusterFamily.setObject (target);
         return target;
      }   
   }

}
