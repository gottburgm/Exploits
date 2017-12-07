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
package org.jboss.varia.scheduler;

import java.security.PrivilegedAction;
import java.security.AccessController;

/** An encapsulation of thread context class loader PrivilegedAction for
 * getting and setting the TCL. 
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
class TCLActions
{
   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }
   private static class GetClassLoaderAction implements PrivilegedAction
   {
      Class c;
      GetClassLoaderAction(Class c)
      {
         this.c = c;
      }
      public Object run()
      {
         ClassLoader loader = c.getClassLoader();
         c = null;
         return loader;
      }
   }
   private static class SetTCLAction implements PrivilegedAction
   {
      ClassLoader loader;
      SetTCLAction(ClassLoader loader)
      {
         this.loader = loader;
      }
      public Object run()
      {
         Thread.currentThread().setContextClassLoader(loader);
         loader = null;
         return null;
      }
   }

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }
   static ClassLoader getClassLoader(Class c)
   {
      GetClassLoaderAction action = new GetClassLoaderAction(c);
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(action);
      return loader;
   }
   static void setContextClassLoader(ClassLoader loader)
   {
      PrivilegedAction action = new SetTCLAction(loader);
      AccessController.doPrivileged(action);
   }
}
