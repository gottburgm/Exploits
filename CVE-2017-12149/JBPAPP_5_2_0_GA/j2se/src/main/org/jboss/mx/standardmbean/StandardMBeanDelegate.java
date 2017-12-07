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
package org.jboss.mx.standardmbean;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;

/**
 * A delegate standard mbean.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81019 $
 */
public interface StandardMBeanDelegate extends DynamicMBean
{
   /**
    * Retrieve the implementation object
    *
    * @return the implementation
    */
   Object getImplementation();

   /**
    * Replace the implementation object
    *
    * @param implementation the new implementation
    * @exception IllegalArgumentException for a null parameter
    * @exception NotCompliantMBeanException if the new implementation
    *            does not implement the interface supplied at
    *            construction
    */
   void setImplementation(Object implementation) throws NotCompliantMBeanException;

   /**
    * Retrieve the management interface
    *
    * @return the management interface
    */
   Class getMBeanInterface();

   /**
    * Retrieve the cached mbean info
    *
    * @return the cached mbean info
    */
   MBeanInfo getCachedMBeanInfo();

   /**
    * Sets the cached mbean info
    *
    * @todo make this work after the mbean is registered
    * @param info the mbeaninfo to cache, can be null to erase the cache
    */
   void cacheMBeanInfo(MBeanInfo info);

   /**
    * Builds a default MBeanInfo for this MBean, using the Management Interface specified for this MBean.
    * 
    * @return the default mbean info
    * @throws NotCompliantMBeanException when the mbean is not a standardmbean
    */
   MBeanInfo buildMBeanInfo() throws NotCompliantMBeanException;
}