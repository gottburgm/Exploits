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

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.URLResource URLResource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public class URLResource extends J2EEResource
   implements URLResourceMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(URLResource.class);

   private StateManagement mState;
   private ObjectName mService;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer pServer, String pName, ObjectName pService)
   {
      ObjectName lServer = null;
      try
      {
         lServer = (ObjectName) pServer.queryNames(new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," +
                 "*"),
                 null).iterator().next();
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 URLResource: " + pName, e);
         return null;
      }
      try
      {
         // Now create the URLResource Representant
         return pServer.createMBean("org.jboss.management.j2ee.URLResource",
                 null,
                 new Object[]{
                    pName,
                    lServer,
                    pService
                 },
                 new String[]{
                    String.class.getName(),
                    ObjectName.class.getName(),
                    ObjectName.class.getName()
                 }).getObjectName();
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 URLResource: " + pName, e);
         return null;
      }
   }

   public static void destroy(MBeanServer pServer, String pName)
   {
      try
      {
         J2EEManagedObject.removeObject(pServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.URLResource + "," +
                 "name=" + pName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 URLResource: " + pName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the URLResource
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    */
   public URLResource(String pName, ObjectName pServer, ObjectName pService)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.URLResource, pName, pServer);
      if (log.isDebugEnabled())
         log.debug("Service name: " + pService);
      mService = pService;
      mState = new StateManagement(this);
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

   // javax.management.j2ee.StateManageable implementation ----------

   public long getStartTime()
   {
      return mState.getStartTime();
   }

   public int getState()
   {
      return mState.getState();
   }
   public String getStateString()
   {
      return mState.getStateString();
   }

   public void mejbStart()
   {
      try
      {
         server.invoke(mService,
                 "start",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         log.error("Start of URL Resource failed", e);
      }
   }

   public void mejbStartRecursive()
   {
      // No recursive start here
      mejbStart();
   }

   public void mejbStop()
   {
      try
      {
         server.invoke(mService,
                 "stop",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         log.error("Stop of URL Resource failed", e);
      }
   }

   public void postCreation()
   {
      try
      {
         server.addNotificationListener(mService, mState, null, null);
      }
      catch (JMException e)
      {
         log.debug("Failed to add notification listener", e);
      }
      sendNotification(NotificationConstants.OBJECT_CREATED, "URL Resource created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "URL Resource deleted");
      // Remove the listener of the target MBean
      try
      {
         server.removeNotificationListener(mService, mState);
      }
      catch (JMException jme)
      {
         // When the service is not available anymore then just ignore the exception
      }
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "URLResource { " + super.toString() + " } [ " +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
