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

import java.util.Set;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ConstructorMetaData;
import org.jboss.beans.metadata.spi.PropertyMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.beans.metadata.spi.factory.AbstractBeanFactory;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.reflect.spi.ClassInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * BeanFactory facade over MC's Controller.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ControllerBeanFactory implements BeanFactory
{
   private Controller controller;

   public ControllerBeanFactory(Controller controller)
   {
      if (controller == null)
         throw new IllegalArgumentException("Null controller");

      this.controller = controller;
   }

   /**
    * Get the controller context.
    *
    * @param name the context name
    * @return context or null if not found
    */
   protected ControllerContext getInstalledContext(String name)
   {
      return controller.getInstalledContext(name);
   }

   /**
    * Get the controller context.
    *
    * @param name  the context name
    * @param state the state
    * @return context or null if not found
    */
   protected ControllerContext getContext(String name, ControllerState state)
   {
      return controller.getContext(name, state);
   }

   public Object getBean(String name) throws BeansException
   {
      ControllerContext context = getInstalledContext(name);
      if (context == null)
         throw new NoSuchBeanDefinitionException(name);

      return context.getTarget();
   }

   @SuppressWarnings("unchecked")
   public Object getBean(String name, Class clazz) throws BeansException
   {
      return getExactBean(name, clazz);
   }

   /**
    * Get exact bean.
    *
    * @param name the bean name
    * @param clazz the expected class
    * @param <T> the exact type
    * @return exact bean
    * @throws BeansException for any error
    */
   protected <T> T getExactBean(String name, Class<T> clazz) throws BeansException
   {
      Object result = getBean(name);
      if (clazz.isInstance(result) == false)
         throw new BeanNotOfRequiredTypeException(name, clazz, result.getClass());

      return clazz.cast(result);
   }

   @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
   public Object getBean(String name, Object[] parameters) throws BeansException
   {
      AbstractBeanFactory result = getExactBean(name, AbstractBeanFactory.class);
      ConstructorMetaData cmd = result.getConstructor();

      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("Temp");
      for (Object parameter : parameters )
         builder.addConstructorParameter(null, parameter);
      ConstructorMetaData tempCMD = builder.getBeanMetaData().getConstructor();

      synchronized (result)
      {
         result.setConstructor(tempCMD);
         try
         {
            return createBean(result);
         }
         finally
         {
            result.setConstructor(cmd);
         }
      }
   }

   public boolean containsBean(String name)
   {
      return getInstalledContext(name) != null;
   }

   public boolean isSingleton(String name) throws NoSuchBeanDefinitionException
   {
      return isPrototype(name) == false;
   }

   public boolean isPrototype(String name) throws NoSuchBeanDefinitionException
   {
      Object result = getBean(name);
      return isPrototype(result);
   }

   /**
    * Is the result prototype.
    *
    * @param result the result
    * @return true if prototyle, false otherwise
    */
   protected boolean isPrototype(Object result)
   {
      return org.jboss.beans.metadata.spi.factory.BeanFactory.class.isInstance(result);
   }

   /**
    * Create the bean.
    *
    * @param factory the bean factory
    * @return new bean instance
    * @throws BeansException for any error
    */
   protected Object createBean(Object factory) throws BeansException
   {
      try
      {
         return org.jboss.beans.metadata.spi.factory.BeanFactory.class.cast(factory).createBean();
      }
      catch (Throwable t)
      {
         throw new FatalBeanException("Cannot create bean: " + factory, t);
      }
   }

   public boolean isTypeMatch(String name, Class clazz) throws NoSuchBeanDefinitionException
   {
      return clazz.isInstance(getBean(name));
   }

   @SuppressWarnings("deprecation")
   public Class getType(String name) throws NoSuchBeanDefinitionException
   {
      ControllerContext context = getContext(name, ControllerState.DESCRIBED);
      if (context == null)
         throw new NoSuchBeanDefinitionException(name);

      if (context instanceof KernelControllerContext)
      {
         KernelControllerContext kcc = (KernelControllerContext)context;
         BeanInfo beanInfo = kcc.getBeanInfo();
         ClassInfo classInfo = beanInfo.getClassInfo();
         TypeInfoFactory tif = classInfo.getTypeInfoFactory();
         // it's a bean factory
         if (tif.getTypeInfo(AbstractBeanFactory.class).isAssignableFrom(classInfo))
         {
            return getPrototypeClass(kcc);
         }
         else
         {
            return classInfo.getType();
         }
      }
      return null;
   }

   /**
    * Get prototype class.
    *
    * @param kcc the kernel controller context
    * @return prototype's class
    */
   protected Class<?> getPrototypeClass(KernelControllerContext kcc)
   {
      BeanMetaData bmd = kcc.getBeanMetaData();
      Set<PropertyMetaData> properties = bmd.getProperties();
      for (PropertyMetaData pmd : properties)
      {
         if ("bean".equals(pmd.getName()))
         {
            ValueMetaData value = pmd.getValue();
            if (value != null && value.getUnderlyingValue() != null)
            {
               String className = value.getUnderlyingValue().toString();
               return getBeanClass(className, kcc);
            }
            else
            {
               return null;
            }
         }
      }
      return null;
   }

   /**
    * Get the bean class.
    *
    * @param className the class name
    * @param context the context
    * @return bean's class
    * @throws BeansException for any error
    */
   protected Class<?> getBeanClass(String className, KernelControllerContext context) throws BeansException
   {
      try
      {
         ClassLoader cl = context.getClassLoader();
         return cl.loadClass(className);
      }
      catch (Throwable t)
      {
         throw new FatalBeanException("Cannot load class: " + className + ", context: " + context, t);
      }
   }

   public String[] getAliases(String name)
   {
      ControllerContext context = getContext(name, null);
      if (context == null || context.getAliases() == null)
      {
         return new String[]{};
      }
      else
      {
         Set<Object> aliases = context.getAliases();
         String[] result = new String[aliases.size()];
         int i = 0;
         for (Object alias : aliases)
         {
            result[i++] = alias.toString();
         }
         return result;
      }
   }
}
