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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.ResourceTreeNode;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;
import org.jboss.management.j2ee.J2EEServerMBean;
import org.jboss.mx.util.MBeanProxyExt;

/**
 * As the number of MBeans is very big, we use a real Java class which is far
 * faster than beanshell
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81010 $
 */
public class JSR77Lister extends AbstractPluginWrapper
{
   private static final long serialVersionUID = -5466799611043095874L;

   /**
    * Default CTOR
    */
   public JSR77Lister()
   {
      super();
   }

   /*
   ResourceTreeNode[] createEARSubModules (ObjectName parent)  throws Exception
   {
      // there is a bug in the current jsr77 implementation with regard to naming
      // of EJBModule that are part of EARs => I've used a workaround
      //
      ObjectInstance[] insts = getMBeansForQuery(JMX_JSR77_DOMAIN + 
         ":j2eeType=EJBModule,J2EEServer="+parent.getKeyProperty("name")+",*", null);

      ResourceTreeNode[] jars = new ResourceTreeNode[insts.length];      
      for (int i=0; i<insts.length; i++)
      {
         ObjectName objName = insts[i].getObjectName();
         //EJBModule jarProxy = (EJBModule) 
         //   MBeanProxy.create(EJBModule.class, objName, getMBeanServer());
            
         jars[i] = createResourceNode(
               objName.getKeyProperty("name"),  // name
               "", // description
               null, // Icon URL
               null, // Default URL
               null,
               null, // sub nodes
               null,   // Sub-Resources                  
               objName.toString(),
               org.jboss.management.j2ee.EJBModule.class.getName()
            ).setVisibility(ResourceTreeNode.INVISIBLE_IF_SUBNODE_EXISTS);                  
         
      }
          
      return jars;  
   }
   
   ResourceTreeNode[] createEARs (ObjectName parent)  throws Exception
   {
      ObjectInstance[] insts = getMBeansForQuery(JMX_JSR77_DOMAIN + 
         ":j2eeType=J2EEApplication,J2EEServer="+parent.getKeyProperty("name")+",*", null);

      ResourceTreeNode[] ears = new ResourceTreeNode[insts.length];
      for (int i=0; i<insts.length; i++)
      {
         ObjectName objName = insts[i].getObjectName();
         //J2EEApplication earProxy = (J2EEApplication)
         //   MBeanProxy.create(J2EEApplication.class, objName, getMBeanServer());

         ears[i] = createResourceNode (
               objName.getKeyProperty("name"),  // name
               "", // description
               "images/EspressoMaker.gif", // Icon URL
               "J2EEApp.jsp?ObjectName=" + encode(objName.toString()), // Default URL
               null,
               null, // sub nodes
               createEARSubModules (objName),   // Sub-Resources
               parent.toString(),
               J2EEApplication.class.getName()
            );

      }
          
      return ears;  
   }
   
   ResourceTreeNode[] singleEJBs (ObjectName parent)  throws Exception
   {
      ObjectInstance[] insts = getMBeansForQuery(JMX_JSR77_DOMAIN + 
         ":j2eeType=EJBModule,J2EEServer="+parent.getKeyProperty("name")+",*", null);

      ResourceTreeNode[] jars = new ResourceTreeNode[insts.length];      
      for (int i=0; i<insts.length; i++)
      {
         ObjectName objName = insts[i].getObjectName();
         //EJBModule jarProxy = (EJBModule) 
         //   MBeanProxy.create(EJBModule.class, objName, getMBeanServer());
            
         jars[i] = createResourceNode(
               objName.getKeyProperty("name"),  // name
               "", // description
               null, // Icon URL
               null, // Default URL
               null,
               null, // sub nodes
               null,   // Sub-Resources                  
               objName.toString(),
               org.jboss.management.j2ee.EJBModule.class.getName()
            ).setVisibility(ResourceTreeNode.INVISIBLE_IF_SUBNODE_EXISTS);                  
         
      }
          
      return jars;  
   }
*/
   TreeNode createSubResources (String[] resources) throws Exception
   {
      ResourceTreeNode[] deployed = new ResourceTreeNode[resources.length];
      for (int i = 0; i < resources.length; i++)
      {
         ObjectName objectName = new ObjectName(resources[i]);
         deployed[i] = createResourceNode (
            objectName.getKeyProperty ("name"), // name
            "J2EE Resource", // description
            null, //"images/EspressoMaker.gif", // Icon URL
            null, // "J2EEApp.jsp?ObjectName=" + encode (objName.toString ()), // Default URL
            null,
            null, // sub nodes
            null, //createEARSubModules (objName), // Sub-Resources
            resources[i].toString (),
            this.mbeanServer.getMBeanInfo (objectName).getClassName ()
         ).setVisibility (ResourceTreeNode.INVISIBLE_IF_SUBNODE_EXISTS);
      }

      return createTreeNode (
         "J2EE Resources", // name
         "J2EE Resources", // description
         "images/spirale.gif", // Icon URL
         null, //"J2EEDomain.jsp&objectName=" + encode(objName.toString()), // Default URL
         null,
         null, // sub nodes
         deployed   // Sub-Resources
      );
   }

