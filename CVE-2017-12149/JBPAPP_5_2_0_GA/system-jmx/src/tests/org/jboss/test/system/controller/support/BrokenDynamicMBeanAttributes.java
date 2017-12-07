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
package org.jboss.test.system.controller.support;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.util.NotImplementedException;

/**
 * BrokenDynamicMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class BrokenDynamicMBeanAttributes implements DynamicMBean
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.test:type=BrokenDynamicMBeanAttributes"); 
   
   public MBeanInfo getMBeanInfo()
   {
      return new BrokenMBeanInfoAttributes();
   }

   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      throw new org.jboss.util.NotImplementedException("getAttribute");
   }

   public AttributeList getAttributes(String[] attributes)
   {
      throw new NotImplementedException("getAttributes");
   }

   public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
   {
      throw new NotImplementedException("invoke");
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      throw new NotImplementedException("setAttribute");
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      throw new NotImplementedException("setAttributes");
   }
   
   private static class BrokenMBeanInfoAttributes extends MBeanInfo
   {
      private static final long serialVersionUID = 1158114004365977632L;

      public BrokenMBeanInfoAttributes() throws IllegalArgumentException
      {
         super(BrokenDynamicMBeanAttributes.class.getName(), "Broken", null, null, null, null);
      }

      public MBeanAttributeInfo[] getAttributes()
      {
         throw new Error("BROKEN");
      }
   }
}
