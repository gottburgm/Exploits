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
package org.jboss.web.tomcat.security;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;

import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;

/**
 * An implementation of the catalina GenericPrincipal to allow caching of
 * security manager invocation results.
 * 
 * @author remm@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
class JBossGenericPrincipal
   extends GenericPrincipal
{
   /** The authenticated user name as a Principal */
   private Principal authPrincipal = null;
   /** The caller principal name mapping if any */
   private Principal callerPrincipal = null;
   /** The authenticated user credentials */
   private Object credentials = null;
   /** The authenticated user Subject */
   private Subject subject = null;
   /** Set<Principal> of the roles assigned to the subject */
   private Set userRoles = null;

   /**
    * Create an encapsulation of the authenticated caller information.
    * 
    * @param realm - the Relam implementation creating the principal
    * @param subject - the authenticated JAAS subject
    * @param authPrincipal - the principal used for authentication and stored in
    * the security manager cache
    * @param callerPrincipal - the possibly different caller principal
    * representation of the authenticated principal
    * @param credentials - the opaque credentials used for authentication
    * @param roles - the List<String> of the role names assigned to the subject
    * @param userRoles - the Set<Principal> of the roles assigned to the subject
    */ 
   JBossGenericPrincipal(Realm realm, Subject subject,
      Principal authPrincipal, Principal callerPrincipal,
      Object credentials, List roles, Set userRoles)
   {
      super(realm, callerPrincipal.getName(), null, roles, callerPrincipal);
      this.credentials = credentials;
      this.authPrincipal = authPrincipal;
      this.callerPrincipal = callerPrincipal;
      this.subject = subject;
      this.userRoles = userRoles;
   }

   Principal getAuthPrincipal()
   {
      return this.authPrincipal;
   }
   /**
    * Gets the value of principal
    * 
    * @return the value of principal
    */
   Principal getCallerPrincipal()
   {
      return this.callerPrincipal;
   }

   /**
    * Gets the value of credentials
    * 
    * @return the value of credentials
    */
   Object getCredentials()
   {
      return this.credentials;
   }

   Subject getSubject()
   {
      return subject;
   }

   /**
    * Get the original set of role Principals
    * @return Set<Princpals> for the roles assigned to the user
    */ 
   Set getUserRoles()
   {
      return userRoles;
   }
}
