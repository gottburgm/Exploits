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

import org.jboss.bootstrap.spi.ServerConfig;

/**
 * An mbean wrapper for the BaseServerConfig that exposes the config as the
 * legacy ServerConfigImplMBean.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81033 $
 */
public class ServerConfigImpl implements ServerConfigImplMBean
{
   private ServerConfig config;

   /**
    * Construct a ServerConfigImpl with the ServerConfig pojo which will be used
    * as the delegate for the ServerConfigImplMBean ops.
    *
    * @param config - the ServerConfig pojo to expose as a ServerConfigImplMBean
    */
   public ServerConfigImpl(ServerConfig config)
   {
      this.config = config;
   }

   public ServerConfig getConfig()
   {
      return config;
   }

   public boolean equals(Object obj)
   {
      return config.equals(obj);
   }

   public boolean getBlockingShutdown()
   {
      return config.getBlockingShutdown();
   }

   public boolean getExitOnShutdown()
   {
      return config.getExitOnShutdown();
   }

   public File getHomeDir()
   {
      return config.getHomeDir();
   }

   public URL getHomeURL()
   {
      return config.getHomeURL();
   }

   public URL getLibraryURL()
   {
      return config.getLibraryURL();
   }

   public URL getPatchURL()
   {
      return config.getPatchURL();
   }

   public boolean getPlatformMBeanServer()
   {
      return config.getPlatformMBeanServer();
   }

   public boolean getRequireJBossURLStreamHandlerFactory()
   {
      return config.getRequireJBossURLStreamHandlerFactory();
   }

   public String getRootDeploymentFilename()
   {
      return config.getRootDeploymentFilename();
   }

   public File getServerBaseDir()
   {
      return config.getServerBaseDir();
   }

   public URL getServerBaseURL()
   {
      return config.getServerBaseURL();
   }

   public URL getServerConfigURL()
   {
      return config.getServerConfigURL();
   }

   public File getServerDataDir()
   {
      return config.getServerDataDir();
   }

   public File getServerHomeDir()
   {
      return config.getServerHomeDir();
   }

   public URL getServerHomeURL()
   {
      return config.getServerHomeURL();
   }

   public URL getServerLibraryURL()
   {
      return config.getServerLibraryURL();
   }

   public URL getCommonBaseURL()
   {
      return config.getCommonBaseURL();
   }
   
   public URL getCommonLibraryURL()
   {
      return config.getCommonLibraryURL();
   }
   
   public File getServerLogDir()
   {
      return config.getServerLogDir();
   }

   public String getServerName()
   {
      return config.getServerName();
   }

   public File getServerNativeDir()
   {
      return config.getServerNativeDir();
   }

   public File getServerTempDeployDir()
   {
      return config.getServerTempDeployDir();
   }

   public File getServerTempDir()
   {
      return config.getServerTempDir();
   }

   public int hashCode()
   {
      return config.hashCode();
   }

   public void setBlockingShutdown(boolean flag)
   {
      config.setBlockingShutdown(flag);
   }

   public void setExitOnShutdown(boolean flag)
   {
      config.setExitOnShutdown(flag);
   }

   public void setRequireJBossURLStreamHandlerFactory(boolean flag)
   {
      config.setRequireJBossURLStreamHandlerFactory(flag);
   }

   public void setRootDeploymentFilename(String filename)
   {
      config.setRootDeploymentFilename(filename);
   }

   public String getSpecificationVersion()
   {
      return config.getSpecificationVersion();
   }
}
