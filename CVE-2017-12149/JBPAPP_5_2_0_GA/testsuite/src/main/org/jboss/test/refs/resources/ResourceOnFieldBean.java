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
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.omg.CORBA.ORB;

/**
 * An ejb that injects all resource types using annotations on methods
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@Stateless(name = "ResourceOnFieldBean")
@Remote( { ResourceIF.class })
@TransactionManagement(TransactionManagementType.BEAN)
public class ResourceOnFieldBean extends ResourceBase
   implements ResourceIF
{

   @Resource(name = "session", description = "session context", type = SessionContext.class)
   private SessionContext sessionContext;

   @Resource(description = "user transaction", name = "myUserTransaction", type = UserTransaction.class)
   private UserTransaction ut;

   protected String getUserTransactionName()
   {
      return "myUserTransaction";
   }

   @Resource(name = "dataSource", description = "<resource-ref>")
   private DataSource dataSource;

   protected String getDataSourceName()
   {
      return "dataSource";
   }

   @Resource(name = "myDataSource2", type = DataSource.class, shareable = true, authenticationType = AuthenticationType.CONTAINER)
   private DataSource dataSource2;

   protected String getDataSource2Name()
   {
      return "myDataSource2";
   }

   @Resource(name = "mailSession")
   private javax.mail.Session mailSession;

   protected String getMailSessionName()
   {
      return "mailSession";
   }

   @Resource(name = "url")
   private URL url;

   protected String getUrlName()
   {
      return "url";
   }

   @Resource(name = "queueConnectionFactory")
   private QueueConnectionFactory queueConnectionFactory;

   protected String getQueueConnectionFactoryName()
   {
      return "queueConnectionFactory";
   }

   @Resource(name = "topicConnectionFactory")
   private TopicConnectionFactory topicConnectionFactory;

   protected String getTopicConnectionFactoryName()
   {
      return "topicConnectionFactory";
   }

   @Resource(name = "connectionFactoryQ")
   private ConnectionFactory connectionFactoryQ;

   protected String getConnectionFactoryQName()
   {
      return "connectionFactoryQ";
   }

   protected ConnectionFactory getConnectionFactoryQ()
   {
      return connectionFactoryQ;
   }

   @Resource(name = "connectionFactoryT")
   private ConnectionFactory connectionFactoryT;

   protected String getConnectionFactoryTName()
   {
      return "connectionFactoryT";
   }

   protected ConnectionFactory getConnectionFactoryT()
   {
      return connectionFactoryT;
   }

   @Resource(name = "topic")
   private Topic topic;

   protected String getTopicName()
   {
      return "topic";
   }

   @Resource(name = "queue")
   private Queue queue;

   protected String getQueueName()
   {
      return "queue";
   }

   @Resource(name = "myOrb", type = ORB.class, description = "corba orb", shareable = false)
   private ORB orb;

   protected String getOrbName()
   {
      return "myOrb";
   }

   public ResourceOnFieldBean()
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
      return dataSource;
   }

   protected DataSource getDataSource2()
   {
      return dataSource2;
   }

   protected javax.mail.Session getMailSession()
   {
      return mailSession;
   }

   protected URL getUrl()
   {
      return url;
   }

   protected QueueConnectionFactory getQueueConnectionFactory()
   {
      return queueConnectionFactory;
   }

   protected Queue getQueue()
   {
      return queue;
   }

   protected TopicConnectionFactory getTopicConnectionFactory()
   {
      return topicConnectionFactory;
   }

   protected Topic getTopic()
   {
      return topic;
   }

   protected UserTransaction getUserTransaction()
   {
      return ut;
   }

   protected ORB getOrb()
   {
      return this.orb;
   }

}
