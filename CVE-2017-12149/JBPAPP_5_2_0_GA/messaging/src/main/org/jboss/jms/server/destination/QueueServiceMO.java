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

import org.jboss.jms.server.messagecounter.MessageCounter;
import org.jboss.jms.server.messagecounter.MessageStatistics;
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
 * org.jboss.jms.server.destination.QueueService, when deployed in AS5.
 * 
 * @see {@linkplain org.jboss.jms.server.destination.QueueService}
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 89838 $
 */
@ManagementObject(
      componentType = @ManagementComponent(type = "JMSDestination", subtype = "Queue"),
      properties = ManagementProperties.EXPLICIT,
      isRuntime = true)
public class QueueServiceMO implements Serializable
{
   private static final long serialVersionUID = 8483702123881698540L;

// Management Properties
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The destination name",
         includeInTemplate=true, mandatory=true, readOnly=true)
   public String getName() { return null; }   

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The destination's JNDI name",
         mandatory=true, includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   @ManagementObjectID(type="Queue")
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
         description="The DLQ for this queue, overrides the default DLQ on the server peer",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   @MetaMapping(StringObjectNameMetaMapper.class)
   public ObjectName getDLQ() { return null; }
   public void setDLQ(ObjectName arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The expiry queue for this queue, overrides the default expiry queue on the server peer",
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
   
   @ManagementProperty(use={ViewUse.STATISTIC},
         description="True if this destination was created programmatically")
   public boolean isCreatedProgrammatically() { return false; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The number of messages in the queue")
   public int getMessageCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The number of messages currently being delivered")
   public int getDeliveringCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The number of scheduled messages in the queue")
   public int getScheduledMessageCount() { return 0; }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The maximum number of messages this queue can hold before they are dropped",
         includeInTemplate=true)     
   public int getMaxSize() { return 0; }
   public void setMaxSize(int arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The in-memory message limit, can only be set when queue is stopped",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public int getFullSize() { return 0; }
   public void setFullSize(int arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The paging size, can only be set when queue is stopped",
         includeInTemplate=true,
         activationPolicy=ActivationPolicy.DEPLOYMENT_RESTART)
   public int getPageSize() { return 0; }
   public void setPageSize(int arg0) { }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The write-cache size, can only be set when queue is stopped",
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

   @ManagementProperty(use={ViewUse.STATISTIC}, description="Get the message counter for the queue")
   public MessageCounter getMessageCounter() { return null; }

   @ManagementProperty(use={ViewUse.STATISTIC}, description="Get the message statistics for the queue")
   public MessageStatistics getMessageStatistics() { return null; }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The day limit for the message counter",
         includeInTemplate=true)
   public int getMessageCounterHistoryDayLimit() { return 0; }
   public void setMessageCounterHistoryDayLimit(int arg0) { }

   @ManagementProperty(use={ViewUse.CONFIGURATION},
         description="The maximum delivery attempts to the queue",
         includeInTemplate=true)
   public int getMaxDeliveryAttempts() { return 0; }
   public void setMaxDeliveryAttempts(int arg0) { }

   @ManagementProperty(use={ViewUse.STATISTIC}, description="The number of consumers on the queue")
   public int getConsumerCount() { return 0; }
   
   // Management Operations
   
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void create() throws Exception { }

   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void start() throws Exception { }

   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void stop() { }
   
   @ManagementOperation(description = "Service lifecycle operation", impact = Impact.Lifecycle)
   public void destroy() { }

   @ManagementOperation(description = "Remove all messages in the queue", impact = Impact.WriteOnly)
   public void removeAllMessages() throws Exception { }

   @ManagementOperation(description = "List all messages", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listAllMessages() throws Exception { return null; }
   
   @ManagementOperation(description = "List all messages with selector", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listAllMessages(String selector) throws Exception { return null; }
   
   @ManagementOperation(description = "List all durable mesages", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listDurableMessages() throws Exception { return null; }
   
   @ManagementOperation(description = "List all durable mesages using a selector", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listDurableMessages(String selector) throws Exception { return null; }
   
   @ManagementOperation(description = "List all non durable mesages", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listNonDurableMessages() throws Exception { return null; }

   @ManagementOperation(description = "List all non durable mesages using a selector", impact = Impact.ReadOnly)
   @MetaMapping(MessageListMapper.class)
   public List listNonDurableMessages(String selector) throws Exception { return null; }
    
   @ManagementOperation(description = "Reset the message counter", impact = Impact.WriteOnly)
   public void resetMessageCounter() throws Exception { }

   @ManagementOperation(description = "Reset the message counter history", impact = Impact.WriteOnly)
   public void resetMessageCounterHistory() throws Exception { }

   @ManagementOperation(description = "Get the message counter as HTML", impact = Impact.ReadOnly)
   public String listMessageCounterAsHTML() throws Exception { return null; }

   @ManagementOperation(description = "Get the message counter history as HTML", impact = Impact.ReadOnly)
   public String listMessageCounterHistoryAsHTML() throws Exception { return null; }
   
}