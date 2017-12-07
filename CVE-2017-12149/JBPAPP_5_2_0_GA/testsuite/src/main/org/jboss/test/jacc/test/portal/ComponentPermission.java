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

import org.jboss.logging.Logger; 

import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.StringTokenizer;

/**
 * Portal permission class for authorisation checks.
 * <p>This class is an extension to the JACC mechanism to allow configurable and dynamically modifiable permissions.</p>
 * <p/>
 * Note: for now this is a final class. We might want to have subclasses of it later though.
 *
 * @author <a href="mailto:mholzner@novell.com">Martin Holzner</a>
 * @version $Revision: 81036 $
 */
public final class ComponentPermission extends PortalPermission implements Serializable
{

   private static Logger log = Logger.getLogger(ComponentPermission.class);

   /**
    * The view action.
    */
   public static final String VIEW = "view";

   private static final int VIEW_MASK = 0x00000001;

   private final String uri;

   private final int mask;

   private final String actions;


   private boolean trace;

   /**
    * Create a permission for the specified resource.
    *
    * @param uri handle of the resource that is being protected.
    * @param actions the allowed actions (or the actions to check for access) as a comma separated list
    * @throws IllegalArgumentException if the provided arguments are null or the actions string
    *                                  doesn't contain any valid actions
    */
   public ComponentPermission(String uri, String actions)
   {
      super(uri);

      //
      if (uri == null || actions == null)
      {
         throw new IllegalArgumentException("Arguments must not be null [" + uri + "][" + actions + "]");
      }

      //
      int mask = 0;
      StringTokenizer tokens = new StringTokenizer(actions, ",");
      while (tokens.hasMoreTokens())
      {
         String action = tokens.nextToken().trim();

         if (VIEW.equals(action))
         {
            mask |= VIEW_MASK;
         }
         else
         {
            log.warn("Unknown action in string [" + action + "] will be ignored");
         }
      }

      //
      this.mask = mask;
      this.uri = uri;
      this.actions = actions;
      this.trace = log.isTraceEnabled();
   }

   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      final ComponentPermission that = (ComponentPermission)o;

      if (!uri.equals(that.uri))
      {
         return false;
      }

      if (that.mask != this.mask)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = mask;
      result = 29 * result + uri.hashCode();
      return result;
   }

   public String toString()
   {
      return "ComponentPermission[" + uri + "] [" + actions + "]";
   }

   public String getActions()
   {
      return actions;
   }

   public String getURI()
   {
      return uri;
   }

   public boolean implies(Permission permission)
   {
      if (trace)
      {
         log.trace("implies ? " + getURI() + ": [" + permission + "]");
      }
      if (permission instanceof ComponentPermission)
      {
         ComponentPermission other = (ComponentPermission)permission;
         return uri.equals(other.uri) && (this.mask & other.mask) == other.mask;
      }
      else
      {
         return false;
      }
   }

   public PermissionCollection newPermissionCollection()
   {
      return new ComponentPermissionCollection();
   }

   public String getType()
   {
      return "component";
   }
}
