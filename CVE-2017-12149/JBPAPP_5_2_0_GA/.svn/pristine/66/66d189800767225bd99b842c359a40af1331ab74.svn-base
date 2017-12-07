/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.deployers;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.helpers.DefaultDeploymentContextComparator;

/**
 * Legacy deployment sorter
 *
 * @author ales.justin@jboss.org
 */
public class LegacyDeploymentContextComparator extends DefaultDeploymentContextComparator
{
   /** The instance */
   public static final LegacyDeploymentContextComparator INSTANCE = new LegacyDeploymentContextComparator();
   /** Legacy orders */
   private static Map<String, Integer> legacyOrder;

   static
   {
      legacyOrder = new HashMap<String, Integer>();
      legacyOrder.put(".deployer", 50);
      legacyOrder.put("-deployer.xml", 50);
      legacyOrder.put(".aop", 100);
      legacyOrder.put("-aop.xml", 100);
      legacyOrder.put(".sar", 150);
      legacyOrder.put("-service.xml", 150);
      legacyOrder.put(".beans", 200);
      legacyOrder.put("-jboss-beans.xml", 200);
      legacyOrder.put(".rar", 250);
      legacyOrder.put("-ds.xml", 300);
      legacyOrder.put(".har", 350);
      legacyOrder.put(".jar", 400);
      legacyOrder.put(".ejb3", 400);
      legacyOrder.put(".par", 400);
      legacyOrder.put(".war", 500);
      legacyOrder.put(".wsr", 600);
      legacyOrder.put(".ear", 650);
      legacyOrder.put(".zip", 750);
      legacyOrder.put(".bsh", 800);
      legacyOrder.put(".last", 900);
   }

   private boolean useDefaults = true;
   private Map<String, Integer> suffixOrder;
   private Map<String, Integer> orderMap;
   private int defaultOrder = 850;

   /**
    * Get the instance.
    *
    * @return the instance
    */
   public static LegacyDeploymentContextComparator getInstance()
   {
      return INSTANCE;
   }

   public int compare(DeploymentContext fst, DeploymentContext snd)
   {
      int fstOrder = getContextOrder(fst);
      int sndOrder = getContextOrder(snd);
      int diff = fstOrder - sndOrder;
      if (diff != 0)
         return diff;
      else
         return super.compare(fst, snd);
   }

   /**
    * Get context's order.
    *
    * @param context the deployment context
    * @return context's order, or default if no match
    */
   protected int getContextOrder(DeploymentContext context)
   {
      String simpleName = context.getSimpleName();
      for (Map.Entry<String, Integer> entry : getOrderMap().entrySet())
      {
         if (simpleName.endsWith(entry.getKey()))
            return entry.getValue();
      }
      return defaultOrder;
   }

   protected Map<String, Integer> getOrderMap()
   {
      if (orderMap == null)
         orderMap = createOrderMap();

      return orderMap;
   }

   public void create()
   {
      // do nothing -- back compatibility purpose only
   }

   /**
    * Create order map.
    *
    * Legacy mappings first,
    * then override it with custom suffix order.
    *
    * @return the new order map
    */
   public Map<String, Integer> createOrderMap()
   {
      Map<String, Integer> map = new HashMap<String, Integer>();
      if (useDefaults)
         map.putAll(legacyOrder);
      if (suffixOrder != null)
         map.putAll(suffixOrder);
      return map;
   }

   /**
    * Should we use defaults.
    *
    * @param useDefaults default flag
    */
   public void setUseDefaults(boolean useDefaults)
   {
      this.useDefaults = useDefaults;
   }

   /**
    * Set suffix order map.
    *
    * @param suffixOrder the suffix order map
    */
   public void setSuffixOrder(Map<String, Integer> suffixOrder)
   {
      this.suffixOrder = suffixOrder;
   }

   /**
    * Set default order.
    *
    * @param defaultOrder the default order
    */
   public void setDefaultOrder(int defaultOrder)
   {
      this.defaultOrder = defaultOrder;
   }

   Object readResolve()
   {
      return INSTANCE;
   }
}