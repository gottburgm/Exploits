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
package org.jboss.deployment;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;

//$Id: EARInitializingDeployer.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  EAR Deployer that can be used for initialization
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @author adrian@jboss.org
 *  @since  Dec 6, 2006 
 *  @version $Revision: 85945 $
 */
public class EARInitializingDeployer extends AbstractSimpleRealDeployer<JBossAppMetaData>
{    
   /**
    * Create a new EARInitializingDeployer.
    */
   public EARInitializingDeployer()
   {
      super(JBossAppMetaData.class);
      setOutput(PolicyConfiguration.class);
   } 

   @Override
   public void deploy(DeploymentUnit unit, JBossAppMetaData deployment) throws DeploymentException
   { 
      //Perform JACC Policy Configuration
      String contextID =  shortNameFromDeploymentName(unit.getSimpleName()); 
      PolicyConfigurationFactory pcFactory = null;
      try
      {
         pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, true);
         unit.addAttachment(PolicyConfiguration.class, pc);
      } 
      catch (PolicyContextException e)
      { 
         throw new DeploymentException("PolicyContextException generated in deploy", e);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Exception generated in deploy", e);
      }
      
   }

   @Override
   public void undeploy(DeploymentUnit unit, JBossAppMetaData deployment)
   {  
      //Perform JACC cleanup for the EAR
      unit.removeAttachment(PolicyConfiguration.class);
   } 
   
   
   /**
    * A utility method that takes a deployment unit name and strips it down to the base ear
    * name without the .ear suffix.
    * @param name - the DeploymentUnit name.
    * @return the short name
    */
   public static String shortNameFromDeploymentName(String name)
   {
      String shortName = name.trim();
      String[] parts = name.split("/|\\.|\\!");
      if( parts.length > 1 )
      {
         // If it ends in .war, use the previous part
         if( parts[parts.length-1].equals("ear") )
            shortName = parts[parts.length-2];
         // else use the last part
         else
            shortName = parts[parts.length-1];
      }
      return shortName;
   }
}
