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
package org.jboss.wsf.container.jboss50.invocation;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

/**
 * A JBoss specific SecurityAssociationAdaptor 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class SecurityAdaptorImpl implements SecurityAdaptor
{
   SecurityAdaptorImpl()
   {
   }

   public Principal getPrincipal()
   {
      return SecurityAssociation.getPrincipal();
   }
   
   public void setPrincipal(Principal pricipal)
   {
      SecurityAssociation.setPrincipal(pricipal);
   }

   public Object getCredential()
   {
      return SecurityAssociation.getCredential();
   }

   public void setCredential(Object credential)
   {
      SecurityAssociation.setCredential(credential);
   }

   public void pushSubjectContext(Subject subject, Principal principal, Object credential)
   {
      SecurityAdaptorImpl.pushSubjectContext(principal, credential, subject);
   }

   private static SecurityContext getSecurityContext()
   {
      return (SecurityContext)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run()
         {
            return SecurityContextAssociation.getSecurityContext();
         }
      });
   }

   private static void pushSubjectContext(final Principal p, final Object cred, final Subject s)
   {
      AccessController.doPrivileged(new PrivilegedAction() {

         public Object run()
         {
            SecurityContext sc = getSecurityContext();
            if (sc == null)
               throw new IllegalStateException("Security Context is null");
            sc.getUtil().createSubjectInfo(p, cred, s);
            return null;
         }
      });
   }


}
