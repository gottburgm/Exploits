/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.test.profileservice.test.ejb3.LocalExpositionBean;
import org.jboss.test.profileservice.test.ejb3.LocalExpositionRemoteBusiness;
import org.jboss.test.profileservice.test.ejb3.TestLoggingMDB;
import org.jboss.test.profileservice.test.ejb3.TestRemoteBusiness;
import org.jboss.test.profileservice.test.ejb3.TestStatefulBean;
import org.jboss.test.profileservice.test.ejb3.TestStatelessBean;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Ejb3MetricsUnitTestCase
 *
 * Asserts that ManagementObjects and corresponding
 * properties/operations are exposed in expected form for
 * EJB3 SLSBs, SFSBs, and MDBs.
 *
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision$
 */
public class Ejb3MetricsUnitTestCase extends AbstractProfileServiceTest
{

   // ---------------------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------------------||
   // ---------------------------------------------------------------------------------------||

   /**
    * Component type for the EJB3 namespace
    */
   private static final String COMPONENT_TYPE_EJB3 = "EJB3";

   /**
    * Component subtype for Stateless Session Beans
    */
   private static final String COMPONENT_SUBTYPE_STATELESS_SESSION = "StatelessSession";

   /**
    * Component subtype for Stateful Session Beans
    */
   private static final String COMPONENT_SUBTYPE_STATEFUL_SESSION = "StatefulSession";

   /**
    * Component subtype for Message-Driven Beans
    */
   private static final String COMPONENT_SUBTYPE_MESSAGE_DRIVEN = "MessageDriven";

   /**
    * Properties which should be exposed by SLSB MOs.
    */
   private static final String[] PROPERTY_NAMES_SLSB = new String[]
   {
   // general EJB metrics
         "name", "invocationStats",
         // session bean specific metrics
         "availableCount", "createCount", "currentSize", "maxSize", "removeCount"};

   /**
    * Properties which should be exposed by SFSB MOs.
    */
   private static final String[] PROPERTY_NAMES_SFSB = new String[]
   {
   // general EJB metrics
         "name", "invocationStats",
         // session bean specific metrics
         "availableCount", "createCount", "currentSize", "maxSize", "removeCount",
         // stateful session bean specific metrics
         "cacheSize", "passivatedCount", "totalSize"};

   /**
    * Properties which should be exposed by MDB MOs.
    */
   private static final String[] PROPERTY_NAMES_MDB = new String[]
   {
   // general EJB metrics
         "name", "invocationStats",
         // message-driven bean specific metrics
         "deliveryActive", "keepAliveMillis", "maxMessages", "maxPoolSize", "minPoolSize"};

   /**
    * Operations which should be exposed by SLSB MOs
    */
   private static final String[] OPERATION_NAMES_SLSB = new String[]
   {"resetInvocationStats"};

   /**
    * Operations which should be exposed by SFSB MOs
    */
   private static final String[] OPERATION_NAMES_SFSB = new String[]
   {"resetInvocationStats"};

   /**
    * Operations which should be exposed by MDB MOs
    */
   private static final String[] OPERATION_NAMES_MDB = new String[]
   {"resetInvocationStats", "startDelivery", "stopDelivery"};

   /**
    * The name of the test EAR to deploy
    */
   private static final String NAME_TEST_EAR = "testEjb3xMetrics.ear";

   /**
    * The name of the test JAR to deploy
    */
   private static final String NAME_TEST_JAR = "testEjb3xMetrics.jar";

   /**
    * Name of the Queue Connection Factory in JNDI
    */
   private static final String JNDI_NAME_CONNECTION_FACTORY = "ConnectionFactory";

   // ---------------------------------------------------------------------------------------||
   // Instance Members ----------------------------------------------------------------------||
   // ---------------------------------------------------------------------------------------||

   /**
    * The hook to ManagementObjects
    */
   private ManagementView managementView;

   /**
    * Profile Service deployer; required to get things in management view
    */
   private DeploymentManager deploymentManager;

   /**
    * Repository names from the deployment
    */
   private String[] repositoryNames;

