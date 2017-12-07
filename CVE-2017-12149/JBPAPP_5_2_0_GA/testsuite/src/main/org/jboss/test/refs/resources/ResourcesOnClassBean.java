/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.refs.resources;

import java.net.URL;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.annotation.Resources;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.mail.Session;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.jboss.test.refs.common.ServiceLocator;
import org.omg.CORBA.ORB;

/**
 * An ejb that injects all resource types using annotations on the class
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@Stateless(name = "ResourcesOnClassBean")
@Remote( { ResourceIF.class })
@TransactionManagement(TransactionManagementType.BEAN)
@Resources( {
      @Resource(description = "user transaction", name = "myUserTransaction", type = UserTransaction.class),
      @Resource(name = "dataSource", type = DataSource.class, shareable = true, authenticationType = AuthenticationType.CONTAINER, description = "<resource-ref>"),
      @Resource(name = "myDataSource2", type = DataSource.class, authenticationType = AuthenticationType.CONTAINER),
      @Resource(name = "mailSession", type = Session.class),
      @Resource(name = "url", type = URL.class),
      @Resource(name = "queueConnectionFactory", type = QueueConnectionFactory.class),
      @Resource(name = "topicConnectionFactory", type = TopicConnectionFactory.class),
      @Resource(name = "connectionFactoryQ", type = ConnectionFactory.class),
      @Resource(name = "connectionFactoryT", type = ConnectionFactory.class),
      @Resource(name = "queue", type = Queue.class),
      @Resource(name = "topic", type = Topic.class),
      @Resource(name = "myOrb", type = ORB.class, description = "corba orb", shareable = false) })
public class ResourcesOnClassBean extends ResourceBase
   implements ResourceIF
{
   @Resource(name = "sessionContext", description = "session context", type = SessionContext.class)
   private SessionContext sessionContext;

   protected String getUserTransactionName()
   {
      return "myUserTransaction";
   }

   protected String getDataSourceName()
   {
      return "dataSource";
   }

   protected String getDataSource2Name()
   {
      return "myDataSource2";
   }

   protected String getMailSessionName()
   {
      return "mailSession";
   }

   protected String getUrlName()
   {
      return "url";
   }

   protected String getQueueConnectionFactoryName()
   {
      return "queueConnectionFactory";
   }

   protected String getTopicConnectionFactoryName()
   {
      return "topicConnectionFactory";
   }

   protected String getConnectionFactoryQName()
   {
      return "connectionFactoryQ";
   }

   protected String getConnectionFactoryTName()
   {
      return "connectionFactoryT";
   }

   protected String getTopicName()
   {
      return "topic";
   }

   protected String getQueueName()
   {
      return "queue";
   }

   protected String getOrbName()
   {
      return "myOrb";
   }

   public ResourcesOnClassBean()
   {
   }

   public void remove()
   {
   }

   protected javax.ejb.EJBContext getEJBContext()
   {
      return sessionContext;
   }

   protected DataSource getDataSource()
   {
      return (DataSource) getEJBContext().lookup(getDataSourceName());
   }

   protected DataSource getDataSource2()
   {
      return (DataSource) getEJBContext().lookup(getDataSource2Name());
   }

   protected javax.mail.Session getMailSession()
   {
      return (Session) getEJBContext().lookup(getMailSessionName());
   }

   protected URL getUrl()
   {
      return (URL) getEJBContext().lookup(getUrlName());
   }

   protected QueueConnectionFactory getQueueConnectionFactory()
   {
      return (QueueConnectionFactory) getEJBContext().lookup(
            getQueueConnectionFactoryName());
   }

   protected TopicConnectionFactory getTopicConnectionFactory()
   {
      return (TopicConnectionFactory) getEJBContext().lookup(
            getTopicConnectionFactoryName());
   }

   protected ConnectionFactory getConnectionFactoryQ()
   {
      return (ConnectionFactory) getEJBContext().lookup(
            getConnectionFactoryQName());
   }

   protected ConnectionFactory getConnectionFactoryT()
   {
      return (ConnectionFactory) getEJBContext().lookup(
            getConnectionFactoryTName());
   }

   protected Queue getQueue()
   {
      return (Queue) getEJBContext().lookup(getQueueName());
   }

   protected Topic getTopic()
   {
      return (Topic) getEJBContext().lookup(getTopicName());
   }

   protected javax.transaction.UserTransaction getUserTransaction()
   {
      return getEJBContext().getUserTransaction();
   }

   protected ORB getOrb()
   {
      try
      {
         return (ORB) ServiceLocator.lookup("java:comp/ORB");
      }
      catch (NamingException e)
      {
         e.printStackTrace();
      }
      return null;
   }

}
