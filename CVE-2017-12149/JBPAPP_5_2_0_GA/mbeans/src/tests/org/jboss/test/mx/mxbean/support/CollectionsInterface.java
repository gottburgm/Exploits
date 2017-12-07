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
package org.jboss.test.mx.mxbean.support;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.jboss.mx.mxbean.MXBeanUtils;

/**
 * CollectionsInterface.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public interface CollectionsInterface
{
   String ARRAY = "array";
   String COLLECTION = "collection";
   String SET = "set";
   String LIST = "list";
   String MAP = "map";
   String ENUM = "enum";
   
   String[] KEYS =
   {
      ARRAY,
      COLLECTION,
      SET,
      LIST,
      MAP,
      ENUM
   };

   OpenType[] TYPES = Initializer._TYPES;

   String[] getArray();
   
   Collection<String> getCollection();
   
   Set<String> getSet();
   
   List<String> getList();
   
   Map<String, Integer> getMap();
   
   TestEnum getEnum();
   
   public static class Initializer
   {
      static OpenType[] _TYPES;
      
      static
      {
         try
         {
            _TYPES = new OpenType[]
            {
               new ArrayType(1, SimpleType.STRING),
               new ArrayType(1, SimpleType.STRING),
               new ArrayType(1, SimpleType.STRING),
               new ArrayType(1, SimpleType.STRING),
               MXBeanUtils.createMapType(String.class, Integer.class),
               SimpleType.STRING
            };
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}
