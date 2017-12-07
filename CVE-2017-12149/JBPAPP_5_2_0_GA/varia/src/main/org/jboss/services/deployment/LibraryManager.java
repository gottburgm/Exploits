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
package org.jboss.services.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.logging.Logger;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.file.Files;

/**
 * Simple helper singleton to manage library operations
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public final class LibraryManager
{
   // Static --------------------------------------------------------
   
   /** the Logger instance */
   private static final Logger log = Logger.getLogger(LibraryManager.class);
   
   /** the singleton instance */
   private static final LibraryManager INSTANCE = new LibraryManager();
   
   // Private Data --------------------------------------------------
   
   /** The local server library dir */
   File serverLibDir;
   
   /** The local server tmp dir */
   File serverTmpDir;
   
   // Constructors --------------------------------------------------
   
   /**
    * Private CTOR
    * 
    * Requires that ServerConfig object is created
    * and registered to the jboss MBeanServer
    */
   private LibraryManager()
   {
      // discover if there is a local server library dir
      ServerConfig config = ServerConfigLocator.locate();
      URL serverLibURL = config.getServerLibraryURL();
      
      if (serverLibURL != null && serverLibURL.getProtocol().startsWith("file"))
      {
         this.serverLibDir = new File(serverLibURL.getFile());
         this.serverTmpDir = config.getServerTempDir();
         log.debug("Using serverLibDir: " + this.serverLibDir);
         log.debug("Using serverTmpDir: " + this.serverTmpDir);
      }
      else
      {
         log.info("Cannot manage remote serverLibraryURL: " + serverLibURL);
      }
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Gets the singleton
    */
   public static LibraryManager getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Upload a new library to server lib dir. A different
    * filename may be specified, when writing the library.
    * 
    * If the target filename exists, upload is not performed.
    * 
    * @param src the source url to copy
    * @param filename the filename to use when copying (optional)  
    * @return true if upload was succesful, false otherwise
    */
   public boolean uploadLibrary(URL src, String filename)
   {
      if (src != null)
      {
         log.debug("Uploading from URL: " + src);
         if (filename == null || filename.equals(""))
         {
            // get the basename of the url, let File do the dirty work
            filename = (new File(src.getPath())).getName();
            log.debug("Null or empty target filename, using basename: " + filename);
         }
         else
         {
            log.debug("Using target filename: " + filename);
         }
         // make sure target file does not exist
         File target = new File(this.serverLibDir, filename);
         if (!target.exists())
         {
            try
            {
               Files.copy(src, target);
               return true;  // success
            }
            catch (IOException e)
            {
               log.warn("Could not upload target library: " + filename, e);
            }
         }
         else
         {
            log.warn("Target library exists: " + filename);
         }
      }
      else
      {
         log.warn("Null src URL");
      }
      // upload failed
      return false;
   }
}
