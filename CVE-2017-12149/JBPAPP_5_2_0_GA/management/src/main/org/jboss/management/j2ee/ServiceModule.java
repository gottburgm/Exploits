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

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of ServiceModule model.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81025 $
 */
public class ServiceModule extends J2EEModule
   implements ServiceModuleMBean
{
   private static final String[] eventTypes = {NotificationConstants.OBJECT_CREATED,
                                               NotificationConstants.OBJECT_DELETED};

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(ServiceModule.class);

   // list of object names as strings
   private List mbeans = new ArrayList();

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String moduleName, URL url)
   {
      String lDD = null;
      ObjectName jsr77Name = null;
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      
      // First get the deployement descriptor
      if (url != null && url.getFile().endsWith(".xml"))
      {
         // the url points to -service.xml or -deployer.xml, use as is 
         lDD = J2EEDeployedObject.getDeploymentDescriptor(url, null);
      }
      else
      {
         // the url points to .sar or .deployer, so look for META-INF/jboss-service.xml
         lDD = J2EEDeployedObject.getDeploymentDescriptor(url, J2EEDeployedObject.SAR);
      }

      try
      {
         // Get JVM of the j2eeServer
         String[] jvms = (String[]) mbeanServer.getAttribute(j2eeServerName, "javaVMs");
         // Now create the ServiceModule
         ServiceModule serviceModule = new ServiceModule(moduleName, j2eeServerName, jvms, lDD);
         jsr77Name = serviceModule.getObjectName();
         mbeanServer.registerMBean(serviceModule, jsr77Name);
         log.debug("Created JSR-77 ServiceModule, name: " + moduleName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 ServiceModule: " + moduleName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String pModuleName)
   {
      try
      {
         log.debug("destroy(), remove Service Module: " + pModuleName);
         // If Module Name already contains the JSR-77 Object Name String
         if (pModuleName.indexOf(J2EEManagedObject.TYPE + "=" + J2EETypeConstants.ServiceModule) >= 0)
         {
            J2EEManagedObject.removeObject(mbeanServer, pModuleName);
         }
         else
         {
            J2EEManagedObject.removeObject(mbeanServer,
                    pModuleName,
                    J2EEDomain.getDomainName() + ":" +
                    J2EEManagedObject.TYPE + "=" + J2EETypeConstants.ServiceModule +
                    "," + "*");
         }
      }
      catch (javax.management.InstanceNotFoundException infe)
      {
      }
      catch (Throwable e)
      {
         log.error("Could not destroy JSR-77 ServiceModule: " + pModuleName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param moduleName            the sar deployment module name
    * @param j2eeServerName        the J2EEServer ObjectName parent
    * @param pDeploymentDescriptor
    * @throws InvalidParameterException If the given Name is null
    */
   public ServiceModule(String moduleName, ObjectName j2eeServerName,
                        String[] jvmNames, String pDeploymentDescriptor)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.ServiceModule, moduleName, j2eeServerName, jvmNames, pDeploymentDescriptor);
   }

   // Public --------------------------------------------------------

   // ResourceAdapterodule implementation --------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getMBeans()
   {
      return (String[]) mbeans.toArray(new String[mbeans.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getMBean(int pIndex)
   {
      if (pIndex >= 0 && pIndex < mbeans.size())
      {
         return (String) mbeans.get(pIndex);
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
      if (J2EETypeConstants.MBean.equals(lType))
      {
         mbeans.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.MBean.equals(lType))
      {
         mbeans.remove(pChild.getCanonicalName());
      }
   }

   // javax.managment.j2ee.EventProvider implementation -------------

   public String[] getEventTypes()
   {
      return eventTypes;
   }

   public String getEventType(int index)
   {
      String type = null;
      if (index >= 0 && index < eventTypes.length)
      {
         type = eventTypes[index];
      }
      return type;
   }


   // org.jboss.ServiceMBean overrides ------------------------------------

   public void postCreation()
   {
      sendNotification(NotificationConstants.OBJECT_CREATED, "SAR module created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "SAR module destroyed");
   }

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "ServiceModule[ " + super.toString() +
              "MBeans: " + mbeans +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get("name"));

      return lReturn;
   }
}
