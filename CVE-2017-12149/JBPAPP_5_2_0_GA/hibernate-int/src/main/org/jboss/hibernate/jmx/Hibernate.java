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
package org.jboss.hibernate.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.jmx.StatisticsService;
import org.hibernate.jmx.StatisticsServiceMBean;
import org.hibernate.transaction.JBossTransactionManagerLookup;
import org.hibernate.transaction.JTATransactionFactory;
import org.hibernate.Version;
import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.api.model.FromContext;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.hibernate.ListenerInjector;
import org.jboss.hibernate.deployers.metadata.BaseNamedElement;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * The {@link HibernateMBean} implementation.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:gavin@hibernate.org">Gavin King</a>
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:pferraro@redhat.com">Paul Ferraro</a>
 * @version <tt>$Revision: 104585 $</tt>
 */
public class Hibernate implements HibernateMBean
{
   private static final Logger log = Logger.getLogger( Hibernate.class );

   public static final String SESSION_FACTORY_CREATE = "hibernate.sessionfactory.create";
   public static final String SESSION_FACTORY_DESTROY = "hibernate.sessionfactory.destroy";
   
   // Legacy mbean/MC bean configuration attributes "passed through" to Hibernate
   private static final String DATASOURCE_NAME = "datasourceName";
   private static final String DIALECT = "dialect";
   private static final String DEFAULT_SCHEMA = "defaultSchema";
   private static final String DEFAULT_CATALOG = "defaultCatalog";
   private static final String SQL_COMMENTS_ENABLED = "sqlCommentsEnabled";
   private static final String  MAX_FETCH_DEPTH = "maxFetchDepth";
   private static final String JDBC_FETCH_SIZE = "jdbcFetchSize";
   private static final String JDBC_BATCH_SIZE = "jdbcBatchSize";
   private static final String BATCH_VERSIONED_DATA_ENABLED = "batchVersionedDataEnabled";
   private static final String JDBC_SCROLLABLE_RESULT_SET_ENABLED = "jdbcScrollableResultSetEnabled";
   private static final String GET_GENERATED_KEYS_ENABLED = "getGeneratedKeysEnabled";
   private static final String STREAMS_FOR_BINARY_ENABLED = "streamsForBinaryEnabled";
   private static final String HBM2DDL_AUTO = "hbm2ddlAuto";
   private static final String QUERY_SUBSTITUTIONS = "querySubstitutions";
   private static final String SHOW_SQL_ENABLED = "showSqlEnabled";
   private static final String USERNAME = "username";
   private static final String PASSWORD = "password";
   private static final String SECOND_LEVEL_CACHE_ENABLED = "secondLevelCacheEnabled";
   private static final String QUERY_CACHE_ENABLED = "queryCacheEnabled";
   private static final String CACHE_PROVIDER_CLASS = "cacheProviderClass";
   private static final String CACHE_REGION_FACTORY_CLASS = "cacheRegionFactoryClass";
   private static final String DEPLOYED_CACHE_JNDI_NAME = "deployedCacheJndiName";
   private static final String DEPLOYED_CACHE_MANAGER_JNDI_NAME = "deployedCacheManagerJndiName";
   private static final String MINIMAL_PUTS_ENABLED = "minimalPutsEnabled";
   private static final String CACHE_REGION_PREFIX = "cacheRegionPrefix";
   private static final String STRUCTURED_CACHE_ENTRIES_ENABLED = "useStructuredCacheEntriesEnabled";
   private static final String STAT_GENERATION_ENABLED = "statGenerationEnabled";
   private static final String REFLECTION_OPTIMIZATION_ENABLED = "reflectionOptimizationEnabled";

   // Configuration attributes used by the MBean
   private String sessionFactoryName;
   private static final String SESSION_FACTORY_INTERCEPTOR = "sessionFactoryInterceptor";
   private String sessionFactoryInterceptor;
   private static final String LISTENER_INJECTOR = "listenerInjector";
   private String listenerInjector;
   private static final String HAR_URL = "harUrl";
   private URL harUrl;
   private static final String SCAN_FOR_MAPPINGS_ENABLED = "scanForMappingsEnabled";
   private boolean scanForMappingsEnabled = false;

   // Internal state
   
   // Storage of injected configuration values
   private final Map<String, Object> configurationElements = Collections.synchronizedMap(new HashMap<String, Object>());
   // The actual properties stored in hibernate Configuration object when SessionFactory was built
   private Properties sessionFactoryProperties;
   private VirtualFile root;
   private boolean dirty = false;
   private Date runningSince;
   private SessionFactory sessionFactory;
   private String hibernateStatisticsServiceName;
   
   // For unit testing
   private boolean bindInJndi = true;

   /**
    * Backward compatible constructor. Usage requires configuring a 
    * {@link #setHarUrl(URL) HAR URL} before calling {@link #start()}.
    */
   public Hibernate()
   {
   }
   
