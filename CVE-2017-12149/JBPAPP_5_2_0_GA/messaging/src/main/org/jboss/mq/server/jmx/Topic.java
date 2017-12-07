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
package org.jboss.mq.server.jmx;

import java.util.List;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.w3c.dom.Element;
import org.jboss.jms.server.destination.TopicService;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * Portability class that maps the mbean ops onto the org.jboss.jms.server.destination.TopicService.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@ManagementObject(componentType = @ManagementComponent(type = "JMSDestination", subtype = "Topic"), properties = ManagementProperties.EXPLICIT)
public class Topic
   implements MBeanRegistration, TopicMBean
{
   private TopicService delegate;

   public Topic()
   {
      delegate = new TopicService();
   }
   public Topic(boolean createProgramatically)
   {
      delegate = new TopicService(createProgramatically);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#create()
    */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   public void create() throws Exception
   {
      delegate.create();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#destroy()
    */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   public void destroy()
   {
      delegate.destroy();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getDownCacheSize()
    */
	@ManagementProperty(use={ViewUse.CONFIGURATION}, description="The write-cache size, can only be set when queue is stopped")
   public int getDownCacheSize()
   {
      return delegate.getDownCacheSize();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getFullSize()
    */
	@ManagementProperty(use={ViewUse.CONFIGURATION}, description="The in-memory message limit, can only be set when queue is stopped")
   public int getFullSize()
   {
      return delegate.getFullSize();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getJNDIName()
    */
	@ManagementProperty(use={ViewUse.RUNTIME})
   public String getJNDIName()
   {
      return delegate.getJNDIName();
   }

	@ManagementOperation(description = "List all non durable mesages", impact = Impact.ReadOnly)
   public List listNonDurableMessages(String subscriptionId) throws Exception
   {
      return delegate.listNonDurableMessages(subscriptionId);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getName()
    */
	@ManagementProperty(use={ViewUse.RUNTIME})
   public String getName()
   {
      return delegate.getName();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getPageSize()
    */
	@ManagementProperty(use={ViewUse.CONFIGURATION}, description="he paging size, can only be set when queue is stopped")
   public int getPageSize()
   {
      return delegate.getPageSize();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getServer()
    */
   public MBeanServer getServer()
   {
      return delegate.getServer();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#getServerPeer()
    */
   public ObjectName getServerPeer()
   {
      return delegate.getServerPeer();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#isCreatedProgrammatically()
    */
	@ManagementProperty(use={ViewUse.STATISTIC})
   public boolean isCreatedProgrammatically()
   {
      return delegate.isCreatedProgrammatically();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#listMessagesDurableSub(java.lang.String, java.lang.String, java.lang.String)
    */
	@ManagementOperation(description = "List all durable mesages", impact = Impact.ReadOnly)
   public List listMessagesDurableSub(String arg0, String arg1, String arg2) throws Exception
   {
      return delegate.listDurableMessages(arg0, arg1);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#listMessagesNonDurableSub(long, java.lang.String)
    */
	@ManagementOperation(description = "List all non durable mesages", impact = Impact.ReadOnly)
   public List listMessagesNonDurableSub(long arg0, String arg1) throws Exception
   {
      return delegate.listNonDurableMessages(arg1);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#listSubscriptionsAsText()
    */
	@ManagementOperation(description = "List subscriptions on text", impact = Impact.ReadOnly)
   public String listSubscriptionsAsText() throws Exception
   {
      return delegate.listAllSubscriptionsAsHTML();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#listSubscriptionsAsText(boolean)
    */
	@ManagementOperation(description = "List subscriptions on text", impact = Impact.ReadOnly)
   public String listSubscriptionsAsText(boolean arg0) throws Exception
   {
      return delegate.listAllSubscriptionsAsHTML();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#removeAllMessages()
    */
	@ManagementOperation(description = "Remove all the messages on the queue DB", impact = Impact.WriteOnly)
   public void removeAllMessages() throws Exception
   {
      delegate.removeAllMessages();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setDownCacheSize(int)
    */
   public void setDownCacheSize(int arg0)
   {
      delegate.setDownCacheSize(arg0);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setFullSize(int)
    */
   public void setFullSize(int arg0)
   {
      delegate.setFullSize(arg0);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setJNDIName(java.lang.String)
    */
   public void setJNDIName(String arg0) throws Exception
   {
      delegate.setJNDIName(arg0);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setPageSize(int)
    */
   public void setPageSize(int arg0)
   {
      delegate.setPageSize(arg0);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setSecurityConfig(org.w3c.dom.Element)
    */
   public void setSecurityConfig(Element arg0) throws Exception
   {
      delegate.setSecurityConfig(arg0);
   }
   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setSecurityConf(org.w3c.dom.Element)
    */
   public void setSecurityConf(Element arg0) throws Exception
   {
      delegate.setSecurityConfig(arg0);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setSecurityManager(javax.management.ObjectName)
    */
   public void setSecurityManager(ObjectName arg0)
   {
      // noop
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setServerPeer(javax.management.ObjectName)
    */
   public void setServerPeer(ObjectName arg0)
   {
      delegate.setServerPeer(arg0);
   }
   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#setDestinationManager(javax.management.ObjectName)
    */
   public void setDestinationManager(ObjectName arg0) throws Exception
   {
      ObjectName peer = new ObjectName("jboss.messaging:service=ServerPeer");
      delegate.setServerPeer(peer);
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#start()
    */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   public void start() throws Exception
   {
      delegate.start();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#stop()
    */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
   public void stop()
   {
      delegate.stop();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#subscriptionCount()
    */
   public int subscriptionCount() throws Exception
   {
      return delegate.getAllSubscriptionsCount();
   }

   /* (non-Javadoc)
    * @see org.jboss.mq.server.jmx.TopicMBean#subscriptionCount(boolean)
    */
   public int subscriptionCount(boolean arg0) throws Exception
   {
      return arg0 ? delegate.getDurableSubscriptionsCount() : delegate.getNonDurableSubscriptionsCount();
   }

   public ObjectName getExpiryDestination()
   {
      return null;
   }
   public void setExpiryDestination(ObjectName destination)
   {
      System.err.println("There is no ExpiryDestination currently");
   }

   public String toString()
   {
      return delegate.toString();
   }

   public void postDeregister()
   {
      delegate.postDeregister();
   }
   public void postRegister(Boolean registrationDone)
   {
      delegate.postRegister(registrationDone);
   }
   public void preDeregister() throws Exception
   {
      delegate.preDeregister();
   }
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      return delegate.preRegister(server, name);
   }
}
