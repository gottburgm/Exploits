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
package org.jboss.system.server.profileservice.attachments;

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.managed.api.ManagedDeployment;

/**
 * Common RepositoryAttachmentMetaData operations.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class RepositoryAttachmentMetaDataFactory
{

   public static RepositoryAttachmentMetaData createInstance()
   {
      return new RepositoryAttachmentMetaData();
   }
   
   public static RepositoryAttachmentMetaData createInstance(ManagedDeployment md)
   {
      if(md == null)
         throw new IllegalArgumentException("managed deployment may not be null.");
      
      RepositoryAttachmentMetaData metaData = createInstance();
      metaData.setDeploymentName(md.getSimpleName());
      return metaData;
   }
   
   public static RepositoryAttachmentMetaData createNewParent(ManagedDeployment md, RepositoryAttachmentMetaData child)
   {
      if(md == null)
         throw new IllegalArgumentException("managed deployment may not be null.");
      
      RepositoryAttachmentMetaData metaData = createInstance(md);
      addChild(metaData, child);
      return metaData;
   }
   
   public static void addChild(RepositoryAttachmentMetaData parent, RepositoryAttachmentMetaData child)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null.");
      
      if(parent.getChildren() == null)
         parent.setChildren(new ArrayList<RepositoryAttachmentMetaData>());
      
      parent.getChildren().add(child);
   }
   
   public static void addAttachment(RepositoryAttachmentMetaData metaData, AttachmentMetaData attachment)
   {
      if(metaData == null)
         throw new IllegalArgumentException("meta data may not be null.");
      
      if(metaData.getAttachments() == null)
         metaData.setAttachments(new ArrayList<AttachmentMetaData>());
      
      metaData.getAttachments().add(attachment);
   }
   
   public static void applyStructureContext(RepositoryAttachmentMetaData metaData, ContextInfo info)
   {
      if(metaData == null)
         throw new IllegalArgumentException("meta data may not be null.");
      if(info == null)
         throw new IllegalArgumentException("context info may not be null.");
      
      DeploymentStructureMetaData structure = new DeploymentStructureMetaData(); 
      
      // meta data paths
      structure.setMetaDataPaths(info.getMetaDataPath());
      // classpath
      structure.setClassPaths(getDeploymentClassPathMetaData(info));
      // comparator
      structure.setComparatorClass(info.getComparatorClassName());
      // relativeOrder
      structure.setRelatativeOrder(info.getRelativeOrder());
      
      metaData.setDeploymentStructure(structure);
   }
   
   public static AttachmentMetaData findAttachment(String name, List<AttachmentMetaData> attachments)
   {
      if(name == null)
         return null;
      if(attachments == null)
         return null;
      
      for(AttachmentMetaData attachment : attachments)
      {
         if(name.equals(attachment.getName()))
            return attachment;
      }
      return null;
   }
   
   protected static List<DeploymentClassPathMetaData> getDeploymentClassPathMetaData(ContextInfo info)
   {
      if(info == null)
         throw new IllegalArgumentException("context info may not be null.");
      
      if(info.getClassPath() == null || info.getClassPath().isEmpty())
         return null;
      
      List<DeploymentClassPathMetaData> classPath = new ArrayList<DeploymentClassPathMetaData>();
      for(ClassPathEntry entry : info.getClassPath())
         classPath.add(new DeploymentClassPathMetaData(entry.getPath(), entry.getSuffixes()));
      
      return classPath;
   }
}

