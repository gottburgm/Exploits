/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.profileservice.test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.plugins.ManagedOperationMatcher;
import org.jboss.managed.plugins.jmx.ManagementFactoryUtils;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * Tests of the ManagedObjects corresponding to the server platform mbeans.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 91209 $
 */
public class PlatformMBeanUnitTestCase extends AbstractProfileServiceTest
{  
   public PlatformMBeanUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected String getProfileName()
   {
      return "profileservice";
   }

   /**
    * Get the ManagedDeployment named "JDK PlatformMBeans" and validate its
    * components and subdeployments.
    * 
    * @throws Exception
    */
   public void testPlatformMBeanDeployment()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      //mgtView.reloadProfile();
      ManagedDeployment mbeans = mgtView.getDeployment("JDK PlatformMBeans");
      assertNotNull(mbeans);

      log.debug("Root component names: "+mbeans.getComponents().keySet());
      // 
      ManagedComponent clMC = mbeans.getComponent(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
      assertNotNull(clMC);
      ManagedComponent memoryMC = mbeans.getComponent(ManagementFactory.MEMORY_MXBEAN_NAME);
      assertNotNull(memoryMC);
      ManagedComponent osMC = mbeans.getComponent(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
      assertNotNull(osMC);
      ManagedComponent runtimeMC = mbeans.getComponent(ManagementFactory.RUNTIME_MXBEAN_NAME);
      assertNotNull(runtimeMC);
      ManagedComponent threadMC = mbeans.getComponent(ManagementFactory.THREAD_MXBEAN_NAME);
      assertNotNull(threadMC);

      List<ManagedDeployment> submbeans = mbeans.getChildren();
      assertNotNull(submbeans);
      assertEquals("3", 3, submbeans.size());
      HashMap<String, ManagedDeployment> map = new HashMap<String, ManagedDeployment>();
      for(ManagedDeployment md : submbeans)
         map.put(md.getName(), md);
      // the garbage collector beans deployment
      ManagedDeployment gcMD = map.get("GarbageCollectorMXBeans");
      assertNotNull(gcMD);
      Map<String, ManagedComponent> gcMCs = gcMD.getComponents();
      assertNotNull(gcMCs);
      assertTrue(gcMCs.size() > 0);
      // the memory manager beans deployment
      ManagedDeployment mmMD = map.get("MemoryManagerMXBeans");
      assertNotNull(mmMD);
      Map<String, ManagedComponent> mmMCs = mmMD.getComponents();
      assertNotNull(mmMCs);
      assertTrue(mmMCs.size() > 0);
      // the memory pool beans deployment
      ManagedDeployment mpoolMD = map.get("MemoryPoolMXBeans");
      assertNotNull(mpoolMD);
      Map<String, ManagedComponent> mpoolMCs = mpoolMD.getComponents();
      assertNotNull(mpoolMCs);
      assertTrue(mpoolMCs.size() > 0);
   }

   /**
    * Test the ManagedObject for the ManagementFactory.CLASS_LOADING_MXBEAN_NAME
    */
   public void testClassLoadingMXBean()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MBean", "Platform");
      ManagedComponent clMC = mgtView.getComponent(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, type);
      assertEquals(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, clMC.getName());

      Map<String, ManagedProperty> props = clMC.getProperties();
      assertNotNull(props);
      // totalLoadedClassCount
      ManagedProperty totalLoadedClassCount = props.get("totalLoadedClassCount");
      assertNotNull(totalLoadedClassCount);
      assertEquals(SimpleMetaType.LONG_PRIMITIVE, totalLoadedClassCount.getMetaType());
      assertEquals("the total number of classes loaded.", totalLoadedClassCount.getDescription());
      SimpleValue totalLoadedClassCountSV = SimpleValue.class.cast(totalLoadedClassCount.getValue());
      assertNotNull(totalLoadedClassCountSV);
      getLog().debug("totalLoadedClassCountSV"+totalLoadedClassCountSV);
      SimpleValue sv1 = SimpleValueSupport.wrap(new Long(100));
      assertTrue("> 100 classes loaded", sv1.compareTo(totalLoadedClassCountSV) < 0);
      // loadedClassCount
      ManagedProperty loadedClassCount = props.get("loadedClassCount");
      assertNotNull(loadedClassCount);
      assertEquals(SimpleMetaType.INTEGER_PRIMITIVE, loadedClassCount.getMetaType());
      assertEquals("the number of currently loaded classes.", loadedClassCount.getDescription());
      SimpleValue loadedClassCountSV = SimpleValue.class.cast(loadedClassCount.getValue());
      assertNotNull(loadedClassCountSV);
      getLog().debug("loadedClassCountSV"+loadedClassCountSV);
      assertTrue("> 100 classes loaded", sv1.compareTo(loadedClassCountSV) < 0);
      // unloadedClassCount
      ManagedProperty unloadedClassCount = props.get("unloadedClassCount");
      assertNotNull(unloadedClassCount);
      assertEquals(SimpleMetaType.LONG_PRIMITIVE, unloadedClassCount.getMetaType());
      assertEquals("the total number of unloaded classes.", unloadedClassCount.getDescription());
      SimpleValue unloadedClassCountSV = SimpleValue.class.cast(unloadedClassCount.getValue());
      assertNotNull(unloadedClassCountSV);
      getLog().debug("unloadedClassCountSV"+unloadedClassCountSV);
      // verbose
      ManagedProperty verbose = props.get("verbose");
      assertNotNull(verbose);
      assertEquals(SimpleMetaType.BOOLEAN_PRIMITIVE, verbose.getMetaType());
      assertEquals("the verbose output flag for the class loading system.", verbose.getDescription());
      SimpleValue verboseSV = SimpleValue.class.cast(verbose.getValue());
      assertNotNull(verboseSV);
      getLog().debug("verboseSV"+verboseSV);
      
   }

