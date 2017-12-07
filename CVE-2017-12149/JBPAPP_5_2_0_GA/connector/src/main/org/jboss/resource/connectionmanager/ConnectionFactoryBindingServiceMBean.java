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
package org.jboss.resource.connectionmanager;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public interface ConnectionFactoryBindingServiceMBean extends ServiceMBean
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=ConnectionFactoryBinding");

   /**
    * Get the connection manager
    * 
    * @return the connection manager    
    */
   ObjectName getConnectionManager();

   /**
    * Set the connection manager
    * 
    * @param cm the connection manager    
    */
   void setConnectionManager(ObjectName cm);

   /**
    * Get the bind name
    * 
    * @return the real jndi binding    
    */
   String getBindName();

   /**
    * Get the jndi name
    * 
    * @return the jndi name    
    */
   String getJndiName();

   /**
    * Set the jndi name
    * 
    * @param jndiName the jndi name    
    */
   void setJndiName(String jndiName);

   /**
    * Are we using the java naming context
    * 
    * @return true when using the java naming context, false otherwise    
    */
   boolean isUseJavaContext();

   /**
    * Set whether to use the java naming context
    * 
    * @param useJavaContext pass true to use the java naming context, false otherwise    
    */
   void setUseJavaContext(boolean useJavaContext);
}
