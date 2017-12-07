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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public abstract class JMXConnectorServer extends NotificationBroadcasterSupport implements JMXConnectorServerMBean, MBeanRegistration
{
   private MBeanServer mbeanServer = null;
   private List connectionIds = new ArrayList();

   private static final Object lock = new Object();
   private static long sequenceNumber = 0;

   public static final String AUTHENTICATOR = "jmx.remote.authenticator";

   public JMXConnectorServer()
   {
      //TODO: -TME -Implement
   }

   public JMXConnectorServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public MBeanServer getMBeanServer()
   {
      return mbeanServer;
   }

   public void setMBeanServerForwarder(MBeanServerForwarder mbsf)
   {
      //TODO: -TME -Implement
   }

   public String[] getConnectionIds()
   {
      return null; //TODO: -TME -Implement
   }

   public JMXServiceURL getAddress()
   {
      return null;  //TODO: -TME -Implement
   }

   public Map getAttributes()
   {
      return null; //TODO: -TME -Implement
   }

   public JMXConnector toJMXConnector(Map env) throws IOException
   {
      return null; //TODO: -TME -Implement
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return null; //TODO: -TME -Implement
   }

   protected void connectionOpened(String connectionId, String message, Object userData)
   {
      if(connectionId == null)
      {
         // per spec
         throw new NullPointerException("Connection id is null.");
      }

      synchronized(connectionIds)
      {
         connectionIds.add(connectionId);
      }

      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.OPENED,
                                                                             this, connectionId,
                                                                             getNextSequenceNumber(), message, userData);
      sendNotification(notification);

   }

   private static long getNextSequenceNumber()
   {
      synchronized(lock)
      {
         return sequenceNumber++;
      }
   }

   protected void connectionClosed(String connectionId, String message, Object userData)
   {
      //TODO: -TME -Implement
   }

   protected void connectionFailed(String connectionId, String message, Object userData)
   {
      //TODO: -TME -Implement
   }

   public ObjectName preRegister(MBeanServer mbs, ObjectName name)
   {
      if(mbeanServer == null)
      {
         this.mbeanServer = mbs;
      }
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister() throws Exception
   {
   }

   public void postDeregister()
   {
   }
}