   public void testMemoryMXBean()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MBean", "Platform");
      ManagedComponent mo = mgtView.getComponent(ManagementFactory.MEMORY_MXBEAN_NAME, type);
      assertNotNull(mo);
      assertEquals(ManagementFactory.MEMORY_MXBEAN_NAME, mo.getName());

      Map<String, ManagedProperty> props = mo.getProperties();
      assertNotNull(props);

      // heapMemoryUsage
      ManagedProperty heapMemoryUsage = props.get("heapMemoryUsage");
      assertNotNull(heapMemoryUsage);
      assertEquals("object representing the heap memory usage.", heapMemoryUsage.getDescription());
      CompositeValue heapMemoryUsageMV = CompositeValue.class.cast(heapMemoryUsage.getValue());
      assertNotNull(heapMemoryUsageMV);
      getLog().debug("heapMemoryUsageMV; "+heapMemoryUsageMV);
      MemoryUsage heapMemoryUsageMU = ManagementFactoryUtils.unwrapMemoryUsage(heapMemoryUsageMV);
      assertTrue(heapMemoryUsageMU.getInit() >= 0);
      assertTrue(heapMemoryUsageMU.getUsed() >= 1000);
      assertTrue(heapMemoryUsageMU.getMax() >= heapMemoryUsageMU.getCommitted());
      assertTrue(heapMemoryUsageMU.getCommitted() >=  heapMemoryUsageMU.getUsed());

