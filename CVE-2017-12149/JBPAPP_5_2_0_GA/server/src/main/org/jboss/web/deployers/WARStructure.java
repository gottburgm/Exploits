/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.deployers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.vfs.helpers.SuffixMatchFilter;

/**
 * WARStructure.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 85945 $
 */
public class WARStructure extends AbstractVFSStructureDeployer
{
   /** The default filter which allows jars/jar directories */
   public static final VirtualFileFilter DEFAULT_WEB_INF_LIB_FILTER = new SuffixMatchFilter(".jar", VisitorAttributes.DEFAULT);
   
   /** The web-inf/lib filter */
   private VirtualFileFilter webInfLibFilter = DEFAULT_WEB_INF_LIB_FILTER;

   /** The web-inf/lib/[some-archive]/META-INF filter */
   private VirtualFileFilter webInfLibMetaDataFilter;

   /** Whether to include web-inf in the classpath */
   private boolean includeWebInfInClasspath;

   /**
    * Sets the default relative order 1000.
    *
    */
   public WARStructure()
   {
      setRelativeOrder(1000);
   }

   /**
    * Get the webInfLibFilter.
    * 
    * @return the webInfLibFilter.
    */
   public VirtualFileFilter getWebInfLibFilter()
   {
      return webInfLibFilter;
   }

   /**
    * Set the webInfLibFilter.
    * 
    * @param webInfLibFilter the webInfLibFilter.
    * @throws IllegalArgumentException for a null filter
    */
   public void setWebInfLibFilter(VirtualFileFilter webInfLibFilter)
   {
      if (webInfLibFilter == null)
         throw new IllegalArgumentException("Null filter");
      this.webInfLibFilter = webInfLibFilter;
   }

   /**
    * Get webInfLibMetaDataFilter
    *
    * @return the webInfLibMetaDataFilter
    */
   public VirtualFileFilter getWebInfLibMetaDataFilter()
   {
      return webInfLibMetaDataFilter;
   }

   /**
    * Set the webInfLibMetaDataFilter.
    *
    * @param webInfLibMetaDataFilter the webInfLibFilter.
    */
   public void setWebInfLibMetaDataFilter(VirtualFileFilter webInfLibMetaDataFilter)
   {
      this.webInfLibMetaDataFilter = webInfLibMetaDataFilter;
   }

   /**
    * Should we include web-inf in classpath.
    *
    * @param includeWebInfInClasspath the include web-inf flag
    */
   public void setIncludeWebInfInClasspath(boolean includeWebInfInClasspath)
   {
      this.includeWebInfInClasspath = includeWebInfInClasspath;
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      ContextInfo context = null;
      VirtualFile file = structureContext.getFile();
      try
      {
         boolean trace = log.isTraceEnabled();

         // the WEB-INF
         VirtualFile webinf = null;

         if (isLeaf(file) == false)
         {
            // We require either a WEB-INF or the name ends in .war
            if (file.getName().endsWith(".war") == false)
            {
               try
               {
                  webinf = file.getChild("WEB-INF");
                  if (webinf != null)
                  {
                     if (trace)
                        log.trace("... ok - directory has a WEB-INF subdirectory");
                  }
                  else
                  {
                     if (trace)
                        log.trace("... no - doesn't look like a war and no WEB-INF subdirectory.");
                     return false;
                  }
               }
               catch (IOException e)
               {
                  log.warn("Exception while checking if file is a war: " + e);
                  return false;
               }
            }
            else if (trace)
            {
               log.trace("... ok - name ends in .war.");
            }

            List<String> metaDataLocations = new ArrayList<String>();
            metaDataLocations.add("WEB-INF");

            // Check for WEB-INF/classes
            VirtualFile classes = null;
            try
            {
               // The classpath contains WEB-INF/classes
               classes = file.getChild("WEB-INF/classes");
               
               // Check for a META-INF for metadata
               if (classes != null)
                  metaDataLocations.add("WEB-INF/classes/META-INF");
            }
            catch(IOException e)
            {
               log.warn("Exception while looking for classes, " + file.getPathName() + ", " + e);
            }

            // Check for jars in WEB-INF/lib
            List<VirtualFile> archives = null;
            try
            {
               VirtualFile webinfLib = file.getChild("WEB-INF/lib");
               if (webinfLib != null)
               {
                  archives = webinfLib.getChildren(webInfLibFilter);
                  // Add the jars' META-INF for metadata
                  for (VirtualFile jar : archives)
                  {
                     // either same as plain lib filter, null or accepts the jar
                     if (webInfLibMetaDataFilter == null || webInfLibMetaDataFilter == webInfLibFilter || webInfLibMetaDataFilter.accepts(jar))
                        metaDataLocations.add("WEB-INF/lib/" + jar.getName() + "/META-INF");
                  }
               }
            }
            catch (IOException e)
            {
               log.warn("Exception looking for WEB-INF/lib, " + file.getPathName() + ", " + e);
            }
            
            // Create a context for this war file and all its metadata locations
            context = createContext(structureContext, metaDataLocations.toArray(new String[metaDataLocations.size()]));

            // Add the war manifest classpath entries
            addClassPath(structureContext, file, false, true, context);

            // Add WEB-INF/classes if present
            if (classes != null)
               addClassPath(structureContext, classes, true, false, context);
            else if (trace)
               log.trace("No WEB-INF/classes for: " + file.getPathName());

            // and the top level jars in WEB-INF/lib
            if (archives != null)
            {
               for (VirtualFile jar : archives)
                  addClassPath(structureContext, jar, true, true, context);
            }
            else if (trace)
            {
               log.trace("No WEB-INF/lib for: " + file.getPathName());
            }

            // do we include WEB-INF in classpath
            if (includeWebInfInClasspath && webinf != null)
            {
               addClassPath(structureContext, webinf, true, false, context);
            }

            // There are no subdeployments for wars
            return true;
         }
         else
         {
            if (trace)
               log.trace("... no - not a directory or an archive.");
            return false;
         }
      }
      catch (Exception e)
      {
         // Remove the invalid context
         if (context != null)
            structureContext.removeChild(context);

         throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
      }
   }
}
