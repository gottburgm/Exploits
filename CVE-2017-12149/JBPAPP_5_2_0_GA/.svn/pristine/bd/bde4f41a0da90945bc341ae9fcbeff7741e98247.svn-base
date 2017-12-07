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
package org.jboss.console.manager;

/**
 * MBean interface.
 */
public interface PluginManagerMBean extends org.jboss.system.ServiceMBean {

   /**
    * send a message
    */
  void registerPlugin(java.lang.String consolePluginClassName) throws java.lang.Exception;

   /**
    * send a message
    */
  void registerPlugin(org.jboss.console.manager.interfaces.ConsolePlugin plugin) ;

   /**
    * send a message
    */
  void unregisterPlugin(org.jboss.console.manager.interfaces.ConsolePlugin plugin) ;

  void regenerateAdminTree() ;

  void regenerateAdminTreeForProfile(java.lang.String profile) ;

  org.jboss.console.manager.interfaces.TreeInfo getTreeForProfile(java.lang.String profile) ;

   /**
    * Only return the tree if the actual version is bigger than the known version
    */
  org.jboss.console.manager.interfaces.TreeInfo getUpdateTreeForProfile(java.lang.String profile,long knownVersion) ;

  javax.management.MBeanServer getMBeanServer() ;

  org.jboss.console.manager.interfaces.ManageableResource getBootstrapResource() ;

  java.lang.String getJndiName() ;

  void setJndiName(java.lang.String jndiName) ;

  boolean isEnableShutdown() ;

  void setEnableShutdown(boolean enableShutdown) ;

  java.lang.String getMainLinkUrl() ;

  void setMainLinkUrl(java.lang.String mainLinkUrl) ;

  java.lang.String getMainLogoUrl() ;

  void setMainLogoUrl(java.lang.String mainLogoUrl) ;

}
