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
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.console.manager.interfaces.ManageableResource;
import org.jboss.console.manager.interfaces.TreeNode;
import org.jboss.console.plugins.helpers.AbstractPluginWrapper;
import org.jboss.monitor.services.ActiveAlarmTableMBean;
import org.jboss.mx.util.InstanceOfQueryExp;
/**
 * As the number of MBeans is very big, we use a real Java class which is far
 * faster than beanshell
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81010 $
 */
public class MonitorLister 
   extends AbstractPluginWrapper
{
   /** @since 4.0.1 */
   private static final long serialVersionUID = 1625760520837838058L;

   public MonitorLister () { super(); }

   private TreeNode[] createMonitorSubNodes ()  throws Exception
   {
      MBeanServer mbeanServer = getMBeanServer();
      InstanceOfQueryExp queryExp = null;
      queryExp = new InstanceOfQueryExp("org.jboss.monitor.JBossMonitorMBean");
      Set monitors = mbeanServer.queryNames(null, queryExp);
      Iterator mbeans = monitors.iterator();;
      
      TreeNode[] result = null;
      
      ArrayList monitorNodes = new ArrayList ();

      String emptySig[] = {};
      Object emptyArgs[] = {};
      while( mbeans.hasNext() )
      {
         ObjectName mbean = (ObjectName)mbeans.next();
         String monitorName = (String)mbeanServer.getAttribute(mbean, "MonitorName");
         boolean alerted = ((Boolean)mbeanServer.invoke(mbean, "alerted", emptyArgs, emptySig)).booleanValue();
         //String image = alerted ? "images/red_bullet.gif" : "images/green_bullet.gif";
         String image = "images/service.gif";
         Object[] args = {"monitors", monitorName, "-service.xml"};
         String[] signature = {"java.lang.String", "java.lang.String", "java.lang.String"};
         Object rtn = mbeanServer.invoke(new ObjectName("jboss.admin:service=DeploymentFileRepository"), "isStored", args, signature);
         boolean persisted = ((Boolean)rtn).booleanValue();
         String url = "";
         if (persisted)
         {
            url = "manageThresholdMonitor.jsp?monitorObjectName=" + encode(mbean.toString());
         }
         else
         {
            url = "/jmx-console/HtmlAdaptor?action=inspectMBean&name=" +encode(mbean.toString());
         }
         monitorNodes.add(createTreeNode (
               monitorName, // name
               "Alert Monitor " + monitorName, // description
               image, // Icon URL
               url, // Default URL
               null, // menu
               null, // sub nodes
               null   // Sub-Resources                  
            )
         );       
         
      }
      
      if (monitorNodes.size() == 0)
      {
         result = null;
      }
      else
      {
         result = (TreeNode[]) monitorNodes.toArray(new TreeNode[monitorNodes.size()]);
      }
      
      return result;                                                  
   }
   
   private TreeNode[] createSnapshotSubNodes()  throws Exception
   {
      MBeanServer mbeanServer = getMBeanServer();
      InstanceOfQueryExp queryExp = null;
      queryExp = new InstanceOfQueryExp("org.jboss.monitor.SnapshotRecordingMonitorMBean");
      Set monitors = mbeanServer.queryNames(null, queryExp);
      Iterator mbeans = monitors.iterator();;

      TreeNode[] result = null;

      ArrayList monitorNodes = new ArrayList ();

      while( mbeans.hasNext() )
      {
         ObjectName mbean = (ObjectName)mbeans.next();
         String monitorName = (String)mbeanServer.getAttribute(mbean, "MonitorName");
         String url = "manageSnapshot.jsp?monitorObjectName=" + encode(mbean.toString());
         String image = "images/service.gif";
         monitorNodes.add(createTreeNode (
               monitorName, // name
               "Snapshot " + monitorName, // description
               image, // Icon URL
               url, // Default URL
               null, // menu
               null, // sub nodes
               null   // Sub-Resources
            )
         );

      }

      if (monitorNodes.size() == 0)
      {
         result = null;
      }
      else
      {
         result = (TreeNode[]) monitorNodes.toArray(new TreeNode[monitorNodes.size()]);
      }

      return result;
   }

   private TreeNode[] createWebSubNodes() throws Exception
   {
      TreeNode[] webSubNodes = new TreeNode[2];
      
      webSubNodes[0] = createTreeNode(
            "Connector scoreboard", // name
            "JBossWeb Connectors status scoreboard", // description
            "images/smallnet.gif", // Icon URL
            "status", // Default URL
            null,
            null, // sub nodes
            null   // Sub-Resources
         );
      webSubNodes[1] = createTreeNode(
            "Full status", // name
            "JBossWeb complete status", // description
            "images/smallnet.gif", // Icon URL
            "status?full=true", // Default URL
            null,
            null, // sub nodes
            null   // Sub-Resources
         );
     
      return webSubNodes;
   }
   
   protected TreeNode getTreeForResource(String profile, ManageableResource resource)
   {
      try
      {
         ArrayList subNodeList = new ArrayList();
         
         if (isActiveAlarmTablePresent())
         {
            subNodeList.add(createTreeNode(
                  "Alarm Table", // name
                  "Alarm Table", // description
                  "images/smallnet.gif", // icon URL
                  "listActiveAlarmTable.jsp", // default URL
                  null,
                  null, // sub-nodes
                  null // sub-resources
                  ));
         }
         
         subNodeList.add(createTreeNode(
               "Monitor Alerts", // name
               "Monitor Alerts", // description
               "images/smallnet.gif", // Icon URL
               "listMonitors.jsp", // Default URL
               null,
               createMonitorSubNodes(), // sub nodes
               null   // Sub-Resources
            ));
         
         subNodeList.add(createTreeNode(
               "Snapshots", // name
               "Snapshot Monitors", // description
               "images/smallnet.gif", // Icon URL
               null, // Default URL
               null,
               createSnapshotSubNodes(), // sub nodes
               null   // Sub-Resources
            ));
         
         subNodeList.add(createTreeNode(
               "Web Status", // name
               "JBossWeb Connectors status", // description
               "images/smallnet.gif", // Icon URL
               null, // Default URL
               null,
               createWebSubNodes(), // sub nodes
               null   // Sub-Resources
            )); 
         
         // convert ArrayList to TreeNode[]
         TreeNode[] subnodes = (TreeNode[]) subNodeList.toArray(new TreeNode[subNodeList.size()]);
         
         return createTreeNode (
               "Monitoring", // name
               "Monitoring", // description
               "images/smallnet.gif", // Icon URL
               null, // Default URL
               null,
               subnodes, // sub nodes
               null   // Sub-Resources
            );
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         return null;
      }
   }
   
   private boolean isActiveAlarmTablePresent()
   {
      MBeanServer server = getMBeanServer();
      return server.isRegistered(ActiveAlarmTableMBean.OBJECT_NAME);
   }
}
