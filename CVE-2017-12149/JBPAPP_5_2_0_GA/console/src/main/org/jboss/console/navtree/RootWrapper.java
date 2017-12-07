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
package org.jboss.console.navtree;

import java.util.Vector;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.TreeAction;
import org.jboss.console.manager.interfaces.TreeInfo;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.TreeNodeMenuEntry;

/**
 * Default implementation of NodeWrapper for the first level of the tree
 * which aggregates the first level of plugins using the "bootstrap" managed objects
 *
 * @see org.jboss.console.navtree.NodeWrapper
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20 decembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class RootWrapper 
   implements NodeWrapper
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   TreeInfo tree = null;

   NodeWrapper[] sons = null;
   TreeNode[] realSons = null;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
     
   public RootWrapper (TreeInfo tree)
   {
      this.tree = tree;

      // create starting set of subnodes
      //
      Vector nodes = new Vector ();
      ManageableResource[] roots = tree.getRootResources ();
      for (int i=0; i<roots.length; i++)
      {
         ManageableResource mr = roots[i];
         TreeNode[] ns = tree.getTreesForResource (mr);
         if (ns != null && ns.length > 0)
            nodes.addAll (java.util.Arrays.asList (ns));
      }

      realSons = new TreeNode[nodes.size ()];
      sons = new NodeWrapper[nodes.size ()];

      for (int i=0; i<realSons.length; i++) 
         realSons[i] = (TreeNode)nodes.elementAt (i);
               
   }

   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   public Object getChild (int index)
   {
      if (index >= sons.length)
         return null;

      if (sons[index] == null)
         sons[index] = new StdNodeWrapper(realSons[index], tree, "");

      return sons[index];
   }

   public int getChildCount ()
   {
      return this.realSons.length;
   }

   public int getIndexOfChild (Object child)
   {
      for (int i=0; i<this.sons.length; i++)
      {
         if (this.sons[i] == child)
            return i;
      }
      return -1;         
   }

   public boolean isLeaf ()
   {
      return this.sons.length == 0;
   }

   public String getIconUrl ()
   {
      return this.tree.getIconUrl(); 
   }

   public String toString ()
   {
      return "JBoss Management Console";
   }
   
   public TreeAction getAssociatedAction ()
   {
      return this.tree.getHomeAction ();
   }
   
   public String getDescription ()
   {
      return this.tree.getDescription ();
   }
   
   public TreeNodeMenuEntry[] getMenuEntries ()
   {
      return this.tree.getRootMenus(); 
   }
   
   public String getPath ()
   {
      return "";
   }
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}

