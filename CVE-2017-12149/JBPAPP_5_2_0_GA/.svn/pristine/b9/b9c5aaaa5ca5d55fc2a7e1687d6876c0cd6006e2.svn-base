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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Hammer ObjectName, making sure it spots all malformed inputs.
 *
 * This may look like overkill but it's not.  I want each
 * permutation to run independantly for full test coverage.
 *
 * This suite has twice as many tests (about 60) as my last
 * testcase - and for that it caught one extra bug for me.
 *
 * @todo check we are throwing MalformedObjectNameException and NullPointerException
 *       for the right errors
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.  
 */ 
public class MalformedSUITE extends TestSuite {
   public static final String GOOD_DOMAIN = "domain";
   public static final String GOOD_KEY = "key1";
   public static final String GOOD_VALUE = "val1";

   // strings containing illegal chars to use in keys
   public static final String[] BAD_KEYS = {
      "",            // cannot be zero sized
      "som:thing",   // cannot contain domain separator
      "som?thing",   // cannot contain pattern chars
      "som*thing",   // cannot contain pattern chars
      "som,thing",   // cannot contain kvp chunk separator
      "som=thing",   // cannot contain kvp separator
      "som\nthing",   // cannot contain \n separator
   };

   // strings containing illegal chars to use in values
   public static final String[] BAD_VALS = {
      "som:thing",   // cannot contain domain separator
      "som?thing",   // cannot contain pattern chars
      "som*thing",   // cannot contain pattern chars
      "som,thing",   // cannot contain kvp chunk separator
      "som=thing",   // cannot contain kvp separator
      "som\nthing",   // cannot contain \n separator
      "som\"thing",   // unterminated quote
      "som\"\\b\"thing",   // bad escape inside quote
      "som\"" + '\n' + "\"thing",   // new line inside quote
      "som\"\"\"thing",   // quote inside quote
      "som\"?\"thing",   // question mark inside quote
      "som\"*\"thing",   // asterisk inside quote
   };

   // domains containing illegal domain chars
   public static final String[] BAD_DOMAINS = {
      "doma:in",    // : char in domain
      "doma\nin",    // \n char in domain
   };

   // pre-cooked name strings dealing with structural malformations    
   public static final String[] BAD_FULLNAMES = {
      "domain:key=val,key=val2",    // duplicate key
      "domain:=,foo=bar",           // both key and value empty
      "domain:key=val,,foo=bar",    // missing kvp in middle
      "domain:,key=val,foo=bar",    // missing kvp at beginning
      "domain:key=val,foo=bar,",    // missing kvp at end
      "domain:key=val,   ,foo=bar", // malformed kvp, no = char
      "domain:key=val,*,*", // duplicate asterisks
      "domain:*,key=val,*", // duplicate asterisks
      "domain:*,key1=val1,*,key2=val2", // duplicate asterisks
      "domain: *,key1=val1,key2=val2", // asterisk with a leading space
      "domain:key1=val1,key2=val2, *", // asterisk with a leading space
      "domain:key1=val1,key2=val2,* ", // asterisk with a trailing space
      "domain:", // no properties
      null,                         // null is not allowed
   };

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()    {
      TestSuite suite = new TestSuite("All Malformed Tests"); 
      // Tests for nulls
      suite.addTest(new DomainKeyValueTEST(null, null, null));
      suite.addTest(new DomainKeyValueTEST(null, "key1", "val1"));
      suite.addTest(new DomainKeyValueTEST("domain", null, "val1"));
      suite.addTest(new DomainKeyValueTEST("domain", "key1", null));
      suite.addTest(new DomainKeyValueTEST("domain", null, null));
      suite.addTest(new DomainHashtableTEST(null, "key1", "val1"));

      // extra stuff related to null or zero sized hashtable
      suite.addTestSuite(DomainHashtableExtraTEST.class);

      // all illegal domain characters
      for (int i = 0; i < BAD_DOMAINS.length; i++)
      {
         suite.addTest(new FullNameTEST(BAD_DOMAINS[i] + ":" + GOOD_KEY + "=" + GOOD_VALUE));
         suite.addTest(new DomainKeyValueTEST(BAD_DOMAINS[i], GOOD_KEY, GOOD_VALUE));
         suite.addTest(new DomainHashtableTEST(BAD_DOMAINS[i], GOOD_KEY, GOOD_VALUE));
      }

      // all illegal key characters
      for (int i = 0; i < BAD_KEYS.length; i++)
      {
         suite.addTest(new FullNameTEST(GOOD_DOMAIN + ":" + BAD_KEYS[i] + "=" + GOOD_VALUE));
         suite.addTest(new DomainKeyValueTEST(GOOD_DOMAIN, BAD_KEYS[i], GOOD_VALUE));
         suite.addTest(new DomainHashtableTEST(GOOD_DOMAIN, BAD_KEYS[i], GOOD_VALUE));
      }

      // all illegal value characters
      for (int i = 0; i < BAD_VALS.length; i++)
      {
         suite.addTest(new FullNameTEST(GOOD_DOMAIN + ":" + GOOD_KEY + "=" + BAD_VALS[i]));
         suite.addTest(new DomainKeyValueTEST(GOOD_DOMAIN, GOOD_KEY, BAD_VALS[i]));
         suite.addTest(new DomainHashtableTEST(GOOD_DOMAIN, GOOD_KEY, BAD_VALS[i]));
      }

      // all the structurally malformed fullnames
      for (int i = 0; i < BAD_FULLNAMES.length; i++)
      {
         suite.addTest( new FullNameTEST(BAD_FULLNAMES[i]));
      }

      return suite;
   } 

