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
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.SessionContext;
import javax.annotation.Resource;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.QueueConnectionFactory;
import javax.jms.ConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.jms.Queue;
import javax.jms.Topic;
import org.omg.CORBA.ORB;

/**
 * An ejb that injects all resource types using annotations on methods
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@Stateless(name = "ResourceOnMethodBean")
@Remote( { ResourceIF.class })
@TransactionManagement(TransactionManagementType.BEAN)
public class ResourceOnMethodBean extends ResourceBase
   implements ResourceIF
{
   private SessionContext sessionContext;

   private UserTransaction userTransaction;

   private DataSource dataSource;

   private DataSource dataSource2;

   private javax.mail.Session mailSession;

   private URL url;

   private QueueConnectionFactory queueConnectionFactory;

   private TopicConnectionFactory topicConnectionFactory;

   private ConnectionFactory connectionFactoryQ;

   private ConnectionFactory connectionFactoryT;

   private Topic topic;

   private Queue queue;

   private ORB orb;

   public ResourceOnMethodBean()
   {
   }

   public void remove()
   {
   }


   protected javax.ejb.EJBContext getEJBContext()
   {
      return sessionContext;
   }

   @Resource(name = "sessionContext", description = "session context", type = SessionContext.class)
   private void setSessionContext(SessionContext sessionContext)
   {
      this.sessionContext = sessionContext;
   }

   protected DataSource getDataSource()
   {
      return dataSource;
   }

   @Resource(name = "dataSource")
   private void setDataSource(DataSource dataSource)
   {
      this.dataSource = dataSource;
   }

   protected String getDataSourceName()
   {
      return "dataSource";
   }

   protected DataSource getDataSource2()
   {
      return dataSource2;
   }

   @Resource(name = "myDataSource2", type = DataSource.class, shareable = true, authenticationType = AuthenticationType.CONTAINER)
   private void setDataSource2(DataSource dataSource2)
   {
      this.dataSource2 = dataSource2;
   }

   protected String getDataSource2Name()
   {
      return "myDataSource2";
   }

   protected javax.mail.Session getMailSession()
   {
      return mailSession;
   }

   @Resource(name = "mailSession")
   private void setMailSession(javax.mail.Session mailSession)
   {
      this.mailSession = mailSession;
   }

   protected String getMailSessionName()
   {
      return "mailSession";
   }

   protected URL getUrl()
   {
      return url;
   }

   @Resource(name = "url")
   private void setUrl(URL url)
   {
      this.url = url;
   }

   protected String getUrlName()
   {
      return "url";
   }

   protected QueueConnectionFactory getQueueConnectionFactory()
   {
      return queueConnectionFactory;
   }

   @Resource(name = "queueConnectionFactory")
   private void setQueueConnectionFactory(
         QueueConnectionFactory queueConnectionFactory)
   {
      this.queueConnectionFactory = queueConnectionFactory;
   }

   protected String getQueueConnectionFactoryName()
   {
      return "queueConnectionFactory";
   }

   protected TopicConnectionFactory getTopicConnectionFactory()
   {
      return topicConnectionFactory;
   }

   @Resource(name = "topicConnectionFactory")
   private void setTopicConnectionFactory(TopicConnectionFactory topicConnectionFactory)
   {
      this.topicConnectionFactory = topicConnectionFactory;
   }

   protected String getTopicConnectionFactoryName()
   {
      return "topicConnectionFactory";
   }

   protected ConnectionFactory getConnectionFactoryT()
   {
      return connectionFactoryT;
   }

   @Resource(name = "connectionFactoryT")
   private void setConnectionFactoryT(ConnectionFactory conn)
   {
      connectionFactoryT = conn;
   }

   protected String getConnectionFactoryTName()
   {
      return "connectionFactoryT";
   }

   protected ConnectionFactory getConnectionFactoryQ()
   {
      return connectionFactoryQ;
   }

   @Resource(name = "connectionFactoryQ")
   private void setConnectionFactoryQ(ConnectionFactory conn)
   {
      connectionFactoryQ = conn;
   }

   protected String getConnectionFactoryQName()
   {
      return "connectionFactoryQ";
   }

   protected Topic getTopic()
   {
      return topic;
   }

   @Resource(name = "topic")
   private void setTopic(Topic topic)
   {
      this.topic = topic;
   }

   protected String getTopicName()
   {
      return "topic";
   }

   protected Queue getQueue()
   {
      return queue;
   }

   @Resource(name = "queue")
   private void setQueue(Queue queue)
   {
      this.queue = queue;
   }

   protected String getQueueName()
   {
      return "queue";
   }

   protected String getUserTransactionName()
   {
      return "myUserTransaction";
   }

   @Resource(description = "user transaction", name = "myUserTransaction", type = UserTransaction.class)
   private void setUserTransaction(UserTransaction ut)
   {
      userTransaction = ut;
   }

   protected javax.transaction.UserTransaction getUserTransaction()
   {
      return userTransaction;
   }


   protected String getOrbName()
   {
      return "myOrb";
   }

   @Resource(name = "myOrb", type = ORB.class, description = "corba orb", shareable = false)
   private void setOrb(ORB orb)
   {
      this.orb = orb;
   }

   protected ORB getOrb()
   {
      return this.orb;
   }

}
