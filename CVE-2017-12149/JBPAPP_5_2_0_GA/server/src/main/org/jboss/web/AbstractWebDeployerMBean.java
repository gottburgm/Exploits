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
package org.jboss.web;

/**
 * MBean interface.
 * @see #performDeploy(WebApplication webApp, String warUrl, WebDescriptorParser webAppParser)
 * @see #performUndeploy(String, WebApplication)
 * @see #parseWebAppDescriptors(DeploymentInfo,ClassLoader, WebMetaData)
 * @see #linkSecurityDomain(String, Context)
 * @see org.jboss.security.RealmMapping;
 * @see org.jboss.security.SimplePrincipal;
 * @see org.jboss.security.SecurityAssociation;
 */
public interface AbstractWebDeployerMBean extends org.jboss.deployment.SubDeployerMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.web:service=WebServer");

   /**
    * Get the flag indicating if the normal Java2 parent first class loading model should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    */
  boolean getJava2ClassLoadingCompliance() ;

   /**
    * Set the flag indicating if the normal Java2 parent first class loading model should be used over the servlet 2.3 web container first model.
    * @param flag true for parent first, false for the servlet 2.3 model
    */
  void setJava2ClassLoadingCompliance(boolean flag) ;

   /**
    * Set the flag indicating if war archives should be unpacked. This may need to be set to false as long extraction paths under deploy can show up as deployment failures on some platforms.
    * @return true is war archives should be unpacked    */
  boolean getUnpackWars() ;

   /**
    * Get the flag indicating if war archives should be unpacked. This may need to be set to false as long extraction paths under deploy can show up as deployment failures on some platforms.
    * @param flag , true is war archives should be unpacked    */
  void setUnpackWars(boolean flag) ;

   /**
    * Get the flag indicating if ejb-link errors should be ignored in favour of trying the jndi-name in jboss-web.xml
    * @return a <code>boolean</code> value
    */
  boolean getLenientEjbLink() ;

   /**
    * Set the flag indicating if ejb-link errors should be ignored in favour of trying the jndi-name in jboss-web.xml
    */
  void setLenientEjbLink(boolean flag) ;

   /**
    * Get the default security domain implementation to use if a war does not declare a security-domain.
    * @return jndi name of the security domain binding to use.
    */
  java.lang.String getDefaultSecurityDomain() ;

   /**
    * Set the default security domain implementation to use if a war does not declare a security-domain.
    * @param defaultSecurityDomain - jndi name of the security domain binding to use.
    */
  void setDefaultSecurityDomain(java.lang.String defaultSecurityDomain) ;

}
