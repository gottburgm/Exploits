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
package org.jboss.profileservice.management.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.annotation.factory.AnnotationCreator;
import org.jboss.logging.Logger;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.managed.plugins.ManagedDeploymentImpl;
import org.jboss.metadata.spi.MetaData;
import org.jboss.profileservice.management.MBeanManagedObjectFactory;
import org.jboss.profileservice.management.ManagedOperationProxyFactory;
import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory.MBeanComponent;
import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory.MBeanDeployment;

/**
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class MBeanProfileView extends AbstractProfileView
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(MBeanProfileView.class);

   /** A fake profile key. */
   private static final ProfileKey key = new ProfileKey(MBeanProfileView.class.getName());

   /** */
   private HashMap<String, ManagedMBeanDeploymentFactory> mdfs =
      new HashMap<String, ManagedMBeanDeploymentFactory>();
   private MBeanServer mbeanServer;
   private MBeanManagedObjectFactory mbeanMOFactory = new MBeanManagedObjectFactory();

   public MBeanProfileView(ManagedOperationProxyFactory proxyFactory)
   {
      super(proxyFactory);
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public void addManagedMBeanDeployments(ManagedMBeanDeploymentFactory factory)
   {
      log.info("addManagedDeployment, "+factory);
      String name = factory.getFactoryName();
      this.mdfs.put(name, factory);
   }
   public void removeManagedMBeanDeployments(ManagedMBeanDeploymentFactory factory)
   {
      log.info("removeManagedDeployment, "+factory);
      String name = factory.getFactoryName();
      this.mdfs.remove(name);
   }

   @Override
   public ProfileKey getProfileKey()
   {
      return key;
   }

   protected void load()
   {
      boolean trace = log.isTraceEnabled();
      // Process mbean components that need to be exposed as ManagedDeployment/ManagedComponent
      for(ManagedMBeanDeploymentFactory mdf : mdfs.values())
      {
         log.debug("Processing deployments for factory: "+mdf.getFactoryName());
         Collection<MBeanDeployment> deployments = mdf.getDeployments(mbeanServer);
         for(MBeanDeployment md : deployments)
         {
            log.debug("Saw MBeanDeployment: "+md);
            HashMap<String, ManagedObject> unitMOs = new HashMap<String, ManagedObject>();
            Collection<MBeanComponent> components = md.getComponents();
            if(components != null)
            {
               for(MBeanComponent comp : components)
               {
                  log.debug("Saw MBeanComponent: "+comp);
                  try
                  {
                     ManagedObject mo = createManagedObject(comp.getName(), mdf.getDefaultViewUse(), mdf.getPropertyMetaMappings());
                     // Add a ManagementComponent annotation
                     String annotationExpr = "@org.jboss.managed.api.annotation.ManagementObject("
                        + "name=\""+comp.getName()+"\","
                        + "componentType=@org.jboss.managed.api.annotation.ManagementComponent(type=\""
                        + comp.getType()+"\",subtype=\""+comp.getSubtype()+"\")"
                        + ")";
                     // System.err.println(annotationExpr);
                     ManagementObject moAnn = (ManagementObject) AnnotationCreator.createAnnotation(
                           annotationExpr, ManagementObject.class);
                     // Bot the ManagementObject and ManagementComponent annotation need to be in the MO annotations
                     mo.getAnnotations().put(ManagementObject.class.getName(), moAnn);
                     ManagementComponent mcAnn = moAnn.componentType();
                     mo.getAnnotations().put(ManagementComponent.class.getName(), mcAnn);
                     unitMOs.put(comp.getName().getCanonicalName(), mo);
                  }
                  catch(Exception e)
                  {
                     log.warn("Failed to create ManagedObject for: "+comp, e);
                  }
               }
            }
            ManagedDeploymentImpl mdi = new ManagedDeploymentImpl(md.getName(), md.getName(), null, unitMOs);
            try
            {
               processManagedDeployment(mdi, DeploymentState.STARTED, 0, trace);
            }
            catch(Exception e)
            {
               log.warn("Failed to process ManagedDeployment for: " + md.getName(), e);
            }
         }
      }
   }

   private ManagedObject createManagedObject(ObjectName mbean, String defaultViewUse, Map<String, String> propertyMetaMappings) throws Exception
   {
      MBeanInfo info = mbeanServer.getMBeanInfo(mbean);
      ClassLoader mbeanLoader = mbeanServer.getClassLoaderFor(mbean);
      MetaData metaData = null;
      ViewUse[] viewUse = defaultViewUse == null ? null : new ViewUse[] { ViewUse.valueOf(defaultViewUse) };
      ManagedObject mo = mbeanMOFactory.getManagedObject(mbean, info, mbeanLoader, metaData, viewUse, propertyMetaMappings);
      return mo;
   }

   @Override
   public boolean hasBeenModified(Profile profile)
   {
      return false;
   }

}

