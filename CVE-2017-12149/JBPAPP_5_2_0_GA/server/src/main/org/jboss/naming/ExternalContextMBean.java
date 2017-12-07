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
package org.jboss.naming;

/**
 * MBean interface.
 * @see org.jboss.naming.NonSerializableFactory
 */
public interface ExternalContextMBean extends org.jboss.system.ServiceMBean {

   /**
    * Set the jndi name under which the external context is bound.
    */
  java.lang.String getJndiName() ;

   /**
    * Set the jndi name under which the external context is bound.
    */
  void setJndiName(java.lang.String jndiName) throws javax.naming.NamingException;

  boolean getRemoteAccess() ;

  void setRemoteAccess(boolean remoteAccess) ;

  boolean getCacheContext() ;

  void setCacheContext(boolean cacheContext) ;

   /**
    * Get the class name of the InitialContext implementation to use. Should be one of: <ul> <li>javax.naming.InitialContext <li>javax.naming.directory.InitialDirContext <li>javax.naming.ldap.InitialLdapContext </ul>
    * @return the classname of the InitialContext to use    */
  java.lang.String getInitialContext() ;

   /**
    * Set the class name of the InitialContext implementation to use. Should be one of: <ul> <li>javax.naming.InitialContext <li>javax.naming.directory.InitialDirContext <li>javax.naming.ldap.InitialLdapContext </ul>
    * @param contextClass, the classname of the InitialContext to use    */
  void setInitialContext(java.lang.String className) throws java.lang.ClassNotFoundException;

   /**
    * Set the InitialContex class environment properties from the given URL.
    */
  void setPropertiesURL(java.lang.String contextPropsURL) throws java.io.IOException;

   /**
    * Set the InitialContex class environment properties.
    */
  void setProperties(java.util.Properties props) throws java.io.IOException;

   /**
    * Get the InitialContex class environment properties.
    */
  java.util.Properties getProperties() throws java.io.IOException;

}
