/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployers.plugins.managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.managed.ManagedObjectCreator;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.DefaultFieldsImpl;
import org.jboss.managed.plugins.ManagedObjectImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.GenericValueSupport;
import org.jboss.profileservice.spi.types.ControllerStateMetaType;

/**
 * ManagedObjectCreator for KernelDeployment
 * 
 * @author Scott.Stark@jboss.org
 * @author Ales.Justin@jboss.org
 * @version $Revision: 106340 $
 */
public class KernelDeploymentManagedObjectCreator implements ManagedObjectCreator
{
   private static Logger log = Logger.getLogger(KernelDeploymentManagedObjectCreator.class);
   private ManagedObjectFactory mof;

   public KernelDeploymentManagedObjectCreator(ManagedObjectFactory mof)
   {
      if (mof == null)
         throw new IllegalArgumentException("Null ManagedObjectFactory.");
      this.mof = mof;
   }

   public void build(DeploymentUnit unit, Set<String> attachments, Map<String, ManagedObject> managedObjects) throws DeploymentException
   {
      // Handle multiple kernel deployments
      for(final Entry<String, Object> entry : unit.getAttachments().entrySet())
      {
         if(KernelDeployment.class.isInstance(entry.getValue()))
         {
            final KernelDeployment deployment = KernelDeployment.class.cast(entry.getValue());
            ManagedObject deploymentMO = managedObjects.get(entry.getKey());
            if(deploymentMO == null)
            {
               // allowMultipleAttachments
               deploymentMO = mof.createManagedObject(deployment.getClass());
               managedObjects.put(entry.getKey(), deploymentMO);
            }
            if(deploymentMO instanceof MutableManagedObject)
               ((MutableManagedObject) deploymentMO).setName(KernelDeployment.class.getName());
            if(deploymentMO instanceof ManagedObjectImpl)
               ((ManagedObjectImpl) deploymentMO).setAttachmentName(entry.getKey());
            // Build the BMD MO components
            build(unit, deployment, deploymentMO, managedObjects);
         }
      }
   }
   
