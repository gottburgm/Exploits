/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jms.server;

import java.util.List;

import javax.management.ObjectName;

import org.jboss.jms.server.destination.StringObjectNameMetaMapper;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.messaging.util.Version;
import org.jboss.metatype.api.annotations.MetaMapping;

/**
 * Empty class to provide the management metadata for JBoss Messaging
 * org.jboss.jms.server.ServerPeer, when deployed in AS5.
 * 
 * @see {@linkplain org.jboss.jms.server.ServerPeer}
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
@ManagementObject(
      name="jboss.messaging:service=ServerPeer",
      componentType = @ManagementComponent(type = "JMS", subtype = "ServerPeer"),
      properties = ManagementProperties.ALL,
      isRuntime = true)
public class ServerPeerMO
{
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The persistence manager name",
         includeInTemplate=true, mandatory=true, readOnly=false)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getPersistenceManager()
   {
      return null;
   }
   public void setPersistenceManager(ObjectName on)
   {
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The post office name",
         includeInTemplate=true, mandatory=true, readOnly=false)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getPostOffice()
   {
      return null;
   }
   public void setPostOffice(ObjectName on)
   {
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The user manager name",
         includeInTemplate=true, mandatory=true, readOnly=false)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getJmsUserManager()
   {
      return null;
   }
   public void setJMSUserManager(ObjectName on)
   {
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The default DLQ on the server peer",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getDefaultDLQ()
   {
      return null;
   }
   public void setDefaultDLQ(ObjectName on)
   {
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The default expiry queue on the server peer",
         includeInTemplate=true, mandatory=true, readOnly=false)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getDefaultExpiryQueue()
   {
      return null;
   }
   public void setDefaultExpiryQueue(ObjectName on)
   {
   }

   //read only JMX attributes

   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)
   public String getJMSVersion()
   {
      return null;
   }

   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)
   public int getJMSMajorVersion()
   {
      return 0;
   }
   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)
   public int getJMSMinorVersion()
   {
      return 0;
   }
   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)
   public String getJMSProviderName()
   {
      return null;
   }
   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)
   public String getProviderVersion()
   {
      return null;
   }
   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)

   public int getProviderMajorVersion()
   {
      return 0;
   }
   @ManagementProperty(use={ViewUse.RUNTIME},
         includeInTemplate=false, mandatory=false, readOnly=true)

   public int getProviderMinorVersion()
   {
      return 0;
   }

   //Read - write attributes

   public void setSecurityDomain(String securityDomain) throws Exception
   {
   }
   public String getSecurityDomain()
   {
      return null;
   }

   public long getFailoverStartTimeout()
   {
      return 0;
   }

   public void setFailoverStartTimeout(long timeout)
   {
   }

   public long getFailoverCompleteTimeout()
   {
      return 0;
   }

   public void setFailoverCompleteTimeout(long timeout)
   {
   }

   public int getDefaultMaxDeliveryAttempts()
   {
      return 0;
   }

   public void setDefaultMaxDeliveryAttempts(int attempts)
   {
   }

   public long getMessageCounterSamplePeriod()
   {
      return 0;
   }

   public void setMessageCounterSamplePeriod(long newPeriod)
   {

   }

   public long getDefaultRedeliveryDelay()
   {
      return 0;
   }

   public void setDefaultRedeliveryDelay(long delay)
   {
   }

   public int getDefaultMessageCounterHistoryDayLimit()
   {
      return 0;
   }

   public void setDefaultMessageCounterHistoryDayLimit(int limit)
   {
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public String getClusterPullConnectionFactoryName()
   {
      return null;
   }
   public void setClusterPullConnectionFactoryName(String name)
   {
   }

   public boolean isUseXAForMessagePull()
   {
      return false;
   }
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setUseXAForMessagePull(boolean useXA) throws Exception
   {
   }

   public boolean isDefaultPreserveOrdering()
   {
      return false;
   }
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setDefaultPreserveOrdering(boolean preserve) throws Exception
   {
   }

   public long getRecoverDeliveriesTimeout()
   {
      return 0;
   }
   public void setRecoverDeliveriesTimeout(long timeout)
   {
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setServerPeerID(int serverPeerID)
   {
   }
   public int getServerPeerID()
   {
      return 0;
   }

   public String getDefaultQueueJNDIContext()
   {
      return null;
   }
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setDefaultQueueJNDIContext(String defaultQueueJNDIContext)
   {
   }

   public String getDefaultTopicJNDIContext()
   {
      return null;
   }

   public void setDefaultTopicJNDIContext(String defaultTopicJNDIContext)
   {
   }

   public void setSuckerPassword(String password)
   {

   }

   public void setStrictTck(boolean strictTck)
   {
   }

   public boolean isStrictTck()
   {
      return false;
   }
   
   
   public void setEnableMessageCounters(boolean enable) 
   {

   }
   
   public boolean isEnableMessageCounters()
   {
      return false;
   }      
   
   public void enableMessageCounters()
   {
      setEnableMessageCounters(true);
   }

   public void disableMessageCounters()
   {
      setEnableMessageCounters(false);
   }

   // JMX Operations -------------------------------------------------------------------------------

   @ManagementOperation(impact = Impact.ReadWrite)
   public String deployQueue(String name, String jndiName) throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public String deployQueue(String name, String jndiName, int fullSize, int pageSize, int downCacheSize) throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public boolean destroyQueue(String name) throws Exception
   {
      return false;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public boolean undeployQueue(String name) throws Exception
   {
      return false;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public String deployTopic(String name, String jndiName) throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public String deployTopic(String name, String jndiName, int fullSize, int pageSize, int downCacheSize) throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public boolean destroyTopic(String name) throws Exception
   {
      return false;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public boolean undeployTopic(String name) throws Exception
   {
      return false;
   }
   @ManagementOperation(impact = Impact.ReadOnly)
   public List getMessageCounters() throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadOnly)
   public List getMessageStatistics() throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadOnly)
   public String listMessageCountersAsHTML() throws Exception
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public void resetAllMessageCounters()
   {
   }
   @ManagementOperation(impact = Impact.ReadWrite)
   public void resetAllMessageCounterHistories()
   {
   }
   @ManagementOperation(impact = Impact.ReadOnly)
   public String showPreparedTransactionsAsHTML()
   {
      return null;
   }
   @ManagementOperation(impact = Impact.ReadOnly)
   public String showActiveClientsAsHTML() throws Exception
   {
      return null;
   }

   // Public ---------------------------------------------------------------------------------------

   @ManagementOperation(impact = Impact.ReadWrite)
   public void resetAllSuckers()
   {
   }

   public Version getVersion()
   {
      return null;
   }

   public boolean isSupportsFailover()
   {
      return false;
   }
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setSupportsFailover(boolean supportsFailover) throws Exception
   {
   }

   public String getServerAopConfig()
   {
      return null;
   }
    @ManagementProperty(use={ViewUse.CONFIGURATION},
          includeInTemplate=false,
          activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setServerAopConfig(String serverAopConfig)
   {
   }

   public String getClientAopConfig()
   {
      return null;
   }
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         includeInTemplate=false,
         activationPolicy=ActivationPolicy.COMPONENT_RESTART)
   public void setClientAopConfig(String clientAopConfig)
   {
   }
}
