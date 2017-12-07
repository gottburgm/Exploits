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
package org.jboss.management.j2ee.cluster;

/**
 * MBean interface.
 */
public interface HAManagementServiceMBean extends org.jboss.system.ServiceMBean
{

   java.lang.Object getAttribute(javax.management.ObjectName pName, java.lang.String pAttribute)
         throws javax.management.MBeanException, javax.management.AttributeNotFoundException,
         javax.management.InstanceNotFoundException, javax.management.ReflectionException, java.rmi.RemoteException;

   javax.management.AttributeList getAttributes(javax.management.ObjectName pName, java.lang.String[] pAttributes)
         throws javax.management.InstanceNotFoundException, javax.management.ReflectionException,
         java.rmi.RemoteException;

   java.lang.String getDefaultDomain() throws java.rmi.RemoteException;

   java.lang.Integer getMBeanCount() throws java.rmi.RemoteException;

   javax.management.MBeanInfo getMBeanInfo(javax.management.ObjectName pName)
         throws javax.management.IntrospectionException, javax.management.InstanceNotFoundException,
         javax.management.ReflectionException, java.rmi.RemoteException;

   javax.management.j2ee.ListenerRegistration getListenerRegistry() throws java.rmi.RemoteException;

   java.lang.Object invoke(javax.management.ObjectName pName, java.lang.String pOperationName,
         java.lang.Object[] pParams, java.lang.String[] pSignature) throws javax.management.InstanceNotFoundException,
         javax.management.MBeanException, javax.management.ReflectionException, java.rmi.RemoteException;

   boolean isRegistered(javax.management.ObjectName pName) throws java.rmi.RemoteException;

   java.util.Set queryNames(javax.management.ObjectName pName, javax.management.QueryExp pQuery)
         throws java.rmi.RemoteException;

   void setAttribute(javax.management.ObjectName pName, javax.management.Attribute pAttribute)
         throws javax.management.AttributeNotFoundException, javax.management.InstanceNotFoundException,
         javax.management.InvalidAttributeValueException, javax.management.MBeanException,
         javax.management.ReflectionException, java.rmi.RemoteException;

   javax.management.AttributeList setAttributes(javax.management.ObjectName pName,
         javax.management.AttributeList pAttributes) throws javax.management.InstanceNotFoundException,
         javax.management.ReflectionException, java.rmi.RemoteException;

   javax.management.ObjectInstance createMBean(java.lang.String pClass, javax.management.ObjectName pName,
         java.lang.Object[] pParameters, java.lang.String[] pSignature)
         throws javax.management.InstanceAlreadyExistsException, javax.management.MBeanException,
         javax.management.MBeanRegistrationException, javax.management.NotCompliantMBeanException,
         javax.management.ReflectionException, java.rmi.RemoteException;

   void unregisterMBean(javax.management.ObjectName pName) throws javax.management.InstanceNotFoundException,
         javax.management.MBeanRegistrationException, java.rmi.RemoteException;

   void addNotificationListener(javax.management.ObjectName pBroadcaster, javax.management.ObjectName pListener,
         javax.management.NotificationFilter pFilter, java.lang.Object pHandback)
         throws javax.management.InstanceNotFoundException, java.rmi.RemoteException;

   void removeNotificationListener(javax.management.ObjectName pBroadcaster, javax.management.ObjectName pListener)
         throws javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException,
         java.rmi.RemoteException;

}
