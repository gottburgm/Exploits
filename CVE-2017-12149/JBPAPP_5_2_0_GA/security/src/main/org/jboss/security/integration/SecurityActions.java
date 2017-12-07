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
package org.jboss.security.integration;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;


/**
 *  Privileged Blocks
 *  @author Anil.Saldhana@redhat.com
 *  @since  Sep 10, 2007 
 *  @version $Revision: 88886 $
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
   
   static SecurityContext getSecurityContext()
   {
      return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>()
      {
         public SecurityContext run()
         { 
            return SecurityContextAssociation.getSecurityContext();
         }
      });
   }
   
   static Principal getPrincipal()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Principal>()
      {
         public Principal run()
         { 
            Principal principal = null;
            SecurityContext sc = getSecurityContext();
            if(sc != null)
            {
               principal = sc.getUtil().getUserPrincipal();
            }
            return principal;
         }
      });
   }
   
   static Object getCredential()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         { 
            Object credential = null;
            SecurityContext sc = getSecurityContext();
            if(sc != null)
            {
               credential = sc.getUtil().getCredential();
            }
            return credential;
         }
      });
   }
   
   static String getSystemProperty(final String key, final String defaultValue)
   {
      return AccessController.doPrivileged(new PrivilegedAction<String>()
      {
         public String run()
         { 
            return System.getProperty(key, defaultValue); 
         }
      });
   }
}