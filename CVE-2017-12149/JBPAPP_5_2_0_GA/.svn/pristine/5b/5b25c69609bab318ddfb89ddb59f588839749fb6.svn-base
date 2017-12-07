/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ha.framework.server.managed;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jgroups.conf.ProtocolData;
import org.jgroups.conf.ProtocolParameter;

/**
 * MetaMapper for the ProtocolData[] description of a JGroups protocol stack
 * configuration. 
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ProtocolStackConfigMapper extends MetaMapper<ProtocolData[]>
{
   public static final CompositeMetaType PROTOCOL_PARAMETER_TYPE;
   public static final CompositeMetaType PROTOCOL_PARAMETER_MAP_TYPE;
   public static final CompositeMetaType PROTOCOL_STACK_CONFIG_TYPE;
   public static final CollectionMetaType TYPE;
   
   static
   {
      String[] paramItemNames = {
            "description",
            "value"
      };
      String[] paramItemDescs = {
            "description of the meaning of the attribute",
            "the value of the configuration parameter"
      };
      MetaType[] paramItemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING            
      };
      
      PROTOCOL_PARAMETER_TYPE = new ImmutableCompositeMetaType(ProtocolParameter.class.getName(), 
            "Protocol Parameters",
            paramItemNames, paramItemDescs, paramItemTypes);
      
//      PROTOCOL_PARAMETER_MAP_TYPE = new CollectionMetaType(Set.class.getName(), PROTOCOL_PARAMETER_TYPE);

      PROTOCOL_PARAMETER_MAP_TYPE = new MapCompositeMetaType(PROTOCOL_PARAMETER_TYPE);
      
      String[] configItemNames = {
            "name",
            "description",
            "className",
            "protocolParameters"            
      };
      String[] configItemDescs = {
            "the name of the protocol",
            "description of the protocol",
            "fully-qualified name of the protocol implementation class",
            "set of configuration parameters for the protocol"
      };
      
      MetaType[] configItemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            PROTOCOL_PARAMETER_MAP_TYPE
            
      };
      
      PROTOCOL_STACK_CONFIG_TYPE = new ImmutableCompositeMetaType(ProtocolData.class.getName(), 
            "Protocol Stack Configuration",
            configItemNames, configItemDescs, configItemTypes);
      
      TYPE = new CollectionMetaType(List.class.getName(), PROTOCOL_STACK_CONFIG_TYPE);
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   @Override
   public Type mapToType()
   {
      return List.class;
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, ProtocolData[] data)
   {
      MetaValue[] dataElements = new MetaValue[data.length];
      for (int i = 0; i < data.length; i++)
      {
         Map<String, MetaValue> protocolValue = new HashMap<String, MetaValue>();
         protocolValue.put("name", SimpleValueSupport.wrap(data[i].getProtocolName()));
         protocolValue.put("description", SimpleValueSupport.wrap(data[i].getDescription()));
         protocolValue.put("className", SimpleValueSupport.wrap(data[i].getClassName()));
         
         ProtocolParameter[] params = data[i].getParametersAsArray();
         Map<String, MetaValue> paramValues = new HashMap<String, MetaValue>();
//         MetaValue[] paramElements = new MetaValue[params.length];
         for (int j = 0; j < params.length; j++)
         {
            Map<String, MetaValue> paramValue = new HashMap<String, MetaValue>();
//            paramValue.put("name", SimpleValueSupport.wrap(params[j].getName()));
            // FIXME -- deal with description
//            protocolValue.put("description", SimpleValueSupport.wrap(params[j].getDescription()));
//            paramValue.put("description", null);
            paramValue.put("value", SimpleValueSupport.wrap(params[j].getValue()));
            
            paramValues.put(params[j].getName(), new CompositeValueSupport(PROTOCOL_PARAMETER_TYPE, paramValue));
         }
         protocolValue.put("protocolParameters", new MapCompositeValueSupport(paramValues, PROTOCOL_PARAMETER_MAP_TYPE));
         
         dataElements[i] = new MapCompositeValueSupport(protocolValue, PROTOCOL_STACK_CONFIG_TYPE);
      }
      return new CollectionValueSupport(TYPE, dataElements);
   }

   @Override
   public ProtocolData[] unwrapMetaValue(MetaValue metaValue)
   {
      if (metaValue == null)
      {
         return null;
      }
      
      if ((metaValue instanceof CollectionValue) == false)
      {
         throw new IllegalArgumentException(metaValue + " is not a " + CollectionValue.class.getSimpleName());
      }
      MetaValue[] elements = ((CollectionValue) metaValue).getElements();
      
      ProtocolData[] protocolData = new ProtocolData[elements.length];
      for (int i = 0; i < elements.length; i++)
      {
         CompositeValue protocolValue = (CompositeValue) elements[i];
         String protName = (String) ((SimpleValue) protocolValue.get("name")).getValue();
         String protDesc = (String) ((SimpleValue) protocolValue.get("description")).getValue();
         String protClass = (String) ((SimpleValue) protocolValue.get("className")).getValue();
         CompositeValue paramsValue = (CompositeValue) protocolValue.get("protocolParameters");
         Set<String> paramNames = paramsValue.getMetaType().keySet();
         ProtocolParameter[] protParams = new ProtocolParameter[paramNames.size()];
         int j = 0;
         for (String paramName : paramNames)
         {
            CompositeValue paramValue = (CompositeValue) paramsValue.get(paramName);
            String paramVal = (String) ((SimpleValue) paramValue.get("value")).getValue();
            protParams[j] = new ProtocolParameter(paramName, paramVal);   
            j++;
         }
         protocolData[i] = new ProtocolData(protName, protDesc, protClass, protParams);
      }
      return protocolData;
   }

}
