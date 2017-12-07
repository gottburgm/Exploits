/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb.deployers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.MappedReferenceMetaDataResolverDeployer;
import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.ejb.Ejb2xMCContainer;
import org.jboss.ejb.EjbModule;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.ejb.jboss.ContainerConfigurationMetaData;
import org.jboss.metadata.ejb.jboss.InvokerProxyBindingMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceInjectionValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;

/**
 * A real deployer that translates JBossMetaData into ServiceMetaData for
 * the ejb module service mbeans.
 *
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 92554 $
 */
@JMX(name="jboss.ejb:service=EJBDeployer", exposedInterface=EjbDeployerMBean.class)
public class EjbDeployer extends AbstractSimpleVFSRealDeployer<JBossMetaData> implements EjbDeployerMBean
{
   /** */
   private String transactionManagerServiceName;
   /** Class loading web server name */
   private String webServiceName;
   /** The CachedConnectionManager service used by the CachedConnectionInterceptor */
   private String ccmServiceName;
   /** The ejb timer service */
   private String timerServiceName;

   private boolean callByValue;

   private String unauthenticatedIdentity = null;
   private String securityManagementName;
   private String securityContextClassName;
   private String defaultSecurityDomain;

   /** Verify EJB-jar contents on deployments */
   private boolean verifyDeployments;

   /** Enable verbose verification. */
   private boolean verifierVerbose;

   /** Enable strict verification: deploy JAR only if Verifier reports
    * no problems */
   private boolean strictVerifier;

   /**
    * Inject Policy Registration Bean Name
    */
   private String policyRegistrationName;

   /**
    * Create a new EjbDeployer.
    */
   public EjbDeployer()
   {
      super(JBossMetaData.class);
      setOutput(ServiceMetaData.class);
      setOutput(EjbDeployment.class);
      setOutput(KernelDeployment.class);
   }

   public String getTransactionManagerServiceName()
   {
      return transactionManagerServiceName;
   }

   public void setTransactionManagerServiceName(
         String transactionManagerServiceName)
   {
      this.transactionManagerServiceName = transactionManagerServiceName;
   }

   public String getWebServiceName()
   {
      return webServiceName;
   }

   public void setWebServiceName(String webServiceName)
   {
      this.webServiceName = webServiceName;
   }

   public String getCachedConnectionManagerName()
   {
      return ccmServiceName;
   }

   public void setCachedConnectionManagerName(String ccmServiceName)
   {
      this.ccmServiceName = ccmServiceName;
   }

   public String getTimerService()
   {
      return timerServiceName;
   }

   public void setTimerServiceName(String timerServiceName)
   {
      this.timerServiceName = timerServiceName;
   }


   public boolean isCallByValue()
   {
      return callByValue;
   }

   public void setCallByValue(boolean callByValue)
   {
      this.callByValue = callByValue;
   }

   /**
    * Obtain an unauthenticated identity
    *
    * @return the unauthenticated identity
    */
   public String getUnauthenticatedIdentity()
   {
      return unauthenticatedIdentity;
   }

   /**
    * Specify an unauthenticated identity
    * @param unauthenticatedIdentity
    */
   public void setUnauthenticatedIdentity(String unauthenticatedIdentity)
   {
      this.unauthenticatedIdentity = unauthenticatedIdentity;
   }

   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

   public void setSecurityManagementName(String sm)
   {
      this.securityManagementName = sm;
   }

   public void setSecurityContextClassName(String securityContextClassName)
   {
      this.securityContextClassName = securityContextClassName;
   }

   /**
    * Get the PolicyRegistration Name
    * @return
    */
   public String getPolicyRegistration()
   {
      return policyRegistrationName;
   }

   /**
    * Set the Policy Registration Bean Name
    * @param policyRegistration
    */
   public void setPolicyRegistrationName(String policyRegistration)
   {
      this.policyRegistrationName = policyRegistration;
   }

