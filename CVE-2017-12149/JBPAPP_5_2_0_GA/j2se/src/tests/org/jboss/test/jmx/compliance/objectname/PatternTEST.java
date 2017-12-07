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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

public class PatternTEST extends TestCase
{
   public PatternTEST(String s)
   {
      super(s);
   }

   public void testBasicDomainPattern()
   {
      String nameArg = "*:key1=val1,key2=val2";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertEquals("toString should be: '" + nameArg + "'", nameArg, name.toString());
      assertTrue("isPropertyPattern should be false", !name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      assertEquals("*", name.getDomain());
   }

   public void testBasicDomainPatternExtra()
   {
      String nameArg = "**:key1=val1,key2=val2";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertEquals("toString should be: '" + nameArg + "'", nameArg, name.toString());
      assertTrue("isPropertyPattern should be false", !name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      assertEquals("**", name.getDomain());
   }

   public void testPartialDomainPattern()
   {
      String nameArg = "*domain:key1=val1,key2=val2";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertEquals("toString should be: '" + nameArg + "'", nameArg, name.toString());
      assertTrue("isPropertyPattern should be false", !name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      assertEquals("*domain", name.getDomain());
   }

   public void testHarderPartialDomainPattern()
   {
      String nameArg = "d*n:key1=val1,key2=val2";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertEquals("toString should be: '" + nameArg + "'", nameArg, name.toString());
      assertTrue("isPropertyPattern should be false", !name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      assertEquals("d*n", name.getDomain());
   }

   public void testHarderPartialDomainPatternExtra()
   {
      String nameArg = "d**n:key1=val1,key2=val2";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertEquals("toString should be: '" + nameArg + "'", nameArg, name.toString());
      assertTrue("isPropertyPattern should be false", !name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      assertEquals("d**n", name.getDomain());
   }

   public void testPositionalDomainPattern()
   {
      String nameArg = "do??in:key1=val1,key2=val2";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertEquals("toString should be: '" + nameArg + "'", nameArg, name.toString());
      assertTrue("isPropertyPattern should be false", !name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      assertEquals("do??in", name.getDomain());
   }

   public void testPatternOnly()
   {
      String nameArg = "*:*";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertTrue("isPropertyPattern should be true", name.isPropertyPattern());
      assertTrue("isDomainPattern should be true", name.isDomainPattern());
      // The RI incorrectly (IMHO) removes the * from propertyPatterns
      assertEquals("FAILS IN RI", nameArg, name.getCanonicalName());
   }

   public void testKeyPatternOnly()
   {
      String nameArg = "domain:*";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertTrue("isPropertyPattern should be true", name.isPropertyPattern());
      assertTrue("isDomainPattern should be false", !name.isDomainPattern());
      // The RI incorrectly (IMHO) removes the * from propertyPatterns
      assertEquals("FAILS IN RI", nameArg, name.getCanonicalName());
      assertTrue("key properties hash should be zero size", 0 == name.getKeyPropertyList().size());
   }

   public void testPartialKeyPattern()
   {
      String nameArg = "domain:key2=val2,*,key1=val1";
      ObjectName name = constructSafely(nameArg);
      assertTrue("isPattern should be true", name.isPattern());
      assertTrue("isPropertyPattern should be true", name.isPropertyPattern());
      assertTrue("isDomainPattern should be false", !name.isDomainPattern());
      // The RI incorrectly (IMHO) removes the * from propertyPatterns
      assertEquals("FAILS IN RI", "domain:key1=val1,key2=val2,*", name.getCanonicalName());
      assertTrue("key properties hash should only have 2 elements", 2 == name.getKeyPropertyList().size());
   }

   public void testEquality_a()
   {
      ObjectName pat1 = constructSafely("domain:*,key=value");
      ObjectName pat2 = constructSafely("domain:key=value,*");
      assertEquals(pat1, pat2);
   }

   public void testEquality_b()
   {
      ObjectName pat1 = constructSafely("do**main:key=value,*");
      ObjectName pat2 = constructSafely("do*main:key=value,*");
      assertTrue(".equals() should return false", !pat1.equals(pat2));
   }

   public void testEquality_c()
   {
      ObjectName conc = constructSafely("domain:key=value");
      ObjectName pat = constructSafely("domain:key=value,*");
      assertTrue("toString() should not match", conc.toString().equals(pat.toString()) == false);
      assertTrue("equals() should be false", !conc.equals(pat));
   }

   private ObjectName constructSafely(String nameArg)
   {
      ObjectName name = null;
      try
      {
         name = new ObjectName(nameArg);
      }
      catch (MalformedObjectNameException e)
      {
         fail("spurious MalformedObjectNameException on ('" + nameArg + "')");
      }

      return name;
   }
}
