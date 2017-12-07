/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployers;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.sax.SAXSource;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.repository.JCAMetaDataRepository;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.virtual.VirtualFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A ManagedConnectionFactoryParserDeployer.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @author adrian@jboss.org
 * @version $Revision: 105576 $
 * 
 * TODO Note, this is currently a total hack until we can rework the JAXBDeployer
 */
public class ManagedConnectionFactoryParserDeployer extends AbstractVFSParsingDeployer<ManagedConnectionFactoryDeploymentGroup>
{   
   /** The context */
   private JAXBContext context;   
   
   /** The repository */
   private JCAMetaDataRepository repository;

   /**
    * Create a new ManagedConnectionFactoryParserDeployer.
    */
   public ManagedConnectionFactoryParserDeployer()
   {
      super(ManagedConnectionFactoryDeploymentGroup.class);
      setIncludeDeploymentFile(true);
      setBuildManagedObject(true);

   }

   /**
    * Get the repository.
    * 
    * @return the repository.
    */
   public JCAMetaDataRepository getRepository()
   {
      return repository;
   }

   /**
    * Set the repository.
    * 
    * @param repository The repository to set.
    */
   public void setRepository(JCAMetaDataRepository repository)
   {
      this.repository = repository;
   }
   
   public void create() throws Exception
   {
      Class<?>[] classes = {super.getOutput()};
      context = JAXBContext.newInstance(classes);      
   }
   
   @Override
   protected ManagedConnectionFactoryDeploymentGroup parse(VFSDeploymentUnit unit, VirtualFile file, ManagedConnectionFactoryDeploymentGroup root) throws Exception
   {
      //TODO do we need to this every time?
      Unmarshaller um = context.createUnmarshaller();
      um.setEventHandler(new ValidationEventHandler()
      {

         public boolean handleEvent(ValidationEvent event)
         {
            int severity = event.getSeverity();
            return (severity != ValidationEvent.FATAL_ERROR);
         }

      });
      
      InputStream is = file.openStream();

      try
      {
         InputSource input = new InputSource(is);
         input.setSystemId(file.toURI().toString());
         XMLReader reader = XMLReaderFactory.createXMLReader();
         reader.setEntityResolver(new JBossEntityResolver());
         SAXSource source = new SAXSource(reader, input);
         JAXBElement<ManagedConnectionFactoryDeploymentGroup> elem = um.unmarshal(source, ManagedConnectionFactoryDeploymentGroup.class);
         ManagedConnectionFactoryDeploymentGroup deployment = elem.getValue();
         repository.addManagedConnectionFactoryDeploymentGroup(deployment);
         return deployment;
      }      
      finally
      {
         if (is != null)
            is.close();            
      }
   }
   
   @Override
   protected void init(VFSDeploymentUnit unit, ManagedConnectionFactoryDeploymentGroup metaData, VirtualFile file) throws Exception
   {
      metaData.setUrl(file.toURL());
   }

}
