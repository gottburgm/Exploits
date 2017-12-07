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
package org.jboss.ejb.plugins;

import static org.jboss.security.SecurityConstants.DEFAULT_EJB_APPLICATION_POLICY;

import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.auth.Subject;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAs;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SecurityUtil;
import org.jboss.security.identity.plugins.SimpleRoleGroup;
import org.jboss.security.javaee.AbstractEJBAuthorizationHelper;
import org.jboss.security.javaee.EJBAuthenticationHelper;
import org.jboss.security.javaee.SecurityHelperFactory;
import org.jboss.system.Registry;

/**
 * The SecurityInterceptor is where the EJB 2.0 declarative security model
 * is enforced. This is where the caller identity propagation is controlled as well.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 112940 $
 */
public class SecurityInterceptor extends AbstractInterceptor
{
   /** The interface of an observer that should be notified when principal 
    authentication fails.
    */
   public interface AuthenticationObserver
   {
      final String KEY = "SecurityInterceptor.AuthenticationObserver";

      void authenticationFailed();
   }

   /** The authentication manager plugin
    */
   protected AuthenticationManager securityManager;

   /** The authorization manager plugin
    */
   protected RealmMapping realmMapping;

   // The bean uses this run-as identity to call out
   protected RunAs runAsIdentity;

   // A map of SecurityRolesMetaData from jboss.xml
   protected Map securityRoles;

   //A map of principal versus roles from jboss-app.xml/jboss.xml
   protected Map<String, Set<String>> deploymentRoles;

   // The observer to be notified when principal authentication fails.
   // This is a hook for the CSIv2 code. The authenticationObserver may
   // send out a ContextError message, as required by the CSIv2 protocol.
   protected AuthenticationObserver authenticationObserver;

   /** The TimedObject.ejbTimeout callback */
   protected Method ejbTimeout;

   //Authorization Framework changes
   protected String ejbName = null;

   protected CodeSource ejbCS = null;

   /**
    * Security Domain configured as part of the application
    */
   protected String appSecurityDomain = null;

   //Fallback Security Domain
   protected String defaultAuthorizationSecurityDomain = DEFAULT_EJB_APPLICATION_POLICY;

   /**
    * Specify whether <use-caller-identity> is configured, mainly
    * for the use case of caller identity coming with run-as
    */
   protected boolean isUseCallerIdentity = false;

   /**
    * Represents the holder of the various security managers
    * configured at the container level
    */
   protected ISecurityManagement securityManagement = null;

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData applicationMetaData = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = applicationMetaData.getAssemblyDescriptor();
         securityRoles = assemblyDescriptor.getSecurityRoles();
         deploymentRoles = assemblyDescriptor.getPrincipalVersusRolesMap();

         SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
         if (secMetaData != null && secMetaData.getUseCallerIdentity() == false)
         {
            String roleName = secMetaData.getRunAsRoleName();
            String principalName = secMetaData.getRunAsPrincipalName();

            //Special Case: if RunAsPrincipal is not configured, then we use unauthenticatedIdentity
            if (principalName == null)
               principalName = applicationMetaData.getUnauthenticatedPrincipal();

            // the run-as principal might have extra roles mapped in the assembly-descriptor
            Set extraRoleNames = assemblyDescriptor.getSecurityRoleNamesByPrincipal(principalName);
            runAsIdentity = new RunAsIdentity(roleName, principalName, extraRoleNames);
         }

         if (secMetaData != null && secMetaData.getUseCallerIdentity())
            this.isUseCallerIdentity = true;

         securityManager = container.getSecurityManager();
         realmMapping = container.getRealmMapping();
         //authorizationManager = container.getAuthorizationManager();

