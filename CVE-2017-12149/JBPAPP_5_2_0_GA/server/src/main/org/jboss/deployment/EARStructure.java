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
package org.jboss.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.structure.spi.helpers.RelativeDeploymentContextComparator;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ear.jboss.ServiceModuleMetaData;
import org.jboss.metadata.ear.jboss.JBossAppMetaData.ModuleOrder;
import org.jboss.metadata.ear.spec.AbstractModule;
import org.jboss.metadata.ear.spec.ConnectorModuleMetaData;
import org.jboss.metadata.ear.spec.EarMetaData;
import org.jboss.metadata.ear.spec.EjbModuleMetaData;
import org.jboss.metadata.ear.spec.JavaModuleMetaData;
import org.jboss.metadata.ear.spec.ModuleMetaData;
import org.jboss.metadata.ear.spec.ModulesMetaData;
import org.jboss.metadata.ear.spec.WebModuleMetaData;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.plugins.vfs.helpers.SuffixMatchFilter;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * Structure deployer for EARs.
 *
 * @author Bill Burke
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 94199 $
 */
public class EARStructure extends AbstractVFSStructureDeployer
{
   /**
    * The default ear/lib filter
    */
   public static final VirtualFileFilter DEFAULT_EAR_LIB_FILTER = new SuffixMatchFilter(".jar");

   /**
    * The ear/lib filter
    */
   private VirtualFileFilter earLibFilter = DEFAULT_EAR_LIB_FILTER;
   /** The schema resolver used to determine which schema to use for application.xml/jboss-app.xml */
   private SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
   /** unmarshaller factory */
   UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
   /** The root in classpath flag */
   private boolean includeEarRootInClasspath = true;
   /** The ear child context comparator */
   private String comparatorClassName;
   /** whether to validate deployment descriptors */
   private boolean useValidation = true;

   /**
    * Set the relative order to 1000 by default
    */
   public EARStructure()
   {
      setRelativeOrder(1000);
   }

   /**
    * Get the earLibFilter.
    *
    * @return the earLibFilter.
    */
   public VirtualFileFilter getEarLibFilter()
   {
      return earLibFilter;
   }

   /**
    * Set the earLibFilter.
    *
    * @param earLibFilter the earLibFilter.
    * @throws IllegalArgumentException for a null filter
    */
   public void setEarLibFilter(VirtualFileFilter earLibFilter)
   {
      if (earLibFilter == null)
         throw new IllegalArgumentException("Null filter");
      this.earLibFilter = earLibFilter;
   }

   /**
    * Get the schema resolver
    * @return the schema resolver
    */
   public SchemaBindingResolver getResolver()
   {
      return resolver;
   }
   /**
    * Set the schema resolver
    * @param resolver the schema resolver
    */
   public void setResolver(SchemaBindingResolver resolver)
   {
      this.resolver = resolver;
   }

