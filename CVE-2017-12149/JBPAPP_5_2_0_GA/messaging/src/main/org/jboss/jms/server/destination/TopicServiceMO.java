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
package org.jboss.jms.server.destination;

import java.io.Serializable;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.annotations.MetaMapping;
import org.w3c.dom.Element;

/**
 * Empty class to provide the management metadata for JBoss Messaging
 * org.jboss.jms.server.destination.TopicService, when deployed in AS5.
 * 
 * @see {@linkplain org.jboss.jms.server.destination.TopicService}
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 89838 $
 */
@ManagementObject(
      componentType = @ManagementComponent(type = "JMSDestination", subtype = "Topic"),
      properties = ManagementProperties.EXPLICIT,
      isRuntime = true)
public class TopicServiceMO implements Serializable
{
   private static final long serialVersionUID = -2972719964517681496L;
   
   // Management Properties
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The destination name",
         mandatory=true, includeInTemplate=true, readOnly=true)
   public String getName() { return null; }   

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The destination's JNDI name",
         mandatory=true, includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   @ManagementObjectID(type="Topic")
   public String getJNDIName() { return null; }
   public void setJNDIName(String arg0) throws Exception { }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The ObjectName of the server peer this destination was deployed on",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getServerPeer() { return null; }
   public void setServerPeer(ObjectName arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The DLQ for this topic, overrides the default DLQ on the server peer",
         includeInTemplate=true)   
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getDLQ() { return null; }
   public void setDLQ(ObjectName arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The expiry queue for this topic, overrides the default expiry queue on the server peer",
         includeInTemplate=true)   
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getExpiryQueue() { return null; }
   public void setExpiryQueue(ObjectName arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The delay before redelivering",
         includeInTemplate=true)   
   public long getRedeliveryDelay() { return 0; }
   public void setRedeliveryDelay(long arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The destination's security configuration",
         includeInTemplate=true)     
   @MetaMapping(SecurityConfigMapper.class)
   public Element getSecurityConfig() { return null; }
   public void setSecurityConfig(Element arg0) { }
   
   @ManagementProperty(use={ViewUse.RUNTIME}, description="True if this destination was created programmatically")
   public boolean isCreatedProgrammatically() { return false; }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The maximum number of messages this topic can hold before they are dropped",
         includeInTemplate=true)     
   public int getMaxSize() { return 0; }
   public void setMaxSize(int arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The in-memory message limit, can only be set when topic is stopped",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public int getFullSize() { return 0; }
   public void setFullSize(int arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The paging size, can only be set when topic is stopped",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public int getPageSize() { return 0; }
   public void setPageSize(int arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The write-cache size, can only be set when topic is stopped",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public int getDownCacheSize() { return 0; }
   public void setDownCacheSize(int arg0) { }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="Is this a clustered destination?",
         includeInTemplate=true, readOnly=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public boolean isClustered() { return false; }
   public void setClustered(boolean arg0) { }

   // FIXME 
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The day limit for the message counter")
   public int getMessageCounterHistoryDayLimit() { return 0; }
   public void setMessageCounterHistoryDayLimit(int arg0) { }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The maximum delivery attempts to the topic",
         includeInTemplate=true)
   public int getMaxDeliveryAttempts() { return 0; }
   public void setMaxDeliveryAttempts(int arg0) { }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The message counters for the topic")
   public List getMessageCounters() { return null; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The count of all messages in all subscriptions of this topic")
   public int getAllMessageCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The count of all messages in all durable subscriptions of this topic")
   public int getDurableMessageCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The count of all messages in all non durable subscriptions of this topic")
   public int getNonDurableMessageCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The count of all subscriptions of this topic")
   public int getAllSubscriptionsCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The count of all durable subscriptions of this topic")
   public int getDurableSubscriptionsCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The count of all non durable subscriptions of this topic")
   public int getNonDurableSubscriptionsCount() { return 0; }
   
   // Management Operations
   
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void create() throws Exception { }

   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void start() throws Exception { }

   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void stop() { }
   
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void destroy() { }

   @ManagementOperation(description = "Remove all messages", impact = Impact.WriteOnly)
   public void removeAllMessages() throws Exception { }
   
   @ManagementOperation(description = "Return all subscriptions for the topic", impact = Impact.ReadOnly)
   public List listAllSubscriptions() throws Exception { return null; }
   
   @ManagementOperation(description = "Return all durable subscriptions for the topic", impact = Impact.ReadOnly)
   public List listDurableSubscriptions() throws Exception { return null; }
   
   @ManagementOperation(description = "Return all non durable subscriptions for the topic", impact = Impact.ReadOnly)
   public List listNonDurableSubscriptions() throws Exception { return null; }
   
   @ManagementOperation(description = "Return all subscriptions for the topic in HTML", impact = Impact.ReadOnly)
   public String listAllSubscriptionsAsHTML() throws Exception { return null; }
   
   @ManagementOperation(description = "Return all durable subscriptions for the topic in HTML", impact = Impact.ReadOnly)
   public String listDurableSubscriptionsAsHTML() throws Exception { return null; }
   
   @ManagementOperation(description = "Return all non durable subscriptions for the topic in HTML", impact = Impact.ReadOnly)
   public String listNonDurableSubscriptionsAsHTML() throws Exception { return null; }
   
   @ManagementOperation(description = "List all messages for the specified subscription", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listAllMessages(String subscriptionID) throws Exception { return null; }
   
   @ManagementOperation(description = "List all messages for the specified subscription with the specified selector", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listAllMessages(String subscriptionID, String selector) throws Exception { return null; }
   
   @ManagementOperation(description = "List all durable messages for the specified subscription", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listDurableMessages(String subscriptionID) throws Exception { return null; }
   
   @ManagementOperation(description = "List all durable messages for the specified subscription with the specified selector", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listDurableMessages(String subscriptionID, String selector) throws Exception { return null; }
   
   @ManagementOperation(description = "List all non durable messages for the specified subscription", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listNonDurableMessages(String subscriptionID) throws Exception { return null; }
   
   @ManagementOperation(description = "List all non durable messages for the specified subscription with the specified selector", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listNonDurableMessages(String subscriptionID, String selector) throws Exception { return null; }
   
}