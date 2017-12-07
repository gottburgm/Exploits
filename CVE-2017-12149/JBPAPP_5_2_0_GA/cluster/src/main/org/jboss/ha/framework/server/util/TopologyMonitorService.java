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
package org.jboss.ha.framework.server.util;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Vector;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.apache.log4j.MDC;
import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.AsynchHAMembershipListener;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/** A cluster parition membership monitor. It can be used to view how
 the nodes in a cluster are seeing the topology change using either email or
 a centralized log server.

 To use this to send email on change notifications use the following log4j.xml
 fragments:

  <appender name="SMTP" class="org.apache.log4j.net.SMTPAppender">
    <param name="To" value="admin@dot.com"/>
    <param name="From" value="cluster-monitor@dot.com"/>
    <param name="Subject" value="JBoss Cluster Changes"/>
    <param name="SMTPHost" value="mailhost"/>
    <param name="BufferSize" value="8"/>
    <param name="EvaluatorClass"
      value="org.jboss.logging.appender.RegexEventEvaluator" />
    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="[%d{ABSOLUTE},%c{1}] %m%n"/>
    </layout>
  </appender>

  <category name="org.jboss.ha.framework.server.util.TopologyMonitorService.membershipChanged">
    <priority value="DEBUG" />
    <appender-ref ref="SMTP"/>
  </category>

 You can also have this service notify another MBean of the change to perform
 arbitrary checks by specifying the MBean name as the TriggerServiceName
 attribute value. This MBean must have an operation with the following
 signature:
<pre>
   param: removed ArrayList<AddressPort> of nodes that were removed
   param: added ArrayList<AddressPort> of nodes that were added
   param: members ArrayList<AddressPort> of nodes currently in the cluster
   param: logLoggerName the log4j category name used by the
      TopologyMonitorService. This should be used for logging to integrate with
      the TopologyMonitorService output.
   public void membershipChanged(ArrayList deadMembers, ArrayList newMembers,
      ArrayList allMembers, String logLoggerName)
</pre>

 @author Scott.Stark@jboss.org
 @version $Revision: 81001 $
 */
