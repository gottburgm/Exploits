/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.channelfactory.managed;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.plugins.values.mappers.PropertiesCompositeObjectNameMetaMapper;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ManagedObjectTestUtil
{
   private static final Logger log = Logger.getLogger(ManagedObjectTestUtil.class);
   
   public static void validateProtocolStackConfigurations(MetaValue metaVal, String[] expectedStacks)
   {
      assertTrue(metaVal instanceof CompositeValue);
      CompositeValue compVal = (CompositeValue) metaVal;
      if (expectedStacks != null)
      {
         for (String stack : expectedStacks)
         {
            assertTrue("ChannelFactory has stack " + stack, compVal.containsKey(stack));
         }
      }
      for (String stack : compVal.getMetaType().keySet())
      {
         validateProtocolStackValue(stack, compVal.get(stack));
      }      
   }
   
   public static Set<ChannelIds> validateOpenChannels(MetaValue metaVal)
   {
      assertTrue(metaVal instanceof CollectionValue);
      
      Set<ChannelIds> result = new HashSet<ChannelIds>();
      
      MetaValue[] elements = ((CollectionValue) metaVal).getElements();
      assertNotNull(elements);
      assertTrue(elements.length > 0); // may not be true someday if things go "on-demand"
      for (MetaValue element : elements)
      {
         result.add(validateOpenChannelValue(element));         
      }
      
      return result;
   }
   
   public static void validateProtocolStackValue(String stack, MetaValue metaValue)
   {
      assertNotNull("null value for " + stack, metaValue);
      log.info(stack + " -- " + metaValue);
      
      assertTrue(metaValue instanceof CompositeValue);
      CompositeValue compVal = (CompositeValue) metaValue;
      assertEquals(stack, getSimpleValue(compVal, "name"));
      Object val = getSimpleValue(compVal, "description");
      if (val != null)
      {
         assertTrue(val instanceof String);
      }
      MetaValue mv = compVal.get("configuration");
      assertTrue(mv instanceof CollectionValue);
      CollectionValue config = (CollectionValue) mv;
      MetaValue[] elements = config.getElements();
      for (MetaValue element : elements)
      {
         validateProtocolStackConfiguration(element);
      }
   }
   
   public static void validateProtocolStackConfiguration(MetaValue metaValue)
   {
      assertNotNull("null protocol stack configuration", metaValue);      
      assertTrue(metaValue instanceof CompositeValue);
      CompositeValue compVal = (CompositeValue) metaValue;
      assertNotNull(getSimpleValue(compVal, "name", String.class));
      Object val = getSimpleValue(compVal, "description");
      if (val != null)
      {
         assertTrue(val instanceof String);
      }
      assertNotNull(getSimpleValue(compVal, "className", String.class));
      MetaValue mv = compVal.get("protocolParameters");
      assertNotNull(mv);
      assertTrue(mv + " (" + mv.getClass().getSimpleName() + ") is a CompositeValue", mv instanceof CompositeValue);
      CompositeValue params = (CompositeValue) mv;
//      MetaValue[] elements = params.getElements();
      for (String paramName : params.getMetaType().keySet())
      {
         validateProtocolParameter(params.get(paramName));
      }
   }
   
   public static void validateProtocolParameter(MetaValue metaValue)
   {
      assertNotNull("null protocol parameter", metaValue);      
      assertTrue(metaValue instanceof CompositeValue);
      CompositeValue compVal = (CompositeValue) metaValue;
//      assertNotNull(getSimpleValue(compVal, "name", String.class)); 
      Object val = getSimpleValue(compVal, "description");
      if (val != null)
      {
         assertTrue(val instanceof String);
      }    
      assertNotNull(getSimpleValue(compVal, "value", String.class)); 
   }
   
   public static ChannelIds validateOpenChannelValue(MetaValue metaValue)
   {
      log.info(metaValue);
      
      assertTrue(metaValue instanceof CompositeValue);
      
      ChannelIds result = new ChannelIds();
      
      CompositeValue compVal = (CompositeValue) metaValue;
      
      result.id = getSimpleValue(compVal, "id", String.class);
      result.clusterName = getSimpleValue(compVal, "clusterName", String.class);      
      assertNotNull("Channel " + result.id + " has clusterName", result.clusterName);
      
      result.stackName = getSimpleValue(compVal, "stackName", String.class);
      
      MetaValue mv = compVal.get("protocolStackConfiguration");
      assertTrue(mv instanceof CollectionValue);
      CollectionValue config = (CollectionValue) mv;
      MetaValue[] elements = config.getElements();
      for (MetaValue element : elements)
      {
         ManagedObjectTestUtil.validateProtocolStackConfiguration(element);
      }
      
      mv = compVal.get("channelObjectName");
      validateObjectNameMetaValue(mv);
      
      mv = compVal.get("protocolObjectNames");
      assertTrue(mv instanceof CollectionValue);
      CollectionValue protocolNames = (CollectionValue) mv;
      elements = protocolNames.getElements();
      for (MetaValue element : elements)
      {
         validateObjectNameMetaValue(element);
      }
      
      getSimpleValue(compVal, "localAddress", String.class);
      
      mv = compVal.get("currentView");
      validateView(mv);
      
      return result;
   }

   public static void validateObjectNameMetaValue(MetaValue mv)
   {
      if (mv != null)
      {
         try
         {
            Object on = MetaValueFactory.getInstance().unwrap(mv);
            assertNotNull(on);
            assertTrue(on instanceof ObjectName);
         }
         catch (RuntimeException e)
         {
            fail(e.getLocalizedMessage());
         }
      }
   }

   public static void validateView(MetaValue metaValue)
   {
      assertNotNull(metaValue);
      log.info(metaValue);
      assertTrue(metaValue instanceof CompositeValue);
      CompositeValue compVal = (CompositeValue) metaValue;
      assertNotNull(getSimpleValue(compVal, "id", Long.class));
      assertNotNull(getSimpleValue(compVal, "creator", String.class));
      String coord = getSimpleValue(compVal, "coordinator", String.class);
      assertNotNull(coord);
      MetaValue mv = compVal.get("members");
      assertTrue(mv instanceof CollectionValue);
      CollectionValue protocolNames = (CollectionValue) mv;
      MetaValue[] elements = protocolNames.getElements();
      boolean foundCoord = false;
      for (MetaValue element : elements)
      {
         assertTrue(element instanceof SimpleValue);
         Object val = ((SimpleValue) element).getValue();
         assertTrue(val instanceof String);
         if (coord.equals(val))
         {
            foundCoord = true;
         }
      }
      assertTrue(foundCoord);
      getSimpleValue(compVal, "payload", String.class);
   }

   /** Simple data object to pass back aggregated data for test callers */
   public static class ChannelIds
   {
      public String id;
      public String clusterName;
      public String stackName;
   }
   
   public static Object getSimpleValue(MetaValue val, String key)
   {
      return getSimpleValue(val, key, Object.class);
   }
   
   public static <T> T getSimpleValue(MetaValue val, String key, Class<T> type)
   {
      T result = null;
      assertTrue(val instanceof CompositeValue);
      CompositeValue cval = (CompositeValue) val;
      MetaValue mv = cval.get(key);
      if (mv != null)
      {
         assertTrue(mv instanceof SimpleValue);
         Object obj = ((SimpleValue) mv).getValue();
         result = type.cast(obj);
      }
      return result;
   }
   
   /**
    * Prevent instantiation
    */
   private ManagedObjectTestUtil()
   {
      // no-op
   }

}
