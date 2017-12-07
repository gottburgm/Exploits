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

package org.jboss.test.system.metadata.value.valuefactory.test;

import java.lang.annotation.Annotation;

import org.jboss.reflect.spi.AnnotationValue;
import org.jboss.reflect.spi.ClassInfo;
import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.ParameterInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.util.JBossStringBuilder;

/**
 * A MockMethodInfo.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class MockMethodInfo implements MethodInfo
{
   private final String name;
   private final TypeInfo[] parameterTypes;
   
   public MockMethodInfo(String name, TypeInfo[] parameterTypes)
   {
      this.name = name;
      this.parameterTypes = parameterTypes;
   }
   
   @Override
   public Object clone()
   {
      try
      {
         return super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new Error("Can't clone", e);
      }
   }

   public ClassInfo[] getExceptionTypes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getName()
   {
      return name;
   }

   public TypeInfo[] getParameterTypes()
   {
      return parameterTypes;
   }

   public ParameterInfo[] getParameters()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public TypeInfo getReturnType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object invoke(Object arg0, Object[] arg1) throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   public AnnotationValue getAnnotation(String arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public AnnotationValue[] getAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T extends Annotation> T getUnderlyingAnnotation(Class<T> arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Annotation[] getUnderlyingAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isAnnotationPresent(String arg0)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> arg0)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public String toShortString()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void toShortString(JBossStringBuilder buffer)
   {
      // TODO Auto-generated method stub

   }

   public ClassInfo getDeclaringClass()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public int getModifiers()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public boolean isPublic()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isStatic()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isVolatile()
   {
      // TODO Auto-generated method stub
      return false;
   }

}
