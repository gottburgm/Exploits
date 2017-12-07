/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.client;

import java.util.Collections;
import java.util.Set;

import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.javaee.JavaEEComponent;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class NoopDependencyPolicy
   extends JBoss5DependencyPolicy
{

   public NoopDependencyPolicy(JavaEEComponent component)
   {
      super(component);
   }

   @Override
   public void addDependency(Class<?> businessInterface)
   {
   }

   @Override
   public void addDependency(DemandMetaData dependency)
   {
   }

   @Override
   public void addDependency(DependencyMetaData dependency)
   {
   }

   @Override
   public void addDependency(String ejbLink, Class<?> businessInterface)
   {
   }

   @Override
   public void addDependency(String dependency)
   {
   }

   @Override
   public void addJNDIName(String name)
   {
   }

   @Override
   public void addSupply(Class<?> businessInterface)
   {
   }

   @Override
   public Set<DemandMetaData> getDemands()
   {
      return Collections.emptySet();
   }

   @Override
   public Set<DependencyMetaData> getDependencies()
   {
      return Collections.emptySet();
   }

   @Override
   public Set<SupplyMetaData> getSupplies()
   {
      return Collections.emptySet();
   }

}
