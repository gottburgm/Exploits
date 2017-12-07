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

import javax.management.NotCompliantMBeanException;

/**
 * A standard mbean delegate factory.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81019 $
 */
public interface StandardMBeanDelegateFactory
{
   /**
    * Construct a new StandardMBeanImpl from the given implementation object
    * and the passed management interface class.
    *
    * @param implementation the object implementing the mbean
    * @param mbeanInterface the management interface of the mbean
    * @return the standardmbean delegate
    * @exception IllegalArgumentException for a null implementation
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or the implementation
    *            does not implement the interface
    */
   public StandardMBeanDelegate createStandardMBean(Object implementation, Class mbeanInterface)
      throws NotCompliantMBeanException;
}

