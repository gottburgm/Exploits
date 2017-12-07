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
package org.jboss.test.jmx.compliance.standard.support;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.StandardMBean;

public class MyStandardMBean
   extends StandardMBean
   implements MyManagementInterface
{
   public static final String MBEAN_CLASSNAME = "MBEAN_CLASSNAME";
   public static final String MBEAN_DESCRIPTION = "MBEAN_DESCRIPTION";
   public static final String MBEAN_ATTRIBUTE_DESCRIPTION = "MBEAN_ATTRIBUTE_DESCRIPTION";
   public static final String MBEAN_CONSTRUCTOR_DESCRIPTION = "MBEAN_CONSTRUCTOR_DESCRIPTION";
   public static final String MBEAN_OPERATION_DESCRIPTION = "MBEAN_OPERATION_DESCRIPTION";
   public static final String MBEAN_PARAMETER = "MBEAN_PARAMETER";
   public static final String MBEAN_PARAMETER_DESCRIPTION = "MBEAN_PARAMETER_DESCRIPTION";

   public MyStandardMBean()
      throws Exception
   {
      super(MyManagementInterface.class);
   }

   public MyStandardMBean(String param1, String param2)
      throws Exception
   {
      super(MyManagementInterface.class);
   }

   public String getAnAttribute()
   {
      return null;
   }

   public void setAnAttribute(String value)
   {
   }

   public void anOperation(String param1, String param2)
   {
   }

   protected String getClassName(MBeanInfo info)
   {
      return MBEAN_CLASSNAME;
   }

   protected String getDescription(MBeanInfo info)
   {
      return MBEAN_DESCRIPTION;
   }

   protected String getDescription(MBeanAttributeInfo info)
   {
      return MBEAN_ATTRIBUTE_DESCRIPTION + info.getName();
   }

   protected String getDescription(MBeanConstructorInfo info)
   {
      return MBEAN_CONSTRUCTOR_DESCRIPTION + info.getSignature().length;
   }

   protected String getDescription(MBeanOperationInfo info)
   {
      return MBEAN_OPERATION_DESCRIPTION + info.getName();
   }

   protected String getDescription(MBeanConstructorInfo info, MBeanParameterInfo param, int sequence)
   {
      return MBEAN_PARAMETER_DESCRIPTION + sequence;
   }

   protected String getDescription(MBeanOperationInfo info, MBeanParameterInfo param, int sequence)
   {
      return MBEAN_PARAMETER_DESCRIPTION + info.getName() + sequence;
   }

   protected String getParameterName(MBeanConstructorInfo info, MBeanParameterInfo param, int sequence)
   {
      return MBEAN_PARAMETER + sequence;
   }

   protected String getParameterName(MBeanOperationInfo info, MBeanParameterInfo param, int sequence)
   {
      return MBEAN_PARAMETER + info.getName() + sequence;
   }

   protected int getImpact(MBeanOperationInfo info)
   {
      return MBeanOperationInfo.ACTION;
   }
}
