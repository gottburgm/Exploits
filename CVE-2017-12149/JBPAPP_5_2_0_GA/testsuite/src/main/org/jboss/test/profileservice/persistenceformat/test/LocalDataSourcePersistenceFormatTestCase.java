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
package org.jboss.test.profileservice.persistenceformat.test;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.resource.deployers.management.LocalDSInstanceClassFactory;
import org.jboss.resource.deployers.management.MCFDGComponentMapper;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.system.deployers.managed.ServiceMetaDataICF;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.util.xml.JBossEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Test marshalling/unmarshalling with the JAXBAttachmentSerializer.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
public class LocalDataSourcePersistenceFormatTestCase extends AbstractPersistenceFormatTest
{

   public LocalDataSourcePersistenceFormatTestCase(String name) throws Exception
   {
      super(name);
   }
   
   public void setUp() throws Exception
   {
      super.setUp();
   }
   
   public void testProfileServiceTestDS() throws Throwable
   {
      getMOF().addInstanceClassFactory(new LocalDSInstanceClassFactory());
      getMOF().addInstanceClassFactory(new ServiceMetaDataICF());
      // 
      addComponentMapper(new TestMapper(getPersistenceFactory()));

      // Initial parsing of the dataSource deployment
      ManagedConnectionFactoryDeploymentGroup deployment = parseDataSource("profileservice/persistence/profileservice-test-ds.xml");
      assertNotNull(deployment);

      ManagedObject mo = getMOF().initManagedObject(deployment, null);

      ManagedComponent c = createDSComponent(mo);
      // Change values
      c.getProperty("min-pool-size").setValue(SimpleValueSupport.wrap(13));
      c.getProperty("max-pool-size").setValue(SimpleValueSupport.wrap(53));
      
      PersistenceRoot root = updateComponent(mo, c);
      assertNotNull(root);

      // Recreate
      deployment = parseDataSource("profileservice/persistence/profileservice-test-ds.xml");
      // update the information
      getPersistenceFactory().restorePersistenceRoot(root, deployment, null);

      // Create the MO again
      mo = getMOF().initManagedObject(deployment, null);
      c = createDSComponent(mo);
      
      assertEquals(SimpleValueSupport.wrap(13), c.getProperty("min-pool-size").getValue());
      assertEquals(SimpleValueSupport.wrap(53), c.getProperty("max-pool-size").getValue());
      
      // Assert the attachment
      ManagedConnectionFactoryDeploymentGroup mcfdg = deployment;
      assertNotNull(mcfdg);
      // Assert services
      assertServices(mcfdg.getServices()); 
      // Assert deployments
      assertDeployments(mcfdg.getDeployments());
      // TODO Assert loader repository
      assertNotNull(mcfdg.getLoaderRepositoryConfig());
   }
   
   protected void assertDeployments(List<ManagedConnectionFactoryDeploymentMetaData> deployments)
   {
      assertNotNull(deployments);
      assertEquals(1, deployments.size());
      
      ManagedConnectionFactoryDeploymentMetaData deployment = deployments.get(0);
      assertNotNull(deployment);
      assertEquals(13, deployment.getMinSize());
      assertEquals(53, deployment.getMaxSize());
   }
   
   protected void assertServices(List<ServiceMetaData> services)
   {
      assertNotNull(services);
      assertEquals(2, services.size());
      
      for(ServiceMetaData service : services)
      {
         assertNotNull(service.getCode());
         assertNotNull(service.getAttributes());
         assertFalse(service.getAttributes().isEmpty());
      }
   }
   
   protected ManagedComponent createDSComponent(ManagedObject deployment)
   {
      CollectionValue collection = (CollectionValue) deployment.getProperty("deployments").getValue();
      GenericValue generic = (GenericValue) collection.iterator().next();
      ManagedObject mo = (ManagedObject) generic.getValue();
      return createComponent(mo);
   }
   
   protected ManagedConnectionFactoryDeploymentGroup parseDataSource(String resource) throws Exception
   {
      // Get resource
      URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
      // The input source
      InputSource input = new InputSource(url.openStream());
      input.setSystemId(url.toURI().toString());
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setEntityResolver(new JBossEntityResolver());
      SAXSource source = new SAXSource(reader, input);
      // New JAXB context
      JAXBContext context = JAXBContext.newInstance(ManagedConnectionFactoryDeploymentGroup.class);
      Unmarshaller um = context.createUnmarshaller();
      // Unmarshal
      JAXBElement<ManagedConnectionFactoryDeploymentGroup> elem = um.unmarshal(source,
            ManagedConnectionFactoryDeploymentGroup.class);
      return elem.getValue();
   }
   
   private static class TestMapper extends MCFDGComponentMapper
   {

      public TestMapper(PersistenceFactory persistenceFactory)
      {
         super(persistenceFactory);
      }
      
      @Override
      protected PersistedComponent createComponent(Object attachment, ManagedComponent component)
      {
         // Note: this is using the TestMgtComponentImpl to get the MO
         ManagedObject mo = (ManagedObject) component.getParent();
         PersistedManagedObject persisted = getPersistencePlugin().createPersistedManagedObject(mo);
         PersistedComponent persistedComponent = new PersistedComponent(persisted);
         setComponentName(persistedComponent, mo);
         return persistedComponent;
      }
      
   }
}
