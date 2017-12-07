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
 * Root class of the JBoss JSR-77 implementation of JNDIResource.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public class JNDIResource extends J2EEResource
   implements JNDIResourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JNDIResource.class);

   // Attributes ----------------------------------------------------

   private StateManagement mState;
   private ObjectName jndiServiceName;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName jndiServiceName)
   {
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      ObjectName jsr77Name = null;
      try
      {
         JNDIResource jndiRes = new JNDIResource(resName, j2eeServerName, jndiServiceName);
         jsr77Name = jndiRes.getObjectName();
         mbeanServer.registerMBean(jndiRes, jsr77Name);
         log.debug("Created JSR-77 JNDIResource: " + resName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JNDIResource: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         J2EEManagedObject.removeObject(mbeanServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JNDIResource + "," +
                 "name=" + resName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 JNDIResource: " + resName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param resName Name of the JNDIResource
    */
   public JNDIResource(String resName, ObjectName mbeanServer, ObjectName jndiServiceName)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JNDIResource, resName, mbeanServer);
      log.debug("Service name: " + jndiServiceName);
      this.jndiServiceName = jndiServiceName;
      mState = new StateManagement(this);
   }

   // Public --------------------------------------------------------

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
         server.invoke(jndiServiceName,
                 "start",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         log.debug("Start of JNDI Resource failed", e);
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
         server.invoke(jndiServiceName,
                 "stop",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         log.debug("Stop of JNDI Resource failed", e);
      }
   }

   public void postCreation()
   {
      try
      {
         server.addNotificationListener(jndiServiceName, mState, null, null);
      }
      catch (JMException e)
      {
         log.debug("Failed to add notification listener", e);
      }
      sendNotification(NotificationConstants.OBJECT_CREATED, "JNDI Resource created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "JNDI Resource destroyed");
      // Remove the listener of the target MBean
      try
      {
         server.removeNotificationListener(jndiServiceName, mState);
      }
      catch (JMException jme)
      {
         // When the service is not available anymore then just ignore the exception
      }
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JNDIResource { " + super.toString() + " } [ " +
              " ]";
   }

}
