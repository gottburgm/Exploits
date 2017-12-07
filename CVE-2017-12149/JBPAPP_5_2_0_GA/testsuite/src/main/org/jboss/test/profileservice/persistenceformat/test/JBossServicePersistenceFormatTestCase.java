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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.system.deployers.managed.ServiceDeploymentComponentMapper;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceDeploymentClassPath;
import org.jboss.system.metadata.ServiceDeploymentParser;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.jboss.system.server.profileservice.persistence.ManagedObjectPersistencePlugin;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.ModificationInfo;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.w3c.dom.Document;

/**
 * Persistence test for ServiceMetaData components.
 * 
 * TODO the ManagedObject view of ServiceMetaData needs to be completed. 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88906 $
 */
public class JBossServicePersistenceFormatTestCase extends AbstractPersistenceFormatTest
{

   public JBossServicePersistenceFormatTestCase(String name)
   {
      super(name);
   }
   
   
   public void testServiceMetaData() throws Exception
   {
      // Don't use the ServiceMetaDataICF - to be able to test plain
      // ServiceMetaData without MOClass override... 
      // getMOF().addInstanceClassFactory(new ServiceMetaDataICF());
      
      addComponentMapper(new TestMapper(getPersistenceFactory()));
      
      ServiceDeployment deployment = parseJbossServiceXml("profileservice/persistence/jboss-service.xml");
      
      // Manually create a persistence view
      List<ServiceMetaData> services = deployment.getServices();
      List<PersistedComponent> components = new ArrayList<PersistedComponent>();
      ManagedObjectPersistencePlugin plugin = getPersistenceFactory().getPersistencePlugin();
      for(ServiceMetaData md : deployment.getServices())
      {
         // Bypass the ServiceMetaDataICF...
         ManagedObject mo = getMOF().initManagedObject(md, null); 
         PersistedManagedObject persisted = plugin.createPersistedManagedObject(mo);
         
         // Fix the names, as we don't use the ServiceMetaDataICF
         String name = md.getObjectName().getCanonicalName();
         persisted.setName(name);
         persisted.setOriginalName(name);
         persisted.setModificationInfo(ModificationInfo.ADDED);
         
         components.add(new PersistedComponent(persisted));
      }
      
      PersistenceRoot root = new PersistenceRoot();
      root.setComponents(components);
      root = restore(root);
      
      // Test if we can recreate a complete view, without
      // requiring any previous service
      deployment = new ServiceDeployment();
      deployment.setServices(new ArrayList<ServiceMetaData>());      

      getPersistenceFactory().restorePersistenceRoot(root, deployment, null);
      
      assertServices(services, deployment.getServices());
      
   }
   
//   
//   public void testJbossService() throws Exception
//   {
//      // Parse 
//      ServiceDeployment deployment = parseJbossServiceXml("profileservice/persistence/jboss-service.xml");
//      assertNotNull(deployment);
//      // Create serializer
//      AbstractFileAttachmentsSerializer serializer = getAttachmentSerializer();
//      // Save
//      serializer.saveAttachment("test", deployment);
//      // Restore
//      ServiceDeployment restored = serializer.loadAttachment("test", ServiceDeployment.class);
//      assertNotNull(restored);
//      
//      // Assert services
//      assertServices(deployment.getServices(), restored.getServices());
//      // loader repository
//      assertLoaderRepository(deployment.getLoaderRepositoryConfig(), restored.getLoaderRepositoryConfig());
//      // classpath
//      assertClassPaths(deployment.getClassPaths(), restored.getClassPaths());
//   }
   
   protected void assertServices(List<ServiceMetaData> original, List<ServiceMetaData> restored) throws Exception
   {
      assertNotNull(original);
      assertNotNull(restored);
      
      assertEquals("same size", original.size(), restored.size());
      
      Map<String, ServiceMetaData> restoredMap = new HashMap<String, ServiceMetaData>();
      for(ServiceMetaData service : restored)
         restoredMap.put(service.getObjectName().getCanonicalName(), service);
      
      for(ServiceMetaData originalService : original)
      {
         ServiceMetaData restoredService = restoredMap.get(originalService.getObjectName().getCanonicalName());
         assertNotNull(restoredService);
         // assert service
         assertServiceMetaData(originalService, restoredService);
      }
   }
   