   /**
    * Create a new Hibernate instance.
    * 
    * @param root root file for the deployment. Cannot be <code>null</code>
    * 
    * @throws IllegalArgumentException if <code>root</code> is <code>null</code>
    */
   public Hibernate(VirtualFile root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root file");
      this.root = root;
   }

   /**
    * For use in unit testing
    * 
    * @param root root of the deployment. May be <code>null</code>, in which
    *             case configuring a {@link #setHarUrl(URL) HAR URL} before 
    *             calling {@link #start()} is required
    * @param bindInJndi <code>true</code> if the SessionFactory should be bound
    *                   in JNDI (the default); <code>false</code> if not (to
    *                   ease basic unit testing)
    */
   protected Hibernate(VirtualFile root, boolean bindInJndi)
   {
      this.root = root;
      this.bindInJndi = bindInJndi;
   }

   // Injected from underlying MC
   private Object beanName;
   private KernelController controller;

   @Inject(fromContext = FromContext.NAME)
   public void setBeanName(Object beanName)
   {
      this.beanName = beanName;
   }

   @Inject(bean = KernelConstants.KERNEL_CONTROLLER_NAME)
   public void setController(KernelController controller)
   {
      this.controller = controller;
   }
   
   public Properties getConfigurationProperties()
   {
      Properties props = new Properties();
      if (sessionFactoryProperties == null)
      {
         for (Map.Entry<String, Object> coEntry : configurationElements.entrySet())
         {
            props.setProperty(coEntry.getKey(), coEntry.getValue().toString());
         }
      }
      else
      {
         return new Properties(sessionFactoryProperties);
      }
      return props;
   }
   
   public Set<BaseNamedElement> getConfigurationElements()
   {
      Set<BaseNamedElement> result = new HashSet<BaseNamedElement>();
      for (Map.Entry<String, Object> entry : configurationElements.entrySet())
      {
         BaseNamedElement element = new BaseNamedElement();
         element.setName(entry.getKey());
         element.setValue(entry.getValue());
         result.add(element);
      }
      return result;
   }
   
   public void setConfigurationElements(Set<BaseNamedElement> elements)
   {
      if (elements != null)
      {
         for (BaseNamedElement element : elements)
         {
            // Handle special elements that don't get passed through to
            // Hibernate Configuration object
            String name = element.getName();
            Object value = element.getValue();
            if ( SESSION_FACTORY_INTERCEPTOR.equals(name) && value != null )
            {
               setSessionFactoryInterceptor(value.toString());
            }
            else if ( LISTENER_INJECTOR.equals(name) && value != null )
            {
               setListenerInjector(value.toString());
            }
            else if ( HAR_URL.equals(name) && value != null )
            {
               try
               {
                  setHarUrl(new URL(value.toString()));
               }
               catch (MalformedURLException e)
               {
                  throw new IllegalArgumentException("Value " + value + " for property " + name + " is not a valid URL", e);
               }
            }
            else if ( SCAN_FOR_MAPPINGS_ENABLED.equals(name) && value != null )
            {
               setScanForMappingsEnabled(Boolean.valueOf(value.toString()));
            }
            else
            {
               // The 99% case -- pass through to Hibernate
               configurationElements.put(element.getName(), element.getValue());
            }
         }
      }
   }

   /**
    * Configure Hibernate and bind the <tt>SessionFactory</tt> to JNDI.
    */
   public void start() throws Throwable
   {
      log.debug( "Hibernate MBean starting; " + this );

      // be defensive...
      if ( sessionFactory != null )
      {
         destroySessionFactory();
      }

      buildSessionFactory();
   }

   /**
    * Close the <tt>SessionFactory</tt>.
    */
   public void stop() throws Exception
   {
      destroySessionFactory();
   }

