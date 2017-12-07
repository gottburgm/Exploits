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
package org.jboss.mx.util;

import javax.management.*;

/**
 * A simple helper to rethrow and/or decode those pesky 
 * JMX exceptions.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 81019 $
 */
public class JMXExceptionDecoder
{
   /**
    * Attempt to decode the given Throwable.  If it
    * is a container JMX exception, then the target
    * is returned.  Otherwise the argument is returned.
    */
   public static Throwable decode(final Throwable t)
   {
      Throwable result = t;
      
      while (true)
      {
         if (result instanceof MBeanException)
            result = ((MBeanException) result).getTargetException();
         else if (result instanceof ReflectionException)
            result = ((ReflectionException) result).getTargetException();
         else if (result instanceof RuntimeOperationsException)
            result = ((RuntimeOperationsException) result).getTargetException();
         else if (result instanceof RuntimeMBeanException)
            result = ((RuntimeMBeanException) result).getTargetException();
         else if (result instanceof RuntimeErrorException)
            result = ((RuntimeErrorException) result).getTargetError();
         else
            // can't decode
            break;
      }

      return result;
   }

   /** Unwrap a possibly nested jmx exception down to the last 
    * JMException || JMRuntimeException.
    * 
    * @param ex the exception to unwrap
    * @return A JMException || JMRuntimeException if ex was of this type, or
    *    ex if it was not.
    */ 
   public static Throwable decodeToJMXException(final Throwable ex)
   {
      Throwable jmxEx = ex;
      Throwable lastJmxEx = ex;
      while( jmxEx instanceof JMException || jmxEx instanceof JMRuntimeException)
      {
         lastJmxEx = jmxEx;
         jmxEx = decode(jmxEx);
         if( jmxEx == lastJmxEx )
            break;
      }

      return lastJmxEx;
   }

   /**
    * Decode and rethrow the given Throwable.  If it
    * is a container JMX exception, then the target
    * is thrown.  Otherwise the argument is thrown.
    */
   public static void rethrow(final Exception e)
      throws Exception
   {
      Throwable t = decode(e);
      if (t instanceof Exception)
         throw (Exception) t;
      else
         throw (Error) t;
   }
}
