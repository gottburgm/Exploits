/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployers;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.management.ObjectName;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.resource.metadata.DependsMetaData;
import org.jboss.resource.metadata.RARDeploymentMetaData;
import org.jboss.resource.metadata.JBossRAMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceInjectionValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * RARDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="vicky.kak@jboss.com">Vicky Kak</a>
 * @version $Revision: 85945 $
 */
public class RARDeployer extends AbstractSimpleRealDeployer<RARDeploymentMetaData>
{
   /** The work manager name */
   private String workManagerName;
   
   /** The xa terminator name */
   private String xaTerminatorName;
   
   /**
    * Create a new RARDeployer.
    */
   public RARDeployer()
   {
      super(RARDeploymentMetaData.class);
      setOutput(ServiceMetaData.class);
   }

   /**
    * Get the workManagerName.
    * 
    * @return the workManagerName.
    */
   public String getWorkManagerName()
   {
      return workManagerName;
   }

   /**
    * Set the workManagerName.
    * 
    * @param workManagerName the workManagerName.
    */
   public void setWorkManagerName(String workManagerName)
   {
      this.workManagerName = workManagerName;
   }

   /**
    * Get the XATerminatorName.
    * 
    * @return the xaTerminatorName.
    */
   public String getXATerminatorName()
   {
      return xaTerminatorName;
   }

   /**
    * Set the XATerminatorName.
    * 
    * @param xaTerminatorName the xaTerminatorName.
    */
   public void setXATerminatorName(String xaTerminatorName)
   {
      this.xaTerminatorName = xaTerminatorName;
   }

   protected String getObjectName(DeploymentUnit unit, RARDeploymentMetaData rdmd)
   {
      // TODO this is not needed 
      String name = unit.getSimpleName();
      unit = unit.getParent();
      while (unit != null)
      {
         name = unit.getSimpleName() + "#" + name;
         unit = unit.getParent();
      }
      return "jboss.jca:service=RARDeployment,name='" + name + "'";
   }
   
   public void deploy(DeploymentUnit unit, RARDeploymentMetaData rdmd) throws DeploymentException
   {
      try
      {
         ServiceMetaData rarDeployment = new ServiceMetaData();
         String name = getObjectName(unit, rdmd);
         ObjectName objectName = new ObjectName(name);
         rarDeployment.setObjectName(objectName);
         rarDeployment.setCode(RARDeployment.class.getName());
         ServiceConstructorMetaData constructor = new ServiceConstructorMetaData();
         constructor.setSignature(new String[] { RARDeploymentMetaData.class.getName() });
         constructor.setParameters(new Object[] { rdmd });
         rarDeployment.setConstructor(constructor);
         
         List<ServiceAttributeMetaData> attributes = new ArrayList<ServiceAttributeMetaData>();
         ServiceAttributeMetaData attribute = null;
         if (workManagerName != null)
         {
            attribute = new ServiceAttributeMetaData();
            attribute.setName("WorkManager");
            attribute.setValue(new ServiceInjectionValueMetaData(workManagerName));
            attributes.add(attribute);
         }
         if (xaTerminatorName != null)
         {
            attribute = new ServiceAttributeMetaData();
            attribute.setName("XATerminator");
            attribute.setValue(new ServiceInjectionValueMetaData(xaTerminatorName, "XATerminator"));
            attributes.add(attribute);
         }
         if (attributes.isEmpty() == false)
            rarDeployment.setAttributes(attributes);
         
         // We will have to see how to define the set of dependencies
         JBossRAMetaData jmd = rdmd.getRaXmlMetaData();
         if(jmd.getDependsMetaData() != null)
         {
        	 //String dependsObjectName = jmd.getDependsMetaData().getDependsName();
        	 List<ServiceDependencyMetaData> dependencies = new ArrayList<ServiceDependencyMetaData>();
        	         	 
        	 List<DependsMetaData> depends = jmd.getDependsMetaData();
        	 
        	 if(!depends.isEmpty())
        	 {
        		 Iterator<DependsMetaData> iter = depends.iterator();
        		 while(iter.hasNext())
        		 {
        			 DependsMetaData dmd = iter.next();
        			 String dependsObjectName = dmd.getDependsName();
        			 ServiceDependencyMetaData dependency = new ServiceDependencyMetaData();
        	         dependency.setIDependOn(dependsObjectName); 
        	         System.out.println(unit.getSimpleName()+ "  have got the dependency on ---->>>>> "+dependsObjectName);
        	         dependencies.add(dependency);
        		 }                 
        	 }        	 
	         rarDeployment.setDependencies(dependencies);
         }
          
         unit.addAttachment(ServiceMetaData.class, rarDeployment);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error creating rar deployment " + unit.getName(), e);
      }
   }

   public void undeploy(DeploymentUnit unit, RARDeploymentMetaData rdmd)
   {
	   /* To DO...
	   // Remove the contents from the MetaData repository here 
	   DeploymentUnit parent = unit.getParent();
	   String name = unit.getSimpleName();
	   if( parent != null )
		   name = parent.getSimpleName() + "#" + name;
	   metaDataRepository.removeConnectorMetaData(name, rdmd.getConnectorMetaData());
	   */
   }   
}
