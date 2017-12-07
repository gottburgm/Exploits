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
package org.jboss.resource.adapter.jms;

import javax.jms.JMSException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;

import org.jboss.resource.JBossResourceException;

/**
 * JMS Local transaction
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71790 $
 */
public class JmsLocalTransaction implements LocalTransaction
{
	protected JmsManagedConnection mc;

	public JmsLocalTransaction(final JmsManagedConnection mc)
	{
		this.mc = mc;
	}

	public void begin() throws ResourceException
	{
	}

	public void commit() throws ResourceException
	{
	    mc.lock();
		try
		{
			if (mc.getSession().getTransacted())
				mc.getSession().commit();
		}
		catch (JMSException e)
		{
			throw new JBossResourceException("Could not commit LocalTransaction", e);
		}
		finally
		{
		   mc.unlock();
		}
	}

	public void rollback() throws ResourceException
	{
	    mc.lock();
		try
		{
			if (mc.getSession().getTransacted())
				mc.getSession().rollback();
		}
		catch (JMSException ex)
		{
			throw new JBossResourceException("Could not rollback LocalTransaction", ex);
		}
		finally
		{
		   mc.unlock();
		}
	}
}
