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
package org.jboss.proxy.ejb;

import java.util.List;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import org.jboss.system.Registry;
import org.jboss.logging.Logger;
import org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxyHA;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.ha.framework.server.HATarget;

import javax.management.AttributeChangeNotificationFilter;
import javax.management.NotificationListener;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import org.jboss.invocation.InvokerProxyHA;
import org.jboss.invocation.InvokerHA;
import org.jboss.system.ServiceMBean;
import org.jboss.naming.Util;

/**
* ProxyFactory for Clustering
*
*  @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
*  @version $Revision: 81001 $
*
*  <p><b>Revisions:</b><br>
*  <p><b>2002/01/13: billb</b>
*  <ol>
*   <li>Initial Revisition
*  </ol>
* <p><b>2002/08/24: Sacha Labourey</b>
* <ol>
*   <li>Added a "Proxy Family" string that identifies, for a same HATarget,
        different families of proxies (remote, home, etc.) that may each
        have its own client behaviour (round robin, etc.) => each needs its own
        id in the Proxy Family Repository on the client side</li>
* </ol>
*/
public class ProxyFactoryHA 
   extends ProxyFactory
   implements DistributedReplicantManager.ReplicantListener, ClusterProxyFactory
{
   
   protected static Logger log = Logger.getLogger(ProxyFactory.class);
   protected String replicantName = null;   
   protected InvokerHA jrmp;
   protected HATarget target;
   
   /** 
    * Track whether we're in a start()/stop()/start() cycle not driven
    * by our EjbModule, in which case we will not get a JMX start 
    * notification from EjbModule
    */
   private boolean containerRestart = true;
   /** Are we in the middle of an EjbModule stop and thus not a containerRestart?*/
   private boolean ejbModuleStopping = false;
   
   protected DistributedReplicantManager drm = null;
   
   public void create () throws Exception
   {
      super.create ();
      
      // we register our inner-class to retrieve STATE notifications from our container
      //
      AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter ();
      filter.enableAttribute ("State");
      
      // ************************************************************************
      // NOTE: We could also subscribe for the container service events instead of the
      // ejbModule service events. This problem comes from beans using other beans
      // in the same ejbModule: we may receive an IllegalStateException thrown
      // by the Container implementation. Using ejbModule events solve this
      // problem. 
      // ************************************************************************
      this.container.getServer ().
         addNotificationListener (this.container.getEjbModule ().getServiceName (), 
                                  new ProxyFactoryHA.StateChangeListener (), 
                                  filter, 
                                  null);
   }
   
   public void start () throws Exception
   {
      super.start ();
      
      if (containerRestart)
      {
         containerIsFullyStarted();
         containerRestart = false;
      }
   }
   
   protected void setupInvokers() throws Exception
   {
      String partitionName = container.getBeanMetaData().getClusterConfigMetaData().getPartitionName();
      HAPartition partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(partitionName, null);
      this.drm = partition.getDistributedReplicantManager ();
      
      replicantName = jmxName.toString ();
      
      ObjectName oname = new ObjectName(invokerMetaData.getInvokerMBean());
      jrmp = (InvokerHA)Registry.lookup(oname);
      if (jrmp == null)
         throw new RuntimeException("home JRMPInvokerHA is null: " + oname);


      target = new HATarget(partition, replicantName, jrmp.getStub (), HATarget.MAKE_INVOCATIONS_WAIT);
      jrmp.registerBean(jmxName, target);

      String clusterFamilyName = partitionName + "/" + jmxName + "/";
      
      // make ABSOLUTLY sure we do register with the DRM AFTER the HATarget
      // otherwise we will refresh the *old* home in JNDI (ie before the proxy
      // is re-generated)
      //
      drm.registerListener (replicantName, this);
      
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz;
      LoadBalancePolicy policy;
      
      clazz = cl.loadClass(container.getBeanMetaData().getClusterConfigMetaData().getHomeLoadBalancePolicy());
      policy = (LoadBalancePolicy)clazz.newInstance();
      homeInvoker = jrmp.createProxy(jmxName, policy, clusterFamilyName + "H");
      
      // (Re-)Bind the home invoker in the JNDI naming space
      InitialContext ctx = new InitialContext();      
      String homeName = jndiBinding + "-HomeInvoker";
      log.debug("(re-)Binding Home invoker under: " + homeName);
      Util.rebind(ctx,
         // Jndi name
         homeName,
         // The home invoker
         homeInvoker
      );

      clazz = cl.loadClass(container.getBeanMetaData().getClusterConfigMetaData().getBeanLoadBalancePolicy());
      policy = (LoadBalancePolicy)clazz.newInstance();
      beanInvoker = jrmp.createProxy(jmxName, policy, clusterFamilyName + "R");
      // (Re-)Bind the remote invoker in the JNDI naming space
      String beanName = jndiBinding + "-RemoteInvoker";
      log.debug("(re-)Binding Remote invoker under: " + beanName);
      Util.rebind(ctx,
         // Jndi name
         beanName,
         // The bean invoker
         beanInvoker
      );

      JRMPInvokerProxyHA.colocation.add(new Integer(jmxNameHash));
   }
   
   
   @Override
   public void stop()
   {
      super.stop();
      
      teardownInvokers();
      
      if (!ejbModuleStopping)
         containerRestart = true;
      else
         ejbModuleStopping = false;
   }

   @Override
   public void destroy()
   {
      super.destroy();
      
      containerRestart = false;
      ejbModuleStopping = false;
   }

   private void teardownInvokers()
   {
      try
      {
         JRMPInvokerProxyHA.colocation.remove(new Integer(jmxNameHash));
         jrmp.unregisterBean(jmxName);
         target.destroy();
      } 
      catch (Exception ignored)
      {
         // ignore.
      }
      try
      {
         InitialContext ctx = new InitialContext();
         String homeInvokerName = jndiBinding + "-HomeInvoker";
         ctx.unbind(homeInvokerName);
      }
      catch(Exception ignored)
      {
      }
      try
      {
         InitialContext ctx = new InitialContext();
         String beanInvokerName = jndiBinding + "-RemoteInvoker";
         ctx.unbind(beanInvokerName);
      }
      catch(Exception ignored)
      {
      }

      if( drm != null )
         drm.unregisterListener (replicantName, this);
   }

   protected void containerIsFullyStarted ()
   {
      if( target != null )
         target.setInvocationsAuthorization (HATarget.ENABLE_INVOCATIONS);
   }
   
   protected void containerIsAboutToStop ()
   {
      if( target != null )
      {
         target.setInvocationsAuthorization (HATarget.DISABLE_INVOCATIONS);
         target.disable ();
      }
      
      this.ejbModuleStopping = true;
   }

   // synchronized keyword added when it became possible for DRM to issue
   // concurrent replicantsChanged notifications. JBAS-2169.
   public synchronized void replicantsChanged (String key, 
                                               List newReplicants, 
                                               int newReplicantsViewId, 
                                               boolean merge)
   {
      try
      {
         if (homeInvoker instanceof InvokerProxyHA)
         {
            ((InvokerProxyHA)homeInvoker).updateClusterInfo (target.getReplicants(), target.getCurrentViewId ());
         }
         if (beanInvoker instanceof InvokerProxyHA)
         {
            ((InvokerProxyHA)beanInvoker).updateClusterInfo (target.getReplicants(), target.getCurrentViewId ());
         }

         log.debug ("Rebinding in JNDI... " + key);
         rebindHomeProxy ();
      }
      catch (Exception none)
      {
         log.debug (none);
      }
   }

   // inner-classes
   
   class StateChangeListener implements NotificationListener
   {
      
      public void handleNotification (Notification notification, java.lang.Object handback)
      {
         if (notification instanceof AttributeChangeNotification)
         {
            AttributeChangeNotification notif = (AttributeChangeNotification) notification;
            int value = ((Integer)notif.getNewValue()).intValue ();
            
            if (value == ServiceMBean.STARTED)
            {
               log.debug ("Container fully started: enabling HA-RMI access to bean");              
               containerIsFullyStarted ();
            }
            else if (value == ServiceMBean.STOPPING)
            {
               log.debug ("Container about to stop: disabling HA-RMI access to bean");
               containerIsAboutToStop ();
            }
         }
      }
      
   }
}
