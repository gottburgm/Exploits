/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web;

import java.net.URL;
import java.net.UnknownHostException;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;
import org.jboss.util.threadpool.BasicThreadPoolMBean;

/**
 * WebService MBean interface
 * 
 * @version $Revision: 62435 $
 */
public interface WebServiceMBean extends ServiceMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=WebService");

   // Attributes ----------------------------------------------------
   
   /** The specific address the WebService listens on. */
   void setBindAddress(String bindAddress) throws UnknownHostException;
   String getBindAddress();
   
   /** The WebService listening port, 0 for anonymous. */
   void setPort(int port);
   int getPort();
   
   /** The name of the interface to use for the host portion of the RMI codebase URL. */
   void setHost(String hostname);
   String getHost();
   
   /** The WebService listen queue backlog limit. */
   void setBacklog(int backlog);   
   int getBacklog();
   
   /** Whether the server should attempt to download classes using the thread context
    * class loader when a request arrives that does not have a class loader key prefix. */
   void setDownloadServerClasses(boolean flag);
   boolean getDownloadServerClasses();
   
   /** Whether the server will serve resource files. */
   void setDownloadResources(boolean flag);
   boolean getDownloadResources();

   /** The thread pool used for the WebServer class loading. */
   void setThreadPool(BasicThreadPoolMBean threadPool);
   
   /** The RMI codebase URL. */
   String getCodebase();
   
   // Operations ----------------------------------------------------
   
   URL addClassLoader(ClassLoader cl);

   void removeClassLoader(ClassLoader cl);

}
