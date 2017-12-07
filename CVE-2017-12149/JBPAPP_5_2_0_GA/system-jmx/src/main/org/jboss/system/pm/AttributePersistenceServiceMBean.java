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
package org.jboss.system.pm;

/**
 * MBean interface.
 */
public interface AttributePersistenceServiceMBean extends org.jboss.system.ServiceMBean {

  java.lang.String getVersionTag() ;

  void setVersionTag(java.lang.String versionTag) ;

  java.lang.String getAttributePersistenceManagerClass() ;

  void setAttributePersistenceManagerClass(java.lang.String apmClass) ;

  org.w3c.dom.Element getAttributePersistenceManagerConfig() ;

  void setAttributePersistenceManagerConfig(org.w3c.dom.Element apmConfig) ;

  boolean getApmDestroyOnServiceStop() ;

  void setApmDestroyOnServiceStop(boolean apmDestroyOnStop) ;

  org.jboss.mx.persistence.AttributePersistenceManager apmCreate() ;

  boolean apmExists(java.lang.String id) throws java.lang.Exception;

  void apmRemove(java.lang.String id) throws java.lang.Exception;

  void apmRemoveAll() throws java.lang.Exception;

  java.lang.String[] apmListAll() throws java.lang.Exception;

  java.lang.String apmListAllAsString() throws java.lang.Exception;

}
