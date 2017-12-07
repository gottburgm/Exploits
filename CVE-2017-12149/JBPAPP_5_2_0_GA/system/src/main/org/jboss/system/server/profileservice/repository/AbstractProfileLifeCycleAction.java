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
package org.jboss.system.server.profileservice.repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.profileservice.spi.Profile;

/**
 * A abstract profile lifecycle action.
 * TODO use jboss-reflect for this.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87161 $
 */
public abstract class AbstractProfileLifeCycleAction extends AbstractProfileAction
{
   private static final Class<?>[] methodTypes = new Class[0];
   private static final Object[] args = new Object[0];
     
   protected abstract String getInstallMethod();
   protected abstract String getUninstallMethod();

   @Override
   public void install(Profile profile) throws Exception
   {
      invoke(profile, getInstallMethod());
   }

   @Override
   public void uninstall(Profile profile)
   {
      try
      {
         invoke(profile, getUninstallMethod());
      }
      catch(Exception e)
      {
         log.warn("Error invoking uninstall method '" + getUninstallMethod()
               + "' on profile: " + profile.getKey());
      }
   }

   protected static void invoke(Profile profile, String method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Method m = null;
      try
      {
         m = profile.getClass().getMethod(method, methodTypes);
      }
      catch(NoSuchMethodException ignore)
      {
         return;
      }
      invoke(profile, m);
   }

   private static void invoke(Profile profile, final Method method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
         method.setAccessible(true);
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               method.setAccessible(true);
               return null;
            }
         });
      }
      // invoke
      method.invoke(profile, args);
   }
   
}
