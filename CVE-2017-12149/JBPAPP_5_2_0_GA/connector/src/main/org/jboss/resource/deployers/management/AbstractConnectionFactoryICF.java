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
package org.jboss.resource.deployers.management;

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.AbstractInstanceClassFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryPropertyMetaData;

/**
 * Common operations for the ManagedConnectionFactoryDeploymentMetaData.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 90453 $
 */
public abstract class AbstractConnectionFactoryICF<T extends ManagedConnectionFactoryDeploymentMetaData>
      extends AbstractInstanceClassFactory<T>
{

   public AbstractConnectionFactoryICF()
   {
      super();
   }
   
   public AbstractConnectionFactoryICF(ManagedObjectFactory mof)
   {
      super(mof);
   }
   
   @Override
   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         T attachment)
   {
      // Get the property name
      String propertyName = property.getMappedName();
      if(propertyName == null)
         propertyName = property.getName();
      
      // Get the property info
      PropertyInfo propertyInfo = property.getField(Fields.PROPERTY_INFO, PropertyInfo.class);
      if(propertyInfo == null)
         propertyInfo = beanInfo.getProperty(propertyName);

      // Check if the property is readable
      if(propertyInfo != null && propertyInfo.isReadable() == false)
         return null;
      
      MetaValue value = null;
      if("config-property".equals(property.getName()))
      {
         MapCompositeValueSupport mapValue = new MapCompositeValueSupport(SimpleMetaType.STRING);
         List<ManagedConnectionFactoryPropertyMetaData> list = attachment.getManagedConnectionFactoryProperties();
         if(list != null)
         {
            for(ManagedConnectionFactoryPropertyMetaData prop : list)
            {
               String name = prop.getName();               
               MetaValue svalue = SimpleValueSupport.wrap(prop.getValue());
               mapValue.put(name, svalue);
               MetaValue stype = SimpleValueSupport.wrap(prop.getType());
               mapValue.put(name+".type", stype);
            }
         }
         value = mapValue;
      }
      else
      {
         value = super.getValue(beanInfo, property, metaData, attachment);
      }
      return value;
   }
   
   @Override
   protected Object unwrapValue(BeanInfo beanInfo, ManagedProperty property,
         MetaValue value)
   {
      Object unwrapValue = null;
      if("config-property".equals(property.getName()))
      {
         if((value instanceof MapCompositeValueSupport) == false)
         {
            return super.unwrapValue(beanInfo, property, value);
         }
         
         MapCompositeValueSupport mapValue = (MapCompositeValueSupport) value;
         
         List<ManagedConnectionFactoryPropertyMetaData> list = new ArrayList<ManagedConnectionFactoryPropertyMetaData>();
         for(String name : mapValue.getMetaType().keySet())
         {
            // Ignore the type we've added before
            if(name.endsWith(".type"))
               continue;
            
            ManagedConnectionFactoryPropertyMetaData prop = new ManagedConnectionFactoryPropertyMetaData();
            prop.setName(name);
            String svalue = (String) getMetaValueFactory().unwrap(mapValue.get(name));
            prop.setValue(svalue);
            String nameType = name+".type";
            MetaValue typeValue = mapValue.get(nameType);
            if(typeValue != null)
            {
               String type = (String) getMetaValueFactory().unwrap(typeValue);
               prop.setType(type);
            }
            list.add(prop);
         }
         unwrapValue = list;
      }
      else
      {
         unwrapValue = super.unwrapValue(beanInfo, property, value);
      }
      return unwrapValue;
   }
}
