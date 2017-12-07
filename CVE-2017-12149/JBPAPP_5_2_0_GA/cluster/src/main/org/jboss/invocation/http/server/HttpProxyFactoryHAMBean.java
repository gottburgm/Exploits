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
package org.jboss.invocation.http.server;

import javax.management.ObjectName;

import org.jboss.invocation.http.server.HttpProxyFactoryMBean;

/** An mbean interface that extends the HttpProxyFactoryMBean to provide
 * support for cluster aware proxies. This interface adds the
 * ability to configure the load-balancing policy of the proxy as well
 * as the cluster partition name the mbean belongs to.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public interface HttpProxyFactoryHAMBean extends HttpProxyFactoryMBean
{
   /** Get the server side mbean that exposes the invoke operation for the
    exported interface */
   public Class getLoadBalancePolicy();
   /** Set the server side mbean that exposes the invoke operation for the
    exported interface */
   public void setLoadBalancePolicy(Class policyClass);

   /** Get the name of the cluster partition the invoker is deployed in
    */
   public String getPartitionName();
   /** Set the name of the cluster partition the invoker is deployed in
    */
   public void setPartitionName(String name);

   /** A read-only property for accessing the non-wrapped JMX invoker
    *
    */
   public ObjectName getRealJmxInvokerName();
}
