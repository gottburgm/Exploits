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
package org.jboss.test.profileservice.override.restart.test;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.RunState;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.test.profileservice.override.test.AbstractProfileServiceTest;

/**
 * Test if the changes to the ManagedComponent from the ProfileServiceOverrideTestCase
 * were persisted and restored correctly after restarting AS.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 90193 $
 */
public class PersistedDataSourceUnitTestCase extends AbstractProfileServiceTest
{
  
   public PersistedDataSourceUnitTestCase(String name)
   {
      super(name);
   }

   public void testDS() throws Exception
   {
      String deploymentName = "profileservice-test-ds.xml";
      
      try
      {
         // No deployment after restart
//         deployPackage(deploymentName);
         
         ManagementView mgtView = getManagementView();
         ManagedDeployment md = mgtView.getDeployment(deploymentName);
         assertNotNull(md);
         
         ManagedComponent mc = md.getComponent("ChangedDsJNDIName");
         assertNotNull(mc);
         
         ManagedProperty property = mc.getProperty("prepared-statement-cache-size");
         assertEquals("prepared-statement-cache-size: "+ property.getValue(), SimpleValueSupport.wrap(34), property.getValue());
         
         property = mc.getProperty("max-pool-size");
         assertEquals("max-pool-size: "+ property.getValue(), SimpleValueSupport.wrap(34), property.getValue());

         // This should work too
         ManagedComponent comp = getManagedComponent(mgtView, "ChangedDsJNDIName");
         assertNotNull(comp);
         
         ManagedComponent mc2 = getManagedComponent(mgtView, "ProfileServiceTestDS");
         assertNull(mc2);
         
         // Check run state
         assertEquals("DS is running", RunState.RUNNING, mc.getRunState());
         
      }
      catch(Exception e)
      {
         log.error(e);
         throw e;
      }
      finally
      {
         undeployPackage(new String[] { deploymentName });
      }
   }
   
   public void testUpdatedDSTemplate() throws Exception
   {
      String deploymentName = "LocalTestDS-ds.xml";
      try
      {
         ManagementView mgtView = getManagementView();
         ManagedComponent mc = mgtView.getComponent("TestLocalDS", KnownComponentTypes.DataSourceTypes.LocalTx.getType());
         assertNotNull(mc);
         
         ManagedProperty property = mc.getProperty("prepared-statement-cache-size");
         assertEquals("prepared-statement-cache-size: "+ property.getValue(), SimpleValueSupport.wrap(34), property.getValue());
         
         property = mc.getProperty("max-pool-size");
         assertEquals("max-pool-size: "+ property.getValue(), SimpleValueSupport.wrap(34), property.getValue());

      }
      finally
      {
         undeployPackage(new String[] { deploymentName });
      }
      
   }
   
   public void testNestedDS() throws Exception
   {
      String deploymentName = "profileservice-datasource.ear";
      try
      {
//         deployPackage(deploymentName);
         ManagementView mgtView = getManagementView();
         ManagedDeployment deployment = mgtView.getDeployment(deploymentName);
         assertNotNull(deployment);
         
         assertNotNull(deployment.getChildren());
         assertFalse(deployment.getChildren().isEmpty());
         
         // Check first dataSource
         // get test-ds.xml child
         ManagedDeployment md = null;
         for(ManagedDeployment d : deployment.getChildren())
         {
            if(d.getName().endsWith("test-ds.xml"))
            {
               md = d;
               break;
            }
         }
         assertNotNull(md);
         
         ManagedComponent mc = md.getComponent("ChangedNestedDsJNDIName");
         assertNotNull("test-ds.xml", mc);
         ManagedProperty property = mc.getProperty("prepared-statement-cache-size");
         assertEquals("prepared-statement-cache-size: "+ property.getValue(), SimpleValueSupport.wrap(34), property.getValue());
         
         // This should work too
         ManagedComponent comp = getManagedComponent(mgtView, "ChangedNestedDsJNDIName");
         assertNotNull(comp);
         
         ManagedComponent comp3 = getManagedComponent(mgtView, "ProfileServiceNestedTestDS");
         assertNull(comp3);
         
         // Check 2nd dataSource
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
         assertNotNull("test-second-ds.xml", mc);

         // prepared-statement-cache-size
         property = mc.getProperty("prepared-statement-cache-size");
         assertEquals(property.getValue(), SimpleValueSupport.wrap(33));
         // max-pool-size
         property = mc.getProperty("max-pool-size");
         assertEquals(property.getValue(), SimpleValueSupport.wrap(19));
         
      }
      catch(Exception e)
      {
         log.error(e);
         throw e;
      }
      finally
      {
          undeployPackage(new String[] { deploymentName });
      }
   }
}

