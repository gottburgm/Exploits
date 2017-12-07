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
package org.jboss.test.naming.test;

import java.net.URL;
import java.net.InetAddress;
import java.util.Properties;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Tests of the JNDIBindingServiceMgr
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class JNDIBindingUnitTestCase extends JBossTestCase
{
   public JNDIBindingUnitTestCase(String name)
   {
      super(name);
   }

   /** Tests of accessing the various types of java:comp entries
    *
    * @exception Exception  Description of Exception
    */
   public void testBindings() throws Exception
   {
      InitialContext ctx = getInitialContext();
      // Test the URL binding
      URL jbossHome = (URL) ctx.lookup("urls/jboss-home");
      assertTrue("urls/jboss-home == URL(http://www.jboss.org)",
         jbossHome.toString().equals("http://www.jboss.org"));

      // Test the InetAddress binding
      InetAddress localhost = (InetAddress) ctx.lookup("hosts/localhost");
      InetAddress localhost2 = InetAddress.getByName("127.0.0.1");
      assertTrue("hosts/localhost InetAddress(127.0.0.1)",
         localhost.getHostAddress().equals(localhost2.getHostAddress()));

      // Test the InetAddress binding
      Properties props = (Properties) ctx.lookup("maps/testProps");
      assertTrue("Properties(key1) == value1", props.getProperty("key1").equals("value1"));
      assertTrue("Properties(key2) == value2", props.getProperty("key2").equals("value2"));
   }

   public static Test suite() throws Exception
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL service = loader.getResource("naming/services/bindings-service.xml");
      return getDeploySetup(JNDIBindingUnitTestCase.class, service.toString());
   }

}
