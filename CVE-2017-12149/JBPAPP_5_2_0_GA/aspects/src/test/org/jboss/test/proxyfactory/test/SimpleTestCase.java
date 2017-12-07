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
package org.jboss.test.proxyfactory.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.Test;

import org.jboss.test.proxyfactory.AbstractProxyTest;
import org.jboss.test.proxyfactory.support.Simple;
import org.jboss.test.proxyfactory.support.SimpleBean;
import org.jboss.test.proxyfactory.support.SimpleInterceptor;

/**
 * SimpleTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class SimpleTestCase extends AbstractProxyTest
{
   private static final int iterations = 1000;
   
   public void testSimple() throws Exception
   {
      SimpleBean bean = new SimpleBean();
      Simple simple = (Simple) assertCreateProxy(bean, Simple.class);
      SimpleInterceptor.invoked = null;
      simple.doSomething();
      assertTrue(bean.invoked);
      Method invoked = SimpleInterceptor.invoked;
      assertNotNull(invoked);
      assertEquals("doSomething", invoked.getName());
   }
   
   public void testStressNoProxy() throws Exception
   {
      for (int i = 0; i < iterations; ++i)
      {
         SimpleBean bean = new SimpleBean();
         bean.doSomething();
      }
   }

   public class MyInvocationHandler implements InvocationHandler
   {
      private Object target;

      public MyInvocationHandler(Object target)
      {
         this.target = target;
      }
      
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         return method.invoke(target, args);
      }
   }
   
   public void testStressDynamicProxy() throws Exception
   {
      Class[] interfaces = new Class[] { Simple.class };
      ClassLoader cl = Simple.class.getClassLoader();
      for (int i = 0; i < iterations; ++i)
      {
         SimpleBean bean = new SimpleBean();
         InvocationHandler ih = new MyInvocationHandler(bean);
         Simple simple = (Simple) Proxy.newProxyInstance(cl, interfaces, ih);
         simple.doSomething();
      }
   }
   
   public void testStressProxyFactory() throws Exception
   {
      for (int i = 0; i < iterations; ++i)
      {
         SimpleBean bean = new SimpleBean();
         Simple simple = (Simple) createProxy(bean);
         simple.doSomething();
      }
   }
   
   public static Test suite()
   {
      return suite(SimpleTestCase.class);
   }

   public SimpleTestCase(String name)
   {
      super(name);
   }
}
