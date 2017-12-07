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
package org.jboss.system.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.dependency.spi.dispatch.InvokeDispatchContext;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * ServiceInjectionValueMetaData.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class ServiceValueFactoryValueMetaData extends AbstractMetaDataVisitorNode
   implements ServiceValueMetaData, Serializable
{
   private static final long serialVersionUID = 2;
   
   /** The dependency */
   private final Object dependency;

   /** The method */
   private final String method;
   
   private final ServiceTextValueMetaData defaultValue;
   
   private final List<ServiceValueFactoryParameterMetaData> parameterMetaData; 
   
   private Object[] parameterValues;
   
   private String[] parameterTypes;

   /** The required state of the dependency */
   private final ControllerState dependentState;
   
   /**
    * Create a new ServiceInjectionValueMetaData.
    * 
    * @param dependency the dependency
    * @param method the property name
    * @param dependentState the dependent state
    */
   @SuppressWarnings("unchecked")
   public ServiceValueFactoryValueMetaData(Object dependency, String method, List<ServiceValueFactoryParameterMetaData> parameters, ControllerState dependentState, ServiceTextValueMetaData defaultValue)
   {
      if (dependency == null)
         throw new IllegalArgumentException("Null dependency");
      this.dependency = dependency;
      
      if (method == null)
         throw new IllegalArgumentException("Null method");
      this.method = method;
      
      this.parameterMetaData = (parameters == null ? Collections.EMPTY_LIST : parameters);
      
      this.dependentState = (dependentState == null ? ControllerState.INSTALLED : dependentState);
      
      this.defaultValue = defaultValue;
   }

   /**
    * Get the dependency.
    * 
    * @return the dependency.
    */
   public Object getDependency()
   {
      return dependency;
   }

   /**
    * Get the method.
    * 
    * @return the method.
    */
   public String getMethod()
   {
      return method;
   }

   public List<ServiceValueFactoryParameterMetaData> getParameterMetaData()
   {
      return parameterMetaData;
   }

   /**
    * Get the dependentState.
    * 
    * @return the dependentState.
    */
   public ControllerState getDependentState()
   {
      return dependentState;
   }

   public ServiceTextValueMetaData getDefaultValue()
   {
      return defaultValue;
   }

   public Object getValue(ServiceValueContext valueContext) throws Throwable
   {
      KernelController controller = valueContext.getController();
      
      ControllerState state = dependentState;
      if (state == null)
         state = ControllerState.INSTALLED;

      
      ControllerContext factoryContext = controller.getContext(dependency, state);
      if (factoryContext == null)
         throw new Error("Should not be here - dependency failed! " + this);
      
      Object result = null;
      
      if (factoryContext instanceof InvokeDispatchContext)
      {
         InvokeDispatchContext idc = (InvokeDispatchContext) factoryContext;
         result = idc.invoke(method, getParameterValues(valueContext, factoryContext), getParameterTypes(valueContext, factoryContext));
      }
      else
      {
         throw new IllegalArgumentException(
               "Cannot use property attribute, context is not InvokeDispatchContext: " + factoryContext +
               ", metadata: " + this);
      }
      
      if (result == null && this.defaultValue != null)
      {
         result = this.defaultValue.getValue(valueContext);
      }
      
      return result;
   }

   public void visit(ServiceMetaDataVisitor visitor)
   {
      ServiceControllerContext context = visitor.getControllerContext();
      Object name = context.getName();
      ControllerState whenRequired = visitor.getContextState();

      DependencyItem item = new AbstractDependencyItem(name, dependency, whenRequired, dependentState);
      visitor.addDependency(item);

      visitor.visit(this);
   }

   private Object[] getParameterValues(ServiceValueContext valueContext, ControllerContext factoryContext) throws DeploymentException
   {
      if (parameterValues != null)
         return parameterValues;
      
      if (factoryContext instanceof KernelControllerContext)
      {
         analyzeParameters(valueContext, (KernelControllerContext) factoryContext);
      }
      else
      {
         extractParameters(valueContext);
      }
      
      return parameterValues;
   }
   
   private String[] getParameterTypes(ServiceValueContext valueContext, ControllerContext factoryContext) throws DeploymentException
   {
      if (parameterTypes != null)
         return parameterTypes;
      
      if (factoryContext instanceof KernelControllerContext)
         analyzeParameters(valueContext, (KernelControllerContext) factoryContext);
      else
         extractParameters(valueContext);
      
      return parameterTypes;
   }

   private void analyzeParameters(ServiceValueContext valueContext, KernelControllerContext factoryContext) throws DeploymentException
   {
      BeanInfo beanInfo = factoryContext.getBeanInfo();
      Set<MethodInfo> allMethods = beanInfo.getMethods();
      
      populateParameterTypes(allMethods, method, parameterMetaData);
      
      parameterTypes = new String[parameterMetaData.size()];
      parameterValues = new Object[parameterMetaData.size()];
      
      for (int i = 0; i < parameterMetaData.size(); i++)
      {
         ServiceValueFactoryParameterMetaData metadata = parameterMetaData.get(i);
         parameterTypes[i] = metadata.getParameterTypeName();
         parameterValues[i] = metadata.getValue(valueContext);
      }      
   }
   
   private void extractParameters(ServiceValueContext valueContext) throws DeploymentException
   {      
      parameterTypes = new String[parameterMetaData.size()];
      parameterValues = new Object[parameterMetaData.size()];
      
      for (int i = 0; i < parameterMetaData.size(); i++)
      {
         ServiceValueFactoryParameterMetaData metadata = parameterMetaData.get(i);
         parameterTypes[i] = metadata.getParameterTypeName();
         if (parameterTypes[i] == null)
         {
            parameterTypes = null;
            parameterValues = null;
            throw new IllegalStateException("No type available for parameter " + i 
                  + " -- parameter types must be specified to invoke on mbeans");
         }
         
         parameterValues[i] = metadata.getValue(valueContext);

      }
   }

   /**
    * Attempts to find a method in <code>allMethods</code> whose name and parameters
    * match the given arguments. If successful, modifies the {@link ServiceValueFactoryParameterMetaData}
    * in the provided list to ensure any null parameterTypeName values are no longer null, but instead
    * match the equivalent parameter in the MethodInfo.
    * <p>
    * If a given ServiceValueFactoryParameterMetaData has no parameterTypeName set, that is treated
    * as meaning "matches any parameter type".
    * </p>
    * 
    * @param allMethods set of methods to match against
    * @param methodName name of method to match
    * @param parameterMetaData parameters to the method
    * 
    * @throws IllegalArgumentException if less or more than one MethodInfo matches
    */
   public static void populateParameterTypes(Set<MethodInfo> allMethods, String methodName, List<ServiceValueFactoryParameterMetaData> parameterMetaData)
   {
      List<MethodInfo> possibleMatches = new ArrayList<MethodInfo>();
      for (MethodInfo mi : allMethods)
      {
         TypeInfo[] typeInfos = mi.getParameterTypes();
         if (methodName.equals(mi.getName()) && typeInfos.length == parameterMetaData.size())
         {
            boolean match = true;
            for (int i = 0; i < typeInfos.length; i++)
            {
               String ourType = parameterMetaData.get(i).getParameterTypeName();
               if (ourType != null && ourType.equals(typeInfos[i].getName()) == false)
               {
                  match = false;
                  break;
               }
            }
            
            if (match)
            {
               possibleMatches.add(mi);
            }
         }
      }
      
      if (possibleMatches.size() == 1)
      {
         MethodInfo match = possibleMatches.get(0);
         TypeInfo[] types = match.getParameterTypes();
         for (int i = 0; i < types.length; i++)
         {
            ServiceValueFactoryParameterMetaData metadata = parameterMetaData.get(i);
            if (metadata.getParameterTypeName() == null)
            {
               metadata.setParameterTypeName(types[i].getName());
            }
         }
      }
      else if (possibleMatches.size() == 0)
      {
         throw new IllegalArgumentException("Cannot match parameters to any method.");
      }
      else
      {
         throw new IllegalArgumentException("Cannot match parameters to a single method. Possible matches : " + possibleMatches);
      }
   }
}
