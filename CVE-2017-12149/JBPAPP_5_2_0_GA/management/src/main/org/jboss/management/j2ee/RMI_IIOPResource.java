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

import java.security.InvalidParameterException;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.RMI_IIOPResource RMI_IIOPResource}.
 *
 * AS Currently CorbaORBService does not support to be restarted therefore no manageability
 * 
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81025 $
 */
public class RMI_IIOPResource extends J2EEResource
   implements RMI_IIOPResourceMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(RMI_IIOPResource.class);

   private ObjectName mService;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName, ObjectName corbaServiceName)
   {
      ObjectName lServer = null;
      try
      {
         lServer = (ObjectName) mbeanServer.queryNames(new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," +
                 "*"),
                 null).iterator().next();
      }
      catch (Exception e)
      {
         log.error("Could not find parent J2EEServer", e);
         return null;
      }
      try
      {
         RMI_IIOPResource rmiiiopRes = new RMI_IIOPResource(resName, lServer, corbaServiceName);
         ObjectName jsr77Name = rmiiiopRes.getObjectName();
         mbeanServer.registerMBean(rmiiiopRes, jsr77Name);
         log.debug("Created JSR-77 RMI_IIOPResource: " + resName);
         
         return jsr77Name;
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 RMI_IIOPResource: " + resName, e);
         return null;
      }
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         J2EEManagedObject.removeObject(mbeanServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," +
                 "name=" + resName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 RMI_IIOPResource: " + resName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the RMI_IIOPResource
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    */
   public RMI_IIOPResource(String resName, ObjectName pServer, ObjectName corbaServiceName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.RMI_IIOPResource, resName, pServer);
      log.debug("Service name: " + corbaServiceName);
      mService = corbaServiceName;
//      mState = new StateManagement( this );
   }

   // javax.managment.j2ee.EventProvider implementation -------------


   public String[] getEventTypes()
   {
      return StateManagement.stateTypes;
   }

   public String getEventType(int pIndex)
   {
      if (pIndex >= 0 && pIndex < StateManagement.stateTypes.length)
      {
         return StateManagement.stateTypes[pIndex];
      }
      else
      {
         return null;
      }
   }

   public void postCreation()
   {
      sendNotification(NotificationConstants.OBJECT_CREATED, "RMI_IIOP Resource created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "RMI_IIOP Resource deleted");
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "RMI_IIOPResource { " + super.toString() + " } [ " +
              "Service name: " + mService +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
