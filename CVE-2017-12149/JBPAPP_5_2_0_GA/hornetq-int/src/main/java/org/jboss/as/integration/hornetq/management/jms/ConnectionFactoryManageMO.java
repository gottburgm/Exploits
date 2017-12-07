/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
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

import org.hornetq.api.jms.management.ConnectionFactoryControl;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.management.ManagementService;
import org.jboss.managed.api.annotation.*;
import org.jboss.metatype.api.annotations.MetaMapping;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created Mar 19, 2010
 */
@ManagementObject(componentType = @ManagementComponent(type = "JMSManage", subtype = "ConnectionFactoryManage"),
    properties = ManagementProperties.EXPLICIT, isRuntime = true)
public class ConnectionFactoryManageMO extends JMSManageMO
{
   public ConnectionFactoryManageMO(HornetQServer server)
   {
      super(server);
   }

   @ManagementOperation(name = "deleteConnectionFactory", description = "returns the JMS Connection Factory configuration",
       params = {@ManagementParameter(name = "name", description = "the connection factory name")})
   public void deleteConnectionFactory(String name) throws Exception
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      jmsServerControl.destroyConnectionFactory(name);
   }

   @ManagementOperation(name = "getConfiguration", description = "returns the JMS Connection Factory configuration",
       params = {@ManagementParameter(name = "name", description = "the connection factory name")})
   @MetaMapping(value = ConnectionFactoryMapper.class)
   public ConnectionFactoryControl getConfiguration(String name)
   {
      return (ConnectionFactoryControl) getManagementService().getResource("jms.connectionfactory." + name);
   }

   @ManagementOperation(name = "getMeasurements", description = "gets a connection factories",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "names", description = "the measurement names")})
   public String[] getMeasurements(String name, String[] names) throws Exception
   {
      ManagementService managementService = getManagementService();
      ConnectionFactoryControl control = (ConnectionFactoryControl) managementService.getResource("jms.connectionfactory." + name);
      String[] val = new String[names.length];
      for (int i = 0, valLength = val.length; i < valLength; i++)
      {
         Object o = control.getClass().getMethod(names[i]).invoke(control);
         if(o instanceof Object[])
         {
            val[i] = coomaSeparatedString((Object[]) o);
         }
         else
         {
            val[i] = o.toString();
         }
      }
      return val;
   }


   @ManagementOperation(name = "getConnectionFactories", description = "returns the JMS Connection Factories")
   public String[] getJMSConnectionFactories()
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      if(jmsServerControl == null)
      {
         return new String[]{};
      }
      else
      {
         return jmsServerControl.getConnectionFactoryNames();
      }
   }

   @ManagementOperation(name = "createConnectionFactory", description = "creates a connection factories",
       params = {
           @ManagementParameter(name = "name", description = "the connection factory name"),
           @ManagementParameter(name = "ha", description = "does the connection factory support high availibility"),
           @ManagementParameter(name = "useDiscovery", description = "should we use a discovery group configuration or a connector configuration"),
           @ManagementParameter(name = "cfType", description = "the type of connection factory"),
           @ManagementParameter(name = "connectorNames", description = "comma-separated list of connectors to uses"),
           @ManagementParameter(name = "bindings", description = "comma-separated list of JNDI bindings"),
           @ManagementParameter(name = "clientId", description = "the client id"),
           @ManagementParameter(name = "dupsOkBatchSize", description = "the batch size for DUPS_OK acknowledge mode"),
           @ManagementParameter(name = "transactionBatchSize", description = "the transaction batch size"),
           @ManagementParameter(name = "clientFailureCheckPeriod", description = "the client failure check period"),
           @ManagementParameter(name = "connectionTTL", description = "the connection time to live"),
           @ManagementParameter(name = "callTimeout", description = "the remote call timeout"),
           @ManagementParameter(name = "consumerWindowSize", description = "the consumer window size"),
           @ManagementParameter(name = "confirmationWindowSize", description = "the confirmation window size"),
           @ManagementParameter(name = "producerMaxRate", description = "the produxer max rate"),
           @ManagementParameter(name = "producerWindowSize", description = "the producer window size"),
           @ManagementParameter(name = "cacheLargeMessageClient", description = "do we cache large messages on the client"),
           @ManagementParameter(name = "minLargeMessageSize", description = "the minimum large message size"),
           @ManagementParameter(name = "blockOnNonDurableSend", description = "do we block on non durable send"),
           @ManagementParameter(name = "blockOnAcknowledge", description = "do we block on acknowledge"),
           @ManagementParameter(name = "blockOnDurableSend", description = "do we block on durable send"),
           @ManagementParameter(name = "autoGroup", description = "do we use autogroup"),
           @ManagementParameter(name = "preAcknowledge", description = "do we pre acknowledge messages"),
           @ManagementParameter(name = "maxRetryInterval", description = "the max retry interval"),
           @ManagementParameter(name = "retryIntervalMultiplier", description = "the max retry interval multiplier"),
           @ManagementParameter(name = "reconnectAttempts", description = "the reconnect attempts"),
           @ManagementParameter(name = "scheduledThreadPoolMaxSize", description = "the pool size for scheduled threads"),
           @ManagementParameter(name = "threadPoolMaxSize", description = "the pool size for threads"),
           @ManagementParameter(name = "groupId", description = "the group id"),
           @ManagementParameter(name = "initialMessagePacketSize", description = "the initial message packet size"),
           @ManagementParameter(name = "useGlobalPools", description = "do we use global pools"),
           @ManagementParameter(name = "retryInterval", description = "the retry interval"),
           @ManagementParameter(name = "connectionLoadBalancingPolicyClassName", description = "the load balancing class")})
   public void createConnectionFactory(final String name,
                                       final boolean ha,
                                       final boolean useDiscovery,
                                       final int cfType,
                                        final String connectorNames,
                                        String bindings,
                                        String clientId,
                                        int dupsOkBatchSize,
                                        int transactionBatchSize,
                                        long clientFailureCheckPeriod,
                                        long connectionTTL,
                                        long callTimeout,
                                        int consumerWindowSize,
                                        int confirmationWindowSize,
                                        int producerMaxRate,
                                        int producerWindowSize,
                                        boolean cacheLargeMessageClient,
                                        int minLargeMessageSize,
                                        boolean blockOnNonDurableSend,
                                        boolean blockOnAcknowledge,
                                        boolean blockOnDurableSend,
                                        boolean autoGroup,
                                        boolean preAcknowledge,
                                        long maxRetryInterval,
                                        double retryIntervalMultiplier,
                                        int reconnectAttempts,
                                        int scheduledThreadPoolMaxSize,
                                        int threadPoolMaxSize,
                                        String groupId,
                                        int initialMessagePacketSize,
                                        boolean useGlobalPools,
                                        long retryInterval,
                                        String connectionLoadBalancingPolicyClassName)
         throws Exception
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      jmsServerControl.createConnectionFactory(name, ha, useDiscovery, cfType, connectorNames, bindings);

      ManagementService managementService = getManagementService();
      ConnectionFactoryControl control = (ConnectionFactoryControl) managementService.getResource("jms.connectionfactory." + name);
      control.setClientID(clientId);
      control.setDupsOKBatchSize(dupsOkBatchSize);
      control.setTransactionBatchSize(transactionBatchSize);
      control.setClientFailureCheckPeriod(clientFailureCheckPeriod);
      control.setConnectionTTL(connectionTTL);
      control.setCallTimeout(callTimeout);
      control.setConsumerWindowSize(consumerWindowSize);
      control.setConfirmationWindowSize(confirmationWindowSize);
      control.setProducerMaxRate(producerMaxRate);
      control.setProducerWindowSize(producerWindowSize);
      control.setCacheLargeMessagesClient(cacheLargeMessageClient);
      control.setMinLargeMessageSize(minLargeMessageSize);
      control.setBlockOnDurableSend(blockOnNonDurableSend);
      control.setBlockOnAcknowledge(blockOnAcknowledge);
      control.setBlockOnDurableSend(blockOnDurableSend);
      control.setAutoGroup(autoGroup);
      control.setPreAcknowledge(preAcknowledge);
      control.setMaxRetryInterval(maxRetryInterval);
      control.setRetryIntervalMultiplier(retryIntervalMultiplier);
      control.setReconnectAttempts(reconnectAttempts);
      control.setScheduledThreadPoolMaxSize(scheduledThreadPoolMaxSize);
      control.setThreadPoolMaxSize(threadPoolMaxSize);
      control.setGroupID(groupId);
      // TODO: figure out what to do with initialPacketSize as it's not really being applied to the connection factories
      // control.setInitialMessagePacketSize(initialMessagePacketSize);
      control.setUseGlobalPools(useGlobalPools);
      control.setRetryInterval(retryInterval);
      control.setConnectionLoadBalancingPolicyClassName(connectionLoadBalancingPolicyClassName);
   }

   @ManagementOperation(name = "createConnectionFactory", description = "creates a connection factories",
       params = {
           @ManagementParameter(name = "name", description = "the connection factory name"),
           @ManagementParameter(name = "ha", description = "does the connection factory support high availibility"),
           @ManagementParameter(name = "clientId", description = "the client id"),
           @ManagementParameter(name = "dupsOkBatchSize", description = "the batch size for DUPS_OK acknowledge mode"),
           @ManagementParameter(name = "transactionBatchSize", description = "the transaction batch size"),
           @ManagementParameter(name = "clientFailureCheckPeriod", description = "the client failure check period"),
           @ManagementParameter(name = "connectionTTL", description = "the connection time to live"),
           @ManagementParameter(name = "callTimeout", description = "the remote call timeout"),
           @ManagementParameter(name = "consumerWindowSize", description = "the consumer window size"),
           @ManagementParameter(name = "confirmationWindowSize", description = "the confirmation window size"),
           @ManagementParameter(name = "producerMaxRate", description = "the produxer max rate"),
           @ManagementParameter(name = "producerWindowSize", description = "the producer window size"),
           @ManagementParameter(name = "cacheLargeMessageClient", description = "do we cache large messages on the client"),
           @ManagementParameter(name = "minLargeMessageSize", description = "the minimum large message size"),
           @ManagementParameter(name = "blockOnNonDurableSend", description = "do we block on non durable send"),
           @ManagementParameter(name = "blockOnAcknowledge", description = "do we block on acknowledge"),
           @ManagementParameter(name = "blockOnDurableSend", description = "do we block on durable send"),
           @ManagementParameter(name = "autoGroup", description = "do we use autogroup"),
           @ManagementParameter(name = "preAcknowledge", description = "do we pre acknowledge messages"),
           @ManagementParameter(name = "maxRetryInterval", description = "the max retry interval"),
           @ManagementParameter(name = "retryIntervalMultiplier", description = "the max retry interval multiplier"),
           @ManagementParameter(name = "reconnectAttempts", description = "the reconnect attempts"),
           @ManagementParameter(name = "scheduledThreadPoolMaxSize", description = "the pool size for scheduled threads"),
           @ManagementParameter(name = "threadPoolMaxSize", description = "the pool size for threads"),
           @ManagementParameter(name = "groupId", description = "the group id"),
           @ManagementParameter(name = "initialMessagePacketSize", description = "the initial message packet size"),
           @ManagementParameter(name = "useGlobalPools", description = "do we use global pools"),
           @ManagementParameter(name = "retryInterval", description = "the retry interval"),
           @ManagementParameter(name = "connectionLoadBalancingPolicyClassName", description = "the load balancing class")})
   public void updateConnectionFactory(String name,
                                        String clientId,
                                        int dupsOkBatchSize,
                                        int transactionBatchSize,
                                        long clientFailureCheckPeriod,
                                        long connectionTTL,
                                        long callTimeout,
                                        int consumerWindowSize,
                                        int confirmationWindowSize,
                                        int producerMaxRate,
                                        int producerWindowSize,
                                        boolean cacheLargeMessageClient,
                                        int minLargeMessageSize,
                                        boolean blockOnNonDurableSend,
                                        boolean blockOnAcknowledge,
                                        boolean blockOnDurableSend,
                                        boolean autoGroup,
                                        boolean preAcknowledge,
                                        long maxRetryInterval,
                                        double retryIntervalMultiplier,
                                        int reconnectAttempts,
                                        int scheduledThreadPoolMaxSize,
                                        int threadPoolMaxSize,
                                        String groupId,
                                        int initialMessagePacketSize,
                                        boolean useGlobalPools,
                                        long retryInterval,
                                        String connectionLoadBalancingPolicyClassName)
         throws Exception
   {
      ManagementService managementService = getManagementService();
      ConnectionFactoryControl control = (ConnectionFactoryControl) managementService.getResource("jms.connectionfactory." + name);
      
      control.setClientID(clientId);
      control.setDupsOKBatchSize(dupsOkBatchSize);
      control.setTransactionBatchSize(transactionBatchSize);
      control.setClientFailureCheckPeriod(clientFailureCheckPeriod);
      control.setConnectionTTL(connectionTTL);
      control.setCallTimeout(callTimeout);
      control.setConsumerWindowSize(consumerWindowSize);
      control.setConfirmationWindowSize(confirmationWindowSize);
      control.setProducerMaxRate(producerMaxRate);
      control.setProducerWindowSize(producerWindowSize);
      control.setCacheLargeMessagesClient(cacheLargeMessageClient);
      control.setMinLargeMessageSize(minLargeMessageSize);
      control.setBlockOnDurableSend(blockOnNonDurableSend);
      control.setBlockOnAcknowledge(blockOnAcknowledge);
      control.setBlockOnDurableSend(blockOnDurableSend);
      control.setAutoGroup(autoGroup);
      control.setPreAcknowledge(preAcknowledge);
      control.setMaxRetryInterval(maxRetryInterval);
      control.setRetryIntervalMultiplier(retryIntervalMultiplier);
      control.setReconnectAttempts(reconnectAttempts);
      control.setScheduledThreadPoolMaxSize(scheduledThreadPoolMaxSize);
      control.setThreadPoolMaxSize(threadPoolMaxSize);
      control.setGroupID(groupId);
      // TODO: figure out what to do with initialPacketSize as it's not really being applied to the connection factories
      // control.setInitialMessagePacketSize(initialMessagePacketSize);
      control.setUseGlobalPools(useGlobalPools);
      control.setRetryInterval(retryInterval);
      control.setConnectionLoadBalancingPolicyClassName(connectionLoadBalancingPolicyClassName);
   }
}
