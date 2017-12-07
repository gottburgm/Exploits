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
package org.jboss.resource.adapter.jms.inflow;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * A generic jms session pool.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com>Weston Price</a>
 * @version $Revision: 91228 $
 */
public class JmsServerSession implements ServerSession, MessageListener, Work,
      WorkListener
{
   /** The log */
   private static final Logger log = Logger.getLogger(JmsServerSession.class);

   /** The session pool */
   JmsServerSessionPool pool;

   /** The transacted flag */
   boolean transacted;

   /** The acknowledge mode */
   int acknowledge;

   /** The session */
   Session session;

   /** Any XA session */
   XASession xaSession;

   /** The endpoint */
   MessageEndpoint endpoint;

   /** Any DLQ handler */
   DLQHandler dlqHandler;

   TransactionDemarcationStrategy txnStrategy;

   /**
    * Create a new JmsServerSession
    * 
    * @param pool
    *           the server session pool
    */
   public JmsServerSession(JmsServerSessionPool pool)
   {
      this.pool = pool;

   }

   /**
    * Setup the session
    */
   public void setup() throws Exception
   {
      JmsActivation activation = pool.getActivation();
      JmsActivationSpec spec = activation.getActivationSpec();

      dlqHandler = activation.getDLQHandler();

      Connection connection = activation.getConnection();

      // Create the session
      if (connection instanceof XAConnection
            && activation.isDeliveryTransacted())
      {
         xaSession = ((XAConnection) connection).createXASession();
         session = xaSession.getSession();
      } else
      {
         transacted = spec.isSessionTransacted();
         acknowledge = spec.getAcknowledgeModeInt();
         session = connection.createSession(transacted, acknowledge);
      }

      // Get the endpoint
      MessageEndpointFactory endpointFactory = activation
            .getMessageEndpointFactory();
      XAResource xaResource = null;

      if (activation.isDeliveryTransacted() && xaSession != null)
         xaResource = xaSession.getXAResource();

      endpoint = endpointFactory.createEndpoint(xaResource);

      // Set the message listener
      session.setMessageListener(this);
   }

   /**
    * Stop the session
    */
   public void teardown()
   {
      try
      {
         if (endpoint != null)
            endpoint.release();
      } catch (Throwable t)
      {
         log.debug("Error releasing endpoint " + endpoint, t);
      }

      try
      {
         if (xaSession != null)
            xaSession.close();
      } catch (Throwable t)
      {
         log.debug("Error releasing xaSession " + xaSession, t);
      }

      try
      {
         if (session != null)
            session.close();
      } catch (Throwable t)
      {
         log.debug("Error releasing session " + session, t);
      }
   }

   public void onMessage(Message message)
   {
      try
      {
         if (!(txnStrategy != null && txnStrategy instanceof TraditionalXATransactionDemarcationStrategy))
            endpoint.beforeDelivery(JmsActivation.ONMESSAGE);

         try
         {
            if (dlqHandler == null
                  || dlqHandler.handleRedeliveredMessage(message) == false)
            {
               MessageListener listener = (MessageListener) endpoint;
               listener.onMessage(message);
            }
         } finally
         {
            if (!(txnStrategy != null && txnStrategy instanceof TraditionalXATransactionDemarcationStrategy))
               endpoint.afterDelivery();

            if (dlqHandler != null)
               dlqHandler.messageDelivered(message);
         }
      }

      catch (Throwable t)
      {
         log.error("Unexpected error delivering message " + message, t);

         if (txnStrategy != null)
            txnStrategy.error();

      }

   }

   public Session getSession() throws JMSException
   {
      return session;
   }

   public void start() throws JMSException
   {
      JmsActivation activation = pool.getActivation();
      WorkManager workManager = activation.getWorkManager();
      try
      {
         workManager.scheduleWork(this, 0, null, this);
      } catch (WorkException e)
      {
         log.error("Unable to schedule work", e);
         throw new JMSException("Unable to schedule work: " + e.toString());
      }
   }

   public void run()
   {

      try
      {
         txnStrategy = createTransactionDemarcation();

      } catch (Throwable t)
      {
         log.error("Error creating transaction demarcation. Cannot continue.");
         return;
      }

      try
      {
         session.run();
      } catch (Throwable t)
      {
         if (txnStrategy != null)
            txnStrategy.error();

      } finally
      {
         if (txnStrategy != null)
            txnStrategy.end();

         txnStrategy = null;
      }

   }

   private TransactionDemarcationStrategy createTransactionDemarcation()
   {
      return new DemarcationStrategyFactory().getStrategy();

   }

   public void release()
   {
   }

   public void workAccepted(WorkEvent e)
   {
   }

   public void workCompleted(WorkEvent e)
   {
      pool.returnServerSession(this);
   }

   public void workRejected(WorkEvent e)
   {
      pool.returnServerSession(this);
   }

   public void workStarted(WorkEvent e)
   {
   }

   private class DemarcationStrategyFactory
   {
      TransactionDemarcationStrategy getStrategy()
      {
         TransactionDemarcationStrategy current = null;
         final JmsActivationSpec spec = pool.getActivation().getActivationSpec();
         final JmsActivation activation = pool.getActivation();
         try 
         {
            //If we have a transacted delivery
            if (activation.isDeliveryTransacted())
            {
               //if we have an XASession
               if (xaSession != null)
               {
                  if (spec.isForceTransacted())
                  {
                     current = new XATransactionDemarcationStrategy();
                  }
                  else
                  {
                     current = new TraditionalXATransactionDemarcationStrategy();
                  }
               }
               else  //if we don't have an XASession, simulate it with a transacted session
               {
                  current = new SimulatedXATransactionDemarcationStrategy();
               }
            }
            else
            {
               current = new LocalDemarcationStrategy();
            }
         } 
         catch (Throwable t) 
         {
            log.error(this + " error creating transaction demarcation ", t);
         }

         if (current != null && log.isTraceEnabled())
            log.trace("Using strategy: " + current.getClass().getName());

         return current;
      }
   }

   private interface TransactionDemarcationStrategy
   {
      void error();

      void end();

   }

   /**
    * This class simulates xa using a transacted session for connection factories that don't have an xa interface.  This is true with
    * default ibmmq adapter.  It is not XA, but still needs to be able to have transactions.  So for these connection factories we
    * use transacted sessions to commit and rollback, while monitoring the transaction.
    *   
    * JBAS-6343 - This class is exactly like the XADemarcationStrategy, but it uses a transacted session under the covers.
    * Unfortuneately we have to start a transaction the for local, because we need to be able to get a handle to any failed
    * transactions.  
    * @author jhowell
    *
    */
   private class SimulatedXATransactionDemarcationStrategy implements
			TransactionDemarcationStrategy
	{

		boolean trace = log.isTraceEnabled();

		Transaction trans = null;

		TransactionManager tm = pool.getActivation().getTransactionManager();;

		public SimulatedXATransactionDemarcationStrategy() throws Throwable
		{
			final int timeout = pool.getActivation().getActivationSpec()
					.getTransactionTimeout();

			if (timeout > 0)
			{
				log.trace("Setting transactionTimeout for JMSSessionPool to "
						+ timeout);
				tm.setTransactionTimeout(timeout);

			}

			tm.begin();

			try
			{
				trans = tm.getTransaction();

				if (trace)
					log.trace(JmsServerSession.this + " using tx=" + trans);
				
			} catch (Throwable t)
			{
				try
				{
					tm.rollback();
				} catch (Throwable ignored)
				{
					log.trace(JmsServerSession.this+ " ignored error rolling back after failing to get transaction",	ignored);
				}
				throw t;
			}

		}

		public void error()
		{
			// Mark for tollback TX via TM
			try
			{

				if (trace)
					log.trace(JmsServerSession.this+ " using TM to mark TX for rollback tx=" + trans);
				trans.setRollbackOnly();
				
			} catch (Throwable t)
			{
				log.error(JmsServerSession.this	+ " failed to set rollback only", t);
			}
			//even if the rollback fails on the transaction, we want to rollback the session.
			try
			{
				session.rollback();
			} catch (JMSException e)
			{
				log.error(JmsServerSession.this	+ " failed to rollback the transacted session", e);
			}

		}

		public void end()
		{
			try
			{

				// Use the TM to commit the Tx (assert the correct association)
				Transaction currentTx = tm.getTransaction();
				if (trans.equals(currentTx) == false)
					throw new IllegalStateException(
							"Wrong tx association: expected " + trans + " was "
									+ currentTx);

				// Marked rollback
				if (trans.getStatus() == Status.STATUS_MARKED_ROLLBACK)
				{
					if (trace)
						log.trace(JmsServerSession.this
								+ " rolling back JMS transaction tx=" + trans);
					// actually roll it back
					tm.rollback();
					session.rollback();
				}

				else if (trans.getStatus() == Status.STATUS_ACTIVE)
				{
					// Commit tx
					// This will happen if
					// a) everything goes well
					// b) app. exception was thrown
					if (trace)
						log.trace(JmsServerSession.this	+ " commiting the JMS transaction tx=" + trans);
					tm.commit();
					session.commit();

				} else
				{
					tm.suspend();
					session.rollback();
				}

			} catch (Throwable t)
			{
				log.error(JmsServerSession.this + " failed to commit/rollback",	t);
				//if anything goes wrong with the transaction, we need to rollback the session.
				try
				{
					session.rollback();
				} catch (JMSException e)
				{
					log.error(JmsServerSession.this + " failed to rollback transacted session after transaction failure",	t);
				}
			}

		}

	}
   /**
    * LocalDemaracationStrategy is for anything where the delivery is not marked as transacted.  
    * In CMT the delivery is always marked as transacted.  BMT does not mark the delivery as transacted, but 
    * it does mark the session as transacted.  BMT uses this class with Transacted sessions in order to rollback or commit the
    * message.
    * @author jhowell
    *
    */
   private class LocalDemarcationStrategy implements
			TransactionDemarcationStrategy
	{
		public void end()
		{
			final JmsActivationSpec spec = pool.getActivation()
					.getActivationSpec();

			if (spec.isSessionTransacted())
			{
				if (session != null)
				{
					try
					{
						session.commit();
					} catch (JMSException e)
					{
						log.error("Failed to commit session transaction", e);
					}
				}
			}
		}

		public void error()
		{
			final JmsActivationSpec spec = pool.getActivation()
					.getActivationSpec();

			if (spec.isSessionTransacted())
			{
				if (session != null)

					try
					{
						/*
						 * Looks strange, but this basically means
						 * 
						 * If the underlying connection was non-XA and the
						 * transaction attribute is REQUIRED we rollback. Also,
						 * if the underlying connection was non-XA and the
						 * transaction attribute is NOT_SUPPORT and the non
						 * standard redelivery behavior is enabled we rollback
						 * to force redelivery.
						 */
						if (pool.getActivation().isDeliveryTransacted()
								|| spec.getRedeliverUnspecified())
						{
							session.rollback();
						}

					} catch (JMSException e)
					{
						log.error("Failed to rollback session transaction", e);
					}

			}
		}

	}

	/**
	 * This class is used for XATransactions(ie. CMT) for the mdb message delivery.  It creates a transaction for the message delivery,
	 * enlists the XASession object in the transaction and then after the on message is called, it will commit/rollback the transaction.
	 * @author jhowell
	 *
	 */
    private class XATransactionDemarcationStrategy implements
			TransactionDemarcationStrategy
	{

		boolean trace = log.isTraceEnabled();

		Transaction trans = null;

		TransactionManager tm = pool.getActivation().getTransactionManager();;

		public XATransactionDemarcationStrategy() throws Throwable
		{
			final int timeout = pool.getActivation().getActivationSpec()
					.getTransactionTimeout();

			if (timeout > 0)
			{
				log.trace("Setting transactionTimeout for JMSSessionPool to "
						+ timeout);
				tm.setTransactionTimeout(timeout);

			}

			tm.begin();

			try
			{
				trans = tm.getTransaction();

				if (trace)
					log.trace(JmsServerSession.this + " using tx=" + trans);

				if (xaSession != null)
				{
					XAResource res = xaSession.getXAResource();

					if (!trans.enlistResource(res))
					{
						throw new JMSException("could not enlist resource");
					}
					if (trace)
						log.trace(JmsServerSession.this + " XAResource '" + res
								+ "' enlisted.");
				}
			} catch (Throwable t)
			{
				try
				{
					tm.rollback();
				} catch (Throwable ignored)
				{
					log
							.trace(
									JmsServerSession.this
											+ " ignored error rolling back after failed enlist",
									ignored);
				}
				throw t;
			}

		}

		public void error()
		{
			// Mark for tollback TX via TM
			try
			{

				if (trace)
					log.trace(JmsServerSession.this
							+ " using TM to mark TX for rollback tx=" + trans);
				trans.setRollbackOnly();
			} catch (Throwable t)
			{
				log.error(JmsServerSession.this
						+ " failed to set rollback only", t);
			}

		}

		public void end()
		{
			try
			{

				// Use the TM to commit the Tx (assert the correct association)
				Transaction currentTx = tm.getTransaction();
				if (trans.equals(currentTx) == false)
					throw new IllegalStateException(
							"Wrong tx association: expected " + trans + " was "
									+ currentTx);

				// Marked rollback
				if (trans.getStatus() == Status.STATUS_MARKED_ROLLBACK)
				{
					if (trace)
						log.trace(JmsServerSession.this
								+ " rolling back JMS transaction tx=" + trans);
					// actually roll it back
					tm.rollback();

					// NO XASession? then manually rollback.
					// This is not so good but
					// it's the best we can do if we have no XASession.
					if (xaSession == null
							&& pool.getActivation().isDeliveryTransacted())
					{
						session.rollback();
					}
				}

				else if (trans.getStatus() == Status.STATUS_ACTIVE)
				{
					// Commit tx
					// This will happen if
					// a) everything goes well
					// b) app. exception was thrown
					if (trace)
						log.trace(JmsServerSession.this
								+ " commiting the JMS transaction tx=" + trans);
					tm.commit();

					// NO XASession? then manually commit. This is not so good
					// but
					// it's the best we can do if we have no XASession.
					if (xaSession == null
							&& pool.getActivation().isDeliveryTransacted())
					{
						session.commit();
					}

				} else
				{
					tm.suspend();

					if (xaSession == null
							&& pool.getActivation().isDeliveryTransacted())
					{
						session.rollback();
					}

				}

			} catch (Throwable t)
			{
				log.error(JmsServerSession.this + " failed to commit/rollback",
						t);
			}

		}

	}

   /**
    * This class is used for traditional XATransaction interaction as described in JCA 1.5 12.5.6
    */
   private class TraditionalXATransactionDemarcationStrategy implements TransactionDemarcationStrategy
   {
      boolean trace = log.isTraceEnabled();
      TransactionManager tm = pool.getActivation().getTransactionManager();;

      public TraditionalXATransactionDemarcationStrategy() throws Throwable
      {
         final int timeout = pool.getActivation().getActivationSpec().getTransactionTimeout();
         
         if (timeout > 0)
         {
            log.trace("Setting transactionTimeout for JMSSessionPool to " + timeout);
            tm.setTransactionTimeout(timeout);
         }
      }

      public void error()
      {
      }

      public void end()
      {
      }
   }
}