         try
         {
            // Get the timeout method
            ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]
            {Timer.class});
         }
         catch (NoSuchMethodException ignore)
         {
         }
         if (securityManager != null)
         {
            appSecurityDomain = securityManager.getSecurityDomain();
            appSecurityDomain = SecurityUtil.unprefixSecurityDomain(appSecurityDomain);
         }
         ejbName = beanMetaData.getEjbName();
         ejbCS = container.getBeanClass().getProtectionDomain().getCodeSource();
         securityManagement = (ISecurityManagement) container.getSecurityManagement();
      }
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
      authenticationObserver = (AuthenticationObserver) Registry.lookup(AuthenticationObserver.KEY);

      //Take care of hot deployed security domains
      if (container != null)
      {
         securityManager = container.getSecurityManager();
         if (securityManager != null)
         {
            appSecurityDomain = securityManager.getSecurityDomain();
            appSecurityDomain = SecurityUtil.unprefixSecurityDomain(appSecurityDomain);
         }
      }
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
      boolean isInvoke = false;
      return process(mi, isInvoke);
   }

   public Object invoke(Invocation mi) throws Exception
   {
      boolean isInvoke = true;
      return process(mi, isInvoke);
   }

   /**
    * Process the invocation
    * @param mi
    * @param isInvoke Are we from the invoke method? False = invokeHome method
    * @return
    * @throws Exception
    */
   private Object process(Invocation mi, boolean isInvoke) throws Exception
   {
      if (this.shouldBypassSecurity(mi))
      {
         RunAs previousRunAsIdentity = SecurityActions.peekRunAsIdentity();

         if (log.isTraceEnabled())
            log.trace("Bypass security for invoke or invokeHome");

         try
         {
           if (isInvoke)
             return getNext().invoke(mi);
           else
             return getNext().invokeHome(mi);
         }
         finally
         {
            if( previousRunAsIdentity != null) {
              SecurityActions.pushRunAsIdentity(previousRunAsIdentity);
            }
         }

      }

      SecurityContext sc = SecurityActions.getSecurityContext();
      if (sc == null)
         throw new IllegalStateException("Security Context is null");

      RunAs callerRunAsIdentity = sc.getIncomingRunAs();
      if (log.isTraceEnabled())
         log.trace("Caller RunAs=" + callerRunAsIdentity + ": useCallerIdentity=" + this.isUseCallerIdentity);
      // Authenticate the subject and apply any declarative security checks
      try
      {
         checkSecurityContext(mi, callerRunAsIdentity);
      }
      catch (Exception e)
      {
         log.error("Error in Security Interceptor", e);
         throw e;
      }

      RunAs runAsIdentityToPush = runAsIdentity; 
      /**
       * Special case: if <use-caller-identity> configured and
       * the caller is arriving with a run-as, we need to push that run-as
       */
      if (callerRunAsIdentity != null && this.isUseCallerIdentity)
         runAsIdentityToPush = callerRunAsIdentity;

      /* If a run-as role was specified, push it so that any calls made
       by this bean will have the runAsRole available for declarative
       security checks.
      */
      SecurityActions.pushRunAsIdentity(runAsIdentityToPush);

      try
      {
         if (isInvoke)
            return getNext().invoke(mi);
         else
            return getNext().invokeHome(mi);
      }
      finally
      {
         SecurityActions.popRunAsIdentity();
         SecurityActions.popSubjectContext();
      }
   }

   /** The EJB 2.0 declarative security algorithm:
   1. Authenticate the caller using the principal and credentials in the MethodInvocation
   2. Validate access to the method by checking the principal's roles against
   those required to access the method.
   */
   private void checkSecurityContext(Invocation mi, RunAs callerRunAsIdentity) throws Exception
   {
      Principal principal = mi.getPrincipal();
      Object credential = mi.getCredential();

      boolean trace = log.isTraceEnabled();

      // If there is not a security manager then there is no authentication required
      Method m = mi.getMethod();
      boolean containerMethod = m == null || m.equals(ejbTimeout);
      if (containerMethod == true || securityManager == null || container == null)
      {
         // Allow for the propagation of caller info to other beans
         SecurityActions.pushSubjectContext(principal, credential, null);
         return;
      }

      if (realmMapping == null)
      {
         throw new SecurityException("Role mapping manager has not been set");
      }

      SecurityContext sc = SecurityActions.getSecurityContext();

      EJBAuthenticationHelper helper = SecurityHelperFactory.getEJBAuthenticationHelper(sc);
      boolean isTrusted = containsTrustableRunAs(sc) || helper.isTrusted();

      if (!isTrusted)
      {
         // Check the security info from the method invocation
         Subject subject = new Subject();
         if (SecurityActions.isValid(helper, subject, m.getName()) == false)
         {
            // Notify authentication observer
            if (authenticationObserver != null)
               authenticationObserver.authenticationFailed();
            // Else throw a generic SecurityException
            String msg = "Authentication exception, principal=" + principal;
            throw new SecurityException(msg);
         }
         else
         {
            SecurityActions.pushSubjectContext(principal, credential, subject);
            if (trace)
            {
               log.trace("Authenticated principal=" + principal + " in security domain=" + sc.getSecurityDomain());
            }
         }
      }
      else
      {
         // Duplicate the current subject context on the stack since
         //SecurityActions.dupSubjectContext();  
         SecurityActions.pushRunAsIdentity(callerRunAsIdentity);
      }

      Method ejbMethod = mi.getMethod();
      // Ignore internal container calls
      if (ejbMethod == null)
         return;
      // Get the caller
      Subject caller = SecurityActions.getContextSubject();
      if (caller == null)
         throw new IllegalStateException("Authenticated User. But caller subject is null");

      //Establish the deployment rolename-principalset custom mapping(if available)
      SecurityRolesAssociation.setSecurityRoles(this.deploymentRoles);

      boolean isAuthorized = false;
      Set<Principal> methodRoles = container.getMethodPermissions(ejbMethod, mi.getType());

      SecurityContext currentSC = SecurityActions.getSecurityContext();
      if (SecurityActions.getSecurityManagement(currentSC) == null)
         SecurityActions.setSecurityManagement(currentSC, securityManagement); 

      AbstractEJBAuthorizationHelper authorizationHelper = SecurityHelperFactory.getEJBAuthorizationHelper(sc);
      authorizationHelper.setPolicyRegistration(container.getPolicyRegistration());

      isAuthorized = SecurityActions.authorize(authorizationHelper, ejbName, ejbMethod, mi.getPrincipal(),
            mi.getType().toInterfaceString(), ejbCS, caller, callerRunAsIdentity, container.getJaccContextID(),
            new SimpleRoleGroup(methodRoles));
      
      if (!isAuthorized)
      {
    	 String msg = "Denied: caller with subject=" + caller + " and security context post-mapping roles="
    	       + SecurityActions.getRolesFromSecurityContext(currentSC) + ": ejbMethod=" + ejbMethod;
         throw new SecurityException(msg);
      }
   }

   private boolean shouldBypassSecurity(Invocation mi) throws Exception
   {
      // If there is not a security manager then there is no authentication required
      Method m = mi.getMethod();
      boolean containerMethod = m == null || m.equals(ejbTimeout);
      if (containerMethod == true || securityManager == null || container == null)
      {
         RunAs previousRunAsIdentity = SecurityActions.peekRunAsIdentity();

         // Allow for the propagation of caller info to other beans
         SecurityActions.createAndSetSecurityContext(mi.getPrincipal(), mi.getCredential(), "BYPASSED-SECURITY");
         if (this.runAsIdentity != null)
            SecurityActions.pushRunAsIdentity(runAsIdentity);
         else if( previousRunAsIdentity != null )
            SecurityActions.pushRunAsIdentity(previousRunAsIdentity);
         return true;
      }
      return false;
   }
   
   private boolean containsTrustableRunAs(SecurityContext sc)
   {
      RunAs incomingRunAs = sc.getIncomingRunAs();
      return incomingRunAs != null && incomingRunAs instanceof RunAsIdentity;
   }
}
