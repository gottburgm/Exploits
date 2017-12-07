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
package org.jboss.mx.metadata.xb;

import java.util.ArrayList;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.Descriptor;

import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * A javabean wrapper used to construct a ModelMBeanInfoSupport
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class ModelMBeanInfoSupportWrapper
{
   private static final Logger log = Logger.getLogger(ModelMBeanInfoSupportWrapper.class);
   private String mmbClassName = "org.jboss.mx.modelmbean.XMBean";

   private String description;
   private ArrayList operInfo = new ArrayList();
   private ArrayList attrInfo = new ArrayList();
   private ArrayList constrInfo = new ArrayList();
   private ArrayList notifInfo = new ArrayList();
   private Descriptor descriptor;

   public String getClassName()
   {
      return mmbClassName;
   }

   public void setClassName(String mmbClassName)
   {
      this.mmbClassName = mmbClassName;
   }

   public String getDescription()
   {
      return description;
   }
   public void setDescription(String description)
   {
      this.description = description;
   }
   public Descriptor getDescriptors()
   {
      return descriptor;
   }
   public void setDescriptors(Descriptor descriptor)
   {
      this.descriptor = descriptor;
   }

   public void addConstructor(ModelMBeanConstructorInfo info)
   {
      this.constrInfo.add(info);
   }
   public void addAttribute(ModelMBeanAttributeInfo info)
   {
      this.attrInfo.add(info);
   }
   public void addOperation(ModelMBeanOperationInfo info)
   {
      this.operInfo.add(info);
   }
   public void addNotification(ModelMBeanNotificationInfo info)
   {
      this.notifInfo.add(info);
   }

   public Object instantiate()
   {
      ModelMBeanOperationInfo[] ops = new ModelMBeanOperationInfo[operInfo.size()];
      operInfo.toArray(ops);
      ModelMBeanAttributeInfo[] attrs = new ModelMBeanAttributeInfo[attrInfo.size()];
      attrInfo.toArray(attrs);
      ModelMBeanConstructorInfo[] ctors = new ModelMBeanConstructorInfo[constrInfo.size()];
      constrInfo.toArray(ctors);
      ModelMBeanNotificationInfo[] msgs = new ModelMBeanNotificationInfo[notifInfo.size()];
      notifInfo.toArray(msgs);
      // Validate the required descriptor fields
      if( descriptor != null )
      {
         if( descriptor.getFieldValue(ModelMBeanConstants.NAME) == null )
            descriptor.setField(ModelMBeanConstants.NAME, getClassName());
         if( descriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE) == null )
            descriptor.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.MBEAN_DESCRIPTOR);
      }
      ModelMBeanInfo info = new ModelMBeanInfoSupport(
         mmbClassName, description, attrs, ctors,
         ops, msgs, descriptor);
      return info;
   }

   public ModelMBeanInfo getMBeanInfo()
   {
      return (ModelMBeanInfo) instantiate();
   }
}
