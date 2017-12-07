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
package org.jboss.mx.remoting.tracker;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import org.jboss.logging.Logger;
import org.jboss.mx.remoting.JMXUtil;
import org.jboss.mx.remoting.MBeanLocator;
import org.jboss.mx.remoting.MBeanServerLocator;
import org.jboss.mx.remoting.event.ClassQueryExp;
import org.jboss.mx.remoting.event.CompositeEventFilter;
import org.jboss.mx.remoting.event.CompositeQueryExp;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.network.NetworkInstance;
import org.jboss.remoting.network.NetworkNotification;
import org.jboss.remoting.network.NetworkRegistryFinder;
import org.jboss.remoting.network.NetworkRegistryMBean;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * MBeanTracker is a utility class that will track MBeans on behalf of a user object.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanTracker implements NotificationListener
{
   private static final boolean logEvents = Boolean.getBoolean("jboss.mx.tracker.debug");
   private static final transient Logger log = Logger.getLogger(MBeanTracker.class.getName());
   private final QueryExp query;
   private final boolean localOnly;
   private final boolean wantNotifications;
   private final NotificationFilter filter;
   private final SynchronizedInt count = new SynchronizedInt(0);
   private final Map mbeans = new HashMap();
   private final String classes[];
   private final List actions = new ArrayList(1);
   private final ObjectName networkRegistry;
   private final MBeanServer myserver;

   public MBeanTracker(MBeanServer myserver, Class cl[], QueryExp query, boolean localOnly, MBeanTrackerAction action)
         throws Exception
   {
      this(myserver, cl, query, localOnly, null, false, new MBeanTrackerAction[]{action});
   }

   public MBeanTracker(MBeanServer myserver, Class cl[], QueryExp query, boolean localOnly, MBeanTrackerAction actions[])
         throws Exception
   {
      this(myserver, cl, query, localOnly, null, false, actions);
   }

   public MBeanTracker(MBeanServer myserver, Class cl[], boolean localOnly, MBeanTrackerAction action)
         throws Exception
   {
      this(myserver, cl, null, localOnly, null, false, new MBeanTrackerAction[]{action});
   }

   public MBeanTracker(MBeanServer myserver, Class cl[], boolean localOnly, MBeanTrackerAction actions[])
         throws Exception
   {
      this(myserver, cl, null, localOnly, null, false, actions);
   }

   public MBeanTracker(MBeanServer myserver, Class cl[], QueryExp query, boolean localOnly, NotificationFilter filter, boolean wantNotifications, MBeanTrackerAction action)
         throws Exception
   {
      this(myserver, cl, query, localOnly, filter, wantNotifications, new MBeanTrackerAction[]{action});
   }

   public MBeanTracker(MBeanServer myserver, Class cl[], QueryExp query, boolean localOnly, NotificationFilter filter, boolean wantNotifications)
         throws Exception
   {
      this(myserver, cl, query, localOnly, filter, wantNotifications, (MBeanTrackerAction[]) null);
   }

   /**
    * create a tracker
    *
    * @param myserver          local mbean server
    * @param cl                array of classes that mbeans implement that you want to track, or null to not look at class interfaces
    * @param query             query expression to apply when selecting mbeans or null to not use a query expression
    * @param localOnly         true to only search the local mbeanserver, false to search the entire network of mbeans servers
    * @param filter            filter to apply for receiving notifications or null to apply no filter
    * @param wantNotifications if true, will also track notifications by the mbeans being tracked
    * @param actions           array of actions to automatically register as listeners, or null if none
    * @throws Exception raised on exception
    */
   public MBeanTracker(MBeanServer myserver, Class cl[], QueryExp query, boolean localOnly, NotificationFilter filter, boolean wantNotifications, MBeanTrackerAction actions[])
         throws Exception
   {
      this.localOnly = localOnly;
      this.wantNotifications = wantNotifications;
      this.filter = filter;
      this.myserver = myserver;

      if(log.isTraceEnabled())
      {
         StringBuffer buf = new StringBuffer("creating an MBeanTracker with the following parameters:\n");
         buf.append("==========================================\n");
         buf.append("MBeanServer:   " + myserver + "\n");
         if(cl == null)
         {
            buf.append("classes: none\n");
         }
         else
         {
            for(int c = 0; c < cl.length; c++)
            {
               buf.append("classes[" + c + "] " + cl[c].getName() + "\n");
            }
         }
         log.debug("QueryExp:       " + query + "\n");
         log.debug("localOnly:      " + localOnly + "\n");
         log.debug("filter:         " + filter + "\n");
         log.debug("notifications:  " + wantNotifications + "\n");

         if(actions == null)
         {
            log.debug("actions: none\n");
         }
         else
         {
            for(int c = 0; c < actions.length; c++)
            {
               log.debug("actions[" + c + "]: " + actions[c] + "\n");
            }
         }
         buf.append("==========================================\n");
         log.debug(buf.toString());
      }

      // add actions
      if(actions != null)
      {
         for(int c = 0; c < actions.length; c++)
         {
            if(actions[c] != null)
            {
               addActionListener(actions[c]);
            }
         }
      }
      if(cl != null)
      {
         this.classes = new String[cl.length];
         for(int c = 0; c < cl.length; c++)
         {
            classes[c] = cl[c].getName();
         }
      }
      else
      {
         this.classes = null;
      }
      if(query == null && cl != null)
      {
         this.query = new ClassQueryExp(cl);
      }
      else
      {
         if(cl != null)
         {
            this.query = new CompositeQueryExp(new QueryExp[]{new ClassQueryExp(cl, ClassQueryExp.OR), query});
         }
         else
         {
            this.query = query;
         }
      }
      // add ourself as a listener to the NetworkRegistry
      networkRegistry = NetworkRegistryFinder.find(myserver);
      if(networkRegistry == null)
      {
         throw new Exception("NetworkRegistryMBean not found - MBeanTracker has a dependency on this MBean");
      }

      foundMBeanServer(new MBeanServerLocator(Identity.get(myserver)));

      if(this.localOnly == false)
      {
         // add ourself as a listener for network changes
         myserver.addNotificationListener(networkRegistry, this, null, null);

         // find any instances we already have registered
         NetworkInstance instances[] = (NetworkInstance[]) myserver.getAttribute(networkRegistry, "Servers");

         if(instances != null)
         {
            for(int c = 0; c < instances.length; c++)
            {
               foundMBeanServer(new MBeanServerLocator(instances[c].getIdentity()));
            }
         }
      }
   }

   /**
    * add a action listener.  this method will automatically call register to your action on
    * all the mbeans that are contained within it before this method returns.
    *
    * @param action
    */
   public void addActionListener(MBeanTrackerAction action)
   {
      addActionListener(action, true);
   }

   /**
    * add a action listener.  this method will automatically call register to your action on
    * all the mbeans that are contained within it before this method returns.
    *
    * @param action
    */
   public void addActionListener(MBeanTrackerAction action, boolean autoinitialregister)
   {
      if(log.isTraceEnabled())
      {
         log.debug("adding action: " + action + ", autoinitialregister:" + autoinitialregister);
      }

      synchronized(actions)
      {
         actions.add(action);
      }
      if(autoinitialregister)
      {
         Set set = getMBeans();
         Iterator iter = set.iterator();
         while(iter.hasNext())
         {
            MBeanLocator locator = (MBeanLocator) iter.next();
            fireRegister(locator);
         }
      }
   }

   /**
    * remove a action listener
    *
    * @param action
    */
   public void removeActionListener(MBeanTrackerAction action)
   {
      if(log.isTraceEnabled())
      {
         log.debug("removing action: " + action);
      }

      Iterator iter = actions();
      while(iter.hasNext())
      {
         MBeanTrackerAction _action = (MBeanTrackerAction) iter.next();
         if(_action.equals(action))
         {
            iter.remove();
         }
      }
   }

   private NotificationFilter createFilterForServer(String id)
   {
      NotificationFilter serverfilter = null;
      NotificationFilter nfilter = new MBeanTrackerFilter(id, classes, wantNotifications);
      if(filter == null)
      {
         serverfilter = nfilter;
      }
      else
      {
         serverfilter = new CompositeEventFilter(new NotificationFilter[]{nfilter, filter});
      }
      return serverfilter;
   }

   protected void finalize() throws Throwable
   {
      destroy();
      super.finalize();
   }

   /**
    * called to stop tracking and clean up internally held resources
    */
   public void destroy()
   {
      if(log.isTraceEnabled())
      {
         log.debug("destroy");
      }
      try
      {
         myserver.removeNotificationListener(networkRegistry, this);
      }
      catch(Throwable ex)
      {
      }
   }

   /**
    * returns true if no mbeans are found that are being tracked
    *
    * @return
    */
   public final boolean isEmpty()
   {
      return count() <= 0;
   }

   /**
    * return the number of mbeans being tracked
    *
    * @return
    */
   public final int count()
   {
      return count.get();
   }

   /**
    * return a copy of the internal mbeans being tracked
    *
    * @return
    */
   public final Set getMBeans()
   {
      Set set = new HashSet();
      synchronized(mbeans)
      {
         Iterator iter = mbeans.values().iterator();
         while(iter.hasNext())
         {
            Set beans = (Set) iter.next();
            set.addAll(beans);
         }
      }
      return set;
   }

   /**
    * return an iterator to a copy of the internal mbeans being tracked
    *
    * @return
    */
   public final Iterator iterator()
   {
      return getMBeans().iterator();
   }


   private void tryAddListener(MBeanServerLocator server, ObjectName mbean)
   {
      try
      {
         if(server.getMBeanServer().isInstanceOf(mbean, NotificationBroadcaster.class.getName()) &&
            server.getMBeanServer().isInstanceOf(mbean, NetworkRegistryMBean.class.getName()) == false)
         {
            server.getMBeanServer().addNotificationListener(mbean, this, createFilterForServer(server.getServerId()), server);
            if(log.isTraceEnabled())
            {
               log.debug("added notification listener to: " + mbean + " on server: " + server);
            }
         }
      }
      catch(Throwable e)
      {
         log.error("Error registering listener for server:" + server + " and mbean:" + mbean, e);
      }
   }

   /**
    * try and remove a listener
    *
    * @param server
    * @param mbean
    */
   private void tryRemoveListener(MBeanServerLocator server, ObjectName mbean)
   {
      try
      {
         if(server.getMBeanServer() == null)
         {
            return;
         }
         if(server.getMBeanServer().isInstanceOf(mbean, NotificationBroadcaster.class.getName()) &&
            server.getMBeanServer().isInstanceOf(mbean, NetworkRegistryMBean.class.getName()) == false)
         {
            server.getMBeanServer().removeNotificationListener(mbean, this);
            if(log.isTraceEnabled())
            {
               log.debug("removed notification listener to: " + mbean + " on server: " + server);
            }
         }
      }
      catch(javax.management.InstanceNotFoundException nf)
      {
         //this is OK, since it means we're trying to remove a listener from an
         // unregsitered mbean - which in most cases it is
      }
      catch(ConnectionFailedException cnf)
      {
         // this is OK
      }
      catch(Exception e)
      {
         if(e instanceof UndeclaredThrowableException)
         {
            UndeclaredThrowableException ut = (UndeclaredThrowableException) e;
            if(ut.getUndeclaredThrowable() instanceof ReflectionException)
            {
               ReflectionException re = (ReflectionException) ut.getUndeclaredThrowable();
               if(re.getTargetException() instanceof InstanceNotFoundException ||
                  re.getTargetException() instanceof ConnectionFailedException)
               {
                  // these are OK
                  return;
               }
            }
            else if(ut.getUndeclaredThrowable() instanceof MBeanException)
            {
               MBeanException mbe = (MBeanException) ut.getUndeclaredThrowable();
               if(mbe.getTargetException() instanceof ConnectionFailedException)
               {
                  // this is OK
                  return;
               }
            }
         }
         if(e instanceof MBeanException)
         {
            MBeanException mbe = (MBeanException) e;
            if(mbe.getTargetException() instanceof ConnectionFailedException)
            {
               // this is OK
               return;
            }
         }
         log.warn("Error removing listener for server:" + server + " and mbean:" + mbean, e);
      }
   }

   /**
    * called for each notification
    *
    * @param notification
    * @param o
    */
   public void handleNotification(Notification notification, Object o)
   {
      if(log.isTraceEnabled())
      {
         log.debug("tracker received notification=" + notification + " with handback=" + o);
      }
      try
      {
         if(notification instanceof MBeanServerNotification && JMXUtil.getMBeanServerObjectName().equals(notification.getSource()))
         {
            MBeanServerNotification n = (MBeanServerNotification) notification;
            String type = n.getType();
            ObjectName mbean = n.getMBeanName();
            if(type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
            {
               addMBean((MBeanServerLocator) o, mbean);
            }
            else
            {
               // unreg a specific MBean
               removeMBean((MBeanServerLocator) o, mbean);
            }
            return;
         }
         else if(notification instanceof NetworkNotification)
         {
            NetworkNotification nn = (NetworkNotification) notification;
            String type = nn.getType();
            if(type.equals(NetworkNotification.SERVER_ADDED))
            {
               // found a server
               Identity ident = nn.getIdentity();
               MBeanServerLocator l = new MBeanServerLocator(ident);
               foundMBeanServer(l);
            }
            else if(type.equals(NetworkNotification.SERVER_REMOVED))
            {
               // lost a server
               Identity ident = nn.getIdentity();
               MBeanServerLocator l = new MBeanServerLocator(ident);
               lostMBeanServer(l);
            }
            return;
         }
         else if(notification instanceof AttributeChangeNotification)
         {
            AttributeChangeNotification ch = (AttributeChangeNotification) notification;
            if(ch.getAttributeName().equals("State") && hasActions())
            {
               MBeanServerLocator server = (MBeanServerLocator) o;
               Object src = ch.getSource();
               if(src instanceof ObjectName)
               {
                  ObjectName obj = (ObjectName) src;
                  // indicate the state changed
                  fireStateChange(new MBeanLocator(server, obj), ((Integer) ch.getOldValue()).intValue(), ((Integer) ch.getNewValue()).intValue());
                  return;
               }
               else if(src instanceof MBeanLocator)
               {
                  fireNotification((MBeanLocator) src, notification, o);
                  return;
               }
            }
         }
         if(wantNotifications && hasActions())
         {
            // fire notification to listener
            MBeanServerLocator server = (MBeanServerLocator) o;
            if(server != null)
            {
               Object src = notification.getSource();
               if(src instanceof ObjectName)
               {
                  ObjectName obj = (ObjectName) src;
                  MBeanLocator locator = new MBeanLocator(server, obj);
                  fireNotification(locator, notification, o);
                  return;
               }
               else if(src instanceof MBeanLocator)
               {
                  fireNotification((MBeanLocator) src, notification, o);
                  return;
               }
               else
               {
                  log.debug("Unknown source type for notification: " + src);
               }
            }
         }
      }
      catch(Exception e)
      {
         log.warn("Error encountered receiving notification: " + notification, e);
      }
   }

   /**
    * returns true if we have any actions
    *
    * @return
    */
   private boolean hasActions()
   {
      synchronized(actions)
      {
         return actions.isEmpty() == false;
      }
   }

   /**
    * fire a notification to actions
    *
    * @param locator
    * @param n
    * @param o
    */
   protected void fireNotification(MBeanLocator locator, Notification n, Object o)
   {
      Iterator iter = actions();
      while(iter.hasNext())
      {
         MBeanTrackerAction action = (MBeanTrackerAction) iter.next();
         if(wantNotifications && log.isTraceEnabled())
         {
            log.debug("forwarding tracker notification: " + n + " to action: " + action + " for tracker: " + this);
         }
         action.mbeanNotification(locator, n, o);
      }
   }

   /**
    * fire a state changed event to actions
    *
    * @param locator
    * @param ov
    * @param nv
    */
   protected void fireStateChange(MBeanLocator locator, int ov, int nv)
   {
      Iterator iter = actions();
      while(iter.hasNext())
      {
         MBeanTrackerAction action = (MBeanTrackerAction) iter.next();
         if(wantNotifications && log.isTraceEnabled())
         {
            log.debug("forwarding tracker state change: " + nv + " [" + ov + "] to action: " + action + " for tracker: " + this);
         }
         action.mbeanStateChanged(locator, ov, nv);
      }
   }

   /**
    * return an Iterator to a unmodifiable Iterator so that you can avoid having to synchronize
    * on traversing the action list
    *
    * @return
    */
   private final Iterator actions()
   {
      synchronized(actions)
      {
         if(actions.isEmpty())
         {
            return Collections.EMPTY_LIST.iterator();
         }
         return new ArrayList(actions).iterator();
      }
   }

   /**
    * fire unregister event to listeners
    *
    * @param locator
    */
   protected void fireUnregister(MBeanLocator locator)
   {
      int c = 0;
      Iterator iter = actions();
      while(iter.hasNext())
      {
         MBeanTrackerAction action = (MBeanTrackerAction) iter.next();
         if(logEvents && log.isTraceEnabled())
         {
            log.debug("firing unregister to action [" + (++c) + "] => " + action + " for locator => " + locator);
         }
         action.mbeanUnregistered(locator);
      }
   }

   /**
    * fire register event to listeners
    *
    * @param locator
    */
   protected void fireRegister(MBeanLocator locator)
   {
      int c = 0;
      Iterator iter = actions();
      while(iter.hasNext())
      {
         MBeanTrackerAction action = (MBeanTrackerAction) iter.next();
         if(logEvents && log.isTraceEnabled())
         {
            log.debug("firing register to action [" + (++c) + "] => " + action + " for locator => " + locator);
         }
         action.mbeanRegistered(locator);
      }
   }

   /**
    * fired when an MBeanServer is found
    *
    * @param theserver
    */
   public void foundMBeanServer(MBeanServerLocator theserver)
   {
      synchronized(mbeans)
      {
         // already found him
         if(mbeans.containsKey(theserver))
         {
            return;
         }
         mbeans.put(theserver, new HashSet());
      }

      for(int c = 0; c < 3; c++)
      {
         try
         {
            theserver.getMBeanServer().addNotificationListener(JMXUtil.getMBeanServerObjectName(), this, createFilterForServer(theserver.getServerId()), theserver);
            Set beans = theserver.getMBeanServer().queryMBeans(new ObjectName("*:*"), query);
            if(beans.isEmpty() == false)
            {
               Iterator iter = beans.iterator();
               while(iter.hasNext())
               {
                  addMBean(theserver, ((ObjectInstance) iter.next()).getObjectName());
               }
            }
            else
            {
               if(log.isTraceEnabled())
               {
                  log.debug("Queried server: " + theserver + ", but found 0 mbeans matching query");
               }
            }

            break;
         }
         catch(ConnectionFailedException ce)
         {
            if(log.isTraceEnabled())
            {
               log.debug("while trying to add a listener and get info for: " + theserver + ", i lost it", ce);
            }
            if(c >= 3)
            {
               if(log.isTraceEnabled())
               {
                  log.debug("giving up on connection failed after " + c + " attempts... " + theserver);
               }
               // lost mbean server
               lostMBeanServer(theserver);
            }
         }
         catch(Exception ex)
         {
            log.warn("Exception adding mbeans from server: " + theserver, ex);
         }
      }
   }

   /**
    * add an mbean
    *
    * @param server
    * @param mbean
    */
   private void addMBean(MBeanServerLocator server, ObjectName mbean)
   {
      if(log.isTraceEnabled())
      {
         log.debug("addMBean called: " + server + ", mbean: " + mbean);
      }

      MBeanLocator locator = new MBeanLocator(server, mbean);

      boolean found = false;

      synchronized(mbeans)
      {
         Set set = (Set) mbeans.get(server);
         if(set != null)
         {
            if(set.add(locator))
            {
               count.increment();
               found = true;
            }
         }

      }

      if(!found)
      {
         return;
      }

      tryAddListener(server, mbean);

      if(hasActions())
      {
         fireRegister(locator);
      }
   }

   /**
    * called to remove an mbean
    *
    * @param server
    * @param mbean
    */
   private void removeMBean(MBeanServerLocator server, ObjectName mbean)
   {
      if(log.isTraceEnabled())
      {
         log.debug("removeMBean called: " + server + ", mbean: " + mbean);
      }

      MBeanLocator locator = new MBeanLocator(server, mbean);

      synchronized(mbeans)
      {
         Set set = (Set) mbeans.get(server);
         if(set != null)
         {
            if(set.remove(locator))
            {
               // only if we found the dude
               count.decrement();
            }
            else
            {
               // we didn't find him, just return
               return;
            }
         }
      }

      tryRemoveListener(server, mbean);

      if(hasActions())
      {
         fireUnregister(locator);
      }
   }

   /**
    * fired when we lose an MBeanServer
    *
    * @param server
    */
   public void lostMBeanServer(MBeanServerLocator server)
   {
      if(wantNotifications && log.isTraceEnabled())
      {
         log.debug("lostMBeanServer: " + server + " for tracker: " + this);
      }

      Collection list = null;

      synchronized(mbeans)
      {
         list = (Set) mbeans.remove(server);
      }
      if(list != null)
      {
         if(log.isTraceEnabled())
         {
            log.debug("lost mbean server = " + server + ", list = " + list);
         }
         Iterator iter = list.iterator();
         while(iter.hasNext())
         {
            MBeanLocator locator = (MBeanLocator) iter.next();
            removeMBean(server, locator.getObjectName());
         }
         list.clear();
         list = null;
      }
   }

}