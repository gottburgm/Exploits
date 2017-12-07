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
package org.jboss.test.cluster.hapartition.state;

import java.util.List;

import org.jboss.ha.framework.server.ClusterPartition;

/**
 * ClusterPartition that catches and saves any exceptions thrown in start().
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class StartupTestClusterPartition 
      extends ClusterPartition
      implements StartupTestClusterPartitionMBean
{
   private HAPartitionRecorderMBean startupRecorder;
   private List<HAPartitionStateTransferMBean> stateTransferTargets;
   
   public HAPartitionRecorderMBean getStartupRecorder()
   {
      return startupRecorder;
   }

   public void setStartupRecorder(HAPartitionRecorderMBean startupRecorder)
   {
      this.startupRecorder = startupRecorder;
   }

   @Override
   protected void startService() throws Exception
   {      
      try
      {
         super.startService();
      }
      catch (Exception e)
      {
         startupRecorder.setStartupException(e);
         throw e;
      }
   }

   public List<HAPartitionStateTransferMBean> getStateTransferTargets()
   {
      return stateTransferTargets;
   }

   public void setStateTransferTargets(List<HAPartitionStateTransferMBean> targets)
   {
      this.stateTransferTargets = targets;
      for (HAPartitionStateTransferMBean target : targets)
      {
         this.subscribeToStateTransferEvents(target.getServiceHAName(), target);
//         target.setClusterPartition(this);
      }
      
   }  
   
   

}
