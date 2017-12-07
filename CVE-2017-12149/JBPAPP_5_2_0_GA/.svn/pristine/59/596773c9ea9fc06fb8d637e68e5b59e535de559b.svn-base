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

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.impl.MBeanResource;
import org.jboss.console.manager.interfaces.impl.SimpleTreeNode;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;

/**
 *
 * 
 */
public class JMSLister extends AbstractPluginWrapper
{
   private static final long serialVersionUID = -2428954274429502892L;

   protected final static String JMX_JSR77_DOMAIN = "jboss.management.local";

   public JMSLister()
   {
      super();
   }

   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         ObjectName objName = ((MBeanResource) resource).getObjectName();
         SimpleTreeNode node = createTreeNode(objName.getKeyProperty("name"), // name
               "", // description
               "images/spirale.gif", // Icon URL
               null, // Default URL									
               null, createDestinations(), // sub nodes 
               null // Sub-Resources                 
         );
         node.setMasterNode(true);
         return node;

      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println(checker);
         return null;

      }
   }

   private TreeNode[] createDestinations() throws Exception
   {
      TreeNode[] destinations = new TreeNode[2];

      destinations[0] = createTreeNode("Queues", "", "images/spirale.gif", null, null, null,
            createDestinationItems("Queue"));
      destinations[1] = createTreeNode("Topics", "", "images/spirale.gif", null, null, null,
            createDestinationItems("Topic"));

      return destinations;
   }

   /**
    * 
    * @return
    * @throws Exception
    */
   private ResourceTreeNode[] createDestinationItems(String type) throws Exception
   {
      ObjectInstance[] insts = getMBeansForQuery("jboss.mq.destination:service=" + type + ",*", null);
      ResourceTreeNode[] destinations = new ResourceTreeNode[insts.length];
      //JMSDestinationManager jmsServer = (JMSDestinationManager)this.mbeanServer.getAttribute(new ObjectName("jboss.mq:service=DestinationManager"), "Interceptor");
      for (int i = 0; i < insts.length; i++)
      {
         ObjectName objName = insts[i].getObjectName();
         destinations[i] = createDestinationItem(objName);
      }
      return destinations;
   }

   /**
    * @param objName
    * @return
    */
   private ResourceTreeNode createDestinationItem(ObjectName objName) throws Exception
   {
      String destinationName = objName.getKeyProperty("name");
      String type = objName.getKeyProperty("service");
      String className = this.mbeanServer.getMBeanInfo(objName).getClassName();
      String fileName = "";
      if (type.equalsIgnoreCase("Queue"))
      {
         fileName = "Queue.jsp";
      }
      else if (type.equalsIgnoreCase("Topic"))
      {
         fileName = "Topic.jsp";
      }

      ResourceTreeNode item = this.createResourceNode(destinationName, type, //Description Tooltip
            "images/serviceset.gif", fileName + "?ObjectName=" + encode(objName.toString()), null, //menus 
            null, //sub-nodes
            null, //sub-resources
            objName.toString(), className);
      return item;
   }

}