   /**
    * Called by the KernelDeploymentDeployer to update the ManagedObjects map
    * created by the default ManagedObjectCreator. 
    *
    * @param unit the deployment unit
    * @param managedObjects map of managed objects
    * @throws DeploymentException for any deployment exception
    */
   public void build(DeploymentUnit unit, KernelDeployment deployment, ManagedObject deploymentMO, Map<String, ManagedObject> managedObjects) throws DeploymentException
   {
      List<BeanMetaDataFactory> beanFactories = deployment.getBeanFactories();

      MetaData metaData = unit.getMetaData();
      // Update the beanFactories value to a list of BeanMetaDataFactory with BeanMetaDatas
      ManagedProperty beanFactoriesMP = deploymentMO.getProperty("beanFactories");
      // In case of e.g. AOPDeployment
      if(beanFactoriesMP == null)
      {
         return;
      }
      List<GenericValue> tmpBFs = new ArrayList<GenericValue>();
      CollectionMetaType beansFactoryType = new CollectionMetaType(BeanMetaDataFactory.class.getName(), AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE);
      if(beanFactories != null)
      {
         List<GenericValue> tmpBeans = new ArrayList<GenericValue>();
         CollectionMetaType beansType = new CollectionMetaType(BeanMetaDataFactory.class.getName(), AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE);
         for(BeanMetaDataFactory bmdf : beanFactories)
         {
            
            ManagedObject bmdfMO = mof.initManagedObject(bmdf, metaData);
            if(bmdfMO == null)
            {
               // Create a container managed object
               bmdfMO = createFactoryManagedObject(bmdf, deploymentMO, metaData);
            }

            if((bmdfMO instanceof MutableManagedObject) == false)
            {
               // Just go with the default ManagedObject
               GenericValueSupport gv = new GenericValueSupport(AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE, bmdfMO);
               tmpBFs.add(gv);
               continue;
            }

            MutableManagedObject bmdfMMO = (MutableManagedObject) bmdfMO;
            bmdfMMO.setParent(deploymentMO);
            Map<String, ManagedProperty> oldProps = bmdfMMO.getProperties();
            ManagedProperty beansMPCheck = oldProps.get("beans");
            // If there already is a beans property assume it's correct
            if(beansMPCheck != null)
            {
               // Need to map 
               continue;
            }

            Map<String, ManagedProperty> newProps = new HashMap<String, ManagedProperty>(oldProps);
            // Create a beans ManagedProperty, a list of BeanMetaData ManagedObjects
            Fields fields = getFields("beans", beansType);
            ManagedPropertyImpl beansMP = new ManagedPropertyImpl(bmdfMO, fields);
            newProps.put("beans", beansMP);

            // Create a ManagedObject for each of the beans BeanMetaData
            List<BeanMetaData> beans = bmdf.getBeans();
            if(beans != null)
            {
               for(BeanMetaData bmd : beans)
               {
                  DeploymentUnit compUnit = unit.getComponent(bmd.getName());
                  if(compUnit == null)
                  {
                     log.debug("Failed to find component for bean: "+bmd.getName());
                     continue;
                  }
                  MetaData compMetaData = compUnit.getMetaData();
                  GenericValue gv = getManagedObjectValue(bmd, compMetaData, bmdfMO);
                  if(gv != null)
                  {
                     // The component managed objects need to be in the root map
                     ManagedObject compMO = (ManagedObject) gv.getValue();
                     // Use the ManagedObject name if it's not the same as the attachmentName
                     String managedObjectName = compUnit.getName();
                     if(compMO != null && compMO.getAttachmentName() != null)
                     {
                        managedObjectName = compMO.getAttachmentName().equals(compMO.getName()) ?
                              compUnit.getName() : compMO.getName();
                     }
                     // Add the managed object 
                     managedObjects.put(managedObjectName, compMO);
                     // Add the bean MO to the beans list
                     tmpBeans.add(gv);
                  }
               }
            }
            GenericValue[] beanMOs = new GenericValue[tmpBeans.size()];
            tmpBeans.toArray(beanMOs);
            CollectionValueSupport values = new CollectionValueSupport(beansType, beanMOs);
            beansMP.setValue(values);
            // Update the bean factory properties
            bmdfMMO.setProperties(newProps);
         }
      }
      GenericValue[] mos = new GenericValue[tmpBFs.size()];
      tmpBFs.toArray(mos);
      CollectionValueSupport values = new CollectionValueSupport(beansFactoryType, mos);
      // This bypasses the write through back to the metadata
      beanFactoriesMP.getFields().setField(Fields.VALUE, values);
   }

   protected ManagedObject createFactoryManagedObject(BeanMetaDataFactory bmdf,
         ManagedObject parent, MetaData metaData)
   {
      ManagedObjectImpl bmdfMO = new ManagedObjectImpl(bmdf.getClass().getName());
      Map<String, ManagedProperty> newProps = new HashMap<String, ManagedProperty>();
      bmdfMO.setParent(parent);
      bmdfMO.setProperties(newProps);
      return bmdfMO;
   }

   /**
    * 
    * @param bmd
    * @param metaData
    * @param parentMO
    * @return
    */
   protected GenericValue getManagedObjectValue(BeanMetaData bmd, MetaData metaData, ManagedObject parentMO)
   {
      String name = bmd.getName();
      ManagedObject mo = mof.initManagedObject(bmd, null, metaData, name, null);
      if(parentMO != null && mo instanceof MutableManagedObject)
      {
         MutableManagedObject mmo = (MutableManagedObject) mo;
         mmo.setParent(parentMO);
         Map<String, ManagedProperty> oldProps = mmo.getProperties();
         Map<String, ManagedProperty> newProps = new HashMap<String, ManagedProperty>(oldProps);
         // Add a state property
         Fields stateFields = getFields("state", ControllerStateMetaType.TYPE);
         ManagedPropertyImpl stateMP = new ManagedPropertyImpl(mmo, stateFields);
         newProps.put("state", stateMP);
         mmo.setProperties(newProps);
      }
      return new GenericValueSupport(AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE, mo);
   }

   /**
    * Create a DefaultFieldsImpl for the given property name and type
    * @param name - the property name
    * @param type - the property type
    * @return return the fields implementation
    */
   protected Fields getFields(String name, MetaType type)
   {
      DefaultFieldsImpl fields = new DefaultFieldsImpl();
      fields.setMetaType(type);
      fields.setName(name);
      fields.setField(Fields.MAPPED_NAME, name);
      fields.setMandatory(false);
      fields.setDescription("The bean controller state");

      return fields;
   }
}
