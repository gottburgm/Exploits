/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.jms.server.destination;

import java.lang.reflect.Type;

import javax.management.ObjectName;

import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

/**
 * A mapper for String forms of javax.management.ObjectName values to a
 * SimpleMetaType.STRING/SimpleValue
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81572 $
 */
public class StringObjectNameMetaMapper extends MetaMapper<ObjectName>
{

   @Override
   public MetaType getMetaType()
   {
      return SimpleMetaType.STRING;
   }
   @Override
   public Type mapToType()
   {
      return ObjectName.class;
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, ObjectName object)
   {
      if(object == null)
         return null;
      String str = object.getCanonicalName();
      return SimpleValueSupport.wrap(str);
   }

   @Override
   public ObjectName unwrapMetaValue(MetaValue metaValue)
   {
      if(metaValue == null)
         return null;

      SimpleValue sv = (SimpleValue) metaValue;      
      // ignore a null object name
      if(sv.getValue() == null)
         return null;
      
      String str = sv.getValue().toString();
      try
      {
         return new ObjectName(str);
      }
      catch(Exception e)
      {
         throw new IllegalArgumentException(e);
      }
   }

}
