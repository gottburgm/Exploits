/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.resource.metadata.mcf.XAConnectionPropertyMetaData;
import org.jboss.resource.metadata.mcf.XADataSourceDeploymentMetaData;

/**
 * An InstanceClassFactory for XADataSourceDeploymentMetaData that handles
 * the xa-datasource-properties property as a MapCompositeValueSupport
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */
public class XADSInstanceClassFactory
   extends AbstractConnectionFactoryICF<XADataSourceDeploymentMetaData>
{

   public XADSInstanceClassFactory()
   {
      super();
   }
   public XADSInstanceClassFactory(ManagedObjectFactory mof)
   {
      super(mof);
   }

   public Class<XADataSourceDeploymentMetaData> getType()
   {
      return XADataSourceDeploymentMetaData.class;
   }

   @Override
   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         XADataSourceDeploymentMetaData attachment)
   {
      MetaValue metaValue = null;
      if("xa-datasource-properties".equals(property.getName()))
      {
         List<XAConnectionPropertyMetaData> list = attachment.getXADataSourceProperties();
         if(list != null)
         {
            MapCompositeValueSupport map = new MapCompositeValueSupport(SimpleMetaType.STRING);
            for(XAConnectionPropertyMetaData prop : list)
            {
               MetaValue svalue = SimpleValueSupport.wrap(prop.getValue());
               map.put(prop.getName(), svalue);
            }
            metaValue = map;
         }
      }
      else
      {
         metaValue = super.getValue(beanInfo, property, metaData, attachment);
      }
      return metaValue;
   }

   @Override
   protected Object unwrapValue(BeanInfo beanInfo, ManagedProperty property,
         MetaValue value)
   {

      Object unwrapValue = null;
      if("xa-datasource-properties".equals(property.getName()))
      {
         if((value instanceof MapCompositeValueSupport) == false)
         {
            return super.unwrapValue(beanInfo, property, value);
         }
         
         MapCompositeValueSupport mapValue = (MapCompositeValueSupport) value;         
         
         List<XAConnectionPropertyMetaData> list = new ArrayList<XAConnectionPropertyMetaData>();
         for(String name : mapValue.getMetaType().keySet())
         {
            XAConnectionPropertyMetaData prop = new XAConnectionPropertyMetaData();
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