   protected void assertServiceMetaData(ServiceMetaData original, ServiceMetaData restored) throws Exception
   {
      // Code
      assertEquals(original.getCode(), restored.getCode());
      // Interface
      assertEquals(original.getInterfaceName(), restored.getInterfaceName());
      // XMBeanCode
      assertEquals(original.getXMBeanCode(), restored.getXMBeanCode());
      // XMBeanDD 
      assertEquals(original.getXMBeanDD(), restored.getXMBeanDD());
      // ClassLoaderName
      assertEquals(original.getClassLoaderName(), restored.getClassLoaderName());
      // Mode
      assertEquals(original.getMode(), restored.getMode());
      // XMBeanDescriptor
      assertEquals(original.getXMBeanDescriptor(), restored.getXMBeanDescriptor());
      // Constructor
      assertServiceConstructor(original.getConstructor(), restored.getConstructor());

      // TODO more checking
      
      List<String> originalAliasases = original.getAliases();
      List<String> restoredAliasases = restored.getAliases();
      
      assertEquals(originalAliasases, restoredAliasases);
      
      assertEquals(original.getAnnotations(), restored.getAnnotations());
      if(original.getAnnotations() != null)
      {
         assertNotNull(restored.getAnnotations());
         assertEquals(original.getAnnotations().size(), restored.getAnnotations().size());
         
      }
      
      assertEquals(original.getDependencies(), restored.getDependencies());
      if(original.getDependencies() != null)
      {
         assertNotNull(restored.getDependencies());
         assertEquals(original.getDependencies().size(), restored.getDependencies().size());
         // TODO assertDependency
      }
      else
      {
         assertNull(restored.getDependencies());
      }
      
//      assertAttributes(original.getAttributes(), restored.getAttributes());
      assertDependencies(original.getDependencies(), restored.getDependencies());
      
   }
   
   protected void assertAttributes(List<ServiceAttributeMetaData> original, List<ServiceAttributeMetaData> restored)
   {
      if(original == null)
      {
         assertNull(restored);
         return;
      }
      else
      {
         assertNotNull(restored);
      }
      
      assertEquals(original.size(), restored.size());
      if(original.isEmpty())
         return;
      
      Map<String, ServiceAttributeMetaData> attributesMap = new HashMap<String, ServiceAttributeMetaData>();
      for(ServiceAttributeMetaData attribute : restored)
         attributesMap.put(attribute.getName(), attribute);
      
      for(ServiceAttributeMetaData attribute : original)
         assertAttributeMetaData(attribute, attributesMap.get(attribute.getName()));
   }
   
   protected void assertAttributeMetaData(ServiceAttributeMetaData original, ServiceAttributeMetaData restored)
   {
      assertNotNull(original);
      assertNotNull(original.getName(), restored);
      
      if(original.getValue() == null)
      {
         assertNull(restored.getValue());
         return;
      }
      else
      {
         assertNotNull(restored.getValue());
      }
      
      ServiceValueMetaData value = original.getValue();
      // TODO we need some MetaMapping for attributes
      assertEquals(original.getName(), value.getClass().getName(), restored.getValue().getClass().getName());
   }
   

   protected void assertServiceConstructor(ServiceConstructorMetaData original, ServiceConstructorMetaData restored) throws Exception
   {
      if(original == null)
      {
         assertNull(restored);
         return;
      }
      else
      {
         assertNotNull(restored);
      }
      
      if(original.getParams() != null)
      {
         assertNotNull(restored.getParams());
         assertEquals(original.getParams().length, restored.getParams().length);
         assertEquals(original.getParams(), restored.getParams());
      }
      if(original.getSignature() != null)
      {
         assertNotNull(restored.getSignature());
         assertEquals(original.getParams().length, restored.getParams().length);
         assertEquals(original.getSignature(), restored.getSignature());
      }
   }
   
   protected void assertDependencies(List<ServiceDependencyMetaData> original, List<ServiceDependencyMetaData> restored)
   {
      if(original == null)
      {
         assertNull(restored);
         return;
      }
      else
      {
         assertNotNull(restored);
      }
         
      assertEquals(original.size(), restored.size());
      // TODO assertDependency
   }
   
   protected void assertDependency(ServiceDependencyMetaData original, ServiceDependencyMetaData restored)
   {
      if(original == null)
      {
         assertNull(restored);
         return;
      }
      else
      {
         assertNotNull(restored);
      }
      
      assertEquals(original.getIDependOn(), restored.getIDependOn());
   }
   
   protected void assertLoaderRepository(LoaderRepositoryConfig original, LoaderRepositoryConfig restored)
   {
      if(original == null)
      {
         assertNull(restored);
         return;
      }
      else
      {
         assertNotNull(restored);
      }
      
      assertEquals(original.configParserClassName, restored.configParserClassName);
      assertEquals(original.repositoryClassName, restored.repositoryClassName);
      assertEquals(original.repositoryConfig, restored.repositoryConfig);
      assertEquals(original.repositoryName, restored.repositoryName);
   }
   
   protected void assertClassPaths(List<ServiceDeploymentClassPath> original, List<ServiceDeploymentClassPath> restored)
   {
      if(original == null)
      {
         assertNull(restored);
         return;
      }
      else
      {
         assertNotNull(restored);
      }
      
      assertEquals(original.size(), restored.size());
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

