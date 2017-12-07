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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * The JBoss JSR-77.3.16 implementation of the WebModule model
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class WebModule extends J2EEModule
   implements WebModuleMBean
{

   private static final String[] eventTypes = {NotificationConstants.OBJECT_CREATED,
                                               NotificationConstants.OBJECT_DELETED};

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(WebModule.class);

   /**
    * list of Servlet names as strings
    */
   private List servletNames = new ArrayList();

   private String jbossWebDD;

   /**
    * used to see if we should remove our parent when we are destroyed.
    */
   private static final Map fakeJ2EEApps = new HashMap();

   // Static --------------------------------------------------------

   /**
    * Creates the JSR-77 WebModule
    *
    * @param mbeanServer      MBeanServer the WebModule is created on
    * @param earName          Name of the Application but if null object then it
    *                         is a standalone module (no EAR wrapper around)
    * @param warName          Name of the war
    * @param pURL             URL path to the local deployment of the module (where to find the DD file)
    * @param webContainerName the JBoss web container mbean name
    */
   public static ObjectName create(MBeanServer mbeanServer,
                                   String earName,
                                   String warName,
                                   URL pURL,
                                   ObjectName webContainerName)
   {
      String webXml = null;
      String jbossWebXml = null;
      ObjectName jsr77ParentName = null;
      ObjectName lCreated = null;
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      ObjectName jsr77Name = null;
      try
      {
         // Get the J2EEServer name
         Hashtable props = j2eeServerName.getKeyPropertyList();
         String j2eeServer = props.get(J2EEManagedObject.TYPE) + "=" +
                 props.get("name");


         if (earName == null)
         {
            // If there is no ear use the J2EEServer as the parent
            jsr77ParentName = j2eeServerName;
         }
         else
         {
            // Query for the J2EEApplication matching earName
            ObjectName lApplicationQuery = new ObjectName(J2EEDomain.getDomainName() + ":" +
                    J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEApplication + "," +
                    "name=" + earName + "," +
                    j2eeServer + "," +
                    "*");
            Set lApplications = mbeanServer.queryNames(lApplicationQuery, null);

            if (lApplications.isEmpty())
            {
               lCreated = J2EEApplication.create(mbeanServer,
                       earName,
                       null);
               jsr77ParentName = lCreated;
            } // end of if ()
            else if (lApplications.size() == 1)
            {
               jsr77ParentName = (ObjectName) lApplications.iterator().next();
            } // end of if ()
         }

         // Get the J2EE deployement descriptor
         webXml = J2EEDeployedObject.getDeploymentDescriptor(pURL, J2EEDeployedObject.WEB);
         // Get the JBoss Web deployement descriptor
         jbossWebXml = J2EEDeployedObject.getDeploymentDescriptor(pURL,
            J2EEDeployedObject.JBOSS_WEB);
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 WebModule: " + warName, e);
         return null;
      }

      try
      {
         // Get JVM of the j2eeServer
         String[] jvms = (String[]) mbeanServer.getAttribute(j2eeServerName,
                 "javaVMs");

         WebModule webModule = new WebModule(warName, jsr77ParentName, jvms, webXml,
                 webContainerName, jbossWebXml);
         jsr77Name = webModule.getObjectName();
         mbeanServer.registerMBean(webModule, jsr77Name);
         //remember if we created our parent, if we did we have to kill it on destroy.
         if (lCreated != null)
         {
            fakeJ2EEApps.put(jsr77Name, lCreated);
         } // end of if ()
         log.debug("Created JSR-77 WebModule: " + jsr77Name);
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 WebModule: " + warName, e);
         return null;
      }
      return jsr77Name;
   }

   /**
    * Destroy a JSR-77 WebModule
    *
    * @param mbeanServer The JMX MBeanServer the desired WebModule is registered on
    * @param jsr77Name   the JSR77 EJBModule component ObjectName
    */
   public static void destroy(MBeanServer mbeanServer, ObjectName jsr77Name)
   {
      try
      {
         mbeanServer.unregisterMBean(jsr77Name);
         log.debug("Remove JSR-77 WebModule: " + jsr77Name);
         ObjectName jsr77ParentName = (ObjectName) fakeJ2EEApps.get(jsr77Name);
         if (jsr77ParentName != null)
         {
            log.debug("Remove fake JSR-77 parent Application: " + jsr77ParentName);
            J2EEApplication.destroy(mbeanServer, jsr77ParentName);
         }
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 WebModule: " + jsr77Name, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param warName          Name to be set which must not be null
    * @param j2eeAppName      the name of the parent JSR77 model component
    * @param jvms             the names of the deployment env JVM JSR77 model components
    * @param webDD            the web.xml descriptor text
    * @param webContainerName the JBoss web container service name for the war
    * @param jbossWebDD       the jboss-web.xml descriptor text
    */
   public WebModule(String warName, ObjectName j2eeAppName, String[] jvms,
                    String webDD, ObjectName webContainerName, String jbossWebDD)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.WebModule, warName, j2eeAppName, jvms, webDD);
      this.jbossWebDD = (jbossWebDD == null ? "" : jbossWebDD);
   }

   // Public --------------------------------------------------------

   /**
    * Return the associated servlet names as Strings.
    *
    * @jmx:managed-attribute
    */
   public String[] getservlets()
   {
      String[] servlets = new String[servletNames.size()];
      servletNames.toArray(servlets);
      return servlets;
   }

   /**
    * @jmx:managed-operation
    */
   public String getservlet(int pIndex)
   {
      if (pIndex >= 0 && pIndex < servletNames.size())
      {
         return (String) servletNames.get(pIndex);
      }
      else
      {
         return null;
      }
   }

   /**
    * @jmx:managed-attribute
    */
   public String getjbossWebDeploymentDescriptor()
   {
      return jbossWebDD;
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.Servlet.equals(lType)
      )
      {
         servletNames.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.Servlet.equals(lType))
      {
         servletNames.remove(pChild.getCanonicalName());
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

   public void postCreation()
   {
      sendNotification(NotificationConstants.OBJECT_CREATED, "Web module created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "Web module destroyed");
   }

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "WebModule[ " + super.toString() +
              ", Servlets: " + servletNames +
              ", JBoss-Web-DD: " + jbossWebDD +
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

