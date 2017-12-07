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
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.resource.metadata.mcf.DataSourceConnectionPropertyMetaData;
import org.jboss.resource.metadata.mcf.NonXADataSourceDeploymentMetaData;

/**
 * Common operations for the NonXADataSourceDeploymentMetaData.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 90453 $
 */
public abstract class AbstractNonXADataSourceICF<T extends NonXADataSourceDeploymentMetaData>
      extends AbstractConnectionFactoryICF<T>
{

   public AbstractNonXADataSourceICF()
   {
      super();
   }
   
   public AbstractNonXADataSourceICF(ManagedObjectFactory mof)
   {
      super(mof);
   }
   
   @Override
   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         T attachment)
   {
      MetaValue value = null;
      if("connection-properties".equals(property.getName()))
      {
         MapCompositeValueSupport mapValue = new MapCompositeValueSupport(SimpleMetaType.STRING);
         List<DataSourceConnectionPropertyMetaData> list = attachment.getDataSourceConnectionProperties();
         if(list != null)
         {
            for(DataSourceConnectionPropertyMetaData prop : list)
            {
               MetaValue wrapped = SimpleValueSupport.wrap(prop.getValue());
               mapValue.put(prop.getName(), wrapped);
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
      if("connection-properties".equals(property.getName()))
      {
         if((value instanceof MapCompositeValueSupport) == false)
         {
            return super.unwrapValue(beanInfo, property, value);
         }
    
         MapCompositeValueSupport mapValue = (MapCompositeValueSupport) value;
         
         List<DataSourceConnectionPropertyMetaData> list = new ArrayList<DataSourceConnectionPropertyMetaData>();
         for(String name : mapValue.getMetaType().keySet())
         {
            DataSourceConnectionPropertyMetaData prop = new DataSourceConnectionPropertyMetaData();
            prop.setName(name);
            String svalue = (String) getMetaValueFactory().unwrap(mapValue.get(name));
            prop.setValue(svalue);
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
