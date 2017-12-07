/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb3.jbpapp3026.unit;

import javax.jms.Queue;
import javax.jms.Topic;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * AutoCreateDestinationForMDBTestCase
 * 
 * Tests that destinations which are set to be auto created (through the use of 
 * "create-destination" on the MDB) are created successfully, when the
 * destination jndi name has forward slashes.
 * 
 * See https://jira.jboss.org/jira/browse/JBPAPP-3026 for details
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class AutoCreateDestinationForMDBTestCase extends JBossTestCase
{

   /**
    * Constructor
    * @param name
    */
   public AutoCreateDestinationForMDBTestCase(String name)
   {
      super(name);
   }

   /**
    * 
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(AutoCreateDestinationForMDBTestCase.class, "createdestination-mdb.jar");
   }
   
   /**
    * Tests that a queue containing forward slashes in it's destination jndi name 
    * is correctly created.
    * 
    * @throws Exception
    */
   public void testQueueCreationForMDB() throws Exception
   {
      // make sure the deployment was deployed successfully
      serverFound();
      // lookup queue
      // successful lookup, in itself should be sufficient enough to prove that 
      // the queue was created by the "create-destination" activation config property
      // of the MDB
      Queue queue = (Queue) this.getInitialContext().lookup("queue/create-destination/testQueue");
      assertNotNull("Queue lookup returned null", queue);
   }
   
   /**
    * Tests that a topic containing forward slashes in it's destination jndi name 
    * is correctly created.
    * 
    * @throws Exception
    */
   public void testTopicCreationForMDB() throws Exception
   {
      // make sure the deployment was deployed successfully
      serverFound();
      // lookup topic
      // successful lookup, in itself should be sufficient enough to prove that 
      // the topic was created by the "create-destination" activation config property
      // of the MDB
      Topic topic = (Topic) this.getInitialContext().lookup("topic/create-destination/testTopic");
      assertNotNull("Topic lookup returned null", topic);
   }
}
