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

package org.jboss.ha.framework.server.managed;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.jboss.ha.framework.server.ProtocolStackConfigInfo;
import org.jboss.logging.Logger;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolData;
import org.jgroups.conf.ProtocolParameter;
import org.jgroups.conf.ProtocolStackConfigurator;

/**
 * {@link MetaMapper} for a map of {@link ProtocolStackConfigInfo}s keyed by
 * the name of the protocol stack.
 * 
 * @author Brian Stansberry
 */
public class ProtocolStackConfigurationsMapper extends MetaMapper<Map<String, ProtocolStackConfigInfo>>
{
   private static final Logger log = Logger.getLogger(ProtocolStackConfigurationsMapper.class);
   
   private static final ProtocolStackConfigMapper CONFIG_MAPPER = new ProtocolStackConfigMapper();
   public static final CompositeMetaType TYPE;

   static
   {
      
      String[] itemNames = {
            "name",
            "description",
            "configuration"
      };
      String[] itemDescriptions = {
            "the name of the protocol stack",
            "description of the protocol stack",
            "list of protocol configuration elements, each configuring a single protocol",
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            CONFIG_MAPPER.getMetaType()
      };
      TYPE = new ImmutableCompositeMetaType(ProtocolStackConfigInfo.class.getName(), 
            "Protocol Stack Configurations",
            itemNames, itemDescriptions, itemTypes);
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   @Override
   public Type mapToType()
   {
      return Map.class;
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, Map<String, ProtocolStackConfigInfo> object)
   {
      Map<String, MetaValue> result = new HashMap<String, MetaValue>();
      for (Map.Entry<String, ProtocolStackConfigInfo> entry : object.entrySet())
      {
         ProtocolStackConfigInfo info = entry.getValue();
         Map<String, MetaValue> stackValue = new HashMap<String, MetaValue>();
         stackValue.put("name", SimpleValueSupport.wrap(info.getName()));
         stackValue.put("description", SimpleValueSupport.wrap(info.getDescription()));
         
         ProtocolData[] data = info.getConfiguration();         
         stackValue.put("configuration", CONFIG_MAPPER.createMetaValue(CONFIG_MAPPER.getMetaType(), data));
         
         result.put(entry.getKey(), new CompositeValueSupport(TYPE, stackValue));
      }
      
      return new MapCompositeValueSupport(result, TYPE);
   }
   
   /**
    * Converts the {@link CompositeValue} <code>metaValue</code> into a 
    * <code>Map<String, ProtocolStackConfigInfo></code>.
    * 
    * {@inheritDoc}
    * 
    * @throws IllegalArgumentException if <code>metaValue</code> is not a 
    *              {@link CompositeValue}
    */
   @Override
   public Map<String, ProtocolStackConfigInfo> unwrapMetaValue(MetaValue metaValue)
   {
      if (metaValue == null)
      {
         return null;
      }
      
      if ((metaValue instanceof CompositeValue) == false)
      {
         throw new IllegalArgumentException(metaValue + " is not a " + CompositeValue.class.getSimpleName());
      }
      Map<String, ProtocolStackConfigInfo> result = new HashMap<String, ProtocolStackConfigInfo>();
      CompositeValue topCompValue = (CompositeValue) metaValue;
      for (String stack : topCompValue.getMetaType().keySet())
      {
         CompositeValue stackValue = (CompositeValue) topCompValue.get(stack);
         
         String name = (String) ((SimpleValue) stackValue.get("name")).getValue();
         
         String description = (String) ((SimpleValue) stackValue.get("description")).getValue();
         
         CollectionValue protocolsValue = (CollectionValue) stackValue.get("configuration");
         ProtocolData[] protocolData = CONFIG_MAPPER.unwrapMetaValue(protocolsValue);         
         ProtocolStackConfigurator configurator = new ProtocolDataProtocolStackConfigurator(protocolData);
         // fixes http://jira.jboss.com/jira/browse/JGRP-290
         ConfiguratorFactory.substituteVariables(configurator); // replace vars with system props
         
         result.put(stack, new ProtocolStackConfigInfo(name, description, configurator));
      }
      
      return result;
   }

}
