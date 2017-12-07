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
package org.jboss.test.jca.test;

import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossTestCase;

/**
 * XADataSource unit tests case
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class XADSUnitTestCase extends JBossTestCase
{
   public XADSUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(XADSUnitTestCase.class, "jcaxadstest.jar");
   }

   public void testXADSDeployed() throws Exception
   {
      ObjectName name = new ObjectName("jboss.jca:service=DataSourceBinding,name=TestXADS");
      assertTrue("TestXADS is deployed", getServer().getAttribute(name, "State").equals(new Integer(ServiceMBean.STARTED)));
      name = new ObjectName("jboss.test.jca:name=Tester");
      getServer().invoke(name, "test", null, null);
   }
}
