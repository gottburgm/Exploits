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
package org.jboss.embedded.test.ejb.unit;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.adapters.JMXKernel;
import org.jboss.embedded.DeploymentGroup;
import org.jboss.embedded.test.ejb.DAO;
import org.jboss.embedded.test.ejb.Customer;
import org.jboss.embedded.test.ejb.Secured;
import org.jboss.deployers.spi.DeploymentException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.ejb.EJBAccessException;
import java.util.Hashtable;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class    EjbTestCase extends TestCase
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
            long start = System.currentTimeMillis();
            try
            {
               startupEmbeddedJboss();
            }
            finally
            {
               System.out.println("Bootstrap took " + (System.currentTimeMillis() - start) + " (ms)");
            }

         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      try
      {
         Bootstrap.getInstance().bootstrap();
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException("Failed to bootstrap", e);
      }
   }

   public static void shutdownEmbeddedJboss()
   {
      Bootstrap.getInstance().shutdown();
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

   public void testSecurity() throws Exception
   {
      DeploymentGroup group = Bootstrap.getInstance().createDeploymentGroup();
      group.addClasspath("ejb-test.jar");
      group.process();

      Hashtable env = new Hashtable();
      env.put(Context.SECURITY_PRINCIPAL, "scott");
      env.put(Context.SECURITY_CREDENTIALS, "invalidpassword");

      InitialContext ctx = new InitialContext(env);
      Secured secured = (Secured)ctx.lookup("SecuredBean/local");
      boolean exceptionThrown = false;
      try
      {
         secured.allowed();
      }
      catch (EJBAccessException ignored)
      {
         exceptionThrown = true;
      }
      assertTrue("Security exception not thrown for invalid password", exceptionThrown);
      env.put(Context.SECURITY_CREDENTIALS, "password");
      ctx = new InitialContext(env);

      secured.allowed();

      exceptionThrown = false;
      try
      {
         secured.nobody();
      }
      catch (EJBAccessException ignored)
      {
         exceptionThrown = true;
      }
      assertTrue("Security exception not thrown for invalid role", exceptionThrown);

      group.undeploy();


   }
}
