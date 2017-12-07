/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.tests.jca.managed;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.ObjectName;

import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.DeploymentTemplateInfoFactory;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.EnumMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.values.EnumValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.deployers.management.LocalDSInstanceClassFactory;
import org.jboss.resource.deployers.management.NoTxCFInstanceClassFactory;
import org.jboss.resource.deployers.management.TxInstanceClassFactory;
import org.jboss.resource.deployers.management.XADSInstanceClassFactory;
import org.jboss.resource.metadata.mcf.ApplicationManagedSecurityMetaData;
import org.jboss.resource.metadata.mcf.DataSourceConnectionPropertyMetaData;
import org.jboss.resource.metadata.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.SecurityDeploymentType;
import org.jboss.resource.metadata.mcf.SecurityDomainApplicationManagedMetaData;
import org.jboss.resource.metadata.mcf.SecurityMetaData;
import org.jboss.resource.metadata.mcf.TxConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.XAConnectionPropertyMetaData;
import org.jboss.resource.metadata.mcf.XADataSourceDeploymentMetaData;
import org.jboss.system.deployers.managed.ServiceMetaDataICF;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.BaseTestCase;

/**
 * Tests that validate that the expected ManagedObjects are created from
 * the jca metadata.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90300 $
 */
public class ManagedObjectTestCase extends BaseTestCase
{
   static final String[] ManagedConnectionFactoryDeploymentTemplate_NAMES = {
      "jndi-name",
      "use-java-context",
      "jmx-invoker-name",
      "rar-name",
      "connection-definition",
      "min-pool-size",
      "max-pool-size",
      "blocking-timeout-millis",
      "idle-timeout-minutes",
      "prefill",
      "background-validation",
      "background-validation-millis",
      "validate-on-match",
      "use-strict-min",
      "statistics-formatter",
      "isSameRM-override-value",
      "track-connection-by-tx",
      "security-domain",
      "type-mapping",
      "metadata",
      "no-tx-separate-pools",
      "allocation-retry",
      "allocation-retry-wait-millis"
   };
   static final String[] ManagedConnectionFactoryDeploymentMetaData_NAMES = {
      "jndi-name",
      "rar-name",
      "use-java-context",
      "connection-definition",
      "jmx-invoker-name",
      "min-pool-size",
      "max-pool-size",
      "blocking-timeout-millis",
      "idle-timeout-minutes",
      "prefill",
      "background-validation",
      "background-validation-millis",
      "validate-on-match",
      "use-strict-min",
      "statistics-formatter",
      "isSameRM-override-value",
      "track-connection-by-tx",
      "config-property",
      "security-domain",
      "type-mapping",
      "local-transaction",
      "metadata",
      "no-tx-separate-pools",
      "interleaving",
      "allocation-retry",
      "allocation-retry-wait-millis"
   };

   static final String[] DataSourceDeploymentMetaData_NAMES = {
      "transaction-isolation",
      "user-name",
      "password",
      "new-connection-sql",
      "check-valid-connection-sql",
      "valid-connection-checker-class-name",
      "exception-sorter-class-name",
      "track-statements",
      "prepared-statement-cache-size",
      "share-prepared-statements",
      "set-tx-query-timeout",
      "query-timeout",
      "stale-connection-checker-class-name",
      "url-delimiter",
      "url-selector-strategy-class-name",
      "use-try-lock"
   };
   static final String[] DataSourceDeploymentTemplate_NAMES = DataSourceDeploymentMetaData_NAMES;

   static final String[] NonXADataSourceDeploymentMetaData_NAMES = {
      "driver-class",
      "connection-properties",
      "connection-url"
   };
   static final String[] NonXADataSourceDeploymentTemplate_NAMES = NonXADataSourceDeploymentMetaData_NAMES;

   static final String[] XADataSourceDeploymentMetaData_NAMES = {
      "url-property",
      "xa-datasource-properties",
      "xa-datasource-class",
      "xa-resource-timeout"
   };
   static final String[] XADataSourceDeploymentTemplate_NAMES = XADataSourceDeploymentMetaData_NAMES;
   