      // nonHeapMemoryUsage
      ManagedProperty nonHeapMemoryUsage = props.get("nonHeapMemoryUsage");
      assertNotNull(nonHeapMemoryUsage);
      assertEquals("object representing the non-heap memory usage.", nonHeapMemoryUsage.getDescription());
      CompositeValue nonHeapMemoryUsageMV = CompositeValue.class.cast(nonHeapMemoryUsage.getValue());
      assertNotNull(nonHeapMemoryUsageMV);
      getLog().debug("nonHeapMemoryUsageMV; "+nonHeapMemoryUsageMV);
      MemoryUsage nonHeapMemoryUsageMU = ManagementFactoryUtils.unwrapMemoryUsage(nonHeapMemoryUsageMV);
      assertTrue(nonHeapMemoryUsageMU.getInit() >= 0);
      assertTrue(nonHeapMemoryUsageMU.getUsed() >= 1000);
      // Ignore undefined nonHeap max memory, seen with IBM JDK 6 
      if(nonHeapMemoryUsageMU.getMax() != -1)
    	  assertTrue(nonHeapMemoryUsageMU.getMax() >= nonHeapMemoryUsageMU.getCommitted());
      assertTrue(nonHeapMemoryUsageMU.getCommitted() >=  nonHeapMemoryUsageMU.getUsed());
      // objectPendingFinalizationCount
      ManagedProperty objectPendingFinalizationCount = props.get("objectPendingFinalizationCount");
      assertNotNull(objectPendingFinalizationCount);
      assertEquals("the approximate number objects for which finalization is pending.", objectPendingFinalizationCount.getDescription());
      MetaValue objectPendingFinalizationCountMV = objectPendingFinalizationCount.getValue();
      assertNotNull(objectPendingFinalizationCountMV);
      getLog().debug("objectPendingFinalizationCountMV; "+objectPendingFinalizationCountMV);

      // verbose
      ManagedProperty verbose = props.get("verbose");
      assertNotNull(verbose);
      assertEquals(SimpleMetaType.BOOLEAN_PRIMITIVE, verbose.getMetaType());
      assertEquals("the verbose output flag for the memory system.", verbose.getDescription());
      SimpleValue verboseSV = SimpleValue.class.cast(verbose.getValue());
      assertNotNull(verboseSV);
      getLog().debug("verboseSV; "+verboseSV);

