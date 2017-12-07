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

import org.jboss.system.Registry;
import javax.management.ObjectName;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.pooled.interfaces.PooledInvokerProxy;
import org.jboss.invocation.pooled.interfaces.ServerAddress;
import java.util.HashMap;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;
import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.GenericClusteringException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

import java.util.ArrayList;

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
 * @author    <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81001 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class PooledInvokerHA extends PooledInvoker implements InvokerHA
{
   protected HashMap beanMap = new HashMap();

   protected void jmxBind()
   {
      Registry.bind(getServiceName(), this);
   }

   // JRMPInvoker.destroyService() does the right thing

   public java.io.Serializable getStub() 
   {
      ServerAddress sa = new ServerAddress(clientConnectAddress,
         clientConnectPort, enableTcpNoDelay, timeout, clientSocketFactory); 
      return new PooledInvokerProxy(sa, clientMaxPoolSize);
   }

   public void registerBean(ObjectName beanName, HATarget target) throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      log.debug("registerBean: "+beanName);
      
      if (beanMap.containsKey(hash))
      {
         // FIXME [oleg] In theory this is possible!
         log.debug("Trying to register target " + target + " using an existing hashCode. Already registered: " + hash + "=" + beanMap.get(hash));
         throw new IllegalStateException("Trying to register target using an existing hashCode.");
      }
      beanMap.put(hash, target);
   }
   
   public Invoker createProxy(ObjectName beanName, LoadBalancePolicy policy,
      String proxyFamilyName) throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      HATarget target = (HATarget) beanMap.get(hash);
      if (target == null)
      {
         throw new IllegalStateException("The bean hashCode not found");
      }

      String familyName = proxyFamilyName;
      if (familyName == null)
         familyName= target.getAssociatedPartition().getPartitionName() + "/" + beanName;

      JRMPInvokerProxyHA proxy = new JRMPInvokerProxyHA(target.getReplicants(), 
                                                        policy, 
                                                        familyName, 
                                                        target.getCurrentViewId ());
      return proxy;
   }

   public void unregisterBean(ObjectName beanName) throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      beanMap.remove(hash);
   }

   /**
    * Invoke a Remote interface method.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         // Deserialize the transaction if it is there
         invocation.setTransaction(importTPC(((MarshalledInvocation) invocation).getTransactionPropagationContext()));

         // Extract the ObjectName, the rest is still marshalled
         ObjectName mbean = (ObjectName) Registry.lookup(invocation.getObjectName());
         long clientViewId = ((Long)invocation.getValue("CLUSTER_VIEW_ID")).longValue();

         HATarget target = (HATarget)beanMap.get(invocation.getObjectName());
         if (target == null) 
         {
            // We could throw IllegalStateException but we have a race condition that could occur:
            // when we undeploy a bean, the cluster takes some time to converge
            // and to recalculate a new viewId and list of replicant for each HATarget.
            // Consequently, a client could own an up-to-date list of the replicants
            // (before the cluster has converged) and try to perform an invocation
            // on this node where the HATarget no more exist, thus receiving a
            // wrong exception and no failover is performed with an IllegalStateException
            //
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, 
                                                 "target is not/no more registered on this node");            
         }
         
         if (!target.invocationsAllowed ())
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, 
                                        "invocations are currently not allowed on this target");            

         // The cl on the thread should be set in another interceptor
         Object rtn = getServer().invoke(mbean,
                                         "invoke",
                                         new Object[] { invocation },
                                         Invocation.INVOKE_SIGNATURE);
         
         HARMIResponse rsp = new HARMIResponse();

         if (clientViewId != target.getCurrentViewId())
         {
            rsp.newReplicants = new ArrayList(target.getReplicants());
            rsp.currentViewId = target.getCurrentViewId();
         }
         rsp.response = rtn;
         return rsp;
      }
      catch (InstanceNotFoundException e)
      {
         throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, e);
      }
      catch (ReflectionException e)
      {
         throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, e);
      }
      catch (Exception e)
      {
         org.jboss.mx.util.JMXExceptionDecoder.rethrow(e);

         // the compiler does not know an exception is thrown by the above
         throw new org.jboss.util.UnreachableStatementException();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }      
   }
}
// vim:expandtab:tabstop=3:shiftwidth=3
