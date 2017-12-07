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
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.manager.interfaces.TreeNodeMenuEntry;
import org.jboss.console.manager.interfaces.impl.GraphMBeanAttributeAction;
import org.jboss.console.manager.interfaces.impl.SimpleTreeNodeMenuEntryImpl;
import org.jboss.console.manager.interfaces.impl.HttpLinkTreeAction;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;
import org.jboss.console.plugins.helpers.jmx.DomainData;
import org.jboss.console.plugins.helpers.jmx.MBeanData;
import org.jboss.console.plugins.helpers.jmx.Server;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
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
public class MBeansLister 
   extends AbstractPluginWrapper
{
   private static HashSet graphableClasses = new HashSet();

   static
   {
      graphableClasses.add("java.lang.Integer");
      graphableClasses.add("java.lang.Short");
      graphableClasses.add("java.lang.Double");
      graphableClasses.add("java.lang.Float");
      graphableClasses.add("java.lang.Long");
      graphableClasses.add("int");
      graphableClasses.add("short");
      graphableClasses.add("double");
      graphableClasses.add("float");
      graphableClasses.add("long");
   }
   public MBeansLister () { super(); }

   TreeNode createJmxAttributeSubResource(MBeanAttributeInfo attr, ObjectName mbeanName) throws Exception
   {
      TreeNodeMenuEntry[] entries = null;
      if (graphableClasses.contains(attr.getType()))
      {
         SimpleTreeNodeMenuEntryImpl entry = new SimpleTreeNodeMenuEntryImpl("graph", new GraphMBeanAttributeAction(mbeanName, attr.getName()));
         SimpleTreeNodeMenuEntryImpl entry2 = new SimpleTreeNodeMenuEntryImpl("create monitor", new HttpLinkTreeAction(
                 "/web-console/createThresholdMonitor.jsp?attribute=" + attr.getName() + "&objectName=" + encode(mbeanName.toString())));
         SimpleTreeNodeMenuEntryImpl entry3 = new SimpleTreeNodeMenuEntryImpl("create snapshot", new HttpLinkTreeAction(
                 "/web-console/createSnapshot.jsp?attribute=" + attr.getName() + "&objectName=" + encode(mbeanName.toString())));
         entries = new TreeNodeMenuEntry[3];
         entries[0] = entry;
         entries[1] = entry2;
         entries[2] = entry3;
      }
      else if (attr.getType().equals("String") || attr.getType().equals("java.lang.String"))
      {
         SimpleTreeNodeMenuEntryImpl entry = new SimpleTreeNodeMenuEntryImpl("create monitor", new HttpLinkTreeAction(
                 "/web-console/createStringThresholdMonitor.jsp?attribute=" + attr.getName() + "&objectName=" + encode(mbeanName.toString())));
         entries = new TreeNodeMenuEntry[1];
         entries[0] = entry;
      }

      return createTreeNode(
              attr.getName(),
              attr.getDescription(),
              "images/container.gif",
              "/jmx-console/HtmlAdaptor?action=inspectMBean&name=" + encode("" + mbeanName), // Default URL
              entries,
              null,
              null
              //name,
              //data.getClassName() TOO HEAVY TO GENERATE RESOURCE LOOKUP FOR EACH MBEAN!
           );
   }
   TreeNode createJmxMBeanSubResources (MBeanData data) throws Exception
   {
      String name = "" + data.getObjectName();
      String displayName = data.getName ();
            
      if (displayName == null)
      {
         // Get ride of the domain name because it is already is the header
         int index = name.indexOf( ":" );
         displayName = ( index >= 0 ) ? name.substring( index + 1 ) : name;
      }

      MBeanAttributeInfo[] attributes = data.getMetaData().getAttributes();
      TreeNode[] attrNodes = new TreeNode[attributes.length];
      for (int i = 0; i < attributes.length; i++)
      {
         attrNodes[i] = createJmxAttributeSubResource(attributes[i], data.getObjectName());
      }

      return createTreeNode (
            displayName, // name
            name, // description
            "images/server.gif", // Icon URL
            "/jmx-console/HtmlAdaptor?action=inspectMBean&name=" + encode(name), // Default URL
            null,
            attrNodes,
            null
            //name,
            //data.getClassName() TOO HEAVY TO GENERATE RESOURCE LOOKUP FOR EACH MBEAN!
         );
   }   
   
   TreeNode[] createJmxDomainsSubNodes ()  throws Exception
   {
      Iterator mbeans = Server.getDomainData(null);
      
      TreeNode[] result = null;
      
      ArrayList domains = new ArrayList ();            
            
      while( mbeans.hasNext() )
      {
         DomainData domainData = (DomainData) mbeans.next();         
         String domainName = domainData.getDomainName();
         MBeanData[] data = domainData.getData();                                    
         TreeNode[] subResources = new TreeNode[data.length];
         
         for(int d = 0; d < data.length; d ++)
         {
            subResources[d] = createJmxMBeanSubResources (data[d]);
         }

         TreeNodeMenuEntry[] menu = createMenus (new String[] 
            {
               "Number of MBeans: " + data.length, null,
            }
         );
         
         domains.add(createTreeNode (
               domainName, // name
               "MBeans for domain " + domainName, // description
               "images/serviceset.gif", // Icon URL
               null, // Default URL
               menu, // menu
               subResources, // sub nodes
               null   // Sub-Resources                  
            )
         );       
         
      }
      
      if (domains.size() == 0)
      {
         result = null;
      }
      else
      {
         result = (TreeNode[]) domains.toArray(new TreeNode[domains.size()]);
      }
      
      return result;                                                  
   }
   
   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         return createTreeNode (
               "JMX MBeans", // name
               "Display all JMX MBeans", // description
               "images/flash.gif", // Icon URL
               "/jmx-console/HtmlAdaptor?action=displayMBeans", // Default URL
               null,
               createJmxDomainsSubNodes (), // sub nodes
               null   // Sub-Resources                  
            );            
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         return null;
      }
   }
}
