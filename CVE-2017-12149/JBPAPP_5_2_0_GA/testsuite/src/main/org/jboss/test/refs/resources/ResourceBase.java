/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import javax.ejb.EJBContext;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.omg.CORBA.ORB;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public abstract class ResourceBase
   implements ResourceIF
{
   abstract protected EJBContext getEJBContext();
   
   abstract protected UserTransaction getUserTransaction();
   abstract protected String getUserTransactionName();
   
   abstract protected DataSource getDataSource();
   abstract protected String getDataSourceName();

   abstract protected DataSource getDataSource2();
   abstract protected String getDataSource2Name();

   abstract protected javax.mail.Session getMailSession();
   abstract protected String getMailSessionName();

   abstract protected URL getUrl();
   abstract protected String getUrlName();

   abstract protected QueueConnectionFactory getQueueConnectionFactory();
   abstract protected String getQueueConnectionFactoryName();

   abstract protected TopicConnectionFactory getTopicConnectionFactory();
   abstract protected String getTopicConnectionFactoryName();
   
   abstract protected ConnectionFactory getConnectionFactoryQ();
   abstract protected String getConnectionFactoryQName();

   abstract protected ConnectionFactory getConnectionFactoryT();
   abstract protected String getConnectionFactoryTName();
   
   abstract protected Queue getQueue();
   abstract protected String getQueueName();
   
   abstract protected Topic getTopic();
   abstract protected String getTopicName();
   
   abstract protected ORB getOrb();
   abstract protected String getOrbName();

   public void testConnectionFactoryQ() throws Exception
   {
      ConnectionFactory cf = this.getConnectionFactoryQ();
   }

   public void testConnectionFactoryT() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testDataSource() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testDataSource2() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testEJBContext() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testMailSession() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testOrb() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testQueue() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testQueueConnectionFactory() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testTopic() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testTopicConnectionFactory() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testUrl() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

   public void testUserTransaction() throws Exception
   {
      // TODO Auto-generated method stub
      
   }

}
