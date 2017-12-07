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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.logging.Logger;

/**
 * Election policy that chooses the node where the singleton should run based on 
 * the given preferred master node in ip_address:port_number or 
 * host_name:port_number format. If the preferred master is null, or its 
 * ip_address does not resolve to a valid host name, or the port number is 
 * invalid, it delegates to the standard policy.  
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @author Paul Ferraro
 */
public class PreferredMasterElectionPolicy
   extends HASingletonElectionPolicySimple 
   implements PreferredMasterElectionPolicyMBean
{
   private Logger log = Logger.getLogger(this.getClass());
   
   private volatile InetSocketAddress preferredMaster;
   
   // -------------------------------------------------------------  Properties
   
   /**
    * @see PreferredMasterElectionPolicyMBean#setPreferredMaster(String)
    */
   public void setPreferredMaster(String value)
   {
      String node = (value != null) ? value.trim() : "";
      
      if (node.length() > 0)
      {
         try
         {
            URI uri = new URI("cluster://" + node);
            
            String host = uri.getHost();
            
            if (host == null)
            {
               throw new IllegalArgumentException("Cannot extract host/address from " + node);
            }
            
            this.preferredMaster = new InetSocketAddress(InetAddress.getByName(host), uri.getPort());
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException("Cannot extract URI from " + node, e);
         }
         catch (UnknownHostException e)
         {
            throw new IllegalArgumentException("Cannot resolve host from " + node, e);
         }
      }
      else
      {
         this.preferredMaster = null;
      }
   }
   
   /**
    * @see PreferredMasterElectionPolicyMBean#getPreferredMaster()
    */
   public String getPreferredMaster()
   {
      InetSocketAddress address = this.preferredMaster;
      
      return (address != null) ? address.toString() : null;
   }

   // -----------------------------------------------------  HASingletonElector
   
   @Override
   public ClusterNode elect(List<ClusterNode> candidates)
   {
      InetSocketAddress sockAddress = this.preferredMaster;
      
      ClusterNode master = null;
      
      // If preferred master is defined and contained in cluster, return it
      if (sockAddress != null) 
      {
         InetAddress address = sockAddress.getAddress();
         int port = sockAddress.getPort();
         
         // First find by address
         master = this.find(candidates, address.getHostAddress(), port);
         
         if (master == null)
         {
            // Then try by hostname
            master = this.find(candidates, address.getHostName(), port);
         }
      }
      
      return (master != null) ? master : super.elect(candidates);
   }
   
   private ClusterNode find(List<ClusterNode> candidates, String host, int port)
   {
      String node = host + ":" + port;
      
      this.log.debug("Checking if " + node + " is in candidate list: " + candidates);
      
      for (ClusterNode candidate: candidates)
      {
         if (candidate.getName().equals(node))
         {
            return candidate;
         }
      }
      
      return null;
   }
}