   public static class FullNameTEST extends TestCase
   {
      private String fullName;

      public FullNameTEST(String fullName)
      {
         super("testMalformed");
         this.fullName = fullName;
      }

      public void testMalformed()
      {
         boolean caught = false;
         try
         {
            new ObjectName(fullName);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
         }
         catch (NullPointerException e)
         {
            caught = true;
            if (fullName != null)
               fail("Unexpected NullPointerException for " + fullName);
         }
         if (caught == false)
         {
            if (fullName != null)
               if (fullName.equals("domain:=val1") ||
                   fullName.equals("domain:=,foo=bar"))
                  fail("FAILS IN RI: expected a MalformedObjectNameException for: " + fullName);
            fail("expected a MalformedObjectNameException for: " + fullName);
         }

         caught = false;
         try
         {
            ObjectName.getInstance(fullName);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
         }
         catch (NullPointerException e)
         {
            caught = true;
            if (fullName != null)
               fail("Unexpected NullPointerException for " + fullName);
         }
         if (caught == false)
         {
            if (fullName != null)
               if (fullName.equals("domain:=val1") ||
                   fullName.equals("domain:=,foo=bar"))
                  fail("FAILS IN RI: expected a MalformedObjectNameException for: " + fullName);
            fail("expected a MalformedObjectNameException for: " + fullName);
         }
      }
   }

   public static class DomainKeyValueTEST extends TestCase
   {
      private String domain;
      private String key;
      private String value;

      public DomainKeyValueTEST(String domain, String key, String value)
      {
         super("testMalformed");
         this.domain = domain;
         this.key = key;
         this.value = value;
      }

