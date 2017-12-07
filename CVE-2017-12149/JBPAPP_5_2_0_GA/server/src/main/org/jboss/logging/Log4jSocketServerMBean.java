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
package org.jboss.logging;

/**
 * MBean interface.
 */
public interface Log4jSocketServerMBean extends org.jboss.system.ServiceMBean {

  void setPort(int port) ;

  int getPort() ;

  void setBacklog(int backlog) ;

  int getBacklog() ;

  void setBindAddress(java.net.InetAddress addr) ;

  java.net.InetAddress getBindAddress() ;

  void setListenerEnabled(boolean enabled) ;

  boolean setListenerEnabled() ;

  void setLoggerRepositoryFactoryType(java.lang.Class type) throws java.lang.InstantiationException, java.lang.IllegalAccessException, java.lang.ClassCastException;

  java.lang.Class getLoggerRepositoryFactoryType() ;

  org.apache.log4j.spi.LoggerRepository getLoggerRepository(java.net.InetAddress addr) ;

}
