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
package org.jboss.test.classloader.scoping.singleton;

import java.security.CodeSource;

import org.jboss.system.ServiceMBeanSupport;

/** A service that validates that its version of the MySingleton corresponds
 * the version passed into the checkVersion operation.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class TestService extends ServiceMBeanSupport
   implements TestServiceMBean
{
   protected void startService()
   {
      MySingleton singleton = MySingleton.getInstance();
      log.debug("Start called, singleton="+singleton);
      log.debug("Singleton version="+singleton.getVersion());
   }

   public boolean checkVersion(String version)
   {
      MySingleton singleton = MySingleton.getInstance();
      CodeSource cs = singleton.getClass().getProtectionDomain().getCodeSource();
      log.debug("MySingleton.CS: "+cs);
      log.debug("Comparing version: "+version+", to: "+singleton.getVersion());
      return version.equals(singleton.getVersion());
   }

}
