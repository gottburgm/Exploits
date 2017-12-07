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
package org.jboss.mx.mxbean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.management.openmbean.OpenType;

import org.jboss.util.UnreachableStatementException;

/**
 * SkeletonOpenType.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class SkeletonOpenType
{
   /** The open type */
   private OpenType openType;
   
   /** The composite type meta data */
   private CompositeTypeMetaData composite;

   /**
    * Create a new SkeletonOpenType.
    * 
    * @param type the type
    */
   public SkeletonOpenType(Type type)
   {
      openType = MXBeanUtils.checkType(type);
      if (openType != null)
         return;
      Class clazz = null;
      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         clazz = (Class) parameterizedType.getRawType();
      }
      else if (type instanceof Class)
      {
         clazz = (Class) type;
      }
      else
      {
         throw new UnsupportedOperationException("Unable to create open type for " + type);
      }
      composite = CompositeTypeMetaDataFactory.getCompositeTypeMetaData(clazz);
   }
   
   /**
    * Get the openType.
    * 
    * @return the openType.
    */
   public OpenType getOpenType()
   {
      if (openType != null)
         return openType;
      if (composite != null)
         return composite.getCompositeType();
      throw new UnreachableStatementException();
   }
}
