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
package org.jboss.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.server.ServerConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract base class that provides for declarative JMX notification
 * subscription handling.
 * <p>
 * A JBoss service that is in addition a NotificationListener can
 * subclass ListenerServiceMBeanSupport instead of ServiceMBeanSupport
 * and specify at run-time, inline in the MBean descriptor using the
 * SubscriptionList attribute, the set of MBeans/notifications the
 * service wants to subscribe/receive.
 * <p>
 * Call subscribe(boolean dynamicSubscriptions) at anytime to register to
 * those MBeans and for those notifications that match the specified criteria.
 * Call unsubscribe() to unsubscribe for Notifications.
 * <p>
 * If true is passed to subscribe() the baseclass will monitor for
 * registration events from the MBeanServer and automatically subscribe
 * to new instances of MBeans that match the subscription criteria.
 * Monitoring for unsubscribe events in not necessary, since the MBeanServer
 * automatically removes subscriptions to unregistering MBeans.
 * <p>
 * An alternative subscribe(boolean dynamicSubscription, ObjectName listener)
 * can be used to specify a different MBean as the receiver of the
 * subscribed notifications. The specified MBean must be a NotificationListener.
 * <p>
 * To handle the incoming notifications override the handleNotification2()
 * method. The usual handleNotification() method should not be overriden,
 * since it is used to monitor the incoming notifications for registration
 * events coming from the MBeanServer, before delegating to
 * handleNotification2(), in order to implement dynamic subscriptions.
 *
 * @see ListenerServiceMBean
 * @see NotificationFilterFactory
 *
 * REVISIONS
 * =========
 * 14/03/05, dimitris
 * The filter mechanism has been extended to support specification
 * of arbitrary filters, using dynamic filter factory plugins
 * implementing the NotificationFilterFactory interface.
 * Three filter factories corresponding to the "standard" jmx
 * notification filters are supplied by default in package
 * org.jboss.system.filterfactory.
 * 
 * 19/10/04, dimitris
 * renamed inner class MBeanInfo to SubscriptionInfo and made public,
 * using NotificationFilter instead of NotificationFilterSupport and added new
 * subscribe(List subscriptionList, boolean dynamicSubscriptions, ObjectName listener)
 * to allow external programmatic specification of the subscription list.
 *  
 * 28/02/04, dimitris
 * explicit subscribe()/unsubscribe() replaced implicit start()/stop();
 * dynamic subscription behaviour can be enabled/disabled, plus it is
 * now possible to specify an external MBean as notification listener.
 * 
 * 02/02/04, dimitris
 * Initial version, that resulted by generalizing the notification
 * subscription mechanism of the snmp adapter.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 *
 * @version $Revision: 81033 $
