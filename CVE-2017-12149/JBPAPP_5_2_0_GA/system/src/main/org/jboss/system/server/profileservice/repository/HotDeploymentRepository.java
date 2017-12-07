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
package org.jboss.system.server.profileservice.repository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.profileservice.spi.DeploymentContentFlags;
import org.jboss.profileservice.spi.ModificationInfo;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ModificationInfo.ModifyStatus;
import org.jboss.virtual.VirtualFile;

/**
 * A deployment repository, with hot deployment capabilities.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 87159 $
 */
public class HotDeploymentRepository extends BasicDeploymentRepository
{

   /** The structure modification checker */
   private StructureModificationChecker checker;
   
   public HotDeploymentRepository(ProfileKey key, URI[] uris)
   {
      super(key, uris);
   }

   /**
    * Get the structure modified checker.
    *
    * @return the checker
    */
   protected StructureModificationChecker getChecker()
   {
      if (checker == null)
         throw new IllegalArgumentException("Checker must be set");

      return checker;
   }

   /**
    * Set the checker.
    *
    * @param checker the checker
    */
   public void setChecker(StructureModificationChecker checker)
   {
      this.checker = checker;
   }

   @Override
   public synchronized Collection<ModificationInfo> getModifiedDeployments() throws Exception
   {
      boolean trace = log.isTraceEnabled();
      Collection<ProfileDeployment> apps = getDeployments();
      List<ModificationInfo> modified = new ArrayList<ModificationInfo>();
      if (trace)
         log.trace("Checking applications for modifications");
      if (trace)
         log.trace("Aquiring content read lock");
      lockRead();
      try
      {
         if (apps != null)
         {
            Iterator<ProfileDeployment> iter = apps.iterator();
            while (iter.hasNext())
            {
               ProfileDeployment ctx = iter.next();
               VirtualFile root = ctx.getRoot();
               String pathName = ctx.getName();
               // Ignore locked or disabled applications
               if (this.hasDeploymentContentFlags(pathName, ignoreFlags))
               {
                  if (trace)
                     log.trace("Ignoring locked application: " + root);
                  continue;
               }
               // Check for removal
               if (root.exists() == false)
               {
                  long rootLastModified = root.getLastModified();
                  ModificationInfo info = new ModificationInfo(ctx, rootLastModified, ModifyStatus.REMOVED);
                  modified.add(info);
                  iter.remove();
                  // Remove last modified cache
                  cleanUpRoot(root);
                  if (trace)
                     log.trace(pathName + " was removed");
               }
               // Check for modification
               else if (hasDeploymentContentFlags(pathName, DeploymentContentFlags.MODIFIED)
                     || getChecker().hasStructureBeenModified(root))
               {
                  long rootLastModified = root.getLastModified();
                  if (trace)
                     log.trace(pathName + " was modified: " + rootLastModified);
                  // Create the modification info
                  ModificationInfo info = new ModificationInfo(ctx, rootLastModified, ModifyStatus.MODIFIED);
                  modified.add(info);
               }
            }
            // Now check for additions
            checkForAdditions(modified);
         }
      }
      finally
      {
         unlockRead();
         if (trace)
            log.trace("Released content read lock");
      }

      if (modified.size() > 0)
         updateLastModfied();
      return modified;
   }

   /**
    * Check for additions.
    *
    * @param modified the modified list
    * @throws Exception for any error
    */
   protected void checkForAdditions(List<ModificationInfo> modified) throws Exception
   {
      for (URI applicationDir : getRepositoryURIs())
      {
         VirtualFile deployDir = getCachedVirtualFile(applicationDir);
         List<VirtualFile> added = new ArrayList<VirtualFile>();
         addedDeployments(added, deployDir);         
         applyAddedDeployments(applicationDir, modified, added);
      }
   }

   /**
    * Apply added deployments.
    *
    * @param applicationDir the app dir
    * @param modified the modifed list
    * @param added the added deployments
    * @throws Exception for any error
    */
   protected void applyAddedDeployments(URI applicationDir, List<ModificationInfo> modified, List<VirtualFile> added) throws Exception
   {
      for (VirtualFile vf : added)
      {
         // Create deployment
         ProfileDeployment ctx = createDeployment(vf);
         // Create modification info
         ModificationInfo info = new ModificationInfo(ctx, vf.getLastModified(), ModifyStatus.ADDED);
         // Add
         modified.add(info);
         internalAddDeployment(ctx.getName(), ctx);
         getChecker().addStructureRoot(vf);
      }
   }

   @Override
   protected void cleanUpRoot(VirtualFile vf)
   {
      getChecker().removeStructureRoot(vf);
   }
}
