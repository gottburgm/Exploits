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
package org.jboss.resource.metadata.mcf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.logging.Logger;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementObjectRef;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ManagementPropertyFactory;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.resource.deployers.management.ConnectionFactoryProperty;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.util.StringPropertyReplacer;

/**
 * A ManagedConnectionFactoryDeployment.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 110845 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@ManagementObject(properties=ManagementProperties.EXPLICIT)
public class ManagedConnectionFactoryDeploymentMetaData
   implements Serializable, ConnectionPoolMetaData
{
   private static Logger log = Logger.getLogger(ManagedConnectionFactoryDeploymentMetaData.class);

   /** The serialVersionUID */
   private static final long serialVersionUID = -4591557831734316580L;

   /** The jndiName */   
   @XmlElement(name="jndi-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String jndiName;   
   
   /** The rarName */
   @XmlElement(name="rar-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String rarName;
   
   /** The useJavaContext */
   @XmlElement(name="use-java-context")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean useJavaContext = Boolean.TRUE;
   
   /** The connectionDefinition */
   @XmlElement(name="connection-definition")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String connectionDefinition;

   /** The jmxInvokerName */
   @XmlElement(name="jmx-invoker-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String jmxInvokerName = "jboss:service=invoker,type=jrmp";
   
   @XmlElement(name="min-pool-size")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer minSize = 0;
   
   @XmlElement(name="max-pool-size")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer maxPoolSize = 10;
   
   @XmlElement(name="blocking-timeout-millis")
   @XmlJavaTypeAdapter(LongSystemPropertyXmlJavaTypeAdapter.class)
   private Long blockingTimeout = 30000L;
   
   @XmlElement(name="idle-timeout-minutes")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer idleTimeout = 30;
   
   @XmlElement(name="prefill")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean prefill = Boolean.FALSE;
   
   @XmlElement(name="background-validation")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean backgroundValidation = Boolean.FALSE;
   
   @XmlElement(name="background-validation-millis")
   @XmlJavaTypeAdapter(LongSystemPropertyXmlJavaTypeAdapter.class)
   private Long backgroundValidationMillis = new Long(0L);

   @XmlElement(name="validate-on-match")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean validateOnMatch = Boolean.TRUE;
   
   @XmlJavaTypeAdapter(ManagedConnectionEmptyContentAdapter.class)
   @XmlElement(name="use-strict-min")
   private Boolean useStrictMin = Boolean.FALSE;

   @XmlElement(name="use-fast-fail")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean useFastFail = Boolean.FALSE;

   @XmlJavaTypeAdapter(ManagedConnectionEmptyContentAdapter.class)
   @XmlElement(name="no-tx-separate-pools")
   private Boolean noTxSeparatePools = Boolean.FALSE;
   
   @XmlElement(name="statistics-formatter")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String statisticsFormatter = "org.jboss.resource.statistic.pool.JBossDefaultSubPoolStatisticFormatter";
   
   @XmlElement(name="isSameRM-override-value")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean isSameRMOverrideValue = Boolean.FALSE;

   // is always true now and left here for the xml binding
   @Deprecated
   @XmlJavaTypeAdapter(ManagedConnectionEmptyContentAdapter.class)
   @XmlElement(name="track-connection-by-tx")
   private Boolean trackConnectionByTransaction;

   @XmlJavaTypeAdapter(ManagedConnectionEmptyContentAdapter.class)
   @XmlElement(name="interleaving")
   private Boolean interleaving;

   @XmlElement(name="allocation-retry")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer allocationRetry = 0;
   
   @XmlElement(name="allocation-retry-wait-millis")
   @XmlJavaTypeAdapter(LongSystemPropertyXmlJavaTypeAdapter.class)
   private Long allocationRetryWaitMillis = 5000L;
   
   /** The transactionSupportMetaData */
   @XmlTransient
   private ManagedConnectionFactoryTransactionSupportMetaData transactionSupportMetaData = ManagedConnectionFactoryTransactionSupportMetaData.NONE;

   /** The managedConnectionFactoryProperties */
   @XmlElement(name="config-property")
   private List<ManagedConnectionFactoryPropertyMetaData> managedConnectionFactoryProperties = new ArrayList<ManagedConnectionFactoryPropertyMetaData>();
   
   /** The securityMetaData */
   @XmlElements({@XmlElement(name="security-domain", type=SecurityDomainMetaData.class), @XmlElement(name="security-domain-and-application",type=SecurityDomainApplicationManagedMetaData.class), @XmlElement(name="application-managed-security",type=ApplicationManagedSecurityMetaData.class)})
   private SecurityMetaData securityMetaData;
      
   @XmlElement(name="depends")
   private List<String> dependsNames = new ArrayList<String>();
   
   @XmlTransient
   private List<ServiceMetaData> dependsServices = new ArrayList<ServiceMetaData>();

   @XmlElement(name="metadata")
   private DBMSMetaData dbmsMetaData;

   // todo: this should be wrapped into <metadata> element
   @XmlElement(name="type-mapping")
   String typeMapping;
   
   /** The localTransactions */
   @XmlJavaTypeAdapter(ManagedConnectionEmptyContentAdapter.class)
   @XmlElement(name="local-transaction")
   private Boolean localTransactions = Boolean.FALSE;

   public ManagedConnectionFactoryDeploymentMetaData()
   {
      this.interleaving = Boolean.FALSE;
   }
   
   /**
    * Get the connectionDefinition.
    * 
    * @return the connectionDefinition.
    */
   @ManagementProperty(name="connection-definition",
         description="The connection factory class name",
         mandatory=true, includeInTemplate=true)
   public String getConnectionDefinition()
   {
      return connectionDefinition;
   }

   /**
    * Set the connectionDefinition.
    * 
    * @param connectionDefinition The connectionDefinition to set.
    */
   public void setConnectionDefinition(String connectionDefinition)
   {
      this.connectionDefinition = connectionDefinition;
   }

   /**
    * Get the jndiName. This is the id for the DataSource ManagedObject.
    * 
    * @return the jndiName.
    */
   @ManagementProperty(name="jndi-name",
         description="The global JNDI name to bind the factory under",
         includeInTemplate=true,
         mandatory=true)
   @ManagementObjectID(type="DataSource")
   public String getJndiName()
   {
      if (jndiName.indexOf("${") >= 0 && jndiName.indexOf('}') >=0)
      {
         jndiName = StringPropertyReplacer.replaceProperties(jndiName);
      }
      return jndiName;
   }

   /**
    * Set the jndiName.
    * 
    * @param jndiName The jndiName to set.
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * Get the transactionSupportMetaData.
    * 
    * @return the transactionSupportMetaData.
    */
   @ManagementProperty(ignored=true)
   public ManagedConnectionFactoryTransactionSupportMetaData getTransactionSupportMetaData()
   {
      return transactionSupportMetaData;
   }

   /**
    * Set the transactionSupportMetaData.
    * 
    * @param transactionSupportMetaData The transactionSupportMetaData to set.
    */
   public void setTransactionSupportMetaData(ManagedConnectionFactoryTransactionSupportMetaData transactionSupportMetaData)
   {
      this.transactionSupportMetaData = transactionSupportMetaData;
   }

   /**
    * Get the useJavaContext.
    * 
    * @return the useJavaContext.
    */
   @ManagementProperty(name="use-java-context",
         description="Should the jndi name be bound under the java: context",
         includeInTemplate=true)
   public boolean isUseJavaContext()
   {
      return useJavaContext;
   }
   /**
    * Set the useJavaContext.
    * 
    * @param useJavaContext The useJavaContext to set.
    */
   public void setUseJavaContext(boolean useJavaContext)
   {
      this.useJavaContext = useJavaContext;
   }

   /**
    * Get the managedConnectionFactoryProperties.
    * 
    * @return the managedConnectionFactoryProperties.
    */
   @ManagementProperty(name="config-property",
         description="The connection factory config properties",
         managed=true, includeInTemplate=true)
   @ManagementPropertyFactory(ConnectionFactoryProperty.class)
   public List<ManagedConnectionFactoryPropertyMetaData> getManagedConnectionFactoryProperties()
   {
      return managedConnectionFactoryProperties;
   }

   /**
    * Set the managedConnectionFactoryProperties.
    * 
    * @param managedConnectionFactoryProperties The managedConnectionFactoryProperties to set.
    */
   public void setManagedConnectionFactoryProperties(
         List<ManagedConnectionFactoryPropertyMetaData> managedConnectionFactoryProperties)
   {
      this.managedConnectionFactoryProperties = managedConnectionFactoryProperties;
   }

   /**
    * Get the rarName.
    * 
    * @return the rarName.
    */
   @ManagementProperty(name="rar-name",
         description="The resource adapter archive name",
         mandatory=true, includeInTemplate=true)
   public String getRarName()
   {
      return rarName;
   }

   /**
    * Set the rarName.
    * 
    * @param rarName The rarName to set.
    */
   public void setRarName(String rarName)
   {
      this.rarName = rarName;
   }

   /**
    * Get the securityMetaData.
    * 
    * @return the securityMetaData.
    */
   @ManagementProperty(name="security-domain",
         description="The security-domain used to validate connections",
         includeInTemplate=true)
   public SecurityMetaData getSecurityMetaData()
   {
      return securityMetaData;
   }

   /**
    * Set the securityMetaData.
    * 
    * @param securityMetaData The securityMetaData to set.
    */
   public void setSecurityMetaData(SecurityMetaData securityMetaData)
   {
      this.securityMetaData = securityMetaData;
   }

   
   /**
    * Get the typeMapping.
    * 
    * @return the typeMapping.
    */
   @ManagementProperty(name="type-mapping", includeInTemplate=true)
   public String getTypeMapping()
   {
      return typeMapping;
   }

   /**
    * Set the typeMapping.
    * 
    * @param typeMapping The typeMapping to set.
    */
   public void setTypeMapping(String typeMapping)
   {
      this.typeMapping = typeMapping;
   }

   /**
    * Get the jmxInvokerName.
    * 
    * @return the jmxInvokerName.
    */
   @ManagementProperty(name="jmx-invoker-name",
         description="The name of the JMX invoker",
         includeInTemplate=true)
   @ManagementObjectRef(type="JMXInvoker")
   public String getJmxInvokerName()
   {
      return jmxInvokerName;
   }

   /**
    * Set the jmxInvokerName.
    * 
    * @param jmxInvokerName The jmxInvokerName to set.
    */
   public void setJmxInvokerName(String jmxInvokerName)
   {
      this.jmxInvokerName = jmxInvokerName;
   }

   /**
    * Get the dependsNames.
    * 
    * @return the dependsNames.
    */
   @ManagementProperty(name="depends", ignored=true)
   public List<String> getDependsNames()
   {
      return dependsNames;
   }

   /**
    * Set the dependsNames.
    * 
    * @param dependsNames The dependsNames to set.
    */
   public void setDependsNames(List<String> dependsNames)
   {
      this.dependsNames = dependsNames;
   }

   /**
    * Get the dependsServices.
    * 
    * @return the dependsServices.
    */
   @ManagementProperty(name="services", ignored=true)
   public List<ServiceMetaData> getDependsServices()
   {
      return dependsServices;
   }

   /**
    * Set the dependsServices.
    * 
    * @param dependsServices The dependsServices to set.
    */
   public void setDependsServices(List<ServiceMetaData> dependsServices)
   {
      this.dependsServices = dependsServices;
   }
   
   @ManagementProperty(name="min-pool-size",
         description="The min size of the pool",
         includeInTemplate=true)
   public void setMinSize(int minSize)
   {
      this.minSize = minSize;
   } 
   
   public int getMinSize()
   {
      return this.minSize;
   }
   
   @ManagementProperty(name="max-pool-size",
         description="The max size of the pool",
         includeInTemplate=true)
   public void setMaxSize(int maxSize)
   {
      this.maxPoolSize = maxSize;
   }
   
   public int getMaxSize()
   {
      if (this.maxPoolSize >= this.minSize)
      {
         return this.maxPoolSize;
      } else {
         return this.minSize;
      }
   }
   
   @ManagementProperty(name="blocking-timeout-millis",
         description="The time to wait for a connection to become available before giving up",
         includeInTemplate=true)
   public void setBlockingTimeoutMilliSeconds(long blockTimeout)
   {
     this.blockingTimeout = blockTimeout;
   }
   
   public long getBlockingTimeoutMilliSeconds()
   {
      return this.blockingTimeout;
   }
   
   @ManagementProperty(name="idle-timeout-minutes",
         description="The idle timeout in minutes",
         includeInTemplate=true)
   public void setIdleTimeoutMinutes(int idleTimeout)
   {
      this.idleTimeout = idleTimeout;
   }
   
   public int getIdleTimeoutMinutes()
   {
      return this.idleTimeout;
   }

   @ManagementProperty(name="prefill",
         description = "Whether to prefill the pool",
         includeInTemplate=true)
   public void setPrefill(Boolean prefill)
   {
      this.prefill = prefill;
   }

   public Boolean getPrefill()
   {
      return this.prefill;      
   }

   @ManagementProperty(name="background-validation",
         description = "Whether to use backgroup validation",
         includeInTemplate=true)
   public boolean isBackgroundValidation()
   {
      return backgroundValidation;
   }
   public void setBackgroundValidation(boolean backgroundValidation)
   {
      this.backgroundValidation = backgroundValidation;
   }

   public void setBackgroundValidationMillis(long interval)
   {
      this.backgroundValidationMillis = interval;
   }

   @ManagementProperty(name="background-validation-millis", includeInTemplate=true)
   public long getBackgroundValidationMillis()
   {
      return this.backgroundValidationMillis;
   }

   public void setValidateOnMatch(boolean validateOnMatch)
   {
      this.validateOnMatch = validateOnMatch;
   }

   @ManagementProperty(name="validate-on-match", includeInTemplate=true)
   public boolean isValidateOnMatch()
   {
      return this.validateOnMatch;
   }
   @ManagementProperty(name="isSameRM-override-value", includeInTemplate=true)
   public Boolean getIsSameRMOverrideValue()
   {
      return isSameRMOverrideValue;
   }

   public void setIsSameRMOverrideValue(Boolean isSameRMOverrideValue)
   {
      this.isSameRMOverrideValue = isSameRMOverrideValue;
   }
   @Deprecated
   @ManagementProperty(name="track-connection-by-tx", includeInTemplate=true,
         use={ViewUse.CONFIGURATION})
   public Boolean getTrackConnectionByTransaction()
   {
      return !isInterleaving();
   }

   @Deprecated
   public void setTrackConnectionByTransaction(Boolean trackConnectionByTransaction)
   {
      if(Boolean.TRUE == getLocalTransactions() && !Boolean.TRUE.equals(trackConnectionByTransaction))
      {
         log.warn("In case of local transactions track-connection-by-tx must always be true");
         trackConnectionByTransaction = Boolean.TRUE;
      }
      setInterleaving(!Boolean.TRUE.equals(trackConnectionByTransaction));
   }

   public Boolean isInterleaving()
   {
      return interleaving == Boolean.TRUE && !Boolean.TRUE.equals(getLocalTransactions());
   }
   @ManagementProperty(name="interleaving", includeInTemplate=true,
         use={ViewUse.CONFIGURATION}, activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public Boolean getInterleaving()
   {
      return isInterleaving();
   }
   public void setInterleaving(Boolean interleaving)
   {
      this.interleaving = interleaving;
   }

   @ManagementProperty(name="allocation-retry",
         description="The number of times allocation retries should be tried",
         includeInTemplate=true,
         use={ViewUse.CONFIGURATION})
   public int getAllocationRetry()
   {
      return this.allocationRetry;
   }
   
   public void setAllocationRetry(int ar)
   {
      this.allocationRetry = ar;
   } 
   
   @ManagementProperty(name="allocation-retry-wait-millis",
         description="The time to wait between allocation retries",
         includeInTemplate=true,
         use={ViewUse.CONFIGURATION})
   public long getAllocationRetryWaitMillis()
   {
      return this.allocationRetryWaitMillis;
   }
   
   public void setAllocationRetryWaitMillis(long arwm)
   {
      this.allocationRetryWaitMillis = arwm;
   } 
   
   @ManagementProperty(name="local-transaction", use={ViewUse.RUNTIME}, readOnly=true)
   public Boolean getLocalTransactions()
   {
      return localTransactions;
   }

   public void setLocalTransactions(Boolean localTransactions)
   {
      this.localTransactions = localTransactions;
   }

   @ManagementProperty(name="use-strict-min", includeInTemplate=true)
   public Boolean getUseStrictMin()
   {
      return useStrictMin;
   }

   public void setUseStrictMin(Boolean useStrictMin)
   {
      this.useStrictMin = useStrictMin;
   }

   @ManagementProperty(name="use-fast-fail", includeInTemplate=true)
   public Boolean getUseFastFail()
   {
      return useFastFail;
   }

   public void setUseFastFail(Boolean useFastFail)
   {
      this.useFastFail = useFastFail;
   }
   @ManagementProperty(name="statistics-formatter", includeInTemplate=true)
   public String getStatisticsFormatter()
   {
      return statisticsFormatter;
   }

   public void setStatisticsFormatter(String statisticsFormatter)
   {
      this.statisticsFormatter = statisticsFormatter;
   }
   
   @ManagementProperty(name="no-tx-separate-pools", includeInTemplate=true)
   public Boolean getNoTxSeparatePools()
   {
      return this.noTxSeparatePools;
   }

   public void setNoTxSeparatePools(Boolean notxpool)
   {
      this.noTxSeparatePools = notxpool;
   }

   @ManagementProperty(name="metadata", includeInTemplate=true)
   public DBMSMetaData getDBMSMetaData()
   {
      return dbmsMetaData;
   }

   public void setDBMSMetaData(DBMSMetaData dbmsMetaData)
   {
      this.dbmsMetaData = dbmsMetaData;
   }
}
