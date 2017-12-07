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
package org.jboss.test.ejb.lifecycle.test;

import java.util.Collections;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.jboss.ejb.Container;
import org.jboss.test.invokers.test.MultiInvokersUnitTestCase;

/**
 * LifeCycleTestCase based on the testCase
 * @see {@linkplain MultiInvokersUnitTestCase} 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 105321 $
 */
public class InvokersLifeCycleUnitTestCase extends AbstractLifeCycleTestWrapper
{
   /** The package */
   private static final String PACKAGE = "invokers.jar";

   /** The mdb jndi name */
   private String mdbName;
   
   /** The SimpleBMP jmxName */
   private static final String simpleBMPName = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=SimpleBMP";
   
   /** The statelessSession jmxName */
   private static final String statelessSessionName = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=StatelessSession";
   
   /** The businessSession jmxName */
   private static final String businessSessionname = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=BusinessSession";
   
   public InvokersLifeCycleUnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      mdbName = getMDBName("local/JMSGatewayMD");
      assertNotNull(mdbName);
   }

   public void testRestartMDBContainer() throws Exception
   {
      String name = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=" + mdbName;
      restart(name);
   }
   
   public void testRestartMDBInvokers() throws Exception
   {
      String invokerName = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=" +  mdbName + ",plugin=invoker,binding=message-driven-bean";
      restart(invokerName);
   }
   
   public void testRestartMDBPool() throws Exception
   {
      String poolJMXName = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=" +  mdbName + ",plugin=pool";
      restart(poolJMXName);
   }

//   TODO: remove the test after merge as it doesn't make sense
//   public void testRestartQueues() throws Exception
//   {
//      String[] queues = new String[] {
//            "jboss.mq.destination:service=Queue,name=A",
//            "jboss.mq.destination:service=Queue,name=B",
//            "jboss.mq.destination:service=Queue,name=C",
//            "jboss.mq.destination:service=Queue,name=D",
//            "jboss.mq.destination:service=Queue,name=ex" };
//      restart(queues);
//   }
   
   public void testRestartBMPContainer() throws Exception
   {
      restart(simpleBMPName);
   }
   
   public void testRestartBMPPool() throws Exception
   {
      String poolName = simpleBMPName + ",plugin=pool";
      restart(poolName);
   }
   
   public void testRestartBMPCache() throws Exception
   {
      String cacheName = simpleBMPName + ",plugin=cache";
      restart(cacheName);
   }
   
   public void testRestartStatelessSessionContainer() throws Exception
   {
      restart(statelessSessionName);
   }
   
   public void testRestartStatelessSessionPool() throws Exception
   {
      String poolName = statelessSessionName + ",plugin=pool";
      restart(poolName);
   }
   
   public void testRestartBusinessSessionContainer() throws Exception
   {
      restart(businessSessionname);
   }
   
   public void testRestartBusinessSessionPool() throws Exception
   {
      String poolName = businessSessionname + ",plugin=pool";
      restart(poolName);
   }
   
   protected Enumeration<TestCase> getTests()
   {
      return getTestCases(MultiInvokersUnitTestCase.class, Collections.singleton("testClientContainer"));
   }

   @Override
   protected String getPackage()
   {
      return PACKAGE;
   }
   
}