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
package org.jboss.system.deployers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.JAXPDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.ManagedObjectImpl;
import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceDeploymentClassPath;
import org.jboss.system.metadata.ServiceDeploymentParser;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.xml.DOMWriter;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SARDeployer.<p>
 *
 * This deployer is responsible for looking for -service.xml
 * and creating the metadata object.<p>
 *
 * The {@link ServiceClassLoaderDeployer} and {@link ServiceDeployer} does the
 * real work of deployment.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 113235 $
 */
public class SARDeployer extends JAXPDeployer<ServiceDeployment> implements ManagedObjectCreator
{
   
   /** The managed object factory. */
   private static final ManagedObjectFactory factory = ManagedObjectFactoryBuilder.create();
   
   /**
    * Create a new SARDeployer.
    *
    * @throws IllegalArgumentException for a null kernel
    */
   public SARDeployer()
   {
      super(ServiceDeployment.class);
      setSuffix("-service.xml");
      // Enable the super class ManagedObjectCreator implementation
      setBuildManagedObject(true);
      setAllowMultipleFiles(true);
   }

   /**
    * @param unit - the deployment unit
    * @param file - the vf for the jboss-service.xml descriptor
    * @param document - the jaxp document for the jboss-service.xml descriptor
    */
   @Override
   protected ServiceDeployment parse(VFSDeploymentUnit unit, VirtualFile file, Document document) throws Exception
   {
      ServiceDeploymentParser parser = new ServiceDeploymentParser(document);
      ServiceDeployment parsed = parser.parse();
      String name = file.toURI().toString();
      parsed.setName(name);

      List<ServiceDeploymentClassPath> classPaths = parsed.getClassPaths();
      if (classPaths != null)
         processXMLClasspath(unit, classPaths);

      List<ServiceMetaData> services = parsed.getServices();
      if (services == null)
      {
         Element config = parsed.getConfig();
         if (config == null)
         {
            log.debug("Service deployment has no services: " + parsed.getName());
            return parsed;
         }
         if (log.isDebugEnabled())
         {
            String docStr = DOMWriter.printNode(config, true);
            int index = docStr.toLowerCase().indexOf("password");
            if (index != -1)
            {
               docStr = maskPasswords(docStr, index);
            }
            log.debug(docStr);
         }
         ServiceMetaDataParser SMDparser = new ServiceMetaDataParser(config);
         services = SMDparser.parse();
         parsed.setServices(services);
      }

      return parsed;
   }

   /**
    * Process the xml classpath
    *
    * @param unit the unit
    * @param classpaths the classpaths
    * @throws Exception for any error
    */
   private void processXMLClasspath(VFSDeploymentUnit unit, List<ServiceDeploymentClassPath> classpaths) throws Exception
   {
      ArrayList<VirtualFile> classpath = new ArrayList<VirtualFile>();

      for (ServiceDeploymentClassPath path : classpaths)
      {
         String codebase = path.getCodeBase();
         String archives = path.getArchives();

         log.debug("Processing classpath: " + unit.getName() + " codebase=" + codebase + " archives=" + archives);
         VirtualFile codebaseFile = unit.getRoot();
         if (".".equals(codebase) == false)
         {
            ServerConfig config = ServerConfigLocator.locate();
            URL codeBaseURL = new URL(config.getServerHomeURL(), codebase);
            codebaseFile = VFS.getRoot(codeBaseURL);
         }

         if (codebaseFile == null)
            throw new DeploymentException("Cannot use classpath without a root: " + unit.getName());

         if (archives == null)
         {
            classpath.add(codebaseFile);
            log.debug("Using codebase as classpath: " + unit.getName());
         }
         else
         {
            SARArchiveFilter filter = new SARArchiveFilter(archives);
            List<VirtualFile> archiveFiles = codebaseFile.getChildren(filter);
            classpath.addAll(archiveFiles);
            
            // JBPAPP-9412 - SARDeployer is not adding MANIFEST Class-Path entries transitively
            for(VirtualFile vf : archiveFiles)
            {
               // this will look the VirtualFile and add to the classpath list any that it finds on the VirtualFile's MANIFEST.MF Class-Path
               log.trace("add manifest classpath locations for : " + vf.getName());
               VFSUtils.addManifestLocations(vf, classpath);           
            }
         }
      }
      
      unit.prependClassPath(classpath);
   }

   private String maskPasswords(String original, int index)
   {
      StringBuilder sb = new StringBuilder(original);
      String modified = null;
      int startPasswdStringIndex = sb.indexOf(">", index);
      if (startPasswdStringIndex != -1)
      {
         // checks if the keyword 'password' was not in a comment
         if (sb.charAt(startPasswdStringIndex - 1) != '-')
         {
            int endPasswdStringIndex = sb.indexOf("<", startPasswdStringIndex);
            if (endPasswdStringIndex != -1) // shouldn't happen, but check anyway
            {
               sb.replace(startPasswdStringIndex + 1, endPasswdStringIndex, "****");
            }
         }
         modified = sb.toString();
         // unlikely event of more than one password
         index = modified.toLowerCase().indexOf("password", startPasswdStringIndex);
         if (index != -1)
            return maskPasswords(modified, index);
         return modified;
      }
      return original;
   }
   
   /**
    * {@inheritDoc}
    * 
    * handle multiple attachments
    * 
    */
   public void build(DeploymentUnit unit, Set<String> attachmentNames, Map<String, ManagedObject> managedObjects)
         throws DeploymentException
   {
      for(final Entry<String, Object> entry : unit.getAttachments().entrySet())
      {
         final String key = entry.getKey();
         if(ServiceDeployment.class.isInstance(entry.getValue()))
         {
            ManagedObject mo = managedObjects.get(key);
            if(mo == null)
            {
               // allowMultipleAttachments
               mo = factory.initManagedObject(entry.getValue(), null, unit.getMetaData(), key, null);
               if (mo != null)
               {
                  if (mo instanceof ManagedObjectImpl)
                  {
                     ManagedObjectImpl.class.cast(mo).setAttachmentName(key);
                  }
                  managedObjects.put(key, mo);
               }
            }
         }
      }
   }
   
}
