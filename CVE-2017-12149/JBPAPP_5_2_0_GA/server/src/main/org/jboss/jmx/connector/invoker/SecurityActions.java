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
package org.jboss.jmx.connector.invoker;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction; 
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
 
import org.jboss.security.SecurityAssociation;  
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SecurityContextAssociation;

/** Common PrivilegedAction used by classes in this package.
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@redhat.com
 * @version $Revison:$
 */
class SecurityActions
{
   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject subject = SecurityAssociation.getSubject();
         return subject;
      }
   }
   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }
   private static class SetTCLAction implements PrivilegedAction
   {
      ClassLoader loader;
      SetTCLAction(ClassLoader loader)
      {
         this.loader = loader;
      }
      public Object run()
      {
         Thread.currentThread().setContextClassLoader(loader);
         loader = null;
         return null;
      }
   }
   interface PrincipalInfoAction
   {
      PrincipalInfoAction PRIVILEGED = new PrincipalInfoAction()
      {
         public void push(final Principal principal, final Object credential,
            final Subject subject)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     //SecurityAssociation.pushSubjectContext(subject, principal, credential);
                     getSecurityContext().getUtil().createSubjectInfo(principal, credential, subject);
                     return null;
                  }
               }
            );
         }
         public void pop()
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                   //SecurityAssociation.popSubjectContext();
                     SecurityContext sc = getSecurityContext();
                     if(sc != null)
                     {
                        sc.getUtil().createSubjectInfo(null, null, null);
                     } 
                     return null;
                  }
               }
            );
         }
      };

      PrincipalInfoAction NON_PRIVILEGED = new PrincipalInfoAction()
      {
         public void push(Principal principal, Object credential, Subject subject)
         {
            //SecurityAssociation.pushSubjectContext(subject, principal, credential);
            getSecurityContext().getUtil().createSubjectInfo(principal, credential, subject); 
         }
         public void pop()
         {
            //SecurityAssociation.popSubjectContext();
            SecurityContext sc = getSecurityContext();
            if(sc != null)
            {
               sc.getUtil().createSubjectInfo(null, null, null);
            } 
         }
      };

      void push(Principal principal, Object credential, Subject subject);
      void pop();
   }
   
   static class SetSecurityContextAction implements PrivilegedAction
   { 
      private SecurityContext securityContext;

      SetSecurityContextAction(SecurityContext sc)
      {
         this.securityContext = sc; 
      }
      
      public Object run()
      {
         SecurityContextAssociation.setSecurityContext(securityContext);
         return null;
      }
   }

   static Subject getActiveSubject()
   {
      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }
   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }
   static void setContextClassLoader(ClassLoader loader)
   {
      PrivilegedAction action = new SetTCLAction(loader);
      AccessController.doPrivileged(action);
   }

   static void pushSubjectContext(Principal principal, Object credential,
      Subject subject)
   {
      if(System.getSecurityManager() == null)
      {
         PrincipalInfoAction.NON_PRIVILEGED.push(principal, credential, subject);
      }
      else
      {
         PrincipalInfoAction.PRIVILEGED.push(principal, credential, subject);
      }
   }
   static void popSubjectContext()
   {
      if(System.getSecurityManager() == null)
      {
         PrincipalInfoAction.NON_PRIVILEGED.pop();
      }
      else
      {
         PrincipalInfoAction.PRIVILEGED.pop();
      }
   }
  
   static SecurityContext createSecurityContext(final String domain) 
   throws PrivilegedActionException
   {
      return (SecurityContext)AccessController.doPrivileged( new PrivilegedExceptionAction()
      {

         public Object run() throws Exception
         {
            return SecurityContextFactory.createSecurityContext(domain); 
         }});
   } 
   
   static SecurityContext getSecurityContext()
   {
      return (SecurityContext)AccessController.doPrivileged( new PrivilegedAction()
      {

         public Object run()
         {
            return SecurityContextAssociation.getSecurityContext();
         }});
   }
   
   
   static void clearSecurityContext()
   { 
      AccessController.doPrivileged(new PrivilegedAction(){

         public Object run()
         {
           SecurityContextAssociation.clearSecurityContext();
           return null;
         }});
   }
   
   static void setSecurityContext(SecurityContext sc)
   {
      SetSecurityContextAction action = new SetSecurityContextAction(sc);
      AccessController.doPrivileged(action);
   }
}
