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
package org.jboss.test.profileservice.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.RunState;
import org.jboss.managed.plugins.ManagedOperationMatcher;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author Scott.Stark@jboss.org
 * @version <tt>$Revision: 110335 $<
 */
public class JmsDestinationUnitTestCase extends AbstractProfileServiceTest
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
   
   public JmsDestinationUnitTestCase(String s)
   {
      super(s);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();

      if (JMSDestinationsUtil.isJBM())
      {
          // These tests are written for JBM only
          suite.addTest(new JmsDestinationUnitTestCase("testQueueTemplate"));
          suite.addTest(new JmsDestinationUnitTestCase("testTopicTemplate"));
          suite.addTest(new JmsDestinationUnitTestCase("testDLQ"));
          suite.addTest(new JmsDestinationUnitTestCase("testCreateQueue"));
          suite.addTest(new JmsDestinationUnitTestCase("testCreateDuplicateQueue"));
          suite.addTest(new JmsDestinationUnitTestCase("testQueueMetrics"));
          suite.addTest(new JmsDestinationUnitTestCase("testQueueOperations"));
          suite.addTest(new JmsDestinationUnitTestCase("testQueueRestart"));
          suite.addTest(new JmsDestinationUnitTestCase("testRemoveQueue"));
          suite.addTest(new JmsDestinationUnitTestCase("testCreateTopic"));
          suite.addTest(new JmsDestinationUnitTestCase("testTopicMetrics"));
          suite.addTest(new JmsDestinationUnitTestCase("testTopicSubscriptions"));
          suite.addTest(new JmsDestinationUnitTestCase("testTopicOperations"));
          suite.addTest(new JmsDestinationUnitTestCase("testRemoveTopic"));
          suite.addTest(new JmsDestinationUnitTestCase("testCreateSecureQueue"));
          suite.addTest(new JmsDestinationUnitTestCase("testRemoveSecureQueue"));
          suite.addTest(new JmsDestinationUnitTestCase("testCreateQueueWithNullDLQ"));
          suite.addTest(new JmsDestinationUnitTestCase("testRemoveQueueWithNullDLQ"));
      }

      return suite;
   }  

   public void testQueueTemplate() throws Exception
   {
      DeploymentTemplateInfo info = getManagementView().getTemplate("QueueTemplate");
      assertNotNull(info);
      
      Map<String, ManagedProperty> properties = info.getProperties();
      assertNotNull(properties);
      
      List<String> items = Arrays.asList("JNDIName", "downCacheSize", "fullSize",
            "clustered", "maxDeliveryAttempts", "maxSize", "messageCounterHistoryDayLimit",
            "pageSize", "redeliveryDelay");

      properties.keySet().containsAll(items);
      // Validate the ObjectName property types
      ManagedProperty serverPeer = info.getProperties().get("serverPeer");
      assertNotNull(serverPeer);
      assertEquals(SimpleMetaType.STRING, serverPeer.getMetaType());
   }
   
   public void testTopicTemplate() throws Exception
   {
      DeploymentTemplateInfo info = getManagementView().getTemplate("TopicTemplate");
      assertNotNull(info);
      
      Map<String, ManagedProperty> properties = info.getProperties();
      assertNotNull(properties);
      
      List<String> items = Arrays.asList("JNDIName", "downCacheSize", "fullSize",
            "clustered", "maxDeliveryAttempts", "maxSize", "messageCounterHistoryDayLimit",
            "pageSize", "redeliveryDelay");

      properties.keySet().containsAll(items);
   }
   
   /**
    * Validate the default dead letter queue exists
    * @throws Exception
    */
   public void testDLQ() throws Exception
   {
      ManagementView mgtView = getManagementView();
      ManagedComponent queue = mgtView.getComponent("/queue/DLQ", QueueType);
      assertNotNull(queue);
      assertEquals("/queue/DLQ", queue.getName());
      // Validate some of the expected properties
      ManagedProperty serverPeer = queue.getProperty("serverPeer");
      assertNotNull(serverPeer);
      MetaType serverPeerType = serverPeer.getMetaType();
      assertEquals(SimpleMetaType.STRING, serverPeerType);
   }

   public void testCreateQueue() throws Exception
   {
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      String jndiName = getName();
      propValues.put("JNDIName", SimpleValueSupport.wrap(jndiName));
      propValues.put("downCacheSize", SimpleValueSupport.wrap(1999));
      propValues.put("DLQ", SimpleValueSupport.wrap("jboss.messaging.destination:name=DLQ,service=Queue"));
      propValues.put("expiryQueue", SimpleValueSupport.wrap("jboss.messaging.destination:name=ExpiryQueue,service=Queue"));
      
      ComponentType type = KnownComponentTypes.JMSDestination.Queue.getType();
      createComponentTest("QueueTemplate", propValues, getName(), type, jndiName);
      ManagedComponent queue = activeView.getComponent("testCreateQueue", type);
      assertNotNull(queue);
      assertEquals("testCreateQueue", queue.getName());
      log.info(queue.getProperties().keySet());
      assertEquals("downCacheSize", queue.getProperty("downCacheSize").getValue(), new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 1999));

      ManagedProperty serverPeer = queue.getProperty("serverPeer");
      assertNotNull(serverPeer);
      MetaType serverPeerType = serverPeer.getMetaType();
      assertEquals(SimpleMetaType.STRING, serverPeerType);
      ManagedProperty dlq = queue.getProperty("DLQ");
      assertNotNull(dlq);
      MetaType dlqType = dlq.getMetaType();
      assertEquals(SimpleMetaType.STRING, dlqType);
      ManagedProperty expiryQueue = queue.getProperty("expiryQueue");
      assertNotNull(expiryQueue);
      MetaType expiryQueueType = serverPeer.getMetaType();
      assertEquals(SimpleMetaType.STRING, expiryQueueType);
   }
   
   public void testCreateDuplicateQueue() throws Exception
   {
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      String jndiName = "testCreateQueue";
      
      ComponentType type = KnownComponentTypes.JMSDestination.Queue.getType();
      try {
    	  createComponentTest("QueueTemplate", propValues, getName(), type, jndiName);
    	  fail();
      } catch (Exception e) {
    	  // ok;
      }
   }
   
   public void testQueueMetrics() throws Exception
   {
      // Get the managed component
      ManagedComponent component = getManagementView().getComponent("testCreateQueue", QueueType);
      assertNotNull("testCreateQueue ManagedComponent", component);
      
      // Send a few messages and validate the msg counts
      sendQueueMsgs("testCreateQueue", component);
   }
   
   public void testQueueOperations() throws Exception
   {
      ManagedComponent component = getManagementView().getComponent("testCreateQueue", QueueType);

      // Send a few messages so the message based ops have values
      sendQueueMsgs("testCreateQueue", component);

      
      ManagedOperation o = getOperation(component, "listMessageCounterAsHTML", new String[0]);
      MetaValue v = o.invoke(new MetaValue[0]);
      assertNotNull("null operation return value", v);
      log.debug("result: " + v);
      
      
      // JBAS-7024,
      ManagedOperation listAllMessages = getOperation(component, "listAllMessages", new String[0]);
      MetaType listAllMessagesRT = listAllMessages.getReturnType();
      log.debug("listAllMessagesRT: " + listAllMessagesRT);
      MetaValue listAllMessagesMV = listAllMessages.invoke(new MetaValue[0]);
      assertNotNull("null operation return value", listAllMessagesMV);
      log.debug("result: " + listAllMessagesMV);
      MetaType resultType = listAllMessagesMV.getMetaType();
      log.debug("resultType: "+resultType);
      assertTrue("resultType instanceof CompositeMetaType", resultType instanceof CollectionMetaType);
      CollectionMetaType resultCMT = (CollectionMetaType) resultType;
      MetaType resultElementType = resultCMT.getElementType();
      log.debug("resultElementType: "+resultElementType);
      assertTrue("resultElementType instanceof CompositeMetaType", resultElementType instanceof CompositeMetaType);
      log.debug("resultElementType: "+resultElementType);
      CollectionValue listAllMessagesCV = (CollectionValue) listAllMessagesMV;
      MetaValue[] listAllMessagesElements = listAllMessagesCV.getElements();
      log.debug("listAllMessagesElements: "+listAllMessagesElements);
      if(listAllMessagesElements.length > 0)
      {
         MetaValue m0 = listAllMessagesElements[0];
         MetaType m0MT = m0.getMetaType();
         assertTrue("m0MT.isComposite", m0MT.isComposite());
         assertTrue("m0MT instanceof CompositeMetaType", m0MT instanceof CompositeMetaType);
         assertTrue("m0 instanceof CompositeValue", m0 instanceof CompositeValue);
         CompositeValue m0MV = (CompositeValue) m0;
         log.debug("m0MV.values: "+m0MV.values());
      }
   }
   
   public void testQueueRestart() throws Exception
   {
      ManagedComponent component = getManagementView().getComponent("testCreateQueue", QueueType);
      assertEquals(RunState.RUNNING, component.getRunState());
      // Stop
      ManagedOperation o = getOperation(component, "stop", new String[0]);
      o.invoke(new MetaValue[0]);
      // Check runState dispatching
      assertEquals(RunState.STOPPED, component.getRunState());
      // Start
      o = getOperation(component, "start", new String[0]);
      o.invoke(new MetaValue[0]);
      //
      assertEquals(RunState.RUNNING, component.getRunState());
   }

   public void testMultipleQueues() throws Exception
   {
      ManagementView managementView = getManagementView();

      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      // testCreateQueue1
      String jndiName = "testCreateQueue1";
      String templateName = "QueueTemplate";
      MetaValue jndiName1MV = SimpleValueSupport.wrap(jndiName);
      propValues.put("JNDIName", jndiName1MV);
      DeploymentTemplateInfo queue1Info = managementView.getTemplate(templateName);
      Map<String, ManagedProperty> testCreateQueue1Props = queue1Info.getProperties();
      log.debug("QueueTemplate#1: "+testCreateQueue1Props);
      for(String propName : testCreateQueue1Props.keySet())
      {
         ManagedProperty prop = testCreateQueue1Props.get(propName);
         assertNotNull("property " + propName + " found in template " + templateName, prop);
         log.debug("createComponentTest("+propName+") before: "+prop.getValue());
         prop.setValue(propValues.get(propName));
         log.debug("createComponentTest("+propName+") after: "+prop.getValue());
      }
      managementView.applyTemplate("testCreateQueue1", queue1Info);
      managementView.process();

      // testCreateQueue2
      jndiName = "testCreateQueue2";
      MetaValue jndiName2MV = SimpleValueSupport.wrap(jndiName);
      propValues.put("JNDIName", jndiName2MV);
      // Get a fresh template info view
      DeploymentTemplateInfo queue2Info = managementView.getTemplate(templateName);
      Map<String, ManagedProperty> testCreateQueue2Props = queue2Info.getProperties();
      log.debug("QueueTemplate#2: "+testCreateQueue2Props);
      // Validate the properties don't have the previous template values
      ManagedProperty jndiNameCheck1 = testCreateQueue2Props.get("JNDIName");
      assertFalse("Fresh temmplate properties does not have previous JNDIName",
            jndiName1MV.equals(jndiNameCheck1.getValue()));
      for(String propName : testCreateQueue2Props.keySet())
      {
         ManagedProperty prop = testCreateQueue2Props.get(propName);
         assertNotNull("property " + propName + " found in template " + templateName, prop);
         log.debug(propName+" before: "+prop.getValue());
         prop.setValue(propValues.get(propName));
         log.debug(propName+" after: "+prop.getValue());
      }
      managementView.applyTemplate("testCreateQueue2", queue2Info);
      managementView.process();

      // Validate the components
//      managementView.reload();
      ManagedComponent queue1 = managementView.getComponent("testCreateQueue1", QueueType);
      assertNotNull(queue1);
      assertEquals("testCreateQueue1", queue1.getName());

      ManagedComponent queue2 = managementView.getComponent("testCreateQueue2", QueueType);
      assertNotNull(queue2);
      assertEquals("testCreateQueue2", queue2.getName());
      
   }
   
   /**
    * JBAS-6939
    * @throws Exception
    */
   public void testQueueRunState() throws Exception
   {
      ManagementView mgtView = getManagementView();
      ManagedComponent queue = mgtView.getComponent("testCreateQueue", QueueType);
      assertNotNull(queue);
      assertEquals("running", RunState.RUNNING, queue.getRunState());
      ManagedOperation stop = getOperation(queue, "stop", new String[0]);
      stop.invoke(new MetaValue[0]);
      
      mgtView.reload();
      queue = mgtView.getComponent("testCreateQueue", QueueType);
      log.info("runtstate: " + queue.getRunState());
   }

   public void testRemoveQueue() throws Exception
   {
      removeDeployment("testCreateQueue-service.xml");
	   ManagedComponent queue = getManagementView().getComponent("testCreateQueue", QueueType);
      assertNull("queue should be removed" + queue, queue);
   }

   public void testCreateTopic() throws Exception
   {
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      String jndiName = getName();
      propValues.put("JNDIName", SimpleValueSupport.wrap(jndiName));
      HashSet<String> removedProps = new HashSet<String>();
      removedProps.add("serverPeer");
      createComponentTest("TopicTemplate", propValues, removedProps, getName(), TopicType, jndiName, true);
      ManagedComponent topic = activeView.getComponent("testCreateTopic", TopicType);
      assertNotNull(topic);
      assertEquals("testCreateTopic", topic.getName());
   }

   /**
    * Run after testCreateTopic to validate the topic subscriptions, list msgs.
    */
   public void testTopicSubscriptions()
      throws Exception
   {
      ComponentType type = KnownComponentTypes.JMSDestination.Topic.getType();
      ManagementView managementView = getManagementView();
      ManagedComponent topic = managementView.getComponent("testCreateTopic", type);
      assertNotNull(topic);

      // Subscribe to a topic and validate the subscription shows up in the list op
      InitialContext ctx = super.getInitialContext();
      Topic topicDest = (Topic) ctx.lookup("testCreateTopic");
      TopicConnectionFactory tcf = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");
      TopicConnection tc = tcf.createTopicConnection();
      tc.start();
      TopicSession ts = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer mc = ts.createConsumer(topicDest);
      MessageProducer mp = ts.createProducer(topicDest);
      Message helloMsg = ts.createTextMessage("Hello from testCreateTopic");
      mp.send(helloMsg);
      log.info("Producer sent: "+helloMsg);
      Message response = mc.receive();
      log.info("Consumer saw: "+response);

      // Check some stats
      log.info(topic.getProperties().keySet());
      ManagedProperty NonDurableSubscriptionsCount = topic.getProperty("nonDurableSubscriptionsCount");
      assertNotNull(NonDurableSubscriptionsCount);
      log.info(NonDurableSubscriptionsCount);
      SimpleValue NonDurableSubscriptionsCountMV = (SimpleValue) NonDurableSubscriptionsCount.getValue();
      log.info(NonDurableSubscriptionsCountMV);
      assertTrue(NonDurableSubscriptionsCountMV.compareTo(SimpleValueSupport.wrap(0)) > 0);

      Set<ManagedOperation> ops = topic.getOperations();
      log.info("Topic ops: "+ops);
      Map<String, ManagedOperation> opsByName = new HashMap<String, ManagedOperation>();
      for(ManagedOperation op : ops)
         opsByName.put(op.getName(), op);
      ManagedOperation listNonDurableSubscriptions = opsByName.get("listNonDurableSubscriptions");
      assertNotNull(listNonDurableSubscriptions);
      MetaValue subscriptions = listNonDurableSubscriptions.invoke();
      log.info(subscriptions);
      assertTrue(subscriptions instanceof CollectionValue);
      CollectionValue subscriptionsCV = (CollectionValue) subscriptions;
      assertTrue("subscriptions.size > 0", subscriptionsCV.getSize() > 0);
      MetaValue[] subscriptionsMVs = subscriptionsCV.getElements();
      for(MetaValue mv : subscriptionsMVs)
      {
         CompositeValue cv = (CompositeValue) mv;
         MetaValue name = cv.get("name");
         log.info(name);
         MetaValue clientID = cv.get("clientID");
         log.info(clientID);
         MetaValue durable = cv.get("durable");
         log.info(durable);
         MetaValue selector = cv.get("selector");
         log.info(selector);
         MetaValue id = cv.get("id");
         log.info(id);
         MetaValue maxSize = cv.get("maxSize");
         log.info(maxSize);
         MetaValue messageCount = cv.get("messageCount");
         log.info(messageCount);
      }
   }
   
   public void testServerPeer() throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("JMS", "ServerPeer");
      ManagedComponent component = mgtView.getComponent("jboss.messaging:service=ServerPeer", type);
      assertNotNull(component);
      
      log.info("Properties: "+component.getProperties().keySet());
      
      ManagedOperation o = getOperation(component, "listMessageCountersAsHTML", new String[0]);
      MetaValue v = o.invoke(new MetaValue[0]);
      assertNotNull("null operation return value", v);
      log.debug("result" + v);
   }

   public void testTopicMetrics() throws Exception
   {
      ManagementView mgtView = getManagementView();
      ManagedComponent component = mgtView.getComponent("testCreateTopic", TopicType);
      assertNotNull(component);
      
      Topic topic = (Topic) getInitialContext().lookup("testCreateTopic");
      TopicConnectionFactory cf = (TopicConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      
      TopicConnection connection = cf.createTopicConnection();
      TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      session.createSubscriber(topic);
      
      TopicPublisher pub = session.createPublisher(topic);
      try
      {
         for(int i = 0; i < 10; i++)
            pub.send(session.createTextMessage("Message nr " + i));
         
         SimpleValue nondurable = (SimpleValue)component.getProperty("nonDurableSubscriptionsCount").getValue();
         SimpleValue durable = (SimpleValue)component.getProperty("durableSubscriptionsCount").getValue();
         SimpleValue all = (SimpleValue)component.getProperty("allSubscriptionsCount").getValue();
         SimpleValue allMessageCount = (SimpleValue)component.getProperty("allMessageCount").getValue();
         
         CollectionValue messageCounters = (CollectionValue) component.getProperty("messageCounters").getValue();
         assertNotNull(messageCounters);
         
         assertEquals(1, nondurable.getValue());
         assertEquals(0, durable.getValue());
         assertEquals(1, all.getValue());
         assertEquals(10, allMessageCount.getValue());
   
         CompositeValue messageCounter = (CompositeValue) messageCounters.iterator().next();
         assertNotNull(messageCounter);
         
         SimpleValue count = (SimpleValue) messageCounter.get("messageCount");
         assertEquals(count, allMessageCount);
      }
      finally
      {
         pub.close();
         session.close();
      }
   }

   public void testTopicOperations() throws Exception
   {
      ManagedComponent component = getManagementView().getComponent("testCreateTopic", TopicType);
      ManagedOperation o = getOperation(component, "listAllSubscriptionsAsHTML", new String[0]);
      MetaValue v = o.invoke(new MetaValue[0]);
      assertNotNull("null operation return value", v);
      log.debug("result" + v);
   }
   
   public void testRemoveTopic() throws Exception
   {
      removeDeployment("testCreateTopic-service.xml");
      ManagedComponent topic = getManagementView().getComponent("testCreateTopic", TopicType);
      assertNull("topic should be removed " + topic, topic);
   }
   
   public void testCreateSecureQueue() throws Exception
   {
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      String jndiName = getName();
      propValues.put("name", SimpleValueSupport.wrap(jndiName));
      propValues.put("JNDIName", SimpleValueSupport.wrap(jndiName));
      propValues.put("clustered", SimpleValueSupport.wrap(true));
      // Security config
      Map<String, MetaValue> values = new HashMap<String, MetaValue>();
      values.put("admin", createCompositeValue(true, true, true));
      values.put("publisher", createCompositeValue(true, true, false));
      values.put("user", createCompositeValue(true, null, null));
      CompositeValue map = new MapCompositeValueSupport(values, securityConfType);
      propValues.put("securityConfig", map);
      
      createComponentTest("QueueTemplate", propValues, getName(), QueueType, jndiName);
      ManagedComponent queue = activeView.getComponent("testCreateSecureQueue", QueueType);
      assertNotNull(queue);
      assertEquals("testCreateSecureQueue", queue.getName());
      log.info(queue.getProperties().keySet());
      assertNotNull(queue.getProperty("securityConfig").getValue());
      
   }
   
   public void testRemoveSecureQueue() throws Exception
   {
      removeDeployment("testCreateSecureQueue-service.xml");
             ManagedComponent queue = getManagementView().getComponent("testCreateSecureQueue", QueueType);
      assertNull("queue should be removed" + queue, queue);
   }

   /**
    * Validate creating a queue with a null ObjectName value for the DLQ
    * and Expiry
    * @throws Exception
    */
   public void testCreateQueueWithNullDLQ() throws Exception
   {
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      String jndiName = getName();
      propValues.put("JNDIName", SimpleValueSupport.wrap(jndiName));
      propValues.put("downCacheSize", SimpleValueSupport.wrap(1999));
      propValues.put("DLQ", SimpleValueSupport.wrap((String)null));
      propValues.put("expiryQueue", null);
      
      ComponentType type = KnownComponentTypes.JMSDestination.Queue.getType();
      createComponentTest("QueueTemplate", propValues, getName(), type, jndiName);
      ManagedComponent queue = activeView.getComponent("testCreateQueueWithNullDLQ", type);
      assertNotNull(queue);
      assertEquals("testCreateQueueWithNullDLQ", queue.getName());
      log.info(queue.getProperties().keySet());
      assertEquals("downCacheSize", queue.getProperty("downCacheSize").getValue(), new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 1999));

      ManagedProperty serverPeer = queue.getProperty("serverPeer");
      assertNotNull(serverPeer);
      MetaType serverPeerType = serverPeer.getMetaType();
      assertEquals(SimpleMetaType.STRING, serverPeerType);
      ManagedProperty dlq = queue.getProperty("DLQ");
      assertNotNull(dlq);
      MetaType dlqType = dlq.getMetaType();
      assertEquals(SimpleMetaType.STRING, dlqType);
      ManagedProperty expiryQueue = queue.getProperty("expiryQueue");
      assertNotNull(expiryQueue);
      MetaType expiryQueueType = serverPeer.getMetaType();
      assertEquals(SimpleMetaType.STRING, expiryQueueType);
   }
   public void testRemoveQueueWithNullDLQ() throws Exception
   {
      removeDeployment("testCreateQueueWithNullDLQ-service.xml");
      ManagedComponent queue = getManagementView().getComponent("testCreateQueueWithNullDLQ", QueueType);
      assertNull("queue should be removed" + queue, queue);
   }

   protected void sendQueueMsgs(String jndiName, ManagedComponent component)
      throws Exception
   {
      // The message count property
      ManagedProperty messageCount = component.getProperty("messageCount");
      assertNotNull("messageCount", messageCount);
      ManagedProperty messageCounter = component.getProperty("messageCounter");
      assertNotNull("messageCounter", messageCounter);

      // Clear any 
      Set<ManagedOperation> ops = component.getOperations();
      ManagedOperation removeAllMessages = ManagedOperationMatcher.findOperation(ops, "removeAllMessages");
      assertNotNull("removeAllMessages", removeAllMessages);
      removeAllMessages.invoke();
      assertEquals(SimpleValueSupport.wrap(0), messageCount.getValue());

      // Send a message
      Queue queue = (Queue) getInitialContext().lookup(jndiName);
      assertNotNull(queue);
      QueueConnectionFactory qCf = (QueueConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      QueueConnection c = qCf.createQueueConnection();
      c.start();
      QueueSession s = c.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueSender sender = s.createSender(queue);
      
      // Message 1
      sender.send(s.createTextMessage("Hey!"));

      SimpleValue messageCount1 = (SimpleValue) messageCount.getValue();
      assertTrue((Integer) (messageCount1).getValue() > 0);
      CompositeValue messageCounterCV = (CompositeValue) messageCounter.getValue();
      log.info(messageCounterCV);
      assertEquals(messageCounterCV.get("messageCount"), messageCount1);

      // Message 2
      sender.send(s.createTextMessage("Message2"));
      
      SimpleValue messageCount2 = (SimpleValue) messageCount.getValue();
      assertTrue(messageCount2.compareTo(messageCount1) > 0);
      messageCounterCV = (CompositeValue) messageCounter.getValue();
      assertEquals(messageCounterCV.get("messageCount"), messageCount2);
      
      //
      ManagedOperation listAllMessages = ManagedOperationMatcher.findOperation(ops, "listAllMessages");
      assertNotNull("listAllMessages", listAllMessages);
      MetaValue msgs = listAllMessages.invoke();
      assertNotNull(msgs);
      log.info("listAllMessages.MV: "+msgs);
      assertTrue("msgs is a CollectionValue", msgs instanceof CollectionValue);
      CollectionValue msgsCV = (CollectionValue) msgs;
      MetaValue[] msgsMVs = msgsCV.getElements();
      assertTrue("listAllMessages length > 0", msgsMVs.length > 0);
      for(MetaValue mv : msgsMVs)
      {
         assertTrue(mv instanceof CompositeValue);
         CompositeValue cv = (CompositeValue) mv;
         MetaValue JMSMessageID = cv.get("JMSMessageID");
         log.info(JMSMessageID);
         assertNotNull(JMSMessageID);
         MetaValue JMSCorrelationID = cv.get("JMSCorrelationID");
         log.info(JMSCorrelationID);
         MetaValue JMSTimestamp = cv.get("JMSTimestamp");
         log.info(JMSTimestamp);
         assertNotNull(JMSTimestamp);
      }
      c.stop();
      c.close();
   }

   protected ManagedOperation getOperation(ManagedComponent comp, String name, String[] signature)
   {
      assertNotNull(comp);
      ManagedOperation operation = null;
      for(ManagedOperation o : comp.getOperations())
      {
         if(o.getName().equals(name))
         {
            if(Arrays.equals(signature, o.getReflectionSignature()))
               operation = o;
         }
      }
      assertNotNull("null operation", operation);
      return operation;
   }
   
   protected CompositeValue createCompositeValue(Boolean read, Boolean write, Boolean create)
   {
      Map<String, MetaValue> map = new HashMap<String, MetaValue>();
      
      map.put("read", new SimpleValueSupport(SimpleMetaType.BOOLEAN, read));
      map.put("write", new SimpleValueSupport(SimpleMetaType.BOOLEAN, write));
      map.put("create", new SimpleValueSupport(SimpleMetaType.BOOLEAN, create));
      
      return new CompositeValueSupport(composite, map);
   }
}
