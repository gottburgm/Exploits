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
package org.jboss.test.jmsra.bean;

import java.rmi.RemoteException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;

import javax.naming.InitialContext;
import javax.naming.Context;

import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.QueueReceiver;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.JMSException;

import org.jboss.logging.Logger;

/**
 * <p>QueueRec bean, get a message from the configured queue. The JMS stuff is
 * configured via the deployment descriptor.
 * 
 * <p>Test sync receive for jms ra.
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>
 * @version $Revision: 81036 $
 */
public class QueueRecBean implements SessionBean
{

	private final Logger log = Logger.getLogger(this.getClass());

	/**
	 * Name used to lookup QueueConnectionFactory
	 */
	private static final String CONNECTION_JNDI = "java:comp/env/jms/MyQueueConnection";

	private QueueConnectionFactory factory = null;

	/**
	 * Name used to lookup queue destination
	 */
	private static final String QUEUE_JNDI = "java:comp/env/jms/QueueName";

	private SessionContext ctx = null;

	private Queue queue = null;

	public QueueRecBean()
	{
	}

	public void setSessionContext(SessionContext ctx)
	{
		this.ctx = ctx;
	}

	public void ejbCreate()
	{
		try
		{
			Context context = new InitialContext();

			// Lookup the queue
			queue = (Queue) context.lookup(QUEUE_JNDI);

			// Lookup the connection factory
			factory = (QueueConnectionFactory) context.lookup(CONNECTION_JNDI);

			// Keep both around
		}
		catch (Exception ex)
		{
			// JMSException or NamingException could be thrown
			log.debug("failed", ex);
			throw new EJBException(ex.toString());
		}
	}

	public void ejbRemove() throws RemoteException
	{
	}

	public void ejbActivate()
	{
	}

	public void ejbPassivate()
	{
	}

	/**
	 * Get a message with sync rec.
	 * 
	 * @return int property name defined in Publisher.JMS_MESSAGE_NR, or -1 if
	 *         fail.
	 */
	public int getMessage()
	{
		QueueConnection queueConnection = null;
		QueueSession queueSession = null;
		int ret;
		try
		{

			// Create a session
			queueConnection = factory.createQueueConnection();
			queueConnection.start();
			queueSession = queueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
			// Get message
			QueueReceiver queueReceiver = queueSession.createReceiver(queue);
			log.info("Waiting for message");
			Message msg = queueReceiver.receive(500L);
			if (msg != null)
			{
				log.info("Recived message: " + msg);
				int nr = msg.getIntProperty(Publisher.JMS_MESSAGE_NR);
				log.debug("nr: " + nr);
				ret = nr;
			}
			else
			{
				log.info("NO message recived");
				ret = -1;
			}

		}
		catch (JMSException ex)
		{

			log.warn("failed", ex);
			ctx.setRollbackOnly();
			throw new EJBException(ex.toString());
		}
		finally
		{
			// ALWAYS close the session. It's pooled, so do not worry.
			if (queueConnection != null)
			{
				try
				{
					queueConnection.close();
				}
				catch (Exception e)
				{
					log.debug("failed", e);
				}
			}
		}
		return ret;
	}

}
