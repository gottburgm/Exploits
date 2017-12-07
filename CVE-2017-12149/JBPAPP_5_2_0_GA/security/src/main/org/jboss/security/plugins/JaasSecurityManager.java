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

import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler; 

import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.auth.callback.JBossCallbackHandler; 
import org.jboss.security.plugins.auth.JaasSecurityManagerBase;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.CachePolicy;

/** The JaasSecurityManager is responsible both for authenticating credentials
 associated with principals and for role mapping. This implementation relies
 on the JAAS LoginContext/LoginModules associated with the security
 domain name associated with the class for authentication,
 and the context JAAS Subject object for role mapping.
 
 @see #isValid(Principal, Object, Subject)
 @see #getPrincipal(Principal)
 @see #doesUserHaveRole(Principal, Set)
 
 @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 @author Scott.Stark@jboss.org
 @author Anil.Saldhana@jboss.org
 @version $Revision: 85945 $
*/
public class JaasSecurityManager extends ServiceMBeanSupport
   implements SubjectSecurityManager, RealmMapping
{ 
   private JaasSecurityManagerBase delegate = null;
      
   /** Creates a default JaasSecurityManager for with a securityDomain
    name of 'other'.
    */
   public JaasSecurityManager()
   {
      this("other", new JBossCallbackHandler());
   }
   /** Creates a JaasSecurityManager for with a securityDomain
    name of that given by the 'securityDomain' argument.
    @param securityDomain the name of the security domain
    @param handler the JAAS callback handler instance to use
    @exception UndeclaredThrowableException thrown if handler does not
      implement a setSecurityInfo(Princpal, Object) method
    */
   public JaasSecurityManager(String securityDomain, CallbackHandler handler)
   {
      delegate = new JaasSecurityManagerBase(securityDomain,handler); 
   }

   /** The domainCache is typically a shared object that is populated
    by the login code(LoginModule, etc.) and read by this class in the
    isValid() method.
    @see #isValid(Principal, Object, Subject)
    */
   public void setCachePolicy(CachePolicy domainCache)
   {
      delegate.setCachePolicy(domainCache); 
   }

   /**
    * Flag to specify if deep copy of subject sets needs to be 
    * enabled
    * 
    * @param flag
    */
   public void setDeepCopySubjectOption(Boolean flag)
   {
      delegate.setDeepCopySubjectOption(flag); 
   } 
   
   /** Not really used anymore as the security manager service manages the
    security domain authentication caches.
    */
   public void flushCache()
   {
      delegate.flushCache(); 
   }

   /** Get the name of the security domain associated with this security mgr.
    @return Name of the security manager security domain.
    */
   public String getSecurityDomain()
   {
      return delegate.getSecurityDomain();
   }

   /** Get the currently authenticated Subject. This is a thread local
    property shared across all JaasSecurityManager instances.
    @return The Subject authenticated in the current thread if one
    exists, null otherwise.
    */
   public Subject getActiveSubject()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Subject>()
      {
         public Subject run()
         {
            return delegate.getActiveSubject();
         }
      }); 
   }

   /** Validate that the given credential is correct for principal. This
    returns the value from invoking isValid(principal, credential, null).
    @param principal - the security domain principal attempting access
    @param credential - the proof of identity offered by the principal
    @return true if the principal was authenticated, false otherwise.
    */
   public boolean isValid(Principal principal, Object credential)
   {
      return delegate.isValid(principal, credential, null);
   }

   /** Validate that the given credential is correct for principal. This first
    will check the current CachePolicy object if one exists to see if the
    user's cached credentials match the given credential. If there is no
    credential cache or the cache information is invalid or does not match,
    the user is authenticated against the JAAS login modules configured for
    the security domain.
    @param principal - the security domain principal attempting access
    @param credential  the proof of identity offered by the principal
    @param activeSubject - if not null, a Subject that will be populated with
      the state of the authenticated Subject.
    @return true if the principal was authenticated, false otherwise.
    */
   public boolean isValid(Principal principal, Object credential,
      Subject activeSubject)
   {
      return delegate.isValid(principal, credential, activeSubject); 
   } 
   
   /** Map the argument principal from the deployment environment principal
    to the developer environment. This is called by the EJB context
    getCallerPrincipal() to return the Principal as described by
    the EJB developer domain.
    @return a Principal object that is valid in the deployment environment
    if one exists. If no Subject exists or the Subject has no principals
    then the argument principal is returned.
    */
   public Principal getPrincipal(Principal principal)
   {
      return delegate.getPrincipal(principal); 
   }

   /** Does the current Subject have a role(a Principal) that equates to one
    of the role names. This method obtains the Group named 'Roles' from
    the principal set of the currently authenticated Subject as determined
    by the SecurityAssociation.getSubject() method and then creates a
    SimplePrincipal for each name in roleNames. If the role is a member of the
    Roles group, then the user has the role. This requires that the caller
    establish the correct SecurityAssociation subject prior to calling this
    method. In the past this was done as a side-effect of an isValid() call,
    but this is no longer the case.

    @param principal - ignored. The current authenticated Subject determines
    the active user and assigned user roles.
    @param rolePrincipals - a Set of Principals for the roles to check.
    
    @see java.security.acl.Group;
    @see Subject#getPrincipals()
    */
   public boolean doesUserHaveRole(Principal principal, Set<Principal> rolePrincipals)
   {
      return delegate.doesUserHaveRole(principal, rolePrincipals); 
   } 

   /** Return the set of domain roles the current active Subject 'Roles' group
      found in the subject Principals set.

    @param principal - ignored. The current authenticated Subject determines
    the active user and assigned user roles.
    @return The Set<Principal> for the application domain roles that the
    principal has been assigned.
   */
   public Set<Principal> getUserRoles(Principal principal)
   {
      return delegate.getUserRoles(principal); 
   } 
   
   /**
    * @see AuthenticationManager#getTargetPrincipal(Principal,Map)
    */
   public Principal getTargetPrincipal(Principal anotherDomainPrincipal, Map<String,Object> contextMap)
   {
      return delegate.getTargetPrincipal(anotherDomainPrincipal, contextMap); 
   } 
}