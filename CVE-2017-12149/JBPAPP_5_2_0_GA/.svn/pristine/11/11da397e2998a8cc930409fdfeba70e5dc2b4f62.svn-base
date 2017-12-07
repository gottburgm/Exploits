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
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.invocation.unified.interfaces.UnifiedInvokerHAProxy;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.remoting.InvocationRequest;
import org.jboss.system.Registry;

/**
 * Unified invoker implementation for InvokerHA 
 * 
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class UnifiedInvokerHA extends UnifiedInvoker implements InvokerHA
{
   private HashMap beanMap = new HashMap();

   public UnifiedInvokerHA()
   {
      super();
      setSubSystem("invokerha");
   }

   protected void jmxBind()
   {
      Registry.bind(getServiceName(), this);
   }

   public java.io.Serializable getStub()
   {
      return getInvoker().getLocator();
   }

   public void registerBean(ObjectName beanName, HATarget target) throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());

      if(beanMap.containsKey(hash))
      {
         log.debug("Trying to register target " + target + " using an existing hashCode. Already registered: " + hash + "=" + beanMap.get(hash));
         throw new IllegalStateException("Trying to register target using an existing hashCode.");
      }
      beanMap.put(hash, target);
   }

   public Invoker createProxy(ObjectName beanName, LoadBalancePolicy policy, String proxyFamilyName)
         throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      HATarget target = (HATarget) beanMap.get(hash);
      if(target == null)
      {
         throw new IllegalStateException("The bean hashCode not found");
      }

      String familyName = proxyFamilyName;
      if(familyName == null)
      {
         familyName = target.getAssociatedPartition().getPartitionName() + "/" + beanName;
      }

      return createProxy(getStrictRMIException(),
            target.getReplicants(), policy, proxyFamilyName, target.getCurrentViewId());
   }

   public void unregisterBean(ObjectName beanName) throws Exception
   {
      Integer hash = new Integer(beanName.hashCode());
      beanMap.remove(hash);
   }

   /**
    * Implementation of the server invoker handler interface.  Will take the invocation request
    * and invoke down the interceptor chain.
    *
    * @param invocationReq
    * @return response of the invocation
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

         /** Clustering **/
         long clientViewId = ((Long) invocation.getValue("CLUSTER_VIEW_ID")).longValue();
         HATarget target = (HATarget) beanMap.get(invocation.getObjectName());
         if(target == null)
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

         if(!target.invocationsAllowed())
         {
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO,
                                                 "invocations are currently not allowed on this target");
         }
         /** End Clustering **/

         // The cl on the thread should be set in another interceptor
         Object obj = getServer().invoke(mbean,
                                         "invoke",
                                         new Object[]{invocation},
                                         Invocation.INVOKE_SIGNATURE);

         /** Clustering **/

         HARMIResponse haResponse = new HARMIResponse();

         if(clientViewId != target.getCurrentViewId())
         {
            haResponse.newReplicants = new ArrayList(target.getReplicants());
            haResponse.currentViewId = target.getCurrentViewId();
         }
         haResponse.response = obj;

         /** End Clustering **/

         return new MarshalledObject(haResponse);
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

   @Override
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      ObjectName result = super.preRegister(server, name);
      log.info("Service name is " + getServiceName());
      return result;
   }
   
   protected Invoker createProxy(boolean isStrictRMIException,
         ArrayList targets, LoadBalancePolicy policy,
         String proxyFamilyName, long viewId)
   {
      return new UnifiedInvokerHAProxy(getInvoker().getLocator(), isStrictRMIException,
            targets, policy, proxyFamilyName, viewId);
   }
}