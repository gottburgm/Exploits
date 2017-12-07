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
package org.jboss.embedded.adapters;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.jboss.bootstrap.BaseServerConfig;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.DeploymentScanner;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class ServerConfig implements org.jboss.bootstrap.spi.ServerConfig
{
   protected BaseServerConfig config;
   protected Properties props;

   public void start() throws Exception
   {
      props = new Properties(System.getProperties());

      String homeDir = System.getProperty(HOME_DIR);

      // if HOME_DIR property is null, then try to figure it out
      if (homeDir == null)
      {

         String path = System.getProperty(Bootstrap.BOOTSTRAP_RESOURCE_PATH);
         if (path == null) throw new Exception("${jboss.embedded.bootstrap.resource.path} is null");


         path += Bootstrap.BOOTSTRAP_RESOURCE_FILE;

         URL url = Thread.currentThread().getContextClassLoader().getResource(path);
         if (url.toString().startsWith("file:"))
         {
            initializeByFile(path);
         }
         else
         {
            // we do not know the protocol
            initializeByUnknown();
         }
      }
      else
      {
         props.put(SERVER_HOME_DIR, homeDir);
      }
      config = new BaseServerConfig(props);
      config.initURLs();

      // create tmp and data directories

      if (!config.getServerTempDir().exists())
      {
         config.getServerTempDir().mkdir();
      }
      if (!config.getServerDataDir().exists())
      {
         config.getServerDataDir().mkdir();
      }
   }

   protected void initializeByFile(String path) throws Exception
   {
      URL homeDirUrl = DeploymentScanner.getDirFromResource(Thread.currentThread().getContextClassLoader(), path + "/..");
      File homeDir = new File(homeDirUrl.toURI());
      props.put(HOME_DIR, homeDir.toString());
      props.put(SERVER_HOME_DIR, homeDir.toString());
   }

   /**
    * Creates base JBoss Embedded directory structure under java.io.tmpdir
    *
    * @throws Exception
    */
   protected void initializeByUnknown() throws Exception
   {
      String temp = System.getProperty("java.io.tmpdir");
      File fp = new File(temp, "embedded-jboss");
      if (!fp.exists())
      {
         fp.mkdir();
      }
      props.put(HOME_DIR, fp.toString());
      props.put(SERVER_HOME_DIR, fp.toString());
   }

   public void initURLs()
           throws MalformedURLException
   {
      config.initURLs();
   }

   public File getHomeDir()
   {
      return config.getHomeDir();
   }

   public URL getBootstrapURL()
   {
      return config.getBootstrapURL();
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

   public boolean isInstallLifeThread()
   {
      return config.isInstallLifeThread();
   }

   public String getServerName()
   {
      return config.getServerName();
   }

   public File getServerBaseDir()
   {
      return config.getServerBaseDir();
   }

   public File getServerHomeDir()
   {
      return config.getServerHomeDir();
   }

   public File getServerLogDir()
   {
      return config.getServerLogDir();
   }

   public File getServerTempDir()
   {
      return config.getServerTempDir();
   }

   public File getServerDataDir()
   {
      return config.getServerDataDir();
   }

   public File getServerNativeDir()
   {
      return config.getServerNativeDir();
   }

   public File getServerTempDeployDir()
   {
      return config.getServerTempDeployDir();
   }

   public URL getServerBaseURL()
   {
      return config.getServerBaseURL();
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
   
   public URL getServerConfigURL()
   {
      return config.getServerConfigURL();
   }

   public boolean getPlatformMBeanServer()
   {
      return config.getPlatformMBeanServer();
   }

   public void setExitOnShutdown(boolean flag)
   {
      config.setExitOnShutdown(flag);
   }

   public boolean getExitOnShutdown()
   {
      return config.getExitOnShutdown();
   }

   public void setBlockingShutdown(boolean flag)
   {
      config.setBlockingShutdown(flag);
   }

   public boolean getBlockingShutdown()
   {
      return config.getBlockingShutdown();
   }

   public void setRequireJBossURLStreamHandlerFactory(boolean flag)
   {
      config.setRequireJBossURLStreamHandlerFactory(flag);
   }

   public boolean getRequireJBossURLStreamHandlerFactory()
   {
      return config.getRequireJBossURLStreamHandlerFactory();
   }

   public void setRootDeploymentFilename(String filename)
   {
      config.setRootDeploymentFilename(filename);
   }

   public String getRootDeploymentFilename()
   {
      return config.getRootDeploymentFilename();
   }

   public String getSpecificationVersion()
   {
      return config.getSpecificationVersion();
   }
}