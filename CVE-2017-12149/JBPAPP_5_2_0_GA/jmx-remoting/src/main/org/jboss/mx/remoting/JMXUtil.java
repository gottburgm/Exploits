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
package org.jboss.mx.remoting;

import java.util.ArrayList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * JMXUtil is a set of utility functions for dealing with JMX servers and
 * MBeans
 *
 * @author <a href="jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81084 $
 */
public class JMXUtil
{
   private static ObjectName mbeanserver;

   /**
    * return the local MbeanServer by using the system property <tt>jboss.remoting.jmxid</tt>
    * which is set by the NetworkRegistry MBean when registered in the mbean server
    *
    * @return
    */
   public static MBeanServer getMBeanServer()
   {
      ArrayList list = MBeanServerFactory.findMBeanServer(System.getProperty("jboss.remoting.jmxid"));
      if(list.isEmpty())
      {
         return null;
      }
      return (MBeanServer) list.iterator().next();
   }

   /**
    * get the ObjectName of the MBeanServer
    *
    * @return object name of the mbean server delegate
    * @throws Exception
    */
   public static final ObjectName getMBeanServerObjectName()
         throws Exception
   {
      if(mbeanserver == null)
      {
         mbeanserver = new ObjectName("JMImplementation:type=MBeanServerDelegate");
      }
      return mbeanserver;
   }

   /**
    * get the MBeanServerId attribute from the server
    *
    * @param server
    * @return mbean server id
    * @throws Exception
    */
   public static final String getServerId(MBeanServer server)
         throws Exception
   {
      if(server == null)
      {
         throw new NullPointerException("MBeanServer is null");
      }
      if(mbeanserver == null)
      {
         mbeanserver = new ObjectName("JMImplementation:type=MBeanServerDelegate");
      }
      return (String) server.getAttribute(mbeanserver, "MBeanServerId");
   }

}
