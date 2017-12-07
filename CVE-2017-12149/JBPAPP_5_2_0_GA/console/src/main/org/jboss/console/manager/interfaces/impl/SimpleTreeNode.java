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
 * <p><b>31 dec 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class SimpleTreeNode implements TreeNode
{
   
   protected String name = null;
   protected String description = null;
   protected String icon = null;
   protected TreeAction action = null;
   protected TreeNodeMenuEntry[] menuEntries = null;
   protected TreeNode[] subNodes = null;
   protected ResourceTreeNode[] nodeManagableResources = null;
   protected boolean isMaster = false;
   
   public SimpleTreeNode (){ super(); }
   
   public SimpleTreeNode (String name,
                           String description,
                           String icon,
                           TreeAction action,
                           TreeNodeMenuEntry[] menuEntries,
                           TreeNode[] subNodes,
                           ResourceTreeNode[] nodeManagableResources)
   {  
      this.name = name;
      this.description = description;
      this.icon = icon;
      this.action = action;
      this.menuEntries = menuEntries;
      this.subNodes = subNodes;
      this.nodeManagableResources = nodeManagableResources;      
   }
   

   public String getName()
   {
      return this.name;
   }

   public String getDescription()
   {
      return this.description;
   }

   public String getIcon()
   {
      return this.icon;
   }

   public TreeAction getAction()
   {
      return this.action;
   }

   public TreeNodeMenuEntry[] getMenuEntries()
   {
      return this.menuEntries;
   }

   public TreeNode[] getSubNodes()
   {
      return this.subNodes;
   }

   public ResourceTreeNode[] getNodeManagableResources()
   {
      return this.nodeManagableResources;
   }
   
   public boolean isMasterNode ()
   {
      return this.isMaster;
   }

   public TreeNode setMasterNode (boolean master)
   {
      this.isMaster = master;
      return this;
   }

}
