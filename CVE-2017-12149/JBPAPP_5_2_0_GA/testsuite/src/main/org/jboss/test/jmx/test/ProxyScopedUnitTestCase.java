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

import junit.framework.Test;

import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossTestCase;

/**
 * Tests of mbean proxy attributes.
 *
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81036 $
 */
public class ProxyScopedUnitTestCase
   extends JBossTestCase
{
   public ProxyScopedUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(ProxyUnitTestCase.class, "jmxproxyscoped.sar");
   }

   public void testStarted()
      throws Exception
   {
      // All the tests are done during start service
      MBeanServerConnection server = getServer();
      ObjectName serviceName = new ObjectName("jboss.test:name=ProxyTests");
      assertTrue("Proxy tests should be started", server.getAttribute(serviceName, "State").equals(new Integer(ServiceMBean.STARTED)));
      serviceName = new ObjectName("jboss.test:name=ProxyTestsNested");
      assertTrue("Proxy tests nested should be started", server.getAttribute(serviceName, "State").equals(new Integer(ServiceMBean.STARTED)));
      serviceName = new ObjectName("jboss.test:name=ProxyTestsAttribute");
      assertTrue("Proxy tests attribute should be started", server.getAttribute(serviceName, "State").equals(new Integer(ServiceMBean.STARTED)));
   }
}
