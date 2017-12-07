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

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.jboss.deployers.spi.attachments.AttachmentsFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.system.server.profileservice.persistence.deployer.ProfileServicePersistenceDeployer;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.system.server.profileservice.repository.AbstractFileAttachmentsSerializer;

/**
 * Basic wrapper for MutableAttachmets. This maintains a list of associated
 * metadata, which is loaded on demand.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89838 $
 * 
 * @deprecated this is not used anymore {@link ProfileServicePersistenceDeployer}
 */
@Deprecated
public class LazyPredeterminedManagedObjects implements MutableAttachments
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The attachments. */
   private final MutableAttachments delegate;
   
   /** The attachmentsSerializer. */
   private final AbstractFileAttachmentsSerializer serializer;
   
   /** The relative path. */
   private final String relativePath;
   
   /** The available attachments. */
   private final Collection<String> attachments;
   
   public LazyPredeterminedManagedObjects(AbstractFileAttachmentsSerializer serializer, String relativePath, Collection<String> attachments)
   {
      if(serializer == null)
         throw new IllegalArgumentException("serializer may not be null;");
      if(relativePath == null)
         throw new IllegalArgumentException("relativePath may not be null;");
      if(attachments == null)
         throw new IllegalArgumentException("serializer may not be null;");
      
      // Create a mutable attachment
      this.delegate = AttachmentsFactory.createMutableAttachments();
      // The serializer
      this.serializer = serializer;
      // Fix the path, if needed
      this.relativePath = relativePath.endsWith(File.separator) ? relativePath : relativePath + File.separator;  
      // The attachments, which can be loaded
      this.attachments = attachments;
      
   }

   public Object addAttachment(String name, Object attachment)
   {
      return delegate.addAttachment(name, attachment);
   }

   public <T> T addAttachment(Class<T> type, T attachment)
   {
      return delegate.addAttachment(type, attachment);
   }

   public <T> T addAttachment(String name, T attachment, Class<T> expectedType)
   {
      return delegate.addAttachment(name, attachment, expectedType);
   }

   public void clear()
   {
      delegate.clear();
   }

   public void clearChangeCount()
   {
      delegate.clearChangeCount();
   }

   public int getChangeCount()
   {
      return delegate.getChangeCount();
   }

   public Object removeAttachment(String name)
   {
      return delegate.removeAttachment(name);
   }

   public <T> T removeAttachment(Class<T> type)
   {
      return delegate.removeAttachment(type);
   }

   public <T> T removeAttachment(String name, Class<T> expectedType)
   {
      return delegate.removeAttachment(name, expectedType);
   }

   public void setAttachments(Map<String, Object> map)
   {
      delegate.setAttachments(map);
   }

   public Object getAttachment(String name)
   {
      if(ishandleAttachment(name))
         return loadAttachment(name);
      return delegate.getAttachment(name); 
   }

   public <T> T getAttachment(Class<T> type)
   {
      return delegate.getAttachment(type);  
   }

   public <T> T getAttachment(String name, Class<T> expectedType)
   {
      return delegate.getAttachment(name, expectedType);
   }

   public Map<String, Object> getAttachments()
   {
      return delegate.getAttachments();
   }

   public boolean hasAttachments()
   {
      return delegate.hasAttachments();
   }

   public boolean isAttachmentPresent(String name)
   {
      if(ishandleAttachment(name)) return true;
      return delegate.isAttachmentPresent(name);
   }

   public boolean isAttachmentPresent(Class<?> type)
   {
      if(ishandleAttachment(type.getName())) return true;
      return delegate.isAttachmentPresent(type);
   }

   public boolean isAttachmentPresent(String name, Class<?> expectedType)
   {
      if(ishandleAttachment(name)) return true;
      return delegate.isAttachmentPresent(name, expectedType);
   }
   
   private boolean ishandleAttachment(String name)
   {
      if(name == null)
         return false;
      if(! name.startsWith(ProfileServicePersistenceDeployer.PERSISTED_ATTACHMENT_PREFIX))
         return false;
      
      return attachments.contains(name.substring(ProfileServicePersistenceDeployer.PERSISTED_ATTACHMENT_PREFIX.length()));
   }

   private PersistenceRoot loadAttachment(String name)
   {
      if(! ishandleAttachment(name))
         return null;
    
      String attachmentClassName = name.substring(ProfileServicePersistenceDeployer.PERSISTED_ATTACHMENT_PREFIX.length());
      try
      {
         // deploy/deployment/child/attachmentName
         String attachmentName = relativePath + attachmentClassName;
         // Load attachment
         return serializer.loadAttachment(attachmentName, PersistenceRoot.class);
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
