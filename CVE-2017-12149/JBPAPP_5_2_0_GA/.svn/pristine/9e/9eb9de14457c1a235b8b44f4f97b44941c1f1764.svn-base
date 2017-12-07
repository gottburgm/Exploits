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
package org.jboss.ha.jmx;

import java.io.Serializable;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.api.model.FromContext;
import org.jboss.ha.framework.interfaces.DistributedState;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAService;
import org.jboss.ha.framework.server.ClusterPartitionMBean;
import org.jboss.ha.framework.server.EventFacility;
import org.jboss.ha.framework.server.EventFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Abstract implementation of HAServiceMBean for use by subclasses, e.g. {@link org.jboss.ha.singleton.HASingletonSupport}.
 * 
 * @param S the class of the HAService delegate
 * @author Paul Ferraro
 */
@SuppressWarnings("deprecation")
public abstract class AbstractHAServiceMBeanSupport<S extends HAService<Notification>>
   extends ServiceMBeanSupport
   implements HAServiceMBean, HAService<Notification>, EventFactory<Notification>, EventFacility<Notification>
{
   private final S service = this.createHAService();
   private ClusterPartitionMBean clusterPartition;

   private volatile boolean sendLocalLifecycleNotifications = true;
   private volatile boolean sendRemoteLifecycleNotifications = true;

   /**
    * Defines the object used to delegate service methods.
    * @return an {@link HAService} delegate
    */
   protected abstract S createHAService();

   /**
    * @return the object created by {@link #createHAService()}.
    */
   protected final S getHAService()
   {
      return this.service;
   }
   
   // Public --------------------------------------------------------
   
   public HAPartition getHAPartition()
   {
      return this.service.getHAPartition();
   }
   
   public void setHAPartition(HAPartition partition)
   {
      this.service.setHAPartition(partition);
   }

   public void setClusterPartition(ClusterPartitionMBean clusterPartition)
   {
      if ((this.getState() != STARTED) && (this.getState() != STARTING))
      {
         this.clusterPartition = clusterPartition;
      }
   }

   public String getPartitionName()
   {
      return this.service.getHAPartition().getPartitionName();
   }
   
   public boolean isRegisterThreadContextClassLoader()
   {
      return this.service.isRegisterThreadContextClassLoader();
   }
   
   public void setRegisterThreadContextClassLoader(boolean register)
   {
      this.service.setRegisterThreadContextClassLoader(register);
   }

   public String getHAServiceKey()
   {
      return this.service.getHAServiceKey();
   }
   
   // Protected ------------------------------

   /**
    * 
    * 
    * Convenience method for sharing state across a cluster partition.
    * Delegates to the DistributedStateService
    * 
    * @param key key for the distributed object
    * @param value the distributed object
    * 
    */
   public void setDistributedState(String key, Serializable value) throws Exception
   {
      DistributedState ds = this.getHAPartition().getDistributedStateService();
      ds.set(this.getHAServiceKey(), key, value);
   }

   /**
    * 
    * Convenience method for sharing state across a cluster partition.
    * Delegates to the DistributedStateService
    * 
    * @param key key for the distributed object
    * @return Serializable the distributed object
    * 
    */
   public Serializable getDistributedState(String key)
   {
      DistributedState ds = this.getHAPartition().getDistributedStateService();
      return ds.get(this.getHAServiceKey(), key);
   }

   /**
    * @see org.jboss.system.ServiceMBeanSupport#createService()
    */
   @Override
   protected void createService() throws Exception
   {
      this.service.create();
   }
   
   /**
    * <p>
    * Implementors of this method should not
    * code the singleton logic here.
    * The MBean lifecycle create/start/stop are separate from
    * the singleton logic.
    * Singleton logic should originate in becomeMaster().
    * </p>
    * 
    * <p>
    * <b>Attention</b>: Always call this method when you overwrite it in a subclass
    *                   because it elects the master singleton node.
    * </p>
    * 
    */
   @Override
   protected void startService() throws Exception
   {
      this.log.debug("start HAServiceMBeanSupport");
      
      this.setupPartition();

      if (this.getHAPartition() == null)
      {
         throw new IllegalStateException("HAPartition property must be set before starting HAServiceMBeanSupport");
      }

      // Default serviceHAName, if none defined
      if (this.service.getServiceHAName() == null)
      {
         ObjectName name = this.getServiceName();
         
         if (name != null)
         {
            this.service.setServiceHAName(name.getCanonicalName());
         }
         else
         {
            // This shouldn't occur as the service name is now injected by the microcontainer.
            //  If injection fails, the service name should then be used.
            throw new IllegalStateException("Cannot determine ServiceHAName for " + 
                    getClass().getName() + "; either set it explicitly " + 
                    "or register this object in JMX before calling create");
         }
      }
      
      this.service.start();
   }
   
   /**
    * <p>
    * <b>Attention</b>: Always call this method when you overwrite it in a subclass
    * </p>
    * 
    */
   @Override
   protected void stopService() throws Exception
   {
      this.log.debug("stop HAServiceMBeanSupport");

      this.service.stop();
   }

   /**
    * @see org.jboss.system.ServiceMBeanSupport#destroyService()
    */
   @Override
   protected void destroyService() throws Exception
   {
      this.service.destroy();
   }

   protected void setupPartition() throws Exception
   {
      if ( clusterPartition != null )
         this.service.setHAPartition(clusterPartition.getHAPartition());
   }

   @SuppressWarnings("unchecked")
   public List callMethodOnPartition(String methodName, Object[] args, Class[] types) throws Exception
   {
      return this.getHAPartition().callMethodOnCluster(this.getHAServiceKey(), methodName, args, types, true);
   }

   @SuppressWarnings("unchecked")
   protected void callAsyncMethodOnPartition(String methodName, Object[] args, Class[] types) throws Exception
   {
      this.getHAPartition().callAsynchMethodOnCluster(this.getHAServiceKey(), methodName, args, types, true);
   }
   
   /**
    * Gets whether JMX Notifications should be sent to local (same JVM) listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * @see #sendNotification(Notification)
    */
   public boolean getSendLocalLifecycleNotifications()
   {
      return this.sendLocalLifecycleNotifications;
   }

   /**
    * Sets whether JMX Notifications should be sent to local (same JVM) listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * @see #sendNotification(Notification)
    */
   public void setSendLocalLifecycleNotifications(boolean sendLocalLifecycleNotifications)
   {
      this.sendLocalLifecycleNotifications = sendLocalLifecycleNotifications;
   }

   /**
    * Gets whether JMX Notifications should be sent to remote listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * <p>
    * See http://jira.jboss.com/jira/browse/JBAS-3194 for an example of a
    * use case where this property should be set to false.
    * </p>
    * 
    * @see #sendNotification(Notification)
    */
   public boolean getSendRemoteLifecycleNotifications()
   {
      return this.sendRemoteLifecycleNotifications;
   }

   /**
    * Sets whether JMX Notifications should be sent to remote listeners
    * if the notification is for an attribute change to attribute "State".
    * <p>
    * Default is <code>true</code>.
    * </p>
    * <p>
    * See http://jira.jboss.com/jira/browse/JBAS-3194 for an example of a
    * use case where this property should be set to false.
    * </p>
    * 
    * @see #sendNotification(Notification)
    */
   public void setSendRemoteLifecycleNotifications(boolean sendRemoteLifecycleNotifications)
   {
      this.sendRemoteLifecycleNotifications = sendRemoteLifecycleNotifications;
   }

   /** 
    * Broadcast the notification to the remote listener nodes (if any) and then 
    * invoke super.sendNotification() to notify local listeners.
    * 
    * @param notification sent out to local listeners and other nodes. It should be serializable.
    * It is recommended that the source of the notification is an ObjectName of an MBean that 
    * is is available on all nodes where the broadcaster MBean is registered. 
    *   
    * @see #getSendLocalLifecycleNotifications()
    * @see #getSendRemoteLifecycleNotifications()
    * @see javax.management.NotificationBroadcasterSupport#sendNotification(Notification)
    * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#sendNotification(javax.management.Notification)
    */
   public void sendNotification(Notification notification)
   {
      boolean stateChange = (notification instanceof AttributeChangeNotification) ? "State".equals(((AttributeChangeNotification) notification).getAttributeName()) : false;
      
      if (!stateChange || this.sendRemoteLifecycleNotifications)
      {
         try
         {
            this.sendNotificationRemote(notification);
         }
         catch (Throwable e)
         {
            // even if broadcast failed, local notification should still be sent
            this.log.warn("handleNotification( " + notification + " ) failed ", e);
         }
      }
      
      if (!stateChange || this.sendLocalLifecycleNotifications)
      {
         this.sendNotificationToLocalListeners(notification);
      }
   }

   /**
    * 
    * Broadcast a notifcation remotely to the partition participants
    * 
    * @param notification
    */
   protected void sendNotificationRemote(Notification notification) throws Exception
   {
      // Overriding the source MBean with its ObjectName
      // to ensure that it can be safely transferred over the wire
      notification.setSource(this.getServiceName());
      
      this.service.handleEvent(notification);
   }

   protected void sendNotificationToLocalListeners(Notification notification)
   {
      this.notifyListeners(notification);
   }

   /**
    * @see org.jboss.ha.framework.server.EventFacility#notifyListeners(java.util.EventObject)
    */
   public void notifyListeners(Notification notification)
   {
      super.sendNotification(notification);
   }

   /**
    * @see org.jboss.ha.framework.interfaces.EventListener#handleEvent(java.util.EventObject)
    */
   public void handleEvent(Notification notification) throws Exception
   {
      this.notifyListeners(notification);
   }

   /**
    * @see org.jboss.ha.framework.server.EventFactory#createEvent(java.lang.Object, java.lang.String)
    */
   public Notification createEvent(Object source, String type)
   {
      return new Notification(type, this, this.getNextNotificationSequenceNumber());
   }

   /**
    * 
    * Override this method only if you need to provide a custom partition wide unique service name.
    * The default implementation will usually work, provided that
    * the getServiceName() method returns a unique canonical MBean name.
    * 
    * @return partition wide unique service name
    */
   public String getServiceHAName()
   {
      return this.service.getServiceHAName();
   }
   
   @Inject(fromContext = FromContext.NAME)
   public void setServiceHAName(String haName)
   {
      this.service.setServiceHAName(haName);
   }
}
