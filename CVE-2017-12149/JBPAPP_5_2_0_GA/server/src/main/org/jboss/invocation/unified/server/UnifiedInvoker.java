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
package org.jboss.invocation.unified.server;

import java.rmi.MarshalledObject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.unified.interfaces.UnifiedInvokerProxy;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.transport.ConnectorMBean;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;

/**
 * This is a detached invoker which sits on top of jboss remoting.
 * Since this uses remoting, the transport protocol used is defined within
 * the remoting service and this, the UnifiedInvoker, is declared as the handler.
 *
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class UnifiedInvoker extends ServiceMBeanSupport implements ServerInvocationHandler, UnifiedInvokerMBean
{
   private ConnectorMBean connector;
   private ServerInvoker serverInvoker;

   private MBeanServer mbServer;

   private boolean strictRMIException = false;

   private UnifiedInvokerProxy proxy;

   private String subsystem = "invoker";

   /**
    * If set to true, this will cause the UnifiedInvokerProxy (on the client side) to
    * wrap all RemoteExceptions thrown from the server in a new ServerException.  If false,
    * will unwrap the original exception thrown from withint the RemoteException and throw that.
    * The default is false.
    *
    * @param isStrict
    */
   public void setStrictRMIException(boolean isStrict)
   {
      this.strictRMIException = isStrict;
   }

   /**
    * A return of true means that the UnifiedInvokerProxy (on the client side) will wrap all
    * RemoteExceptions within a new ServerException.  A return of false, will unwrap the original
    * exception thrown from within the RemoteException and throw that.  The default, if not explicitly set,
    * is false.
    *
    * @return
    */
   public boolean getStrictRMIException()
   {
      return strictRMIException;
   }

   /**
    * Gets the remoting subsystem being used.
    * @return
    */
   public String getSubSystem()
   {
      return subsystem;
   }

   public void setSubSystem(String subsystem)
   {
      this.subsystem = subsystem;
   }

   /**
    * This may be called if set depends in config with optional-attribute-name.
    *
    * @param connector
    */
   public void setConnector(ConnectorMBean connector)
   {
      this.connector = connector;
   }

   protected void createService() throws Exception
   {
      if(connector != null)
      {
         try
         {
            connector.addInvocationHandler(getSubSystem(), this);
         }
         catch(Exception e)
         {
            log.error("Error adding unified invoker as handler upon connector being set.", e);
         }
      }
   }

   /**
    * Will get the invoker locator from the server invoker, start the server invoker, create the proxy,
    * and bind the proxy.
    *
    * @throws Exception
    */
   protected void startService() throws Exception
   {
      log.debug("Starting unified invoker service.");

      InvokerLocator locator = null;
      if(serverInvoker != null)
      {
         locator = serverInvoker.getLocator();
         if(!serverInvoker.isStarted())
         {
            serverInvoker.start();
         }
      }
      else if(connector != null)
      {
         locator = connector.getLocator();
      }
      else
      {
         /**
          * This will happen in one of two scenarios.  One, the unified invoker was not declared in as
          * service before the connector AND was not specified as the handler within the connector config.
          * Two, the unified invoker service config did not use the proxy-type attribute within the depends
          * tag to have the container set the connector upon creating the unified invoker.
          */
         log.error("Error referencing either remoting connector or server invoker to be used.  " +
                   "Please check configuration to make sure proper dependancies are set.");
         throw new RuntimeException("Error getting locator because server invoker is null.");
      }

      proxy = new UnifiedInvokerProxy(locator, strictRMIException);

      jmxBind();

   }

   protected void jmxBind()
   {
      Registry.bind(getServiceName(), proxy);
   }

   /**
    * Stops the server invoker.
    *
    * @throws Exception
    */
   public void stopService() throws Exception
   {
      // JBAS-5590 -- the serverInvoker is a shared resource and shouldn't
      // be stopped just because we don't want it any more
//      if(serverInvoker != null)
//      {
//         serverInvoker.stop();
//      }
   }

   /**
    * Gives this JMX service a name.
    *
    * @return The Name value
    */
   public String getName()
   {
      return "Unified-Invoker";
   }

   /**
    * Gets the invoker locator string for this server
    *
    * @return
    */
   public String getInvokerLocator()
   {
      if(serverInvoker != null)
      {
         return serverInvoker.getLocator().getLocatorURI();
      }
      else
      {
         return null;
      }
   }

   /**
    * Implementation of the server invoker handler interface.  Will take the invocation request
    * and invoke down the interceptor chain.
    *
    * @param invocationReq
    * @return
    * @throws Throwable
    */
   public Object invoke(InvocationRequest invocationReq) throws Throwable
   {
      Invocation invocation = (Invocation) invocationReq.getParameter();
      Thread currentThread = Thread.currentThread();
      ClassLoader oldCl = currentThread.getContextClassLoader();
      ObjectName mbean = null;
      try
      {
         mbean = (ObjectName) Registry.lookup(invocation.getObjectName());

         // The cl on the thread should be set in another interceptor
         Object obj = getServer().invoke(mbean,
                                         "invoke",
                                         new Object[]{invocation},
                                         Invocation.INVOKE_SIGNATURE);
         return new MarshalledObject(obj);
      }
      catch(Exception e)
      {
         Throwable th = JMXExceptionDecoder.decode(e);
         if(log.isTraceEnabled())
         {
            log.trace("Failed to invoke on mbean: " + mbean, th);
         }

         if(th instanceof Exception)
         {
            e = (Exception) th;
         }

         throw e;
      }
      finally
      {
         currentThread.setContextClassLoader(oldCl);
         Thread.interrupted(); // clear interruption because this thread may be pooled.
      }

   }

   /**
    * set the mbean server that the handler can reference
    *
    * @param server
    */
   public void setMBeanServer(MBeanServer server)
   {
      mbServer = server;
   }

   public MBeanServer getServer()
   {
      return mbServer;
   }

   /**
    * set the invoker that owns this handler
    *
    * @param invoker
    */
   public void setInvoker(ServerInvoker invoker)
   {
      /**
       * This is needed in case we need to make calls on the server invoker (for classloading
       * in particular).  Will just leave alone for now and come back to this when have
       * a use to call on it.
       */
      serverInvoker = invoker;
   }

   protected ServerInvoker getInvoker()
   {
      return serverInvoker;
   }

   /**
    * Adds a callback handler that will listen for callbacks from
    * the server invoker handler.
    * This is a no op as don't expect the detached invokers to have callbacks
    *
    * @param callbackHandler
    */
   public void addListener(InvokerCallbackHandler callbackHandler)
   {
      //NO OP - do not expect the detached invoker to have callbacks
   }

   /**
    * Removes the callback handler that was listening for callbacks
    * from the server invoker handler.
    * This is a no op as don't expect the detached invokers to have callbacks
    *
    * @param callbackHandler
    */
   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
      //NO OP - do not expect the detached invoker to have callbacks
   }

}