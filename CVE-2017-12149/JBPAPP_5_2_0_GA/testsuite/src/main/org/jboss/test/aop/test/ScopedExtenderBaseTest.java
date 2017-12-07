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
package org.jboss.test.aop.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public abstract class ScopedExtenderBaseTest extends JBossTestCase
{
   final static String BASE_NAME= "jboss.aop:name=BaseTester";
   final static String CHILD_NAME= "jboss.aop:name=ChildTester";
   final static Object[] PARAMS = new Object[0];
   final static String[] SIG = new String[0];
   
   public ScopedExtenderBaseTest(String name)
   {
      super(name);
   }

   public void testCorrectBaseDeployed() throws Exception
   {
      String name = invokeReadName(BASE_NAME);
      assertEquals(getExpectedBaseName(), name);
   }
   
   public void testCorrectChildDeployed() throws Exception
   {
      String name = invokeReadName(CHILD_NAME);
      assertEquals(getExpectedChildName(), name);
   }
   
   public void testLoaders() throws Exception
   {
      invokeTestClassLoaders(BASE_NAME);
      invokeTestClassLoaders(CHILD_NAME);
   }
   
   public void testMethod() throws Exception
   {
      invokeMethodTest(BASE_NAME);
      invokeMethodTest(CHILD_NAME);
   }
   
   public void testField() throws Exception
   {
      invokeFieldTest(BASE_NAME);
      invokeFieldTest(CHILD_NAME);
   }
   
   public void testConstructor() throws Exception
   {
      invokeConstructorTest(BASE_NAME);
      invokeConstructorTest(CHILD_NAME);
   }
   
   public void testDifferentScopes() throws Exception
   {
      invokeOverriddenInterceptorsTest(BASE_NAME);
      invokeOverriddenInterceptorsTest(CHILD_NAME);
   }
   
   private void invokeMethodTest(String objName) throws Exception
   {
      invoke(objName, "testMethod");
   }

   private void invokeFieldTest(String objName) throws Exception
   {
      invoke(objName, "testField");
   }
   
   private void invokeConstructorTest(String objName) throws Exception
   {
      invoke(objName, "testConstructor");
   }
   
   private String invokeReadName(String objName) throws Exception
   {
      return (String)invoke(objName, "readName");
   }
   
   private void invokeTestClassLoaders(String objName) throws Exception
   {
      invoke(objName, "testLoaders");
   }
   
   private void invokeOverriddenInterceptorsTest(String objName) throws Exception
   {
      invoke(objName, "testOverriddenInterceptors");
   }
   
   private Object invoke(String objName, String method) throws Exception
   {
      ObjectName testerName = new ObjectName(objName);
      return getServer().invoke(testerName, method, PARAMS, SIG);
   }
   
   protected abstract String getExpectedBaseName();

   protected abstract String getExpectedChildName();

}