   @Override
   public void deploy(VFSDeploymentUnit unit, JBossMetaData deployment)
      throws DeploymentException
   {
      // If it is a deployment with ejbVersion unknown or 3
      if (!deployment.isEJB2x() && !deployment.isEJB1x())
         return; // let EJB3 deployer handle this

      ApplicationMetaData legacyMD = new ApplicationMetaData(deployment);

      if( verifyDeployments )
      {
         // we have a positive attitude
         boolean allOK = true;

         // wrapping this into a try - catch block to prevent errors in
         // verifier from stopping the deployment
         try
         {
            BeanVerifier verifier = new BeanVerifier();

            // add a listener so we can log the results
            verifier.addVerificationListener(new VerificationListener()
            {
               Logger verifierLog = Logger.getLogger(EjbDeployer.class, "verifier");

               public void beanChecked(VerificationEvent event)
               {
                  verifierLog.debug( "Bean checked: " + event.getMessage() );
               }

               public void specViolation(VerificationEvent event)
               {
                  verifierLog.warn( "EJB spec violation: " + (verifierVerbose ? event.getVerbose() : event.getMessage()));
               }
            });

            log.debug("Verifying " + unit.getRoot().toURL());
            verifier.verify(unit.getRoot().toURL(), legacyMD, unit.getClassLoader());

            allOK = verifier.getSuccess();
         }
         catch (Throwable t)
         {
            log.warn("Verify failed; continuing", t );
            allOK = false;
         }

         // If the verifier is in strict mode and an error/warning
         // was found in the Verification process, throw a Deployment
         // Exception
         if( strictVerifier && !allOK )
         {
            throw new DeploymentException("Verification of Enterprise Beans failed, see above for error messages.");
         }
      }

      ServiceMetaData ejbModule = new ServiceMetaData();
      ejbModule.setCode(EjbModule.class.getName());


      // Build an escaped JMX name including deployment shortname
      ObjectName moduleObjectName = null;
      try
      {
         moduleObjectName = this.getObjectName(unit, deployment);
      }
      catch(MalformedObjectNameException e)
      {
         throw new DeploymentException("Failed to create EJB module " + unit.getName() +
            ": malformed EjbModule name", e);
      }

      ejbModule.setObjectName(moduleObjectName);

      ServiceConstructorMetaData ctor = new ServiceConstructorMetaData();
      ctor.setSignature(
         new String[]{VFSDeploymentUnit.class.getName(), ApplicationMetaData.class.getName()}
      );
      ctor.setParameters(new Object[]{unit, legacyMD});
      ejbModule.setConstructor(ctor);

      // set attributes
      List<ServiceAttributeMetaData> attrs = new ArrayList<ServiceAttributeMetaData>();
      // Transaction manager
      ServiceAttributeMetaData attr = new ServiceAttributeMetaData();
      attr.setName("TransactionManagerFactory");
      ServiceDependencyValueMetaData dependencyValue = new ServiceDependencyValueMetaData();
      dependencyValue.setDependency(getTransactionManagerServiceName());
      dependencyValue.setProxyType("attribute");
      attr.setValue(dependencyValue);
      attrs.add(attr);
      // Security management
      attr = new ServiceAttributeMetaData();
      attr.setName("SecurityManagement");
      ServiceInjectionValueMetaData injectionValue = new ServiceInjectionValueMetaData(securityManagementName);
      attr.setValue(injectionValue);
      attrs.add(attr);
      //Policy Registration
      attr = new ServiceAttributeMetaData();
      attr.setName("PolicyRegistration");
      ServiceInjectionValueMetaData prInjectionValue = new ServiceInjectionValueMetaData(policyRegistrationName);
      attr.setValue(prInjectionValue);
      attrs.add(attr);
      // Add injection of the WebServiceName
      String wsName = getWebServiceName();
      if (wsName != null)
      {
         ServiceAttributeMetaData ws = new ServiceAttributeMetaData();
         ws.setName("WebServiceName");
         ServiceDependencyValueMetaData wsDepends = new ServiceDependencyValueMetaData();
         wsDepends.setDependency(wsName);
         ws.setValue(wsDepends);
         attrs.add(ws);
      }
      // Injection of the TimerService
      ServiceAttributeMetaData tms = new ServiceAttributeMetaData();
      ServiceDependencyValueMetaData tmsDepends = new ServiceDependencyValueMetaData();
      tms.setName("TimerService");
      tmsDepends.setDependency(timerServiceName);
      tmsDepends.setProxyType("attribute");
      tms.setValue(tmsDepends);
      attrs.add(tms);

      ejbModule.setAttributes(attrs);

      List<ServiceDependencyMetaData> dependencies = new ArrayList<ServiceDependencyMetaData>();
      // CCM for CachedConnectionInterceptor dependency
      // TODO: this should be injected directly to the interceptor
      if( ccmServiceName != null && ccmServiceName.length() > 0 )
      {
         ServiceDependencyMetaData ccm = new ServiceDependencyMetaData();
         ccm.setIDependOn(ccmServiceName);
         dependencies.add(ccm);
      }
      // Add dependencies on the invoker services in use
      JBossEnterpriseBeansMetaData beans = deployment.getEnterpriseBeans();
      Iterator<JBossEnterpriseBeanMetaData> beansIter = beans.iterator();
      HashSet<String> invokerNames = new HashSet<String>();
      HashSet<String> beanDepends = new HashSet<String>();
      // Process ContainerDependencyMetaData
      VFSDeploymentUnit topUnit = unit.getTopLevel();
      Map<String, ContainerDependencyMetaData> endpoints = (Map<String, ContainerDependencyMetaData>) topUnit.getAttachment(MappedReferenceMetaDataResolverDeployer.ENDPOINT_MAP_KEY);
      if(endpoints == null)
         log.warn(unit+" has no ContainerDependencyMetaData attachment");
      String vfsPath = unit.getRelativePath();
      ArrayList<BeanMetaData> mcBeanMD = new ArrayList<BeanMetaData>();
      while( beansIter.hasNext() )
      {
         JBossEnterpriseBeanMetaData bmd = beansIter.next();
         Set<String> depends = bmd.getDepends();
         if (depends != null)
            beanDepends.addAll(depends);
         String configName = bmd.getConfigurationName();
         ContainerConfigurationMetaData cmd = bmd.determineContainerConfiguration();
         Set<String> invokers = cmd.getInvokerProxyBindingNames();
         if(invokers != null)
         for(String iname : invokers)
         {
            InvokerProxyBindingMetaData imd = deployment.getInvokerProxyBinding(iname);
            if (imd == null)
               throw new DeploymentException("Failed to locate invoker: "+iname);
            String invokerName = imd.getInvokerMBean();
            if( invokerName.equalsIgnoreCase("default") )
            {
               // TODO: JBAS-4306 hack to ingore the invalid default invoker-mbean
               continue;
            }
            if( invokerNames.contains(invokerName) == false )
            {
               invokerNames.add(invokerName);
               ServiceDependencyMetaData invoker = new ServiceDependencyMetaData();
               invoker.setIDependOn(invokerName);
               dependencies.add(invoker);
            }
         }

         // Create mc beans that declare the container ejb jndi name supplies
         if(endpoints != null)
         {
            String ejbKey = "ejb/" + vfsPath + "#" + bmd.getEjbName();
            ContainerDependencyMetaData cdmd = endpoints.get(ejbKey);
            if(cdmd != null)
            {
               // Create the metadata for the bean to install
               String mcname = ejbKey + ",uid"+System.identityHashCode(bmd);
               BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(mcname, Ejb2xMCContainer.class.getName());
               for(String jndiName : cdmd.getJndiNames())
               {
                  String supplyName = "jndi:" + jndiName;
                  builder.addSupply(supplyName);
               }

               BeanMetaData mcbmd = builder.getBeanMetaData();
               log.info("installing bean: " + mcname);
               log.info("  with dependencies:");
               log.info("  and supplies:");
               for(SupplyMetaData smd : mcbmd.getSupplies())
               {
                  log.info("\t" + smd.getSupply());
               }
               mcBeanMD.add(mcbmd);
            }
         }
      }

      // Add any declared dependencies
      /* TODO: this is too coarse as bean to bean depends are being pulled up to the module level
         and an ejb module cannot depend on the beans it creates. The ejb deployer needs to
         be refactored into a component based deployer.
      */
      try
      {
         for(String depend : beanDepends)
         {
            ObjectName n = new ObjectName(depend);
            ServiceDependencyMetaData sdmd = new ServiceDependencyMetaData();
            sdmd.setIDependOn(n.toString());
            dependencies.add(sdmd);
         }
      }
      catch(MalformedObjectNameException e)
      {
         throw new DeploymentException(e);
      }
      ejbModule.setDependencies(dependencies);

      unit.addAttachment("EjbServiceMetaData", ejbModule, ServiceMetaData.class);
      // Create a kernel deployment for the module mc beans
      AbstractKernelDeployment akd = new AbstractKernelDeployment();
      akd.setName(ejbModule.getObjectName().getCanonicalName()+"Beans");
      akd.setBeans(mcBeanMD);
      unit.addAttachment(KernelDeployment.class, akd);

      // Pass the ejb callByValue setting
      if (callByValue)
         unit.addAttachment("EJB.callByValue", Boolean.TRUE, Boolean.class);
      //Pass the unauthenticated identity
      if(this.unauthenticatedIdentity != null)
         unit.addAttachment("EJB.unauthenticatedIdentity", this.unauthenticatedIdentity, String.class);
      //Pass the SecurityContextClassName
      if(this.securityContextClassName != null)
         unit.addAttachment("EJB.securityContextClassName", securityContextClassName, String.class);
      //Pass the Default SecurityDomain
      if(this.defaultSecurityDomain != null)
         unit.addAttachment("EJB.defaultSecurityDomain", defaultSecurityDomain, String.class);
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit, JBossMetaData deployment)
   {
   }

