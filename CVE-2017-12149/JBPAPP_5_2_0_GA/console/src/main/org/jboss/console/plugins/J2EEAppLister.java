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
import org.jboss.management.j2ee.J2EEApplicationMBean;
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
public class J2EEAppLister 
   extends AbstractPluginWrapper
{
   private static final long serialVersionUID = 4168885656223716764L;
   
   protected final static String JMX_JSR77_DOMAIN = "jboss.management.local";
   
   public J2EEAppLister () { super(); }
      
   ResourceTreeNode[] createModules (String[] modules)  throws Exception
   {
      ResourceTreeNode[] deployed = new ResourceTreeNode[modules.length];
      for (int i = 0; i < modules.length; i++)
      {
         //J2EEApplication earProxy = (J2EEApplication)
         //   MBeanProxy.create(J2EEApplication.class, objName, getMBeanServer());


         ObjectName objectName = new ObjectName(modules[i]);
         deployed[i] = createResourceNode (
            objectName.getKeyProperty ("name"), // name
            "", // description
            "images/EspressoMaker.gif", // Icon URL
            null, // "J2EEApp.jsp?ObjectName=" + encode (objName.toString ()), // Default URL
            null,
            null, // sub nodes
            null, //createEARSubModules (objName), // Sub-Resources
            modules[i].toString (),
            this.mbeanServer.getMBeanInfo (objectName).getClassName ()
         ).setVisibility (ResourceTreeNode.INVISIBLE_IF_SUBNODE_EXISTS);

      }

      return deployed;
   }

   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         ObjectName objName = ((MBeanResource)resource).getObjectName();
         J2EEApplicationMBean appProxy = (J2EEApplicationMBean)
            MBeanProxyExt.create (J2EEApplicationMBean.class, objName, getMBeanServer());

         return createTreeNode
            (
               objName.getKeyProperty("name"),  // name
               "", // description
               "images/EspressoMaker.gif", // Icon URL
               "J2EEApp.jsp?ObjectName=" + encode (objName.toString ()), // Default URL
               null,
               null, // sub nodes
               createModules (appProxy.getmodules())   // Sub-Resources
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
