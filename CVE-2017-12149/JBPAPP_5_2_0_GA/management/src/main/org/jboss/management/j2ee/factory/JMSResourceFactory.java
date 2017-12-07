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
package org.jboss.management.j2ee.factory;

import org.jboss.management.j2ee.JMSResource;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * A factory for JMSResource managed objects
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class JMSResourceFactory
        implements ManagedObjectFactory
{
   /**
    * Creates a "LocalJMS" JMSResource associated with the ServiceController
    * create notification
    *
    * @param server
    * @param data   A MBeanServerNotification
    * @return the JNDIResource ObjectName
    */
   public ObjectName create(MBeanServer server, Object data)
   {
      ObjectName serviceName = (ObjectName) data;
      ObjectName name = JMSResource.create(server, "LocalJMS", serviceName);
      return name;
   }

   /**
    * Destroys the "LocalJMS" JMSResource
    *
    * @param server
    * @param data   A MBeanServerNotification
    * @return the JNDIResource ObjectName
    */
   public void destroy(MBeanServer server, Object data)
   {
      JMSResource.destroy(server, "LocalJMS");
   }
}
