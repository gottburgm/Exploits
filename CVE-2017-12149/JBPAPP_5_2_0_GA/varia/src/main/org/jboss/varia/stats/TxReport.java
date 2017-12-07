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
package org.jboss.varia.stats;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class TxReport
   implements Serializable
{
   private static final String DEFAULT_NAME = "UNKNOWN";

   private String name = DEFAULT_NAME;
   private final Map stats = new HashMap();
   private int count = 1;

   public String getName()
   {
      return name;
   }

   public int getCount()
   {
      return count;
   }

   public Map getStats()
   {
      return stats;
   }

   public boolean addItem(StatisticalItem item)
   {
      if(name == DEFAULT_NAME)
      {
         name = item.getValue();
      }

      boolean addedNew = false;
      Map itemMap = (Map) stats.get(item.getName());
      if(itemMap == null)
      {
         itemMap = new HashMap();
         stats.put(item.getName(), itemMap);
         addedNew = true;
      }

      StatisticalItem curItem = (StatisticalItem) itemMap.get(item.getValue());
      if(curItem == null)
      {
         itemMap.put(item.getValue(), item);
      }
      else
      {
         curItem.add(item);
      }
      return addedNew;
   }

   /**
    * This method destroys txReport parameter!!! Not really a nice implementation.
    */
   public void merge(TxReport txReport)
   {
      for(Iterator iter = txReport.stats.entrySet().iterator(); iter.hasNext();)
      {
         Map.Entry entry = (Map.Entry) iter.next();
         String itemName = (String) entry.getKey();

         Map myMap = (Map) stats.get(itemName);
         Map itemMap = (Map) entry.getValue();

         if(myMap == null)
         {
            stats.put(itemName, itemMap);
         }
         else
         {
            // first merge common items
            for(Iterator myItems = myMap.values().iterator(); myItems.hasNext();)
            {
               StatisticalItem myItem = (StatisticalItem) myItems.next();
               StatisticalItem newItem = (StatisticalItem) itemMap.remove(myItem.getValue());

               if(newItem == null)
               {
                  myItem.mergeNull();
               }
               else
               {
                  myItem.merge(newItem);
               }
            }

            // add new items
            if(!itemMap.isEmpty())
            {
               for(Iterator newItems = itemMap.values().iterator(); newItems.hasNext();)
               {
                  StatisticalItem newItem = (StatisticalItem) newItems.next();
                  myMap.put(newItem.getValue(), newItem);
               }
            }
         }
      }

      count += txReport.count;
   }

   // Inner

   public static class MethodStats extends AbstractStatisticalItem
   {
      public static final String NAME = "Method Statistics Per Transaction";

      public MethodStats(String method)
      {
         super(NAME);
         value = method;
      }
   }

   public static class SqlStats extends AbstractStatisticalItem
   {
      public static final String NAME = "SQL Statistics Per Transaction";

      public SqlStats(String sql)
      {
         super(NAME);
         value = sql;
      }
   }
}
