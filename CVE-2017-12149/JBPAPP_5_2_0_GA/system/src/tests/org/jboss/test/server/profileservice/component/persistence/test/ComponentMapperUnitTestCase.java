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
package org.jboss.test.server.profileservice.component.persistence.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.test.server.profileservice.component.persistence.support.CompositeMetaData;
import org.jboss.test.server.profileservice.component.persistence.support.TestComponent;
import org.jboss.test.server.profileservice.component.persistence.support.TestComponentMapper;
import org.jboss.test.server.profileservice.component.persistence.support.TestDeployment;
import org.jboss.test.server.profileservice.component.persistence.support.TestMgtComponentImpl;

/**
 * Basic component mapper unit test case.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ComponentMapperUnitTestCase extends AbstractComponentMapperTest
{
   
   public ComponentMapperUnitTestCase(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
   }
   
   public void testAdd() throws Exception
   {
      // Add the component mapper
      addComponentMapper(new TestComponentMapper(getPersistenceFactory()));

      TestComponent addComponent = createComponentMetaData("component3",
            new ObjectName("org.jboss:type=testComponent3"),
            createCompositeMetaData("composite3", 'c', new Integer(3)));
      
      ManagedObject temp = getMOF().initManagedObject(addComponent, null);
      ManagedComponent component = new TestMgtComponentImpl(temp);
      
      ManagedObject mo = createDeploymentMO();
      PersistenceRoot root = getPersistenceFactory().addComponent(mo, component);
      
      root = restore(root);
      
      TestDeployment attachment = createDeploymentMetaData();
      getPersistenceFactory().restorePersistenceRoot(root, attachment, null);
      
      //
      assertEquals(3, attachment.getComponents().size());
   }
   
   public void testUpdate() throws Exception
   {
      // Add the component mapper
      addComponentMapper(new TestComponentMapper(getPersistenceFactory()));

      // Get the components
      ManagedObject mo = createDeploymentMO();
      Iterator<?> iterator = ((CollectionValue) mo.getProperty("components").getValue()).iterator();
      ManagedObject component1 = (ManagedObject) ((GenericValue) iterator.next()).getValue();
      ManagedObject component2 = (ManagedObject) ((GenericValue) iterator.next()).getValue();
      
      // create the persistence information
      TestMgtComponentImpl tComp1 = new TestMgtComponentImpl(component1);
      PersistenceRoot root = getPersistenceFactory().updateComponent(mo, tComp1);
      TestMgtComponentImpl tComp2 = new TestMgtComponentImpl(component2);
      root = getPersistenceFactory().updateComponent(root, mo, tComp2);      
      
      assertNotNull(root.getComponents());
      // serialize / deserialize
      PersistenceRoot persisted = restore(root);
      
      // Create a new root attachment with empty components
      TestDeployment test = new TestDeployment();
      test.getComponents().add(createComponentMetaData("component1", null, null));
      test.getComponents().add(createComponentMetaData("component2", null, null));
      // Apply the persisted information, which should recreate the missing properties
      getPersistenceFactory().restorePersistenceRoot(persisted, test, null);
      assertFalse(test.getComponents().isEmpty());
      
      // Check if the properties are available again
      for(TestComponent restoredComponent : test.getComponents())
      {
         assertNotNull("null object name "+ restoredComponent.getName(), restoredComponent.getObjectName());
         assertNotNull("null composite "+ restoredComponent.getName(), restoredComponent.getComposite());
      }
   }
   
   public void testRemove() throws Exception
   {
      // Add the component mapper
      addComponentMapper(new TestComponentMapper(getPersistenceFactory()));

      // Get the components
      ManagedObject mo = createDeploymentMO();      
      Iterator<?> iterator = ((CollectionValue) mo.getProperty("components").getValue()).iterator();
      ManagedObject component1 = (ManagedObject) ((GenericValue) iterator.next()).getValue();
      
      // create the persistence information
      TestMgtComponentImpl tComp1 = new TestMgtComponentImpl(component1);
      PersistenceRoot root = getPersistenceFactory().removeComponent(mo, tComp1);
      PersistenceRoot persisted = restore(root);

      // Check removed
      TestDeployment deployment = createDeploymentMetaData();
      getPersistenceFactory().restorePersistenceRoot(persisted, deployment, null);
      assertEquals(1, deployment.getComponents().size());
      
      // Reset
      root = getPersistenceFactory().resetComponent(persisted, mo, tComp1);
      persisted = restore(root);
      assertNull(persisted.getComponents());
   }
   
   public void testNameChanges() throws Exception
   {
      // Add the component mapper
      addComponentMapper(new TestComponentMapper(getPersistenceFactory()));

      // Get the components
      ManagedObject mo = createDeploymentMO();

      PersistenceRoot root = new PersistenceRoot();
      
      TestDeployment deployment = updateName(root, mo, "change1");
      mo = getMOF().initManagedObject(deployment, null); 
      
      deployment = updateName(root, mo, "change2");
      mo = getMOF().initManagedObject(deployment, null);
      
      deployment = updateName(root, mo, "change3");
      mo = getMOF().initManagedObject(deployment, null);      
      
   }
   
   protected TestDeployment updateName(PersistenceRoot root, ManagedObject mo, String name) throws Exception
   {
      Iterator<?> iterator = ((CollectionValue) mo.getProperty("components").getValue()).iterator();
      ManagedObject component = (ManagedObject) ((GenericValue) iterator.next()).getValue();
      
      component.getProperty("name").setValue(SimpleValueSupport.wrap(name));
      root = getPersistenceFactory().updateComponent(root, mo, new TestMgtComponentImpl(component));
      root = restore(root);
      
      TestDeployment deployment = createDeploymentMetaData();
      getPersistenceFactory().restorePersistenceRoot(root, deployment, null);
      return deployment;
   }
   
   protected ManagedObject createDeploymentMO() throws Exception
   {
      return getMOF().initManagedObject(createDeploymentMetaData(), null);
   }
   
   protected TestDeployment createDeploymentMetaData() throws Exception
   {
      TestDeployment deployment = new TestDeployment();
      List<TestComponent> components = new ArrayList<TestComponent>();
      components.add(
            createComponentMetaData("component1",
                  new ObjectName("org.jboss:type=testComponent1"),
                  createCompositeMetaData("composite1", 'a', new Integer(1))));
      components.add(
            createComponentMetaData("component2",
                  new ObjectName("org.jboss:type=testComponent2"),
                  createCompositeMetaData("composite2", 'b', new Integer(2))));
      deployment.setComponents(components);
      return deployment;
   }
   
   protected TestComponent createComponentMetaData(String name, ObjectName objectName,
         CompositeMetaData composite)
   {
      TestComponent component = new TestComponent();
      component.setName(name);
      component.setObjectName(objectName);
      component.setComposite(composite);
      return component;
   }
   
   protected CompositeMetaData createCompositeMetaData(String name, char character, Integer integer)
   {
      return new CompositeMetaData(name, character, integer);
   }
   
}

