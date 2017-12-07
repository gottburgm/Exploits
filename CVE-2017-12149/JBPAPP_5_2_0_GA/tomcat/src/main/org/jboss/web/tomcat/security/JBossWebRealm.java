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

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Policy;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.security.CertificatePrincipal;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityUtil;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.audit.AuditEvent;
import org.jboss.security.audit.AuditLevel;
import org.jboss.security.audit.AuditManager;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.security.auth.certs.SubjectDNMapping;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.security.authorization.ResourceKeys;
import org.jboss.security.javaee.AbstractWebAuthorizationHelper;
import org.jboss.security.javaee.SecurityHelperFactory;
import org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve;

//$Id: JBossWebRealm.java 103433 2010-04-01 14:40:07Z mmoyses $

/**
 *  Implementation of the Tomcat Realm Interface.
 *  The Realm implementation handles authentication and authorization 
 *  using the JBossSX security framework. It relies on the JNDI ENC namespace 
 *  setup by the AbstractWebContainer. In particular, it uses the java:comp/env/security
 *  subcontext to access the security manager interfaces for authentication. 
 *  @author Scott.Stark@jboss.org
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jul 10, 2006 
 *  @version $Revision: 103433 $
 */
public class JBossWebRealm extends RealmBase
{
   static Logger log = Logger.getLogger(JBossWebRealm.class);

   /**
    * The converter from X509 cert chain to Princpal
    */
   protected CertificatePrincipal certMapping = new SubjectDNMapping();

   /**
    * The JBossSecurityMgrRealm category trace flag
    */
   private boolean trace = log.isTraceEnabled();

   /** The JACC PolicyContext key for the current Subject */
   private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";

   protected String securityDomain = SecurityConstants.DEFAULT_WEB_APPLICATION_POLICY;

   /**
    * JBAS-2519:Delegate to JACC provider for unsecured resources in web.xml 
    */
   protected boolean unprotectedResourceDelegation = false;

   protected String securityConstraintProviderClass = "";

   /** Should Security Audit be done **/
   protected boolean enableAudit = false;

   /** Should RealmBase Authorization decision be considered or not?
    * false - consider, true - do not consider
    */
   protected boolean ignoreBaseDecision = false;
   
   /**
    * Should we rely on RealmBase Authorization Check Alone?
    */
   protected boolean ignoreJBossAuthorization = false;
   
   protected static boolean securityManagerFallback = false;
   
   static
   {
      String str = SecurityAssociationActions.getSystemProperty("jbosswebrealm.fallback", "false");
      securityManagerFallback = Boolean.parseBoolean(str);
   }