   /**
    * Centralize the logic needed for starting/binding the SessionFactory.
    *
    * @throws Exception
    */
   private void buildSessionFactory() throws Throwable
   {
      log.debug( "Building SessionFactory; " + this );

      Configuration cfg = new Configuration();
      cfg.getProperties().clear(); // avoid reading hibernate.properties and Sys-props

      // Handle custom listeners....
      ListenerInjector listenerInjector = generateListenerInjectorInstance();
      if ( listenerInjector != null )
      {
         listenerInjector.injectListeners( beanName, cfg );
      }

      // Handle config settings....
      transferSettings( cfg.getProperties() );

      // Handle mappings....
      handleMappings( cfg );

      // Handle interceptor....
      Interceptor interceptorInstance = generateInterceptorInstance();
      if ( interceptorInstance != null )
      {
         cfg.setInterceptor( interceptorInstance );
      }

      sessionFactoryProperties = new Properties(cfg.getProperties());
      
      // Generate sf....
      sessionFactory = cfg.buildSessionFactory();

      try
      {
         // Handle stat-mbean creation/registration....
         if ( controller != null && sessionFactory.getStatistics() != null && sessionFactory.getStatistics().isStatisticsEnabled() )
         {
            String serviceName = beanName.toString();
            if( serviceName.indexOf("type=service") != -1 )
            {
               serviceName = serviceName.replaceAll("type=service","type=stats");
            }
            else
            {
               serviceName = serviceName + ",type=stats";
            }
            hibernateStatisticsServiceName = serviceName;
            StatisticsService hibernateStatisticsService = new StatisticsService();
            hibernateStatisticsService.setSessionFactory( sessionFactory );
            BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(hibernateStatisticsServiceName, StatisticsService.class.getName());
            StringBuffer buffer = new StringBuffer();
            buffer.append("@").append(JMX.class.getName()).append("(name=\"").append(hibernateStatisticsServiceName).append("JMX\"");
            buffer.append(", exposedInterface=").append(StatisticsServiceMBean.class.getName()).append(".class, registerDirectly=true)");
            String jmxAnnotation = buffer.toString();
            builder.addAnnotation(jmxAnnotation);
            controller.install(builder.getBeanMetaData(), hibernateStatisticsService);
         }

         // Handle JNDI binding....
         bind();
      }
      catch ( Exception e )
      {
         forceCleanup();
         throw e;
      }

      dirty = false;

      runningSince = new Date();

      log.info( "SessionFactory successfully built and bound into JNDI [" + sessionFactoryName + "]" );
   }

   /**
    * Centralize the logic needed to unbind/close a SessionFactory.
    *
    * @throws Exception
    */
   private void destroySessionFactory() throws Exception
   {
      if ( sessionFactory != null )
      {
         // TODO : exact situations where we need to clear the 2nd-lvl cache?
         // (to allow clean release of the classloaders)
         // Most likely, if custom classes are directly cached (UserTypes); anything else?
         unbind();
         sessionFactory.close();
         sessionFactory = null;
         runningSince = null;
         sessionFactoryProperties = null;

         if ( hibernateStatisticsServiceName != null )
         {
            try
            {
               controller.uninstall( hibernateStatisticsServiceName );
            }
            catch ( Throwable t )
            {
               // just log it
               log.warn( "unable to cleanup statistics mbean", t );
            }
         }
      }
   }

   private void handleMappings(Configuration cfg) throws IOException
   {
      if (root == null)
      {
         if (harUrl == null)
            throw new IllegalArgumentException("Must set one of the resources, root or harUrl: " + this);

         root = VFS.getRoot(harUrl);
      }

      log.debug("Scanning for Hibernate mappings, root: " + root);

      HibernateMappingVisitor visitor = new HibernateMappingVisitor();
      root.visit(visitor);
      for (URL url : visitor.getUrls())
      {
         log.debug("Passing input stream [" + url + "] to Hibernate Configration");
         cfg.addInputStream(url.openStream());
      }
   }

