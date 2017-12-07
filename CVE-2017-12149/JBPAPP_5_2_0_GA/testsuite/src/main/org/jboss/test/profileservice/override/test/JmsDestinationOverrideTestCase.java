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
package org.jboss.test.profileservice.override.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Test updating a Queue and Topic.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 106340 $
 */
public class JmsDestinationOverrideTestCase extends AbstractProfileServiceTest
{
   
   /** The queue type. */
   public static final ComponentType QueueType = KnownComponentTypes.JMSDestination.Queue.getType();
   /** The topic type. */
   public static final ComponentType TopicType = KnownComponentTypes.JMSDestination.Topic.getType();
   
   /** The meta type. */
   protected static final MapCompositeMetaType securityConfType;
   
   /** The composite meta type. */
   public static MutableCompositeMetaType composite;
   
   static
   {
      // Create the meta type
      composite = new MutableCompositeMetaType("SecurityConfig", "The security config");
      composite.addItem("read", "read permission", SimpleMetaType.BOOLEAN);
      composite.addItem("write", "write permission", SimpleMetaType.BOOLEAN);
      composite.addItem("create", "create permission", SimpleMetaType.BOOLEAN);
      composite.freeze();
      securityConfType = new MapCompositeMetaType(composite);
   }
   
   public JmsDestinationOverrideTestCase(String name)
   {
      super(name);
   }
   
   public void testQueue() throws Throwable
   {
      final String deploymentName = "profileservice-testQueue-service.xml";
      try
      {
         deployPackage(deploymentName);
         
         ManagementView mgtView = getManagementView();
         ManagedDeployment md = mgtView.getDeployment(deploymentName);
         assertNotNull(md);

         // Modify 
         ManagedComponent component = md.getComponent("testQueue");
         assertNotNull(component);
         
         ManagedProperty property = component.getProperty("downCacheSize");
         assertNotNull(property);
         assertEquals(property.getValue(), SimpleValueSupport.wrap(2000));
         
         property.setValue(SimpleValueSupport.wrap(3000));
         
         mgtView.updateComponent(component);

         // Remove
         component = md.getComponent("testRemoveQueue");
         assertNotNull(component);
         
         //
         mgtView.removeComponent(component);
         redeploy(component.getDeployment().getName());
         
         // Check removed
         mgtView = getManagementView();
         component = mgtView.getComponent("testRemoveQueue", QueueType);
         assertNull("component removed", component);
         
         // Check updated 
         component = md.getComponent("testQueue");
         assertNotNull(component);
         
         property = component.getProperty("downCacheSize");
         assertNotNull(property);
         assertEquals(property.getValue(), SimpleValueSupport.wrap(3000));
         
      }
      catch(Throwable e)
      {
         getLog().error("Caugt exception: ", e);
         throw e;
      }
   }
   
   public void testQueueTemplate() throws Exception
   {
      String jndiName = getName();
      ManagementView mgtView = getManagementView();
      
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      
      propValues.put("name", SimpleValueSupport.wrap("queueTemplate"));
      propValues.put("JNDIName", SimpleValueSupport.wrap(jndiName));
      
      ComponentType type = KnownComponentTypes.JMSDestination.Queue.getType();
      DeploymentTemplateInfo info = mgtView.getTemplate("QueueTemplate");
      
      // update values
      Map<String, ManagedProperty> props = info.getProperties();
      for(String propName : propValues.keySet())
      {
         ManagedProperty prop = props.get(propName);
         log.debug("createComponentTest("+propName+") before: "+prop.getValue());
         assertNotNull("property " + propName + " found in template " + jndiName, prop);
         prop.setValue(propValues.get(propName));
         log.debug("createComponentTest("+propName+") after: "+prop.getValue());
      }

      mgtView.applyTemplate(jndiName, info);
      
      // update security Config
      ManagedComponent queue = mgtView.getComponent("testQueueTemplate", type);
      assertNotNull(queue);
      assertEquals("testQueueTemplate", queue.getName()); 
      
      // Test with a empty value
      queue.getProperty("securityConfig").setValue(new MapCompositeValueSupport(new HashMap<String, MetaValue>(), securityConfType));
      mgtView.updateComponent(queue);
      
      //
      mgtView = getManagementView();
      queue = mgtView.getComponent("testQueueTemplate", type);
      
      Map<String, MetaValue> values = new HashMap<String, MetaValue>();
      values.put("admin", createCompositeValue(true, true, true));
      values.put("publisher", createCompositeValue(true, true, false));
      values.put("user", createCompositeValue(false, false, false));
      MapCompositeValueSupport map= new MapCompositeValueSupport(values, securityConfType);

      // Test a normal value 
      queue.getProperty("securityConfig").setValue(map);
      mgtView.updateComponent(queue);

      mgtView = getManagementView();
      queue = mgtView.getComponent("testQueueTemplate", type);
      MapCompositeValueSupport securityConfig = (MapCompositeValueSupport) queue.getProperty("securityConfig").getValue();
      assertEquals(map, securityConfig);
      
      securityConfig.remove("publisher");
      
      // Test remove
      mgtView.updateComponent(queue);
      securityConfig = (MapCompositeValueSupport) queue.getProperty("securityConfig").getValue();
      assertNotNull(securityConfig);
      assertNull(securityConfig.get("publisher"));
      
   }
   

