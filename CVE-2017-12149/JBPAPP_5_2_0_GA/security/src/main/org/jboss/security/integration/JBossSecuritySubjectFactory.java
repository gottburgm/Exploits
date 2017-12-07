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
package org.jboss.security.integration;

import java.security.Principal;

import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SubjectFactory;

/**
 *  Create a Subject given the details available
 *  via implementation strategies such as SecurityContextAssociation
 *  to get hold of the Principal, Cred etc
 *  @author Anil.Saldhana@redhat.com
 *  @since  May 19, 2008 
 *  @version $Revision: 85945 $
 */
@ManagementObject(name = "JBossSecuritySubjectFactory", componentType = @ManagementComponent(
                  type = "MCBean", subtype = "Security"))
public class JBossSecuritySubjectFactory implements SubjectFactory
{ 
   protected static Logger log = Logger.getLogger(JBossSecuritySubjectFactory.class);
   
   protected ISecurityManagement securityManagement;

   /**
    * @see SubjectFactory#createSubject()
    */
   @ManagementOperation(description="Create a subject using the modules specified in the 'other' security domain",
         impact=ManagedOperation.Impact.ReadOnly)
   public Subject createSubject()
   { 
      return createSubject(SecurityConstants.DEFAULT_APPLICATION_POLICY);
   }
   
   /**
    * @see SubjectFactory#createSubject(String)
    */
   @ManagementOperation(description="Create a subject using the modules configured in the specified security domain",
         params={@ManagementParameter(name="securityDomainName", description="The security domain name")},
         impact=ManagedOperation.Impact.ReadOnly)
   public Subject createSubject(String securityDomainName)
   {
      if(this.securityManagement == null)
         throw new IllegalStateException("SecurityManagement has not been injected");
      Subject subject = new Subject();
      //Validate the caller
      Principal principal = SecurityActions.getPrincipal();
      AuthenticationManager authenticationManager = securityManagement.getAuthenticationManager(securityDomainName);
      if(authenticationManager == null)
      {
         String defaultSecurityDomain = SecurityConstants.DEFAULT_APPLICATION_POLICY;
         if(log.isTraceEnabled())
         {
            log.trace("AuthenticationManager for " 
                  + securityDomainName + " not found. Using " + defaultSecurityDomain);
         }
         authenticationManager = 
            securityManagement.getAuthenticationManager(defaultSecurityDomain);
      }
      if(authenticationManager.isValid(principal, 
            SecurityActions.getCredential(), subject) == false)
         throw new SecurityException("Unauthenticated caller:" + principal);
      return subject;
   }
   
   /**
    * Inject SecurityManagement
    * @param securityManagement
    */
   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement; 
   }
}