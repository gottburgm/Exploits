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

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.dependency.spi.dispatch.AttributeDispatchContext;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * ServiceInjectionValueMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceInjectionValueMetaData extends AbstractMetaDataVisitorNode
   implements ServiceValueMetaData, Serializable
{
   private static final long serialVersionUID = 2;

   /** The dependency */
   private Object dependency;

   /** The property */
   private String property;

   /** The required state of the dependency */
   private ControllerState dependentState = ControllerState.INSTALLED;

   /**
    * Create a new ServiceInjectionValueMetaData.
    */
   public ServiceInjectionValueMetaData()
   {
   }

   /**
    * Create a new ServiceInjectionValueMetaData.
    * 
    * @param dependency the dependency
    */
   public ServiceInjectionValueMetaData(Object dependency)
   {
      this(dependency, null);
   }
   
   /**
    * Create a new ServiceInjectionValueMetaData.
    * 
    * @param dependency the dependency
    * @param property the property name
    */
   public ServiceInjectionValueMetaData(Object dependency, String property)
   {
      this(dependency, property, ControllerState.INSTALLED);
   }
   
   /**
    * Create a new ServiceInjectionValueMetaData.
    * 
    * @param dependency the dependency
    * @param property the property name
    * @param dependentState the dependent state
    */
   public ServiceInjectionValueMetaData(Object dependency, String property, ControllerState dependentState)
   {
      setDependency(dependency);
      setProperty(property);
      setDependentState(dependentState);
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
    * Set the dependency.
    * 
    * @param dependency the dependency.
    */
   public void setDependency(Object dependency)
   {
      if (dependency == null)
         throw new IllegalArgumentException("Null dependency");
      this.dependency = dependency;
   }

   /**
    * Get the property.
    * 
    * @return the property.
    */
   public String getProperty()
   {
      return property;
   }

   /**
    * Set the property.
    * 
    * @param property the property.
    */
   public void setProperty(String property)
   {
      this.property = property;
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

   /**
    * Set the dependentState.
    * 
    * @param dependentState the dependentState.
    */
   public void setDependentState(ControllerState dependentState)
   {
      this.dependentState = dependentState;
   }

   public Object getValue(ServiceValueContext valueContext) throws Throwable
   {
      KernelController controller = valueContext.getController();
      
      ControllerState state = dependentState;
      if (state == null)
         state = ControllerState.INSTALLED;

      ControllerContext context = controller.getContext(dependency, state);
      if (context == null)
         throw new Error("Should not be here - dependency failed! " + this);
      Object result = context.getTarget();
      if (property != null)
      {
         if (context instanceof AttributeDispatchContext)
         {
            AttributeDispatchContext adc = (AttributeDispatchContext)context;
            result = adc.get(property);
         }
         else
            throw new IllegalArgumentException(
                  "Cannot use property attribute, context is not AttributeDispatchContext: " + context +
                  ", metadata: " + this);
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
}
