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

import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.proxy.TargetMBean;

/** 
 * Tests for MBeanProxyExt used remotely
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class RemoteMBeanProxyUnitTestCase extends JBossTestCase
{
   public RemoteMBeanProxyUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(RemoteMBeanProxyUnitTestCase.class, "jmxproxy.sar");
   }

   /**
    * Test that we can iterate and retrieve MBeanInfo
    * for all registered MBeans
    * 
    * @throws Exception
    */
   public void testRemoteMBeanProxy() throws Exception
   {
      ObjectName name = new ObjectName("jboss.test:name=ProxyTests");
      Object proxy = getServer().getAttribute(name, "Proxy");
      assertNotNull(proxy);
      assertTrue(proxy instanceof TargetMBean);
      TargetMBean target = (TargetMBean) proxy;
      assertEquals(ServiceMBean.STARTED, target.getState());
   }
}