   static final String[] TxConnectionFactoryDeploymentMetaData_NAMES = {
      "xa-resource-timeout",
      "xa-transaction",
      "local-transaction"
   };
   static final String[] TxConnectionFactoryDeploymentTemplate_NAMES = TxConnectionFactoryDeploymentMetaData_NAMES;
   
   static final String[] JBossManagedConnectionPool_NAMES = {
      "availableConnectionCount",
      "connectionCount",
      "connectionCreatedCount",
      "connectionDestroyedCount",
      "inUseConnectionCount",
      "maxConnectionsInUseCount",
      "maxSize",
      "minSize",
      "poolJndiName"
   };

   public ManagedObjectTestCase(String name)
   {
      super(name);
   }
   
   public void testNoTxDataSourceDeploymentMetaData()
   {
      enableTrace("org.jboss.managed.plugins.factory");
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      mof.addInstanceClassFactory(new NoTxCFInstanceClassFactory());
      ManagedObject mo = mof.createManagedObject(NoTxDataSourceDeploymentMetaData.class);
      log.info(mo.getProperties());
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(ManagedConnectionFactoryDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(DataSourceDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(NonXADataSourceDeploymentMetaData_NAMES));
      Set<String> propertyNames = mo.getPropertyNames();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
   }
   public void testTxConnectionFactoryMetaData()
   {
      enableTrace("org.jboss.managed.plugins.factory");
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      TxInstanceClassFactory icf = new TxInstanceClassFactory();
      mof.setInstanceClassFactory(TxConnectionFactoryDeploymentMetaData.class, icf);
      TxConnectionFactoryDeploymentMetaData txcf = new TxConnectionFactoryDeploymentMetaData();
      ManagedObject mo = mof.initManagedObject(txcf, "TxConnectionFactoryDeploymentMetaData", null);
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(ManagedConnectionFactoryDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(TxConnectionFactoryDeploymentMetaData_NAMES));
      Set<String> propertyNames = mo.getPropertyNames();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
      ManagedProperty connectionProperties = mo.getProperty("config-property");
      MetaType cpType = connectionProperties.getMetaType();
      assertTrue("connection-properties.type="+cpType, cpType instanceof CompositeMetaType);
      Object cpValue = connectionProperties.getValue();
      assertTrue("connection-properties.value="+cpValue, cpValue instanceof CompositeValue);
      CompositeValue cvalue = (CompositeValue) cpValue;
      // Now update the values
      MapCompositeValueSupport map = (MapCompositeValueSupport) cvalue;

      // Test simple property types
      ManagedProperty xatx = mo.getProperty("xa-transaction");
      xatx.setValue(SimpleValueSupport.wrap(true));
      assertEquals(Boolean.TRUE, txcf.getXATransaction());
      ManagedProperty xart = mo.getProperty("xa-resource-timeout");
      xart.setValue(SimpleValueSupport.wrap(12345));
      assertEquals(12345, txcf.getXaResourceTimeout());
   }
   public void testLocalDataSourceDeploymentMetaData()
   {
      enableTrace("org.jboss.managed.plugins.factory");
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      LocalDSInstanceClassFactory icf = new LocalDSInstanceClassFactory(mof);
      icf.setMof(mof);
      mof.setInstanceClassFactory(LocalDataSourceDeploymentMetaData.class, icf);
      LocalDataSourceDeploymentMetaData lds = new LocalDataSourceDeploymentMetaData();
      // Set a SecurityMetaData to validate its MO
      SecurityMetaData smd = new ApplicationManagedSecurityMetaData();
      smd.setDomain("java:/jaas/SomeDomain");
      lds.setSecurityMetaData(smd);
      ManagedObject mo = mof.initManagedObject(lds, "LocalDataSourceDeploymentMetaData", null);
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(ManagedConnectionFactoryDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(DataSourceDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(NonXADataSourceDeploymentMetaData_NAMES));
      Set<String> propertyNames = mo.getPropertyNames();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
      // interleaving
      ManagedProperty interleaving = mo.getProperty("interleaving");
      assertNotNull(interleaving);
      MetaType interleavingType = interleaving.getMetaType();
      assertEquals("interleaving.type", SimpleMetaType.BOOLEAN, interleavingType);

      // Validate the connection-properties type
      ManagedProperty connectionProperties = mo.getProperty("connection-properties");
      MetaType cpType = connectionProperties.getMetaType();
      assertTrue("connection-properties.type="+cpType, cpType instanceof CompositeMetaType);
      Object cpValue = connectionProperties.getValue();
      assertTrue("connection-properties.value="+cpValue, cpValue instanceof CompositeValue);
      CompositeValue cvalue = (CompositeValue) cpValue;
      // Now update the values
      MapCompositeValueSupport map = (MapCompositeValueSupport) cvalue;
      map.put("key1", SimpleValueSupport.wrap("value1"));
      map.put("key2", SimpleValueSupport.wrap("value2"));
      connectionProperties.setValue(map);
      // Check the underlying values
      List<DataSourceConnectionPropertyMetaData> ldsProps = lds.getDataSourceConnectionProperties();
      assertEquals(2, ldsProps.size());
      DataSourceConnectionPropertyMetaData key1 = null;
      DataSourceConnectionPropertyMetaData key2 = null;
      for(DataSourceConnectionPropertyMetaData dspmd : ldsProps)
      {
         if(dspmd.getName().equals("key1"))
            key1 = dspmd;
         else if(dspmd.getName().equals("key2"))
            key2 = dspmd;
      }
      assertNotNull(key1);
      assertEquals("value1", key1.getValue());
      assertNotNull(key2);
      assertEquals("value2", key2.getValue());
      // Test a simple property
      ManagedProperty driverClass = mo.getProperty("driver-class");
      driverClass.setValue(SimpleValueSupport.wrap("org.jboss.jdbc.ClusteredDriver"));
      String driverClassName = lds.getDriverClass();
      assertEquals("org.jboss.jdbc.ClusteredDriver", driverClassName);
      // Validate the security-domain
      ManagedProperty secDomain = mo.getProperty("security-domain");
      assertNotNull("security-domain", secDomain);
      CompositeMetaType compType = (CompositeMetaType) secDomain.getMetaType();
      assertNotNull(compType);
      CompositeValue sdCV = (CompositeValue) secDomain.getValue();
      assertNotNull("security-domain.CV", sdCV);

      SimpleValue domainName = (SimpleValue) sdCV.get("domain");
      assertNotNull("security-domain.domain", domainName);
      assertEquals(SimpleValueSupport.wrap("java:/jaas/SomeDomain"), domainName);
      assertNotNull("security-domain.deploymentType", sdCV.get("securityDeploymentType"));
      assertEquals("APPLICATION", ((EnumValue) sdCV.get("securityDeploymentType")).getValue());
      assertFalse(lds.getSecurityMetaData() instanceof SecurityDomainApplicationManagedMetaData);
      
      // Set a new security domain and check if the metaType changed
      CompositeValueSupport newSecDomain = new CompositeValueSupport(compType);
      newSecDomain.set("domain", SimpleValueSupport.wrap("test"));
      newSecDomain.set("securityDeploymentType", new EnumValueSupport(
            (EnumMetaType) compType.getType("securityDeploymentType"),
            SecurityDeploymentType.DOMAIN_AND_APPLICATION));
      secDomain.setValue(newSecDomain);
      
      assertTrue(lds.getSecurityMetaData() instanceof SecurityDomainApplicationManagedMetaData);
   }
   
   public void testXADataSourceDeploymentMetaData()
   {
      enableTrace("org.jboss.managed.plugins.factory");
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      XADSInstanceClassFactory icf = new XADSInstanceClassFactory();
      mof.setInstanceClassFactory(XADataSourceDeploymentMetaData.class, icf);
      XADataSourceDeploymentMetaData xads = new XADataSourceDeploymentMetaData();
      ManagedObject mo = mof.initManagedObject(xads, "XADataSourceDeploymentMetaData", null);
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(ManagedConnectionFactoryDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(DataSourceDeploymentMetaData_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(XADataSourceDeploymentMetaData_NAMES));
      Set<String> propertyNames = mo.getPropertyNames();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
      // Validate the xa-datasource-properties type
      ManagedProperty connectionProperties = mo.getProperty("xa-datasource-properties");
      MetaType cpType = connectionProperties.getMetaType();
      assertTrue("xa-datasource-properties.type="+cpType, cpType instanceof CompositeMetaType);
      Object cpValue = connectionProperties.getValue();
      assertTrue("xa-datasource-properties.value="+cpValue, cpValue instanceof CompositeValue);
      CompositeValue cvalue = (CompositeValue) cpValue;
      // Now update the values
      MapCompositeValueSupport map = (MapCompositeValueSupport) cvalue;
      map.put("key1", SimpleValueSupport.wrap("value1"));
      map.put("key2", SimpleValueSupport.wrap("value2"));
      connectionProperties.setValue(map);
      // Check the underlying values
      List<XAConnectionPropertyMetaData> xaProps = xads.getXADataSourceProperties();
      assertEquals(2, xaProps.size());
      DataSourceConnectionPropertyMetaData key1 = null;
      DataSourceConnectionPropertyMetaData key2 = null;
      for(XAConnectionPropertyMetaData dspmd : xaProps)
      {
         if(dspmd.getName().equals("key1"))
            key1 = dspmd;
         else if(dspmd.getName().equals("key2"))
            key2 = dspmd;
      }
      assertNotNull(key1);
      assertEquals("value1", key1.getValue());
      assertNotNull(key2);
      assertEquals("value2", key2.getValue());
      // Test a simple property
      ManagedProperty jndiName = mo.getProperty("jndi-name");
      jndiName.setValue(SimpleValueSupport.wrap("java:ClusteredDS"));
      String jndiNameTest = xads.getJndiName();
      assertEquals("java:ClusteredDS", jndiNameTest);
   }
   /**
    * Validate the expected LocalDataSourceDeploymentMetaData template properties
    * @throws Exception
    */
   public void testXADataSourceTemplateProperties()
      throws Exception
   {
      DeploymentTemplateInfoFactory factory = new DeploymentTemplateInfoFactory();
      DeploymentTemplateInfo dsInfo = factory.createTemplateInfo(XADataSourceDeploymentMetaData.class, "TestDS", "test ds");
      Map<String,ManagedProperty> props = dsInfo.getProperties();
      
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(ManagedConnectionFactoryDeploymentTemplate_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(DataSourceDeploymentTemplate_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(XADataSourceDeploymentTemplate_NAMES));

      Set<String> propertyNames = props.keySet();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
   }
   /**
    * Validate that the XATxDataSourceTemplate ManagedPropertys are values are of type MetaValue
    * @throws Exception
    */
   public void testXADataSourceTemplatePropertiesAreMetaValues()
      throws Exception
   {
      DeploymentTemplateInfoFactory factory = new DeploymentTemplateInfoFactory();
      DeploymentTemplateInfo dsInfo = factory.createTemplateInfo(XADataSourceDeploymentMetaData.class, "TestDS", "test ds");
      Map<String,ManagedProperty> props = dsInfo.getProperties();
      validatePropertyMetaValues(props);
   }
   /**
    * Validate the expected LocalDataSourceDeploymentMetaData template properties
    * @throws Exception
    */
   public void testLocalTxDataSourceTemplateProperties()
      throws Exception
   {
      DeploymentTemplateInfoFactory factory = new DeploymentTemplateInfoFactory();
      DeploymentTemplateInfo dsInfo = factory.createTemplateInfo(LocalDataSourceDeploymentMetaData.class, "TestDS", "test ds");
      Map<String,ManagedProperty> props = dsInfo.getProperties();
      
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(ManagedConnectionFactoryDeploymentTemplate_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(DataSourceDeploymentTemplate_NAMES));
      expectedPropertyNames.addAll(Arrays.asList(NonXADataSourceDeploymentTemplate_NAMES));
      
      Set<String> propertyNames = props.keySet();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
   }
   /**
    * Validate that the LocalTxDataSourceTemplate ManagedPropertys are values are of type MetaValue
    * @throws Exception
    */
   public void testLocalTxDataSourceTemplatePropertiesAreMetaValues()
      throws Exception
   {
      DeploymentTemplateInfoFactory factory = new DeploymentTemplateInfoFactory();
      DeploymentTemplateInfo dsInfo = factory.createTemplateInfo(LocalDataSourceDeploymentMetaData.class, "TestDS", "test ds");
      Map<String,ManagedProperty> props = dsInfo.getProperties();
      validatePropertyMetaValues(props);
   }

   public void testJBossManagedConnectionPool() throws Exception
   {
      enableTrace("org.jboss.managed.plugins.factory");
      ManagedObjectFactory mof = ManagedObjectFactory.getInstance();
      ServiceMetaDataICF icf = new ServiceMetaDataICF();
      mof.setInstanceClassFactory(ServiceMetaData.class, icf);
      ServiceMetaData smd = new ServiceMetaData();
      smd.setCode(JBossManagedConnectionPool.class.getName());
      smd.setObjectName(new ObjectName("jboss:service=Hypersonic,database=localDB"));
      ManagedObject mo = mof.initManagedObject(smd, null, null);
      // Validate the expected property names
      Set<String> expectedPropertyNames = new TreeSet<String>();
      expectedPropertyNames.addAll(Arrays.asList(JBossManagedConnectionPool_NAMES));
      Set<String> propertyNames = mo.getPropertyNames();
      TreeSet<String> sortedPropertyNames = new TreeSet<String>(propertyNames);
      if(expectedPropertyNames.equals(sortedPropertyNames) == false)
      {
         Set<String> missingNames = new TreeSet<String>();
         Set<String> extraNames = new TreeSet<String>();
         analyzeDiffs(expectedPropertyNames, sortedPropertyNames,
               missingNames, extraNames);
         fail("Extra properties: "+extraNames+", missing properties: "+missingNames);
      }
   }

   /**
    * Build the missingNames, extraNames from the input expectedPropertyNames
    * and sortedPropertyNames.
    * 
    * @param expectedPropertyNames
    * @param sortedPropertyNames
    * @param missingNames
    * @param extraNames
    */
   private void analyzeDiffs(Set<String> expectedPropertyNames,
         TreeSet<String> sortedPropertyNames,
         Set<String> missingNames, Set<String> extraNames)
   {
      // Build the list of extra names
      for(String name : sortedPropertyNames)
      {
         if(expectedPropertyNames.contains(name) == false)
            extraNames.add(name);
      }
      // Build the list of missing names
      for(String name : expectedPropertyNames)
      {
         if(sortedPropertyNames.contains(name) == false)
            missingNames.add(name);
      }
   }

   protected void validatePropertyMetaValues(Map<String, ManagedProperty> props)
   {
      HashMap<String, Object> invalidValues = new HashMap<String, Object>();
      HashMap<String, Object> nullValues = new HashMap<String, Object>();
      for(ManagedProperty prop : props.values())
      {
         Object value = prop.getValue();
         if((value instanceof MetaValue) == false)
         {
            if(value == null)
               nullValues.put(prop.getName(), value);
            else
               invalidValues.put(prop.getName(), value);
         }
      }
      log.info("Propertys with null values: "+nullValues);
      assertEquals("InvalidPropertys: "+invalidValues, 0, invalidValues.size());
   }
}
