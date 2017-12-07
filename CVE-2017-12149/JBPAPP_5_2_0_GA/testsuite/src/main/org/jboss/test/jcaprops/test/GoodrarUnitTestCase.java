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

import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jcaprops.support.PropertyTestActivationSpecMBean;
import org.jboss.test.jcaprops.support.PropertyTestAdminObject;
import org.jboss.test.jcaprops.support.PropertyTestResourceAdapterMBean;

/**
 * A GoodrarUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class GoodrarUnitTestCase extends JBossTestCase
{
   public GoodrarUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(GoodrarUnitTestCase.class, "testjcaprops-good.rar");
   }

   public void testRARProperties() throws Exception
   {
      AttributeList expected = new AttributeList();
      expected.add(new Attribute("StringRAR", "stringFromRARProperties"));
      expected.add(new Attribute("BooleanRAR", Boolean.TRUE));
      expected.add(new Attribute("ByteRAR", new Byte((byte) 1)));
      expected.add(new Attribute("CharacterRAR", new Character('a')));
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

   public void testASProperties() throws Exception
   {
      AttributeList expected = new AttributeList();
      expected.add(new Attribute("StringAS", "stringFromASProperties"));
      expected.add(new Attribute("BooleanAS", Boolean.TRUE));
      expected.add(new Attribute("ByteAS", new Byte((byte) 11)));
      expected.add(new Attribute("CharacterAS", new Character('b')));
      expected.add(new Attribute("ShortAS", new Short((short) 12)));
      expected.add(new Attribute("IntegerAS", new Integer(13)));
      expected.add(new Attribute("LongAS", new Long(14)));
      expected.add(new Attribute("FloatAS", Float.valueOf("5e16")));
      expected.add(new Attribute("DoubleAS", Double.valueOf("7e18")));
      MBeanServerConnection connection = getServer();
      AttributeList result = connection.getAttributes(PropertyTestActivationSpecMBean.NAME, getExpectedStringArray(expected));
      
      AttributeList resultClone = (AttributeList) result.clone();
      resultClone.removeAll(expected);
      assertTrue("Did not expect: " + list(resultClone) + " expected " + list(expected), resultClone.size() == 0);
      
      expected.removeAll(result);
      assertTrue("Expected: " + list(expected) + " got " + list(result), expected.size() == 0);
   }

   public void testMCFProperties() throws Exception
   {
      AttributeList expected = new AttributeList();
      expected.add(new Attribute("StringRAR", "stringFromRARProperties"));
      expected.add(new Attribute("BooleanRAR", Boolean.TRUE));
      expected.add(new Attribute("ByteRAR", new Byte((byte) 1)));
      expected.add(new Attribute("CharacterRAR", new Character('a')));
      expected.add(new Attribute("ShortRAR", new Short((short) 2)));
      expected.add(new Attribute("IntegerRAR", new Integer(3)));
      expected.add(new Attribute("LongRAR", new Long(4)));
      expected.add(new Attribute("FloatRAR", Float.valueOf("5e6")));
      expected.add(new Attribute("DoubleRAR", Double.valueOf("7e8")));
      expected.add(new Attribute("StringMCF", "stringFromMCFProperties"));
      expected.add(new Attribute("BooleanMCF", Boolean.TRUE));
      expected.add(new Attribute("ByteMCF", new Byte((byte) 21)));
      expected.add(new Attribute("CharacterMCF", new Character('c')));
      expected.add(new Attribute("ShortMCF", new Short((short) 22)));
      expected.add(new Attribute("IntegerMCF", new Integer(23)));
      expected.add(new Attribute("LongMCF", new Long(24)));
      expected.add(new Attribute("FloatMCF", Float.valueOf("5e26")));
      expected.add(new Attribute("DoubleMCF", Double.valueOf("7e28")));
      expected.add(new Attribute("StringCD", "stringFromCDProperties"));
      expected.add(new Attribute("BooleanCD", Boolean.TRUE));
      expected.add(new Attribute("ByteCD", new Byte((byte) 31)));
      expected.add(new Attribute("CharacterCD", new Character('d')));
      expected.add(new Attribute("ShortCD", new Short((short) 32)));
      expected.add(new Attribute("IntegerCD", new Integer(33)));
      expected.add(new Attribute("LongCD", new Long(34)));
      expected.add(new Attribute("FloatCD", Float.valueOf("6e26")));
      expected.add(new Attribute("DoubleCD", Double.valueOf("8e28")));
      AttributeList result = getMCFAttributes(expected);
      
      AttributeList resultClone = (AttributeList) result.clone();
      resultClone.removeAll(expected);
      assertTrue("Did not expect: " + list(resultClone) + " expected " + list(expected), resultClone.size() == 0);
      
      expected.removeAll(result);
      assertTrue("Expected: " + list(expected) + " got " + list(result), expected.size() == 0);
   }

   public void testAdminObjectProperties() throws Exception
   {
      AttributeList expected = new AttributeList();
      expected.add(new Attribute("StringAOMBean", "stringFromAOMBeanProperties"));
      expected.add(new Attribute("BooleanAOMBean", Boolean.TRUE));
      expected.add(new Attribute("ByteAOMBean", new Byte((byte) 21)));
      expected.add(new Attribute("CharacterAOMBean", new Character('c')));
      expected.add(new Attribute("ShortAOMBean", new Short((short) 22)));
      expected.add(new Attribute("IntegerAOMBean", new Integer(23)));
      expected.add(new Attribute("LongAOMBean", new Long(24)));
      expected.add(new Attribute("FloatAOMBean", Float.valueOf("5e26")));
      expected.add(new Attribute("DoubleAOMBean", Double.valueOf("7e28")));
      expected.add(new Attribute("StringAO", "stringFromAOProperties"));
      expected.add(new Attribute("BooleanAO", Boolean.TRUE));
      expected.add(new Attribute("ByteAO", new Byte((byte) 31)));
      expected.add(new Attribute("CharacterAO", new Character('d')));
      expected.add(new Attribute("ShortAO", new Short((short) 32)));
      expected.add(new Attribute("IntegerAO", new Integer(33)));
      expected.add(new Attribute("LongAO", new Long(34)));
      expected.add(new Attribute("FloatAO", Float.valueOf("6e26")));
      expected.add(new Attribute("DoubleAO", Double.valueOf("8e28")));
      AttributeList result = getAOAttributes(expected);
      
      AttributeList resultClone = (AttributeList) result.clone();
      resultClone.removeAll(expected);
      assertTrue("Did not expect: " + list(resultClone) + " expected " + list(expected), resultClone.size() == 0);
      
      expected.removeAll(result);
      assertTrue("Expected: " + list(expected) + " got " + list(result), expected.size() == 0);
   }
   
   protected AttributeList getMCFAttributes(AttributeList attributes) throws Exception
   {
      MBeanServerConnection connection = getServer();
      ObjectName name = new ObjectName("jboss.jca:service=ManagedConnectionFactory,name=Goodrar");
      AttributeList result = new AttributeList();
      for (int i = 0; i < attributes.size(); ++i)
      {
         String attributeName = ((Attribute) attributes.get(i)).getName();
         try
         {
            Object value = connection.invoke(name, "getManagedConnectionFactoryAttribute", 
               new Object[] { attributeName }, new String[] { String.class.getName() } );
            result.add(new Attribute(attributeName, value));
         }
         catch (Exception e)
         {
            log.debug("Could not retrieve attribute " + attributeName, e);
         }
      }
      return result;
   }
   
   protected AttributeList getAOAttributes(AttributeList attributes) throws Exception
   {
      InitialContext ctx = new InitialContext();
      PropertyTestAdminObject ao = (PropertyTestAdminObject) ctx.lookup("GoodrarAO");
      AttributeList result = new AttributeList();
      for (int i = 0; i < attributes.size(); ++i)
      {
         String attributeName = ((Attribute) attributes.get(i)).getName();
         try
         {
            String getter = "get" + Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
            Method method = PropertyTestAdminObject.class.getMethod(getter, null);
            Object value = method.invoke(ao, null); 
            result.add(new Attribute(attributeName, value));
         }
         catch (Exception e)
         {
            log.debug("Could not retrieve attribute " + attributeName, e);
         }
      }
      return result;
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
