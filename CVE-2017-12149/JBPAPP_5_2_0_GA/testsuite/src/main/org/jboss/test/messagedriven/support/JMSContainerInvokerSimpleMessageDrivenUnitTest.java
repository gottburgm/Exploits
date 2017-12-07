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
package org.jboss.test.messagedriven.support;

import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * A simple test 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public abstract class JMSContainerInvokerSimpleMessageDrivenUnitTest extends SimpleMessageDrivenUnitTest
{
   protected ObjectName persistenceManager = ObjectNameFactory.create("jboss.mq:service=PersistenceManager");
   
   protected ObjectName mdbInvoker = ObjectNameFactory.create("jboss.j2ee:service=EJB,jndiName=TestMDB,plugin=invoker,binding=jms-container-invoker-driven-bean");
   
   public JMSContainerInvokerSimpleMessageDrivenUnitTest(String name, ObjectName jmxDestination, Properties defaultProps)
   {
      super(name, jmxDestination, defaultProps);
   }

   public void testDeliveryActive() throws Exception
   {
      Properties props = (Properties) defaultProps.clone();
      props.put("DeliveryActive", "false");
      runTest(getDeliveryActiveOperations(), props);
   }

   public abstract Operation[] getDeliveryActiveOperations() throws Exception;
}
