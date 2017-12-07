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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.virtual.VFS;

/**
 * Validates the expected HAPartition-related ManagedObjects are there
 * 
 * @author Brian Stansberry
 * @version $Revision: 89852 $
 */
public class ClusterPartitionManagedObjectsTestCase
   extends JBossClusteredTestCase
{
   private static final String HAJNDI = "HAJNDI";
   
   protected ManagementView activeView;
   private String partitionName = System.getProperty("jbosstest.partitionName", "DefaultPartition");

   public ClusterPartitionManagedObjectsTestCase(String name)
   {
      super(name);
   }
   /**
    * Look at the HAPartition ManagedComponent
    * @throws Exception
    */
   public void testHAPartition()
      throws Exception
   {
      ManagementView mgtView = getManagementView(); 
      ComponentType type = new ComponentType("MCBean", "HAPartition");
      ManagedComponent mc = mgtView.getComponent(this.partitionName, type);
      validateHAPartitionManagedComponent(mc);
   }
   
   private void validateHAPartitionManagedComponent(ManagedComponent mc)
   {
      assertNotNull(mc);
      assertEquals("HAPartition", mc.getNameType());
      assertEquals("DefaultPartition", mc.getName());
      
      Map<String, ManagedOperation> operations = new HashMap<String, ManagedOperation>();
      getLog().debug(mc);
      for (ManagedOperation mo : mc.getOperations())
      {
         getLog().debug("name="+mo.getName()+",description="+mo.getDescription()+",impact="+mo.getImpact());
         operations.put(mo.getName(), mo);
      }
      
      ManagedOperation mgdop = operations.get("showHistory");
      assertNotNull("HAPartition has showHistory", mgdop); 
      MetaValue result = mgdop.invoke();
      assertNotNull(result);
      
      mgdop = operations.get("showHistoryAsXML");
      assertNotNull("HAPartition has showHistoryAsXML", mgdop); 
      result = mgdop.invoke();
      assertNotNull(result);
      
      mgdop = operations.get("getDRMServiceNames");
      assertNotNull("HAPartition has getDRMServiceNames", mgdop);
      result = mgdop.invoke();
      assertTrue(result instanceof CollectionValue);
      MetaValue[] elements = ((CollectionValue) result).getElements();
      assertNotNull(elements);
      boolean found = false;
      for (MetaValue element : elements)
      {
         assertTrue(element instanceof SimpleValue);
         if (HAJNDI.equals(((SimpleValue) element).getValue()))
         {
            found = true;
            break;
         }
      }
      assertTrue(found);
      
      mgdop = operations.get("listDRMContent");      
      assertNotNull("HAPartition has listDRMContent", mgdop); 
      result = mgdop.invoke();
      assertTrue(result instanceof SimpleValue);
      Object val = ((SimpleValue) result).getValue();
      assertTrue(val instanceof String);
      assertTrue(((String) val).indexOf(HAJNDI) > -1);
      
      mgdop = operations.get("listDRMContentAsXml");
      assertNotNull("HAPartition has listDRMContentAsXml", mgdop); 
      result = mgdop.invoke();
      assertTrue(result instanceof SimpleValue);
      val = ((SimpleValue) result).getValue();
      assertTrue(val instanceof String);
      assertTrue(((String) val).indexOf(HAJNDI) > -1);
      
      mgdop = operations.get("getDRMServiceViewId");
      assertNotNull("HAPartition has getDRMServiceViewId", mgdop);  
      MetaValue hajndiparam = SimpleValueSupport.wrap(HAJNDI);
      MetaValue[] hajndiparams = new MetaValue[]{hajndiparam}; 
      result = mgdop.invoke(hajndiparams);
      assertNotNull(result);
      
      mgdop = operations.get("lookupDRMNodeNames");
      assertNotNull("HAPartition has lookupDRMNodeNames", mgdop);
      result = mgdop.invoke(hajndiparams);
      assertTrue(result instanceof CollectionValue);
      elements = ((CollectionValue) result).getElements();
      assertNotNull(elements);
      assertEquals(2, elements.length);
      
      mgdop = operations.get("isDRMMasterForService");
      assertNotNull("HAPartition has isDRMMasterForService", mgdop); 
      result = mgdop.invoke(hajndiparams);
      assertTrue(result instanceof SimpleValue);
      val = ((SimpleValue) result).getValue();
      assertTrue(val instanceof Boolean);
      
      // FIXME test for service lifecycle
      
      assertEquals("Correct number of operations", 8, operations.size());
      
      for (Map.Entry<String, ManagedProperty> entry : mc.getProperties().entrySet())
      {
         getLog().debug(entry.getKey() + " == " + entry.getValue());
         ManagedObject mo = entry.getValue().getTargetManagedObject();
         if (mo != null)
         {
            getLog().debug(entry.getKey() + " -- ManagedObject == " + mo);
         }
      }
      
      ManagedProperty prop = mc.getProperty("stateString");
      assertNotNull("HAPartition has property stateString", prop);
      MetaValue metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("nodeName");
      assertNotNull("HAPartition has property nodeName", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("partitionName");
      assertNotNull("HAPartition has property partitionName", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("currentViewId");
      assertNotNull("HAPartition has property currentViewId", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof Long);
      
      prop = mc.getProperty("currentView");
      assertNotNull("HAPartition has property currentView", prop);
      
      prop = mc.getProperty("currentNodeCoordinator");
      assertNotNull("HAPartition has property currentNodeCoordinator", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof Boolean);
      
      prop = mc.getProperty("allowSynchronousMembershipNotifications");
      assertNotNull("HAPartition has property allowSynchronousMembershipNotifications", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof Boolean);
      
      prop = mc.getProperty("bindIntoJndi");
      assertNotNull("HAPartition has property bindIntoJndi", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof Boolean);
      
      prop = mc.getProperty("JGroupsVersion");
      assertNotNull("HAPartition has property JGroupsVersion", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("cacheConfigName");
      assertNotNull("HAPartition has property cacheConfigName", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("channelStackName");
      assertNotNull("HAPartition has property channelStackName", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("stateTransferTimeout");
      assertNotNull("HAPartition has property stateTransferTimeout", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof Long);
      
      prop = mc.getProperty("methodCallTimeout");
      assertNotNull("HAPartition has property methodCallTimeout", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof Long);
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
         String[] urls = getNamingURLs();
         Properties env = new Properties();
         env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
            "org.jnp.interfaces.NamingContextFactory");
         env.setProperty(Context.PROVIDER_URL, urls[0]);
         Context ctx = new InitialContext(env);
         
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         activeView = ps.getViewManager();
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      activeView.load();
      return activeView;
   }

   protected String getProfileName()
   {
      return "cluster-udp-0";
   }

}
