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
package org.jboss.test.server.profileservice.component.persistence.support;

import java.util.ArrayList;
import java.util.List;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.component.AbstractComponentMapper;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class TestComponentMapper extends AbstractComponentMapper
{

   public TestComponentMapper(PersistenceFactory persistenceFactory)
   {
      super(persistenceFactory);
   }

   @Override
   protected PersistedComponent createComponent(Object attachment, ManagedComponent component)
   {
      // Note: this is using the TestMgtComponentImpl to get the MO
      ManagedObject mo = (ManagedObject) component.getParent();
      PersistedManagedObject persistedMO = getPersistencePlugin().createPersistedManagedObject(mo);
      PersistedComponent persisted = new PersistedComponent(persistedMO);
      setComponentName(persisted, mo);
      return persisted;
   }
   
   @Override
   protected void setComponentName(PersistedComponent component, ManagedObject mo)
   {
      // Set the current name for tracking.
      component.setName((String) ((SimpleValue)mo.getProperty("name").getValue()).getValue()); 
   }

   @Override
   protected ManagedObject getComponent(Object attachment, PersistedComponent component, boolean create)
   {
      TestDeployment deployment = (TestDeployment) attachment;
      TestComponent instance = null;
      if(deployment.getComponents() != null && deployment.getComponents().isEmpty() == false)
      {
         for(TestComponent md : deployment.getComponents())
         {
            if(md.getName().equals(component.getOriginalName()))
            {
               instance = md;
               break;
            }
         }
      }
      if(instance == null && create)
      {
         instance = createTestComponent(component);
         deployment.getComponents().add(instance);
      }
      if(instance == null)
      {
         throw new IllegalStateException("could not find instance with name " + component.getOriginalName());
      }
      return getMOF().initManagedObject(instance, null);
   }

   @Override
   protected void removeComponent(Object attachment, PersistedComponent component)
   {
      TestDeployment deployment = (TestDeployment) attachment;
      List<TestComponent> components = new ArrayList<TestComponent>(); 
      if(deployment.getComponents() != null && deployment.getComponents().isEmpty() == false)
      {
         for(TestComponent md : deployment.getComponents())
         {
            if(md.getName().equals(component.getOriginalName()) == false)
               components.add(md);
         }
         deployment.setComponents(components);
      }
   }
   
   protected TestComponent createTestComponent(PersistedComponent component)
   {
      TestComponent test = new TestComponent();
      test.setName(component.getOriginalName());
      return test;
   }

   public String getType()
   {
      return TestDeployment.class.getName();
   }

}

