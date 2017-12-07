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
package org.jboss.aspects.library;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85943 $
 */
class SecurityActions
{
   interface SetTcl
   {
      void setContextClassLoader(ClassLoader loader);
      
      SetTcl PRIVILEGED = new SetTcl() {

         public void setContextClassLoader(final ClassLoader loader)
         {
            AccessController.doPrivileged(new PrivilegedAction() {

               public Object run()
               {
                  Thread.currentThread().setContextClassLoader(loader);
                  return null;
               }});
         }
      };

      SetTcl NON_PRIVILEGED = new SetTcl() {

         public void setContextClassLoader(ClassLoader loader)
         {
            Thread.currentThread().setContextClassLoader(loader);
         }
      };
   }
   
   interface GetTcl
   {
      ClassLoader getContextClassLoader();
      
      GetTcl PRIVILEGED = new GetTcl() {

         public ClassLoader getContextClassLoader()
         {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

               public ClassLoader run()
               {
                  return Thread.currentThread().getContextClassLoader();
               }});
         }
      };

      GetTcl NON_PRIVILEGED = new GetTcl() {

         public ClassLoader getContextClassLoader()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      };
   }
   
   interface GetClassLoader
   {
      ClassLoader getClassLoader(Class clazz);
      
      GetClassLoader PRIVILEGED = new GetClassLoader() {

         public ClassLoader getClassLoader(final Class clazz)
         {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

               public ClassLoader run()
               {
                  return clazz.getClassLoader();
               }});
         }
      };

      GetClassLoader NON_PRIVILEGED = new GetClassLoader() {

         public ClassLoader getClassLoader(Class clazz)
         {
            return clazz.getClassLoader();
         }
      };
   }
   
   public static void setThreadContextClassLoader(ClassLoader loader)
   {
      if (System.getSecurityManager() == null)
      {
         SetTcl.NON_PRIVILEGED.setContextClassLoader(loader);
      }
      else
      {
         SetTcl.PRIVILEGED.setContextClassLoader(loader);
      }
   }
   
   public static ClassLoader getThreadContextClassLoader()
   {
      if (System.getSecurityManager() == null)
      {
         return GetTcl.NON_PRIVILEGED.getContextClassLoader();
      }
      else
      {
         return GetTcl.PRIVILEGED.getContextClassLoader();
      }
   }
   
   public static ClassLoader getClassLoader(Class clazz)
   {
      if (System.getSecurityManager() == null)
      {
         return GetClassLoader.NON_PRIVILEGED.getClassLoader(clazz);
      }
      else
      {
         return GetClassLoader.PRIVILEGED.getClassLoader(clazz);
      }
   }
}
