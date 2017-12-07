/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.system.deployers.vfs;

import java.io.IOException;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.ModificationType;
import org.jboss.deployers.vfs.plugins.structure.modify.AbstractModificationTypeMatcher;
import org.jboss.virtual.VirtualFile;

/**
 * Forces all archive deployments (jar, war, ear, etc) to be copied to the temp directory.
 * <p>
 * There are 2 very important reasons why this needs to be done:
 *
 * <ol>
 * <li> Windows File Locking - An open file can not be deleted </li>
 * <li> Zlib - causes jvm segfaults when the contents of the jar change unexpectedly
 *      (during a user copy) </li>
 * </ol>
 *
 * @author Jason T. Greene
 */
public class ArchiveForceTempTypeMatcher extends AbstractModificationTypeMatcher
{
   public ArchiveForceTempTypeMatcher()
   {
      setModificationType(ModificationType.TEMP);
      setTopLevelOnly(true);
   }

   protected boolean isModificationDetermined(VirtualFile file, ContextInfo contextInfo)
   {
      try
      {
         return file.isArchive();
      }
      catch (IOException e)
      {
         // Will never happen
         return false;
      }
   }

}
