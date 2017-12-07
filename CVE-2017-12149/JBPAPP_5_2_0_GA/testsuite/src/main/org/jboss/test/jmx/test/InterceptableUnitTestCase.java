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
package org.jboss.test.jmx.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * Test the Interceptable interface
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class InterceptableUnitTestCase extends JBossTestCase
{
   public InterceptableUnitTestCase(String name)
   {
      super(name);
   }

   public void testInterceptableXMBean() throws Exception
   {
      getLog().info("+++ testInterceptableXMBean");

      MBeanServerConnection server = getServer();
      String module1 = "interceptable-xmbean.sar";
      String module2 = "adderinterceptor-mbean.sar";

      // make sure both modules are not deployed
      undeployForSure(module1);
      undeployForSure(module2);

      boolean isRegistered;
      
      try
      {
         ObjectName target1 = new ObjectName("jboss.test:service=interceptable");
         ObjectName target2 = new ObjectName("jboss.test:service=adderinterceptor");
         
         deploy(module1);
         
         isRegistered = server.isRegistered(target1);
         assertTrue(target1 + " is registered", isRegistered);
         
         // check 1+1 == 2
         Object[] args = new Object[] { new Integer(1), new Integer(1) };
         String[] desc = new String[] { int.class.getName(), int.class.getName() };
         
         Integer result = (Integer) server.invoke(
               target1,
               "add",
               args,
               desc);
         
         assertTrue("1+1 == 2, got: " + result, result.intValue() == 2);
         
         // not deploy the service that will install dynamically
         // the interceptor on target1
         deploy(module2);
         
         isRegistered = server.isRegistered(target2);
         assertTrue(target2 + " is registered", isRegistered);
         
         // 1+1 == 3 now!
         result = (Integer) server.invoke(
               target1,
               "add",
               args,
               desc);
         
         assertTrue("1+1 == 3, got: " + result, result.intValue() == 3);
         
         undeploy(module2);
         
         // 1+1=2 again!
         result = (Integer) server.invoke(
               target1,
               "add",
               args,
               desc);
         
         assertTrue("1+1 == 2, got: " + result, result.intValue() == 2);
      }
      finally
      {
         undeploy(module1);
      }
   }
   
   private void undeployForSure(String module)
   {
      try
      {
         undeploy(module);
      }
      catch (Exception e)
      {
         // ignore
      }
   }
}
