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
package org.jboss.spring.cluster;

import java.lang.annotation.Annotation;

import org.jboss.cache.pojo.PojoCache;
import org.jboss.cache.pojo.annotation.Replicable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Pojo cache / cluster post processor.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class CachePostProcessor extends CacheLookup implements BeanFactoryPostProcessor, BeanPostProcessor
{
   private String scopeName = "cache";

   public CachePostProcessor(PojoCache pojoCache)
   {
      super(pojoCache);
   }

   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
      beanFactory.registerScope(scopeName, new CacheScope(pojoCache));
   }

   public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
   {
      Class<?> beanClass = bean.getClass();
      if (beanClass.isAnnotationPresent(getMarkerAnnotation()))
      {
         Object result = get(beanName);
         if (result != null)
         {
            return result;
         }
         else
         {
            return put(beanName, bean);
         }
      }
      return bean;
   }

   public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
   {
      return bean;
   }

   /**
    * Get the bean cache / cluster marker annotation.
    *
    * @return the cache / cluster marker annotation
    */
   public Class<? extends Annotation> getMarkerAnnotation()
   {
      return Replicable.class;
   }

   /**
    * Set scope name.
    *
    * @param scopeName the scope name
    */
   public void setScopeName(String scopeName)
   {
      this.scopeName = scopeName;
   }
}
