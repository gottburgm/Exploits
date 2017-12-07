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
package org.jboss.profileservice.management;

import org.jboss.deployers.spi.management.ContextStateMapper;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;

/**
 * Abstract component dispatcher.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractRuntimeComponentDispatcher implements RuntimeComponentDispatcher
{
   private MetaValueFactory valueFactory;
   /** */
   private static ThreadLocal<ManagedProperty> activeProperty =
      new ThreadLocal<ManagedProperty>();
   private static ThreadLocal<ManagedOperation> activeOperation =
      new ThreadLocal<ManagedOperation>();

   public static ManagedProperty getActiveProperty()
   {
      return activeProperty.get();
   }
   public static void setActiveProperty(ManagedProperty property)
   {
      activeProperty.set(property);
   }
   public static ManagedOperation getActiveOperation()
   {
      return activeOperation.get();
   }
   public static void setActiveOperation(ManagedOperation op)
   {
      activeOperation.set(op);
   }

   protected AbstractRuntimeComponentDispatcher(MetaValueFactory valueFactory)
   {
      if (valueFactory == null)
         valueFactory = MetaValueFactory.getInstance();
      this.valueFactory = valueFactory;
   }

   /**
    * Get the state of the component
    *
    * @param name the component name
    * @return state enum value
    */
   public abstract String getState(Object name);
   
   /**
    * Map the state of the component.
    * 
    * @param <T> the state enum
    * @param name the component name
    * @param mapper the state mapper
    * @return the mapped state
    */
   public abstract <T extends Enum<?>> T mapControllerState(Object name, ContextStateMapper<T> mapper);

   /**
    * Create meta value.
    *
    * @param value the value
    * @return meta value instance
    */
   protected MetaValue create(Object value)
   {
      MetaValue mvalue = valueFactory.create(value);
      return mvalue;
   }

   /**
    * Unwrap meta value.
    *
    * @param metaValue the meta value
    * @return unwrapped value
    */
   protected Object unwrap(MetaValue metaValue)
   {
      return valueFactory.unwrap(metaValue);
   }

   /**
    * Get the arguments from meta values.
    *
    * @param param the meta value parameters
    * @return unwrapped object array
    */
   protected Object[] toArguments(MetaValue... param)
   {
      Object[] args = new Object[param.length];
      for(int i=0; i < param.length; i++)
      {
         args[i] = unwrap(param[i]);
      }
      return args;
   }

   /**
    * Get the parameters signatures.
    *
    * @param param the parameters
    * @return signatures
    */
   protected static String[] toSignature(MetaValue... param)
   {
      String[] signature = new String[param.length];
      for(int i=0; i < param.length; i++)
      {
         signature[i] = param[i].getMetaType().getTypeName();
      }
      return signature;
   }
}