      // The gc op
      Set<ManagedOperation> ops = mo.getOperations();
      assertNotNull(ops);
      assertEquals("There is 1 op", 1, ops.size());
      ManagedOperation gc = ops.iterator().next();
      assertEquals("gc", gc.getName());
      assertEquals("Runs the garbage collector", gc.getDescription());
      gc.invoke();
   }

   public void testThreadMXBean()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      //mgtView.reloadProfile();

      ComponentType type = new ComponentType("MBean", "Platform");
      ManagedComponent mo = mgtView.getComponent(ManagementFactory.THREAD_MXBEAN_NAME, type);
      assertNotNull(mo);
      assertEquals(ManagementFactory.THREAD_MXBEAN_NAME, mo.getName());
      
      // Test ThreadInfo MetaValue wrap/unwrap
      MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();

      // Properties
      ManagedProperty allThreadIds = mo.getProperty("allThreadIds");
      assertNotNull(allThreadIds);
      MetaValue allThreadIdsMV = allThreadIds.getValue();
      assertTrue("allThreadIds is Array", allThreadIdsMV instanceof ArrayValue);
      ArrayValue idsArray = ArrayValue.class.cast(allThreadIdsMV);
      assertTrue(idsArray.getLength() > 10);
      SimpleValue tid0SV = SimpleValue.class.cast(idsArray.getValue(0));
      ManagedProperty currentThreadCpuTime = mo.getProperty("currentThreadCpuTime");
      assertNotNull(currentThreadCpuTime);
      long x = (Long) metaValueFactory.unwrap(currentThreadCpuTime.getValue());
      assertTrue(x > 1000);
      ManagedProperty currentThreadUserTime = mo.getProperty("currentThreadUserTime");
      assertNotNull(currentThreadUserTime);
      ManagedProperty daemonThreadCount = mo.getProperty("daemonThreadCount");
      assertNotNull(daemonThreadCount);
      x = (Integer) metaValueFactory.unwrap(daemonThreadCount.getValue());
      assertTrue(x > 1);
      ManagedProperty peakThreadCount = mo.getProperty("peakThreadCount");
      assertNotNull(peakThreadCount);
      x = (Integer) metaValueFactory.unwrap(peakThreadCount.getValue());
      assertTrue(x > 10);
      ManagedProperty threadCount = mo.getProperty("threadCount");
      assertNotNull(threadCount);
      x = (Integer) metaValueFactory.unwrap(threadCount.getValue());
      assertTrue(x > 10);
      ManagedProperty totalStartedThreadCount = mo.getProperty("totalStartedThreadCount");
      assertNotNull(totalStartedThreadCount);
      x = (Long) metaValueFactory.unwrap(totalStartedThreadCount.getValue());
      assertTrue(x > 10);
      ManagedProperty currentThreadCpuTimeSupported = mo.getProperty("currentThreadCpuTimeSupported");
      assertNotNull(currentThreadCpuTimeSupported);
      Boolean flag = (Boolean) metaValueFactory.unwrap(currentThreadCpuTimeSupported.getValue());
      assertNotNull(flag);
      ManagedProperty threadContentionMonitoringEnabled = mo.getProperty("threadContentionMonitoringEnabled");
      assertNotNull(threadContentionMonitoringEnabled);
      flag = (Boolean) metaValueFactory.unwrap(threadContentionMonitoringEnabled.getValue());
      assertNotNull(flag);
      ManagedProperty threadContentionMonitoringSupported = mo.getProperty("threadContentionMonitoringSupported");
      assertNotNull(threadContentionMonitoringSupported);
      flag = (Boolean) metaValueFactory.unwrap(threadContentionMonitoringSupported.getValue());
      assertNotNull(flag);
      ManagedProperty threadCpuTimeEnabled = mo.getProperty("threadCpuTimeEnabled");
      assertNotNull(threadCpuTimeEnabled);
      flag = (Boolean) metaValueFactory.unwrap(threadCpuTimeEnabled.getValue());
      assertNotNull(flag);
      ManagedProperty threadCpuTimeSupported = mo.getProperty("threadCpuTimeSupported");
      assertNotNull(threadCpuTimeSupported);
      flag = (Boolean) metaValueFactory.unwrap(threadCpuTimeSupported.getValue());
      assertNotNull(flag);

      // Ops
      Set<ManagedOperation> ops = mo.getOperations();
      log.debug(ops);

      String javaSpecVersion = System.getProperty("java.specification.version");
      if (javaSpecVersion.equals("1.5") || javaSpecVersion.equals("5.0"))
         assertEquals(mo + " has wrong number of ManagedOperations.", 8, ops.size());
      else if (javaSpecVersion.equals("1.6") || javaSpecVersion.equals("6.0"))
         assertEquals(mo + " has wrong number of ManagedOperations.", 11, ops.size());

      ManagedOperation getThreadInfo = ManagedOperationMatcher.findOperation(ops,
            "getThreadInfo", SimpleMetaType.LONG_PRIMITIVE, SimpleMetaType.INTEGER_PRIMITIVE);
      assertNotNull("getThreadInfo", getThreadInfo);
      log.debug(getThreadInfo);

      MetaValue tid0InfoMV = getThreadInfo.invoke(tid0SV);
      assertNotNull(tid0InfoMV);
      assertTrue(tid0InfoMV instanceof CompositeValue);
      log.debug(tid0InfoMV);
      CompositeValue tid0InfoCV = (CompositeValue) tid0InfoMV;
      ThreadInfo tid0Info = ManagementFactoryUtils.unwrapThreadInfo(tid0InfoCV);
      log.debug(tid0Info);
   }

   public void testRuntimeMXBean()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MBean", "Platform");
      ManagedComponent mo = mgtView.getComponent(ManagementFactory.RUNTIME_MXBEAN_NAME, type);
      assertNotNull(mo);
      assertEquals(ManagementFactory.RUNTIME_MXBEAN_NAME, mo.getName());

      RuntimeMXBean mbean = ManagementFactory.getRuntimeMXBean();
      MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();
      
      ManagedProperty bootClassPath = mo.getProperty("bootClassPath");
      String x = (String) metaValueFactory.unwrap(bootClassPath.getValue());
      assertTrue(x.length() > 0);
      ManagedProperty classPath = mo.getProperty("classPath");
      x = (String) metaValueFactory.unwrap(classPath.getValue());
      assertTrue(x.length() > 0);
      ManagedProperty libraryPath = mo.getProperty("libraryPath");
      x = (String) metaValueFactory.unwrap(libraryPath.getValue());
      assertTrue(x.length() > 0);
      ManagedProperty managementSpecVersion = mo.getProperty("managementSpecVersion");
      x = (String) metaValueFactory.unwrap(managementSpecVersion.getValue());
      assertEquals(mbean.getManagementSpecVersion(), x);      
      ManagedProperty specName = mo.getProperty("specName");
      x = (String) metaValueFactory.unwrap(specName.getValue());
      assertEquals(mbean.getSpecName(), x);      
      ManagedProperty specVendor = mo.getProperty("specVendor");
      x = (String) metaValueFactory.unwrap(specVendor.getValue());
      assertEquals(mbean.getSpecVendor(), x);      
      ManagedProperty specVersion = mo.getProperty("specVersion");
      x = (String) metaValueFactory.unwrap(specVersion.getValue());
      assertEquals(mbean.getSpecVersion(), x);      
      ManagedProperty vmName = mo.getProperty("vmName");
      x = (String) metaValueFactory.unwrap(vmName.getValue());
      assertEquals(mbean.getVmName(), x);      
      ManagedProperty vmVendor = mo.getProperty("vmVendor");
      x = (String) metaValueFactory.unwrap(vmVendor.getValue());
      assertEquals(mbean.getVmVendor(), x);      
      ManagedProperty vmVersion = mo.getProperty("vmVersion");
      x = (String) metaValueFactory.unwrap(vmVersion.getValue());
      assertEquals(mbean.getVmVersion(), x);
      ManagedProperty startTime = mo.getProperty("startTime");
      long time = (Long) metaValueFactory.unwrap(startTime.getValue());
      assertTrue(time > 10000000);
      ManagedProperty uptime = mo.getProperty("uptime");
      time = (Long) metaValueFactory.unwrap(uptime.getValue());
      ManagedProperty inputArguments = mo.getProperty("inputArguments");
      List<String> ls = (List<String>) metaValueFactory.unwrap(inputArguments.getValue());
      assertTrue(ls.size() > 0);
      ManagedProperty systemProperties = mo.getProperty("systemProperties");
      MetaValue sysPropsMV = systemProperties.getValue();
      Map<String, String> sysProps = (Map<String, String>) metaValueFactory.unwrap(sysPropsMV);
      log.debug(sysProps);
      assertNotNull(sysProps.get("jboss.home.dir"));
      assertTrue(sysProps.get("jboss.home.dir") instanceof String);
      assertNotNull(sysProps.get("jboss.server.home.dir"));
      assertNotNull(sysProps.get("jboss.common.lib.url"));
      assertNotNull(sysProps.get("jboss.server.base.url"));
   }
   public void testOperatingSystemMXBean()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MBean", "Platform");
      ManagedComponent mo = mgtView.getComponent(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, type);
      assertNotNull(mo);
      assertEquals(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, mo.getName());

      ManagedProperty arch = mo.getProperty("arch");
      assertNotNull(arch);
      ManagedProperty availableProcessors = mo.getProperty("availableProcessors");
      assertNotNull(availableProcessors);
      SimpleValue procsSV = (SimpleValue) availableProcessors.getValue();
      Integer procs = (Integer) procsSV.getValue();
      assertTrue(procs.intValue() >= 1);
      ManagedProperty version = mo.getProperty("version");
      assertNotNull(version);
      log.debug(version.getValue());
   }
}
