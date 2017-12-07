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
package org.jboss.test.security.test;

import java.security.Permissions;
import javax.security.jacc.WebUserDataPermission;

import junit.framework.TestCase;

/** Tests of the JAAC WebUserDataPermission
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class WebUserDataPermissionUnitTestCase
   extends TestCase
{

   public WebUserDataPermissionUnitTestCase(String name)
   {
      super(name);
   }

   public void testCtor2() throws Exception
   {
      String nullActions = null;
      WebUserDataPermission p = new WebUserDataPermission("/", nullActions);
      String actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);
      
      p = new WebUserDataPermission("", "POST");
      actions = p.getActions();
      assertTrue("actions("+actions+") == POST", actions.equals("POST"));

      p = new WebUserDataPermission("/", "POST");
      actions = p.getActions();
      assertTrue("actions("+actions+") == POST", actions.equals("POST"));

      p = new WebUserDataPermission("/", "GET,POST,PUT,DELETE,HEAD,OPTIONS,TRACE");
      actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      p = new WebUserDataPermission("/", "TRACE,GET,DELETE");
      actions = p.getActions();
      assertTrue("actions("+actions+") == DELETE,GET,TRACE",
         actions.equals("DELETE,GET,TRACE"));

      p = new WebUserDataPermission("/", "TRACE,GET,DELETE:NONE");
      actions = p.getActions();
      assertTrue("actions("+actions+") == DELETE,GET,TRACE",
         actions.equals("DELETE,GET,TRACE"));

      p = new WebUserDataPermission("/", "TRACE,GET,DELETE:CONFIDENTIAL");
      actions = p.getActions();
      assertTrue("actions("+actions+") == DELETE,GET,TRACE:CONFIDENTIAL",
         actions.equals("DELETE,GET,TRACE:CONFIDENTIAL"));
   }

   public void testImpliesPermission() throws Exception
   {
      String nullActions = null;
      WebUserDataPermission p0 = new WebUserDataPermission("/", nullActions);
      WebUserDataPermission p1 = new WebUserDataPermission("/", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/", "");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p1 = new WebUserDataPermission("", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/*", nullActions);
      p1 = new WebUserDataPermission("/any", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/*", "GET");
      p1 = new WebUserDataPermission("/any", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/any/*", "GET");
      p1 = new WebUserDataPermission("/any", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p1 = new WebUserDataPermission("/any/", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/any/more/*", "GET");
      p1 = new WebUserDataPermission("/any/more/andsome", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("*.jsp", "POST,GET");
      p1 = new WebUserDataPermission("/snoop.jsp", "GET,POST");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p1 = new WebUserDataPermission("/snoop.jsp", "GET,POST:NONE");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("*.jsp", "POST,GET,TRACE");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/snoop.jsp", "POST,GET,TRACE");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("/:/secured.jsp:/unchecked.jsp:/excluded.jsp:/sslprotected.jsp", "POST,GET");
      p1 = new WebUserDataPermission("/:/secured.jsp:/excluded.jsp:/sslprotected.jsp:/unchecked.jsp", "GET,POST");
      assertTrue("p0.implies(p1)", p0.implies(p1));
      
      p0 = new WebUserDataPermission("*.jsp", "POST,GET,TRACE:NONE");
      p1 = new WebUserDataPermission("/snoop.jsp", "GET,POST");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebUserDataPermission("*.jsp", "POST,GET,TRACE:CONFIDENTIAL");
      p1 = new WebUserDataPermission("/snoop.jsp", "GET,POST:CONFIDENTIAL");
      assertTrue("p0.implies(p1)", p0.implies(p1));
   }

   public void testNotImpliesPermission() throws Exception
   {
      String nullActions = null;
      WebUserDataPermission p0 = new WebUserDataPermission("/", "GET");
      WebUserDataPermission p1 = new WebUserDataPermission("/", nullActions);
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebUserDataPermission("/", "POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("", "");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebUserDataPermission("/", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("/any/*", "GET");
      p1 = new WebUserDataPermission("/anymore", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebUserDataPermission("/anyx", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebUserDataPermission("/any/more", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("/*", "GET");
      p1 = new WebUserDataPermission("/anyx", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("*.jsp", "GET");
      p1 = new WebUserDataPermission("/", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("*.jsp", "GET");
      p1 = new WebUserDataPermission("/*", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("*.jsp", "GET");
      p1 = new WebUserDataPermission("/jsp", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("*.jsp", "GET");
      p1 = new WebUserDataPermission("/snoop,jsp", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("*.jsp", "POST,GET,TRACE:CONFIDENTIAL");
      p1 = new WebUserDataPermission("/snoop.jsp", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebUserDataPermission("*.jsp", "POST,GET,TRACE:CONFIDENTIAL");
      p1 = new WebUserDataPermission("/snoop.jsp", "GET,POST:INTEGRAL");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);
   }

   public void testMatch()
   {
      Permissions perms = new Permissions();
      WebUserDataPermission p = new WebUserDataPermission("/protected/exact/get/roleA",
         "DELETE,HEAD,OPTIONS,POST,PUT,TRACEL");
      perms.add(p);
      p = new WebUserDataPermission("/protected/exact/get/roleA", "GET");
      perms.add(p);

      p = new WebUserDataPermission("/protected/exact/get/roleA", null);
      assertFalse("/protected/exact/get/roleA null is implied", perms.implies(p));
   }

   public void testQualifiedPatterns()
   {
      try
      {
         /*  No pattern may exist in the URLPatternList that matches
         the first pattern.
         */
         WebUserDataPermission p = new WebUserDataPermission("/:/*", "");
         fail("Should not have been able to use a pattern with matching qualifiying pattern");
      }
      catch(IllegalArgumentException e)
      {
         // Failed as expected
      }

      try
      {
         /*  If the first pattern is a path-prefix pattern, only exact
         patterns matched by the first pattern and path-prefix patterns
         matched by, but different from, the first pattern may occur
         in the URLPatternList.
         */
         WebUserDataPermission p = new WebUserDataPermission("/*:*.ext", "");
         fail("Should not have been able to use a pattern with extension qualifiying pattern");
      }
      catch(IllegalArgumentException e)
      {
         // Failed as expected
      }
      
      try
      {
         /*  If the first pattern is an extension pattern, only exact
         patterns that are matched by the first pattern and path-prefix
         patterns may occur in the URLPatternList.
         */
         WebUserDataPermission p = new WebUserDataPermission("*.ext:*.ext2", "");
         fail("Should not have been able to use an extension in qualifiying pattern");
      }
      catch(IllegalArgumentException e)
      {
         // Failed as expected
      }

      try
      {
         /*  If the first pattern is the default pattern, "/", any
         pattern except the default pattern may occur in the
         URLPatternList.
         */
         WebUserDataPermission p0 = new WebUserDataPermission("/:/", "");
         fail("Should not have been able to use the default pattern in qualifiying pattern");
      }
      catch(IllegalArgumentException e)
      {
         // Failed as expected
      }

      try
      {
         /*  If the first pattern is an exact pattern a URLPatternList
         must not be present in the URLPatternSpec.
         */
         WebUserDataPermission p0 = new WebUserDataPermission("/exact:/*", "");
         fail("Should not have been able to use a qualifiying pattern");
      }
      catch(IllegalArgumentException e)
      {
         // Failed as expected
      }
   }
}
