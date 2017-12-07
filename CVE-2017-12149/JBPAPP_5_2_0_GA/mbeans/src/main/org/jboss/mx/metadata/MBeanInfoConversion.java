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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.server.MethodMapper;

/**
 * Routines for converting MBeanInfo to ModelMBeanInfoSupport and stripping
 * ModelMBeanOperationInfos that are referred to in ModelMBeanAttributeInfos.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $
 */
public class MBeanInfoConversion
   implements ModelMBeanConstants
{
   /**
    * Convert regular MBeanInfo into ModelMBeanInfo.
    *
    * @param   info  MBeanInfo to convert (such as the Standard MBean info)
    */
   public static ModelMBeanInfoSupport toModelMBeanInfo(MBeanInfo info)
   {
      return toModelMBeanInfo(info, true);
   }
   
   /**
    * Convert regular MBeanInfo to ModelMBeanInfo.
    *
    * @param   info  MBeanInfo to convert (such a the Standard MBean info)
    * @param   createAttributeOperationMapping setting this boolean to
    *          <tt>true</tt> will automatically create the attribute operation
    *          mapping for Model MBean managemenet attributes. Based on the
    *          Standard MBean attribute naming conventions, the Model MBean
    *          attribute descriptors are mapped to appropriate management 
    *          operations with the <tt>getMethod</tt> and <tt>setMethod</tt>
    *          descriptor fields.
    */
   public static ModelMBeanInfoSupport toModelMBeanInfo(MBeanInfo info, boolean createAttributeOperationMapping)
   {
      if (info instanceof ModelMBeanInfoSupport)
         return (ModelMBeanInfoSupport)info;
         
      if (info instanceof ModelMBeanInfo)
         return new ModelMBeanInfoSupport((ModelMBeanInfo)info);
      
      // create attributes
      MBeanAttributeInfo[] attributes = info.getAttributes();
      ModelMBeanAttributeInfo[] mmbAttributes = new ModelMBeanAttributeInfo[attributes.length];
      List accessorOperations = new ArrayList();
      
      for (int i = 0; i < attributes.length; ++i)
      {
         // add basic info
         ModelMBeanAttributeInfo attrInfo = new ModelMBeanAttributeInfo(
            attributes[i].getName(),
            attributes[i].getType(),
            attributes[i].getDescription(),
            attributes[i].isReadable(),
            attributes[i].isWritable(),
            attributes[i].isIs()
         );
         
         // by default, conversion metadata should not try to cache attributes
         Descriptor d = attrInfo.getDescriptor();
         d.setField(CURRENCY_TIME_LIMIT, CACHE_NEVER);
         attrInfo.setDescriptor(d);
         
         mmbAttributes[i] = attrInfo;

         // if we're doing attribute operation mapping, find the accessor methods
         // from the Standard MBean interface, and create the 'setter' and 'getter'
         // management operations for them. Map the Model MBean attributes to
         // these operations.
         if (createAttributeOperationMapping)
         {
            String getterOperationName  = null;
            String setterOperationName  = null;
            Descriptor getterDescriptor = null;
            Descriptor setterDescriptor = null;
            
            // figure out the getter type
            if (attributes[i].isReadable())
            {
               if (attributes[i].isIs())
                  getterOperationName = "is" + attributes[i].getName();
               else 
                  getterOperationName = "get" + attributes[i].getName();
                  
               // create a descriptor for 'getter' mgmt operation
               getterDescriptor = new DescriptorSupport();
               getterDescriptor.setField(NAME, getterOperationName);
               getterDescriptor.setField(DESCRIPTOR_TYPE, OPERATION_DESCRIPTOR);
               getterDescriptor.setField(ROLE, ROLE_GETTER);
               
               // create the new management operation
               ModelMBeanOperationInfo opInfo = new ModelMBeanOperationInfo(
                     getterOperationName,
                     "Read accessor operation for '" + attributes[i].getName() + "' attribute.",
                     new MBeanParameterInfo[0],    // void signature
                     attributes[i].getType(),      // return type
                     MBeanOperationInfo.INFO,      // impact
                     getterDescriptor
               );

               // modify the attributes descriptor to map the read operation
               // to the above created management operation
               Descriptor attrDescriptor = mmbAttributes[i].getDescriptor();
               attrDescriptor.setField(GET_METHOD, getterOperationName);
               mmbAttributes[i].setDescriptor(attrDescriptor);
               
               accessorOperations.add(opInfo);
            }
            
            // figure out the setter
            if (attributes[i].isWritable())
            {
               setterOperationName = "set" + attributes[i].getName();   
               
               // create a descriptor for 'setter' mgmt operation
               setterDescriptor = new DescriptorSupport();
               setterDescriptor.setField(NAME, setterOperationName);
               setterDescriptor.setField(DESCRIPTOR_TYPE, OPERATION_DESCRIPTOR);
               setterDescriptor.setField(ROLE, ROLE_SETTER);
               
               // create the new management operation
               ModelMBeanOperationInfo opInfo = new ModelMBeanOperationInfo(
                     setterOperationName,
                     "Write accessor operation for '" + attributes[i].getName() + "' attribute.",
                     
                     new MBeanParameterInfo[] {
                        new MBeanParameterInfo("value", attributes[i].getType(), "Attribute's value.")
                     },
                     
                     Void.TYPE.getName(),
                     MBeanOperationInfo.ACTION,
                     setterDescriptor
               );
               
               // modify the attributes descriptor to map the read operation
               // to the above created management operation
               Descriptor attrDescriptor = mmbAttributes[i].getDescriptor();
               attrDescriptor.setField(SET_METHOD, setterOperationName);
               mmbAttributes[i].setDescriptor(attrDescriptor);
               
               accessorOperations.add(opInfo);
            }
         }            
      }

      // deal with the basic manaement operations (non-getter and setter types)
      MBeanOperationInfo[] operations = info.getOperations();
      ModelMBeanOperationInfo[] mmbOperations = new ModelMBeanOperationInfo[operations.length + accessorOperations.size()];

      for (int i = 0; i < operations.length; ++i)
      {
         mmbOperations[i] = new ModelMBeanOperationInfo(
            operations[i].getName(),
            operations[i].getDescription(),
            operations[i].getSignature(),
            operations[i].getReturnType(),
            operations[i].getImpact()
         );
      }
      
      for (int i = operations.length; i < mmbOperations.length; ++i)
         mmbOperations[i] = (ModelMBeanOperationInfo)accessorOperations.get(i - operations.length);

      // the constructors...
      MBeanConstructorInfo[] constructors = info.getConstructors();
      ModelMBeanConstructorInfo[] mmbConstructors = new ModelMBeanConstructorInfo[constructors.length];

      for (int i = 0; i < constructors.length; ++i)
      {
         mmbConstructors[i] = new ModelMBeanConstructorInfo(
            constructors[i].getName(),
            constructors[i].getDescription(),
            constructors[i].getSignature()
         );
      }

      // and finally the notifications
      
      // FIXME: we are assuming here that the Model MBean implementation adds the
      //        default generic and attribute change notifications to the metadata.
      //        I think we could explicitly add them here as well, can't see it
      //        do any harm.   [JPL]
      MBeanNotificationInfo[] notifications = info.getNotifications();
      ModelMBeanNotificationInfo[] mmbNotifications = new ModelMBeanNotificationInfo[notifications.length];

      for (int i = 0; i < notifications.length; ++i)
      {
         mmbNotifications[i] = new ModelMBeanNotificationInfo(
            notifications[i].getNotifTypes(),
            notifications[i].getName(),
            notifications[i].getDescription()
         );
      }

      return new ModelMBeanInfoSupport(info.getClassName(), info.getDescription(),
                                       mmbAttributes, mmbConstructors, mmbOperations, mmbNotifications);
   }

   /**
    * Returns a ModelMBeanInfoSupport where ModelMBeanOperationInfos that are
    * referred to by ModelMBeanAttributeInfo getMethod or setMethod descriptor
    * fields are stripped out.  If the stripAllRoles parameter is true
    * then all the referred-to operations will be stripped.  Otherwise only 
    * referred-to operations with a role of "getter" or "setter" will be stripped.
    */
    // why mbeanexception?
   public static ModelMBeanInfoSupport stripAttributeOperations(ModelMBeanInfo info, boolean stripAllRoles) throws MBeanException
   {
      HashMap opsMap = new HashMap();
      ModelMBeanOperationInfo[] operations = (ModelMBeanOperationInfo[]) info.getOperations();

      for (int i = 0; i < operations.length; i++)
      {
         opsMap.put(MethodMapper.operationSignature(operations[i]), operations[i]);
      }

      ModelMBeanAttributeInfo[] attributes = (ModelMBeanAttributeInfo[]) info.getAttributes();

      for (int i = 0; i < attributes.length; i++)
      {
         if (attributes[i].isReadable() && (attributes[i].getDescriptor().getFieldValue("getMethod") != null))
         {
            String key = MethodMapper.getterSignature(attributes[i]);
            ModelMBeanOperationInfo opinfo = (ModelMBeanOperationInfo) opsMap.get(key);
            String role = (String) opinfo.getDescriptor().getFieldValue("role");
            if ("getter".equals(role) || stripAllRoles)
            {
               opsMap.remove(key);
            }
         }

         if (attributes[i].isWritable() && (attributes[i].getDescriptor().getFieldValue("setMethod") != null))
         {
            String key = MethodMapper.setterSignature(attributes[i]);
            ModelMBeanOperationInfo opinfo = (ModelMBeanOperationInfo) opsMap.get(key);
            
            String role = (String) opinfo.getDescriptor().getFieldValue("role");
            if ("setter".equals(role) || stripAllRoles)
            {
               opsMap.remove(key);
            }
         }
      }

      operations = new ModelMBeanOperationInfo[opsMap.size()];
      int position = 0;
      for (Iterator iterator = opsMap.values().iterator(); iterator.hasNext(); position++)
      {
         operations[position] = (ModelMBeanOperationInfo) iterator.next();
      }

      return new ModelMBeanInfoSupport(
            info.getClassName(), info.getDescription(),
            (ModelMBeanAttributeInfo[]) info.getAttributes(),
            (ModelMBeanConstructorInfo[]) info.getConstructors(),
            operations,
            (ModelMBeanNotificationInfo[]) info.getNotifications(),
            info.getMBeanDescriptor()
      );
   }
}
