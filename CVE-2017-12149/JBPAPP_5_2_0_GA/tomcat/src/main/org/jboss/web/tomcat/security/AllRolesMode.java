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
package org.jboss.web.tomcat.security;

/**
 * An enum for the mode for handling the role-name=* security authorization
 * mode.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
public class AllRolesMode
{
   private String name;
   /** Use the strict servlet spec interpretation which requires that the user
    * have one of the web-app/security-role/role-name 
    */
   public static final AllRolesMode STRICT_MODE = new AllRolesMode("strict");
   /** Allow any authenticated user
    */
   public static final AllRolesMode AUTH_ONLY_MODE = new AllRolesMode("authOnly");
   /** Allow any authenticated user only if there are no web-app/security-roles
    */
   public static final AllRolesMode STRICT_AUTH_ONLY_MODE = new AllRolesMode("strictAuthOnly");

   static AllRolesMode toMode(String name)
   {
      AllRolesMode mode;
      if( name.equalsIgnoreCase(STRICT_MODE.name) )
         mode = STRICT_MODE;
      else if( name.equalsIgnoreCase(AUTH_ONLY_MODE.name) )
         mode = AUTH_ONLY_MODE;
      else if( name.equalsIgnoreCase(STRICT_AUTH_ONLY_MODE.name) )
         mode = STRICT_AUTH_ONLY_MODE;
      else
         throw new IllegalStateException("Unknown mode, must be one of: strict, authOnly, strictAuthOnly");
      return mode;
   }

   private AllRolesMode(String name)
   {
      this.name = name;
   }

   public boolean equals(Object o)
   {
      boolean equals = false;
      if( o instanceof AllRolesMode )
      {
         AllRolesMode mode = (AllRolesMode) o;
         equals = name.equals(mode.name);
      }
      return equals;
   }
   public int hashCode()
   {
      return name.hashCode();
   }
   public String toString()
   {
      return "AllRolesMode("+name+")";
   }
}
