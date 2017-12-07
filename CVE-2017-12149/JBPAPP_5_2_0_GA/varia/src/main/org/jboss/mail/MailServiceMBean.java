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
package org.jboss.mail;

/**
 * MBean interface.
 */
public interface MailServiceMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:type=Service,service=Mail");

   /**
    * User id used to connect to a mail server
    * @see #setPassword
    */
  void setUser(java.lang.String user) ;

  java.lang.String getUser() ;

   /**
    * Password used to connect to a mail server
    * @see #setUser
    */
  void setPassword(java.lang.String password) ;

   /**
    * Configuration for the mail service.
    */
  org.w3c.dom.Element getConfiguration() ;

   /**
    * Configuration for the mail service.
    */
  void setConfiguration(org.w3c.dom.Element element) ;

   /**
    * The JNDI name under which javax.mail.Session objects are bound.
    */
  void setJNDIName(java.lang.String name) ;

  java.lang.String getJNDIName() ;

  java.lang.String getStoreProtocol() ;

  java.lang.String getTransportProtocol() ;

  java.lang.String getDefaultSender() ;

  java.lang.String getSMTPServerHost() ;

  java.lang.String getPOP3ServerHost() ;

}
