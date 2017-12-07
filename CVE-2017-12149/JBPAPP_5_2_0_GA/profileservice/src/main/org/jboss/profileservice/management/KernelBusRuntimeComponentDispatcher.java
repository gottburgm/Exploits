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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.management.ContextStateMapper;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.registry.KernelBus;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedParameter;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.EnumValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.profileservice.spi.types.ControllerStateMetaType;

/**
 * Microcontainer KernelBus runtime component dispatcher.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 88971 $
 */
public class KernelBusRuntimeComponentDispatcher extends AbstractRuntimeComponentDispatcher
{
   
   private static final Logger log = Logger.getLogger(KernelBusRuntimeComponentDispatcher.class);
   
   private KernelBus bus;
   private Kernel kernel;

   public KernelBusRuntimeComponentDispatcher(Kernel kernel)
   {
      this(kernel, null);
   }

   public KernelBusRuntimeComponentDispatcher(Kernel kernel, MetaValueFactory valueFactory)
   {
      super(valueFactory);
      if (kernel == null)
         throw new IllegalArgumentException("Null kernel");

      this.kernel = kernel;
      this.bus = kernel.getBus();
   }

   /**
    * Check kernel and bus.
    */
   public void start()
   {
      if (kernel == null)
         throw new IllegalArgumentException("Null kernel");
      if (bus == null)
         throw new IllegalArgumentException("Null kernel bus");
   }

   public MetaValue get(Object componentName, String propertyName)
   {
      ManagedProperty prop = AbstractRuntimeComponentDispatcher.getActiveProperty();
      try
      {
         Object value = bus.get(componentName, propertyName);
         MetaValue mvalue = null;
         if(value != null)
         {
            if(prop != null)
            {
               MetaMapper mapper = prop.getTransientAttachment(MetaMapper.class);
               if(mapper != null)
                  mvalue = mapper.createMetaValue(prop.getMetaType(), value);
               else
                  mvalue = create(value);                  
            }
            else
            {
               mvalue = create(value);
            }
         }
         return mvalue;
      }
      catch (Throwable t)
      {
         if(propertyName.equals("state") && prop.getMetaType().equals(ControllerStateMetaType.TYPE))
         {
            String stateString = getState(componentName);
            EnumValueSupport state = new EnumValueSupport(ControllerStateMetaType.TYPE, stateString);
            return state;
         }
         else
         {
            throw new UndeclaredThrowableException(t, "Failed to get property '" + propertyName + "' on component '" + componentName + "'.");
         }
      }
   }

   public void set(Object componentName, String propertyName, MetaValue value)
   {
      try
      {
         ManagedProperty prop = AbstractRuntimeComponentDispatcher.getActiveProperty();
         Object uvalue = null;
         if(prop != null)
         {
            MetaMapper mapper = prop.getTransientAttachment(MetaMapper.class);
            if(mapper != null)
               uvalue = mapper.unwrapMetaValue(value);
            else
               uvalue = unwrap(value);
         }
         else
         {
            uvalue = unwrap(value);
         }
         bus.set(componentName, propertyName, uvalue);
      }
      catch (Throwable t)
      {
         throw new UndeclaredThrowableException(t, "Failed to set property '" + propertyName + "' on component '" + componentName + "' to value [" + value + "].");
      }
   }

   public Object invoke(Object componentName, String methodName, MetaValue... param)
   {
      try
      {
         ManagedOperation op = AbstractRuntimeComponentDispatcher.getActiveOperation();
         String[] sig = new String[param.length];
         Object[] args = new Object[param.length];
         if(op != null)
         {
            ManagedParameter[] params = op.getParameters();
            if(params != null &&  params.length == param.length)
            {
               for(int i=0; i < param.length; i++)
               {
                  ManagedParameter mp = params[i];
                  MetaMapper<?> mapper = mp.getTransientAttachment(MetaMapper.class);
                  if(mapper != null)
                     args[i] = mapper.unwrapMetaValue(param[i]);
                  else
                     args[i] = unwrap(param[i]);
                  //
                  sig[i] = mp.getMetaType().getTypeName();
               }               
            }
            else
            {
               args = toArguments(param);
               sig = toSignature(param);
            }
         }
         else
         {
            args = toArguments(param);
            sig = toSignature(param);
         }
         // Invoke
         Object value = bus.invoke(componentName, methodName, args, sig);
         MetaValue mvalue = null;
         if (value != null)
         {
            // Look for a return type MetaMapper
            MetaMapper returnTypeMapper = op.getTransientAttachment(MetaMapper.class);
            if (returnTypeMapper != null)
               mvalue = returnTypeMapper.createMetaValue(op.getReturnType(), value);
            else
               mvalue = create(value);
         }
         return mvalue;
      }
      catch (Throwable t)
      {
         throw new UndeclaredThrowableException(t, "Failed to invoke method '" + methodName + "' on component '" + componentName + "' with parameters " + Arrays.asList(param) + ".");
      }
   }

   public String getState(Object name)
   {
      KernelController controller = kernel.getController();
      ControllerContext context = controller.getContext(name, null);
      if (context == null)
         throw new IllegalStateException("Context not installed: " + name);

      ControllerState state = context.getState();
      return state.getStateString();
   }
   
   public <T extends Enum<?>> T mapControllerState(Object name, ContextStateMapper<T> mapper)
   {
      if(name == null)
         throw new IllegalArgumentException("null name");
      if(mapper == null)
         throw new IllegalArgumentException("null mapper");

      KernelController controller = kernel.getController();
      ControllerContext context = controller.getContext(name, null);
      if (context == null)
         throw new IllegalStateException("Context not installed: " + name);
      
      ControllerState requiredState = null;
      // FIXME
      if(context instanceof KernelRegistryEntry == false)
         requiredState = context.getRequiredState();
      return mapper.map(context.getState(), requiredState);
   }
}
