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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** Test of using ProfileService

 @author Scott.Stark@jboss.org
 @version $Revision: 105883 $
 */
public class ConnectionFactoryUnitTestCase extends AbstractProfileServiceTest
{
   
   /**
    * We need to define the order in which tests runs
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();

      // These tests only make sense if JBM
      if (JMSDestinationsUtil.isJBM())
      {
          // DataSource
          suite.addTest(new ConnectionFactoryUnitTestCase("testAddDataSource"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testRemoveDataSource"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testAddXADataSource"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testRemoveXADataSource"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testAddTxConnectionFactory"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testRemoveTxConnectionFactory"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testAddTxXAConnectionFactory"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testRemoveTxXAConnectionFactory"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testAddNoTxConnectionFactory"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testRemoveNoTxConnectionFactory"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testAddNoTxDataSource"));
          suite.addTest(new ConnectionFactoryUnitTestCase("testRemoveNoTxDataSource"));
      }

      return suite;
   }

   public ConnectionFactoryUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test adding a new hsql DataSource deployment (JBAS-4671)
    * @throws Exception
    */
   public void testAddDataSource() throws Exception
   {
      String jndiName = "TestLocalTxDs";
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      addNonXaDsProperties(propValues, jndiName, "jboss-local-jdbc.rar", "javax.sql.DataSource");
      createComponentTest("LocalTxDataSourceTemplate", propValues, "testLocalTxDs",
         KnownComponentTypes.DataSourceTypes.LocalTx.getType(), jndiName);
   }

   public void testRemoveDataSource()
      throws Exception
   {
      removeDeployment("testLocalTxDs-ds.xml");
   }

   /**
    * Test adding a new hsql DataSource deployment
    * @throws Exception
    */
   public void testAddXADataSource() throws Exception
   {
      String jndiName = "TestXaDs";
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();

      addCommonDsProperties(propValues, jndiName, "jboss-xa-jdbc.rar", "javax.sql.DataSource");

      propValues.put("xa-datasource-class", SimpleValueSupport.wrap("org.hsqldb.jdbcDriver"));
      propValues.put("xa-resource-timeout", SimpleValueSupport.wrap(new Integer(256)));
      propValues.put("interleaving", SimpleValueSupport.wrap(true));

      HashMap<String, String> xaPropValues = new HashMap<String, String>();
      xaPropValues.put("URL", "jdbc:hsqldb");
      xaPropValues.put("User", "sa");
      xaPropValues.put("Password", "");

      //MetaValue metaValue = getMetaValueFactory().create(xaPropValues, getMapType());
      MetaValue metaValue = this.compositeValueMap(xaPropValues);
      propValues.put("xa-datasource-properties", metaValue);

      createComponentTest("XADataSourceTemplate", propValues, "testXaDs",
         KnownComponentTypes.DataSourceTypes.XA.getType(), jndiName);
 
      // Query the interleaving 
      ManagementView mgtView = getManagementView();
      ComponentType type = KnownComponentTypes.DataSourceTypes.XA.getType();
      ManagedComponent txcf = mgtView.getComponent(jndiName, type);
      assertNotNull(txcf);
      ManagedProperty interleaving = txcf.getProperty("interleaving");
      assertNotNull("interleaving", interleaving);
      MetaValue interleavingMV = interleaving.getValue();
      assertNotNull("interleaving.value", interleavingMV);
      assertEquals("interleaving.value is true", SimpleValueSupport.wrap(Boolean.TRUE), interleavingMV);
   }

   /**
    * removes the XADataSource created in the testAddXADataSource
    * @throws Exception
    */
   public void testRemoveXADataSource()
      throws Exception
   {
      removeDeployment("testXaDs-ds.xml");
   }

