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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.logging.Logger;

/**
 * A basic JAXB attachment Serializer.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88906 $
 */
public class JAXBAttachmentSerializer extends AbstractFileAttachmentsSerializer
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(JAXBAttachmentSerializer.class);
   
   /** The attachment suffix. */
   private static final String ATTACHMENT_SUFFIX = ".attachment.xml";
   
   public JAXBAttachmentSerializer(File dir)
   {
      super(dir);
   }

   @SuppressWarnings("unchecked")
   protected <T> T loadAttachment(File attachmentsStore, Class<T> expected) throws Exception
   {
      if(log.isTraceEnabled())
         log.trace("loadAttachment, attachmentsStore=" + attachmentsStore);
      JAXBContext ctx = JAXBContext.newInstance(expected);
      Unmarshaller unmarshaller = ctx.createUnmarshaller();
      InputStream is = new FileInputStream(attachmentsStore);
      try
      {
         return (T) unmarshaller.unmarshal(is);
      }
      finally
      {
         is.close();
      }
   }

   protected void saveAttachment(File attachmentsStore, Object attachment) throws Exception
   {
      if(log.isTraceEnabled())
         log.trace("saveAttachment, attachmentsStore="+attachmentsStore+ ", attachment="+attachment);
      JAXBContext ctx = JAXBContext.newInstance(attachment.getClass());
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
      OutputStream os = new FileOutputStream(attachmentsStore);
      try
      {
         marshaller.marshal(attachment, os);
      }
      finally
      {
         os.close();
      }
   }
   
   @Override
   protected File getAttachmentPath(String baseName)
   {
      final String vfsPath = baseName + ATTACHMENT_SUFFIX;
      return new File(getAttachmentsStoreDir(), vfsPath);
   }
   
}
