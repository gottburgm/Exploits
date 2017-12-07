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
package org.jboss.system.server;

/**
 * ServerInfo MBean interface.
 * 
 * @version $Revision: 81032 $
 */
public interface ServerInfoMBean
{
   /** The default ObjectName */
   final String OBJECT_NAME_STR = "jboss.system:type=ServerInfo";

   // Attributes ----------------------------------------------------
   
   String getJavaVersion();
   String getJavaVendor();

   String getJavaVMName();
   String getJavaVMVersion();
   String getJavaVMVendor();

   String getOSName();
   String getOSVersion();
   String getOSArch();

   Integer getActiveThreadCount();
   Integer getActiveThreadGroupCount();
   
   /** @return <tt>Runtime.getRuntime().maxMemory()<tt> on JDK 1.4 vms or -1 on previous versions. */
   Long getMaxMemory();   
   Long getTotalMemory();
   Long getFreeMemory();

   /** @return <tt>Runtime.getRuntime().availableProcessors()</tt> on JDK 1.4 vms or -1 on previous versions. */
   Integer getAvailableProcessors();

   /** @return InetAddress.getLocalHost().getHostName(); */
   String getHostName();

   /** @return InetAddress.getLocalHost().getHostAddress(); */
   String getHostAddress();

   // Operations ----------------------------------------------------
   
   /**
    * Return a listing of the thread pools on jdk5+.
    * @param fancy produce a text-based graph when true
    * @return the memory pools
    */
   String listMemoryPools(boolean fancy);

   /**
    * Return a listing of the active threads and thread groups,
    * and a full stack trace for every thread, on jdk5+.
    * @return the thread dump
    */
   String listThreadDump();

   /**
    * Return a sort list of thread cpu utilization.
    * @return the cpu utilization
    */
   String listThreadCpuUtilization();

   /**
    * Display the java.lang.Package info for the pkgName
    * @param pkgName the package name
    * @return the package info
    */
   String displayPackageInfo(String pkgName);

}