   /**
    * Transfer the state represented by our current attribute values into the given Properties instance, translating our
    * attributes into the appropriate Hibernate settings.
    *
    * @param settings The Properties instance to which to add our state.
    */
   private void transferSettings(Properties settings)
   {
      // Establish defaults
      if ( getCacheProviderClass() == null )
      {
         setCacheProviderClass("org.hibernate.cache.HashtableCacheProvider");
      }
      if ( getSecondLevelCacheEnabled() == null)
      {
         setSecondLevelCacheEnabled(Boolean.TRUE);
      }
      if (configurationElements.get(Environment.TRANSACTION_MANAGER_STRATEGY) == null)
      {
         configurationElements.put(Environment.TRANSACTION_MANAGER_STRATEGY, JBossTransactionManagerLookup.class.getName());
      }
      if (configurationElements.get(Environment.TRANSACTION_STRATEGY) == null)
      {
         configurationElements.put(Environment.TRANSACTION_STRATEGY, JTATransactionFactory.class.getName());
      }
      
      if ( getDeployedCacheJndiName() != null && getCacheRegionFactoryClass() == null)
      {
         // Implies shared cache region factory
         configurationElements.put(Environment.CACHE_REGION_FACTORY, org.hibernate.cache.jbc2.JndiSharedJBossCacheRegionFactory.class.getName());
      }

      if ( getDeployedCacheManagerJndiName() != null && getCacheRegionFactoryClass() == null)
      {
         // Implies multliplexed cache region factory
         configurationElements.put(Environment.CACHE_REGION_FACTORY, org.hibernate.cache.jbc2.JndiMultiplexedJBossCacheRegionFactory.class.getName());
      }

      if (configurationElements.get(Environment.FLUSH_BEFORE_COMPLETION) == null)
      {
         configurationElements.put( Environment.FLUSH_BEFORE_COMPLETION, "true" );
      }
      if (configurationElements.get(Environment.AUTO_CLOSE_SESSION) == null)
      {
         configurationElements.put( Environment.AUTO_CLOSE_SESSION, "true" );
      }
      
      // This is really H3-version-specific:
      // in 3.0.3 and later, this should be the ConnectionReleaseMode enum;
      // in 3.0.2, this is a true/false setting;
      // in 3.0 -> 3.0.1, there is no such setting
      //
      // so we just set them both :)
      if (configurationElements.get("hibernate.connection.agressive_release") == null)
      {
         configurationElements.put( "hibernate.connection.agressive_release", "true" );
      }
      if (configurationElements.get("hibernate.connection.release_mode") == null)
      {
         configurationElements.put( "hibernate.connection.release_mode", "after_statement" );
      }

      log.debug( "Using JDBC batch size : " + getJdbcBatchSize() );
      
      // Translate any legacy "bean property name" elements
      Map<String, Object> ourConfig = new HashMap<String, Object>(configurationElements);
      
      setUnlessNull( settings, Environment.DATASOURCE, ourConfig.remove(DATASOURCE_NAME) );
      setUnlessNull( settings, Environment.DIALECT, ourConfig.remove(DIALECT) );
      setUnlessNull( settings, Environment.CACHE_PROVIDER, ourConfig.remove(CACHE_PROVIDER_CLASS) );
      setUnlessNull( settings, Environment.CACHE_REGION_FACTORY, ourConfig.remove(CACHE_REGION_FACTORY_CLASS) );
      setUnlessNull( settings, Environment.CACHE_REGION_PREFIX, ourConfig.remove(CACHE_REGION_PREFIX) );
      setUnlessNull( settings, Environment.USE_MINIMAL_PUTS, ourConfig.remove(MINIMAL_PUTS_ENABLED) );
      setUnlessNull( settings, Environment.HBM2DDL_AUTO, ourConfig.remove(HBM2DDL_AUTO) );
      setUnlessNull( settings, Environment.DEFAULT_SCHEMA, ourConfig.remove(DEFAULT_SCHEMA) );
      setUnlessNull( settings, Environment.STATEMENT_BATCH_SIZE, ourConfig.remove(JDBC_BATCH_SIZE) );
      setUnlessNull( settings, Environment.USE_SQL_COMMENTS, ourConfig.remove(SQL_COMMENTS_ENABLED) );

      setUnlessNull( settings, Environment.STATEMENT_FETCH_SIZE, ourConfig.remove(JDBC_FETCH_SIZE) );
      setUnlessNull( settings, Environment.USE_SCROLLABLE_RESULTSET, ourConfig.remove(JDBC_SCROLLABLE_RESULT_SET_ENABLED) );
      setUnlessNull( settings, Environment.USE_QUERY_CACHE, ourConfig.remove(QUERY_CACHE_ENABLED) );
      setUnlessNull( settings, Environment.USE_STRUCTURED_CACHE, ourConfig.remove(STRUCTURED_CACHE_ENTRIES_ENABLED) );
      setUnlessNull( settings, Environment.QUERY_SUBSTITUTIONS, ourConfig.remove(QUERY_SUBSTITUTIONS) );
      setUnlessNull( settings, Environment.MAX_FETCH_DEPTH, ourConfig.remove(MAX_FETCH_DEPTH) );
      setUnlessNull( settings, Environment.SHOW_SQL, ourConfig.remove(SHOW_SQL_ENABLED) );
      setUnlessNull( settings, Environment.USE_GET_GENERATED_KEYS, ourConfig.remove(GET_GENERATED_KEYS_ENABLED) );
      setUnlessNull( settings, Environment.USER, ourConfig.remove(USERNAME) );
      setUnlessNull( settings, Environment.PASS, ourConfig.remove(PASSWORD) );
      setUnlessNull( settings, Environment.BATCH_VERSIONED_DATA, ourConfig.remove(BATCH_VERSIONED_DATA_ENABLED) );
      setUnlessNull( settings, Environment.USE_STREAMS_FOR_BINARY, ourConfig.remove(STREAMS_FOR_BINARY_ENABLED) );
      setUnlessNull( settings, Environment.USE_REFLECTION_OPTIMIZER, ourConfig.remove(REFLECTION_OPTIMIZATION_ENABLED) );
      setUnlessNull( settings, Environment.GENERATE_STATISTICS, ourConfig.remove(STAT_GENERATION_ENABLED) );
      setUnlessNull( settings, Environment.DEFAULT_CATALOG, ourConfig.remove(DEFAULT_CATALOG) );
      setUnlessNull( settings, Environment.USE_SECOND_LEVEL_CACHE, ourConfig.remove(SECOND_LEVEL_CACHE_ENABLED) );
      setUnlessNull( settings, org.hibernate.cache.jbc2.builder.JndiMultiplexingCacheInstanceManager.CACHE_FACTORY_RESOURCE_PROP, ourConfig.remove(DEPLOYED_CACHE_MANAGER_JNDI_NAME));
      setUnlessNull( settings, org.hibernate.cache.jbc2.builder.JndiSharedCacheInstanceManager.CACHE_RESOURCE_PROP, ourConfig.remove(DEPLOYED_CACHE_JNDI_NAME));
      
      // Set any remaining properties; presumably these are standard
      // Hibernate configuration properties
      for (Map.Entry<String, Object> entry : ourConfig.entrySet())
      {
         setUnlessNull( settings, entry.getKey(), entry.getValue() );
      }
   }