   public boolean determineStructure(StructureContext structureContext) throws DeploymentException
   {
      ContextInfo context;
      boolean valid;
      boolean trace = log.isTraceEnabled();
      VirtualFile file = structureContext.getFile();
      try
      {
         if (file.isLeaf() == true || file.getName().endsWith(".ear") == false)
            return false;

         context = createContext(structureContext, "META-INF");
         context.setComparatorClassName(comparatorClassName);

         VirtualFile applicationXml = getMetaDataFile(file, "META-INF/application.xml");
         VirtualFile jbossAppXml = getMetaDataFile(file, "META-INF/jboss-app.xml");
         VirtualFile lib;

         boolean scan = true;

         Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
         unmarshaller.setValidation(useValidation);
         EarMetaData specMetaData = null;
         JBossAppMetaData appMetaData = null;
         if (applicationXml != null)
         {
            InputStream in = applicationXml.openStream();
            try
            {
               specMetaData = (EarMetaData) unmarshaller.unmarshal(in, resolver);
            }
            finally
            {
               in.close();
            }
            scan = false;
         }
         if (jbossAppXml != null)
         {
            InputStream in = jbossAppXml.openStream();
            try
            {
               appMetaData = (JBossAppMetaData) unmarshaller.unmarshal(in, resolver);
            }
            finally
            {
               in.close();
            }
         }
         // Need a metadata instance and there will not be one if there are no descriptors
         if (appMetaData == null)
         {
            appMetaData = new JBossAppMetaData();
         }
         // Create the merged view
         appMetaData.merge(appMetaData, specMetaData);

         String libDir = appMetaData.getLibraryDirectory();
         if (libDir == null || libDir.length() > 0)
         {
            if (libDir == null)
               libDir = "lib";

            // Add the ear lib contents to the classpath
            if(trace)
               log.trace("Checking for ear lib directory: "+libDir);
            try
            {
               lib = file.getChild(libDir);
               if (lib != null)
               {
                  if(trace)
                     log.trace("Found ear lib directory: "+lib);
                  List<VirtualFile> archives = lib.getChildren(earLibFilter);
                  for (VirtualFile archive : archives)
                  {
                     addClassPath(structureContext, archive, true, true, context);
                     try
                     {
                        // add any jars with persistence.xml as a deployment
                        if (archive.getChild("META-INF/persistence.xml") != null)
                        {
                           log.trace(archive.getName() + " in ear lib directory has persistence units");
                           if (structureContext.determineChildStructure(archive) == false)
                           {
                              throw new RuntimeException(archive.getName()
                                    + " in lib directory has persistence.xml but is not a recognized deployment, .ear: "
                                    + file.getName());
                           }
                        }
                        else if (trace)
                           log.trace(archive.getPathName() + " does not contain META-INF/persistence.xml");

                     }
                     catch(IOException e)
                     {
                        // TODO - should we throw this fwd?
                        log.warn("Exception searching for META-INF/persistence.xml in " + archive.getPathName() + ", " + e);
                     }
                  }
               }
               else if (trace)
                  log.trace("No lib directory in ear archive.");
            }
            catch (IOException e)
            {
               // TODO - should we throw this fwd?
               log.warn("Exception while searching for lib dir: " + e);
            }
         }
         else if (trace)
         {
            log.trace("Ignoring library directory, got empty library-directory element.");
         }

         // Add the ear manifest locations?
         addClassPath(structureContext, file, includeEarRootInClasspath, true, context);

         // TODO: need to scan for annotationss
         if( scan )
         {
            scanEar(file, appMetaData);
         }

         // Create subdeployments for the ear modules
         ModulesMetaData modules = appMetaData.getModules();
         if(modules != null)
         {
            for (ModuleMetaData mod : modules)
            {
               String fileName = mod.getFileName();
               if (fileName != null && (fileName = fileName.trim()).length() > 0)
               {
                  if (log.isTraceEnabled())
                     log.trace("Checking application.xml module: " + fileName);

                  try
                  {
                     VirtualFile module = file.getChild(fileName);
                     if (module == null)
                     {
                        throw new RuntimeException(fileName + " module listed in application.xml does not exist within .ear " + file.toURI());
                     }
                     // Ask the deployers to analyze this
                     if(structureContext.determineChildStructure(module) == false)
                     {
                        throw new RuntimeException(fileName
                              + " module listed in application.xml is not a recognized deployment, .ear: "
                              + file.getName());
                     }

                  }
                  catch (IOException e)
                  {
                     throw new RuntimeException("Exception looking for " + fileName + " module listed in application.xml, .ear " + file.getName(), e);
                  }
               }
            }

            if (appMetaData.getModuleOrderEnum() == ModuleOrder.STRICT)
            {
               context.setComparatorClassName(RelativeDeploymentContextComparator.class.getName());
               int i = 0;
               for (ContextInfo ctx : structureContext.getMetaData().getContexts())
               {
                  ctx.setRelativeOrder(i++);
               }
            }
         }

         valid = true;
      }
      catch(Exception e)
      {
         throw new RuntimeException("Error determining structure: " + file.getName(), e);
      }

      return valid;
   }

