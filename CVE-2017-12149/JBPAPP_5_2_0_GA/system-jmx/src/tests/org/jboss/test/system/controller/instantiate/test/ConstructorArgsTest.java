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
package org.jboss.test.system.controller.instantiate.test;

import javax.management.MBeanServer;

import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * ConstructorArgsTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class ConstructorArgsTest extends AbstractControllerTest
{
   public ConstructorArgsTest(String name)
   {
      super(name);
   }
   
   public void testPlainMBeanNoArg() throws Exception
   {
      noArg();
   }
   
   public void testPlainMBeanOneArg() throws Exception
   {
      oneArg();
   }
   
   public void testPlainMBeanTwoArg() throws Exception
   {
      twoArg();
   }
   
   public void testStandardMBeanNoArg() throws Exception
   {
      noArg();
   }
   
   public void testStandardMBeanOneArg() throws Exception
   {
      oneArg();
   }
   
   public void testStandardMBeanTwoArg() throws Exception
   {
      twoArg();
   }
   
   protected void noArg() throws Exception
   {
      Simple simple = getSimple();
      assertEquals("()", simple.constructorUsed);
   }
   
   protected void oneArg() throws Exception
   {
      Simple simple = getSimple();
      assertEquals("(int)", simple.constructorUsed);
      assertEquals(5, simple.getAnint());
   }
   
   protected void twoArg() throws Exception
   {
      Simple simple = getSimple();
      assertEquals("(int,float)", simple.constructorUsed);
      assertEquals(5, simple.getAnint());
      assertEquals(3.14f, simple.getAfloat());
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      assertInstall(SimpleMBean.OBJECT_NAME);
   }
   
   protected void tearDown() throws Exception
   {
      try
      {
         assertUninstall(SimpleMBean.OBJECT_NAME);
      }
      finally
      {
         super.tearDown();
      }
   }

   protected Simple getSimple() throws Exception
   {
      MBeanServer server = getServer();
      return (Simple) server.getAttribute(SimpleMBean.OBJECT_NAME, "Instance");
   }
}
