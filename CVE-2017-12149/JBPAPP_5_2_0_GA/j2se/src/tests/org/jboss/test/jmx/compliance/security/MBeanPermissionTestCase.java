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
package org.jboss.test.jmx.compliance.security;

import javax.management.MBeanPermission;

import junit.framework.TestCase;

/** Tests of the javax.management.MBeanPermission
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81019 $
 */
public class MBeanPermissionTestCase
  extends TestCase
{
   public MBeanPermissionTestCase(String s)
   {
      super(s);
   }
   
   public void testCtor()
   {
      MBeanPermission p = new MBeanPermission("*", "*");
   }

   public void testImpiles()
   {
      MBeanPermission p0 = new MBeanPermission("*", "*");
      MBeanPermission p1 = new MBeanPermission("*", "*");
      assertTrue("* implies *", p1.implies(p0));

      p0 = new MBeanPermission("[*:*]", "*");
      p1 = new MBeanPermission("[*:*]", "*");
      assertTrue("[*:*] * implies [*:*] *", p1.implies(p0));

      p0 = new MBeanPermission("*", "*");
      p1 = new MBeanPermission("#", "*");
      assertTrue("# implies *", p1.implies(p0));

      p0 = new MBeanPermission("*#", "*");
      p1 = new MBeanPermission("*", "*");
      assertTrue("*# implies *", p1.implies(p0));

      p0 = new MBeanPermission("*", "addNotificationListener");
      p1 = new MBeanPermission("*", "*");
      assertTrue("* * implies * addNotificationListener", p1.implies(p0));

      p0 = new MBeanPermission("*", "queryMBeans");
      p1 = new MBeanPermission("*", "queryNames");
      assertTrue("* queryMBeans implies * queryNames", p0.implies(p1));

      p0 = new MBeanPermission("[MyDomain:type=Product]", "getAttribute");
      p1 = new MBeanPermission("test.Product#Price[MyDomain:type=Product]", "getAttribute");
      assertTrue(p0+" implies "+p1, p0.implies(p1));

      p0 = new MBeanPermission("a.b.c#d[e:f=g]", "*");
      p1 = new MBeanPermission("a.b.c#d[e:f=g]", "getAttribute");
      assertTrue(p0+" implies "+p1, p0.implies(p1));      

      p0 = new MBeanPermission("a.b.c#*[e:f=g]", "*");
      p1 = new MBeanPermission("a.b.c#d[e:f=g]", "getAttribute");
      assertTrue(p0+" implies "+p1, p0.implies(p1));      
   }

   public void testNotImpiled() throws Exception
   {
      MBeanPermission p0 = new MBeanPermission("test.Product#Price[MyDomain:type=Product]", "getAttribute");
      MBeanPermission p1 = new MBeanPermission("test.Product#Cost[MyDomain:type=Product]", "getAttribute");
      assertTrue("!p0 implies p1", p0.implies(p1) == false);      

      p0 = new MBeanPermission("a.b.c#d[e:f=g]", "*");
      p1 = new MBeanPermission("a.b.c#d[e:f=g]", "getAttribute");
      assertTrue(p1+" ! implies "+p0, p1.implies(p0) == false);      
   }
}
