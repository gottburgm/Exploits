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
package org.jboss.resource.deployers.builder;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.TxConnectionManager;
import org.jboss.resource.metadata.mcf.DataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryTransactionSupportMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceInjectionValueMetaData;

/**
 * A ConnectionManagerBuilder.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 111895 $
 */
public class ConnectionManagerBuilder extends AbstractBuilder
{
   private static final String CCM_JMX = "jboss.jca:service=CachedConnectionManager";
   private static final String JAAS_JMX = "jboss.security:service=JaasSecurityManager";
   private static final String TM_JMX = "jboss:service=TransactionManager";
   private static final String POOL_JMX = "jboss.jca:service=ManagedConnectionPool,name=";
   
   private static final String SUBJECT_FACTORY_NAME = "JBossSecuritySubjectFactory";

   private String ccmJMXName = CCM_JMX;
   private String jaasJMXName = JAAS_JMX;
   private String jaasJndiBeanName = "JBossSecurityJNDIContextEstablishment";
   private String subjectFactoryName = SUBJECT_FACTORY_NAME;
   private String tmJMXName = TM_JMX;
   private String poolJMXPrefix = POOL_JMX;

   
   public String getCcmJMXName()
   {
      return ccmJMXName;
   }
   public void setCcmJMXName(String ccmJMXName)
   {
      this.ccmJMXName = ccmJMXName;
   }

   public String getJaasJMXName()
   {
      return jaasJMXName;
   }
   public void setJaasJMXName(String jaasJMXName)
   {
      this.jaasJMXName = jaasJMXName;
   }

   public String getJaasJndiBeanName()
   {
      return jaasJndiBeanName;
   }
   public void setJaasJndiBeanName(String jaasJndiBeanName)
   {
      this.jaasJndiBeanName = jaasJndiBeanName;
   }
   
   /**
    * Get the Security Subject Factory Name
    * @return
    */
   public String getSubjectFactoryName()
   {
      return subjectFactoryName;
   }
   
   /**
    * Set the Security Subject Factory Name
    * @param subjectFactoryName
    */
   public void setSubjectFactoryName(String subjectFactoryName)
   {
      this.subjectFactoryName = subjectFactoryName;
   }
   
   public String getTmJMXName()
   {
      return tmJMXName;
   }
   public void setTmJMXName(String tmJMXName)
   {
      this.tmJMXName = tmJMXName;
   }

   public String getPoolJMXPrefix()
   {
      return poolJMXPrefix;
   }
   public void setPoolJMXPrefix(String poolJMXPrefix)
   {
      this.poolJMXPrefix = poolJMXPrefix;
   }

   @Override
   public List<ServiceAttributeMetaData> buildAttributes(ManagedConnectionFactoryDeploymentMetaData md)
   {
      List<ServiceAttributeMetaData> attributes = new ArrayList<ServiceAttributeMetaData>();
      ServiceAttributeMetaData attribute = null;
      
      attribute = buildSimpleAttribute("JndiName", md.getJndiName());
      attributes.add(attribute);
      
      if (md.getSecurityMetaData() != null && 
          md.getSecurityMetaData().getDomain() != null &&
          !md.getSecurityMetaData().getDomain().equals(""))
      {
         attribute = buildSimpleAttribute("SecurityDomainJndiName", md.getSecurityMetaData().getDomain());
         attributes.add(attribute);
      }

      attribute = buildSimpleAttribute("AllocationRetry", String.valueOf(md.getAllocationRetry()));
      attributes.add(attribute);
            
      attribute = buildSimpleAttribute("AllocationRetryWaitMillis", String.valueOf(md.getAllocationRetryWaitMillis()));
      attributes.add(attribute);
            
      attribute = new ServiceAttributeMetaData();
      attribute.setName("SubjectFactory");
      ServiceInjectionValueMetaData injectionValue = new ServiceInjectionValueMetaData(subjectFactoryName);
      attribute.setValue(injectionValue);      
      attributes.add(attribute);
       

      attribute = buildDependencyAttribute("CachedConnectionManager", ccmJMXName);      
      attributes.add(attribute);
      
      attribute = buildDependencyAttribute("JaasSecurityManagerService", jaasJMXName);
      attributes.add(attribute);

      attribute = buildDependencyAttribute("ManagedConnectionPool", poolJMXPrefix + md.getJndiName());
      attributes.add(attribute);
      
      if(!md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.NONE))
      {
         attribute = buildDependencyAttribute("TransactionManagerService", tmJMXName);
         attributes.add(attribute);
         
         attribute = buildSimpleAttribute("LocalTransactions", String.valueOf(md.getLocalTransactions()));
         attributes.add(attribute);
         
         //attribute = buildSimpleAttribute("TrackConnectionByTx", String.valueOf(md.getTrackConnectionByTransaction()));
         //attributes.add(attribute);

         attribute = buildSimpleAttribute("Interleaving", String.valueOf(md.isInterleaving()));
         attributes.add(attribute);

         if(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.XA))
         {
//            attribute = buildSimpleAttribute("XAResourceTransactionTimeout", String.valueOf(md.getX))
         }
      }
       
      
      return attributes;
   }
   
   @Override
   public List<ServiceDependencyMetaData> buildDependencies(ManagedConnectionFactoryDeploymentMetaData md)
   {
      List<ServiceDependencyMetaData> dependencies = super.buildDependencies(md);
      // Add the jaasJndiBeanName dependency
      ServiceDependencyMetaData serviceDependencyMetaData = new ServiceDependencyMetaData();
      serviceDependencyMetaData.setIDependOn(jaasJndiBeanName);
      dependencies.add(serviceDependencyMetaData);
      return dependencies;
   }

   @Override
   public ObjectName buildObjectName(ManagedConnectionFactoryDeploymentMetaData md)
   {
      String cmType = "jboss.jca:service=";
      String jndiName = md.getJndiName();
      
      if(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.NONE))
      {
         cmType += "NoTxCM,name=" + jndiName;         
      
      }
      else if(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.LOCAL))
      {
         if(md instanceof DataSourceDeploymentMetaData)
         {
            cmType += "LocalTxCM,name=" + jndiName;               
         }
         else
         {
            cmType += "TxCM,name=" + jndiName;               
            
         }

      }else
      {
         if(md instanceof DataSourceDeploymentMetaData)
         {
            cmType += "XATxCM,name=" + jndiName;               
            
         }
         else
         {
            cmType += "TxCM,name=" + jndiName;               
            
         }
      }
      
      return ObjectNameFactory.create(cmType);
   }
   
   @Override
   public String getCode(ManagedConnectionFactoryDeploymentMetaData md)
   {
      
      String code = null;
      
      if(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.NONE))
      {
         code = NoTxConnectionManager.class.getName();         
      }
      else
      {
        code = TxConnectionManager.class.getName();        
      }
      
      return code;
   }
  
   
}