   /**
    * Simple helper method for transferring individual settings to a properties
    * instance only if the setting's value is not null.
    *
    * @param props The properties instance into which to transfer the setting
    * @param key   The key under which to transfer the setting
    * @param value The value of the setting.
    */
   private void setUnlessNull(Properties props, String key, Object value)
   {
      if ( value != null )
      {
         props.setProperty( key, value.toString() );
      }
   }

   private ListenerInjector generateListenerInjectorInstance()
   {
      if ( listenerInjector == null )
      {
         return null;
      }

      log.info( "attempting to use listener injector [" + listenerInjector + "]" );
      try
      {
         return ( ListenerInjector ) Thread.currentThread()
               .getContextClassLoader()
               .loadClass( listenerInjector )
               .newInstance();
      }
      catch ( Throwable t )
      {
         log.warn( "Unable to generate specified listener injector", t );
      }

      return null;
   }

   private Interceptor generateInterceptorInstance()
   {
      if ( sessionFactoryInterceptor == null )
      {
         return null;
      }

      log.info( "Generating session factory interceptor instance [" + sessionFactoryInterceptor + "]" );
      try
      {
         return ( Interceptor ) Thread.currentThread()
               .getContextClassLoader()
               .loadClass( sessionFactoryInterceptor )
               .newInstance();
      }
      catch ( Throwable t )
      {
         log.warn( "Unable to generate session factory interceptor instance", t );
      }

      return null;
   }

   /**
    * Perform the steps necessary to bind the managed SessionFactory into JNDI.
    *
    * @throws HibernateException
    */
   private void bind() throws HibernateException
   {
      if (bindInJndi)
      {
         InitialContext ctx = null;
         try
         {
            ctx = new InitialContext();
            Util.bind( ctx, sessionFactoryName, sessionFactory );
         }
         catch ( NamingException e )
         {
            throw new HibernateException( "Unable to bind SessionFactory into JNDI", e );
         }
         finally
         {
            if ( ctx != null )
            {
               try
               {
                  ctx.close();
               }
               catch ( Throwable ignore )
               {
                  // ignore
               }
            }
         }
      }
   }

   /**
    * Perform the steps necessary to unbind the managed SessionFactory from JNDI.
    *
    * @throws HibernateException
    */
   private void unbind() throws HibernateException
   {
      if (bindInJndi)
      {
         InitialContext ctx = null;
         try
         {
            ctx = new InitialContext();
            Util.unbind( ctx, sessionFactoryName );
         }
         catch ( NamingException e )
         {
            throw new HibernateException( "Unable to unbind SessionFactory from JNDI", e );
         }
         finally
         {
            if ( ctx != null )
            {
               try
               {
                  ctx.close();
               }
               catch ( Throwable ignore )
               {
                  // ignore
               }
            }
         }
      }
   }

   private void forceCleanup()
   {
      try
      {
         sessionFactory.close();
         sessionFactory = null;
      }
      catch ( Throwable ignore )
      {
         // ignore
      }
   }

   public String toString()
   {
      return super.toString() + " [BeanName=" + beanName + ", JNDI=" + sessionFactoryName + "]";
   }


   // Managed operations ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

   public void rebuildSessionFactory() throws Throwable
   {
      destroySessionFactory();
      buildSessionFactory();
   }


   // RO managed attributes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

   public boolean isDirty()
   {
      return dirty;
   }

   public boolean isSessionFactoryRunning()
   {
      return sessionFactory != null;
   }

   public String getVersion()
   {
      return Version.getVersionString();
   }

   public SessionFactory getInstance()
   {
      return sessionFactory;
   }

   public Object getStatisticsServiceName()
   {
      return hibernateStatisticsServiceName;
   }

   public Date getRunningSince()
   {
      return runningSince;
   }

   // R/W managed attributes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

   public String getSessionFactoryName()
   {
      return sessionFactoryName;
   }

   public void setSessionFactoryName(String sessionFactoryName)
   {
      this.sessionFactoryName = sessionFactoryName;
      dirty = true;
   }

   public String getDatasourceName()
   {
      return getConfigurationElementAsString(DATASOURCE_NAME, Environment.DATASOURCE);
   }

   public void setDatasourceName(String datasourceName)
   {
      setConfigurationElement(datasourceName, DATASOURCE_NAME, Environment.DATASOURCE);
   }

