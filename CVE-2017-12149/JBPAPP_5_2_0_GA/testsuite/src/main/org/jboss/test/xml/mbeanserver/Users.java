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
package org.jboss.test.xml.mbeanserver;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.security.acl.Group;
import java.security.Principal;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

/**
 * The root object of the user-roles.xsd schema
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class Users
{
   private HashMap users = new HashMap();

   public static class User implements Comparable
   {
      private String name;
      private String password;
      private String encoding;
      private HashMap roleGroups = new HashMap();

      public User()
      {
      }
      public User(String name)
      {
         this.name = name;
      }
      public String getName()
      {
         return name;
      }
      public String getPassword()
      {
         return password;
      }
      public void setPassword(String password)
      {
         this.password = password;
      }

      public String getEncoding()
      {
         return encoding;
      }
      public void setEncoding(String encoding)
      {
         this.encoding = encoding;
      }

      public Group[] getRoleSets()
      {
         Group[] roleSets = new Group[roleGroups.size()];
         roleGroups.values().toArray(roleSets);
         return roleSets;
      }
      public String[] getRoleNames()
      {
         return getRoleNames("Roles");
      }
      public String[] getRoleNames(String roleGroup)
      {
         Group group = (Group) roleGroups.get(roleGroup);
         String[] names = {};
         if( group != null )
         {
            ArrayList tmp = new ArrayList();
            Enumeration iter = group.members();
            while( iter.hasMoreElements() )
            {
               Principal p = (Principal) iter.nextElement();
               tmp.add(p.getName());
            }
            names = new String[tmp.size()];
            tmp.toArray(names);
         }
         return names;
      }
      public void addRole(String roleName, String roleGroup)
      {
         Group group = (Group) roleGroups.get(roleGroup);
         if( group == null )
         {
            group = new SimpleGroup(roleGroup);
            roleGroups.put(roleGroup, group);
         }
         SimplePrincipal role = new SimplePrincipal(roleName);
         group.addMember(role);
      }
      public int compareTo(Object obj)
      {
         Users.User u = (Users.User) obj;
         return name.compareTo(u.name);
      }

      public String toString()
      {
         return "User{" +
            "name='" + name + "'" +
            ", password=*" + 
            ", encoding='" + encoding + "'" +
            ", roleGroups=" + roleGroups +
            "}";
      }
   }

   public void addUser(Users.User user)
   {
      users.put(user.getName(), user);
   }
   public Iterator getUsers()
   {
      return users.values().iterator();
   }
   public Users.User getUser(String name)
   {
      Users.User find = (Users.User) users.get(name);
      return find;
   }
   
   public String toString()
   {
      return "Users("+System.identityHashCode(this)+"){" +
         "users=" + users +
         "}";
   }
}
