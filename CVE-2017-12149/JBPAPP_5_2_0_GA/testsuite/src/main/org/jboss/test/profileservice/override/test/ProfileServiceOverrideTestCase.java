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
package org.jboss.test.profileservice.override.test;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * Update a DS managed component without undeploying it.
 * The {@see PersistedDataSourceUnitTestCase} will test if the changes were restored
 * correctly after restarting AS.  
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86164 $
 */
public class ProfileServiceOverrideTestCase extends AbstractProfileServiceTest
{
   
   public ProfileServiceOverrideTestCase(String name)
   {
      super(name);
   }

   public void testDS() throws Exception
   {
      String deploymentName = "profileservice-test-ds.xml";
      
      try
      {
         deployPackage(deploymentName);
         
         ManagementView mgtView = getManagementView();
         ManagedDeployment md = mgtView.getDeployment(deploymentName);
         assertNotNull("Null managed deployment.", md);
         
         ManagedComponent mc = md.getComponent("ProfileServiceTestDS");
         assertNotNull("Null managed component", mc);
         
         // This should work too
         ManagedComponent comp = getManagedComponent(mgtView, "ProfileServiceTestDS");
         assertNotNull(comp);
         
         ManagedProperty jndiName = mc.getProperty("jndi-name");
         // assert
         assertEquals(jndiName.getValue(), SimpleValueSupport.wrap("ProfileServiceTestDS"));
         // change value
         jndiName.setValue(SimpleValueSupport.wrap("ChangedDsJNDIName"));
         
         
         ManagedProperty property = mc.getProperty("prepared-statement-cache-size");
         // assert
         assertEquals("prepared-statement-cache-size: "+ property.getValue(), SimpleValueSupport.wrap(32), property.getValue());
         // change value
         property.setValue(SimpleValueSupport.wrap(34));
         
         property = mc.getProperty("max-pool-size");
         property.setValue(SimpleValueSupport.wrap(34));
         
         
         ManagedProperty configProps = mc.getProperty("connection-properties");
         assertNotNull(configProps);
         CompositeValue collection = (CompositeValue) configProps.getValue();
         configProps.setValue(collection);
         
         
         // update component
         mgtView.updateComponent(mc);
         
         
         // See if the changes are reflected in the managedView after a reload
         mgtView = getManagementView();
         
         ManagedDeployment md2 = mgtView.getDeployment(deploymentName);
         assertNotNull(md2);
         
         ManagedComponent mc2 = md2.getComponent("ChangedDsJNDIName");
         assertNotNull(mc2);
         
         ManagedProperty changedProperty = mc2.getProperty("prepared-statement-cache-size");
         assertEquals(changedProperty.getValue(), SimpleValueSupport.wrap(34));
         
         mc2 = md2.getComponent("ProfileServiceTestDS");
         assertNull(mc2);
         
         mc2 = getManagedComponent(mgtView, "ProfileServiceTestDS");
         assertNull(mc2);
      }
      catch(Exception e)
      {
         log.error(e);
         undeployPackage(new String[] { deploymentName });
         throw e;
      }
      finally
      {
         // Do not undeploy deployment, if everything went ok
         // undeployPackage(new String[] { deploymentName });
      }
   }
   