   /**
   For an ear without an application.xml, determine modules via:
   a. All ear modules with an extension of .war are considered web modules. The
    context root of the web module is the name of the file relative to the root
    of the application package, with the .war extension removed.
   b. All ear modules with extension of .rar are considered resource adapters.
   c. A directory named lib is considered to be the library directory, as
    described in Section�EE.8.2.1, �Bundled Libraries.�
   d. For all ear modules with a filename extension of .jar, but not in the lib
    directory, do the following:
   i. If the JAR file contains a META-INF/MANIFEST.MF file with a Main-Class
    attribute, or contains a META-INF/application-client.xml file, consider the
    jar file to be an application client module.
   ii. If the JAR file contains a META-INF/ejb-jar.xml file, or contains any
   class with an EJB component annotation (Stateless, etc.), consider the JAR
    file to be an EJB module.
   iii. All other JAR files are ignored unless referenced by a JAR file
    discovered above using one of the JAR file reference mechanisms such as the
    Class-Path header in a manifest file.
    * TODO: rewrite using vfs
    */
   private void scanEar(VirtualFile root, JBossAppMetaData appMetaData) throws IOException
   {
      List<VirtualFile> archives = root.getChildren();
      if (archives != null)
      {
         String earPath = root.getPathName();
         ModulesMetaData modules = appMetaData.getModules();
         if (modules == null)
         {
            modules = new ModulesMetaData();
            appMetaData.setModules(modules);
         }
         for (VirtualFile vfArchive : archives)
         {
            String filename = earRelativePath(earPath, vfArchive.getPathName());
            // Check if the module already exists, i.e. it is declared in jboss-app.xml
            ModuleMetaData moduleMetaData = appMetaData.getModule(filename);
            int type = typeFromSuffix(filename, vfArchive);
            if (type >= 0 && moduleMetaData == null)
            {
               moduleMetaData = new ModuleMetaData();
               AbstractModule module = null;
               switch(type)
               {
                  case J2eeModuleMetaData.EJB:
                     module = new EjbModuleMetaData();
                     break;
                  case J2eeModuleMetaData.CLIENT:
                     module = new JavaModuleMetaData();
                     break;
                  case J2eeModuleMetaData.CONNECTOR:
                     module = new ConnectorModuleMetaData();
                     break;
                  case J2eeModuleMetaData.SERVICE:
                  case J2eeModuleMetaData.HAR:
                     module = new ServiceModuleMetaData();
                     break;
                  case J2eeModuleMetaData.WEB:
                     module = new WebModuleMetaData();
                     break;
               }
               module.setFileName(filename);
               moduleMetaData.setValue(module);
               modules.add(moduleMetaData);
            }
         }
      }
   }

   private int typeFromSuffix(String path, VirtualFile archive)
      throws IOException
   {
      int type = -1;
      if( path.endsWith(".war") )
         type = J2eeModuleMetaData.WEB;
      else if( path.endsWith(".rar") )
         type = J2eeModuleMetaData.CONNECTOR;
      else if( path.endsWith(".har") )
         type = J2eeModuleMetaData.HAR;
      else if( path.endsWith(".sar") )
         type = J2eeModuleMetaData.SERVICE;
      else if( path.endsWith(".jar") )
      {
         // Look for a META-INF/application-client.xml
         VirtualFile mfFile = getMetaDataFile(archive, "META-INF/MANIFEST.MF");
         VirtualFile clientXml = getMetaDataFile(archive, "META-INF/application-client.xml");
         VirtualFile ejbXml = getMetaDataFile(archive, "META-INF/ejb-jar.xml");
         VirtualFile jbossXml = getMetaDataFile(archive, "META-INF/jboss.xml");

         if( clientXml != null )
         {
            type = J2eeModuleMetaData.CLIENT;
         }
         else if( mfFile != null )
         {
            Manifest mf = VFSUtils.readManifest(mfFile);
            Attributes attrs = mf.getMainAttributes();
            if( attrs.containsKey(Attributes.Name.MAIN_CLASS) )
            {
               type = J2eeModuleMetaData.CLIENT;
            }
            else
            {
               // TODO: scan for annotations. Assume EJB for now
               type = J2eeModuleMetaData.EJB;
            }
         }
         else if( ejbXml != null || jbossXml != null )
         {
            type = J2eeModuleMetaData.EJB;
         }
         else
         {
            // TODO: scan for annotations. Assume EJB for now
            type = J2eeModuleMetaData.EJB;
         }
      }

      return type;
   }

   private String earRelativePath(String earPath, String pathName)
   {
      StringBuilder tmp = new StringBuilder(pathName);
      tmp.delete(0, earPath.length());
      return tmp.toString();
   }

   private VirtualFile getMetaDataFile(VirtualFile file, String path)
   {
      VirtualFile metaFile = null;
      try
      {
         metaFile = file.getChild(path);
      }
      catch(IOException ignored)
      {
      }
      return metaFile;
   }

   public void setIncludeEarRootInClasspath(boolean includeEarRootInClasspath)
   {
      this.includeEarRootInClasspath = includeEarRootInClasspath;
   }

   public void setComparatorClassName(String comparatorClassName)
   {
      this.comparatorClassName = comparatorClassName;
   }

   public void setUseValidation(boolean validateXml)
   {
      this.useValidation = validateXml;
   }

   public boolean isUseValidation()
   {
      return useValidation;
   }
}
