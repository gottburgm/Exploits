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
 * Root class of the JBoss JSR-77 implementation of EJBModule.
 *
 * @author Andreas Schaefer.
 * @author Scott.Stark@jboss.org
 * @author thomas.diesler@jboss.org
 * @version $Revision: 81025 $
 * @jmx:mbean extends="org.jboss.management.j2ee.EventProvider, org.jboss.management.j2ee.J2EEModuleMBean"
 */
public class EJBModule
        extends J2EEModule
        implements EJBModuleMBean
{
   private static final String[] eventTypes = {NotificationConstants.OBJECT_CREATED,
                                               NotificationConstants.OBJECT_DELETED};

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(EJBModule.class);

   // list of object names as strings
   private List mEJBs = new ArrayList();

   private ObjectName moduleServiceName;
   private String mJBossDD;
   private String mJAWSDD;
   private String mCMPDD;

   /**
    * The JSR77 ObjectNames of fake J2EEApplications created by standalone jars
    */
   private static final Map fakeJ2EEApps = new HashMap();

   // Static --------------------------------------------------------

   /**
    * Creates the JSR-77 EJBModule
    *
    * @param mbeanServer       MBeanServer the EJBModule is created on
    * @param earName           the ear name unless null which indicates a standalone module (no EAR)
    * @param jarName           the ejb.jar name
    * @param pURL              URL path to the local deployment of the module (where to find the DD file)
    * @param moduleServiceName ObjectName of the EjbModule service to start and stop the module
    * @return the JSR77 ObjectName of the EJBModule
    */
   public static ObjectName create(MBeanServer mbeanServer,
                                   String earName,
                                   String jarName,
                                   URL pURL,
                                   ObjectName moduleServiceName)
   {
      String lDD = null;
      String lJBossDD = null;
      String lJAWSDD = null;
      String lCMPDD = null;
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

         if (earName == null)
         {
            // If there is no ear use the J2EEServer as the parent
            lParent = j2eeServerName;
         }
         else
         {
            // Query for the J2EEApplication matching earName
            ObjectName lApplicationQuery = new ObjectName(J2EEDomain.getDomainName() + ":" +
                    J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEApplication + "," +
                    "name=" + earName + "," +
                    j2eeServer + "," +
                    "*");
            Set parentApps = mbeanServer.queryNames(lApplicationQuery, null);

            if (parentApps.isEmpty())
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
         lDD = J2EEDeployedObject.getDeploymentDescriptor(pURL, J2EEDeployedObject.EJB);
         // Get the JBoss deployement descriptor
         lJBossDD = J2EEDeployedObject.getDeploymentDescriptor(pURL, J2EEDeployedObject.JBOSS);
         // Get the JAWS deployement descriptor
         lJAWSDD = J2EEDeployedObject.getDeploymentDescriptor(pURL, J2EEDeployedObject.JAWS);
         // Get the CMP 2.0 deployement descriptor
         lCMPDD = J2EEDeployedObject.getDeploymentDescriptor(pURL, J2EEDeployedObject.CMP);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 EJBModule: " + jarName, e);
         return null;
      }

      try
      {
         // Get JVM of the j2eeServer
         String[] jvms = (String[]) mbeanServer.getAttribute(j2eeServerName,
                 "javaVMs");

         EJBModule ejbModule = new EJBModule(jarName, lParent,
                 jvms,
                 lDD,
                 moduleServiceName,
                 lJBossDD,
                 lJAWSDD,
                 lCMPDD);
         jsr77Name = ejbModule.getObjectName();
         mbeanServer.registerMBean(ejbModule, jsr77Name);
         // If we created our parent, if we have to delete it in destroy.
         if (lCreated != null)
         {
            fakeJ2EEApps.put(jsr77Name, lCreated);
         }
         log.debug("Created JSR-77 EJBModule: " + jsr77Name);
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 EJBModule: " + jarName, e);
      }

      return jsr77Name;
   }

   /**
    * Destroyes the given JSR-77 EJB-Module
    *
    * @param mbeanServer The JMX MBeanServer the desired EJB-Module is registered on
    * @param jsr77Name   the JSR77 EJBModule component ObjectName
    */
   public static void destroy(MBeanServer mbeanServer, ObjectName jsr77Name)
   {
      try
      {
         log.debug("destroy(), remove EJB-Module: " + jsr77Name);
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
         log.debug("Could not destroy JSR-77 EJBModule: " + jsr77Name, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param jarName               the ejb jar name which must not be null
    * @param jsr77ParentName       ObjectName of the Parent this Module belongs
    *                              too. Either it is a J2EEApplication or J2EEServer
    *                              if a standalone module.
    * @param pJVMs                 Array of ObjectNames of the JVM this module is deployed on
    * @param pDeploymentDescriptor Content of the module deployment descriptor
    * @param moduleServiceName     ObjectName of the service this Managed Object represent
    *                              used for state management (start and stop)
    * @throws MalformedObjectNameException If name or application name is incorrect
    * @throws InvalidParameterException    If the given Name is null
    */
   public EJBModule(String jarName,
                    ObjectName jsr77ParentName,
                    String[] pJVMs,
                    String pDeploymentDescriptor,
                    ObjectName moduleServiceName,
                    String pJBossDD,
                    String pJAWSDD,
                    String pCMPDD)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.EJBModule, jarName, jsr77ParentName, pJVMs, pDeploymentDescriptor);
      this.moduleServiceName = moduleServiceName;
      mJBossDD = (pJBossDD == null ? "" : pJBossDD);
      mJAWSDD = (pJAWSDD == null ? "" : pJAWSDD);
      mCMPDD = (pCMPDD == null ? "" : pCMPDD);
   }

   // Public --------------------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getejbs()
   {
      return (String[]) mEJBs.toArray(new String[mEJBs.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getejb(int pIndex)
   {
      if (pIndex >= 0 && pIndex < mEJBs.size())
      {
         return (String) mEJBs.get(pIndex);
      }
      else
      {
         return null;
      }
   }

   /**
    * @return JBoss Deployment Descriptor
    * @jmx:managed-attribute
    */
   public String getjbossDeploymentDescriptor()
   {
      return mJBossDD;
   }

   /**
    * @return JAWS Deployment Descriptor
    * @jmx:managed-attribute
    */
   public String getjawsDeploymentDescriptor()
   {
      return mJAWSDD;
   }

   /**
    * @return CMP 2.0 Deployment Descriptor
    * @jmx:managed-attribute
    */
   public String getcmpDeploymentDescriptor()
   {
      return mCMPDD;
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.EntityBean.equals(lType) ||
              J2EETypeConstants.StatelessSessionBean.equals(lType) ||
              J2EETypeConstants.StatefulSessionBean.equals(lType) ||
              J2EETypeConstants.MessageDrivenBean.equals(lType)
      )
      {
         mEJBs.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.EntityBean.equals(lType) ||
              J2EETypeConstants.StatelessSessionBean.equals(lType) ||
              J2EETypeConstants.StatefulSessionBean.equals(lType) ||
              J2EETypeConstants.MessageDrivenBean.equals(lType)
      )
      {
         mEJBs.remove(pChild.getCanonicalName());
      }
   }

   public void postCreation()
   {
      sendNotification(NotificationConstants.OBJECT_CREATED, "EJB Module created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "EJB Module destroyed");
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

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "EJBModule[ " + super.toString() +
              ", EJBs: " + mEJBs +
              ", JBoss-DD: " + mJBossDD +
              ", JAWS-DD: " + mJAWSDD +
              ", CMP-2.0-DD: " + mCMPDD +
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

/*
vim:ts=3:sw=3:et
*/
