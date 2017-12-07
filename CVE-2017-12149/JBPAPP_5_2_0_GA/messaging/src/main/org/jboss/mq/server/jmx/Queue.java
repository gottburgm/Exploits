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
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.jms.server.destination.QueueService;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.w3c.dom.Element;

/**
 * Portability class that maps the mbean ops onto the
 * org.jboss.jms.server.destination.QueueService.
 * 
 * @author Scott.Stark@jboss.org
 * @author Clebert.suconic@jboss.com
 * @version $Revision: 85945 $
 */
@ManagementObject(componentType = @ManagementComponent(type = "JMSDestination", subtype = "Queue"), properties = ManagementProperties.EXPLICIT)
public class Queue implements MBeanRegistration, QueueMBean {
	private QueueService delegate;

	public Queue() {
		delegate = new QueueService();
	}

	public Queue(boolean createProgramatically) {
		delegate = new QueueService(createProgramatically);
	}

	public void addNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback) {
		delegate.addNotificationListener(listener, filter, handback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#create()
	 */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
	public void create() throws Exception {
		delegate.create();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#destroy()
	 */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
	public void destroy() {
		delegate.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getDownCacheSize()
	 */
	

	@ManagementProperty(use={ViewUse.CONFIGURATION}, description="The write-cache size, can only be set when queue is stopped")
	public int getDownCacheSize() {
		return delegate.getDownCacheSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getFullSize()
	 */
	@ManagementProperty(use={ViewUse.CONFIGURATION}, description="The in-memory message limit, can only be set when queue is stopped")
	public int getFullSize() {
		return delegate.getFullSize();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getJNDIName()
	 */
	 
	@ManagementProperty(use={ViewUse.RUNTIME})
	public String getJNDIName() {
		return delegate.getJNDIName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getMessageCount()
	 */
	@ManagementProperty(use={ViewUse.STATISTIC})
	public int getMessageCount() throws Exception {
		return delegate.getMessageCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getName()
	 */
	@ManagementProperty(use={ViewUse.RUNTIME})
	public String getName() {
		return delegate.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getPageSize()
	 */
	@ManagementProperty(use={ViewUse.CONFIGURATION}, description="he paging size, can only be set when queue is stopped")
	public int getPageSize() {
		return delegate.getPageSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#getServerPeer()
	 */
	@ManagementProperty(use={ViewUse.RUNTIME})
	public ObjectName getServerPeer() {
		return delegate.getServerPeer();
	}

	public ObjectName getServiceName() {
		return delegate.getServiceName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#isCreatedProgrammatically()
	 */
	@ManagementProperty(use={ViewUse.STATISTIC})
	public boolean isCreatedProgrammatically() {
		return delegate.isCreatedProgrammatically();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#listMessages(java.lang.String)
	 */
	@ManagementOperation(description = "List all mesages with the selector", impact = Impact.ReadOnly)
	public List listMessages(String arg0) throws Exception {
		return delegate.listAllMessages(arg0);
	}

	@ManagementOperation(description = "List all mesages", impact = Impact.ReadOnly)
	public List listAllMessages() throws Exception {
		return delegate.listAllMessages();
	}
	
	@ManagementOperation(description = "List all durable mesages", impact = Impact.ReadOnly)
	public List listDurableMessages() throws Exception
	{
		return delegate.listDurableMessages();
	}

	@ManagementOperation(description = "List all durable mesages using a selector", impact = Impact.ReadOnly)
	public List listDurableMessages(String selector) throws Exception
	{
		return delegate.listDurableMessages(selector);
	}
	
	@ManagementOperation(description = "List all non durable mesages", impact = Impact.ReadOnly)
	public List listNonDurableMessages() throws Exception
	{
		return delegate.listNonDurableMessages();
	}

	@ManagementOperation(description = "List all non durable mesages using a selector", impact = Impact.ReadOnly)
	public List listNonDurableMessages(String selector) throws Exception
	{
		return delegate.listNonDurableMessages(selector);
	}
	
	@ManagementOperation(description = "Reset the message counter", impact = Impact.WriteOnly)
	public void resetMessageCounter() throws Exception
	{
		delegate.resetMessageCounter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#removeAllMessages()
	 */
	@ManagementOperation(description = "Remove all messages in the queue", impact = Impact.WriteOnly)
	public void removeAllMessages() throws Exception {
		delegate.removeAllMessages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#setDownCacheSize(int)
	 */
	public void setDownCacheSize(int arg0) {
		delegate.setDownCacheSize(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#setFullSize(int)
	 */
	public void setFullSize(int arg0) {
		delegate.setFullSize(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#setJNDIName(java.lang.String)
	 */
	public void setJNDIName(String arg0) throws Exception {
		delegate.setJNDIName(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#setPageSize(int)
	 */
	public void setPageSize(int arg0) {
		delegate.setPageSize(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#setSecurityConfig(org.w3c.dom.Element)
	 */
	public void setSecurityConfig(Element arg0) throws Exception {
		delegate.setSecurityConfig(arg0);
	}

	public void setSecurityConf(Element arg0) throws Exception {
		delegate.setSecurityConfig(arg0);
	}

	public void setSecurityManager(ObjectName arg0) {
		// noop
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#setServerPeer(javax.management.ObjectName)
	 */
	public void setServerPeer(ObjectName arg0) {
		delegate.setServerPeer(arg0);
	}

	public void setDestinationManager(ObjectName arg0) throws Exception {
		ObjectName peer = new ObjectName("jboss.messaging:service=ServerPeer");
		delegate.setServerPeer(peer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#start()
	 */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
	public void start() throws Exception {
		delegate.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.mq.server.jmx.QueueMBean#stop()
	 */
	@ManagementOperation(description = "Service lifecycle operation", impact = Impact.WriteOnly)
	public void stop() {
		delegate.stop();
	}

	public void setMessageCounterHistoryDayLimit(int arg0) {
		// noop
	}

	@ManagementProperty(use={ViewUse.CONFIGURATION})
	public ObjectName getExpiryDestination() 
	{
		return delegate.getExpiryQueue();
	}

	public void setExpiryDestination(ObjectName destination)
	{
		try
		{
			delegate.setExpiryQueue(destination);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return delegate.toString();
	}

	public void postDeregister() {
		delegate.postDeregister();
	}

	public void postRegister(Boolean registrationDone) {
		delegate.postRegister(registrationDone);
	}

	public void preDeregister() throws Exception {
		delegate.preDeregister();
	}

	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception {
		return delegate.preRegister(server, name);
	}
}
