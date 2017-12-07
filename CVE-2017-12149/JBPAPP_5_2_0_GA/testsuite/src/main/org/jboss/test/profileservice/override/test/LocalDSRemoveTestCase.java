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

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;

/**
 * Run before the LocalDSRemoveOverrideUnitTestCase
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class LocalDSRemoveTestCase extends AbstractProfileServiceTest
{

   public LocalDSRemoveTestCase(String name)
   {
      super(name);
   }
   
   public void test() throws Exception
   {
      
      String repositoryNames[] = deployPackage("profileservice-remove-ds.xml");
      assertNotNull(repositoryNames);

      ManagementView mgtView = getManagementView();
      
      //
      ComponentType locaDSType = new ComponentType("DataSource", "LocalTx");
      ManagedComponent test1 = mgtView.getComponent("ProfileServiceTestDataSource1", locaDSType);
      assertNotNull(test1);
      ManagedComponent remove = mgtView.getComponent("ProfileServiceTestRemoveDataSource", locaDSType);
      assertNotNull(remove);
      ManagedComponent test2 = mgtView.getComponent("ProfileServiceTestDataSource2", locaDSType);
      assertNotNull(test2);  
      // Remove
      mgtView.removeComponent(remove);
      mgtView.process();
      
      // Redeploy
      DeploymentProgress progress = getDeploymentManager().redeploy(test1.getDeployment().getName());
      progress.run();
      
      mgtView = getManagementView();
      remove = mgtView.getComponent("ProfileServiceTestRemoveDataSource", locaDSType);
      assertNull(remove);      
   }
   
   
}

