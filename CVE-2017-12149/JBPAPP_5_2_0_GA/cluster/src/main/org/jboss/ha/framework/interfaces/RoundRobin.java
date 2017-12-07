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
 * LoadBalancingPolicy implementation that always favor the next available
 * target load balancing always occurs.
 *
 * @see org.jboss.ha.framework.interfaces.LoadBalancePolicy
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @version $Revision: 81001 $
 */
public class RoundRobin implements LoadBalancePolicy
{
   // Constants -----------------------------------------------------
   /** @since 1.3.4.2 */
   private static final long serialVersionUID = 8660076707279597114L;

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
      return this.chooseTarget(clusterFamily, null);
   }
   public Object chooseTarget (FamilyClusterInfo clusterFamily, Invocation routingDecision)
   {
      int cursor = clusterFamily.getCursor ();
      List targets = clusterFamily.getTargets ();

      if (targets.size () == 0)
         return null;
      
      if (cursor == FamilyClusterInfo.UNINITIALIZED_CURSOR)
      {         
         // Obtain a random index into targets
         cursor = RandomRobin.localRandomizer.nextInt(targets.size());
      }
      else
      {
         // Choose the next target
         cursor = ( (cursor + 1) % targets.size() );
      }
      clusterFamily.setCursor (cursor);

      return targets.get(cursor);
   }

}