   // ---------------------------------------------------------------------------------------||
   // Constructor ---------------------------------------------------------------------------||
   // ---------------------------------------------------------------------------------------||

   /**
    * Constructor
    *
    * @param name
    */
   public Ejb3MetricsUnitTestCase(String name)
   {
      super(name);
   }

   // ---------------------------------------------------------------------------------------||
   // Lifecycle -----------------------------------------------------------------------------||
   // ---------------------------------------------------------------------------------------||

   
   public static TestSuite suite()
   {
       if (JMSDestinationsUtil.isJBM())
       {
           return new TestSuite(Ejb3MetricsUnitTestCase.class);
       }
       else
       {
           return new TestSuite(); // empty, this test is written for JBM only
       }
   }
   
   /**
    * Deploys the test JAR via the ProfileService-aware DeploymentManager
    */
   @Override
   protected void setUp() throws Exception
   {
      // Call Super
      super.setUp();

      // Get the Deployment Manager
      deploymentManager = this.getDeploymentManager();

      // Set the Management View
      managementView = this.getManagementView();
   }

   /**
    * Undeploys the test JAR
    */
   @Override
   protected void tearDown() throws Exception
   {
      // Undeploy the test JAR
      log.info("Undeploying: " + repositoryNames);
      final DeploymentProgress stopProgress = deploymentManager.stop(repositoryNames);
      stopProgress.run();
      final DeploymentProgress removeProgress = deploymentManager.remove(repositoryNames);
      removeProgress.run();

      // Null out
      repositoryNames = null;
      deploymentManager = null;
      managementView = null;

      // Call super
      super.tearDown();
   }

   // ---------------------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------------------||
   // ---------------------------------------------------------------------------------------||

   /**
    * Ensures that MOs with the correct metrics and operations are exposed for SLSBs.
    *
    * @throws Exception
    */
   public void testSlsb() throws Exception
   {
      // Log
      this.getLog().info("testSlsb");

      // Deploy
      this.deployTestJar();

      // Ensure component exists.
      final String ejbName = TestStatelessBean.EJB_NAME;
      final String componentName = NAME_TEST_JAR + '/' + ejbName;
      final ManagedComponent component = this.getAndTestManagedComponent(COMPONENT_SUBTYPE_STATELESS_SESSION,
            componentName);

      // Check component has expected props and ops.
      this.ensureManagementPropertiesExposed(component, PROPERTY_NAMES_SLSB);
      this.ensureManagementOperationsExposed(component, OPERATION_NAMES_SLSB);

      validateInvocationSessionStats(component, ejbName);
   }

   /**
    * Ensures that MOs with the correct metrics and operations are exposed for SFSBs.
    *
    * @throws Exception
    */
   public void testSfsb() throws Exception
   {
      // Log
      this.getLog().info("testSfsb");

      // Deploy
      this.deployTestJar();

      // Ensure component exists.
      final String ejbName = TestStatefulBean.EJB_NAME;
      final String componentName = NAME_TEST_JAR + '/' + ejbName;
      final ManagedComponent component = this.getAndTestManagedComponent(COMPONENT_SUBTYPE_STATEFUL_SESSION,
            componentName);

      // Check component has expected props and ops.
      this.ensureManagementPropertiesExposed(component, PROPERTY_NAMES_SFSB);
      this.ensureManagementOperationsExposed(component, OPERATION_NAMES_SFSB);

      validateInvocationSessionStats(component, ejbName);
   }
   
   /**
    * Ensures that SLSB local invocations are recorded
    *
    * @throws Exception
    */
   public void testSlsbLocal() throws Exception
   {
      // Log
      this.getLog().info("testSlsbLocal");

      // Deploy
      this.deployTestJar();

      // Check component has expected props and ops
      validateLocalInvocationSlsbStats();
   }

