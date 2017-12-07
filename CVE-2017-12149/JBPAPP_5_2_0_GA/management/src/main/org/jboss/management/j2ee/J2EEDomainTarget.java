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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEDomain J2EEDomain}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class J2EEDomainTarget extends J2EEManagedObject
//   implements J2EEDomainTargetMBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   
   private List mServers = new ArrayList();
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   /**
    * @jmx:managed-constructor
    */
   public J2EEDomainTarget(String pDomainName)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(pDomainName, "J2EEDomain", "Manager");
   }
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------
   
   /**
    * @jmx:managed-attribute description="List of all Servers on this Managment Domain"
    * access="READ"
    * persistPolicy="Never"
    * persistPeriod="30"
    * currencyTimeLimit="30"
    */
   public String[] getservers()
   {
      return (String[]) mServers.toArray(new String[mServers.size()]);
   }

   /**
    * @jmx:managed-operation description="Returns the requested Server" impact="INFO"
    */
   public String getserver(int pIndex)
   {
      if (pIndex >= 0 && pIndex < mServers.size())
      {
         return (String) mServers.get(pIndex);
      }
      return null;
   }

   public String toString()
   {
      return "J2EEDomainTarget { " + super.toString() + " } [ " +
              ", servers: " + mServers +
              " ]";
   }

   /**
    * @jmx:managed-operation description="adds a new child of this Management Domain" impact="INFO"
    */
   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.J2EEServer.equals(lType))
      {
         mServers.add(pChild.getCanonicalName());
      }
   }

   /**
    * @jmx:managed-operation description="removes a new child of this Management Domain" impact="ACTION"
    */
   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.J2EEServer.equals(lType))
      {
         mServers.remove(pChild.getCanonicalName());
      }
   }
}
