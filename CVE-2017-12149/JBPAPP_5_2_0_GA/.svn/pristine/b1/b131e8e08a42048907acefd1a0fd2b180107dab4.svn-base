/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.as.integration.hornetq.management.jms;

import java.lang.reflect.Type;

import org.hornetq.api.core.DiscoveryGroupConfiguration;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.management.ConnectionFactoryControl;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created Mar 19, 2010
 */
public class ConnectionFactoryMapper extends MetaMapper<ConnectionFactoryControl>
{
   public static final CompositeMetaType TYPE;
   static
   {
      String[] itemNames = {
            "name",
            "ha",
            "useDiscovery",
            "cfType",
            "connectorNames",
            "Bindings",
            "ClientID",
            "DupsOKBatchSize",
            "TransactionBatchSize",
            "ClientFailureCheckPeriod",
            "ConnectionTTL",
            "CallTimeout",
            "ConsumerWindowSize",
            "ConsumerMaxRate",
            "ConfirmationWindowSize",
            "ProducerMaxRate",
            "ProducerWindowSize",
            "CacheLargeMessagesClient",
            "MinLargeMessageSize",
            "BlockOnNonDurableSend",
            "BlockOnAcknowledge",
            "BlockOnDurableSend",
            "AutoGroup",
            "PreAcknowledge",
            "MaxRetryInterval",
            "RetryIntervalMultiplier",
            "ReconnectAttempts",
            "FailoverOnServerShutdown",
            "ScheduledThreadPoolMaxSize",
            "ThreadPoolMaxSize",
            "GroupID",
            "InitialMessagePacketSize",
            "UseGlobalPools",
            "RetryInterval",
            "ConnectionLoadBalancingPolicyClassName"
      };
      String[] itemDescriptions = {
            "name",
            "ha",
            "useDiscovery",
            "cfType",
            "connectorNames",
            "Bindings",
            "ClientID",
            "DupsOKBatchSize",
            "TransactionBatchSize",
            "ClientFailureCheckPeriod",
            "ConnectionTTL",
            "CallTimeout",
            "ConsumerWindowSize",
            "ConsumerMaxRate",
            "ConfirmationWindowSize",
            "ProducerMaxRate",
            "ProducerWindowSize",
            "CacheLargeMessagesClient",
            "MinLargeMessageSize",
            "BlockOnNonDurableSend",
            "BlockOnAcknowledge",
            "BlockOnDurableSend",
            "AutoGroup",
            "PreAcknowledge",
            "MaxRetryInterval",
            "RetryIntervalMultiplier",
            "ReconnectAttempts",
            "FailoverOnServerShutdown",
            "ScheduledThreadPoolMaxSize",
            "ThreadPoolMaxSize",
            "GroupID",
            "InitialMessagePacketSize",
            "UseGlobalPools",
            "RetryInterval",
            "ConnectionLoadBalancingPolicyClassName"
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING
      };
      TYPE = new ImmutableCompositeMetaType("javax.jms.ConnectionFactory", "Connection Factory Settings",
            itemNames, itemDescriptions, itemTypes);
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, ConnectionFactoryControl control)
   {
      CompositeValueSupport cvs = new CompositeValueSupport(TYPE);
      cvs.set("name", new SimpleValueSupport(SimpleMetaType.STRING, control.getName()));

      TransportConfiguration[] connectors = control.getStaticConnectors();
      DiscoveryGroupConfiguration groupConfiguration = control.getDiscoveryGroupConfiguration();

      StringBuffer connectorNames = new StringBuffer();
      if (groupConfiguration == null)
      {
         for (TransportConfiguration connector : connectors)
         {
            if(connectorNames.length() > 0)
            {
               connectorNames.append(",");
            }
            connectorNames.append(connector.getName());
         }
      }
      else
      {
         connectorNames.append(groupConfiguration.getName());
      }
      cvs.set("ha", new SimpleValueSupport(SimpleMetaType.STRING, control.isHA()));
      cvs.set("useDiscovery", new SimpleValueSupport(SimpleMetaType.STRING, control.getDiscoveryGroupConfiguration() != null));
      //todo uncomment on next HornetQ release
      //cvs.set("cfType", new SimpleValueSupport(SimpleMetaType.STRING, control.getFactoryType()));
      cvs.set("connectorNames", new SimpleValueSupport(SimpleMetaType.STRING, connectorNames));
      cvs.set("Bindings", new SimpleValueSupport(SimpleMetaType.STRING, getJndiString(control.getJNDIBindings())));
      cvs.set("ClientID", new SimpleValueSupport(SimpleMetaType.STRING, control.getClientID()));
      cvs.set("DupsOKBatchSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getDupsOKBatchSize()));
      cvs.set("TransactionBatchSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getTransactionBatchSize()));
      cvs.set("ClientFailureCheckPeriod", new SimpleValueSupport(SimpleMetaType.STRING, control.getClientFailureCheckPeriod()));
      cvs.set("ConnectionTTL", new SimpleValueSupport(SimpleMetaType.STRING, control.getConnectionTTL()));
      cvs.set("CallTimeout", new SimpleValueSupport(SimpleMetaType.STRING, control.getCallTimeout()));
      cvs.set("ConsumerWindowSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getConfirmationWindowSize()));
      cvs.set("ConsumerMaxRate", new SimpleValueSupport(SimpleMetaType.STRING, control.getConsumerMaxRate()));
      cvs.set("ConfirmationWindowSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getConfirmationWindowSize()));
      cvs.set("ProducerMaxRate", new SimpleValueSupport(SimpleMetaType.STRING, control.getProducerMaxRate()));
      cvs.set("ProducerWindowSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getProducerWindowSize()));
      cvs.set("CacheLargeMessagesClient", new SimpleValueSupport(SimpleMetaType.STRING, control.isCacheLargeMessagesClient()));
      cvs.set("MinLargeMessageSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getMinLargeMessageSize()));
      cvs.set("BlockOnNonDurableSend", new SimpleValueSupport(SimpleMetaType.STRING, control.isBlockOnNonDurableSend()));
      cvs.set("BlockOnAcknowledge", new SimpleValueSupport(SimpleMetaType.STRING, control.isBlockOnAcknowledge()));
      cvs.set("BlockOnDurableSend", new SimpleValueSupport(SimpleMetaType.STRING, control.isBlockOnDurableSend()));
      cvs.set("AutoGroup", new SimpleValueSupport(SimpleMetaType.STRING, control.isAutoGroup()));
      cvs.set("PreAcknowledge", new SimpleValueSupport(SimpleMetaType.STRING, control.isPreAcknowledge()));
      cvs.set("MaxRetryInterval", new SimpleValueSupport(SimpleMetaType.STRING, control.getMaxRetryInterval()));
      cvs.set("RetryIntervalMultiplier", new SimpleValueSupport(SimpleMetaType.STRING, control.getRetryIntervalMultiplier()));
      cvs.set("ReconnectAttempts", new SimpleValueSupport(SimpleMetaType.STRING, control.getReconnectAttempts()));
      cvs.set("ScheduledThreadPoolMaxSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getScheduledThreadPoolMaxSize()));
      cvs.set("ThreadPoolMaxSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getThreadPoolMaxSize()));
      cvs.set("GroupID", new SimpleValueSupport(SimpleMetaType.STRING, control.getGroupID()));
      cvs.set("InitialMessagePacketSize", new SimpleValueSupport(SimpleMetaType.STRING, control.getInitialMessagePacketSize()));
      cvs.set("UseGlobalPools", new SimpleValueSupport(SimpleMetaType.STRING, control.isUseGlobalPools()));
      cvs.set("RetryInterval", new SimpleValueSupport(SimpleMetaType.STRING, control.getRetryInterval()));
      cvs.set("ConnectionLoadBalancingPolicyClassName", new SimpleValueSupport(SimpleMetaType.STRING, control.getConnectionLoadBalancingPolicyClassName()));
      return cvs;
   }

   @Override
   public ConnectionFactoryControl unwrapMetaValue(MetaValue metaValue)
   {
      return null;
   }

   @Override
   public Type mapToType()
   {
      return ConnectionFactoryControl.class;
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   private String getJndiString(String[] array)
   {
      StringBuffer sb = new StringBuffer();
      for (String o : array)
      {
         if (sb.length() > 0)
         {
            sb.append(",");
         }
         sb.append(o);
      }
      return sb.toString();
   }
}