   ResourceTreeNode[] createDeployedObjects (String[] resources) throws Exception
   {
      ArrayList deployed = new ArrayList ();
      for (int i = 0; i < resources.length; i++)
      {
         ObjectName objectName = new ObjectName(resources[i]);
         //if (resources[i].getKeyProperty ("J2EEApplication") == null)
         {
            deployed.add(createResourceNode (
               objectName.getKeyProperty("name"), // name
               "", // description
               "images/EspressoMaker.gif", // Icon URL
               null, // "J2EEApp.jsp?ObjectName=" + encode (objName.toString ()), // Default URL
               null,
               null, // sub nodes
               null, //createEARSubModules (objName), // Sub-Resources
               resources[i].toString (),
               this.mbeanServer.getMBeanInfo (objectName).getClassName ()
            ).setVisibility (ResourceTreeNode.INVISIBLE_IF_SUBNODE_EXISTS));
         }

      }
      Collections.sort(deployed, new ListerSorter());

      return (ResourceTreeNode[])deployed.toArray(new ResourceTreeNode[deployed.size()]);
   }

   ResourceTreeNode createServer (String serverName) throws Exception
   {

      ObjectName objectName = new ObjectName(serverName);
      J2EEServerMBean serv = (J2EEServerMBean)
         MBeanProxyExt.create(J2EEServerMBean.class, objectName, getMBeanServer ());

      String[] deployedON = serv.getdeployedObjects();
      ResourceTreeNode[] subResArray = createDeployedObjects (deployedON);

      return createResourceNode (
         serv.getserverVendor () + " - " + serv.getserverVersion (), // name
         objectName.getKeyProperty ("name"), // description
         "images/database.gif", // Icon URL
         null, //"J2EEDomain.jsp?objectName=" + encode(objName.toString()), // Default URL
         null,
         new TreeNode[] {createSubResources (serv.getresources())}, // sub nodes
         subResArray, // Sub-Resources
         serverName.toString (),
         org.jboss.management.j2ee.J2EEServer.class.getName ()
      );

   }

   ResourceTreeNode[] createServers(ObjectName domain) throws Exception
   {
      // will throw an exception if there is no such attribute
      String[] serversObjectNames = (String[])getMBeanServer().getAttribute(domain, "servers");
      
      ArrayList servers = new ArrayList();
      for (int i = 0; i < serversObjectNames.length; i++)
      {
         servers.add(createServer(serversObjectNames[i]));
      }

      return (ResourceTreeNode[])servers.toArray(new ResourceTreeNode[servers.size()]);
   }
   
   /*
   TreeNode createGenericNode (String name, ObjectName on, Class clazz) throws Exception
   {
      return createResourceNode(name, name, null, null, null, null, null, on.toString(), clazz.toString());
   }
   */

   TreeNode createDomain(ObjectName domain) throws Exception
   {
      return createTreeNode(
         domain.getKeyProperty("name"), // name
         "", // description
         "images/spirale.gif", // Icon URL
         null, //"J2EEDomain.jsp&objectName=" + encode(objName.toString()), // Default URL
         null,
         null, // sub nodes
         createServers(domain)   // Sub-Resources
      );
   }

   /**
    * Find the root JSR-77 domains
    */
   TreeNode[] createDomains()
   {
      // The potential list of domain starting points. According to the
      // spec we should be looking for *:j2eeType=J2EEDomain,* where
      // the value of the "name" attribute matches the domain name.
      ObjectInstance[] insts = getMBeansForQuery("*:j2eeType=J2EEDomain,*", null);
      
      // Holds the domains that are succesfully created
      ArrayList domainsCreated = new ArrayList();
      
      for (int i=0; i < insts.length; i++)
      {
         ObjectName objectName = insts[i].getObjectName();
         try
         {
            if (objectName.getDomain().equals(objectName.getKeyProperty("name")))
            {
               // value of "name" attribute matches the domain name, assume is ok
               domainsCreated.add(createDomain(objectName));
            }
         }
         catch (AttributeNotFoundException e)
         {
            // we are looking at a bad starting point
            System.err.println(e.getClass().getName() + ": " + e.getMessage()
                  + "; mbean '" + objectName + "' not a proper j2eeType=J2EEDomain");
         }
         catch (Exception e)
         {
            // show error and continue
            e.printStackTrace();
         }
      }
      // Transform the list to TreeNode[]
      TreeNode[] domains = (TreeNode[])domainsCreated.toArray(new TreeNode[domainsCreated.size()]);
      
      // Done
      return domains;
   }
   
   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         return createTreeNode (
               "J2EE Domains",  // name
               "Display JSR-77 Managed Objects", // description
               "images/elements32.gif", // Icon URL
               null, // Default URL
               null,
               createDomains (), // sub nodes
               null   // Sub-Resources                  
            );            
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         return null;
      }
   }

   public final static String[] DEFAULT_SUFFIX_ORDER = {
      "ear", "jar", "war", "sar", "rar", "ds.xml", "service.xml", "wsr", "zip"
   };

   public class ListerSorter implements Comparator
   {

      protected String[] suffixOrder;

      public ListerSorter (String[] suffixOrder)
      {
         this.suffixOrder = suffixOrder;
      }

      public ListerSorter ()
      {
         this (DEFAULT_SUFFIX_ORDER);
      }

      public int compare (Object o1, Object o2)
      {
         return getExtensionIndex ((ResourceTreeNode) o1) - getExtensionIndex ((ResourceTreeNode) o2);
      }

      public int getExtensionIndex (ResourceTreeNode node)
      {
         String name = node.getName();
         if (name == null) name = "";

         int i = 0;
         for (; i < suffixOrder.length; i++)
         {
            if (name.endsWith (suffixOrder[i]))
               break;
         }
         return i;
      }
   }

}
