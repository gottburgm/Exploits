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
package org.jboss.test.security.test.auth;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashSet;
import java.security.acl.Group;
import java.security.Principal;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.security.SimplePrincipal;

/**
 A role mapping login module.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class RoleMappingLoginModule implements LoginModule
{
   /** The sec domain to app domaon role mappings */
   private HashMap roleMappings = new HashMap();
   /** The mapped roles added to the subject */
   HashSet addedRoles = new HashSet();
   private Subject theSubject;

   public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options)
   {
      this.theSubject = subject;

      int count = 1;
      String key = "role.";
      String mapping = (String) options.get(key+count);
      while( mapping != null )
      {
         StringTokenizer tokenizer = new StringTokenizer(mapping, "=,");
         String appRole = tokenizer.nextToken();
         while( tokenizer.hasMoreTokens() )
         {
            String secDomainRole = tokenizer.nextToken();
            roleMappings.put(secDomainRole, appRole);
         }
         count ++;
         mapping = (String) options.get(key+count);
      }
   }

   /**
    there is nothing to do here
    @return true
    */
   public boolean login()
   {
      return true;
   }

   /**
    Add the mapped roles
    @return true
    @throws LoginException
    */
   public boolean commit() throws LoginException
   {
      Set groups = theSubject.getPrincipals(Group.class);
      Iterator iter = groups.iterator();
      Group roles = null;
      while( iter.hasNext() )
      {
         Group g = (Group) iter.next();
         if( g.getName().equals("Roles") )
         {
            roles = g;
            break;
         }
      }
      // Map the group roles
      if( roles != null )
      {
         
         Enumeration members = roles.members();
         while( members.hasMoreElements() )
         {
            Principal role = (Principal) members.nextElement();
            String name = role.getName();
            String mappedName = (String) roleMappings.get(name);
            if( mappedName != null )
            {
               SimplePrincipal p = new SimplePrincipal(mappedName);
               addedRoles.add(p);
            }
         }

         Iterator riter = addedRoles.iterator();
         while( riter.hasNext() )
         {
            Principal p = (Principal) riter.next();
            roles.addMember(p);
         }
      }

      return true;
   }

   public boolean abort() throws LoginException
   {
      return true;
   }

   /**
    Remove the added roles
    @return true
    */
   public boolean logout()
   {
      if( theSubject.isReadOnly() == false )
      {
         Set groups = theSubject.getPrincipals(Group.class);
         Iterator iter = groups.iterator();
         Group roles = null;
         while( iter.hasNext() )
         {
            Group g = (Group) iter.next();
            if( g.getName().equals("Roles") )
            {
               roles = g;
               break;
            }
         }
         // Remove the added roles
         Iterator riter = addedRoles.iterator();
         while( riter.hasNext() )
         {
            Principal p = (Principal) riter.next();
            roles.removeMember(p);
         }
      }
      return true;
   }
}
