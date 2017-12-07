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
import java.util.Random;

import org.jboss.invocation.Invocation;

/**
 * LoadBalancingPolicy implementation that always fully randomly select its target
 * (without basing its decision on any historic).
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 * @see org.jboss.ha.framework.interfaces.LoadBalancePolicy
 */
public class RandomRobin implements LoadBalancePolicy
{
   // Constants -----------------------------------------------------
   /** @since 1.1.2.3 */
   private static final long serialVersionUID = -3599638697906618428L;
   /** This needs to be a class variable or else you end up with multiple
    * Random numbers with the same seed when many clients lookup a proxy.
    */
   public static final Random localRandomizer = new Random (System.currentTimeMillis ());

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
    // Public --------------------------------------------------------
   
   // LoadBalancePolicy implementation ----------------------------------------------

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
      int max = targets.size();

      if (max == 0)
         return null;

      int cursor = localRandomizer.nextInt (max);
      return targets.get(cursor);
   }

}
