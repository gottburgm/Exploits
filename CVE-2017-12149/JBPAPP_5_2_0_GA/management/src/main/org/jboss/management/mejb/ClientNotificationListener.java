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

import javax.management.*;
import java.rmi.RemoteException;
import java.util.Random;

/**
 * Basic Local Listener to receive Notification from a remote JMX Agent
 *
 * @author ???
 * @version $Revision: 81025 $
 */
public abstract class ClientNotificationListener
{
   private ObjectName mSender;
   private ObjectName mRemoteListener;
   protected NotificationListener mClientListener;
   protected Object mHandback;
   private Random mRandom = new Random();

   protected Logger log = Logger.getLogger(this.getClass());

   public ClientNotificationListener(ObjectName pSender,
                                     NotificationListener pClientListener,
                                     Object pHandback)
   {
      mSender = pSender;
      mClientListener = pClientListener;
      mHandback = pHandback;
   }

   public ObjectName createListener(MEJB pConnector,
                                    String pClass,
                                    Object[] pParameters,
                                    String[] pSignatures) throws
           MalformedObjectNameException,
           ReflectionException,
           MBeanRegistrationException,
           MBeanException,
           NotCompliantMBeanException,
           RemoteException
   {
      ObjectName lName = null;
      while (lName == null)
      {
         try
         {
            lName = new ObjectName("JMX:type=listener,id=" + mRandom.nextLong());
            ObjectInstance lInstance = pConnector.createMBean(pClass,
                    lName,
                    pParameters,
                    pSignatures);
            lName = lInstance.getObjectName();
         }
         catch (InstanceAlreadyExistsException iaee)
         {
            lName = null;
         }
/* A remote exception could cause an endless loop therefore take it out
         catch( RemoteException re ) {
            lName = null;
         }
*/
      }
      mRemoteListener = lName;
      return lName;
   }

   public void addNotificationListener(MEJB pConnector,
                                       NotificationFilter pFilter) throws
           InstanceNotFoundException,
           RemoteException
   {
      pConnector.addNotificationListener(mSender,
              mRemoteListener,
              pFilter,
              null);
   }

   public void removeNotificationListener(MEJB pConnector) throws
           InstanceNotFoundException,
           RemoteException
   {
      try
      {
         pConnector.removeNotificationListener(mSender,
                 mRemoteListener);
      }
      catch (JMException jme)
      {
      }
      try
      {
         pConnector.unregisterMBean(mRemoteListener);
      }
      catch (JMException jme)
      {
      }
   }

   public ObjectName getSenderMBean()
   {
      return mSender;
   }

   public ObjectName getRemoteListenerName()
   {
      return mRemoteListener;
   }

   public boolean equals(Object pTest)
   {
      if (pTest instanceof ClientNotificationListener)
      {
         ClientNotificationListener lListener = (ClientNotificationListener) pTest;
         return
                 mSender.equals(lListener.mSender) &&
                 mClientListener.equals(lListener.mClientListener);
      }
      return false;
   }

}
