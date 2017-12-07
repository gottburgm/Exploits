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
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests quoting and unquoting
 *
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.  
 */ 
public class QuoteSUITE 
   extends TestSuite
{
   private static final String EMPTY = "";
   private static final String WHITESPACE = " ";
   private static final String DOMAIN = "domain";
   private static final String LETTER = "A";
   private static final String QUOTE = "\"";
   private static final String ESCAPE = "\\";
   private static final String ASTERISK = "*";
   private static final String QUESTION = "?";
   private static final String NL = "\n";
   private static final String NEWLINE = ESCAPE + "n";
   private static final String COLON = ":";
   private static final String COMMA = ",";
   private static final String EQUALS = "=";
   private static final String KEY = "type";
   private static final String VALUE = "user";
   private static final String JMI = "JMImplementation";
   private static final String TYPE = "type";
   private static final String DELEGATE = "MBeanServerDelegate";

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("All Quote Tests"); 

      // Characters that need escaping
      suite.addTest(new QuoteTEST(QUOTE, ESCAPE + QUOTE));
      suite.addTest(new QuoteTEST(ESCAPE, ESCAPE + ESCAPE));
      suite.addTest(new QuoteTEST(QUESTION, ESCAPE + QUESTION));
      suite.addTest(new QuoteTEST(ASTERISK, ESCAPE + ASTERISK));
      suite.addTest(new QuoteTEST(NL, NEWLINE));

      // Special ObjectName characters that don't need escaping
      suite.addTest(new QuoteTEST(COLON, COLON));
      suite.addTest(new QuoteTEST(COMMA, COMMA));
      suite.addTest(new QuoteTEST(EQUALS, EQUALS));

      // Tests with no special considerations
      suite.addTest(new QuoteTEST(EMPTY, EMPTY));
      suite.addTest(new QuoteTEST(WHITESPACE, WHITESPACE));
      suite.addTest(new QuoteTEST(LETTER, LETTER));

      // Here's the one from the spec
      suite.addTest(new QuoteTEST(ASTERISK + COLON + KEY + EQUALS + VALUE + COMMA + ASTERISK,
                                  ESCAPE + ASTERISK + COLON + KEY + EQUALS + VALUE + COMMA + ESCAPE + ASTERISK));

      // And the delegate
      suite.addTest(new QuoteTEST(JMI + COLON + TYPE + EQUALS + DELEGATE,
                                  JMI + COLON + TYPE + EQUALS + DELEGATE));

      // And select everything
      suite.addTest(new QuoteTEST(ASTERISK + COLON + ASTERISK,
                                  ESCAPE + ASTERISK + COLON + ESCAPE + ASTERISK));

      // Unquote escaped characters
      suite.addTest(new UnquoteTEST(ESCAPE + QUOTE, QUOTE));
      suite.addTest(new UnquoteTEST(ESCAPE + ESCAPE, ESCAPE));
      suite.addTest(new UnquoteTEST(ESCAPE + QUESTION, QUESTION));
      suite.addTest(new UnquoteTEST(ESCAPE + ASTERISK, ASTERISK));

      // Unquote special object name characters
      suite.addTest(new UnquoteTEST(COLON, COLON));
      suite.addTest(new UnquoteTEST(COMMA, COMMA));
      suite.addTest(new UnquoteTEST(EQUALS, EQUALS));

      // Unquote with no special considerations
      suite.addTest(new UnquoteTEST(EMPTY, EMPTY));
      suite.addTest(new UnquoteTEST(WHITESPACE, WHITESPACE));
      suite.addTest(new UnquoteTEST(LETTER, LETTER));

      // Here's the one from the spec
      suite.addTest(new UnquoteTEST(ESCAPE + ASTERISK + COLON + KEY + EQUALS + VALUE + COMMA + ESCAPE + ASTERISK,
                                    ASTERISK + COLON + KEY + EQUALS + VALUE + COMMA + ASTERISK));

      // And the delegate
      suite.addTest(new UnquoteTEST(JMI + COLON + TYPE + EQUALS + DELEGATE,
                                    JMI + COLON + TYPE + EQUALS + DELEGATE));

      // And select everything
      suite.addTest(new UnquoteTEST(ESCAPE + ASTERISK + COLON + ESCAPE + ASTERISK,
                                    ASTERISK + COLON + ASTERISK));

      // Must be quoted
      suite.addTest(new UnquoteFailuresTEST(EMPTY));
      suite.addTest(new UnquoteFailuresTEST(LETTER + QUOTE + LETTER + QUOTE));
      suite.addTest(new UnquoteFailuresTEST(QUOTE + LETTER + QUOTE + LETTER));

      // Unterminated quote
      suite.addTest(new UnquoteFailuresTEST(QUOTE + LETTER));

      // Characters must be escaped
      suite.addTest(new UnquoteFailuresTEST(QUOTE + QUOTE + QUOTE));
      suite.addTest(new UnquoteFailuresTEST(QUOTE + ESCAPE + QUOTE));
      suite.addTest(new UnquoteFailuresTEST(QUOTE + QUESTION + QUOTE));
      suite.addTest(new UnquoteFailuresTEST(QUOTE + ASTERISK + QUOTE));

      return suite;
   } 

   public static class QuoteTEST
      extends TestCase
   {
      private String original;
      private String expectedResult;

      public QuoteTEST(String original, String expectedResult)
      {
         super("testQuote");
         this.original = original;
         this.expectedResult = QUOTE + expectedResult + QUOTE;
      }

      public void testQuote()
         throws Exception
      {
         String quoted = ObjectName.quote(original);
         assertTrue("The quoted string for " + original + " should be " + 
                    expectedResult + " but got " + quoted, expectedResult.equals(quoted));

         String quoteUnquote = ObjectName.unquote(quoted);
         assertTrue("quote/unquote should produce the original string " +
                    original + " but got " + quoteUnquote,
                    original.equals(quoteUnquote));

         new ObjectName(DOMAIN, KEY, quoted);
      }
   }

   public static class UnquoteTEST
      extends TestCase
   {
      private String original;
      private String expectedResult;

      public UnquoteTEST(String original, String expectedResult)
      {
         super("testUnquote");
         this.original = QUOTE + original + QUOTE;
         this.expectedResult = expectedResult;
      }

      public void testUnquote()
         throws Exception
      {
         String unquoted = ObjectName.unquote(original);
         assertTrue("The unquoted string for " + original + " should be " + 
                    expectedResult + " but got " + unquoted, expectedResult.equals(unquoted));
      }
   }

   public static class UnquoteFailuresTEST
      extends TestCase
   {
      private String test;

      public UnquoteFailuresTEST(String test)
      {
         super("testUnquoteFailures");
         this.test = test;
      }

      public void testUnquoteFailures()
         throws Exception
      {
         boolean caught = false;
         try
         {
            ObjectName.unquote(test);
         }
         catch (Exception e)
         {
            caught = true;
         }
         assertTrue("The value " + test + " should fail in unquote", caught);
      }
   }
} 