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
package org.jboss.test.jacc.test.portal;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration; 
import java.util.Iterator;
import java.util.List; 
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.jboss.logging.Logger;

//$Id: LazyPermissionCollection.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBPORTAL-565: Create Testcase for JACC Usage
 *  PermissionCollection that is lazily loaded
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 16, 2006 
 *  @version $Revision: 81036 $
 */
public class LazyPermissionCollection extends PermissionCollection
{
   private static Logger log = Logger.getLogger(LazyPermissionCollection.class);
    
   private List permissionTable = new ArrayList();
   
   public void add(Permission perm)
   { 
      log.debug("Inside add with perm=" + perm);
      permissionTable.add(perm);
   }

   
   public boolean implies(Permission perm)
   { 
      log.debug("Inside implies with perm=" + perm); 
      
      if(perm instanceof PortalPermission == false)
         return false;
      
      try
      {
         loadPermissionsBasedOnRole();
      }catch(Exception e)
      {
         log.error(e);
         return false;
      }
      
      int len = permissionTable.size();
      for(int i = 0; i < len ; i++)
      {
         Permission p = (Permission)permissionTable.get(i);
         if(p.implies(perm))
            return true;
      } 
      
      return false;
   }

    
   public Enumeration elements()
   { 
      return Collections.enumeration(permissionTable); 
   }
    
   
   private String getRole() throws Exception
   {
      Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
      Set principals = subject.getPrincipals();
      Iterator iter = principals != null ? principals.iterator() : null;
      while(iter != null && iter.hasNext())
      {
         Principal p = (Principal)iter.next();
         if(p instanceof Group)
         {
            Group gp = (Group)p;
            if("Roles".equals(gp.getName()) == false)
               continue;
            Enumeration en = gp.members();
            while(en.hasMoreElements())
            {
               Principal role = (Principal)en.nextElement();
               return role.getName();
            }
         }
      }
      return null; 
   }
   
   private void loadPermissionsBasedOnRole() throws Exception
   {
      String role = this.getRole();
      if(role == null)
         throw new IllegalStateException("role is null");
      this.permissionTable.clear();
      if("employee".equalsIgnoreCase(role))
      {
         //Permitted to access for all portal resources
         permissionTable.add(new PortalObjectPermission("/default", "view"));
      }
      else
         if("janitor".equalsIgnoreCase(role))
         {
            //Only permitted to view a window
            permissionTable.add(new PortalObjectPermission("/default/default/a", "view"));
         }
         else
            if("admin".equals(role))
            {
               //Permitted for the whole context
               permissionTable.add(new PortalObjectPermission("/", "view"));
            } 
   }
}
