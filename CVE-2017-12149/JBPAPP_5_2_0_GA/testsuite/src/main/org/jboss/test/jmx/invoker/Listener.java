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

/** An RMI callback implementation used to receive remote JMX notifications
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class Listener implements NotificationListener
{
   private int expectedCount;
   private int count;

   public Listener(int expectedCount)
   {
      this.expectedCount = expectedCount;
   }
   public int getCount()
   {
      return count;
   }
   public void handleNotification(Notification event, Object handback)
   {
      System.out.println("handleNotification, event: "+event+", count="+count);
      count ++;
      synchronized( this )
      {
         if( count == expectedCount )
            notifyAll();
      }
   }
}
