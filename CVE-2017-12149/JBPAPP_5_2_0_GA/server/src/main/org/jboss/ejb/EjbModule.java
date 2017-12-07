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
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import javax.ejb.EJBLocalHome;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import javax.transaction.TransactionManager;

import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.invocation.InvocationType;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.MethodMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityUtil;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.security.plugins.SecurityDomainContext;
import org.jboss.system.Registry;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionManagerFactory;
import org.jboss.util.loading.DelegatingClassLoader;
import org.jboss.web.WebClassLoader;
import org.jboss.web.WebClassLoaderFactory;
import org.jboss.web.WebServiceMBean;
import org.w3c.dom.Element;

/**
 * An EjbModule represents a collection of beans that are deployed as a unit.
 * 
 * <p>
 * The beans may use the EjbModule to access other beans within the same deployment unit.
 * 
 * @see Container
 * @see EJBDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian.Brock</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 104066 $
 */
@SuppressWarnings("deprecation")
public class EjbModule extends ServiceMBeanSupport implements EjbModuleMBean
{
   public static final String BASE_EJB_MODULE_NAME = "jboss.j2ee:service=EjbModule";

   public static final ObjectName EJB_MODULE_QUERY_NAME = ObjectNameFactory.create(BASE_EJB_MODULE_NAME + ",*");

   public static String DEFAULT_STATELESS_CONFIGURATION = "Default Stateless SessionBean";

   public static String DEFAULT_STATEFUL_CONFIGURATION = "Default Stateful SessionBean";

   public static String DEFAULT_ENTITY_BMP_CONFIGURATION = "Default BMP EntityBean";

   public static String DEFAULT_ENTITY_CMP_CONFIGURATION = "Default CMP EntityBean";

   public static String DEFAULT_MESSAGEDRIVEN_CONFIGURATION = "Default MesageDriven Bean";

   // Constants uses with container interceptor configurations
   public static final int BMT = 1;

   public static final int CMT = 2;

   public static final int ANY = 3;

   static final String BMT_VALUE = "Bean";

   static final String CMT_VALUE = "Container";

   static final String ANY_VALUE = "Both";

   /** The security management */
   private ISecurityManagement securityManagement;

   /** Class logger. */
   private static final Logger log = Logger.getLogger(EjbModule.class);

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** HashMap<ejbName, Container> the containers for this deployment unit. */
   HashMap containers = new HashMap();

   /** The containers in their ApplicationMetaData ordering */
   LinkedList containerOrdering = new LinkedList();

   /** HashMap<ejbName, EJBLocalHome> of local homes */
   HashMap localHomes = new HashMap();

   /** Class loader of this deployment unit. */
   ClassLoader classLoader = null;

   /** Name of this deployment unit, url it was deployed from */
   final String name;

   private VFSDeploymentUnit deploymentUnit;

   private ServiceControllerMBean serviceController;

   private final Map moduleData = Collections.synchronizedMap(new HashMap());

   private ObjectName webServiceName;

   private TransactionManagerFactory tmFactory;

   private EJBTimerService timerService;

   /** Whether we are call by value */
   private boolean callByValue;

   private ApplicationMetaData appMetaData;

   /**
    * Policy Registration Bean (Holder of Authorization Policies)
    */
   private PolicyRegistration policyRegistration = null;

   public EjbModule(final VFSDeploymentUnit unit, ApplicationMetaData metaData)
   {
      this.appMetaData = metaData;
      this.deploymentUnit = unit;
      String name = deploymentUnit.getName();
      if (name.endsWith("/"))
      {
         name = name.substring(0, name.length() - 1);
      }
      this.name = name;

      // FIXME all this deployment unit access should be replaced with deployers processing updating the metadata
      // Ask the ejb deployer whether we are call by value
      Boolean flag = unit.getAttachment("EJB.callByValue", Boolean.class);
      DeploymentUnit temp = unit;
      while (flag == null && temp != null)
      {
         // Ask the ear deployer whether we are call by value
         flag = temp.getAttachment("EAR.callByValue", Boolean.class);
         if (flag != null)
            break;
         temp = temp.getParent();
      }
      if (flag != null)
         callByValue = flag.booleanValue();

      // Set the unauthenticated identity on the metadata if absent
      if (metaData.getUnauthenticatedPrincipal() == null)
      {
         String unauthenticatedPrincipal = unit.getAttachment("EJB.unauthenticatedIdentity", String.class);
         if (unauthenticatedPrincipal == null)
            unauthenticatedPrincipal = unit.getAttachment("EAR.unauthenticatedIdentity", String.class);
         metaData.getJBossMetaData().setUnauthenticatedPrincipal(unauthenticatedPrincipal);
      }
      // Add the ApplicationMetaData for the jbossws ejb21 deployer to pickup later
      if (unit.getAttachment(ApplicationMetaData.class) == null)
         unit.addAttachment(ApplicationMetaData.class, metaData);
   }

