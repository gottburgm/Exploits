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
package org.jboss.mx.mxbean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * MXBeanSupport.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanSupport implements DynamicMBean, MBeanRegistration, NotificationEmitter
{
   /** No notifications */
   private static final MBeanNotificationInfo[] NO_NOTIFICATIONS = new MBeanNotificationInfo[0];
   
   /** The delegate */
   private DynamicMBean delegate;
   
   /** The mbean registration delegate */
   private MBeanRegistration registration;
   
   /** The emitter delegate */
   private NotificationEmitter emitter;
   
   /**
    * Create a new MXBeanSupport.
    */
   protected MXBeanSupport()
   {
      init(MXBeanUtils.createMXBean(this, null));
   }

   /**
    * Create a new MXBeanSupport.
    * 
    * @param mxbeanInterface the interface
    */
   protected MXBeanSupport(Class<?> mxbeanInterface)
   {
      init(MXBeanUtils.createMXBean(this, mxbeanInterface));
   }
   
   public MBeanInfo getMBeanInfo()
   {
      return delegate.getMBeanInfo();
   }

   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      return delegate.getAttribute(attribute);
   }

   public AttributeList getAttributes(String[] attributes)
   {
      return delegate.getAttributes(attributes);
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      delegate.setAttribute(attribute);
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      return delegate.setAttributes(attributes);
   }

   public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
   {
      return delegate.invoke(actionName, params, signature);
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      return registration.preRegister(server, name);
   }

   public void postRegister(Boolean registrationDone)
   {
      registration.postDeregister();
   }

   public void preDeregister() throws Exception
   {
      registration.preDeregister();
   }

   public void postDeregister()
   {
      registration.postDeregister();
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return NO_NOTIFICATIONS;
   }

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
   {
      emitter.addNotificationListener(listener, filter, handback);
      
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      emitter.removeNotificationListener(listener);
   }
   
   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      emitter.removeNotificationListener(listener, filter, handback);
   }

   /**
    * Initialise the delegates
    * 
    * @param delegate the delegate
    */
   private void init(DynamicMBean delegate)
   {
      this.delegate = delegate;
      this.registration = (MBeanRegistration) delegate;
      this.emitter = (NotificationEmitter) delegate;
   }
}
