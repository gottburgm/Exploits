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
package org.jboss.deployment;

import java.net.URL;
import java.util.Comparator;

import org.jboss.util.NullArgumentException;

/**
 * A helper class for sorting deployments.
 *
 * @version <tt>$Revision: 81033 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 */
public class DeploymentSorter implements Comparator, DefaultDeploymentSorter
{
   /**
    * The default order for sorting deployments; this has been deprecated,
    * order is really defined in SuffixOrderHelper class and/or individualy
    * within each subdeployer.
    * 
    * @deprecated
    */
   public static final String[] DEFAULT_SUFFIX_ORDER = {
       ".deployer", "-deployer.xml", ".sar", "-service.xml", ".rar", "-ds.xml",
       ".har", ".jar", ".war", ".wsr", ".ear", ".zip", ".bsh", ".last"
   };

   protected String[] suffixOrder;

   public DeploymentSorter(String[] suffixOrder)
   {
      if (suffixOrder == null)
         throw new NullArgumentException("suffixOrder");

      this.suffixOrder = suffixOrder;
   }

   public DeploymentSorter()
   {
      this(DEFAULT_SUFFIX_ORDER);
   }
   
   public String[] getSuffixOrder()
   {
      return suffixOrder;
   }
   public void setSuffixOrder(String[] suffixOrder)
   {
      this.suffixOrder = suffixOrder;
   }

   /**
    * Return a negative number if o1 appears lower in the the suffix order than
    * o2.
    * If the suffixes are indentical, then sorts based on name.
    * This is so that deployment order of components is always identical.
    */
   public int compare(Object o1, Object o2) 
   {
      URL u1 = (URL)o1;
      URL u2 = (URL)o2;
      int order = getExtensionIndex(u1) - getExtensionIndex(u2);
      if (order != 0)
          return order;
      return u1.getFile().compareTo(u2.getFile());
   }
   
   /**
    * Return the index that matches this url
    */
   public int getExtensionIndex(URL url)
   {
      String path = url.getPath();
      if (path.endsWith("/")) 
          path = path.substring(0, path.length() - 1);
      int i = 0;
      for (; i < suffixOrder.length; i++)
      {
          if (path.endsWith(suffixOrder[i]))
              break;
      }
      return i;
   }
}
