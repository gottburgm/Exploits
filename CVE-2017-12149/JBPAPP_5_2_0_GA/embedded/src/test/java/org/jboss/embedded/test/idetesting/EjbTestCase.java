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
package org.jboss.embedded.test.idetesting;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.DeploymentGroup;
import org.jboss.embedded.test.ejb.DAO;
import org.jboss.embedded.test.ejb.Customer;
import org.jboss.embedded.adapters.JMXKernel;
import org.jboss.deployers.spi.DeploymentException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class EjbTestCase extends TestCase
{
   public EjbTestCase()
   {
      super("BootstrapTestCase");
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(EjbTestCase.class);


      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            if (Bootstrap.getInstance().isStarted()) return;

            try
            {
               Bootstrap.getInstance().bootstrap();
            }
            catch (Exception error)
            {
               throw new RuntimeException("Failed to bootstrap", error);
            }
         }

         protected void tearDown()
         {
         }
      };

      return wrapper;
   }

   private static void outputJNDI()
           throws InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException
   {
      MBeanServer server = getMBeanServer();
      String xml = (String)server.invoke(new ObjectName("jboss:service=JNDIView"), "listXML", null, null);
      System.out.println(xml);
   }

   private static MBeanServer getMBeanServer()
   {
      JMXKernel jmxKernel = (JMXKernel)Bootstrap.getInstance().getKernel().getRegistry().getEntry("JMXKernel").getTarget();
      MBeanServer server = jmxKernel.getMbeanServer();
      return server;
   }


   public void testSimpleEjb() throws Exception
   {
      DeploymentGroup group = Bootstrap.getInstance().createDeploymentGroup();
      group.addClasspath("ejb-test.jar");
      group.process();

      outputJNDI();
      InitialContext ctx = new InitialContext();
      DAO dao = (DAO)ctx.lookup("DAOBean/local");
      Customer cust = dao.createCustomer("Bill");
      cust = dao.findCustomer("Bill");
      assert cust != null;
      assert cust.getName().equals("Bill");

      group.undeploy();


   }

    public void testSimpleEjb2() throws Exception
    {
       DeploymentGroup group = Bootstrap.getInstance().createDeploymentGroup();
       group.addClasspath("ejb-test.jar");
       group.process();

       outputJNDI();
       InitialContext ctx = new InitialContext();
       DAO dao = (DAO)ctx.lookup("DAOBean/local");
       Customer cust = dao.createCustomer("Bill");
       cust = dao.findCustomer("Bill");
       assert cust != null;
       assert cust.getName().equals("Bill");

       group.undeploy();


    }
}
