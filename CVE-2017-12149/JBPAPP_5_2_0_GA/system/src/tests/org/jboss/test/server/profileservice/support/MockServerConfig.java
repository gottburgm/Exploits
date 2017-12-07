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
package org.jboss.test.server.profileservice.support;

import java.io.File;
import java.net.URL;

import org.jboss.bootstrap.spi.ServerConfig;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class MockServerConfig implements ServerConfig
{
   String serverName;
   URL configURL;
   URL commonBaseURL;
   
   public MockServerConfig(String serverName, URL configURL, URL commonBaseURL)
   {
      if(serverName == null)
         throw new IllegalArgumentException("Null serverName");
      if(configURL == null)
         throw new IllegalArgumentException("Null configURL");
      if(commonBaseURL == null)
         throw new IllegalArgumentException("Null commonBaseURL");
      
      this.serverName = serverName;
      this.configURL = configURL;
      this.commonBaseURL = commonBaseURL;
   }
   
   public URL getServerConfigURL()
   {
      return this.configURL;
   }
   
   public URL getCommonBaseURL()
   {
      return this.commonBaseURL;
   }
   
   public String getServerName()
   {
      return this.serverName;
   }
   
   public boolean getBlockingShutdown()
   {
      // FIXME getBlockingShutdown
      return false;
   }

   public URL getBootstrapURL()
   {
      // FIXME getBootstrapURL
      return null;
   }

   public URL getCommonLibraryURL()
   {
      // FIXME getCommonLibraryURL
      return null;
   }

   
   public boolean getExitOnShutdown()
   {
      // FIXME getExitOnShutdown
      return false;
   }

   public File getHomeDir()
   {
      // FIXME getHomeDir
      return null;
   }

   public URL getHomeURL()
   {
      // FIXME getHomeURL
      return null;
   }

   public URL getLibraryURL()
   {
      // FIXME getLibraryURL
      return null;
   }

   public URL getPatchURL()
   {
      // FIXME getPatchURL
      return null;
   }

   public boolean getPlatformMBeanServer()
   {
      // FIXME getPlatformMBeanServer
      return false;
   }

   public boolean getRequireJBossURLStreamHandlerFactory()
   {
      // FIXME getRequireJBossURLStreamHandlerFactory
      return false;
   }

   public String getRootDeploymentFilename()
   {
      // FIXME getRootDeploymentFilename
      return null;
   }

   public File getServerBaseDir()
   {
      // FIXME getServerBaseDir
      return null;
   }

   public URL getServerBaseURL()
   {
      // FIXME getServerBaseURL
      return null;
   }

   public File getServerDataDir()
   {
      // FIXME getServerDataDir
      return null;
   }

   public File getServerHomeDir()
   {
      // FIXME getServerHomeDir
      return null;
   }

   public URL getServerHomeURL()
   {
      // FIXME getServerHomeURL
      return null;
   }

   public URL getServerLibraryURL()
   {
      // FIXME getServerLibraryURL
      return null;
   }

   public File getServerLogDir()
   {
      // FIXME getServerLogDir
      return null;
   }

   public File getServerNativeDir()
   {
      // FIXME getServerNativeDir
      return null;
   }

   public File getServerTempDeployDir()
   {
      // FIXME getServerTempDeployDir
      return null;
   }

   public File getServerTempDir()
   {
      // FIXME getServerTempDir
      return null;
   }

   public String getSpecificationVersion()
   {
      // FIXME getSpecificationVersion
      return null;
   }

   public boolean isInstallLifeThread()
   {
      // FIXME isInstallLifeThread
      return false;
   }

   public void setBlockingShutdown(boolean blockingShutdown)
   {
      // FIXME setBlockingShutdown
      
   }

   public void setExitOnShutdown(boolean flag)
   {
      // FIXME setExitOnShutdown
      
   }

   public void setRequireJBossURLStreamHandlerFactory(boolean requireJBossURLStreamHandlerFactory)
   {
      // FIXME setRequireJBossURLStreamHandlerFactory
      
   }

   public void setRootDeploymentFilename(String filename)
   {
      // FIXME setRootDeploymentFilename
      
   }

}

