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
package org.jboss.test.classloader.leak.clstore;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;

public class ClassLoaderTracker implements ClassLoaderTrackerMBean
{
   private static final Logger log = Logger.getLogger(ClassLoaderTracker.class);
   
   private ClassLoaderStore store = ClassLoaderStore.getInstance();
   private JaasSecurityManagerServiceMBean securityManagerService;
   
   public List<String> hasClassLoaders(String[] keys)
   {
      System.gc();
      List<String> list = new ArrayList<String>();
      for (String key : keys)
      {
         if (store.getClassLoader(key, false, null) != null)
            list.add(key);
      }
      return list;
   }
   
   public boolean hasClassLoader(String key)
   {
      log.debug("hasClassLoader(): calling System.gc()");
      System.gc();
      log.debug("hasClassLoader(): System.gc() done");
      return (store.getClassLoader(key, false, null) != null);
   }
   
   public boolean hasClassLoaderBeenReleased(String key)
   {
      return (store.getClassLoader(key, true, null) == null);
   }

   public void removeClassLoader(String key)
   {
      store.removeClassLoader(key);
   }

   public JaasSecurityManagerServiceMBean getSecurityManagerService()
   {
      return securityManagerService;
   }

   public void setSecurityManagerService(JaasSecurityManagerServiceMBean securityManagerService)
   {
      this.securityManagerService = securityManagerService;
   }
   
   public void flushSecurityCache(String securityDomain)
   {
      log.debug("Flushing security domain " + securityDomain);
      this.securityManagerService.flushAuthenticationCache(securityDomain);
   }

}
