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

import java.net.UnknownHostException;

/**
 * HA-JNDI service that provides JNDI services in a clustered way.
 * Bindings are replicated cluster-wide.
 * Lookups are:
 *    - first resolved locally in the cluster-wide tree
 *    - if not available, resolved in the local underlying JNDI tree
 *    - if not available, the query is broadcast on the cluster and each node determines
 *      if it has one in its local JNDI tree
 *
 * The HA-JNDI service also provides an automatic-discovery feature that allow clients
 * to resolve the service through multicast.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */
public interface HANamingServiceMBean
   extends DetachedHANamingServiceMBean
{
   /**
    * RmiPort to be used by the HA-JNDI service once bound. 0 => auto.
    */
   void setRmiPort(int p);
   int getRmiPort();

   /**
    * RmiBindAddress to be used by the HA-JNDI service once bound.
    * @param address
    */
   void setRmiBindAddress(String address) throws UnknownHostException;
   String getRmiBindAddress();
   
   /**
    * Client socket factory to be used for client-server RMI invocations during JNDI queries
    */
   String getClientSocketFactory();
   void setClientSocketFactory(String factoryClassName)
           throws ClassNotFoundException, InstantiationException, IllegalAccessException;
   /**
    * Server socket factory to be used for client-server RMI invocations during JNDI queries
    */
   String getServerSocketFactory();
   void setServerSocketFactory(String factoryClassName)
           throws ClassNotFoundException, InstantiationException, IllegalAccessException;
   
   /**
    * LoadBalancePolicy to be used by the HA-JNDI service.
    */
   void setLoadBalancePolicy(String policyName);
   String getLoadBalancePolicy();

}

