/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb.plugins.jms;

import java.util.ArrayList;

import javax.jms.MessageListener;
import javax.resource.spi.endpoint.MessageEndpoint;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.plugins.inflow.JBossJMSMessageEndpointFactory;
import org.jboss.ejb.plugins.inflow.MessageEndpointInterceptor;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.proxy.ClientMethodInterceptor;
import org.jboss.proxy.TransactionInterceptor;

/**
 * EJBProxyFactory for JMS MessageDrivenBeans
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 66023 $</tt>
 */
public class JMSContainerInvoker extends JBossJMSMessageEndpointFactory
   implements EJBProxyFactory, JMSContainerInvokerMBean
{
   protected void setupProxyParameters() throws DeploymentException
   {
      // Set the interfaces
      interfaces = new Class[] { MessageEndpoint.class, MessageListener.class };
      
      // Set the interceptors
      interceptors = new ArrayList<Class<?>>();
      interceptors.add(ClientMethodInterceptor.class);
      interceptors.add(MessageEndpointInterceptor.class);
      interceptors.add(TransactionInterceptor.class);
      interceptors.add(InvokerInterceptor.class);
   }

   public MessageDrivenMetaData getMetaData()
   {
      MessageDrivenMetaData config =
         (MessageDrivenMetaData) container.getBeanMetaData();
      return config;
   }

   public boolean getCreateJBossMQDestination()
   {
      // TODO getCreateJBossMQDestination
      return false;
   }

   public long getKeepAliveMillis()
   {
      // TODO getKeepAliveMillis
      return 0;
   }

   public int getMaxMessages()
   {
      // TODO getMaxMessages
      return 0;
   }

   public int getMaxPoolSize()
   {
      // TODO getMaxPoolSize
      return 0;
   }

   public int getMinPoolSize()
   {
      // TODO getMinPoolSize
      return 0;
   }

   public void setKeepAliveMillis(long keepAlive)
   {
      // TODO setKeepAliveMillis
      throw new org.jboss.util.NotImplementedException("setKeepAliveMillis");
      
   }

   public void setMaxMessages(int maxMessages)
   {
      // TODO setMaxMessages
      throw new org.jboss.util.NotImplementedException("setMaxMessages");
      
   }

   public void setMaxPoolSize(int maxPoolSize)
   {
      // TODO setMaxPoolSize
      throw new org.jboss.util.NotImplementedException("setMaxPoolSize");
      
   }

   public void setMinPoolSize(int minPoolSize)
   {
      // TODO setMinPoolSize
      throw new org.jboss.util.NotImplementedException("setMinPoolSize");
   }
}
