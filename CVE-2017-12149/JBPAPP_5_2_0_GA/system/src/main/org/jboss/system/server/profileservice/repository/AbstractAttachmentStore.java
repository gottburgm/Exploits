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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.AttachmentsFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentContext;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedCommon;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.system.server.profileservice.attachments.AttachmentMetaData;
import org.jboss.system.server.profileservice.attachments.AttachmentStore;
import org.jboss.system.server.profileservice.attachments.RepositoryAttachmentMetaData;
import org.jboss.system.server.profileservice.attachments.RepositoryAttachmentMetaDataFactory;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.virtual.VirtualFile;

/**
 * The AbstractAttachmentStore.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 106340 $
 */
public class AbstractAttachmentStore implements AttachmentStore
{

   /** The metadata name */
   public static final String METADATA_NAME = "metadata";
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractAttachmentStore.class);
   
   /** The vfs deployment factory. */
   private static final VFSDeploymentFactory deploymentFactory = VFSDeploymentFactory.getInstance();
   
   /** The attachment store root. */
   private final URI attachmentStoreRoot;
   
   /** The attachment serializer. */
   private AbstractFileAttachmentsSerializer serializer;

   /** The main deployer. */
   private MainDeployerStructure mainDeployer;
   
   /** The persistence factory. */
   private PersistenceFactory persistenceFactory;
  
   protected static URI getURI(File root)
   {
      if(root == null)
         throw new IllegalArgumentException("Null attachment root.");
      if(root.exists() && root.isDirectory() == false)
         throw new IllegalArgumentException("Attachment root is not a directory.");
      return root.toURI();
   }

   public AbstractAttachmentStore(File root)
   {
      this(getURI(root));
   }
   
   public AbstractAttachmentStore(URI uri)
   {
      if(uri == null)
         throw new IllegalArgumentException("Null uri.");
      
      this.attachmentStoreRoot = uri;
   }
   
   public URI getAttachmentStoreRoot()
   {
      return this.attachmentStoreRoot;
   }
  
   public MainDeployerStructure getMainDeployer()
   {
      return mainDeployer;
   }
   
   public void setMainDeployer(MainDeployerStructure mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }
   
   public AbstractFileAttachmentsSerializer getSerializer()
   {
      return this.serializer;
   }
   
   public void setSerializer(AbstractFileAttachmentsSerializer serializer)
   {
      this.serializer = serializer;
   }

   public PersistenceFactory getPersistenceFactory()
   {
      return persistenceFactory;
   }
   
   public void setPersistenceFactory(PersistenceFactory persistenceFactory)
   {
      this.persistenceFactory = persistenceFactory;
   }
   
   public Deployment createDeployment(ProfileDeployment deployment) throws Exception
   {
      Deployment mcDeployment = null;
      if(deployment.getRoot() == null)
         mcDeployment = new AbstractDeployment(deployment.getName());
      else
         mcDeployment = deploymentFactory.createVFSDeployment(deployment.getRoot());
      
      // Add the deployment attachments as PredeterminedManagedObjects
      Map<String, Object> attachments = deployment.getAttachments();
      if(attachments != null && attachments.isEmpty() == false)
      {
         MutableAttachments predetermined = AttachmentsFactory.createMutableAttachments();
         for(String name : attachments.keySet())
            predetermined.addAttachment(name, attachments.get(name));
         mcDeployment.setPredeterminedManagedObjects(predetermined);
      }
      return mcDeployment;
   }
   
   /**
    * Load the attachment metadata for a deployment.
    * 
    * @param relativeDeploymentPath the relative path
    * @return the attachment metadata
    */
   protected RepositoryAttachmentMetaData loadAttachmentMetaData(String relativeDeploymentPath)
   {
      // attachments/simpleName - hash/metadata.xml
      String fixedMetadataPath = getMetaDataPathName(relativeDeploymentPath);   
               
      try
      {  // Try to load the repository attachment metadata
         return getSerializer().loadAttachment(fixedMetadataPath, RepositoryAttachmentMetaData.class);
      }
      catch(Exception e)
      {
         log.error("Failed to load attachment metadata from relative path: "+ relativeDeploymentPath, e);
      }
      return null;
   }
   
   public RepositoryAttachmentMetaData loadMetaData(VirtualFile deploymentRoot) throws Exception
   {
      if(deploymentRoot == null)
         throw new IllegalArgumentException("Null deployment root.");
      
      String deploymentPath = createRelativeDeploymentPath(deploymentRoot);
      return loadAttachmentMetaData(deploymentPath);
   }

   public void removeComponent(String ctx, ManagedComponent comp) throws Exception
   {
      if(ctx == null)
         throw new IllegalArgumentException("null deployment ctx name");
      if(comp == null)
         throw new IllegalArgumentException("null managed component");
      
      saveAttachment(ctx, comp, true);
   }

   public void updateDeployment(String ctx, ManagedComponent comp) throws Exception
   {
      if(ctx == null)
         throw new IllegalArgumentException("null deployment ctx name");
      if(comp == null)
         throw new IllegalArgumentException("null managed component");

      saveAttachment(ctx, comp, false);
   }
   
   public void saveAttachment(String deploymentName, ManagedComponent component, boolean remove) throws Exception
   {
      VFSDeploymentContext ctx = getDeploymentContext(deploymentName);
      if(ctx == null)
         throw new IllegalStateException("Cannot persist attachment, failed to find deployment: " + deploymentName);

      // Get the root
      VirtualFile root = ctx.getRoot();
      String deploymentPath = createRelativeDeploymentPath(root);
      
      // Load previous saved information
      RepositoryAttachmentMetaData repositoryMetaData = loadAttachmentMetaData(deploymentPath);
      if(repositoryMetaData == null)
      {
         repositoryMetaData = RepositoryAttachmentMetaDataFactory.createInstance();
         repositoryMetaData.setDeploymentName(root.getName());
      }

      // Get the parent MO
      ManagedCommon parent = component;
      while(parent.getParent() != null)
         parent = parent.getParent();

      // Get the managed object, as a component can also be a child of a managedObject
      ManagedObject managedObject = component.getDeployment().getManagedObject(parent.getName());
      if(managedObject == null && parent instanceof ManagedObject)
         managedObject = (ManagedObject) parent;
      
      // Get the current attachment
      String attachmentName = managedObject.getAttachmentName(); 
      List<AttachmentMetaData> attachments = repositoryMetaData.getAttachments();
      if(attachments == null)
      {
         attachments = new ArrayList<AttachmentMetaData>();
         repositoryMetaData.setAttachments(attachments);
      }
      // Extract the attachment 
      AttachmentMetaData attachment = null;
      for(AttachmentMetaData a : attachments)
      {
         if(attachmentName.equals(a.getName()))
            attachment = a;
      }      
      
      // Create a new one
      if(attachment == null)
      {
         // Create attachment meta data
         attachment = new AttachmentMetaData();
         // Add attachment meta data
         attachments.add(attachment);
      }

      // Is attachmentName the same as the className ?
      attachment.setName(attachmentName);
      attachment.setClassName(managedObject.getAttachment().getClass().getName());
      
      // Save the attachment
      String attachmentPath = deploymentPath + HashGenerator.createHash(attachment.getName());
      // Create the persistence information
      PersistenceRoot persistenceRoot = getSerializer().loadAttachment(attachmentPath, PersistenceRoot.class);
      //
      persistenceRoot = createPersistedMetaData(persistenceRoot, managedObject, component, remove);
      // Serialize the attachment
      getSerializer().saveAttachment(attachmentPath, persistenceRoot);

      // Update the last modified.
      long lastModified = System.currentTimeMillis();
      attachment.setLastModified(lastModified);
      repositoryMetaData.setLastModified(lastModified);
      
      //  Save the updated repository meta data
      getSerializer().saveAttachment(getMetaDataPathName(deploymentPath), repositoryMetaData);
   }

   /**
   * create the xml meta data for persisting the managed object.
   * 
   * @param parent the parent managed object.
   * @param the managed object
   * @param handler the persistence handler
   * @return the xml metadata.
   */
  protected PersistenceRoot createPersistedMetaData(PersistenceRoot root, ManagedObject managedObject, ManagedComponent component, boolean remove)
  {
     if(root == null)
        root = new PersistenceRoot();
 
     if(remove)
     {
        root = this.persistenceFactory.removeComponent(root, managedObject, component);
     }
     else
     {
        root = this.persistenceFactory.updateComponent(root, managedObject, component);
     }
     if(root.getName() == null)
        root.setName(managedObject.getAttachmentName());
     if(root.getClassName() == null)
        root.setClassName(managedObject.getAttachment().getClass().getName());
     
     return root;
  }

   
   
   public PersistenceRoot loadAttachment(VirtualFile deploymentCtx, AttachmentMetaData attachment) throws Exception
   {
      if(deploymentCtx == null)
         throw new IllegalArgumentException("Null deployment root.");
      if(attachment == null)
         throw new IllegalArgumentException("Null attachment");
      
      String deploymentPath = createRelativeDeploymentPath(deploymentCtx);
      
      // Load
      String attachmentPath = deploymentPath + HashGenerator.createHash(attachment.getName());
      PersistenceRoot root = getSerializer().loadAttachment(attachmentPath, PersistenceRoot.class);
      if(root == null)
      {
         attachmentPath = deploymentPath + attachment.getName();
         root = getSerializer().loadAttachment(attachmentPath, PersistenceRoot.class);
      }
      return root;
   }
   
   /**
    * Get the metadata path, based on a relative path.
    * 
    * @param deploymentPath the relative path to the deployment
    * @return
    */
   protected String getMetaDataPathName(String deploymentPath)
   {
      return deploymentPath.endsWith(File.separator) ? deploymentPath + METADATA_NAME : deploymentPath + File.separator + METADATA_NAME;
   }
   
   /**
    * Create the relative path to the persisted deployment attachment meta data.
    * The string is simpleName + "-" + hash (based on the URI of the deployment)
    * 
    * @param deployment the deployment
    * @return the relative name
    * @throws Exception
    */
   protected String createRelativeDeploymentPath(VirtualFile vf) throws Exception
   {
      if(vf == null)
         throw new IllegalStateException("Null deployment.");
      
      // deployment URI 
      String pathName = vf.toURI().toString();
      String fileName = vf.getName();
      // Generate hash
      String hash = HashGenerator.createHash(pathName);
      // simple name + "-" + hash
      return fileName + "-" + hash + File.separator;
      
   }
   
   /**
    * Get deployment context.
    *
    * @param name the deployment context name
    * @return vfs deployment context or null if doesn't exist or not vfs based
    */
   @SuppressWarnings("deprecation")
   protected VFSDeploymentContext getDeploymentContext(String name)
   {
      if (mainDeployer == null)
         throw new IllegalStateException("Null main deployer.");

      DeploymentContext deploymentContext = mainDeployer.getDeploymentContext(name);
      if (deploymentContext == null || deploymentContext instanceof VFSDeploymentContext == false)
         return null;

      return (VFSDeploymentContext)deploymentContext;
   }

   private static class HashGenerator
   {
      /** The digest. */
      private static MessageDigest digest;
      
      /**
       * Create a hash based on a deployment vfs path name.
       * 
       * @param deployment the deployment
       * @return a hash
       * @throws NoSuchAlgorithmException
       * @throws MalformedURLException
       * @throws URISyntaxException
       */
      public static String createHash(String pathName)
            throws NoSuchAlgorithmException, MalformedURLException, URISyntaxException
      {
         // buffer
         StringBuffer buffer = new StringBuffer();
         // formatter
         Formatter f = new Formatter(buffer);
         // get the bytez
         byte[] bytez = internalCreateHash(pathName);
         for(byte b : bytez)
         {
            // format the byte
            f.format("%02x", b);
         }
         // toString
         return f.toString();
      }
      
      protected static byte[] internalCreateHash(String pathName) throws NoSuchAlgorithmException
      {
         MessageDigest digest = getDigest();
         try
         {
            // update
            digest.update(pathName.getBytes());
            // return
            return digest.digest();
         }
         finally
         {
            // reset
            digest.reset();
         }
      }
      
      public static MessageDigest getDigest() throws NoSuchAlgorithmException
      {
         if(digest == null)
            digest = MessageDigest.getInstance("MD5");

         return digest;
      }
   }
 
}
