/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.integration.hornetq.management.template;

import java.util.List;

import javax.naming.Context;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;

/**
 * A JAXBJMSConfiguration.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="configuration", namespace="urn:hornetq")
@XmlType(propOrder={"queues", "topics"})
public class JAXBJMSConfiguration implements JMSConfiguration, java.io.Serializable
{   
   @XmlElement(name="queue", type=JAXBJMSQueueConfiguration.class)
   private List<JMSQueueConfiguration> queues;
   
   @XmlElement(name="topic", type=JAXBJMSTopicConfiguration.class)
   private List<TopicConfiguration> topics;
   
   public List<ConnectionFactoryConfiguration> getConnectionFactoryConfigurations()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Context getContext()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setContext(Context arg0)
   {
      // TODO Auto-generated method stub
   }

   public List<JMSQueueConfiguration> getQueueConfigurations()
   {
      return queues;
   }

   public void setQueueConfigurations(List<JMSQueueConfiguration> queues)
   {
      this.queues = queues;
   }

   public List<TopicConfiguration> getTopicConfigurations()
   {
      return topics;
   }

   public void setTopicConfigurations(List<TopicConfiguration> topics)
   {
      this.topics = topics;
   }

@Override
public String getDomain() {
	// TODO Auto-generated method stub
	return null;
}
}