   public void testTemplateDS() throws Exception
   {
      String jndiName = "TestLocalDS";
      
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      propValues.put("driver-class", SimpleValueSupport.wrap("org.hsqldb.jdbcDriver"));
      propValues.put("connection-url", SimpleValueSupport.wrap("jdbc:hsqldb:."));
      propValues.put("connection-definition", SimpleValueSupport.wrap("javax.sql.DataSource"));
      propValues.put("jndi-name", SimpleValueSupport.wrap(jndiName));
      propValues.put("rar-name", SimpleValueSupport.wrap("jboss-local-jdbc.rar"));
      
      // The management view
      ManagementView mgtView = getManagementView();
      DeploymentTemplateInfo dsInfo = mgtView.getTemplate("LocalTxDataSourceTemplate");
      assertNotNull(dsInfo);
      // 
      for(ManagedProperty property : dsInfo.getProperties().values())
      {
         MetaValue v = propValues.get(property.getName());
         if(v != null)
         {
            property.setValue(v);
            property.setField(Fields.META_TYPE, v.getMetaType());
         }
      }
      //
      mgtView.applyTemplate("LocalTestDS", dsInfo);
      
      // Check 
      ManagedDeployment md = mgtView.getDeployment("LocalTestDS-ds.xml");
      assertNotNull(md);
      ManagedComponent mc = mgtView.getComponent(jndiName, KnownComponentTypes.DataSourceTypes.LocalTx.getType());
      assertNotNull(mc);
      
      ManagedProperty property = mc.getProperty("prepared-statement-cache-size");
      // change value
      property.setValue(SimpleValueSupport.wrap(34));
      
      property = mc.getProperty("max-pool-size");
      property.setValue(SimpleValueSupport.wrap(34));
      
      mgtView.updateComponent(mc);
     
   }
   
   
   public void testNestedDS() throws Exception
   {
      String deploymentName = "profileservice-datasource.ear";
      try
      {
         deployPackage(deploymentName);
         ManagementView mgtView = getManagementView();
         ManagedDeployment deployment = mgtView.getDeployment(deploymentName);
         assertNotNull("Null managed deployment", deployment);
         
         assertNotNull(deployment.getChildren());
         assertFalse(deployment.getChildren().isEmpty());

         // Update first DataSource
         // get test-ds.xml child
         ManagedDeployment md =  null;
         for(ManagedDeployment d : deployment.getChildren())
         {
            if(d.getName().endsWith("test-ds.xml"))
            {
               md = d;
               break;
            }
         }
         assertNotNull(md);
         
         ManagedComponent mc = md.getComponent("ProfileServiceNestedTestDS");
         assertNotNull("Null managed component", mc);
         
         // This should work too
         ManagedComponent comp = getManagedComponent(mgtView, "ProfileServiceNestedTestDS");
         assertNotNull(comp);
         
         ManagedProperty p = mc.getProperty("jndi-name");
         p.setValue(SimpleValueSupport.wrap("ChangedNestedDsJNDIName"));
         
         // Update prepared-statement-cache-size
         ManagedProperty property = mc.getProperty("prepared-statement-cache-size");
         // assert
         assertEquals("prepared-statement-cache-size: "+ property.getValue(), SimpleValueSupport.wrap(32), property.getValue());
         // change value
         property.setValue(SimpleValueSupport.wrap(34));
         
         // updateComponent and process()
         mgtView.updateComponent(mc);
         
         //
         // Update 2nd DataSource
         md = null;
         for(ManagedDeployment d : deployment.getChildren())
         {
            if(d.getName().endsWith("test-second-ds.xml"))
            {
               md = d;
               break;
            }
         }
         assertNotNull(md);
         mc = md.getComponent("OtherNestedTestDS");
         
         // prepared-statement-cache-size
         property = mc.getProperty("prepared-statement-cache-size");
         // assert
         assertEquals(property.getValue(), SimpleValueSupport.wrap(12));
         // change
         property.setValue(SimpleValueSupport.wrap(33));
         
         // max-pool-size
         property = mc.getProperty("max-pool-size");
         // assert
         assertEquals(property.getValue(), SimpleValueSupport.wrap(22));
         // change
         property.setValue(SimpleValueSupport.wrap(19));
         
         // updateComponent and process()
         mgtView.updateComponent(mc);
       
         // See if the changes are reflected in the managedView after a reload
         mgtView = getManagementView();

         ManagedComponent comp2 = getManagedComponent(mgtView, "ChangedNestedDsJNDIName");
         assertNotNull(comp2);
         
         ManagedComponent comp3 = getManagedComponent(mgtView, "ProfileServiceNestedTestDS");
         assertNull(comp3);

      }
      catch(Exception e)
      {
         log.error(e);
         undeployPackage(new String[] { deploymentName });
         throw e;
      }
      finally
      {
         // Do not undeploy deployment, if everything went ok
         // undeployPackage(new String[] { deploymentName });
      }
   }
}