   /**
    * @deprecated DeploymentInfo is obsolete
    */
   @Deprecated
   public EjbModule(final DeploymentInfo di, TransactionManager tm, ObjectName webServiceName)
   {
      this.name = "deprecated";
   }

   public void setTransactionManagerFactory(TransactionManagerFactory tm)
   {
      this.tmFactory = tm;
   }

   public void setSecurityManagement(ISecurityManagement sm)
   {
      this.securityManagement = sm;
   }

   public void setPolicyRegistration(PolicyRegistration policyRegistration)
   {
      this.policyRegistration = policyRegistration;
   }

   public EJBTimerService getTimerService()
   {
      return timerService;
   }

   public void setTimerService(EJBTimerService timerService)
   {
      this.timerService = timerService;
   }

   public ObjectName getWebServiceName()
   {
      return webServiceName;
   }

   public void setWebServiceName(ObjectName webServiceName)
   {
      this.webServiceName = webServiceName;
   }

   public Map getModuleDataMap()
   {
      return moduleData;
   }

   public Object getModuleData(Object key)
   {
      return moduleData.get(key);
   }

   public void putModuleData(Object key, Object value)
   {
      moduleData.put(key, value);
   }

   public void removeModuleData(Object key)
   {
      moduleData.remove(key);
   }

   /**
    * Add a container to this deployment unit.
    * 
    * @param con
    */
   private void addContainer(Container con) throws DeploymentException
   {
      String ejbName = con.getBeanMetaData().getEjbName();
      if (containers.containsKey(ejbName))
         throw new DeploymentException("Duplicate ejb-name. Container for " + ejbName + " already exists.");
      containers.put(ejbName, con);
      containerOrdering.add(con);
      con.setEjbModule(this);
   }

   /**
    * Remove a container from this deployment unit.
    * 
    * @param con
    */
   public void removeContainer(Container con)
   {
      containers.remove(con.getBeanMetaData().getEjbName());
      containerOrdering.remove(con);
   }

   public void addLocalHome(Container con, EJBLocalHome localHome)
   {
      localHomes.put(con.getBeanMetaData().getEjbName(), localHome);
   }

   public void removeLocalHome(Container con)
   {
      localHomes.remove(con.getBeanMetaData().getEjbName());
   }

   public EJBLocalHome getLocalHome(Container con)
   {
      return (EJBLocalHome) localHomes.get(con.getBeanMetaData().getEjbName());
   }

   /**
    * Whether the container is call by value
    * 
    * @return true for call by value
    */
   public boolean isCallByValue()
   {
      return callByValue;
   }

   /**
    * Get a container from this deployment unit that corresponds to a given name
    * 
    * @param name ejb-name name defined in ejb-jar.xml
    * 
    * @return container for the named bean, or null if the container was not found
    */
   public Container getContainer(String name)
   {
      return (Container) containers.get(name);
   }

   /**
    * Get all containers in this deployment unit.
    * 
    * @return a collection of containers for each enterprise bean in this deployment unit.
    * @jmx:managed-attribute
    */
   public Collection getContainers()
   {
      return containerOrdering;
   }

   /**
    * Get the class loader of this deployment unit.
    * 
    * @return
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Set the class loader of this deployment unit
    * 
    * @param cl
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }

   /**
    * Get the URL from which this deployment unit was deployed
    * 
    * @return The URL from which this Application was deployed.
    */
   public URL getURL()
   {
      return appMetaData.getUrl();
   }

   // Service implementation ----------------------------------------

