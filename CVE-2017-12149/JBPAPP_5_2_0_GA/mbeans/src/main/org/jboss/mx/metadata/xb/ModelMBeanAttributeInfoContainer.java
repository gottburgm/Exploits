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

import javax.management.Descriptor;
import javax.management.NotCompliantMBeanException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.xml.namespace.QName;

import org.jboss.xb.binding.GenericValueContainer;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * The ModelMBeanAttributeInfo JBossXB container 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class ModelMBeanAttributeInfoContainer
   implements GenericValueContainer
{
   private static final Logger log = Logger.getLogger(ModelMBeanAttributeInfoContainer.class);
   private String name;
   private String type;
   private String access;
   private String getMethod;
   private String setMethod;
   private String description;
   private Object value;
   private Object defaultValue;
   private Descriptor descriptor;

   public Object getValue()
   {
      return value;
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   public Object getDefaultValue()
   {
      return defaultValue;
   }

   public void setDefaultValue(Object defaultValue)
   {
      this.defaultValue = defaultValue;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getAccess()
   {
      return access;
   }

   public void setAccess(String access)
   {
      this.access = access;
   }

   public String getGetMethod()
   {
      return getMethod;
   }

   public void setGetMethod(String getMethod)
   {
      this.getMethod = getMethod;
   }

   public String getSetMethod()
   {
      return setMethod;
   }

   public void setSetMethod(String setMethod)
   {
      this.setMethod = setMethod;
   }

   public Descriptor getDescriptors()
   {
      return descriptor;
   }
   public void setDescriptors(Descriptor descriptor)
   {
      this.descriptor = descriptor;
   }

   public Object instantiate()
   {
      try
      {
         ModelMBeanAttributeInfo info = buildAttributeInfo();
         return info;
      }
      catch(NotCompliantMBeanException e)
      {
         throw new JBossXBRuntimeException(e);
      }
   }

   public void addChild(QName name, Object value)
   {
      log.debug("addChild, " + name + "," + value);
      String localName = name.getLocalPart();
      if("name".equals(localName))
      {
         this.name = (String) value;
      }
      if("type".equals(localName))
      {
         this.type = (String) value;
      }
      if("access".equals(localName))
      {
         this.access = (String) value;
      }
      if("getMethod".equals(localName))
      {
         this.getMethod = (String) value;
      }
      if("setMethod".equals(localName))
      {
         this.setMethod = (String) value;
      }
      if("value".equals(localName))
      {
         this.value = value;
      }
      if("default".equals(localName))
      {
         this.defaultValue = value;
      }
      if("description".equals(localName))
      {
         this.description = (String) value;
      }
   }
   public Class getTargetClass()
   {
      return ModelMBeanAttributeInfo.class;
   }

   protected ModelMBeanAttributeInfo buildAttributeInfo()
      throws NotCompliantMBeanException
   {
      if (descriptor == null)
      {
         descriptor = new DescriptorSupport();
      }
      if (descriptor.getFieldValue(ModelMBeanConstants.NAME) == null)
      {
         descriptor.setField(ModelMBeanConstants.NAME, name);
      }
      if (descriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE) == null)
      {
         descriptor.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR);
      }

      if (value != null)
      {
         descriptor.setField(ModelMBeanConstants.CACHED_VALUE, value);
      }
      if (defaultValue != null)
      {
         descriptor.setField(ModelMBeanConstants.DEFAULT, defaultValue);
      }
      
      if (getMethod != null) 
      {
         descriptor.setField(ModelMBeanConstants.GET_METHOD, getMethod);
      }
      if (setMethod != null) 
      {
         descriptor.setField(ModelMBeanConstants.SET_METHOD, setMethod);
      }

      // defaults read-write
      boolean isReadable = true;
      boolean isWritable = true;
      if (access.equalsIgnoreCase("read-only"))
      {
         isWritable = false;
      }
      else if (access.equalsIgnoreCase("write-only"))
      {
         isReadable = false;
      }

      ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo(
         name, type, description, isReadable, isWritable, false, descriptor
      );
      return info;
   }

}
