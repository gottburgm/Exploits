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
package test.implementation.modelmbean;

import javax.management.Attribute;
import javax.management.Descriptor;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;

import junit.framework.TestCase;

import org.jboss.mx.modelmbean.XMBean;
import org.jboss.mx.modelmbean.XMBeanConstants;

import test.implementation.modelmbean.support.Trivial;
import test.implementation.modelmbean.support.User;

/**
 * Here are some basic XMBean tests, mainly to demonstrate the use of the
 * XMBean class and the MBean creation (this is the doc ;)
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $
 */
public class XMBeanTEST extends TestCase implements XMBeanConstants
{
   public XMBeanTEST(String s)
   {
      super(s);
   }

   public void testCreateXMBean() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, new User());
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/UserManagementInterface.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);
      
      ObjectName name = new ObjectName(":test=test");
      
      server.registerMBean(mmb, name);     
      assertTrue(server.isRegistered(name));
      
      server.setAttribute(name, new Attribute("Name", "Juha"));
      assertTrue(server.getAttribute(name, "Name").equals("Juha"));
      
      server.setAttribute(name, new Attribute("Address", "StrawBerry Street"));
      assertTrue(server.getAttribute(name, "Address").equals("StrawBerry Street"));
      
      assertTrue(server.invoke(name, "printInfo", null, null) instanceof String);
   }

   public void testCreateWithJBossXMBean10DTD() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, new User());
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/User.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);

      server.registerMBean(mmb, new ObjectName(":test=test"));     
      
      assertTrue(server.isRegistered(new ObjectName(":test=test")));
      
      server.setAttribute(new ObjectName(":test=test"), new Attribute("Name", "Juha"));
      
      assertTrue(server.getAttribute(new ObjectName(":test=test"), "Name").equals("Juha"));
      
   }
   
   public void testCreateWithStandardInterface() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Trivial trivial = new Trivial();
      ObjectName name = new ObjectName(":foo=bar");
      
      XMBean mmb = new XMBean(trivial, XMBeanConstants.STANDARD_INTERFACE);
      server.registerMBean(mmb, name);
      
      assertTrue(server.isRegistered(new ObjectName(":foo=bar")));
      
      server.setAttribute(name, new Attribute("Something", "foobar"));
      assertTrue(server.getAttribute(name, "Something").equals("foobar"));
      
      Boolean b = (Boolean)server.invoke(name, "doOperation", new Object[] { "" }, new String[] { "java.lang.String" });
      assertTrue(b.booleanValue() == true);
   }
   
   
}
