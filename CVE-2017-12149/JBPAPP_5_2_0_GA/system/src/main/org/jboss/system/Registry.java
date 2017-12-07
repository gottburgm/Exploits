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
package org.jboss.system;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

/**
 * A registry, really, a registry.
 *
 * <p>All methods static to lookup pointers from anyplace in the VM.  
 *    We use it for hooking up JMX managed objects.  Use the JMX MBeanName 
 *    to put objects here.
 *  
 * @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @version $Revision: 106922 $
 */
public class Registry
{
   private static final Logger log = Logger.getLogger(Registry.class);
   
   public static Map<Object, Object> entries = new ConcurrentHashMap<Object, Object>();
   
   // Permission required to bind/unbind would be RuntimePermission "org.jboss.system.Registry"
   private static RuntimePermission perm = new RuntimePermission( Registry.class.getName() );
   
   public static void bind(final Object key, final Object value)
   {
      SecurityManager securityManager = System.getSecurityManager();
      if( securityManager != null )
      {
         securityManager.checkPermission( perm );
      }
      entries.put(key, value);
      if(log.isTraceEnabled())
         log.trace("bound " + key + "=" + value);
   }
   
   public static Object unbind(final Object key)
   {
      SecurityManager securityManager = System.getSecurityManager();
      if( securityManager != null )
      {
         securityManager.checkPermission( perm );
      }
      
      Object obj = entries.remove(key);
      if(log.isTraceEnabled())
         log.trace("unbound " + key + "=" + obj);
      return obj;
   }
   
   public static Object lookup(final Object key)
   {
      Object obj = entries.get(key);
      if(log.isTraceEnabled())
         log.trace("lookup " + key + "=" + obj);
      return obj;
   }
}