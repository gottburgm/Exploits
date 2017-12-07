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

import org.jboss.test.JBossTestCase;

/** Tests of mbean attributes.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class AttributesUnitTestCase
   extends JBossTestCase
{
   public AttributesUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(AttributesUnitTestCase.class, "attrtest.sar");
   }

   public void testXmlString()
      throws Exception
   {
      getLog().info("+++ testXmlString");
      MBeanServerConnection server = super.getServer();
      ObjectName serviceName = new ObjectName("test:name=AttrTests,case=#1");
      String xml = (String) server.getAttribute(serviceName, "XmlString");
      getLog().info("XmlString: '"+xml+"'");
      String expectedXml = "<depinfo>\n<value name='abc'>A Value</value>\n</depinfo>";
      assertTrue("xml cdata as expected", xml.equals(expectedXml));
   }

   public void testSysPropRef()
      throws Exception
   {
      MBeanServerConnection server = super.getServer();
      ObjectName serviceName = new ObjectName("test:name=AttrTests,case=#1");
      String prop = (String) server.getAttribute(serviceName, "SysPropRef");
      assertTrue("prop has been replaced", prop.equals("${java.vm.version}") == false);
   }

   public void testTrimedString()
      throws Exception
   {
      MBeanServerConnection server = super.getServer();
      ObjectName serviceName = new ObjectName("test:name=AttrTests,case=#1");
      String prop = (String) server.getAttribute(serviceName, "TrimedString");
      assertTrue("whitespace is trimed", prop.equals("123456789"));
   }

   public void testSysPropRefNot()
      throws Exception
   {
      MBeanServerConnection server = super.getServer();
      ObjectName serviceName = new ObjectName("test:name=AttrTests,case=#2");
      String prop = (String) server.getAttribute(serviceName, "SysPropRef");
      assertTrue("prop has NOT been replaced", prop.equals("${java.vm.version}"));
   }

   public void testTrimedStringNot()
      throws Exception
   {
      MBeanServerConnection server = super.getServer();
      ObjectName serviceName = new ObjectName("test:name=AttrTests,case=#2");
      String prop = (String) server.getAttribute(serviceName, "TrimedString");
      assertTrue("whitespace is NOT trimed", prop.equals(" 123456789 "));
   }
}
