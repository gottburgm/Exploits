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

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedParameter;
import org.jboss.managed.api.ManagedProperty;

/**
 * <p>
 * Profile service security tests.
 * </p>
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @version $Revision: 110325 $
 */
public class SecurityManagedObjectsTestCase extends AbstractProfileServiceTest
{
   protected ProgressListener listener = new TestProgressListener();

   /**
    * <p>
    * Creates an instance of {@code SecurityManagedObjectsTestCase} with the specified name.
    * </p>
    * 
    * @param name a {@code String} representing the name of this {@code TestCase}.
    */
   public SecurityManagedObjectsTestCase(String name)
   {
      super(name);
   }

   /**
    * Looks for ComponentType("MCBean", "Security") type and logs them.
    * 
    * @exception thrown if there are no matching components
    */
   public void testSecurityMCBeans() throws Exception
   {
      ManagementView managementView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      Set<ManagedComponent> mcs = managementView.getComponentsForType(type);
      assertTrue("There are MCBean,Security components", mcs.size() > 0);
      super.getLog().debug("MCBeans: " + mcs);
   }

   /**
    * <p>
    * Validates at the {@code SecurityConfig} managed component.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testSecurityConfig() throws Exception
   {
      ManagementView managementView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent component = managementView.getComponent("SecurityConfig", type);
      assertNotNull(component);

      // verify that the component has the expected properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      assertEquals("Unexpected number of properties", 4, properties.size());
      assertTrue("Missing expected property: loginConfig", properties.containsKey("loginConfig"));
      assertTrue("Missing expected property: mbeanServer", properties.containsKey("mbeanServer"));
      assertTrue("Missing expected property: defaultLoginConfig", properties.containsKey("defaultLoginConfig"));
      assertTrue("Missing expected property: state", properties.containsKey("state"));

      // verify that the component has the expected operations.
      String[] expectedOperations = {"startService", "stopService", "pushLoginConfig", "popLoginConfig"};
      Set<ManagedOperation> operations = component.getOperations();
      assertEquals("Unexpected number of operations", expectedOperations.length, operations.size());
      // copy the names of the operations to a new collection to compare them with the expected names.
      Set<String> operationNames = new HashSet<String>();
      for (ManagedOperation operation : operations)
         operationNames.add(operation.getName());
      for (String expectedOperation : expectedOperations)
         assertTrue("Expected operation " + expectedOperation + " not found", operationNames
               .contains(expectedOperation));
   }

   /**
    * <p>
    * Validates the {@code XMLLoginConfig} managed component.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testXMLLoginConfig() throws Exception
   {
      // get the XMLLoginConfig managed component.
      ManagementView managementView = super.getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent component = managementView.getComponent("XMLLoginConfig", type);
      assertNotNull(component);

      // verify that the component has the expected properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      assertEquals("Unexpected number of properties", 3, properties.size());
      assertTrue("Missing expected property: configURL", properties.containsKey("configURL"));
      assertTrue("Missing expected property: validateDTD", properties.containsKey("validateDTD"));
      assertTrue("Missing expected property: state", properties.containsKey("state"));

      // verify that the component has the expected operations.
      String[] expectedOperations = {"loadConfig", "setConfigResource", "displayAppConfig", "addApplicationPolicy",
            "removeConfigs", "getApplicationPolicy", "getConfiguration"};
      Set<ManagedOperation> operations = component.getOperations();
      assertEquals("Unexpected number of operations", expectedOperations.length, operations.size());
      // copy the names of the operations to a new collection to compare them with the expected names.
      Set<String> operationNames = new HashSet<String>();
      for (ManagedOperation operation : operations)
         operationNames.add(operation.getName());
      for (String expectedOperation : expectedOperations)
         assertTrue("Expected operation " + expectedOperation + " not found", operationNames
               .contains(expectedOperation));
   }

   /**
    * <p>
    * Validates the {@code JBossSecuritySubjectFactory} managed component.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testJBossSecuritySubjectFactory() throws Exception
   {
      // get the XMLLoginConfig managed component.
      ManagementView managementView = super.getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent component = managementView.getComponent("JBossSecuritySubjectFactory", type);
      assertNotNull(component);

      // verify that the component has the expected properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      assertEquals("Unexpected number of properties", 2, properties.size());
      assertTrue("Missing expected property: securityManagement", properties.containsKey("securityManagement"));
      assertTrue("Missing expected property: state", properties.containsKey("state"));

      // this component should have 2 createSubject operations - one with no args and one with a String arg.
      boolean foundNoArgOperation = false;
      boolean foundStringArgOperation = false;
      Set<ManagedOperation> operations = component.getOperations();
      assertEquals("Unexpected number of operations", 2, operations.size());
      for (ManagedOperation operation : operations)
      {
         assertEquals("Unexpected operation found", "createSubject", operation.getName());
         ManagedParameter[] parameters = operation.getParameters();
         if (parameters.length == 0)
         {
            foundNoArgOperation = true;
         }
         else
         {
            assertEquals("Unexpected number of parameters", 1, parameters.length);
            ManagedParameter parameter = parameters[0];
            assertEquals("Invalid parameter name", "securityDomainName", parameter.getName());
            assertEquals("Invalid parameter type", "java.lang.String", parameter.getMetaType().getTypeName());
            foundStringArgOperation = true;
         }
      }
      assertTrue(foundNoArgOperation);
      assertTrue(foundStringArgOperation);
   }

   /**
    * <p>
    * Validates the {@code JNDIContextEstablishment} managed component.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testJNDIContextEstablishment() throws Exception
   {
      // get the XMLLoginConfig managed component.
      ManagementView managementView = super.getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent component = managementView.getComponent("JNDIContextEstablishment", type);
      assertNotNull(component);

      // verify that the component has the expected properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      assertEquals("Unexpected number of properties", 3, properties.size());
      assertTrue("Missing expected property: baseContext", properties.containsKey("baseContext"));
      assertTrue("Missing expected property: factoryName", properties.containsKey("factoryName"));
      assertTrue("Missing expected property: state", properties.containsKey("state"));

      // verify that the component has the expected operations.
      Set<ManagedOperation> operations = component.getOperations();
      assertEquals("Unexpected number of operations", 0, operations.size());
   }

   /**
    * <p>
    * Validates the {@code JNDIBasedSecurityManagement} managed component.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testJNDIBasedSecurityManagement() throws Exception
   {
      // get the XMLLoginConfig managed component.
      ManagementView managementView = super.getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent component = managementView.getComponent("JNDIBasedSecurityManagement", type);
      assertNotNull(component);

      // verify that the component has the expected managed properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      assertEquals("Unexpected number of properties", 6, properties.size());
      assertTrue("Missing expected property: authenticationMgrClass", properties.containsKey("authenticationMgrClass"));
      assertTrue("Missing expected property: authorizationMgrClass", properties.containsKey("authorizationMgrClass"));
      assertTrue("Missing expected property: auditMgrClass", properties.containsKey("auditMgrClass"));
      assertTrue("Missing expected property: identityTrustMgrClass", properties.containsKey("identityTrustMgrClass"));
      assertTrue("Missing expected property: mappingMgrClass", properties.containsKey("mappingMgrClass"));
      assertTrue("Missing expected property: state", properties.containsKey("state"));

      // verify that the component has the expected managed operations.
      String[] opsWithStringParam = {"getAuthenticationManager", "getAuthorizationManager", "getAuditManager",
            "getIdentityTrustManager", "getMappingManager", "createSecurityDomainContext",
            "deregisterJaasSecurityDomainInstance"};
      String[] opsWithDomainParam = {"registerJaasSecurityDomainInstance"};
      Map<String, ManagedOperation> operations = new HashMap<String, ManagedOperation>();
      for (ManagedOperation operation : component.getOperations())
         operations.put(operation.getName(), operation);
      assertEquals("Unexpected number of operations", opsWithStringParam.length + opsWithDomainParam.length, operations
            .size());

      // first check that all operations that receive a security domain String are present.
      for (String operationName : opsWithStringParam)
      {
         ManagedOperation operation = operations.get(operationName);
         assertNotNull("Missing expected operation: " + operationName, operation);
         ManagedParameter[] parameters = operation.getParameters();
         assertEquals("Unexpected number of parameters", 1, parameters.length);
         assertEquals("Invalid parameter name", "securityDomain", parameters[0].getName());
         assertEquals("Invalid parameter type", "java.lang.String", parameters[0].getMetaType().getTypeName());
      }

      // now check that the operations that receive a JaasSecurityDomain are present.
      for (String operationName : opsWithDomainParam)
      {
         ManagedOperation operation = operations.get(operationName);
         assertNotNull("Missing expected operation: " + operationName, operation);
         ManagedParameter[] parameters = operation.getParameters();
         assertEquals("Unexpected number of parameters", 1, parameters.length);
         assertEquals("Invalid parameter name", "domain", parameters[0].getName());
         assertEquals("Invalid parameter type", "org.jboss.security.plugins.JaasSecurityDomain", parameters[0]
               .getMetaType().getTypeName());
      }
   }

   /**
    * <p>
    * Validates {@code JaasSecurityDomain} managed objects that are created by deploying a
    * {@code testdomains-jboss-beans.xml} file.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testJaasSecurityDomain() throws Exception
   {
      // deploy the XML file that contains the test security domains.
      String domainsDeployment = "testdomains-jboss-beans.xml";
      this.deployResource(domainsDeployment, "profileservice/security/" + domainsDeployment);

      // validate the managed deployment.
      ManagementView managementView = getManagementView();
      
      ManagedDeployment deployment = managementView.getDeployment(domainsDeployment);
      assertNotNull(deployment);
      // verify the deployment contains the expected managed components.
      assertEquals("Unexpected number of components", 2, deployment.getComponents().size());
      assertNotNull("Missing expected component: TestDomain1", deployment.getComponent("TestDomain1"));
      assertNotNull("Missing expected component: TestDomain2", deployment.getComponent("TestDomain2"));

      // validate the components created upon deployment.
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent component = managementView.getComponent("TestDomain1", type);
      assertNotNull(component);

      // verify that the component has the expected managed properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      assertEquals("Unexpected number of properties", 27, properties.size());
      assertTrue("Missing expected property: securityDomain", properties.containsKey("securityDomain"));
      assertTrue("Missing expected property: state", properties.containsKey("state"));
      // keystore and truststore configuration properties.
      assertTrue("Missing expected property: keyStoreType", properties.containsKey("keyStoreType"));
      assertTrue("Missing expected property: keyStoreURL", properties.containsKey("keyStoreURL"));
      assertTrue("Missing expected property: keyStorePass", properties.containsKey("keyStorePass"));
      assertTrue("Missing expected property: keyStoreAlias", properties.containsKey("keyStoreAlias"));
      assertTrue("Missing expected property: keyStoreProvider", properties.containsKey("keyStoreProvider"));
      assertTrue("Missing expected property: keyStoreProviderArgument", properties.containsKey("keyStoreProviderArgument"));
      assertTrue("Missing expected property: keyManagerFactoryAlgorithm", properties.containsKey("keyManagerFactoryAlgorithm"));
      assertTrue("Missing expected property: keyManagerFactoryProvider", properties.containsKey("keyManagerFactoryProvider"));
      assertTrue("Missing expected property: trustStoreType", properties.containsKey("trustStoreType"));
      assertTrue("Missing expected property: trustStoreURL", properties.containsKey("trustStoreURL"));
      assertTrue("Missing expected property: trustStorePass", properties.containsKey("trustStorePass"));
      assertTrue("Missing expected property: trustStoreProvider", properties.containsKey("trustStoreProvider"));
      assertTrue("Missing expected property: trustStoreProviderArgument", properties.containsKey("trustStoreProviderArgument"));
      assertTrue("Missing expected property: trustManagerFactoryAlgorithm", properties.containsKey("trustManagerFactoryAlgorithm"));
      assertTrue("Missing expected property: trustManagerFactoryProvider", properties.containsKey("trustManagerFactoryProvider"));
      assertTrue("Missing expected property: clientAlias", properties.containsKey("clientAlias"));
      assertTrue("Missing expected property: serverAlias", properties.containsKey("serverAlias"));
      assertTrue("Missing expected property: clientAuth", properties.containsKey("clientAuth"));
      assertTrue("Missing expected property: additionalOptions", properties.containsKey("additionalOptions"));
      assertTrue("Missing expected property: serviceAuthToken", properties.containsKey("serviceAuthToken"));
      // security manager service injection properties.
      assertTrue("Missing expected property: managerServiceName", properties.containsKey("managerServiceName"));
      assertTrue("Missing expected property: securityManagement", properties.containsKey("securityManagement"));
      // cipher algorithm properties.
      assertTrue("Missing expected property: salt", properties.containsKey("salt"));
      assertTrue("Missing expected property: iterationCount", properties.containsKey("iterationCount"));
      assertTrue("Missing expected property: cipherAlgorithm", properties.containsKey("cipherAlgorithm"));

      // verify that the component has the expected managed operations.
      Map<String, ManagedOperation> operations = new HashMap<String, ManagedOperation>();
      for (ManagedOperation operation : component.getOperations())
         operations.put(operation.getName(), operation);
      String[] noArgsOperations = {"getKeyStore", "getTrustStore", "getKeyManagerFactory", "getTrustManagerFactory",
            "reloadKeyAndTrustStore"};
      String[] oneArgOperations = {"encode", "decode", "encode64", "decode64"};
      assertEquals("Unexpected number of operations", noArgsOperations.length + oneArgOperations.length, operations
            .size());
      // first check the methods that don't have any parameter.
      for(String operationName : noArgsOperations)
      {
         ManagedOperation operation = operations.get(operationName);
         assertNotNull("Unexpected operation name: " + operationName, operation);
         ManagedParameter[] parameters = operation.getParameters();
         assertEquals("Unexpected number of parameters", 0, parameters.length);
      }
      // now check the methods that contain a 'secret' parameter.
      for(String operationName : oneArgOperations)
      {
         ManagedOperation operation = operations.get(operationName);
         assertNotNull("Unexpected operation name: " + operationName, operation);
         ManagedParameter[] parameters = operation.getParameters();
         assertEquals("Unexpected number of parameters", 1, parameters.length);
         assertEquals("Invalid parameter name", "secret", parameters[0].getName());
      }
      
      // just the check the second security domain is also available - we don't repeat the tests because the
      // properties and operations must be the same of those verified in the first domain.
      component = managementView.getComponent("TestDomain2", type);
      assertNotNull(component);

      // undeploy the test security domains.
      this.undeployResource(domainsDeployment);
   }

   /**
    * <p>
    * Validates the {@code JNDIBasedSecurityRegistration} managed component.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testJNDIBasedSecurityRegistration() throws Exception
   {
      ManagementView mgtMview = getManagementView();
      ComponentType type = new ComponentType("MCBean", "Security");
      ManagedComponent mc = mgtMview.getComponent("JNDIBasedSecurityRegistration", type);
      assertNotNull(mc);
   }

   /**
    * <p>
    * Deploy a resource, registering it with the profile service.
    * </p>
    * 
    * @param resourceName a {@code String} representing the resource's unique name. This will be the name the resource
    *            will have when it is 'copied' to the server/partition/deploy directory.
    * @param resourcePath the path to the resource that will be deployed.
    * @throws Exception if an error occurs while deploying the resource.
    */
   private void deployResource(String resourceName, String resourcePath) throws Exception
   {
      // create a URL for the resource.
      String contentURLString = super.getResourceURL(resourcePath);
      int index = contentURLString.indexOf(":/");
      URL contentURL = new URL("vfsfile" + contentURLString.substring(index));

      // distribute the resource deployment.
      DeploymentManager manager = super.getDeploymentManager();
      DeploymentProgress progress = manager.distribute(resourceName, contentURL, true);
      progress.addProgressListener(this.listener);
      progress.run();
      
      assertDeployed(progress);

      // start the deployment.
      String[] uploadedNames = progress.getDeploymentID().getRepositoryNames();
      progress = manager.start(uploadedNames);
      progress.addProgressListener(this.listener);
      progress.run();
      
      assertDeployed(progress);
   }
   