   /**
    * Get the object name of the ServiceMetaData instance associated with
    * the EjbModule. This uses the pattern:
    * "jboss.j2ee:service=EjbModule,module="+unit.getSimpleName()
    *
    * @param unit the deployment unit
    * @param metaData - the ejb jar metaData
    * @return "jboss.j2ee:service=EjbModule,module="+unit.getSimpleName()
    * @throws MalformedObjectNameException
    */
   protected ObjectName getObjectName(VFSDeploymentUnit unit, JBossMetaData metaData)
      throws MalformedObjectNameException
   {
      String name = metaData.getJmxName();
      if( name == null )
      {
         String unitShortName = unit.getName();
         if (unitShortName.endsWith("/"))
         {
            unitShortName = unitShortName.substring(0, unitShortName.length() - 1);
         }

         if(unitShortName.endsWith("!"))
         {
            unitShortName = unitShortName.substring(0, unitShortName.length() - 1);
         }

         unitShortName = unitShortName.substring(unitShortName.lastIndexOf("/") + 1);
         //
         unitShortName = ObjectName.quote(unitShortName);
         name = EjbModule.BASE_EJB_MODULE_NAME + ",module=" + unitShortName + ",uid=" + System.identityHashCode(metaData);
      }

      return new ObjectName(name);
   }

	public boolean getStrictVerifier()
	{
		return strictVerifier;
	}

	public boolean getVerifierVerbose()
	{
		return verifierVerbose;
	}

	public boolean getVerifyDeployments()
	{
		return verifyDeployments;
	}

	public void setStrictVerifier(boolean strictVerifier)
	{
		this.strictVerifier = strictVerifier;
	}

	public void setVerifierVerbose(boolean verbose)
	{
		this.verifierVerbose = verbose;
	}

	public void setVerifyDeployments(boolean verify)
	{
		this.verifyDeployments = verify;
	}
}
