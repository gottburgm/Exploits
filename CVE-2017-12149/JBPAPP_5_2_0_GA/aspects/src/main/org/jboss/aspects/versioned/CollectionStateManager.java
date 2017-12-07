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

import org.jboss.util.id.GUID;

import java.lang.reflect.Method;
import java.util.HashMap;



/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public abstract class CollectionStateManager extends StateManager implements java.io.Externalizable
{
   protected HashMap methodMap;

   protected CollectionStateManager() {}

   protected CollectionStateManager(GUID guid, long timeout, HashMap methodMap)
   {
      super(guid, timeout);
      this.methodMap = methodMap;
   }

   public abstract HashMap getMethodMap();

   public Method isManagerMethod(Method method)
   {
      try
      {
         long hash = org.jboss.aop.util.MethodHashing.methodHash(method);
         return (Method)methodMap.get(new Long(hash));
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }
   public Method isManagerMethod(long hash)
   {
      return (Method)methodMap.get(new Long(hash));
   }

   public void writeExternal(java.io.ObjectOutput out)
      throws java.io.IOException
   {
      super.writeExternal(out);
   }

   public void readExternal(java.io.ObjectInput in)
      throws java.io.IOException, ClassNotFoundException
   {
      super.readExternal(in);
   }
}
