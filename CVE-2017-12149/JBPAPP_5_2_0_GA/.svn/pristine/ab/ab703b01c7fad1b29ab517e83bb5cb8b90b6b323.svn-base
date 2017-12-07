/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;

import org.jboss.deployers.plugins.main.MainDeployerImpl;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 *    
     <bean name="CheckSubDeploymentCompleteDeployer" class="org.jboss.deployment.CheckSubDeploymentCompleteDeployer">
         <property name="mainDeployer"><inject bean="MainDeployer"/></property>     
         <property name="types">.ear,.war</property>
      </bean>

 * @author bmaxwell
 *
 */

public class CheckSubDeploymentCompleteDeployer extends AbstractDeployer
{
   private MainDeployerImpl mainDeployer;
   private long version = new Date().getTime(); 
   
   // String passed in as property from bean configuration xml
   private String types;

   // array of types we will check, if no types specified we will check all types
   private String[] checkTypes = new String[0];   

   public void setMainDeployer(MainDeployerImpl md)
   {          
      mainDeployer = md;
   }

   public void create()
   {
      // set deployment stage that this deployer is interested in checking
      setStage(DeploymentStages.INSTALLED);
      
      if ( types != null )
      {
         checkTypes = types.split(",");
      }
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      // in the case where types is not specified we will check everything
      boolean requiresCheck = true;
      
      if (unit.getParent() != null)
      {
         return;
      }
      
      // check to see if this deployment is one of the types we want to check complete 
      for ( String type : checkTypes )
      {
         // Set to false because we have some types to test, if we don't match them then we will skip the check
         requiresCheck = false;
         if ( unit.getSimpleName().endsWith(type) ) 
         {
            requiresCheck = true;
            break;
         }
      }
            
      if( requiresCheck )
      {
         // for some reason unit can be null when test case deploys a test-jboss-beanx.xml defining CheckSubDeploymentCompleteDeployer
//         if ( mainDeployer == null )
//         {
//            System.out.println("CheckSubDeploymentCompleteDeployer: " + version + " maindeployer is null for " + unit.getName());
//            return;
//         }
//         else
//         {
//            System.out.println("CheckSubDeploymentCompleteDeployer: " + version + " maindeployer is not null for" + unit.getName());
//         }
         
         // this will throw a DeploymentException if it is not complete
         mainDeployer.checkComplete(unit.getName());
      }
      else
      {
         this.log.trace("Unit name " + unit.getSimpleName() + " does not end in one of these types: " + types);
      }      
   }

   public String getTypes()
   {
      return types;
   }

   public void setTypes(String types)
   {
      this.types = types;
   }
}
