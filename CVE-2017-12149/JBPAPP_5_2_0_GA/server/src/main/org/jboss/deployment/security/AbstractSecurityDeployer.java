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
package org.jboss.deployment.security;

import java.util.ArrayList;

import javax.management.ObjectName;
import javax.xml.bind.JAXBElement;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.javaee.support.IdMetaData;
import org.jboss.security.acl.config.ACLConfiguration;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * Abstract Security Deployer Sets up the top level JaccPolicy service beans
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Feb 18, 2008
 * @version $Revision: 86126 $
 */
public abstract class AbstractSecurityDeployer<T extends IdMetaData> extends AbstractDeployer
{
   private static final String JACC_ATTACHMENT_NAME = "jboss.jacc";

   private static final String BASE_OBJECT_NAME = "jboss.jacc:service=jacc,id=";

   /** Attachment name for the JAXB model for xacml config - match the one from XacmlConfigParsingDeployer */
   private static final String XACML_ATTACHMENT_NAME = "xacml.config";  

   private PolicyRegistration policyRegistration;

   /**
    * 
    * @return
    */
   public PolicyRegistration getPolicyRegistration()
   {
      return this.policyRegistration;
   }

   /**
    * 
    * @param policyRegistration
    */
   public void setPolicyRegistration(PolicyRegistration policyRegistration)
   {
      this.policyRegistration = policyRegistration;
   }

   public AbstractSecurityDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      setInput(getMetaDataClassType());
      setOutput(ServiceMetaData.class);
      addOutput(JACC_ATTACHMENT_NAME);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      T metaData = unit.getAttachment(getMetaDataClassType());

      if (metaData == null)
         return;

      String contextId = unit.getSimpleName();

      // Is the war the top level deployment?
      // DeploymentUnit topUnit = unit.getTopLevel();
      if (unit.getParent() == null || getParentJaccPolicyBean(unit) == null)
      {
         createTopLevelServiceBeanWithMetaData(contextId, unit, metaData);
      }
      else
      {
         ServiceMetaData subjaccPolicy = getServiceMetaData();

         String deploymentName = unit.getSimpleName();

         try
         {
            subjaccPolicy.setObjectName(new ObjectName(getObjectName(unit)));
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         // Provide a constructor for the service bean
         ServiceConstructorMetaData serviceConstructor = new ServiceConstructorMetaData();
         serviceConstructor.setSignature(new String[]{String.class.getName(), getMetaDataClassType().getName()});
         serviceConstructor.setParameters(new Object[]{deploymentName, metaData});
         subjaccPolicy.setConstructor(serviceConstructor);

         ArrayList<ServiceMetaData> services = new ArrayList<ServiceMetaData>();
         services.add(subjaccPolicy);
         unit.addAttachment(JACC_ATTACHMENT_NAME, subjaccPolicy, ServiceMetaData.class);

         // Add a dependence into the parent JaccPolicy
         ServiceMetaData parentServiceMetaData = this.getParentJaccPolicyBean(unit);
         if (parentServiceMetaData != null)
         {
            ServiceDependencyMetaData serviceDependencyMetaData = new ServiceDependencyMetaData();
            serviceDependencyMetaData.setIDependOnObjectName(subjaccPolicy.getObjectName());
            parentServiceMetaData.addDependency(serviceDependencyMetaData);

            // Add an attribute in the parent service
            ServiceAttributeMetaData serviceAttributeMetaData = new ServiceAttributeMetaData();
            serviceAttributeMetaData.setName("PolicyConfigurationFacadeMBean");
            ServiceDependencyValueMetaData dependencyValue = new ServiceDependencyValueMetaData();
            dependencyValue.setDependency(subjaccPolicy.getObjectName().toString());
            dependencyValue.setProxyType("attribute");
            serviceAttributeMetaData.setValue(dependencyValue);
            parentServiceMetaData.addAttribute(serviceAttributeMetaData);
         }
      }
      
      /** Register XACML/ACL policies if present in the deployment */
      if(this.policyRegistration != null)
      {
         String xacmlType = PolicyRegistration.XACML;
         JAXBElement<?> policyConfig = (JAXBElement<?>) unit.getAttachment(XACML_ATTACHMENT_NAME);
         if(policyConfig != null)
            this.policyRegistration.registerPolicyConfig(contextId, xacmlType, policyConfig);
         
         String aclType = PolicyRegistration.ACL;
         ACLConfiguration aclConfig = (ACLConfiguration) unit.getAttachment(ACLConfiguration.class.getName());
         if(aclConfig != null)
            this.policyRegistration.registerPolicyConfig(contextId, aclType, aclConfig);   
      } 
   }

