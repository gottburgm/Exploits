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
package org.jboss.web.tomcat.security.authenticators;

import java.security.PrivilegedAction;
import java.security.Principal;
import java.security.AccessController;

import javax.security.auth.Subject;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.RunAsIdentity;

/** A PrivilegedAction implementation for setting the SecurityAssociation
 * principal and credential
 * 
 * @author Scott.Stark@jboss.org
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
      SetPrincipalInfoAction(Principal principal, Object credential, Subject subject)
      {
         this.principal = principal;
         this.credential = credential;
         this.subject = subject;
      }

      public Object run()
      {
         SecurityAssociation.pushSubjectContext(subject, principal, credential);
         credential = null;
         principal = null;
         subject = null;
         return null;
      }
   }
   private static class SetServerAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new SetServerAction();
      public Object run()
      {
         SecurityAssociation.setServer();
         return null;
      }
   }
   private static class ClearAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new ClearAction();
      public Object run()
      {
         SecurityAssociation.clear();
         return null;
      }
   }
   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject subject = SecurityAssociation.getSubject();
         return subject;
      }
   }
   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();
      public Object run()
      {
         Principal principal = SecurityAssociation.getPrincipal();
         return principal;
      }
   }
   private static class GetCredentialAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetCredentialAction();
      public Object run()
      {
         Object credential = SecurityAssociation.getCredential();
         return credential;
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
         SecurityAssociation.pushRunAsIdentity(principal);
         return null;
      }
   }

   private static class PopRunAsRoleAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new PopRunAsRoleAction();
      public Object run()
      {
         RunAsIdentity principal = SecurityAssociation.popRunAsIdentity();
         return principal;
      }
   }
   private static class GetAuthExceptionAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetAuthExceptionAction();
      public Object run()
      {
         Object exception = SecurityAssociation.getContextInfo(AUTH_EXCEPTION_KEY);
         return exception;
      }
   }
   private static class ClearAuthExceptionAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new ClearAuthExceptionAction();
      public Object run()
      {
         Object exception = SecurityAssociation.setContextInfo(AUTH_EXCEPTION_KEY, null);
         return exception;
      }
   }

   static void setPrincipalInfo(Principal principal, Object credential, Subject subject)
   {
      SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential, subject);
      AccessController.doPrivileged(action);
   }
   static void setServer()
   {
      AccessController.doPrivileged(SetServerAction.ACTION);
   }
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
}
