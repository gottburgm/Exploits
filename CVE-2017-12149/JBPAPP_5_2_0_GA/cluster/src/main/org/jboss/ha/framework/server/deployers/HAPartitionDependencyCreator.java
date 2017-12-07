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

package org.jboss.ha.framework.server.deployers;

/**
 * Policy for creating an MC bean name from a partition name. Bean name
 * can be added as a dependency to metadata for deployments that use
 * the specified partition.
 * 
 * @author Brian Stansberry
 *
 */
public interface HAPartitionDependencyCreator
{
   /**
    * Gets the MC bean name upon which a bean using the given partition
    * can depend.
    * 
    * @param partitionName the partition name. Cannot be <code>null</code>
    * @return the bean name
    */
   String getHAPartitionDependencyName(String partitionName);
}
