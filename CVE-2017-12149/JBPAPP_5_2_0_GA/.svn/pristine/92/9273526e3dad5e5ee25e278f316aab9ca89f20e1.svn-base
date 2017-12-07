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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.management.ObjectName;

import org.jboss.ha.framework.server.ChannelInfo;
import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.MetaTypeFactory;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jgroups.Address;
import org.jgroups.View;

/**
 * MetaMapper for the {@link JChannelFactory#getOpenChannels()} property.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class OpenChannelsMapper extends MetaMapper<Set<ChannelInfo>>
{   
   public static final MetaType OBJECT_NAME_TYPE;
   public static final CollectionMetaType PROTOCOL_OBJECT_NAMES_TYPE;
   public static final CollectionMetaType MEMBERS_TYPE;
   public static final CompositeMetaType VIEW_TYPE;
   public static final CompositeMetaType CHANNEL_TYPE;
   public static final CollectionMetaType TYPE;
   
   private static final ProtocolStackConfigMapper CONFIG_MAPPER = new ProtocolStackConfigMapper();
   
   static
   {
      OBJECT_NAME_TYPE = MetaTypeFactory.getInstance().resolve(ObjectName.class);
      
      PROTOCOL_OBJECT_NAMES_TYPE = 
         new CollectionMetaType(List.class.getName(), OBJECT_NAME_TYPE);
      
      MEMBERS_TYPE = 
         new CollectionMetaType(List.class.getName(), SimpleMetaType.STRING);
      
      String[] viewItemNames = {
            "id",
            "creator",
            "members",
            "coordinator",
            "payload"
      };
      
      String[] viewDescriptions = {
            "sequence number of the view",
            "Address of the node that issued the view",
            "Addresses of the group members",
            "Address of the node acting as group coordinator",
            "arbitrary information added by the application to the view"
      };
      MetaType[] viewItemTypes = {
            SimpleMetaType.LONG_PRIMITIVE,
            SimpleMetaType.STRING,
            MEMBERS_TYPE,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING            
      };
      
      VIEW_TYPE = new ImmutableCompositeMetaType(View.class.getName(), "JGroups View",
            viewItemNames, viewDescriptions, viewItemTypes);
      
      String[] itemNames = {
            "id",
            "clusterName",
            "stackName",
            "protocolStackConfiguration",
            "channelObjectName",
            "protocolObjectNames",
            "localAddress",
            "currentView"
      };
      String[] itemDescriptions = {
            "id, if any, passed by the client when it requested the channel",
            "name of the cluster group, shared by all channels that form a group",
            "name, if any, of the protocol stack configuration that was used to create the channel",
            "the channel's protocol stack configuration",
            "ObjectName of the mbean that represents the channel, if the factory registered one",
            "ObjectNames of the mbeans that represent the channel's protocol, if the factory registered them",
            "Address of this node in the group",
            "the channel's current group membership view"
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            CONFIG_MAPPER.getMetaType(),
            OBJECT_NAME_TYPE,
            PROTOCOL_OBJECT_NAMES_TYPE,
            SimpleMetaType.STRING,
            VIEW_TYPE
      };
      
      CHANNEL_TYPE = new ImmutableCompositeMetaType(ChannelInfo.class.getName(), 
            "Channel",
            itemNames, itemDescriptions, itemTypes);
      TYPE = new CollectionMetaType(Set.class.getName(), CHANNEL_TYPE);
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   @Override
   public Type mapToType()
   {
      return Set.class;
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, Set<ChannelInfo> object)
   {
      MetaValueFactory valueFactory = MetaValueFactory.getInstance();
      
      List<MetaValue> elements = new ArrayList<MetaValue>();
      for (ChannelInfo chInfo : object)
      {
         Map<String, MetaValue> data = new HashMap<String, MetaValue>();
         
         String id = chInfo.getId();
         if (id != null)
         {
            data.put("id", SimpleValueSupport.wrap(id));
         }
         
         data.put("clusterName", SimpleValueSupport.wrap(chInfo.getClusterName()));
         
         String stackName = chInfo.getStackName();
         if (stackName != null)
         {
            data.put("stackName", SimpleValueSupport.wrap(stackName));
         }
         
         ObjectName on = chInfo.getChannelObjectName();
         if (on != null)
         {
            data.put("channelObjectName", valueFactory.create(on));
         }
         
         data.put("protocolStackConfiguration", CONFIG_MAPPER.createMetaValue(getMetaType(), chInfo.getProtocolStackConfiguration()));
         
         List<ObjectName> protNames = chInfo.getProtocolObjectNames();
         if (protNames != null)
         {
            List<MetaValue> onValues = new ArrayList<MetaValue>();
            for (ObjectName protON : protNames)
            {
               onValues.add(valueFactory.create(protON));
            }
            data.put("protocolObjectNames", 
                  new CollectionValueSupport(PROTOCOL_OBJECT_NAMES_TYPE, 
                        onValues.toArray(new MetaValue[onValues.size()])));
         }
         
         data.put("localAddress", SimpleValueSupport.wrap(chInfo.getLocalAddress().toString()));
         
         MetaValue viewValue = createViewMetaValue(chInfo.getCurrentView());
         data.put("currentView", viewValue);
         
         elements.add(new MapCompositeValueSupport(data, CHANNEL_TYPE));
      }
      return new CollectionValueSupport(TYPE, elements.toArray(new MetaValue[elements.size()]));
   }

   private MetaValue createViewMetaValue(View view)
   {
      Map<String, MetaValue> viewMap = new HashMap<String, MetaValue>();
      viewMap.put("id", SimpleValueSupport.wrap(view.getVid().getId()));
      viewMap.put("creator", SimpleValueSupport.wrap(view.getCreator().toString()));
      Vector<Address> members = view.getMembers();
      MetaValue[] memberValues = new MetaValue[members.size()];
      for (int i = 0; i < memberValues.length; i++)
      {
         memberValues[i] = SimpleValueSupport.wrap(members.get(i).toString());
      }
      viewMap.put("members", new CollectionValueSupport(MEMBERS_TYPE, memberValues));
      if (memberValues.length > 0)
      {
         viewMap.put("coordinator", memberValues[0]);
      }
      
      MetaValue viewValue = new MapCompositeValueSupport(viewMap, VIEW_TYPE);
      return viewValue;
   }

   @Override
   public Set<ChannelInfo> unwrapMetaValue(MetaValue metaValue)
   {
      // We cannot create a ChannelInfo from a metaValue, and it is a 
      // read-only property
      return null;
   }

}
