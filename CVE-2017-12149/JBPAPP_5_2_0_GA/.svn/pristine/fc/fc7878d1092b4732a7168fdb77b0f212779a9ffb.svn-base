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
package org.jboss.security.integration.password;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Apr 1, 2009
 */
class SecurityActions
{
   static ClassLoader getContextClassLoader()
   {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() 
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      }); 
   }
   
   static Method getMethod(final Class<?> clazz, final String methodName) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() 
      {
         public Method run() throws Exception
         {
            Method m = null;
            try
            {
               m = clazz.getMethod(methodName, new Class[] {String.class});
            }
            catch(Exception ignore)
            {   
            }
            
            if(m == null)
               try
            {
               m = clazz.getMethod(methodName, new Class[] {char[].class});
            }
            catch(Exception ignore)
            {   
            } 
            return m; 
         }
      }); 
   }
}