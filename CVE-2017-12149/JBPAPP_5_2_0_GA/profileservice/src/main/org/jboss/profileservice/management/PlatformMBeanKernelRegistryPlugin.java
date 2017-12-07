/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
import org.jboss.system.ServiceController;

/**
 * A KernelRegistryPlugin that make the j2se platform mbeans visible.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 87013 $
 */
public class PlatformMBeanKernelRegistryPlugin implements KernelRegistryPlugin
{
   private Map<String, KernelRegistryEntry> mbeans = new HashMap<String, KernelRegistryEntry>();
   /** The service controller */
   private ServiceController serviceController;
   private MBeanServer mbeanServer;
   
   public ServiceController getServiceController()
   {
      return serviceController;
   }
   public void setServiceController(ServiceController serviceController)
   {
      this.serviceController = serviceController;
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }
   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public KernelRegistryEntry getEntry(Object name)
   {
      String key;
      ObjectName oname = null;
      if(name instanceof ObjectName)
      {
         oname = ObjectName.class.cast(name);
         key = oname.getCanonicalName();
      }
      else
         key = name.toString();
      KernelRegistryEntry entry = mbeans.get(key);
      // If
      if(entry == null && oname != null)
      {
         System.out.println("Searching for MBean: "+oname);
         try
         {
            ObjectInstance oi = mbeanServer.getObjectInstance(oname);
            System.out.println("Found "+oname+", "+oi.getClassName());
         }
         catch (InstanceNotFoundException e)
         {
            e.printStackTrace();
         }
      }
      return entry;
   }

   public void start()
      throws Throwable
   {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();

      // ClassLoadingMXBean
      ClassLoadingMXBean clmbean = ManagementFactory.getClassLoadingMXBean();
      ObjectName clname = new ObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
      PlatformMBeanIDC clidc = new PlatformMBeanIDC(server, clname, clmbean);
      mbeans.put(clname.getCanonicalName(), clidc);

      // CompilationMXBean
      CompilationMXBean cmbean = ManagementFactory.getCompilationMXBean();
      ObjectName cmname = new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME);
      PlatformMBeanIDC cmidc = new PlatformMBeanIDC(server, cmname, cmbean);
      mbeans.put(cmname.getCanonicalName(), cmidc);

      // GarbageCollectorMXBeans
      List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
      for(GarbageCollectorMXBean mbean : gcbeans)
      {
         String name = mbean.getName();
         ObjectName oname = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+name);
         String cname = oname.getCanonicalName();
         PlatformMBeanIDC gcidc = new PlatformMBeanIDC(server, oname, mbean);
         mbeans.put(cname, gcidc);
      }
      // getMemoryManagerMXBeans
      List<MemoryManagerMXBean> mmbeans = ManagementFactory.getMemoryManagerMXBeans();
      for(MemoryManagerMXBean mbean : mmbeans)
      {
         String name = mbean.getName();
         ObjectName oname = new ObjectName(ManagementFactory.MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE+name);
         String cname = oname.getCanonicalName();
         PlatformMBeanIDC mmidc = new PlatformMBeanIDC(server, oname, mbean);
         mbeans.put(cname, mmidc);
      }
      // MemoryMXBean
      MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
      ObjectName mxname = new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
      PlatformMBeanIDC mxidc = new PlatformMBeanIDC(server, mxname, mxbean);
      mbeans.put(mxname.getCanonicalName(), mxidc);
      
      // MemoryPoolMXBeans
      List<MemoryPoolMXBean> mpbeans = ManagementFactory.getMemoryPoolMXBeans();
      for(MemoryPoolMXBean mbean : mpbeans)
      {
         String name = mbean.getName();
         ObjectName oname = new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+name);
         String cname = oname.getCanonicalName();
         PlatformMBeanIDC mpidc = new PlatformMBeanIDC(server, oname, mbean);
         mbeans.put(cname, mpidc);
      }
      // OperatingSystemMXBean
      OperatingSystemMXBean osbean = ManagementFactory.getOperatingSystemMXBean();
      ObjectName osname = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
      PlatformMBeanIDC osidc = new PlatformMBeanIDC(server, osname, osbean);
      mbeans.put(osname.getCanonicalName(), osidc);

      // RuntimeMXBean
      RuntimeMXBean rtbean = ManagementFactory.getRuntimeMXBean();
      ObjectName rtname = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
      PlatformMBeanIDC rtidc = new PlatformMBeanIDC(server, rtname, rtbean);
      mbeans.put(rtname.getCanonicalName(), rtidc);

      // ThreadMXBean
      ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
      ObjectName tname = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
      PlatformMBeanIDC tidc = new PlatformMBeanIDC(server, tname, tbean);
      mbeans.put(tname.getCanonicalName(), tidc);
   }
   public void stop()
   {
      
   }
}