   private void assertDeployed(DeploymentProgress progress)
   {
      if(progress.getDeploymentStatus().isFailed())
      {
         fail("deployment failed: " + progress.getDeploymentStatus().getFailure());
      }      
   }

   /**
    * <p>
    * Undeploys the specified resource.
    * </p>
    * 
    * @param resourceName the resource's unique name. This must match the {@code resourceName} used when deploying the
    *            resource.
    * @throws Exception if an error occurs while undeploying the resource.
    */
   private void undeployResource(String resourceName) throws Exception
   {
      // stop the resource deployment.
      DeploymentManager manager = super.getDeploymentManager();
      DeploymentProgress progress = manager.stop(resourceName);
      progress.addProgressListener(this.listener);
      progress.run();

      // undeploy the resource.
      progress = manager.remove(resourceName);
      progress.addProgressListener(this.listener);
      progress.run();
   }

   /**
    * <p>
    * Simple {@code ProgressListener} that logs progress events.
    * </p>
    * 
    * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
    */
   private class TestProgressListener implements ProgressListener
   {

      /*
       * (non-Javadoc)
       * 
       * @see org.jboss.deployers.spi.management.deploy.ProgressListener#progressEvent(org.jboss.deployers.spi.management.deploy.ProgressEvent)
       */
      public void progressEvent(ProgressEvent eventInfo)
      {
         log.trace("Received progress event: " + eventInfo);
      }
   }
}
