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
import org.jboss.services.binding.ServiceBindingMetadata;

/**
 * {@link MetaMapper} for a set of {@link ServiceBindingMetadata}.
 * 
 * @author Brian Stansberry
 */
public class ServiceBindingMetadataMapper extends MetaMapper<Set<ServiceBindingMetadata>>
{
   private static final Logger log = Logger.getLogger(ServiceBindingMetadataMapper.class);
   public static final CollectionMetaType TYPE;
   public static final CompositeMetaType SERVICE_BINDING_METADATA_TYPE;
   
   static
   {
      String[] itemNames = {
            "serviceName",
            "bindingName",
            "fullyQualifiedName",
            "description",
            "hostName",
            "port",
            "fixedHostName",
            "fixedPort"//,
//            "serviceBindingValueSourceClassName",
//            "serviceBindingValueSourceConfig"
      };
      String[] itemDescriptions = {
            "the name of the service to which this binding applies",
            "a qualifier identifying which particular binding within the service this is",
            "the fully qualified binding name",
            "description of the binding",
            "the host name or string notation IP address to use for the binding",
            "the port to use for the binding",
            "whether the host name should remain fixed in all binding sets",
            "whether the port should remain fixed in all binding sets"//,
//            "fully qualified classname of specialized object used to process binding results",
//            ""
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.INTEGER_PRIMITIVE,
            SimpleMetaType.BOOLEAN_PRIMITIVE,
            SimpleMetaType.BOOLEAN_PRIMITIVE//,
//            SimpleMetaType.STRING,
//            new GenericMetaType(ManagedObject)
      };
      SERVICE_BINDING_METADATA_TYPE = new ImmutableCompositeMetaType(ServiceBindingMetadata.class.getName(), 
            "Service Binding Metadata",
            itemNames, itemDescriptions, itemTypes);
      TYPE = new CollectionMetaType(Set.class.getName(), SERVICE_BINDING_METADATA_TYPE);
   }

   public ServiceBindingMetadataMapper()
   {
      super();
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
   public MetaValue createMetaValue(MetaType metaType, Set<ServiceBindingMetadata> object)
   {
      Set<CompositeValue> tmp = new TreeSet<CompositeValue>(new FullyQualifiedNameComparator());
      for (ServiceBindingMetadata b : object)
      {
         try
         {
            MapCompositeValueSupport cvs = new MapCompositeValueSupport(SERVICE_BINDING_METADATA_TYPE);
            cvs.put("serviceName", SimpleValueSupport.wrap(b.getServiceName()));
            cvs.put("bindingName", SimpleValueSupport.wrap(b.getBindingName()));
            cvs.put("fullyQualifiedName", SimpleValueSupport.wrap(b.getFullyQualifiedName()));
            cvs.put("description", SimpleValueSupport.wrap(b.getDescription()));
            cvs.put("hostName", SimpleValueSupport.wrap(b.getHostName()));
            cvs.put("port", SimpleValueSupport.wrap(b.getPort()));
            cvs.put("fixedHostName", SimpleValueSupport.wrap(b.isFixedHostName()));
            cvs.put("fixedPort", SimpleValueSupport.wrap(b.isFixedPort()));
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
   public Set<ServiceBindingMetadata> unwrapMetaValue(MetaValue metaValue)
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
      Set<ServiceBindingMetadata> result = new HashSet<ServiceBindingMetadata>(elements.length);
      for (MetaValue element : elements)
      {

         if ((element instanceof CompositeValue) == false)
         {
            throw new IllegalArgumentException(element + " is not a " + CompositeValue.class.getSimpleName());
         }
         CompositeValue compValue = (CompositeValue) element;
         String serviceName = Util.getValueFromComposite(compValue, "serviceName", String.class);
         String bindingName = Util.getValueFromComposite(compValue, "bindingName", String.class);
         String description = Util.getValueFromComposite(compValue, "description", String.class);
         String hostName = Util.getValueFromComposite(compValue, "hostName", String.class);
         Integer port = Util.getValueFromComposite(compValue, "port", Integer.class);
         if (port == null)
         {
            throw new IllegalStateException(element + " has no value for key 'port'");
         }
         Boolean fixedHostName = Util.getValueFromComposite(compValue, "fixedHostName", Boolean.class);
         Boolean fixedPort = Util.getValueFromComposite(compValue, "fixedPort", Boolean.class);
//         String serviceBindingValueSourceClassName = getValueFromComposite(compValue, "serviceBindingValueSourceClassName", String.class);
         ServiceBindingMetadata sbm = 
            new ServiceBindingMetadata(serviceName, bindingName, hostName, port.intValue(), 
                  fixedPort == null ? false : fixedPort.booleanValue(),
                  fixedHostName == null ? false : fixedHostName.booleanValue());
         sbm.setDescription(description);
//         sbm.setServiceBindingValueSourceClassName(serviceBindingValueSourceClassName);
         result.add(sbm);
      }
      return result;
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
