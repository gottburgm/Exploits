/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import java.util.Collection;
import java.util.HashSet;

/**
 * Metadata to hold information about the JSF managed bean classes present in a deployment.
 *
 * @author Jaikiran Pai
 */
public class JSFDeployment
{
   /**
    * Collection of fully qualified class names of JSF managed beans
    */
   private Collection<String> managedBeans = new HashSet<String>();

   /**
    * Returns a collection of fully qualified classnames of JSF managed beans
    * belong to this JSF deployment
    *
    * @return
    */
   public Collection<String> getManagedBeans()
   {
      return this.managedBeans;
   }

   /**
    * Adds the passed <code>managedBeanClass</code> to the collection of managed bean class names.
    *
    * @param managedBeanClass The fully qualified classname of the JSF managed bean.
    * @throws IllegalArgumentException If the passed <code>managedBeanClass</code> is null or an empty string.
    */
   public void addManagedBean(String managedBeanClass)
   {
      if (managedBeanClass == null || managedBeanClass.trim().isEmpty())
      {
         throw new IllegalArgumentException("Managed bean class cannot be null or empty string");
      }
      this.managedBeans.add(managedBeanClass);
   }
}
