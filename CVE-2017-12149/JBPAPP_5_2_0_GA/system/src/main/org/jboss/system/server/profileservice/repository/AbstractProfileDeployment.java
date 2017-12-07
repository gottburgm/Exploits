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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.virtual.VirtualFile;

/**
 * The profile deployment.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88259 $
 */
public class AbstractProfileDeployment implements ProfileDeployment
{

   /** The name. */
   private String name;
   
   /** The vfs root. */
   private VirtualFile root;

   /** The attachments. */
   private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();
   
   /** The transient attachments. */
   private transient Map<String, Object> transientAttachments = new ConcurrentHashMap<String, Object>();
   
   /** The serialVersionUID. */
   private static final long serialVersionUID = -2208890215429044674L;
   
   /**
    * Get the vfs file name safely.
    * @see VFSDeployment
    * 
    * @param root the virtual file
    * @return the name
    */
   static final String safeVirtualFileName(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      try
      {
         return root.toURI().toString();
      }
      catch (Exception e)
      {
         return root.getName();
      }
   }
   
   public AbstractProfileDeployment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      this.name = name;
   }
   
   public AbstractProfileDeployment(VirtualFile root)
   {
      this(safeVirtualFileName(root));
      this.root = root;
   }

   /**
    * Get the deployment name.
    * 
    * @return the name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Get the root of the deployment.
    * 
    * @return the root, or null if it's not a VFS deployment.
    */
   public VirtualFile getRoot()
   {
      return this.root;
   }
   
   /**
    * Get all attachments.
    * 
    * @return the attachments
    */
   public Map<String, Object> getAttachments()
   {
      return Collections.unmodifiableMap(this.attachments);
   }
   
   /**
    * Get attachment.
    * 
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * 
    * @throws IllegalArgumentException for a null name
    */
   public Object getAttachment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null attachment name.");
      
      return this.attachments.get(name);
   }
   
   /**
    * Get attachment.
    * 
    * @param <T> the expected type
    * @param name the name of the attachment
    * @param expected the expected type
    * @return the attachment or null if not present
    * 
    * @throws IllegalArgumentException for a null name
    */
   public <T> T getAttachment(String name, Class<T> expectedType)
   {
      if(expectedType == null)
         throw new IllegalArgumentException("null expected type");
      
      Object attachment = getAttachment(name);
      if(attachment == null)
         return null;
     
      if(expectedType.isInstance(attachment) == false)
         throw new IllegalStateException("attachment " + name + 
            " with value " + attachment + " is not of the expected type " + expectedType);
      
      return expectedType.cast(attachment);
   }

   /**
    * Add attachment.
    *
    * @param name the name of the attachment
    * @param attachment the attachment
    * @return any previous attachment
    * 
    * @throws IllegalArgumentException for a null name or attachment
    */
   public Object addAttachment(String name, Object attachment)
   {
      if(name == null)
         throw new IllegalArgumentException("Null attachment name.");
      if(attachment == null)
         throw new IllegalArgumentException("Null attachment.");
      
      return this.attachments.put(name, attachment);
   }
   
   /**
    * Remove attachment.
    * 
    * @param name the attachment name
    * @return the attachment or null if not present
    * 
    * @throws IllegalArgumentException for a null name
    */
   public Object removeAttachment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null attachment name.");
      
      return this.attachments.remove(name);
   }
   
   /**
    * Get the transient attachment. 
    * 
    * @param name the name of the attachment
    * @return the attachment or null if not present
    * 
    * @throws IllegalArgumentException for a null name
    */
   public Object getTransientAttachment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null attachment name.");
      
      return this.transientAttachments.get(name);
   }
   
   /**
    * Get transient attachment.
    * 
    * @param <T> the expected type
    * @param name the name of the attachment
    * @param expected the expected type
    * @return the attachment or null if not present
    * 
    * @throws IllegalArgumentException for a null name
    */
   public <T> T getTransientAttachment(String name, Class<T> expectedType)
   {
      if(expectedType == null)
         throw new IllegalArgumentException("null expected type");
      
      Object attachment = getTransientAttachment(name);
      if(attachment == null)
         return null;
     
      if(expectedType.isInstance(attachment) == false)
         throw new IllegalStateException("attachment " + name + 
            " with value " + attachment + " is not of the expected type " + expectedType);
      
      return expectedType.cast(attachment);
   }
   
   /**
    * Add transient attachment
    * 
    * @param name the name of the attachment
    * @param attachment the attachment
    * @return any previous attachment
    * 
    * @throws IllegalArgumentException for a null name or attachment 
    */
   public Object addTransientAttachment(String name, Object attachment)
   {
      if(name == null)
         throw new IllegalArgumentException("Null attachment name.");
      if(attachment == null)
         throw new IllegalArgumentException("Null attachment.");
      
      return this.transientAttachments.put(name, attachment);
   }
   
   /**
    * Remove transient attachment.
    * 
    * @param name the attachment name
    * @return the attachment or null if not present
    * 
    * @throws IllegalArgumentException for a null name
    */
   public Object removeTransientAttachment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null attachment name.");
      
      return this.transientAttachments.remove(name);
   }
   
   public String toString()
   {
      return "AbstractProfileDeployment(" + root != null ? root.getName() : name + ")";
   }
   
}
