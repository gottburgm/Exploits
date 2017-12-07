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
package org.jboss.invocation.jrmp.server;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.Serializable;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RemoteStub;
import java.rmi.MarshalledObject;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import javax.management.ObjectName;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.transaction.Transaction;

import org.jboss.beans.metadata.api.annotations.Create;
import org.jboss.beans.metadata.api.annotations.Destroy;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContextAware;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.net.sockets.DefaultSocketFactory;
import org.jboss.security.SecurityDomain;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * The JRMPInvoker is an RMI implementation that can generate Invocations
 * from RMI/JRMP into the JMX base.
 *
 * @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org>Scott Stark</a>
 * @version $Revision: 79760 $
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class JRMPInvoker
   extends RemoteServer
   implements Invoker, JRMPInvokerMBean, MBeanRegistration, KernelControllerContextAware
{
   /** @since 4.2.0 */
   static final long serialVersionUID = 3110972460891691492L;
   
   /**
    * Identifer to instruct the usage of an anonymous port.
    */
   public static final int ANONYMOUS_PORT = 0;

   /**
    * Instance logger.
    */
   protected Logger log;

   /**
    * Service MBean support delegate.
    */
   protected ServiceMBeanSupport support;

   /**
    * The port the container will be exported on
    */
   protected int rmiPort = ANONYMOUS_PORT;

   /**
    * An optional custom client socket factory
    */
   protected RMIClientSocketFactory clientSocketFactory;

   /**
    * An optional custom server socket factory
    */
   protected RMIServerSocketFactory serverSocketFactory;

   /**
    * The class name of the optional custom client socket factory
    */
   protected String clientSocketFactoryName;

   /**
    * The class name of the optional custom server socket factory
    */
   protected String serverSocketFactoryName;

   /**
    * The address to bind the rmi port on
    */
   protected String serverAddress;
   /**
    * The name of the security domain to use with server sockets that support SSL
    */
   protected String sslDomain;

   protected RemoteStub invokerStub;
   /**
    * The socket accept backlog
    */
   protected int backlog = 200;
   /**
    * A flag to enable caching of classes in the MarshalledValueInputStream
    */
   protected boolean enableClassCaching = false;
   /**
    * A priviledged actions for MBeanServer.invoke when running with sec mgr
    */
   private MBeanServerAction serverAction = new MBeanServerAction();

   public JRMPInvoker()
   {
      final JRMPInvoker delegate = this;

      // adapt the support delegate to invoke our state methods
      support = new ServiceMBeanSupport(getClass())
      {
         protected void startService() throws Exception
         {
            delegate.startService();
         }
         protected void stopService() throws Exception
         {
            delegate.stopService();
         }
         protected void destroyService() throws Exception
         {
            delegate.destroyService();
         }
      };

      // Setup logging from delegate
      log = support.getLog();
   }

   /**
    * @jmx.managed-attribute
    */
   public int getBacklog()
   {
      return backlog;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setBacklog(int back)
   {
      backlog = back;
   }

   /**
    * @jmx.managed-attribute
    */
   public boolean getEnableClassCaching()
   {
      return enableClassCaching;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setEnableClassCaching(boolean flag)
   {
      enableClassCaching = flag;
      MarshalledValueInputStream.useClassCache(enableClassCaching);
   }

   /**
    * @return The localhost name or null.
    */
   public String getServerHostName()
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (Exception ignored)
      {
         return null;
      }
   }

   /**
    * @jmx.managed-attribute
    */
   public void setRMIObjectPort(final int rmiPort)
   {
      this.rmiPort = rmiPort;
   }

   /**
    * @jmx.managed-attribute
    */
   public int getRMIObjectPort()
   {
      return rmiPort;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setRMIClientSocketFactory(final String name)
   {
      clientSocketFactoryName = name;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getRMIClientSocketFactory()
   {
      return clientSocketFactoryName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setRMIClientSocketFactoryBean(final RMIClientSocketFactory bean)
   {
      clientSocketFactory = bean;
   }

   /**
    * @jmx.managed-attribute
    */
   public RMIClientSocketFactory getRMIClientSocketFactoryBean()
   {
      return clientSocketFactory;
   }
   
   /**
    * @jmx.managed-attribute
    */
   public void setRMIServerSocketFactory(final String name)
   {
      serverSocketFactoryName = name;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getRMIServerSocketFactory()
   {
      return serverSocketFactoryName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setRMIServerSocketFactoryBean(final RMIServerSocketFactory bean)
   {
      serverSocketFactory = bean;
   }

   /**
    * @jmx.managed-attribute
    */
   public RMIServerSocketFactory getRMIServerSocketFactoryBean()
   {
      return serverSocketFactory;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setServerAddress(final String address)
   {
      serverAddress = address;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getServerAddress()
   {
      return serverAddress;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setSecurityDomain(String domainName)
   {
      this.sslDomain = domainName;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getSecurityDomain()
   {
      return sslDomain;
   }

   public Serializable getStub()
   {
      return this.invokerStub;
   }

   protected void startService() throws Exception
   {
      loadCustomSocketFactories();

      log.debug("RMI Port='" +
            (rmiPort == ANONYMOUS_PORT ? "Anonymous" :
            Integer.toString(rmiPort)) + "'");

      log.debug("Client SocketFactory='" +
            (clientSocketFactory == null ? "Default" :
            clientSocketFactory.toString()) + "'");

      log.debug("Server SocketFactory='" +
            (serverSocketFactory == null ? "Default" :
            serverSocketFactory.toString()) + "'");

      log.debug("Server SocketAddr='" +
            (serverAddress == null ? "Default" :
            serverAddress) + "'");
      log.debug("SecurityDomain='" +
            (sslDomain == null ? "Default" :
            sslDomain) + "'");

      InitialContext ctx = new InitialContext();

      // Validate that there is a TransactionPropagationContextImporter
      // bound in JNDI
      TransactionPropagationContextUtil.getTPCImporter();

      // Set the transaction manager and transaction propagation
      // context factory of the GenericProxy class

      Invoker delegateInvoker = createDelegateInvoker();

      // Make the remote invoker proxy available for use by the proxy factory
      Registry.bind(support.getServiceName(), delegateInvoker);

      // Export CI
      exportCI();

      log.debug("Bound JRMP invoker for JMX node");

      ctx.close();
   }

   protected void stopService() throws Exception
   {
      InitialContext ctx = new InitialContext();

      try
      {
         unexportCI();
      }
      finally
      {
         ctx.close();
      }
      this.clientSocketFactory = null;
      this.serverSocketFactory = null;
      this.invokerStub = null;
   }

   protected void destroyService() throws Exception
   {
      // Export references to the bean
      Registry.unbind(support.getServiceName());
   }

   /**
    * Invoke a Remote interface method.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      ClassLoader oldCl = TCLAction.UTIL.getContextClassLoader();
      ObjectName mbean = null;
      try
      {
         // Deserialize the transaction if it is there
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         invocation.setTransaction(importTPC(mi.getTransactionPropagationContext()));

         mbean = (ObjectName) Registry.lookup(invocation.getObjectName());

         // The cl on the thread should be set in another interceptor
         Object obj = serverAction.invoke(mbean,
            "invoke",
            new Object[]{invocation},
            Invocation.INVOKE_SIGNATURE);
         return new MarshalledObject(obj);
      }
      catch (Exception e)
      {
         Throwable th = JMXExceptionDecoder.decode(e);
         if (log.isTraceEnabled())
            log.trace("Failed to invoke on mbean: " + mbean, th);

         if (th instanceof Exception)
            e = (Exception) th;

         throw e;
      }
      finally
      {
         TCLAction.UTIL.setContextClassLoader(oldCl);
         Thread.interrupted(); // clear interruption because this thread may be pooled.
      }
   }

   protected Invoker createDelegateInvoker()
   {
      return new JRMPInvokerProxy(this);
   }

   protected void exportCI() throws Exception
   {
      this.invokerStub = (RemoteStub) UnicastRemoteObject.exportObject
         (this, rmiPort, clientSocketFactory, serverSocketFactory);
   }

   protected void unexportCI() throws Exception
   {
      UnicastRemoteObject.unexportObject(this, true);
   }

   protected void rebind(Context ctx, String name, Object val)
      throws NamingException
   {
      // Bind val to name in ctx, and make sure that all
      // intermediate contexts exist

      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context) ctx.lookup(ctxName);
         }
         catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }

      ctx.rebind(n.get(0), val);
   }

   /**
    * Load and instantiate the clientSocketFactory, serverSocketFactory using
    * the TCL and set the bind address and SSL domain if the serverSocketFactory
    * supports it.
    */
   protected void loadCustomSocketFactories()
   {
      ClassLoader loader = TCLAction.UTIL.getContextClassLoader();

      if( clientSocketFactory == null )
      {
         try
         {
            if (clientSocketFactoryName != null)
            {
               Class csfClass = loader.loadClass(clientSocketFactoryName);
               clientSocketFactory = (RMIClientSocketFactory) csfClass.newInstance();
            }
         }
         catch (Exception e)
         {
            log.error("Failed to load client socket factory", e);
            clientSocketFactory = null;
         }
      }

      if( serverSocketFactory == null )
      {
         try
         {
            if (serverSocketFactoryName != null)
            {
               Class ssfClass = loader.loadClass(serverSocketFactoryName);
               serverSocketFactory = (RMIServerSocketFactory) ssfClass.newInstance();
               if (serverAddress != null)
               {
                  // See if the server socket supports setBindAddress(String)
                  try
                  {
                     Class[] parameterTypes = {String.class};
                     Method m = ssfClass.getMethod("setBindAddress", parameterTypes);
                     Object[] args = {serverAddress};
                     m.invoke(serverSocketFactory, args);
                  }
                  catch (NoSuchMethodException e)
                  {
                     log.warn("Socket factory does not support setBindAddress(String)");
                     // Go with default address
                  }
                  catch (Exception e)
                  {
                     log.warn("Failed to setBindAddress=" + serverAddress + " on socket factory", e);
                     // Go with default address
                  }
               }
               /* See if the server socket supports setSecurityDomain(SecurityDomain)
               if an sslDomain was specified
               */
               if (sslDomain != null)
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
                  catch (NoSuchMethodException e)
                  {
                     log.error("Socket factory does not support setSecurityDomain(SecurityDomain)");
                  }
                  catch (Exception e)
                  {
                     log.error("Failed to setSecurityDomain=" + sslDomain + " on socket factory", e);
                  }
               }
            }
            // If a bind address was specified create a DefaultSocketFactory
            else if (serverAddress != null)
            {
               DefaultSocketFactory defaultFactory = new DefaultSocketFactory(backlog);
               serverSocketFactory = defaultFactory;
               try
               {
                  defaultFactory.setBindAddress(serverAddress);
               }
               catch (UnknownHostException e)
               {
                  log.error("Failed to setBindAddress=" + serverAddress + " on socket factory", e);
               }
            }
         }
         catch (Exception e)
         {
            log.error("operation failed", e);
            serverSocketFactory = null;
         }
      }
   }

   /**
    * Import a transaction propagation context into the local VM, and
    * return the corresponding <code>Transaction</code>.
    *
    * @return A transaction or null if no tpc.
    */
   protected Transaction importTPC(Object tpc)
   {
      if (tpc != null)
         return TransactionPropagationContextUtil.importTPC(tpc);
      return null;
   }

   //
   // Delegate the ServiceMBean details to our support delegate
   //

   public String getName()
   {
      return support.getName();
   }

   public MBeanServer getServer()
   {
      return support.getServer();
   }

   public int getState()
   {
      return support.getState();
   }

   public String getStateString()
   {
      return support.getStateString();
   }

   public void create() throws Exception
   {
      support.create();
   }

   public void start() throws Exception
   {
      support.start();
   }

   public void stop()
   {
      support.stop();
   }

   public void destroy()
   {
      support.destroy();
   }

   public void jbossInternalLifecycle(String method) throws Exception
   {
      support.jbossInternalLifecycle(method);
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      return support.preRegister(server, name);
   }

   public void postRegister(Boolean registrationDone)
   {
      support.postRegister(registrationDone);
   }

   public void preDeregister() throws Exception
   {
      support.preDeregister();
   }

   public void postDeregister()
   {
      support.postDeregister();
   }

   public void setKernelControllerContext(KernelControllerContext context) throws Exception
   {
      support.setKernelControllerContext(context);      
   }

   public void unsetKernelControllerContext(KernelControllerContext context) throws Exception
   {
      support.unsetKernelControllerContext(context);
   }
   
   @Create
   public void pojoCreate() throws Exception
   {
      support.pojoCreate();
   }
   
   @Start
   public void pojoStart() throws Exception
   {
      support.pojoStart();
   }

   @Stop
   public void pojoStop() throws Exception
   {
      support.pojoStop();
   }
   
   @Destroy
   public void pojoDestroy() throws Exception
   {
      support.pojoDestroy();
   }

   interface TCLAction
   {
      class UTIL
      {
         static TCLAction getTCLAction()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }

         static ClassLoader getContextClassLoader()
         {
            return getTCLAction().getContextClassLoader();
         }

         static ClassLoader getContextClassLoader(Thread thread)
         {
            return getTCLAction().getContextClassLoader(thread);
         }

         static void setContextClassLoader(ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(cl);
         }

         static void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(thread, cl);
         }
      }

      TCLAction NON_PRIVILEGED = new TCLAction()
      {
         public ClassLoader getContextClassLoader()
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public ClassLoader getContextClassLoader(Thread thread)
         {
            return thread.getContextClassLoader();
         }

         public void setContextClassLoader(ClassLoader cl)
         {
            Thread.currentThread().setContextClassLoader(cl);
         }

         public void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            thread.setContextClassLoader(cl);
         }
      };

      TCLAction PRIVILEGED = new TCLAction()
      {
         private final PrivilegedAction getTCLPrivilegedAction = new PrivilegedAction()
         {
            public Object run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };

         public ClassLoader getContextClassLoader()
         {
            return (ClassLoader) AccessController.doPrivileged(getTCLPrivilegedAction);
         }

         public ClassLoader getContextClassLoader(final Thread thread)
         {
            return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return thread.getContextClassLoader();
               }
            });
         }

         public void setContextClassLoader(final ClassLoader cl)
         {
            AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  Thread.currentThread().setContextClassLoader(cl);
                  return null;
               }
            });
         }

         public void setContextClassLoader(final Thread thread, final ClassLoader cl)
         {
            AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  thread.setContextClassLoader(cl);
                  return null;
               }
            });
         }
      };

      ClassLoader getContextClassLoader();

      ClassLoader getContextClassLoader(Thread thread);

      void setContextClassLoader(ClassLoader cl);

      void setContextClassLoader(Thread thread, ClassLoader cl);
   }

   /**
    * Perform the MBeanServer.invoke op in a PrivilegedExceptionAction if
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
         Object rtnValue = support.getServer().invoke(target, method, args, sig);
         return rtnValue;
      }

      Object invoke(ObjectName target, String method, Object[] args, String[] sig)
         throws Exception
      {
         SecurityManager sm = System.getSecurityManager();
         Object rtnValue = null;
         if (sm == null)
         {
            // Direct invocation on MBeanServer
            rtnValue = support.getServer().invoke(target, method, args, sig);
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
