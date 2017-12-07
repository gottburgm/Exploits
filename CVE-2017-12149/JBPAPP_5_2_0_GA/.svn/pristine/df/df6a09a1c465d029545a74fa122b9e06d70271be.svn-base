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
package org.jboss.aspects.versioned;
import java.lang.ref.WeakReference;

import org.jboss.aop.InstanceAdvised;
import org.jboss.util.id.GUID;

public class VersionReference implements java.io.Serializable
{
   private static final long serialVersionUID = 1567486678025136095L;

   private GUID guid;
   private transient WeakReference advisedRef;

   public VersionReference() {}

   public VersionReference(GUID guid)
   {
      this(guid, null);
   }
   public VersionReference(GUID guid, InstanceAdvised obj)
   {
      System.out.println("VERSION REFERENCE CREATION: " + guid);
      this.guid = guid;
      set(obj);
   }
   public void set(InstanceAdvised obj) { this.advisedRef = new WeakReference(obj); }
   public InstanceAdvised get() 
   {
      if (advisedRef != null)
      {
         return (InstanceAdvised)advisedRef.get();
      }
      return null;
   }
   public GUID getGUID() { return guid; }
}
