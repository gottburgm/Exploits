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

import junit.framework.Test;
import org.jboss.embedded.junit.EmbeddedTestCase;
import org.jboss.embedded.test.ejb.Customer;
import org.jboss.embedded.test.ejb.DAO;
import org.jboss.test.JBossTestCase;

import javax.naming.InitialContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class EjbJBossTestCase extends JBossTestCase
{
   public EjbJBossTestCase()
   {
      super("BootstrapTestCase");
   }

   /*
   private static void outputJNDI()
           throws InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException
   {
      MBeanServer server = getMBeanServer();
      String xml = (String)server.invoke(new ObjectName("jboss:service=JNDIView"), "listXML", null, null);
      System.out.println(xml);
   }

   private static MBeanServer getMBeanServer()
   {
      JMXKernel jmxKernel = (JMXKernel) bootstrap.getKernel().getRegistry().getEntry("JMXKernel").getTarget();
      MBeanServer server = jmxKernel.getMbeanServer();
      return server;
   }
   */


   public void testSimpleEjb() throws Exception
   {
      InitialContext ctx = getInitialContext();
      DAO dao = (DAO)ctx.lookup("DAOBean/local");
      Customer cust = dao.createCustomer("Bill");
      cust = dao.findCustomer("Bill");
      assertNotNull(cust);
      assertEquals(cust.getName(), "Bill");
   }

   public void testDummy()
   {
      
   }

   public static Test suite() throws Exception
   {
      return EmbeddedTestCase.getAdaptedSetup(EjbJBossTestCase.class, "ejb-test.jar");
   }


}