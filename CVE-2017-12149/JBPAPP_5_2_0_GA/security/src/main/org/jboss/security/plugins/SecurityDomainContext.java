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

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.audit.AuditManager;
import org.jboss.security.identitytrust.IdentityTrustManager;
import org.jboss.security.mapping.MappingManager;
import org.jboss.util.CachePolicy;

/** An encapsulation of the JNDI security context information
 *
 * @author  Scott.Stark@jboss.org
 * @author  Anil.Saldhana@jboss.org
 * @version 
 */
public class SecurityDomainContext
{
   static final String ACTIVE_SUBJECT = "subject";
   static final String AUTHENTICATION_MGR = "authenticationMgr";
   static final String SECURITY_MGR = "securityMgr";
   static final String REALM_MAPPING = "realmMapping";
   static final String AUTHORIZATION_MGR = "authorizationMgr";
   static final String AUDIT_MGR = "auditMgr";
   static final String MAPPING_MGR = "mappingMgr";
   static final String IDENTITY_TRUST_MGR = "identityTrustMgr";
   static final String AUTH_CACHE = "authenticationCache";
   static final String DOMAIN_CONTEXT = "domainContext";

   AuthenticationManager securityMgr;
   AuthorizationManager authorizationMgr;
   CachePolicy authenticationCache;
   AuditManager auditMgr;
   MappingManager mappingMgr;
   IdentityTrustManager identityTrustMgr;

   /** Creates new SecurityDomainContextHandler */
   public SecurityDomainContext(AuthenticationManager securityMgr, 
         CachePolicy authenticationCache)
   {
      this.securityMgr = securityMgr;
      this.authenticationCache = authenticationCache; 
   }

   public Object lookup(String name) throws NamingException
   {
      Object binding = null;
      if( name == null || name.length() == 0 )
         throw new InvalidNameException("name cannot be null or empty");

      if( name.equals(ACTIVE_SUBJECT) )
         binding = getSubject();
      else if( name.equals(AUTHENTICATION_MGR) || name.equals(SECURITY_MGR))
         binding = securityMgr;
      else if( name.equals(REALM_MAPPING) )
         binding = getRealmMapping();
      else if( name.equals(AUTHORIZATION_MGR) )
         binding = getAuthorizationManager();
      else if( name.equals(AUDIT_MGR) )
         binding = this.getAuditMgr();
      else if( name.equals(MAPPING_MGR) )
         binding = this.getMappingMgr();
      else if( name.equals(IDENTITY_TRUST_MGR) )
         binding = this.getIdentityTrustMgr();
      else if( name.equals(AUTH_CACHE) )
         binding = authenticationCache;
      else if( name.equals(DOMAIN_CONTEXT) )
         binding = this;
         
      return binding;
   }
   public Subject getSubject()
   {
      Subject subject = null;
      if( securityMgr instanceof SubjectSecurityManager )
      {
         subject = ((SubjectSecurityManager)securityMgr).getActiveSubject();
      }
      return subject;
   }
   public AuthenticationManager getSecurityManager()
   {
      return securityMgr;
   }
   public RealmMapping getRealmMapping()
   {
      RealmMapping realmMapping = null;
      if(authorizationMgr != null && authorizationMgr instanceof RealmMapping)
      {
         realmMapping = (RealmMapping)authorizationMgr;
      }
      else
      if( securityMgr instanceof RealmMapping )
      {
         realmMapping = (RealmMapping)securityMgr;
      }
      return realmMapping;
   }
   
   public void setAuthenticationManager(AuthenticationManager aum)
   {
      this.securityMgr = aum;
   }
   
   public void setAuthorizationManager(AuthorizationManager am)
   {
      this.authorizationMgr = am;
   }
   
   public AuthorizationManager getAuthorizationManager()
   {
      return authorizationMgr;
   }
   
   public void setAuthenticationCache(CachePolicy cp)
   {
      this.authenticationCache = cp;
   }
    
   public CachePolicy getAuthenticationCache()
   {
      return authenticationCache;
   }

   public AuditManager getAuditMgr()
   {
      return auditMgr;
   }

   public void setAuditMgr(AuditManager auditMgr)
   {
      this.auditMgr = auditMgr;
   }

   public MappingManager getMappingMgr()
   {
      return mappingMgr;
   }

   public void setMappingMgr(MappingManager mappingMgr)
   {
      this.mappingMgr = mappingMgr;
   }

   public IdentityTrustManager getIdentityTrustMgr()
   {
      return identityTrustMgr;
   }

   public void setIdentityTrustMgr(IdentityTrustManager identityTrustMgr)
   {
      this.identityTrustMgr = identityTrustMgr;
   } 
}
