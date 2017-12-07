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
package org.jboss.console.manager.interfaces.impl;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeAction;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.TreeNodeMenuEntry;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>31 d�c. 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class SimpleResourceTreeNode
   extends SimpleTreeNode
   implements ResourceTreeNode
{

   protected ManageableResource resource = null;
   protected int visibility = ResourceTreeNode.ALWAYS_VISIBLE;

   public SimpleResourceTreeNode()
   {
      super();
   }

   public SimpleResourceTreeNode(
      String name,
      String description,
      String icon,
      TreeAction action,
      TreeNodeMenuEntry[] menuEntries,
      TreeNode[] subNodes,
      ResourceTreeNode[] nodeManagableResources)
   {
      super(
         name,
         description,
         icon,
         action,
         menuEntries,
         subNodes,
         nodeManagableResources);
   }

   public SimpleResourceTreeNode(
      String name,
      String description,
      String icon,
      TreeAction action,
      TreeNodeMenuEntry[] menuEntries,
      TreeNode[] subNodes,
      ResourceTreeNode[] nodeManagableResources,
      ManageableResource resource)
   {
      super(
         name,
         description,
         icon,
         action,
         menuEntries,
         subNodes,
         nodeManagableResources);
      
      this.resource = resource;
   }

   public ManageableResource getResource()
   {
      return this.resource;
   }


   public int getVisibility()
   {
      return visibility;
   }

   public ResourceTreeNode setVisibility(int visibility)
   {
      this.visibility = visibility;
      return this;
   }

}
