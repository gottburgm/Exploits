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

import org.jboss.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Root class of the JBoss JSR-77 implementation of ResourceAdapterModule.
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class ResourceAdapterModule extends J2EEModule
   implements ResourceAdapterModuleMBean
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(ResourceAdapterModule.class);

   // list of object names as strings
   private List resourceAdapters = new ArrayList();

   /**
    * The JSR77 ObjectNames of fake J2EEApplications created by standalone jars
    */
   private static final Map fakeJ2EEApps = new HashMap();

   // Static --------------------------------------------------------

   /**
    * Creates the JSR-77 EJBModule
    *
    * @param mbeanServer MBeanServer the EJBModule is created on
    * @param earName     the ear name unless null which indicates a standalone module (no EAR)
    * @param rarName     the RAR name
    * @param pURL        URL path to the local deployment of the module (where to find the DD file)
    * @return the JSR77 ObjectName of the RARModule
    */
   public static ObjectName create(MBeanServer mbeanServer, String earName,
                                   String rarName, URL pURL)
   {
      String lDD = null;
      ObjectName lParent = null;
      ObjectName lCreated = null;
      ObjectName jsr77Name = null;
      // Get the J2EEServer name
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);

      try
      {
         Hashtable props = j2eeServerName.getKeyPropertyList();
         String j2eeServer = props.get(J2EEManagedObject.TYPE) + "=" +
                 props.get("name");

         // if pName is equal to pApplicationName then we have
         // a stand alone Module so do not create a J2EEApplication
         if (earName == null)
         {
            // If there is no ear use the J2EEServer as the parent
            lParent = j2eeServerName;
         }
         else
         {
            ObjectName parentAppQuery = new ObjectName(J2EEDomain.getDomainName() + ":" +
                    J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEApplication + "," +
                    "name=" + earName + "," +
                    j2eeServer + "," +
                    "*");
            Set parentApps = mbeanServer.queryNames(parentAppQuery, null);

            if (parentApps.size() == 0)
            {
               lCreated = J2EEApplication.create(mbeanServer,
                       earName,
                       null);
               lParent = lCreated;
            } // end of if ()
            else if (parentApps.size() == 1)
            {
               lParent = (ObjectName) parentApps.iterator().next();
            } // end of if ()
         }

         // Get the J2EE deployement descriptor
         lDD = J2EEDeployedObject.getDeploymentDescriptor(pURL, J2EEDeployedObject.RAR);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 ResourceAdapterModule: " + rarName, e);
         return null;
      }

      try
      {
         // Get JVM of the j2eeServer
         String[] jvms = (String[]) mbeanServer.getAttribute(j2eeServerName,
                 "javaVMs");

         // Now create the ResourceAdapterModule
         ResourceAdapterModule rarModule = new ResourceAdapterModule(rarName,
                 lParent, jvms, lDD);
         jsr77Name = rarModule.getObjectName();
         mbeanServer.registerMBean(rarModule, jsr77Name);

         if (lCreated != null)
         {
            fakeJ2EEApps.put(jsr77Name, lCreated);
         }
         log.debug("Created JSR-77 EJBModule: " + jsr77Name);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 ResourceAdapterModule: " + rarName, e);
      }
      return jsr77Name;
   }

   /**
    * Destroyes the given JSR-77 RARModule
    *
    * @param mbeanServer The JMX MBeanServer the desired RARModule is registered on
    * @param jsr77Name   the JSR77 RARModule component ObjectName
    */
   public static void destroy(MBeanServer mbeanServer, ObjectName jsr77Name)
   {
      try
      {
         log.debug("destroy(), remove RARModule: " + jsr77Name);
         mbeanServer.unregisterMBean(jsr77Name);

         ObjectName jsr77ParentName = (ObjectName) fakeJ2EEApps.get(jsr77Name);
         if (jsr77ParentName != null)
         {
            log.debug("Remove fake JSR-77 parent Application: " + jsr77ParentName);
            J2EEApplication.destroy(mbeanServer, jsr77ParentName);
         }
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 RARModule: " + jsr77Name, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param rarName               Name to be set which must not be null
    * @param jsr77ParentName       ObjectName of the Parent this Module belongs
    *                              too. Either it is a J2EEApplication or J2EEServer
    *                              if a standalone module.
    * @param pDeploymentDescriptor
    * @throws InvalidParameterException If the given Name is null
    */
   public ResourceAdapterModule(String rarName, ObjectName jsr77ParentName,
                                String[] pJVMs, String pDeploymentDescriptor)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.ResourceAdapterModule, rarName, jsr77ParentName, pJVMs, pDeploymentDescriptor);
   }

   // Public --------------------------------------------------------

   // ResourceAdapterodule implementation --------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getresourceAdapters()
   {
      return (String[]) resourceAdapters.toArray(new String[resourceAdapters.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getresourceAdapter(int pIndex)
   {
      if (pIndex >= 0 && pIndex < resourceAdapters.size())
      {
         return (String) resourceAdapters.get(pIndex);
      }
      else
      {
         return null;
      }
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.ResourceAdapter.equals(lType))
      {
         resourceAdapters.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.ResourceAdapter.equals(lType))
      {
         resourceAdapters.remove(pChild.getCanonicalName());
      }
   }

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "ResourceAdapterModule[ " + super.toString() +
              "ResourceAdapters: " + resourceAdapters +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @param jsr77ParentName the WebModule parent's JSR77 ObjectName
    * @return A hashtable with the J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName jsr77ParentName)
   {
      Hashtable parentKeys = new Hashtable();
      Hashtable parentProps = jsr77ParentName.getKeyPropertyList();
      String parentName = (String) parentProps.get("name");
      String j2eeType = (String) parentProps.get(J2EEManagedObject.TYPE);

      // Check if parent is a J2EEServer or J2EEApplication
      if (j2eeType.equals(J2EETypeConstants.J2EEApplication) == false)
      {
         // J2EEServer
         parentKeys.put(J2EETypeConstants.J2EEServer, parentName);
         parentKeys.put(J2EETypeConstants.J2EEApplication, "null");
      }
      else
      {
         // J2EEApplication
         parentKeys.put(J2EETypeConstants.J2EEApplication, parentName);
         String j2eeServerName = (String) parentProps.get(J2EETypeConstants.J2EEServer);
         parentKeys.put(J2EETypeConstants.J2EEServer, j2eeServerName);
      }

      return parentKeys;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
