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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.channelfactory.managed.ManagedObjectTestUtil;
import org.jboss.virtual.VFS;

/**
 * Validates the expected ChannelFactory-related ManagedObjects are there
 * 
 * @author Brian Stansberry
 * @version $Revision: 90011 $
 */
public class ChannelFactoryManagedObjectsTestCase
   extends JBossClusteredTestCase
{
   protected ManagementView activeView;

   public ChannelFactoryManagedObjectsTestCase(String name)
   {
      super(name);
   }
   
   /**
    * Look at the JGroups ChannelFactory ManagedComponent
    * @throws Exception
    */
   public void testChannelFactory()
      throws Exception
   {
      ManagedComponent mc = getChannelFactoryManagedComponent();
      assertNotNull(mc);
      assertEquals("JChannelFactory", mc.getNameType());
      assertEquals("JChannelFactory", mc.getName());
      
      for (Map.Entry<String, ManagedProperty> entry : mc.getProperties().entrySet())
      {
         getLog().debug(entry.getKey() + " == " + entry.getValue());
         ManagedObject mo = entry.getValue().getTargetManagedObject();
         if (mo != null)
         {
            getLog().debug(entry.getKey() + " -- ManagedObject == " + mo);
         }
      }
      
      ManagedProperty prop = mc.getProperty("domain");
      assertNotNull("ChannelFactory has property domain", prop);
      MetaValue metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      Object val = ((SimpleValue) metaVal).getValue();
      assertEquals("jboss.jgroups", val);
      
      prop = mc.getProperty("exposeChannels");
      assertNotNull("ChannelFactory has property exposeChannels", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertEquals(Boolean.TRUE, val);
      
      prop = mc.getProperty("exposeProtocols");
      assertNotNull("ChannelFactory has property exposeProtocols", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertEquals(Boolean.TRUE, val);
      
      prop = mc.getProperty("nodeName");
      assertNotNull("ChannelFactory has property nodeName", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertNotNull(val);
      assertTrue(val instanceof String);
      
      prop = mc.getProperty("assignLogicalAddresses");
      assertNotNull("ChannelFactory has property assignLogicalAddresses", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertEquals(Boolean.TRUE, val);
      
      prop = mc.getProperty("manageNewThreadClassLoader");
      assertNotNull("ChannelFactory has property manageNewThreadClassLoader", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertEquals(Boolean.TRUE, val);
      
      prop = mc.getProperty("manageReleasedThreadClassLoader");
      assertNotNull("ChannelFactory has property manageReleasedThreadClassLoader", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertTrue(val instanceof Boolean);
      
      prop = mc.getProperty("addMissingSingletonName");
      assertNotNull("ChannelFactory has property addMissingSingletonName", prop);
      metaVal = prop.getValue();
      assertTrue(metaVal instanceof SimpleValue);
      val = ((SimpleValue) metaVal).getValue();
      assertEquals(Boolean.TRUE, val);
      
      prop = mc.getProperty("state");
      assertNotNull("ChannelFactory has property state", prop);

      // FIXME ProfileService messes with this prop -- figure out how to validate
//      metaVal = prop.getValue();      
//      assertNotNull(metaVal);
//      assertTrue(metaVal instanceof SimpleValue);
//      val = ((SimpleValue) metaVal).getValue();
//      assertEquals(ServiceMBean.states[ServiceMBean.STARTED], val);
      
      prop = mc.getProperty("protocolStackConfigurations");
      assertNotNull("ChannelFactory has property protocolStackConfigurations", prop);
      metaVal = prop.getValue();
      String defaultStack = System.getProperty("jboss.default.jgroups.stack", "udp");
      String[] expectedStacks = new String[]{defaultStack, "jbm-control", "jbm-data"};
      ManagedObjectTestUtil.validateProtocolStackConfigurations(metaVal, expectedStacks);
      
      prop = mc.getProperty("openChannels");
      assertNotNull("ChannelFactory has property openChannels", prop);
      try
      {
         metaVal = prop.getValue();
      }
      catch (UndeclaredThrowableException ute)
      {
         log.error("Undeclared throwable: ", ute.getUndeclaredThrowable());
      }
      ManagedObjectTestUtil.validateOpenChannels(metaVal);
   }

   public void testUpdateProtocolStackConfigurations() throws Exception
   {
      // Don't run this test if we're running the cluster-ec2 config
      // Detect that by seeing if the default stack is tcp
      if ("tcp".equals(System.getProperty("jboss.default.jgroups.stack", "udp")))
         return;
      
      ManagedComponent component = getChannelFactoryManagedComponent();
      ManagedProperty prop = component.getProperty("protocolStackConfigurations");
      MetaValue mapValue = prop.getValue();
      assertTrue(mapValue instanceof CompositeValue);
      MetaValue stackValue = ((CompositeValue) mapValue).get("udp-async");
      assertTrue(stackValue instanceof CompositeValue);
      MetaValue configurationValue = ((CompositeValue) stackValue).get("configuration");
      assertTrue(configurationValue instanceof CollectionValue);
      MetaValue[] protocols = ((CollectionValue) configurationValue).getElements();
      MetaValue udp = protocols[0];
      assertTrue(udp instanceof CompositeValue);
      MetaValue parametersValue = ((CompositeValue) udp).get("protocolParameters");
      assertTrue(parametersValue instanceof CompositeValue);
      Set<String> params = ((CompositeValue) parametersValue).getMetaType().keySet();
      int maxThreads = -1;
      MapCompositeValueSupport newVal = null;
      for (String name : params)
      {         
         if ("oob_thread_pool.max_threads".equals(name))
         {
            CompositeValue param = (CompositeValue) ((CompositeValue) parametersValue).get(name);
            String value = (String) ((SimpleValue) param.get("value")).getValue();
            maxThreads = Integer.parseInt(value);
            newVal = cloneCompositeValue(param);
            newVal.put("value", SimpleValueSupport.wrap(String.valueOf(maxThreads + 1)));
            break;
         }
      }
      assertNotNull("updated max_threads config", newVal);
      
      MapCompositeValueSupport newParametersValue = cloneCompositeValue((CompositeValue) parametersValue);
      newParametersValue.put("oob_thread_pool.max_threads", newVal);
      MapCompositeValueSupport newUdp = cloneCompositeValue((CompositeValue) udp);
      newUdp.put("protocolParameters", newParametersValue);
      protocols[0] = newUdp;
      CollectionValue newConfigurationValue = 
         new CollectionValueSupport(((CollectionValue) configurationValue).getMetaType(), protocols);
      MapCompositeValueSupport updatedStack = cloneCompositeValue((CompositeValue) stackValue);
      updatedStack.put("configuration", newConfigurationValue);
      MapCompositeValueSupport newMapValue = cloneCompositeValue((CompositeValue) mapValue);
      newMapValue.put("udp-async", updatedStack);
      
      // Add a stack
      MapCompositeValueSupport newStack = cloneCompositeValue((CompositeValue) stackValue);
      newStack.put("name", SimpleValueSupport.wrap("new-stack"));
      newMapValue.put("new-stack", newStack);
      
      // Remove a stack
      newMapValue.remove("tcp-async");
      
      // Store the updates
      prop.setValue(newMapValue);
      getManagementView().updateComponent(component);
      
      // Re-read the component and validate the changes took
      
      component = getChannelFactoryManagedComponent();
      prop = component.getProperty("protocolStackConfigurations");
      mapValue = prop.getValue();
      assertTrue(mapValue instanceof CompositeValue);
      stackValue = ((CompositeValue) mapValue).get("udp-async");
      assertTrue(stackValue instanceof CompositeValue);
      configurationValue = ((CompositeValue) stackValue).get("configuration");
      assertTrue(configurationValue instanceof CollectionValue);
      protocols = ((CollectionValue) configurationValue).getElements();
      udp = protocols[0];
      assertTrue(udp instanceof CompositeValue);
      parametersValue = ((CompositeValue) udp).get("protocolParameters");
      assertTrue(parametersValue instanceof CompositeValue);
      params = ((CompositeValue) parametersValue).getMetaType().keySet();
      boolean sawIt = false;
      for (String name : params)
      {
         if ("oob_thread_pool.max_threads".equals(name))
         {
            CompositeValue param = (CompositeValue) ((CompositeValue) parametersValue).get(name);
            
            String value = (String) ((SimpleValue) param.get("value")).getValue();
            assertEquals(String.valueOf(maxThreads + 1), value);
            sawIt = true;
            break;
         }
      }
      assertTrue(sawIt);
      
      assertTrue(((CompositeValue) mapValue).containsKey("new-stack"));      
      assertFalse(((CompositeValue) mapValue).containsKey("tcp-async"));
   }

   private ManagedComponent getChannelFactoryManagedComponent() throws Exception
   {
      ManagementView mgtView = getManagementView(); 
      ComponentType type = new ComponentType("MCBean", "JGroupsChannelFactory");
      ManagedComponent mc = mgtView.getComponent("JChannelFactory", type);
      return mc;
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
   
   private static MapCompositeValueSupport cloneCompositeValue(CompositeValue toClone)
   {
      if (toClone instanceof MapCompositeValueSupport)
      {
         return (MapCompositeValueSupport) toClone.clone();
      }
      else
      {
         CompositeMetaType type = toClone.getMetaType();
         Map<String, MetaValue> map = new HashMap<String, MetaValue>();
         for (String key : type.keySet())
         {
            map.put(key, toClone.get(key));
         }
         return new MapCompositeValueSupport(map, type);
      }
   }

}
