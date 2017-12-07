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
package org.jboss.jmx.connector.invoker;

/**
 * MBean interface.
 */
public interface InvokerAdaptorServiceMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.jmx:type=adaptor,protocol=INVOKER");

  java.lang.Class[] getExportedInterfaces() ;

  void setExportedInterfaces(java.lang.Class[] exportedInterfaces) ;

   /**
    * Expose the service interface mapping as a read-only attribute
    * @return A Map<Long hash, Method> of the MBeanServer    */
  java.util.Map getMethodMap() ;

   /**
    * Expose the MBeanServer service via JMX to invokers.
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.    */
  java.lang.Object invoke(org.jboss.invocation.Invocation invocation) throws java.lang.Exception;

}
