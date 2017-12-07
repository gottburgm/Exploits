/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployment;

import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public interface AdminObjectMBean extends ServiceMBean
{

   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=AdminObject");

   /**
    * Get the jndi name
    * 
    * @return the jndi name
    */
   String getJNDIName();

   /**
    * Set the jndi name
    * 
    * @param jndiName the jndi name
    */
   void setJNDIName(String jndiName);

   /**
    * Get the properties
    * 
    * @return the properties
    */
   Properties getProperties();

   /**
    * Set the properties
    * 
    * @param properties the properties
    */
   void setProperties(java.util.Properties properties);

   /**
    * Get the rar name
    * @return the rar name
    */
   javax.management.ObjectName getRARName();

   /**
    * Set the rar name
    * @param rarName the rar name
    */
   void setRARName(ObjectName rarName);

   /**
    * Get the interface type
    * 
    * @return the interface type
    */
   String getType();

   /**
    * Set the interface type
    * 
    * @param type the interface type
    */
   void setType(String type);
}
