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
package org.jboss.as.integration.hornetq.deployers;

import java.util.Map;

import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.jboss.as.integration.hornetq.deployers.pojo.HornetQConnectionFactoryDeployment;
import org.jboss.as.integration.hornetq.deployers.pojo.HornetQCoreDeployment;
import org.jboss.as.integration.hornetq.deployers.pojo.HornetQQueueDeployment;
import org.jboss.as.integration.hornetq.deployers.pojo.HornetQTopicDeployment;
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.logging.Logger;

/**
 * 
 * @author <mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 *
 */
public class DeploymentFactory
{

   private static final Logger log = Logger.getLogger(DeploymentFactory.class);

   private static DeploymentFactory instance = new DeploymentFactory();
   
   public static DeploymentFactory getInstance()
   {
      return instance;
   }
   
   public void deployJMS(DeploymentUnit unit, JMSConfiguration mainConfig) throws DeploymentException
   {
      
      try
      {
         ObjectNameBuilder builder = ObjectNameBuilder.create(mainConfig.getDomain());
         for (JMSQueueConfiguration config : mainConfig.getQueueConfigurations())
         {
            String name = builder.getJMSQueueObjectName(config.getName()).toString();
            unit.addAttachment(name, createJMSBasicBean(unit, HornetQQueueDeployment.class.getName(), name, config, builder));
         }
   
         for (TopicConfiguration config : mainConfig.getTopicConfigurations())
         {
            String name = builder.getJMSTopicObjectName(config.getName()).toString();
            unit.addAttachment(name, createJMSBasicBean(unit, HornetQTopicDeployment.class.getName(), name, config, builder));
         }
   
         for (ConnectionFactoryConfiguration config : mainConfig.getConnectionFactoryConfigurations())
         {
            String name = mainConfig.getDomain() + ":module=JMS,name=\"" + config.getName() + "\",type=ConnectionFactory";
            unit.addAttachment(name, createJMSBasicBean(unit, HornetQConnectionFactoryDeployment.class.getName(), name, config, builder));
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException (e.getMessage(), e);
      }
   }

   /**
    * @param name
    */
   private static BeanMetaData createJMSBasicBean(DeploymentUnit unit, String configClass, String name, Object config, ObjectNameBuilder objectNameBuilder)
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, configClass);

      builder.addPropertyMetaData("name", name);

      builder.addPropertyMetaData("jmsServer", builder.createInject("JMSServerManager"));

      builder.addPropertyMetaData("builder", objectNameBuilder);
      
      builder.addPropertyMetaData("config", config);

      // runtime control registry
      builder.addPropertyMetaData("kernel", builder.createInject(KernelConstants.KERNEL_NAME));
      
      builder.addPropertyMetaData("mbeanServer", builder.createInject("MBeanServer"));
      
      builder.addDependency("HornetQ.main.config");
      
      for (Map.Entry<String, Object> entry : unit.getAttachments().entrySet())
      {
         if (entry.getValue() instanceof BeanMetaData)
         {
            BeanMetaData checkBuilder = (BeanMetaData)entry.getValue();
            if (checkBuilder.getBean().equals(HornetQCoreDeployment.class.getName()))
            {
               // If there is any core deployment on the same deployment unit, it needs to be deployed first.
               // So we add a dependency here
               builder.addDependency(entry.getKey());
            }
         }
      }
      
      return builder.getBeanMetaData();
   }

   
}