   /**
    * Test adding a new tx-connection-factory deployment
    * @throws Exception
    */
   public void testAddTxConnectionFactory() throws Exception
   {
      String jndiName = "TestTxCf";
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();

      addCommonCfProperties(propValues, jndiName, "jms-ra.rar", "org.jboss.resource.adapter.jms.JmsConnectionFactory");

      Map<String, String> xaProps = new HashMap<String, String>();
      xaProps.put("SessionDefaultType", "javax.jms.Topic");
      xaProps.put("SessionDefaultType.type", "java.lang.String");
      xaProps.put("JmsProviderAdapterJNDI", "java:/DefaultJMSProvider");
      xaProps.put("JmsProviderAdapterJNDI.type", "java.lang.String");
      MetaValue metaValue = this.compositeValueMap(xaProps);

      propValues.put("config-property", metaValue);

      propValues.put("xa-transaction", SimpleValueSupport.wrap(Boolean.FALSE));
      propValues.put("xa-resource-timeout", SimpleValueSupport.wrap(new Integer(256)));

      // todo: how to set the specific domain?
      //ApplicationManagedSecurityMetaData secDomain = new ApplicationManagedSecurityMetaData();
      //props.get("security-domain").setValue(secDomain);

      createComponentTest("TxConnectionFactoryTemplate", propValues, "testTxCf",
         new ComponentType("ConnectionFactory", "Tx"), jndiName);
      // Query the interleaving 
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("ConnectionFactory", "Tx");
      ManagedComponent txcf = mgtView.getComponent(jndiName, type);
      assertNotNull(txcf);
      ManagedProperty interleaving = txcf.getProperty("interleaving");
      assertNotNull("interleaving", interleaving);
      MetaValue interleavingMV = interleaving.getValue();
      assertNotNull("interleaving.value", interleavingMV);
      
   }

   /**
    * removes the tx-connection-factory created in the testAddTxConnectionFactory
    * @throws Exception
    */
   public void testRemoveTxConnectionFactory()
      throws Exception
   {
      removeDeployment("testTxCf-ds.xml");
   }

