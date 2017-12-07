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

import javax.management.ObjectName;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.impl.MBeanResource;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;
import org.jboss.management.j2ee.WebModuleMBean;
import org.jboss.mx.util.MBeanProxyExt;
/**
 * As the number of MBeans is very big, we use a real Java class which is far
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
public class WebModuleLister 
   extends AbstractPluginWrapper
{
   private static final long serialVersionUID = -8019251323455453105L;

   protected final static String JMX_JSR77_DOMAIN = "jboss.management.local";
   
   public WebModuleLister () { super(); }
      
   ResourceTreeNode[] createBeans (ObjectName parent)  throws Exception
   {
       WebModuleMBean wmProxy = (WebModuleMBean)
               MBeanProxyExt.create(WebModuleMBean.class, parent, getMBeanServer());

       String[] servletsObjectName = wmProxy.getservlets();

      ResourceTreeNode[] servlets = new ResourceTreeNode[servletsObjectName.length];
      for (int i=0; i< servletsObjectName.length; i++)
      {
         ObjectName objectName = new ObjectName(servletsObjectName[i]);
          String name = objectName.getKeyProperty("name");

          servlets[i] = createResourceNode(
                  name,  // name
               "'" + name + "' Servlet", // description
               "images/serviceset.gif", // Icon URL
               "Servlet.jsp?ObjectName=" + encode(objectName.toString()), // Default URL
               null,
               null, // sub nodes
               null,   // Sub-Resources
               objectName.toString(),
               org.jboss.management.j2ee.Servlet.class.getName()
            );                  
         
      }
          
      return servlets;
   }

   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         ObjectName objName = ((MBeanResource)resource).getObjectName();

         return createTreeNode
            (
               objName.getKeyProperty("name"),  // name
               "", // description
               "images/spirale.gif", // Icon URL
               "WebModule.jsp?ObjectName=" + encode(objName.toString()), // Default URL
               null,
               null, // sub nodes
               createBeans (objName)   // Sub-Resources                  
            ).setMasterNode(true);                  
         
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         System.out.println (checker);
         return null;
         
      }
   }

}
