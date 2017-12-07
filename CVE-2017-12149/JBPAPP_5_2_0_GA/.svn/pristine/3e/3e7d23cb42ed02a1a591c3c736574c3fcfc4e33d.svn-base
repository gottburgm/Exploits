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
package org.jboss.test.jmx.compliance.varia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Object Instance tests.<p>
 *
 * NOTE: The tests use String literals to ensure the comparisons are
 *       not performed on object references.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class ObjectInstanceTestCase
  extends TestCase
{
  // Attributes ----------------------------------------------------------------

  // Constructor ---------------------------------------------------------------

  /**
   * Construct the test
   */
  public ObjectInstanceTestCase(String s)
  {
    super(s);
  }

  // Tests ---------------------------------------------------------------------

  /**
   * Test String constructor.
   */
  public void testStringConstructor()
  {
    ObjectInstance instance = null;

    try
    {
      instance = new ObjectInstance("test:type=test", "ClassName");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }

    // Did it work?
    assertEquals("test:type=test", instance.getObjectName().toString());
    assertEquals("ClassName", instance.getClassName());
  }

  /**
   * Test ObjectName constructor.
   */
  public void testObjectNameConstructor()
  {
    ObjectInstance instance = null;
    ObjectName objectName = null;

    try
    {
      objectName = new ObjectName(":type=test");
      instance = new ObjectInstance(objectName, "ClassName");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }

    // Did it work?
    assertEquals(objectName, instance.getObjectName());
    assertEquals("ClassName", instance.getClassName());
  }

  /**
   * Test Equals.
   */
  public void testEquals()
  {
    ObjectInstance instanceTest = null;
    ObjectInstance instanceSame = null;
    ObjectInstance instanceDiffName = null;
    ObjectInstance instanceDiffClass = null;

    try
    {
      instanceTest = new ObjectInstance("test:type=test", "ClassName");
      instanceSame = new ObjectInstance("test:type=test", "ClassName");
      instanceDiffName = new ObjectInstance("test:type=different", "ClassName");
      instanceDiffClass = new ObjectInstance("test:type=test", "Another");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }

    assertEquals(instanceTest, instanceTest);
    assertEquals(instanceTest, instanceSame);
    if (instanceTest.equals(instanceDiffName))
      fail("ObjectInstance.equals broken for object name");
    if (instanceTest.equals(instanceDiffClass))
      fail("ObjectInstance.equals broken for class name");
  }

  /**
   * Test errors.
   */
  public void testErrors()
  {
    boolean caught = false;
    try
    {
      new ObjectInstance("rubbish", "ClassName");
    }
    catch (MalformedObjectNameException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("ObjectInstance(String, String) failed to report malformed object name");

    try
    {
      String NULL = null;
      new ObjectInstance(NULL, "ClassName");
    }
    catch (MalformedObjectNameException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("ObjectInstance(String, String) failed to report null object name");
  }

  /**
   * Test serialization.
   */
  public void testSerialization()
  {
    ObjectInstance original = null;
    ObjectInstance result = null;
    ObjectName objectName = null;

    try
    {
      objectName = new ObjectName(":type=test");
      original = new ObjectInstance(objectName, "ClassName");

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(original);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      result = (ObjectInstance) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }

    // Did it work?
    assertEquals(original, result);
    assertEquals(objectName, result.getObjectName());
    assertEquals("ClassName", result.getClassName());
  }
}
