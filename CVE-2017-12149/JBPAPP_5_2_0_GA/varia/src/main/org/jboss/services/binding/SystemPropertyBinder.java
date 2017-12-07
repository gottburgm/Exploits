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

package org.jboss.services.binding;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

/**
 * Defines system properties for a given set of {@link SystemPropertyBinding}s
 * in its {@link #start()} phase, and clears them in its {@link #stop()} phase.
 * <p>
 * Intent is this service would be used as a utility to convert 
 * {@link ServiceBindingManager} values into system properties so other
 * services could consume them without being aware of 
 * <code>ServiceBindingManager</code>. 
 * <p/>
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class SystemPropertyBinder
{
   private final Set<SystemPropertyBinding> bindings;
   
   public SystemPropertyBinder(Set<SystemPropertyBinding> bindings)
   {
      this.bindings = bindings;
   }
   
   public void start()
   {
      for (SystemPropertyBinding binding : bindings)
      {
         final String property = binding.getProperty();
         final String value = binding.getValue();
         
         if (System.getSecurityManager() == null)
         {
            System.setProperty(property, value);
         }
         else
         {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

               public Object run()
               {
                  System.setProperty(property, value);
                  return null;
               }
               
            });
         }
      }
      
   }
   
   public void stop()
   {
      for (SystemPropertyBinding binding : bindings)
      {
         final String property = binding.getProperty();
         
         if (System.getSecurityManager() == null)
         {
            System.clearProperty(property);
         }
         else
         {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

               public Object run()
               {
                  System.clearProperty(property);
                  return null;
               }
               
            });
         }
      }
   }
}
