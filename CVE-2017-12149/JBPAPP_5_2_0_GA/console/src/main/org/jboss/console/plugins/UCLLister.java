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
package org.jboss.console.plugins;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;

import javax.management.ObjectInstance;

/**
 * As the number of UCL can be very big, we use a real Java class which is far
 * faster than beanshell
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class UCLLister 
   extends AbstractPluginWrapper
{

   public UCLLister () { super(); }
   
   ResourceTreeNode createUCLSubResource (ObjectInstance instance) throws Exception
   {
      String uclName = instance.getObjectName().getKeyProperty ("UCL");
            
      return createResourceNode ( 
            "UCL " + uclName, // name
            "UCL with id " + uclName, // description
            "images/service.gif", // Icon URL
            "/jmx-console/HtmlAdaptor?action=inspectMBean&name=" + encode(instance.getObjectName().toString()), // Default URL
            null,
            null,
            null,
            instance.getObjectName().toString(),
            instance.getClassName () );
   }
   
   ResourceTreeNode[] createUCLSubResources ()  throws Exception
   {
      ObjectInstance[] insts = 
         getMBeansForClass("jmx.loading:*", 
            "org.jboss.mx.loading.UnifiedClassLoader3");
      
      ResourceTreeNode[] result = new ResourceTreeNode[insts.length];
      for (int i=0; i<result.length; i++)
      {
         result[i] = createUCLSubResource (insts[i]);
      }
      
      return result;                  
   }
   
   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         return createTreeNode (
               "Unified ClassLoaders", // name
               "Display all JBoss UCLs", // description
               "images/recycle.gif", // Icon URL
               null, // Default URL
               null,
               null, // sub nodes
               createUCLSubResources ()   // Sub-Resources                  
            );            
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         return null;
      }
   }

  
}
