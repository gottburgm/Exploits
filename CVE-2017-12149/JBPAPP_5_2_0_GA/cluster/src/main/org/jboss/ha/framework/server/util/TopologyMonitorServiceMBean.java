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
package org.jboss.ha.framework.server.util;

import java.util.Vector;
import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.system.ServiceMBean;

/** A utility mbean that monitors membership of a cluster parition
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public interface TopologyMonitorServiceMBean extends ServiceMBean
{
   /** 
    * Get the name of the cluster partition the mbean is monitoring
    */
   public String getPartitionName();
   
   /** 
    * Set the cluster parition name the mbean is monitoring
    * 
    * @deprecated use {@link #setPartition(HAPartition)}
    */
   public void setPartitionName(String name);

   /** 
    * Set the cluster partition the mbean is monitoring
    */
   public void setPartition(HAPartition partition);

   /** Get the trigger mbean to notify on cluster membership changes
    */
   public ObjectName getTriggerServiceName();
   /** Set the trigger mbean to notify on cluster membership changes
    */
   public void setTriggerServiceName(ObjectName name);

   /** Get the current cluster parition membership info
    *@return a Vector of org.jgroups.Address implementations, for example,
    *org.jgroups.stack.IpAddress
    */
   public Vector getClusterNodes();
}
