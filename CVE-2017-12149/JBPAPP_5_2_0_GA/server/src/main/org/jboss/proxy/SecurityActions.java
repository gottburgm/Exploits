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
package org.jboss.proxy; 

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;


/**
 * Interface defining the Privileged Blocks 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @since  Mar 5, 2007 
 * @version $Revision: 112774 $
 */
interface SecurityActions
{ 
   class UTIL
   {
      static SecurityActions getSecurityActions()
      {
         return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
      }
   }

   SecurityActions NON_PRIVILEGED = new SecurityActions()
   {
      public Principal getPrincipal()
      { 
         Principal p = null; 
         SecurityContext sc = SecurityContextAssociation.getSecurityContext(); 
         if(sc != null)
         { 
            p = sc.getUtil().getUserPrincipal();
         }
         if(p == null && SecurityContextAssociation.isClient())
            p = SecurityAssociation.getPrincipal();
         return p; 
      }

      public Object getCredential()
      { 
         Object cred = null;
         SecurityContext sc = SecurityContextAssociation.getSecurityContext(); 
         if(sc != null)
         {  
            cred = sc.getUtil().getCredential();
         }
         if(cred == null && SecurityContextAssociation.isClient())
            cred = SecurityAssociation.getCredential();
         return cred; 
      }

   };

   SecurityActions PRIVILEGED = new SecurityActions()
   {
      private final PrivilegedAction<Principal> getPrincipalAction = new PrivilegedAction<Principal>()
      {
         public Principal run()
         { 
            Principal p = null; 
            SecurityContext sc = SecurityContextAssociation.getSecurityContext(); 
            if(sc != null)
            { 
               p = sc.getUtil().getUserPrincipal();
            }
            if(p == null && SecurityContextAssociation.isClient())
              p = SecurityAssociation.getPrincipal();
            return p; 
         }
      };

      private final PrivilegedAction<Object> getCredentialAction = new PrivilegedAction<Object>()
      {
         public Object run()
         { 
            Object cred = null;
            SecurityContext sc = SecurityContextAssociation.getSecurityContext(); 
            if(sc != null)
            { 
               cred = sc.getUtil().getCredential();
            }
            if(cred == null && SecurityContextAssociation.isClient())
              cred = SecurityAssociation.getCredential();
            return cred; 
         }
      }; 

      public Principal getPrincipal()
      {
         return AccessController.doPrivileged(getPrincipalAction);
      }

      public Object getCredential()
      {
         return AccessController.doPrivileged(getCredentialAction);
      }

   };

   Principal getPrincipal();

   Object getCredential(); 
}
