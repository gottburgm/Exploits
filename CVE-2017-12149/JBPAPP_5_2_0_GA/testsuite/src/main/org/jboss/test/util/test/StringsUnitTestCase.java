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

import org.jboss.test.JBossTestCase;
import org.jboss.util.Strings;

/**
 * Unit tests for jboss Strings utility class
 *
 * @see org.jboss.util.Strings
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class StringsUnitTestCase extends JBossTestCase
{
   public StringsUnitTestCase(String name)
   {
      super(name);
   }

   /** 
    * Test Strings.parseTimePeriod()
    * 
    * @throws Exception
    */ 
   public void testParseTimePeriod() throws Exception
   {
      long result;
      
      result = Strings.parseTimePeriod("-1");
      assertTrue("expected -1 msec, got " + result, result == -1);
      
      result = Strings.parseTimePeriod("0");
      assertTrue("expected 0 msec, got " + result, result == 0);
      
      result = Strings.parseTimePeriod("1");
      assertTrue("expected 1 msec, got " + result, result == 1);
      
      result = Strings.parseTimePeriod("1msec");
      assertTrue("expected 1 msec, got " + result, result == 1);
      
      result = Strings.parseTimePeriod("1sec");
      assertTrue("expected 1000 msec, got " + result, result == 1000);
      
      result = Strings.parseTimePeriod("1min");
      assertTrue("expected 60000 msec, got " + result, result == 60000);
      
      result = Strings.parseTimePeriod("1h");
      assertTrue("expected 3600000 msec, got " + result, result == 3600000);
      
      result = Strings.parseTimePeriod("666msec");
      assertTrue("expected 666 msec, got " + result, result == 666);
      
      try
      {
         result = Strings.parseTimePeriod(null);
         fail("Expected NumberFormatException()");
      }
      catch (NumberFormatException e)
      {
         getLog().debug("Caught expected NumberFormatException: " + e.getMessage());
      }
      
      try
      {
         result = Strings.parseTimePeriod("bla");
         fail("Expected NumberFormatException()");
      }
      catch (NumberFormatException e)
      {
         getLog().debug("Caught expected NumberFormatException: " + e.getMessage());
      }
   }
   
   /** 
    * Test Strings.parsePositiveTimePeriod()
    * 
    * @throws Exception
    */ 
   public void testParsePositiveTimePeriod() throws Exception
   {
      long result;
      
      try
      {      
         result = Strings.parsePositiveTimePeriod("-1");
         fail("Expected NumberFormatException()");
      }
      catch (NumberFormatException e)
      {
         getLog().debug("Caught expected NumberFormatException: " + e.getMessage());
      }         

      result = Strings.parsePositiveTimePeriod("0");
      assertTrue("expected 0 msec, got " + result, result == 0);   
      
      result = Strings.parsePositiveTimePeriod("1");
      assertTrue("expected 1 msec, got " + result, result == 1);
   }
      
}
