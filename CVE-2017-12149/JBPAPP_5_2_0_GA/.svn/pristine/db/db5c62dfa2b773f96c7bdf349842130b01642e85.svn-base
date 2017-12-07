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
package org.jboss.test.cluster.hapartition.ds;

import java.io.Serializable;
import java.util.Collection;

/** The public DistributedStateUser interface
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public interface IDistributedState
{
   public static class NotifyData implements Serializable
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -7125734195566140252L;
      
      public String category;
      public Serializable key;
      public Serializable value;
      public boolean locallyModified;
      public String toString()
      {
         StringBuffer tmp = new StringBuffer();
         tmp.append("category:");
         tmp.append(category);
         tmp.append(", key:");
         tmp.append(key);
         tmp.append(", value:");
         tmp.append(value);
         tmp.append(", locallyModified:");
         tmp.append(locallyModified);
         return tmp.toString();
      }
   }
   public Serializable get(Serializable key);
   public void put(Serializable key, Serializable value)
      throws Exception;
   public void remove(Serializable key)
      throws Exception;
   
   public Collection listAllCategories();
   public Collection listAllKeys(String category);
   public Collection listAllValues(String category);

   public void flush();
   public int size();

}
