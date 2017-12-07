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
package org.jboss.jdo.castor;

/**
 * MBean interface.
 */
public interface CastorJDOImplMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:type=Service,service=JDO,flavor=Castor");

  void setJndiName(java.lang.String jndiName) ;

  java.lang.String getJndiName() ;

  void setConfiguration(java.lang.String dbConf) ;

  java.lang.String getConfiguration() ;

  java.lang.String getConfigurationURL() ;

  void setLockTimeout(int lockTimeout) ;

  int getLockTimeout() ;

  void setLoggingEnabled(boolean loggingEnabled) ;

  boolean getLoggingEnabled() ;

  void setCommonClassPath(boolean commonClassPath) ;

  boolean getCommonClassPath() ;

  void setAutoStore(boolean autoStore) ;

  boolean isAutoStore() ;

   /**
    * True if user prefers to use application server database pools. False if user wants a new connection for each call to getDatabase().
    */
  void setDatabasePooling(boolean dbPooling) ;

   /**
    * Return true if the Database instance uses the application server pooling.
    */
  boolean isDatabasePooling() ;

}
