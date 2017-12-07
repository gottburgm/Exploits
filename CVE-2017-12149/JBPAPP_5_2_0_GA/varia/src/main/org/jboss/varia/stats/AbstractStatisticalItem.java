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


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public abstract class AbstractStatisticalItem
   implements StatisticalItem
{
   protected final String name;
   protected String value;
   private int count = 1;
   private int minCountPerTx = Integer.MAX_VALUE;
   private int maxCountPerTx;
   private int mergedItemsTotal = 1;

   public AbstractStatisticalItem(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public String getValue()
   {
      return value;
   }

   public int getMinCountPerTx()
   {
      return minCountPerTx == Integer.MAX_VALUE ? count : minCountPerTx;
   }

   public int getMaxCountPerTx()
   {
      return maxCountPerTx == 0 ? count : maxCountPerTx;
   }

   public int getCount()
   {
      return count;
   }

   public void add(StatisticalItem item)
   {
      if(!getName().equals(item.getName()))
      {
         throw new IllegalArgumentException("Can't merge statistical items with different names: " +
            getName() + " and " + item.getName());
      }

      this.count += item.getCount();
   }

   public void merge(StatisticalItem item)
   {
      add(item);

      ++mergedItemsTotal;

      int count = item.getCount();
      if(count > maxCountPerTx)
      {
         maxCountPerTx = count;
      }

      if(count < minCountPerTx)
      {
         minCountPerTx = count;
      }
   }

   public void mergeNull()
   {
      //++mergedItemsTotal;
      minCountPerTx = 0;
   }

   public int getMergedItemsTotal()
   {
      return mergedItemsTotal;
   }
}
