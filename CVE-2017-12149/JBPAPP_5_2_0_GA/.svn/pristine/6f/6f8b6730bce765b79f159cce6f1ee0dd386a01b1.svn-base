/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.system.server.profileservice.repository.clustered.local;

import java.io.File;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.jboss.bootstrap.spi.Server;
import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;

/**
 * RepositoryContentPersister that uses JAXB to store the content metadata as XML.
 * 
 * @author Brian Stansberry
 */
public class JAXBRepositoryContentMetadataPersister extends AbstractContentMetadataPersister
{   
   /** The logger */
   private static final Logger log = Logger.getLogger(JAXBRepositoryContentMetadataPersister.class);
   
   /** The attachment suffix. */
   private static final String METADATA_SUFFIX = "-repository-contents.xml";

   /** The default schema resolver. */
   private static final DefaultSchemaResolver resolver = new DefaultSchemaResolver();
   
   static
   {
      resolver.addClassBindingForLocation("repository-content", RepositoryContentMetadata.class);
   }
   
   public JAXBRepositoryContentMetadataPersister(Server server)
   {
      this(server.getConfig().getServerDataDir());
   }
   
   public JAXBRepositoryContentMetadataPersister(URI uri)
   {
      this(new File(uri));
   }
   
   public JAXBRepositoryContentMetadataPersister(File dir)
   {
      super(dir);
   }

   @Override
   public File getMetadataPath(String storeName)
   {
      final String vfsPath = storeName + METADATA_SUFFIX;
      return new File(getContentMetadataDir(), vfsPath);
   }

   @Override
   protected RepositoryContentMetadata loadMetadata(File metadataStore) throws Exception
   {
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      return (RepositoryContentMetadata) unmarshaller.unmarshal(metadataStore.toURL().openStream(), resolver);
   }

   @Override
   protected void saveMetadata(File metadataStore, RepositoryContentMetadata metadata) throws Exception
   {
      if (log.isTraceEnabled())
      {
         log.trace("saveMetadata, metadataStore="+metadataStore+ ", metadata="+metadata);
      }
      JAXBContext ctx = JAXBContext.newInstance(metadata.getClass());
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
      marshaller.marshal(metadata, metadataStore);
   }
   
   

}
