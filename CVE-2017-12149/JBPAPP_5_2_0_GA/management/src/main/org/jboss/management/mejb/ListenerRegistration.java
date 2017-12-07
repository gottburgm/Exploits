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
package org.jboss.management.mejb;

import org.jboss.logging.Logger;

import javax.ejb.CreateException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.j2ee.ManagementHome;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.ListenerRegistration ListenerRegistration}.
 *
 * @author Andreas Schaefer.
 * @version $Revision: 81025 $
 */
public class ListenerRegistration
        implements javax.management.j2ee.ListenerRegistration
{
   // Constants -----------------------------------------------------

   public static final int NOTIFICATION_TYPE_RMI = 0;
   public static final int NOTIFICATION_TYPE_JMS = 1;
   public static final int NOTIFICATION_TYPE_POLLING = 2;

   // Attributes ----------------------------------------------------

   private ManagementHome mHome;
   private int mEventType = NOTIFICATION_TYPE_RMI;
   private String[] mOptions;
   private List mListeners = new ArrayList();

   // Static --------------------------------------------------------

   private static final Logger log = Logger.getLogger(ListenerRegistration.class);

   // Constructors --------------------------------------------------

   public ListenerRegistration(ManagementHome pHome, String[] pOptions)
   {
      if (pHome == null)
      {
         throw new InvalidParameterException("Home Interface must be specified");
      }
      mHome = pHome;
      mOptions = pOptions;
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.ListenerRegistration implementation -----

   public void addNotificationListener(ObjectName pName,
                                       NotificationListener pListener,
                                       NotificationFilter pFilter,
                                       Object pHandback)
           throws
           InstanceNotFoundException,
           RemoteException
   {
      MEJB lManagement = null;
      // Create the remote MBean and register it
      try
      {
         // Get EJB
         lManagement = getMEJB();
         ClientNotificationListener lListener = null;
         switch (mEventType)
         {
            case NOTIFICATION_TYPE_RMI:
               lListener = new RMIClientNotificationListener(pName,
                       pListener,
                       pHandback,
                       pFilter,
                       lManagement);
               break;
            case NOTIFICATION_TYPE_JMS:
               lListener = new JMSClientNotificationListener(pName,
                       pListener,
                       pHandback,
                       pFilter,
                       mOptions[0],
                       mOptions[1], // JNDI-Server name
                       lManagement);
               break;
            case NOTIFICATION_TYPE_POLLING:
               lListener = new PollingClientNotificationListener(pName,
                       pListener,
                       pHandback,
                       pFilter,
                       5000, // Sleeping Period
                       2500, // Maximum Pooled List Size
                       lManagement);
         }
         // Add this listener on the client to remove it when the client goes down
         mListeners.add(lListener);
      }
      catch (Exception e)
      {
         if (e instanceof RuntimeException)
         {
            throw (RuntimeException) e;
         }
         if (e instanceof InstanceNotFoundException)
         {
            throw (InstanceNotFoundException) e;
         }
         throw new RuntimeException("Remote access to perform this operation failed: " + e.getMessage());
      }
      finally
      {
         if (lManagement != null)
         {
            try
            {
               lManagement.remove();
            }
            catch (Exception e)
            {
               log.error("operation failed", e);
            }
         }
      }
   }

   public void removeNotificationListener(ObjectName pName,
                                          NotificationListener pListener)
           throws
           InstanceNotFoundException,
           ListenerNotFoundException,
           RemoteException
   {
      MEJB lManagement = null;
      try
      {
         // Get EJB
         lManagement = getMEJB();

         ClientNotificationListener lCheck = new SearchClientNotificationListener(pName, pListener);
         int i = mListeners.indexOf(lCheck);
         if (i >= 0)
         {
            ClientNotificationListener lListener = (ClientNotificationListener) mListeners.get(i);
            lListener.removeNotificationListener(lManagement);
         }
      }
      catch (Exception e)
      {
         if (e instanceof RuntimeException)
         {
            throw (RuntimeException) e;
         }
         if (e instanceof InstanceNotFoundException)
         {
            throw (InstanceNotFoundException) e;
         }
         throw new RuntimeException("Remote access to perform this operation failed: " + e.getMessage());
      }
      finally
      {
         if (lManagement != null)
         {
            try
            {
               lManagement.remove();
            }
            catch (Exception e)
            {
               log.error("operation failed", e);
            }
         }
      }
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private MEJB getMEJB()
           throws
           CreateException,
           RemoteException
   {
      Object lTemp = mHome.create();
      MEJB lReturn = (MEJB) lTemp;
      return lReturn;
   }

   // Inner classes -------------------------------------------------
}