   /**
    * Ensures that the managed component names of
    * EJB3 deployments in an EAR have form:
    *
    * "EARName/JARName/EJBName"
    */
   public void testEarManagedComponentNames() throws Exception
   {
      // Log
      this.getLog().info("testEarManagedComponentNames");

      // Deploy the EAR (it will be undeployed and cleaned up as part of test lifecycle)
      final String deployName = NAME_TEST_EAR;
      final URL contentURL = super.getDeployURL(deployName);
      this.deploy(deployName, contentURL);

      // Generate expected names
      final String earName = NAME_TEST_EAR;
      final char delimiter = '/';
      final String jarName = NAME_TEST_JAR;
      final String componentPrefix = earName + delimiter + jarName + delimiter;
      final String slsbComponentName = componentPrefix + TestStatelessBean.EJB_NAME;
      final String sfsbComponentName = componentPrefix + TestStatefulBean.EJB_NAME;
      final String mdbComponentName = componentPrefix + TestLoggingMDB.class.getSimpleName();

      // Test
      this.getAndTestManagedComponent(COMPONENT_SUBTYPE_STATELESS_SESSION, slsbComponentName);
      this.getAndTestManagedComponent(COMPONENT_SUBTYPE_STATEFUL_SESSION, sfsbComponentName);
      this.getAndTestManagedComponent(COMPONENT_SUBTYPE_MESSAGE_DRIVEN, mdbComponentName);
   }

   /**
    * Ensures that MOs with the correct metrics and operations are exposed for MDBs.
    *
    * @throws Exception
    */
   public void testMdb() throws Exception
   {
      // Log
      this.getLog().info("testMdb");

      // Deploy
      this.deployTestJar();

      // Ensure component exists.
      final String ejbName = TestLoggingMDB.class.getSimpleName();
      final String componentName = NAME_TEST_JAR + '/' + ejbName;
      final ManagedComponent component = this.getAndTestManagedComponent(COMPONENT_SUBTYPE_MESSAGE_DRIVEN,
            componentName);

      // Check component has expected props and ops.
      this.ensureManagementPropertiesExposed(component, PROPERTY_NAMES_MDB);
      this.ensureManagementOperationsExposed(component, OPERATION_NAMES_MDB);

      validateInvocationMdbStats(component);
   }

   // ---------------------------------------------------------------------------------------||
   // Internal Helper Methods ---------------------------------------------------------------||
   // ---------------------------------------------------------------------------------------||

   /**
    * Deploys the specified URL under the specified deployment name
    * using the profile service; this is required such that ManagedObjects will
    * be found (which is not the case using traditional JMX deployment)
    *
    * @param deployName
    * @param url
    * @throws Exception
    */
   protected void deploy(final String deployName, final URL url) throws Exception
   {
      // Deploy
      final DeploymentProgress distributeProgress = deploymentManager.distribute(deployName, url, true);
      distributeProgress.run();
      repositoryNames = distributeProgress.getDeploymentID().getRepositoryNames();
      final DeploymentProgress startProgress = deploymentManager.start(repositoryNames);
      log.info("Deploying: " + repositoryNames);
      startProgress.run();

      // Reset the Management View
      managementView = this.getManagementView();
   }

   /**
    * Deploys the test JAR
    *
    * @throws Exception
    */
   protected void deployTestJar() throws Exception
   {
      // Deploy the test JAR
      final String deployName = NAME_TEST_JAR;
      final URL contentURL = super.getDeployURL(deployName);
      this.deploy(deployName, contentURL);
   }

   /**
    * Obtains the component of the specified subtype and name, failing the test if not found
    *
    * @param componentSubType
    * @param componentName
    * @throws IllegalArgumentException If any of the arguments were not specified
    * @throws Exception If a general error occured
    */
   protected ManagedComponent getAndTestManagedComponent(final String componentSubType, final String componentName)
         throws Exception
   {
      // Log
      this.getLog().info("getAndTestManagedComponent");

      // Precondition checks
      if (componentSubType == null || componentSubType.length() == 0)
      {
         throw new IllegalArgumentException("componentSubType must be specified");
      }
      if (componentName == null || componentName.length() == 0)
      {
         throw new IllegalArgumentException("componentName must be specified");
      }

      // Get the SLSB Type
      final ComponentType componentType = new ComponentType(COMPONENT_TYPE_EJB3, componentSubType);
      final Set<ManagedComponent> components = managementView.getComponentsForType(componentType);

      // Get SLSB
      final Iterator<ManagedComponent> componentsIt = components.iterator();
      ManagedComponent component = null;
      while (componentsIt.hasNext())
      {
         // Obtain the next component
         final ManagedComponent currentComponent = componentsIt.next();
         final String name = currentComponent.getName();

         // Ensure it's the component we're looking for
         if (name.equals(componentName))
         {
            component = currentComponent;
            break;
         }
      }

      // Ensure we've got the component
      if (component == null)
      {
         TestCase.fail("Component not found; no managed object with name \"" + componentName + "\" in: " + components);
      }

      // Obtains the Component
      return component;
   }

