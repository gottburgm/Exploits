/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.naming.restart;

import org.jboss.ha.jndi.HANamingService;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;

/**
 * Subclass of HANamingService that ensures we don't screw up
 * the in-VM NamingContext class static haServers map.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class RestartHANamingService extends HANamingService
{

   /**
    * Create a new RestartHANamingService.
    * 
    */
   public RestartHANamingService()
   {
      super();
      this.replicantName = "RestartHAJNDI";
   }

   @Override
   protected void createService() throws Exception
   {
      Naming naming = NamingContext.getHANamingServerForPartition(clusterPartition.getPartitionName());
      try
      {
         super.createService();
      }
      finally
      {
         if (naming == null)
            NamingContext.removeHANamingServerForPartition(clusterPartition.getPartitionName());
         else
            NamingContext.setHANamingServerForPartition(clusterPartition.getPartitionName(), naming);
      }
   }

   @Override
   protected void stopService() throws Exception
   {
      Naming naming = NamingContext.getHANamingServerForPartition(clusterPartition.getPartitionName());
      try
      {
         super.stopService();
      }
      finally
      {
         if (naming != null)
            NamingContext.setHANamingServerForPartition(clusterPartition.getPartitionName(), naming);
      }
   }
   
   
   
   

}
