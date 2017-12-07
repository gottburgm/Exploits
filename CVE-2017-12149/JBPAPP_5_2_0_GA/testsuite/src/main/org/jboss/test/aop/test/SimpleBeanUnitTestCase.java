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
package org.jboss.test.aop.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.aop.bean.SimpleBeanInterceptor;
import org.jboss.test.aop.simpleejb.Simple;
import org.jboss.test.aop.simpleejb.SimpleHome;

/**
 * @author ifedorenko
 */
public class SimpleBeanUnitTestCase
   extends JBossTestCase 
{

   public SimpleBeanUnitTestCase(String name)
   {
      super(name);
   }

   public void testEjb() throws Exception
   {
      Context ctx = new InitialContext();
      try 
      {
         Object obj = ctx.lookup("ejb/test/Simple");
         SimpleHome home = (SimpleHome)
                 PortableRemoteObject.narrow(obj, SimpleHome.class);
         Simple test = home.create();
         assertEquals(SimpleBeanInterceptor.RETURN_VALUE, test.getTest());
      } 
      finally
      {
         ctx.close();
      }
   }

   public void testEjbCallerSide() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=SimpleBeanTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testEJBCallside", params, sig);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SimpleBeanUnitTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "simpleejb.sar");
      return setup; 
   }
}
