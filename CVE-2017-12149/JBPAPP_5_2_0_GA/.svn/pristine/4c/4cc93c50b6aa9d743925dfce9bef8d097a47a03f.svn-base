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

package org.jboss.services.binding.managed;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.logging.Logger;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.ArrayValueSupport;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.services.binding.ServiceBinding;

/**
 * {@link MetaMapper} for a map of all {@link ServiceBinding}s available,
 * where keys are the name of a {@link ServiceBindingSet} and the values are
 * the set of {@link ServiceBinding}s associated with that binding set.
 * 
 * @author Brian Stansberry
 */
public class ServiceBindingMapper extends MetaMapper<Map<String, Set<ServiceBinding>>>
{
   private static final Logger log = Logger.getLogger(ServiceBindingMapper.class);
   public static final MapCompositeMetaType TYPE;
   public static final CompositeMetaType SERVICE_BINDING_TYPE;
   public static final CollectionMetaType MAP_VALUE_TYPE;

   static
   {
      String[] itemNames = {
            "serviceName",
            "bindingName",
            "fullyQualifiedName",
            "description",
            "hostName",
            "bindAddress",
            "port"
      };
      String[] itemDescriptions = {
            "the name of the service to which this binding applies",
            "a qualifier identifying which particular binding within the service this is",
            "the fully qualified binding name",
            "description of the binding",
            "the host name or string notation IP address to use for the binding",
            "byte[] representing the InetAddress of the interface to use for the binding",
            "the port to use for the binding",
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            ArrayMetaType.getPrimitiveArrayType(byte[].class),
            SimpleMetaType.INTEGER_PRIMITIVE
      };
      SERVICE_BINDING_TYPE = new ImmutableCompositeMetaType(ServiceBinding.class.getName(), 
            "Service Binding",
            itemNames, itemDescriptions, itemTypes);
      MAP_VALUE_TYPE = new CollectionMetaType(Set.class.getName(), SERVICE_BINDING_TYPE);
      TYPE = new MapCompositeMetaType(MAP_VALUE_TYPE);
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
   public MetaValue createMetaValue(MetaType metaType, Map<String, Set<ServiceBinding>> object)
   {
      Map<String, MetaValue> map = new HashMap<String, MetaValue>();
      for (Map.Entry<String, Set<ServiceBinding>> mapEntry: object.entrySet())
      {
         Set<CompositeValue> tmp = new TreeSet<CompositeValue>(new FullyQualifiedNameComparator());
         for (ServiceBinding b : mapEntry.getValue())
         {
            try
            {
               MapCompositeValueSupport cvs = new MapCompositeValueSupport(SERVICE_BINDING_TYPE);
               cvs.put("serviceName", SimpleValueSupport.wrap(b.getServiceName()));
               cvs.put("bindingName", SimpleValueSupport.wrap(b.getBindingName()));
               cvs.put("fullyQualifiedName", SimpleValueSupport.wrap(b.getFullyQualifiedName()));
               cvs.put("description", SimpleValueSupport.wrap(b.getDescription()));
               cvs.put("hostName", SimpleValueSupport.wrap(b.getHostName()));
               InetAddress inet = b.getBindAddress();
               if (inet != null)
               {
                  ArrayValueSupport avs = new ArrayValueSupport(ArrayMetaType.getPrimitiveArrayType(byte[].class));
                  avs.setValue(inet.getAddress());
                  cvs.put("bindAddress", avs);
               }
               else
               {
                  cvs.put("bindAddress", null);
               }
               cvs.put("port", SimpleValueSupport.wrap(b.getPort()));
               tmp.add(cvs);
            }
            catch(Exception e)
            {
               log.warn("Skipping binding: "+ b, e);
            }
            
         }
         MetaValue[] elements = tmp.toArray(new MetaValue[tmp.size()]);
         CollectionValueSupport bindingSet = new CollectionValueSupport(MAP_VALUE_TYPE, elements);
         
         map.put(mapEntry.getKey(), bindingSet);
      }
      
      return new MapCompositeValueSupport(map, MAP_VALUE_TYPE);
   }

   /**
    * This always returns null as ServiceBindings cannot be created from a meta value
    */
   @Override
   public Map<String, Set<ServiceBinding>> unwrapMetaValue(MetaValue metaValue)
   {
      return null;
   }
   
   /** Used to order CompositeValues by the fullyQualifiedName key */   
   private static class FullyQualifiedNameComparator implements Comparator<CompositeValue>
   {

      public int compare(CompositeValue o1, CompositeValue o2)
      {
         SimpleValue sv1 = (SimpleValue) o1.get("fullyQualifiedName");
         if (sv1 == null)
         {
            throw new IllegalStateException(o1 + " has no fullyQualifiedName");
         }
         SimpleValue sv2 = (SimpleValue) o2.get("fullyQualifiedName");
         if (sv2 == null)
         {
            throw new IllegalStateException(o2 + " has no fullyQualifiedName");
         }
         
         String name1 = (String) sv1.getValue();
         String name2 = (String) sv2.getValue();
         return name1.compareTo(name2);
      }      
   }

}