   /**
    * Ensures that the specified management properties are exposed for
    * the specified component
    *
    * @param The component
    * @param expectedProperties Properties expected to be exposed by the specified component
    * @throws IllegalArgumentException If any of the arguments are blank or null
    * @throws Exception
    */
   protected void ensureManagementPropertiesExposed(final ManagedComponent component, final String[] expectedProperties)
         throws IllegalArgumentException, Exception
   {

      // Log
      this.getLog().info("ensureManagementPropertiesExposed");

      // Precondition checks
      if (component == null)
      {
         throw new IllegalArgumentException("component must be specified");
      }
      if (expectedProperties == null)
      {
         throw new IllegalArgumentException("expectedProperties must be specified");
      }

      // Ensure all expected properties are in place
      for (final String expectedProperty : expectedProperties)
      {
         final ManagedProperty prop = component.getProperty(expectedProperty);
         TestCase.assertNotNull("Component did not contain expected managed property \"" + expectedProperty + "\": "
               + component, prop);
      }
   }

   /**
    * Ensures that the specified management operations are exposed for
    * the specified component
    *
    * @param The component
    * @param expectedOperationNames Operation names expected to be exposed by the specified component
    * @throws IllegalArgumentException If any of the arguments are blank or null
    * @throws Exception
    */
   protected void ensureManagementOperationsExposed(final ManagedComponent component,
         final String[] expectedOperationNames) throws IllegalArgumentException, Exception
   {

      // Log
      this.getLog().info("ensureManagementOperationsExposed");

      // Precondition checks
      if (component == null)
      {
         throw new IllegalArgumentException("component must be specified");
      }
      if (expectedOperationNames == null)
      {
         throw new IllegalArgumentException("expectedProperties must be specified");
      }

      // Obtain managed operations for this component
      final Set<ManagedOperation> operations = component.getOperations();

      // For all expected operations
      for (final String expectedOperation : expectedOperationNames)
      {
         // Ensure it's exposed
         boolean found = false;
         for (final ManagedOperation currentOperation : operations)
         {
            if (currentOperation.getName().equals(expectedOperation))
            {
               found = true;
               break;
            }
         }

         // Ensure the operation was found
         TestCase.assertTrue("Component did not contain expected managed operation \"" + expectedOperation + "\": "
               + component, found);
      }
   }

   /**
    * Validates that the invocation statistics are in place for the specified
    * EJB3 Session Bean ManagedComponent with specified EJB Name
    *
    * @param component
    * @param ejbName
    * @throws IllegalArgumentException If either argument is not specified
    */
   protected void validateInvocationSessionStats(final ManagedComponent component, final String ejbName)
         throws IllegalArgumentException, Exception
   {
      // Precondition checks
      if (component == null)
      {
         throw new IllegalArgumentException("component must be specified");
      }
      if (ejbName == null || ejbName.length() == 0)
      {
         throw new IllegalArgumentException("ejbName must be specified");
      }

      // Obtain invocation stats
      InvocationStats invocationStats = getInvocationStats(component);

      // Ensure the stats are expected (empty)
      List<MethodStats> methodStats = invocationStats.methodStats;
      log.info("Method stats before invocation: " + methodStats);
      Assert.assertTrue("Method stats should be empty", methodStats.isEmpty());

      // Invoke upon the EJB
      TestRemoteBusiness remoteBusiness = (TestRemoteBusiness) getInitialContext().lookup(ejbName + "/remote");
      remoteBusiness.echo("Some test String");

      // Ensure the stats reflect the invocation
      invocationStats = getInvocationStats(component);
      methodStats = invocationStats.methodStats;
      log.info("Method stats after invocation: " + methodStats);
      Assert.assertEquals("One invocation should be represented in stats", 1, methodStats.size());

      // Reset the stats
      resetInvocationStats(component);
      invocationStats = getInvocationStats(component);

      // Ensure the stats were reset
      invocationStats = getInvocationStats(component);
      methodStats = invocationStats.methodStats;
      log.info("Method stats after reset: " + methodStats);
      Assert.assertTrue("Method stats should be empty, was instead of size: " + methodStats.size(), methodStats
            .isEmpty());
   }

