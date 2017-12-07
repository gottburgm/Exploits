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

import javax.management.openmbean.CompositeType;

import org.jboss.util.collection.WeakClassCache;

/**
 * CompositeTypeMetaDataFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeTypeMetaDataFactory extends WeakClassCache
{
   /** The singleton */
   private static final CompositeTypeMetaDataFactory SINGLETON = new CompositeTypeMetaDataFactory(); 
   
   /**
    * Get the composite type for a class
    * 
    * @param clazz the class
    * @return the composite type
    */
   public static CompositeType getCompositeType(Class<?> clazz)
   {
      CompositeTypeMetaData metaData = getCompositeTypeMetaData(clazz);
      return metaData.getCompositeType();
   }
   
   /**
    * Get the composite type meta data
    * 
    * @param clazz the class
    * @return the meta data
    */
   public static CompositeTypeMetaData getCompositeTypeMetaData(Class<?> clazz)
   {
      return (CompositeTypeMetaData) SINGLETON.get(clazz);
   }
   
   /**
    * Singleton
    */
   private CompositeTypeMetaDataFactory()
   {
   }

   protected Object instantiate(Class clazz)
   {
      return new CompositeTypeMetaData(clazz);
   }

   protected void generate(Class clazz, Object object)
   {
      CompositeTypeMetaData metaData = (CompositeTypeMetaData) object;
      metaData.generate();
   }
}
