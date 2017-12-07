/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb3;

import org.jboss.as.javaee.SimpleJavaEEModuleInformer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.test.JBossTestCase;

/**
 * @author bmaxwell
 *
 */
public class JBPAPP7128UnitTestCase extends JBossTestCase
{
   private DeploymentUnit topLevelDeploymentUnit;
   private DeploymentUnit nonTopLevelDeploymentUnit;

   public JBPAPP7128UnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {    
      super.setUp();
      
      String name = "TopLevelDeploymentUnitName";
      String simpleName = "TopLevelDeploymentUnitNameSimpleName";
      String relativePath = "TopLevelDeploymentUnitNameRelativePath";
           
      nonTopLevelDeploymentUnit = new AbstractDeploymentUnit(new AbstractDeploymentContext("non"+name, "non"+simpleName, "non"+relativePath));
      
      topLevelDeploymentUnit = new AbstractDeploymentUnit(new AbstractDeploymentContext(name, simpleName, relativePath));
      topLevelDeploymentUnit.addAttachment(JBossAppMetaData.class, new JBossAppMetaData());                     
   }
   
   public void testGetModulePathTopLevelDeploymentUnit()
   {               
      String modulePath = new SimpleJavaEEModuleInformer().getModulePath(topLevelDeploymentUnit);
            
      if(!modulePath.equals(topLevelDeploymentUnit.getRelativePath()))
         fail("SimpleJavaEEModuleInformer.getModulePath(DeploymentInfo) should return DeploymentInfo.getRelativePath()="+ topLevelDeploymentUnit.getRelativePath() + " for top level deployment not unit.getSimpleName(), modulePath="+ modulePath);
   }
   
   public void testGetModulePathNonTopLevelDeploymentUnit()
   {      
      String modulePath = new SimpleJavaEEModuleInformer().getModulePath(nonTopLevelDeploymentUnit);
      
      if(!modulePath.equals(nonTopLevelDeploymentUnit.getSimpleName()))
         fail("SimpleJavaEEModuleInformer.getModulePath(DeploymentInfo) should return DeploymentInfo.getSimpleName()="+ nonTopLevelDeploymentUnit.getSimpleName() +" not unit.getRelativePath(), modulePath=" + modulePath);            
   }
}
