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
package org.jboss.jmx.adaptor.snmp.trapd;

/**
 * MBean interface.
 */
public interface TrapdServiceMBean extends org.jboss.system.ServiceMBean {

   /**
    * Sets the port that will be used to receive traps
    * @param port the port to listen for traps
    */
  void setPort(int port) ;

   /**
    * Gets the port that will be used to receive traps
    * @return the port to listen for traps
    */
  int getPort() ;

   /**
    * Sets the interface that will be bound
    * @param host the interface to bind
    */
  void setBindAddress(java.lang.String host) throws java.net.UnknownHostException;

   /**
    * Gets the interface that will be bound
    * @return the interface to bind
    */
  java.lang.String getBindAddress() ;

}
