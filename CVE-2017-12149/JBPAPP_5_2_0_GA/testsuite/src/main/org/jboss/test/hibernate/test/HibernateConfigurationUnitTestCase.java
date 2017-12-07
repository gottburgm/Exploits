/**
 * 
 */
package org.jboss.test.hibernate.test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.hibernate.cache.HashtableCacheProvider;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.transaction.JTATransactionFactory;
import org.jboss.hibernate.deployers.metadata.BaseNamedElement;
import org.jboss.hibernate.jmx.Hibernate;
import org.jboss.test.hibernate.mocks.MockCacheProvider;
import org.jboss.test.hibernate.mocks.MockInterceptor;
import org.jboss.test.hibernate.mocks.MockListenerInjector;
import org.jboss.test.hibernate.mocks.MockRegionFactory;
import org.jboss.test.hibernate.mocks.TransactionManagerLookupImpl;

/**
 * A HibernateConfigurationUnitTestCase.
 * 
 * @author Brian Stansberry
 * @version $Revision: 1.1 $
 */
public class HibernateConfigurationUnitTestCase extends TestCase
{
   private static long testCount = System.currentTimeMillis();
   
   private File tempFile = null;
   
   private Hibernate testee;
   
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      tempFile = File.createTempFile(getClass().getSimpleName() + (testCount++), null);
      tempFile.mkdirs();
   }

   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         try
         {
            super.tearDown();
         }
         finally
         {
            if (testee != null)
            {
               testee.stop();
            }
         }
         
      }
      finally
      {
         if (tempFile != null && tempFile.exists())
         {
            if (!tempFile.delete())
            {
               tempFile.deleteOnExit();
            }
         }
      }
   }
   
   public void testDefaults() throws Throwable
   {
      testee = new TestableHibernate();

      testee.setHarUrl(tempFile.toURI().toURL());
      testee.setDialect(HSQLDialect.class.getName());
      Set<BaseNamedElement> config = new HashSet<BaseNamedElement>();
      config.add(createBaseNamedElement(Environment.TRANSACTION_MANAGER_STRATEGY, TransactionManagerLookupImpl.class.getName()));
      config.add(createBaseNamedElement(Environment.USER_TRANSACTION, JTATransactionFactory.DEFAULT_USER_TRANSACTION_NAME));
      testee.setConfigurationElements(config);
      testee.start();
      
      SessionFactory factory = testee.getInstance();
      assertTrue(factory instanceof SessionFactoryImplementor);
      Settings settings = ((SessionFactoryImplementor) factory).getSettings();
      assertTrue(settings.getTransactionFactory() instanceof JTATransactionFactory);
      assertTrue(settings.isSecondLevelCacheEnabled());
      
      assertTrue(settings.isFlushBeforeCompletionEnabled());
      assertTrue(settings.isAutoCloseSessionEnabled());
      
      Properties props = testee.getConfigurationProperties();
      assertEquals(HashtableCacheProvider.class.getName(), props.getProperty(Environment.CACHE_PROVIDER));
      assertEquals("after_statement", props.getProperty("hibernate.connection.release_mode"));
      // Disabled, as use of JTA causes hibernate to ignore hibernate.connection.release_mode=after_statement
      //assertEquals(ConnectionReleaseMode.AFTER_STATEMENT, settings.getConnectionReleaseMode());
   }

   public void testMBeanConfiguration() throws Throwable
   {
      testee = new TestableHibernate();
      
      // Non-Hibernate configs
      testee.setHarUrl(tempFile.toURI().toURL());
      testee.setScanForMappingsEnabled(true);
      testee.setSessionFactoryInterceptor(MockInterceptor.class.getName());
      testee.setListenerInjector(MockListenerInjector.class.getName());
      
      testee.setDialect(HSQLDialect.class.getName());
//      testee.setDatasourceName("TestDS");
      testee.setCacheProviderClass(MockCacheProvider.class.getName());
      testee.setCacheRegionFactoryClass(MockRegionFactory.class.getName());
      testee.setCacheRegionPrefix("aprefix");
      testee.setMinimalPutsEnabled(true);
      testee.setHbm2ddlAuto("bogus");
      testee.setSecondLevelCacheEnabled(Boolean.TRUE);
      testee.setQueryCacheEnabled(Boolean.TRUE);
      testee.setUseStructuredCacheEntriesEnabled(Boolean.TRUE);
      testee.setDefaultSchema("schema");
      testee.setDefaultCatalog("catalog");
      testee.setJdbcBatchSize(Integer.valueOf(8));
      testee.setJdbcFetchSize(Integer.valueOf(9));
      testee.setBatchVersionedDataEnabled(Boolean.TRUE);
      testee.setMaxFetchDepth(Integer.valueOf(13));
      testee.setJdbcScrollableResultSetEnabled(Boolean.TRUE);
      testee.setSqlCommentsEnabled(Boolean.TRUE);
      testee.setQuerySubstitutions("key=value");
      testee.setShowSqlEnabled(Boolean.TRUE);
      testee.setGetGeneratedKeysEnabled(Boolean.TRUE);
      testee.setStatGenerationEnabled(Boolean.TRUE);
      testee.setUsername("Brian");
      testee.setPassword("Brian");
      testee.setStreamsForBinaryEnabled(Boolean.TRUE);
      testee.setReflectionOptimizationEnabled(Boolean.TRUE);
      testee.setDeployedCacheJndiName("java:/test/Cache");
      testee.setDeployedCacheManagerJndiName("java:/test/CacheManager");
      
      Set<BaseNamedElement> config = new HashSet<BaseNamedElement>();
      config.add(createBaseNamedElement(Environment.TRANSACTION_MANAGER_STRATEGY, TransactionManagerLookupImpl.class.getName()));
      config.add(createBaseNamedElement(Environment.USER_TRANSACTION, JTATransactionFactory.DEFAULT_USER_TRANSACTION_NAME));
      config.add(createBaseNamedElement(org.hibernate.cache.jbc2.builder.MultiplexingCacheInstanceManager.ENTITY_CACHE_RESOURCE_PROP, "entities-test"));
      testee.setConfigurationElements(config);
      
      testee.start();
      validate(testee);
   }
   
   public void testHibernatePropertyConfiguration() throws Throwable
   {
      testee = new TestableHibernate();
      
      Set<BaseNamedElement> config = new HashSet<BaseNamedElement>();
      
      config.add(createBaseNamedElement("harUrl", tempFile.toURI().toURL()));
      config.add(createBaseNamedElement("scanForMappingsEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("sessionFactoryInterceptor", MockInterceptor.class.getName()));
      config.add(createBaseNamedElement("listenerInjector", MockListenerInjector.class.getName()));
      
      config.add(createBaseNamedElement(Environment.DIALECT, HSQLDialect.class.getName()));
      config.add(createBaseNamedElement(Environment.TRANSACTION_MANAGER_STRATEGY, TransactionManagerLookupImpl.class.getName()));
      config.add(createBaseNamedElement(Environment.USER_TRANSACTION, JTATransactionFactory.DEFAULT_USER_TRANSACTION_NAME));
      
//      config.add(createBaseNamedElement(Environment.DATASOURCE, "TestDS"));
      config.add(createBaseNamedElement(Environment.CACHE_PROVIDER, MockCacheProvider.class.getName()));
      config.add(createBaseNamedElement(Environment.CACHE_REGION_FACTORY, MockRegionFactory.class.getName()));
      config.add(createBaseNamedElement(Environment.CACHE_REGION_PREFIX, "aprefix"));
      config.add(createBaseNamedElement(Environment.USE_MINIMAL_PUTS, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.HBM2DDL_AUTO, "bogus"));
      config.add(createBaseNamedElement(Environment.DEFAULT_SCHEMA, "schema"));
      config.add(createBaseNamedElement(Environment.DEFAULT_CATALOG, "catalog"));
      config.add(createBaseNamedElement(Environment.USE_SECOND_LEVEL_CACHE, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.USE_QUERY_CACHE, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.USE_STRUCTURED_CACHE, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.STATEMENT_BATCH_SIZE, Integer.valueOf(8)));
      config.add(createBaseNamedElement(Environment.STATEMENT_FETCH_SIZE, Integer.valueOf(9)));
      config.add(createBaseNamedElement(Environment.BATCH_VERSIONED_DATA, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.MAX_FETCH_DEPTH, Integer.valueOf(13)));
      config.add(createBaseNamedElement(Environment.USE_SCROLLABLE_RESULTSET, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.USE_SQL_COMMENTS, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.QUERY_SUBSTITUTIONS, "key=value"));
      config.add(createBaseNamedElement(Environment.SHOW_SQL, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.USE_GET_GENERATED_KEYS, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.GENERATE_STATISTICS, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.USER, "Brian"));
      config.add(createBaseNamedElement(Environment.PASS, "Brian"));
      config.add(createBaseNamedElement(Environment.USE_STREAMS_FOR_BINARY, Boolean.TRUE));
      config.add(createBaseNamedElement(Environment.USE_REFLECTION_OPTIMIZER, Boolean.TRUE));
      config.add(createBaseNamedElement(org.hibernate.cache.jbc2.builder.JndiSharedCacheInstanceManager.CACHE_RESOURCE_PROP, "java:/test/Cache"));
      config.add(createBaseNamedElement(org.hibernate.cache.jbc2.builder.JndiMultiplexingCacheInstanceManager.CACHE_FACTORY_RESOURCE_PROP, "java:/test/CacheManager"));
      config.add(createBaseNamedElement(org.hibernate.cache.jbc2.builder.MultiplexingCacheInstanceManager.ENTITY_CACHE_RESOURCE_PROP, "entities-test"));
      
      testee.setConfigurationElements(config);
      testee.start();
      validate(testee); 
   }
   
   public void testLegacyPropertyNames() throws Throwable
   {
      testee = new TestableHibernate();
      
      Set<BaseNamedElement> config = new HashSet<BaseNamedElement>();
      
      config.add(createBaseNamedElement("harUrl", tempFile.toURI().toURL()));
      config.add(createBaseNamedElement("scanForMappingsEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("sessionFactoryInterceptor", MockInterceptor.class.getName()));
      config.add(createBaseNamedElement("listenerInjector", MockListenerInjector.class.getName()));
      
      config.add(createBaseNamedElement("dialect", HSQLDialect.class.getName()));
      
      config.add(createBaseNamedElement(Environment.TRANSACTION_MANAGER_STRATEGY, TransactionManagerLookupImpl.class.getName()));
      config.add(createBaseNamedElement(Environment.USER_TRANSACTION, JTATransactionFactory.DEFAULT_USER_TRANSACTION_NAME));
      
//      config.add(createBaseNamedElement("datasourceName", "TestDS"));
      config.add(createBaseNamedElement("cacheProviderClass", MockCacheProvider.class.getName()));
      config.add(createBaseNamedElement("cacheRegionFactoryClass", MockRegionFactory.class.getName()));
      config.add(createBaseNamedElement("cacheRegionPrefix", "aprefix"));
      config.add(createBaseNamedElement("minimalPutsEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("hbm2ddlAuto", "bogus"));
      config.add(createBaseNamedElement("defaultSchema", "schema"));
      config.add(createBaseNamedElement("defaultCatalog", "catalog"));
      config.add(createBaseNamedElement("secondLevelCacheEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("queryCacheEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("useStructuredCacheEntriesEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("jdbcBatchSize", Integer.valueOf(8)));
      config.add(createBaseNamedElement("jdbcFetchSize", Integer.valueOf(9)));
      config.add(createBaseNamedElement("batchVersionedDataEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("maxFetchDepth", Integer.valueOf(13)));
      config.add(createBaseNamedElement("jdbcScrollableResultSetEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("sqlCommentsEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("querySubstitutions", "key=value"));
      config.add(createBaseNamedElement("showSqlEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("getGeneratedKeysEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("statGenerationEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("username", "Brian"));
      config.add(createBaseNamedElement("password", "Brian"));
      config.add(createBaseNamedElement("streamsForBinaryEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("reflectionOptimizationEnabled", Boolean.TRUE));
      config.add(createBaseNamedElement("deployedCacheJndiName", "java:/test/Cache"));
      config.add(createBaseNamedElement("deployedCacheManagerJndiName", "java:/test/CacheManager"));
      config.add(createBaseNamedElement(org.hibernate.cache.jbc2.builder.MultiplexingCacheInstanceManager.ENTITY_CACHE_RESOURCE_PROP, "entities-test"));
      
      testee.setConfigurationElements(config);
      testee.start();
      validate(testee);
      
   }
   
   private void validate(Hibernate testee) throws Exception
   {
      // Validate getter values on Hibernate object
      assertEquals(TestableHibernate.class.getSimpleName() + testCount, testee.getSessionFactoryName());
      assertEquals(tempFile.toURI().toURL(), testee.getHarUrl());
      assertTrue(testee.isScanForMappingsEnabled());
      assertEquals(MockInterceptor.class.getName(), testee.getSessionFactoryInterceptor());
      assertEquals(MockListenerInjector.class.getName(), testee.getListenerInjector());
      
      // Where we can, validate stuff exposed by hibernate Settings
      SessionFactory factory = testee.getInstance();
      assertTrue(factory instanceof SessionFactoryImplementor);
      Settings settings = ((SessionFactoryImplementor) factory).getSettings();
      
//      assertEquals(TestableHibernate.class.getSimpleName() + testCount, settings.getSessionFactoryName());
      assertEquals(HSQLDialect.class.getName(), testee.getDialect());
      assertTrue(settings.getDialect() instanceof HSQLDialect);
      assertTrue(settings.getTransactionFactory() instanceof JTATransactionFactory);
      assertTrue(settings.getTransactionManagerLookup() instanceof TransactionManagerLookupImpl);
      assertTrue(settings.getRegionFactory() instanceof MockRegionFactory);
      assertEquals("aprefix", settings.getCacheRegionPrefix());
      assertTrue(settings.isMinimalPutsEnabled());
      assertTrue(settings.isSecondLevelCacheEnabled());
      assertTrue(settings.isQueryCacheEnabled());
      assertTrue(settings.isStructuredCacheEntriesEnabled());
      assertEquals("schema", settings.getDefaultSchemaName());
      assertEquals("catalog", settings.getDefaultCatalogName());
      assertEquals(Integer.valueOf(9), settings.getJdbcFetchSize());
      assertTrue(settings.isJdbcBatchVersionedData());
      assertEquals(Integer.valueOf(13), settings.getMaximumFetchDepth());
      assertTrue(settings.isScrollableResultSetsEnabled());
      assertTrue(settings.isCommentsEnabled());
      @SuppressWarnings("unchecked")
      Map substitutions = settings.getQuerySubstitutions();
      assertNotNull(substitutions);
      assertEquals(1, substitutions.size());
      assertEquals("value", substitutions.get("key"));
      assertTrue(settings.getSqlStatementLogger().isLogToStdout());
      assertTrue(settings.isGetGeneratedKeysEnabled());
      assertTrue(settings.isStatisticsEnabled());
      
      // For stuff not available via Settings, second best is to check the properties exposed by Hibernate object
      Properties props = testee.getConfigurationProperties();
      assertEquals(JTATransactionFactory.DEFAULT_USER_TRANSACTION_NAME, props.getProperty(Environment.USER_TRANSACTION));
      assertEquals(MockCacheProvider.class.getName(), props.getProperty(Environment.CACHE_PROVIDER));
      assertEquals("bogus", props.getProperty(Environment.HBM2DDL_AUTO));
      assertEquals("8", props.getProperty(Environment.STATEMENT_BATCH_SIZE));
      assertEquals("Brian", props.getProperty(Environment.USER));
      assertEquals("Brian", props.getProperty(Environment.PASS));
      assertEquals("true", props.getProperty(Environment.USE_STREAMS_FOR_BINARY));
      assertEquals("true", props.getProperty(Environment.USE_REFLECTION_OPTIMIZER));
      assertEquals("java:/test/Cache", props.getProperty(org.hibernate.cache.jbc2.builder.JndiSharedCacheInstanceManager.CACHE_RESOURCE_PROP));
      assertEquals("java:/test/CacheManager", props.getProperty(org.hibernate.cache.jbc2.builder.JndiMultiplexingCacheInstanceManager.CACHE_FACTORY_RESOURCE_PROP));
      assertEquals("entities-test", props.getProperty(org.hibernate.cache.jbc2.builder.MultiplexingCacheInstanceManager.ENTITY_CACHE_RESOURCE_PROP));
   }
   
   private static BaseNamedElement createBaseNamedElement(String name, Object value)
   {
      BaseNamedElement element = new BaseNamedElement();
      element.setName(name);
      element.setValue(value);
      return element;
   }
   
   private static class TestableHibernate extends Hibernate
   {
      public TestableHibernate()
      {
         super(null, false);
         
         setSessionFactoryName(getClass().getSimpleName() + testCount);
      }
   }
}