**/
public abstract class ListenerServiceMBeanSupport
   extends ServiceMBeanSupport
   implements ListenerServiceMBean, NotificationListener
{
   // Private Data --------------------------------------------------
     
   /** The list of mbean subscriptions */
   private List sublist; // if null, subscriptions not made
   
   /** The mbean subscription config in XML form */
   private Element xmllist; // set through SubscriptionList attribute
   
   /** monitoring and registering to new MBeans, as they appear */
   private boolean dynamicSubscriptions;
   
   /** the receiver of the notifications */
   private ObjectName listener;
   
   /** Handback to identify our own MBeanServerDelegate subscription */
   private Object myHandback;  
   
   /** Filter to receive only registration events */
   private NotificationFilterSupport myFilter;   
  
   /** Has subscribe() been called and unsubscribe not been called? */
   private boolean subscribed;
   
   // Constructors -------------------------------------------------
    
   /**
    * Constructs a <tt>ListenerServiceMBeanSupport</tt>.
   **/
   public ListenerServiceMBeanSupport()
   {
        super();
        init();
   }

   /**
    * Constructs a <tt>ListenerServiceMBeanSupport</tt>.
    *
    * Pass-through to ServiceMBeanSupport.
    *
    * @param type   The class type to determine Logger name from.
   **/
   public ListenerServiceMBeanSupport(final Class type)
   {
      super(type);
      init();
   }
   
   /**
    * Constructs a <tt>ListenerServiceMBeanSupport</tt>.
    *
    * Pass-through to ServiceMBeanSupport.
    *
    * @param category   The logger category name.
   **/
   public ListenerServiceMBeanSupport(final String category)
   {
      super(category);
      init();
   }

   /**
    * Constructs a <tt>ListenerServiceMBeanSupport</tt>.
    *
    * Pass-through to ServiceMBeanSupport.
    *
    * @param log   The logger to use.
   **/
   public ListenerServiceMBeanSupport(final Logger log)
   {
      super(log);
      init();
   }    
    
   // ListenerServiceMBean Implementation ---------------------------
    
   /**
    * Used to configure the JMX notification subscriptions.
    *
    * The configuration is done inline in the mbean descriptor.
    *
    * See jboss-subscription.dtd
   **/
   public void setSubscriptionList(Element list)
   {
      // deep copy the provided Element for later use
      // not sure if really necessary - play it safe
      this.xmllist = (Element)list.cloneNode(true);
   }
   
   // Public API ----------------------------------------------------

   public List<SubscriptionInfo> getSubscriptions()
   {
      return sublist;
   }

   public void setSubscriptions(List<SubscriptionInfo> list)
   {
      this.sublist = list;
   }
   
   /**
    * Subscribes this MBean for JMX notifications.
    *
    * @param dynamicSubscriptions indicates whether to monitor and subscribe
    *                             to new MBeans that match the specification.
   **/
   public void subscribe(boolean dynamicSubscriptions)
      throws Exception
   {
      subscribe(dynamicSubscriptions, this.getServiceName());
   }
   
   /**
    * Subscribes a listener MBean for JMX notifications.
    *
    * @param dynamicSubscriptions indicates whether to monitor and subscribe
    *                             to new MBeans that match the specification.
    * @param listener the receiver of the notifications.
   **/
   public void subscribe(boolean dynamicSubscriptions, ObjectName listener)
      throws Exception
   {
      // we need an xml subscription specification
      if (this.xmllist != null && this.sublist == null)
      {
         // Parse the XML spec
         log.debug("Parsing subscription specification");
         List subscriptionList = parseXMLSubscriptionSpec(this.xmllist);
         
         subscribe(subscriptionList, dynamicSubscriptions, listener);
      }
      else if (this.sublist != null)
      {
         subscribe(sublist, dynamicSubscriptions, listener);
      }
      else
         log.debug("Subscription specification not provided");      
   }
   
   /**
    * Subscribes a listener MBean for JMX notifications.
    *
    * @param subscriptionList the list containing SubscriptionInfo data.
    * @param dynamicSubscriptions indicates whether to monitor and subscribe
    *                             to new MBeans that match the specification.
    * @param listener the receiver of the notifications.
   **/
   public void subscribe(List subscriptionList, boolean dynamicSubscriptions, ObjectName listener)
      throws Exception
   {
      // return if already subscribed
      if (subscribed)
         return;

      // we need an subscription specification
      if (subscriptionList != null)
      {
         // store input  
         this.sublist = subscriptionList;         
         this.dynamicSubscriptions = dynamicSubscriptions;
         this.listener = listener;         
         
         log.debug(this.sublist);
            
         log.debug("Subscribing for JMX notifications" +
                  ", dynamic=" + dynamicSubscriptions +
                  (this.getServiceName().equals(listener) ? "" :
                  ", listener='" + listener + "'"));
                  
         bulkRegister();
      
         if (dynamicSubscriptions == true)
         {
            // Subscribe to MBeanServerDelegate MBean for registrations
            getServer().addNotificationListener(
               new ObjectName(ServerConstants.MBEAN_SERVER_DELEGATE),
               this.getServiceName(),
               this.myFilter,
               this.myHandback
            );
            
            log.debug("Subscribed to MBeanServerDelegate, too");
         }
         
         subscribed = true;
      }
      else
         log.debug("Subscription list not provided");
   }

   /**
    * Unsubscribes for JMX notifications
   **/
   public void unsubscribe()
   {
      // return if not subscribed
      if (!subscribed)
         return;
      
      log.debug("Removing all JMX notification subscriptions");
      bulkUnregister();
      
      if (this.dynamicSubscriptions == true)
      {
         // Unbscribe from MBeanServerDelegate MBean for registrations
         try {
            getServer().removeNotificationListener(
               new ObjectName(ServerConstants.MBEAN_SERVER_DELEGATE),
               this.getServiceName(),
               this.myFilter,
               this.myHandback
            );             
         
            log.debug("Unsubscribed from MBeanServerDelegate, too");
         }
         catch (MalformedObjectNameException e) 
         {
            // shouldn't happen!
            log.warn("Could not convert '" + ServerConstants.MBEAN_SERVER_DELEGATE
                   + "' to ObjectName", e);
         }
         catch (InstanceNotFoundException e) 
         {
            // shouldn't happen
            log.warn("Could not unsubscribe from non-existent MBeanServerDelegate!", e);
         }
         catch (ListenerNotFoundException e) 
         {
            // shouldn't happend
            log.warn("Could not unsubscribe from MBeanServerDelegate", e);
         }
      }
      // indicate we've unsubscribed
      this.subscribed = false;
   }
   
   // NotificationListener -----------------------------------------
    
   /**
    * DO NOT OVERRIDE THIS!
    *
    * Handles dynamic subscriptions before delegating to
    * handleNotification2()
   **/
   public void handleNotification(Notification notification, Object handback)
   {
      // check if the notification is for me!
      if (this.dynamicSubscriptions == true && handback == this.myHandback) 
      {
         if (log.isTraceEnabled())
            log.trace("It's for me: " + notification + ", handback:" + handback);
         
         String type = notification.getType();
         ObjectName target = null;
         try {
            target = ((MBeanServerNotification)notification).getMBeanName();
         }
         catch (ClassCastException e) {
            log.warn("MBeanServer sent unknown notification class type: " +
                     notification.getClass().getName());
            return;
         }
               
         if (type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) 
         {
            // iterate over the subscription specification
            Iterator i = this.sublist.iterator();
      
            while (i.hasNext()) 
            {
               SubscriptionInfo mbeanInfo = (SubscriptionInfo)i.next();
         
               ObjectName objectName = mbeanInfo.getObjectName();
                  
               try 
               {
                  if(objectName.apply(target)) 
                  {
                     log.debug("ObjectName: '" + target + "' matched '" + objectName + "'");
                     
                     // go for it!
                     singleRegister(
                        this.getServer(),
                        target,
                        this.listener,
                        mbeanInfo.getFilter(),
                        mbeanInfo.getHandback()
                     );
                  }
               }
               catch (Exception e) 
               {
                  // catch exceptions from apply()
                  // shouldn't happen
                  log.warn("Caught exception from ObjectName.apply("
                         + target + ")", e);
               }
            }
         }
         else 
         {
            log.warn("Got unknown notification type from MBeanServerDelegate: "
                   + type);
         }
      }
      else // delegate to subclass
         handleNotification2(notification, handback);
   }

   /**
    * Override to add notification handling!
   **/
   public void handleNotification2(Notification notification, Object handback)
   {
      // empty!
   }
   
   // Private Methods -----------------------------------------------

   /**
    * Initialises myself
   **/
   private void init()
   {
      // just pickup a unique object
      this.myHandback = new Integer(Integer.MAX_VALUE);
      
      // allow only registration events
      this.myFilter = new NotificationFilterSupport();
      this.myFilter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
   }

   /**
    * Subscribes for notifications to a single MBean
   **/
   private void singleRegister(
      MBeanServer server, ObjectName target, ObjectName listener,
      NotificationFilter filter, Object handback)
   {
      try
      {
         server.addNotificationListener(target, listener, filter, handback);
               
         logSubscription(target, listener, handback, filter);
      }
      catch (InstanceNotFoundException e) 
      {
         // ignore - mbean might not be registered
         log.debug("Could not subscribe to: '" + target
                    + "', target or listener MBean not registered");
      }
      catch (RuntimeException e) 
      {
         log.warn("Failed to subscribe to: '" + target 
                + "', maybe not a notification broadcaster or: '" + listener
                + "', maybe not a notification listener");
      }      
   }

   /**
    * Unsubscribes for notifications from a single MBean 
   **/
   private void singleUnregister(
      MBeanServer server, ObjectName target, ObjectName listener,
      NotificationFilter filter, Object handback)
   {
      try
      {
         // remove the matching subscription
         server.removeNotificationListener(target, listener, filter, handback);
         
         log.debug("Unsubscribed from: '" + target + "'");
      }
      catch (InstanceNotFoundException e) 
      {
         // ignore - target mbean not present
         log.debug("Could not unsubscribe from non-existent: '"
                    + target + "'");
      }
      catch (ListenerNotFoundException e) 
      {
         // May happen if target is not a notification broadcaster
         // and so we hadn't registered in the first place
         log.debug("Could not unsubscribe from: '" + target + "'");
      }
      catch (RuntimeException e) 
      {
         // whatever
         log.debug("Could not unsubscribe from: '" + target + "'");
      }          
   }
   
   /**
    * Performs the notification subscriptions
   **/
   private void bulkRegister()
   {
      // iterate over the subscription specification
      Iterator i = this.sublist.iterator();
      
      // find out my server
      MBeanServer server = this.getServer();
      
      while (i.hasNext()) 
      {
         SubscriptionInfo mbeanInfo = (SubscriptionInfo)i.next();
         
         ObjectName objectName = mbeanInfo.getObjectName();
         Object handback = mbeanInfo.getHandback();
         NotificationFilter filter = mbeanInfo.getFilter();
         
         if (objectName.isPattern()) 
         {
            Set mset = server.queryNames(objectName, null);
            
            log.debug("ObjectName: '" + objectName + "' matched " + mset.size() + " MBean(s)");
               
            Iterator j = mset.iterator();
            while (j.hasNext())
               singleRegister(server, (ObjectName)j.next(), this.listener,
                              filter, handback);
         }
         else
            singleRegister(server, objectName, this.listener, filter, handback);
      }
   }
   
   /**
    * Performs bulk unregistration
   **/
   private void bulkUnregister()
   {
      // iterate over the subscription specification
      Iterator i = this.sublist.iterator();
      
      // find out my server
      MBeanServer server = this.getServer();
      
      while (i.hasNext()) 
      {
         SubscriptionInfo mbeanInfo = (SubscriptionInfo)i.next();
         
         ObjectName objectName = mbeanInfo.getObjectName();
         Object handback = mbeanInfo.getHandback();
         NotificationFilter filter = mbeanInfo.getFilter();
         
         if (objectName.isPattern()) 
         {
            Set mset = server.queryNames(objectName, null);
            
            log.debug("ObjectName: '" + objectName + "' matched " + mset.size() + " MBean(s)");
               
            Iterator j = mset.iterator();
            while (j.hasNext())
               singleUnregister(server, (ObjectName)j.next(), this.listener,
                                filter, handback);
         }
         else
            singleUnregister(server, objectName, this.listener, filter, handback);
      }      
   }
   
   /**
    * Logs subscription info
   **/
   private void logSubscription(
      ObjectName objectName, ObjectName listener,
      Object handback, NotificationFilter filter)
   {
      StringBuffer sbuf = new StringBuffer(100);
         
      sbuf.append("Subscribed to: { objectName='").append(objectName);
      sbuf.append("', listener='").append(listener);
      sbuf.append("', handback=").append(handback);
      sbuf.append(", filter=");
      sbuf.append(filter == null ? null : filter.toString());
      sbuf.append(" }");
         
      log.debug(sbuf.toString());
   }
   
   /**
    * Encapsulte the factory and filter creation logic
    */
   private NotificationFilter createNotificationFilter(String factoryClass, Element filterConfig)
      throws Exception
   {
      NotificationFilterFactory factory;
      try
      {
         // try to load the factory Class
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(factoryClass);
         factory = (NotificationFilterFactory)clazz.newInstance();
      }
      catch (Exception e) // ClassNotFoundException, IllegalAccessException, InstantiationException
      {
         // factory class not found. Make a second try using
         // the 'org.jboss.system.filterfactory.' package prefix
         // for the "standard" filter factories provided with jboss.
         // If that fails, too, rethrow the original exception.
         try
         {
            factoryClass = "org.jboss.system.filterfactory." + factoryClass;
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(factoryClass);
            factory = (NotificationFilterFactory)clazz.newInstance();
         }
         catch (Exception inner)
         {
            throw e;
         }
      }
      // delegate the filter creation/configuration to the factory
      return factory.createNotificationFilter(filterConfig);
   }
   
   /**
    * Parses the XML subscription specification
   **/
   private ArrayList parseXMLSubscriptionSpec(Element root)
       throws Exception
   {
      ArrayList slist = new ArrayList();
      
      // parse level 0 - subscription-list
      if (!root.getNodeName().equals(SL_ROOT_ELEMENT))
      {
         throw new Exception("Expected '" + SL_ROOT_ELEMENT + "' element, "
                    + "got: " + "'" + root.getNodeName() + "'");
      }
      else
      {
         NodeList rootlist = root.getChildNodes();
            
         for (int i = 0; i < rootlist.getLength(); i++) 
         {
            // Parse level 1 - look for mbeans
            Node mbean = rootlist.item(i);
                
            if (mbean.getNodeName().equals(SL_MBEAN_ELEMENT)) 
            {
               // mbean found look for name & handback attrs
               String name = null;
               
               if (((Element)mbean).hasAttribute(SL_MBEAN_NAME_ATTRIBUTE))
               {
                  name = ((Element)mbean).getAttribute(SL_MBEAN_NAME_ATTRIBUTE);
               }
               else
               {
                  throw new Exception("'" + SL_MBEAN_ELEMENT + "' element must have a '" 
                                    + SL_MBEAN_NAME_ATTRIBUTE + "' attribute");
               }

               String handback = null;
               if (((Element)mbean).hasAttribute(SL_MBEAN_HANDBACK_ATTRIBUTE))
               {
                  handback = ((Element)mbean).getAttribute(SL_MBEAN_HANDBACK_ATTRIBUTE);
               }
               
               // try to convert name to the correct data type
               // may throw MalformedObjectNameException
               ObjectName objectName = new ObjectName(name);
               
               // Parse level 2 - see if we have a filter for this subscription
               NotificationFilter filter = null;
               
               NodeList mbeanChildren = mbean.getChildNodes();

               // check for filter spec, as a single mbean child node               
               for (int j = 0; j < mbeanChildren.getLength(); j++)
               {
                  Node mbeanChildNode = mbeanChildren.item(j);
                  
                  // check if this is a 'filter' node
                  if (mbeanChildNode.getNodeName().equals(SL_FILTER_ELEMENT))
                  {
                     // look for the 'factory' attribute
                     String factory = null;
                     if (((Element)mbeanChildNode).hasAttribute(SL_FILTER_FACTORY_ATTRIBUTE))
                     {
                        factory = ((Element)mbeanChildNode).getAttribute(SL_FILTER_FACTORY_ATTRIBUTE);
                        
                        // instantiate the factory and request the filter
                        filter = createNotificationFilter(factory, (Element)mbeanChildNode);
                        break;
                     }
                     else
                     {
                        throw new Exception("'" + SL_FILTER_ELEMENT + "' element must have a '"
                              + SL_FILTER_FACTORY_ATTRIBUTE + "' attribute");
                     }
                  }
               }
               
               if (filter == null)
               {
                  // if no filter has been set check for old-style
                  // <notification type="..."/> construct that results
                  // in a fixed NotificationFilterSupport filter

                  // need to find out all notification types (if any)
                  // in order to create the Notification filter
                  ArrayList tmplist = new ArrayList(mbeanChildren.getLength());
               
                  for (int j = 0; j < mbeanChildren.getLength(); j++) 
                  {
                     Node mbeanChildNode = mbeanChildren.item(j);
                  
                     // check if this is a 'notification' element
                     if (mbeanChildNode.getNodeName().equals(SL_NOTIFICATION_ELEMENT)) 
                     {
                        // look for 'type' attribute
                        String type = null;
                        if (((Element)mbeanChildNode).hasAttribute(SL_NOTIFICATION_TYPE_ATTRIBUTE)) 
                        {
                           type = ((Element)mbeanChildNode).getAttribute(SL_NOTIFICATION_TYPE_ATTRIBUTE);
                           tmplist.add(type);
                        }
                        else
                        {
                           throw new Exception("'" + SL_NOTIFICATION_ELEMENT + "' element must have a '"
                                 + SL_NOTIFICATION_TYPE_ATTRIBUTE + "' attribute");
                        }
                     }
                  }
                  // create the filter (if needed)
                  if (tmplist.size() > 0) 
                  {
                     NotificationFilterSupport sfilter = new NotificationFilterSupport();
                     for (int j = 0; j < tmplist.size(); j++)
                     {
                        sfilter.enableType((String)tmplist.get(j));
                     }
                     filter = sfilter;
                  }                  
               }
               slist.add(new SubscriptionInfo(objectName, handback, filter));
            }
         }
      }
      return slist;
   }
    
   // Inner Class ---------------------------------------------------
   
   /**
    * Inner data holder class to store the parsed subscription specification.
   **/
   public static final class SubscriptionInfo
   {
      // Private Data -----------------------------------------------
      
      /** MBean notification broadcaster or pattern */
      private ObjectName objectName;
      
      /** Optional handback object to identify a subscription */
      private Object handback;
      
      /** Arbitrary NotificationFilter */
      private NotificationFilter filter;
      
      // Constructor ------------------------------------------------
      
      /**
       * Simple CTOR
      **/
      public SubscriptionInfo(ObjectName objectName, Object handback, NotificationFilter filter)
      {
         this.objectName = objectName;
         this.handback = handback;
         this.filter = filter;
      }
      
      // Accessors --------------------------------------------------
      /**
       * Gets objectname
      **/
      public ObjectName getObjectName()
      {
         return this.objectName;
      }
      
      /**
       * Gets handback object
      **/
      public Object getHandback()
      {
         return this.handback;
      }
      
      /**
       * Gets notification filter
      **/
      public NotificationFilter getFilter()
      {
         return this.filter;
      }
      
      /**
       * Pretty prints
      **/
      public String toString()
      {
         StringBuffer sbuf = new StringBuffer(100);
            
         sbuf.append("SubscriptionInfo { objectName='").append(this.objectName);
         sbuf.append("', handback=").append(this.handback);
         sbuf.append(", filter=");
         sbuf.append(this.filter == null ? null : this.filter.toString());
         
         sbuf.append(" }");
         
         return sbuf.toString();
      }
   }
}    
