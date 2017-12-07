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
package org.jboss.test.util.test;

import java.io.File;

import org.jboss.util.StringPropertyReplacer;
import org.jboss.test.JBossTestCase;

/** 
 * Unit tests for the custom JBoss property editors
 *
 * @see org.jboss.util.StringPropertyReplacer
 * @author <a href="Adrian.Brock@HappeningTimes.com">Adrian.Brock</a>
 * @version $Revision: 81036 $
 */
public class PropertyPatternUnitTestCase extends JBossTestCase
{
   static final String simpleKey = "org.jboss.test.util.test.Simple";
   static final String simple = "AProperty";
   static final String anotherKey = "org.jboss.test.util.test.Another";
   static final String another = "BProperty";
   static final String doesNotExist = "org.jboss.test.util.test.DoesNotExist";
   static final String before = "Before";
   static final String between = "Between";
   static final String after = "After";
   static final String fileSeparatorKey = "/";
   static final String pathSeparatorKey = ":";

   String longWithNoProperties = new String("\n"+
"      BLOB_TYPE=OBJECT_BLOB\n"+
"      INSERT_TX = INSERT INTO JMS_TRANSACTIONS (TXID) values(?)\n"+
"      INSERT_MESSAGE = INSERT INTO JMS_MESSAGES (MESSAGEID, DESTINATION, MESSAGEBLOB, TXID, TXOP) VALUES(?,?,?,?,?)\n"+
"      SELECT_ALL_UNCOMMITED_TXS = SELECT TXID FROM JMS_TRANSACTIONS\n"+
"      SELECT_MAX_TX = SELECT MAX(TXID) FROM JMS_MESSAGES\n"+
"      SELECT_MESSAGES_IN_DEST = SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE DESTINATION=?\n"+
"      SELECT_MESSAGE = SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?\n"+
"      MARK_MESSAGE = UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE MESSAGEID=? AND DESTINATION=?\n"+
"      UPDATE_MESSAGE = UPDATE JMS_MESSAGES SET MESSAGEBLOB=? WHERE MESSAGEID=? AND DESTINATION=?\n"+
"      UPDATE_MARKED_MESSAGES = UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE TXOP=?\n"+
"      UPDATE_MARKED_MESSAGES_WITH_TX = UPDATE JMS_MESSAGES SET TXID=?, TXOP=? WHERE TXOP=? AND TXID=?\n"+
"      DELETE_MARKED_MESSAGES_WITH_TX = DELETE FROM JMS_MESSAGES WHERE TXID IS NOT NULL AND TXOP=?\n"+
"      DELETE_TX = DELETE FROM JMS_TRANSACTIONS WHERE TXID = ?\n"+
"      DELETE_MARKED_MESSAGES = DELETE FROM JMS_MESSAGES WHERE TXID=? AND TXOP=?\n"+
"      DELETE_MESSAGE = DELETE FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?\n"+
"      CREATE_MESSAGE_TABLE = CREATE TABLE JMS_MESSAGES ( MESSAGEID INTEGER NOT NULL, \\\n"+
"         DESTINATION VARCHAR(255) NOT NULL, TXID INTEGER, TXOP CHAR(1), \\\n"+
"         MESSAGEBLOB OBJECT, PRIMARY KEY (MESSAGEID, DESTINATION) )\n"+
"      CREATE_TX_TABLE = CREATE TABLE JMS_TRANSACTIONS ( TXID INTEGER )\n");

   static
   {
      System.setProperty(simpleKey, simple);
      System.setProperty(anotherKey, another);
   }

   public PropertyPatternUnitTestCase(String name)
   {
      super(name);
   }

   public void testEmptyPattern()
      throws Exception
   {
      assertEquals("Empty pattern", "",
         StringPropertyReplacer.replaceProperties(""));
   }

   public void testNoPattern()
      throws Exception
   {
      assertEquals("No pattern", "xxx",
         StringPropertyReplacer.replaceProperties("xxx"));
   }

   public void testNoProperty()
      throws Exception
   {
      assertEquals("No pattern", "${xxx}",
         StringPropertyReplacer.replaceProperties("${xxx}"));
   }

   public void testNoPropertyWithDefault()
      throws Exception
   {
      assertEquals("No pattern", "xxx-default",
         StringPropertyReplacer.replaceProperties("${xxx:xxx-default}"));
   }

   public void testPropertyWithDefault()
      throws Exception
   {
      assertEquals("Simple pattern", simple, 
         StringPropertyReplacer.replaceProperties("${"+simpleKey+":simpleDefault}"));
   }

   public void testSimpleProperty()
      throws Exception
   {
      assertEquals("Simple pattern", simple, 
         StringPropertyReplacer.replaceProperties("${"+simpleKey+"}"));
   }
   
