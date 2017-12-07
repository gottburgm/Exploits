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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ResourceAllocationException;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.SecurityException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.jms.ConnectionFactoryHelper;
import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.logging.Logger;
import org.jboss.resource.JBossResourceException;

/**
 * Managed Connection, manages one or more JMS sessions.
 *
 * <p>Every ManagedConnection will have a physical JMSConnection under the
 *    hood. This may leave out several session, as specifyed in 5.5.4 Multiple
 *    Connection Handles. Thread safe semantics is provided
 *
 * <p>Hm. If we are to follow the example in 6.11 this will not work. We would
 *    have to use the SAME session. This means we will have to guard against
 *    concurrent access. We use a stack, and only allowes the handle at the
 *    top of the stack to do things.
 *
 * <p>As to transactions we some fairly hairy alternatives to handle:
 *    XA - we get an XA. We may now only do transaction through the
 *    XAResource, since a XASession MUST throw exceptions in commit etc. But
 *    since XA support implies LocatTransaction support, we will have to use
 *    the XAResource in the LocalTransaction class.
 *    LocalTx - we get a normal session. The LocalTransaction will then work
 *    against the normal session api.
 *
 * <p>An invokation of JMS MAY BE DONE in none transacted context. What do we
 *    do then? How much should we leave to the user???
 *
 * <p>One possible solution is to use transactions any way, but under the hood.
 *    If not LocalTransaction or XA has been aquired by the container, we have
 *    to do the commit in send and publish. (CHECK is the container required
 *    to get a XA every time it uses a managed connection? No its is not, only
 *    at creation!)
 *
 * <p>Does this mean that a session one time may be used in a transacted env,
 *    and another time in a not transacted.
 *
 * <p>Maybe we could have this simple rule:
 *
 * <p>If a user is going to use non trans:
 * <ul>
 * <li>mark that i ra deployment descr
 * <li>Use a JmsProviderAdapter with non XA factorys
 * <li>Mark session as non transacted (this defeats the purpose of specifying
 * <li>trans attrinbutes in deploy descr NOT GOOD
 * </ul>
 *
 * <p>From the JMS tutorial:
 *    "When you create a session in an enterprise bean, the container ignores
 *    the arguments you specify, because it manages all transactional
 *    properties for enterprise beans."
 *
 * <p>And further:
 *    "You do not specify a message acknowledgment mode when you create a
 *    message-driven bean that uses container-managed transactions. The
 *    container handles acknowledgment automatically."
 *
 * <p>On Session or Connection:
 * <p>From Tutorial:
 *    "A JMS API resource is a JMS API connection or a JMS API session." But in
 *    the J2EE spec only connection is considered a resource.
 *
 * <p>Not resolved: connectionErrorOccurred: it is verry hard to know from the
 *    exceptions thrown if it is a connection error. Should we register an
 *    ExceptionListener and mark al handles as errounous? And then let them
 *    send the event and throw an exception?
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 111797 $
 */
