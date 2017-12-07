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
package org.jboss.ha.singleton;

/**
 * MBean interface for the preferred master election policy that allows 
 * preferred master to be changed at runtime. 
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public interface PreferredMasterElectionPolicyMBean extends HASingletonElectionPolicySimpleMBean
{
   /**
    * Sets the preferred master node. As long as the preferred master node
    * presents in the cluster, it will be always selected as master node,
    * no matter what the election policy is.
    * @param node String format of ip_address:port_number or 
    * host_name:port_number.
    */
   void setPreferredMaster(String node);
   
   /**
    * Get the preferred master node.
    * 
    * @return preferred master node in ip_address:port_number or 
    * host_name:port_number format.
    */
   String getPreferredMaster();
}