      public void testMalformed()
      {
         boolean caught = false;
         try
         {
            new ObjectName(domain, key, value);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
         }
         catch (NullPointerException e)
         {
            caught = true;
            if (domain != null && key != null && value != null)
               fail("Unexpected NullPointerException for " + domain + ":" + key + "=" + value);
         }
         if (caught == false)
         {
            if (value != null)
               if (value.equals("som\"thing") ||
                   value.equals("som\"\\b\"thing") ||
                   value.equals("som\"\"\"thing"))
                  fail("FAILS IN RI: expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
            fail("expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
         }

         caught = false;
         try
         {
            ObjectName.getInstance(domain, key, value);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
         }
         catch (NullPointerException e)
         {
            caught = true;
            if (domain != null && key != null && value != null)
               fail("Unexpected NullPointerException for " + domain + ":" + key + "=" + value);
         }
         if (caught == false)
         {
            if (value != null)
               if (value.equals("som\"thing") ||
                   value.equals("som\"\\b\"thing") ||
                   value.equals("som\"\"\"thing"))
                  fail("FAILS IN RI: expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
            fail("expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
         }
      }
   }

   public static class DomainHashtableTEST extends TestCase
   {
      private String domain;
      private String key;
      private String value;

      public DomainHashtableTEST(String domain, String key, String value)
      {
         super("testMalformed");
         this.domain = domain;
         this.key = key;
         this.value = value;
      }

      public void testMalformed()
      {
         boolean caught = false;
         try
         {
            Hashtable h = new Hashtable();
            h.put(key, value);
            new ObjectName(domain, h);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
         }
         catch (NullPointerException e)
         {
            caught = true;
            if (domain != null && key != null && value != null)
               fail("Unexpected NullPointerException for " + domain + ":" + key + "=" + value);
         }
         if (caught == false)
         {
            if (value != null)
               if (value.equals("som\"thing") ||
                   value.equals("som\"\\b\"thing") ||
                   value.equals("som\"\"\"thing"))
                  fail("FAILS IN RI: expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
            fail("expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
         }

         caught = false;
         try
         {
            Hashtable h = new Hashtable();
            h.put(key, value);
            ObjectName.getInstance(domain, h);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
         }
         catch (NullPointerException e)
         {
            caught = true;
            if (domain != null && key != null && value != null)
               fail("Unexpected NullPointerException for " + domain + ":" + key + "=" + value);
         }
         if (caught == false)
         {
            if (value != null)
               if (value.equals("som\"thing") ||
                   value.equals("som\"\\b\"thing") ||
                   value.equals("som\"\"\"thing"))
                  fail("FAILS IN RI: expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
            fail("expected a MalformedObjectNameException for: " + domain + ":" + key + "=" + value);
         }
      }
   }

   public static class DomainHashtableExtraTEST extends TestCase
   {
      public DomainHashtableExtraTEST(String s)
      {
         super(s);
      }

      public void testNullDomain()
      {
         Hashtable h = new Hashtable();
         h.put(new Object(), GOOD_VALUE);
         doCheck(null, h, "<null domain>", true);
      }

      public void testNullHashtable()
      {
         doCheck(GOOD_DOMAIN, null, "<null hashtable>", true);
      }

      public void testEmptyHashtable()
      {
         doCheck(GOOD_DOMAIN, new Hashtable(), "<empty_hashtable>", false);
      }

      public void testNonStringKey()
      {
         Hashtable h = new Hashtable();
         h.put(new Object(), GOOD_VALUE);
         doCheck(GOOD_DOMAIN, h, "<non_string_key>=" + GOOD_VALUE, false);
      }

      public void testNonStringValue()
      {
         Hashtable h = new Hashtable();
         h.put(GOOD_KEY, new Object());
         doCheck(GOOD_DOMAIN, h, GOOD_KEY + "=<non_string_value>", false);
      }

      private void doCheck(String domain, Hashtable h, String failureHint, boolean expectNull)
      {
         boolean caught = true;
         try
         {
            new ObjectName(domain, h);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
            if (expectNull)
               fail("FAILS IN RI: Expected a NullPointerException for: " + domain + ":" + failureHint);
         }
         catch (NullPointerException e)
         {
            if (expectNull == false)
               fail("unexpected a NullPointerException for: " + domain + ":" + failureHint);
         }
         if (caught == false)
            fail("expected a MalformedObjectNameException for: " + domain + ":" + failureHint);

         caught = true;
         try
         {
            ObjectName.getInstance(domain, h);
         }
         catch (MalformedObjectNameException e)
         {
            caught = true;
            if (expectNull)
               fail("FAILS IN RI: Expected a NullPointerException for: " + domain + ":" + failureHint);
         }
         catch (NullPointerException e)
         {
            if (expectNull == false)
               fail("unexpected a NullPointerException for: " + domain + ":" + failureHint);
         }
         if (caught == false)
            fail("expected a MalformedObjectNameException for: " + domain + ":" + failureHint);
      }
   }
} 