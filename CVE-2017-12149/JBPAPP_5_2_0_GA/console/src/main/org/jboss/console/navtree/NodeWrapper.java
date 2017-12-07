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

import org.jboss.console.manager.interfaces.TreeAction;
import org.jboss.console.manager.interfaces.TreeNodeMenuEntry;

/**
 * Base interface for wrapper objects used to represent tree nodes
 *
 * @see org.jboss.console.navtree.ConsoleTreeModel
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

public interface NodeWrapper
{
   public Object getChild (int index);      
   public int getChildCount ();
   public int getIndexOfChild (Object child);
   public boolean isLeaf ();

   public String getIconUrl ();
   public TreeAction getAssociatedAction();
   public String getDescription ();
   public TreeNodeMenuEntry[] getMenuEntries ();
   
   public String getPath ();
}
