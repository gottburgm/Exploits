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
 */
public interface NamingAliasMBean extends org.jboss.system.ServiceMBean {

   /**
    * Get the from name of the alias. This is the location where the LinkRef is bound under JNDI.
    * @return the location of the LinkRef    */
  java.lang.String getFromName() ;

   /**
    * Set the from name of the alias. This is the location where the LinkRef is bound under JNDI.
    * @param name, the location where the LinkRef will be bound    */
  void setFromName(java.lang.String name) throws javax.naming.NamingException;

   /**
    * Get the to name of the alias. This is the target name to which the LinkRef refers. The name is a URL, or a name to be resolved relative to the initial context, or if the first character of the name is ".", the name is relative to the context in which the link is bound.
    * @return the target JNDI name of the alias.    */
  java.lang.String getToName() ;

   /**
    * Set the to name of the alias. This is the target name to which the LinkRef refers. The name is a URL, or a name to be resolved relative to the initial context, or if the first character of the name is ".", the name is relative to the context in which the link is bound.
    * @param name, the target JNDI name of the alias.    */
  void setToName(java.lang.String name) throws javax.naming.NamingException;

}
