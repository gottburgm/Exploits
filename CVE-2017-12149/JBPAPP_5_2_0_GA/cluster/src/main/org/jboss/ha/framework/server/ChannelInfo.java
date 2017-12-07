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

package org.jboss.ha.framework.server;

import java.util.List;

import javax.management.ObjectName;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.View;
import org.jgroups.conf.ProtocolData;

/**
 * Information describing an open JGroups Channel.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ChannelInfo
{
   private final String id;
   private final String stackName;
   private final Channel channel;
   private final ProtocolData[] protocolStackConfiguration;
   private final ObjectName channelObjectName;
   private final List<ObjectName> protocolObjectNames;
   
   public ChannelInfo(String id, String stackName, Channel channel, 
         ProtocolData[] config, ObjectName channelObjectName, 
         List<ObjectName> protocolObjectNames)
   {
      if (channel == null)
      {
         throw new IllegalArgumentException("null channel");
      }
      
      this.id = id;
      this.stackName = stackName;
      this.channel = channel;
      this.protocolStackConfiguration = config;
      this.channelObjectName = channelObjectName;
      this.protocolObjectNames = protocolObjectNames;
   }
   
   public String getId()
   {
      return id;
   }
   
   public String getClusterName()
   {
      return channel.getClusterName();
   }
   
   public String getStackName()
   {
      return stackName;
   }
   
   public Channel getChannel()
   {
      return channel;
   }
   
   public ProtocolData[] getProtocolStackConfiguration()
   {
      return protocolStackConfiguration;
   }
   
   public ObjectName getChannelObjectName()
   {
      return channelObjectName;
   }
   
   public List<ObjectName> getProtocolObjectNames()
   {
      return protocolObjectNames;
   } 
   
   public Address getLocalAddress()
   {
      return this.channel.getLocalAddress();
   }
   
   public View getCurrentView()
   {
      return this.channel.getView();
   }
}
