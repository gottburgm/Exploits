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

import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.xml.namespace.QName;

import org.jboss.xb.binding.GenericValueContainer;
import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * The ModelMBeanOperationInfo JBossXB container 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class ModelMBeanOperationInfoContainer
   implements GenericValueContainer
{
   private static final Logger log = Logger.getLogger(ModelMBeanOperationInfoContainer.class);
   private String name;
   private String returnType = "void";
   private String impact;
   private String description;
   private ArrayList params = new ArrayList();
   private Descriptor descriptor;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getReturnType()
   {
      return returnType;
   }

   public void setReturnType(String returnType)
   {
      this.returnType = returnType;
   }

   public String getImpact()
   {
      return impact;
   }

   public void setImpact(String impact)
   {
      this.impact = impact;
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

   public void addParameter(MBeanParameterInfo param)
   {
      params.add(param);
   }

   public Object instantiate()
   {
      MBeanParameterInfo[] sig = new MBeanParameterInfo[params.size()];
      params.toArray(sig);
      if( descriptor != null )
      {
         if( descriptor.getFieldValue(ModelMBeanConstants.NAME) == null )
            descriptor.setField(ModelMBeanConstants.NAME, name);
         if( descriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE) == null )
            descriptor.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.OPERATION_DESCRIPTOR);
         if( descriptor.getFieldValue(ModelMBeanConstants.ROLE) == null )
            descriptor.setField(ModelMBeanConstants.ROLE, ModelMBeanConstants.ROLE_OPERATION);
      }
      int operImpact = MBeanOperationInfo.ACTION_INFO;

      if (impact != null)
      {
         if (impact.equals(ModelMBeanConstants.INFO))
            operImpact = MBeanOperationInfo.INFO;
         else if (impact.equals(ModelMBeanConstants.ACTION))
            operImpact = MBeanOperationInfo.ACTION;
         else if (impact.equals(ModelMBeanConstants.ACTION_INFO))
            operImpact = MBeanOperationInfo.ACTION_INFO;
      }

      ModelMBeanOperationInfo info = new ModelMBeanOperationInfo(name,
         description, sig, returnType, operImpact, descriptor);
      return info;
   }
   public void addChild(QName name, Object value)
   {
      ModelMBeanOperationInfoContainer.log.debug("addChild, " + name + "," + value);
      if("name".equals(name.getLocalPart()))
      {
         this.name = (String) value;
      }
      if("description".equals(name.getLocalPart()))
      {
         this.description = (String) value;
      }
   }
   public Class getTargetClass()
   {
      return ModelMBeanOperationInfo.class;
   }

}
