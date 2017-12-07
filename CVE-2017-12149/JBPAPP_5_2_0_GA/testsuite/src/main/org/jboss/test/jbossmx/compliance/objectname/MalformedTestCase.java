/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jbossmx.compliance.objectname;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.jbossmx.compliance.TestCase;
import org.jboss.util.platform.Java;

/**
 * Hammer ObjectName, making sure it spots all malformed inputs.
 * <p/>
 * This may look like overkill but it's not.  I want each
 * permutation to run independantly for full test coverage.
 * <p/>
 * This suite has twice as many tests (about 60) as my last
 * testcase - and for that it caught one extra bug for me.
 * 
 * @author <a href="mailto:trevor@protocool.com">Trevor Squires</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 73169 $
 */
public class MalformedTestCase extends TestSuite
{
   public static final String GOOD_DOMAIN = "domain";
   public static final String GOOD_KEY = "key1";
   public static final String GOOD_VALUE = "val1";
   public static String[] BAD_KEYVALS = null;

   // strings containing illegal chars to use in key or value positions
   // JBAS-5031 WildCards are allowed with JDK 6
   public static final String[] BAD_KEYVALS_JDK5 = {
      "som:thing", // cannot contain domain separator
      "som?thing", // cannot contain pattern chars
      "som*thing", // cannot contain pattern chars
      "som,thing", // cannot contain kvp chunk separator
      "som=thing", // cannot contain kvp separator
   };

   public static final String[] BAD_KEYVALS_JDK6 = {
      "som:thing", // cannot contain domain separator
      "som,thing", // cannot contain kvp chunk separator
      "som=thing", // cannot contain kvp separator
   };

   // domains containing illegal domain chars
   public static final String[] BAD_DOMAINS = {
      "doma:in", // : char in domain
   };

   // pre-cooked name strings dealing with structural malformations
   public static final String[] BAD_FULLNAMES = {
      "domain:key=val,key=val2", // duplicate key
      "domain:key=val,,foo=bar", // missing kvp in middle
      "domain:,key=val,foo=bar", // missing kvp at beginning
      "domain:key=val,foo=bar,", // missing kvp at end
      "domain:key=val,   ,foo=bar", // malformed kvp, no = char
   };

   public MalformedTestCase(String s)
   {
      super(s);
   }

   public static Test suite()
   {
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

      // all illegal key value characters

      if (Java.isVersion(Java.VERSION_1_5) == true) {
	BAD_KEYVALS = BAD_KEYVALS_JDK5; 
      } else {
	BAD_KEYVALS = BAD_KEYVALS_JDK6; 
      }

      for (int i = 0; i < BAD_KEYVALS.length; i++)
      {
         suite.addTest(new FullNameTEST(GOOD_DOMAIN + ":" + BAD_KEYVALS[i] + "=" + GOOD_VALUE));
         suite.addTest(new FullNameTEST(GOOD_DOMAIN + ":" + GOOD_KEY + "=" + BAD_KEYVALS[i]));
         suite.addTest(new DomainKeyValueTEST(GOOD_DOMAIN, BAD_KEYVALS[i], GOOD_VALUE));
         suite.addTest(new DomainKeyValueTEST(GOOD_DOMAIN, GOOD_KEY, BAD_KEYVALS[i]));
         suite.addTest(new DomainHashtableTEST(GOOD_DOMAIN, BAD_KEYVALS[i], GOOD_VALUE));
         suite.addTest(new DomainHashtableTEST(GOOD_DOMAIN, GOOD_KEY, BAD_KEYVALS[i]));
      }

      // all the structurally malformed fullnames
      for (int i = 0; i < BAD_FULLNAMES.length; i++)
      {
         suite.addTest(new FullNameTEST(BAD_FULLNAMES[i]));
      }
      if (Java.isVersion(Java.VERSION_1_5) == false)
      {
         // exclude test when running under jdk5 - it's broken
         suite.addTest(new FullNameTEST("domain:=val,foo=bar")); // JBAS-3615, empty key
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
         try
         {
            ObjectName name = new ObjectName(fullName);
         }
         catch (MalformedObjectNameException e)
         {
            if (fullName != null)
            {
               return;
            }
         }
         catch (NullPointerException e)
         {
            if (fullName == null)
            {
               return;
            }
         }
         fail("invalid object name: " + fullName);
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
         try
         {
            ObjectName name = new ObjectName(domain, key, value);
         }
         catch (MalformedObjectNameException e)
         {
            if (domain != null && key != null && value != null)
            {
               return;
            }
         }
         catch (NullPointerException e)
         {
            if (domain == null || key == null || value == null)
            {
               return;
            }
         }
         fail("invalid object name: " + domain + ":" + key + "=" + value);
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
         try
         {
            Hashtable h = new Hashtable();
            h.put(key, value);
            ObjectName name = new ObjectName(domain, h);
         }
         catch (MalformedObjectNameException e)
         {
            if (domain != null && key != null && value != null)
            {
               return;
            }
         }
         catch (NullPointerException e)
         {
            if (domain == null || key == null || value == null)
            {
               return;
            }
         }
         fail("invalid object name: " + domain + ":" + key + "=" + value);
      }
   }

   public static class DomainHashtableExtraTEST extends TestCase
   {
      public DomainHashtableExtraTEST(String s)
      {
         super(s);
      }

      public void testNullHashtable()
      {
         doCheck(GOOD_DOMAIN, null, "<null>");
      }

      public void testEmptyHashtable()
      {
         doCheck(GOOD_DOMAIN, new Hashtable(), "<empty_hashtable>");
      }

      public void testNonStringKey()
      {
         Hashtable h = new Hashtable();
         h.put(new Object(), GOOD_VALUE);
         doCheck(GOOD_DOMAIN, h, "<non_string_key>=" + GOOD_VALUE);
      }

      public void testNonStringValue()
      {
         Hashtable h = new Hashtable();
         h.put(GOOD_KEY, new Object());
         doCheck(GOOD_DOMAIN, h, GOOD_KEY + "=<non_string_value>");
      }

      private void doCheck(String domain, Hashtable table, String failureHint)
      {
         try
         {
            ObjectName name = new ObjectName(domain, table);
         }
         catch (MalformedObjectNameException e)
         {
            if (domain != null && table != null)
            {
               return;
            }
         }
         catch (NullPointerException e)
         {
            if (domain == null || table == null)
            {
               return;
            }
         }
         catch (ClassCastException cce)
         {
  	    if (Java.isVersion(Java.VERSION_1_5) == false)
            {
               return;
            }
         }
         fail("invalid object name: " + domain + ":" + failureHint);
      }
   }
}
