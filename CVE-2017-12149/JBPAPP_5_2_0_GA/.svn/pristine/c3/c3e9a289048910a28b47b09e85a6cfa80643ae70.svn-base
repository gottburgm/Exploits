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
package org.jboss.test.jmx.compliance.objectname;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

public class BasicTEST extends TestCase
{
   public static final String STD_DOMAIN = "domain";
   public static final String WHITESPACE = " ";
   public static final String COMMA = ",";
   public static final String EQUALS = "=";
   public static final String COLON = ":";
   public static final String EMPTY = "";
   public static final String ASTERISK = "*";
   public static final String QUESTION = "?";
   public static final String QUOTE = "\"";
   public static final String ESCAPE = "\\";
   public static final String STD_KEYPROP_STRING = "key1=val1,key2=val2";
   public static final String REV_KEYPROP_STRING = "key2=val2,key1=val1";
   public static final String KEY1 = "key1";
   public static final String KEY2 = "key2";
   public static final String VAL1 = "val1";
   public static final String VAL2 = "val2";

   public static final int JMX1_0 = 0;
   public static final int JMX1_2 = 1;
   public static final int QUOTED = 100;
   public static final int QUOTEDBACKSLASH = 101;

   public BasicTEST(String s)
   {
      super(s);
   }

   public void testStringNoDomain()
   {
      String nameArg = COLON + STD_KEYPROP_STRING;
      try
      {
         ObjectName name = new ObjectName(nameArg);
         String domain = name.getDomain();
         if (null == domain)
         {
            fail("getDomain() should return empty string rather than null");
         }
         assertTrue("domain should have been zero size", domain.length() == 0);
         assertEquals("value for key: " + KEY1 + " should be: " + VAL1, VAL1, name.getKeyProperty(KEY1));
         assertEquals("value for key: " + KEY2 + " should be: " + VAL2, VAL2, name.getKeyProperty(KEY2));
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + nameArg + "')");
      }
   }

   public void testStringWithDomain()
   {
      String nameArg = STD_DOMAIN + COLON + STD_KEYPROP_STRING;
      try
      {
         ObjectName name = new ObjectName(nameArg);
         assertEquals("domain should be equivalent", STD_DOMAIN, name.getDomain());
         assertEquals("value for key: " + KEY1 + " should be: " + VAL1, VAL1, name.getKeyProperty(KEY1));
         assertEquals("value for key: " + KEY2 + " should be: " + VAL2, VAL2, name.getKeyProperty(KEY2));
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + nameArg + "')");
      }
   }

   public void testSingleKVP()
   {
      try
      {
         ObjectName name = new ObjectName(STD_DOMAIN, KEY1, VAL1);
         assertEquals("domain should be equivalent", STD_DOMAIN, name.getDomain());
         assertEquals("value for key: " + KEY1 + " should be: " + VAL1, VAL1, name.getKeyProperty(KEY1));
         assertNull("should return NULL key property for: " + KEY2, name.getKeyProperty(KEY2));

         String kplistString = name.getKeyPropertyListString();
         if (null == kplistString)
         {
            fail("key property list string was null;");
         }
         assertTrue("KeyPropertyListString should match",
                    kplistString.equals("key1=val1"));

      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + STD_DOMAIN + "','" + KEY1 + "','" + VAL1 + "')");
      }
   }

   public void testHashtable()
   {
      try
      {
         Hashtable properties = new Hashtable();
         properties.put(KEY1, VAL1);
         properties.put(KEY2, VAL2);
         ObjectName name = new ObjectName(STD_DOMAIN, properties);
         assertEquals("domain should be equivalent", STD_DOMAIN, name.getDomain());
         assertEquals("value for key: " + KEY1 + " should be: " + VAL1, VAL1, name.getKeyProperty(KEY1));
         assertEquals("value for key: " + KEY2 + " should be: " + VAL2, VAL2, name.getKeyProperty(KEY2));

         String kplistString = name.getKeyPropertyListString();
         if (null == kplistString)
         {
            fail("key property list string was null;");
         }
         assertTrue("KeyPropertyListString should match",
                    (kplistString.equals(STD_KEYPROP_STRING) || kplistString.equals(REV_KEYPROP_STRING)));
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + STD_DOMAIN + "','" + KEY1 + "','" + VAL1 + "')");
      }
   }

   public void testWhitespaceDomain()
   {
      domainTest(WHITESPACE, JMX1_0);
   }

   public void testCommaDomain()
   {
      domainTest(COMMA, JMX1_2);
   }

   public void testEqualsDomain()
   {
      domainTest(EQUALS, JMX1_2);
   }

   public void testQuestionValue()
   {
      valueTest(QUESTION, QUOTEDBACKSLASH);
   }

   public void testAsteriskValue()
   {
      valueTest(ASTERISK, QUOTEDBACKSLASH);
   }

   public void testQuoteValue()
   {
      valueTest(QUOTE, QUOTEDBACKSLASH);
   }

   public void testEqualsValue()
   {
      valueTest(EQUALS, QUOTED);
   }

   public void testCommaValue()
   {
      valueTest(COMMA, QUOTED);
   }

   public void testColonValue()
   {
      valueTest(COLON, QUOTED);
   }

   public void testEscapeValue()
   {
      valueTest(ESCAPE, QUOTEDBACKSLASH);
      valueTest(ESCAPE, JMX1_0);
   }

   public void testEmptyQuotesValue()
   {
      valueTest(QUOTE + QUOTE, JMX1_0);
   }

   public void testKeyPropertyList()
   {
      String nameArg = ":" + STD_KEYPROP_STRING;
      try
      {
         ObjectName name = new ObjectName(nameArg);
         String kplistString = name.getKeyPropertyListString();
         if (null == kplistString)
         {
            fail("key property list string was null;");
         }
         assertTrue("KeyPropertyListString should match",
                    (kplistString.equals(STD_KEYPROP_STRING) || kplistString.equals(REV_KEYPROP_STRING)));

      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + nameArg + "')");
      }
   }

   public void testToString()
   {
      String nameArg1 = ":key1=val1";
      String nameArg2 = "domain:key1=val1";

      try
      {
         ObjectName name1 = new ObjectName(nameArg1);
         assertEquals("toString should match", nameArg1, name1.toString());
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + nameArg1 + "')");
      }

      try
      {
         ObjectName name2 = new ObjectName(nameArg2);
         assertEquals("toString should match", nameArg2, name2.toString());
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + nameArg2 + "')");
      }
   }

   private void domainTest(String domain, int version)
   {
      String nameArg = domain + COLON + STD_KEYPROP_STRING;
      try
      {
         ObjectName name = new ObjectName(nameArg);
         assertEquals("domain should be equivalent", domain, name.getDomain());
      }
      catch (MalformedObjectNameException e)
      {
         if (version == JMX1_2)
            fail("spurious MalformedObjectNameException on ('" + nameArg + "') as of JMX 1.2 " + 
                 domain + " is allowed in a domain");
         else
            fail("spurious MalformedObjectNameException on ('" + nameArg + "')");
      }

      try
      {
         ObjectName name = new ObjectName(domain, KEY1, VAL1);
         assertEquals("domain should be equivalent", domain, name.getDomain());
      }
      catch (MalformedObjectNameException e)
      {
         if (version == JMX1_2)
            fail("spurious MalformedObjectNameException on ('" + 
                 domain + "','" + KEY1 + "','" + VAL1 + "') as of JMX 1.2 " + 
                 domain + " is allowed in a domain");
         else
            fail("spurious MalformedObjectNameException on ('" + 
                 domain + "','" + KEY1 + "','" + VAL1 + "')");
      }

      Hashtable table = new Hashtable();
      try
      {
         table.put(KEY1, VAL1);
         ObjectName name = new ObjectName(domain, table);
         assertEquals("domain should be equivalent", domain, name.getDomain());
      }
      catch (MalformedObjectNameException e)
      {
         if (version == JMX1_2)
            fail("spurious MalformedObjectNameException on ('" + 
                 domain + " " + table + "') as of JMX 1.2 " + 
                 domain + " is allowed in a domain");
         else
            fail("spurious MalformedObjectNameException on ('" + 
                 domain + " " + table + "')");
      }
   }

   public void valueTest(String value, int type)
   {
      String name = null;
      if (type == QUOTEDBACKSLASH)
         name = STD_DOMAIN + COLON + KEY1 + EQUALS + QUOTE + ESCAPE + value + QUOTE;
      else if (type == QUOTED)
         name = STD_DOMAIN + COLON + KEY1 + EQUALS + QUOTE + value + QUOTE;
      else
         name = STD_DOMAIN + COLON + KEY1 + EQUALS + value;
      try
      {
         new ObjectName(name);
      }
      catch (MalformedObjectNameException e)
      {
         if (type == QUOTEDBACKSLASH)
            fail("spurious MalformedObjectNameException on ('" + name + 
                 "') as of JMX 1.2 " + value + " is allowed inside quotes escaped by a backslash");
         else if (type == QUOTED)
            fail("spurious MalformedObjectNameException on ('" + name + 
                 "') as of JMX 1.2 " + value + " is allowed inside quotes");
         else
            fail("FAILS IN RI: spurious MalformedObjectNameException on ('" + name + "')");
      }

      String test = null;
      if (type == QUOTEDBACKSLASH)
         test = QUOTE + ESCAPE + value + QUOTE;
      else if (type == QUOTED)
         test = QUOTE + value + QUOTE;
      else
         test = value;
      try
      {
         new ObjectName(STD_DOMAIN, KEY1, test);
      }
      catch (MalformedObjectNameException e)
      {
         if (type == QUOTEDBACKSLASH)
            fail("spurious MalformedObjectNameException on ('" + 
                 STD_DOMAIN + "','" + KEY1 + "','" + value + "') as of JMX 1.2 " + 
                 STD_DOMAIN + " is allowed inside quotes escaped by a backslah");
         if (type == QUOTED)
            fail("spurious MalformedObjectNameException on ('" + 
                 STD_DOMAIN + "','" + KEY1 + "','" + value + "') as of JMX 1.2 " + 
                 STD_DOMAIN + " is allowed inside quotes");
         else
            fail("spurious MalformedObjectNameException on ('" + 
                 STD_DOMAIN + "','" + KEY1 + "','" + value + "')");
      }

      Hashtable table = new Hashtable();
      if (type == QUOTEDBACKSLASH)
         table.put(KEY1, QUOTE + ESCAPE + value + QUOTE);
      else if (type == QUOTED)
         table.put(KEY1, QUOTE + value + QUOTE);
      else
         table.put(KEY1, value);
      try
      {
         new ObjectName(STD_DOMAIN, table);
      }
      catch (MalformedObjectNameException e)
      {
         if (type == QUOTEDBACKSLASH)
            fail("spurious MalformedObjectNameException on ('" + 
                 STD_DOMAIN + " " + table + "') as of JMX 1.2 " + 
                 STD_DOMAIN + " is allowed inside quotes escaped by a backslah");
         if (type == QUOTED)
            fail("spurious MalformedObjectNameException on ('" + 
                 STD_DOMAIN + " " + table + "') as of JMX 1.2 " + 
                 STD_DOMAIN + " is allowed inside quotes");
         else
            fail("spurious MalformedObjectNameException on ('" + 
                 STD_DOMAIN + " " + table + "')");
      }
   }
}
