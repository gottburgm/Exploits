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
package org.jboss.jms.server.jbosssx;
 
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SecurityContextAssociation;


/** A collection of privileged actions for this package
* @author Scott.Stark@jboss.org
* @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
* @author <a her="mailto:tim.fox@jboss.com">Tim Fox</a>
* @version $Revison: 1.0$
*/
class SecurityActions
{
  interface PrincipalInfoAction
  {
     PrincipalInfoAction PRIVILEGED = new PrincipalInfoAction()
     {
        public void push(final Principal principal, final Object credential,
           final Subject subject, final String domain)
        {
           AccessController.doPrivileged(
              new PrivilegedAction<Object>()
              {
                 public Object run()
                 {
                    try
                    {
                       SecurityContext sc = SecurityContextFactory.createSecurityContext(domain);
                       SecurityContextAssociation.setSecurityContext(sc);
                       sc.getUtil().createSubjectInfo(principal, credential, subject);
                    }
                    catch (Exception e)
                    {
                      throw new RuntimeException(e);
                    }
                    return null;
                 }
              }
           );
        } 
        public void pop()
        {
           AccessController.doPrivileged(
              new PrivilegedAction<Object>()
              {
                 public Object run()
                 {
                    SecurityContextAssociation.clearSecurityContext(); 
                    return null;
                 }
              }
           );
        }
     };

     PrincipalInfoAction NON_PRIVILEGED = new PrincipalInfoAction()
     {
        public void push(Principal principal, Object credential, Subject subject, String domain)
        {
           //ToDo: Get the correct security domain name
           try
           {
              SecurityContext sc = SecurityContextFactory.createSecurityContext(domain);
              SecurityContextAssociation.setSecurityContext(sc);
              sc.getUtil().createSubjectInfo(principal, credential, subject);
           }
           catch (Exception e)
           {
             throw new RuntimeException(e);
           }
        }
         
        public void pop()
        {
           SecurityContextAssociation.clearSecurityContext();
        }
     };

     void push(Principal principal, Object credential, Subject subject, String domain);
     void pop();
  }

  static void pushSubjectContext(Principal principal, Object credential,
     Subject subject, String domain)
  {
     if(System.getSecurityManager() == null)
     {
        PrincipalInfoAction.NON_PRIVILEGED.push(principal, credential, subject, domain);
     }
     else
     {
        PrincipalInfoAction.PRIVILEGED.push(principal, credential, subject, domain);
     }
  }
 }