public class TopologyMonitorService
   extends ServiceMBeanSupport
   implements TopologyMonitorServiceMBean, AsynchHAMembershipListener
{
   private static final String CHANGE_NAME = TopologyMonitorService.class.getName() + ".membershipChanged";

   private static Logger changeLog = Logger.getLogger(CHANGE_NAME);

   private volatile String partitionName = ServerConfigUtil.getDefaultPartitionName();

   private volatile HAPartition partition;

   private String hostname;

   private volatile ObjectName triggerServiceName;

   public TopologyMonitorService()
   {
   }

   // --- Begin ServiceMBeanSupport overriden methods

   @Override
   protected void startService() throws Exception
   {
      if (this.partition == null)
      {
         this.partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(this.partitionName, null);
      }
      // Register as a listener of cluster membership changes
      this.partition.registerMembershipListener(this);
      this.log.info("Registered as MembershipListener");
      try
      {
         this.hostname = InetAddress.getLocalHost().getHostName();
      }
      catch (IOException e)
      {
         this.log.warn("Failed to lookup local hostname", e);
         this.hostname = "<unknown>";
      }
   }

   @Override
   protected void stopService() throws Exception
   {
      this.partition.unregisterMembershipListener(this);
   }

   // --- End ServiceMBeanSupport overriden methods

   // --- Begin TopologyMonitorServiceMBean interface methods
   public String getPartitionName()
   {
      return (this.partition == null) ? this.partitionName : this.partition.getPartitionName();
   }

   public void setPartitionName(String name)
   {
      this.partitionName = name;
   }

   public void setPartition(HAPartition partition)
   {
      this.partition = partition;
   }

   public ObjectName getTriggerServiceName()
   {
      return this.triggerServiceName;
   }

   public void setTriggerServiceName(ObjectName triggerServiceName)
   {
      this.triggerServiceName = triggerServiceName;
   }

   public Vector getClusterNodes()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         String jndiName = "/HAPartition/" + this.partitionName;
         HAPartition partition = (HAPartition) ctx.lookup(jndiName);
         return partition.getCurrentView();
      }
      catch (Exception e)
      {
         this.log.error("Failed to access HAPartition state", e);
         return null;
      }
   }

   // --- End TopologyMonitorServiceMBean interface methods

   // --- Begin HAMembershipListener interface methods
   /** Called when a new partition topology occurs.
    * @param deadMembers A list of nodes that have died since the previous view
    * @param newMembers A list of nodes that have joined the partition since
    * the previous view
    * @param allMembers A list of nodes that built the current view
    */
   public void membershipChanged(final Vector deadMembers, final Vector newMembers, final Vector allMembers)
   {
      MDC.put("RegexEventEvaluator", "End membershipChange.*");
      ArrayList removed = new ArrayList();
      ArrayList added = new ArrayList();
      ArrayList members = new ArrayList();
      changeLog.info("Begin membershipChanged info, hostname=" + this.hostname);
      changeLog.info("DeadMembers: size=" + deadMembers.size());
      for (int m = 0; m < deadMembers.size(); m++)
      {
         AddressPort addrInfo = this.getMemberAddress(deadMembers.get(m));
         removed.add(addrInfo);
         changeLog.info(addrInfo);
      }
      changeLog.info("NewMembers: size=" + newMembers.size());
      for (int m = 0; m < newMembers.size(); m++)
      {
         AddressPort addrInfo = this.getMemberAddress(newMembers.get(m));
         added.add(addrInfo);
         changeLog.info(addrInfo);
      }
      changeLog.info("AllMembers: size=" + allMembers.size());
      for (int m = 0; m < allMembers.size(); m++)
      {
         AddressPort addrInfo = this.getMemberAddress(allMembers.get(m));
         members.add(addrInfo);
         changeLog.info(addrInfo);
      }
      // Notify the trigger MBean
      if (this.triggerServiceName != null)
      {
         changeLog.info("Invoking trigger service: " + this.triggerServiceName);
         try
         {
            Object[] params = { removed, added, members, CHANGE_NAME };
            String[] sig = { "java.util.ArrayList", "java.util.ArrayList", "java.util.ArrayList", "java.lang.String" };
            this.server.invoke(this.triggerServiceName, "membershipChanged", params, sig);
         }
         catch (Throwable t)
         {
            changeLog.error("Failed to notify trigger service: " + this.triggerServiceName, t);
            this.log.debug("Failed to notify trigger service: " + this.triggerServiceName, t);
         }
      }
      changeLog.info("End membershipChanged info, hostname=" + this.hostname);
      MDC.remove("RegexEventEvaluator");
   }

   // --- End HAMembershipListener interface methods

   /** Use reflection to access the address InetAddress and port if they exist
    * in the Address implementation
    */
   private AddressPort getMemberAddress(Object addr)
   {
      AddressPort info = null;
      try
      {
         org.jboss.ha.framework.interfaces.ClusterNode node = (org.jboss.ha.framework.interfaces.ClusterNode) addr;

         InetAddress inetAddr = node.getIpAddress();
         Integer port = new Integer(node.getPort());
         info = new AddressPort(inetAddr, port);
      }
      catch (Exception e)
      {
         this.log.warn("Failed to obtain InetAddress/port from addr: " + addr, e);
      }
      return info;
   }

   public static class AddressPort
   {
      private InetAddress addr;
      private Integer port;

      AddressPort(InetAddress addr, Integer port)
      {
         this.addr = addr;
         this.port = port;
      }

      public Integer getPort()
      {
         return this.port;
      }

      public InetAddress getInetAddress()
      {
         return this.addr;
      }

      public String getHostAddress()
      {
         return this.addr.getHostAddress();
      }

      public String getHostName()
      {
         return this.addr.getHostName();
      }

      @Override
      public String toString()
      {
         return "{host(" + this.addr + "), port(" + this.port + ")}";
      }
   }
}
