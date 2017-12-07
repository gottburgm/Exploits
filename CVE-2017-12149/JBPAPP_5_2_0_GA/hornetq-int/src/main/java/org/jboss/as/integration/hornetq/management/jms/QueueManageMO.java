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
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.management.ManagementService;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.jboss.managed.api.annotation.*;
import org.jboss.metatype.api.annotations.MetaMapping;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * Created 10-Mar-2010
 */
@ManagementObject(componentType = @ManagementComponent(type = "JMSDestinationManage", subtype = "QueueManage"),
    properties = ManagementProperties.EXPLICIT, isRuntime = true)
public class QueueManageMO extends JMSManageMO
{
   public QueueManageMO(HornetQServer server)
   {
      super(server);
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
           @ManagementParameter(name = "consumeRoles", description = "consume roles for a queue")
       })
   public void createQueue(String name,
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
                           String consumeRoles) throws Exception
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
      jmsServerControl.createQueue(name, jndiName);
      //update security
      hornetQServerControl.addSecuritySettings("jms.queue." + name, sendRoles, consumeRoles, "", "", "", "", "");
   }

   @ManagementOperation(name = "deleteQueue", description = "delete the queue",
       params = {@ManagementParameter(name = "name", description = "the queue name")})
   public void deleteQueue(String name) throws Exception
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      jmsServerControl.destroyQueue(name.replace("jms.queue.", ""));
      HornetQServerControl hornetQServerControl = getHornetQServerControl();
      hornetQServerControl.removeSecuritySettings(name);
      hornetQServerControl.removeAddressSettings(name);
   }

   @ManagementOperation(name = "getQueueConfiguration", description = "Returns a queues configuration",
       params = {@ManagementParameter(name = "name", description = "the queue name")})
   @MetaMapping(value = AddressSettingsMapper.class)
   public Object[] getQueueConfiguration(String name) throws Exception
   {
      Object[] config = new Object[3];
      ManagementService managementService = getManagementService();
      JMSQueueControl control = (JMSQueueControl) managementService.getResource(name);
      JMSQueueConfiguration queueConfiguration = new JMSQueueConfigurationImpl(control.getName(), control.getSelector(), !control.isTemporary(), control.getJNDIBindings());
      config[0] = queueConfiguration;
      HornetQServerControl hornetQServerControl = getHornetQServerControl();
      String addressSettingsAsJSON = hornetQServerControl.getAddressSettingsAsJSON(name);
      config[1] = AddressSettingsInfo.from(addressSettingsAsJSON);
      String rolesAsJSON = hornetQServerControl.getRolesAsJSON(name);
      RoleInfo[] roles = RoleInfo.from(rolesAsJSON);
      config[2] = roles;
      return config;
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
           @ManagementParameter(name = "consumeRoles", description = "consume roles for a queue")
       })
   public void updateQueueConfiguration(String name,
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
                                        String consumeRoles) throws Exception
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
      hornetQServerControl.addSecuritySettings(name, sendRoles, consumeRoles, "", "", "", "", "");
   }

   @ManagementOperation(name = "getQueueMeasurements", description = "updates a queues configuration",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "names", description = "the measurement names")})
   public String[] getQueueMeasurements(String name, String[] names) throws Exception
   {
      ManagementService managementService = getManagementService();
      JMSQueueControl control = (JMSQueueControl) managementService.getResource(name);
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

   @ManagementOperation(name = "invokeQueueOperation", description = "invokes a queues method",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "method", description = "the method"),
           @ManagementParameter(name = "params", description = "the method params")})
   public Object invokeQueueOperation(String name, String method, String[] params, String[] type) throws Exception
   {
      ManagementService managementService = getManagementService();
      JMSQueueControl control = (JMSQueueControl) managementService.getResource(name);
      Class[] classes = getClassTypes(type);
      Method m = control.getClass().getMethod(method, classes);
      return m.invoke(control, getParams(params, classes));
   }

   @ManagementOperation(name = "invokeQueueOperationMessageType", description = "invokes a queues method",
       params = {
           @ManagementParameter(name = "name", description = "the queue name"),
           @ManagementParameter(name = "method", description = "the method"),
           @ManagementParameter(name = "params", description = "the method params")})
   @MetaMapping(value = MessageListMapper.class)
   public List invokeQueueOperationMessageType(String name, String method, String[] params, String[] type) throws Exception
   {
      ManagementService managementService = getManagementService();
      JMSQueueControl control = (JMSQueueControl) managementService.getResource(name);
      Class[] classes = getClassTypes(type);
      Method m = control.getClass().getMethod(method, classes);
      Map<String, Serializable>[] maps = (Map<String, Serializable>[]) m.invoke(control, getParams(params, classes));
      List list = new ArrayList();
      list.addAll(Arrays.asList(maps));
      return list;
   }

   @ManagementOperation(name = "getJMSQueues", description = "returns the JMS Queues")
   public String[] getJMSQueues()
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      if(jmsServerControl == null)
      {
         return new String[]{};
      }
      else
      {
         return jmsServerControl.getQueueNames();
      }
   }
   
   @ManagementOperation(name = "isPaused", description = "is the queue paused?",
	       params = {
           @ManagementParameter(name = "name", description = "the queue name")
   })
   public boolean isPaused(String name) throws Exception
   {
      ManagementService managementService = getManagementService();
      JMSQueueControl control = (JMSQueueControl) managementService.getResource(name);
	   return control.isPaused();
   }
}
