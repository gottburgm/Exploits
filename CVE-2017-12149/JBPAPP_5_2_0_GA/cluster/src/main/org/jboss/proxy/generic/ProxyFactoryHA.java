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
package org.jboss.proxy.generic;

import java.util.List;

import javax.management.AttributeChangeNotificationFilter;
import javax.management.NotificationListener;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.invocation.InvokerProxyHA;
import org.jboss.invocation.jrmp.server.JRMPProxyFactory;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.RoundRobin;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBean;

/**
 * ProxyFactory for Clustering
 *
 *  @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 *  @version $Revision: 82169 $
 */
public class ProxyFactoryHA 
   extends JRMPProxyFactory
   implements ProxyFactoryHAMBean, DistributedReplicantManager.ReplicantListener
{
   protected String replicantName = null;   
   protected InvokerHA invokerHA;
   protected HATarget target;
   protected Invoker invoker;
   protected DistributedReplicantManager drm = null;
   protected HAPartition partition;
   protected String loadBalancePolicy = RoundRobin.class.getName();
   protected NotificationListener listener;
   protected int state = 0;

   public HAPartition getPartition()
   {
      return partition;
   }

   public void setPartition(HAPartition partition)
   {
      this.partition = partition;
   }
   
   public String getLoadBalancePolicy()
   {
      return loadBalancePolicy;
   }

   public void setLoadBalancePolicy(String loadBalancePolicy)
   {
      this.loadBalancePolicy = loadBalancePolicy;
   }

   public void createService() throws Exception
   {
      super.createService();
      
      // we register our inner-class to retrieve STATE notifications from our container
      AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
      filter.enableAttribute("State");
      listener = new StateChangeListener();
      getServer().addNotificationListener(getTargetName(), listener, filter, null);
   }
   
   protected void startService() throws Exception
   {
      String partitionName = partition.getPartitionName();
      this.drm = partition.getDistributedReplicantManager();
      
      replicantName = getTargetName().toString();
      
      invokerHA = (InvokerHA) Registry.lookup(getInvokerName());
      if (invokerHA == null)
         throw new RuntimeException("Invoker is not registered: " + getInvokerName());

      int mode = HATarget.MAKE_INVOCATIONS_WAIT;
      if (state == ServiceMBean.STARTED)
         mode = HATarget.ENABLE_INVOCATIONS;
      target = new HATarget(partition, replicantName, invokerHA.getStub(), mode);
      invokerHA.registerBean(getServiceName(), target);

      String clusterFamilyName = partitionName + "/" + getTargetName() + "/";
      
      // make ABSOLUTLY sure we do register with the DRM AFTER the HATarget
      // otherwise we will refresh the *old* home in JNDI (ie before the proxy
      // is re-generated)
      drm.registerListener (replicantName, this);
      
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz;
      LoadBalancePolicy policy;
      
      clazz = cl.loadClass(loadBalancePolicy);
      policy = (LoadBalancePolicy)clazz.newInstance();
      invoker = invokerHA.createProxy(getServiceName(), policy, clusterFamilyName + "H");
      
      // JRMPInvokerProxyHA.colocation.add(new Integer(jmxNameHash));

      super.startService();
   }
   
   public void stopService() throws Exception
   {
      super.stopService();

      // JBAS-5164.  Unregister the listener first, or when we destroy
      // the target we will get a callback and rebind the proxy
      if (drm != null)
         drm.unregisterListener(replicantName, this);
      
      try
      {
         // JRMPInvokerProxyHA.colocation.remove(new Integer(jmxNameHash));
         invokerHA.unregisterBean(getServiceName());
         target.destroy();
      } 
      catch (Exception ignored)
      {
         // ignore.
      }
   }

   protected void destroyService() throws Exception
   {
      super.destroyService();
      getServer().removeNotificationListener(getTargetName(), listener);
   }

   protected void containerIsFullyStarted ()
   {
      if (target != null)
         target.setInvocationsAuthorization(HATarget.ENABLE_INVOCATIONS);
   }
   
   protected void containerIsAboutToStop()
   {
      if (target != null)
      {
         target.setInvocationsAuthorization(HATarget.DISABLE_INVOCATIONS);
         target.disable();
      }
   }

   // synchronized keyword added when it became possible for DRM to issue
   // concurrent replicantsChanged notifications. JBAS-2169.
   public synchronized void replicantsChanged(String key, 
                                              List newReplicants, 
                                              int newReplicantsViewId, 
                                              boolean merge)
   {
      try
      {
         if (invoker instanceof InvokerProxyHA)
            ((InvokerProxyHA) invoker).updateClusterInfo(target.getReplicants(), target.getCurrentViewId());

         log.debug ("Rebinding in JNDI... " + key);
         rebind();
      }
      catch (Exception none)
      {
         log.debug(none);
      }
   }

   protected void createProxy
   (
      Object cacheID, 
      String proxyBindingName,
      ClassLoader loader,
      Class[] ifaces
   )
   {
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      theProxy = proxyFactory.createProxy(cacheID, getServiceName(), invoker,
         getJndiName(), proxyBindingName, getInterceptorClasses(), loader, ifaces);
   }

   // inner-classes
   
   class StateChangeListener implements NotificationListener
   {
      public void handleNotification (Notification notification, Object handback)
      {
         if (notification instanceof AttributeChangeNotification)
         {
            AttributeChangeNotification notif = (AttributeChangeNotification) notification;
            state = ((Integer)notif.getNewValue()).intValue();
            
            if (state == ServiceMBean.STARTED)
            {
               log.debug ("Started: enabling remote access to mbean " + getTargetName());
               containerIsFullyStarted ();
            }
            else if (state == ServiceMBean.STOPPING)
            {
               log.debug ("About to stop: disabling remote access to mbean " + getTargetName());
               containerIsAboutToStop ();
            }
         }
      }
      
   }
}
