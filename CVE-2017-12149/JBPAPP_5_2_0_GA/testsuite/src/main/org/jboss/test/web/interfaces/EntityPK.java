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
package org.jboss.test.web.interfaces;
import java.net.URL;
import java.security.ProtectionDomain;

public class EntityPK implements java.io.Serializable
{
   static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(EntityPK.class);
   
   public int the_key;
   
   public EntityPK()
   {
   }
   
   public EntityPK(int the_key)
   {
      this.the_key = the_key;
   }
   
   public boolean equals(Object obj)
   {
      boolean equals = false;
      try
      {
         EntityPK key = (EntityPK) obj;
         equals = the_key == key.the_key;
      }
      catch(ClassCastException e)
      {
         log.debug("failed", e);
         // Find the codebase of obj
         ProtectionDomain pd0 = getClass().getProtectionDomain();
         URL loc0 = pd0.getCodeSource().getLocation();
         ProtectionDomain pd1 = obj.getClass().getProtectionDomain();
         URL loc1 = pd1.getCodeSource().getLocation();
         log.debug("PK0 location="+loc0);
         log.debug("PK0 loader="+getClass().getClassLoader());
         log.debug("PK1 location="+loc1);
         log.debug("PK1 loader="+obj.getClass().getClassLoader());
      }
      return equals;
   }
   public int hashCode()
   {
      return the_key;
   }
   
   public String toString()
   {
      return "EntityPK[" + the_key + "]";
   }
   
}
