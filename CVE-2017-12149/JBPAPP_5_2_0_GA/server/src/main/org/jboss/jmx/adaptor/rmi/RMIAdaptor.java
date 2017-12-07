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
package org.jboss.jmx.adaptor.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.ObjectName;
import javax.management.NotificationFilter;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;

/**
 * RMI Interface for the server side Connector which
 * is nearly the same as the MBeanServer Interface but
 * has an additional RemoteException.
 *
 * @version <tt>$Revision: 81030 $</tt>
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author  <A href="mailto:andreas@jboss.org">Andreas Schaefer</A>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface RMIAdaptor
   extends Remote, MBeanServerConnection
{
   /**
    *
    * @param name
    * @param listener
    * @param filter
    * @param handback
    * @throws InstanceNotFoundException
    * @throws RemoteException
    */
   void addNotificationListener(ObjectName name,
                                RMINotificationListener listener,
                                NotificationFilter filter,
                                Object handback)
      throws InstanceNotFoundException,
             RemoteException;

   /**
    *
    * @param name
    * @param listener
    * @throws InstanceNotFoundException
    * @throws ListenerNotFoundException
    * @throws RemoteException
    */
   void removeNotificationListener(ObjectName name, RMINotificationListener listener)
      throws InstanceNotFoundException,
             ListenerNotFoundException,
             RemoteException;
}
