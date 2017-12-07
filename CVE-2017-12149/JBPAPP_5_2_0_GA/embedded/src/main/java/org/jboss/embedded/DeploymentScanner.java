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
package org.jboss.embedded;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;

/**
 * Bean so that you can create a DeploymentGroup from the Microcontainer
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class DeploymentScanner extends DeploymentGroup
{
   private static final Logger log = Logger.getLogger(DeploymentScanner.class);

   private List<URL> urls;
   private List<String> resources;
   private List<String> multiple;
   private List<String> dirs;
   private List<String> filesByResource;

   public void setUrls(List<URL> urls) throws DeploymentException
   {
      this.urls = urls;
   }

   public void setResources(List<String> resources) throws DeploymentException, NullPointerException
   {
      this.resources = resources;
   }

   public void setMultipleResources(List<String> resources) throws DeploymentException, IOException
   {
      this.multiple = resources;
   }

   public void setDirectoriesByResource(List<String> resources) throws DeploymentException, IOException
   {
      dirs = resources;
   }

   public void setFilesByResource(List<String> filesByResource)
   {
      this.filesByResource = filesByResource;
   }

   public void start() throws Exception
   {
      try
      {
         if (urls != null) addUrls(urls);
         if (resources != null)
         {
            for (String resource : resources)
            {
               addResource(resource);
            }
         }
         if (multiple != null)
         {
            for (String resource : multiple)
            {
               addMultipleResources(resource);
            }
         }
         if (dirs != null)
         {
            for (String resource : dirs)
            {
               addDirectoryByResource(resource, true);
            }
         }
         if (filesByResource != null)
         {
            for (String resource : filesByResource)
            {
               addFileByResource(resource);
            }
         }
         mainDeployer.process();
      }
      catch (Exception ex)
      {
         log.error("Failed to deploy", ex);
         throw ex;
      }
   }

   public void stop() throws DeploymentException
   {
      undeploy();
   }
}
