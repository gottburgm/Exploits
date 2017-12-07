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
package org.jboss.mx.capability;

import org.jboss.mx.metadata.AttributeOperationResolver;
import org.jboss.mx.metadata.MethodMapper;

import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.PropertyAccess;

import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import java.lang.reflect.Method;

/**
 * Creates and binds a dispatcher
 *
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 */
public class DispatcherFactory
      implements ServerConstants
{

   /**
    * Creates a Dispatcher for an arbitrary resource.  Useful for when you don't care
    * about the AttributeOperationResolver.
    */
   public static DynamicMBean create(MBeanInfo info, Object resource) throws IntrospectionException
   {
      return create(info, resource, new AttributeOperationResolver(info));
   }

   /**
    * Creates a dispatcher for an arbitrary resource using the named AttributeOperationResolver.
    */
   public static DynamicMBean create(MBeanInfo info, Object resource, AttributeOperationResolver resolver) throws IntrospectionException
   {
      if (null == info)
      {
         throw new IllegalArgumentException("info cannot be null");
      }

      if (null == resolver)
      {
         throw new IllegalArgumentException("resolver cannot be null");
      }

      if (null == resource)
      {
         throw new IllegalArgumentException("resource cannot be null");
      }

      MethodMapper mmap = new MethodMapper(resource.getClass());
      ReflectedMBeanDispatcher dispatcher = new ReflectedMBeanDispatcher(info, resolver, resource);

      String flag = PropertyAccess.getProperty(OPTIMIZE_REFLECTED_DISPATCHER, "false");
      if (flag.equalsIgnoreCase("true"))
      {
         // FIXME: subclassing for now so I can rely on the reflection based implementation for the parts
         // that aren't implemented yet
         dispatcher = OptimizedMBeanDispatcher.create(info, resource /*, parent classloader */);
      }

      MBeanAttributeInfo[] attributes = info.getAttributes();
      for (int i = 0; i < attributes.length; i++)
      {
         MBeanAttributeInfo attribute = attributes[i];
         Method getter = null;
         Method setter = null;

         if (attribute.isReadable())
         {
            if (attribute instanceof ModelMBeanAttributeInfo)
            {
               ModelMBeanAttributeInfo mmbAttribute = (ModelMBeanAttributeInfo) attribute;
               Descriptor desc = mmbAttribute.getDescriptor();
               if (desc != null && desc.getFieldValue("getMethod") != null)
               {
                  getter = mmap.lookupGetter(mmbAttribute);
                  if (getter == null)
                  {
                     throw new IntrospectionException("no getter method found for attribute: " + attribute.getName());
                  }
               }
            }
            else
            {
               getter = mmap.lookupGetter(attribute);
               if (getter == null)
               {
                  throw new IntrospectionException("no getter method found for attribute: " + attribute.getName());
               }
            }
         }

         if (attribute.isWritable())
         {
            if (attribute instanceof ModelMBeanAttributeInfo)
            {
               ModelMBeanAttributeInfo mmbAttribute = (ModelMBeanAttributeInfo) attribute;
               Descriptor desc = mmbAttribute.getDescriptor();
               if (desc != null && desc.getFieldValue("setMethod") != null)
               {
                  setter = mmap.lookupSetter(mmbAttribute);
                  if (setter == null)
                  {
                     throw new IntrospectionException("no setter method found for attribute: " + attribute.getName());
                  }
               }
            }
            else
            {
               setter = mmap.lookupSetter(attribute);
               if (setter == null)
               {
                  throw new IntrospectionException("no setter method found for attribute: " + attribute.getName());
               }
            }
         }

         dispatcher.bindAttributeAt(i, getter, setter);
      }

      MBeanOperationInfo[] operations = info.getOperations();
      for (int i = 0; i < operations.length; i++)
      {
         MBeanOperationInfo operation = operations[i];
         Method method = mmap.lookupOperation(operation);
         if (method == null)
         {
            throw new IntrospectionException("no method found for operation: " + operation.getName()); // FIXME better error!
         }

         dispatcher.bindOperationAt(i, method);
      }

      return dispatcher;
   }
}
