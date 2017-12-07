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
package org.jboss.test.xslt.test;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Test an mbean deployment using a legacy xalan version using scoping
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class ScopedXalanUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return getDeploySetup(ScopedXalanUnitTestCase.class, "xalan-check-default.sar,xalan-check-scoped.sar");
   }
   
   public ScopedXalanUnitTestCase(String name)
   {
      super(name);
   }

   public void testScopedXalanDeployment() throws Exception
   {
      getLog().info("+++ testScopedXalanDeployment");
      
      // log the xalan environment seeing by the default deployment
      ObjectName defaultTarget = new ObjectName("jboss.test:service=XalanCheckDefault");
      Hashtable defaultHashtable = (Hashtable)getServer().invoke(defaultTarget, "fetchXalanEnvironmentHash", new Object[] {}, new String[] {});
      logHashtable("XalanCheckDefault", defaultHashtable);
      
      // get the xalan version of the default deployment
      String defaultVersion = (String)getServer().getAttribute(defaultTarget, "XalanVersion");
      
      // check if Bug15140 exists in the default deployment
      boolean defaultXalan25Bug15140;
      try
      {
         getServer().invoke(defaultTarget, "testXalan25Bug15140", new Object[] {}, new String[] {});
         defaultXalan25Bug15140 = false;
      }
      catch (Exception e)
      {
         defaultXalan25Bug15140 = true;
      }
      
      // log the xalan environment seeing by the scoped deployment      
      ObjectName scopedTarget = new ObjectName("jboss.test:service=XalanCheckScoped");      
      Hashtable scopedHashtable = (Hashtable)getServer().invoke(scopedTarget, "fetchXalanEnvironmentHash", new Object[] {}, new String[] {});
      logHashtable("XalanCheckScoped", scopedHashtable);
      
      // get the xalan version of the scoped deployment      
      String scopedVersion = (String)getServer().getAttribute(scopedTarget, "XalanVersion");
      
      // check if Bug15140 exists in the scoped deployment      
      boolean scopedXalan25Bug15140;
      try
      {
         getServer().invoke(scopedTarget, "testXalan25Bug15140", new Object[] {}, new String[] {});
         scopedXalan25Bug15140 = false;
      }
      catch (Exception e)
      {
         scopedXalan25Bug15140 = true;
      }
      
      getLog().info("*******************************************************");
      getLog().info("Default deployment uses xalan version: " + defaultVersion);
      getLog().info("Default deployment sees xalan v2.5.2 bug 15140: " + defaultXalan25Bug15140);
      getLog().info("Scoped deployment uses xalan version : " + scopedVersion);
      getLog().info("Scoped deployment sees xalan v2.5.2 bug 15140: " + scopedXalan25Bug15140);
      getLog().info("*******************************************************");
      
      // We expect to be seeing the scoped version
      assertTrue("Expected scoped deployment using xalan version 'Xalan Java 2.5.2'", scopedVersion.equals("Xalan Java 2.5.2") == true);
      // This can be verified by the presence of the 15140 xalan bug
      assertTrue("Expected scoped deployment to have the xalan v2.5.2 bug 15140", scopedXalan25Bug15140 == true);
      // This happens if you move xalan.jar from lib/endorsed to server/default/lib, under jdk1.4
      assertTrue("Expected default deployment NOT to see jdk1.4 xalan v2.4.1", defaultVersion.equals("Xalan Java 2.4.1") == false);
      // This is true, because we don't use 2.5.2
      assertTrue("Expected default deployment NOT to have the xalan v2.5.2 bug 15140", defaultXalan25Bug15140 == false);
   }
   
   private void logHashtable(String name, Hashtable htab)
   {
      getLog().info("***" + name + "***");
      Iterator i = htab.entrySet().iterator();
      while (i.hasNext())
      {
         Map.Entry entry = (Map.Entry)i.next();
         getLog().info(entry.getKey().toString() + '=' + entry.getValue());
      }      
   }
 
}
