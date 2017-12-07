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
package org.jboss.mx.metadata;

import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;

/**
 * The <tt>MetaDataBuilder</tt> interface defines the contract between the
 * Model MBean and a metadata builder implementation. The metadata builder
 * implementations can extract the MBean management interface definition from
 * a given data source and construct the corresponding JMX MBeanInfo object
 * instances that define the Model MBean. <p>
 *
 * This interface also defines accessor methods for setting properties which
 * can be used to configure the builder implementations. See 
 * {@link #setProperty} and {@link #getProperty} methods for more information.
 *
 * @see     org.jboss.mx.metadata.AbstractBuilder
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $
 */
public interface MetaDataBuilder
{

   /**
    * Constructs the Model MBean metadata.
    *
    * @return initialized MBean info
    * @throws NotCompliantMBeanException if there were errors building the 
    *         MBean info from the given data source
    */
   public MBeanInfo build() throws NotCompliantMBeanException;

   /**
    * Sets a property that can be used to control the behaviour of the builder
    * implementation.
    *
    * @param   key      unique string key for a property
    * @param   value    property value
    */
   public void setProperty(String key, Object value);
   
   /**
    * Returns an existing property for this builder implementation.
    *
    * @param   key      property key string
    *
    * @return  property value
    */
   public Object getProperty(String key);
}