   /**
    * Validates that the invocation statistics are in place 
    * for local invocations upon a SLSB 
    * 
    * @throws IllegalArgumentException If either argument is not specified
    */
   protected void validateLocalInvocationSlsbStats()
         throws IllegalArgumentException, Exception
   {
      // Ensure component exists
      final String ejbName = TestStatelessBean.EJB_NAME;
      final String slsbComponentName = NAME_TEST_JAR + '/' + ejbName;
      final ManagedComponent slsbComponent = this.getAndTestManagedComponent(COMPONENT_SUBTYPE_STATELESS_SESSION,
            slsbComponentName);

      // Obtain invocation stats
      InvocationStats invocationStats = getInvocationStats(slsbComponent);

      // Ensure the stats are expected (empty)
      List<MethodStats> methodStats = invocationStats.methodStats;
      log.info("Method stats before invocation: " + methodStats);
      Assert.assertTrue("Method stats should be empty", methodStats.isEmpty());

      // Invoke upon the EJB locally via the Remote access delegate
      LocalExpositionRemoteBusiness remoteBusiness = (LocalExpositionRemoteBusiness) getInitialContext().lookup(LocalExpositionBean.class.getSimpleName() + "/remote");
      remoteBusiness.echoViaLocal("Some test String");

      // Ensure the stats reflect the invocation in the SLSB
      invocationStats = getInvocationStats(slsbComponent);
      methodStats = invocationStats.methodStats;
      log.info("Method stats after invocation: " + methodStats);
      Assert.assertEquals("One invocation (made locally) should be represented in stats", 1, methodStats.size());

      // Reset the stats
      resetInvocationStats(slsbComponent);
      invocationStats = getInvocationStats(slsbComponent);

      // Ensure the stats were reset
      invocationStats = getInvocationStats(slsbComponent);
      methodStats = invocationStats.methodStats;
      log.info("Method stats after reset: " + methodStats);
      Assert.assertTrue("Method stats should be empty, was instead of size: " + methodStats.size(), methodStats
            .isEmpty());
   }
   
   /**
    * Validates that the invocation statistics are in place for the specified
    * EJB3 MDB ManagedComponent with specified EJB Name
    *
    * @param component
    * @throws IllegalArgumentException If the component is not specified
    */
   protected void validateInvocationMdbStats(final ManagedComponent component) throws IllegalArgumentException,
         Exception
   {
      // Precondition checks
      if (component == null)
      {
         throw new IllegalArgumentException("component must be specified");
      }
      // Obtain invocation stats
      InvocationStats invocationStats = getInvocationStats(component);

      // Ensure the stats are expected (empty)
      List<MethodStats> methodStats = invocationStats.methodStats;
      Assert.assertTrue("Method stats should be empty", methodStats.isEmpty());

      // Get the queue from JNDI
      final Context naimgContext = getInitialContext();
      final Queue queue = (Queue) naimgContext.lookup(TestLoggingMDB.QUEUE_NAME);

      // Get the ConnectionFactory from JNDI
      final QueueConnectionFactory factory = (QueueConnectionFactory) naimgContext.lookup(JNDI_NAME_CONNECTION_FACTORY);

      // Make a Connection
      final QueueConnection connection = factory.createQueueConnection();
      final QueueSession sendSession = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      // Make the message
      final String contents = "Test Message Contents";
      final TextMessage message = sendSession.createTextMessage(contents);

      // Send the message
      final QueueSender sender = sendSession.createSender(queue);
      sender.send(message);
      log.info("Sent message " + message + " with contents: " + contents);

      // Clean up
      sendSession.close();
      connection.close();

      // Wait, this is async
      Thread.sleep(3000);

      // Ensure the stats reflect the invocation
      invocationStats = getInvocationStats(component);
      methodStats = invocationStats.methodStats;
      log.info("Method stats after invocation: " + methodStats);
      Assert.assertEquals("One invocation should be represented in stats", 1, methodStats.size());

      // Reset the stats
      resetInvocationStats(component);
      invocationStats = getInvocationStats(component);

      // Ensure the stats were reset
      invocationStats = getInvocationStats(component);
      methodStats = invocationStats.methodStats;
      log.info("Method stats after reset: " + methodStats);
      Assert.assertTrue("Method stats should be empty", methodStats.isEmpty());
   }

