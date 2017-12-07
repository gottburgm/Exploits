/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.aop.scopedattach;

import org.jboss.aop.Advised;
import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.loading.RepositoryClassLoader;

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
   
   public void checkPOJOAdvised()
   {
      System.out.println("--------------------------- TESTING POJO ADVISED ------------------");
      if (!Advised.class.isAssignableFrom(POJO.class))
      {
         throw new RuntimeException("POJO is not advised");
      }
   }
   
   public void testScoped() throws Exception
   {
      try
      {
         System.out.println("--------------------------- TESTING SCOPED ------------------");
         System.out.println("MY CLASSLOADER " + getClass().getClassLoader());
         System.out.println("SCOPED INTERCEPTOR CLASSLOADER " + ScopedInterceptor.class.getClassLoader());
         System.out.println("POJO CLASSLOADER " + POJO.class.getClassLoader());
   
         if (getClass().getClassLoader() instanceof RepositoryClassLoader)
         {
            //Check that the classloaders have the same repositories
            LoaderRepository repository1 = ((RepositoryClassLoader)getClass().getClassLoader()).getLoaderRepository();
            LoaderRepository repository2 = ((RepositoryClassLoader)ScopedInterceptor.class.getClassLoader()).getLoaderRepository();
            LoaderRepository repository3 = ((RepositoryClassLoader)POJO.class.getClassLoader()).getLoaderRepository();
            if (repository1 != repository2)
            {
               throw new RuntimeException("Repositories were not the same");
            }
            if (repository1 != repository3)
            {
               throw new RuntimeException("Repositories were not the same");
            }
         }
         
         System.out.println("------- CTOR");
         ScopedAspect.intercepted = 0;
         ScopedFactoryAspect.intercepted = 0;
         ScopedPCInterceptor.intercepted = 0;
         ScopedPCJInterceptor.intercepted = 0;
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
         if (ScopedPCInterceptor.intercepted != 1)
         {
            throw new RuntimeException("Expected ScopedPCInterceptor 1 for POJO constructor, was " + ScopedPCInterceptor.intercepted);
         }
         if (ScopedPCJInterceptor.intercepted != 1)
         {
            throw new RuntimeException("Expected ScopedPCJInterceptor 1 for POJO constructor, was " + ScopedPCJInterceptor.intercepted);
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
         ScopedPCInterceptor.intercepted = 0;
         ScopedPCJInterceptor.intercepted = 0;
         ScopedPJInterceptor.intercepted = 0;
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
         if (ScopedPCInterceptor.intercepted != 1)
         {
            throw new RuntimeException("Expected ScopedPCInterceptor 1 for POJO method, was " + ScopedPCInterceptor.intercepted);
         }
         if (ScopedPCJInterceptor.intercepted != 1)
         {
            throw new RuntimeException("Expected ScopedPCJInterceptor 1 for POJO method, was " + ScopedPCJInterceptor.intercepted);
         }
         if (ScopedPJInterceptor.intercepted != 1)
         {
            throw new RuntimeException("Expected ScopedPJInterceptor 1 for POJO method, was " + ScopedPJInterceptor.intercepted);
         }
         if (!methodPlainMetadata.equals(ScopedFactoryAspect.metadata))
         {
            throw new RuntimeException("Expected method metadata '" + methodPlainMetadata + ", was " + ScopedFactoryAspect.metadata);
         }
         if (!customMetadata.equals(ScopedFactoryAspect.customMetadata))
         {
            throw new RuntimeException("Expected method customm metadata " + customMetadata + ", was " + ScopedFactoryAspect.customMetadata);
         }
         
         
         if (ScopedInterceptor.value != expectedInterceptorValue)
         {
            throw new RuntimeException("Expected " + expectedInterceptorValue + " was " + ScopedInterceptor.value);
         }
         if (ScopedPCInterceptor.value != expectedInterceptorValue)
         {
            throw new RuntimeException("Expected " + expectedInterceptorValue + " was " + ScopedPCInterceptor.value);
         }
         if (ScopedPJInterceptor.value != expectedInterceptorValue)
         {
            throw new RuntimeException("Expected " + expectedInterceptorValue + " was " + ScopedPJInterceptor.value);
         }
         if (ScopedPCJInterceptor.value != expectedInterceptorValue)
         {
            throw new RuntimeException("Expected " + expectedInterceptorValue + " was " + ScopedPCJInterceptor.value);
         }
         if (ScopedAspect.value != expectedAspectValue)
         {
            throw new RuntimeException("Expected " + expectedAspectValue + " was " + ScopedAspect.value);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }   
}
