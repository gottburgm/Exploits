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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * A permission  collection to cache portal object permissions.
 * <p>The <code>PermissionCollection</code> is used by the JACC Policy to cache permissions for a given PolicyContext.</p>
 *
 * @author <a href="mailto:mholzner@novell.com">Martin Holzner</a>
 * @version $Revision: 81036 $
 */
public final class InstancePermissionCollection extends PermissionCollection implements Serializable
{
   private static Logger log = Logger.getLogger(InstancePermissionCollection.class);
   private final Vector permissions;
   private boolean trace;

   public InstancePermissionCollection()
   {
      trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("creating portal object permission collection...");
      }

      permissions = new Vector();
   }

   public void add(Permission permission)
   {
      if (trace)
      {
         log.trace("adding " + permission);
      }

      if (!(permission instanceof InstancePermission))
      {
         throw new IllegalArgumentException();
      }
      permissions.add(permission);
   }

   public boolean implies(Permission permission)
   {
      if (trace)
      {
         log.trace("implies ? " + permission);
      }
      if (!(permission instanceof InstancePermission))
      {
         return false;
      }

      Iterator permIterator = permissions.iterator();
      while (permIterator.hasNext())
      {
         InstancePermission p = (InstancePermission)permIterator.next();
         if (p.implies(permission))
         {
            return true;
         }
      }

      return false;
   }

   public Enumeration elements()
   {
      return permissions.elements();
   }
}
