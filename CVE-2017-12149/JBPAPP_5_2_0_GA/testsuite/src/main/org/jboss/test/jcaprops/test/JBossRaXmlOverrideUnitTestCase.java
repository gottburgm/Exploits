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
package org.jboss.test.jcaprops.test;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jcaprops.support.PropertyTestResourceAdapterMBean;

/**
 * A JBossRaXmlOverrideTestCase.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public class JBossRaXmlOverrideUnitTestCase extends JBossTestCase
{

   public JBossRaXmlOverrideUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testJBossRaXmlOverride() throws Exception
   {
      
      AttributeList expected = new AttributeList();
      expected.add(new Attribute("StringRAR", "XMLOVERRIDE"));
      expected.add(new Attribute("BooleanRAR", Boolean.FALSE));
      expected.add(new Attribute("ByteRAR", new Byte((byte) 1)));
      expected.add(new Attribute("CharacterRAR", new Character('A')));
      expected.add(new Attribute("ShortRAR", new Short((short) 2)));
      expected.add(new Attribute("IntegerRAR", new Integer(3)));
      expected.add(new Attribute("LongRAR", new Long(4)));
      expected.add(new Attribute("FloatRAR", Float.valueOf("5e6")));
      expected.add(new Attribute("DoubleRAR", Double.valueOf("7e8")));
      MBeanServerConnection connection = getServer();
      AttributeList result = connection.getAttributes(PropertyTestResourceAdapterMBean.NAME, getExpectedStringArray(expected));
      
      AttributeList resultClone = (AttributeList) result.clone();
      resultClone.removeAll(expected);
      assertTrue("Did not expect: " + list(resultClone) + " expected " + list(expected), resultClone.size() == 0);
      
      expected.removeAll(result);
      assertTrue("Expected: " + list(expected) + " got " + list(result), expected.size() == 0);

   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(JBossRaXmlOverrideUnitTestCase.class, "testjcaprops-override.rar");
   }
   
   protected String[] getExpectedStringArray(AttributeList attributes)
   {
      String[] result = new String[attributes.size()];
      for (int i = 0; i < attributes.size(); ++i)
         result [i] = ((Attribute) attributes.get(i)).getName();
      return result;
   }
   
   protected String list(AttributeList list)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append('[');
      for (int i = 0; i < list.size(); ++i)
      {
         Attribute attribute = (Attribute) list.get(i);
         buffer.append(attribute.getName());
         buffer.append('=');
         buffer.append(attribute.getValue());
         if (i+1 < list.size())
            buffer.append(", ");
      }
      buffer.append(']');
      return buffer.toString();
   }
   
   
}
