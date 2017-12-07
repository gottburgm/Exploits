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
package org.jboss.test.aop.bean;

import org.jboss.aop.Advised;

/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class CallingPOJO
{
   POJO pojo;
   NonadvisedPOJO nonpojo;

   public CallingPOJO()
   {
      CallerInterceptor.called = false;
      pojo = new POJO();
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("constructor caller interceptor didn't work from within constructor");
      }
      CallerInterceptor.called = false;
      pojo.someMethod();
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("caller interceptor didn't work");
      }
      CallerInterceptor.called = false;
      nonpojo = new NonadvisedPOJO("helloworld");
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("constructor caller interceptor didn't work");
      }
      CallerInterceptor.called = false;
      nonpojo.remoteTest();
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("caller interceptor didn't work");
      }
      if (nonpojo instanceof Advised)
      {
         throw new RuntimeException("nonpojo is Advised when it shouldn't be");
      }
   }

  /**
   * This method should be a caller pointcut
   */
   public void callSomeMethod()
   {
      CallerInterceptor.called = false;
      pojo = new POJO();
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("constructor caller interceptor didn't work within method");
      }
      CallerInterceptor.called = false;
      pojo.someMethod();
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("caller interceptor didn't work");
      }
   }

   /**
    * This method should not be a caller pointcut
    */
   public void nocallSomeMethod()
   {
      CallerInterceptor.called = false;
      pojo = new POJO();
      if (CallerInterceptor.called)
      {
         throw new RuntimeException("constructor caller interceptor didn't work, interceptor was invoked when it shouldn't have been");
      }
      pojo.someMethod();
      if (CallerInterceptor.called)
      {
         throw new RuntimeException("caller interceptor didn't work, caller interceptor was invoked when it shouldn't have been");
      }
   }

   public void callUnadvised()
   {
      CallerInterceptor.called = false;
      nonpojo = new NonadvisedPOJO("helloworld");
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("consturctor caller interceptor didn't work");
      }
      CallerInterceptor.called = false;
      nonpojo.remoteTest();
      if (!CallerInterceptor.called)
      {
         throw new RuntimeException("caller interceptor didn't work");
      }
      if (nonpojo instanceof Advised)
      {
         throw new RuntimeException("nonpojo is Advised when it shouldn't be");
      }
   }

}

