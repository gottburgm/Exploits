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
package org.jboss.test.messagedriven.test;

import org.jboss.test.messagedriven.support.ActivateOperation;
import org.jboss.test.messagedriven.support.CheckJMSDestinationOperation;
import org.jboss.test.messagedriven.support.CheckMessageIDOperation;
import org.jboss.test.messagedriven.support.CheckMessageSizeOperation;
import org.jboss.test.messagedriven.support.DeactivateOperation;
import org.jboss.test.messagedriven.support.JMSContainerInvokerSimpleMessageDrivenUnitTest;
import org.jboss.test.messagedriven.support.Operation;
import org.jboss.test.messagedriven.support.SendMessageOperation;

/**
 * Tests of ejb 2.1 using the JMSContainerInvoker
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class JMSContainerInvokerTopicMessageDrivenUnitTestCase extends JMSContainerInvokerSimpleMessageDrivenUnitTest
{
   public JMSContainerInvokerTopicMessageDrivenUnitTestCase(String name)
   {
      super(name, testTopic, testTopicProps);
   }

   public String getMDBDeployment()
   {
      return "jmscontainerinvoker.jar";
   }

   public Operation[] getDeliveryActiveOperations() throws Exception
   {
      return new Operation[]
      {
         new SendMessageOperation(this, "1"),
         new CheckMessageSizeOperation(this, 0, 5000),
         new ActivateOperation(this, mdbInvoker),
         new CheckMessageSizeOperation(this, 0, 5000),
         new SendMessageOperation(this, "1"),
         new CheckMessageSizeOperation(this, 1, 5000),
         new CheckJMSDestinationOperation(this, 0),
         new CheckMessageIDOperation(this, 0, "1"),
         new DeactivateOperation(this, mdbInvoker),
         new SendMessageOperation(this, "2"),
         new CheckMessageSizeOperation(this, 1, 5000),
         new ActivateOperation(this, mdbInvoker),
         new CheckMessageSizeOperation(this, 1, 5000),
      };
   }
}
