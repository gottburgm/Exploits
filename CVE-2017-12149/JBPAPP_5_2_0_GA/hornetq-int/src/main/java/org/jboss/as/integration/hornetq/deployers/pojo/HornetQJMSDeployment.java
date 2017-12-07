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
import org.hornetq.jms.server.JMSServerManager;
import org.jboss.as.integration.hornetq.deployers.HornetQJMSRealDeployer;
import org.jboss.logging.Logger;

/**
 * 
 * Objects of this class are created by the MicroContainer through
 * BeanMetadataClass at {@link HornetQJMSRealDeployer}
 * 
 * @see HornetQJMSRealDeployer
 * */
public abstract class HornetQJMSDeployment<T> extends AbstractHornetQControlReferenceDeployment
{

   private static final Logger log = Logger.getLogger(HornetQJMSDeployment.class);

   protected JMSServerManager jmsServer;

   protected ObjectNameBuilder builder;

   protected String name;
   
   protected T config;
   
   public T getConfig()
   {
      return config;
   }
   
   public void setConfig(T config)
   {
      this.config = config;
   }

   public JMSServerManager getJmsServer()
   {
      return jmsServer;
   }

   public void setJmsServer(JMSServerManager jmsServer)
   {
      this.jmsServer = jmsServer;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setBuilder(ObjectNameBuilder builder)
   {
      this.builder = builder;
   }

   public abstract void start() throws Exception;

   public abstract void stop() throws Exception;

}
