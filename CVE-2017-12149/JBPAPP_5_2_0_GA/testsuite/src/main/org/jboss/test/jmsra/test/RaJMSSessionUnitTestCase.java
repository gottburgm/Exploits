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
package org.jboss.test.jmsra.test;

import javax.jms.*;
import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.client.test.AppClientUnitTestCase;

import org.jboss.test.jmsra.bean.*;
import org.jboss.test.util.jms.JMSDestinationsUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for jmsra.
 *
 * @author <a href="mailto:Adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 105321 $
 */

public class RaJMSSessionUnitTestCase extends JBossTestCase
{
   public RaJMSSessionUnitTestCase(String name)
   {
      super(name);
   }

   public void testSendToQueueAndTopic()
      throws Exception
   {
      JMSSessionHome home = (JMSSessionHome) getInitialContext().lookup("JMSSession");
      JMSSession session = home.create();
      session.sendToQueueAndTopic();
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new JBossTestSetup(new TestSuite(RaJMSSessionUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy ("jmsra.jar");
         }
         protected void tearDown() throws Exception
         {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            undeploy ("jmsra.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      });

      return suite;
   }
}