   public void testTopic() throws Throwable
   {
      final String deploymentName = "profileservice-testTopic-service.xml";
      try
      {
         deployPackage(deploymentName);
         
         ManagementView mgtView = getManagementView();
         ManagedDeployment md = mgtView.getDeployment(deploymentName);
         assertNotNull(md);

         // Update
         ManagedComponent component = md.getComponent("testTopic");
         assertNotNull(component);

         ManagedProperty property = component.getProperty("downCacheSize");
         assertNotNull(property);
         assertEquals(property.getValue(), SimpleValueSupport.wrap(2000));
         
         property.setValue(SimpleValueSupport.wrap(3000));
         
         mgtView.updateComponent(component);
         
         // Remove
         component = md.getComponent("testRemoveTopic");
         assertNotNull(component);
         
         //
         mgtView.removeComponent(component);
         redeploy(component.getDeployment().getName());
         
         mgtView = getManagementView();
         component = mgtView.getComponent("testRemoveTopic", TopicType);
         assertNull("topic removed", component);
      }
      catch(Throwable e)
      {
         getLog().error("Caugt exception: ", e);
         throw e;
      }      
   }
   
   public void testSar() throws Exception
   {
      final String deploymentName = "profileservice-test.jar";
      try
      {
         deployPackage(deploymentName);
         ManagementView mgtView = getManagementView();

         ManagedComponent queue = mgtView.getComponent("otherTestQueue", QueueType);
         assertNotNull(queue);
         queue.getProperty("downCacheSize").setValue(SimpleValueSupport.wrap(3000));
         
         ManagedComponent topic = mgtView.getComponent("otherTestTopic", TopicType);
         assertNotNull(topic);
         topic.getProperty("downCacheSize").setValue(SimpleValueSupport.wrap(3000));
         
         mgtView.updateComponent(queue);
         mgtView.updateComponent(topic);
      }
      catch(Exception e)
      {
         throw e;
      }
   }
   
   protected CompositeValue createCompositeValue(Boolean read, Boolean write, Boolean create)
   {
      Map<String, MetaValue> map = new HashMap<String, MetaValue>();
      
      map.put("read", new SimpleValueSupport(SimpleMetaType.BOOLEAN, read));
      map.put("write", new SimpleValueSupport(SimpleMetaType.BOOLEAN, write));
      map.put("create", new SimpleValueSupport(SimpleMetaType.BOOLEAN, create));
      
      return new CompositeValueSupport(composite, map);
   }

   protected void redeploy(String name) throws Exception
   {
      DeploymentProgress progress = getDeploymentManager().redeploy(name);
      progress.run();
      if(progress.getDeploymentStatus().isFailed())
      {
         throw new IllegalStateException("Redeployment failed ", progress.getDeploymentStatus().getFailure());
      }
   }
   
   
   public static TestSuite suite()
   {
       if (JMSDestinationsUtil.isJBM())
       {
           return new TestSuite(JmsDestinationOverrideTestCase.class);
       }
       else
       {
           // empty if HQ, it doesn't make sense
           return new TestSuite(); 
       }
   }
   
}

