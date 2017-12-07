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

import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The AttachmentMetaData, containing the information for storing and 
 * restoring the persisted Attachment.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 106340 $
 */
public class AttachmentMetaData
{

   /** The attachment name */
   private String name;
   
   /** The attachment class name */
   private String className;

   /** The last modified. */
   private long lastModified;
   
   /** The attachment */
   private transient Object attachment;

   @XmlElement(name = "attachment-name")
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   @XmlElement(name = "attachment-class-name")
   public String getClassName()
   {
      return className;
   }
   
   public void setClassName(String className)
   {
      this.className = className;
   }

   @XmlElement(name = "last-modified")
   public long getLastModified()
   {
      return lastModified;
   }
   
   public void setLastModified(long lastModified)
   {
      this.lastModified = lastModified;
   }
   
   @XmlTransient
   public Object getAttachment()
   {
      return attachment;
   }

   public void setAttachment(Object attachment)
   {
      this.attachment = attachment;
   }
   
   public static String getEncodedAttachmentName(String name) throws Exception
   {
      return URLEncoder.encode(name, "UTF-8");
   }

   public static String getDecodedAttachmentName(String name) throws Exception
   {
      return URLDecoder.decode(name, "UTF-8");
   }
   
}

