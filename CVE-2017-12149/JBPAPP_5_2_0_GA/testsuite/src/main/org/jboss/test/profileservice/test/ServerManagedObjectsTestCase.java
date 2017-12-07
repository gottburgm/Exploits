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

import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.RunState;
import org.jboss.managed.plugins.ManagedOperationMatcher;
import org.jboss.metatype.api.types.EnumMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.values.EnumValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.profileservice.management.matchers.AliasMatcher;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossTestCase;
import org.jboss.virtual.VFS;

/**
 * Tests of key server bean managed object views
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 88775 $
 */
public class ServerManagedObjectsTestCase
   extends JBossTestCase
{
   protected ManagementView activeView;

   public ServerManagedObjectsTestCase(String name)
   {
      super(name);
   }

   /**
    * Validate the ServerInfo component
    * @throws Exception
    */
   public void testServerInfo()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.reload();
      ComponentType type = new ComponentType("MCBean", "ServerInfo");
      getLog().debug("MCBeans: "+mgtView.getComponentsForType(type));
      ManagedComponent mc = mgtView.getComponent("jboss.system:type=ServerInfo", type);
      assertNotNull(mc);
      // Serach by alias for the jmx name
      AliasMatcher matcher = new AliasMatcher();
      Set<ManagedComponent> mcs = mgtView.getMatchingComponents("jboss.system:type=ServerInfo", type, matcher);
      log.debug("jboss.system:type=ServerInfo components: "+mcs);
      assertEquals("Found one MC for alias", 1, mcs.size());
      Map<String, ManagedProperty> props = mc.getProperties();
      getLog().info(props);
      // , activeThreadGroupCount, hostAddress, OSVersion, javaVMName, totalMemory, activeThreadCount, alias, hostName, javaVMVendor, javaVendor, javaVMVersion, OSName, javaVersion
      // maxMemory
      ManagedProperty maxMemory = props.get("maxMemory");
      long maxMemoryValue = getLong(maxMemory);
      assertTrue("maxMemory > 1MB", maxMemoryValue > 1024*1024);
      // freeMemory
      ManagedProperty freeMemory = props.get("freeMemory");
      long freeMemoryValue = getLong(freeMemory);
      assertTrue("freeMemory > 1MB", freeMemoryValue > 1024*1024);
      // TotalMemory
      ManagedProperty totalMemory = props.get("totalMemory");
      long totalMemoryValue = getLong(totalMemory);
      assertTrue("totalMemory > 1MB", totalMemoryValue > 1024*1024);
      // availableProcessors
      ManagedProperty availableProcessors = props.get("availableProcessors");
      long availableProcessorsValue = getLong(availableProcessors);
      assertTrue("availableProcessors > 0", availableProcessorsValue > 0);
      // ActiveThreadCount
      ManagedProperty activeThreadCount = props.get("activeThreadCount");
      long activeThreadCountValue = getLong(activeThreadCount);
      assertTrue("activeThreadCount > 0", activeThreadCountValue > 0);
      // ActiveThreadGroupCount
      ManagedProperty activeThreadGroupCount = props.get("activeThreadGroupCount");
      long activeThreadGroupCountValue = getLong(activeThreadGroupCount);
      assertTrue("activeThreadGroupCount > 0", activeThreadGroupCountValue > 0);
      
      // Operations
      Set<ManagedOperation> ops = mc.getOperations();
      log.info("ServerInfo.ops: "+ ops);
      ManagedOperation listThreadCpuUtilization = ManagedOperationMatcher.findOperation(ops, "listThreadCpuUtilization");
      assertNotNull(listThreadCpuUtilization);
      MetaValue listThreadCpuUtilizationMV = listThreadCpuUtilization.invoke();
      // TODO
      assertNotNull(listThreadCpuUtilizationMV);
      assertEquals(SimpleMetaType.STRING, listThreadCpuUtilizationMV.getMetaType());
      SimpleValue listThreadCpuUtilizationSV = (SimpleValue) listThreadCpuUtilizationMV;
      String cpuUtilization = (String) listThreadCpuUtilizationSV.getValue();
      log.info(cpuUtilization);
      assertTrue(cpuUtilization.length() > 100);
      

      // Try invoking listThreadCpuUtilization and checking freeMemory until it changes
      long currentFreeMemoryValue = freeMemoryValue;
      for(int n = 0; n < 100; n ++)
      {
         listThreadCpuUtilization.invoke();
         currentFreeMemoryValue = getLong(freeMemory);
         if(currentFreeMemoryValue != freeMemoryValue)
            break;
      }
      assertTrue("currentFreeMemoryValue != original freeMemoryValue",
            currentFreeMemoryValue != freeMemoryValue);

      // The bean state
      ManagedProperty state = props.get("state");
      assertNotNull("state", state);
      EnumMetaType stateType = (EnumMetaType) state.getMetaType();
      EnumValue stateValue = (EnumValue) state.getValue();
      getLog().info("state: "+stateValue);
      EnumValue installed = new EnumValueSupport(stateType, "Installed");
      assertEquals(installed, stateValue);
   }

   /**
    * Test the jboss.system:type=MCServer component from the bootstrap
    * @throws Exception
    */
   public void testMCServer()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "MCServer");
      ManagedComponent mc = mgtView.getComponent("jboss.system:type=MCServer", type);
      assertNotNull(mc);

      // Validate we can obtain the bootstrap deployment by name
      ManagedDeployment md = mc.getDeployment();
      assertNotNull(md);
      getLog().info(md);
      ManagedDeployment bootstrapMD = mgtView.getDeployment(md.getName());
      assertNotNull(bootstrapMD);

      // Validate properties, [buildOS, buildID, config, buildNumber, startDate, buildDate, versionName, buildJVM, versionNumber, version]
      Map<String, ManagedProperty> props = mc.getProperties();
      getLog().info(props);
      ManagedProperty buildOS = mc.getProperty("buildOS");
      assertNotNull(buildOS);
      ManagedProperty buildID = mc.getProperty("buildID");
      assertNotNull(buildID);
      ManagedProperty buildNumber = mc.getProperty("buildNumber");
      assertNotNull(buildNumber);
      ManagedProperty buildDate = mc.getProperty("buildDate");
      assertNotNull(buildDate);
      ManagedProperty buildJVM = mc.getProperty("buildJVM");
      assertNotNull(buildJVM);
      ManagedProperty startDate = mc.getProperty("startDate");
      assertNotNull(startDate);
      ManagedProperty versionName = mc.getProperty("versionName");
      assertNotNull(versionName);
      ManagedProperty versionNumber = mc.getProperty("versionNumber");
      assertNotNull(versionNumber);
      ManagedProperty version = mc.getProperty("version");
      assertNotNull(version);

      // The config should be the ServerConfig ManagedObject
      ManagedProperty config = mc.getProperty("config");
      assertNotNull(config);

      // This should have a shutdown operation
      Set<ManagedOperation> ops = mc.getOperations();
      MetaType[] signature = {};
      ManagedOperation shutdown = ManagedOperationMatcher.findOperation(ops, "shutdown", signature);
      assertNotNull(shutdown);
      /* Invoke it
      MetaValue[] args = {};
      shutdown.invoke(args);
      */
   }

   /**
    * 
    * @throws Exception
    */
   public void testTransactionManager()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "JTA");
      ManagedComponent mc = mgtView.getComponent("TransactionManager", type);
      assertNotNull(mc);
      Map<String, ManagedProperty> props = mc.getProperties();
      getLog().info(props);

      ManagedProperty transactionCount = props.get("transactionCount");
      assertNotNull(transactionCount);
      getLog().info("transactionCount, "+transactionCount.getValue());
      ManagedProperty commitCount = props.get("commitCount");
      assertNotNull(commitCount);
      getLog().info("commitCount, "+commitCount.getValue());
      ManagedProperty runningTransactionCount = props.get("runningTransactionCount");
      assertNotNull(runningTransactionCount);
      getLog().info("runningTransactionCount, "+runningTransactionCount.getValue());
      ManagedProperty rollbackCount = props.get("rollbackCount");
      assertNotNull(rollbackCount);
      getLog().info("rollbackCount, "+rollbackCount.getValue());
      ManagedProperty transactionTimeout = props.get("transactionTimeout");
      assertNotNull(transactionTimeout);
      getLog().info("transactionTimeout, "+transactionTimeout.getValue());
      ManagedProperty timedoutCount = props.get("timedoutCount");
      assertNotNull(timedoutCount);
      getLog().info("timedoutCount, "+timedoutCount.getValue());
      
   }

   /**
    * 
    * @throws Exception
    */
   public void testServerConfig()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "ServerConfig");
      ManagedComponent mc = mgtView.getComponent("jboss.system:type=ServerConfig", type);
      assertNotNull(mc);
      Map<String, ManagedProperty> props = mc.getProperties();
      getLog().info(props);
   }

   public void testMBeanFactory() throws Exception
   {
      ManagementView mgtView = getManagementView();
      Collection<ManagedComponent> components = mgtView.getComponentsForType(new ComponentType("MBean", "WebApplicationManager"));
      assertNotNull(components);
      
   }
   
   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected ManagementView getManagementView()
      throws Exception
   {
      if( activeView == null )
      {
         InitialContext ctx = getInitialContext();
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         activeView = ps.getViewManager();
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      activeView.load();
      return activeView;
   }

   private long getLong(ManagedProperty prop)
   {
      SimpleValue mv = (SimpleValue) prop.getValue();
      Number value = (Number) mv.getValue();
      return value.longValue();
   }
}
