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
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.AuthStatus;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.RealmBase;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.AuthorizationManager;
import org.jboss.security.GeneralizedAuthenticationManager;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.message.GenericMessageInfo;
import org.jboss.security.authorization.AuthorizationContext;  
import org.jboss.security.authorization.resources.WebResource;

//$Id: JBossExtendedSecurityMgrRealm.java 81037 2008-11-14 13:40:33Z dimitris@jboss.org $

/**
 *  Tomcat security realm that has the request/response messages
 *  available during authentication decisions
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 24, 2006 
 *  @version $Revision: 81037 $
 */
public class JBossExtendedSecurityMgrRealm extends JBossSecurityMgrRealm 
implements ExtendedRealm
{  
   private static Logger logger = Logger.getLogger(JBossExtendedSecurityMgrRealm.class);
   protected ObjectName authenticationManagerService = null;
   protected ObjectName authorizationManagerService = null;
   
   public JBossExtendedSecurityMgrRealm()
   {
      try
      {
         this.authenticationManagerService = new ObjectName("jboss.security:service=JASPISecurityManager");
         this.authorizationManagerService = new ObjectName("jboss.security:service=AuthorizationManager");
      }
      catch(JMException jme)
      {
         log.error("Error in instantiating object names:",jme);
      }
   }
   /**
    * Allow injection of the object name for the authentication
    * manager jmx service
    * 
    * @param oname Object Name
    */
   public void setAuthenticationManagerService(String oname)
   {
      ObjectName temp = null;
      try
      {
         temp = new ObjectName(oname);
      }
      catch(JMException jme)
      {
         log.error("Error in setAuthenticationManagerService:",jme); 
      }
      if(temp != null)
         this.authenticationManagerService = temp;
   }
   
   /**
    * Allow injection of the object name for the authorization
    * manager jmx service
    * 
    * @param oname Object Name
    */
   public void setAuthorizationManagerService(String oname)
   {
      ObjectName temp = null;
      try
      {
         temp = new ObjectName(oname);
      }
      catch(JMException jme)
      {
         log.error("Error in setAuthorizationManagerService:",jme); 
      }
      if(temp != null)
         this.authorizationManagerService = temp;
   }
   
   /**
    * @see ExtendedRealm#authenticate(Request, Response, LoginConfig)
    */
   public Principal authenticate(Request request, Response response, 
         LoginConfig config) throws Exception
   {
      log.debug("ExtendedSecurityMgrRealm:authenticate");
      MessageInfo authParam = new GenericMessageInfo(request,response);
      GeneralizedAuthenticationManager gam = getAuthenticationManager();
      Subject clientSubject = new Subject();
      Subject serviceSubject = new Subject();
      Map sharedState = getSharedState(request,config);
      AuthStatus status = AuthStatus.FAILURE;
      while(!status.equals(AuthStatus.SEND_CONTINUE))
      {
         status = gam.validateRequest(authParam, clientSubject, serviceSubject);
         if(status.equals(AuthStatus.FAILURE))
            throw new SecurityException("Authentication failed");
      } 
      Principal authenticatedPrincipal = this.getAuthenticatedPrincipal(clientSubject);
      return null;
      /*
      AuthorizationManager authzManager = getAuthorizationManager();
      Principal callerPrincipal = getAuthenticationManager().getPrincipal(authenticatedPrincipal);
      return getCachingPrincipal(authzManager, authenticatedPrincipal, callerPrincipal, null, clientSubject); 
      */
   } 
   
   /**
    * @see RealmBase#hasResourcePermission(org.apache.catalina.connector.Request, 
    * org.apache.catalina.connector.Response, org.apache.catalina.deploy.SecurityConstraint[], org.apache.catalina.Context)
    */
   public boolean hasResourcePermission(Request request, Response response, 
         SecurityConstraint[] constraints, Context context) throws IOException
   {
      boolean isAuthorized = super.hasResourcePermission(request, response, 
                                   constraints, context);
      log.debug("Super class has authorized="+isAuthorized);
      AuthorizationManager authzManager = null;
      try
      {
         authzManager = this.getAuthorizationManager(); 
      }
      catch(Exception e)
      {
         log.error("Error obtaining Authorization Manager:",e);
      }
      
      final HashMap map =  new HashMap();
      map.put("catalina.request",request);
      map.put("catalina.constraints",constraints);
      map.put("catalina.context", context);
      map.put("authorizationManager",authzManager);
      WebResource resource = new WebResource(map); 
      try
      {
         int check = authzManager.authorize(resource);
         isAuthorized = (check == AuthorizationContext.PERMIT);
      } 
      catch (Exception e)
      {
         isAuthorized = false;
         log.error("Error in authorization:",e);
      }
      log.debug("Final Authorization Result="+isAuthorized);
      if(!isAuthorized)
      {
        ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);
      } 
      return isAuthorized;
   }
   
   private Map getSharedState(Request request, LoginConfig config)
   {
      Map map = new HashMap();
      if(config.getAuthMethod().equals(Constants.FORM_METHOD))
      {
         map.put("javax.security.auth.login.name",
               getPrincipal(request.getParameter(Constants.FORM_USERNAME)));
         map.put("javax.security.auth.login.password",
               request.getParameter(Constants.FORM_PASSWORD));
      }
      return map;
   }
   
   /**
    * Create the session principal tomcat will cache to avoid callouts to this
    * Realm.
    *
    * @param authzManager    - the AuthorizationManager
    * @param authPrincipal   - the principal used for authentication and stored in
    *                        the security manager cache
    * @param callerPrincipal - the possibly different caller principal
    *                        representation of the authenticated principal
    * @param credential      - the credential used for authentication
    * @return the tomcat session principal wrapper
    */
   protected Principal getCachingPrincipal(AuthorizationManager authzManager,
      Principal authPrincipal, Principal callerPrincipal, Object credential,
      Subject subject)
   {
      // Cache the user roles in the principal
      Set userRoles = authzManager.getUserRoles(authPrincipal);
      ArrayList roles = new ArrayList();
      if (userRoles != null)
      {
         Iterator iterator = userRoles.iterator();
         while (iterator.hasNext())
         {
            Principal role = (Principal) iterator.next();
            roles.add(role.getName());
         }
      }
      JBossGenericPrincipal gp = new JBossGenericPrincipal(this, subject,
         authPrincipal, callerPrincipal, credential, roles, userRoles);
      return gp;
   }
   
   private Principal getAuthenticatedPrincipal(Subject subject)
   {
      if(subject == null)
         throw new IllegalArgumentException("subject is null");
      Principal authPrincipal = null;
      Iterator iter = subject.getPrincipals(SimplePrincipal.class).iterator();
      while(iter.hasNext())
      {
         authPrincipal = (Principal)iter.next();
         if(authPrincipal instanceof Group == false)
            break;
      }
      return authPrincipal;
   }
   
   private GeneralizedAuthenticationManager getAuthenticationManager()
   throws Exception
   {
      String contextID = PolicyContext.getContextID();
      MBeanServer server = MBeanServerLocator.locateJBoss(); 
      String securityDomain = (String)server.invoke(this.authenticationManagerService,
            "getSecurityDomain", 
            new String[]{contextID}, new String[]{"java.lang.String"});
      return (GeneralizedAuthenticationManager)server.invoke(this.authenticationManagerService,
            "getSecurityManager", 
            new String[]{securityDomain}, new String[]{"java.lang.String"}); 
   } 
   
   private AuthorizationManager getAuthorizationManager() throws Exception
   { 
      MBeanServer server = MBeanServerLocator.locateJBoss();
      GeneralizedAuthenticationManager gam = this.getAuthenticationManager();
      String securityDomain = gam.getSecurityDomain();
      return (AuthorizationManager)server.invoke(this.authorizationManagerService,
            "getAuthorizationManager", 
            new String[]{securityDomain}, new String[]{"java.lang.String"});
   }
}
