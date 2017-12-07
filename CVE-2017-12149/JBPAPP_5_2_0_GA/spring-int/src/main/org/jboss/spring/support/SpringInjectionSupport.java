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
package org.jboss.spring.support;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

import org.jboss.annotation.spring.Spring;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Injects objects from bean factory located in JNDI at jndiName gained
 * from @Spring annotation's field jndiName.
 * It is applied to setter methods and fields annotated with @Spring annotation.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @see MethodComparator Excludes overridden @Spring annotated methods
 *      Class type check is performed before actual setting.
 */
public abstract class SpringInjectionSupport
{
   protected Logger log = Logger.getLogger(getClass());
   private final Comparator<Method> METHOD_COMPARATOR = new MethodComparator();

   protected Object inject(Object target) throws Exception
   {
      log.debug("Invoking Spring injection: " + target.getClass().getName());

      Method[] methods = getAllMethods(target);
      for (Method m : methods)
      {
         Spring spring = m.getAnnotation(Spring.class);
         if (spring != null)
         {
            if (isSetterMethod(m))
            {
               injectToMethod(target, m, spring);
            }
            else
            {
               log.warn("Spring annotation only allowed on setter methods.");
            }
         }
      }

      Field[] fields = getAllFields(target);
      for (Field f : fields)
      {
         Spring spring = f.getAnnotation(Spring.class);
         if (spring != null)
         {
            injectToField(target, f, spring);
         }
      }

      return target;
   }

   protected Method[] getAllMethods(Object bean)
   {
      Class beanClass = bean.getClass();
      Set<Method> methods = new TreeSet<Method>(METHOD_COMPARATOR);
      while (beanClass != Object.class)
      {
         methods.addAll(Arrays.asList(beanClass.getDeclaredMethods()));
         beanClass = beanClass.getSuperclass();
      }
      return methods.toArray(new Method[methods.size()]);
   }

   protected Field[] getAllFields(Object bean)
   {
      Class beanClass = bean.getClass();
      List<Field> fields = new ArrayList<Field>();
      while (beanClass != Object.class)
      {
         fields.addAll(Arrays.asList(beanClass.getDeclaredFields()));
         beanClass = beanClass.getSuperclass();
      }
      return fields.toArray(new Field[fields.size()]);
   }

   private boolean isSetterMethod(Method m)
   {
      return m.getName().startsWith("set") && m.getParameterTypes().length == 1;
   }

   /**
    * Get jndi name for bean factory.
    * Simple check for null or empty string is applied.
    * You can override this in subclasses for any extra
    * jndi name handling.
    *
    * @param jndiName the current jndi name
    * @return jndiName parameter
    */
   protected String getJndiName(String jndiName)
   {
      if (jndiName == null || jndiName.length() == 0)
         throw new IllegalArgumentException("Empty BeanFactory jndi name.");
      return jndiName;
   }

   private Object getObjectFromBeanFactory(Spring spring, String defaultBeanName, Class beanType) throws Exception
   {
      BeanFactory beanFactory = (BeanFactory) Util.lookup(getJndiName(spring.jndiName()), BeanFactory.class);
      String beanName = spring.bean();
      if (beanName != null && beanName.length() > 0)
      {
         return beanFactory.getBean(beanName, beanType);
      }
      else
      {
         // by type injection
         if (beanFactory instanceof ListableBeanFactory)
         {
            ListableBeanFactory lbf = (ListableBeanFactory) beanFactory;
            Map beans = lbf.getBeansOfType(beanType);
            if (beans.size() > 1)
            {
               Object bean = beans.get(defaultBeanName);
               if (bean == null)
               {
                  throw new IllegalArgumentException("More than one bean of type: " + beanType);
               }
               return bean;
            }
            else if (beans.size() == 1)
            {
               return beans.values().iterator().next();
            }
            else
            {
               throw new IllegalArgumentException("No such bean by type: " + beanType);
            }
         }
         else
         {
            // bean factory is not listable - use default bean name
            return beanFactory.getBean(defaultBeanName, beanType);
         }
      }
   }

   private void injectToMethod(Object target, Method method, Spring spring) throws Exception
   {
      String defaultBeanName = getDefaultBeanName(method);
      Object bean = getObjectFromBeanFactory(spring, defaultBeanName, method.getParameterTypes()[0]);
      logInjection(spring, bean, target, method);
      method.setAccessible(true);
      method.invoke(target, bean);
   }

   protected String getDefaultBeanName(Method method)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(method.getName().substring(3, 3).toLowerCase());
      buffer.append(method.getName().substring(4));
      return buffer.toString();
   }

   private void injectToField(Object target, Field field, Spring spring) throws Exception
   {
      String defaultBeanName = getDefaultBeanName(field);
      Object bean = getObjectFromBeanFactory(spring, defaultBeanName, field.getType());
      logInjection(spring, bean, target, field);
      field.setAccessible(true);
      field.set(target, bean);
   }

   protected String getDefaultBeanName(Field field)
   {
      return field.getName();
   }

   private void logInjection(Spring spring, Object bean, Object target, Member m)
   {
      log.debug("Injecting bean '" + spring.bean() + "' of class type " +
            bean.getClass().getName() + " into " + target + " via " + m);
   }

   /**
    * Equals on overridden methods.
    * Any other solution?
    */
   private class MethodComparator implements Comparator<Method>
   {

      public int compare(Method m1, Method m2)
      {
         String name1 = m1.getName();
         String name2 = m2.getName();

         if (name1.equals(name2))
         {
            Class returnType1 = m1.getReturnType();
            Class returnType2 = m2.getReturnType();
            Class[] params1 = m1.getParameterTypes();
            Class[] params2 = m1.getParameterTypes();
            if (params1.length == params2.length)
            {
               if (returnType1.equals(returnType2))
               {
                  int i;
                  int length = params1.length;
                  for (i = 0; i < length; i++)
                  {
                     if (!params1[i].equals(params2[i]))
                     {
                        break;
                     }
                  }
                  //not equal
                  if (i < length)
                  {
                     return params1[i].getName().compareTo(params2[i].getName());
                  }
                  else
                  {
                     //overridden method
                     if (m1.getAnnotation(Spring.class) != null)
                     {
                        log.warn("Found overridden @Spring annotated method: " + m1);
                     }
                     return 0;
                  }
               }
               else
               {
                  return returnType1.getName().compareTo(returnType2.getName());
               }
            }
            else
            {
               return params1.length - params2.length;
            }
         }
         else
         {
            return name1.compareTo(name2);
         }
      }
   }
}
