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
package org.jboss.test.jmx.invoker;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.management.Notification;
import javax.management.NotificationListener;
import org.jboss.jmx.adaptor.rmi.RMINotificationListener;
import org.jboss.net.sockets.TimeoutClientSocketFactory;

/** An RMI callback implementation used to receive remote JMX notifications
 * that blocks in the handleNotification callback to validate that poorly
 * behaving clients do not affect the server side service sending the
 * nofication.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class BadListener implements NotificationListener
{
   int count;

   public int getCount()
   {
      return count;
   }
   public void handleNotification(Notification event, Object handback)
   {
      System.out.println("BadListener handleNotification, event: "+event+", count="+count);
      count ++;
      try
      {
         System.out.println("Sleeping 30 seconds...");
         Thread.sleep(30*1000);
      }
      catch(InterruptedException e)
      {
         e.printStackTrace();
      }
      System.out.println("Awake");
   }
}