   @Override
   protected void createService() throws Exception
   {
      serviceController = (ServiceControllerMBean) MBeanProxyExt.create(ServiceControllerMBean.class,
            ServiceControllerMBean.OBJECT_NAME, server);

      log.debug("createService, begin");

      // Set up the beans in this module.
      try
      {
         Iterator beans = appMetaData.getEnterpriseBeans();
         String contextID = appMetaData.getJaccContextID();
         if (contextID == null)
            contextID = deploymentUnit.getSimpleName();
         // appMetaData.gsetJaccContextID(contextID);
         /* PolicyConfiguration pc = null; */
         while (beans.hasNext())
         {
            BeanMetaData bean = (BeanMetaData) beans.next();
            log.info("Deploying " + bean.getEjbName());
            Container con = createContainer(bean, deploymentUnit);
            addContainer(con);
            // @todo support overriding the context id via metadata is needed
            con.setJaccContextID(contextID);
         }

         // only one iteration should be necessary, but we won't sweat it.
         // 2 iterations are needed by cmp...jdbc/bridge/JDBCCMRFieldBridge which
         // assumes persistence managers are all set up for every
         // bean in the relationship!
         ListIterator iter = containerOrdering.listIterator();
         while (iter.hasNext())
         {
            Container con = (Container) iter.next();
            ObjectName jmxName = con.getJmxName();
            /*
             * Add the container mbean to the deployment mbeans so the state of the deployment can be tracked.
             */
            server.registerMBean(con, jmxName);
            // deploymentUnit.mbeans.add(jmxName);
            BeanMetaData metaData = con.getBeanMetaData();
            Collection<ObjectName> depends = new ArrayList<ObjectName>();
            for (String dependsName : metaData.getDepends())
            {
               depends.add(ObjectName.getInstance(dependsName));
            }
            Iterator<String> invokerBindings = metaData.getInvokerBindings();
            while (invokerBindings != null && invokerBindings.hasNext())
            {
               String invokerBindingName = invokerBindings.next();
               InvokerProxyBindingMetaData ipbmd = appMetaData.getInvokerProxyBindingMetaDataByName(invokerBindingName);
               if (ipbmd != null)
               {
                  String invokerName = ipbmd.getInvokerMBean();
                  if (invokerName != null)
                  {
                     try
                     {
                        ObjectName invokerMBean = ObjectName.getInstance(invokerName);
                        if (depends.contains(invokerMBean) == false)
                           depends.add(invokerMBean);
                     }
                     catch (MalformedObjectNameException e)
                     {
                        log.trace("Ignored malformed invoker mbean '" + invokerName + "' " + e.toString());
                     }
                  }
               }
            }
            serviceController.create(jmxName, depends);
            // We keep the hashCode around for fast creation of proxies
            int jmxHash = jmxName.hashCode();
            Registry.bind(new Integer(jmxHash), jmxName);
            log.debug("Bound jmxName=" + jmxName + ", hash=" + jmxHash + "into Registry");
         }
      }
      catch (Exception e)
      {
         destroyService();
         throw e;
      } // end of try-catch

   }

   /**
    * The mbean Service interface <code>start</code> method calls the start method on each contatiner, then the init
    * method on each container. Conversion to a different registration system with one-phase startup is conceivable.
    * 
    * @exception Exception if an error occurs
    */
   @Override
   protected void startService() throws Exception
   {
      // before EntityContainer returns from the startService, its PM should be usable
      ListIterator iter = containerOrdering.listIterator();
      while (iter.hasNext())
      {
         Container con = (Container) iter.next();
         if (con.getBeanMetaData().isEntity())
         {
            ClassLoader oldCl = SecurityActions.getContextClassLoader();
            SecurityActions.setContextClassLoader(con.getClassLoader());
            con.pushENC();
            try
            {
               ((EntityContainer) con).getPersistenceManager().start();
            }
            finally
            {
               con.popENC();
               // Reset classloader
               SecurityActions.setContextClassLoader(oldCl);
            }
         }
      }

      iter = containerOrdering.listIterator();
      while (iter.hasNext())
      {
         Container con = (Container) iter.next();
         log.debug("startService, starting container: " + con.getBeanMetaData().getEjbName());
         serviceController.start(con.getJmxName());
      }
   }

   /**
    * Stops all the containers of this application.
    */
   @Override
   protected void stopService() throws Exception
   {
      ListIterator iter = containerOrdering.listIterator(containerOrdering.size());
      while (iter.hasPrevious())
      {
         Container con = (Container) iter.previous();
         try
         {
            ObjectName jmxName = con.getJmxName();
            // The container may already be destroyed so validate metaData
            BeanMetaData metaData = con.getBeanMetaData();
            String ejbName = metaData != null ? metaData.getEjbName() : "Unknown";
            log.debug("stopService, stopping container: " + ejbName);

            serviceController.stop(jmxName);
         }
         catch (Exception e)
         {
            log.error("unexpected exception stopping Container: " + con.getJmxName(), e);
         } // end of try-catch
      }
   }

