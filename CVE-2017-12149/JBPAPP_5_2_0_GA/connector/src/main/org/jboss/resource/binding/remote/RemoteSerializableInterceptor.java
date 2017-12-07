/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.binding.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;

/**
 * A RemoteSerializableInterceptor that allows remote JCA clients to pass non-serializabe arguments.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class RemoteSerializableInterceptor extends Interceptor
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -6940983640563009944L;

   public Object invoke(Invocation mi) throws Throwable
   {
      
      final Object[] arguments = mi.getArguments();
      
      for (int i = 0; i < arguments.length; i++)
      {
         
         final Object argument = arguments[i];
         
         if(wrapArgument(argument)){
            
            arguments[i] = generateWrapper(argument);
             
         }
         
      }
      
      return getNext().invoke(mi);
   }
   
   private boolean wrapArgument(final Object obj){
      
      return (obj instanceof Serializable);
      
   }
   
   private Object generateWrapper(Object argument) throws Exception{
      
      RemoteSerializer serializer = RemoteSerializerFactory.getSerializer();
      Object wrapper = serializer.serialize(argument);
      return wrapper;
      
   }
 
}
