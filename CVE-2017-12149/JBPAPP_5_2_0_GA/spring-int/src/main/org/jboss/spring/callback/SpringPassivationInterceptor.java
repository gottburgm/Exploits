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
package org.jboss.spring.callback;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.InvocationContext;

import org.jboss.spring.support.SpringInjectionSupport;

/**
 * Injects Spring beans only on postActivation.
 * If you need injection also on construction, use SpringLifecycleInterceptor.
 * Sets all non serializable, non transient fields to null before passivation.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @see org.jboss.spring.support.SpringInjectionSupport
 */
public class SpringPassivationInterceptor extends SpringInjectionSupport implements Serializable
{
   private static final long serialVersionUID = -7259379236645796135L;

   @PostActivate
   public void postActivate(InvocationContext ctx) throws Exception
   {
      inject(ctx.getTarget());
      ctx.proceed();
   }

   @PrePassivate
   public void prePassivate(InvocationContext ctx) throws Exception
   {
      Field[] fields = getAllFields(ctx.getTarget());
      for (Field f : fields)
      {
         boolean isSerializable = Serializable.class.isAssignableFrom(f.getType());
         boolean isTransient = Modifier.isTransient(f.getModifiers());
         if (!isSerializable && !isTransient)
         {
            f.setAccessible(true);
            f.set(ctx.getTarget(), null);
         }
      }
      ctx.proceed();
   }

   @PreDestroy
   public void preDestroy(InvocationContext ctx) throws Exception
   {
      ctx.proceed();
   }
}
