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
package org.jboss.resource.connectionmanager;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.logging.util.LoggerPluginWriter;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.resource.JBossResourceException;
import org.jboss.security.SubjectFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionTimeoutConfiguration;
import org.jboss.util.NestedRuntimeException;
import org.jboss.util.NotImplementedException;

/**
 * The BaseConnectionManager2 is an abstract base class for JBoss ConnectionManager
 * implementations.  It includes functionality to obtain managed connections from
 * a ManagedConnectionPool mbean, find the Subject from a SubjectSecurityDomain,
 * and interact with the CachedConnectionManager for connections held over
 * transaction and method boundaries.  Important mbean references are to a
 * ManagedConnectionPool supplier (typically a JBossManagedConnectionPool), and a
 * RARDeployment representing the ManagedConnectionFactory.
 *
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:E.Guib@ceyoniq.com">Erwin Guib</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @author Anil.Saldhana@redhat.com
 * 
 * @version $Revision: 86985 $
 */
@SuppressWarnings("unchecked")
public abstract class BaseConnectionManager2 extends ServiceMBeanSupport
      implements
         BaseConnectionManager2MBean,
         ConnectionCacheListener,
         ConnectionListenerFactory,
         TransactionTimeoutConfiguration,
         JTATransactionChecker
{
   /**
    * Note that this copy has a trailing / unlike the original in
    * JaasSecurityManagerService.
    */
   private static final String SECURITY_MGR_PATH = "java:/jaas/";

   public static final String STOPPING_NOTIFICATION = "jboss.jca.connectionmanagerstopping";

   protected ObjectName managedConnectionPoolName;

   protected ManagedConnectionPool poolingStrategy;

   protected String jndiName;

   protected String securityDomainJndiName;
   
   protected SubjectFactory subjectFactory;
   
   protected ObjectName jaasSecurityManagerService;

   protected ObjectName ccmName;

   protected CachedConnectionManager ccm;

   protected boolean trace;

   protected int allocationRetry;

   protected long allocationRetryWaitMillis;

   protected AtomicBoolean shutdown = new AtomicBoolean(false);

   /**
    * Rethrow a throwable as resource exception
    * 
    * @deprecated use JBossResourceException.rethrowAsResourceException
    */
   protected static void rethrowAsResourceException(String message, Throwable t) throws ResourceException
   {
      JBossResourceException.rethrowAsResourceException(message, t);
   }

   /**
    * Default BaseConnectionManager2 managed constructor for use by subclass mbeans.
    */
   public BaseConnectionManager2()
   {
      super();
      trace = log.isTraceEnabled();
   }

   /**
    * Creates a new <code>BaseConnectionManager2</code> instance.
    * for TESTING ONLY! not a managed operation.
    * @param ccm a <code>CachedConnectionManager</code> value
    * @param poolingStrategy a <code>ManagedConnectionPool</code> value
    */
   public BaseConnectionManager2(CachedConnectionManager ccm, ManagedConnectionPool poolingStrategy)
   {
      super();
      this.ccm = ccm;
      this.poolingStrategy = poolingStrategy;
      trace = log.isTraceEnabled();
   }

   /**
    * For testing
    */
   public ManagedConnectionPool getPoolingStrategy()
   {
      return poolingStrategy;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public ObjectName getManagedConnectionPool()
   {
      return managedConnectionPoolName;
   }

   public void setManagedConnectionPool(ObjectName newManagedConnectionPool)
   {
      this.managedConnectionPoolName = newManagedConnectionPool;
   }

   public void setCachedConnectionManager(ObjectName ccmName)
   {
      this.ccmName = ccmName;
   }

   public ObjectName getCachedConnectionManager()
   {
      return ccmName;
   }

   public void setSecurityDomainJndiName(String securityDomainJndiName)
   {
      if (securityDomainJndiName != null && securityDomainJndiName.startsWith(SECURITY_MGR_PATH))
      {
         securityDomainJndiName = securityDomainJndiName.substring(SECURITY_MGR_PATH.length());
         log.warn("WARNING: UPDATE YOUR SecurityDomainJndiName! REMOVE " + SECURITY_MGR_PATH);
      }
      this.securityDomainJndiName = securityDomainJndiName;
   }

   public String getSecurityDomainJndiName()
   {
      return securityDomainJndiName;
   }
 
   public SubjectFactory getSubjectFactory()
   {
      return subjectFactory;
   }

   public void setSubjectFactory(SubjectFactory subjectFactory)
   {
      this.subjectFactory = subjectFactory;
   }

   /**
    * @deprecated
    */
   public ObjectName getJaasSecurityManagerService()
   {
      return this.jaasSecurityManagerService; 
   }

   /**
    * @deprecated  Maintained for legacy
    */
   public void setJaasSecurityManagerService(final ObjectName jaasSecurityManagerService)
   {   
      this.jaasSecurityManagerService = jaasSecurityManagerService;
   }

   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return poolingStrategy.getManagedConnectionFactory();
   }

   public BaseConnectionManager2 getInstance()
   {
      return this;
   }

   /**
    * Set the number of allocation retries
    * @param number
    */
   public void setAllocationRetry(int number)
   {
      if (number >= 0)
         allocationRetry = number;
   }

   /**
    * Get the number of allocation retries
    * @return The number of retries
    */
   public int getAllocationRetry()
   {
      return allocationRetry;
   }

   /**
    * Set the wait time between each allocation retry
    * @param millis
    */
   public void setAllocationRetryWaitMillis(long millis)
   {
      if (millis > 0)
         allocationRetryWaitMillis = millis;
   }

   /**
    * Get the wait time between each allocation retry
    * @return The millis
    */
   public long getAllocationRetryWaitMillis()
   {
      return allocationRetryWaitMillis;
   }

   public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
   {
      return -1;
   }

   public int getTransactionTimeout() throws SystemException
   {
      throw new NotImplementedException("NYI: getTransactionTimeout()");
   }

   public void checkTransactionActive() throws RollbackException, SystemException
   {
      // Nothing
   }

   //ServiceMBeanSupport

   protected void startService() throws Exception
   {
      try
      {
         ccm = (CachedConnectionManager) server.getAttribute(ccmName, "Instance");
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
      }

      if (ccm == null)
         throw new DeploymentException("cached ConnectionManager not found: " + ccmName);

      if (managedConnectionPoolName == null)
         throw new DeploymentException("managedConnectionPool not set!");
      try
      {
         poolingStrategy = (ManagedConnectionPool) server.getAttribute(managedConnectionPoolName,
               "ManagedConnectionPool");
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
      }

      poolingStrategy.setConnectionListenerFactory(this);

      // Give it somewhere to tell people things
      String categoryName = poolingStrategy.getManagedConnectionFactory().getClass().getName() + "." + jndiName;
      Logger log = Logger.getLogger(categoryName);
      PrintWriter logWriter = new LoggerPluginWriter(log.getLoggerPlugin());
      try
      {
         poolingStrategy.getManagedConnectionFactory().setLogWriter(logWriter);
      }
      catch (ResourceException re)
      {
         log.warn("Unable to set log writer '" + logWriter + "' on " + "managed connection factory", re);
         log.warn("Linked exception:", re.getLinkedException());
      }
      
      if (poolingStrategy instanceof PreFillPoolSupport)
      {
         PreFillPoolSupport prefill = (PreFillPoolSupport) poolingStrategy;
         
         if (prefill.shouldPreFill())
            prefill.prefill();         
      }
   
      shutdown.set(false);
   }
   
   protected void stopService() throws Exception
   {
      shutdown.set(true);

      //notify the login modules the mcf is going away, they need to look it up again later.
      sendNotification(new Notification(STOPPING_NOTIFICATION, getServiceName(), getNextNotificationSequenceNumber()));
      /*
       * if (jaasSecurityManagerService != null && securityDomainJndiName != null)
         server.invoke(jaasSecurityManagerService, "flushAuthenticationCache", new Object[] { securityDomainJndiName }, new String[] { String.class.getName() });
       */
      poolingStrategy.setConnectionListenerFactory(null);

      poolingStrategy = null;
      subjectFactory = null;
      ccm = null;
   }

   /**
    * Public for use in testing pooling functionality by itself.
    * called by both allocateConnection and reconnect.
    * 
    * @param subject a <code>Subject</code> value
    * @param cri a <code>ConnectionRequestInfo</code> value
    * @return a <code>ManagedConnection</code> value
    * @exception ResourceException if an error occurs
    */
   public ConnectionListener getManagedConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      return getManagedConnection(null, subject, cri);
   }

   /**
    * Get the managed connection from the pool
    * 
    * @param transaction the transaction for track by transaction
    * @param subject the subject
    * @param cri the ConnectionRequestInfo
    * @return a managed connection
    * @exception ResourceException if an error occurs
    */
   protected ConnectionListener getManagedConnection(Transaction transaction, Subject subject, ConnectionRequestInfo cri)
         throws ResourceException
   {
      ResourceException failure = null;

      if (shutdown.get())
         throw new ResourceException("The connection manager is shutdown " + jndiName);
      
      // First attempt
      try
      {
         return poolingStrategy.getConnection(transaction, subject, cri);
      }
      catch (ResourceException e)
      {
         failure = e;
         
         // Retry?
         if (allocationRetry != 0)
         {
            for (int i = 0; i < allocationRetry; i++)
            {
               if (shutdown.get())
                  throw new ResourceException("The connection manager is shutdown " + jndiName);

               if (trace)
                  log.trace("Attempting allocation retry for cri=" + cri);

               try
               {
                  if (allocationRetryWaitMillis != 0)
                     Thread.sleep(allocationRetryWaitMillis);

                  return poolingStrategy.getConnection(transaction, subject, cri);
               }
               catch (ResourceException e1)
               {
                  failure = e1;
               }
               catch (InterruptedException e1)
               {
                  JBossResourceException.rethrowAsResourceException("getManagedConnection retry wait was interrupted " + jndiName, e1);
               }
            }
         }
      }

      // If we get here all retries failed, throw the lastest failure
      throw new ResourceException("Unable to get managed connection for " + jndiName, failure);
   }
   
   public void returnManagedConnection(ConnectionListener cl, boolean kill)
   {
      ManagedConnectionPool localStrategy = cl.getManagedConnectionPool();
      if (localStrategy != poolingStrategy)
         kill = true;

      try
      {
         if (kill == false && cl.getState() == ConnectionListener.NORMAL)
            cl.tidyup();
      }
      catch (Throwable t)
      {
         log.warn("Error during tidyup " + cl, t);
         kill = true;
      }
      
      try
      {
         localStrategy.returnConnection(cl, kill);
      }
      catch (ResourceException re)
      {
         // We can receive notification of an error on the connection
         // before it has been assigned to the pool. Reduce the noise for
         // these errors
         if (kill)
            log.debug("resourceException killing connection (error retrieving from pool?)", re);
         else
            log.warn("resourceException returning connection: " + cl.getManagedConnection(), re);
      }
   }

   public int getConnectionCount()
   {
      return poolingStrategy.getConnectionCount();
   }

   // implementation of javax.resource.spi.ConnectionManager interface

   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException
   {
      if (poolingStrategy == null)
         throw new ResourceException(
               "You are trying to use a connection factory that has been shut down: ManagedConnectionFactory is null.");

      //it is an explicit spec requirement that equals be used for matching rather than ==.
      if (!poolingStrategy.getManagedConnectionFactory().equals(mcf))
         throw new ResourceException("Wrong ManagedConnectionFactory sent to allocateConnection!");

      // Pick a managed connection from the pool
      Subject subject = getSubject();
      ConnectionListener cl = getManagedConnection(subject, cri);

      // Tell each connection manager the managed connection is active
      reconnectManagedConnection(cl);

      // Ask the managed connection for a connection
      Object connection = null;
      try
      {
         connection = cl.getManagedConnection().getConnection(subject, cri);
      }
      catch (Throwable t)
      {
         try {
            managedConnectionDisconnected(cl);
         }
         catch (ResourceException re)
         {
            log.trace("Get exception from managedConnectionDisconnected, maybe delist() have problem" + re);
            returnManagedConnection(cl, true);
         }
         JBossResourceException.rethrowAsResourceException(
               "Unchecked throwable in ManagedConnection.getConnection() cl=" + cl, t);
      }

      // Associate managed connection with the connection
      registerAssociation(cl, connection);
      if (ccm != null)
         ccm.registerConnection(this, cl, connection, cri);
      return connection;
   }

   // ConnectionCacheListener implementation

   public void transactionStarted(Collection conns) throws SystemException
   {
      //reimplement in subclasses
   }

   public void reconnect(Collection conns, Set unsharableResources) throws ResourceException
   {
      // if we have an unshareable connection the association was not removed
      // nothing to do
      if (unsharableResources.contains(jndiName))
      {
         log.trace("reconnect for unshareable connection: nothing to do");
         return;
      }

      Map criToCLMap = new HashMap();
      for (Iterator i = conns.iterator(); i.hasNext();)
      {
         ConnectionRecord cr = (ConnectionRecord) i.next();
         if (cr.cl != null)
         {
            //This might well be an error.
            log.warn("reconnecting a connection handle that still has a managedConnection! "
                  + cr.cl.getManagedConnection() + " " + cr.connection);
         }
         ConnectionListener cl = (ConnectionListener) criToCLMap.get(cr.cri);
         if (cl == null)
         {
            cl = getManagedConnection(getSubject(), cr.cri);
            criToCLMap.put(cr.cri, cl);
            //only call once per managed connection, when we get it.
            reconnectManagedConnection(cl);
         }

         cl.getManagedConnection().associateConnection(cr.connection);
         registerAssociation(cl, cr.connection);
         cr.setConnectionListener(cl);
      }
      criToCLMap.clear();//not needed logically, might help the gc.
   }

   public void disconnect(Collection crs, Set unsharableResources) throws ResourceException
   {
      // if we have an unshareable connection do not remove the association
      // nothing to do
      if (unsharableResources.contains(jndiName))
      {
         log.trace("disconnect for unshareable connection: nothing to do");
         return;
      }

      Set cls = new HashSet();
      for (Iterator i = crs.iterator(); i.hasNext();)
      {
         ConnectionRecord cr = (ConnectionRecord) i.next();
         ConnectionListener cl = cr.cl;
         cr.setConnectionListener(null);
         unregisterAssociation(cl, cr.connection);
         if (!cls.contains(cl))
         {
            cls.add(cl);
         }
      }
      for (Iterator i = cls.iterator(); i.hasNext();)
         disconnectManagedConnection((ConnectionListener) i.next());
   }

   // implementation of javax.management.NotificationBroadcaster interface

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // TODO: implement this javax.management.NotificationBroadcaster method
      return super.getNotificationInfo();
   }

   //protected methods

   //does NOT put the mc back in the pool if no more handles. Doing so would introduce a race condition
   //whereby the mc got back in the pool while still enlisted in the tx.
   //The mc could be checked out again and used before the delist occured.
   protected void unregisterAssociation(ConnectionListener cl, Object c)
   {
      cl.unregisterConnection(c);
   }

   /**
    * Invoked to reassociate a managed connection
    * 
    * @param cl the managed connection
    */
   protected void reconnectManagedConnection(ConnectionListener cl) throws ResourceException
   {
      try
      {
         //WRONG METHOD NAME!!
         managedConnectionReconnected(cl);
      }
      catch (Throwable t)
      {
         disconnectManagedConnection(cl);
         JBossResourceException.rethrowAsResourceException("Unchecked throwable in managedConnectionReconnected() cl="
               + cl, t);
      }
   }

   /**
    * Invoked when a managed connection is no longer associated
    * 
    * @param cl the managed connection
    */
   protected void disconnectManagedConnection(ConnectionListener cl)
   {
      try
      {
         managedConnectionDisconnected(cl);
      }
      catch (Throwable t)
      {
         log.warn("Unchecked throwable in managedConnectionDisconnected() cl=" + cl, t);
      }
   }

   protected final CachedConnectionManager getCcm()
   {
      return ccm;
   }

   /**
    * For polymorphism.<p>
    * 
    * Do not invoke directly use reconnectManagedConnection
    * which does the relevent exception handling
    */
   protected void managedConnectionReconnected(ConnectionListener cl) throws ResourceException
   {
   }

   /**
    * For polymorphism.<p>
    * 
    * Do not invoke directly use disconnectManagedConnection
    * which does the relevent exception handling
    */
   protected void managedConnectionDisconnected(ConnectionListener cl) throws ResourceException
   {
   }

   private void registerAssociation(ConnectionListener cl, Object c) throws ResourceException
   {
      cl.registerConnection(c);
   }

   private Subject getSubject()
   {
      Subject subject = null;
      if(subjectFactory != null && securityDomainJndiName != null)
      {
         subject = subjectFactory.createSubject(securityDomainJndiName);
      } 
      if (trace)
         log.trace("subject: " + subject);
      return subject;
   }

   // ConnectionListenerFactory

   public boolean isTransactional()
   {
      return false;
   }

   public TransactionManager getTransactionManagerInstance()
   {
      return null;
   }

   //ConnectionListener

   protected abstract class BaseConnectionEventListener implements ConnectionListener
   {
      private final ManagedConnection mc;

      private final ManagedConnectionPool mcp;

      private final Object context;

      private int state = NORMAL;

      private final List handles = new LinkedList();

      private long lastUse;

      private AtomicBoolean trackByTx = new AtomicBoolean(false);

      private boolean permit = false;

      protected Logger log;

      protected boolean trace;
      
      protected long lastValidated;
      

      protected BaseConnectionEventListener(ManagedConnection mc, ManagedConnectionPool mcp, Object context, Logger log)
      {
         this.mc = mc;
         this.mcp = mcp;
         this.context = context;
         this.log = log;
         trace = log.isTraceEnabled();
         lastUse = System.currentTimeMillis();
      }

      public ManagedConnection getManagedConnection()
      {
         return mc;
      }

      public ManagedConnectionPool getManagedConnectionPool()
      {
         return mcp;
      }

      public Object getContext()
      {
         return context;
      }

      public int getState()
      {
         return state;
      }

      public void setState(int newState)
      {
         this.state = newState;
      }

      public boolean isTimedOut(long timeout)
      {
         return lastUse < timeout;
      }

      public void used()
      {
         lastUse = System.currentTimeMillis();
      }

      public boolean isTrackByTx()
      {
         return trackByTx.get();
      }

      public void setTrackByTx(boolean trackByTx)
      {
         this.trackByTx.set(trackByTx);
      }

      public void tidyup() throws ResourceException
      {
      }

      public synchronized void registerConnection(Object handle)
      {
         handles.add(handle);
      }

      public synchronized void unregisterConnection(Object handle)
      {
         if (!handles.remove(handle))
         {
            log.info("Unregistered handle that was not registered! " + handle + " for managedConnection: " + mc);
         }
         if (trace)
            log.trace("unregisterConnection: " + handles.size() + " handles left");
      }

      public synchronized boolean isManagedConnectionFree()
      {
         return handles.isEmpty();
      }

      protected synchronized void unregisterConnections()
      {
         try
         {
            for (Iterator i = handles.iterator(); i.hasNext();)
            {
               getCcm().unregisterConnection(BaseConnectionManager2.this, i.next());
            }
         }
         finally
         {
            handles.clear();
         }
      }

      public void connectionErrorOccurred(ConnectionEvent ce)
      {
         if (state == NORMAL)
         {
            if (ce != null)
            {
               Throwable t = ce.getException();
               if (t == null)
                  t = new Exception("No exception was reported");
               log.warn("Connection error occured: " + this, t);
            }
            else
            {
               Throwable t = new Exception("No exception was reported");
               log.warn("Unknown Connection error occured: " + this, t);
            }
         }
         try
         {
            unregisterConnections();
         }
         catch (Throwable t)
         {
            //ignore, it wasn't checked out.
         }
         if (ce != null && ce.getSource() != getManagedConnection())
            log.warn("Notified of error on a different managed connection?");
         returnManagedConnection(this, true);
      }

      public void enlist() throws SystemException
      {
      }

      public void delist() throws ResourceException
      {
      }

      public boolean hasPermit()
      {
         return permit;
      }

      public void grantPermit(boolean value)
      {
         this.permit = value;
      }
      
      public long getLastValidatedTime()
      {
         return this.lastValidated;
      }
      
      public void setLastValidatedTime(long lastValidated)
      {
         this.lastValidated = lastValidated;
      }
      
      // For debugging
      public String toString()
      {
         StringBuffer buffer = new StringBuffer(100);
         buffer.append(getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)));
         buffer.append("[state=");
         if (state == ConnectionListener.NORMAL)
            buffer.append("NORMAL");
         else if (state == ConnectionListener.DESTROY)
            buffer.append("DESTROY");
         else if (state == ConnectionListener.DESTROYED)
            buffer.append("DESTROYED");
         else
            buffer.append("UNKNOWN?");
         buffer.append(" mc=").append(mc);
         buffer.append(" handles=").append(handles.size());
         buffer.append(" lastUse=").append(lastUse);
         buffer.append(" permit=").append(permit);
         buffer.append(" trackByTx=").append(trackByTx.get());
         buffer.append(" mcp=").append(mcp);
         buffer.append(" context=").append(context);
         toString(buffer);
         buffer.append(']');
         return buffer.toString();
      }

      // For debugging
      protected void toString(StringBuffer buffer)
      {
      }
   }

   public static class ConnectionManagerProxy
         implements
            ConnectionManager,
            Serializable,
            TransactionTimeoutConfiguration,
            JTATransactionChecker
   {
      static final long serialVersionUID = -528322728929261214L;

      private transient BaseConnectionManager2 realCm;

      private final ObjectName cmName;

      ConnectionManagerProxy(final BaseConnectionManager2 realCm, final ObjectName cmName)
      {
         this.realCm = realCm;
         this.cmName = cmName;
      }

      // implementation of javax.resource.spi.ConnectionManager interface

      public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri)
            throws ResourceException
      {
         return getCM().allocateConnection(mcf, cri);
      }

      public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
      {
         try
         {
            return getCM().getTimeLeftBeforeTransactionTimeout(errorRollback);
         }
         catch (ResourceException e)
         {
            throw new NestedRuntimeException("Unable to retrieve connection manager", e);
         }
      }

      public int getTransactionTimeout() throws SystemException
      {
         try
         {
            return getCM().getTransactionTimeout();
         }
         catch (ResourceException e)
         {
            throw new NestedRuntimeException("Unable to retrieve connection manager", e);
         }
      }

      public void checkTransactionActive() throws RollbackException, SystemException
      {
         try
         {
            getCM().checkTransactionActive();
         }
         catch (ResourceException e)
         {
            throw new NestedRuntimeException("Unable to retrieve connection manager", e);
         }
      }

      private BaseConnectionManager2 getCM() throws ResourceException
      {
         if (realCm == null)
         {
            try
            {
               MBeanServer server = MBeanServerLocator.locateJBoss();
               realCm = (BaseConnectionManager2) server.getAttribute(cmName, "Instance");
            }
            catch (Throwable t)
            {
               Throwable t2 = JMXExceptionDecoder.decode(t);
               JBossResourceException.rethrowAsResourceException("Problem locating real ConnectionManager: " + cmName,
                     t2);
            }
         }
         return realCm;
      }
   }
}