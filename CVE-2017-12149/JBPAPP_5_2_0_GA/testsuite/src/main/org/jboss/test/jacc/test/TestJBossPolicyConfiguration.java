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
package org.jboss.test.jacc.test;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.HashMap;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

//$Id: TestJBossPolicyConfiguration.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Policy Configuration used for permissions validation
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 18, 2006 
 *  @version $Revision: 85945 $
 */
public class TestJBossPolicyConfiguration implements PolicyConfiguration
{
   private String contextID;  
   
   private Permissions excludedPolicy = new Permissions();
   private Permissions uncheckedPolicy = new Permissions();
   
   private HashMap<String,Permissions> rolePerms = new HashMap<String,Permissions>();
   

   public TestJBossPolicyConfiguration(String contextID)
   { 
      this.contextID = contextID;
   }

   public void addToExcludedPolicy(Permission permission) throws PolicyContextException
   { 
      this.excludedPolicy.add(permission);
   }

   public void addToExcludedPolicy(PermissionCollection permissions) throws PolicyContextException
   { 
      Enumeration<Permission> en = permissions.elements();
      while(en.hasMoreElements())
         addToExcludedPolicy(en.nextElement());
   }

   public void addToRole(String roleName, Permission permission) throws PolicyContextException
   { 
      Permissions p = rolePerms.get(roleName);
      if(p == null)
         p = new Permissions();
      p.add(permission);
      rolePerms.put(roleName, p);
   }

   public void addToRole(String roleName, PermissionCollection permissions) throws PolicyContextException
   { 
      Enumeration<Permission> en = permissions.elements();
      while(en.hasMoreElements())
         addToRole(roleName,en.nextElement());
   }

   public void addToUncheckedPolicy(Permission permission) throws PolicyContextException
   { 
      this.uncheckedPolicy.add(permission);
   }

   public void addToUncheckedPolicy(PermissionCollection permissions) throws PolicyContextException
   { 
      Enumeration<Permission> en = permissions.elements();
      while(en.hasMoreElements())
         addToUncheckedPolicy(en.nextElement());
   }

   public void commit() throws PolicyContextException
   { 
   }

   public void delete() throws PolicyContextException
   { 
   }

   public String getContextID() throws PolicyContextException
   { 
      return this.contextID;
   }

   public boolean inService() throws PolicyContextException
   { 
      return false;
   }

   public void linkConfiguration(PolicyConfiguration link) throws PolicyContextException
   {  
   }

   public void removeExcludedPolicy() throws PolicyContextException
   { 
      this.excludedPolicy = null;
   }

   public void removeRole(String roleName) throws PolicyContextException
   { 
      Permissions p = this.rolePerms.get(roleName);
      if(p != null)
      {
         p = null;
         rolePerms.remove(roleName);
      }   
   }

   public void removeUncheckedPolicy() throws PolicyContextException
   { 
      this.uncheckedPolicy = null;
   }
   
   //Value added methods 

   public Permissions getExcludedPolicy()
   {
      return excludedPolicy;
   }

   public Permissions getUncheckedPolicy()
   {
      return uncheckedPolicy;
   } 
   
   public Permissions getPermissionsForRole(String roleName)
   {
      return this.rolePerms.get(roleName);
   }
}
