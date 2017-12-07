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

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.test.profileservice.override.test.AbstractProfileServiceTest;

/**
 * Run after LocalDSRemoveTestCase to validate that the removal of the
 * ProfileServiceTestRemoveDataSource has occurred.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class LocalDSRemoveOverrideUnitTestCase extends AbstractProfileServiceTest
{

   public LocalDSRemoveOverrideUnitTestCase(String name)
   {
      super(name);
   }

   public void test() throws Exception
   {
      try
      {
         ManagementView mgtView = getManagementView();
         
         //
         ComponentType locaDSType = new ComponentType("DataSource", "LocalTx");
         ManagedComponent test1 = mgtView.getComponent("ProfileServiceTestDataSource1", locaDSType);
         assertNotNull(test1);
         ManagedComponent remove = mgtView.getComponent("ProfileServiceTestRemoveDataSource", locaDSType);
         assertNull(remove);
         ManagedComponent test2 = mgtView.getComponent("ProfileServiceTestDataSource2", locaDSType);
         assertNotNull(test2);  
      }
      finally
      {
         // Undeploy
         undeployPackage(new String[] { "profileservice-remove-ds.xml" });         
      }
   }
   
}

