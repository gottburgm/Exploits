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
package org.jboss.system.deployers.managed;

import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.component.AbstractComponentMapper;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;

/**
 * The ServiceDeployment component mapper. This handles the ServiceMetaData
 * components in a ServiceDeployment.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ServiceDeploymentComponentMapper extends AbstractComponentMapper
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(ServiceDeploymentComponentMapper.class);
   
   public ServiceDeploymentComponentMapper(PersistenceFactory persistenceFactory)
   {
      super(persistenceFactory);
   }

   @Override
   protected void setComponentName(PersistedComponent component, ManagedObject mo)
   {
      ServiceMetaData md = (ServiceMetaData) mo.getAttachment();
      String name = md.getObjectName().getCanonicalName();
      component.setName(name);
   }
   
   @Override
   protected ManagedObject getComponent(Object attachment, PersistedComponent component, boolean create)
   {
      ServiceDeployment deployment = (ServiceDeployment) attachment;
      ServiceMetaData service = null;
      List<ServiceMetaData> services = deployment.getServices();
      if(services == null)
      {
         services = new ArrayList<ServiceMetaData>();
         deployment.setServices(services);
      }
      if(services != null && services.isEmpty() == false)
      {
         for(ServiceMetaData metaData : services)
         {
            if(metaData.getObjectName().getCanonicalName().equals(component.getOriginalName()))
            {
               service = metaData;
               break;
            }
         }
      }
      if(service == null && create)
      {
         // Create a new empty service meta data
         service = createEmptyServiceMetaData(component);
         deployment.getServices().add(service);
      }
      if(service == null)
      {
         throw new IllegalStateException("could not find service with name " + component.getOriginalName());
      }
      return getMOF().initManagedObject(service, null);
   }

   @Override
   protected void removeComponent(Object attachment, PersistedComponent component)
   {
      ServiceDeployment deployment = (ServiceDeployment) attachment;
      List<ServiceMetaData> deploymentServices = deployment.getServices();
      List<ServiceMetaData> services = new ArrayList<ServiceMetaData>();
      if(deploymentServices != null && deploymentServices.isEmpty() == false)
      {
         for(ServiceMetaData metaData : deploymentServices)
         {
            // Ignore the removed bean
            if(metaData.getObjectName().getCanonicalName().
                  equals(component.getOriginalName()) == false)
               services.add(metaData);
         }
      }
      deployment.setServices(services);
   }
   
   protected ServiceMetaData createEmptyServiceMetaData(PersistedComponent component)
   {
      ServiceMetaData service = new ServiceMetaData();
      ObjectName objectName = null;
      try
      {
         objectName = new ObjectName(component.getOriginalName());
         service.setObjectName(objectName);
      }
      catch (MalformedObjectNameException e)
      {
         // TODO: this should not happen, but we might could just ignore this
         // as the Persistence should restore the object name anyway
         throw new RuntimeException("failed to create object name for component " + component, e);
      }
      log.debug("created service "+ component.getOriginalName());
      return service;
   }

   public String getType()
   {
      return ServiceDeployment.class.getName();
   }

}

