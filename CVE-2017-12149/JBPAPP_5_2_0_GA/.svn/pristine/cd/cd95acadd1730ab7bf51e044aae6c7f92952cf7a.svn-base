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
package org.jboss.deployers.plugins.managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.metadata.spi.MetaData;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.component.AbstractComponentMapper;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;

/**
 * A AbstractKernelDeployment persistence ComponentMapper.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class KernelDeploymentComponentMapper extends AbstractComponentMapper
{

   public KernelDeploymentComponentMapper(PersistenceFactory persistenceFactory)
   {
      super(persistenceFactory);
   }

   @Override
   protected void setComponentName(PersistedComponent component, ManagedObject mo)
   {
      BeanMetaData bmd = (BeanMetaData) mo.getAttachment();
      // Set the current name
      component.setName(bmd.getName());
   }
   
   @Override
   protected ManagedObject getComponent(Object attachment, PersistedComponent component, boolean create)
   {
      AbstractKernelDeployment deployment = (AbstractKernelDeployment) attachment;
      Map<String, BeanMetaData> beans = new HashMap<String, BeanMetaData>();
      for(BeanMetaData bmd : deployment.getBeans())
      {
         beans.put(bmd.getName(), bmd);
      }
      BeanMetaData bmd = beans.get(component.getOriginalName());
      // Create a new Bean
      if(bmd == null && create)
      {
         AbstractBeanMetaData bean = createEmptyBeanMetaData(component);
         if(deployment.getBeanFactories() == null)
            deployment.setBeanFactories(new ArrayList<BeanMetaDataFactory>());
         
         deployment.getBeanFactories().add(bean);
         bmd = bean;
      }
      if(bmd == null)
      {
         throw new IllegalStateException("Could not find bean: " + component.getOriginalName());
      }
      // TODO we need the MetaData here!
      MetaData metaData = null;
      return getMOF().initManagedObject(bmd, metaData);
   }

   @Override
   protected void removeComponent(Object attachment, PersistedComponent component)
   {
      AbstractKernelDeployment deployment = (AbstractKernelDeployment) attachment;
      if(deployment.getBeanFactories() != null && deployment.getBeanFactories().isEmpty() == false)
      {
         boolean removed = false;
         List<BeanMetaDataFactory> beanFactories = new ArrayList<BeanMetaDataFactory>();
         for(BeanMetaDataFactory bmdf : deployment.getBeanFactories())
         {
            if(bmdf instanceof AbstractBeanMetaData)
            {
               AbstractBeanMetaData bean = (AbstractBeanMetaData) bmdf;
               if(bean.getName().equals(component.getOriginalName()))
               {
                  removed = true;
               }
               else
               {
                  beanFactories.add(bmdf);                  
               }
            }
            else
            {
               beanFactories.add(bmdf);
            }
         }
         if(! removed)
            throw new IllegalStateException("Could not remove component " + component.getOriginalName());
         // Update deployment
         deployment.setBeanFactories(beanFactories);
      }
   }
   
   protected AbstractBeanMetaData createEmptyBeanMetaData(PersistedComponent component)
   {
      AbstractBeanMetaData bean = new AbstractBeanMetaData();
      bean.setName(component.getOriginalName());
      // See if we stored the bean class name
      String beanClassName = component.getTemplateName();
      if(beanClassName == null)
         beanClassName = component.getClassName();
      if(beanClassName != null && beanClassName.equals(AbstractBeanMetaData.class.getName()) == false)
         bean.setBean(beanClassName);
      return bean;
   }

   public String getType()
   {
      return AbstractKernelDeployment.class.getName();
   }
   
}

