/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployment;

import java.net.URL;

import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Exclude ear's lib from AnnotationEnvironmentDeployer / OptAnnotationMetaDataDeployer processing.
 *
 * @author Ales.Justin@jboss.org
 */
public class EarLibExcludeDeployer extends AbstractSimpleVFSRealDeployer<JBossAppMetaData>
{
   public EarLibExcludeDeployer()
   {
      super(JBossAppMetaData.class);
      setStage(DeploymentStages.POST_CLASSLOADER);
      setOutputs(ResourceFilter.class.getName() + ".recurse");
      setTopLevelOnly(true);
   }

   public void deploy(VFSDeploymentUnit unit, JBossAppMetaData jBossAppMetaData) throws DeploymentException
   {
      try
      {
         VirtualFile root = unit.getRoot();
         String libDir = jBossAppMetaData.getLibraryDirectory();
         if (libDir == null || libDir.length() == 0) // take 'lib' even on empty
            libDir = "lib";
         VirtualFile lib = root.getChild(libDir);
         if (lib != null)
         {
            ResourceFilter recurseFilter = new UrlExcludeResourceFilter(lib.toURL());
            unit.addAttachment(ResourceFilter.class.getName() + ".recurse", recurseFilter, ResourceFilter.class);
            log.debug("Excluding ear's lib directory: " + lib);
         }
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot exclude ear's lib.", e);
      }
   }

   /**
    * Do exclude based on url.
    *
    * This might not be the best way to check,
    * as we move up the hierarchy, and if there is no VFS caching
    * we might not have parent for the first file instance.
    *
    * But otoh, by default we should have some VFS caching, which makes this OK.
    */
   private class UrlExcludeResourceFilter implements ResourceFilter
   {
      private URL url;

      private UrlExcludeResourceFilter(URL url)
      {
         if (url == null)
            throw new IllegalArgumentException("Null url");
         this.url = url;
      }

      public boolean accepts(ResourceContext rc)
      {
         try
         {
            VirtualFile file = VFS.getRoot(rc.getUrl());
            while (file != null)
            {
               if (url.equals(file.toURL())) // our parent is the lib
                  return false;

               file = file.getParent();
            }
            return true;
         }
         catch (Exception e)
         {
            return false;
         }
      }
   }
}