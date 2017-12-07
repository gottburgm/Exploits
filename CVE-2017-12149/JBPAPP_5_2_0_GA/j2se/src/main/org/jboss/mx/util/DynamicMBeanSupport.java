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
package org.jboss.mx.util;

import javax.management.DynamicMBean;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.Attribute;
import javax.management.InvalidAttributeValueException;
import javax.management.AttributeList;
import javax.management.MBeanInfo;

/** A noop stub implementation of DynamicMBean
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81019 $
 */
public class DynamicMBeanSupport implements DynamicMBean
{
   public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      return null;
   }

   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException,
         MBeanException, ReflectionException
   {
   }

   public AttributeList getAttributes(String[] attributes)
   {
      return null;
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      return null;
   }

   public Object invoke(String actionName,
         Object[] params,
         String[] signature)
         throws MBeanException, ReflectionException
   {
      return null;
   }

   public MBeanInfo getMBeanInfo()
   {
      return null;
   }
}
