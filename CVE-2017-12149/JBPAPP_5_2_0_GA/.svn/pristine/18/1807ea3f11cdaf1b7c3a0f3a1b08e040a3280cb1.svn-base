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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.logging.Logger;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.ServiceBindingSet;

/**
 * {@link MetaMapper} for a set of {@link ServiceBindingSet}s.
 * 
 * @author Brian Stansberry
 */
public class ServiceBindingSetMapper extends MetaMapper<Set<ServiceBindingSet>>
{
   private static final Logger log = Logger.getLogger(ServiceBindingSetMapper.class);
   public static final CollectionMetaType TYPE;
   public static final CompositeMetaType SERVICE_BINDING_SET_TYPE;

   static
   {
      String[] itemNames = {
            "name",
            "defaultHostName",
            "portOffset",
            "overrideBindings"
      };
      String[] itemDescriptions = {
            "the name of the binding set",
            "the host name that should be used for all bindings whose configuration " +
               "does not specify fixedHostName=\"true\"",
            "value to add to the port configuration for a standard binding to " +
               "derive the port to use in this binding set",
            "binding configurations that apply only to this binding set, either " +
               "non-standard bindings or ones that override standard binding configurations",
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.INTEGER_PRIMITIVE,
            ServiceBindingMetadataMapper.TYPE
      };
      SERVICE_BINDING_SET_TYPE = new ImmutableCompositeMetaType(ServiceBindingSet.class.getName(), 
            "Service Binding Set",
            itemNames, itemDescriptions, itemTypes);
      TYPE = new CollectionMetaType(Set.class.getName(), SERVICE_BINDING_SET_TYPE);
   }

   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   @Override
   public Type mapToType()
   {
      return Set.class;
   }

   @Override
   public MetaValue createMetaValue(MetaType metaType, Set<ServiceBindingSet> object)
   {
      ServiceBindingMetadataMapper mapper = new ServiceBindingMetadataMapper();
      Set<CompositeValue> tmp = new TreeSet<CompositeValue>(new NameComparator());
      for (ServiceBindingSet b : object)
      {
         try
         {
            MapCompositeValueSupport cvs = new MapCompositeValueSupport(SERVICE_BINDING_SET_TYPE);
            cvs.put("name", SimpleValueSupport.wrap(b.getName()));
            cvs.put("defaultHostName", SimpleValueSupport.wrap(b.getDefaultHostName()));
            cvs.put("portOffset", SimpleValueSupport.wrap(b.getPortOffset()));
            Set<ServiceBindingMetadata> metadata = b.getOverrideBindings();
            if (metadata != null)
            {
               cvs.put("overrideBindings", mapper.createMetaValue(mapper.getMetaType(), metadata));
            }
            tmp.add(cvs);
         }
         catch(Exception e)
         {
            log.warn("Skipping binding: "+ b, e);
         }
         
      }
      MetaValue[] elements = tmp.toArray(new MetaValue[tmp.size()]);
      CollectionValueSupport bindings = new CollectionValueSupport(TYPE, elements);
      return bindings;
   }
   
   /**
    * Converts the {@link CollectionValue} <code>metaValue</code> into a set
    * of {@link ServiceBindingMetadata}.
    * 
    * {@inheritDoc}
    * 
    * @throws IllegalArgumentException if <code>metaValue</code> is not a 
    *              {@link CollectionValue} or if any element is not a {@link CompositeValue}
    */
   @Override
   public Set<ServiceBindingSet> unwrapMetaValue(MetaValue metaValue)
   {
      if (metaValue == null)
      {
         return null;
      }
      
      if ((metaValue instanceof CollectionValue) == false)
      {
         throw new IllegalArgumentException(metaValue + " is not a " + CollectionValue.class.getSimpleName());
      }
      CollectionValue collValue = (CollectionValue) metaValue;
      MetaValue[] elements = collValue.getElements();
      
      ServiceBindingMetadataMapper mapper = new ServiceBindingMetadataMapper();
      
      Set<ServiceBindingSet> result = new HashSet<ServiceBindingSet>(elements.length);
      for (MetaValue element : elements)
      {

         if ((element instanceof CompositeValue) == false)
         {
            throw new IllegalArgumentException(element + " is not a " + CompositeValue.class.getSimpleName());
         }
         CompositeValue compValue = (CompositeValue) element;
         String name = Util.getValueFromComposite(compValue, "name", String.class);
         String defaultHostName = Util.getValueFromComposite(compValue, "defaultHostName", String.class);
         Integer portOffset = Util.getValueFromComposite(compValue, "portOffset", Integer.class);
         if (portOffset == null)
         {
            throw new IllegalStateException(element + " has no value for key 'port'");
         }
         Set<ServiceBindingMetadata> overrides = mapper.unwrapMetaValue(compValue.get("overrideBindings"));
         ServiceBindingSet sbs = new ServiceBindingSet(name, defaultHostName, portOffset.intValue(), overrides);
         result.add(sbs);
      }
      return result;
   }
   
   /** Used to order CompositeValues by the name key */   
   private static class NameComparator implements Comparator<CompositeValue>
   {

      public int compare(CompositeValue o1, CompositeValue o2)
      {
         SimpleValue sv1 = (SimpleValue) o1.get("name");
         if (sv1 == null)
         {
            throw new IllegalStateException(o1 + " has no name");
         }
         SimpleValue sv2 = (SimpleValue) o2.get("name");
         if (sv2 == null)
         {
            throw new IllegalStateException(o2 + " has no name");
         }
         
         String name1 = (String) sv1.getValue();
         String name2 = (String) sv2.getValue();
         return name1.compareTo(name2);
      }      
   }

}