   public String getUsername()
   {
      return getConfigurationElementAsString(USERNAME, Environment.USER);
   }

   public void setUsername(String username)
   {
      setConfigurationElement( username, USERNAME, Environment.USER );
   }

   public void setPassword(String password)
   {
      setConfigurationElement( password, PASSWORD, Environment.PASS ); 
   }

   public String getDefaultSchema()
   {
      return getConfigurationElementAsString(DEFAULT_SCHEMA, Environment.DEFAULT_SCHEMA);
   }

   public void setDefaultSchema(String defaultSchema)
   {
      setConfigurationElement(defaultSchema, DEFAULT_SCHEMA, Environment.DEFAULT_SCHEMA);
   }

   public String getDefaultCatalog()
   {
      return getConfigurationElementAsString(DEFAULT_CATALOG, Environment.DEFAULT_CATALOG);
   }

   public void setDefaultCatalog(String defaultCatalog)
   {
      setConfigurationElement( defaultCatalog, DEFAULT_CATALOG, Environment.DEFAULT_CATALOG);
   }

   public String getHbm2ddlAuto()
   {
      return getConfigurationElementAsString(HBM2DDL_AUTO, Environment.HBM2DDL_AUTO);
   }

   public void setHbm2ddlAuto(String hbm2ddlAuto)
   {
      setConfigurationElement(hbm2ddlAuto, HBM2DDL_AUTO, Environment.HBM2DDL_AUTO);
   }

   public String getDialect()
   {
      return getConfigurationElementAsString(DIALECT, Environment.DIALECT);
   }

   public void setDialect(String dialect)
   {
      setConfigurationElement(dialect, DIALECT, Environment.DIALECT);
   }

   public Integer getMaxFetchDepth()
   {
      return getConfigurationElementAsInteger(MAX_FETCH_DEPTH, Environment.MAX_FETCH_DEPTH);
   }

   public void setMaxFetchDepth(Integer maxFetchDepth)
   {
      setConfigurationElement( maxFetchDepth, MAX_FETCH_DEPTH, Environment.MAX_FETCH_DEPTH);
   }

   public Integer getJdbcBatchSize()
   {
      return getConfigurationElementAsInteger(JDBC_BATCH_SIZE, Environment.STATEMENT_BATCH_SIZE );
   }

   public void setJdbcBatchSize(Integer jdbcBatchSize)
   {
      setConfigurationElement( jdbcBatchSize, JDBC_BATCH_SIZE, Environment.STATEMENT_BATCH_SIZE );
   }

   public Integer getJdbcFetchSize()
   {
      return getConfigurationElementAsInteger(JDBC_FETCH_SIZE, Environment.STATEMENT_FETCH_SIZE);
   }

   public void setJdbcFetchSize(Integer jdbcFetchSize)
   {
      setConfigurationElement(jdbcFetchSize, JDBC_FETCH_SIZE, Environment.STATEMENT_FETCH_SIZE);
   }

   public Boolean getJdbcScrollableResultSetEnabled()
   {
      return getConfigurationElementAsBoolean(JDBC_SCROLLABLE_RESULT_SET_ENABLED, Environment.USE_SCROLLABLE_RESULTSET);
   }

   public void setJdbcScrollableResultSetEnabled(Boolean jdbcScrollableResultSetEnabled)
   {
      setConfigurationElement(jdbcScrollableResultSetEnabled, JDBC_SCROLLABLE_RESULT_SET_ENABLED, Environment.USE_SCROLLABLE_RESULTSET);
   }

   public Boolean getGetGeneratedKeysEnabled()
   {
      return getConfigurationElementAsBoolean(GET_GENERATED_KEYS_ENABLED, Environment.USE_GET_GENERATED_KEYS);
   }

   public void setGetGeneratedKeysEnabled(Boolean getGeneratedKeysEnabled)
   {
      setConfigurationElement( getGeneratedKeysEnabled, GET_GENERATED_KEYS_ENABLED, Environment.USE_GET_GENERATED_KEYS);
   }

   public String getQuerySubstitutions()
   {
      return getConfigurationElementAsString(QUERY_SUBSTITUTIONS, Environment.QUERY_SUBSTITUTIONS);
   }

   public void setQuerySubstitutions(String querySubstitutions)
   {
      setConfigurationElement(querySubstitutions, QUERY_SUBSTITUTIONS, Environment.QUERY_SUBSTITUTIONS);
   }

   public Boolean getSecondLevelCacheEnabled()
   {
      return getConfigurationElementAsBoolean(SECOND_LEVEL_CACHE_ENABLED, Environment.USE_SECOND_LEVEL_CACHE);
   }

   public void setSecondLevelCacheEnabled(Boolean secondLevelCacheEnabled)
   {
      setConfigurationElement( secondLevelCacheEnabled, SECOND_LEVEL_CACHE_ENABLED, Environment.USE_SECOND_LEVEL_CACHE);
   }

