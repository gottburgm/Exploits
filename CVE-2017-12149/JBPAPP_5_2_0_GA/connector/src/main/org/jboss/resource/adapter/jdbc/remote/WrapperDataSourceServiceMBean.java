/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jdbc.remote;

import javax.management.ObjectName;

import org.jboss.invocation.Invocation;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.connectionmanager.ConnectionFactoryBindingServiceMBean;

/**
 * MBean interface.
 */
public interface WrapperDataSourceServiceMBean extends ConnectionFactoryBindingServiceMBean
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=ConnectionFactoryBinding");

   /**
    * Get the transport
    * 
    * @return the invoker name
    */
   ObjectName getJMXInvokerName();

   /**
    * Set the transport
    * 
    * @param jmxInvokerName the invoker name    
    */
   void setJMXInvokerName(ObjectName jmxInvokerName);

   /**
    * Expose the DataSource via JMX to invokers.
    * 
    * @param invocation A pointer to the invocation object
    * 
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.    
    */
   Object invoke(Invocation invocation) throws Exception;
}
