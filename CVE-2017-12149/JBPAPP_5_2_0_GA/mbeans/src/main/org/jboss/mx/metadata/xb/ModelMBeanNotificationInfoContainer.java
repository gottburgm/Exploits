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
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.xml.namespace.QName;

import org.jboss.xb.binding.GenericValueContainer;
import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * The JBossXB ModelMBeanNotificationInfo container
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class ModelMBeanNotificationInfoContainer
   implements GenericValueContainer
{
   private static final Logger log = Logger.getLogger(ModelMBeanNotificationInfoContainer.class);
   private String name;
   private ArrayList types = new ArrayList();
   private String description;
   private Descriptor descriptor;

   public Descriptor getDescriptors()
   {
      return descriptor;
   }
   public void setDescriptors(Descriptor descriptor)
   {
      this.descriptor = descriptor;
   }

   public void addType(String type)
   {
      types.add(type);
   }
   public Object instantiate()
   {
      if (descriptor != null)
      {
         if (descriptor.getFieldValue(ModelMBeanConstants.NAME) == null)
         {
            descriptor.setField(ModelMBeanConstants.NAME, name);
         }
         if (descriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE) == null)
         {
            descriptor.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.NOTIFICATION_DESCRIPTOR);
         }
      }

      String[] ntypes = new String[types.size()];
      types.toArray(ntypes);
      ModelMBeanNotificationInfo info = new ModelMBeanNotificationInfo(
         ntypes, name, description, descriptor);
      return info;
   }

   public void addChild(QName name, Object value)
   {
      log.debug("addChild, " + name + "," + value);
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
      return ModelMBeanNotificationInfo.class;
   }

}