   public void testFileSeparatorProperty()
      throws Exception
   {
      assertEquals("File Separator", before + File.separator + after,
         StringPropertyReplacer.replaceProperties(before + "${" + fileSeparatorKey + "}" + after));
   }

   public void testPathSeparatorProperty()
      throws Exception
   {
      assertEquals("Path Separator", before + File.pathSeparator + after,
         StringPropertyReplacer.replaceProperties(before + "${" + pathSeparatorKey + "}" + after));
   }

   public void testStringBeforeProperty()
      throws Exception
   {
      assertEquals("String before pattern", before + simple,
         StringPropertyReplacer.replaceProperties(before + "${"+simpleKey+"}"));
   }

   public void testStringAfterProperty()
      throws Exception
   {
      assertEquals("String after pattern", simple + after,
         StringPropertyReplacer.replaceProperties("${"+simpleKey+"}" + after));
   }

   public void testStringBeforeAfterProperty()
      throws Exception
   {
      assertEquals("String before and after pattern", before + simple + after,
         StringPropertyReplacer.replaceProperties(before + "${"+simpleKey+"}" + after));
   }

   public void testStringBeforeBetweenProperty()
      throws Exception
   {
      assertEquals("String before and between pattern", before + simple + between + another,
         StringPropertyReplacer.replaceProperties(before + "${"+simpleKey+"}" + between + "${" + anotherKey + "}"));
   }

   public void testStringAfterBetweenProperty()
      throws Exception
   {
      assertEquals("String after and between pattern", simple + between + another + after,
         StringPropertyReplacer.replaceProperties("${"+simpleKey+"}" + between + "${" + anotherKey + "}" + after));
   }

   public void testStringBeforeAfterBetweenProperty()
      throws Exception
   {
      assertEquals("String before, after and between pattern", before + simple + between + another + after,
         StringPropertyReplacer.replaceProperties(before + "${"+simpleKey+"}" + between + "${" + anotherKey + "}" + after)); 
   }

   public void testDollarBeforeProperty()
      throws Exception
   {
      assertEquals("Dollar before pattern", "$" + simple,
         StringPropertyReplacer.replaceProperties("$${"+simpleKey+"}"));
   }

   public void testSpaceBetweenDollarAndProperty()
      throws Exception
   {
      assertEquals("Dollar before pattern", "$ {"+simpleKey+"}",
         StringPropertyReplacer.replaceProperties("$ {"+simpleKey+"}"));
   }

   public void testPropertyDoesNotExist()
      throws Exception
   {
      assertEquals("Property does not exist", "${"+doesNotExist+"}",
         StringPropertyReplacer.replaceProperties("${"+doesNotExist+"}"));
   }

   public void testPathologicalProperties()
      throws Exception
   {
      assertEquals("$", StringPropertyReplacer.replaceProperties("$"));
      assertEquals("{", StringPropertyReplacer.replaceProperties("{"));
      assertEquals("}", StringPropertyReplacer.replaceProperties("}"));
      assertEquals("${", StringPropertyReplacer.replaceProperties("${"));
      assertEquals("$}", StringPropertyReplacer.replaceProperties("$}"));
      assertEquals("{$", StringPropertyReplacer.replaceProperties("{$"));
      assertEquals("{}", StringPropertyReplacer.replaceProperties("{}"));
      assertEquals("{{", StringPropertyReplacer.replaceProperties("{{"));
      assertEquals("}$", StringPropertyReplacer.replaceProperties("}$"));
      assertEquals("}{", StringPropertyReplacer.replaceProperties("}{"));
      assertEquals("}}", StringPropertyReplacer.replaceProperties("}}"));
      assertEquals("}}", StringPropertyReplacer.replaceProperties("}}"));
      assertEquals("${}", StringPropertyReplacer.replaceProperties("${}"));
      assertEquals("$}{", StringPropertyReplacer.replaceProperties("$}{"));
      assertEquals("}${", StringPropertyReplacer.replaceProperties("}${"));
      assertEquals("}{$", StringPropertyReplacer.replaceProperties("}{$"));
      assertEquals("{$}", StringPropertyReplacer.replaceProperties("{$}"));
      assertEquals("{}$", StringPropertyReplacer.replaceProperties("{}$"));
   }

   public void testLongWithNoProperties()
      throws Exception
   {
      long start = System.currentTimeMillis();
      assertEquals("No properties in long string", longWithNoProperties,
         StringPropertyReplacer.replaceProperties(longWithNoProperties));
      long end = System.currentTimeMillis();
      assertTrue("Shouldn't take very long", end - start < 1000);
   }

   /**
    * Override the testServerFound since these test don't need the JBoss server
    */
   public void testServerFound()
   {
   }

}