   @Override
   public void undeploy(DeploymentUnit unit)
   {
      unit.removeAttachment(JACC_ATTACHMENT_NAME);
      // unregister any XACML or ACL policies associated with the deployment unit.
      String contextId = unit.getSimpleName();
      if (this.policyRegistration != null)
      {
         this.policyRegistration.deRegisterPolicy(contextId, PolicyRegistration.XACML);
         this.policyRegistration.deRegisterPolicy(contextId, PolicyRegistration.ACL);
      }
   }

   private void createTopLevelServiceBeanWithMetaData(String contextId, DeploymentUnit unit, T deployment)
   {
      // Provide a constructor for the service bean
      ServiceConstructorMetaData serviceConstructor = new ServiceConstructorMetaData();
      serviceConstructor.setSignature(new String[]{String.class.getName(), this.getMetaDataClassType().getName(),
            Boolean.class.getName()});
      serviceConstructor.setParameters(new Object[]{contextId, deployment, Boolean.TRUE});
      createJaccPolicyBean(serviceConstructor, unit);
   }

   private void createJaccPolicyBean(ServiceConstructorMetaData serviceConstructor, DeploymentUnit unit)
   {
      // Create a Service Bean for the JACC Policy
      ServiceMetaData jaccPolicy = new ServiceMetaData();
      jaccPolicy.setCode(getJaccPolicyName());
      try
      {
         jaccPolicy.setObjectName(new ObjectName(getObjectName(unit)));
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      // Provide a constructor for the service bean
      jaccPolicy.setConstructor(serviceConstructor);
      ArrayList<ServiceMetaData> services = new ArrayList<ServiceMetaData>();
      services.add(jaccPolicy);

      unit.addAttachment(JACC_ATTACHMENT_NAME, jaccPolicy, ServiceMetaData.class);
   }

   private ServiceMetaData getParentJaccPolicyBean(DeploymentUnit childDU)
   {
      DeploymentUnit parentDU = childDU.getParent();

      while (parentDU != null)
      {
         ServiceMetaData parentJacc = (ServiceMetaData) parentDU.getAttachment(JACC_ATTACHMENT_NAME);
         if (parentJacc != null)
            return parentJacc;
         parentDU = parentDU.getParent();
      }
      return null;
   }

   /**
    * Get the name of the JaccPolicy subclass (EjbJaccPolicy,WebJaccPolicy etc)
    * 
    * @return
    */
   protected abstract String getJaccPolicyName();

   /**
    * Return the type of metadata
    * 
    * @return
    */
   protected abstract Class<T> getMetaDataClassType();

   /**
    * Get the top level service bean meta data
    * 
    * @return
    */
   protected abstract ServiceMetaData getServiceMetaData();

   /**
    * Qualify the object name with parent name just to avoid conflicts with deployments with the same name in multiple
    * archives
    */
   private String getObjectName(DeploymentUnit unit)
   {
      String deploymentName = unit.getName();
      DeploymentUnit parentDU = unit.getParent();
      String parentDeploymentName = parentDU != null ? ",parent=\"" + parentDU.getSimpleName() + "\"" : "";
      return BASE_OBJECT_NAME + "\"" + deploymentName + "\"" + parentDeploymentName;
   }
}
