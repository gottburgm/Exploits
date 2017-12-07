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
package org.jboss.system.server;

import java.io.File;
import java.net.URL;

import javax.management.ObjectName;

import org.jboss.bootstrap.spi.Server;
import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBean interface.
 */
public interface ServerConfigImplMBean
{
   /** Default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.system:type=ServerConfig");

   /**
    * Get the server Specification-Version
    */
   String getSpecificationVersion();
   
   /**
    * Get the local home directory which the server is running from.
    */
   File getHomeDir();

   /**
    * Get the home URL which the server is running from.
    */
   URL getHomeURL();

   /**
    * Get the home URL which the server is running from.
    */
   URL getLibraryURL();

   /**
    * Get the patch URL for the server.
    */
   URL getPatchURL();

   /**
    * Get the name of the server.
    */
   String getServerName();

   /**
    * Get the base directory for calculating server home directories.
    */
   File getServerBaseDir();

   /**
    * Get the server home directory.
    */
   File getServerHomeDir();

   /**
    * Get the directory where log files will be stored.
    * @return the writable log directory
    */
   java.io.File getServerLogDir();

   /**
    * Get the directory where temporary files will be stored.
    */
   File getServerTempDir();

   /**
    * Get the directory where local data will be stored.
    */
   File getServerDataDir();

   /**
    * Get the base directory for calculating server home URLs.
    */
   URL getServerBaseURL();

   /**
    * Get the server home URL.
    */
   URL getServerHomeURL();

   /**
    * Get the server library URL.
    */
   URL getServerLibraryURL();

   /**
    * Get the server configuration URL.
    */
   URL getServerConfigURL();
  
   /**
    * Get the common base URL.
    */
   URL getCommonBaseURL();
   
   /**
    * Get the common library URL.
    */
   URL getCommonLibraryURL();
   
   /**
    * Get the current value of the flag that indicates if we are
    * using the platform MBeanServer as the main jboss server.
    * Both the {@link ServerConfig.PLATFORM_MBEANSERVER}
    * property must be set, and the jvm must be jdk1.5+
    */
   boolean getPlatformMBeanServer();

   /**
    * Enable or disable exiting the JVM when {@link Server#shutdown}
    * is called. If enabled, then shutdown calls {@link Server#exit}.
    * If disabled, then only the shutdown hook will be run.
    * @param flag True to enable calling exit on shutdown.
    */
   void setExitOnShutdown(boolean flag);

   /**
    * Get the current value of the exit on shutdown flag.
    * @return The current value of the exit on shutdown flag.
    */
   boolean getExitOnShutdown();

   /**
    * Enable or disable blocking when {@link Server#shutdown} is called.
    * If enabled, then shutdown will be called in the current thread.
    * If disabled, then the shutdown hook will be run ansynchronously
    * in a separate thread.
    * @param flag True to enable blocking shutdown.
    */
   void setBlockingShutdown(boolean flag);

   /**
    * Get the current value of the blocking shutdown flag.
    * @return The current value of the blocking shutdown flag.
    */
   boolean getBlockingShutdown();

   /**
    * Set the RequireJBossURLStreamHandlerFactory flag. if false,
    * exceptions when setting the URLStreamHandlerFactory will
    * be logged and ignored.
    * @param flag True to enable blocking shutdown.
    */
   void setRequireJBossURLStreamHandlerFactory(boolean flag);

   /**
    * Get the current value of the requireJBossURLStreamHandlerFactory flag.
    * @return The current value of the requireJBossURLStreamHandlerFactory flag.
    */
   boolean getRequireJBossURLStreamHandlerFactory();
  
   /**
    * Set the filename of the root deployable that will be used to
    * finalize the bootstrap process.
    * @param filename The filename of the root deployable.
    */
   void setRootDeploymentFilename(String filename);

   /**
    * Get the filename of the root deployable that will be used to
    * finalize the bootstrap process.
    * @return The filename of the root deployable.
    */
   String getRootDeploymentFilename();

   /**
    * Get the native dir for unpacking
    * 
    * @return the directory    
    */
  File getServerNativeDir() ;

   /**
    * Get the temporary deployment dir for unpacking
    * 
    * @return the directory
    */
  File getServerTempDeployDir() ;

}
