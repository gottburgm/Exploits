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
package org.jboss.naming;

/**
 * MBean interface.
 */
public interface NamingServiceMBean extends org.jboss.system.ServiceMBean, org.jnp.server.MainMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=Naming");

   /**
    * Set the thread pool used for the bootstrap lookups
    * @param poolMBean    */
  void setLookupPool(org.jboss.util.threadpool.BasicThreadPoolMBean poolMBean) ;

   /**
    * Get the call by value flag for jndi lookups.
    * @return true if all lookups are unmarshalled using the caller's TCL, false if in VM lookups return the value by reference.    */
  boolean getCallByValue() ;

   /**
    * Set the call by value flag for jndi lookups.
    * @param flag - true if all lookups are unmarshalled using the caller's TCL, false if in VM lookups return the value by reference.    */
  void setCallByValue(boolean flag) ;

   /**
    * Expose the Naming service interface mapping as a read-only attribute
    * @return A Map<Long hash, Method> of the Naming interface    */
  java.util.Map getMethodMap() ;

   /**
    * Expose the Naming service via JMX to invokers.
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.    */
  java.lang.Object invoke(org.jboss.invocation.Invocation invocation) throws java.lang.Exception;
  
  /**
   * Create an alias
   * 
   * @param fromName the from name
   * @param toName the to name
   * @throws Exception for any error
   */
  void createAlias(String fromName, String toName) throws Exception;
  
  /**
   * Remove an alias
   * 
   * @param name the name
   * @throws Exception for any error
   */
  void removeAlias(String name) throws Exception;
}
