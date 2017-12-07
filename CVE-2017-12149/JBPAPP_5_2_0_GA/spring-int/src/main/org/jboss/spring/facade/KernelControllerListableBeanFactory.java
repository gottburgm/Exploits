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
package org.jboss.spring.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.beans.metadata.spi.factory.AbstractBeanFactory;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.ControllerStateModel;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * ListableBeanFactory facade over MC's KernelController.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class KernelControllerListableBeanFactory extends ControllerBeanFactory implements ListableBeanFactory
{
   private KernelController kernelController;

   public KernelControllerListableBeanFactory(KernelController kernelController)
   {
      super(kernelController);
      this.kernelController = kernelController;
   }

   public KernelControllerListableBeanFactory(Kernel kernel)
   {
      super(kernel != null ? kernel.getController() : null);
      this.kernelController = kernel.getController();
   }

   public boolean containsBeanDefinition(String name)
   {
      return getContext(name, null) != null;
   }

   public int getBeanDefinitionCount()
   {
      int count = 0;
      ControllerStateModel stateModel = kernelController.getStates();
      for (ControllerState state : stateModel)
      {
         Set<ControllerContext> byState = kernelController.getContextsByState(state);
         count += byState.size();
      }
      return count;
   }

   public String[] getBeanDefinitionNames()
   {
      List<String> result = new ArrayList<String>();
      ControllerStateModel stateModel = kernelController.getStates();
      for (ControllerState state : stateModel)
      {
         Set<ControllerContext> byState = kernelController.getContextsByState(state);
         for (ControllerContext context : byState)
            result.add(context.getName().toString());
      }
      return result.toArray(new String[result.size()]);
   }

   public String[] getBeanNamesForType(Class clazz)
   {
      return getBeanNamesForType(clazz, true, true);
   }

   public String[] getBeanNamesForType(Class clazz, boolean includePrototypes, boolean allowEagerInit)
   {
      List<String> result = new ArrayList<String>();
      Set<KernelControllerContext> contexts = kernelController.getInstantiatedContexts(clazz);
      if (contexts != null && contexts.isEmpty() == false)
      {
         for (KernelControllerContext context : contexts)
         {
            result.add(context.getName().toString());
         }
      }
      if (includePrototypes)
      {
         Set<KernelControllerContext> factories = kernelController.getInstantiatedContexts(AbstractBeanFactory.class);
         if (factories != null && factories.isEmpty() == false)
         {
            for (KernelControllerContext kcc : factories)
            {
               Class<?> prototypeClass = getPrototypeClass(kcc);
               if (prototypeClass != null)
               {
                  if (clazz.isAssignableFrom(prototypeClass))
                     result.add(kcc.getName().toString());
               }
               else if (allowEagerInit)
               {
                  Object bean = createBean(kcc.getTarget());
                  if (clazz.isInstance(bean))
                     result.add(kcc.getName().toString());
               }
            }
         }
      }
      return result.toArray(new String[result.size()]);
   }

   public Map getBeansOfType(Class clazz) throws BeansException
   {
      return getBeansOfType(clazz, true, true);
   }

   public Map getBeansOfType(Class clazz, boolean includePrototypes, boolean allowEagerInit) throws BeansException
   {
      Map<String, Object> result = new HashMap<String, Object>();
      Set<KernelControllerContext> contexts = kernelController.getContexts(clazz, ControllerState.INSTALLED);
      for (KernelControllerContext context : contexts)
      {
         Object target = context.getTarget();
         result.put(context.getName().toString(), target);
      }
      if (includePrototypes)
      {
         Set<KernelControllerContext> factories = kernelController.getInstantiatedContexts(AbstractBeanFactory.class);
         if (factories != null && factories.isEmpty() == false)
         {
            for (KernelControllerContext kcc : factories)
            {
               Class<?> prototypeClass = getPrototypeClass(kcc);
               if (prototypeClass != null)
               {
                  if (clazz.isAssignableFrom(prototypeClass))
                     result.put(kcc.getName().toString(), createBean(kcc.getTarget()));
               }
               else if (allowEagerInit)
               {
                  Object bean = createBean(kcc.getTarget());
                  if (clazz.isInstance(bean))
                     result.put(kcc.getName().toString(), bean);
               }
            }
         }
      }
      return result;
   }
}