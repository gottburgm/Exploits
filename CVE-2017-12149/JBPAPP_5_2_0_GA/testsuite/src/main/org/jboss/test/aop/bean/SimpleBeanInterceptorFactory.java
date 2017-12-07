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
package org.jboss.test.aop.bean;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.advice.InterceptorFactory;

/**
 * @author ifedorenko
 */
public class SimpleBeanInterceptorFactory implements org.jboss.aop.advice.AspectFactory
{
   public Object createPerVM()
   {
      return new SimpleBeanInterceptor();
   }

   public Object createPerClass(Advisor advisor)
   {
      return new SimpleBeanInterceptor();
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      return new SimpleBeanInterceptor();
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      return new SimpleBeanInterceptor();
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      return new SimpleBeanInterceptor();
   }

   public String getName()
   {
      return getClass().getName();
   }
}
