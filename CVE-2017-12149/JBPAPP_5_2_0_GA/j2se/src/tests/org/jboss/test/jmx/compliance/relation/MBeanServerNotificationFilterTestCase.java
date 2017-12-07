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
package org.jboss.test.jmx.compliance.relation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.MBeanServerNotification;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;

import junit.framework.TestCase;

/**
 * MBean Server Notification Filter tests.<p>
 *
 * Test it to death.<p>
 *
 * NOTE: The tests use String literals to ensure the comparisons are
 *       not performed on object references.<p>
 *
 * WARNING!! WARNING!! The spec says the MBeanServerNotificationFilter
 * accepts everything by default. The RI does exactly the opposite.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class MBeanServerNotificationFilterTestCase
  extends TestCase
{
  // Attributes ----------------------------------------------------------------

  MBeanServerNotificationFilter mbsnf;
  ObjectName on1;
  ObjectName on2;

  MBeanServerNotification n1;
  MBeanServerNotification n2;

  // Constructor ---------------------------------------------------------------

  /**
   * Construct the test
   */
  public MBeanServerNotificationFilterTestCase(String s)
  {
    super(s);
  }

  // Tests ---------------------------------------------------------------------

  /**
   * By default all names are enabled.
   */
  public void testDefault()
  {
    setUpTest();
    mbsnf.enableObjectName(on1);
    mbsnf.enableObjectName(on2);
    assertEquals(true, mbsnf.isNotificationEnabled(n1));
    assertEquals(true, mbsnf.isNotificationEnabled(n2));
  }

  /**
   * Enable all
   */
  public void testEnableAll()
  {
    setUpTest();
    mbsnf.enableAllObjectNames();
    assertEquals(true, mbsnf.isNotificationEnabled(n1));
    assertEquals(true, mbsnf.isNotificationEnabled(n2));
  }

  /**
   * Enable one
   */
  public void testEnableOne()
  {
    setUpTest();
    mbsnf.enableObjectName(on2);
    assertEquals(false, mbsnf.isNotificationEnabled(n1));
    assertEquals(true, mbsnf.isNotificationEnabled(n2));
  }

  /**
   * Disable all
   */
  public void testDisableAll()
  {
    setUpTest();
    mbsnf.enableObjectName(on1);
    mbsnf.disableAllObjectNames();
    assertEquals(false, mbsnf.isNotificationEnabled(n1));
    assertEquals(false, mbsnf.isNotificationEnabled(n2));
  }

  /**
   * Disable one
   */
  public void testDisableOne()
  {
    setUpTest();
    mbsnf.enableAllObjectNames();
    mbsnf.disableObjectName(on2);
    assertEquals(true, mbsnf.isNotificationEnabled(n1));
    assertEquals(false, mbsnf.isNotificationEnabled(n2));
  }

  /**
   * Test getters
   */
  public void testGetters()
  {
    setUpTest();

    try
    {

      // By default Everything disabled
      assertEquals(0, mbsnf.getEnabledObjectNames().size());
      assertEquals(null, mbsnf.getDisabledObjectNames());

      // Enabled everything
      mbsnf.enableAllObjectNames();
      assertEquals(null, mbsnf.getEnabledObjectNames());
      assertEquals(0, mbsnf.getDisabledObjectNames().size());

      // Disable one
      mbsnf.disableObjectName(on1);
      assertEquals(null, mbsnf.getEnabledObjectNames());
      assertEquals(1, mbsnf.getDisabledObjectNames().size());
      assertEquals(on1, mbsnf.getDisabledObjectNames().elementAt(0));

      // Disable everything
      mbsnf.disableAllObjectNames();
      assertEquals(0, mbsnf.getEnabledObjectNames().size());
      assertEquals(null, mbsnf.getDisabledObjectNames());

      // Enable one
      mbsnf.enableObjectName(on1);
      assertEquals(1, mbsnf.getEnabledObjectNames().size());
      assertEquals(null, mbsnf.getDisabledObjectNames());
      assertEquals(on1, mbsnf.getEnabledObjectNames().elementAt(0));
    }
    catch (NullPointerException e)
    {
      fail("FAILS IN RI: " + e.toString());
    }
  }

  /**
   * Test serialization.
   */
  public void testSerialization()
  {
    setUpTest();

    // Enable only one
    mbsnf.enableObjectName(on2);

    try
    {
      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(mbsnf);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      ois.readObject();
    }
    catch (IOException ioe)
    {
      fail(ioe.toString());
    }
    catch (ClassNotFoundException cnfe)
    {
      fail(cnfe.toString());
    }

    // Did it work?
    assertEquals(false, mbsnf.isNotificationEnabled(n1));
    assertEquals(true, mbsnf.isNotificationEnabled(n2));
  }

  // Support -------------------------------------------------------------------

  private void setUpTest()
  {
    mbsnf = new MBeanServerNotificationFilter();
    mbsnf.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
    try
    {
      on1 = new ObjectName(":a=a");
      on2 = new ObjectName(":b=b");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    n1 = new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION,
                                     new Object(), 1, on1);
    n2 = new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION,
                                     new Object(), 2, on2);
  }
}
