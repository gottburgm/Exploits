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
package org.jboss.test.jmx.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * AnnotatedJMXUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class AnnotatedJMXUnitTestCase extends JBossTestCase
{
   public AnnotatedJMXUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testAnnotatedJMX() throws Exception
   {
      ObjectName name = new ObjectName("test:name=AnnotatedJMXPojo");
      ObjectName nameNotDirect = new ObjectName("test:name=AnnotatedJMXPojoNotDirect");
      
      deploy("testannotatedjmxpojo.beans");
      try
      {
         assertTrue((Boolean) getServer().getAttribute(name, "CreateInvoked"));
         assertTrue((Boolean) getServer().getAttribute(name, "StartInvoked"));
         assertTrue((Boolean) getServer().getAttribute(nameNotDirect, "CreateInvoked"));
         assertTrue((Boolean) getServer().getAttribute(nameNotDirect, "StartInvoked"));
      }
      finally
      {
         undeploy("testannotatedjmxpojo.beans");
      }
   }
   
   public void testAnnotatedJMXRestart() throws Exception
   {
      ObjectName name = new ObjectName("test:name=AnnotatedJMXPojo");
      
      deploy("testannotatedjmxpojo.beans");
      try
      {
         assertTrue((Boolean) getServer().getAttribute(name, "CreateInvoked"));
         assertTrue((Boolean) getServer().getAttribute(name, "StartInvoked"));
         
         getServer().invoke(name, "destroy", null, null);
         assertTrue((Boolean) getServer().getAttribute(name, "StopInvoked"));
         assertTrue((Boolean) getServer().getAttribute(name, "DestroyInvoked"));

         getServer().invoke(name, "reset", null, null);
         
         getServer().invoke(name, "create", null, null);
         getServer().invoke(name, "start", null, null);
         assertTrue((Boolean) getServer().getAttribute(name, "StartInvoked"));
      }
      finally
      {
         undeploy("testannotatedjmxpojo.beans");
      }
   }
   
   public void testAnnotatedJMXRestartNotDirect() throws Exception
   {
      ObjectName name = new ObjectName("test:name=AnnotatedJMXPojoNotDirect");
      
      deploy("testannotatedjmxpojo.beans");
      try
      {
         assertTrue((Boolean) getServer().getAttribute(name, "CreateInvoked"));
         assertTrue((Boolean) getServer().getAttribute(name, "StartInvoked"));
         
         getServer().invoke(name, "destroy", null, null);
         assertTrue((Boolean) getServer().getAttribute(name, "StopInvoked"));
         assertTrue((Boolean) getServer().getAttribute(name, "DestroyInvoked"));

         getServer().invoke(name, "reset", null, null);
         
         getServer().invoke(name, "create", null, null);
         getServer().invoke(name, "start", null, null);
         assertTrue((Boolean) getServer().getAttribute(name, "StartInvoked"));
      }
      finally
      {
         undeploy("testannotatedjmxpojo.beans");
      }
   }
}
