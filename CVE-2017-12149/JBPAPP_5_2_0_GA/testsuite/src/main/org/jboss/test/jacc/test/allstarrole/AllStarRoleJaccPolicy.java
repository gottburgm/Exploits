/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.test.jacc.test.allstarrole;

import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;

import org.jboss.security.jacc.DelegatingPolicy;

//$Id: AllStarRoleJaccPolicy.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  JBAS-1824: Jacc Policy Provider for testing that bypasses authorization checks
 *  for <role-name>*</role-name>
 *  
 *  This policy is an extension of DelegatingPolicy and only checks for the
 *  presence of WebResourcePermission(url, null) for role-name '*'
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Feb 16, 2007 
 *  @version $Revision: 85945 $
 */
public class AllStarRoleJaccPolicy extends DelegatingPolicy
{  
   public AllStarRoleJaccPolicy()
   {
      super(); 
   }

   public AllStarRoleJaccPolicy(Policy delegate)
   {
      super(delegate); 
   } 

   public boolean implies(ProtectionDomain domain, Permission permission)
   { 
      boolean implied = false; 

      if (permission instanceof WebResourcePermission == false)
      {
         // Let DelegatingPolicy handle the check
         implied = super.implies(domain, permission);
      }
      else
      {  
         try
         {
            Permissions perms = this.getPermissionsForRole("*");
            if(perms != null)
               implied = perms.implies(new WebResourcePermission("/*",(String)null)); 
         }
         catch (PolicyContextException e)
         {
            throw new RuntimeException(e);
         }  
      }
      return implied;
   }
}
