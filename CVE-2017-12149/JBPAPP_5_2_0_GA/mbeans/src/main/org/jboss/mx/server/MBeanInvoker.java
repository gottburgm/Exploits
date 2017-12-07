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
package org.jboss.mx.server;

import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.NotificationEmitter;
import javax.management.ObjectName;

/**
 * This interface represents an invoker for an MBean. An invoker is registered
 * to the MBean server to represent a user MBean. The invoker itself implements
 * a <tt>DynamicMBean</tt> interface and therefore receives all attribute
 * accessor and operation invocations targeted at the user MBean.  <p>
 *
 * Invoker implementations may handle the incoming invocations in different
 * ways. The default implementation in <tt>AbstractMBeanInvoker</tt> constructs
 * an <tt>Invocation</tt> object for each invocation and dispatches it through
 * a set of <tt>Interceptor</tt> instances before the invocation is dispatched
 * on the user MBean.  <p>
 * 
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81026 $   
 */
public interface MBeanInvoker
   extends DynamicMBean, MBeanRegistration, NotificationEmitter,
           Suspendable, Interceptable
{
   MBeanInfo getMetaData();
   
   Object getResource();
   
   void setResource(Object resource);

   ObjectName getObjectName();

   void updateAttributeInfo(Descriptor attrDesc) throws MBeanException;
}
