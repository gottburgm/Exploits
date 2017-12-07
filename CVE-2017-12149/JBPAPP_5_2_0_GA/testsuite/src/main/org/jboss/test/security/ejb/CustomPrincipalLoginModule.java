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
import javax.security.auth.login.LoginException;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

/** Test of installing a custom principal via a login module.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CustomPrincipalLoginModule extends UsernamePasswordLoginModule
{
   private CustomPrincipalImpl caller;

   public boolean login() throws LoginException
   {
      if (super.login())
      {
         caller = new CustomPrincipalImpl(getUsername());
         return true;
      }
      return false;
   }

   protected Principal getIdentity()
   {
      Principal identity = caller;
      if( identity == null )
         identity = super.getIdentity();
      return identity;
   }

   protected Group[] getRoleSets() throws LoginException
   {
      try
      {
         // The declarative permissions
         Group roles = new SimpleGroup("Roles");
         // The caller identity
         Group callerPrincipal = new SimpleGroup("CallerPrincipal");
         Group[] groups = {roles, callerPrincipal};
         log.info("Getting roles for user=" + getUsername());
         // Add the Echo role
         roles.addMember(new SimplePrincipal("Echo"));
         // Add the custom principal for the caller
         callerPrincipal.addMember(caller);
         return groups;
      }
      catch (Exception e)
      {
         log.error("Failed to obtain groups for user=" + getUsername(), e);
         throw new LoginException(e.toString());
      }
   }

   protected String getUsersPassword()
   {
      return "theduke";
   }

}