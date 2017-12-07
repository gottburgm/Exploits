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
package org.jboss.ha.jndi;

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Map;
import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.jndi.spi.DistributedTreeManager;
import org.jboss.invocation.Invocation;
import org.jboss.util.threadpool.BasicThreadPoolMBean;

/** The standard mbean management interface for the DetachedHANamingService
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */
public interface DetachedHANamingServiceMBean
   extends org.jboss.system.ServiceMBean
{
   /** 
    * Gets the name of the partition used by this service.  This is a 
    * convenience method as the partition name is an attribute of HAPartition.
    * 
    * @return the name of the partition
    */
   String getPartitionName();
   
   /**
    * Get the underlying partition used by this service.
    * 
    * @return the partition
    */
   HAPartition getHAPartition();
   
   /**
    * Sets the underlying partition used by this service.
    * 
    * @param clusterPartition the partition
    */
   void setHAPartition(HAPartition clusterPartition);
   
   /**
    * Get the DistributedTreeManager used by this service.
    * 
    * @return the cache
    */
   DistributedTreeManager getDistributedTreeManager();
   
   /**
    * Sets the DistributedTreeManager used by this service.
    * 
    * @param cache the cache
    */
   void setDistributedTreeManager (DistributedTreeManager distributedTreeManager);

   /** Get the proxy factory service name used to create the Naming transport
    * proxy.
    */ 
   public ObjectName getProxyFactoryObjectName();
   /** Set the proxy factory service name used to create the Naming transport
    * proxy.
    */ 
   public void setProxyFactoryObjectName(ObjectName proxyFactory);

   /** Get the bootstrap port on which the HA-JNDI stub is made available
    */
   int getPort();
   /** Set the bootstrap port on which the HA-JNDI stub is made available
    */
   void setPort(int p);

   /** Get the bootstrap IP address on which the HA-JNDI stub is made available
    */
   String getBindAddress();
   /** Set the bootstrap IP address on which the HA-JNDI stub is made available
    */
   void setBindAddress(String host) throws UnknownHostException;

   /** Get the accept backlog for the bootstrap server socket
    */
   int getBacklog();
   /** Set the accept backlog for the bootstrap server socket
    */
   void setBacklog(int backlog);
 
   /**
    * prevent autodiscovery service from starting
    */
   void setDiscoveryDisabled(boolean disabled);

   /**
    * prevent autodiscovery service from starting
    */
   boolean getDiscoveryDisabled();

   /** Get the Auto-discovery multicast port
    */
   int getAutoDiscoveryGroup();
   /** Set the Auto-discovery multicast port.
    */
   void setAutoDiscoveryGroup(int adGroup);

   /** Get the auto-discovery bootstrap multicast address.
    */ 
   String getAutoDiscoveryAddress();
   /** Set the auto-discovery bootstrap multicast address. If null or empty, no
    * auto-discovery bootstrap socket will be created.
    */
   void setAutoDiscoveryAddress(String adAddress);

   /** Get the auto-discovery bootstrap multicast bind address.
    */ 
   String getAutoDiscoveryBindAddress();
   /** Set the auto-discovery bootstrap multicast bind address. If not specified
    * and a BindAddress is specified, the BindAddress will be used.
    */
   void setAutoDiscoveryBindAddress(String adAddress) throws UnknownHostException;

   /** Get the TTL (time-to-live) for autodiscovery IP multicast packets */
   int getAutoDiscoveryTTL();

   /** Set the TTL (time-to-live) for autodiscovery IP multicast packets */
   void setAutoDiscoveryTTL(int ttl);

   /** Set the thread pool used for the bootstrap and autodiscovery lookups
    *
    * @param poolMBean
    */
   public void setLookupPool(BasicThreadPoolMBean poolMBean);

   /** Expose the Naming service interface mapping as a read-only attribute
    *
    * @jmx:managed-attribute
    *
    * @return A Map<Long hash, Method> of the Naming interface
    */
   public Map<Long, Method> getMethodMap();

   /** Expose the Naming service via JMX for detached invokers.
    *
    * @param invocation    A pointer to the invocation object
    * @return              Return value of method invocation.
    *
    * @throws Exception    Failed to invoke method.
    */
   public Object invoke(Invocation invocation) throws Exception;
}