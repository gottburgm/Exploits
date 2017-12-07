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

import java.util.Hashtable;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss implementation of a custom MBean managed object.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class MBean extends J2EEManagedObject
   implements MBeanMBean
{

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(MBean.class);

   private ObjectName jbossServiceName;
   private StateManagement mState;
   private boolean monitorsStateChanges = false;
   /** Does the mbean have a State attribute */
   private boolean hasState;
   
   // Static --------------------------------------------------------

   /**
    * Create a
    *
    * @param pServer
    * @param pServiceModule
    * @param pTarget
    * @return ObjectName of the MBean
    */
   public static ObjectName create(MBeanServer pServer, String pServiceModule,
                                   ObjectName pTarget)
   {
      String pName = pTarget.toString();
      ObjectName mbeanName = null;
      try
      {
         if (pServiceModule == null)
         {
            log.debug("Parent SAR Module not defined");
            return null;
         }

         MBean mbean = new MBean(pName, new ObjectName(pServiceModule), pTarget);
         mbeanName = mbean.getObjectName();
         pServer.registerMBean(mbean, mbeanName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 MBean: " + pName, e);
      }
      return mbeanName;
   }

   public static void destroy(MBeanServer pServer, String pName)
   {
      try
      {
         if (pName.indexOf(J2EEManagedObject.TYPE + "=" + J2EETypeConstants.MBean) >= 0)
         {
            J2EEManagedObject.removeObject(pServer,
                    pName);
         }
         else
         {
            J2EEManagedObject.removeObject(pServer,
                    pName,
                    J2EEDomain.getDomainName() + ":" +
                    J2EEManagedObject.TYPE + "=" + J2EETypeConstants.MBean +
                    "," + "*");
         }
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 MBean: " + pName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the MBean
    */
   public MBean(String pName, ObjectName pServiceModule, ObjectName pTarget)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.MBean, pName, pServiceModule);
      mState = new StateManagement(this);
      jbossServiceName = pTarget;
   }

   /**
    * Does the MBean monitor state changes of the JBoss MBean service.
    *
    * @return True if the underlying JBoss MBean service is monitored for state
    *         changes.
    * @jmx:managed-attribute
    */
   public boolean isstateMonitored()
   {
      return monitorsStateChanges;
   }

   public boolean isstateManageable()
   {
      return hasState;
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void postCreation()
   {
      try
      {
         // First check if the service implements the NotificationBroadcaster
         monitorsStateChanges = getServer().isInstanceOf(jbossServiceName,
                 "javax.management.NotificationBroadcaster");
         if (monitorsStateChanges)
         {
            getServer().addNotificationListener(jbossServiceName, mState, null, null);
         }
      }
      catch (Exception jme)
      {
         log.debug("Failed to register as listener of: " + jbossServiceName, jme);
      }
      sendNotification(NotificationConstants.OBJECT_CREATED, "MBean created");

      // Initialize the state
      try
      {
         Integer jbossState = (Integer)getServer().getAttribute(jbossServiceName, "State"); 
         int jsr77State = StateManagement.convertJBossState(jbossState.intValue());
         mState.setState(jsr77State);
      }
      catch (Exception e)
      {
         log.trace("Failed to initialize state from: '" + jbossServiceName +
               "' : " + e.getClass().getName() + " : " + e.getMessage());
         hasState = false;
      }
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "MBean destroyed");
      // Remove the listener of the target MBean
      try
      {
         if( monitorsStateChanges )
            getServer().removeNotificationListener(jbossServiceName, mState);
      }
      catch (JMException jme)
      {
         // When the service is not available anymore then just ignore the exception
      }
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
         getServer().invoke(jbossServiceName,
                 "start",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         getLog().error("Start of MBean failed", e);
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
         getServer().invoke(jbossServiceName,
                 "stop",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         getLog().error("Stop of MBean failed", e);
      }
   }

   // java.lang.Object overrides --------------------------------------

   public String toString()
   {
      return "MBean { " + super.toString() + " } []";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the SAR-Module, J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.ServiceModule, lProperties.get("name"));
      // J2EE-Application is never a parent of a MBean therefore set it to "null"
      lReturn.put(J2EETypeConstants.J2EEApplication, "null");
      // J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get(J2EETypeConstants.J2EEServer));

      return lReturn;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
