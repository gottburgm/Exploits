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
package org.jboss.jmx.connector.invoker;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.security.Principal;
import java.security.acl.Group;

import javax.security.auth.Subject;

import org.jboss.security.SimplePrincipal;

/** A default authorization delegate used by the AuthorizationInterceptor. This
 * looks for a hard coded JBossAdmin role in the current authenticated Subject.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class RolesAuthorization
{
   private HashSet requiredRoles = new HashSet();

   public RolesAuthorization()
   {
      requiredRoles.add(new SimplePrincipal("JBossAdmin"));
   }
   public void setRequiredRoles(HashSet requiredRoles)
   {
      this.requiredRoles = requiredRoles;
   }
   public void authorize(Principal caller, Subject subject,
      String objectname, String opname)
   {
      Set groups = subject.getPrincipals(Group.class);
      Group roles = null;
      Iterator iter = groups.iterator();
      while( iter.hasNext() )
      {
         Group grp = (Group) iter.next();
         if( grp.getName().equals("Roles") )
         {
            roles = grp;
            break;
         }
      }
      if( roles == null )
      {
         throw new SecurityException("Subject has no Roles");
      }

      iter = requiredRoles.iterator();
      boolean hasRole = false;
      while( iter.hasNext() && hasRole == false )
      {
         Principal p = (Principal) iter.next();
         hasRole = roles.isMember(p);
      }
      if( hasRole == false )
      {
         throw new SecurityException("Authorization failure, requiredRoles="+requiredRoles
            +", callerRoles="+roles);
      }
   }
}
