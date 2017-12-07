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
package org.jboss.invocation.jrmp.server;

import java.rmi.MarshalledObject;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.system.Registry;

import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.GenericClusteringException;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The JRMPInvokerHA is an HA-RMI implementation that can generate Invocations from RMI/JRMP 
 * into the JMX base
 *
 * @author <a href="mailto:bill@burkecentral.com>Bill Burke</a>
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 81001 $
 */
public class JRMPInvokerHA
   extends JRMPInvoker
   implements InvokerHA
{
   /** @since v4.2.3 */
   private static final long serialVersionUID = -7657305823982668529L;

   protected HashMap beanMap = new HashMap();
   
   protected ObjectName serviceName;
   
   /**
    * Explicit no-args constructor.
    */
   public JRMPInvokerHA()
   {
      super();
   }

   // JRMPInvoker.createService() does the right thing
   
   protected void startService() throws Exception
   {
      loadCustomSocketFactories();

      if (log.isDebugEnabled())
      {
         log.debug("RMI Port='" +  (rmiPort == ANONYMOUS_PORT ?
            "Anonymous" : Integer.toString(rmiPort)+"'"));
         log.debug("Client SocketFactory='" + (clientSocketFactory == null ?
            "Default" : clientSocketFactory.toString()+"'"));
         log.debug("Server SocketFactory='" + (serverSocketFactory == null ?
            "Default" : serverSocketFactory.toString()+"'"));
         log.debug("Server SocketAddr='" + (serverAddress == null ?
            "Default" : serverAddress+"'"));
         log.debug("SecurityDomain='" + (sslDomain == null ?
            "None" : sslDomain+"'"));
      }

      exportCI();
      Registry.bind(getServiceName(), this);
   }

   protected void stopService() throws Exception
   {
      unexportCI();
   }

   // JRMPInvoker.destroyService() does the right thing

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

      return createProxy(target.getReplicants(), policy, familyName, target.getCurrentViewId ());
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
         Object rtn = support.getServer().invoke(mbean,
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
         
         return new MarshalledObject(rsp);
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
   
   public ObjectName getServiceName()
   {
      return (serviceName == null ? support.getServiceName() : serviceName);
   }
   
   public void setServiceName(ObjectName serviceName)
   {
      this.serviceName = serviceName;
   }

   @Override
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      ObjectName result = super.preRegister(server, name);
      
      if (!result.equals(getServiceName()))
         throw new IllegalStateException("JMX registration (" + result + 
               ") differs from our configured service name (" + 
               getServiceName() +")");
      
      return result;
   }
   
   protected Invoker createProxy(ArrayList targets, LoadBalancePolicy policy,
         String proxyFamilyName, long viewId)
   {
      return new JRMPInvokerProxyHA(targets, policy, proxyFamilyName, viewId);
   }   
   
   
   
}

