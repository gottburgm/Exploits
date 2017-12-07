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
package javax.management.j2ee;

import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * ListenerRegistration defines the methods which clients of the MEJB use to add and remove event listeners.
 *
 * @author thomas.diesler@jboss.org
 */
public interface ListenerRegistration extends Serializable

{
   /**
    * Add a listener to a registered managed object.
    *
    * @param name     The name of the managed object on which the listener should be added.
    * @param listener The listener object which will handle the notifications emitted by the registered managed object.
    * @param filter   The filter object. If filter is null, no filtering will be performed before handling notifications.
    * @param handback The context to be sent to the listener when a notification is emitted.
    * @throws InstanceNotFoundException The managed object name provided does not match any of the registered managed objects.
    * @throws RemoteException           A communication exception occurred during the execution of a remote method call
    */
   public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException, RemoteException;

   /**
    * Remove a listener from a registered managed object.
    *
    * @param name     The name of the managed object on which the listener should be removed.
    * @param listener The listener object which will handle the notifications emitted by the registered managed object.
    *                 This method will remove all the information related to this listener.
    * @throws InstanceNotFoundException The managed object name provided does not match any of the registered managed objects.
    * @throws ListenerNotFoundException The listener is not registered in the managed object.
    * @throws RemoteException           A communication exception occurred during the execution of a remote method call
    */
   public void removeNotificationListener(ObjectName name, NotificationListener listener)
           throws InstanceNotFoundException, ListenerNotFoundException, RemoteException;
}

