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

import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;

/**
 * A MockTypeInfo.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class MockTypeInfo implements TypeInfo
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   private final Class<?> clazz;
   
   public MockTypeInfo(Class<?> clazz)
   {
      this.clazz = clazz;
   }

   public Object convertValue(Object obj) throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object convertValue(Object obj, boolean flag) throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object convertValue(Object obj, boolean flag, boolean flag1) throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   public TypeInfo getArrayType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getAttachment(String s)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T getAttachment(Class<T> arg0)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getName()
   {
      return clazz.getName();
   }

   public String getSimpleName()
   {
      return clazz.getSimpleName();
   }

   public Class<?> getType()
   {
      return clazz;
   }

   public TypeInfoFactory getTypeInfoFactory()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isAnnotation()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isArray()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isAssignableFrom(TypeInfo typeinfo)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isCollection()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isEnum()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isMap()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isPrimitive()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isInstance(Object obj)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public Object newArrayInstance(int i) throws Throwable
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setAttachment(String s, Object obj)
   {
      // TODO Auto-generated method stub

   }

}
