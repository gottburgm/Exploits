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
package org.jboss.web.tomcat.service.session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.metadata.web.jboss.SnapshotMode;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactoryFactory;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.TomcatClusterConfig;
import org.jboss.web.tomcat.service.session.distributedcache.spi.TomcatClusterDistributedCacheManagerFactory;

/**
 * A Tomcat <code>Cluster</code> implementation that uses a JBoss
 * <code>TreeCache</code> to support intra-cluster session replication.
 * <p>
 * This class registers a <code>TreeCache</code> in JMX, making it 
 * available to other users who wish to replicate data within the cluster.
 * </p>
 *  
 * @author Brian Stansberry
 * @version $Revision: 87304 $
 */
public class JBossCacheCluster
   implements TomcatClusterConfig, JBossCacheClusterMBean, Lifecycle 
{
   //  -------------------------------------------------------  Static Fields
   
   protected static final String info = "JBossCacheCluster/2.1";

   public static Log log = LogFactory.getLog(JBossCacheCluster.class);

   public static final String DEFAULT_CACHE_CONFIG_PATH = "conf/cluster-cache.xml";
   
   //  -------------------------------------------------------  Instance Fields

   /** Parent container of this cluster. */
   private Container container = null;
   
   /** Our JMX Server. */
   private MBeanServer mserver = null;
   
   /** Name under which we are registered in JMX */
   private ObjectName objectName = null;
   
   /** The factory we use to create our DistributedCacheManager */
   private TomcatClusterDistributedCacheManagerFactory factory = null;
   
   /** Are we started? */
   private boolean started = false;

   /** The lifecycle event support for this component. */
   private LifecycleSupport lifecycle = new LifecycleSupport(this);

   /** Name under which our TreeCache is registered in JMX */
   private String pojoCacheObjectName = "jboss.cache:service=TomcatClusteringCache";
   
   /** Name of the tree cache's JGroups channel */
   private String clusterName = null;
   
   /** File name, URL or String to use to configure JGroups. */
   private String cacheConfigPath = null;
   
   /**
    * Implementation of Manager to instantiate when 
    * createManager() is called.
    */
   private String managerClassName = JBossCacheManager.class.getName();

   /** Does the Engine in which we are running use mod_jk? */
   private boolean useJK = false;
   
   /** Whether our Managers should use a local cache. */
   private boolean useLocalCache = false;

   /**
    * Default replication trigger to assign to our 
    * Managers that haven't had this property set.
    */
   private ReplicationTrigger defaultReplicationTrigger = null;

   /**
    * Default replication granularity to assign to our Managers
    * that haven't had this property set.
    */
   private ReplicationGranularity defaultReplicationGranularity = null;
   
   /**
    * JBossCacheManager's snapshot mode.
    */
   private SnapshotMode snapshotMode = null;
   
   /**
    * JBossCacheManager's snapshot interval.
    */
   private int snapshotInterval = 0;
   
   /** Whether we use batch mode replication for field level granularity */
   private boolean replicationFieldBatchMode = true;
   
   //  ----------------------------------------------------------  Constructors
   
   /**
    * Default constructor.
    */
   public JBossCacheCluster()
   {
      super();
   }

   //  ------------------------------------------------------------  Properties 

   /**
    * Gets a String representation of the JMX <code>ObjectName</code> under
    * which our <code>TreeCache</code> is registered.
    * <p>
    * If this property is not explicitly set, the <code>TreeCache</code> will
    * be registered under 
    * @{@link Tomcat6.DEFAULT_CACHE_NAME the default name used in 
    * embedded JBoss/Tomcat}.
    * </p>
    * 
    * @jmx.managed-attribute
    */
   public String getCacheObjectName()
   {
      return pojoCacheObjectName;
   }

   /**
    * Sets the JMX <code>ObjectName</code> under which our 
    * <code>TreeCache</code> is registered, if already created, or under
    * which it should be registered if this object creates it.
    * 
    * @jmx.managed-attribute
    */
   public void setCacheObjectName(String objectName)
   {
      this.pojoCacheObjectName = objectName;
   }

   /**
    * Sets the name of the <code>TreeCache</code>'s JGroups channel.
    * <p>
    * This property is ignored if a <code>TreeCache</code> is already
    * registered under the provided 
    * {@link #setCacheObjectName cache object name}.
    * </p>
    * 
    * @jmx.managed-attribute
    */
   public void setClusterName(String clusterName)
   {
      this.clusterName = clusterName;
   }

   /**
    * Gets the filesystem path, which can either be absolute or a path 
    * relative to <code>$CATALINA_BASE</code>, where a
    * a JBossCache configuration file can be found.
    * 
    * @return  a path, either absolute or relative to 
    *          <code>$CATALINA_BASE</code>.  Will return 
    *          <code>null</code> if no such path was configured.
    * 
    * @jmx.managed-attribute
    */
   public String getCacheConfigPath()
   {
      return cacheConfigPath;
   }

   /** 
    * Sets the filesystem path, which can either be absolute or a path 
    * relative to <code>$CATALINA_BASE</code>, where a
    * a JBossCache configuration file can be found.
    * <p>
    * This property is ignored if a <code>TreeCache</code> is already
    * registered under the provided 
    * {@link #setCacheObjectName cache object name}.
    * </p>
    * 
    * @param cacheConfigPath   a path, absolute or relative to 
    *                          <code>$CATALINA_BASE</code>,
    *                          pointing to a JBossCache configuration file.
    *                            
    * @jmx.managed-attribute
    */
   public void setCacheConfigPath(String cacheConfigPath)
   {
      this.cacheConfigPath = cacheConfigPath;
   }

   /**
    * Get the current Catalina MBean Server.
    * 
    * @return the mbean server
    */
   public MBeanServer getMBeanServer()
   {
      if (mserver == null)
      {
         mserver = Registry.getRegistry(null, null).getMBeanServer();
      }
      return mserver;
   }

   /**
    * Gets the name of the implementation of Manager to instantiate when 
    * createManager() is called.
    * 
    * @jmx.managed-attribute
    */
   public String getManagerClassName()
   {
      return managerClassName;
   }

   /**
    * Sets the name of the implementation of Manager to instantiate when 
    * createManager() is called.
    * <p>
    * This should be {@link JBossCacheManager} (the default) or a subclass
    * of it.
    * </p>
    * 
    * @jmx.managed-attribute
    */
   public void setManagerClassName(String managerClassName)
   {
      this.managerClassName = managerClassName;
   }

   public void registerManager(Manager arg0)
   {
      // TODO tie this into the managerClassName      
   }

   public void removeManager(Manager arg0)
   {
      // TODO tie this into the managerClassName
   }

   /**
    * Gets whether the <code>Engine</code> in which we are running
    * uses <code>mod_jk</code>.
    * 
    * @jmx.managed-attribute
    */
   public boolean isUseJK()
   {
      return useJK;
   }

   /**
    * Sets whether the <code>Engine</code> in which we are running
    * uses <code>mod_jk</code>.
    * 
    * @jmx.managed-attribute
    */
   public void setUseJK(boolean useJK)
   {
      this.useJK = useJK;
   }

   /**
    * Gets the <code>JBossCacheManager</code>'s <code>useLocalCache</code>
    * property.
    * 
    * @jmx.managed-attribute
    */
   public boolean isUseLocalCache()
   {
      return useLocalCache;
   }

   /**
    * Sets the <code>JBossCacheManager</code>'s <code>useLocalCache</code>
    * property.
    * 
    * @jmx.managed-attribute
    */
   public void setUseLocalCache(boolean useLocalCache)
   {
      this.useLocalCache = useLocalCache;
   }

   /**
    * Gets the default granularity of session data replicated across the 
    * cluster; i.e. whether the entire session should be replicated when 
    * replication is triggered, or only modified attributes.
    * <p>
    * The value of this property is applied to <code>Manager</code> instances
    * that did not have an equivalent property explicitly set in 
    * <code>context.xml</code> or <code>server.xml</code>.
    * </p>
    * 
    * @jmx.managed-attribute
    */
   public String getDefaultReplicationGranularity()
   {
      return defaultReplicationGranularity == null ? null : defaultReplicationGranularity.toString();
   }

   /**
    * Sets the granularity of session data replicated across the cluster.
    * Valid values are:
    * <ul>
    * <li>SESSION</li>
    * <li>ATTRIBUTE</li>
    * <li>FIELD</li>
    * </ul>
    * @jmx.managed-attribute
    */
   public void setDefaultReplicationGranularity(
         String defaultReplicationGranularity)
   {
      this.defaultReplicationGranularity = (defaultReplicationGranularity == null ? null : ReplicationGranularity.fromString(defaultReplicationGranularity.toUpperCase()));
   }

   /**
    * Gets the type of operations on a <code>HttpSession</code> that 
    * trigger replication.
    * <p>
    * The value of this property is applied to <code>Manager</code> instances
    * that did not have an equivalent property explicitly set in 
    * <code>context.xml</code> or <code>server.xml</code>.
    * </p>
    *  
    * @jmx.managed-attribute
    */
   public String getDefaultReplicationTrigger()
   {
      return defaultReplicationTrigger == null ? null : defaultReplicationTrigger.toString();
   }

   /**
    * Sets the type of operations on a <code>HttpSession</code> that 
    * trigger replication.  Valid values are:
    * <ul>
    * <li>SET_AND_GET</li>
    * <li>SET_AND_NON_PRIMITIVE_GET</li>
    * <li>SET</li>
    * </ul>
    * 
    * @jmx.managed-attribute
    */
   public void setDefaultReplicationTrigger(String defaultTrigger)
   {
      this.defaultReplicationTrigger = (defaultTrigger == null ? null : ReplicationTrigger.fromString(defaultTrigger.toUpperCase()));
   }
   
   /**
    * Gets whether Managers should use batch mode replication.
    * Only meaningful if replication granularity is set to <code>FIELD</code>.
    * 
    * @jmx.managed-attribute
    */
   public boolean getDefaultReplicationFieldBatchMode()
   {
      return replicationFieldBatchMode;
   }
   
   /**
    * Sets whether Managers should use batch mode replication.
    * Only meaningful if replication granularity is set to <code>FIELD</code>.
    * 
    * @jmx.managed-attribute
    */
   public void setDefaultReplicationFieldBatchMode(boolean replicationFieldBatchMode)
   {
      this.replicationFieldBatchMode = replicationFieldBatchMode;
   }

   /**
    * Gets when sessions are replicated to the other nodes.
    * The default value, "instant", synchronously replicates changes
    * to the other nodes. In this case, the "SnapshotInterval" attribute
    * is not used.
    * The "interval" mode, in association with the "SnapshotInterval"
    * attribute, indicates that Tomcat will only replicate modified
    * sessions every "SnapshotInterval" miliseconds at most.
    * 
    * @see #getSnapshotInterval()
    * 
    * @jmx.managed-attribute
    */
   public String getSnapshotMode()
   {
      return snapshotMode == null ? null : snapshotMode.toString();
   }

   /**
    * Sets when sessions are replicated to the other nodes. Valid values are:
    * <ul>
    * <li>instant</li>
    * <li>interval</li> 
    * </ul>
    * 
    * @jmx.managed-attribute
    */
   public void setSnapshotMode(String snapshotMode)
   {
      this.snapshotMode = (snapshotMode == null ? null : SnapshotMode.fromString(snapshotMode.toUpperCase()));
   }

   /**
    * Gets how often session changes should be replicated to other nodes.
    * Only relevant if property {@link #getSnapshotMode() snapshotMode} is 
    * set to <code>interval</code>.
    * 
    * @return the number of milliseconds between session replications.
    * 
    * @jmx.managed-attribute
    */
   public int getSnapshotInterval()
   {
      return snapshotInterval;
   }

   /**
    * Sets how often session changes should be replicated to other nodes.
    * 
    * @param snapshotInterval the number of milliseconds between 
    *                         session replications.
    * @jmx.managed-attribute
    */
   public void setSnapshotInterval(int snapshotInterval)
   {
      this.snapshotInterval = snapshotInterval;
   }
   
   // ----------------------------------------------------------------  Cluster

   /**
    * Gets the name of the <code>TreeCache</code>'s JGroups channel.
    * 
    * @see org.apache.catalina.Cluster#getClusterName()
    */
   public String getClusterName()
   {
      return clusterName;
   }
   
   /* (non-javadoc)
    * @see org.apache.catalina.Cluster#getContainer()
    */
   public Container getContainer()
   {
      return container;
   }
   
   /* (non-javadoc)
    * @see org.apache.catalina.Cluster#setContainer()
    */
   public void setContainer(Container container)
   {
      this.container = container;
   }

   /**
    * @see org.apache.catalina.Cluster#getInfo()
    * 
    * @jmx.managed-attribute access="read-only"
    */
   public String getInfo()
   {
      return info;
   }

   /**
    * @see org.apache.catalina.Cluster#createManager(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public Manager createManager(String name)
   {
      if (log.isDebugEnabled())
         log.debug("Creating ClusterManager for context " + name
               + " using class " + getManagerClassName());
      Manager manager = null;
      
      String mgrClass = getManagerClassName();
      if (mgrClass != null && !JBossCacheManager.class.getName().equals(mgrClass))
      {
         try
         {
            manager = (Manager) getClass().getClassLoader().loadClass(mgrClass).newInstance();
         } 
         catch (Exception x)
         {
            log.error("Unable to load class " + mgrClass + " for replication manager; using JBossCacheManager", x);
        }
      }
      
      if (manager == null)
      {
         if (factory == null)
            throw new IllegalStateException("PojoCache not initialized");
         manager = new JBossCacheManager(factory);
      }
      
      manager.setDistributable(true);      
      
      if (manager instanceof JBossCacheManager)
      {
         configureManager((JBossCacheManager) manager);
      }
      
      return manager;
   }

   /**
    * Does nothing; tracking the status of other members of the cluster is
    * provided by the JGroups layer. 
    * 
    * @see org.apache.catalina.Cluster#backgroundProcess()
    */
   public void backgroundProcess()
   {
      ; // no-op
   }

   // ---------------------------------------------  Deprecated Cluster Methods
   
   /**
    * Returns <code>null</code>; method is deprecated.
    * 
    * @return <code>null</code>, always.
    * 
    * @see org.apache.catalina.Cluster#getProtocol()
    */
   public String getProtocol()
   {
      return null;
   }
   
   /**
    * Does nothing; method is deprecated.
    * 
    * @see org.apache.catalina.Cluster#setProtocol(java.lang.String)
    */
   public void setProtocol(String protocol)
   {
      ; // no-op
   }
   
   /**
    * Does nothing; method is deprecated.
    * 
    * @see org.apache.catalina.Cluster#startContext(java.lang.String)
    */
   public void startContext(String contextPath) throws IOException
   {
      ; // no-op
   }
   
   /**
    * Does nothing; method is deprecated.
    * 
    * @see org.apache.catalina.Cluster#installContext(java.lang.String, java.net.URL)
    */
   public void installContext(String contextPath, URL war)
   {
      ; // no-op
   }
   
   /**
    * Does nothing; method is deprecated.
    * 
    * @see org.apache.catalina.Cluster#stop(java.lang.String)
    */
   public void stop(String contextPath) throws IOException 
   {
      ; // no-op
   }

   // ---------------------------------------------------------  Public Methods
   
   /**
    * Sets the cluster-wide properties of a <code>Manager</code> to
    * match those of this cluster.  Does not override 
    * <code>Manager</code>-specific properties with cluster-wide defaults 
    * if the <code>Manager</code>-specfic properties have already been set.
    */
   public void configureManager(JBossCacheManager<? extends OutgoingDistributableSessionData> manager)
   {
      manager.setSnapshotMode(snapshotMode);
      manager.setSnapshotInterval(snapshotInterval);
      manager.setUseJK(useJK);
      
      // Only set replication attributes if they were not
      // already set via a <Manager> element in an XML config file
      
      if (manager.getReplicationGranularity() == null) 
      {
         manager.setReplicationGranularity(defaultReplicationGranularity);
      }
      
      if (manager.getReplicationTrigger() == null) 
      {
         manager.setReplicationTrigger(defaultReplicationTrigger);
      }
      
      if (manager.isReplicationFieldBatchMode() == null)
      {
         manager.setReplicationFieldBatchMode(replicationFieldBatchMode);
      }
   }

   // ---------------------------------------------------------------  Lifecyle

   /**
    * Finds or creates a {@link TreeCache}; if created, starts the 
    * cache and registers it with our JMX server.
    * 
    * @see org.apache.catalina.Lifecycle#start()
    */
   public void start() throws LifecycleException
   {
      if (started)
      {
         throw new LifecycleException("Cluster already started");
      }

      // Notify our interested LifecycleListeners
      lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, this);
      
      try
      {         
         // Tell the JBoss MBeanServerLocator utility 
         // that Tomcat's MBean server is 'jboss'
         // JBAS-4623 Only do this if there isn't already a 'jboss' server
         try
         {
            MBeanServerLocator.locateJBoss();
         }
         catch (IllegalStateException ise)
         {
            // This is the expected condition when running in standalone Tomcat
            MBeanServerLocator.setJBoss(getMBeanServer());
         }
         
         // Initialize the tree cache
         
         factory = DistributedCacheManagerFactoryFactory.getInstance().getTomcatClusterDistributedCacheManagerFactory(this);

         registerMBeans();

         factory.start();

         started = true;
         
         // Notify our interested LifecycleListeners
         lifecycle.fireLifecycleEvent(AFTER_START_EVENT, this);

      }
      catch (LifecycleException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.error("Unable to start cluster.", e);
         throw new LifecycleException(e);
      } 
   }

   /**
    * If this object created its own {@link TreeCache}, stops it 
    * and unregisters it with JMX.
    * 
    * @see org.apache.catalina.Lifecycle#stop()
    */
   public void stop() throws LifecycleException
   {
      if (!started)
      {
         throw new IllegalStateException("Cluster not started");
      }
      
      // Notify our interested LifecycleListeners
      lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, this);
      
      try
      {
         factory.stop();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Failed to stop DistributedCacheManagerFactory", e);
      }

      started = false;
      // Notify our interested LifecycleListeners
      lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, this);

      unregisterMBeans();
   }

   /* (non-javadoc)           
    * @see org.apache.catalina.Lifecycle#addLifecycleListener()
    */
   public void addLifecycleListener(LifecycleListener listener)
   {
      lifecycle.addLifecycleListener(listener);
   }

   /* (non-javadoc)           
    * @see org.apache.catalina.Lifecycle#findLifecycleListeners()
    */
   public LifecycleListener[] findLifecycleListeners()
   {
      return lifecycle.findLifecycleListeners();
   }

   /* (non-javadoc)           
    * @see org.apache.catalina.Lifecycle#removeLifecycleListener()
    */
   public void removeLifecycleListener(LifecycleListener listener)
   {
      lifecycle.removeLifecycleListener(listener);
   }
   
   // -------------------------------------------------------- Private Methods
   
   

   public File getCacheConfigFile() throws FileNotFoundException
   {
      boolean useDefault = (this.cacheConfigPath == null);
      String path = (useDefault) ? DEFAULT_CACHE_CONFIG_PATH : cacheConfigPath;
      // See if clusterProperties points to a file relative
      // to $CATALINA_BASE
      File file = new File(path);
      if (!file.isAbsolute())
      {
         file = new File(System.getProperty("catalina.base"), path);
      }
      
      if (file.exists())
      {
         return file;
      }
      else
      {
         // User provided config was invalid; throw the exception
         String msg = "No tree cache config file found at " + 
                        file.getAbsolutePath();
         log.error(msg);
         throw new IllegalStateException(msg);
      }    
   }

   /**
    * Registers this object and the tree cache (if we created it) with JMX.
    */
   private void registerMBeans()
   {
      try
      {
         MBeanServer server = getMBeanServer();
         
         String domain;
         if (container instanceof ContainerBase)
         {
            domain = ((ContainerBase) container).getDomain();
         }
         else
         {
            domain = server.getDefaultDomain();
         }
         
         String name = ":type=Cluster";
         if (container instanceof Host) {
            name += ",host=" + container.getName();
         }
         else if (container instanceof Engine)
         {            
            name += ",engine=" + container.getName();
         }
         
         ObjectName clusterName = new ObjectName(domain + name);

         if (server.isRegistered(clusterName))
         {
            log.warn("MBean " + clusterName + " already registered");
         }
         else
         {
            this.objectName = clusterName;
            server.registerMBean(this, objectName);
         }

      }
      catch (Exception ex)
      {
         log.error(ex.getMessage(), ex);
      }
   }   

   /**
    * Unregisters this object and the tree cache (if we created it) with JMX.
    */
   private void unregisterMBeans()
   {
      if (mserver != null)
      {
         try
         {
            if (objectName != null) {
               mserver.unregisterMBean(objectName);
            }
         }
         catch (Exception e)
         {
            log.error(e);
         }
      }
   }

}
