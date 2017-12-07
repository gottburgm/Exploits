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
package org.jboss.test.cluster.hapartition.drm;

import java.io.Serializable;
import java.util.List;

/** The public DistributedReplicantManager user service interface
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public interface IReplicants
{
   public static class NotifyData implements Serializable
   {
      static final long serialVersionUID = -1698998375571817852L;
      public Serializable key;
      public List newReplicants;
      public int newReplicantsViewId;
      public String toString()
      {
         StringBuffer tmp = new StringBuffer();
         tmp.append("key:");
         tmp.append(key);
         tmp.append(", newReplicants:");
         tmp.append(newReplicants);
         return tmp.toString();
      }
   }

   public Serializable lookupLocalReplicant();
   public Serializable lookupLocalReplicant(String key);
   public List lookupReplicants();
   public List lookupReplicants(String key);
   public void add(String key, Serializable data)
      throws Exception;
   public void remove(String key)
      throws Exception;
}
