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
package org.jboss.test.security.ejb;

import java.security.acl.Group;
import java.security.Principal;
import java.util.Enumeration;

import javax.security.auth.login.LoginException;

import org.jboss.security.auth.spi.UsersRolesLoginModule;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

/** A simple override of UsersRolesLoginModule used to test security domain
 * traversal. This simulates trust based on the same passwords with the
 * roles of the second domain being distinct modifications of the first
 * domains roles.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class UsersRoles2LoginModule extends UsersRolesLoginModule
{
   /**
    * Override to add '2' to every role name to make the roles different
    * @throws LoginException
    */ 
   protected Group[] getRoleSets() throws LoginException
   {
      Group[] groups = super.getRoleSets();
      Group[] newGroups = {new SimpleGroup("Roles")};
      Group roles = null;
      Group newRoles = newGroups[0];
      for(int n = 0; n < groups.length; n ++)
      {
         Group g = groups[n];
         if( g.getName().equals("Roles") )
         {
            roles = g;
            break;
         }
      }
      if( roles != null )
      {
         Enumeration iter = roles.members();
         Principal role = (Principal) iter.nextElement();
         String name2 = role.getName() + "2";
         SimplePrincipal role2 = new SimplePrincipal(name2);
         newRoles.addMember(role2);
      }
      return newGroups;
   }
}
