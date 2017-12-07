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
package org.jboss.test.invokers.test;

import java.util.ArrayList;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.proxy.IClientContainer;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.test.BmpUnitTestCase;
import org.jboss.test.invokers.interfaces.SimpleBMP;
import org.jboss.test.invokers.interfaces.SimpleBMPHome;
import org.jboss.test.invokers.interfaces.StatelessSession;
import org.jboss.test.invokers.interfaces.StatelessSessionHome;
import org.jboss.test.invokers.interfaces.BusinessObjectHome;
import org.jboss.test.invokers.interfaces.BusinessObject;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Test use of multiple invokers per container
 *
 * @author    bill@burkecentral.com
 * @version   $Revision: 105321 $
 */
public class MultiInvokersUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the CustomSocketsUnitTestCase object
    *
    * @param name  Description of Parameter
    */
   public MultiInvokersUnitTestCase(String name)
   {
      super(name);
   }


   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testMultiInvokers() throws Exception
   {
      InitialContext ctx = new InitialContext();
      SimpleBMPHome home1 = (SimpleBMPHome)ctx.lookup("SimpleBMP");
      SimpleBMPHome home2 = (SimpleBMPHome)ctx.lookup("CompressionSimpleBMP");

      SimpleBMP bmp1 = home1.create(1, "bill");
      SimpleBMP bmp2 = home2.findByPrimaryKey(new Integer(1)); // should find it.

      getLog().debug("");
      getLog().debug("bmp1 name: " + bmp1.getName());
      getLog().debug("bmp2 name: " + bmp2.getName());
      getLog().debug("setting name to burke");
      bmp1.setName("burke");
      getLog().debug("bmp1 name: " + bmp1.getName());
      getLog().debug("bmp2 name: " + bmp2.getName());
      assertTrue("bmp1 " + bmp1.getName() + "  == bmp2 " + bmp2.getName(), bmp1.getName().equals(bmp2.getName()));

      StatelessSessionHome shome1 = (StatelessSessionHome)ctx.lookup("StatelessSession");
      StatelessSessionHome shome2 = (StatelessSessionHome)ctx.lookup("CompressionStatelessSession");
      StatelessSession ss1 = shome1.create();
      StatelessSession ss2 = shome2.create();

      ss1.getBMP(1);
      ss2.getBMP(1);
      
   }

   /** Use the IClientContainer view of the proxy to install a custom
    * InvokerInterceptor which routes requests to either the server side
    * selected transport for the BusinessSession, or an mdb depending
    * on the method invoked.
    * 
    * @throws Exception
    */ 
   public void testClientContainer() throws Exception
   {
      log.info("+++ testClientContainer");
      InitialContext ctx = new InitialContext();
      BusinessObjectHome home = (BusinessObjectHome)ctx.lookup("BusinessSession");
      // Check the IClientContainer interface
      IClientContainer container = (IClientContainer) home;
      ArrayList interceptors = container.getInterceptors();
      for(int n = 0; n < interceptors.size(); n ++)
      {
         log.info(interceptors.get(n));
      }
      BusinessObject bean = home.create();
      container = (IClientContainer) bean;
      interceptors = container.getInterceptors();
      for(int n = 0; n < interceptors.size(); n ++)
      {
         log.info(interceptors.get(n));
      }
      // Replace the default InvokerInterceptor
      int last = interceptors.size() - 1;
      interceptors.set(last, new InvokerInterceptor());
      container.setInterceptors(interceptors);

      // Invoke over the rpc transport
      bean.doSomething();
      // Invoker over the jms transport
      String reply = bean.doSomethingSlowly("arg1", "arg2");
      assertTrue("Reply is decorated with viaJMSGatewayMDB",
         reply.indexOf("viaJMSGatewayMDB") > 0 );
      // Remove the bean to close the jms resources
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(MultiInvokersUnitTestCase.class))
      {
         public void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy("invokers.jar");
            
         }
         
         public void tearDown() throws Exception
         {
            undeploy("invokers.jar");
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();
         }
      };

   }

}
