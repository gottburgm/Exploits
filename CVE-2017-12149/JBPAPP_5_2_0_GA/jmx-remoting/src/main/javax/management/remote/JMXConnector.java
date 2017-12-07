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
package javax.management.remote;

import java.io.IOException;
import java.util.Map;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.security.auth.Subject;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public interface JMXConnector
{
   public static final String CREDENTIALS = "jmx.remote.credentials";

   public void connect() throws IOException;

   public void connect(Map env) throws IOException;

   public MBeanServerConnection getMBeanServerConnection() throws IOException;

   public MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) throws IOException;

   public void close() throws IOException;

   public String getConnectionId() throws IOException;

   public void addConnectionNotificationListener(NotificationListener listener,
                                                 NotificationFilter filter,
                                                 Object handback);

   public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException;

   public void removeConnectionNotificationListener(NotificationListener l,
                                                    NotificationFilter f,
                                                    Object handback)
         throws ListenerNotFoundException;
}