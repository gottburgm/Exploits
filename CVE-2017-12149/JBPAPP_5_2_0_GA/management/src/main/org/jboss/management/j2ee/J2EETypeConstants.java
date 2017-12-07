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
package org.jboss.management.j2ee;

// $Id: J2EETypeConstants.java 81025 2008-11-14 12:49:40Z dimitris@jboss.org $

/**
 * Constants for J2EE management types
 * 
 * @author thomas.diesler@jboss.org
 */
public final class J2EETypeConstants
{
   public static final String J2EEDomain = "J2EEDomain";
   public static final String J2EEServer = "J2EEServer";
   public static final String J2EEApplication = "J2EEApplication";

   // Modules
   public static final String AppClientModule = "AppClientModule";
   public static final String EJBModule = "EJBModule";
   public static final String WebModule = "WebModule";
   public static final String ResourceAdapterModule = "ResourceAdapterModule";

   // EJBs
   public static final String EntityBean = "EntityBean";
   public static final String StatefulSessionBean = "StatefulSessionBean";
   public static final String StatelessSessionBean = "StatelessSessionBean";
   public static final String MessageDrivenBean = "MessageDrivenBean";

   // Web components
   public static final String Servlet = "Servlet";

   // Resources
   public static final String ResourceAdapter = "ResourceAdapter";
   public static final String JavaMailResource = "JavaMailResource";
   public static final String JCAResource = "JCAResource";
   public static final String JCAConnectionFactory = "JCAConnectionFactory";
   public static final String JCAManagedConnectionFactory = "JCAManagedConnectionFactory";
   public static final String JDBCResource = "JDBCResource";
   public static final String JDBCDataSource = "JDBCDataSource";
   public static final String JDBCDriver = "JDBCDriver";
   public static final String JMSResource = "JMSResource";
   public static final String JNDIResource = "JNDIResource";
   public static final String JTAResource = "JTAResource";
   public static final String RMI_IIOPResource = "RMI_IIOPResource";
   public static final String URLResource = "URLResource";
   public static final String JVM = "JVM";

   // JBoss managed objects
   public static final String MBean = "MBean";
   public static final String ServiceModule = "ServiceModule";
}
