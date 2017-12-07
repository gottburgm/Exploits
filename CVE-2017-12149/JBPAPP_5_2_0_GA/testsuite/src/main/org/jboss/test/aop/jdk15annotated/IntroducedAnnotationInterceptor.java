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
package org.jboss.test.aop.jdk15annotated;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jboss.aop.AnnotationIntroductionDef;
import org.jboss.aop.Bind;
import org.jboss.aop.InterceptorDef;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.annotation.AnnotationElement;
import org.jboss.aop.introduction.AnnotationIntroduction;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
@SuppressWarnings({"unused", "unchecked"})
@InterceptorDef (scope=org.jboss.aop.advice.Scope.PER_VM)
@Bind (pointcut="all(org.jboss.test.aop.jdk15annotated.IntroducedAnnotationPOJO)")
      public class IntroducedAnnotationInterceptor implements Interceptor
{
   @AnnotationIntroductionDef (expr="method(* org.jboss.test.aop.jdk15annotated.IntroducedAnnotationPOJO->annotationIntroductionMethod())", invisible=false, annotation="@org.jboss.test.aop.jdk15annotated.MyAnnotation (string='hello', integer=5, bool=true)")
   public static AnnotationIntroduction annotationIntroduction;

   public static MyAnnotation lastMyAnnotation;

   public String getName()
   {
      return "TestAnnotationInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      System.out.println("IntroducedInterceptor");
      if (invocation instanceof MethodInvocation)
      {
         Method method = ((MethodInvocation)invocation).getMethod();
         System.out.println("executing method " + method.toString());
         MyAnnotation myAnn = (MyAnnotation)AnnotationElement.getAnyAnnotation(method, MyAnnotation.class);
         lastMyAnnotation = myAnn;
      }
      else if (invocation instanceof ConstructorInvocation)
      {
         Constructor constructor = ((ConstructorInvocation)invocation).getConstructor();
         System.out.println("executing constructor " + constructor);
         MyAnnotation myAnn = (MyAnnotation)AnnotationElement.getAnyAnnotation(constructor, MyAnnotation.class);
         lastMyAnnotation = myAnn;
      }

      return invocation.invokeNext();
   }
}
