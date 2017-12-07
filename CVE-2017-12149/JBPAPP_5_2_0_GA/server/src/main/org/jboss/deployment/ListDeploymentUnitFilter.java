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

import java.util.List;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnitFilter;

/**
 * ListDeploymentUnitFilter
 *
 * @author ales.justin@jboss.org
 */
public class ListDeploymentUnitFilter implements VFSDeploymentUnitFilter
{
   private List<VFSDeploymentUnitFilter> filters;

   /**
    * Check filters list.
    */
   public void start()
   {
      if (filters == null || filters.isEmpty())
         throw new IllegalArgumentException("Null or empty filters list.");
   }

   public boolean accepts(VFSDeploymentUnit unit)
   {
      for (VFSDeploymentUnitFilter filter : filters)
      {
         if (filter.accepts(unit) == false)
            return false;
      }
      return true;
   }

   /**
    * Set list of filters.
    *
    * @param filters the filters
    */
   public void setFilters(List<VFSDeploymentUnitFilter> filters)
   {
      this.filters = filters;
   }
}