   public Boolean getQueryCacheEnabled()
   {
      return getConfigurationElementAsBoolean(QUERY_CACHE_ENABLED, Environment.USE_QUERY_CACHE);
   }

   public void setQueryCacheEnabled(Boolean queryCacheEnabled)
   {
      setConfigurationElement(queryCacheEnabled, QUERY_CACHE_ENABLED, Environment.USE_QUERY_CACHE);
   }

   public String getCacheProviderClass()
   {
      return getConfigurationElementAsString(CACHE_PROVIDER_CLASS, Environment.CACHE_PROVIDER);
   }

   public void setCacheProviderClass(String cacheProviderClass)
   {
      setConfigurationElement(cacheProviderClass,CACHE_PROVIDER_CLASS, Environment.CACHE_PROVIDER);
   }

   /**
    * @see org.jboss.hibernate.jmx.HibernateMBean#getCacheRegionFactoryClass()
    */
   public String getCacheRegionFactoryClass()
   {
      return getConfigurationElementAsString(CACHE_REGION_FACTORY_CLASS, Environment.CACHE_REGION_FACTORY);
   }

   /**
    * @see org.jboss.hibernate.jmx.HibernateMBean#setCacheRegionFactoryClass(java.lang.String)
    */
   public void setCacheRegionFactoryClass(String regionFactoryClass)
   {
      setConfigurationElement(regionFactoryClass, CACHE_REGION_FACTORY_CLASS, Environment.CACHE_REGION_FACTORY);
   }

   public String getCacheRegionPrefix()
   {
      return getConfigurationElementAsString(CACHE_REGION_PREFIX, Environment.CACHE_REGION_PREFIX);
   }

   public void setCacheRegionPrefix(String cacheRegionPrefix)
   {
      setConfigurationElement(cacheRegionPrefix, CACHE_REGION_PREFIX, Environment.CACHE_REGION_PREFIX);
   }

   public Boolean getMinimalPutsEnabled()
   {
      return getConfigurationElementAsBoolean(MINIMAL_PUTS_ENABLED, Environment.USE_MINIMAL_PUTS);
   }

   public void setMinimalPutsEnabled(Boolean minimalPutsEnabled)
   {
      setConfigurationElement(minimalPutsEnabled, MINIMAL_PUTS_ENABLED, Environment.USE_MINIMAL_PUTS);
   }

   public Boolean getUseStructuredCacheEntriesEnabled()
   {
      return getConfigurationElementAsBoolean(STRUCTURED_CACHE_ENTRIES_ENABLED, Environment.USE_STRUCTURED_CACHE);
   }

   public void setUseStructuredCacheEntriesEnabled(Boolean structuredCacheEntriesEnabled)
   {
      setConfigurationElement(structuredCacheEntriesEnabled, STRUCTURED_CACHE_ENTRIES_ENABLED, Environment.USE_STRUCTURED_CACHE);
   }

   public Boolean getShowSqlEnabled()
   {
      return getConfigurationElementAsBoolean(SHOW_SQL_ENABLED, Environment.SHOW_SQL);
   }

   public void setShowSqlEnabled(Boolean showSqlEnabled)
   {
      setConfigurationElement( showSqlEnabled, SHOW_SQL_ENABLED, Environment.SHOW_SQL);
   }

   public Boolean getSqlCommentsEnabled()
   {
      return getConfigurationElementAsBoolean(SQL_COMMENTS_ENABLED, Environment.USE_SQL_COMMENTS);
   }

   public void setSqlCommentsEnabled(Boolean commentsEnabled)
   {
      setConfigurationElement( commentsEnabled, SQL_COMMENTS_ENABLED, Environment.USE_SQL_COMMENTS);
   }

   public String getSessionFactoryInterceptor()
   {
      return sessionFactoryInterceptor;
   }

   public void setSessionFactoryInterceptor(String sessionFactoryInterceptor)
   {
      this.sessionFactoryInterceptor = sessionFactoryInterceptor;
      dirty = true;
   }

   public String getListenerInjector()
   {
      return listenerInjector;
   }

   public void setListenerInjector(String listenerInjector)
   {
      this.listenerInjector = listenerInjector;
   }

   /**
    * @see org.jboss.hibernate.jmx.HibernateMBean#getDeployedCacheJndiName()
    */
   public String getDeployedCacheJndiName()
   {
      return getConfigurationElementAsString(DEPLOYED_CACHE_JNDI_NAME, org.hibernate.cache.jbc2.builder.JndiSharedCacheInstanceManager.CACHE_RESOURCE_PROP);
   }

   /**
    * @see org.jboss.hibernate.jmx.HibernateMBean#setDeployedCacheJndiName(java.lang.String)
    */
   public void setDeployedCacheJndiName(String name)
   {
      setConfigurationElement( name, DEPLOYED_CACHE_JNDI_NAME, org.hibernate.cache.jbc2.builder.JndiSharedCacheInstanceManager.CACHE_RESOURCE_PROP);
   }

