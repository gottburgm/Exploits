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
package org.jboss.as.integration.hornetq.deployers.pojo;

import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.jboss.logging.Logger;

import javax.management.ObjectName;

public class HornetQTopicDeployment extends HornetQJMSDeployment<TopicConfiguration>
{
   private static final Logger log = Logger.getLogger(HornetQTopicDeployment.class);

   @Override
   public void start() throws Exception
   {
      log.debug("Deploying Topic " + config.getName());
      try
      {
         jmsServer.createTopic(false, config.getName(), config.getBindings());
         registerControlReference(builder.getJMSTopicObjectName(config.getName()));
      }
      catch (Exception e)
      {
         log.warn("Error deploying Topic: " + config.getName(), e);
         throw e;
      }
   }

   @Override
   public void stop() throws Exception
   {
      log.debug("Undeploying Topic " + config.getName());
      try
      {
         unregisterControlReference(ObjectNameBuilder.DEFAULT.getJMSTopicObjectName(config.getName()));
         
         jmsServer.removeTopicFromJNDI(config.getName());
      }
      catch (Exception e)
      {
         log.warn("Error destroying Topic: " + config.getName(), e);
      }
   }

}
