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

import java.util.Map;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80054 $
 */
public interface CachedConnectionManagerMBean extends ServiceMBean
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=CachedConnectionManager");

   /**
    * Get the SpecCompliant value.
    * 
    * @return the SpecCompliant value.
    */
   boolean isSpecCompliant();

   /**
    * Set the SpecCompliant value.
    * 
    * @param specCompliant The new SpecCompliant value.
    */
   void setSpecCompliant(boolean specCompliant);

   /**
    * Get the debug value.
    * 
    * @return the debug value.
    */
   boolean isDebug();

   /**
    * Set the Debug value.
    * 
    * @param value The new debug value.
    */
   void setDebug(boolean value);

   /**
    * Get the error value.
    * 
    * @return the error value.
    */
   boolean isError();

   /**
    * Set the error value.
    * 
    * @param value The new error value.
    */
   void setError(boolean value);

   /**
    * The Instance attribute simply holds the current instance, which is normally the only instance of CachedConnectionManager.
    * 
    * @return a <code>CachedConnectionManager</code> value
    */
   CachedConnectionManager getInstance();

   /**
    * Get the inuse connections
    * 
    * @return the number of inuse connections
    */
   int getInUseConnections();

   /**
    * List the inuse connections
    * 
    * @return a map of connections to allocation stack traces
    */
   Map listInUseConnections();
}
