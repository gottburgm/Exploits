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
package org.jboss.test.profileservice.override.restart.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
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
import org.jboss.test.profileservice.override.test.AbstractProfileServiceTest;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Test if the changes to the Queue and Topic are applied after restart.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 106340 $
 */
public class JmsDestinationRestartUnitTestCase extends AbstractProfileServiceTest
{

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
   
   public JmsDestinationRestartUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testQueue() throws Exception
   {
      final String deploymentName = "profileservice-testQueue-service.xml";
      try
      {

         ManagementView mgtView = getManagementView();
         ManagedDeployment md = mgtView.getDeployment(deploymentName);
         assertNotNull(md);

         ManagedComponent component = md.getComponent("testQueue");
         assertNotNull(component);
         
         ManagedProperty property = component.getProperty("downCacheSize");
         assertNotNull(property);
         assertEquals(property.getValue(), SimpleValueSupport.wrap(3000));
      }
      catch(Exception e)
      {
         getLog().error("Caugt exception: ", e);
         throw e;
      }
      finally
      {
         undeployPackage(new String[] { deploymentName });         
      }
   }
   
   public void testQueueTemplate() throws Exception
   {
      final String deploymentName = "testQueueTemplate-service.xml";
      try
      {
         ManagementView mgtView = getManagementView();
         
         ComponentType type = KnownComponentTypes.JMSDestination.Queue.getType();
         ManagedComponent queue = mgtView.getComponent("testQueueTemplate", type);
         assertNotNull(queue);
         
         Map<String, MetaValue> values = new HashMap<String, MetaValue>();
         values.put("admin", createCompositeValue(true, true, true));
         values.put("user", createCompositeValue(false, false, false));
         MapCompositeValueSupport map = new MapCompositeValueSupport(values, securityConfType);
         
         ManagedProperty p = queue.getProperty("securityConfig");
         assertNotNull("security config property", p);
         MetaValue v = p.getValue();
         assertNotNull("securityConfig", v);
         //
         assertTrue("security equals", map.equals(v));
      }
      finally
      {
         undeployPackage(new String[] { deploymentName });
      }
   }
   

   public void testTopic() throws Exception
   {
      final String deploymentName = "profileservice-testTopic-service.xml";
      try
      {
         ManagementView mgtView = getManagementView();
         ManagedDeployment md = mgtView.getDeployment(deploymentName);
         assertNotNull(md);

         ManagedComponent component = md.getComponent("testTopic");
         assertNotNull(component);

         ManagedProperty property = component.getProperty("downCacheSize");
         assertNotNull(property);

         assertEquals(property.getValue(), SimpleValueSupport.wrap(3000));
         
      }
      catch(Exception e)
      {
         getLog().error("Caugt exception: ", e);
         throw e;
      }
      finally
      {
         undeployPackage(new String[] { deploymentName });
      }
   }
 
   public void testSar() throws Exception
   {
      final String deploymentName = "profileservice-test.jar";
      try
      {
         deployPackage(deploymentName);
         ManagementView mgtView = getManagementView();

         ManagedComponent queue = mgtView.getComponent("otherTestQueue", KnownComponentTypes.JMSDestination.Queue.getType());
         assertNotNull(queue);
         
         ManagedComponent topic = mgtView.getComponent("otherTestTopic", KnownComponentTypes.JMSDestination.Topic.getType());
         assertNotNull(topic);
      }
      finally
      {
         undeployPackage(new String[] { deploymentName });
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
   
   public static TestSuite suite()
   {
       if (JMSDestinationsUtil.isJBM())
       {
           return new TestSuite(JmsDestinationRestartUnitTestCase.class);
       }
       else
       {
           // empty if HQ, it doesn't make sense
           return new TestSuite(); 
       }
   }
   
   
}