   private InvocationStats getInvocationStats(ManagedComponent component)
   {
      InvocationStats invocationStats = new InvocationStats();
      List<MethodStats> allMethodStats = new ArrayList<MethodStats>();
      ManagedProperty invocationStatsProp = component.getProperty("invocationStats");
      invocationStats.endTime = System.currentTimeMillis();
      CompositeValue invocationStatsMetaValue = (CompositeValue) invocationStatsProp.getValue();
      CompositeValue allMethodStatsMetaValue = (CompositeValue) invocationStatsMetaValue.get("methodStats");
      Set<String> methodNames = allMethodStatsMetaValue.getMetaType().keySet();
      for (String methodName : methodNames)
      {
         CompositeValue methodStatsMetaValue = (CompositeValue) allMethodStatsMetaValue.get(methodName);
         MethodStats methodStats = new MethodStats();
         methodStats.name = methodName;
         methodStats.count = Long.parseLong(((SimpleValue) methodStatsMetaValue.get("count")).getValue().toString());
         methodStats.totalTime = Long.parseLong(((SimpleValue) methodStatsMetaValue.get("totalTime")).getValue()
               .toString());
         methodStats.minTime = Long
               .parseLong(((SimpleValue) methodStatsMetaValue.get("minTime")).getValue().toString());
         methodStats.maxTime = Long
               .parseLong(((SimpleValue) methodStatsMetaValue.get("maxTime")).getValue().toString());
         allMethodStats.add(methodStats);
      }
      invocationStats.methodStats = allMethodStats;

      SimpleValue lastResetTimeMetaValue = (SimpleValue) invocationStatsMetaValue.get("lastResetTime");
      invocationStats.beginTime = Long.valueOf(lastResetTimeMetaValue.getValue().toString()); // TODO: handle null value?

      return invocationStats;
   }

   private void resetInvocationStats(ManagedComponent component)
   {
      Set<ManagedOperation> operations = component.getOperations();
      final List<String> opNames = new ArrayList<String>();
      final String resetMethodName = "resetInvocationStats";
      for (ManagedOperation operation : operations)
      {
         final String opName = operation.getName();
         opNames.add(opName);
         if (opName.equals(resetMethodName))
         {
            operation.invoke();
            return;
         }
      }

      // If we've reached here, fail
      Assert
            .fail("No operation named \"" + resetMethodName + "\" exists in " + opNames + " for " + component.getName());

   }

   class InvocationStats
   {
      List<MethodStats> methodStats;

      long beginTime;

      long endTime;

      @Override
      public String toString()
      {
         return "InvocationStats [beginTime=" + beginTime + ", endTime=" + endTime + ", methodStats=" + methodStats
               + "]";
      }
   }

   class MethodStats
   {
      String name;

      long count;

      long minTime;

      long maxTime;

      long totalTime;

      @Override
      public String toString()
      {
         return "MethodStats [count=" + count + ", maxTime=" + maxTime + ", minTime=" + minTime + ", name=" + name
               + ", totalTime=" + totalTime + "]";
      }
   }
}