   @Override
   protected void destroyService() throws Exception
   {
      WebServiceMBean webServer = null;
      if (webServiceName != null)
      {
         webServer = (WebServiceMBean) MBeanProxyExt.create(WebServiceMBean.class, webServiceName);
      }
      ListIterator iter = containerOrdering.listIterator(containerOrdering.size());
      while (iter.hasPrevious())
      {
         Container con = (Container) iter.previous();
         ObjectName jmxName = con.getJmxName();
         int conState = con.getState();
         boolean destroyContainer = true;
         log.debug("Looking to destroy container: " + jmxName + ", state: " + con.getStateString() + ", destroy: "
               + destroyContainer);

         // always unregister from Registry
         int jmxHash = jmxName.hashCode();
         Registry.unbind(new Integer(jmxHash));

         // Unregister the web classloader
         // Removing the wcl should probably be done in stop of the container,
         // but I don't want to look for errors today.
         if (webServer != null)
         {
            ClassLoader wcl = con.getWebClassLoader();
            if (wcl != null)
            {
               try
               {
                  webServer.removeClassLoader(wcl);
               }
               catch (Throwable e)
               {
                  log.warn("Failed to unregister webClassLoader", e);
               }
            }
         }

         // Only destroy containers that have been created or started
         if (destroyContainer)
         {
            try
            {
               serviceController.destroy(jmxName);
               serviceController.remove(jmxName);
               log.info("Undeployed " + con.getBeanMetaData().getEjbName());
               if (server.isRegistered(jmxName))
                  server.unregisterMBean(jmxName);
            }
            catch (Throwable e)
            {
               log.error("unexpected exception destroying Container: " + jmxName, e);
            } // end of try-catch
         }

         // Destroy proxy factories
         if (destroyContainer)
         {
            if (con.getBeanMetaData() != null && con.getBeanMetaData().getInvokerBindings() != null)
            {
               Iterator<String> invokerBindings = con.getBeanMetaData().getInvokerBindings();
               while (invokerBindings.hasNext())
               {
                  String invoker = invokerBindings.next();
                  EJBProxyFactory ci = con.lookupProxyFactory(invoker);
                  if (ci != null)
                  {
                     ci.setContainer(null);
                     ci.setInvokerBinding(null);
                     ci.setInvokerMetaData(null);
                  }
               }
            }
         }

         // cleanup container
         con.setBeanMetaData(null);
         con.setWebClassLoader(null);
         con.setClassLoader(null);
         con.setEjbModule(null);
         con.setDeploymentInfo(null);
         con.setTransactionManager(null);
         con.setSecurityManager(null);
         con.setRealmMapping(null);
         con.setSecurityProxy(null);
         con.setSecurityManagement(null);
         con.setPolicyRegistration(null);
         con.proxyFactories.clear();
      }

      this.containers.clear();
      this.localHomes.clear();
      this.containerOrdering.clear();
      this.moduleData.clear();
      this.serviceController = null;
   }

   // ******************
   // Container Creation
   // ******************

   private Container createContainer(BeanMetaData bean, VFSDeploymentUnit unit) throws Exception
   {
      Container container = null;
      // Added message driven deployment
      if (bean.isMessageDriven())
      {
         container = createMessageDrivenContainer(bean, unit);
      }
      else if (bean.isSession()) // Is session?
      {
         if (((SessionMetaData) bean).isStateless()) // Is stateless?
         {
            container = createStatelessSessionContainer((SessionMetaData) bean, unit);
         }
         else
         // Stateful
         {
            container = createStatefulSessionContainer((SessionMetaData) bean, unit);
         }
      }
      else
      // Entity
      {
         container = createEntityContainer(bean, unit);
      }

      container.setDeploymentUnit(unit);

      return container;
   }

   private MessageDrivenContainer createMessageDrivenContainer(BeanMetaData bean, DeploymentUnit unit) throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Stolen from Stateless deploy
      // Create container
      MessageDrivenContainer container = new MessageDrivenContainer();
      int transType = bean.isContainerManagedTx() ? CMT : BMT;

      initializeContainer(container, conf, bean, transType, unit);
      createProxyFactories(bean, container);
      container.setInstancePool(createInstancePool(conf, unit.getClassLoader()));