   /**
    * Set the class name of the CertificatePrincipal used for mapping X509 cert
    * chains to a Princpal.
    *
    * @param className the CertificatePrincipal implementation class that must
    *                  have a no-arg ctor.
    * @see org.jboss.security.CertificatePrincipal
    */
   public void setCertificatePrincipal(String className)
   {
      try
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class<?> cpClass = loader.loadClass(className);
         certMapping = (CertificatePrincipal) cpClass.newInstance();
      }
      catch (Exception e)
      {
         log.error("Failed to load CertificatePrincipal: " + className, e);
         certMapping = new SubjectDNMapping();
      }
   }

   public void setSecurityConstraintProviderClass(String securityConstraintProviderClass)
   {
      this.securityConstraintProviderClass = securityConstraintProviderClass;
   }

   /**
    * Override the security domain driving the authorization for the realm
    * @param securityDomain
    */
   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public void setUnprotectedResourceDelegation(boolean unprotectedResourceDelegation)
   {
      this.unprotectedResourceDelegation = unprotectedResourceDelegation;
   }

   public void setEnableAudit(boolean enableAudit)
   {
      this.enableAudit = enableAudit;
   }

   public void setIgnoreBaseDecision(boolean ignoreBaseDecision)
   {
      this.ignoreBaseDecision = ignoreBaseDecision;
      if (ignoreBaseDecision && ignoreJBossAuthorization)
    	 throw new RuntimeException("One of ignoreBaseDecision or ignoreJBossAuthorization should be false");
   }

   public void setIgnoreJBossAuthorization(boolean ignoreJBossAuthz )
   {
	  this.ignoreJBossAuthorization = ignoreJBossAuthz;
	  if (ignoreBaseDecision && ignoreJBossAuthorization)
		 throw new RuntimeException("One of ignoreBaseDecision or ignoreJBossAuthorization should be false");
   }
   
   //*************************************************************************
   //   Realm.Authenticate Methods
   //************************************************************************* 

   /**
    * Return the Principal associated with the specified chain of X509 client
    * certificates.  If there is none, return <code>null</code>.
    *
    * @param certs Array of client certificates, with the first one in the array
    *              being the certificate of the client itself.
    */
   public Principal authenticate(X509Certificate[] certs)
   {
      Principal principal = null; 

      try
      {
         // Get the JBoss security manager from the ENC context
         SubjectSecurityManager securityMgr = getSubjectSecurityManager("authenticate(X509Certificate[] certs)");
         if(securityMgr == null)
            return null;
         
         Subject subject = new Subject();
         principal = certMapping.toPrinicipal(certs);
         if (securityMgr.isValid(principal, certs, subject))
         {
            if (trace)
            {
               log.trace("User: " + principal + " is authenticated");
            }
            securityDomain = securityMgr.getSecurityDomain();
            SecurityAssociationActions.setPrincipalInfo(principal, certs, subject);

            // Get the CallerPrincipal mapping
            RealmMapping realmMapping = null;
            if(securityMgr instanceof RealmMapping)
               realmMapping = (RealmMapping)securityMgr;
            else
               realmMapping = getRealmMapping();
            
            if(realmMapping == null)
            {
               log.trace("RealmMapping is null for authenticate(x509 params)");
               return null;
            }
            Principal oldPrincipal = principal;
            principal = realmMapping.getPrincipal(oldPrincipal);
            if (trace)
            {
               log.trace("Mapped from input principal: " + oldPrincipal + "to: " + principal);
            }
            // Get the caching principal
            principal = getCachingPrincipal(realmMapping, oldPrincipal, principal, certs, subject);
            if (enableAudit)
               successAudit(oldPrincipal, principal);
         }
         else
         {
            if (trace)
            {
               log.trace("User: " + principal + " is NOT authenticated");
            }
            if (enableAudit)
               failureAudit(principal);
            principal = null;
         }
      }
      catch (Exception e)
      {
         log.error("Error during authenticate", e);
         if (enableAudit)
            errorAudit(principal, e);
      }
      return principal;
   }

   /**
    * Return the Principal associated with the specified username, which matches
    * the digest calculated using the given parameters using the method
    * described in RFC 2069; otherwise return <code>null</code>.
    *
    * @param username Username of the Principal to look up
    * @param digest   Digest which has been submitted by the client
    * @param nonce    Unique (or supposedly unique) token which has been used for
    *                 this request
    * @param nc       client nonce reuse count
    * @param cnonce   client token
    * @param qop      quality of protection
    * @param realm    Realm name
    * @param md5a2    Second MD5 digest used to calculate the digest : MD5(Method +
    *                 ":" + uri)
    */
   public Principal authenticate(String username, String digest, String nonce, String nc, String cnonce, String qop,
         String realm, String md5a2)
   {
      Principal principal = null; 

      Principal caller = (Principal) SecurityAssociationValve.userPrincipal.get();
      if (caller == null && username == null && digest == null)
      {
         return null;
      }

      try
      {
         DigestCallbackHandler handler = new DigestCallbackHandler(username, nonce, nc, cnonce, qop, realm, md5a2);
         CallbackHandlerPolicyContextHandler.setCallbackHandler(handler);

         // Get the JBoss security manager from the ENC context
         SubjectSecurityManager securityMgr = getSubjectSecurityManager("authenticate( digest related)");
         if(securityMgr == null)
            return null;
         
         principal = new SimplePrincipal(username);
         Subject subject = new Subject();
         if (securityMgr.isValid(principal, digest, subject))
         {
            log.trace("User: " + username + " is authenticated");
            securityDomain = securityMgr.getSecurityDomain();
            SecurityAssociationActions.setPrincipalInfo(principal, digest, subject);

            // Get the CallerPrincipal mapping
            RealmMapping realmMapping = null;
            if(securityMgr instanceof RealmMapping)
               realmMapping = (RealmMapping)securityMgr;
            else
               realmMapping = getRealmMapping();
            
            if(realmMapping == null)
            {
               log.trace("RealmMapping is null for authenticate(digest params)");
               return null;
            }
            Principal oldPrincipal = principal;
            principal = realmMapping.getPrincipal(oldPrincipal);
            if (trace)
            {
               log.trace("Mapped from input principal: " + oldPrincipal + "to: " + principal);
            }
            // Get the caching principal
            principal = getCachingPrincipal(realmMapping, oldPrincipal, principal, digest, subject);
            if (enableAudit)
               successAudit(oldPrincipal, principal);
         }
         else
         {
            if (enableAudit)
               failureAudit(principal);
            principal = null;
            if (trace)
            {
               log.trace("User: " + username + " is NOT authenticated");
            }
         }
      }
      catch (Exception e)
      {
         principal = null;
         log.error("Error during authenticate", e);
         if (enableAudit)
            errorAudit(principal, e);
      }
      finally
      {
         CallbackHandlerPolicyContextHandler.setCallbackHandler(null);
      }
      if (trace)
      {
         log.trace("End authenticate, principal=" + principal);
      }
      return principal;
   }

   /**
    * Return the Principal associated with the specified username and
    * credentials, if there is one; otherwise return <code>null</code>.
    *
    * @param username    Username of the Principal to look up
    * @param credentials Password or other credentials to use in authenticating
    *                    this username
    */
   public Principal authenticate(String username, String credentials)
   {
      if (trace)
      {
         log.trace("Begin authenticate, username=" + username);
      }
      Principal principal = null; 

      Principal caller = SecurityAssociationValve.userPrincipal.get();
      if (caller == null && username == null && credentials == null)
      {
         return null;
      }

      try
      {
         // Get the JBoss security manager from the ENC context
         SubjectSecurityManager securityMgr = getSubjectSecurityManager("authenticate(username,cred)");
         if(securityMgr == null)
            return null;
         
         principal = new SimplePrincipal(username);
         Subject subject = new Subject();
         if (securityMgr.isValid(principal, credentials, subject))
         {
            log.trace("User: " + username + " is authenticated");
            securityDomain = securityMgr.getSecurityDomain();
            SecurityAssociationActions.setPrincipalInfo(principal, credentials, subject);
            
            // Get the CallerPrincipal mapping
            RealmMapping realmMapping = null;
            if(securityMgr instanceof RealmMapping)
               realmMapping = (RealmMapping)securityMgr;
            else
               realmMapping = getRealmMapping();
            
            if(realmMapping == null)
            {
               log.trace("RealmMapping is null for authenticate(username,cred)");
               return null;
            }
            Principal oldPrincipal = principal;
            principal = realmMapping.getPrincipal(oldPrincipal);
            if (trace)
            {
               log.trace("Mapped from input principal: " + oldPrincipal + "to: " + principal);
            }
            // Get the caching principal
            principal = getCachingPrincipal(realmMapping, oldPrincipal, principal, credentials, subject);
            if (enableAudit)
               successAudit(oldPrincipal, principal);
         }
         else
         {
            if (enableAudit)
               failureAudit(principal);
            if (trace)
            {
               log.trace("User: " + username + " is NOT authenticated");
            }
            principal = null;
         }
      }
      catch (Exception e)
      {
         principal = null;
         log.error("Error during authenticate", e);
         if (enableAudit)
            errorAudit(principal, e);
      }
      if (trace)
      {
         log.trace("End authenticate, principal=" + principal);
      }
      return principal;
   }

   /**
    * Return the Principal associated with the specified username and
    * credentials, if there is one; otherwise return <code>null</code>.
    *
    * @param username    Username of the Principal to look up
    * @param credentials Password or other credentials to use in authenticating
    *                    this username
    */
   public Principal authenticate(String username, byte[] credentials)
   {
      return authenticate(username, new String(credentials));
   }

   //*************************************************************************
   //   Realm.hasXXX Methods
   //*************************************************************************
   /**
    * JBAS-2519:Delegate to JACC provider for unsecured resources in web.xml
    */
   public SecurityConstraint[] findSecurityConstraints(Request request, org.apache.catalina.Context context)
   {
      SecurityConstraint[] scarr = super.findSecurityConstraints(request, context);
      if ((scarr == null || scarr.length == 0) && this.unprotectedResourceDelegation)
      {
         scarr = getSecurityConstraintsFromProvider(request, context);
      }
      return scarr;
   }

   /**
    * @see RealmBase#hasResourcePermission(Request, Response, SecurityConstraint[], 
    * org.apache.catalina.Context)
    */
   public boolean hasResourcePermission(Request request, Response response, SecurityConstraint[] securityConstraints,
         org.apache.catalina.Context context) throws IOException
   {
      if (ignoreBaseDecision && ignoreJBossAuthorization)
    	  throw new RuntimeException("One of ignoreBaseDecision or ignoreJBossAuthorization should be false");
	  
      boolean ok = ignoreJBossAuthorization ? true : false;
      boolean baseDecision = ignoreBaseDecision ? true : super.hasResourcePermission(request, response,
            securityConstraints, context);

      //By default, the authorization framework always returns PERMIT such that the
      //decision of the realm base holds.
      if (baseDecision && !ignoreJBossAuthorization)
      {
         Subject caller = this.establishSubjectContext(request.getPrincipal());

         PolicyRegistration policyRegistration = getPolicyRegistration();

         SecurityContext sc = SecurityAssociationActions.getSecurityContext();
         Map<String, Object> contextMap = new HashMap<String, Object>();
         contextMap.put(ResourceKeys.RESOURCE_PERM_CHECK, Boolean.TRUE);
         contextMap.put(ResourceKeys.POLICY_REGISTRATION, policyRegistration);
         contextMap.put("securityConstraints", securityConstraints);

         AbstractWebAuthorizationHelper helper = null;
         try
         {
            helper = SecurityHelperFactory.getWebAuthorizationHelper(sc);
         }
         catch (Exception e)
         {
            log.error("Exception in obtaining helper", e);
            return false;
         }

         helper.setPolicyRegistration(policyRegistration);
         helper.setEnableAudit(this.enableAudit);

         //WebAuthorizationHelper helper = new WebAuthorizationHelper(sc, this.enableAudit);
         ok = helper.checkResourcePermission(contextMap, request, response, caller, PolicyContext.getContextID(),
               requestURI(request));
      }
      boolean finalDecision = baseDecision && ok;
      if (trace)
         log.trace("hasResourcePerm:RealmBase says:" + baseDecision + "::Authz framework says:" + ok + ":final=" + finalDecision);
      if (!finalDecision)
      {
         response.sendError(HttpServletResponse.SC_FORBIDDEN, sm.getString("realmBase.forbidden"));
      }
      return finalDecision;
   }

   /**
    * Returns <code>true</code> if the specified user <code>Principal</code> has
    * the specified security role, within the context of this
    * <code>Realm</code>; otherwise return <code>false</code>. This will be true
    * when an associated role <code>Principal</code> can be found whose
    * <code>getName</code> method returns a <code>String</code> equalling the
    * specified role.
    *
    * @param principal <code>Principal</code> for whom the role is to be
    *                  checked
    * @param role      Security role to be checked
    */
   public boolean hasRole(Principal principal, String role)
   {
      if (ignoreBaseDecision && ignoreJBossAuthorization)
    	  throw new RuntimeException("One of ignoreBaseDecision or ignoreJBossAuthorization should be false");
	  
      String servletName = null;
      //WebProgrammaticAuthentication does not go through hasResourcePermission
      //and hence the activeRequest thread local may not be set
      Request req = ActiveRequestResponseCacheValve.activeRequest.get();
      Wrapper servlet = req.getWrapper();
      if (servlet != null)
      {
         servletName = getServletName(servlet);
      }

      if (servletName == null)
         throw new IllegalStateException("servletName is null");
      JBossWebMetaData metaData = SecurityAssociationValve.activeWebMetaData.get();
      String roleName = role;

      /**
       * If the metaData is null, this is an internal call made by RealmBase.hasResourcePermission
       */
      if (metaData != null)
      {
         ServletMetaData servletMD = metaData.getServlets().get(servletName);
         SecurityRoleRefsMetaData roleRefs = null;
         if (servletMD != null)
            roleRefs = servletMD.getSecurityRoleRefs();
         if (roleRefs != null)
         {
            for (SecurityRoleRefMetaData ref : roleRefs)
            {
               if (ref.getRoleLink().equals(role))
               {
                  roleName = ref.getName();
                  break;
               }
            }
         }
      }

      boolean authzDecision = ignoreJBossAuthorization ? true : false;
      boolean baseDecision = ignoreBaseDecision ? true : super.hasRole(principal, role);

      if (baseDecision && !ignoreJBossAuthorization)
      {
         SecurityContext sc = SecurityAssociationActions.getSecurityContext();

         AbstractWebAuthorizationHelper helper = null;
         try
         {
            helper = SecurityHelperFactory.getWebAuthorizationHelper(sc);
         }
         catch (Exception e)
         {
            log.error("Error obtaining helper", e);
         }
         helper.setPolicyRegistration(getPolicyRegistration());
         helper.setEnableAudit(enableAudit);
         Subject callerSubject = SecurityAssociationActions.getSubject();
         if (callerSubject == null)
         {
            //During hasResourcePermission check, catalina calls hasRole. But we have not established
            // a subject yet in the security context. So we will get the subject from the cached principal
            callerSubject = SecurityAssociationActions.getSubjectFromRequestPrincipal(principal);
         }

         String contextID = PolicyContext.getContextID();
         
         authzDecision = SecurityAssociationActions.hasRole(helper, roleName, principal, servletName, 
                            getPrincipalRoles(principal), contextID, callerSubject);
      }
      boolean finalDecision = baseDecision && authzDecision;
      if (trace)
         log.trace("hasRole:RealmBase says:" + baseDecision + "::Authz framework says:" + authzDecision + ":final="
               + finalDecision);

      return finalDecision;
   }

   /**
    * @see RealmBase#hasUserDataPermission(Request, Response, SecurityConstraint[])
    */
   public boolean hasUserDataPermission(Request request, Response response, SecurityConstraint[] constraints)
         throws IOException
   {
      if (ignoreBaseDecision && ignoreJBossAuthorization)
    	  throw new RuntimeException("One of ignoreBaseDecision or ignoreJBossAuthorization should be false"); 
	   
      boolean ok = ignoreBaseDecision ? true : super.hasUserDataPermission(request, response, constraints);
      //If the realmbase check has passed, then we can go to authz framework
      if (ok && !ignoreJBossAuthorization)
      {
         Principal requestPrincipal = request.getPrincipal();
         establishSubjectContext(requestPrincipal);
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("securityConstraints", constraints);
         map.put(ResourceKeys.USERDATA_PERM_CHECK, Boolean.TRUE);

         SecurityContext sc = SecurityAssociationActions.getSecurityContext();
         AbstractWebAuthorizationHelper helper = null;
         try
         {
            helper = SecurityHelperFactory.getWebAuthorizationHelper(sc);
         }
         catch (Exception e)
         {
            log.error("Error obtaining helper", e);
         }
         helper.setPolicyRegistration(getPolicyRegistration());
         helper.setEnableAudit(enableAudit);
 
         Subject callerSubject = SecurityAssociationActions.getSubject();
         //JBAS-6419:CallerSubject has no bearing on the user data permission check
         if(callerSubject == null)
            callerSubject = new Subject();

         ok = helper.hasUserDataPermission(map, request, response, PolicyContext.getContextID(),
               callerSubject);
      }

      return ok;
   }

   //*****************************************************************************
   // PROTECTED METHODS
   //*****************************************************************************  
   /**
    * Create the session principal tomcat will cache to avoid callouts to this
    * Realm.
    *
    * @param realmMapping    - the role mapping security manager
    * @param authPrincipal   - the principal used for authentication and stored in
    *                        the security manager cache
    * @param callerPrincipal - the possibly different caller principal
    *                        representation of the authenticated principal
    * @param credential      - the credential used for authentication
    * @return the tomcat session principal wrapper
    */
   protected Principal getCachingPrincipal(RealmMapping realmMapping, Principal authPrincipal,
         Principal callerPrincipal, Object credential, Subject subject)
   {
      // Cache the user roles in the principal
      Set<Principal> userRoles = realmMapping.getUserRoles(authPrincipal);
      ArrayList<String> roles = new ArrayList<String>();
      if (userRoles != null)
      {
         Iterator<Principal> iterator = userRoles.iterator();
         while (iterator.hasNext())
         {
            Principal role = (Principal) iterator.next();
            roles.add(role.getName());
         }
      }
      JBossGenericPrincipal gp = new JBossGenericPrincipal(this, subject, authPrincipal, callerPrincipal, credential,
            roles, userRoles);
      return gp;
   }

   /**
    * Return a short name for this Realm implementation, for use in log
    * messages.
    */
   protected String getName()
   {
      return getClass().getName();
   }

   /**
    * Return the password associated with the given principal's user name.
    */
   protected String getPassword(String username)
   {
      String password = null;
      return password;
   }

   /**
    * Return the Principal associated with the given user name.
    */
   protected Principal getPrincipal(String username)
   {
      return new SimplePrincipal(username);
   }

   /**
    * Get the canonical request uri from the request mapping data requestPath
    * @param request
    * @return the request URI path
    */
   static String requestURI(Request request)
   {
      String uri = request.getMappingData().requestPath.getString();
      if (uri == null || uri.equals("/"))
      {
         uri = "";
      }
      return uri;
   }

   /**
    * Access the set of role Princpals associated with the given caller princpal.
    *
    * @param principal - the Principal mapped from the authentication principal
    *                  and visible from the HttpServletRequest.getUserPrincipal
    * @return a possible null Set<Principal> for the caller roles
    */
   protected Set<Principal> getPrincipalRoles(Principal principal)
   {
      if ((principal instanceof GenericPrincipal) == false)
         throw new IllegalStateException("Expected GenericPrincipal, but saw: " + principal.getClass());
      GenericPrincipal gp = (GenericPrincipal) principal;
      String[] roleNames = gp.getRoles();
      Set<Principal> userRoles = new HashSet<Principal>();
      if (roleNames != null)
      {
         for (int n = 0; n < roleNames.length; n++)
         {
            SimplePrincipal sp = new SimplePrincipal(roleNames[n]);
            userRoles.add(sp);
         }
      }
      return userRoles;
   }

   //*****************************************************************************
   // PRIVATE METHODS
   //*****************************************************************************

   /**
    * Ensure that the JACC PolicyContext Subject handler has access to the
    * authenticated Subject. The caching of the authentication state by tomcat
    * means that we need to retrieve the Subject from the JBossGenericPrincipal
    * if the realm was not invoked to authenticate the caller.
    * 
    * @param principal - the http request getPrincipal
    * @return the authenticated Subject is there is one, null otherwise
    */
   private Subject establishSubjectContext(Principal principal)
   {
      Subject caller = null;
      try
      {
         caller = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
      }
      catch (PolicyContextException e)
      {
         if (trace)
            log.trace("Failed to get subject from PolicyContext", e);
      }

      if (caller == null)
      {
         // Test the request principal that may come from the session cache 
         if (principal instanceof JBossGenericPrincipal)
         {
            JBossGenericPrincipal jgp = (JBossGenericPrincipal) principal;
            caller = jgp.getSubject();
            // 
            if (trace)
               log.trace("Restoring principal info from cache");
            SecurityAssociationActions.setPrincipalInfo(jgp.getAuthPrincipal(), jgp.getCredentials(), jgp.getSubject());
         }
      }
      return caller;
   }

   private Context getSecurityNamingContext()
   { 
      Context securityCtx = null;
      InitialContext iniCtx = null;
      // Get the JBoss security manager from the ENC context
      try
      {
         iniCtx = new InitialContext();
         securityCtx = (Context) iniCtx.lookup("java:comp/env/security");
      }
      catch (NamingException e)
      {
         // Apparently there is no security context?
      }   
      return securityCtx;
   }

   /**
    * Get the JBossWebMetaData
    * @return
    */
   private JBossWebMetaData getMetaData()
   {
      return SecurityAssociationValve.activeWebMetaData.get();
   }
   
   /**
    * Get the security domain
    * from the meta data
    * @return
    */
   private String getSecurityDomain()
   {
      String securityDomain = null;
      JBossWebMetaData jbossMetaData = getMetaData();
      if(jbossMetaData != null)
         securityDomain = jbossMetaData.getSecurityDomain(); 
      if(securityDomain != null)
      {
         securityDomain = SecurityUtil.unprefixSecurityDomain(securityDomain);
      }
      return securityDomain;
   }
   
   /**
    * Get the JBoss SubjectSecurityManager (AuthenticationManager)
    * @param wherefrom the method from where this is called for trace log
    * @return
    */
   private SubjectSecurityManager getSubjectSecurityManager(String wherefrom)
   {
      SubjectSecurityManager subjectSecurityManager = null;
      Context securityCtx = getSecurityNamingContext();
      if (securityCtx == null)
      {
         if (trace)
         {
            log.trace("No security naming context for " + wherefrom);
         } 
      }
      try
      {
         if(securityCtx != null)
           subjectSecurityManager = (SubjectSecurityManager) securityCtx.lookup("securityMgr");
      }
      catch (NamingException e)
      {
      }
      if(subjectSecurityManager == null && securityManagerFallback)
      {
         String str = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + getSecurityDomain();
         try
         {
            InitialContext ic = new InitialContext();
            subjectSecurityManager = (SubjectSecurityManager)ic.lookup(str);
         }
         catch (NamingException e)
         {    
         }
      }
      return subjectSecurityManager;
   }
   
   /**
    * Get the Realm Mapping from the Security
    * Naming Context
    * @return
    */
   private RealmMapping getRealmMapping()
   {
      RealmMapping realmMapping = null;
      Context securityCtx = getSecurityNamingContext();
      if (securityCtx == null)
      {
         if (trace)
         {
            log.trace("No security naming context");
         }
         return null;
      }
      try
      {
         realmMapping = (RealmMapping) securityCtx.lookup("realmMapping");        
      }
      catch (NamingException e)
      {
      }
      return realmMapping;
   }
   
   /**
    * Get a set of SecurityConstraints from either the PolicyProvider
    * or the securityConstraintProviderClass class, via reflection
    * 
    * @param request
    * @param context 
    * @return an array of SecurityConstraints
    */
   private SecurityConstraint[] getSecurityConstraintsFromProvider(Request request, org.apache.catalina.Context context)
   {
      SecurityConstraint[] scarr = null;
      Class<?>[] sig =
      {Request.class, Context.class};
      Object[] args =
      {request, context};

      Method findsc = null;

      //Try the Policy Provider 
      try
      {
         Policy policy = Policy.getPolicy();
         findsc = policy.getClass().getMethod("findSecurityConstraints", sig);
         scarr = (SecurityConstraint[]) findsc.invoke(policy, args);
      }
      catch (Throwable t)
      {
         if (trace)
            log.error("Error obtaining security constraints from policy", t);
      }
      //If the policy provider did not provide the security constraints
      //check if a seperate SC provider is plugged in
      if (scarr == null || scarr.length == 0)
      {
         if (securityConstraintProviderClass == "" || securityConstraintProviderClass.length() == 0)
         {
            if (trace)
               log.trace("unprotectedResourceDelegation is true " + "but securityConstraintProviderClass is empty");
         }
         else
            //Try to call the method on the provider class
            try
            {
               Class<?> clazz = SecurityAssociationActions.loadClass(securityConstraintProviderClass);
               Object obj = clazz.newInstance();
               findsc = clazz.getMethod("findSecurityConstraints", sig);
               if (trace)
                  log.trace("findSecurityConstraints method found in securityConstraintProviderClass");
               scarr = (SecurityConstraint[]) findsc.invoke(obj, args);
            }
            catch (Throwable t)
            {
               log.error("Error instantiating " + securityConstraintProviderClass, t);
            }
      }
      return scarr;
   }

   /**
    * Jacc Specification : Appendix
    *  B.19 Calling isUserInRole from JSP not mapped to a Servlet
    *  Checking a WebRoleRefPermission requires the name of a Servlet to
    *  identify the scope of the reference to role translation. The name of a 
    *  scoping  servlet has not been established for an unmapped JSP.
    *  
    *  Resolution- For every security role in the web application add a
    *  WebRoleRefPermission to the corresponding role. The name of all such
    *  permissions shall be the empty string, and the actions of each
    *  permission shall be the corresponding role name. 
    *  When checking a WebRoleRefPermission from a JSP not mapped to a servlet, 
    *  use a permission with the empty string as its name and with the argument to is
    *  UserInRole as its actions.  
    * 
    * @param servlet Wrapper
    * @return empty string if it is for an unmapped jsp or name of the servlet for others 
    */
   private String getServletName(Wrapper servlet)
   {
      //For jsp, the mapping will be (*.jsp, *.jspx)
      String[] mappings = servlet.findMappings();
      if (trace)
         log.trace("[getServletName:servletmappings=" + mappings + ":servlet.getName()=" + servlet.getName() + "]");
      if ("jsp".equals(servlet.getName()) && (mappings != null && mappings[0].indexOf("*.jsp") > -1))
         return "";
      else
         return servlet.getName();
   }

   private void audit(String level, Map<String, Object> contextMap, Exception e)
   {
      String requestInfo = "";
      try
      {
         HttpServletRequest hsr = (HttpServletRequest) PolicyContext.getContext(SecurityConstants.WEB_REQUEST_KEY);
         requestInfo = WebUtil.deriveUsefulInfo(hsr);
         contextMap.put("request", requestInfo);
      }
      catch (PolicyContextException pe)
      {
         if (trace)
            log.trace("Error obtaining the servlet request:", pe);
      }
      contextMap.put("Source", getClass().getName());
      AuditEvent ae = new AuditEvent(level);
      ae.setContextMap(contextMap);
      ae.setUnderlyingException(e);

      SecurityContext sc = SecurityAssociationActions.getSecurityContext();
      if (sc != null)
      {
         AuditManager auditManager = sc.getAuditManager();
         if (auditManager != null)
            auditManager.audit(ae);
         else
            log.trace("Audit Manager obtained from Security Context is null");
      }
   }

   private void successAudit(Principal callerPrincipal, Principal principal)
   {
      Map<String, Object> cmap = new HashMap<String, Object>();
      cmap.put("principal", principal);
      cmap.put("CallerPrincipal", callerPrincipal);
      audit(AuditLevel.SUCCESS, cmap, null);
   }

   private void failureAudit(Principal principal)
   {
      Map<String, Object> cmap = new HashMap<String, Object>();
      cmap.put("principal", principal);
      audit(AuditLevel.FAILURE, cmap, null);
   }

   private void errorAudit(Principal principal, Exception e)
   {
      Map<String, Object> cmap = new HashMap<String, Object>();
      cmap.put("principal", principal);
      audit(AuditLevel.ERROR, cmap, e);
   }

   private PolicyRegistration getPolicyRegistration()
   {
      PolicyRegistration policyRegistration = null;
      try
      {
         policyRegistration = (PolicyRegistration) (new InitialContext()).lookup("java:/policyRegistration");
      }
      catch (Exception e)
      {
         log.trace("Error obtaining PolicyRegistration", e);
      }
      return policyRegistration;
   }
}
