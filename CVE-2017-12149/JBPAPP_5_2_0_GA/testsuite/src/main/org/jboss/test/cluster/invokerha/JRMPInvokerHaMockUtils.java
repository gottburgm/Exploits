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

import java.util.ArrayList;

import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;
import org.jboss.invocation.jrmp.server.JRMPInvokerHA;

/**
 * JrmpInvokerHaMockUtils.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class JRMPInvokerHaMockUtils
{
   /**
    * Mock version of JRMPInvokerHA that eases debugging and swallows an 
    * exception being thrown when unregistering the invoker ha mbean.
    * 
    * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
    */
   public static class MockJRMPInvokerHA extends JRMPInvokerHA
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -4557124707606766661L;
      
      private String name;
      
      public MockJRMPInvokerHA(String name)
      {
         this.name = name;
      }
      
      /**
       * Override postDeregister() to avoid the following exception being 
       * thrown on tearDown():
       * 
       * javax.management.InstanceNotFoundException: jboss.system:service=ServiceController
       *    at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.getMBean(DefaultMBeanServerInterceptor.java:1010)
       *    at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.invoke(DefaultMBeanServerInterceptor.java:804)
       *    at com.sun.jmx.mbeanserver.JmxMBeanServer.invoke(JmxMBeanServer.java:784)
       *    at org.jboss.system.ServiceMBeanSupport.postDeregister(ServiceMBeanSupport.java:424)
       *    at org.jboss.invocation.jrmp.server.JRMPInvoker.postDeregister(JRMPInvoker.java:665)
       */
      @Override
      public void postDeregister()
      {
      }
      
      /**
       * Override toString because in absence of RemoteRef 
       * java.rmi.server.RemoteObject's toString implementation prints just 
       * the class name which makes it pretty ackward for assertions and 
       * debugging.
       */
      @Override
      public String toString()
      {         
         return name;
      }

      @Override
      protected Invoker createProxy(ArrayList targets, LoadBalancePolicy policy, String proxyFamilyName,
            long viewId)
      {
         return new MockJRMPInvokerProxyHA(targets, policy, proxyFamilyName, viewId);
      }

      @Override
      public Object invoke(Invocation invocation) throws Exception
      {
         InvokerHaFailureType failureType = (InvokerHaFailureType)invocation.getValue("FAILURE_TYPE");
         if (failureType != null)
         {
            failureType.injectFailureIfExistsAfterServer((Integer)invocation.getAsIsValue("FAILOVER_COUNTER"));
         }
         
         return super.invoke(invocation);
      }
      
   }
   
   public static class MockJRMPInvokerProxyHA extends JRMPInvokerProxyHA
   {

      public MockJRMPInvokerProxyHA(ArrayList targets, LoadBalancePolicy policy, String proxyFamilyName, long viewId)
      {
         super(targets, policy, proxyFamilyName, viewId);
      }


      @Override
      public boolean txContextAllowsFailover(Invocation invocation)
      {
         return super.txContextAllowsFailover(invocation);
      }
      
      @Override
      public Object invoke(Invocation invocation) throws Exception
      {
         return super.invoke(invocation);
      }
   }

}