      return container;
   }

   private StatelessSessionContainer createStatelessSessionContainer(SessionMetaData bean, DeploymentUnit unit)
         throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Create container
      StatelessSessionContainer container = new StatelessSessionContainer();
      int transType = bean.isContainerManagedTx() ? CMT : BMT;
      initializeContainer(container, conf, bean, transType, unit);
      if (bean.getHome() != null || bean.getServiceEndpoint() != null)
      {
         createProxyFactories(bean, container);
      }
      container.setInstancePool(createInstancePool(conf, unit.getClassLoader()));

      return container;
   }

   private StatefulSessionContainer createStatefulSessionContainer(SessionMetaData bean, DeploymentUnit unit)
         throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Create container
      StatefulSessionContainer container = new StatefulSessionContainer();
      int transType = bean.isContainerManagedTx() ? CMT : BMT;
      initializeContainer(container, conf, bean, transType, unit);
      if (bean.getHome() != null || bean.getServiceEndpoint() != null)
      {
         createProxyFactories(bean, container);
      }

      ClassLoader cl = unit.getClassLoader();
      container.setInstanceCache(createInstanceCache(conf, cl));
      // No real instance pool, use the shadow class
      StatefulSessionInstancePool ip = new StatefulSessionInstancePool();
      ip.importXml(conf.getContainerPoolConf());
      container.setInstancePool(ip);
      // Set persistence manager
      container.setPersistenceManager((StatefulSessionPersistenceManager) cl.loadClass(conf.getPersistenceManager())
            .newInstance());
      // Set the bean Lock Manager
      container.setLockManager(createBeanLockManager(container, false, conf.getLockClass(), cl));

      return container;
   }

   private EntityContainer createEntityContainer(BeanMetaData bean, DeploymentUnit unit) throws Exception
   {
      // get the container configuration for this bean
      // a default configuration is now always provided
      ConfigurationMetaData conf = bean.getContainerConfiguration();
      // Create container
      EntityContainer container = new EntityContainer();
      int transType = CMT;
      initializeContainer(container, conf, bean, transType, unit);
      if (bean.getHome() != null)
      {
         createProxyFactories(bean, container);
      }

      ClassLoader cl = unit.getClassLoader();
      container.setInstanceCache(createInstanceCache(conf, cl));
      container.setInstancePool(createInstancePool(conf, cl));
      // Set the bean Lock Manager
      boolean reentrant = ((EntityMetaData) bean).isReentrant();
      BeanLockManager lockMgr = createBeanLockManager(container, reentrant, conf.getLockClass(), cl);
      container.setLockManager(lockMgr);

      // Set persistence manager
      if (((EntityMetaData) bean).isBMP())
      {
         Class pmClass = cl.loadClass(conf.getPersistenceManager());
         EntityPersistenceManager pm = (EntityPersistenceManager) pmClass.newInstance();
         container.setPersistenceManager(pm);
      }
      else
      {
         // CMP takes a manager and a store
         org.jboss.ejb.plugins.CMPPersistenceManager persistenceManager = new org.jboss.ejb.plugins.CMPPersistenceManager();

         // Load the store from configuration
         Class pmClass = cl.loadClass(conf.getPersistenceManager());
         EntityPersistenceStore pm = (EntityPersistenceStore) pmClass.newInstance();
         persistenceManager.setPersistenceStore(pm);
         // Set the manager on the container
         container.setPersistenceManager(persistenceManager);
      }

      return container;
   }

   // **************
   // Helper Methods
   // **************

   /**
    * Perform the common steps to initializing a container.
    */
   private void initializeContainer(Container container, ConfigurationMetaData conf, BeanMetaData bean, int transType,
         DeploymentUnit unit) throws NamingException, DeploymentException
   {
      // Create local classloader for this container
      // For loading resources that must come from the local jar. Not for loading classes!
      // The VFS should be used for this
      // container.setLocalClassLoader(new URLClassLoader(new URL[0], localCl));
      // Set metadata (do it *before* creating the container's WebClassLoader)
      container.setEjbModule(this);
      container.setBeanMetaData(bean);

      ClassLoader unitCl = unit.getClassLoader();
      // Create the container's WebClassLoader
      // and register it with the web service.
      String webClassLoaderName = getWebClassLoader(conf, bean);
      log.debug("Creating WebClassLoader of class " + webClassLoaderName);
      WebClassLoader wcl = null;
      try
      {
         Class clazz = unitCl.loadClass(webClassLoaderName);
         wcl = WebClassLoaderFactory.createWebClassLoader(clazz, container.getJmxName(), (RealClassLoader) unitCl);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Failed to create WebClassLoader of class " + webClassLoaderName + ": ", e);
      }

      if (webServiceName != null)
      {
         WebServiceMBean webServer = (WebServiceMBean) MBeanProxyExt.create(WebServiceMBean.class, webServiceName);
         URL[] codebase = {webServer.addClassLoader(wcl)};

         wcl.setWebURLs(codebase);
      } // end of if ()

      container.setWebClassLoader(wcl);
      // Create classloader for this container
      // Only used to unique the bean ENC and does not augment class loading
      container.setClassLoader(new DelegatingClassLoader(wcl));

      // Set transaction manager
      InitialContext iniCtx = new InitialContext();
      container.setTransactionManager(tmFactory.getTransactionManager());

      // Set
      container.setTimerService(timerService);

      // Set security domain manager
      String securityDomain = bean.getApplicationMetaData().getSecurityDomain();
      // JBAS-5960: Set default security domain if there is security metadata
      boolean hasSecurityMetaData = hasSecurityMetaData(bean);
      if (securityDomain == null && hasSecurityMetaData)
      {
         securityDomain = SecurityConstants.DEFAULT_EJB_APPLICATION_POLICY;
      }
      String confSecurityDomain = conf.getSecurityDomain();
      // Default the config security to the application security manager
      if (confSecurityDomain == null)
         confSecurityDomain = securityDomain;

      // Check for an empty confSecurityDomain which signifies to disable security
      if (confSecurityDomain != null && confSecurityDomain.length() == 0)
         confSecurityDomain = null;

      if (confSecurityDomain != null)
      { // Either the application has a security domain or the container has security setup
         try
         {
            String unprefixed = SecurityUtil.unprefixSecurityDomain(confSecurityDomain);
            log.debug("Setting security domain from: " + confSecurityDomain);
            String domainCtx = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + unprefixed + "/domainContext";
            SecurityDomainContext sdc = (SecurityDomainContext) iniCtx.lookup(domainCtx);
            Object securityMgr = sdc.getSecurityManager();

            // Object securityMgr = iniCtx.lookup(confSecurityDomain);
            AuthenticationManager ejbS = (AuthenticationManager) securityMgr;
            RealmMapping rM = (RealmMapping) securityMgr;
            container.setSecurityManager(ejbS);
            container.setRealmMapping(rM);

            container.setSecurityManagement(securityManagement);
            container.setPolicyRegistration(policyRegistration);

            container.setDefaultSecurityDomain((String) unit.getAttachment("EJB.defaultSecurityDomain"));
            container.setSecurityContextClassName((String) unit.getAttachment("EJB.securityContextClassName"));
         }
         catch (NamingException e)
         {
            throw new DeploymentException("Could not find the security-domain, name=" + confSecurityDomain, e);
         }
         catch (Exception e)
         {
            throw new DeploymentException("Invalid security-domain specified, name=" + confSecurityDomain, e);
         }
      }
      else
      {
    	 if ("".equals(securityDomain) && hasSecurityMetaData)
    		log.warn("EJB configured to bypass security. Please verify if this is intended. Bean=" + bean.getEjbName()
    			  + " Deployment=" + unit.getName());
      }

      // Load the security proxy instance if one was configured
      String securityProxyClassName = bean.getSecurityProxy();
      if (securityProxyClassName != null)
      {
         try
         {
            Class proxyClass = unitCl.loadClass(securityProxyClassName);
            Object proxy = proxyClass.newInstance();
            container.setSecurityProxy(proxy);
            log.debug("setSecurityProxy, " + proxy);
         }
         catch (Exception e)
         {
            throw new DeploymentException("Failed to create SecurityProxy of type: " + securityProxyClassName, e);
         }
      }

      // Install the container interceptors based on the configuration
      addInterceptors(container, transType, conf.getContainerInterceptorsConf());
   }

   /**
    * Return the name of the WebClassLoader class for this ejb.
    */
   private static String getWebClassLoader(ConfigurationMetaData conf, BeanMetaData bmd) throws DeploymentException
   {
      String webClassLoader = null;
      Iterator it = bmd.getInvokerBindings();
      int count = 0;
      while (it.hasNext())
      {
         String invoker = (String) it.next();
         ApplicationMetaData amd = bmd.getApplicationMetaData();
         InvokerProxyBindingMetaData imd = amd.getInvokerProxyBindingMetaDataByName(invoker);
         if (imd == null)
         {
            String msg = "Failed to find InvokerProxyBindingMetaData for: '" + invoker
                  + "'. Check the invoker-proxy-binding-name to " + "invoker-proxy-binding/name mappings in jboss.xml";
            throw new DeploymentException(msg);
         }

         Element proxyFactoryConfig = imd.getProxyFactoryConfig();
         String webCL = MetaData.getOptionalChildContent(proxyFactoryConfig, "web-class-loader");
         if (webCL != null)
         {
            log.debug("Invoker " + invoker + " specified WebClassLoader class" + webCL);
            webClassLoader = webCL;
            count++;
         }
      }
      if (count > 1)
      {
         log.warn(count + " invokers have WebClassLoader specifications.");
         log.warn("Using the last specification seen (" + webClassLoader + ").");
      }
      else if (count == 0)
      {
         webClassLoader = conf.getWebClassLoader();
         if (webClassLoader == null)
            webClassLoader = "org.jboss.web.WebClassLoader";
      }
      return webClassLoader;
   }

   /**
    * Given a container-interceptors element of a container-configuration, add the indicated interceptors to the
    * container depending on the container transcation type.
    * 
    * @param container the container instance to setup.
    * @param transType one of the BMT, CMT or ANY constants.
    * @param element the container-interceptors element from the container-configuration.
    */
   private void addInterceptors(Container container, int transType, Element element) throws DeploymentException
   {
      // Get the interceptor stack(either jboss.xml or standardjboss.xml)
      Iterator interceptorElements = MetaData.getChildrenByTagName(element, "interceptor");
      String transTypeString = stringTransactionValue(transType);
      ClassLoader loader = container.getClassLoader();
      /*
       * First build the container interceptor stack from interceptorElements match transType values
       */
      ArrayList istack = new ArrayList();
      while (interceptorElements != null && interceptorElements.hasNext())
      {
         Element ielement = (Element) interceptorElements.next();
         /*
          * Check that the interceptor is configured for the transaction mode of the bean by comparing its 'transaction'
          * attribute to the string representation of transType
          */
         String transAttr = ielement.getAttribute("transaction");
         if (transAttr == null || transAttr.length() == 0)
            transAttr = ANY_VALUE;
         if (transAttr.equalsIgnoreCase(ANY_VALUE) || transAttr.equalsIgnoreCase(transTypeString))
         { // The transaction type matches the container bean trans type

            String className = null;
            try
            {
               className = MetaData.getFirstElementContent(ielement, null);
               Class clazz = loader.loadClass(className);
               Interceptor interceptor = (Interceptor) clazz.newInstance();
               if (interceptor instanceof XmlLoadable)
               {
                  ((XmlLoadable) interceptor).importXml(ielement);
               }
               istack.add(interceptor);
            }
            catch (ClassNotFoundException e)
            {
               log.warn("Could not load the " + className + " interceptor", e);
            }
            catch (Exception e)
            {
               log.warn("Could not load the " + className + " interceptor for this container", e);
            }
         }
      }

      if (istack.size() == 0)
         log.warn("There are no interceptors configured. Check the standardjboss.xml file");

      // Now add the interceptors to the container
      for (int i = 0; i < istack.size(); i++)
      {
         Interceptor interceptor = (Interceptor) istack.get(i);
         container.addInterceptor(interceptor);
      }

      /*
       * If there is a security proxy associated with the container add its interceptor just before the container
       * interceptor
       */
      if (container.getSecurityProxy() != null)
         container.addInterceptor(new SecurityProxyInterceptor());

      // Finally we add the last interceptor from the container
      container.addInterceptor(container.createContainerInterceptor());
   }

   /**
    * Create any JACC permissions for the ejb methods that were not explicitly assigned method-permission or
    * exclude-list mappings.
    * 
    * @param con - the ejb container
    * @param bean - the bean metadata
    * @throws ClassNotFoundException
    * @throws PolicyContextException
    */
   void createMissingPermissions(Container con, BeanMetaData bean) throws ClassNotFoundException,
         PolicyContextException
   {
      String contextID = con.getJaccContextID();
      PolicyConfigurationFactory pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
      PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, false);
      Class clazz = con.getHomeClass();
      // If there is no security domain mark all methods as unchecked
      boolean hasSecurityDomain = con.getSecurityManager() != null;
      boolean exclude = hasSecurityDomain ? bean.isExcludeMissingMethods() : false;

      if (clazz != null)
      {
         addMissingMethodPermissions(bean, exclude, clazz, InvocationType.HOME, pc);
      }
      clazz = con.getLocalHomeClass();
      if (clazz != null)
      {
         addMissingMethodPermissions(bean, exclude, clazz, InvocationType.LOCALHOME, pc);
      }
      clazz = con.getLocalClass();
      if (clazz != null)
      {
         addMissingMethodPermissions(bean, exclude, clazz, InvocationType.LOCAL, pc);
      }
      clazz = con.getRemoteClass();
      if (clazz != null)
      {
         addMissingMethodPermissions(bean, exclude, clazz, InvocationType.REMOTE, pc);
      }

      if (pc.inService() == false)
         pc.commit();
      // Allow the policy to incorporate the policy configs
      Policy.getPolicy().refresh();
   }

   private void getInterfaces(Class iface, HashSet tmp)
   {
      tmp.add(iface);
      Class[] ifaces = iface.getInterfaces();
      for (int n = 0; n < ifaces.length; n++)
      {
         Class iface2 = ifaces[n];
         tmp.add(iface2);
         getInterfaces(iface2, tmp);
      }
   }

   private void addMissingMethodPermissions(BeanMetaData bean, boolean exclude, Class iface, InvocationType type,
         PolicyConfiguration pc) throws PolicyContextException
   {
      String ejbName = bean.getEjbName();
      HashSet tmp = new HashSet();
      getInterfaces(iface, tmp);
      Class[] ifaces = new Class[tmp.size()];
      tmp.toArray(ifaces);
      for (int n = 0; n < ifaces.length; n++)
      {
         Class c = ifaces[n];
         Method[] methods = c.getDeclaredMethods();
         for (int m = 0; m < methods.length; m++)
         {
            String methodName = methods[m].getName();
            Class[] params = methods[m].getParameterTypes();
            // See if there is a method-permission
            if (bean.hasMethodPermission(methodName, params, type))
               continue;
            // Create a permission for the missing method-permission
            EJBMethodPermission p = new EJBMethodPermission(ejbName, type.toInterfaceString(), methods[m]);
            if (exclude)
               pc.addToExcludedPolicy(p);
            else
               pc.addToUncheckedPolicy(p);
         }
      }
   }

   private static String stringTransactionValue(int transType)
   {
      String transaction = ANY_VALUE;
      switch (transType)
      {
         case BMT :
            transaction = BMT_VALUE;
            break;
         case CMT :
            transaction = CMT_VALUE;
            break;
      }
      return transaction;
   }

   /**
    * Create all proxy factories for this ejb
    */
   private static void createProxyFactories(BeanMetaData conf, Container container) throws Exception
   {
      ClassLoader cl = container.getClassLoader();
      Iterator it = conf.getInvokerBindings();
      boolean foundOne = false;
      while (it.hasNext())
      {
         String invoker = (String) it.next();
         String jndiBinding = conf.getInvokerBinding(invoker);
         log.debug("creating binding for " + jndiBinding + ":" + invoker);
         InvokerProxyBindingMetaData imd = conf.getApplicationMetaData().getInvokerProxyBindingMetaDataByName(invoker);
         EJBProxyFactory ci = null;

         // create a ProxyFactory instance
         try
         {
            ci = (EJBProxyFactory) cl.loadClass(imd.getProxyFactory()).newInstance();
            ci.setContainer(container);
            ci.setInvokerMetaData(imd);
            ci.setInvokerBinding(jndiBinding);
            if (ci instanceof XmlLoadable)
            {
               // the container invoker can load its configuration from the jboss.xml element
               ((XmlLoadable) ci).importXml(imd.getProxyFactoryConfig());
            }
            container.addProxyFactory(invoker, ci);
            foundOne = true;
         }
         catch (Exception e)
         {
            log.warn("The Container Invoker " + invoker
                  + " (in jboss.xml or standardjboss.xml) could not be created because of " + e
                  + " We will ignore this error, but you may miss a transport for this bean.");
         }
      }
      if (!foundOne)
      {
         throw new DeploymentException("Missing or invalid Container Invokers (in jboss.xml or standardjboss.xml).");
      }
   }

   private static BeanLockManager createBeanLockManager(Container container, boolean reentrant, String beanLock,
         ClassLoader cl) throws Exception
   {
      // The bean lock manager
      BeanLockManager lockManager = new BeanLockManager(container);

      Class lockClass = null;
      try
      {
         if (beanLock == null)
            beanLock = "org.jboss.ejb.plugins.lock.QueuedPessimisticEJBLock";
         lockClass = cl.loadClass(beanLock);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Missing or invalid lock class (in jboss.xml or standardjboss.xml): " + beanLock
               + " - " + e);
      }

      lockManager.setLockCLass(lockClass);
      lockManager.setReentrant(reentrant);

      return lockManager;
   }

   private static InstancePool createInstancePool(ConfigurationMetaData conf, ClassLoader cl) throws Exception
   {
      // Set instance pool
      InstancePool ip = null;

      try
      {
         ip = (InstancePool) cl.loadClass(conf.getInstancePool()).newInstance();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Missing or invalid Instance Pool (in jboss.xml or standardjboss.xml)", e);
      }

      if (ip instanceof XmlLoadable)
         ((XmlLoadable) ip).importXml(conf.getContainerPoolConf());

      return ip;
   }

   private static InstanceCache createInstanceCache(ConfigurationMetaData conf, ClassLoader cl) throws Exception
   {
      // Set instance cache
      InstanceCache ic = null;

      try
      {
         ic = (InstanceCache) cl.loadClass(conf.getInstanceCache()).newInstance();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Missing or invalid Instance Cache (in jboss.xml or standardjboss.xml)", e);
      }

      if (ic instanceof XmlLoadable)
         ((XmlLoadable) ic).importXml(conf.getContainerCacheConf());

      return ic;
   }

   private boolean hasSecurityMetaData(BeanMetaData bean)
   {
      boolean hasSecMetaData = false;
      Iterator<MethodMetaData> iter = bean.getPermissionMethods();
      while (iter.hasNext())
      {
         MethodMetaData method = iter.next();
         if (!method.isUnchecked())
         {
            hasSecMetaData = true;
            break;
         }
      }

      return hasSecMetaData;
   }
}
/*
 * vim:ts=3:sw=3:et
 */
