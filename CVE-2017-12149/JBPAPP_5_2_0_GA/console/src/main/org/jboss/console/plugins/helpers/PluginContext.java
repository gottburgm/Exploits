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
package org.jboss.console.plugins.helpers;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.TreeNodeMenuEntry;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;

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
 * <p><b>24 dec 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public interface PluginContext
{
   public String localizeUrl (String source);
   
   public org.jboss.logging.Logger getLogger();
   
   public MBeanServer getLocalMBeanServer ();
   public ObjectInstance[] getMBeansForClass (String scope, String className); 


   public TreeNode createTreeNode (String name,
                                            String description,
                                            String iconUrl,
                                            String defaultUrl,
                                            TreeNodeMenuEntry[] menuEntries,
                                            TreeNode[] subNodes,
                                            ResourceTreeNode[] subResNodes) throws Exception;

   public ResourceTreeNode createResourceNode (String name,
                                            String description,
                                            String iconUrl,
                                            String defaultUrl,
                                            TreeNodeMenuEntry[] menuEntries,
                                            TreeNode[] subNodes,
                                            ResourceTreeNode[] subResNodes,
                                            String jmxObjectName,
                                            String jmxClassName) throws Exception;

   public ResourceTreeNode createResourceNode (String name,
                                            String description,
                                            String iconUrl,
                                            String defaultUrl,
                                            TreeNodeMenuEntry[] menuEntries,
                                            TreeNode[] subNodes,
                                            ResourceTreeNode[] subResNodes,
                                            ManageableResource resource) throws Exception;

   public TreeNodeMenuEntry[] createMenus (String[] content) throws Exception;
   
   public String encode (String source);
}
