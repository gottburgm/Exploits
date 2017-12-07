/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployment.dependency;

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.util.JBossStringBuilder;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ContainerDependencyItem extends AbstractDependencyItem
   implements DependencyItem
{
   /**
    * Create a dependency on a container.
    * 
    * @param containerName - the name of the target container the item depends on
    * @param componentID - the 
    * @param whenRequired
    */
   public ContainerDependencyItem(Object containerName,
         String componentID, ControllerState whenRequired)
   {
      super(componentID, containerName, whenRequired, null);
   }

   @Override
   public boolean resolve(Controller controller)
   {
      boolean resolved = super.resolve(controller);
      setResolved(resolved);
      return isResolved();
   }

   @Override
   public void toString(JBossStringBuilder buffer)
   {
      super.toString(buffer);
      buffer.append(" depend=").append(getName());
   }
   
   @Override
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(getName()).append(" depend ").append(getName());
   }

   @Override
   public String toHumanReadableString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("JndiDepends: '");
      builder.append(getName());
      builder.append("'");
      return builder.toString();
   }

}
