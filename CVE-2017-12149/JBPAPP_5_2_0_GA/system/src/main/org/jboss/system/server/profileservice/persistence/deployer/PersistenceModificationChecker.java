/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.profileservice.persistence.deployer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class PersistenceModificationChecker
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(PersistenceModificationChecker.class);
   
   /** The filter. */
   private static FileFilter filter = new ExcludeFilter();
   
   public static boolean hasBeenModified(VFSDeploymentUnit unit, long lastModified) throws Exception
   {
      final File file = new File(unit.getRoot().toURL().getPath());
      if(file.exists() == false)
      {
         if(unit.isTopLevel() == false)
         {
            return false;
         }
         throw new IllegalStateException("deployment file does not exist " + file);
      }
      // Archive
      if (file.isFile())
      {
         log.info(file + " " + file.lastModified()  + " // " + lastModified);
         if(file.lastModified() > lastModified)
            return true;
         
         return false;
      }
      // Structure
      final StructureMetaData structureMetaData = unit.getAttachment(StructureMetaData.class);
      if(structureMetaData == null)
         return false;
      
      ContextInfo info = structureMetaData.getContext(unit.getSimpleName());
      if(info == null && unit.isTopLevel())
         info = structureMetaData.getContext("");
      
      if(info == null)
         return false;
      
      return hasBeenModifed(file, info, lastModified);
   }

   protected static boolean hasBeenModifed(final File root, final ContextInfo contextInfo, long lastModified) throws IOException
   {
      List<String> metadataPaths = contextInfo.getMetaDataPath();
      if (metadataPaths != null && metadataPaths.isEmpty() == false)
      {
         for (String metaDataPath : metadataPaths)
         {
            File mdpVF = new File(root, metaDataPath);
            if (mdpVF != null)
            {
               File[] children = mdpVF.listFiles(filter);
               if (children != null && children.length > 0)
               {
                  for (File child : children)
                  {
                     if (child.lastModified() > lastModified)
                     {
                        if (log.isTraceEnabled())
                           log.trace("Metadata location modified: " + child);
                        return true;
                     }
                  }
               }
            }
         }
      }
      return false;
   }
   
   protected static class ExcludeFilter implements FileFilter
   {
      public boolean accept(File pathname)
      {
         return pathname.getName().endsWith(".xml");
      }
   }
   
}

