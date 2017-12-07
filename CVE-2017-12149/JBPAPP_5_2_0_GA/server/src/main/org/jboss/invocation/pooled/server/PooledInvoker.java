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
package org.jboss.invocation.pooled.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;

import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.pooled.interfaces.PooledInvokerProxy;
import org.jboss.invocation.pooled.interfaces.ServerAddress;
import org.jboss.invocation.pooled.interfaces.PooledMarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.proxy.TransactionInterceptor;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.tm.TransactionPropagationContextUtil;
import org.jboss.security.SecurityDomain;
import org.jboss.net.sockets.DefaultSocketFactory;

/**
 * This invoker pools Threads and client connections to one server socket.
 * The purpose is to avoid a bunch of failings of RMI.
 * 
 * 1. Avoid making a client socket connection with every invocation call.
 *    This is very expensive.  Also on windows if too many clients try 
 *    to connect at the same time, you get connection refused exceptions.
 *    This invoker/proxy combo alleviates this.
 *
 * 2. Avoid creating a thread per invocation.  The client/server connection
 *    is preserved and attached to the same thread.

 * So we have connection pooling on the server and client side, and thread pooling
 * on the server side.  Pool, is an LRU pool, so resources should be cleaned up.
 * 
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class PooledInvoker extends ServiceMBeanSupport
   implements PooledInvokerMBean, Runnable
{

   /**
    * logger instance.
    */
   final static protected Logger log = Logger.getLogger(PooledInvoker.class);

   /**
    * If the TcpNoDelay option should be used on the socket.
    */
   protected boolean enableTcpNoDelay = false;

   /**
    * The internet address to bind to by default.
    */
   protected String serverBindAddress = null;

   /**
    * The server port to bind to.
    */
   protected int serverBindPort = 0;

   /**
    * The internet address client will use to connect to the sever.
    */
   protected String clientConnectAddress = null;

   /**
    * The port a client will use to connect to the sever.
    */
   protected int clientConnectPort = 0;
   /**
    * The number of retry attempts on 
    */ 
   protected int clientRetryCount = 1;

   protected int backlog = 200;

   /** The class name of the optional custom client socket factory */
   protected String clientSocketFactoryName;

   /** The class name of the optional custom server socket factory */
   protected String serverSocketFactoryName;

   /** An optional custom client socket factory */
   protected SocketFactory clientSocketFactory;

   /** An optional custom server socket factory */
   protected ServerSocketFactory serverSocketFactory;
   /** The server socket for */
   protected ServerSocket serverSocket = null;

   /** The name of the security domain to use with server sockets that support SSL */
   protected String sslDomain;

   protected int timeout = 60000; // 60 seconds.

   protected int maxPoolSize = 300;

   protected int clientMaxPoolSize = 300;

   protected int numAcceptThreads = 1;
   protected Thread[] acceptThreads;

   protected LRUPool clientpool;
   protected LinkedList threadpool;
   protected boolean running = true;
   /** The logging trace level flag */
   protected boolean trace = false;
   /**
    * ObjectName of the <code>transactionManagerService</code> we use.
    * Probably should not be here -- used to set txInterceptor tx mananger.
    */
   protected ObjectName transactionManagerService;

   protected PooledInvokerProxy optimizedInvokerProxy = null;
   /** A priviledged actions for MBeanServer.invoke when running with sec mgr */
   private MBeanServerAction serverAction = new MBeanServerAction();

   protected static TransactionPropagationContextFactory tpcFactory;
   protected static TransactionPropagationContextImporter tpcImporter;

   ////////////////////////////////////////////////////////////////////////
   //
   // The following methods Override the ServiceMBeanSupport base class
   //
   ////////////////////////////////////////////////////////////////////////

   protected void jmxBind()
   {
      Registry.bind(getServiceName(), optimizedInvokerProxy);
   }

   /**
    * Starts this IL, and binds it to JNDI
    *
    * @exception Exception  Description of Exception
    */
   public void startService() throws Exception
   {
      trace = log.isTraceEnabled();

      ///////////////////////////////////////////////////////////      
      // Setup the transaction stuff
      ///////////////////////////////////////////////////////////      
      InitialContext ctx = new InitialContext();

      // Get the transaction propagation context factory
      tpcFactory = TransactionPropagationContextUtil.getTPCFactory();

      // and the transaction propagation context importer
      tpcImporter = TransactionPropagationContextUtil.getTPCImporter();

      // FIXME marcf: This should not be here
      TransactionInterceptor.setTransactionManager((TransactionManager)ctx.lookup("java:/TransactionManager"));

      ///////////////////////////////////////////////////////////
      // Setup the socket level stuff
      ///////////////////////////////////////////////////////////      

      InetAddress bindAddress =
         (serverBindAddress == null || serverBindAddress.length() == 0)
            ? null
            : InetAddress.getByName(serverBindAddress);

      clientConnectAddress =
         (clientConnectAddress == null || clientConnectAddress.length() == 0)
            ? InetAddress.getLocalHost().getHostName()
            : clientConnectAddress;
      /* We need to check the address against "0.0.0.0" as this is not a valid
      address although some jdks will default to the host, while others fail
      with java.net.BindException: Cannot assign requested address: connect
      */
      clientConnectAddress = ServerConfigUtil.fixRemoteAddress(clientConnectAddress);

      // Load any custom socket factories
      loadCustomSocketFactories();

      clientpool = new LRUPool(2, maxPoolSize);
      clientpool.create();
      threadpool = new LinkedList();
       try
       {
          if( serverSocketFactory != null )
            serverSocket = serverSocketFactory.createServerSocket(serverBindPort, backlog, bindAddress);
          else
            serverSocket = new ServerSocket(serverBindPort, backlog, bindAddress);
       }
       catch( java.net.BindException be)
       {
           throw new Exception("Port "+serverBindPort+" is already in use",be);
       }
       serverBindPort = serverSocket.getLocalPort();
      clientConnectPort = (clientConnectPort == 0) ? serverSocket.getLocalPort() : clientConnectPort;

      ServerAddress sa = new ServerAddress(clientConnectAddress, clientConnectPort,
         enableTcpNoDelay, timeout, clientSocketFactory);
      optimizedInvokerProxy = new PooledInvokerProxy(sa, clientMaxPoolSize, clientRetryCount);

      ///////////////////////////////////////////////////////////      
      // Register the service with the rest of the JBoss Kernel
      ///////////////////////////////////////////////////////////      
      // Export references to the bean
      jmxBind();
      log.debug("Bound invoker for JMX node");
      ctx.close();

      acceptThreads = new Thread[numAcceptThreads];
      for (int i = 0; i < numAcceptThreads; i++)
      {
         String name = "PooledInvokerAcceptor#"+i+"-"+serverBindPort;
         acceptThreads[i] = new Thread(this, name);
         acceptThreads[i].start();
      }
   }

   public void run()
   {
      while (running)
      {
         try
         {
            Socket socket = serverSocket.accept();
            if( trace )
               log.trace("Accepted: "+socket);
            ServerThread thread = null;
            boolean newThread = false;
            
            while (thread == null)
            {
               synchronized(threadpool)
               {
                  if (threadpool.size() > 0)
                  {
                     thread = (ServerThread)threadpool.removeFirst();
                  }
               }
               if (thread == null)
               {
                  synchronized(clientpool)
                  {
                     if (clientpool.size() < maxPoolSize) 
                     {
                        thread = new ServerThread(socket, this, clientpool, threadpool, timeout);
                        newThread = true;
                     }
                     if (thread == null)
                     {
                        clientpool.evict();
                        if( trace )
                           log.trace("Waiting for a thread...");
                        clientpool.wait();
                        if( trace )
                           log.trace("Notified of available thread");
                     }
                  }
               }
            }
            synchronized(clientpool)
            {
               clientpool.insert(thread, thread);
            }
            
            if (newThread)
            {
               if( trace )
                  log.trace("Created a new thread, t="+thread);
               thread.start();
            }
            else
            {
               if( trace )
                  log.trace("Reusing thread t="+thread);
               thread.wakeup(socket, timeout);
            }
         }
         catch (Throwable ex)
         {
            if (running)
               log.error("Failed to accept socket connection", ex);
         }
      }
   }

   /**
    * Stops this service, and unbinds it from JNDI.
    */
   public void stopService() throws Exception
   {
      running = false;
      maxPoolSize = 0; // so ServerThreads don't reinsert themselves
      for (int i = 0; i < acceptThreads.length; i++)
      {
         try
         {
            acceptThreads[i].interrupt();
         }
         catch (Exception ignored){}
      }
      clientpool.flush();
      for (int i = 0; i < threadpool.size(); i++)
      {
         ServerThread thread = (ServerThread)threadpool.removeFirst();
         thread.shutdown();
      }

      try
      {
         serverSocket.close();
      }
      catch(Exception e)
      {         
      }
   }

   protected void destroyService() throws Exception
   {
      // Unexport references to the bean
      Registry.unbind(getServiceName());
   }

   /**
    * The ServerProtocol will use this method to service an invocation 
    * request.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      Thread currentThread = Thread.currentThread();
      ClassLoader oldCl = currentThread.getContextClassLoader();
      try
      {

         // Deserialize the transaction if it is there
         PooledMarshalledInvocation mi = (PooledMarshalledInvocation) invocation;
         invocation.setTransaction(importTPC(mi.getTransactionPropagationContext()));
         ObjectName mbean = (ObjectName) Registry.lookup(invocation.getObjectName());
         if( mbean == null )
         {
            System.err.println("NoSuchObjectException: "+invocation.getObjectName());
            throw new NoSuchObjectException("Failed to find target for objectName: "+invocation.getObjectName());
         }

         // The cl on the thread should be set in another interceptor
         Object obj = serverAction.invoke(mbean, "invoke",
               new Object[] { invocation }, Invocation.INVOKE_SIGNATURE);

         return obj;
      }
      catch (Exception e)
      {
         org.jboss.mx.util.JMXExceptionDecoder.rethrow(e);

         // the compiler does not know an exception is thrown by the above
         throw new org.jboss.util.UnreachableStatementException();
      }
      finally
      {
         currentThread.setContextClassLoader(oldCl);
      }
   }

   protected Transaction importTPC(Object tpc)
   {
      if (tpc != null)
         return tpcImporter.importTransactionPropagationContext(tpc);
      return null;
   }

   //The following are the mbean attributes for TrunkInvoker

   /**
    * Getter for property numAcceptThreads
    *
    * @return Value of property numAcceptThreads
    * @jmx:managed-attribute
    */
   public int getNumAcceptThreads()
   {
      return numAcceptThreads;
   }

   /**
    * Setter for property numAcceptThreads
    *
    * @param size New value of property numAcceptThreads.
    * @jmx:managed-attribute
    */
   public void setNumAcceptThreads(int size)
   {
      this.numAcceptThreads = size;
   }

   /**
    * Getter for property maxPoolSize;
    *
    * @return Value of property maxPoolSize.
    * @jmx:managed-attribute
    */
   public int getMaxPoolSize()
   {
      return maxPoolSize;
   }

   /**
    * Setter for property maxPoolSize.
    *
    * @param maxPoolSize New value of property maxPoolSize.
    * @jmx:managed-attribute
    */
   public void setMaxPoolSize(int maxPoolSize)
   {
      this.maxPoolSize = maxPoolSize;
   }

   /**
    * Getter for property maxPoolSize;
    *
    * @return Value of property maxPoolSize.
    * @jmx:managed-attribute
    */
   public int getClientMaxPoolSize()
   {
      return clientMaxPoolSize;
   }

   /**
    * Setter for property maxPoolSize.
    *
    * @param clientMaxPoolSize New value of property serverBindPort.
    * @jmx:managed-attribute
    */
   public void setClientMaxPoolSize(int clientMaxPoolSize)
   {
      this.clientMaxPoolSize = clientMaxPoolSize;
   }

   /**
    * Getter for property timeout
    *
    * @return Value of property timeout
    * @jmx:managed-attribute
    */
   public int getSocketTimeout()
   {
      return timeout;
   }

   /**
    * Setter for property timeout
    *
    * @param time New value of property timeout
    * @jmx:managed-attribute
    */
   public void setSocketTimeout(int time)
   {
      this.timeout = time;
   }

   /**
    *
    * @return Value of property CurrentClientPoolSize.
    * @jmx:managed-attribute
    */
   public int getCurrentClientPoolSize()
   {
      return clientpool.size();
   }

   /**
    *
    * @return Value of property CurrentThreadPoolSize.
    * @jmx:managed-attribute
    */
   public int getCurrentThreadPoolSize()
   {
      return threadpool.size();
   }

   /**
    * Getter for property serverBindPort.
    *
    * @return Value of property serverBindPort.
    * @jmx:managed-attribute
    */
   public int getServerBindPort()
   {
      return serverBindPort;
   }

   /**
    * Setter for property serverBindPort.
    *
    * @param serverBindPort New value of property serverBindPort.
    * @jmx:managed-attribute
    */
   public void setServerBindPort(int serverBindPort)
   {
      this.serverBindPort = serverBindPort;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getClientConnectAddress()
   {
      return clientConnectAddress;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setClientConnectAddress(String clientConnectAddress)
   {
      this.clientConnectAddress = clientConnectAddress;
   }

   /**
    * @jmx:managed-attribute
    */
   public int getClientConnectPort()
   {
      return clientConnectPort;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setClientConnectPort(int clientConnectPort)
   {
      this.clientConnectPort = clientConnectPort;
   }

   /**
    * @jmx:managed-attribute
    */
   public int getClientRetryCount()
   {
      return clientRetryCount;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setClientRetryCount(int clientRetryCount)
   {
      this.clientRetryCount = clientRetryCount;
   }

   /**
    * @jmx:managed-attribute
    */
   public int getBacklog()
   {
      return backlog;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setBacklog(int backlog)
   {
      this.backlog = backlog;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean isEnableTcpNoDelay()
   {
      return enableTcpNoDelay;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setEnableTcpNoDelay(boolean enableTcpNoDelay)
   {
      this.enableTcpNoDelay = enableTcpNoDelay;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getServerBindAddress()
   {
      return serverBindAddress;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setServerBindAddress(String serverBindAddress)
   {
      this.serverBindAddress = serverBindAddress;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getClientSocketFactoryName()
   {
      return clientSocketFactoryName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setClientSocketFactoryName(String clientSocketFactoryName)
   {
      this.clientSocketFactoryName = clientSocketFactoryName;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getServerSocketFactoryName()
   {
      return serverSocketFactoryName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setServerSocketFactoryName(String serverSocketFactoryName)
   {
      this.serverSocketFactoryName = serverSocketFactoryName;
   }

   /**
    * @jmx:managed-attribute
    */
   public SocketFactory getClientSocketFactory()
   {
      return clientSocketFactory;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setClientSocketFactory(SocketFactory clientSocketFactory)
   {
      this.clientSocketFactory = clientSocketFactory;
   }

   /**
    * @jmx:managed-attribute
    */
   public ServerSocket getServerSocket()
   {
      return serverSocket;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setServerSocket(ServerSocket serverSocket)
   {
      this.serverSocket = serverSocket;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getSslDomain()
   {
      return sslDomain;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setSslDomain(String sslDomain)
   {
      this.sslDomain = sslDomain;
   }

   /**
    * @jmx:managed-attribute
    */
   public ServerSocketFactory getServerSocketFactory()
   {
      return serverSocketFactory;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setServerSocketFactory(ServerSocketFactory serverSocketFactory)
   {
      this.serverSocketFactory = serverSocketFactory;
   }
   
   /**
    * mbean get-set pair for field transactionManagerService
    * Get the value of transactionManagerService
    * @return value of transactionManagerService
    *
    * @jmx:managed-attribute
    */
   public ObjectName getTransactionManagerService()
   {
      return transactionManagerService;
   }
   
   
   /**
    * Set the value of transactionManagerService
    * @param transactionManagerService  Value to assign to transactionManagerService
    *
    * @jmx:managed-attribute
    */
   public void setTransactionManagerService(ObjectName transactionManagerService)
   {
      this.transactionManagerService = transactionManagerService;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public PooledInvokerProxy getOptimizedInvokerProxy()
   {
      return optimizedInvokerProxy;
   }

   /** Load and instantiate the clientSocketFactory, serverSocketFactory using
    the TCL and set the bind address and SSL domain if the serverSocketFactory
    supports it.
   */
   protected void loadCustomSocketFactories()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      try
      {
         if( clientSocketFactoryName != null )
         {
            Class csfClass = loader.loadClass(clientSocketFactoryName);
            clientSocketFactory = (SocketFactory) csfClass.newInstance();
         }
      }
      catch (Exception e)
      {
         log.error("Failed to load client socket factory", e);
         clientSocketFactory = null;
      }

      try
      {
         if( serverSocketFactory == null )
         {
            if( serverSocketFactoryName != null )
            {
               Class ssfClass = loader.loadClass(serverSocketFactoryName);
               serverSocketFactory = (ServerSocketFactory) ssfClass.newInstance();
               if( serverBindAddress != null )
               {
                  // See if the server socket supports setBindAddress(String)
                  try
                  {
                     Class[] parameterTypes = {String.class};
                     Method m = ssfClass.getMethod("setBindAddress", parameterTypes);
                     Object[] args = {serverBindAddress};
                     m.invoke(serverSocketFactory, args);
                  }
                  catch (NoSuchMethodException e)
                  {
                     log.warn("Socket factory does not support setBindAddress(String)");
                     // Go with default address
                  }
                  catch (Exception e)
                  {
                     log.warn("Failed to setBindAddress="+serverBindAddress+" on socket factory", e);
                     // Go with default address
                  }
               }
               /* See if the server socket supports setSecurityDomain(SecurityDomain)
               if an sslDomain was specified
               */
               if( sslDomain != null )
               {
                  try
                  {
                     InitialContext ctx = new InitialContext();
                     SecurityDomain domain = (SecurityDomain) ctx.lookup(sslDomain);
                     Class[] parameterTypes = {SecurityDomain.class};
                     Method m = ssfClass.getMethod("setSecurityDomain", parameterTypes);
                     Object[] args = {domain};
                     m.invoke(serverSocketFactory, args);
                  }
                  catch(NoSuchMethodException e)
                  {
                     log.error("Socket factory does not support setSecurityDomain(SecurityDomain)");
                  }
                  catch(Exception e)
                  {
                     log.error("Failed to setSecurityDomain="+sslDomain+" on socket factory", e);
                  }
               }
            }
            // If a bind address was specified create a DefaultSocketFactory
            else if( serverBindAddress != null )
            {
               DefaultSocketFactory defaultFactory = new DefaultSocketFactory(backlog);
               serverSocketFactory = defaultFactory;
               try
               {
                  defaultFactory.setBindAddress(serverBindAddress);
               }
               catch (UnknownHostException e)
               {
                  log.error("Failed to setBindAddress="+serverBindAddress+" on socket factory", e);
               }
            }
         }
      }
      catch (Exception e)
      {
         log.error("operation failed", e);
         serverSocketFactory = null;
      }
   }

   /** Perform the MBeanServer.invoke op in a PrivilegedExceptionAction if
    * running with a security manager.
    */ 
   class MBeanServerAction implements PrivilegedExceptionAction
   {
      private ObjectName target;
      String method;
      Object[] args;
      String[] sig;

      MBeanServerAction()
      {  
      }
      MBeanServerAction(ObjectName target, String method, Object[] args, String[] sig)
      {
         this.target = target;
         this.method = method;
         this.args = args;
         this.sig = sig;
      }

      public Object run() throws Exception
      {
         Object rtnValue = server.invoke(target, method, args, sig);
         return rtnValue;
      }
      Object invoke(ObjectName target, String method, Object[] args, String[] sig)
         throws Exception
      {
         SecurityManager sm = System.getSecurityManager();
         Object rtnValue = null;
         if( sm == null )
         {
            // Direct invocation on MBeanServer
            rtnValue = server.invoke(target, method, args, sig);
         }
         else
         {
            try
            {
               // Encapsulate the invocation in a PrivilegedExceptionAction
               MBeanServerAction action = new MBeanServerAction(target, method, args, sig);
               rtnValue = AccessController.doPrivileged(action);
            }
            catch (PrivilegedActionException e)
            {
               Exception ex = e.getException();
               throw ex;
            }
         }
         return rtnValue;
      }
   }
}
// vim:expandtab:tabstop=3:shiftwidth=3
