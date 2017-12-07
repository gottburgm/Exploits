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

import javax.management.relation.RoleStatus;

import junit.framework.TestCase;

/**
 * Role Status tests
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class RoleStatusTestCase
  extends TestCase
{

  // Constants -----------------------------------------------------------------

  static int[] statii = new int[]
  {
    RoleStatus.LESS_THAN_MIN_ROLE_DEGREE,
    RoleStatus.MORE_THAN_MAX_ROLE_DEGREE,
    RoleStatus.NO_ROLE_WITH_NAME,
    RoleStatus.REF_MBEAN_NOT_REGISTERED,
    RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS,
    RoleStatus.ROLE_NOT_READABLE,
    RoleStatus.ROLE_NOT_WRITABLE
  };

  static String[] statiiDesc = new String[]
  {
    "LESS_THAN_MIN_ROLE_DEGREE",
    "MORE_THAN_MAX_ROLE_DEGREE",
    "NO_ROLE_WITH_NAME",
    "REF_MBEAN_NOT_REGISTERED",
    "REF_MBEAN_OF_INCORRECT_CLASS",
    "ROLE_NOT_READABLE",
    "ROLE_NOT_WRITABLE"
  };

  // Attributes ----------------------------------------------------------------

  // Constructor ---------------------------------------------------------------

  /**
   * Construct the test
   */
  public RoleStatusTestCase(String s)
  {
    super(s);
  }

  // Tests ---------------------------------------------------------------------

  /**
   * Make sure all the constants are different
   */
  public void testDifferent()
  {
    for (int i = 0; i < (statii.length - 1); i++)
    {
      for (int j = i + 1; j < statii.length; j++)
        if (statii[i] == statii[j])
          fail("RoleStatus constants are not unique");
    }
  }

  /**
   * Make sure all the constants are accepted
   */
  public void testRoleStatus()
  {
    for (int i = 0; i < statii.length; i++)
    {
       if (RoleStatus.isRoleStatus(statii[i]) == false)
         fail(statiiDesc + " is not a role status");
    }
  }
}
