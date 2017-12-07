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
package org.jboss.security.plugins;

import java.security.Principal;
import java.util.Set;

/** An MBean interface that unifies the AuthenticationManager and RealmMapping
 * security interfaces implemented by a security manager for a given domain
 * and provides access to this functionality across all domains by including
 * the security domain name as a method argument.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public interface SecurityManagerMBean
{

   /** The isValid method is invoked to see if a user identity and associated
    credentials as known in the operational environment are valid proof of the
    user identity.
    @param securityDomain - the name of the security to use
    @param principal - the user identity in the operation environment
    @param credential - the proof of user identity as known in the
    operation environment
    @return true if the principal, credential pair is valid, false otherwise.
   */
   public boolean isValid(String securityDomain, Principal principal, Object credential);

    /** Map from the operational environment Principal to the application
     domain principal. This is used by the EJBContext.getCallerPrincipal implentation
     to map from the authenticated principal to a principal in the application
     domain.
    @param principal - the caller principal as known in the operation environment.
    @return the principal
    */
    public Principal getPrincipal(String securityDomain, Principal principal);

    /** Validates the application domain roles to which the operational
    environment Principal belongs. This may first authenticate the principal
    as some security manager impls require a preceeding isValid call.
     @param securityDomain - the name of the security to use
     @param principal - the user identity in the operation environment
     @param credential - the proof of user identity as known in the
    @param roles - Set<Principal> for the application domain roles that the
     principal is to be validated against.
    @return true if the principal has at least one of the roles in the roles set,
        false otherwise.
     */
    public boolean doesUserHaveRole(String securityDomain, Principal principal,
       Object credential, Set roles);

    /** Return the set of domain roles the principal has been assigned.
     This may first authenticate the principal as some security manager impls
     require a preceeding isValid call.
     @param securityDomain - the name of the security to use
     @param principal - the user identity in the operation environment
     @param credential - the proof of user identity as known in the
    @return The Set<Principal> for the application domain roles that the
     principal has been assigned.
     */
    public Set getUserRoles(String securityDomain, Principal principal,
       Object credential);
}