public class JmsManagedConnection
   implements ManagedConnection, ExceptionListener
{
   private static final Logger log = Logger.getLogger(JmsManagedConnection.class);

   private JmsManagedConnectionFactory mcf;
   private JmsConnectionRequestInfo info;
   private String user;
   private String pwd;
   private boolean isDestroyed;

   private ReentrantLock lock = new ReentrantLock(true);
   
   // Physical JMS connection stuff
   private Connection con;
   private Session session;
   private TopicSession topicSession;
   private QueueSession queueSession;
   private XASession xaSession;
   private XATopicSession xaTopicSession;
   private XAQueueSession xaQueueSession;
   private XAResource xaResource;
   private boolean xaTransacted;

   /** Holds all current JmsSession handles. */
   private Set handles = Collections.synchronizedSet(new HashSet());

   /** The event listeners */
   private Vector listeners = new Vector();

   /**
    * Create a <tt>JmsManagedConnection</tt>.
    *
    * @param mcf
    * @param info
    * @param user
    * @param pwd
    *
    * @throws ResourceException
    */
   public JmsManagedConnection(final JmsManagedConnectionFactory mcf,
                               final ConnectionRequestInfo info,
                               final String user,
                               final String pwd)
      throws ResourceException
   {
      this.mcf = mcf;

      // seem like its asking for trouble here
      this.info = (JmsConnectionRequestInfo)info;
      this.user = user;
      this.pwd = pwd;

      try
      {
         setup();
      }
      catch (Throwable t)
      {
         try
         {
            destroy();
         }
         catch (Throwable ignored)
         {
         }
         JBossResourceException.rethrowAsResourceException("Error during setup", t);
      }
   }

   //---- ManagedConnection API ----

   /**
    * Get the physical connection handler.
    *
    * <p>This bummer will be called in two situations:
    * <ol>
    * <li>When a new mc has bean created and a connection is needed
    * <li>When an mc has been fetched from the pool (returned in match*)
    * </ol>
    *
    * <p>It may also be called multiple time without a cleanup, to support
    *    connection sharing.
    *
    * @param subject
    * @param info
    * @return           A new connection object.
    *
    * @throws ResourceException
    */
   public Object getConnection(final Subject subject,
                               final ConnectionRequestInfo info)
      throws ResourceException
   {
      // Check user first
      JmsCred cred = JmsCred.getJmsCred(mcf,subject,info);

      // Null users are allowed!
      if (user != null && !user.equals(cred.name))
         throw new SecurityException
            ("Password credentials not the same, reauthentication not allowed");
      if (cred.name != null && user == null) {
         throw new SecurityException
            ("Password credentials not the same, reauthentication not allowed");
      }

      user = cred.name; // Basically meaningless

      if (isDestroyed)
         throw new IllegalStateException("ManagedConnection already destroyd");

      // Create a handle
      JmsSession handle = new JmsSession(this, (JmsConnectionRequestInfo) info);
      handles.add(handle);
      return handle;
   }

   /**
    * Destroy all handles.
    *
    * @throws ResourceException    Failed to close one or more handles.
    */
   private void destroyHandles() throws ResourceException
   {
      try
      {
         if (con != null)
            con.stop();  
      }
      catch (Throwable t)
      {
         log.trace("Ignored error stopping connection", t);
      }
      
      Iterator iter = handles.iterator();
      while (iter.hasNext())
         ((JmsSession)iter.next()).destroy();

      // clear the handles map
      handles.clear();
   }

   /**
    * Destroy the physical connection.
    *
    * @throws ResourceException    Could not property close the session and
    *                              connection.
    */
   public void destroy() throws ResourceException
   {
      if (isDestroyed || con == null) return;

      isDestroyed = true;

      try
      {
         con.setExceptionListener(null);
      }
      catch (JMSException e)
      {
         log.debug("Error unsetting the exception listener " + this, e);
      }
      
      // destory handles
      destroyHandles();
      
      try
      {
         // Close session and connection
         try
         {
            if (info.getType() == JmsConnectionFactory.TOPIC)
            {
               if (topicSession != null)
                  topicSession.close();
               if (xaTransacted && xaTopicSession != null) {
                  xaTopicSession.close();
               }
            }
            else if (info.getType() == JmsConnectionFactory.QUEUE)
            {
               if (queueSession != null)
                  queueSession.close();
               if (xaTransacted && xaQueueSession != null)
                  xaQueueSession.close();
            }
            else
            {
               if (session != null)
                  session.close();
               if (xaTransacted && xaSession != null)
                  xaSession.close();
            }
         }
         catch (JMSException e)
         {
            log.debug("Error closing session " +this, e);
         }
         con.close();
      }
      catch (Throwable e)
      {
         throw new JBossResourceException
            ("Could not properly close the session and connection", e);
      }
   }

   /**
    * Cleans up the, from the spec
    *  - The cleanup of ManagedConnection instance resets its client specific
    *    state.
    *
    * Does that mean that autentication should be redone. FIXME
    */
   public void cleanup() throws ResourceException
   {
      if (isDestroyed)
         throw new IllegalStateException("ManagedConnection already destroyed");

      // destory handles
      destroyHandles();

      boolean isActive = false;

      if (lock.hasQueuedThreads())
      {
         Collection<Thread> threads = lock.getQueuedThreads();
         for (Thread thread : threads)
         {
            Throwable t = new Throwable("Thread waiting for lock during cleanup");
            t.setStackTrace(thread.getStackTrace());

            log.warn(t.getMessage(), t);
         }

         isActive = true;
      }

      if (lock.isLocked())
      {
         Throwable t = new Throwable("Lock owned during cleanup");
         t.setStackTrace(lock.getOwner().getStackTrace());

         log.warn(t.getMessage(), t);
         
         isActive = true;
      }

      if (isActive)
      {
         // There are active lock - make sure that the JCA container kills
         // this handle by throwing an exception

         throw new ResourceException("Still active locks for " + this);
      }
   }

   /**
    * Move a handler from one mc to this one.
    *
    * @param obj   An object of type JmsSession.
    *
    * @throws ResourceException        Failed to associate connection.
    * @throws IllegalStateException    ManagedConnection in an illegal state.
    */
   public void associateConnection(final Object obj)
      throws ResourceException
   {
      //
      // Should we check auth, ie user and pwd? FIXME
      //

      if (!isDestroyed && obj instanceof JmsSession)
      {
         JmsSession h = (JmsSession)obj;
         h.setManagedConnection(this);
         handles.add(h);
      }
      else
         throw new IllegalStateException
            ("ManagedConnection in an illegal state");
   }

   protected void lock()
   {
      lock.lock();
   }

   protected void tryLock() throws JMSException
   {
      int tryLock = mcf.getUseTryLock();
      if (tryLock <= 0)
      {
         lock();
         return;
      }
      try
      {
         if (lock.tryLock(tryLock, TimeUnit.SECONDS) == false)
            throw new ResourceAllocationException("Unable to obtain lock in " + tryLock + " seconds: " + this);
      }
      catch (InterruptedException e)
      {
         throw new ResourceAllocationException("Interrupted attempting lock: " + this);
      }
   }
   
   protected void unlock()
   {
      if (lock.isLocked())
      {
         lock.unlock();
      }
      else
      {
         log.warn("Owner is null");            
         
         Throwable t = new Throwable("Thread trying to unlock");
         t.setStackTrace(Thread.currentThread().getStackTrace());

         log.warn(t.getMessage(), t);
      }
   }

   /**
    * Add a connection event listener.
    *
    * @param l   The connection event listener to be added.
    */
   public void addConnectionEventListener(final ConnectionEventListener l)
   {
      listeners.addElement(l);

      if (log.isTraceEnabled())
         log.trace("ConnectionEvent listener added: " + l);
   }

   /**
    * Remove a connection event listener.
    *
    * @param l    The connection event listener to be removed.
    */
   public void removeConnectionEventListener(final ConnectionEventListener l)
   {
      listeners.removeElement(l);
   }

   /**
    * Get the XAResource for the connection.
    *
    * @return   The XAResource for the connection.
    *
    * @throws ResourceException    XA transaction not supported
    */
   public XAResource getXAResource() throws ResourceException
   {
      //
      // Spec says a mc must allways return the same XA resource,
      // so we cache it.
      //
      if (!xaTransacted)
         throw new NotSupportedException("Non XA transaction not supported");

      if (xaResource == null)
      {
         if (info.getType() == JmsConnectionFactory.TOPIC)
            xaResource = xaTopicSession.getXAResource();
         else if (info.getType() == JmsConnectionFactory.QUEUE)
            xaResource = xaQueueSession.getXAResource();
         else
            xaResource = xaSession.getXAResource();
      }

      if (log.isTraceEnabled())
         log.trace("XAResource=" + xaResource);

      xaResource = new JmsXAResource(this, xaResource);
      return xaResource;
   }

   /**
    * Get the location transaction for the connection.
    *
    * @return    The local transaction for the connection.
    *
    * @throws ResourceException
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      LocalTransaction tx = new JmsLocalTransaction(this);
      if (log.isTraceEnabled())
         log.trace("LocalTransaction=" + tx);
      return tx;
   }

   /**
    * Get the meta data for the connection.
    *
    * @return    The meta data for the connection.
    *
    * @throws ResourceException
    * @throws IllegalStateException    ManagedConnection already destroyed.
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      if (isDestroyed)
         throw new IllegalStateException("ManagedConnection already destroyd");

      return new JmsMetaData(this);
   }

   /**
    * Set the log writer for this connection.
    *
    * @param out   The log writer for this connection.
    *
    * @throws ResourceException
    */
   public void setLogWriter(final PrintWriter out) throws ResourceException
   {
      //
      // jason: screw the logWriter stuff for now it sucks ass
      //
   }

   /**
    * Get the log writer for this connection.
    *
    * @return   Always null
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      //
      // jason: screw the logWriter stuff for now it sucks ass
      //

      return null;
   }

   // --- Exception listener implementation
   
   public void onException(JMSException exception)
   {
      if (isDestroyed)
      {
         if (log.isTraceEnabled())
            log.trace("Ignoring error on already destroyed connection " + this, exception);
         return;
      }

      log.warn("Handling jms exception failure: " + this, exception);

      // We need to unlock() before sending the connection error to the
      // event listeners. Otherwise the lock won't be in sync once
      // cleanup() is called
      if (lock.isLocked() && Thread.currentThread().equals(lock.getOwner()))
         unlock();

      try
      {
         con.setExceptionListener(null);
      }
      catch (JMSException e)
      {
         log.debug("Unable to unset exception listener", e);
      }
      
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, exception);
      sendEvent(event);
   }
   
   // --- Api to JmsSession

   /**
    * Get the session for this connection.
    *
    * @return   Either a topic or queue connection.
    */
   protected Session getSession()
   {
      if (info.getType() == JmsConnectionFactory.TOPIC)
         return topicSession;
      else if (info.getType() == JmsConnectionFactory.QUEUE)
         return queueSession;
      else
         return session;
   }

   /**
    * Send an event.
    *
    * @param event    The event to send.
    */
   protected void sendEvent(final ConnectionEvent event)
   {
      int type = event.getId();

      if (log.isTraceEnabled())
         log.trace("Sending connection event: " + type);

      // convert to an array to avoid concurrent modification exceptions
      ConnectionEventListener[] list =
         (ConnectionEventListener[])listeners.toArray(new ConnectionEventListener[listeners.size()]);

      for (int i=0; i<list.length; i++)
      {
         switch (type) {
            case ConnectionEvent.CONNECTION_CLOSED:
               list[i].connectionClosed(event);
               break;

            case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
               list[i].localTransactionStarted(event);
               break;

            case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
               list[i].localTransactionCommitted(event);
               break;

            case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
               list[i].localTransactionRolledback(event);
               break;

            case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
               list[i].connectionErrorOccurred(event);
               break;

            default:
               throw new IllegalArgumentException("Illegal eventType: " + type);
         }
      }
   }

   /**
    * Remove a handle from the handle map.
    *
    * @param handle     The handle to remove.
    */
   protected void removeHandle(final JmsSession handle)
   {
      handles.remove(handle);
   }

   // --- Used by MCF

   /**
    * Get the request info for this connection.
    *
    * @return    The request info for this connection.
    */
   protected ConnectionRequestInfo getInfo()
   {
      return info;
   }

   /**
    * Get the connection factory for this connection.
    *
    * @return    The connection factory for this connection.
    */
   protected JmsManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }

   void start() throws JMSException
   {
      con.start();
   }

   void stop() throws JMSException
   {
      con.stop();
   }
   
   // --- Used by MetaData

   /**
    * Get the user name for this connection.
    *
    * @return    The user name for this connection.
    */
   protected String getUserName()
   {
      return user;
   }

   // --- Private helper methods

   /**
    * Get the JMS provider adapter that will be used to create JMS
    * resources.
    *
    * @return    A JMS provider adapter.
    *
    * @throws NamingException    Failed to lookup provider adapter.
    */
   private JMSProviderAdapter getProviderAdapter() throws NamingException
   {
      JMSProviderAdapter adapter;

      if (mcf.getJmsProviderAdapterJNDI() != null)
      {
         // lookup the adapter from JNDI
         Context ctx = new InitialContext();
         try
         {
            adapter = (JMSProviderAdapter)
               ctx.lookup(mcf.getJmsProviderAdapterJNDI());
         }
         finally
         {
            ctx.close();
         }
      }
      else
         adapter = mcf.getJmsProviderAdapter();

      return adapter;
   }

   /**
    * Setup the connection.
    *
    * @throws ResourceException
    */
   private void setup() throws ResourceException
   {
      boolean trace = log.isTraceEnabled();

      try
      {
         JMSProviderAdapter adapter = getProviderAdapter();
         Context context = adapter.getInitialContext();
         Object factory;
         boolean transacted = info.isTransacted();
         int ack = Session.AUTO_ACKNOWLEDGE;

         if (info.getType() == JmsConnectionFactory.TOPIC)
         {
            String jndi = adapter.getTopicFactoryRef();
            if (jndi == null)
               throw new IllegalStateException("No configured 'TopicFactoryRef' on the jms provider " + mcf.getJmsProviderAdapterJNDI());
            factory = context.lookup(jndi);
            con = ConnectionFactoryHelper.createTopicConnection(factory, user, pwd);
            if (info.getClientID() != null)
               con.setClientID(info.getClientID());
            con.setExceptionListener(this);
            if (trace)
               log.trace("created connection: " + con);

            if (con instanceof XATopicConnection)
            {
               xaTopicSession = ((XATopicConnection)con).createXATopicSession();
               topicSession = xaTopicSession.getTopicSession();
               xaTransacted = true;
            }
            else if (con instanceof TopicConnection)
            {
               topicSession =
                  ((TopicConnection)con).createTopicSession(transacted, ack);
               if (trace)
                  log.trace("Using a non-XA TopicConnection.  " +
                            "It will not be able to participate in a Global UOW");
            }
            else
               throw new JBossResourceException("Connection was not recognizable: " + con);

            if (trace)
               log.trace("xaTopicSession=" + xaTopicSession + ", topicSession=" + topicSession);
         }
         else if (info.getType() == JmsConnectionFactory.QUEUE)
         {
            String jndi = adapter.getQueueFactoryRef();
            if (jndi == null)
               throw new IllegalStateException("No configured 'QueueFactoryRef' on the jms provider " + mcf.getJmsProviderAdapterJNDI());
            factory = context.lookup(jndi);
            con = ConnectionFactoryHelper.createQueueConnection(factory, user, pwd);
            if (info.getClientID() != null)
               con.setClientID(info.getClientID());
            con.setExceptionListener(this);
            if (trace) 
               log.debug("created connection: " + con);

            if (con instanceof XAQueueConnection)
            {
               xaQueueSession =
                  ((XAQueueConnection)con).createXAQueueSession();
               queueSession = xaQueueSession.getQueueSession();
               xaTransacted = true;
            }
            else if (con instanceof QueueConnection)
            {
               queueSession =
                  ((QueueConnection)con).createQueueSession(transacted, ack);
               if (trace)
                  log.trace("Using a non-XA QueueConnection.  " +
                            "It will not be able to participate in a Global UOW");
            }
            else
               throw new JBossResourceException("Connection was not reconizable: " + con);

            if (trace)
               log.trace("xaQueueSession=" + xaQueueSession + ", queueSession=" + queueSession);
         }
         else
         {
            String jndi = adapter.getFactoryRef();
            if (jndi == null)
               throw new IllegalStateException("No configured 'FactoryRef' on the jms provider " + mcf.getJmsProviderAdapterJNDI());
            factory = context.lookup(jndi);
            con = ConnectionFactoryHelper.createConnection(factory, user, pwd);
            if (info.getClientID() != null)
               con.setClientID(info.getClientID());
            con.setExceptionListener(this);
            if (trace) 
               log.trace("created connection: " + con);

            if (con instanceof XAConnection)
            {
               xaSession =
                  ((XAConnection)con).createXASession();
               session = xaSession.getSession();
               xaTransacted = true;
            }
            else
            {
               session = con.createSession(transacted, ack);
               if (trace)
                  log.trace("Using a non-XA Connection.  " +
                            "It will not be able to participate in a Global UOW");
            }

            if (trace)
               log.debug("xaSession=" + xaQueueSession + ", Session=" + session);
         }

         if (trace)
            log.debug("transacted=" + transacted + ", ack=" + ack);
      }
      catch (NamingException e)
      {
         throw new JBossResourceException("Unable to setup connection", e);
      }
      catch (JMSException e)
      {
         throw new JBossResourceException("Unable to setup connection", e);
      }
   }
}
