/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.xml.jbpapp9156;

import java.net.MalformedURLException;

import org.jboss.test.JBossTestCase;

public class JBPAPP9156UnitTestCase extends JBossTestCase
{
   private XMLReaderValidator m = null;
   private String testXML = "org/jboss/test/xml/jbpapp9156/test.xml";
   
   public JBPAPP9156UnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      log.trace("Setup called");
      m = new XMLReaderValidator(log);
   }

   public void testJBPAPP9156()
   {
      try
      {
         boolean result = m.parse(getResourceURL(testXML));
         log.info("Assertion SUCCESS Because the XML is valid and Parsed Successfully And returned TRUE.");
         assertTrue("Failed, result should be true and it was: " + result, result == true);
      }
      catch(MalformedURLException e)
      {
         fail("testXML: " + testXML + " is malformed, it should not be");
         e.printStackTrace();
      }
      catch(StackOverflowError se)
      {
         fail("With JBPAPP-9156 applied, there should not be a StackOverflowError - " + se.getClass().getName() + " - " + se.getMessage());
         se.printStackTrace();
      }
      catch(Exception e)
      {
         fail("With JBPAPP-9156 applied, there should not be an exception " +  e.getClass().getName() + " - " + e.getMessage());
         e.printStackTrace();
      }
   }
}