   /**
    * @see org.jboss.hibernate.jmx.HibernateMBean#getDeployedCacheManagerJndiName()
    */
   public String getDeployedCacheManagerJndiName()
   {
      return getConfigurationElementAsString(DEPLOYED_CACHE_MANAGER_JNDI_NAME, org.hibernate.cache.jbc2.builder.JndiMultiplexingCacheInstanceManager.CACHE_FACTORY_RESOURCE_PROP);
   }

   /**
    * @see org.jboss.hibernate.jmx.HibernateMBean#setDeployedCacheManagerJndiName(java.lang.String)
    */
   public void setDeployedCacheManagerJndiName(String name)
   {
      setConfigurationElement( name, DEPLOYED_CACHE_MANAGER_JNDI_NAME, org.hibernate.cache.jbc2.builder.JndiMultiplexingCacheInstanceManager.CACHE_FACTORY_RESOURCE_PROP);
   }

   public Boolean getBatchVersionedDataEnabled()
   {
      return getConfigurationElementAsBoolean(BATCH_VERSIONED_DATA_ENABLED, Environment.BATCH_VERSIONED_DATA);
   }

   public void setBatchVersionedDataEnabled(Boolean batchVersionedDataEnabled)
   {
      setConfigurationElement( batchVersionedDataEnabled, BATCH_VERSIONED_DATA_ENABLED, Environment.BATCH_VERSIONED_DATA);
   }

   public Boolean getStreamsForBinaryEnabled()
   {
      return getConfigurationElementAsBoolean(STREAMS_FOR_BINARY_ENABLED, Environment.USE_STREAMS_FOR_BINARY);
   }

   public void setStreamsForBinaryEnabled(Boolean streamsForBinaryEnabled)
   {
      setConfigurationElement( streamsForBinaryEnabled, STREAMS_FOR_BINARY_ENABLED, Environment.USE_STREAMS_FOR_BINARY);
   }

   public Boolean getReflectionOptimizationEnabled()
   {
      return getConfigurationElementAsBoolean(REFLECTION_OPTIMIZATION_ENABLED, Environment.USE_REFLECTION_OPTIMIZER);
   }

   public void setReflectionOptimizationEnabled(Boolean reflectionOptimizationEnabled)
   {
      setConfigurationElement( reflectionOptimizationEnabled, REFLECTION_OPTIMIZATION_ENABLED, Environment.USE_REFLECTION_OPTIMIZER);
   }

   public Boolean getStatGenerationEnabled()
   {
      return getConfigurationElementAsBoolean(STAT_GENERATION_ENABLED, Environment.GENERATE_STATISTICS);
   }

   public void setStatGenerationEnabled(Boolean statGenerationEnabled)
   {
      setConfigurationElement( statGenerationEnabled, STAT_GENERATION_ENABLED, Environment.GENERATE_STATISTICS);
   }

   public URL getHarUrl()
   {
      return harUrl;
   }

   public void setHarUrl(URL harUrl)
   {
      this.harUrl = harUrl;
      dirty = true;
   }

   public boolean isScanForMappingsEnabled()
   {
      return scanForMappingsEnabled;
   }

   public void setScanForMappingsEnabled(boolean scanForMappingsEnabled)
   {
      this.scanForMappingsEnabled = scanForMappingsEnabled;
   }
   
   private String getConfigurationElementAsString(String beanPropertyName, String hibernateName)
   {
      Object element = getConfigurationElement(beanPropertyName, hibernateName);
      return element == null ? null : element.toString();
   }
   
   private Boolean getConfigurationElementAsBoolean(String beanPropertyName, String hibernateName)
   {
      Object element = getConfigurationElement(beanPropertyName, hibernateName);
      if (element instanceof String)
      {
         return Boolean.valueOf((String) element);
      }
      return (Boolean) element;
   }
   
   private Integer getConfigurationElementAsInteger(String beanPropertyName, String hibernateName)
   {
      Object element = getConfigurationElement(beanPropertyName, hibernateName);
      if (element instanceof String)
      {
         return Integer.valueOf((String) element);
      }
      return (Integer) element;
   }
   
   private Object getConfigurationElement(String beanPropertyName, String hibernateName)
   {
      Object name = configurationElements.get(beanPropertyName);
      if (name == null)
      {
         name = configurationElements.get(hibernateName);
      }
      return name;
   }
   
   private void setConfigurationElement(Object value, String beanPropertyName, String hibernateName)
   {
      configurationElements.remove(beanPropertyName);
      if (value == null)
      {
         configurationElements.remove(hibernateName);
      }
      else
      {
         configurationElements.put(hibernateName, value);
      }
      dirty = true;
   }
}