   /**
    * Test adding a new tx-connection-factory deployment with xa enabled
    * @throws Exception
    */
   public void testAddTxXAConnectionFactory() throws Exception
   {
      String jndiName = "TestTxCf";
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();

      addCommonCfProperties(propValues, jndiName, "jms-ra.rar", "org.jboss.resource.adapter.jms.JmsConnectionFactory");

      Map<String, String> xaProps = new HashMap<String, String>();
      xaProps.put("SessionDefaultType", "javax.jms.Topic");
      xaProps.put("SessionDefaultType.type", "java.lang.String");
      xaProps.put("JmsProviderAdapterJNDI", "java:/DefaultJMSProvider");
      xaProps.put("JmsProviderAdapterJNDI.type", "java.lang.String");
      MetaValue metaValue = this.compositeValueMap(xaProps);

      propValues.put("config-property", metaValue);

      propValues.put("xa-transaction", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("xa-resource-timeout", SimpleValueSupport.wrap(new Integer(256)));
      propValues.put("interleaving", SimpleValueSupport.wrap(Boolean.TRUE));

      // todo: how to set the specific domain?
      //ApplicationManagedSecurityMetaData secDomain = new ApplicationManagedSecurityMetaData();
      //props.get("security-domain").setValue(secDomain);

      createComponentTest("TxConnectionFactoryTemplate", propValues, "testTxXACf",
         new ComponentType("ConnectionFactory", "Tx"), jndiName);
      // Query the interleaving 
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("ConnectionFactory", "Tx");
      ManagedComponent txcf = mgtView.getComponent(jndiName, type);
      assertNotNull(txcf);
      ManagedProperty interleaving = txcf.getProperty("interleaving");
      assertNotNull("interleaving", interleaving);
      MetaValue interleavingMV = interleaving.getValue();
      assertNotNull("interleaving.value", interleavingMV);
      assertEquals(SimpleValueSupport.wrap(Boolean.TRUE), interleavingMV);
   }

   /**
    * removes the tx-connection-factory created in the testAddXAConnectionFactory
    * @throws Exception
    */
   public void testRemoveTxXAConnectionFactory()
      throws Exception
   {
      removeDeployment("testTxXACf-ds.xml");
   }

   /**
    * Test adding a new no-tx-datasource deployment (JBAS-4671)
    * @throws Exception
    */
   public void testAddNoTxDataSource() throws Exception
   {
      String jndiName = "TestNoTxDs";
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();
      addNonXaDsProperties(propValues, jndiName, "jboss-local-jdbc.rar", "javax.sql.DataSource");
      createComponentTest("NoTxDataSourceTemplate", propValues, "testNoTxDs", KnownComponentTypes.DataSourceTypes.NoTx.getType(), jndiName);
   }

   public void testRemoveNoTxDataSource()
      throws Exception
   {
      removeDeployment("testNoTxDs-ds.xml");
   }

   /**
    * Test adding a new no-tx-connection-factory deployment
    * @throws Exception
    */
   public void testAddNoTxConnectionFactory() throws Exception
   {
      String jndiName = "TestNoTxCf";
      Map<String, MetaValue> propValues = new HashMap<String, MetaValue>();

      addCommonCfProperties(propValues, jndiName, "jms-ra.rar", "org.jboss.resource.adapter.jms.JmsConnectionFactory");

      Map<String, String> xaProps = new HashMap<String, String>();
      xaProps.put("SessionDefaultType", "javax.jms.Topic");
      xaProps.put("SessionDefaultType.type", "java.lang.String");
      xaProps.put("JmsProviderAdapterJNDI", "java:/DefaultJMSProvider");
      xaProps.put("JmsProviderAdapterJNDI.type", "java.lang.String");
      MetaValue metaValue = this.compositeValueMap(xaProps);
      propValues.put("config-property", metaValue);

      propValues.put("config-property", 
            new MapCompositeValueSupport(new HashMap<String, MetaValue>(),
                  new MapCompositeMetaType(SimpleMetaType.STRING)));
      // todo: how to set the specific domain?
      //ApplicationManagedSecurityMetaData secDomain = new ApplicationManagedSecurityMetaData();
      //props.get("security-domain").setValue(secDomain);

      ComponentType compType = new ComponentType("ConnectionFactory", "NoTx");
      createComponentTest("NoTxConnectionFactoryTemplate", propValues, "testNoTxCf", compType, jndiName);

      // Validate the config-property
      ManagementView mgtView = getManagementView();
      ManagedComponent dsMC = getManagedComponent(mgtView, compType, jndiName);
      ManagedProperty configProperty = dsMC.getProperty("config-property");
      assertNotNull(configProperty);
      MetaValue value = configProperty.getValue();
      assertTrue("MapCompositeMetaType", value.getMetaType() instanceof MapCompositeMetaType);
      
      MapCompositeValueSupport cValue = (MapCompositeValueSupport) value;
      cValue.put("testKey", new SimpleValueSupport(SimpleMetaType.STRING, "testValue"));
      
      mgtView.updateComponent(dsMC);

      mgtView = getManagementView();
      dsMC = getManagedComponent(mgtView, compType, jndiName);
      configProperty = dsMC.getProperty("config-property");
      assertNotNull(configProperty);
      cValue = (MapCompositeValueSupport) configProperty.getValue();
      assertNotNull(cValue.get("testKey"));
   }

   /**
    * removes the tx-connection-factory created in the testAddTxConnectionFactory
    * @throws Exception
    */
   public void testRemoveNoTxConnectionFactory()
      throws Exception
   {
      removeDeployment("testNoTxCf-ds.xml");
   }


   /**
    * Validate that there is only 1 DefaultDS ManagedComponent
    * @throws Exception
    */
   public void testJTAComponentCount()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "JTA");
      Set<ManagedComponent> comps = mgtView.getComponentsForType(type);
      int count = comps.size();
      assertEquals("There is 1 MCBean:JTA ManagedComponent", 1, 1);
      ManagedComponent comp = comps.iterator().next();
      Map<String, ManagedProperty> props = comp.getProperties();
      for(ManagedProperty prop : props.values())
      {
         log.info(prop+", : "+prop.getValue());
      }
   }

   // Private and protected

   private void addNonXaDsProperties(Map<String, MetaValue> propValues,
                                     String jndiName,
                                     String rarName,
                                     String conDef)
   {
      addCommonDsProperties(propValues, jndiName, rarName, conDef);

      propValues.put("transaction-isolation", SimpleValueSupport.wrap("TRANSACTION_SERIALIZABLE"));
      propValues.put("user-name", SimpleValueSupport.wrap("sa"));
      propValues.put("password", SimpleValueSupport.wrap(""));

      // non xa ds
      propValues.put("driver-class", SimpleValueSupport.wrap("org.hsqldb.jdbcDriver"));
      propValues.put("connection-url", SimpleValueSupport.wrap("jdbc:hsqldb:."));
      // A metadata type with a null type-mapping, JBAS-6215
      MutableCompositeMetaType metadataType = new MutableCompositeMetaType("org.jboss.resource.metadata.mcf.DBMSMetaData", "metadata type");
      metadataType.addItem("typeMapping", "The jdbc type mapping", SimpleMetaType.STRING);
      HashMap<String, MetaValue> items = new HashMap<String, MetaValue>();
      items.put("typeMapping", null);
      CompositeValueSupport metadata = new CompositeValueSupport(metadataType, items);
      propValues.put("metadata", metadata);

      // todo: connection-properties
   }

   private void addCommonDsProperties(Map<String, MetaValue> propValues,
                                              String jndiName,
                                              String rarName,
                                              String conDef)
   {
      addCommonCfProperties(propValues, jndiName, rarName, conDef);
      propValues.put("new-connection-sql", SimpleValueSupport.wrap("CALL ABS(2.0)"));
      propValues.put("check-valid-connection-sql", SimpleValueSupport.wrap("CALL ABS(1.0)"));
      propValues.put("valid-connection-checker-class-name", SimpleValueSupport.wrap("org.jboss.resource.adapter.jdbc.vendor.DummyValidConnectionChecker"));
      propValues.put("exception-sorter-class-name", SimpleValueSupport.wrap("org.jboss.resource.adapter.jdbc.vendor.DummyExceptionSorter"));
      propValues.put("stale-connection-checker-class-name", SimpleValueSupport.wrap("org.jboss.resource.adapter.jdbc.vendor.DummyValidConnectionChecker"));
      propValues.put("track-statements", SimpleValueSupport.wrap(""));
      propValues.put("prepared-statement-cache-size", SimpleValueSupport.wrap(12));
      propValues.put("share-prepared-statements", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("set-tx-query-timeout", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("query-timeout", SimpleValueSupport.wrap(new Integer(100)));
      propValues.put("url-delimiter", SimpleValueSupport.wrap("+"));
      propValues.put("url-selector-strategy-class-name", SimpleValueSupport.wrap("org.jboss.test.jca.support.MyURLSelector"));
      propValues.put("use-try-lock", SimpleValueSupport.wrap(new Integer(5000)));
   }

   private void addCommonCfProperties(Map<String, MetaValue> propValues,
                                    String jndiName,
                                    String rarName,
                                    String conDef)
   {
      propValues.put("jndi-name", SimpleValueSupport.wrap(jndiName));
      propValues.put("rar-name", SimpleValueSupport.wrap(rarName));
      propValues.put("use-java-context", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("connection-definition", SimpleValueSupport.wrap(conDef));
      //propValues.put("jmx-invoker-name", SimpleValueSupport.wrap("jboss:service=invoker,type=jrmp"));
      propValues.put("min-pool-size", SimpleValueSupport.wrap(new Integer(0)));
      propValues.put("max-pool-size", SimpleValueSupport.wrap(new Integer(11)));
      propValues.put("blocking-timeout-millis", SimpleValueSupport.wrap(new Long(15000)));
      propValues.put("idle-timeout-minutes", SimpleValueSupport.wrap(new Integer(111)));
      propValues.put("prefill", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("background-validation", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("background-validation-millis", SimpleValueSupport.wrap(new Long(5000)));
      propValues.put("validate-on-match", SimpleValueSupport.wrap(Boolean.FALSE));
      propValues.put("use-strict-min", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("no-tx-separate-pools", SimpleValueSupport.wrap(Boolean.TRUE));
      propValues.put("statistics-formatter", SimpleValueSupport.wrap("org.jboss.resource.statistic.pool.JBossDefaultSubPoolStatisticFormatter"));
      propValues.put("isSameRM-override-value", SimpleValueSupport.wrap(Boolean.FALSE));
      propValues.put("type-mapping", SimpleValueSupport.wrap("Hypersonic SQL"));
      // todo: config-property
      // todo: security-domain
      // todo: depends
      // todo: metadata
      // todo: local-transaction
   }

   protected void createComponentTest(String templateName,
                                    Map<String, MetaValue> propValues,
                                    String deploymentName,
                                    ComponentType componentType, String componentName)
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      DeploymentTemplateInfo dsInfo = mgtView.getTemplate(templateName);
      assertNotNull("template " + templateName + " found", dsInfo);
      Map<String, ManagedProperty> props = dsInfo.getProperties();

      for(String propName : propValues.keySet())
      {
         ManagedProperty prop = props.get(propName);
         // If the property does not exist on the template we don't set it
         if(prop == null)
            continue;
         
         log.debug("template property before: "+prop.getName()+","+prop.getValue());
         assertNotNull("property " + propName + " found in template " + templateName, prop);
         prop.setValue(propValues.get(propName));
         log.debug("template property after: "+prop.getName()+","+prop.getValue());
      }
      
      // Assert map composite
      if(dsInfo.getProperties().get("config-property") != null)
         assertTrue(dsInfo.getProperties().get("config-property").getMetaType() instanceof MapCompositeMetaType);
      
      mgtView.applyTemplate(deploymentName, dsInfo);

      // reload the view
      activeView = null;
      mgtView = getManagementView();
      ManagedComponent dsMC = getManagedComponent(mgtView, componentType, componentName);
      assertNotNull(dsMC);

      Set<String> mcPropNames = new HashSet<String>(dsMC.getPropertyNames());
      for(String propName : propValues.keySet())
      {
         ManagedProperty prop = dsMC.getProperty(propName);
         log.debug("Checking: "+propName);
         assertNotNull(propName, prop);
         Object propValue = prop.getValue();
         Object expectedValue = propValues.get(propName);
         if(propValue instanceof MetaValue)
         {
            if (prop.getMetaType().isComposite())
            {
               // TODO / FIXME - compare composites
               log.warn("Not checking composite: "+propValue);
            }
            else
            {
               // Compare the MetaValues
               assertEquals(prop.getName(), expectedValue, propValue);
            }
         }
         else if(propValue != null)
         {
            fail(prop.getName()+" is not a MetaValue: "+propValue);
         }

         mcPropNames.remove(propName);
      }

      if(!mcPropNames.isEmpty())
      {
         log.warn(getName() + "> untested properties: " + mcPropNames);
         for(String propName : mcPropNames)
         {
            ManagedProperty prop = dsMC.getProperty(propName);
            log.info(prop);
         }
      }
   }

   public MapCompositeValueSupport compositeValueMap(Map<String,String> map)
   {
      // TODO: update MetaValueFactory for MapCompositeMetaType
      // MetaValue metaValue = getMetaValueFactory().create(xaPropValues, getMapType());
      MapCompositeValueSupport metaValue = new MapCompositeValueSupport(SimpleMetaType.STRING);
      for(String key : map.keySet())
      {
         MetaValue value = SimpleValueSupport.wrap(map.get(key));
         metaValue.put(key, value);
      }

      return metaValue;
   }

}
