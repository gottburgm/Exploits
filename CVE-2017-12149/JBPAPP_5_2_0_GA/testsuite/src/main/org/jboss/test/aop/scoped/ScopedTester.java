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
package org.jboss.test.aop.scoped;

import org.jboss.aop.Advised;
import org.jboss.test.aop.scoped.excluded.ExcludedPOJO;
import org.jboss.test.aop.scoped.excluded.included.IncludedPOJO;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ScopedTester implements ScopedTesterMBean
{
   int expectedInterceptorValue;
   int expectedAspectValue;
   int metadataSuffix;
   
   String ctorPlainMetadata;
   String methodPlainMetadata;
   String customMetadata;
   
   public void setExpectedInterceptorValue(int i)
   {
      expectedInterceptorValue = i;
   }

   public void setExpectedAspectValue(int i)
   {
      expectedAspectValue = i;
   }
   
   public int getExpectedInterceptorValue()
   {
      return expectedInterceptorValue;
   }

   public int getExpectedAspectValue()
   {
      return expectedAspectValue;
   }

   public void setMetadataSuffix(int i)
   {
      metadataSuffix = i;
      ctorPlainMetadata = "ctor" + metadataSuffix;
      methodPlainMetadata = "method" + metadataSuffix;
      customMetadata = "custom" + metadataSuffix;
   }
   
   public void testExpectedValues()
   {
      if (ScopedInterceptor.value != expectedInterceptorValue) 
      { 
         throw new RuntimeException("Expected Interceptor value " + expectedInterceptorValue + ", was " + ScopedInterceptor.value);
      }
      if (ScopedAspect.value != expectedAspectValue) 
      { 
         throw new RuntimeException("Expected Aspect value " + expectedAspectValue + ", was " + ScopedAspect.value);
      }
      
   }
   
   public void testScoped() throws Exception
   {
      System.out.println("--------------------------- TESTING SCOPED ------------------");
      System.out.println("MY CLASSLOADER " + getClass().getClassLoader());
      System.out.println("SCOPED INTERCEPTOR CLASSLOADER " + ScopedInterceptor.class.getClassLoader());


      System.out.println("------- CTOR");
      ScopedAspect.intercepted = 0;
      ScopedFactoryAspect.intercepted = 0;
      ScopedFactoryAspect.metadata = null;
      ScopedFactoryAspect.customMetadata = null;
      
      POJO pojo = new POJO();
      if (ScopedAspect.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedAspect 1 for POJO constructor, was " + ScopedAspect.intercepted);
      }
      if (ScopedFactoryAspect.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedFactoryAspect 1 for POJO constructor, was " + ScopedFactoryAspect.intercepted);
      }
      if (!ctorPlainMetadata.equals(ScopedFactoryAspect.metadata))
      {
         throw new RuntimeException("Expected ctor metadata " + ctorPlainMetadata + ", was " + ScopedFactoryAspect.metadata);
      }
      if (!customMetadata.equals(ScopedFactoryAspect.customMetadata))
      {
         throw new RuntimeException("Expected ctor customm metadata " + customMetadata + ", was " + ScopedFactoryAspect.customMetadata);
      }
      
      System.out.println("------- METHOD");
      ScopedInterceptor.intercepted = 0;
      ScopedAspect.intercepted = 0;
      ScopedFactoryAspect.intercepted = 0;
      ScopedFactoryAspect.metadata = null;
      ScopedFactoryAspect.customMetadata = null;

      pojo.method();
      if (ScopedInterceptor.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedInterceptor 1 for POJO method, was " + ScopedInterceptor.intercepted);
      }
      if (ScopedAspect.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedAspect 1 for POJO method, was " + ScopedAspect.intercepted);
      }
      if (ScopedFactoryAspect.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedFactoryAspect 1 for POJO method, was " + ScopedFactoryAspect.intercepted);
      }
      if (!methodPlainMetadata.equals(ScopedFactoryAspect.metadata))
      {
         throw new RuntimeException("Expected method metadata '" + methodPlainMetadata + ", was " + ScopedFactoryAspect.metadata);
      }
      if (!customMetadata.equals(ScopedFactoryAspect.customMetadata))
      {
         throw new RuntimeException("Expected method customm metadata " + customMetadata + ", was " + ScopedFactoryAspect.customMetadata);
      }
   }
   
   public void testAnnotatedScopedAnnotationsDeployed()
   {
      AnnotatedInterceptor.invoked = false;
      POJO pojo = new POJO();
      if (!AnnotatedInterceptor.invoked) throw new RuntimeException("AnnotatedInterceptor should have been invoked");
   }
   
   public void testAnnotatedScopedAnnotationsNotDeployed()
   {
      try
      {
         AnnotatedInterceptor.invoked = false;
         throw new RuntimeException("AnnotatedInterceptor should not be available in this deployment");
      }
      catch (NoClassDefFoundError expected)
      {
      }
   }
   
   public void testIntroduction1()
   {
      IntroducedPOJO pojo = new IntroducedPOJO();
      Scope1Interface iface = (Scope1Interface)pojo;
      
      ScopedInterceptor.intercepted = 0;
      iface.testMethod();
      if (ScopedInterceptor.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedInterceptor 1 for introduction " + ScopedInterceptor.intercepted);
      }
      
      try
      {
         Scope2Interface iface2 = (Scope2Interface)pojo;
         throw new RuntimeException("Should not be implementing Scope2Interface");
      }
      catch(ClassCastException expected)
      {
      }
   }
   
   public void testIntroduction2()
   {
      IntroducedPOJO pojo = new IntroducedPOJO();
      Scope2Interface iface = (Scope2Interface)pojo;
      
      ScopedInterceptor.intercepted = 0;
      iface.testMethod();
      if (ScopedInterceptor.intercepted != 1)
      {
         throw new RuntimeException("Expected ScopedInterceptor 1 for introduction " + ScopedInterceptor.intercepted);
      }
      
      try
      {
         Scope1Interface iface2 = (Scope1Interface)pojo;
         throw new RuntimeException("Should not be implementing Scope1Interface");
      }
      catch(ClassCastException expected)
      {
      }
   }
   
   public void testInclude()
   {
      if (!Advised.class.isAssignableFrom(IncludedPOJO.class))
      {
         throw new RuntimeException("IncludedPOJO should be Advised");
      }
   }
   
   public void testExclude()
   {
      if (Advised.class.isAssignableFrom(ExcludedPOJO.class))
      {
         throw new RuntimeException("ExcludedPOJO should not be Advised");
      }
   }
   
   public void testIgnore()
   {
      if (Advised.class.isAssignableFrom(POJO$$Ignored$$123.class))
      {
         throw new RuntimeException("POJO$$Ignored$$123 should not be Advised");
      }
   }
}
