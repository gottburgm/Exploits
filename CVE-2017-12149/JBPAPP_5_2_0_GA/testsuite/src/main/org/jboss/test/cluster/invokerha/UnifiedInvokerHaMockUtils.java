/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.invokerha;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.net.SocketFactory;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.unified.interfaces.UnifiedInvokerHAProxy;
import org.jboss.invocation.unified.server.UnifiedInvokerHA;
import org.jboss.remoting.Client;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.marshal.Marshaller;
import org.jboss.remoting.marshal.UnMarshaller;
import org.jboss.remoting.transport.ClientInvoker;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * Helper class with all remoting related mock implementations that allow 
 * direct invocations from proxy to invoker. 
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class UnifiedInvokerHaMockUtils
{
   public static boolean SLOW_DOWN_CLIENT_CONNECT = false;
   
   public static class MockUnifiedInvokerHA extends UnifiedInvokerHA
   {
      private String name;
      
      private InvokerLocator locator;
      
      public MockUnifiedInvokerHA(String name)
      {
         this.name = name;
      }
      
      @Override
      public String toString()
      {         
         return name + "," + super.toString();
      }

      public void setLocator(InvokerLocator locator)
      {
         this.locator = locator;
      }
      
      public InvokerLocator getLocator()
      {
         return locator;
      }

      @Override
      protected Invoker createProxy(boolean isStrictRMIException, ArrayList targets,
            LoadBalancePolicy policy, String proxyFamilyName, long viewId)
      {
         /* default invoker locator that will later be replaced by what the 
          * load balance policy decides */
         return new MockUnifiedInvokerHAProxy(locator, isStrictRMIException, targets, policy, proxyFamilyName, viewId);
      }

      @Override
      public MBeanServer getServer()
      {
         return ManagementFactory.getPlatformMBeanServer();
      }

      @Override
      public Object invoke(InvocationRequest invocationReq) throws Throwable
      {
         Invocation invocation = (Invocation) invocationReq.getParameter(); 
         InvokerHaFailureType failureType = (InvokerHaFailureType)invocation.getValue("FAILURE_TYPE");
         if (failureType != null)
         {
            failureType.injectFailureIfExistsAfterServer((Integer)invocation.getAsIsValue("FAILOVER_COUNTER"));
         }
         
         log.debug("invoking on " + invocation.getObjectName());
         
         return super.invoke(invocationReq);
      }
   }
   
   public static class MockUnifiedInvokerHAProxy extends UnifiedInvokerHAProxy
   {
      
      public MockUnifiedInvokerHAProxy(InvokerLocator locator, boolean isStrictRMIException, ArrayList targets,
            LoadBalancePolicy policy, String proxyFamilyName, long viewId)
      {
         super(locator, isStrictRMIException, targets, policy, proxyFamilyName, viewId);
      }

      @Override
      protected Client createClient(InvokerLocator locator, String subSystem) throws Exception
      {
         return new MockClient(locator);
      }

      @Override
      public Object invoke(Invocation invocation) throws Exception
      {
         InvokerHaFailureType failureType = (InvokerHaFailureType)invocation.getValue("FAILURE_TYPE");
         if (failureType != null)
         {
            failureType.injectFailureIfExistsBeforeServer();
         }
         
         return super.invoke(invocation);
      }
      
      public InvokerLocator getLocator()
      {
         return super.getLocator();
      }
   }

   public static class MockInvokerLocator extends InvokerLocator
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 8641387521254685628L;
      
      private MockUnifiedInvokerHA invoker;

      public MockInvokerLocator(String host, int port, MockUnifiedInvokerHA invoker)
      {
         super("unittest", host, port, "mock", null);
         this.invoker = invoker;
      }      
      
      public MockUnifiedInvokerHA getInvoker()
      {
         return invoker;
      }
      
      @Override
      public String toString()
      {
         return super.toString() + ",[invoker=" + invoker + "]";
      }

      @Override
      public boolean equals(Object obj)
      {
         /* reimplemented equals to avoid lingering static family cluster info 
          * references being valid in latter tests */
         return super.equals(obj) && invoker.equals(((MockInvokerLocator)obj).getInvoker());
      }
   }

   public static class MockClient extends Client
   {
      /* temporary invoker locator to be able to mimic what remoting does 
       * setting the invoker in connect() */
      private InvokerLocator locatorTmp;
      
      public MockClient(InvokerLocator locator) throws Exception
      {
         super(locator);
         locatorTmp = locator;
      }
      
      @Override
      public void connect() throws Exception
      {
         if (SLOW_DOWN_CLIENT_CONNECT)
         {
            Thread.sleep(50);
         }
         
         setInvoker(new MockClientInvoker(locatorTmp));
      }

      @Override
      public Object invoke(Object param, Map metadata) throws Throwable
      {
         Invocation inv = (Invocation) param;
         inv.getTransientPayload().put("TEST_USED_TARGET", this.getInvoker().getLocator());
         
         /* Down the call stack in Remoting's Client.invoke, marshaller is 
          * called which via org.jboss.invocation.unified.marshall.InvocationMarshaller 
          * sets the transaction progagation context.
          * 
          * As we're mocking Remoting's Client.invoke behaivour, we set the 
          * transaction progagation context manually in the mock class. 
          * 
          * Not ideal, as I try avoiding production code in UTs as much as 
          * possible, but makes the mock implementation a lot simpler.
          */
         MarshalledInvocation marshInv = new MarshalledInvocation(inv);
         marshInv.setTransactionPropagationContext(getTransactionPropagationContext());

         MockUnifiedInvokerHA invoker = ((MockInvokerLocator)getInvoker().getLocator()).getInvoker();
         
         return invoker.invoke(new InvocationRequest("", "", marshInv,
               metadata, null, null));
      }
      
      public Object getTransactionPropagationContext()
      {
         TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactoryClientSide();
         return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
      }

      @Override
      public String toString()
      {
         return ((MockInvokerLocator)getInvoker().getLocator()).getInvoker().toString();
      }
   }
   
   static class MockClientInvoker implements ClientInvoker
   {
      private InvokerLocator locator;
      
      public MockClientInvoker(InvokerLocator locator)
      {
         this.locator = locator;
      }
      
      public InvokerLocator getLocator()
      {
         return locator;
      }

      public String addClientLocator(String sessionId, InvokerCallbackHandler callbackhandler, InvokerLocator locator)
      {
         return null;
      }

      public void connect() throws ConnectionFailedException
      {
      }

      public void disconnect()
      {
      }

      public void establishLease(String sessionID, Map configuration, long leasePeriod) throws Throwable
      {
      }

      public InvokerLocator getClientLocator(String listenerId)
      {
         return null;
      }

      public List getClientLocators(String sessionId, InvokerCallbackHandler handler)
      {
         return null;
      }

      public long getLeasePeriod(String sessionID)
      {
         return 0;
      }

      public Marshaller getMarshaller()
      {
         return null;
      }

      public SocketFactory getSocketFactory()
      {
         return null;
      }

      public UnMarshaller getUnMarshaller()
      {
         return null;
      }

      public Object invoke(InvocationRequest in) throws Throwable
      {
         return null;
      }

      public boolean isConnected()
      {
         return false;
      }

      public void setMarshaller(Marshaller marshaller)
      {         
      }

      public void setSocketFactory(SocketFactory socketFactory)
      {
      }

      public void setUnMarshaller(UnMarshaller unmarshaller)
      {
      }

      public void terminateLease(String sessionID, int disconnectTimeout)
      {
      }
   }

}
