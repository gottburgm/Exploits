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
package org.jboss.test.profileservice.persistenceformat.test;

import java.io.File;
import java.util.List;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.system.deployers.managed.ServiceDeploymentComponentMapper;
import org.jboss.system.deployers.managed.ServiceMetaDataICF;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceDeploymentParser;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.system.metadata.ServiceTextValueMetaData;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.w3c.dom.Document;

/**
 * Test the merging of JMSDestination attributes.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
public class JMSDestinationPersistenceFormatTestCase extends AbstractPersistenceFormatTest
{

   public JMSDestinationPersistenceFormatTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      enableTrace("org.jboss.system");      
   }
   
   public void testTopic() throws Throwable
   {
      doTestMerge("profileservice/override/testTopic-service.xml");
   }
   
   public void testQueue() throws Throwable
   {
      doTestMerge("profileservice/override/testQueue-service.xml");
   }
   
   protected void doTestMerge(String xmlName) throws Throwable
   {
      // Set the ICF
      getMOF().addInstanceClassFactory(new ServiceMetaDataICF());
      // TODO create dependency on messaging project ?
//      getMOF().addManagedObjectDefinition(new QueueMODefinition(getMOF()));
//      getMOF().addManagedObjectDefinition(new TopicMODefinition(getMOF()));
      
      // Add the testmapper
      addComponentMapper(new TestMapper(getPersistenceFactory()));
      
      // create
      ManagedObject deploymentMO = getDeploymentMO(xmlName);

      ManagedComponent component = createJMSComponent(deploymentMO);
      
      // update property
      component.getProperty("downCacheSize").setValue(SimpleValueSupport.wrap(123456));
      
      ServiceDeployment deployment = parseJbossServiceXml(xmlName);
      PersistenceRoot root = updateComponent(deploymentMO, component);
      getPersistenceFactory().restorePersistenceRoot(root, deployment, null);
      
      // 
      ManagedObject restored = getMOF().initManagedObject(deployment, null);
      component = createJMSComponent(restored);
      
      // assert
      assertEquals(SimpleValueSupport.wrap(123456),  component.getProperty("downCacheSize").getValue());
      
      
      // Assert attachment meta data
      ServiceMetaData service = deployment.getServices().get(0);
      assertNotNull(service);
      boolean foundAttribute = false;
      for(ServiceAttributeMetaData attribute : service.getAttributes())
      {
         if("DownCacheSize".equals(attribute.getName()))
         {
            String text = ((ServiceTextValueMetaData) attribute.getValue()).getText();
            assertEquals("123456", text);
            foundAttribute = true;
         }
         else if("ServerPeer".equals(attribute.getName()))
         {
            ServiceDependencyValueMetaData value = ((ServiceDependencyValueMetaData) attribute.getValue());
            ObjectName name = value.getObjectName();
            assertNotNull(name);
         }
         else if("ExpiryQueue".equals(attribute.getName()))
         {
            ServiceTextValueMetaData value = (ServiceTextValueMetaData) attribute.getValue();
            assertNotNull(value.getText());
         }
      }
      assertTrue(foundAttribute);
   }
   
   protected ManagedComponent createJMSComponent(ManagedObject serviceDeploymentMO)
   {
      assertNotNull(serviceDeploymentMO);
      CollectionValue collection = (CollectionValue) serviceDeploymentMO.getProperty("services").getValue();
      assertNotNull(collection);
      GenericValue topic = (GenericValue) collection.iterator().next();
      assertNotNull(topic);
      ManagedObject topicMO = (ManagedObject) topic.getValue();
      return createComponent(topicMO);
   }
   
   protected ManagedProperty getProperty(ManagedObject serviceDeploymentMO, String propertyName)
   {
      assertNotNull(serviceDeploymentMO);
      CollectionValue collection = (CollectionValue) serviceDeploymentMO.getProperty("services").getValue();
      assertNotNull(collection);
      GenericValue topic = (GenericValue) collection.iterator().next();
      assertNotNull(topic);
      ManagedObject topicMO = (ManagedObject) topic.getValue();
      assertNotNull(topicMO);
      
      // downCacheSize
      return topicMO.getProperty(propertyName);
   }
   
   protected ManagedObject getDeploymentMO(String resource) throws Exception
   {
      ServiceDeployment deployment = parseJbossServiceXml(resource);
      assertNotNull(deployment);
      
      return getMOF().initManagedObject(deployment, null);      
   }
   
   protected ServiceDeployment parseJbossServiceXml(String resource) throws Exception
   {
      File file = new File(Thread.currentThread().getContextClassLoader().getResource(resource).toURI());
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Document document = factory.newDocumentBuilder().parse(file);
      
      ServiceDeploymentParser parser = new ServiceDeploymentParser(document);
      
      ServiceDeployment deployment = parser.parse();
      assertNotNull(deployment);
      
      ServiceMetaDataParser serviceParser = new ServiceMetaDataParser(deployment.getConfig());
      List<ServiceMetaData> services = serviceParser.parse();
      assertNotNull(services);
      
      deployment.setServices(services);
      return deployment;
   }
   
   private static final class TestMapper extends ServiceDeploymentComponentMapper
   {

      public TestMapper(PersistenceFactory persistenceFactory)
      {
         super(persistenceFactory);
      }
      
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
