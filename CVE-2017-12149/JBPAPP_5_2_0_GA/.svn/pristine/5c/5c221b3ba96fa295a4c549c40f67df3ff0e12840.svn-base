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
package org.jboss.test.cluster.rspfilter;

import java.util.List;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jboss.logging.Logger;

/**
 * Echo.
 * 
 * @author Galder Zamarreño
 */
public class Echo implements EchoMBean
{
   private static final Logger log = Logger.getLogger(Echo.class);
   private static final String NAME = "cluster.rspfilter:service=Echo";
   private HAPartition partition;
   
   public boolean echo(boolean echo, ClusterNode sender)
   {
      // Only reply echo if I'm *not* the node that send it.
      if (!sender.equals(partition.getClusterNode()))
      {
         return echo;
      }
      return false;
   }

   public void setHAPartition(HAPartition partition)
   {
      log.debug("Set partition: " + partition);
      this.partition = partition;
   }

   public List callEchoOnCluster(boolean echo, boolean excludeSelf, ResponseFilter filter) throws Exception
   {
      log.debug("callEchoOnCluster(" + echo+ ", " + excludeSelf + ", " + filter+ ")");
      return partition.callMethodOnCluster(NAME, "echo", new Object[] {echo, partition.getClusterNode()}, 
            new Class[] {boolean.class, ClusterNode.class}, excludeSelf, filter);
   }
   
   public void start() throws Exception
   {
      log.debug("Register rpc handler " + this + " with " + NAME);
      partition.registerRPCHandler(NAME, this);
   }
   
   public void stop() throws Exception
   {
      log.debug("Unregister rpc handler " + this + " with " + NAME);
      partition.unregisterRPCHandler(NAME, this);
   }
}
