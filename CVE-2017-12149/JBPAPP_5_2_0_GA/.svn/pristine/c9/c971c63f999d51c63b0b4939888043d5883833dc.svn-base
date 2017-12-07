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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Queue;
import javax.jms.Topic;

import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb.deployers.CreateDestinationFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertiesMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData;

/**
 * An integration point for the EJB deployers on creating destinations
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="clebert.suconic@jboss.com">Clebert Suconic</a>
 * @version $Revision: 82920 $
 */
public class DestinationFactoryDeployerPlugin implements
      CreateDestinationFactory
{

   private static final Logger log = Logger
         .getLogger(DestinationFactoryDeployerPlugin.class);

   private JMSServerManager jmsManager;

   private AtomicInteger counter = new AtomicInteger(0);

   public JMSServerManager getJmsManager()
   {
      return jmsManager;
   }

   public void setJmsManager(JMSServerManager jmsManager)
   {
      this.jmsManager = jmsManager;
   }

   public Object create(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb)
         throws DeploymentException
   {
      String destinationName = null;
      String destinationType = null;

      ActivationConfigMetaData activationConfig = mdb.getActivationConfig();
      if (activationConfig != null)
      {
         ActivationConfigPropertiesMetaData properties = activationConfig
               .getActivationConfigProperties();
         if (properties != null)
         {
            destinationName = getActivationConfigProperty(properties,
                  "destination");
            destinationType = getActivationConfigProperty(properties,
                  "destinationType");
         }
      }
      
      if (destinationName == null)
      {
         destinationName = mdb.getDestinationJndiName();
      }
      
      // TODO message-destination-link?

      log.info("Deploying destination " + destinationName);

      if (destinationName == null || destinationName.trim().length() == 0)
      {
         log.warn("Unable to determine destination for " + mdb.getName());
         return false;
      }
      boolean isTopic = false;
      JMSConfiguration config = new JMSConfigurationImpl();
      
      String noSlashesName = destinationName.substring(destinationName.lastIndexOf('/') + 1);
      

      if (destinationType == null)
      {
         log.warn("Unable to determine destination type for " + mdb.getName());
         return false;
      } else if (destinationType.equals(Queue.class.getName()))
      {
         config.getQueueConfigurations().add(
               new JMSQueueConfigurationImpl(noSlashesName, null, true,
                     "queue/" + noSlashesName, destinationName));
      } else if (destinationType.equals(Topic.class.getName()))
      {
         config.getTopicConfigurations().add(
               new TopicConfigurationImpl(noSlashesName, "topic/"+ noSlashesName, destinationName));
         isTopic = true;
      } else
      {
         log.warn("Unknown destination type '" + destinationType + "' for "
               + mdb.getName());
         return false;
      }

      Set<String> mdbDependencies = mdb.getDepends();
      if (mdbDependencies == null)
      {
         mdbDependencies = new HashSet<String>();
         mdb.setDepends(mdbDependencies);
      }

      try
      {
         if (isTopic)
         {
            mdbDependencies.add(ObjectNameBuilder.DEFAULT.getJMSTopicObjectName(
                  noSlashesName).toString());
         } else
         {
            mdbDependencies.add(ObjectNameBuilder.DEFAULT.getJMSQueueObjectName(
                  noSlashesName).toString());
         }
      } catch (Exception e)
      {
         log.warn(e.getMessage(), e);
      }

      DeploymentFactory.getInstance().deployJMS(unit, config);

      //todo what should we return
      return unit;
   }

   /**
    * Get an activation config property
    * 
    * @param properties
    *           the properties
    * @param name
    *           the name
    * @return the property or null if not found
    */
   protected static String getActivationConfigProperty(
         ActivationConfigPropertiesMetaData properties, String name)
   {
      ActivationConfigPropertyMetaData property = properties.get(name);
      if (property == null)
         return null;
      return property.getValue();
   }

   public Class<?> getOutput()
   {
      return JMSConfiguration.class;
   }

}
