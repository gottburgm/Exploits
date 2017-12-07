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
package org.jboss.embedded.test.bootstrap.unit;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.adapters.JMXKernel;
import org.jboss.deployers.spi.DeploymentException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import java.sql.Connection;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class BootstrapTestCase extends TestCase
{
   public BootstrapTestCase()
   {
      super("BootstrapTestCase");
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(BootstrapTestCase.class);


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

   public void testDefaultDS() throws Exception
   {
      outputJNDI();
      InitialContext ctx = new InitialContext();
      DataSource ds = (DataSource)ctx.lookup("java:/DefaultDS");
      Connection conn = ds.getConnection();
      conn.close();
   }

   private void outputJNDI()
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
}
