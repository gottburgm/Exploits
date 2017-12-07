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

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.jboss.net.protocol.file.FileURLLister;
import org.jboss.test.JBossTestCase;

/**
 * FileURLLister tests
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
**/
public class FileURLListerUnitTestCase extends JBossTestCase
{
   public FileURLListerUnitTestCase(String name)
   {
      super(name);
   }

   public void testListDeployLikeDirStructureDontRecurse() throws Exception
   {
      getLog().debug("+++ testListDeployLikeDirStructureDontRecurse");
      
      String baseUrlString = super.getResourceURL("util/fileurllister/deploy/");
      URL baseUrl = new URL(baseUrlString);
      
      FileURLLister lister = new FileURLLister();
      Collection files = lister.listMembers(baseUrl, "*", false);
      
      logResult(files);
      
      String[] expected = new String[] {
            "dotted.sub.dir/",
            "nondottedsubdir/",
            "one.xml",
            "two.xml"
      };
      
      checkResult(files, baseUrlString, expected);
   }
   
   public void testListDeployLikeDirStructureRecurse() throws Exception
   {
      getLog().debug("+++ testListDeployLikeDirStructureRecurse");
      
      String baseUrlString = super.getResourceURL("util/fileurllister/deploy/");
      URL baseUrl = new URL(baseUrlString);
      
      FileURLLister lister = new FileURLLister();
      Collection files = lister.listMembers(baseUrl, "*", true);
      
      logResult(files);
      
      String[] expected = new String[] {
            "dotted.sub.dir/",
            "nondottedsubdir/three.xml",
            "one.xml",
            "two.xml",
      };
      
      checkResult(files, baseUrlString, expected);
   }
   
   public void testListDeployLikeDirStructureDontRecurseWithFilter() throws Exception
   {
      getLog().debug("+++ testListDeployLikeDirStructureDontRecurseWithFilter");
      
      String baseUrlString = super.getResourceURL("util/fileurllister/deploy/");
      URL baseUrl = new URL(baseUrlString);
      
      FileURLLister lister = new FileURLLister();
      Collection files = lister.listMembers(baseUrl, "one.xml,nondottedsubdir,three.xml", false);
      
      logResult(files);
      
      String[] expected = new String[] {
            "nondottedsubdir/",
            "one.xml"
      };
      
      checkResult(files, baseUrlString, expected);
   }
   
   public void testListDeployLikeDirStructureRecurseWithFilter() throws Exception
   {
      getLog().debug("+++ testListDeployLikeDirStructureRecurseWithFilter");
      
      String baseUrlString = super.getResourceURL("util/fileurllister/deploy/");
      URL baseUrl = new URL(baseUrlString);
      
      FileURLLister lister = new FileURLLister();
      Collection files = lister.listMembers(baseUrl, "one.xml,nondottedsubdir,three.xml", true);
      
      logResult(files);
      
      String[] expected = new String[] {
            "nondottedsubdir/three.xml",
            "one.xml"
      };
      
      checkResult(files, baseUrlString, expected);
   }
   
   private void logResult(Collection result)
   {
      for (Iterator i = result.iterator(); i.hasNext(); )
      {
         URL url = (URL)i.next();
         getLog().debug(url.toString());
      }
   }
   
   private void checkResult(Collection result, String baseUrlString, String[] expected) throws Exception
   {
      assertTrue("result.size(" + result.size() + ") != expected.length(" + expected.length + ")",
            result.size() == expected.length);
      
      for (int i = 0; i < expected.length; i++)
      {
         URL excepted = new URL(baseUrlString + expected[i]);
         assertTrue("Expected URL: " + excepted, result.contains(excepted));
      }
   }
}
