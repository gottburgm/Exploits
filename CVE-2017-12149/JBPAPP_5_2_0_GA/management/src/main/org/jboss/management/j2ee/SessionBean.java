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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * The JBoss JSR-77.30.13 implementation of the SessionBean model
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81025 $
 */
public abstract class SessionBean extends EJB
   implements SessionBeanMBean
{

   /**
    * Create a SessionBean model
    *
    * @param j2eeType      the type of session bean
    * @param name          the ejb-name from the deployment
    * @param ejbModuleName the JSR-77 EJBModule name for this bean
    * @param ejbContainerName the JMX name of the JBoss ejb container MBean
    * @param jndiName the jndi name of the remote home binding is one exists,
    *    null if there is no remote home.
    * @param localJndiName the jndi name of the local home binding is one exists,
    *    null if there is no local home.
    * @throws MalformedObjectNameException
    * @throws InvalidParentException
    */
   public SessionBean(String j2eeType, String name, ObjectName ejbModuleName,
      ObjectName ejbContainerName, String jndiName, String localJndiName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(j2eeType, name, ejbModuleName,
         ejbContainerName, jndiName, localJndiName);
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------

}
