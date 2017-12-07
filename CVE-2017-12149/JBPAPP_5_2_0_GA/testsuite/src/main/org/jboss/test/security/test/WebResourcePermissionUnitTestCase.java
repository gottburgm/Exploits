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
import javax.security.jacc.WebResourcePermission;

import junit.framework.TestCase;

/** Tests of the JAAC WebResourcePermission
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class WebResourcePermissionUnitTestCase
   extends TestCase
{

   public WebResourcePermissionUnitTestCase(String name)
   {
      super(name);
   }

   public void testCtor2() throws Exception
   {
      String nullActions = null;
      WebResourcePermission p = new WebResourcePermission("/", nullActions);
      String actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      p = new WebResourcePermission("", nullActions);
      actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      String[] emtpy = {};
      p = new WebResourcePermission("/", emtpy);
      actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      p = new WebResourcePermission("/", "POST");
      actions = p.getActions();
      assertTrue("actions("+actions+") == POST", actions.equals("POST"));

      p = new WebResourcePermission("/", "GET,POST,PUT,DELETE,HEAD,OPTIONS,TRACE");
      actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      p = new WebResourcePermission("/", "TRACE,GET,DELETE");
      actions = p.getActions();
      assertTrue("actions("+actions+") == DELETE,GET,TRACE",
         actions.equals("DELETE,GET,TRACE"));
   }

   public void testImpliesPermission() throws Exception
   {
      String nullActions = null;
      WebResourcePermission p0 = new WebResourcePermission("/", nullActions);
      WebResourcePermission p1 = new WebResourcePermission("/", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/", "");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p1 = new WebResourcePermission("", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      String[] emtpy = {};
      p0 = new WebResourcePermission("/", emtpy);
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/*", nullActions);
      p1 = new WebResourcePermission("/any", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/*", "GET");
      p1 = new WebResourcePermission("/any", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/any/*", "GET");
      p1 = new WebResourcePermission("/any", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p1 = new WebResourcePermission("/any/", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/any/more/*", "GET");
      p1 = new WebResourcePermission("/any/more/andsome", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("*.jsp", "POST,GET");
      p1 = new WebResourcePermission("/snoop.jsp", "GET,POST");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("*.jsp", "POST,GET,TRACE");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/snoop.jsp", "POST,GET,TRACE");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new WebResourcePermission("/:/secured.jsp:/unchecked.jsp:/excluded.jsp:/sslprotected.jsp", "POST,GET");
      p1 = new WebResourcePermission("/:/secured.jsp:/excluded.jsp:/sslprotected.jsp:/unchecked.jsp", "GET,POST");
      assertTrue("p0.implies(p1)", p0.implies(p1));
      
      p0 = new WebResourcePermission("/restricted/*", "DELETE,GET,HEAD,POST,PUT");
      p1 = new WebResourcePermission("/restricted/SecureServlet", "GET");
      assertTrue("p0.implies(p1)", p0.implies(p1));
   }

   public void testNotImpliesPermission() throws Exception
   {
      String nullActions = null;
      WebResourcePermission p0 = new WebResourcePermission("/", "GET");
      WebResourcePermission p1 = new WebResourcePermission("/", nullActions);
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebResourcePermission("/", "POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebResourcePermission("", "GET");
      assertTrue("! p1.implies(p0)", p1.implies(p0) == false);

      p1 = new WebResourcePermission("/", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebResourcePermission("/any/*", "GET");
      p1 = new WebResourcePermission("/anymore", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebResourcePermission("/anyx", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p1 = new WebResourcePermission("/any/more", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebResourcePermission("/*", "GET");
      p1 = new WebResourcePermission("/anyx", "GET,POST");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebResourcePermission("*.jsp", "GET");
      p1 = new WebResourcePermission("/", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebResourcePermission("*.jsp", "GET");
      p1 = new WebResourcePermission("/*", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebResourcePermission("*.jsp", "GET");
      p1 = new WebResourcePermission("/jsp", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new WebResourcePermission("*.jsp", "GET");
      p1 = new WebResourcePermission("/snoop,jsp", "GET");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);
   }

   public void testBestMatch() throws Exception
   {
      WebResourcePermission cp = new WebResourcePermission("/restricted/not", "GET");
      WebResourcePermission excluded = new WebResourcePermission("/restricted/*", "");
      WebResourcePermission unchecked = new WebResourcePermission("/restricted/not/*", "");
      assertTrue("cp is excluded", excluded.implies(cp));
      assertTrue("cp is unchecked", unchecked.implies(cp));

      assertTrue("unchecked is excluded", excluded.implies(unchecked));
      assertTrue("excluded is NOT unchecked", unchecked.implies(excluded) == false);

      Permissions excludedPC = new Permissions();
      excludedPC.add(new WebResourcePermission("/restricted/*", ""));
      excludedPC.add(new WebResourcePermission("/restricted/get-only/*", "DELETE,HEAD,OPTIONS,POST,PUT,TRACE"));
      excludedPC.add(new WebResourcePermission("/restricted/post-only/*", "DELETE,HEAD,OPTIONS,POST,PUT,TRACE"));
      excludedPC.add(new WebResourcePermission("/restricted/put-only/excluded/*", ""));
      excludedPC.add(new WebResourcePermission("/restricted/get-only/excluded/*", ""));
      excludedPC.add(new WebResourcePermission("/excluded/*", ""));

      Permissions uncheckedPC = new Permissions();
      uncheckedPC.add(new WebResourcePermission("/unchecked/*", ""));
      uncheckedPC.add(new WebResourcePermission("/restricted/post-only/*", "GET"));
      uncheckedPC.add(new WebResourcePermission("/restricted/not/*", ""));
      uncheckedPC.add(new WebResourcePermission("/unchecked/*:/restricted/not/*:/restricted/*:/restricted/put-only/excluded/*:/restricted/get-only/excluded/*:/restricted/any/*:/restricted/post-only/*:/restricted/get-only/*:/excluded/*", ""));

      assertTrue("unchecked is in excludedPC", excludedPC.implies(unchecked));
      assertTrue("excluded is NOT in uncheckedPC", uncheckedPC.implies(excluded) == false);
      
   }

   public void testQualifiedMatch()
   {
      WebResourcePermission p0 = new WebResourcePermission("/restricted/*:/restricted/any/excluded/*:/restricted/not/*", "");
      WebResourcePermission p1 = new WebResourcePermission("/restricted/not", "GET");
      assertFalse("/restricted/not GET is NOT implied", p0.implies(p1));
   }

   public void testQualifiedPatterns()
   {
      try
      {
         /*  No pattern may exist in the URLPatternList that matches
         the first pattern.
         */
         WebResourcePermission p = new WebResourcePermission("/:/*", "");
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
         WebResourcePermission p = new WebResourcePermission("/*:*.ext", "");
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
         WebResourcePermission p = new WebResourcePermission("*.ext:*.ext2", "");
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
         WebResourcePermission p0 = new WebResourcePermission("/:/", "");
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
         WebResourcePermission p0 = new WebResourcePermission("/exact:/*", "");
         fail("Should not have been able to use a qualifiying pattern");
      }
      catch(IllegalArgumentException e)
      {
         // Failed as expected
      }
   }
}
