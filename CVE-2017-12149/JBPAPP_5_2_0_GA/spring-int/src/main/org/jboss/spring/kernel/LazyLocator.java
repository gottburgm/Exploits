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
package org.jboss.spring.kernel;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Avoid hitting delegate locator until actual bean invocation.
 * Must have delegating locator set.
 * By default NullLocator is delegating locator.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @see NullLocator
 */
public class LazyLocator extends MicrocontainerLocatorSupport implements Locator
{

   public Object locateBean(String beanName, Class targetType)
   {
      TargetSource targetSource = new LocatorTargetSource(beanName, targetType);
      ProxyFactory proxyFactory = new ProxyFactory();
      proxyFactory.addInterface(targetType);
      proxyFactory.setTargetSource(targetSource);
      return proxyFactory.getProxy();
   }

   private class LocatorTargetSource implements TargetSource
   {

      private String beanName;
      private Class targetClass;

      private Object cachedObject;

      public LocatorTargetSource(String beanName, Class targetClass)
      {
         this.beanName = beanName;
         this.targetClass = targetClass;
      }

      public Class getTargetClass()
      {
         return (this.cachedObject != null ? this.cachedObject.getClass() : this.targetClass);
      }

      public boolean isStatic()
      {
         return (this.cachedObject != null);
      }

      public Object getTarget() throws Exception
      {
         synchronized (this)
         {
            if (this.cachedObject == null)
            {
               this.cachedObject = locateBean(beanName, targetClass);
            }
            return this.cachedObject;
         }
      }

      public void releaseTarget(Object target)
      {
      }

   }

}
