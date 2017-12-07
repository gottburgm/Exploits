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
package org.jboss.ha.framework.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jgroups.stack.IpAddress;

/**
 * Replacement for a JG IpAddress that doesn't base its representation
 * on the JG address but on the computed node name added to the IPAddress instead.
 * This is to avoid any problem in the cluster as some nodes may interpret a node name
 * differently (IP resolution, name case, FQDN or host name, etc.)
 *
 * @see org.jboss.ha.framework.server.ClusterPartitionMBean
 *
 * @author  <a href="mailto:sacha.labourey@jboss.org">Sacha Labourey</a>.
 * @author Brian Stansberry
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 85945 $
 */

public class ClusterNodeImpl
   implements ClusterNode
{
   // Constants -----------------------------------------------------

   /** The serialVersionUID */
   private static final long serialVersionUID = 2713397663824031616L;
   
   // Attributes ----------------------------------------------------
   
   protected final String id;
   protected String jgId = null;
   protected final IpAddress originalJGAddress;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
   public ClusterNodeImpl(IpAddress jgAddress)
   {
      if (jgAddress.getAdditionalData() == null)
      {
         this.id = jgAddress.getIpAddress().getHostAddress() + ":" + jgAddress.getPort();
      }
      else
      {
         this.id = new String(jgAddress.getAdditionalData());
      }

      this.originalJGAddress = jgAddress;
   }

   // Public --------------------------------------------------------

   public String getName()
   {
      return this.id;
   }

   public String getJGName()
   {
      if (jgId == null)
      {
         jgId = createJGName(); 
      }
      
      return jgId;
   }

   public IpAddress getOriginalJGAddress()
   {
      return this.originalJGAddress;
   }
   public InetAddress getIpAddress()
   {
      return this.originalJGAddress.getIpAddress();
   }
   public int getPort()
   {
      return this.originalJGAddress.getPort();      
   }

   // Comparable implementation ----------------------------------------------

   // Comparable implementation ----------------------------------------------

   public int compareTo(Object o)
   {
      if ((o == null) || !(o instanceof ClusterNodeImpl))
         throw new ClassCastException("ClusterNode.compareTo(): comparison between different classes");

      ClusterNodeImpl other = (ClusterNodeImpl) o;

      return this.id.compareTo(other.id);
   }
   // java.lang.Object overrides ---------------------------------------------------

   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof ClusterNodeImpl)) return false;
      
      ClusterNodeImpl other = (ClusterNodeImpl) obj;
      return this.id.equals(other.id);
   }

   public int hashCode()
   {
      return id.hashCode();
   }

   public String toString()
   {
      return this.getName();
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   protected String getShortName(String hostname)
   {
      int index = hostname.indexOf('.');

      if (hostname == null) return "";
      if (index > 0 && !Character.isDigit(hostname.charAt(0)))
         return hostname.substring(0, index);
      else
         return hostname;
   }
   
   protected String createJGName()
   {
      StringBuffer sb = new StringBuffer();
      java.net.InetAddress jgIPAddr = originalJGAddress.getIpAddress();
      if (jgIPAddr == null)
         sb.append("<null>");
      else
      {
         if (jgIPAddr.isMulticastAddress())
            sb.append(jgIPAddr.getHostAddress());
         else
            sb.append(getShortName(getFastHostName(jgIPAddr)));
      }
      sb.append(":" + originalJGAddress.getPort());
      
      return sb.toString();
   }

   // Private -------------------------------------------------------
   
   /**
    * Tries to determine the hostname of the given InetAddress without 
    * triggering a reverse DNS lookup.  Tries to parse a symbolic hostname 
    * from {@link InetAddress.toString()}, which is documented to return a 
    * String of the form "symbolicname/ipaddress" with 'symbolicname' blank 
    * if not stored in the object.
    * <p/>
    * If the symbolic name cannot be determined from InetAddress.toString(),
    * the value of {@link InetAddress.getHostAddress()} is returned.
    */
   private static String getFastHostName(InetAddress address)
   {
      String result = null;
      
      String hostAddress = address.getHostAddress();
      
      String inetAddr = address.toString();
      int idx = inetAddr.lastIndexOf('/');
      int idx1 = inetAddr.indexOf(hostAddress);
      if (idx1 == idx + 1)
      {
         if (idx == 0)
            result = hostAddress;
         else
            result = inetAddr.substring(0, idx);
      }
      else
      {
         // Doesn't follow the toString() contract!
         result = hostAddress;
      }
      return result;
   }
   
   // Inner classes -------------------------------------------------
   
}
