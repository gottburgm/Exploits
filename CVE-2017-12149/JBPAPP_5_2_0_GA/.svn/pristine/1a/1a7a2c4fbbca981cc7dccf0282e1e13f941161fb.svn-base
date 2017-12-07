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
package org.jboss.resource.deployers.management;

import java.util.ArrayList;
import java.util.List;

import org.jboss.managed.api.ManagedObject;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.component.AbstractComponentMapper;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;

/**
 * A ManagedConnectionFactoryDeploymentMetaData persistence ComponentMapper.
 * This will handle the persistence operations for the MCFDeploymentMetaData.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class MCFDGComponentMapper extends AbstractComponentMapper
{

   public MCFDGComponentMapper(PersistenceFactory persistenceFactory)
   {
      super(persistenceFactory);
   }

   @Override
   protected void setComponentName(PersistedComponent component, ManagedObject mo)
   {
       ManagedConnectionFactoryDeploymentMetaData metaData =
          (ManagedConnectionFactoryDeploymentMetaData) mo.getAttachment();
       // Set the current name;
       component.setName(metaData.getJndiName());
   }
   
   @Override
   protected ManagedObject getComponent(Object attachment, PersistedComponent component, boolean create)
   {
      ManagedConnectionFactoryDeploymentGroup deployment = (ManagedConnectionFactoryDeploymentGroup) attachment;
      ManagedConnectionFactoryDeploymentMetaData metaData = null;
      if(deployment.getDeployments() != null && deployment.getDeployments().isEmpty() == false)
      {
         for(ManagedConnectionFactoryDeploymentMetaData md : deployment.getDeployments())
         {
            if(md.getJndiName().equals(component.getOriginalName()))
            {
               metaData = md;
               break;
            }
         }
      }
      if(metaData == null && create)
      {
         // TODO create new attachment
      }
      if(metaData == null)
         throw new IllegalStateException("could not find deployment " + component.getOriginalName());
      return getMOF().initManagedObject(metaData, null);
   }

   @Override
   protected void removeComponent(Object attachment, PersistedComponent component)
   {
      ManagedConnectionFactoryDeploymentGroup deployment = (ManagedConnectionFactoryDeploymentGroup) attachment;
      if(deployment.getDeployments() != null && deployment.getDeployments().isEmpty() == false)
      {
         List<ManagedConnectionFactoryDeploymentMetaData> deployments = new ArrayList<ManagedConnectionFactoryDeploymentMetaData>();
         for(ManagedConnectionFactoryDeploymentMetaData md : deployment.getDeployments())
         {
            if(md.getJndiName().equals(component.getOriginalName()) == false)
               deployments.add(md);
         }
         deployment.setDeployments(deployments);
      }
   }

   public String getType()
   {
      return ManagedConnectionFactoryDeploymentGroup.class.getName();
   }

}

