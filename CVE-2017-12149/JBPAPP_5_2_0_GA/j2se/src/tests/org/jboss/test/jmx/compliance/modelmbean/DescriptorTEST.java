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
package org.jboss.test.jmx.compliance.modelmbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;

/**
 * Tests the standard required <tt>DescriptorSupport</tt> implementation.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $   
 */
public class DescriptorTEST
   extends TestCase
{
   public DescriptorTEST(String s)
   {
      super(s);
   }

   // Tests ------------------------------------------------------------

   public void testDefaultConstructor()
      throws Exception
   {
      DescriptorSupport descriptor = new DescriptorSupport();
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());
   }

   public void testSizeConstructor()
      throws Exception
   {
      DescriptorSupport descriptor = new DescriptorSupport(100);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      boolean caught = false;
      try
      {
         descriptor = new DescriptorSupport(-1);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for a negative size", caught);
   }

   public void testCopyConstructor()
      throws Exception
   {
      DescriptorSupport descriptor = new DescriptorSupport((DescriptorSupport) null);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      descriptor = new DescriptorSupport(descriptor);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      descriptor.setField("name", "testName");
      descriptor.setField("descriptorType", "testType");
      descriptor = new DescriptorSupport(descriptor);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
      assertTrue("Should be valid", descriptor.isValid());
   }

   public void testNamesValuesConstructor()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType" };
      Object[] values = new Object[] { "testName", "testType" };
      DescriptorSupport descriptor = new DescriptorSupport(names, values);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
      assertTrue("Should be valid", descriptor.isValid());

      descriptor = new DescriptorSupport(new String[0], new Object[0]);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      boolean caught = false;
      try
      {
         descriptor = new DescriptorSupport(null, null);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null names and values", caught);

      caught = false;
      try
      {
         descriptor = new DescriptorSupport(null, values);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null names", caught);

      caught = false;
      try
      {
         descriptor = new DescriptorSupport(names, null);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null values", caught);

      Object[] tooManyValues = new Object[] { "testName", "testType", "tooMany" };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport(names, tooManyValues);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for too many values", caught);

      Object[] tooFewValues = new Object[] { "testName" };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport(names, tooFewValues);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for too few values", caught);

      String[] nullName = new String[] { "name", null };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport(nullName, values);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null name", caught);

      String[] emptyName = new String[] { "name", "" };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport(emptyName, values);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for empty name ", caught);

      // This is legal?
      String[] notEmptyName = new String[] { "name", " " };
      descriptor = new DescriptorSupport(notEmptyName, values);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue(" "));
      assertFalse("Should not be valid", descriptor.isValid());

      names = new String[] { "name", "descriptorType", "another" };
      values = new Object[] { "testName", "testType", null };
      descriptor = new DescriptorSupport(names, values);
      assertTrue("Should be three fields", descriptor.getFields().length == 3);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
      assertTrue("another should be null", descriptor.getFieldValue("another") == null);
      assertTrue("Should be valid", descriptor.isValid());
   }

   public void testNameEqualsValueConstructor()
      throws Exception
   {
      String[] fields = new String[] { "name=testName", "descriptorType=testType" };
      DescriptorSupport descriptor = new DescriptorSupport(fields);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
      assertTrue("Should be valid", descriptor.isValid());

      descriptor = new DescriptorSupport((String[]) null);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      descriptor = new DescriptorSupport(new String[0]);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      String[] nullName = new String[] { "name=testName", "=rubbish" };
      boolean caught = false;
      try
      {
         descriptor = new DescriptorSupport(nullName);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for '=rubbish'", caught);

      // This is legal?
      String[] notEmptyName = new String[] { "name=testName", " =rubbish" };
      descriptor = new DescriptorSupport(notEmptyName);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("rubbish", descriptor.getFieldValue(" "));
      assertFalse("Should not be valid", descriptor.isValid());
   }

   public void testNameEqualsValueConstructorError()
      throws Exception
   {
      try
      {
         String[] fields = new String[] { "name=testName", "descriptorType=testType", "another=" };
         DescriptorSupport descriptor = new DescriptorSupport(fields);
         assertTrue("Should be three fields", descriptor.getFields().length == 3);
         assertEquals("testName", descriptor.getFieldValue("name"));
         assertEquals("testType", descriptor.getFieldValue("descriptorType"));
         assertTrue("another should be null", descriptor.getFieldValue("another") == null);
         assertTrue("Should be valid", descriptor.isValid());
      }
      catch (Exception e)
      {
         fail("FAILS IN RI: 'another=' should be valid according to the javadoc " + e.toString());
      }
   }

   public void testGetFieldValue()
      throws Exception
   {
      String[] fields = new String[] { "name=testName", "descriptorType=testType" };
      DescriptorSupport descriptor = new DescriptorSupport(fields);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertTrue("Field names are not case sensitive", "testName".equals(descriptor.getFieldValue("NAME")));
      assertTrue("Non existent field should be null", descriptor.getFieldValue("nonExistent") == null);

      boolean caught = false;
      try
      {
         descriptor.getFieldValue(null);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null name", caught);

      caught = false;
      try
      {
         descriptor.getFieldValue("");
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for empty name", caught);

      // This is legal?
      assertTrue("Blank field name is allowed", descriptor.getFieldValue(" ") == null);
   }

   public void testSetFieldValue()
      throws Exception
   {
      String[] fields = new String[] { "name=testName", "descriptorType=testType" };
      DescriptorSupport descriptor = new DescriptorSupport(fields);
      assertEquals("testName", descriptor.getFieldValue("name"));
      descriptor.setField("name", "newName");
      assertEquals("newName", descriptor.getFieldValue("name"));
      descriptor.setField("NAME", "newNAME");
      assertEquals("newNAME", descriptor.getFieldValue("name"));

      boolean caught = false;
      try
      {
         descriptor.setField(null, "null");
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null name", caught);

      caught = false;
      try
      {
         descriptor.setField("", "empty");
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for empty name", caught);

      // This is legal?
      descriptor.setField(" ", "blank");
      assertEquals("blank", descriptor.getFieldValue(" "));

      descriptor.setField("newField", "newValue");
      assertEquals("newValue", descriptor.getFieldValue("newField"));
   }

   public void testGetFields()
      throws Exception
   {
      String[] fields = new String[] { "name=testName", "descriptorType=testType" };
      DescriptorSupport descriptor = new DescriptorSupport(fields);
      String[] getFields = descriptor.getFields();
      compareFields(fields, getFields);
   }

   public void testGetFieldNames()
      throws Exception
   {
      String[] fields = new String[] { "name=testName", "descriptorType=testType" };
      DescriptorSupport descriptor = new DescriptorSupport(fields);
      String[] names = descriptor.getFieldNames();
      compareFieldNames(fields, names);
   }

   public void testGetFieldValues()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType" };
      String[] values = new String[] { "testName", "testType" };
      DescriptorSupport descriptor = new DescriptorSupport(names, values);

      Object[] result = descriptor.getFieldValues(null);
      compareFieldValuesUnsorted(values, result);

      result = descriptor.getFieldValues(new String[0]);
      compareFieldValues(new Object[0], result);

      result = descriptor.getFieldValues(names);
      compareFieldValues(values, result);

      result = descriptor.getFieldValues(new String[] { "name" });
      compareFieldValues(new Object[] { "testName" }, result);

      result = descriptor.getFieldValues(new String[] { "descriptorType", "name" });
      compareFieldValues(new Object[] { "testType", "testName" }, result);

      result = descriptor.getFieldValues(new String[] { "NAME" });
      compareFieldValues(new Object[] { "testName" }, result);

      result = descriptor.getFieldValues(new String[] { null });
      compareFieldValues(new Object[] { null }, result);

      result = descriptor.getFieldValues(new String[] { "" });
      compareFieldValues(new Object[] { null }, result);
   }

   public void testSetFieldValues()
      throws Exception
   {
      DescriptorSupport descriptor = new DescriptorSupport();
      String[] names = new String[] { "name", "descriptorType" };
      Object[] values = new Object[] { "testName", "testType" };
      descriptor.setFields(names, values);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
      assertTrue("Should be valid", descriptor.isValid());

      descriptor = new DescriptorSupport();
      descriptor.setFields(new String[0], new Object[0]);
      assertTrue("Should be empty", descriptor.getFields().length == 0);
      assertFalse("Should not be valid", descriptor.isValid());

      boolean caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(null, null);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null names and values", caught);

      caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(null, values);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null names", caught);

      caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(names, null);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null values", caught);

      Object[] tooManyValues = new Object[] { "testName", "testType", "tooMany" };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(names, tooManyValues);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for too many values", caught);

      Object[] tooFewValues = new Object[] { "testName" };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(names, tooFewValues);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for too few values", caught);

      String[] nullName = new String[] { "name", null };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(nullName, values);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for null name", caught);

      String[] emptyName = new String[] { "name", "" };
      caught = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(emptyName, values);
      }
      catch (RuntimeOperationsException e)
      {
         caught = true;
      }
      assertTrue("Expected an exception for empty name ", caught);

      // This is legal?
      String[] notEmptyName = new String[] { "name", " " };
      descriptor = new DescriptorSupport();
      descriptor.setFields(notEmptyName, values);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue(" "));
      assertFalse("Should not be valid", descriptor.isValid());

      names = new String[] { "name", "descriptorType", "another" };
      values = new Object[] { "testName", "testType", null };
      descriptor = new DescriptorSupport();
      descriptor.setFields(names, values);
      assertTrue("Should be three fields", descriptor.getFields().length == 3);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
      assertTrue("another should be null", descriptor.getFieldValue("another") == null);
      assertTrue("Should be valid", descriptor.isValid());
   }

   public void testClone()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType" };
      Object[] values = new Object[] { "testName", "testType" };
      DescriptorSupport descriptor = new DescriptorSupport(names, values);

      DescriptorSupport clone = (DescriptorSupport) descriptor.clone();
      compareFields(descriptor.getFields(), clone.getFields());
   }

   public void testRemove()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType" };
      Object[] values = new Object[] { "testName", "testType" };
      DescriptorSupport descriptor = new DescriptorSupport(names, values);

      descriptor.removeField("name");
      assertTrue("Should be one field", descriptor.getFields().length == 1);
      assertTrue("name should not be present", descriptor.getFieldValue("name") == null);
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));

      descriptor = new DescriptorSupport(names, values);
      descriptor.removeField("NAME");
      assertTrue("Should be one field", descriptor.getFields().length == 1);
      assertTrue("name should not be present", descriptor.getFieldValue("name") == null);
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));

      descriptor = new DescriptorSupport(names, values);
      descriptor.removeField("notPresent");
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));

      descriptor.removeField(null);
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));

      descriptor.removeField("");
      assertTrue("Should be two fields", descriptor.getFields().length == 2);
      assertEquals("testName", descriptor.getFieldValue("name"));
      assertEquals("testType", descriptor.getFieldValue("descriptorType"));
   }

   public void testIsValidMandatory()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType" };
      Object[] values = new Object[] { "testName", "testType" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType" };
      values = new Object[] { null, "testType" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType" };
      values = new Object[] { "", "testType" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType" };
      values = new Object[] { "testName", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType" };
      values = new Object[] { "testName", "" };
      validate(names, values, false);
   }

   public void testIsValidRole()
      throws Exception
   {
      doTestString("role", "operation");
   }

   public void testIsValidGetMethod()
      throws Exception
   {
      doTestString("getMethod", "getSomething");
   }

   public void testIsValidSetMethod()
      throws Exception
   {
      doTestString("setMethod", "setSomething");
   }

   public void testIsValidPersistPeriod()
      throws Exception
   {
      doTestInteger("persistPeriod");
   }

   public void testIsValidCurrencyTimeLimit()
      throws Exception
   {
      doTestInteger("currencyTimeLimit");
   }

   public void testIsValidLastUpdateTimeStamp()
      throws Exception
   {
      doTestInteger("lastUpdatedTimeStamp");
   }

   public void testIsValidLog()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType", "log" };
      Object[] values = new Object[] { "testName", "testType", "true" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "false" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "t" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "f" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "TRUE" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "FALSE" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "T" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "F" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "log" };
      values = new Object[] { "testName", "testType", "rubbish" };
      validate(names, values, false);
   }

   public void testIsValidVisibility()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType", "visibility" };
      Object[] values = new Object[] { "testName", "testType", "1" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", "2" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", "3" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", "4" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", "0" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", "5" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", new Integer(1) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", new Integer(2) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", new Integer(3) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", new Integer(4) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", new Integer(0) };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", new Integer(6) };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "visibility" };
      values = new Object[] { "testName", "testType", "" };
      validate(names, values, false);
   }

   public void testIsValidSeverity()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType", "severity" };
      Object[] values = new Object[] { "testName", "testType", "1" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "2" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "3" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "4" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "5" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(1) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(2) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(3) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(4) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(5) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "0" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", "7" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(0) };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "severity" };
      values = new Object[] { "testName", "testType", new Integer(7) };
      validate(names, values, false);
   }

   public void testIsValidError()
      throws Exception
   {
      try
      {
         String[] names = new String[] { "name", "descriptorType", "severity" };
         Object[] values = new Object[] { "testName", "testType", "6" };
         validate(names, values, true);

         names = new String[] { "name", "descriptorType", "severity" };
         values = new Object[] { "testName", "testType", new Integer(6) };
         validate(names, values, true);
      }
      catch (Exception e)
      {
         fail("FAILS IN RI: javadoc and spec are inconsistent on whether severity=6 is valid");
      }
   }

   public void testIsValidPersistPolicy()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType", "persistPolicy" };
      Object[] values = new Object[] { "testName", "testType", "onUpdate" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "persistPolicy" };
      values = new Object[] { "testName", "testType", "noMoreOftenThan" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "persistPolicy" };
      values = new Object[] { "testName", "testType", "never" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "persistPolicy" };
      values = new Object[] { "testName", "testType", "onTimer" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", "persistPolicy" };
      values = new Object[] { "testName", "testType", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "persistPolicy" };
      values = new Object[] { "testName", "testType", "" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", "persistPolicy" };
      values = new Object[] { "testName", "testType", "rubbish" };
      validate(names, values, false);
   }

   public void testSerialization()
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType" };
      Object[] values = new Object[] { "testName", "testType" };
      DescriptorSupport descriptor = new DescriptorSupport(names, values);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(descriptor);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      Object result = ois.readObject();

      compareFields(descriptor.getFields(), ((DescriptorSupport) result).getFields());
   }

   // Support -------------------------------------------

   private void doTestString(String field, String value)
      throws Exception
   {
      String[] names = new String[] { "name", "descriptorType", field };
      Object[] values = new Object[] { "testName", "testType", value };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", "" };
      validate(names, values, false);
   }

   public void doTestInteger(String field)
   {
      String[] names = new String[] { "name", "descriptorType", field };
      Object[] values = new Object[] { "testName", "testType", "0" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", "-1" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", "100" };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", "-2" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", null };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", "" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", "rubbish" };
      validate(names, values, false);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", new Integer(0) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", new Integer(-1) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", new Integer(100) };
      validate(names, values, true);

      names = new String[] { "name", "descriptorType", field };
      values = new Object[] { "testName", "testType", new Integer(-2) };
      validate(names, values, false);
   }

   private void validate(String[] names, Object[] values, boolean valid)
   {
      DescriptorSupport descriptor = null;
      RuntimeOperationsException caught = null;
      boolean descriptorValid = false;
      try
      {
         descriptor = new DescriptorSupport(names, values);
         descriptorValid = descriptor.isValid();
      }
      catch (RuntimeOperationsException e)
      {
         caught = e;
      }
      if (valid && caught != null)
         throw caught;
      assertEquals("Expected "+ valid + " for new Descriptor(String[], String[]) names=" + 
              Arrays.asList(names) + " values=" + Arrays.asList(values), valid, descriptorValid);

      caught = null;
      descriptorValid = false;
      try
      {
         String[] fields = new String[names.length];
         for (int i = 0; i < fields.length; i++)
         {
            if (values[i] == null)
               fields[i] = names[i] + "=";
            else
               fields[i] = names[i] + "=" + values[i].toString();
         }
         descriptor = new DescriptorSupport(names, values);
         descriptorValid = descriptor.isValid();
      }
      catch (RuntimeOperationsException e)
      {
         caught = e;
      }
      if (valid && caught != null)
         throw caught;
      assertEquals("Expected "+ valid + " for new Descriptor(String[], String[]) names=" + 
         Arrays.asList(names) + " values=" + Arrays.asList(values), valid, descriptorValid);

      caught = null;
      descriptorValid = false;
      try
      {
         descriptor = new DescriptorSupport();
         for (int i = 0; i < names.length; i++)
            descriptor.setField(names[i], values[i]);
         descriptorValid = descriptor.isValid();
      }
      catch (RuntimeOperationsException e)
      {
         caught = e;
      }
      if (valid && caught != null)
         throw caught;
      assertEquals("Expected "+ valid + " for new Descriptor(String[], String[]) names=" + 
         Arrays.asList(names) + " values=" + Arrays.asList(values), valid, descriptorValid);

      caught = null;
      descriptorValid = false;
      try
      {
         descriptor = new DescriptorSupport();
         descriptor.setFields(names, values);
         descriptorValid = descriptor.isValid();
      }
      catch (RuntimeOperationsException e)
      {
         caught = e;
      }
      if (valid && caught != null)
         throw caught;
      assertEquals("Expected "+ valid + " for new Descriptor(String[], String[]) names=" + 
         Arrays.asList(names) + " values=" + Arrays.asList(values), valid, descriptorValid);
   }

   private void compareFieldNames(String[] one, String[] two)
      throws Exception
   {
      Set setOne = makeMap(one).keySet();
      List setTwo = new ArrayList(Arrays.asList(two));
      for (Iterator i = setOne.iterator(); i.hasNext(); )
      {
         Object key = i.next();
         if (setTwo.remove(key) == false)
            fail("Expected " + Arrays.asList(two) + " to contain field " + key);
      }
      assertTrue("Didn't expect the following fields " + setTwo, setTwo.isEmpty());
   }

   private void compareFieldValuesUnsorted(Object[] one, Object[] two)
      throws Exception
   {
      if (one.length != two.length)
         fail("Lengths are different original=" + Arrays.asList(one) + " result=" + Arrays.asList(two));

      List listOne = Arrays.asList(one);
      List listTwo = new ArrayList(Arrays.asList(two));
      for (Iterator i = listOne.iterator(); i.hasNext();)
      {
         Object value = i.next();
         if (listTwo.remove(value) == false)
            fail("Expected " + two + " to contain " + value);
      }
      assertTrue("Didn't expect the following fields " + listTwo, listTwo.isEmpty());
   }

   private void compareFieldValues(Object[] one, Object[] two)
      throws Exception
   {
      if (one.length != two.length)
         fail("Lengths are different original=" + Arrays.asList(one) + " result=" + Arrays.asList(two));
      for (int i = 0; i < one.length; i++)
      {
         if (one[i] == null && two[i] != null)
            fail("For index " + i + " original=" + one[i] + " result=" + two[i]);
         else if (one[i] != null && two[i] == null)
            fail("For index " + i + " original=" + one[i] + " result=" + two[i]);
         else if (one[i] != null && one[i].equals(two[i]) == false)
            fail("For index " + i + " original=" + one[i] + " result=" + two[i]);
      }
   }

   private void compareFields(String[] one, String[] two)
      throws Exception
   {
      Map mapOne = makeMap(one);
      Map mapTwo = makeMap(two);
      for (Iterator i = mapOne.entrySet().iterator(); i.hasNext(); )
      {
         Map.Entry entry = (Map.Entry) i.next();
         Object key = entry.getKey();
         Object value = entry.getValue();
         if (value.equals(mapTwo.remove(key)) == false)
            fail("Expected " + Arrays.asList(two) + " to contain field " + key);
      }
      assertTrue("Didn't expect the following fields " + mapTwo, mapTwo.isEmpty());
   }

   private Map makeMap(String[] fields)
      throws Exception
   {
      HashMap result = new HashMap(fields.length);
      for (int i = 0; i < fields.length; i++)
      {
         int index = fields[i].indexOf("=");
         String key = fields[i].substring(0, index);
         String value = null;
         if (index != fields[i].length()-1)
            value = fields[i].substring(index);
         result.put(key, value);
      }
      return result;
   }
}
