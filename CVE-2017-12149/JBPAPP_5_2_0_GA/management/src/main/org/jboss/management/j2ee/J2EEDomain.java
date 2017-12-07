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
package org.jboss.management.j2ee;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Root class of the JBoss JSR-77 implementation of
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class J2EEDomain extends J2EEManagedObject
   implements J2EEDomainMBean
{

   // Attributes ----------------------------------------------------
   /**
    * The local server J2EEDomain implementation name
    */
   private static String domainName = null;

   /**
    * list of servers associated with the domain as strings
    */
   private List serverNames = new ArrayList();

   // Static --------------------------------------------------------
   /**
    * Get the local J2EEDomain instance name
    *
    * @return the J2EEDomain object name for the local server.
    */
   public static String getDomainName()
   {
      return domainName;
   }

   /**
    * Query for the J2EEServer MBean in the given domain.
    *
    * @param mbeanServer the local MBeanServer
    * @return the J2EEServer name if found, null otherwise
    */
   public static ObjectName getDomainServerName(MBeanServer mbeanServer)
   {
      ObjectName domainServer = null;
      try
      {
         // Query for all MBeans matching the J2EEServer naming convention
         ObjectName serverQuery = new ObjectName(domainName + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," + "*");

         Set servers = mbeanServer.queryNames(serverQuery, null);
         if (servers.isEmpty() == false)
         {
            domainServer = (ObjectName) servers.iterator().next();
         }
      }
      catch (Exception ignore)
      {
      }
      return domainServer;
   }

   // Constructors --------------------------------------------------

   public J2EEDomain(String domainName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(domainName, J2EETypeConstants.J2EEDomain, domainName);
      J2EEDomain.domainName = domainName;
   }

   // Public --------------------------------------------------------

   /**
    * Return the J2EEServer names associated with this domain.
    *
    * @jmx:managed-attribute
    */
   public String[] getservers()
   {
      String[] servers = new String[serverNames.size()];
      serverNames.toArray(servers);
      return servers;
   }

   /**
    * @jmx:managed-operation
    */
   public String getserver(int pIndex)
   {
      if (pIndex >= 0 && pIndex < serverNames.size())
      {
         return (String) serverNames.get(pIndex);
      }
      return null;
   }

   // J2EEManagedObject implementation ----------------------------------------------

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.J2EEServer.equals(lType))
      {
         serverNames.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.J2EEServer.equals(lType))
      {
         serverNames.remove(pChild.getCanonicalName());
      }
   }
   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "J2EEDomain { " + super.toString() + " } [ " +
              ", servers: " + serverNames +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
