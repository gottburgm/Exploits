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
package org.jboss.test.javassist.test.support;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractCollection;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.RuntimeSupport;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */

public class MyCollectionHandler implements MethodHandler
{
   private AbstractCollection delegate;

   public MyCollectionHandler(AbstractCollection delegate)
   {
      this.delegate = delegate;
   }

   public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
      throws Exception
   {
      Object result = null;
      try
      {
         result = thisMethod.invoke(delegate, args);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if( t instanceof Exception )
            throw (Exception) t;
         else if( t instanceof Error )
            throw (Error) t;
         // Not good, but simply cannot throw a Throwable
         throw e;
      }

      return result;
   }

}
