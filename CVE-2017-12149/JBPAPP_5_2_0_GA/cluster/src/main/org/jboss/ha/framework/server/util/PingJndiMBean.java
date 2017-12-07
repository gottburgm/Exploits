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

import java.util.ArrayList;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public interface PingJndiMBean
{
   /** Get the names of JNDI bindings that should be queried on each host
    * @return the array of target names to test
    */
   public String[] getLookupNames();
   /** Set the names of JNDI bindings that should be queried on each host
    * @param names
    */
   public void setLookupNames(String[] names);

   /** Get the Context.PROVIDER_URL regular expression.
    * @return the expression containing the ${host} reference, for example
    * 'jnp://${host}:1099/'
    */
   public String getProviderURLPattern();
   /** Set the expression containing the hostname/IP ${host} reference of
    * the JNDI provider. This expression is used to build the JNDI
    * Context.PROVIDER_URL for each node in the cluster. The expression
    * should contain a "(host)" component that will be replaced with the
    * cluster node hostname.
    *
    * @param regex the regular expression containing the host, for example
    * 'jnp://(host):1099/'
    */
   public void setProviderURLPattern(String regex);


   /** The TopologyMonitorService trigger callback operation.
    *
    * @param deadMembers ArrayList<AddressPort> of nodes that were removed
    * @param newMembers ArrayList<AddressPort> of nodes that were added
    * @param allMembers ArrayList<AddressPort> of nodes currently in the cluster
    * @param logLoggerName the log4j category name used by the
    * TopologyMonitorService. This is used for logging to integrate with
    * the TopologyMonitorService output.
    */
   public void membershipChanged(ArrayList deadMembers, ArrayList newMembers,
      ArrayList allMembers, String logLoggerName);
}
