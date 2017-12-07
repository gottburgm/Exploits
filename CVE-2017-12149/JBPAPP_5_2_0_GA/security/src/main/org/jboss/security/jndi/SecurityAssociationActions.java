/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.security.jndi;

import java.security.PrivilegedAction;
import java.security.Principal;
import java.security.AccessController; 

import javax.security.auth.Subject;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;

/** A PrivilegedAction implementation for setting the SecurityAssociation
 * principal and credential
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
class SecurityAssociationActions
{
   private static class SetPrincipalInfoAction implements PrivilegedAction<Object>
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
      
      @SuppressWarnings("deprecation")
      public Object run()
      {
         //Client Side usage
         if(!getServer())
         {
            SecurityAssociation.pushSubjectContext(subject, principal, credential);
         }
         
         //Always create a new security context
         SecurityContext sc = null;
         try
         {
            sc = SecurityContextFactory.createSecurityContext(principal, 
                                                credential, subject, "CLIENT_LOGIN_MODULE");
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }          
         setSecurityContext(sc); 
         
         credential = null;
         principal = null;
         subject = null;
         return null;
      }
   }
   private static class PopPrincipalInfoAction implements PrivilegedAction<Object>
   {
      @SuppressWarnings("deprecation")
      public Object run()
      {
         if(!getServer())
           SecurityAssociation.popSubjectContext(); 
         return null;
      }
   }

   private static class GetTCLAction implements PrivilegedAction<Object>
   {
      static PrivilegedAction<Object> ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }
   private static class SetServerAction implements PrivilegedAction<Object>
   {
      static PrivilegedAction<Object> ACTION = new SetServerAction();
      public Object run()
      {
         SecurityAssociation.setServer();
         return null;
      }
   }

   static void setSecurityContext(final SecurityContext sc)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         {
            SecurityContextAssociation.setSecurityContext(sc); 
            return null;
         }
      });
   }
   
   static SecurityContext getSecurityContext()
   {
      return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>()
      { 
         public SecurityContext run()
         {
            return SecurityContextAssociation.getSecurityContext(); 
         }
      });
   }
   
   static SecurityContext createSecurityContext(final Principal p, final Object cred, final Subject subject)
   {
      return (SecurityContext) AccessController.doPrivileged(new PrivilegedAction<SecurityContext>()
      {
         public SecurityContext run()
         {
        	 SecurityContext sc = null;
        	 try
        	 {
        		sc = SecurityContextFactory.createSecurityContext(p, cred, subject, "CLIENT_LOGIN_MODULE");
        	 }
        	 catch (Exception e)
        	 {
        		 throw new RuntimeException(e);
        	 }
            return sc;
         }
      });
   }
   
   static void pushSecurityContext(final Principal p, final Object cred, 
         final Subject subject, final String securityDomain)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         @SuppressWarnings("deprecation")
         public Object run()
         {
            SecurityContext sc;
            try
            {
               sc = SecurityContextFactory.createSecurityContext(p, cred, 
                     subject, securityDomain);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
            setSecurityContext(sc);
            //For Client Side legacy usage
            if(getServer() == Boolean.FALSE)
            {
               SecurityAssociation.pushSubjectContext(subject, p, cred);
            }
            return null;
         }
      });
   }
   static Boolean getServer()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return SecurityAssociation.isServer();
         }
      });
   }
   
   static void setServer()
   {
      AccessController.doPrivileged(SetServerAction.ACTION);
   }

   static void setPrincipalInfo(Principal principal, Object credential, Subject subject)
   {
      SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential, subject);
      AccessController.doPrivileged(action);
   }
   static void popPrincipalInfo()
   {
      PopPrincipalInfoAction action = new PopPrincipalInfoAction();
      AccessController.doPrivileged(action);
   }

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }

}
