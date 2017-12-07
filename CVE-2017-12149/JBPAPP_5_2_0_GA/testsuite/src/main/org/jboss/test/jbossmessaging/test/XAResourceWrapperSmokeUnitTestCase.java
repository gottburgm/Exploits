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
package org.jboss.test.jbossmessaging.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.JBossTestSetup;

/**
 * Test the XAResourceWrapper makes a connection.
 * 
 * @author <a href="richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 105321 $
 */
public class XAResourceWrapperSmokeUnitTestCase extends JBossJMSTestCase
{
   ObjectName name = ObjectNameFactory.create("jboss.test:service=XAWrapperSmoke");
   
   public void testSmoke() throws Exception
   {
      MBeanServerConnection server = getServer();
      server.invoke(name, "smokeTest", null, null);
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(XAResourceWrapperSmokeUnitTestCase.class, "jms-xawrapper-smoke.sar"));
   }

   public XAResourceWrapperSmokeUnitTestCase(String name)
   {
      super(name);
   }
}
