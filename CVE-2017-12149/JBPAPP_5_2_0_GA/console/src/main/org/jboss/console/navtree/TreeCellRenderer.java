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

import java.awt.Component;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Tree cell rendered. Can display another icon if available in the
 * plugin description.
 *
 * @see org.jboss.console.navtree.AdminTreeBrowser
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

public class TreeCellRenderer extends DefaultTreeCellRenderer
{
   protected TreeContext ctx = null;
   protected static HashMap cache = new HashMap ();
   
   public TreeCellRenderer (TreeContext ctx)
   {
      super();
      this.ctx = ctx;
   }

   public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

      super.getTreeCellRendererComponent(
                     tree, value, sel,
                     expanded, leaf, row,
                     hasFocus);
      if (value instanceof NodeWrapper)
      {
         NodeWrapper node = (NodeWrapper)value;
         
         String targetUrl = node.getIconUrl ();
         ImageIcon img = (ImageIcon)cache.get( targetUrl );
         
         if (img != null)
         {
            setIcon (img);
         }
         else
         {
            URL target = null;                                    
            
            try { target = new URL(this.ctx.localizeUrl(targetUrl)); } catch (Exception ignored) {}
            
            if (target != null)
            {
               try
               {
                  img = new ImageIcon(target);
                  cache.put (targetUrl, img);
                  setIcon (img);                  
               }
               catch (Exception tobad) {}
            }
         }
         
         
         String desc = node.getDescription ();
         if (desc != null)
         {
            setToolTipText (desc);
         }
      }

      return this;
   }
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public TreeCellRenderer ()
   {
   }
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
