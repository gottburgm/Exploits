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
package org.jboss.mx.mxbean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;

import org.jboss.util.UnreachableStatementException;

/**
 * CompositeDataInvocationHandler.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataInvocationHandler implements InvocationHandler
{
   /** The composite data */
   private CompositeData compositeData;
   
   /**
    * Create a new CompositeDataInvocationHandler.
    * 
    * @param compositeData
    */
   public CompositeDataInvocationHandler(CompositeData compositeData)
   {
      if (compositeData == null)
         throw new IllegalArgumentException("Null compositeData");
      this.compositeData = compositeData;
   }

   /**
    * Get the compositeData.
    * 
    * @return the compositeData.
    */
   public CompositeData getCompositeData()
   {
      return compositeData;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (Object.class.equals(method.getDeclaringClass()))
         return handleObjectInvocation(method.getName(), args);
      
      Object value = getValue(method);

      Type returnType = method.getGenericReturnType(); 
      return MXBeanUtils.reconstruct(returnType, value, method);
   }
   
   private Object getValue(Method method)
   {
      String key = MXBeanUtils.getCompositeDataKey(method);
      if (key == null)
         throw new IllegalArgumentException("Unsupported method '" + method + "'; it must be a property getter.");
      try
      {
         return compositeData.get(key);
      }
      catch (InvalidKeyException e)
      {
         throw new IllegalArgumentException("Unsupported method '" + method + "'; it must be a property getter for one of the item names of the composite data: " + compositeData, e);
      }
   }
   
   private Object handleObjectInvocation(String name, Object[] args) throws Throwable
   {
      if ("equals".equals(name))
      {
         Object object = args[0];
         if (object == null || object instanceof Proxy == false)
            return false;
         InvocationHandler handler = Proxy.getInvocationHandler(object);
         if (handler == this)
            return true;
         if (handler == null || handler instanceof CompositeDataInvocationHandler == false)
            return false;
         
         CompositeDataInvocationHandler other = (CompositeDataInvocationHandler) handler;
         return getCompositeData().equals(other.getCompositeData());
      }
      else if ("hashCode".equals(name))
         return getCompositeData().hashCode();
      else if ("toString".equals(name))
         return getCompositeData().toString();
      throw new UnreachableStatementException();
   }
}
