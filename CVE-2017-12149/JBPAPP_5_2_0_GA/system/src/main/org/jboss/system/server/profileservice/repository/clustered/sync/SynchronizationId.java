/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.system.server.profileservice.repository.clustered.sync;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unique id for a set of changes needed to synchronize a node's
 * repository content with the cluster.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 * 
 * @param T the type of the originator
 */
public class SynchronizationId<T extends Serializable> implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -604832735573100571L;
   
   private static final long vm_base = System.currentTimeMillis();
   private static final AtomicInteger count = new AtomicInteger();
   
   private final T originator;
   private final long timestamp = vm_base;
   private final int index = count.incrementAndGet();
   
   public SynchronizationId(T originator)
   {
      if (originator == null)
      {
         throw new IllegalArgumentException("Null originator");
      }
      this.originator = originator;
   }

   public T getOriginator()
   {
      return originator;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      
      if (obj instanceof SynchronizationId)
      {
         SynchronizationId<?> other = (SynchronizationId<?>) obj;
         return this.index == other.index 
                  && this.timestamp == other.timestamp 
                  && this.originator.equals(other.originator);
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + index;
      result = 31 * result + ((int) (timestamp ^ (timestamp >>>32)));
      result = 31 * result + originator.hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      return new StringBuilder(getClass().getName())
      .append("[originator='")
      .append(originator)
      .append(",timestamp=")
      .append(timestamp)
      .append(",index=")
      .append(index)
      .append(']').toString();
   }
   
   
}
