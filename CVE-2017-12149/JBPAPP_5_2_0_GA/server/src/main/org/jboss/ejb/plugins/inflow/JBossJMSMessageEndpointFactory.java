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
package org.jboss.ejb.plugins.inflow;

import javax.jms.MessageListener;
import javax.jms.Session;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.ActivationConfigPropertyMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Hacked version of message endpoint factory for backwards compatibility
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a> .
 * @version <tt>$Revision: 86294 $</tt>
 */
public class JBossJMSMessageEndpointFactory  extends JBossMessageEndpointFactory
{
   /** The JBoss resource adapter deployment name */
   protected static String jmsra = "jms-ra.rar";

   @Override
   protected String resolveResourceAdapterName() throws DeploymentException
   {
      // No resource adapter specified assume jms
      String result = super.resolveResourceAdapterName();
      if (result == null)
         result = jmsra;
      return result;
   }
   
   @Override
   protected void resolveMessageListener() throws DeploymentException
   {
      // No messaging type use jms
      if (metaData.getMessagingType() == null)
         messagingTypeClass = MessageListener.class;
      else
         super.resolveMessageListener();
   }

   /**
    * Add activation config properties
    * 
    * @throws DeploymentException for any error
    */
   protected void augmentActivationConfigProperties() throws DeploymentException
   {
      super.augmentActivationConfigProperties();
      
      // Hack for old style deployments (jms)
      if (messagingTypeClass.equals(MessageListener.class))
      {
         checkActivationConfig("destination", metaData.getDestinationJndiName());
         checkActivationConfig("destinationType", metaData.getDestinationType());
         checkActivationConfig("messageSelector", metaData.getMessageSelector());
         if (Session.DUPS_OK_ACKNOWLEDGE == metaData.getAcknowledgeMode())
            checkActivationConfig("acknowledgeMode", "DUPS_OK_ACKNOWLEDGE");
         else
            checkActivationConfig("acknowledgeMode", "AUTO_ACKNOWLEDGE");
         if (MessageDrivenMetaData.DURABLE_SUBSCRIPTION == metaData.getSubscriptionDurability())
            checkActivationConfig("subscriptionDurability", "Durable");
         else
            checkActivationConfig("subscriptionDurability", "NonDurable");
         checkActivationConfig("clientId", metaData.getClientId());
         checkActivationConfig("subscriptionName", metaData.getSubscriptionId());
         
         // Only for JBoss's resource adapter
         if (jmsra.equals(resourceAdapterName))
         {
            checkActivationConfig("user", metaData.getUser());
            checkActivationConfig("password", metaData.getPasswd());
            Element proxyConfig = invokerMetaData.getProxyFactoryConfig();
            checkActivationConfig("maxMessages", MetaData.getOptionalChildContent(proxyConfig, "MaxMessages"));
            checkActivationConfig("minSession", MetaData.getOptionalChildContent(proxyConfig, "MinimumSize"));
            checkActivationConfig("maxSession", MetaData.getOptionalChildContent(proxyConfig, "MaximumSize"));
            checkActivationConfig("keepAlive", MetaData.getOptionalChildContent(proxyConfig, "KeepAliveMillis"));
            Element mdbConfig = MetaData.getOptionalChild(proxyConfig, "MDBConfig");
            if (mdbConfig != null)
            {
               try
               {
                  if ("false".equalsIgnoreCase(MetaData.getElementContent(MetaData.getUniqueChild(mdbConfig, "DeliveryActive"))))
                  {
                     setDeliveryActive(false);
                  }
               }
               catch (Exception ignore)
               {
               }

               checkActivationConfig("reconnectInterval", MetaData.getOptionalChildContent(proxyConfig, "ReconnectIntervalSec"));
               checkActivationConfig("deliveryActive", MetaData.getOptionalChildContent(proxyConfig, "DeliveryActive"));
               checkActivationConfig("providerAdapterJNDI", MetaData.getOptionalChildContent(proxyConfig, "JMSProviderAdapterJNDI"));
               
               Element dlqEl = MetaData.getOptionalChild(mdbConfig, "DLQConfig");
               if (dlqEl != null)
               {
                  checkActivationConfig("useDLQ", "true");
                  checkActivationConfig("DLQJNDIName", MetaData.getElementContent(MetaData.getUniqueChild(dlqEl, "DestinationQueue")));
                  try
                  {
                     checkActivationConfig("DLQMaxResent", MetaData.getElementContent(MetaData.getUniqueChild(dlqEl, "MaxTimesRedelivered")));
                  }
                  catch (Exception ignored)
                  {
                     // backwards comaptibility
                  }
                  
                  // TODO TimeToLive
                  
                  checkActivationConfig("DLQUser", MetaData.getElementContent(MetaData.getOptionalChild(dlqEl, "DLQUser")));
                  checkActivationConfig("DLQPassword", MetaData.getElementContent(MetaData.getOptionalChild(dlqEl, "DLQPassword")));
               }
               else
               {
                  // backwards compatibility - no DLQConfig in MDBConfig means no DLQ
                  checkActivationConfig("useDLQ", "false");
               }
            }
         }
      }
   }   

   /**
    * When the config doesn't exist for a given name adds the value when not null
    * 
    * @param name the name of the config property
    * @param value the value to add
    */
   void checkActivationConfig(String name, String value)
   {
      if (value != null && properties.containsKey(name) == false)
      {
         org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData md = new org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData();
         ActivationConfigPropertyMetaData metaData = new ActivationConfigPropertyMetaData(md);
         md.setName(name);
         md.setValue(value);
         properties.put(name, metaData);
      }
   }
}
