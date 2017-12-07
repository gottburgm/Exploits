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

//$Id: DummyPortalPermission.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBPORTAL-565: Create Testcase for JACC Usage
 *  A Dummy permission to be plugged into a lazy permission collection
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 18, 2006 
 *  @version $Revision: 81036 $
 */
public class DummyPortalPermission extends Permission
{   
   private String actions = null;
   
   public DummyPortalPermission(String uri)
   { 
      super(uri); 
   } 
   
   public DummyPortalPermission(String uri, String actions)
   { 
      super(uri); 
      this.actions = actions;
   } 
 
   public PermissionCollection newPermissionCollection()
   { 
      return new LazyPermissionCollection();
   } 
   
   public boolean implies(Permission perm)
   {
      if(perm instanceof DummyPortalPermission)
         return perm.getName().equals(this.getName());
      return false;
   }
 
   public boolean equals(Object arg0)
   { 
      return false;
   }

    
   public int hashCode()
   { 
      return 0;
   } 
    
   public String getActions()
   { 
      return this.actions;
   } 
}
