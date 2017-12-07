/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.remoting;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.metadata.MetaDataResolver;
import org.jboss.aop.metadata.SimpleMetaData;


/**
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class PojiMethodInvocation extends MethodInvocation
{
   private MethodInvocation delegate;
   private Method method;

   public PojiMethodInvocation(MethodInvocation delegate, Method method)
   {
      super();
      this.delegate = delegate;
      this.method = method;
   }

   public void addResponseAttachment(Object key, Object val)
   {
      delegate.addResponseAttachment(key, val);
   }

   public Invocation copy()
   {
      return delegate.copy();
   }

   public boolean equals(Object obj)
   {
      return delegate.equals(obj);
   }

   public Method getActualMethod()
   {
      return method;
   }

   public Advisor getAdvisor()
   {
      return delegate.getAdvisor();
   }

   public Object[] getArguments()
   {
      return delegate.getArguments();
   }

   public int getCurrentInterceptor()
   {
      return delegate.getCurrentInterceptor();
   }

   public MetaDataResolver getInstanceResolver()
   {
      return delegate.getInstanceResolver();
   }

   public Interceptor[] getInterceptors()
   {
      return delegate.getInterceptors();
   }

   public SimpleMetaData getMetaData()
   {
      return delegate.getMetaData();
   }

   public Object getMetaData(Object group, Object attr)
   {
      return delegate.getMetaData(group, attr);
   }

   public Method getMethod()
   {
      return method;
   }

   public long getMethodHash()
   {
      return delegate.getMethodHash();
   }

   public Object getResponseAttachment(Object key)
   {
      return delegate.getResponseAttachment(key);
   }

   public Map<Object, Object> getResponseContextInfo()
   {
      return delegate.getResponseContextInfo();
   }

   public Object getTargetObject()
   {
      return delegate.getTargetObject();
   }

   public Invocation getWrapper(Interceptor[] newchain)
   {
      return delegate.getWrapper(newchain);
   }

   public int hashCode()
   {
      return delegate.hashCode();
   }

   public Object invokeNext() throws Throwable
   {
      interceptors = delegate.getInterceptors();
      currentInterceptor = delegate.getCurrentInterceptor();
      if (interceptors != null && currentInterceptor < interceptors.length)
      {
         try
         {
            return interceptors[currentInterceptor++].invoke(this);
         }
         finally
         {
            // so that interceptors like clustering can reinvoke down the chain
            currentInterceptor--;
         }
      }

      return invokeTarget();
   }

   public Object invokeNext(Interceptor[] newInterceptors) throws Throwable
   {
      // Save the old stack position
      Interceptor[] oldInterceptors = interceptors;
      int oldCurrentInterceptor = currentInterceptor;

      // Start the new stack
      interceptors = newInterceptors;
      currentInterceptor = 0;

      // Invoke the new stack
      try
      {
         return invokeNext();
      }
      finally
      {
         // Restore the old stack
         interceptors = oldInterceptors;
         currentInterceptor = oldCurrentInterceptor;
      }
   }

   public Object invokeTarget() throws Throwable
   {
      return null;
   }

   public void readExternal(ObjectInput in) throws IOException,
         ClassNotFoundException
   {
      delegate.readExternal(in);
   }

   public Object resolveAnnotation(Class<? extends Annotation> annotation)
   {
      return delegate.resolveAnnotation(annotation);
   }

   public Object resolveAnnotation(Class<? extends Annotation>[] annotations)
   {
      return delegate.resolveAnnotation(annotations);
   }

   public Object resolveClassAnnotation(Class<? extends Annotation> annotation)
   {
      return delegate.resolveClassAnnotation(annotation);
   }

   public Object resolveClassMetaData(Object key, Object attr)
   {
      return delegate.resolveClassMetaData(key, attr);
   }

   public <T extends Annotation> T resolveTypedAnnotation(Class<T> annotation)
   {
      return delegate.resolveTypedAnnotation(annotation);
   }

   public <T extends Annotation> T resolveTypedAnnotation(Class<T>[] annotations)
   {
      return delegate.resolveTypedAnnotation(annotations);
   }

   public <T extends Annotation> T resolveTypedClassAnnotation(
         Class<T> annotation)
   {
      return delegate.resolveTypedClassAnnotation(annotation);
   }

   public void setAdvisor(Advisor advisor)
   {
      delegate.setAdvisor(advisor);
   }

   public void setArguments(Object[] arguments)
   {
      delegate.setArguments(arguments);
   }

   public void setInstanceResolver(MetaDataResolver instanceResolver)
   {
      delegate.setInstanceResolver(instanceResolver);
   }

   public void setMetaData(SimpleMetaData data)
   {
      delegate.setMetaData(data);
   }

   public void setResponseContextInfo(Map<Object, Object> responseContextInfo)
   {
      delegate.setResponseContextInfo(responseContextInfo);
   }

   public void setTargetObject(Object targetObject)
   {
      delegate.setTargetObject(targetObject);
   }

   public String toString()
   {
      return delegate.toString();
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      delegate.writeExternal(out);
   }

   
}
