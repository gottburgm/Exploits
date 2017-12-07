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
package org.jboss.test.profileservice.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossTestCase;
import org.jboss.virtual.VFS;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 90193 $</tt>
 */
public abstract class AbstractProfileServiceTest
   extends JBossTestCase
{
   
   /** We use the default profile, defined by DeploymentManager to deploy apps. */
   public static final ProfileKey defaultProfile = new ProfileKey(ProfileKey.DEFAULT);
   
   protected ManagementView activeView;
   protected DeploymentManager deployMgr;
   private MetaValueFactory metaValueFactory;

   public AbstractProfileServiceTest(String name)
   {
      super(name);
   }
   
   /**
    * @return the ProfileKey.name to use when loading the profile
    */
   protected String getProfileName()
   {
      return null;
   }
   
   protected ProfileKey getProfileKey()
   {
      if(getProfileName() == null)
         return defaultProfile;
      
      return new ProfileKey(getProfileName());
   }

   protected void removeDeployment(String deployment)
      throws Exception
   {
      String names[] = new String[] {deployment};
      DeploymentManager deployMgr = getDeploymentManager();
      try
      {
         DeploymentProgress progress = deployMgr.stop(names);
         progress.run();
         assertFalse("failed: " + progress.getDeploymentStatus().getFailure(), progress.getDeploymentStatus().isFailed());
      }
      finally
      {
         DeploymentProgress progress = deployMgr.remove(names);
         progress.run();
         assertFalse("failed: " + progress.getDeploymentStatus().getFailure(), progress.getDeploymentStatus().isFailed());
      }
   }

   protected void createComponentTest(String templateName,
         Map<String, MetaValue> propValues,
         String deploymentName,
         ComponentType componentType, String componentName)
         throws Exception
   {
      createComponentTest(templateName, propValues, deploymentName, componentType, componentName, true);
   }

   protected void createComponentTest(String templateName,
         Map<String, MetaValue> propValues,
         String deploymentName,
         ComponentType componentType, String componentName,
         boolean processChanges)
   throws Exception
   {
      Set<String> removedPropNames = Collections.emptySet();
      createComponentTest(templateName, propValues, removedPropNames,
            deploymentName, componentType, componentName, processChanges);
   }
   protected void createComponentTest(String templateName,
                                      Map<String, MetaValue> propValues,
                                      Set<String> removedPropNames,
                                      String deploymentName,
                                      ComponentType componentType, String componentName,
                                      boolean processChanges)
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      DeploymentTemplateInfo info = mgtView.getTemplate(templateName);
      assertNotNull("template " + templateName + " found", info);
      Map<String, ManagedProperty> props = info.getProperties();
      for(String propName : propValues.keySet())
      {
         ManagedProperty prop = props.get(propName);
         assertTrue(prop.getName(), prop.hasViewUse(ViewUse.CONFIGURATION));
         log.debug("createComponentTest("+propName+") before: "+prop.getValue());
         assertNotNull("property " + propName + " found in template " + templateName, prop);
         prop.setValue(propValues.get(propName));
         log.debug("createComponentTest("+propName+") after: "+prop.getValue());
      }
      for(String propName : removedPropNames)
      {
         ManagedProperty prop = props.get(propName);
         prop.setRemoved(true);
         log.debug("removed property: "+propName);
      }
      
      mgtView.applyTemplate(deploymentName, info);
      if(processChanges)
      {
         mgtView.process();
   
         // reload the view
         activeView = null;
         mgtView = getManagementView();
         ManagedComponent dsMC = getManagedComponent(mgtView, componentType, componentName);
         assertNotNull(dsMC);
   
         Set<String> mcPropNames = new HashSet<String>(dsMC.getPropertyNames());
         for(String propName : propValues.keySet())
         {
            ManagedProperty prop = dsMC.getProperty(propName);
            assertNotNull(prop);
            Object propValue = prop.getValue();
            Object expectedValue = propValues.get(propName);
            assertEquals(prop.getName(), expectedValue, propValue);
   
            mcPropNames.remove(propName);
         }
   
         if(!mcPropNames.isEmpty())
         {
            log.warn(getName() + "> untested properties: " + mcPropNames);
         }
      }
   }

   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected ManagementView getManagementView()
      throws Exception
   {
      if( activeView == null )
      {
         InitialContext ctx = getInitialContext();
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         activeView = ps.getViewManager();
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      // Reload
      activeView.load();
      return activeView;
   }
   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected DeploymentManager getDeploymentManager()
      throws Exception
   {
      if( deployMgr == null )
      {
         InitialContext ctx = getInitialContext();
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         deployMgr = ps.getDeploymentManager();
         deployMgr.loadProfile(getProfileKey());
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      return deployMgr;
   }

   /**
    * Locate the given ComponentType with the given component name.
    *
    * @param mgtView -
    * @return the matching ManagedComponent if found, null otherwise
    * @throws Exception
    */
   protected ManagedComponent getManagedComponent(ManagementView mgtView, ComponentType type, String name)
      throws Exception
   {
      Set<ManagedComponent> comps = mgtView.getComponentsForType(type);
      ManagedComponent mc = null;
      for (ManagedComponent comp : comps)
      {
        String cname = comp.getName();
        if( cname.endsWith(name) )
        {
           mc = comp;
           break;
        }
      }
      return mc;
   }

   protected MetaValueFactory getMetaValueFactory()
   {
      if(metaValueFactory == null)
         metaValueFactory = MetaValueFactory.getInstance();
      return metaValueFactory;
   }
}
