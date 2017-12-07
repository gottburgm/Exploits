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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.security.ISecurityManagement;
import org.jboss.security.authorization.PolicyRegistration;

/**
 *  Establishes Security Instances
 *  under a particular JNDI context
 *  @author Anil.Saldhana@redhat.com
 *  @since  Apr 17, 2008 
 *  @version $Revision: 85945 $
 */
public class JNDIBasedSecurityRegistration
{
   private String policyRegistrationContextName = "java:/policyRegistration";
   
   private String securityManagementContextName = "java:/securityManagement";
    
   private PolicyRegistration policyRegistration = null; 
   
   private ISecurityManagement securityManagement = null;
   
   public PolicyRegistration getPolicyRegistration()
   {
      return policyRegistration;
   }

   public void setPolicyRegistration(PolicyRegistration policyRegistration)
   {
      if(policyRegistration == null)
         throw new IllegalArgumentException("null policyRegistration");
      this.policyRegistration = policyRegistration;
      try
      {
         establishPolicyRegistration();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public void setSecurityManagement(ISecurityManagement securityMgmt)
   {
      if(securityMgmt == null)
         throw new IllegalArgumentException("null securityMgmt");
      this.securityManagement = securityMgmt;
      try
      {
         this.establishSecurityManagement();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   
   
   public String getPolicyRegistrationContextName()
   {
      return policyRegistrationContextName;
   }

   public void setPolicyRegistrationContextName(String policyRegistrationContextName)
   {
      this.policyRegistrationContextName = policyRegistrationContextName;
   }

   public String getSecurityManagementContextName()
   {
      return securityManagementContextName;
   }

   public void setSecurityManagementContextName(String securityManagementContextName)
   {
      this.securityManagementContextName = securityManagementContextName;
   }

   private void establishPolicyRegistration() throws Exception
   {
      Context ctx = new InitialContext();
      ctx.rebind(this.policyRegistrationContextName, this.policyRegistration);
   }
   
   private void establishSecurityManagement() throws Exception
   {
      Context ctx = new InitialContext();
      ctx.rebind(this.securityManagementContextName, this.securityManagement);
   }
}