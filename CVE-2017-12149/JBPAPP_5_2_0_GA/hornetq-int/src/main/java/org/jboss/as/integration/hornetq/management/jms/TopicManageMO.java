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

import org.hornetq.api.core.management.AddressSettingsInfo;
import org.hornetq.api.core.management.HornetQServerControl;
import org.hornetq.api.core.management.RoleInfo;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.api.jms.management.TopicControl;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.management.ManagementService;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import org.jboss.managed.api.annotation.*;
import org.jboss.metatype.api.annotations.MetaMapping;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created: 17-Mar-2010
 */
@ManagementObject(componentType = @ManagementComponent(type = "JMSDestinationManage", subtype = "TopicManage"),
    properties = ManagementProperties.EXPLICIT, isRuntime = true)
public class TopicManageMO extends JMSManageMO
{

   public TopicManageMO(HornetQServer server)
   {
      super(server);
   }

   @ManagementOperation(name = "deleteTopic", description = "delete the topic",
       params = {@ManagementParameter(name = "name", description = "the queue name")})
   public void deleteTopic(String name) throws Exception
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      jmsServerControl.destroyTopic(name.replace("jms.topic.", ""));
      HornetQServerControl hornetQServerControl = getHornetQServerControl();
      hornetQServerControl.removeSecuritySettings(name);
      hornetQServerControl.removeAddressSettings(name);
   }

   @ManagementOperation(name = "getTopicConfiguration", description = "Returns a topics configuration",
       params = {@ManagementParameter(name = "name", description = "the queue name")})
   @MetaMapping(value = AddressSettingsMapper.class)
   public Object[] getTopicConfiguration(String name) throws Exception 
   {
      Object[] config = new Object[3];
      ManagementService managementService = getManagementService();
      TopicControl control = (TopicControl) managementService.getResource(name);
      TopicConfiguration topicConfiguration = new TopicConfigurationImpl(control.getName(), control.getJNDIBindings());
      config[0] = topicConfiguration;
      HornetQServerControl hornetQServerControl = getHornetQServerControl();
      String jsonString = hornetQServerControl.getAddressSettingsAsJSON(name);
      config[1] = AddressSettingsInfo.from(jsonString);
      String rolesAsJSON = hornetQServerControl.getRolesAsJSON(name);
      RoleInfo[] roles = RoleInfo.from(rolesAsJSON);
      config[2] = roles;
      return config;
   }
   @ManagementOperation(name = "createQueue", description = "Creates a new Queue",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "jndiName", description = "The JNDI Name of the queue"),
           @ManagementParameter(name = "dla", description = "Dead Letter Address"),
           @ManagementParameter(name = "expiryAddress", description = "Expiry Address"),
           @ManagementParameter(name = "maxSizeBytes", description = "Max Size of Address"),
           @ManagementParameter(name = "pageSizeBytes", description = "Page Size"),
           @ManagementParameter(name = "pageMaxCacheSize", description = "Max number of pages in the soft memory cache"),
           @ManagementParameter(name = "deliveryAttempts", description = "Max Delivery Attempts"),
           @ManagementParameter(name = "redeliveryDelay", description = "Redelivery Delay"),
           @ManagementParameter(name = "lastValueQueue", description = "Last Value Queue"),
           @ManagementParameter(name = "redistributionDelay", description = "Redistribution Delay"),
           @ManagementParameter(name = "sendToDLAOnNoRoute", description = "Send To DLA on no route"),
           @ManagementParameter(name = "addressFullMessagePolicy", description = "Address Full Message Policy"),
           @ManagementParameter(name = "sendRoles", description = "Send roles for a queue"),
           @ManagementParameter(name = "consumeRoles", description = "consume roles for a queue"),
           @ManagementParameter(name = "createDurableQueueRoles", description = "Send roles for a queue"),
           @ManagementParameter(name = "deleteDurableQueueRoles", description = "consume roles for a queue"),
           @ManagementParameter(name = "createTempQueueRoles", description = "Send roles for a queue"),
           @ManagementParameter(name = "deleteTempQueueRoles", description = "consume roles for a queue")
       })
   public void createTopic(String name,
                           String jndiName,
                           String DLA,
                           String expiryAddress,
                           long maxSizeBytes,
                           int pageSizeBytes,
                           int pageMaxCacheSize,
                           int deliveryAttempts,
                           long redeliveryDelay,
                           boolean lastValueQueue,
                           long redistributionDelay,
                           boolean sendToDLAOnNoRoute,
                           String addressFullMessagePolicy,
                           String sendRoles,
                           String consumeRoles,
                           final String createDurableQueueRoles,
                           final String deleteDurableQueueRoles,
                           final String createTempQueueRoles,
                           final String deleteTempQueueRoles) throws Exception
   {

      //update the address settings
      HornetQServerControl hornetQServerControl = getHornetQServerControl();
      hornetQServerControl.addAddressSettings("jms.queue." + name,
            DLA,
            expiryAddress,
            lastValueQueue,
            deliveryAttempts,
            maxSizeBytes,
            pageSizeBytes,
            pageMaxCacheSize,
            redeliveryDelay,
            redistributionDelay,
            sendToDLAOnNoRoute,
            addressFullMessagePolicy);
      //create the queue
      JMSServerControl jmsServerControl = getJmsServerControl();
      jmsServerControl.createTopic(name, jndiName);
      //update security
      hornetQServerControl.addSecuritySettings(name, sendRoles, consumeRoles, createDurableQueueRoles, deleteDurableQueueRoles, createTempQueueRoles, deleteTempQueueRoles, "");
   }

   @ManagementOperation(name = "updateQueueConfiguration", description = "updates a queues configuration",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "jndiName", description = "The JNDI Name of the queue"),
           @ManagementParameter(name = "dla", description = "Dead Letter Address"),
           @ManagementParameter(name = "expiryAddress", description = "Expiry Address"),
           @ManagementParameter(name = "maxSizeBytes", description = "Max Size of Address"),
           @ManagementParameter(name = "pageSizeBytes", description = "Page Size"),
           @ManagementParameter(name = "pageMaxCacheSize", description = "Max number of pages in the soft memory cache"),
           @ManagementParameter(name = "deliveryAttempts", description = "Max Delivery Attempts"),
           @ManagementParameter(name = "redeliveryDelay", description = "Redelivery Delay"),
           @ManagementParameter(name = "lastValueQueue", description = "Last Value Queue"),
           @ManagementParameter(name = "redistributionDelay", description = "Redistribution Delay"),
           @ManagementParameter(name = "sendToDLAOnNoRoute", description = "Send To DLA on no route"),
           @ManagementParameter(name = "addressFullMessagePolicy", description = "Address Full Message Policy"),
           @ManagementParameter(name = "sendRoles", description = "Send roles for a queue"),
           @ManagementParameter(name = "consumeRoles", description = "consume roles for a queue")              ,
           @ManagementParameter(name = "createDurableQueueRoles", description = "Send roles for a queue"),
           @ManagementParameter(name = "deleteDurableQueueRoles", description = "consume roles for a queue"),
           @ManagementParameter(name = "createTempQueueRoles", description = "Send roles for a queue"),
           @ManagementParameter(name = "deleteTempQueueRoles", description = "consume roles for a queue")
       })
   public void updateTopicConfiguration(String name,
                                        String jndiName,
                                        String dla,
                                        String expiryAddress,
                                        long maxSizeBytes,
                                        int pageSizeBytes,
                                        int pageMaxCacheSize,
                                        int deliveryAttempts,
                                        long redeliveryDelay,
                                        boolean lastValueQueue,
                                        long redistributionDelay,
                                        boolean sendToDLAOnNoRoute,
                                        String addressFullMessagePolicy,
                                        String sendRoles,
                                        String consumeRoles,
                                         final String createDurableQueueRoles,
                                         final String deleteDurableQueueRoles,
                                         final String createTempQueueRoles,
                                         final String deleteTempQueueRoles) throws Exception
   {
      //update the address settings
      HornetQServerControl hornetQServerControl = getHornetQServerControl();
      hornetQServerControl.addAddressSettings(name,
            dla,
            expiryAddress,
            lastValueQueue,
            deliveryAttempts,
            maxSizeBytes,
            pageSizeBytes,
            pageMaxCacheSize,
            redeliveryDelay,
            redistributionDelay,
            sendToDLAOnNoRoute,
            addressFullMessagePolicy);
      //update security
      hornetQServerControl.addSecuritySettings(name, sendRoles, consumeRoles, createDurableQueueRoles, deleteDurableQueueRoles, createTempQueueRoles, deleteTempQueueRoles, "");
   }

   @ManagementOperation(name = "getTopicMeasurements", description = "updates a queues configuration",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "names", description = "the measurement names")})
   public String[] getTopicMeasurements(String name, String[] names) throws Exception
   {
      TopicControl control = (TopicControl) getManagementService().getResource(name);
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

    @ManagementOperation(name = "invokeTopicOperation", description = "invokes a queues method",
       params = {
           @ManagementParameter(name = "name", description = "the topic name"),
           @ManagementParameter(name = "method", description = "the method"),
           @ManagementParameter(name = "params", description = "the method params")})
   public Object invokeTopicOperation(String name, String method, String[] params, String[] type) throws Exception
   {
      TopicControl control = (TopicControl) getManagementService().getResource(name);
      Class[] classes = getClassTypes(type);
      Method m = control.getClass().getMethod(method, classes);
      return m.invoke(control, getParams(params, classes));
   }

   @ManagementOperation(name = "invokeTopicOperationMessageType", description = "invokes a queues method",
       params = {
           @ManagementParameter(name = "name", description = "the topic name"),
           @ManagementParameter(name = "method", description = "the method"),
           @ManagementParameter(name = "params", description = "the method params")})
   @MetaMapping(value = MessageListMapper.class)
   public List invokeTopicOperationMessageType(String name, String method, String[] params, String[] type) throws Exception
   {
      ManagementService managementService = getManagementService();
      TopicControl control = (TopicControl) managementService.getResource(name);
      Class[] classes = getClassTypes(type);
      Method m = control.getClass().getMethod(method, classes);

      Map<String, Serializable>[] maps = (Map<String, Serializable>[]) m.invoke(control, getParams(params, classes));
      List list = new ArrayList();
      list.addAll(Arrays.asList(maps));
      return list;
   }

   @ManagementOperation(name = "invokeTopicOperationSubscriptionType", description = "invokes a queues method",
       params = {
           @ManagementParameter(name = "name", description = "the topic name"),
           @ManagementParameter(name = "method", description = "the method"),
           @ManagementParameter(name = "params", description = "the method params")})
   @MetaMapping(value = SubscriptionInfoMapper.class)
   public Object[] invokeTopicOperationSubscriptionType(String name, String method, String[] params, String[] type) throws Exception
   {
      ManagementService managementService = getManagementService();
      TopicControl control = (TopicControl) managementService.getResource(name);
      Class[] classes = getClassTypes(type);
      Method m = control.getClass().getMethod(method, classes);

      Object[] maps = (Object[]) m.invoke(control, getParams(params, classes));
      return maps;
   }

   @ManagementOperation(name = "getJMSTopics", description = "returns the JMS Topics")
   public String[] getJMSTopics()
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      if(jmsServerControl == null)
      {
         return new String[]{};
      }
      else
      {
         return jmsServerControl.getTopicNames();
      }
   }
}
