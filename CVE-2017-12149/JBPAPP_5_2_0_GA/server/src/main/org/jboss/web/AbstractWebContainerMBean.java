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
 * @see org.jboss.web.AbstractWebDeployer
 */
public interface AbstractWebContainerMBean extends org.jboss.deployment.SubDeployerMBean {

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
    * @return the LenientEjbLink flag
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

   /**
    * Get the session attribute number under which the caller Subject is stored
    */
  java.lang.String getSubjectAttributeName() ;

   /**
    * Set the session attribute number under which the caller Subject is stored
    */
  void setSubjectAttributeName(java.lang.String subjectAttributeName) ;

   /**
    * See if a war is deployed.
    */
  boolean isDeployed(java.lang.String warUrl) ;

   /**
    * Returns the applications deployed by the web container subclasses.
    * @return An Iterator of WebApplication objects for the deployed wars.    */
  java.util.Iterator getDeployedApplications() ;

   /**
    * An accessor for any configuration element set via setConfig. This method always returns null and must be overriden by subclasses to return a valid value.
    */
  org.w3c.dom.Element getConfig() ;

   /**
    * This method is invoked to import an arbitrary XML configuration tree. Subclasses should override this method if they support such a configuration capability. This implementation does nothing.
    */
  void setConfig(org.w3c.dom.Element config) ;

}
