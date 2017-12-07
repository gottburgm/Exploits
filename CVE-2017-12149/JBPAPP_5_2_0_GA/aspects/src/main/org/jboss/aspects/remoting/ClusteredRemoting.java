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
package org.jboss.aspects.remoting;

import org.jboss.aop.Advised;
import org.jboss.aop.Dispatcher;
import org.jboss.aop.InstanceAdvised;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.security.SecurityClientInterceptor;
import org.jboss.aspects.tx.ClientTxPropagationInterceptor;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.ClusteringTargetsRepository;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 80997 $
 */

public class ClusteredRemoting implements ClusterConstants
{
   private static final Logger log = Logger.getLogger(ClusteredRemoting.class);

   public static ClassProxy clusterObject(Object objectId, Object obj, String partitionName, LoadBalancePolicy lb, InvokerLocator locator)
   throws Exception
   {
      String proxyFamilyName = objectId.toString() + locator.getProtocol() + partitionName;
      HAPartition partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(partitionName, null);

      HATarget target = null;
      Map families = null;
      InstanceAdvisor advisor = null;
      Class clazz;

      if (obj instanceof Advised)
      {
         advisor = ((Advised) obj)._getInstanceAdvisor();
         clazz = obj.getClass();
         Dispatcher.singleton.registerTarget(objectId, obj);
      }
      else
      {
         clazz = obj.getClass();
         ClassProxy proxy = ClassProxyFactory.newInstance(obj.getClass());
         advisor = proxy._getInstanceAdvisor();
         advisor.insertInterceptor(new ForwardingInterceptor(obj));
         Dispatcher.singleton.registerTarget(objectId, proxy);
      }
      families = (Map) advisor.getMetaData().getMetaData(CLUSTERED_REMOTING, CLUSTER_FAMILIES);
      if (families != null)
      {
         target = (HATarget) families.get(proxyFamilyName);
         if (target == null)
         {
            target = new HATarget(partition, proxyFamilyName, locator, HATarget.ENABLE_INVOCATIONS);
            ClusteringTargetsRepository.initTarget(proxyFamilyName, target.getReplicants());
            families.put(proxyFamilyName, target);
         }
      }
      else
      {
         families = new HashMap();
         target = new HATarget(partition, proxyFamilyName, locator, HATarget.ENABLE_INVOCATIONS);
         ClusteringTargetsRepository.initTarget(proxyFamilyName, target.getReplicants());
         families.put(proxyFamilyName, target);
         advisor.insertInterceptor(0, new ReplicantsManagerInterceptor(families));
      }

      ClassProxy proxy = ClassProxyFactory.newInstance(clazz);
      InstanceAdvisor proxyAdvisor = proxy._getInstanceAdvisor();
      proxyAdvisor.insertInterceptor(IsLocalInterceptor.singleton);
      advisor.insertInterceptor(SecurityClientInterceptor.singleton);
      advisor.insertInterceptor(ClientTxPropagationInterceptor.singleton);
      proxyAdvisor.insertInterceptor(MergeMetaDataInterceptor.singleton);
      proxyAdvisor.insertInterceptor(ClusterChooserInterceptor.singleton);
      proxyAdvisor.insertInterceptor(InvokeRemoteInterceptor.singleton);

      proxyAdvisor.getMetaData().addMetaData(CLUSTERED_REMOTING,
      CLUSTER_FAMILY_WRAPPER,
      new FamilyWrapper(proxyFamilyName, target.getReplicants()),
      PayloadKey.AS_IS);

      // LB policy should be AS_IS so that remoting framework downloads the classes
      proxyAdvisor.getMetaData().addMetaData(CLUSTERED_REMOTING,
      LOADBALANCE_POLICY,
      lb,
      PayloadKey.AS_IS);

      proxyAdvisor.getMetaData().addMetaData(InvokeRemoteInterceptor.REMOTING,
      InvokeRemoteInterceptor.SUBSYSTEM,
      "AOP",
      PayloadKey.AS_IS);

      proxyAdvisor.getMetaData().addMetaData(Dispatcher.DISPATCHER,
      Dispatcher.OID,
      objectId,
      PayloadKey.AS_IS);

      return proxy;
   }

   public static void unregisterClusteredObject(Object object)
   {
      try
      {
         ClassProxy proxy = (ClassProxy) object;
         InstanceAdvisor advisor = proxy._getInstanceAdvisor();

         String oid = (String) advisor.getMetaData().getMetaData(Dispatcher.DISPATCHER, Dispatcher.OID);
         InstanceAdvised registeredObject = (InstanceAdvised) Dispatcher.singleton.getRegistered(oid);
         if (registeredObject == null) throw new NotRegisteredException(oid.toString() + " is not registered");
         Dispatcher.singleton.unregisterTarget(oid);

         advisor = registeredObject._getInstanceAdvisor();
         Map families = (Map) advisor.getMetaData().getMetaData(CLUSTERED_REMOTING, CLUSTER_FAMILIES);
         Iterator it = families.values().iterator();
         while (it.hasNext())
         {
            HATarget target = (HATarget) it.next();
            target.destroy();
         }
      }
      catch (Exception ignored)
      {
         log.trace("Ignored exception unregistering a clustered object", ignored);
      }
   }
}
