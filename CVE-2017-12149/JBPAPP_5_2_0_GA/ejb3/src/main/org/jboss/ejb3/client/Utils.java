/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.client;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.jboss.injection.InjectionContainer;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorFactory;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.BeanPropertyFactory;
import org.jboss.metadata.javaee.spec.ResourceInjectionMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * Injection utilities
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class Utils
{
   /**
    * Create and add multiple injectors for injection targets.
    * 
    * @param injectors          the list on which to add injectors
    * @param classLoader        the class loader to resolve an injection target
    * @param factory            the injector factory
    * @param injectionTargets   the injection targets
    */
   public static void createInjectors(List<Injector> injectors,
         ClassLoader classLoader,
         InjectorFactory<?> factory,
         Collection<ResourceInjectionTargetMetaData> injectionTargets)
   {
      if(injectionTargets == null)
         return;

      for(ResourceInjectionTargetMetaData injectionTarget : injectionTargets)
      {
         AccessibleObject ao = findInjectionTarget(classLoader, injectionTarget);
         BeanProperty property = BeanPropertyFactory.create(ao);
         injectors.add(factory.create(property));
      }
   }

   public static Class<?> injectionTarget(String encName,
         ResourceInjectionMetaData ref,
         InjectionContainer container)
   {
      Class<?> injectionType = null;
      
      if(ref.getInjectionTargets() == null)
         return injectionType;

      ClassLoader loader = container.getClassloader();
      for(ResourceInjectionTargetMetaData injectionTarget : ref.getInjectionTargets())
      {
         AccessibleObject ao = findInjectionTarget(loader, injectionTarget);
         BeanProperty prop = BeanPropertyFactory.create(ao);
         JndiPropertyInjector propInjector = new JndiPropertyInjector(prop, encName, container.getEnc());
         container.getInjectors().add(propInjector);
         // Validate all the injection types are consistent
         Class<?> type;
         if (ao instanceof Field)
         {
            type = ((Field) ao).getType();
         }
         else
         {
            type = ((Method) ao).getParameterTypes()[0];
         }
         if(injectionType == null)
            injectionType = type;
         else
         {
            if(!injectionType.equals(type))
               throw new IllegalStateException("Found multiple injection targets with different types");
         }
      }
      
      return injectionType;
   }
   public static AccessibleObject findInjectionTarget(ClassLoader loader, ResourceInjectionTargetMetaData target)
   {
      Class<?> clazz = null;
      try
      {
         clazz = loader.loadClass(target.getInjectionTargetClass());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("<injection-target> class: " + target.getInjectionTargetClass() + " was not found in deployment");
      }

      for (Field field : clazz.getDeclaredFields())
      {
         if (target.getInjectionTargetName().equals(field.getName()))
            return field;
      }

      for (Method method : clazz.getDeclaredMethods())
      {
         if (method.getName().equals(target.getInjectionTargetName()))
            return method;
      }

      throw new RuntimeException("<injection-target> could not be found: " + target.getInjectionTargetClass() + "." + target.getInjectionTargetName());
   }

   public static String getEncName(Class type)
   {
      return "env/" + type.getName();
   }

   public static String getEncName(Method method)
   {
      String encName = method.getName().substring(3);
      if (encName.length() > 1)
      {
         encName = encName.substring(0, 1).toLowerCase() + encName.substring(1);
      }
      else
      {
         encName = encName.toLowerCase();
      }

      encName = getEncName(method.getDeclaringClass()) + "/" + encName;
      return encName;
   }

   public static String getEncName(Field field)
   {
      return getEncName(field.getDeclaringClass()) + "/" + field.getName();
   }
}
