/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.spi;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * An spi for mapping mbean names to a deployment name.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 88974 $
 */
public interface MBeanDeploymentNameBuilder
{
   /**
    * Generate a deployment name from a given mbean name.
    * 
    * @param name - an mbean name of a ManagedObject to include in the deployment
    * @param server - the MBeanServer under which name is registered
    * @return - the name of the deployment the mbean should belong to
    */
   public String getName(ObjectName name, MBeanServer server);
}
