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
package org.jboss.test.mx.remoting.pingpong;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.AttributeChangeNotification;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.remoting.MBeanLocator;
import org.jboss.mx.remoting.tracker.MBeanTracker;
import org.jboss.mx.remoting.tracker.MBeanTrackerAction;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.ident.Identity;

/**
 * PingPong is a simple test mbean that will call ping on other peers
 * in the JBoss remoting network and will fire a notification and
 * an attribute change notification when ping is invoked back to listeners.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class PingPong implements PingPongMBean, MBeanTrackerAction, MBeanRegistration
{
    private static final transient Logger log = Logger.getLogger(PingPong.class.getName());
    private MBeanTracker tracker;
    private Timer timer=new Timer(false);
    private Map friends=new HashMap();
    private MBeanServer server;
    private ObjectName objectName;
    private JBossNotificationBroadcasterSupport broadcaster=new JBossNotificationBroadcasterSupport();

    public Object ping (Object pong)
    {
        Notification notification = new Notification("pong",objectName,System.currentTimeMillis());
        broadcaster.sendNotification(notification);
            log.debug("ping called: "+pong+", sending notification: "+notification+" for objectName: "+objectName);
        Notification stateChange = new AttributeChangeNotification(objectName,System.currentTimeMillis(),System.currentTimeMillis(),"State Changed","State",Integer.class.getName(),new Integer(1),new Integer(2));
        broadcaster.sendNotification(stateChange);
        return pong;
    }

    /**
     * Add a listener to an MBean.
     *
     * @param   listener    implementation of the listener object
     * @param   filter      implementation of the filter object or <tt>null</tt>
     *                      if no filtering is required
     * @param   handback    A handback object associated with each notification
     *                      sent by this notification broadcaster.
     *
     * @throws  IllegalArgumentException if listener is <tt>null</tt>
     */
    public void addNotificationListener ( NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback )
            throws IllegalArgumentException
    {
            log.debug("addNotificationListener - listener: "+listener);
        broadcaster.addNotificationListener(listener,filter,handback);
    }

    /**
     * Removes a listener from an MBean.
     *
     * @param   listener the listener object to remove
     *
     * @throws ListenerNotFoundException if the listener was not found
     */
    public void removeNotificationListener ( NotificationListener listener )
            throws ListenerNotFoundException
    {
            log.debug("removeNotificationListener - listener: "+listener);
        broadcaster.removeNotificationListener(listener);
    }

    /**
     * Returns the notification metadata associated with the MBean.
     *
     * @see  MBeanNotificationInfo
     *
     * @return  MBean's notification metadata
     */
    public MBeanNotificationInfo[] getNotificationInfo ()
    {
        return new MBeanNotificationInfo[0];
    }

    /**
     * This method is called by the MBeanServer after deregistration takes
     * place.
     */
    public void postDeregister ()
    {
    }

    /**
     * This method is called by the MBeanServer after registration takes
     * place or when registration fails.
     *
     * @param registrationDone the MBeanServer passes true when the
     * MBean was registered, false otherwise.
     */
    public void postRegister (Boolean registrationDone)
    {
    }

    /**
     * This method is called by the MBeanServer before deregistration takes
     * place.<p>
     *
     * The MBean can throw an exception, this will stop the deregistration.
     * The exception is forwarded to the invoker wrapped in
     * an MBeanRegistrationException.
     */
    public void preDeregister ()
            throws Exception
    {
        stop();
    }

    /**
     * This method is called by the MBeanServer before registration takes
     * place. The MBean is passed a reference of the MBeanServer it is
     * about to be registered with. The MBean must return the ObjectName it
     * will be registered with. The MBeanServer can pass a suggested object
     * depending upon how the MBean is registered.<p>
     *
     * The MBean can stop the registration by throwing an exception.The
     * exception is forwarded to the invoker wrapped in an
     * MBeanRegistrationException.
     *
     * @param MBeanServer the MBeanServer the MBean is about to be
     * registered with.
     * @param ObjectName the suggested ObjectName supplied by the
     * MBeanServer.
     * @return the actual ObjectName to register this MBean with.
     * @exception Exception for any error, the MBean is not registered.
     */
    public ObjectName preRegister (MBeanServer server, ObjectName name)
            throws Exception
    {
        this.server = server;
        this.objectName = name;
        start();
        return name;
    }


    /**
     * Describe <code>start</code> method here.
     *
     * @jmx.managed-operation description="Second lifecycle method called after mbeans attributes are set.  During this method declared mbean dependencies are available and may be used.  After completion the mbean should be completely usable."
     *                        impact="ACTION"
     */
    public void start () throws Exception
    {
        tracker = new MBeanTracker(server,new Class[]{PingPongMBean.class},null,false,null,true,this);
        timer.scheduleAtFixedRate(new Pinger(),5000L,5000L);
    }

    /**
     * Describe <code>stop</code> method here.
     *
     * @jmx.managed-operation description="First shutdown lifecycle method.  This method should undo the effects of start"
     *                        impact="ACTION"
     */
    public void stop ()
    {
        if (tracker!=null)
        {
            tracker.destroy();
            tracker=null;
        }
    }

    /**
     * called when a mbean notification is fired
     *
     * @param locator
     * @param notification
     * @param handback
     */
    public void mbeanNotification (MBeanLocator locator, Notification notification, Object handback)
    {
        log.info("received notification from: "+locator+", notification: "+notification);
    }

    /**
     * called when an MBean is registered with the MBeanServer
     *
     * @param locator
     */
    public void mbeanRegistered (MBeanLocator locator)
    {
        if (Identity.get(server).equals(locator.getIdentity()))
        {
            // ignore myself
            return;
        }
        log.info("found a new friend to play ping pong with: "+locator);
        PingPongMBean friend=null;
        try
        {
            friend=(PingPongMBean)locator.narrow(PingPongMBean.class);
        }
        catch (Exception e)
        {
            log.error("error casting my friend to PingPongMBean - his locator is: "+locator,e);
            return;
        }
        synchronized(friends)
        {
            friends.put(locator,friend);
        }
    }

    /**
     * called when the mbean state changes.  Note: this method will only be called on MBeans that have a
     * <tt>State</tt> attribute and where state change attribute notifications are fired
     *
     * @param locator
     * @param oldState
     * @param newState
     */
    public void mbeanStateChanged (MBeanLocator locator, int oldState, int newState)
    {
        log.info("one of my partners ("+locator+") changed its state from: "+oldState+" to: "+newState);
    }

    /**
     * called when an MBean is unregistered with the MBeanServer
     *
     * @param locator
     */
    public void mbeanUnregistered (MBeanLocator locator)
    {
        log.info("I lost a friend, "+locator);
        friends.remove(locator);
    }

    final class Pinger extends TimerTask
    {
        /**
         * The action to be performed by this timer task.
         */
        public void run ()
        {
            Map copy = null;
            synchronized (friends)
            {
                copy = new HashMap(friends);
            }
            if (copy.isEmpty())
            {
                log.info("I don't have any friends on the network, how boring...");
                return;
            }
            Iterator iter = copy.keySet().iterator();
            Integer myhash=new Integer(hashCode());
            while(iter.hasNext())
            {
                MBeanLocator l=(MBeanLocator)iter.next();
                try
                {
                    Object obj = l.getServerLocator().getMBeanServer().invoke(l.getObjectName(),"ping",new Object[]{myhash},new String[]{Object.class.getName()});
                    //PingPongMBean friend=(PingPongMBean)copy.get(l);
                    log.info("pinging my friend at: "+l+", with: "+myhash);
                    // send my friend a ping
                    //Object obj = friend.ping(myhash);
                    if (!obj.equals(myhash))
                    {
                        log.warn("My friend passed me back something I don't understand?! I passed him: "+myhash+", I received: "+obj);
                    }
                }
                catch (Throwable ex)
                {
                    if (ex instanceof MBeanException)
                    {
                        MBeanException mbe=(MBeanException)ex;
                        if (mbe.getTargetException() instanceof ConnectionFailedException)
                        {
                            log.info("my friend died during a ping, "+l);
                            return;
                        }
                    }
                    log.warn("My friend doesn't like me, he gave me an exception back",ex);
                }
            }
            copy=null;
        }
    }
}

