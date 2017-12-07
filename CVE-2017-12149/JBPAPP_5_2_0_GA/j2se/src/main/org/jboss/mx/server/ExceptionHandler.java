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
package org.jboss.mx.server;

// $Id: ExceptionHandler.java 81019 2008-11-14 12:40:42Z dimitris@jboss.org $

import javax.management.*;

/**
 * Handles exceptions and wraps them if neccessary, arccording to the spec.
 * 
 * @author thomas.diesler@jboss.org
 */
public class ExceptionHandler
{

   // hide constructor
   private ExceptionHandler()
   {
   }

   /**
    * Handles exceptions and wraps them if neccessary, arccording to the spec.
    *
    * @param t the exception thrown by the invocation
    * @return any wrapped exception
    */
   public static JMException handleException(Throwable t)
   {
      handleRuntimeExceptionOrError(t);

      // when we get here, only exceptions are left
      Exception e = (Exception)t;

      if (e instanceof OperationsException)
         return (OperationsException)e;
      if (e instanceof ReflectionException)
         return (ReflectionException)e;
      if (e instanceof MBeanRegistrationException)
         return (MBeanRegistrationException)e;

      // wrap the core java exceptions
      if (e instanceof ClassNotFoundException)
         return new ReflectionException(e);
      if (e instanceof IllegalAccessException)
         return new ReflectionException(e);
      if (e instanceof InstantiationException)
         return new ReflectionException(e);
      if (e instanceof NoSuchMethodException)
         return new ReflectionException(e);

      // The MBeanException is the one that might wrap other exceptions
      // For example, the AbstractMBeanInvoker.invoke cannot throw OperationsException
      if (e instanceof MBeanException)
      {
         Throwable cause = e.getCause();

         if (cause instanceof JMException)
            return (JMException)cause;
         else
            return (MBeanException)e;
      }

      // wrap any exception thrown by an mbean
      return new MBeanException(e);
   }

   /**
    * Handles runtime exceptions and rethrows them wraped if neccessary, arccording to the spec.
    *
    * @param e the exception thrown by the invocation
    */
   private static void handleRuntimeExceptionOrError(Throwable e)
   {
      // is already of throwable type
      if (e instanceof RuntimeOperationsException)
         throw (RuntimeOperationsException)e;
      if (e instanceof RuntimeErrorException)
         throw (RuntimeErrorException)e;
      if (e instanceof RuntimeMBeanException)
         throw (RuntimeMBeanException)e;

      // wrap java core runtime exceptions
      if (e instanceof IllegalArgumentException)
         throw new RuntimeOperationsException((IllegalArgumentException)e);
      if (e instanceof IndexOutOfBoundsException)
         throw new RuntimeOperationsException((IndexOutOfBoundsException)e);
      if (e instanceof NullPointerException)
         throw new RuntimeOperationsException((NullPointerException)e);

      // wrap any error
      if (e instanceof Error)
         throw new RuntimeErrorException((Error)e);

      // wrap any runtime exception
      if (e instanceof RuntimeException)
         throw new RuntimeMBeanException((RuntimeException)e);
   }

}
