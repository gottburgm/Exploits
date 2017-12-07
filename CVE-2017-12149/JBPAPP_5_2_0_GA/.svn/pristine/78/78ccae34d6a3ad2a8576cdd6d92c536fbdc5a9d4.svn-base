/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009 Red Hat, Inc. and individual contributors
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

package org.jboss.test.cluster.testutil;

import java.util.List;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheSPI;

/**
 * Utilities related to dealing with JBoss Cache.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class JBossCacheUtil
{

   /**
    * Loops, continually calling {@link #areCacheViewsComplete(org.jboss.cache.Cache[])}
    * until it either returns true or <code>timeout</code> ms have elapsed.
    *
    * @param caches  caches which must all have consistent views
    * @param timeout max number of ms to loop
    * @throws RuntimeException if <code>timeout</code> ms have elapse without
    *                          all caches having the same number of members.
    */
   public static void blockUntilViewsReceived(Cache[] caches, long timeout)
   {
      long failTime = System.currentTimeMillis() + timeout;

      while (System.currentTimeMillis() < failTime)
      {
         sleepThread(100);
         if (areCacheViewsComplete(caches))
         {
            return;
         }
      }

      throw new RuntimeException("timed out before caches had complete views" + views(caches));
   }

   /**
    * Checks each cache to see if the number of elements in the array
    * returned by {@link CacheSPI#getMembers()} matches the size of
    * the <code>caches</code> parameter.
    *
    * @param caches caches that should form a View
    * @return <code>true</code> if all caches have
    *         <code>caches.length</code> members; false otherwise
    * @throws IllegalStateException if any of the caches have MORE view
    *                               members than caches.length
    */
   public static boolean areCacheViewsComplete(Cache[] caches)
   {
      return areCacheViewsComplete(caches, true);
   }

   public static boolean areCacheViewsComplete(Cache[] caches, boolean barfIfTooManyMembers)
   {
      int memberCount = caches.length;

      for (int i = 0; i < memberCount; i++)
      {
         if (!isCacheViewComplete(caches[i], memberCount, barfIfTooManyMembers))
         {
            return false;
         }
      }

      return true;
   }

   public static boolean isCacheViewComplete(Cache c, int memberCount)
   {
      return isCacheViewComplete(c, memberCount, true);
   }

   public static boolean isCacheViewComplete(Cache cache, int memberCount, boolean barfIfTooManyMembers)
   {
      List members = cache.getMembers();
      if (members == null || memberCount > members.size())
      {
         return false;
      }
      else if (memberCount < members.size())
      {
         if (barfIfTooManyMembers)
         {
            // This is an exceptional condition
            StringBuilder sb = new StringBuilder("Cache at address ");
            sb.append(cache.getLocalAddress());
            sb.append(" had ");
            sb.append(members.size());
            sb.append(" members; expecting ");
            sb.append(memberCount);
            sb.append(". Members were (");
            for (int j = 0; j < members.size(); j++)
            {
               if (j > 0)
               {
                  sb.append(", ");
               }
               sb.append(members.get(j));
            }
            sb.append(')');

            throw new IllegalStateException(sb.toString());
         }
         else return false;
      }

      return true;
   }


   /**
    * Puts the current thread to sleep for the desired number of ms, suppressing
    * any exceptions.
    *
    * @param sleeptime number of ms to sleep
    */
   public static void sleepThread(long sleeptime)
   {
      try
      {
         Thread.sleep(sleeptime);
      }
      catch (InterruptedException ie)
      {
      }
   }

   private static String views(Cache... caches)
   {
      StringBuilder builder = new StringBuilder("[\n");
      for (Cache c:caches)
      {
         builder.append("   ").append(c.getLocalAddress()).append("->").append(c.getMembers()).append("\n");
      }
      builder.append("]");
      return builder.toString();
   }

   /**
    * Prevent instantiation 
    */
   private JBossCacheUtil()
   {
      // TODO Auto-generated constructor stub
   }

}
