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
package org.jboss.test.jbossmx.compliance.objectname;

import org.jboss.test.jbossmx.compliance.TestCase;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Hashtable;

public class CanonicalTestCase
   extends TestCase
{
   public static final String EXPECTED_NAME = "domain:a=a,b=b,c=c,d=d,e=e";
   public static final String[] KVP = {"a", "b", "c", "d", "e"};

   public CanonicalTestCase(String s)
   {
      super(s);
   }

   public void testBasicCanonical()
   {
      try
      {
         ObjectName name = new ObjectName("domain:e=e,b=b,d=d,c=c,a=a");
         assertEquals(EXPECTED_NAME, name.getCanonicalName());
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException");
      }
   }

   public void testHashtableCanonical()
   {
      try
      {
         Hashtable h = new Hashtable();
         for (int i = 0; i < KVP.length; i++)
         {
            h.put(KVP[i], KVP[i]);
         }
         ObjectName name = new ObjectName("domain", h);
         assertEquals(EXPECTED_NAME, name.getCanonicalName());
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException");
      }
   }

   public void testSingleKVP()
   {
      try
      {
         ObjectName name = new ObjectName("domain", "a", "a");
         assertEquals("domain:a=a", name.getCanonicalName());
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException");
      }
   }
}
