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

import java.util.Set;

import org.jboss.deployers.spi.management.KnownDeploymentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ManagedDeployment;

/**
 * Test ManagementView operations.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
public class ManagementViewUnitTestCase extends AbstractProfileServiceTest
{

   public ManagementViewUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test that all for deploymentNames of a KnownDeploymentType
    * can be resolved by the ManagementView. 
    * 
    */
   public void testKnownDeploymentTypes() throws Exception
   {
      ManagementView mgtView = getManagementView();
      
      for(KnownDeploymentTypes type : KnownDeploymentTypes.values())
      {
         log.debug("----- deployment type: " + type);
         
         Set<String> deploymentNames = mgtView.getDeploymentNamesForType(type.getType());
         if(deploymentNames != null)
         {
            for(String deploymentName : deploymentNames)
            {
               ManagedDeployment deployment = mgtView.getDeployment(deploymentName);
               log.debug(deployment.getSimpleName());
            }
         }
      }
   }
}
