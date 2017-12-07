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

package org.jboss.profileservice.management.mbean;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

/**
 * {@link MetaMapper} for detyped EJB invocation statistics.
 *
 * @author Jason T. Greene
 */
public class EJBInvocationStatsMapper extends MetaMapper<Map<String, Map<String, Long>>>
{
   public static final CompositeMetaType TYPE;
   public static final CompositeMetaType METHOD_STATS_TYPE;
   private static String[] rootItemNames;
   private static MapCompositeMetaType METHOD_STATS_MAP_TYPE;

   static
   {
      String[] methodItemNames = {
            "count",
            "minTime",
            "maxTime",
            "totalTime"
      };
      String[] methodItemDescriptions = {
            "the number of invocations",
            "the minimum invocation time",
            "the maximum invocation time",
            "the total invocation time",
      };
      MetaType[] methodItemTypes = {
            SimpleMetaType.LONG,
            SimpleMetaType.LONG,
            SimpleMetaType.LONG,
            SimpleMetaType.LONG,
      };
      METHOD_STATS_TYPE = new ImmutableCompositeMetaType("MethodStatistics",
            "Method invocation statistics",
            methodItemNames, methodItemDescriptions, methodItemTypes);

      METHOD_STATS_MAP_TYPE = new MapCompositeMetaType(METHOD_STATS_TYPE);

      rootItemNames = new String[] {
            "concurrentCalls",
            "maxConcurrentCalls",
            "lastResetTime",
            "methodStats"
      };

      String[] rootItemDescriptions = {
            "the number of concurrent invocations",
            "the maximum number of concurrent invocations",
            "last time statistics were reset",
            "method statistics",
      };
      MetaType[] rootItemTypes = {
            SimpleMetaType.LONG,
            SimpleMetaType.LONG,
            SimpleMetaType.LONG,
            METHOD_STATS_MAP_TYPE
      };

      TYPE = new ImmutableCompositeMetaType("InvocationStatistics",
            "EJB invocation statistics",
            rootItemNames, rootItemDescriptions, rootItemTypes);
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   @Override
   public Type mapToType()
   {
      return Map.class;
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, Map<String, Map<String, Long>> object)
   {
      Map<String, MetaValue> methodMap = new HashMap<String, MetaValue>();
      for (Map.Entry<String, Map<String, Long>> entry: object.entrySet())
      {
         if (entry.getKey().charAt(0) == '#')
            continue;

         MapCompositeValueSupport cvs = new MapCompositeValueSupport(METHOD_STATS_TYPE);
         for (String name : METHOD_STATS_TYPE.itemSet())
            cvs.put(name, SimpleValueSupport.wrap(entry.getValue().get(name)));

         methodMap.put(entry.getKey(), cvs);
      }

      MapCompositeValueSupport root = new MapCompositeValueSupport(TYPE);
      for (int i = 0; i < 3; i++)
         root.put(rootItemNames[i], SimpleValueSupport.wrap(object.get("#Global").get(rootItemNames[i])));

      root.put(rootItemNames[3], new MapCompositeValueSupport(methodMap, METHOD_STATS_MAP_TYPE));

      return root;
   }

   @Override
   public Map<String, Map<String, Long>> unwrapMetaValue(MetaValue metaValue)
   {
      // This is read-only, so not needed
      return null;
   }
}
