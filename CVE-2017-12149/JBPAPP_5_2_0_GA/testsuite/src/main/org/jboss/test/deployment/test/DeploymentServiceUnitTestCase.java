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
package org.jboss.test.deployment.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.services.deployment.MBeanData;
import org.jboss.test.JBossTestCase;

/**
 * DeploymentService tests
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis </a>
 * @author <a href="mailto:peter.johnson2@unisys.com">Peter Johnson </a>
 * @version $Revision: 81036 $
 */
public class DeploymentServiceUnitTestCase extends JBossTestCase
{
   private ObjectName deploymentService = ObjectNameFactory
         .create("jboss:service=DeploymentService");

   private ObjectName mainDeployer = ObjectNameFactory
         .create("jboss.system:service=MainDeployer");

   public DeploymentServiceUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Check if I can get the available templates
    */
   public void testListModuleTemplates() throws Exception
   {
      log.info("+++ testListModuleTemplates");
      MBeanServerConnection server = getServer();
      try
      {
         boolean isRegistered = server.isRegistered(deploymentService);
         assertTrue(deploymentService + " is registered", isRegistered);
         log.info("Loaded templates: "
               + server.invoke(deploymentService, "listModuleTemplates",
                     new Object[] {}, new String[] {}));
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to create, remove and re-create a jms topic (don't deploy)
    */
   public void testCreateAndRemoveAndCreateTopic() throws Exception
   {
      log.info("+++ testCreateAndRemoveAndCreateTopic");
      try
      {
         String module = "testTopic1-service.xml";

         // remove module in case it exists
         removeModule(module);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("TopicName", "testTopic1"); // the topic name (mandatory)
         props.put("InMemory", new Boolean(true)); // set this to true to
         // persist the topic in
         // memory
         props.put("MaxDepth", new Integer(5)); // set the MaxDepth property
         // to 5

         String template = "jms-topic";

         // Create a topic destination module, in case of any problem an
         // exception will be thrown
         module = createModule(module, template, props);

         // remove the module
         removeModule(module);

         // Re-create the topic destination module with the same module name.
         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);
      }
      catch (Exception e)
      {
         super.fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to create a no-tx-datasource (don't deploy)
    */
   public void testCreateNoTxDataSource() throws Exception
   {
      log.info("+++ testCreateNoTxDataSource");
      try
      {
         String module = "test-no-tx-hsqldb-ds.xml";

         // remove module in case it exists
         removeModule(module);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", "TestNoTxDataSource");
         props.put("connection-url", "jdbc:hsqldb:hsql://localhost:1701");
         props.put("driver-class", "org.hsqldb.jdbcDriver");

         // Add some fake connection properties
         Hashtable ht = new Hashtable();
         ht.put("property1", "someString");
         ht.put("property2", new Boolean(true));
         ht.put("property3", new Integer(666));
         props.put("connection-properties", ht);

         props.put("user-name", "sa");
         props.put("password", "");

         props.put("min-pool-size", new Integer(5));
         props.put("max-pool-size", new Integer(20));
         props.put("track-statements", "NOWARN");
         props.put("security-config", "APPLICATION-MANAGED-SECURITY");
         props.put("type-mapping", "Hypersonic SQL");
         props.put("dependencies", new ObjectName[] { new ObjectName(
               "jboss:service=Hypersonic") });

         String template = "no-tx-datasource";

         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);
      }
      catch (Exception e)
      {
         super.fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to create an xa-datasource (don't deploy)
    */
   public void testCreateXaDataSource() throws Exception
   {
      log.info("+++ testCreateXaDataSource");
      try
      {
         String module = "test-xa-oracle-ds.xml";

         // remove module in case it exists
         removeModule(module);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", "TestOracleXaDataSource");
         props.put("track-connection-by-tx", new Boolean(true));
         props.put("is-same-RM-override-value", new Boolean(false));
         props.put("xa-datasource-class",
               "oracle.jdbc.xa.client.OracleXADataSource");

         // Add some xa-datasource-properties
         Hashtable ht = new Hashtable();
         ht.put("URL", "jdbc:oracle:oci8:@tc");
         ht.put("User", "scott");
         ht.put("Password", "tiger");
         props.put("xa-datasource-properties", ht);

         props.put("exception-sorter-class-name",
               "org.jboss.resource.adapter.jdbc.vendor.OracleExceptionSorter");
         props.put("no-tx-separate-pools", new Boolean(true));
         props.put("type-mapping", "Oracle9i");

         String template = "xa-datasource";

         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);
      }
      catch (Exception e)
      {
         super.fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to create and deploy a local-tx-datasource
    */
   public void testCreateAndDeployLocalTxDataSource() throws Exception
   {
      log.info("+++ testCreateAndDeployLocalTxDataSource");
      try
      {
         String module = "test-local-tx-hsqldb-ds.xml";
         String jndiName = "TestLocalTxHsqlDataSource";

         // undeploye module in case it's deployed
         undeployModule(module);
         // remove module in case it exists
         removeModule(module);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", jndiName); // use a name other than default
         props.put("use-java-context", new Boolean(false)); // set this to
         // false to allow
         // remote lookup
         props.put("connection-url", "jdbc:hsqldb:hsql://localhost:1701"); // using
         // hsqldb
         props.put("driver-class", "org.hsqldb.jdbcDriver");
         props.put("user-name", "sa");
         props.put("password", "");
         props.put("min-pool-size", new Integer(5));
         props.put("max-pool-size", new Integer(20));
         props.put("idle-timeout-minutes", new Integer(0));
         props.put("track-statements", "TRUE");
         props.put("security-config", "APPLICATION-MANAGED-SECURITY");
         props.put("type-mapping", "Hypersonic SQL");
         props.put("dependencies", new ObjectName[] { new ObjectName(
               "jboss:service=Hypersonic") });

         String template = "local-tx-datasource";

         // In case of any problem an exception will be thrown
         module = createModule(module, template, props);

         boolean isDeployed = deployModule(module);

         // was deployment succesful?
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // see if we can get a connection
         InitialContext ic = new InitialContext();
         DataSource ds = (DataSource) ic.lookup(jndiName);
         Connection connection = ds.getConnection();
         connection.close();

         // undeploy module
         undeployModule(module);
         // remove module
         removeModule(module);

         // regenerate with wrong usename
         props.put("user-name", "rogue-admin");
         module = createModule(module, template, props);

         // deploy again
         isDeployed = deployModule(module);

         // was deployment succesful?
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // lookup the datasource again and see if we can get a connection
         // it should fail this time
         try
         {
            ds = (DataSource) ic.lookup(jndiName);
            connection = ds.getConnection();
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // ok
         }
         // undeploy module
         undeployModule(module);
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to update an mbean with a standard set of properties. In this case,
    * the mbean name is bad and an error should be returned from the deployment
    * service.
    */
   public void testUpdateMBeanBadName() throws Exception
   {
      log.info("+++ testUpdateMBeanBadName");

      // Establish the property values
      Properties attrs = new Properties();
      attrs.put("Attr1", "aaaaa");
      attrs.put("Attr2", "bbbbb");
      ;

      // Set up the MBean configuration bean:
      MBeanData data = new MBeanData();
      data.setTemplateName("mbean-update");
      data.setAttributes(attrs);

      // Try with a null mbean name:
      try
      {
         data.setName(null);
         updateMBean(data);
         String msg = "Unexpectedly found mbean with invalid name: " + data;
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }

      // Try with an empty mbean name:
      try
      {
         data.setName("");
         updateMBean(data);
         String msg = "Unexpectedly found mbean with invalid name: " + data;
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }

      // Try with an unknown mbean name:
      try
      {
         data.setName("jboss.xxx:service=NoneSuch");
         updateMBean(data);
         String msg = "Unexpectedly found mbean with name: " + data;
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }

      // Try with an unknown mbean name with multiple attributes::
      try
      {
         data.setName("jboss.xxx:service=NoneSuch,type=Unknown,Alias=whatever");
         updateMBean(data);
         String msg = "Unexpectedly found mbean with name: " + data;
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }
   }

   /**
    * Try to update an mbean with a standard set of properties. In this case, no
    * template is given for doing the update. The update should fail.
    */
   public void testUpdateMBeanBadTemplate() throws Exception
   {
      log.info("+++ testUpdateMBeanBadTemplate");

      // Establish the property values
      Properties attrs = new Properties();
      attrs.put("Attr1", "aaaaa");
      attrs.put("Attr2", "bbbbb");
      ;

      // Set up the MBean configuration bean:
      MBeanData data = new MBeanData();
      data.setName("jboss.mq:service=MessageCache");
      data.setAttributes(attrs);

      // Try with a null template name:
      try
      {
         data.setTemplateName(null);
         log.info("Template=" + data.getTemplateName());
         updateMBean(data);
         String msg = "Update was successful with null template name: "
               + data.getTemplateName();
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }

      // Try again, but with an empty template name:
      try
      {
         data.setTemplateName("");
         log.info("Template=" + data.getTemplateName());
         updateMBean(data);
         String msg = "Update was successful with empty template name "
               + data.getTemplateName();
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }

      // Try again, but with an unknown template name:
      try
      {
         data.setTemplateName("nonesuch");
         log.info("Template=" + data.getTemplateName());
         updateMBean(data);
         String msg = "Update was successful with null template "
               + data.getTemplateName();
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }
   }

   /**
    * Try to update an mbean with a standard set of properties. In this case,
    * the mbean attributes are bad. The update should succeed.
    */
   public void testUpdateMBeanBadAttributes() throws Exception
   {
      log.info("+++ testUpdateMBeanBadAttributes");

      // Set up the MBean configuration bean:
      MBeanData data = new MBeanData();
      data.setName("jboss.mq:service=MessageCache");
      data.setTemplateName("mbean-update");
      data.setAttributes(null);

      // Update the mbean
      boolean result = updateMBean(data);
      // Yes, I could have used assertTrue, but I want to log all errors:
      if (result)
      {
         log.info("passed");
      }
      else
      {
         String msg = "Failed to update mbean when attributes were null: "
               + data;
         log.error(msg);
         fail(msg);
      }
   }

   /**
    * Try to update an mbean with a standard set of properties. In this case,
    * the mbean data is bad. The update should fail.
    */
   public void testUpdateMBeanBadData() throws Exception
   {
      log.info("+++ testUpdateMBeanBadData");

      // Attempt to update the mbean with no data
      try
      {
         updateMBean(null);
         String msg = "Update was successful with null data";
         log.error(msg);
         fail(msg);
      }
      catch (MBeanException e)
      {
         // expected
         log.info("passed");
      }
   }

   /**
    * Try to update an mbean with a standard set of properties. The mbean we use
    * is the MessageCache mbean for the messaging service. The mbean properties
    * are changed to innocuous values that should cause no problems. This test
    * might have to be updated when the new messing service is rolled out.
    */
   public void testUpdateMBeanStandard() throws Exception
   {
      log.info("+++ testUpdateMBeanStandard");

      // Establish the property values
      String value = "999";
      Properties attrs = new Properties();
      attrs.put("MaxMemoryMark", value);
      attrs.put("HighMemoryMark", value);
      attrs.put("CacheStore", "jboss.mq:service=PersistenceManager");
      attrs.put("MaximumHard", value);
      attrs.put("MinimumHard", value);
      attrs.put("SoftenAtLeastEveryMillis", value);
      attrs.put("SoftenNoMoreOftenThanMillis", value);
      attrs.put("MakeSoftReferences", "true");
      // For some reason, the following value is rounded up to 1000 if set to
      // 999, so we will use 1000 as the value:
      attrs.put("SoftenWaitMillis", "1000");

      // Set up the MBean configuration bean:
      MBeanData data = new MBeanData();
      String name = "jboss.mq:service=MessageCache";
      data.setName(name);
      data.setTemplateName("mbean-update");
      data.setAttributes(attrs);

      // Update and verify the mbean:
      boolean result = updateMBean(data);
      // Yes, I could have used assertTrue, but I want to log all errors:
      if (!result)
      {
         String msg = "Failed to update mbean " + data;
         log.error(msg);
         fail(msg);
      }
      verifyMBean(data, null);
   }

   /**
    * Try to update an mbean where one of the properties has a value expressed
    * as a nested XML property rather than as simple text. The mbean we use is
    * the SecurityManager mbean for the messaging service. The mbean property
    * that is of interest is the DefaultSecurityConfig. This test might have to
    * be updated when the new messing service is rolled out.
    */
   public void testUpdateMBeanNested() throws Exception
   {
      log.info("+++ testUpdateMBeanNested");

      // Establish the property values
      Properties attrs = new Properties();
      String nestedAttr = "DefaultSecurityConfig";
      attrs
            .put(
                  nestedAttr,
                  "<security><role name=\"guest\" read=\"true\" write=\"false\" create=\"false\"/></security>");
      attrs.put("SecurityDomain", "java:/jaas/jbossmq");

      // Establish the dependency values
      Properties depends = new Properties();
      depends.put("NextInterceptor", "jboss.mq:service=DestinationManager");

      // Set up the MBean configuration bean:
      MBeanData data = new MBeanData();
      String name = "jboss.mq:service=SecurityManager";
      data.setName(name);
      data.setTemplateName("mbean-update");
      data.setAttributes(attrs);
      data.setDepends(depends);

      // Update and verify the mbean:
      boolean result = updateMBean(data);
      // Yes, I could have used assertTrue, but I want to log all errors:
      if (!result)
      {
         String msg = "Failed to update mbean " + data;
         log.error(msg);
         fail(msg);
      }
      verifyMBean(data, nestedAttr);
   }

   /**
    * Try to update an mbean that has a name with multiple attributes. We will
    * update the mbean twice, each time presenting the name attributes in a
    * different order. We will use one of the InvocationLayer mbeans from the
    * uil2-service.xml file.
    */
   public void testUpdateMBeanXpath() throws Exception
   {
      log.info("+++ testUpdateMBeanXpath");

      // Establish the property values
      Properties attrs = new Properties();
      attrs.put("FromName", "XXXXXXXXX");
      attrs.put("ToName", "AAAAAAAAAA");

      // Establish the dependency values
      Properties depends = new Properties();
      depends.put("", "jboss:service=Naming");

      // Set up the MBean configuration bean, this time with the attributes
      // in the order in which they appear when asking the mbean for the name
      MBeanData data = new MBeanData();
      data
            .setName("jboss.mq:alias=UIL2XAConnectionFactory,service=InvocationLayer,type=UIL2XA");
      data.setTemplateName("mbean-update");
      data.setAttributes(attrs);
      data.setDepends(depends);

      // Update and verify the mbean:
      if (!updateMBean(data))
      {
         String msg = "Failed to update mbean: " + data;
         log.error(msg);
         fail(msg);
      }
      verifyMBean(data, null);

      // Try again, but with the name attributes in a different order. This
      // time in the order in which they appear in the xml file:
      attrs = new Properties();
      attrs.put("FromName", "RRRRRRRRRRR");
      attrs.put("ToName", "QQQQQQQQQQQQQ");
      data
            .setName("jboss.mq:service=InvocationLayer,type=UIL2XA,alias=UIL2XAConnectionFactory");
      data.setAttributes(attrs);

      // Update and verify the mbean:
      if (!updateMBean(data))
      {
         String msg = "Failed to update mbean: " + data;
         log.error(msg);
         fail(msg);
      }
      verifyMBean(data, null);

      // Try again, but with the name attributes in another order. This
      // time in the order is different that the mbean name or xml file:
      attrs = new Properties();
      attrs.put("FromName", "LLLLLLLLLLLL");
      attrs.put("ToName", "KKKKKKKKKKKK");
      data
            .setName("jboss.mq:type=UIL2XA,alias=UIL2XAConnectionFactory,service=InvocationLayer");
      data.setAttributes(attrs);

      // Update and verify the mbean:
      if (!updateMBean(data))
      {
         String msg = "Failed to update mbean: " + data;
         log.error(msg);
         fail(msg);
      }
      verifyMBean(data, null);
   }

   /**
    * Try to update a local transaction data source. This test case uses the
    * DefaultDS data source.
    */
   public void testUpdateDataSourceLocal() throws Exception
   {
      log.info("+++ testUpdateDataSourceLocal");

      try
      {
         String jndiName = "DefaultDS";

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", jndiName);
         props.put("use-java-context", new Boolean(false)); // set this to
         // false to allow
         // remote lookup
         props.put("connection-url", "jdbc:hsqldb:hsql://localhost:1701"); // using
         // hsqldb
         props.put("driver-class", "org.hsqldb.jdbcDriver");
         props.put("user-name", "sa");
         props.put("password", "");
         props.put("min-pool-size", new Integer(9));
         props.put("max-pool-size", new Integer(99));
         props.put("idle-timeout-minutes", new Integer(99));
         props.put("track-statements", "TRUE");
         props.put("security-config", "APPLICATION-MANAGED-SECURITY");
         props.put("type-mapping", "Hypersonic SQL");
         props.put("dependencies", new ObjectName[] { new ObjectName(
               "jboss:service=Hypersonic") });

         String template = "local-tx-datasource";

         // In case of any problem an exception will be thrown
         boolean isDeployed = updateDataSource("hsqldb-ds.xml", template, props);
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // see if we can get a connection
         boolean connected = connectToDataSource(jndiName);
         assertTrue("Data source " + jndiName + " connected successful : "
               + connected, connected);

         // Try with the other module name
         isDeployed = updateDataSource("hsqldb", template, props);
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // see if we can get a connection
         connected = connectToDataSource(jndiName);
         assertTrue("Data source " + jndiName + " connected successful : "
               + connected, connected);

         log.info("passed");
      }
      catch (Exception e)
      {
         log.error("failed", e);
         fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to update a no transaction data source. This test case uses the data
    * source created by testCreateNoTxDataSource. This test is hidden for now,
    * the test-no-tx-hsqldb data source is never deployed and is thus not
    * visible.
    */
   public void hide_testUpdateDataSourceNo() throws Exception
   {
      log.info("+++ testUpdateDataSourceNo");

      try
      {

         String module = "test-no-tx-hsqldb-ds.xml";

         // remove module in case it exists
         removeModule(module);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", "TestNoTxDataSource");
         props.put("connection-url", "jdbc:hsqldb:hsql://localhost:1701");
         props.put("driver-class", "org.hsqldb.jdbcDriver");

         // Add some fake connection properties
         Hashtable ht = new Hashtable();
         ht.put("property1", "someString");
         ht.put("property2", new Boolean(true));
         ht.put("property3", new Integer(666));
         props.put("connection-properties", ht);

         props.put("user-name", "sa");
         props.put("password", "");

         props.put("min-pool-size", new Integer(5));
         props.put("max-pool-size", new Integer(20));
         props.put("track-statements", "NOWARN");
         props.put("security-config", "APPLICATION-MANAGED-SECURITY");
         props.put("type-mapping", "Hypersonic SQL");
         props.put("dependencies", new ObjectName[] { new ObjectName(
               "jboss:service=Hypersonic,") });

         String template = "no-tx-datasource";

         // In case of any problem an exception will be thrown
         boolean isDeployed = updateDataSource(module, template, props);
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // We don't try to get a connection because the original
         // data source was never deployed.

         log.info("passed");
      }
      catch (Exception e)
      {
         log.error("failed", e);
         fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to update an XA transaction data source. This test case uses the data
    * source created by testCreateXaDataSource. This test is hidden for now, the
    * test-xa-oracle data source is never deployed and is thus not visible.
    */
   public void hide_testUpdateDataSourceXa() throws Exception
   {
      log.info("+++ testUpdateDataSourceNo");

      try
      {
         String module = "test-xa-oracle-ds.xml";

         // remove module in case it exists
         removeModule(module);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", "TestOracleXaDataSource");
         props.put("track-connection-by-tx", new Boolean(true));
         props.put("is-same-RM-override-value", new Boolean(false));
         props.put("xa-datasource-class",
               "oracle.jdbc.xa.client.OracleXADataSource");

         // Add some xa-datasource-properties
         Hashtable ht = new Hashtable();
         ht.put("URL", "jdbc:oracle:oci8:@tc");
         ht.put("User", "scott");
         ht.put("Password", "tiger");
         props.put("xa-datasource-properties", ht);

         props.put("exception-sorter-class-name",
               "org.jboss.resource.adapter.jdbc.vendor.OracleExceptionSorter");
         props.put("no-tx-separate-pools", new Boolean(true));
         props.put("type-mapping", "Oracle9i");

         String template = "xa-datasource";

         // In case of any problem an exception will be thrown
         boolean isDeployed = updateDataSource(module, template, props);
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // We don't try to get a connection because the original
         // data source was never deployed.

         log.info("passed");
      }
      catch (Exception e)
      {
         log.error("failed", e);
         fail("Caught exception, message: " + e.getMessage());
      }
      finally
      {
         // empty
      }
   }

   /**
    * Try to remove a data source using the removeDataSource() method. This test
    * case uses the "TestLocalTxHsqlDataSource" created by
    * testCreateAndDeployLocalTxDataSource().
    * 
    * Note that the "TestLocalTxHsqlDataSource" will be removed from the
    * test-local-tx-hsqldb-ds.xml file, thus further reference to this data
    * source may be in-appropriate.
    */
   public void testRemoveDataSource() throws Exception
   {
      log.info("+++ testRemoveDataSource");

      try
      {

         String module = "test-local-tx-hsqldb-ds.xml";
         String jndiName = "TestLocalTxHsqlDataSource";

         // Deploy the module
         boolean isDeployed = deployModule(module);

         // Was deployment succesful?
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // Prepare the template properties
         HashMap props = new HashMap();
         props.put("jndi-name", jndiName);

         String template = "datasource-remove";

         // In case of any problem an exception will be thrown
         isDeployed = removeDataSource(module, template, props);

         // The module should be deployed
         assertTrue("removed successful : " + isDeployed, isDeployed);

         // But we should NOT be able to get a connection
         try
         {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(jndiName);
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // Ok, undeploy the module
            undeployModule(module);
         }

         log.info("passed");
      }
      catch (Exception e)
      {
         log.error("failed", e);
         fail("Caught exception, message: " + e.getMessage());
      }
   }

   /**
    * Try to remove a data source using the removeDataSource() method from a
    * module with multiple data sources configured. This test case uses the
    * testMultipleDataSources-ds.xml module located under the
    * "testsuite/src/etc/deployment-test" directory.
    */
   public void testRemoveDataSourceFromMultiNodesModule() throws Exception
   {
      log.info("+++ testRemoveDataSourceFromMultiNodesModule");

      try
      {
         String module = "testMultipleDataSources-ds.xml";
         String jndiName1 = "TestDataSource1";
         String jndiName2 = "TestDataSource2";
         String delim = File.separator;

         // Find the server directory
         MBeanServerConnection server = getServer();
         ObjectName serverConfig = new ObjectName(
               "jboss.system:type=ServerConfig");
         String serverHome = ((File) server.getAttribute(serverConfig,
               "ServerHomeDir")).getCanonicalPath();
         log.info("serverHome = " + serverHome);

         // Find the directory where the test module resides
         String myPath = new File("").getAbsolutePath();
         log.info("myPath = " + myPath);
         int inx = myPath.lastIndexOf(delim);
         if (inx >= 0)
            myPath = myPath.substring(0, myPath.lastIndexOf(delim));

         // Copy the module to the deploy directory
         String source = myPath + delim + "src" + delim + "etc" + delim
               + "deployment-test" + delim + module;
         String target = serverHome + delim + "deploy" + delim + module;
         log.info("source = " + source);
         log.info("target = " + target);

         FileChannel srcChannel = new FileInputStream(source).getChannel();
         FileChannel destChannel = new FileOutputStream(target).getChannel();
         srcChannel.transferTo(0, srcChannel.size(), destChannel);
         srcChannel.close();
         destChannel.close();

         // See if we can get connection to the data sources:
         boolean connected = connectToDataSource(jndiName1);
         assertTrue("Data source " + jndiName1 + " connected successful : "
               + connected, connected);
         connected = connectToDataSource(jndiName2);
         assertTrue("Data source " + jndiName2 + " connected successful : "
               + connected, connected);

         // Prepare the template properties for removing "TestDataSource1"
         HashMap props = new HashMap();
         props.put("jndi-name", jndiName1);

         String template = "datasource-remove";

         // In case of any problem an exception will be thrown
         boolean isDeployed = removeDataSource(module, template, props);

         // The module should be deployed
         assertTrue("removed successful : " + isDeployed, isDeployed);

         // We should be able to connect to TestDataSource2
         connected = connectToDataSource(jndiName2);
         assertTrue("Data source " + jndiName2 + " connected successful : "
               + connected, connected);

         // But we should NOT be able connect to TestDataSource1
         try
         {
            InitialContext ic = new InitialContext();
            DataSource ds1 = (DataSource) ic.lookup(jndiName1);
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // Ok, remove the file
            File targetFile = new File(target);
            targetFile.delete();

         }

         log.info("passed");
      }
      catch (Exception e)
      {
         log.error("failed", e);
         fail("Caught exception, message: " + e.getMessage());
      }
   }

   /**
    * Try to remove a data source using the removeDataSource() method. In this
    * case, the module name is bad and an error should be returned from the
    * deployment service.
    */
   public void testRemoveDataSourceBadModule() throws Exception
   {
      log.info("+++ testRemoveDataSourceBadModule");

      // Setup the data
      String module = null;
      String template = "datasource-remove";
      HashMap props = new HashMap();

      // Try with a null module name:
      try
      {
         removeDataSource(module, template, props);
         fail("Shouldn't reach this point");
      }
      catch (Exception e)
      {
         // expected
         log.info("passed");
      }

      // Try with an empty module name:
      try
      {
         module = "";
         removeDataSource(module, template, props);
         fail("Shouldn't reach this point");
      }
      catch (Exception e)
      {
         // expected
         log.info("passed");
      }

      // Try with an unknown module name:
      try
      {
         module = "unknoenModule";
         removeDataSource(module, template, props);
         fail("Shouldn't reach this point");
      }
      catch (Exception e)
      {
         // expected
         log.info("passed");
      }
   }

   /**
    * Try to remove a data source using the removeDataSource() method. In this
    * case, the template name is bad and an error should be returned from the
    * deployment service.
    */
   public void testRemoveDataSourceBadTemplate() throws Exception
   {
      log.info("+++ testRemoveDataSourceBadTemplate");

      try
      {
         String module = "test-local-tx-hsqldb-ds.xml";
         String template = null;
         HashMap props = new HashMap();

         // Deploy the module
         boolean isDeployed = deployModule(module);

         // Was deployment succesful?
         assertTrue("deployed successful : " + isDeployed, isDeployed);

         // Try with a null template name:
         try
         {
            removeDataSource(module, template, props);
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // expected
            log.info("passed");
         }

         // Try with an empty template name:
         try
         {
            template = "";
            removeDataSource(module, template, props);
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // expected
            log.info("passed");
         }

         // Try with an unknown template name:
         try
         {
            template = "unknownTemplate";
            removeDataSource(module, template, props);
            fail("Shouldn't reach this point");
         }
         catch (Exception e)
         {
            // expected
            log.info("passed");
         }

         // Undeploy the module:
         undeployModule(module);
      }
      catch (Exception e)
      {
         log.error("failed", e);
         fail("Caught exception, message: " + e.getMessage());
      }
   }

   private String createModule(String module, String template, HashMap props)
         throws Exception
   {
      MBeanServerConnection server = getServer();

      // create the module
      module = (String) server
            .invoke(deploymentService, "createModule", new Object[] { module,
                  template, props }, new String[] { "java.lang.String",
                  "java.lang.String", "java.util.HashMap" });

      log.info("Module '" + module + "' created: " + module);
      return module;
   }

   private boolean removeModule(String module) throws Exception
   {
      MBeanServerConnection server = getServer();

      // remove the module, in case it exists
      Boolean removed = (Boolean) server.invoke(deploymentService,
            "removeModule", new Object[] { module },
            new String[] { "java.lang.String" });

      log.info("Module '" + module + "' removed: " + removed);

      return removed.booleanValue();
   }

   private boolean deployModule(String module) throws Exception
   {
      MBeanServerConnection server = getServer();

      // Deploy the module (move to ./deploy)
      server.invoke(deploymentService, "deployModuleAsynch",
            new Object[] { module }, new String[] { "java.lang.String" });

      return verifyDeploy(server, module);
   }

   private boolean undeployModule(String module) throws Exception
   {
      MBeanServerConnection server = getServer();
      try
      {
         // Get the deployed URL
         URL deployedURL = (URL) server.invoke(deploymentService,
               "getDeployedURL", new Object[] { module },
               new String[] { "java.lang.String" });

         // Undeploy the module (move to ./undeploy)
         server.invoke(deploymentService, "undeployModuleAsynch",
               new Object[] { module }, new String[] { "java.lang.String" });

         // Ask the MainDeployer every 3 secs, 5 times (15secs max wait) if
         // the module was undeployed
         Boolean isDeployed = new Boolean(false);
         for (int tries = 0; tries < 5; tries++)
         {
            // sleep for 3 secs
            Thread.sleep(3000);
            isDeployed = (Boolean) server
                  .invoke(mainDeployer, "isDeployed",
                        new Object[] { deployedURL },
                        new String[] { "java.net.URL" });

            if (!isDeployed.booleanValue())
            {
               break;
            }
         }
         log.info("Module '" + module + "' deployed: " + isDeployed);
         return isDeployed.booleanValue();
      }
      catch (Exception e)
      {
         // the module does not exist
         log.info("Ignoring caught exception, message: " + e.getMessage());
         return false;
      }
   }

   /**
    * Proxy method that makes the deployment service call to update an mbean.
    * 
    * @param data
    *           The data used to update the mbean
    * @return True if the mbean was updated, false otherwise.
    * @throws Exception
    *            Bad things happened.
    */
   private boolean updateMBean(MBeanData data) throws Exception
   {
      MBeanServerConnection server = getServer();
      log.info("Updating MBean '" + data + "'");

      // create the module
      boolean result = false;
      result = ((Boolean) server.invoke(deploymentService, "updateMBean",
            new Object[] { data },
            new String[] { "org.jboss.services.deployment.MBeanData" }))
            .booleanValue();

      log.info("MBean '" + data + "' update result: " + result);
      return result;
   }

   /**
    * Verifies that the mbean was updated successfully by comparing the values
    * of all of the attributes.
    * 
    * @param data
    *           The mbean data that was set.
    * @param nestedAttr
    *           If any of the mbean attribuets were nested xml data, set this to
    *           the name of said attribute. If not, set to null.
    */
   private void verifyMBean(MBeanData data, String nestedAttr)
   {
      try
      {
         // Wait for the changes to be deployed (assume 5 second scan delay):
         log.info("Wait 10 seconds for changes to deploy");
         Thread.sleep(10000);

         // Compare all of the changed attribute values to the actual values.
         // They must all match for the test to pass.
         InitialContext ic = new InitialContext();
         RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/invoker/RMIAdaptor");
         ObjectName objectName = new ObjectName(data.getName());
         Properties attrs = data.getAttributes();
         Enumeration keys = attrs.keys();
         log.info("Verifying MBean attribute values:");
         while (keys.hasMoreElements())
         {
            String attr = (String) keys.nextElement();
            String expected = (String) attrs.get(attr);
            Object obj = server.getAttribute(objectName, attr);
            String actual = obj.toString();
            log.info("-- attribute    = " + attr);
            log.info("   expect value = " + expected);
            log.info("   actual value = " + actual);
            log.info("   actual type  = " + obj.getClass().getName());
            // Note that the value for the nested attribute is not returned
            // as expected, so we will not verify that it is correct:
            if (!actual.equals(expected) && nestedAttr != null
                  && !attr.equals(nestedAttr))
            {
               String msg = "Attribute '" + attr + "' has actual value '"
                     + actual + "', expected value " + expected + "'";
               log.error(msg);
               fail(msg);
            }
         }
         log.info("passed");
      }
      catch (Exception e)
      {
         log.error(e);
         fail("Unexpected error: " + e.getMessage());
      }
   }

   /**
    * Calls the deployment service to update the data source.
    */
   private boolean updateDataSource(String module, String template,
         HashMap props) throws Exception
   {
      log.info("updateDataSource('" + module + "', '" + template + "', "
            + props);
      MBeanServerConnection server = getServer();

      // update the data source
      module = (String) server
            .invoke(deploymentService, "updateDataSource", new Object[] {
                  module, template, props }, new String[] { "java.lang.String",
                  "java.lang.String", "java.util.HashMap" });

      // sleep for 3 secs to allow the old datasource to be undeployed first
      // before we verify deployment
      Thread.sleep(3000);
      return verifyDeploy(server, module);
   }

   /**
    * Calls the deployment service to remove the data source.
    */
   private boolean removeDataSource(String module, String template,
         HashMap props) throws Exception
   {
      log.info("removeDataSource('" + module + "', '" + template + "', "
            + props);
      MBeanServerConnection server = getServer();

      // remove the data source
      module = (String) server
            .invoke(deploymentService, "removeDataSource", new Object[] {
                  module, template, props }, new String[] { "java.lang.String",
                  "java.lang.String", "java.util.HashMap" });

      // sleep for 3 secs to allow the datasource to be undeployed first
      // before we verify deployment
      Thread.sleep(3000);
      return verifyDeploy(server, module);
   }

   /**
    * Try to connect to the specified data source. Try the connection every 3
    * seconds, maximum 5 times.
    */
   private boolean connectToDataSource(String jndiName) throws Exception
   {
      boolean connected = false;
      InitialContext ic = new InitialContext();
      DataSource ds = null;
      Connection connection = null;

      // See if we can get a connection
      for (int tries = 0; tries < 5; tries++)
      {
         try
         {
            ds = (DataSource) ic.lookup(jndiName);
            connection = ds.getConnection();
            connection.close();
            connected = true;
            log.info("Connected to data source: " + jndiName);
            break;
         }
         catch (Exception e)
         {
            log.info("Unable to connect to data source: " + jndiName
                  + ". Try again");
            // Sleep for 3 secs then try again
            Thread.sleep(3000);
         }
      }

      return connected;
   }

   /**
    * Verifies that a given module is acutally deployed. Waits for a while,
    * checking every few seconds, to see if the module has deployed yet.
    * 
    * @param server
    *           User to invoke methods on the dpeloyment service.
    * @param module
    *           The name of the module (must include the suffix).
    * @return
    */
   private boolean verifyDeploy(MBeanServerConnection server, String module)
         throws Exception
   {
      // Get the deployed URL
      URL deployedURL = (URL) server.invoke(deploymentService,
            "getDeployedURL", new Object[] { module },
            new String[] { "java.lang.String" });

      // Ask the MainDeployer every 3 secs, 5 times (15secs max wait) if the
      // module was deployed
      Boolean isDeployed = new Boolean(false);
      for (int tries = 0; tries < 5; tries++)
      {
         // sleep for 3 secs
         Thread.sleep(3000);
         isDeployed = (Boolean) server.invoke(mainDeployer, "isDeployed",
               new Object[] { deployedURL }, new String[] { "java.net.URL" });

         if (isDeployed.booleanValue())
         {
            break;
         }
      }
      log.info("Module '" + module + "' deployed: " + isDeployed);
      return isDeployed.booleanValue();
   }
}