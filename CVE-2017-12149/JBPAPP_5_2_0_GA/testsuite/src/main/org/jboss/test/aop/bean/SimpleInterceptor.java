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

import org.jboss.aop.joinpoint.FieldInvocation;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.joinpoint.FieldReadInvocation;
import org.jboss.aop.joinpoint.FieldWriteInvocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class SimpleInterceptor implements org.jboss.aop.advice.Interceptor
{

   public String getName()
   {
      return "SimpleInterceptor";
   }

   public Object invoke(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      System.out.println("interception: " + invocation.getClass().getName());
      if (invocation instanceof MethodInvocation)
      {
         org.jboss.aop.joinpoint.MethodInvocation methodInvocation = (org.jboss.aop.joinpoint.MethodInvocation)invocation;
         Method m = methodInvocation.getMethod();
         if (m.getName().equals("whazup")) return "nada";
         lastIntercepted = m.getName();
         String transattr = (String)invocation.getMetaData("transaction", "trans-attribute"); 
         System.out.println("trans-attribute: " + transattr);
         lastTransAttributeAccessed = transattr;
      }
      else if (invocation instanceof FieldReadInvocation || invocation instanceof FieldWriteInvocation)
      {
         org.jboss.aop.joinpoint.FieldInvocation fieldInvocation = (org.jboss.aop.joinpoint.FieldInvocation)invocation;
         Field field = fieldInvocation.getField();
         System.out.println("**** simple: " + field.getName());
         lastFieldIntercepted = field.getName();
         Object obj =invocation.getMetaData("transaction", "trans-attribute");
         System.out.println(field.getName() + "type**" + obj.getClass().getName());
         String transattr = (String)invocation.getMetaData("transaction", "trans-attribute");
         System.out.println("trans-attribute: " + transattr);
         lastFieldTransAttributeAccessed = transattr;
      }
      return invocation.invokeNext();
   }

   public static String lastIntercepted;
   public static String lastTransAttributeAccessed;
   public static String lastFieldIntercepted;
   public static String lastFieldTransAttributeAccessed;
}

