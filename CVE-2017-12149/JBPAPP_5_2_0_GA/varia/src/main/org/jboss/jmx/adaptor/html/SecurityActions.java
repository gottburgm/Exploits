/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jmx.adaptor.html;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;

class SecurityActions
{
   
   static Set getSubjectRoles() {
      Set set = null;
      set = AccessController.doPrivileged(new PrivilegedAction<Set>()
      {
         public Set run() {
            SecurityContext sc = SecurityContextAssociation.getSecurityContext();
            AuthorizationManager am = sc.getAuthorizationManager();
            Principal principal = sc.getUtil().getUserPrincipal();

            return am.getUserRoles(principal);
         }
      });
      
      return set;
   }

}
