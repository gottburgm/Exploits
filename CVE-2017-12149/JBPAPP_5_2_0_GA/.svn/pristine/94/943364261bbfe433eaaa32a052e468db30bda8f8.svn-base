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

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;

import javax.security.auth.Subject;

import org.jboss.security.RunAs;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.javaee.AbstractWebAuthorizationHelper;

/** A PrivilegedAction implementation for setting the SecurityAssociation
 * principal and credential
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revison:$
 */
class SecurityAssociationActions
{
   public static final String AUTH_EXCEPTION_KEY = "org.jboss.security.exception";

   private static class SetPrincipalInfoAction implements PrivilegedAction
   {
      Principal principal;
      Object credential;
      Subject subject;
      String securityDomain;
      
      SetPrincipalInfoAction(Principal principal, Object credential, Subject subject)
      {
         this.principal = principal;
         this.credential = credential;
         this.subject = subject;  
      }

      public Object run()
      {
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context has not been set");
         
         sc.getUtil().createSubjectInfo(principal, credential, subject); 
         //SecurityAssociation.pushSubjectContext(subject, principal, credential); 
         credential = null;
         principal = null;
         subject = null;
         return null;
      }
   } 
   
   private static class ClearAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new ClearAction();
      public Object run()
      {
         SecurityAssociation.clear();
         //SecurityContextAssociation.clearSecurityContext();
         return null;
      }
   }
   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         //Subject subject = SecurityAssociation.getSubject();
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null");
         return sc.getUtil().getSubject(); 
      }
   }
   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();
      public Object run()
      {
         //Principal principal = SecurityAssociation.getPrincipal();
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null");
         return sc.getUtil().getUserPrincipal();
      }
   }
   private static class GetCredentialAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetCredentialAction();
      public Object run()
      {
         //Object credential = SecurityAssociation.getCredential();
         //return credential;
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null");
         return sc.getUtil().getCredential();
      }
   } 
    
   
   private static class PushRunAsRoleAction implements PrivilegedAction
   {
      RunAsIdentity principal;
      PushRunAsRoleAction(RunAsIdentity principal)
      {
         this.principal = principal;
      }
      public Object run()
      {
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null"); 
         sc.setOutgoingRunAs(principal); 
         //SecurityAssociation.pushRunAsIdentity(principal);
         return null;
      }
   }

   private static class PopRunAsRoleAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new PopRunAsRoleAction();
      public Object run()
      {
         //RunAsIdentity principal = SecurityAssociation.popRunAsIdentity();
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null");
         RunAs principal = null; 
         principal = sc.getOutgoingRunAs();
         sc.setOutgoingRunAs(null); 
         return principal;
      }
   }
   
   private static class GetAuthExceptionAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetAuthExceptionAction();
      public Object run()
      {
         //Object exception = SecurityAssociation.getContextInfo(AUTH_EXCEPTION_KEY);
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null");
         Object exception = sc.getData().get(AUTH_EXCEPTION_KEY) ;
         return exception;
      }
   }
   private static class ClearAuthExceptionAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new ClearAuthExceptionAction();
      public Object run()
      {
         //Object exception = SecurityAssociation.setContextInfo(AUTH_EXCEPTION_KEY, null);
         SecurityContext sc = getSecurityContext();
         if(sc == null)
            throw new IllegalStateException("Security Context is null");
         sc.getData().put(AUTH_EXCEPTION_KEY, null); 
         return null;
         //return exception;
      }
   } 

   static void clearSecurityContext()
   {
      //ClearSecurityContextAction action = new ClearSecurityContextAction(securityDomain);
      AccessController.doPrivileged(new PrivilegedAction()
      { 
         public Object run()
         {
            SecurityContextAssociation.clearSecurityContext();
            return null;
         }
       });
   }
   
   static SecurityContext getSecurityContext()
   {
      //GetSecurityContextAction action = new GetSecurityContextAction(securityDomain);
      return (SecurityContext)AccessController.doPrivileged(new PrivilegedAction()
      { 
         public Object run()
         {
            return SecurityContextAssociation.getSecurityContext(); 
         }
       }); 
   }
   
   static SecurityContext createSecurityContext(final String securityDomain) throws PrivilegedActionException
   {
      return (SecurityContext)AccessController.doPrivileged(new PrivilegedExceptionAction()
      { 
         public Object run() throws Exception
         {
            return SecurityContextFactory.createSecurityContext(securityDomain); 
         }
       });
   }
   
   static SecurityContext createSecurityContext(final String securityDomain,
         final String fqnClassName) throws PrivilegedActionException
   {
      return (SecurityContext)AccessController.doPrivileged(new PrivilegedExceptionAction()
      { 
         public Object run() throws Exception
         {
            return SecurityContextFactory.createSecurityContext(securityDomain, fqnClassName); 
         }
       });
   }
   
   static SecurityContext createSecurityContext(final String securityDomain,
         final Class<?> clazz) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>()
      { 
         public SecurityContext run() throws Exception
         {
            Constructor<?> ctr = clazz.getConstructor(new Class[] {String.class} );
            Object obj = ctr.newInstance(new Object[] {securityDomain}); 
            return SecurityContext.class.cast(obj);
         }
       });
   }
   
   static void setSecurityContext(final SecurityContext sc)
   {
      //SetSecurityContextAction action = new SetSecurityContextAction(sc,securityDomain);
      AccessController.doPrivileged(new PrivilegedAction()
      { 
         public Object run()
         {
            SecurityContextAssociation.setSecurityContext(sc);
            return null;
         }
       }); 
   } 
   
   static void setPrincipalInfo(Principal principal, Object credential, Subject subject)
   {
      SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential, subject);
      AccessController.doPrivileged(action);
   }
   /*static void setServer()
   {
      AccessController.doPrivileged(SetServerAction.ACTION);
   }*/
   static void clear()
   {
      AccessController.doPrivileged(ClearAction.ACTION);
   }
   static Subject getSubject()
   {
      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }
   static Principal getPrincipal()
   {
      Principal principal = (Principal) AccessController.doPrivileged(GetPrincipalAction.ACTION);
      return principal;
   }
   static Object getCredential()
   {
      Object credential = AccessController.doPrivileged(GetCredentialAction.ACTION);
      return credential;
   }
   static void pushRunAsIdentity(RunAsIdentity principal)
   {
      PushRunAsRoleAction action = new PushRunAsRoleAction(principal);
      AccessController.doPrivileged(action);
   }
   static RunAsIdentity popRunAsIdentity()
   {
      RunAsIdentity principal = (RunAsIdentity) AccessController.doPrivileged(PopRunAsRoleAction.ACTION);
      return principal;
   }

   static Throwable getAuthException()
   {
      Throwable ex = (Throwable) AccessController.doPrivileged(GetAuthExceptionAction.ACTION);
      return ex;
   }
   static void clearAuthException()
   {
      AccessController.doPrivileged(ClearAuthExceptionAction.ACTION);
   } 
   
   static Class<?> loadClass(final String fqn) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>()
      { 
         public Class<?> run() throws PrivilegedActionException, ClassNotFoundException 
         {   
            return Thread.currentThread().getContextClassLoader().loadClass(fqn);
         }
      });
   }
   
   static Subject getSubjectFromRequestPrincipal(final Principal userPrincipal)
   {
	  return AccessController.doPrivileged(new PrivilegedAction<Subject>()
	  {
		public Subject run() 
		{
		   if(userPrincipal instanceof JBossGenericPrincipal)
		   {
		      return ((JBossGenericPrincipal)userPrincipal).getSubject();
		   }
		   return null;
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
   
   static boolean hasRole(final AbstractWebAuthorizationHelper helper,
         final String roleName,
         final Principal principal, final String servletName, 
         final Set<Principal> principalRoles,
         final String contextID, final Subject callerSubject)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return helper.hasRole(roleName, principal, servletName, principalRoles, contextID, 
                  callerSubject);
         }
      });
   }
}