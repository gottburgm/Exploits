/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.util.platform.Java;

/**
 * An MBean that provides a rich view of system information for the JBoss
 * server in which it is deployed.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 107653 $
 */
@ManagementObject(
   name="jboss.system:type=ServerInfo",
   isRuntime=true,
   description="provides a view of system information for the JBoss server in which it is deployed",
   componentType=@ManagementComponent(type="MCBean", subtype="ServerInfo")
)
public class ServerInfo
   implements ServerInfoMBean, MBeanRegistration
{
   /** Class logger. */
   private static final Logger log = Logger.getLogger(ServerInfo.class);

   /** Zero */
   private static final Integer ZERO = new Integer(0);
   
   /** Empty parameter signature for reflective calls */
   private static final Class[] NO_PARAMS_SIG = new Class[0];

   /** Empty paramater list for reflective calls */
   private static final Object[] NO_PARAMS = new Object[0];
   
   /** used for formating timestamps (date attribute) */
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

   /** Entry point for the management of the thread system */
   private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
   
   /** The cached host name for the server. */
   private String hostName;
   
   /** The cached host address for the server. */
   private String hostAddress;
   
   /** The cached jdk6+ ThreadMXBean methods */
   private boolean isObjectMonitorUsageSupported;
   private boolean isSynchronizerUsageSupported;
   private Method findDeadlockedThreads;
   private Method dumpAllThreads;
   private Method getThreadInfoWithSyncInfo;
   
   /** The cached jdk6+ ThreadInfo methods */
   private Method getLockInfo;
   private Method getLockedMonitors;
   private Method getLockedSynchronizers;
   
   /** The cached jdk6+ LockInfo methods */
   private Method getClassName;
   private Method getIdentityHashCode;
   
   /** The cached jdk6+ MonitorInfo methods */
   private Method from;
   private Method getLockedStackDepth;
   private Method getLockedStackFrame;
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                               JMX Hooks                               //
   ///////////////////////////////////////////////////////////////////////////
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // Dump out basic JVM & OS info as INFO priority msgs
      log.info("Java version: " +
      System.getProperty("java.version") + "," +
      System.getProperty("java.vendor"));

      log.info("Java Runtime: " +
      System.getProperty("java.runtime.name") + " (build " +
      System.getProperty("java.runtime.version") + ")");      
      
      log.info("Java VM: " +
      System.getProperty("java.vm.name") + " " +
      System.getProperty("java.vm.version") + "," +
      System.getProperty("java.vm.vendor"));
      
      log.info("OS-System: " +
      System.getProperty("os.name") + " " +
      System.getProperty("os.version") + "," +
      System.getProperty("os.arch"));
      
      log.info("VM arguments: " + getVMArguments());
      
      // Dump out the entire system properties
      log.debug("Full System Properties Dump");
      Enumeration names = System.getProperties().propertyNames();
      while (names.hasMoreElements())
      {
         String pname = (String)names.nextElement();
            log.debug("    " + pname + ": " + System.getProperty(pname));
      }
      
      if (Java.isCompatible(Java.VERSION_1_6))
      {
         try
         {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            
            Class clazz = ThreadMXBean.class;
            Method method = clazz.getMethod("isObjectMonitorUsageSupported", NO_PARAMS_SIG);
            isObjectMonitorUsageSupported = (Boolean)method.invoke(threadMXBean, NO_PARAMS);
            
            method = clazz.getMethod("isSynchronizerUsageSupported", NO_PARAMS_SIG);
            isSynchronizerUsageSupported = (Boolean)method.invoke(threadMXBean, NO_PARAMS);
            
            this.findDeadlockedThreads = clazz.getMethod("findDeadlockedThreads", NO_PARAMS_SIG);
            this.dumpAllThreads = clazz.getMethod("dumpAllThreads", new Class[] { Boolean.TYPE, Boolean.TYPE } );
            this.getThreadInfoWithSyncInfo = clazz.getMethod("getThreadInfo", new Class[] { long[].class , Boolean.TYPE, Boolean.TYPE});
            
            clazz = ThreadInfo.class;
            this.getLockInfo = clazz.getMethod("getLockInfo", NO_PARAMS_SIG);
            this.getLockedMonitors = clazz.getMethod("getLockedMonitors", NO_PARAMS_SIG);
            this.getLockedSynchronizers = clazz.getMethod("getLockedSynchronizers", NO_PARAMS_SIG);
            
            clazz = cl.loadClass("java.lang.management.LockInfo");
            this.getClassName = clazz.getMethod("getClassName", NO_PARAMS_SIG);
            this.getIdentityHashCode = clazz.getMethod("getIdentityHashCode", NO_PARAMS_SIG);
            
            clazz = cl.loadClass("java.lang.management.MonitorInfo");            
            this.from = clazz.getMethod("from", new Class[] { CompositeData.class });
            this.getLockedStackDepth = clazz.getMethod("getLockedStackDepth", NO_PARAMS_SIG);
            this.getLockedStackFrame = clazz.getMethod("getLockedStackFrame", NO_PARAMS_SIG);
         }
         catch (Exception e)
         {
            log.debug("Cannot access platform ThreadMXBean", e);
         }
      }
      
      return name == null ? new ObjectName(OBJECT_NAME_STR) : name;
   }
   
   public void postRegister(Boolean registrationDone)
   {
      // empty
   }
   
   public void preDeregister() throws Exception
   {
      // empty
   }
   
   public void postDeregister()
   {
      // empty
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                            Server Information                         //
   ///////////////////////////////////////////////////////////////////////////

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getJavaVersion()
   {
      return System.getProperty("java.version");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getJavaVendor()
   {
      return System.getProperty("java.vendor");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getJavaVMName()
   {
      return System.getProperty("java.vm.name");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getJavaVMVersion()
   {
      return System.getProperty("java.vm.version");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getJavaVMVendor()
   {
      return System.getProperty("java.vm.vendor");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getOSName()
   {
      return System.getProperty("os.name");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getOSVersion()
   {
      return System.getProperty("os.version");
   }

   @ManagementProperty(use={ViewUse.RUNTIME}, readOnly=true)
   public String getOSArch()
   {
      return System.getProperty("os.arch");
   }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public Long getTotalMemory()
   {
      return new Long(Runtime.getRuntime().totalMemory());
   }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public Long getFreeMemory()
   {
      return new Long(Runtime.getRuntime().freeMemory());
   }
   
   /**
    * Returns <tt>Runtime.getRuntime().maxMemory()<tt> on 
    * JDK 1.4 vms or -1 on previous versions.
    */
   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public Long getMaxMemory()
   {
      if (Java.isCompatible(Java.VERSION_1_4)) {
         // Uncomment when JDK 1.4 is the base JVM
         // return new Long(Runtime.getRuntime().maxMemory());

         // until then use reflection to do the job
         try {
            Runtime rt = Runtime.getRuntime();
            Method m = rt.getClass().getMethod("maxMemory", NO_PARAMS_SIG);
            return (Long)m.invoke(rt, NO_PARAMS);
         }
         catch (Exception e) {
            log.error("Operation failed", e);
         }
      }

      return new Long(-1);
   }

   /**
    * Returns <tt>Runtime.getRuntime().availableProcessors()</tt> on 
    * JDK 1.4 vms or -1 on previous versions.
    */
   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public Integer getAvailableProcessors()
   {
      if (Java.isCompatible(Java.VERSION_1_4)) {
         // Uncomment when JDK 1.4 is the base JVM
         // return new Integer(Runtime.getRuntime().availableProcessors());

         // until then use reflection to do the job
         try {
            Runtime rt = Runtime.getRuntime();
            Method m = rt.getClass().getMethod("availableProcessors", NO_PARAMS_SIG);
            return (Integer)m.invoke(rt, NO_PARAMS);
         }
         catch (Exception e) {
            log.error("Operation failed", e);
         }
      }

      return new Integer(-1);
   }

   /**
    * Returns InetAddress.getLocalHost().getHostName();
    */
   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public String getHostName()
   {
      if (hostName == null)
      {
         try
         {
            hostName = java.net.InetAddress.getLocalHost().getHostName();
         }
         catch (java.net.UnknownHostException e)
         {
            log.error("Error looking up local hostname", e);
            hostName = "<unknown>";
         }
      }
      
      return hostName;
   }
   
   /**
    * Returns InetAddress.getLocalHost().getHostAddress();
    */
   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public String getHostAddress()
   {
      if (hostAddress == null)
      {
         try
         {
            hostAddress = java.net.InetAddress.getLocalHost().getHostAddress();
         }
         catch (java.net.UnknownHostException e)
         {
            log.error("Error looking up local address", e);
            hostAddress = "<unknown>";
         }
      }
      
      return hostAddress;
   }

   /**
    * Return a listing of the thread pools on jdk5+.
    * 
    * @param fancy produce a text-based graph when true
    */
   @ManagementOperation(description="Return a listing of the thread pools on jdk5+",
         impact=Impact.ReadOnly,
         params={@ManagementParameter(name="fancy", description="produce a text-based graph when true")})
   public String listMemoryPools(boolean fancy)
   {
      StringBuffer sbuf = new StringBuffer(4196);
      
      // get the pools
      List<MemoryPoolMXBean> poolList = ManagementFactory.getMemoryPoolMXBeans();
      sbuf.append("<b>Total Memory Pools:</b> ").append(poolList.size());
      sbuf.append("<blockquote>");
      for (MemoryPoolMXBean pool : poolList)
      {
         // MemoryPoolMXBean instance
         String name = pool.getName();
         // enum MemoryType
         MemoryType type = pool.getType();
         sbuf.append("<b>Pool: ").append(name);
         sbuf.append("</b> (").append(type).append(")");

         // PeakUsage/CurrentUsage
         MemoryUsage peakUsage = pool.getPeakUsage();
         MemoryUsage usage = pool.getUsage();
         
         sbuf.append("<blockquote>");
         if (usage != null && peakUsage != null)
         {
            Long init = peakUsage.getInit();
            Long used = peakUsage.getUsed();
            Long committed = peakUsage.getCommitted();
            Long max = peakUsage.getMax();
            
            sbuf.append("Peak Usage    : ");
            sbuf.append("init:").append(init);
            sbuf.append(", used:").append(used);
            sbuf.append(", committed:").append(committed);
            sbuf.append(", max:").append(max);
            sbuf.append("<br/>");

            init = usage.getInit();
            used = usage.getUsed();
            committed = usage.getCommitted();
            max = usage.getMax();

            sbuf.append("Current Usage : ");
            sbuf.append("init:").append(init);
            sbuf.append(", used:").append(used);
            sbuf.append(", committed:").append(committed);
            sbuf.append(", max:").append(max);

            if (fancy)
            {
               TextGraphHelper.poolUsage(sbuf, used.longValue(), committed.longValue(), max.longValue());
            }
         }
         else
         {
            sbuf.append("Memory pool NOT valid!");
         }
         sbuf.append("</blockquote><br/>");
      }
      
      return sbuf.toString();      
   }

   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public Integer getActiveThreadCount()
   {
      return new Integer(getRootThreadGroup().activeCount());
   }

   @ManagementProperty(use={ViewUse.STATISTIC}, readOnly=true)
   public Integer getActiveThreadGroupCount()
   {
      return new Integer(getRootThreadGroup().activeGroupCount());
   }
   
   /**
    * Return a listing of the active threads and thread groups.
    */
   @ManagementOperation(description="Return a listing of the active threads and thread groups",
         impact=Impact.ReadOnly)
   public String listThreadDump()
   {
      ThreadGroup root = getRootThreadGroup();
      
      // Count the threads/groups during our traversal
      // rather than use the often inaccurate ThreadGroup
      // activeCount() and activeGroupCount()
      ThreadGroupCount count = new ThreadGroupCount();

      StringBuffer rc = new StringBuffer();
      
      // Find deadlocks, if there're any first, so that they're visible first thing
      findDeadlockedThreads(rc);
      
      // Traverse thread dump
      getThreadGroupInfo(root, count, rc);
            
      // Attach counters
      String threadDump =
         "<b>Total Threads:</b> " + count.threads + "<br/>" +
         "<b>Total Thread Groups:</b> " + count.groups + "<br/>" +
         "<b>Timestamp:</b> " + dateFormat.format(new Date()) + "<br/>" +
         rc.toString();
      
      return threadDump;
   }
   
   /**
    * Return a listing of the active threads and thread groups.
    */
   @ManagementOperation(description="Return a listing of the active threads and thread groups",
         impact=Impact.ReadOnly)
   public String listThreadCpuUtilization()
   {
      Set threads = getThreadCpuUtilization(); 

      if (threads == null)
      {
         return("Thread cpu utilization requires J2SE5+");
      }
      else
      {
         long totalCPU = 0;
         StringBuffer buffer = new StringBuffer();
         buffer.append("<table><tr><th>Thread Name</th><th>CPU (milliseconds)</th></tr>");
         for (Iterator i = threads.iterator(); i.hasNext();)
         {
            ThreadCPU thread = (ThreadCPU) i.next();
            buffer.append("<tr><td>").append(thread.name).append("</td><td>");
            buffer.append(thread.cpuTime).append("</td></tr>");
            totalCPU += thread.cpuTime;
         }
         buffer.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>Total</td><td>");
         buffer.append(totalCPU).append("</td></tr></table>");
         return buffer.toString();
      }
   }
   
   ///////////////////////////////////////////////////////////////////////////
   //                               Private                                 //
   ///////////////////////////////////////////////////////////////////////////
   
   /**
    * Get the Thread cpu utilization
    * 
    * @return an ordered 
    */
   private Set<ThreadCPU> getThreadCpuUtilization()
   {
      TreeSet<ThreadCPU> result = new TreeSet<ThreadCPU>();
      
      long[] threads = threadMXBean.getAllThreadIds();
      for (int i = 0; i < threads.length; ++i)
      {
         Long id = new Long(threads[i]);
         Long cpuTime = threadMXBean.getThreadCpuTime(id);
         ThreadInfo threadInfo = threadMXBean.getThreadInfo(id, ZERO );
         if (threadInfo != null)
         {
            String name = threadInfo.getThreadName();
            result.add(new ThreadCPU(name, cpuTime.longValue()));
         }
      }
      return result;
   }
      
   /*
    * Traverse to the root thread group
    */
   private ThreadGroup getRootThreadGroup()
   {
      return AccessController.doPrivileged( new PrivilegedAction<ThreadGroup>() 
      {
         @Override
         public ThreadGroup run()
         {
            ThreadGroup group = Thread.currentThread().getThreadGroup();
            while (group.getParent() != null)
            {
               group = group.getParent();
            }

            return group;
         }} 
      );
      
   }
   
   /*
    * Recurse inside ThreadGroups to create the thread dump
    */
   private void getThreadGroupInfo(ThreadGroup group, ThreadGroupCount count, StringBuffer rc)
   {
      if (Java.isCompatible(Java.VERSION_1_6) && 
            (isObjectMonitorUsageSupported || isSynchronizerUsageSupported))
      {
         /* We're running JDK6+ and either object monitor or ownable 
          * synchronizers are supported by the JVM */
         log.debug("Generate a thread dump [show monitors = " + isObjectMonitorUsageSupported + ", show ownable synchronizers = " + isSynchronizerUsageSupported + "]");
         getThreadGroupInfoWithLocks(group, count, rc);
      }
      else
      {
         /* If we're running JDK5, or JDK6 but neither monitor nor 
          * synchronisers cannot be retrieved, we use standard thread dump 
          * format. */
         log.debug("Generate a thread dump without locks.");
         getThreadGroupInfoWithoutLocks(group, count, rc);
      }      
   }
   
   private void getThreadGroupInfoWithoutLocks(ThreadGroup group, ThreadGroupCount count, StringBuffer rc)
   {
      // Visit one more group
      count.groups++;
      
      rc.append("<br/><b>");
      rc.append("Thread Group: " + group.getName());
      rc.append("</b> : ");
      rc.append("max priority:" + group.getMaxPriority() +
                ", demon:" + group.isDaemon());
      
      rc.append("<blockquote>");
      Thread threads[]= new Thread[group.activeCount()];
      group.enumerate(threads, false);
      for (int i= 0; i < threads.length && threads[i] != null; i++)
      {
         // Visit one more thread
         count.threads++;
         
         rc.append("<b>");
         rc.append("Thread: " + threads[i].getName());
         rc.append("</b> : ");
         rc.append("priority:" + threads[i].getPriority() +
         ", demon:" + threads[i].isDaemon() + ", ");
         // Output extra info with jdk5+, or just <br/>
         outputJdk5ThreadMXBeanInfo(rc, threads[i]);
      }
      
      ThreadGroup groups[]= new ThreadGroup[group.activeGroupCount()];
      group.enumerate(groups, false);
      for (int i= 0; i < groups.length && groups[i] != null; i++)
      {
         getThreadGroupInfoWithoutLocks(groups[i], count, rc);
      }
      rc.append("</blockquote>");      
   }
   
   private void getThreadGroupInfoWithLocks(ThreadGroup group, ThreadGroupCount count, StringBuffer rc)
   {
      // Visit one more group
      count.groups++;
      
      rc.append("<br/><b>");
      rc.append("Thread Group: " + group.getName());
      rc.append("</b> : ");
      rc.append("max priority:" + group.getMaxPriority() +
                ", demon:" + group.isDaemon());
      
      rc.append("<blockquote>");
      Thread threads[]= new Thread[group.activeCount()];
      group.enumerate(threads, false);
      
      long[] idsTmp = new long[threads.length];
      int numberNonNullThreads = 0;
      for (int i= 0; i < threads.length && threads[i] != null; i++)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Adding " + threads[i] + " with id=" + threads[i].getId());
         }
         idsTmp[i] = threads[i].getId();
         numberNonNullThreads++;
      }
      
      long[] ids = new long[numberNonNullThreads];
      System.arraycopy(idsTmp, 0, ids, 0, numberNonNullThreads);
      
      if (log.isTraceEnabled())
      {
         log.trace("List of ids after trimming " + Arrays.toString(ids));
      }    
      
      try
      {
         ThreadInfo[] infos = (ThreadInfo[])getThreadInfoWithSyncInfo.invoke(threadMXBean, 
               new Object[] {ids, isObjectMonitorUsageSupported, isSynchronizerUsageSupported});
         
         for (int i= 0; i < infos.length && threads[i] != null; i++)
         {
            // Visit one more thread
            count.threads++;
            
            rc.append("<b>");
            rc.append("Thread: " + infos[i].getThreadName());
            rc.append("</b> : ");
            rc.append("priority:" + threads[i].getPriority() +
            ", demon:" + threads[i].isDaemon() + ", ");
            // Output extra info with jdk6+
            outputJdk6ThreadMXBeanInfo(rc, infos[i]);
         }
         
         ThreadGroup groups[]= new ThreadGroup[group.activeGroupCount()];
         group.enumerate(groups, false);
         for (int i= 0; i < groups.length && groups[i] != null; i++)
         {
            getThreadGroupInfoWithLocks(groups[i], count, rc);
         }         
      }
      catch(Exception ignore)
      {
         log.debug("Exception to be ignored", ignore);
      }
      
      rc.append("</blockquote>");
   }


   /*
    * Complete the output of thread info, with optional stuff
    * when running under jdk5+, or just change line.
    */
   private void outputJdk5ThreadMXBeanInfo(StringBuffer sbuf, Thread thread)
   {
      // Get the threadId
      Long threadId = thread.getId();

      // Get the ThreadInfo object for that threadId, max StackTraceElement depth
      ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, new Integer(Integer.MAX_VALUE));
      
      outputJdk5ThreadMXBeanInfo(sbuf, threadInfo);      
   }
   
   /*
    * Complete the output of thread info, with optional stuff when running 
    * under jdk5+ or jdk6 without object monitor usage and object synchronizer 
    * usage capabilities, or just change line.
    */
   private void outputJdk5ThreadMXBeanInfo(StringBuffer sbuf, ThreadInfo threadInfo)
   {
      // JBAS-3838, thread might not be alive
      if (threadInfo != null)
      {
         // get misc info from ThreadInfo
         Thread.State threadState = threadInfo.getThreadState(); // enum
         String lockName = threadInfo.getLockName();
         StackTraceElement[] stackTrace = threadInfo.getStackTrace();

         Long threadId = threadInfo.getThreadId();
         sbuf.append("threadId:").append(threadId);
         sbuf.append(", threadState:").append(threadState);
         sbuf.append("<br/>");
         if (stackTrace.length > 0)
         {
            sbuf.append("<blockquote>");
            
            printLockName(sbuf, "waiting on", lockName);
            
            for (int i = 0; i < stackTrace.length; i++)
            {
               sbuf.append(stackTrace[i]).append("<br/>");
            }
            sbuf.append("</blockquote>");
         }
      }
      else
      {
         sbuf.append("<br/>");
      }      
   }
   
   /*
    * Complete the output of thread info, with optional stuff
    * when running under jdk6+, or just change line.
    */
   private void outputJdk6ThreadMXBeanInfo(StringBuffer sbuf, ThreadInfo threadInfo) throws Exception
   {
      // get the threadId
      Long threadId = threadInfo.getThreadId();
      sbuf.append("threadId:").append(threadId);

      if (threadInfo != null)
      {
         // get misc info from ThreadInfo
         Thread.State threadState = threadInfo.getThreadState(); // enum
         String lockName = threadInfo.getLockName();
         StackTraceElement[] stackTrace = threadInfo.getStackTrace();         
         Object[] monitors = (Object[])getLockedMonitors.invoke(threadInfo, NO_PARAMS);

         sbuf.append(", threadState:").append(threadState);
         sbuf.append("<br/>");
         if (stackTrace.length > 0)
         {
            sbuf.append("<blockquote>");

            printLockName(sbuf, "waiting on", lockName);
            
            for (int i = 0; i < stackTrace.length; i++)
            {
               sbuf.append(stackTrace[i]).append("<br/>");
               for (Object monitor : monitors)
               {
                  int lockedStackDepth = (Integer)getLockedStackDepth.invoke(monitor, NO_PARAMS);
                  if (lockedStackDepth == i)
                  {
                     printLockName(sbuf, "locked", monitor.toString()); 
                  }
               }
            }
            
            Object[] synchronizers = (Object[])getLockedSynchronizers.invoke(threadInfo, NO_PARAMS);
            if (synchronizers.length > 0)
            {
               sbuf.append("<br/>").append("<b>Locked synchronizers</b> : ").append("<br/>");
               for (Object synchronizer : synchronizers)
               {
                  printLockName(sbuf, "locked", synchronizer.toString());
               }
            }
            
            sbuf.append("</blockquote>");
         }         
      }
      else
      {
         sbuf.append("<br/>");
      }
   }
   
   private void printLockName(StringBuffer sbuf, String status, String lockName)
   {
      if (lockName != null)
      {
         String[] lockInfo = lockName.split("@");
         sbuf.append("- " + status + " <0x" + lockInfo[1] + "> (a " + lockInfo[0] + ")").append("<br/>");
      }
   }

   private void findDeadlockedThreads(StringBuffer rc)
   {
      if (Java.isCompatible(Java.VERSION_1_6) && isSynchronizerUsageSupported)
      {
         findDeadlockedThreadsMonitorsOrSynchronisers(rc);
      }
      else
      {
         findDeadlockedThreadsOnlyMonitors(rc);
      }
   }
   
   private void findDeadlockedThreadsMonitorsOrSynchronisers(StringBuffer sb)
   {
      try
      {
         long[] ids = (long[])findDeadlockedThreads.invoke(threadMXBean, NO_PARAMS);
         if (ids == null)
         {
            return;
         }
         
         ThreadInfo[] threadsInfo = (ThreadInfo[])getThreadInfoWithSyncInfo.invoke(threadMXBean, 
               new Object[] {ids, isObjectMonitorUsageSupported, isSynchronizerUsageSupported});
         
         sb.append("<br/><b>Found deadlock(s)</b> : <br/><br/>");
         
         for (ThreadInfo threadInfo : threadsInfo)
         {
            sb.append("<b>");
            sb.append("Thread: " + threadInfo.getThreadName());
            sb.append("</b> : ");
            outputJdk6ThreadMXBeanInfo(sb, threadInfo);
         }         
      }
      catch(Exception ignore)
      {
         log.debug("Exception to be ignored", ignore);
      }
   }
   
   private void findDeadlockedThreadsOnlyMonitors(StringBuffer sb)
   {
      try
      {
         long[] ids = threadMXBean.findMonitorDeadlockedThreads();
         if (ids == null)
         {
            return;
         }
         
         ThreadInfo[] threadsInfo = threadMXBean.getThreadInfo(ids, Integer.MAX_VALUE);
         
         sb.append("<br/><b>Found deadlock(s)</b> : <br/><br/>");
         
         for (ThreadInfo threadInfo : threadsInfo)
         {
            sb.append("<b>");
            sb.append("Thread: " + threadInfo.getThreadName());
            sb.append("</b> : ");
            outputJdk5ThreadMXBeanInfo(sb, threadInfo);
         }
      }
      catch(Exception ignore)
      {
         log.debug("Exception to be ignored", ignore);
      }
   }
   
   private String getVMArguments()
   {
      String args = "";
      RuntimeMXBean rmBean = ManagementFactory.getRuntimeMXBean();
      List<String> inputArguments = rmBean.getInputArguments();
      for (String arg : inputArguments) 
      {
          args += arg + " ";
      }
      return args;
   }
   
   /**
    * Display the java.lang.Package info for the pkgName
    */
   public String displayPackageInfo(String pkgName)
   {
      Package pkg = Package.getPackage(pkgName);
      if( pkg == null )
         return "<h2>Package:"+pkgName+" Not Found!</h2>";

      StringBuffer info = new StringBuffer("<h2>Package: "+pkgName+"</h2>");
      displayPackageInfo(pkg, info);
      return info.toString();
   }

   private void displayPackageInfo(Package pkg, StringBuffer info)
   {
      info.append("<pre>\n");
      info.append("SpecificationTitle: "+pkg.getSpecificationTitle());
      info.append("\nSpecificationVersion: "+pkg.getSpecificationVersion());
      info.append("\nSpecificationVendor: "+pkg.getSpecificationVendor());
      info.append("\nImplementationTitle: "+pkg.getImplementationTitle());
      info.append("\nImplementationVersion: "+pkg.getImplementationVersion());
      info.append("\nImplementationVendor: "+pkg.getImplementationVendor());
      info.append("\nisSealed: "+pkg.isSealed());
      info.append("</pre>\n");
   }
   
   ///////////////////////////////////////////////////////////////////////////
   //                               Inner                                   //
   ///////////////////////////////////////////////////////////////////////////
   
   /*
    * Inner Helper class for fancy text graphs
    * 
    * @author dimitris@jboss.org
    */
   private static class TextGraphHelper
   {
      // number conversions
      static final DecimalFormat formatter = new DecimalFormat("#.##");      
      static final long KILO = 1024;
      static final long MEGA = 1024 * 1024;
      static final long GIGA = 1024 * 1024 * 1024;
      
      // how many dashes+pipe is 100%
      static final int factor = 70;
      static char[] fixedline;
      static char[] baseline;
      static char[] barline;
      static char[] spaces;
      static
      {
         // cache a couple of Strings
         StringBuffer sbuf0 = new StringBuffer();
         StringBuffer sbuf1 = new StringBuffer();
         StringBuffer sbuf2 = new StringBuffer();
         StringBuffer sbuf3 = new StringBuffer();
         sbuf0.append('+');
         sbuf1.append('|');
         sbuf2.append('|');
         for (int i = 1; i < factor; i++)
         {
            sbuf0.append('-');
            sbuf1.append('-');
            sbuf2.append('/');
            sbuf3.append(' ');
         }
         sbuf0.append('+');
         fixedline = sbuf0.toString().toCharArray();
         baseline = sbuf1.toString().toCharArray();
         barline = sbuf2.toString().toCharArray();
         spaces = sbuf3.toString().toCharArray();
      }
      
      private TextGraphHelper()
      {
         // do not instantiate
      }
      
      /*
       * Make a text graph of a memory pool usage:
       * 
       * +---------------------------| committed:10Mb
       * +-------------------------------------------------+
       * |////////////////           |                     | max:20Mb
       * +-------------------------------------------------+
       * +---------------| used:3Mb
       *
       * When max is unknown assume max == committed
       * 
       * |-------------------------------------------------| committed:10Mb
       * +-------------------------------------------------+
       * |////////////////                                 | max:-1
       * +-------------------------------------------------+
       * |---------------| used:3Mb
       */      
      public static void poolUsage(StringBuffer sbuf, long used, long committed, long max)
      {
         // there is a chance that max is not provided (-1)
         long assumedMax = (max == -1) ? committed : max;
         // find out bar lengths
         int localUsed = (int)(factor * used / assumedMax);
         int localCommitted = (int)(factor * committed / assumedMax);
         int localMax = factor;

         sbuf.append("<blockquote><br/>");
         sbuf.append(baseline, 0, localCommitted).append("| committed:").append(outputNumber(committed)).append("<br/>");
         sbuf.append(fixedline).append("<br/>");
         
         // the difficult part
         sbuf.append(barline, 0, localUsed);
         if (localUsed < localCommitted)
         {
            sbuf.append(localUsed > 0 ? '/' : '|');
            sbuf.append(spaces, 0, localCommitted - localUsed - 1);            
         }
         sbuf.append('|');
         if (localCommitted < localMax)
         {
            sbuf.append(spaces, 0, localMax - localCommitted - 1);            
            sbuf.append('|');
         }
         sbuf.append(" max:").append(outputNumber(max)).append("<br/>");
         
         sbuf.append(fixedline).append("<br/>");
         sbuf.append(baseline, 0, localUsed).append("| used:").append(outputNumber(used));
         sbuf.append("</blockquote>");
      }
      
      private static String outputNumber(long value)
      {     
         if (value >= GIGA)
         {
            return formatter.format((double)value / GIGA) + "Gb";
         }
         else if (value >= MEGA)
         {
            return formatter.format((double)value / MEGA) + "Mb";
         }
         else if (value >= KILO)
         {
            return formatter.format((double)value / KILO) + "Kb";
         }
         else if (value >= 0)
         {
            return value + "b";
         }
         else
         {
            return Long.toString(value);
         }
      }
   }
   
   private static class ThreadCPU implements Comparable
   {
      public String name;
      public long cpuTime;

      public ThreadCPU(String name, long cpuTime)
      {
         this.name = name;
         this.cpuTime = cpuTime / 1000000; // convert to millis
      }
      
      public int compareTo(Object o)
      {
         ThreadCPU other = (ThreadCPU) o;
         long value = cpuTime - other.cpuTime;
         if (value > 0)
            return -1;
         else if (value < 0)
            return +1;
         else
            return name.compareTo(other.name);
      }
   }
   
   /*
    * Simple data holder
    */
   private static class ThreadGroupCount
   {
      public int threads;
      public int groups;
   }
}
