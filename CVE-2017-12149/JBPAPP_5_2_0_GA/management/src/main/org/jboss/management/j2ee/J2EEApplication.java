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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Root class of the JBoss JSR-77 implementation of J2EEApplication.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 * @todo When all components of a J2EEApplication is state manageable
 * this have to be too !!
 */
public class J2EEApplication extends J2EEDeployedObject
   implements J2EEApplicationMBean
{

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(J2EEApplication.class);
   
   /**
    * The application module names as Strings
    */
   private List moduleNames = new ArrayList();

   // Static --------------------------------------------------------

   /**
    * Create a JSR77 ear model instnace
    *
    * @param mbeanServer the MBeanServer to register with
    * @param earName     the name of the j2ee ear deployment
    * @param url         the ear URL, which may be null to represent a standalone
    *                    jar/war/war without an ear
    * @return the JSR77 ObjectName for the J2EEApplication
    */
   public static ObjectName create(MBeanServer mbeanServer, String earName, URL url)
   {
      String lDD = null;
      ObjectName jsr77Name = null;
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      // First get the deployement descriptor
      lDD = J2EEDeployedObject.getDeploymentDescriptor(url, J2EEDeployedObject.APPLICATION);
      try
      {
         // Now create the J2EEApplication
         J2EEApplication j2eeApp = new J2EEApplication(earName, j2eeServerName, lDD);
         jsr77Name = j2eeApp.getObjectName();
         /* Check to see if the ear is already registered. This will occur when
         an ear is deployed because we do not receive the ear module start
         notification until its contained modules have started. The content
         modules will have created a placeholder ear when they could not find
         an existing J2EEApplication registered.
         */
         if (mbeanServer.isRegistered(jsr77Name) == true)
         {
            // We take the modules from the EAR placeholder
            String[] tmpModules = (String[]) mbeanServer.getAttribute(jsr77Name, "modules");
            // Remove the placeholder and register the j2eeApp
            mbeanServer.unregisterMBean(jsr77Name);
            mbeanServer.registerMBean(j2eeApp, jsr77Name);
            // Add the 
            if (tmpModules != null)
            {
               for (int m = 0; m < tmpModules.length; m++)
                  j2eeApp.addChild(newObjectName(tmpModules[m]));
            }
         }
         else
         {
            mbeanServer.registerMBean(j2eeApp, jsr77Name);
         }
         log.debug("Created JSR-77 J2EEApplication: " + earName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 J2EEApplication: " + jsr77Name, e);
      }
      return jsr77Name;
   }

   /**
    * Destroy the J2EEApplication component
    *
    * @param mbeanServer the MBeanServer used during create
    * @param jsr77Name   the JSR77 J2EEApplication component name
    */
   public static void destroy(MBeanServer mbeanServer, ObjectName jsr77Name)
   {
      try
      {
         mbeanServer.unregisterMBean(jsr77Name);
         log.debug("Destroyed JSR-77 J2EEApplication: " + jsr77Name);
      }
      catch (javax.management.InstanceNotFoundException infe)
      {
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 J2EEApplication: " + jsr77Name, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param name                  Name to be set which must not be null
    * @param pDeploymentDescriptor
    * @throws InvalidParameterException If the given Name is null
    */
   public J2EEApplication(String name, ObjectName mbeanServer, String pDeploymentDescriptor)
           throws MalformedObjectNameException, InvalidParentException
   {
      super(J2EETypeConstants.J2EEApplication, name, mbeanServer, pDeploymentDescriptor);
   }

   // Public --------------------------------------------------------

   // J2EEApplication implementation --------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getmodules()
   {
      return (String[]) moduleNames.toArray(new String[moduleNames.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getmodule(int pIndex)
   {
      if (pIndex >= 0 && pIndex < moduleNames.size())
      {
         return (String) moduleNames.get(pIndex);
      }
      return null;
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (
              J2EETypeConstants.EJBModule.equals(lType) ||
              J2EETypeConstants.WebModule.equals(lType) ||
              J2EETypeConstants.ResourceAdapterModule.equals(lType) ||
              J2EETypeConstants.ServiceModule.equals(lType)
      )
      {
         moduleNames.add(pChild.getCanonicalName());
         try
         {
            // Now it also have to added as child to its
            // parent
            server.invoke(newObjectName(getparent()),
                    "addChild",
                    new Object[]{pChild},
                    new String[]{ObjectName.class.getName()});
         }
         catch (JMException jme)
         {
            // Ignore it because parent has to be there
         }
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (
              J2EETypeConstants.EJBModule.equals(lType) ||
              J2EETypeConstants.WebModule.equals(lType) ||
              J2EETypeConstants.ResourceAdapterModule.equals(lType) ||
              J2EETypeConstants.ServiceModule.equals(lType)
      )
      {
         moduleNames.remove(pChild.getCanonicalName());
         try
         {
            // Now it also have to added as child to its
            // parent
            server.invoke(newObjectName(getparent()),
                    "removeChild",
                    new Object[]{pChild},
                    new String[]{ObjectName.class.getName()});
         }
         catch (JMException jme)
         {
            // Ignore it because parent has to be there
         }
      }
   }

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "J2EEApplication { " + super.toString() + " } [ " +
              "modules: " + moduleNames +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the J2EE Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get("name"));

      return lReturn;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
