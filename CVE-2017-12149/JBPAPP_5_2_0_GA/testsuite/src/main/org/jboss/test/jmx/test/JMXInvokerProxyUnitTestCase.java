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

import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.invokerproxy.MyServiceMBean;
import org.jboss.test.jmx.invokerproxy.IProxy;
import junit.framework.Test;

import javax.naming.InitialContext;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class JMXInvokerProxyUnitTestCase
   extends JBossTestCase
{
   public JMXInvokerProxyUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(JMXInvokerProxyUnitTestCase.class, "invoker-proxy-test.sar");
   }

   public void testInvokeTargetMethod() throws Exception
   {
      InitialContext ic = new InitialContext();
      MyServiceMBean myService = (MyServiceMBean)ic.lookup("MyServiceInvokeTarget");
      final String arg = "myAction";
      String result = myService.myAction(arg);
      assertEquals(arg, result);
   }

   public void testInvoke() throws Exception
   {
      InitialContext ic = new InitialContext();
      MyServiceMBean myService = (MyServiceMBean)ic.lookup("MyService");
      final String arg = "invoke";
      String result = myService.myAction(arg);
      assertEquals(arg, result);
   }

   public void testProgramaticProxy() throws Exception
   {
      InitialContext ic = new InitialContext();
      IProxy myService = (IProxy) ic.lookup("IProxy");
      String result = myService.echoDate("testProgramaticProxy");
      assertTrue(result, result.indexOf("testProgramaticProxy") >= 0);
   }
}
