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
 * JBoss implementation of the JSR-77 {@link javax.management.j2ee.J2EEServer
 * J2EEServer}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class J2EEServer extends J2EEManagedObject
   implements J2EEServerMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // list of object names as strings
   private List deployedObjectNames = new ArrayList();

   // list of object names as strings
   private List resourceNames = new ArrayList();

   // list of object names as strings
   private List mJVMs = new ArrayList();

   private String mServerVendor = null;

   private String mServerVersion = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public J2EEServer(String pName, ObjectName pDomain, String pServerVendor,
                     String pServerVersion)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.J2EEServer, pName, pDomain);
      mServerVendor = pServerVendor;
      mServerVersion = pServerVersion;
   }

   // Public --------------------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getdeployedObjects()
   {
      String[] deployedObjects = new String[deployedObjectNames.size()];
      deployedObjectNames.toArray(deployedObjects);
      return deployedObjects;
   }

   /**
    * @jmx:managed-operation
    */
   public String getdeployedObject(int pIndex)
   {
      if (pIndex >= 0 && pIndex < deployedObjectNames.size())
      {
         return (String) deployedObjectNames.get(pIndex);
      }
      return null;
   }

   /**
    * @jmx:managed-attribute
    */
   public String[] getresources()
   {
      String[] resources = new String[resourceNames.size()];
      resourceNames.toArray(resources);
      return resources;
   }

   /**
    * @jmx:managed-operation
    */
   public String getresource(int pIndex)
   {
      if (pIndex >= 0 && pIndex < resourceNames.size())
      {
         return (String) resourceNames.get(pIndex);
      }
      return null;
   }

   /**
    * @jmx:managed-attribute
    */
   public String[] getjavaVMs()
   {
      String[] jvms = new String[mJVMs.size()];
      mJVMs.toArray(jvms);
      return jvms;
   }

   /**
    * @jmx:managed-operation
    */
   public String getjavaVM(int pIndex)
   {
      if (pIndex >= 0 && pIndex < mJVMs.size())
      {
         return (String) mJVMs.get(pIndex);
      }
      else
      {
         return null;
      }
   }

   /**
    * @jmx:managed-attribute
    */
   public String getserverVendor()
   {
      return mServerVendor;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getserverVersion()
   {
      return mServerVersion;
   }

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.J2EEApplication.equals(lType) ||
              J2EETypeConstants.EJBModule.equals(lType) ||
              J2EETypeConstants.ResourceAdapterModule.equals(lType) ||
              J2EETypeConstants.WebModule.equals(lType) ||
              J2EETypeConstants.ServiceModule.equals(lType))
      {
         String canonicalName = pChild.getCanonicalName();
         if(!deployedObjectNames.contains(canonicalName))
	    deployedObjectNames.add(canonicalName);
      }
      else if (J2EETypeConstants.JVM.equals(lType))
      {
         mJVMs.add(pChild.getCanonicalName());
      }
      else if (J2EETypeConstants.JNDIResource.equals(lType) ||
              J2EETypeConstants.JMSResource.equals(lType) ||
              J2EETypeConstants.URLResource.equals(lType) ||
              J2EETypeConstants.JTAResource.equals(lType) ||
              J2EETypeConstants.JavaMailResource.equals(lType) ||
              J2EETypeConstants.JDBCResource.equals(lType) ||
              J2EETypeConstants.RMI_IIOPResource.equals(lType) ||
              J2EETypeConstants.JCAResource.equals(lType))
      {
         resourceNames.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.J2EEApplication.equals(lType) ||
              J2EETypeConstants.EJBModule.equals(lType) ||
              J2EETypeConstants.ResourceAdapterModule.equals(lType) ||
              J2EETypeConstants.WebModule.equals(lType) ||
              J2EETypeConstants.ServiceModule.equals(lType))
      {
         deployedObjectNames.remove(pChild.getCanonicalName());
      }
      else if (J2EETypeConstants.JVM.equals(lType))
      {
         mJVMs.remove(pChild.getCanonicalName());
      }
      else if (J2EETypeConstants.JNDIResource.equals(lType) ||
              J2EETypeConstants.JMSResource.equals(lType) ||
              J2EETypeConstants.URLResource.equals(lType) ||
              J2EETypeConstants.JTAResource.equals(lType) ||
              J2EETypeConstants.JavaMailResource.equals(lType) ||
              J2EETypeConstants.JDBCResource.equals(lType) ||
              J2EETypeConstants.RMI_IIOPResource.equals(lType) ||
              J2EETypeConstants.JCAResource.equals(lType))
      {
         resourceNames.remove(pChild.getCanonicalName());
      }
   }

   public String toString()
   {
      return "J2EEServer { " + super.toString() + " } [ " +
              "depoyed objects: " + deployedObjectNames +
              ", resources: " + resourceNames +
              ", JVMs: " + mJVMs +
              ", J2EE vendor: " + mServerVendor +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
