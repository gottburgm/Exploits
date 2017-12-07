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
package org.jboss.web.tomcat.service.injection;

import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorFactory;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.BeanPropertyFactory;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * TomcatInjectionContainer injection utils.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class TomcatInjectionUtils extends InjectionUtil
{
   
   public static <X extends RemoteEnvironment> void processDynamicBeanAnnotations(InjectionContainer container, Collection<InjectionHandler<X>> handlers, Class<?> clazz)
   {
      Map<AccessibleObject, Injector> classInjectors = container.getEncInjections().get(clazz.getName());
      if(classInjectors == null)
      {
         classInjectors = new HashMap<AccessibleObject, Injector>();
         container.getEncInjections().put(clazz.getName(), classInjectors);
      }
      
      HashSet<String> visitedMethods = new HashSet<String>();
      collapseXmlMethodInjectors(visitedMethods, clazz, container.getEncInjections(), classInjectors);

      processClassAnnotations(container, handlers, clazz);
      visitedMethods = new HashSet<String>();
      processMethodAnnotations(container, handlers, visitedMethods, clazz, classInjectors);
      processFieldAnnotations(container, handlers, clazz, classInjectors);
   }
   
   
   public static void createInjectors(Map<String, Map<AccessibleObject, Injector>> injectors, ClassLoader classLoader, InjectorFactory<?> factory, Collection<ResourceInjectionTargetMetaData> injectionTargets)
   {
      for(ResourceInjectionTargetMetaData injectionTarget : injectionTargets)
      {
         Map<AccessibleObject, Injector> map = injectors.get(injectionTarget.getInjectionTargetClass());
         if(map == null)
         {
            map = new HashMap<AccessibleObject, Injector>();
            injectors.put(injectionTarget.getInjectionTargetClass(), map);
         }
         
         AccessibleObject ao = InjectionUtil.findInjectionTarget(classLoader, injectionTarget);
         BeanProperty property = BeanPropertyFactory.create(ao);
         map.put(ao, factory.create(property));
      }
   }

}

