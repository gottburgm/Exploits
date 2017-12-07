/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.MarshalException;
import java.security.AccessController;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.spi.HandleDelegate;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;
import javax.xml.soap.SOAPMessage;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.plugins.local.BaseLocalProxyFactory;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.ejb.txtimer.EJBTimerServiceImpl;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationStatistics;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.JBossLazyUnmarshallingException;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EjbLocalRefMetaData;
import org.jboss.metadata.EjbRefMetaData;
import org.jboss.metadata.EnvEntryBinder;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.MessageDestinationRefMetaData;
import org.jboss.metadata.ResourceEnvRefMetaData;
import org.jboss.metadata.ResourceRefMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.serviceref.ServiceReferenceHandler;
import org.jboss.metadata.serviceref.VirtualFileAdaptor;
import org.jboss.mx.util.ObjectNameConverter;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.naming.ENCFactory;
import org.jboss.naming.ENCThreadLocalKey;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.naming.Util;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityConstants;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.NestedError;
import org.jboss.util.NestedRuntimeException;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.omg.CORBA.ORB;

/**
 * This is the base class for all EJB-containers in JBoss. A Container
 * functions as the central hub of all metadata and plugins. Through this
 * the container plugins can get hold of the other plugins and any metadata
 * they need.
 *
 * <p>The EJBDeployer creates instances of subclasses of this class
 *    and calls the appropriate initialization methods.
 *
 * <p>A Container does not perform any significant work, but instead delegates
 *    to the plugins to provide for all kinds of algorithmic functionality.
 *
 * @see EJBDeployer
 *
 * @author <a href="mailto:rickard.oberg@jboss.org">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:christoph.jung@infor.de">Christoph G. Jung</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author adrian@jboss.org
 * @author anil.saldhana@jboss.org
 * @version $Revision: 90276 $
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public abstract class Container extends ServiceMBeanSupport implements ContainerMBean, AllowedOperationsFlags
{
   public final static String BASE_EJB_CONTAINER_NAME = "jboss.j2ee:service=EJB";

   public final static ObjectName ORB_NAME = ObjectNameFactory.create("jboss:service=CorbaORB");

   public final static ObjectName EJB_CONTAINER_QUERY_NAME = ObjectNameFactory.create(BASE_EJB_CONTAINER_NAME + ",*");

   protected static final Method EJBOBJECT_REMOVE;

   /** A reference to {@link javax.ejb.TimedObject#ejbTimeout}. */
   protected static final Method EJB_TIMEOUT;

   /** This is the application that this container is a part of */
   protected EjbModule ejbModule;

   /**
    * This is the classloader of this container. All classes and resources that
    * the bean uses will be loaded from here. By doing this we make the bean
    * re-deployable
    */
   protected ClassLoader classLoader;

   /** The class loader for remote dynamic classloading */
   protected ClassLoader webClassLoader;

   /**
    * Externally supplied configuration data
    */
   private VFSDeploymentUnit unit;

   /**
    * This is the new metadata. it includes information from both ejb-jar and
    * jboss.xml the metadata for the application can be accessed trough
    * metaData.getApplicationMetaData()
    */
   protected BeanMetaData metaData;

   /** This is the EnterpriseBean class */
   protected Class beanClass;

   /** This is the Home interface class */
   protected Class homeInterface;

   /** This is the Remote interface class */
   protected Class remoteInterface;

   /** The local home interface class */
   protected Class localHomeInterface;

   /** The local inteface class */
   protected Class localInterface;

   /** This is the TransactionManager */
   protected TransactionManager tm;

   /** The Security Context FQN */
   protected String securityContextClassName;

   /** Security Domain to fall back on **/
   protected String defaultSecurityDomain;

   /** SecurityManagement Instance - holder of all security managers */
   protected ISecurityManagement securityManagement;

   /** PolicyRegistration - Holds Authorization Policies */
   protected PolicyRegistration policyRegistration;

   /** This is the SecurityManager */
   protected AuthenticationManager sm;

   /** This is the realm mapping */
   protected RealmMapping rm;

   /** The custom security proxy used by the SecurityInterceptor */
   protected Object securityProxy;

   /** This is the bean lock manager that is to be used */
   protected BeanLockManager lockManager;

   /** ??? */
   protected LocalProxyFactory localProxyFactory = new BaseLocalProxyFactory();

   /** This is a cache for method permissions */
   private HashMap<Method, Set<Principal>> methodPermissionsCache = new HashMap<Method, Set<Principal>>();

   /** Maps for MarshalledInvocation mapping */
   protected Map marshalledInvocationMapping = new HashMap();

   /** ObjectName of Container */
   private ObjectName jmxName;

   /** HashMap<String, EJBProxyFactory> for the invoker bindings */
   protected HashMap proxyFactories = new HashMap();

   /** A priviledged actions for MBeanServer.invoke when running with sec mgr */
   private MBeanServerAction serverAction = new MBeanServerAction();

   /**
    * The Proxy factory is set in the Invocation.  This TL is used
    * for methods that do not have access to the Invocation.
    */
   protected ThreadLocal proxyFactoryTL = new ThreadLocal();

   /** The number of create invocations that have been made */
   protected long createCount;

   /** The number of create invocations that have been made */
   protected long removeCount;

   /** Time statistics for the invoke(Invocation) methods */
   protected InvocationStatistics invokeStats = new InvocationStatistics();

   /** The JACC context id for the container */
   protected String jaccContextID;

   /**
    * Flag to denote whether a JACC configuration has been fitted for authorization
    */
   protected boolean isJaccEnabled = false;

   protected EJBTimerService timerService;

   static
   {
      try
      {
         EJBOBJECT_REMOVE = EJBObject.class.getMethod("remove", new Class[0]);
         EJB_TIMEOUT = TimedObject.class.getMethod("ejbTimeout", new Class[] { Timer.class });
      }
      catch (Throwable t)
      {
         throw new NestedRuntimeException(t);
      }
   }

   // Public --------------------------------------------------------

   public Class getLocalClass()
   {
      return localInterface;
   }

   public Class getLocalHomeClass()
   {
      return localHomeInterface;
   }

   public Class getRemoteClass()
   {
      return remoteInterface;
   }

   /**
    * this actually should be called remotehome, but for interface compliance purposes
    * we keep it like that
    */
   public Class getHomeClass()
   {
      return homeInterface;
   }

   /**
    * Whether the bean is call by value
    *
    * @return true for call by value
    */
   public boolean isCallByValue()
   {
      if (ejbModule.isCallByValue())
         return true;
      return metaData.isCallByValue();
   }

   /**
    * Sets a transaction manager for this container.
    *
    * @see javax.transaction.TransactionManager
    *
    * @param tm
    */
   public void setTransactionManager(final TransactionManager tm)
   {
      this.tm = tm;
   }

   /**
    * Returns this container's transaction manager.
    *
    * @return A concrete instance of javax.transaction.TransactionManager
    */
   public TransactionManager getTransactionManager()
   {
      return tm;
   }

   public void setSecurityManager(AuthenticationManager sm)
   {
      this.sm = sm;
   }

   public AuthenticationManager getSecurityManager()
   {
      return sm;
   }

   public ISecurityManagement getSecurityManagement()
   {
      return securityManagement;
   }

   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   public PolicyRegistration getPolicyRegistration()
   {
      return policyRegistration;
   }

   public void setPolicyRegistration(PolicyRegistration policyRegistration)
   {
      this.policyRegistration = policyRegistration;
   }

   public String getDefaultSecurityDomain()
   {
      return defaultSecurityDomain;
   }

   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

   public String getSecurityContextClassName()
   {
      return securityContextClassName;
   }

   public void setSecurityContextClassName(String securityContextClassName)
   {
      this.securityContextClassName = securityContextClassName;
   }

   public BeanLockManager getLockManager()
   {
      return lockManager;
   }

   public void setLockManager(final BeanLockManager lockManager)
   {
      this.lockManager = lockManager;
      lockManager.setContainer(this);
   }

   public void addProxyFactory(String invokerBinding, EJBProxyFactory factory)
   {
      proxyFactories.put(invokerBinding, factory);
   }

   public void setRealmMapping(final RealmMapping rm)
   {
      this.rm = rm;
   }

   public RealmMapping getRealmMapping()
   {
      return rm;
   }

   public void setSecurityProxy(Object proxy)
   {
      this.securityProxy = proxy;
   }

   public Object getSecurityProxy()
   {
      return securityProxy;
   }

   public EJBProxyFactory getProxyFactory()
   {
      EJBProxyFactory factory = (EJBProxyFactory)proxyFactoryTL.get();
      // There's no factory thread local which means this is probably
      // a local invocation. Just use the first (usually only)
      // proxy factory.
      // TODO: define a default factory in the meta data or
      //       even better, let the return over the original transport
      //       plugin the transport layer for the generated proxy
      if (factory == null && remoteInterface != null)
      {
         Iterator i = proxyFactories.values().iterator();
         if (i.hasNext())
            factory = (EJBProxyFactory)i.next();
      }
      return factory;
   }

   public void setProxyFactory(Object factory)
   {
      proxyFactoryTL.set(factory);
   }

   public EJBProxyFactory lookupProxyFactory(String binding)
   {
      return (EJBProxyFactory)proxyFactories.get(binding);
   }

   /**
    * Gets the DeploymentInfo for this Container
    * @deprecated use DeploymentUnit accessors
    * @return The DeploymentInfo for this Container
    */
   public final DeploymentInfo getDeploymentInfo()
   {
      return null;
   }

   /**
    * Sets the DeploymentInfo of this Container
    * @deprecated use DeploymentUnit accessors
    * @param di The new DeploymentInfo to be used
    */
   public final void setDeploymentInfo(DeploymentInfo di)
   {
   }

   public final VFSDeploymentUnit getDeploymentUnit()
   {
      return unit;
   }

   public final void setDeploymentUnit(VFSDeploymentUnit di)
   {
      this.unit = di;
   }

   /**
    * Sets the application deployment unit for this container. All the bean
    * containers within the same application unit share the same instance.
    *
    * @param   app     application for this container
    */
   public void setEjbModule(EjbModule app)
   {
      ejbModule = app;
   }

   public String getJaccContextID()
   {
      return jaccContextID;
   }

   public void setJaccContextID(String id)
   {
      jaccContextID = id;
   }

   public EJBTimerService getTimerService()
   {
      return timerService;
   }

   public void setTimerService(EJBTimerService timerService)
   {
      this.timerService = timerService;
   }

   /**
    * Get the flag whether JACC is enabled
    * @return
    */
   public boolean isJaccEnabled()
   {
      return isJaccEnabled;
   }

   /**
    * Set the flag that JACC is enabled
    *
    * @param isJaccEnabled
    */
   public void setJaccEnabled(boolean isJaccEnabled)
   {
      this.isJaccEnabled = isJaccEnabled;
   }

   /**
    * Gets the application deployment unit for this container. All the bean
    * containers within the same application unit share the same instance.
    * @jmx.managed-attribute
    */
   public EjbModule getEjbModule()
   {
      return ejbModule;
   }

   /**
    * Gets the deployment unit name that contains this container.
    */
   public String getDeploymentName()
   {
      return ejbModule.name;
   }

   /**
    * Gets the number of create invocations that have been made
    * @jmx.managed-attribute
    */
   public long getCreateCount()
   {
      return createCount;
   }

   /**
    * Gets the number of remove invocations that have been made
    * @jmx.managed-attribute
    */
   public long getRemoveCount()
   {
      return removeCount;
   }

   /** Gets the invocation statistics collection
    * @jmx.managed-attribute
    */
   public InvocationStatistics getInvokeStats()
   {
      return invokeStats;
   }


   /**
    * Converts the method invocation stats into a detyped nested map structure.
    * The format is:
    *
    * {methodName => {statisticTypeName => longValue}}
    *
    * @return A map indexed by method name with map values indexed by statistic type
    */
   public Map<String, Map<String, Long>> getDetypedInvocationStatistics()
   {
      return invokeStats.toDetypedMap();
   }

   /**
    * Return the current instance pool associate with this container if
    * supported by the underlying container implementation.
    *
    * @return instance pool
    * @throws UnsupportedOperationException if the container does not support an instance pool
    */
   protected InstancePool getInstancePool()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Get current pool size of the pool associated with this container,
    * also known as the method ready count
    *
    * @throws UnsupportedOperationException if the container type does not support an instance pool
    */
   public int getCurrentPoolSize()
   {
      return getInstancePool().getCurrentSize();
   }

   /**
    * Get current pool size of the pool associated with this container.
    *
    * @throws UnsupportedOperationException if the container type does not support an instance pool
    */
   public int getMaxPoolSize()
   {
      return getInstancePool().getMaxSize();
   }

   /**
    * Resets the current invocation stats
    */
   public void resetInvocationStats()
   {
      invokeStats.resetStats();
   }

   /**
    * Sets the class loader for this container. All the classes and resources
    * used by the bean in this container will use this classloader.
    *
    * @param   cl
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }

   /**
    * Returns the classloader for this container.
    *
    * @return
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /** Get the class loader for dynamic class loading via http.
    */
   public ClassLoader getWebClassLoader()
   {
      return webClassLoader;
   }

   /** Set the class loader for dynamic class loading via http.
    */
   public void setWebClassLoader(final ClassLoader webClassLoader)
   {
      this.webClassLoader = webClassLoader;
   }

   /**
    * Sets the meta data for this container. The meta data consists of the
    * properties found in the XML descriptors.
    *
    * @param metaData
    */
   public void setBeanMetaData(final BeanMetaData metaData)
   {
      this.metaData = metaData;
   }

   /**
    * push the ENC onto the stack so that java:comp works
    *
    */
   public void pushENC()
   {
      ENCFactory.pushContextId(getJmxName());
   }

   public void popENC()
   {
      ENCFactory.popContextId();
   }

   /**
    * cleanup ENC on shutdown
    *
    */
   public void cleanENC()
   {
      ENCFactory.getEncById().remove(getJmxName());
   }

   /** Get the components environment context
    * @jmx.managed-attribute
    * @return Environment Context
    */
   public Context getEnvContext() throws NamingException
   {
      pushENC();
      try
      {
         return (Context)new InitialContext().lookup("java:comp/env");
      }
      finally
      {
         popENC();
      }
   }

   /**
    * Returns the metadata of this container.
    *
    * @jmx.managed-attribute
    * @return metaData;
    */
   public BeanMetaData getBeanMetaData()
   {
      return metaData;
   }

   /**
    * Returns the permissions for a method. (a set of roles)
    *
    * @return assemblyDescriptor;
    */
   public Set<Principal> getMethodPermissions(Method m, InvocationType iface)
   {
      Set<Principal> permissions;

      if (methodPermissionsCache.containsKey(m))
      {
         permissions = (Set)methodPermissionsCache.get(m);
      }
      else if (m.equals(EJB_TIMEOUT))
      {
         // No role is required to access the ejbTimeout as this is
         permissions = new HashSet<Principal>();
         permissions.add(AnybodyPrincipal.ANYBODY_PRINCIPAL);
         methodPermissionsCache.put(m, permissions);
      }
      else
      {
         String name = m.getName();
         Class[] sig = m.getParameterTypes();
         Set<String> roles = getBeanMetaData().getMethodPermissions(name, sig, iface);
         permissions = new HashSet<Principal>();
         if (roles != null)
         {
            for (String role : roles)
               permissions.add(new SimplePrincipal(role));
         }
         methodPermissionsCache.put(m, permissions);
      }

      return permissions;
   }

   /**
    * Returns the bean class instance of this container.
    *
    * @return    instance of the Enterprise bean class.
    */
   public Class getBeanClass()
   {
      return beanClass;
   }

   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * This factory style method is speciffically used by a container to supply
    * an implementation of the abstract accessors in EJB2.0, but could be
    * usefull in other situations. This method should ALWAYS be used instead
    * of getBeanClass().newInstance();
    *
    * @return    the new instance
    *
    * @see java.lang.Class#newInstance
    */
   public Object createBeanClassInstance() throws Exception
   {
      return getBeanClass().newInstance();
   }

   /**
    * Sets the codebase of this container.
    *
    * @param   codebase a possibly empty, but non null String with
    *                   a sequence of URLs separated by spaces
    * /
    public void setCodebase(final String codebase)
    {
    if (codebase != null)
    this.codebase = codebase;
    }
    */
   /**
    * Gets the codebase of this container.
    *
    * @return    this container's codebase String, a sequence of URLs
    *            separated by spaces
    * /
    public String getCodebase()
    {
    return codebase;
    }
    */

   /** Build a JMX name using the pattern jboss.j2ee:service=EJB,jndiName=[jndiName]
    where the [jndiName] is either the bean remote home JNDI binding, or
    the local home JNDI binding if the bean has no remote interfaces.
    */
   public ObjectName getJmxName()
   {
      if (jmxName == null)
      {
         BeanMetaData beanMetaData = getBeanMetaData();
         if (beanMetaData == null)
         {
            throw new IllegalStateException("Container metaData is null");
         }

         String jndiName = beanMetaData.getContainerObjectNameJndiName();
         if (jndiName == null)
         {
            throw new IllegalStateException("Container jndiName is null");
         }

         // The name must be escaped since the jndiName may be arbitrary
         String name = BASE_EJB_CONTAINER_NAME + ",jndiName=" + jndiName;
         try
         {
            jmxName = ObjectNameConverter.convert(name);
         }
         catch (MalformedObjectNameException e)
         {
            throw new RuntimeException("Failed to create ObjectName, msg=" + e.getMessage());
         }
      }
      return jmxName;
   }

   /**
    * Creates the single Timer Service for this container if not already created
    *
    * @param pKey Bean id
    * @return Container Timer Service
    * @throws IllegalStateException If the type of EJB is not allowed to use the
    * timer service, or the bean class does not implement javax.ejb.TimedObject
    *
    * @see javax.ejb.EJBContext#getTimerService
    *
    * @jmx.managed-operation
    **/
   public TimerService getTimerService(Object pKey) throws IllegalStateException
   {
      if (this instanceof StatefulSessionContainer)
         throw new IllegalStateException("Statefull Session Beans are not allowed to access the TimerService");

      // Validate that the bean implements the TimedObject interface
      if (TimedObject.class.isAssignableFrom(beanClass) == false)
      {
         // jbcts-381
         return EJBTimerServiceImpl.FOR_NON_TIMED_OBJECT;
      }

      TimerService timerService = null;
      try
      {
         timerService = this.timerService.createTimerService(getJmxName(), pKey, this);
      }
      catch (Exception e)
      {
         throw new EJBException("Could not create timer service", e);
      }
      return timerService;
   }

   /**
    * Removes Timer Service for this container
    *
    * @param pKey Bean id
    * @throws IllegalStateException If the type of EJB is not allowed to use the timer service
    *
    * @jmx.managed-operation
    **/
   public void removeTimerService(Object pKey) throws IllegalStateException
   {
      try
      {
         if (pKey != null)
         {
            // entity bean->remove()
            timerService.removeTimerService(getJmxName(), pKey);
         }
         else
         {
            // container stop, we choose whether active timers
            // should be persisted (default), or not (legacy)
            timerService.removeTimerService(getJmxName(), getBeanMetaData().getTimerPersistence());
         }
      }
      catch (Exception e)
      {
         log.error("Could not remove timer service", e);
      }
   }

   /**
    * Restore any timers previously persisted for this container
    */
   protected void restoreTimers()
   {
      try
      {
         // TODO: this name needs to be externalized
         // pass to the ejb timer service the container ObjectName
         timerService.restoreTimers(getServiceName(), getClassLoader());
      }
      catch (Exception e)
      {
         log.warn("Could not restore ejb timers", e);
      }
   }

   /**
    * The EJBDeployer calls this method.  The EJBDeployer has set
    * all the plugins and interceptors that this bean requires and now proceeds
    * to initialize the chain.  The method looks for the standard classes in
    * the URL, sets up the naming environment of the bean. The concrete
    * container classes should override this method to introduce
    * implementation specific initialization behaviour.
    *
    * @throws Exception    if loading the bean class failed
    *                      (ClassNotFoundException) or setting up "java:"
    *                      naming environment failed (DeploymentException)
    */
   protected void createService() throws Exception
   {
      // Acquire classes from CL
      beanClass = classLoader.loadClass(metaData.getEjbClass());

      if (metaData.getLocalHome() != null)
         localHomeInterface = classLoader.loadClass(metaData.getLocalHome());
      if (metaData.getLocal() != null)
         localInterface = classLoader.loadClass(metaData.getLocal());

      localProxyFactory.setContainer(this);
      localProxyFactory.create();
      if (localHomeInterface != null)
         ejbModule.addLocalHome(this, localProxyFactory.getEJBLocalHome());
      ejbModule.createMissingPermissions(this, metaData);
      // Allow the policy to incorporate the policy configs
      Policy.getPolicy().refresh();
   }

   /**
    * A default implementation of starting the container service.
    * The container registers it's dynamic MBean interface in the JMX base.
    *
    * The concrete container classes should override this method to introduce
    * implementation specific start behaviour.
    *
    * todo implement the service lifecycle methods in an xmbean interceptor so
    * non lifecycle managed ops are blocked when mbean is not started.
    *
    * @throws Exception    An exception that occured during start
    */
   protected void startService() throws Exception
   {
      // Setup "java:comp/env" namespace
      setupEnvironment();

      localProxyFactory.start();
   }

   /**
    * A default implementation of stopping the container service (no-op). The
    * concrete container classes should override this method to introduce
    * implementation specific stop behaviour.
    */
   protected void stopService() throws Exception
   {
      localProxyFactory.stop();
      removeTimerService(null);
      teardownEnvironment();
   }

   /**
    * A default implementation of destroying the container service (no-op).
    * The concrete container classes should override this method to introduce
    * implementation specific destroy behaviour.
    */
   protected void destroyService() throws Exception
   {
      cleanENC();
      localProxyFactory.destroy();
      ejbModule.removeLocalHome(this);

      beanClass = null;
      homeInterface = null;
      remoteInterface = null;
      localHomeInterface = null;
      localInterface = null;
      methodPermissionsCache.clear();
      // InvocationStatistics holds refs to Methods from
      // application classes, so to avoid a classloader
      // leak, lets not just resetStats() but also replace
      // the object
      invokeStats.resetStats(); // in case someone else has a ref
      invokeStats = new InvocationStatistics();
      marshalledInvocationMapping.clear();
   }

   /**
    * This method is called when a method call comes
    * in on the Home object.  The Container forwards this call to the
    * interceptor chain for further processing.
    *
    * @param mi   the object holding all info about this invocation
    * @return     the result of the home invocation
    *
    * @throws Exception
    */
   public abstract Object internalInvokeHome(Invocation mi) throws Exception;

   /**
    * This method is called when a method call comes
    * in on an EJBObject.  The Container forwards this call to the interceptor
    * chain for further processing.
    */
   public abstract Object internalInvoke(Invocation mi) throws Exception;

   abstract Interceptor createContainerInterceptor();

   public abstract void addInterceptor(Interceptor in);

   /** The detached invoker operation.
    *
    * @jmx.managed-operation
    *
    * @param mi - the method invocation context
    * @return the value of the ejb invocation
    * @throws Exception on error
    */
   public Object invoke(Invocation mi) throws Exception
   {
      ClassLoader callerClassLoader = SecurityActions.getContextClassLoader();
      long start = System.currentTimeMillis();
      Method m = null;

      Object type = null;
      String contextID = getJaccContextID();
      try
      {
         pushENC();

         // JBAS-3732 - Remove classloader.equals optimization
         SecurityActions.setContextClassLoader(this.classLoader);

         // Set the JACC context id
         mi.setValue(InvocationKey.JACC_CONTEXT_ID, contextID);
         contextID = SecurityActions.setContextID(contextID);
         // Set the standard JACC policy context handler data is not a SEI msg
         if (mi.getType() != InvocationType.SERVICE_ENDPOINT)
         {
            EJBArgsPolicyContextHandler.setArgs(mi.getArguments());
         }
         else
         {
            SOAPMessage msg = (SOAPMessage)mi.getValue(InvocationKey.SOAP_MESSAGE);
            SOAPMsgPolicyContextHandler.setMessage(msg);
         }
         // Set custom JACC policy handlers
         BeanMetaDataPolicyContextHandler.setMetaData(this.getBeanMetaData());

         // Check against home, remote, localHome, local, getHome,
         // getRemote, getLocalHome, getLocal
         type = mi.getType();

         // stat gathering: concurrent calls
         this.invokeStats.callIn();

         if (type == InvocationType.REMOTE || type == InvocationType.LOCAL ||
         // web service calls come in as "ordinary" application invocations
               type == InvocationType.SERVICE_ENDPOINT)
         {
            if (mi instanceof MarshalledInvocation)
            {
               ((MarshalledInvocation)mi).setMethodMap(marshalledInvocationMapping);

               if (log.isTraceEnabled())
               {
                  log.trace("METHOD REMOTE INVOKE " + mi.getObjectName() + "||" + mi.getMethod().getName() + "||");
               }
            }

            m = mi.getMethod();

            Object obj = internalInvoke(mi);
            return obj;
         }
         else if (type == InvocationType.HOME || type == InvocationType.LOCALHOME)
         {
            if (mi instanceof MarshalledInvocation)
            {

               ((MarshalledInvocation)mi).setMethodMap(marshalledInvocationMapping);

               if (log.isTraceEnabled())
               {
                  log.trace("METHOD HOME INVOKE " + mi.getObjectName() + "||" + mi.getMethod().getName() + "||" + mi.getArguments().toString());
               }
            }
            m = mi.getMethod();

            Object obj = internalInvokeHome(mi);
            return obj;
         }
         else
         {
            throw new MBeanException(new IllegalArgumentException("Unknown invocation type: " + type));
         }
      }
      /**
       * Having to catch this exception here in case can not
       * unmarshall arguments, values, etc.  Then, convert to
       * UnmarshalException as defined by spec (JBAS-2999)
       */
      catch (JBossLazyUnmarshallingException e)
      {
         InvocationType calltype = mi.getType();
         boolean isLocal = calltype == InvocationType.LOCAL || calltype == InvocationType.LOCALHOME;

         // handle unmarshalling exception which should only come if problem unmarshalling
         // invocation payload, arguments, or value on remote end.
         if (isLocal)
         {
            throw new EJBException("UnmarshalException", e);
         }
         else
         {
            throw new MarshalException("MarshalException", e);
         }
      }
      finally
      {
         if (m != null)
         {
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            this.invokeStats.updateStats(m, elapsed);
         }

         // stat gathering: concurrent calls
         this.invokeStats.callOut();

         popENC();

         // Restore the incoming class loader
         SecurityActions.setContextClassLoader(callerClassLoader);

         // Restore the incoming context id
         contextID = SecurityActions.setContextID(contextID);

         if (mi.getType() == InvocationType.SERVICE_ENDPOINT)
         {
            // Remove msg from ThreadLocal to prevent leakage into the thread pool
            SOAPMsgPolicyContextHandler.setMessage(null);
         }
         else
         {
            // Remove args from ThreadLocal to prevent leakage into the thread pool
            EJBArgsPolicyContextHandler.setArgs(null);
         }

         // Remove metadata from ThreadLocal to prevent leakage into the thread pool
         BeanMetaDataPolicyContextHandler.setMetaData(null);
      }
   }

   // Private -------------------------------------------------------

   /**
    * This method sets up the naming environment of the bean.
    * We create the java:comp/env namespace with properties, EJB-References,
    * and DataSource ressources.
    */
   private void setupEnvironment() throws Exception
   {
      BeanMetaData beanMetaData = getBeanMetaData();

      // debug
      log.debug("Begin java:comp/env for EJB: " + beanMetaData.getEjbName());
      ClassLoader tcl = SecurityActions.getContextClassLoader();
      log.debug("TCL: " + tcl);

      ORB orb = null;
      HandleDelegate hd = null;
      try
      {
         orb = (ORB)server.getAttribute(ORB_NAME, "ORB");
         hd = (HandleDelegate)server.getAttribute(ORB_NAME, "HandleDelegate");
      }
      catch (Throwable t)
      {
         log.debug("Unable to retrieve orb" + t.toString());
      }

      // Since the BCL is already associated with this thread we can start
      // using the java: namespace directly
      Context ctx = (Context)new InitialContext().lookup("java:comp");
      Object id = ENCFactory.getCurrentId();
      log.debug("Using java:comp using id=" + id);

      // Bind the orb
      if (orb != null)
      {
         NonSerializableFactory.rebind(ctx, "ORB", orb);
         log.debug("Bound java:comp/ORB for EJB: " + getBeanMetaData().getEjbName());

         NonSerializableFactory.rebind(ctx, "HandleDelegate", hd);
         log.debug("Bound java:comp/HandleDelegate for EJB: " + getBeanMetaData().getEjbName());
      }

      // JTA links
      ctx.bind("TransactionSynchronizationRegistry", new LinkRef("java:TransactionSynchronizationRegistry"));
      log.debug("Linked java:comp/TransactionSynchronizationRegistry to JNDI name: java:TransactionSynchronizationRegistry");

      Context envCtx = ctx.createSubcontext("env");

      // Bind environment properties
      {
         Iterator i = beanMetaData.getEnvironmentEntries();
         while (i.hasNext())
         {
            EnvEntryMetaData entry = (EnvEntryMetaData)i.next();
            log.debug("Binding env-entry: " + entry.getName() + " of type: " + entry.getType() + " to value:" + entry.getValue());

            EnvEntryBinder.bindEnvEntry(envCtx, entry);
         }
      }

      // Bind EJB references
      {
         Iterator i = beanMetaData.getEjbReferences();
         while (i.hasNext())
         {
            EjbRefMetaData ref = (EjbRefMetaData)i.next();
            log.debug("Binding an EJBReference " + ref.getName());

            if (ref.getLink() != null)
            {
               // Internal link
               String linkName = ref.getLink();
               String jndiName = ref.getJndiName();
               log.debug("Binding " + ref.getName() + " to ejb-link: " + linkName + " -> " + jndiName);

               if (jndiName == null)
               {
                  String msg = "Failed to resolve ejb-link: " + linkName + " from ejb-ref: " + ref.getName() + " in ejb: " + beanMetaData.getEjbName();
                  throw new DeploymentException(msg);
               }

               Util.bind(envCtx, ref.getName(), new LinkRef(jndiName));

            }
            else
            {
               // Get the invoker specific ejb-ref mappings
               Iterator it = beanMetaData.getInvokerBindings();
               Reference reference = null;
               while (it.hasNext())
               {
                  String invokerBinding = (String)it.next();
                  // Check for an invoker level jndi-name
                  String name = ref.getInvokerBinding(invokerBinding);
                  // Check for an global jndi-name
                  if (name == null)
                     name = ref.getJndiName();
                  if (name == null)
                  {
                     throw new DeploymentException("ejb-ref " + ref.getName() + ", expected either ejb-link in ejb-jar.xml or " + "jndi-name in jboss.xml");
                  }

                  StringRefAddr addr = new StringRefAddr(invokerBinding, name);
                  log.debug("adding " + invokerBinding + ":" + name + " to Reference");

                  if (reference == null)
                  {
                     reference = new Reference("javax.naming.LinkRef", ENCThreadLocalKey.class.getName(), null);
                  }
                  reference.add(addr);
               }

               // If there were invoker bindings create bind the reference
               if (reference != null)
               {
                  if (ref.getJndiName() != null)
                  {
                     // Add default for the bean level ejb-ref/jndi-name
                     StringRefAddr addr = new StringRefAddr("default", ref.getJndiName());
                     reference.add(addr);
                  }
                  if (reference.size() == 1 && reference.get("default") == null)
                  {
                     /* There is only one invoker binding and its not default so
                      create a default binding to allow the link to have a value
                      when accessed without an invoker active.
                      */
                     StringRefAddr addr = (StringRefAddr)reference.get(0);
                     String target = (String)addr.getContent();
                     StringRefAddr addr1 = new StringRefAddr("default", target);
                     reference.add(addr1);
                  }
                  Util.bind(envCtx, ref.getName(), reference);
               }
               else
               {
                  // Bind the bean level ejb-ref/jndi-name
                  if (ref.getJndiName() == null)
                  {
                     throw new DeploymentException("ejb-ref " + ref.getName() + ", expected either ejb-link in ejb-jar.xml " + "or jndi-name in jboss.xml");
                  }
                  Util.bind(envCtx, ref.getName(), new LinkRef(ref.getJndiName()));
               }
            }
         }
      }

      // Bind Local EJB references
      {
         Iterator i = beanMetaData.getEjbLocalReferences();
         while (i.hasNext())
         {
            EjbLocalRefMetaData ref = (EjbLocalRefMetaData)i.next();
            String refName = ref.getName();
            log.debug("Binding an EJBLocalReference " + ref.getName());

            if (ref.getLink() != null)
            {
               // Internal link
               log.debug("Binding " + refName + " to bean source: " + ref.getLink());

               String jndiName = ref.getJndiName();

               Util.bind(envCtx, ref.getName(), new LinkRef(jndiName));
            }
            else
            {
               // Bind the bean level ejb-local-ref/local-jndi-name
               if (ref.getJndiName() == null)
               {
                  throw new DeploymentException("ejb-local-ref " + ref.getName() + ", expected either ejb-link in ejb-jar.xml " + "or local-jndi-name in jboss.xml");
               }
               Util.bind(envCtx, ref.getName(), new LinkRef(ref.getJndiName()));
            }
         }
      }

      // Bind service references
      {
         ClassLoader loader = unit.getClassLoader();
         UnifiedVirtualFile vfsRoot = new VirtualFileAdaptor(unit.getRoot());
         Iterator<ServiceReferenceMetaData> serviceReferences = beanMetaData.getServiceReferences();
         if (serviceReferences != null)
         {
            while (serviceReferences.hasNext())
            {
               ServiceReferenceMetaData sref = serviceReferences.next();
               String refName = sref.getServiceRefName();
               new ServiceReferenceHandler().bindServiceRef(envCtx, refName, vfsRoot, loader, sref);
            }
         }
      }

      // Bind resource references
      {
         Iterator i = beanMetaData.getResourceReferences();

         // let's play guess the cast game ;)  New metadata should fix this.
         ApplicationMetaData application = beanMetaData.getApplicationMetaData();

         while (i.hasNext())
         {
            ResourceRefMetaData ref = (ResourceRefMetaData)i.next();

            String resourceName = ref.getResourceName();
            String finalName = application.getResourceByName(resourceName);
            String resType = ref.getType();
            // If there was no resource-manager specified then an immeadiate
            // jndi-name or res-url name should have been given
            if (finalName == null)
               finalName = ref.getJndiName();

            if (finalName == null && resType.equals("java.net.URL") == false)
            {
               // the application assembler did not provide a resource manager
               // if the type is javax.sql.Datasoure use the default one

               if (ref.getType().equals("javax.sql.DataSource"))
               {
                  // Go through JNDI and look for DataSource - use the first one
                  Context dsCtx = new InitialContext();
                  try
                  {
                     // Check if it is available in JNDI
                     dsCtx.lookup("java:/DefaultDS");
                     finalName = "java:/DefaultDS";
                  }
                  catch (Exception e)
                  {
                     log.debug("failed to lookup DefaultDS; ignoring", e);
                  }
                  finally
                  {
                     dsCtx.close();
                  }
               }

               // Default failed? Warn user and move on
               // POTENTIALLY DANGEROUS: should this be a critical error?
               if (finalName == null)
               {
                  log.warn("No resource manager found for " + ref.getResourceName());
                  continue;
               }
            }

            if (resType.equals("java.net.URL"))
            {
               // URL bindings
               if (ref.getResURL() != null)
               {
                  // The URL string was given by the res-url
                  log.debug("Binding URL: " + ref.getRefName() + " to JDNI ENC as: " + ref.getResURL());
                  URL resURL = new URL(ref.getResURL());
                  Util.bind(envCtx, ref.getRefName(), resURL);
               }
               else
               {
                  log.debug("Binding URL: " + ref.getRefName() + " to: " + finalName);
                  Object bind = null;
                  if (ref.getJndiName() != null)
                  {
                     // Was the url given as a jndi-name reference to link to it
                     bind = new LinkRef(finalName);
                  }
                  else
                  {
                     // The url string was given via a resource-name mapping
                     bind = new URL(finalName);
                  }
                  Util.bind(envCtx, ref.getRefName(), bind);
               }
            }
            else
            {
               // Resource Manager bindings, should validate the type...
               log.debug("Binding resource manager: " + ref.getRefName() + " to JDNI ENC as: " + finalName);
               Util.bind(envCtx, ref.getRefName(), new LinkRef(finalName));
            }
         }
      }

      // Bind resource env references
      {
         Iterator i = beanMetaData.getResourceEnvReferences();
         while (i.hasNext())
         {
            ResourceEnvRefMetaData resRef = (ResourceEnvRefMetaData)i.next();
            String encName = resRef.getRefName();
            String jndiName = resRef.getJndiName();
            // Should validate the type...
            log.debug("Binding env resource: " + encName + " to JDNI ENC as: " + jndiName);
            Util.bind(envCtx, encName, new LinkRef(jndiName));
         }
      }

      // Bind message destination references
      {
         Iterator i = beanMetaData.getMessageDestinationReferences();

         while (i.hasNext())
         {
            MessageDestinationRefMetaData ref = (MessageDestinationRefMetaData)i.next();

            String refName = ref.getRefName();
            String jndiName = ref.getJNDIName();
            String link = ref.getLink();
            if (link != null)
            {
               if (jndiName == null)
               {
                  MessageDestinationMetaData messageDestination = getMessageDestination(link);
                  if (messageDestination == null)
                     throw new DeploymentException("message-destination-ref '" + refName + "' message-destination-link '" + link
                           + "' not found and no jndi-name in jboss.xml");
                  else
                  {
                     String linkJNDIName = messageDestination.getJndiName();
                     if (linkJNDIName == null)
                        log.warn("message-destination '" + link + "' has no jndi-name in jboss.xml");
                     else
                        jndiName = linkJNDIName;
                  }
               }
               else
                  log.warn("message-destination-ref '" + refName + "' ignoring message-destination-link '" + link + "' because it has a jndi-name in jboss.xml");
            }
            else if (jndiName == null)
               throw new DeploymentException("message-destination-ref '" + refName + "' has no message-destination-link in ejb-jar.xml and no jndi-name in jboss.xml");
            Util.bind(envCtx, refName, new LinkRef(jndiName));
         }
      }

      // Create a java:comp/env/security/security-domain link to the container
      // or application security-domain if one exists so that access to the
      // security manager can be made without knowing the global jndi name.

      String securityDomain = metaData.getContainerConfiguration().getSecurityDomain();
      if (securityDomain == null)
         securityDomain = metaData.getApplicationMetaData().getSecurityDomain();
      if (securityDomain != null)
      {
         //JBAS-6060: Tolerate a Security Domain configuration without the java:/jaas prefix
         if(securityDomain.startsWith(SecurityConstants.JAAS_CONTEXT_ROOT) == false)
            securityDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + securityDomain;

         log.debug("Binding securityDomain: " + securityDomain + " to JDNI ENC as: security/security-domain");

         Util.bind(envCtx, "security/security-domain", new LinkRef(securityDomain));
         Util.bind(envCtx, "security/subject", new LinkRef(securityDomain + "/subject"));
         Util.bind(envCtx, "security/realmMapping", new LinkRef(securityDomain + "/realmMapping"));
         Util.bind(envCtx, "security/authorizationMgr", new LinkRef(securityDomain + "/authorizationMgr"));
      }

      log.debug("End java:comp/env for EJB: " + beanMetaData.getEjbName());
   }

   public MessageDestinationMetaData getMessageDestination(String link)
   {
      return EjbUtil50.findMessageDestination(null, unit, link);
   }

   /**
    *The <code>teardownEnvironment</code> method unbinds everything from
    * the comp/env context.  It would be better do destroy the env context
    * but destroyContext is not currently implemented..
    *
    * @exception Exception if an error occurs
    */
   private void teardownEnvironment() throws Exception
   {
      Context ctx = (Context)new InitialContext().lookup("java:comp");
      ctx.unbind("env");
      log.debug("Removed bindings from java:comp/env for EJB: " + getBeanMetaData().getEjbName());
      ctx.unbind("TransactionSynchronizationRegistry");
      log.debug("Unbound java:comp/TransactionSynchronizationRegistry for EJB: " + getBeanMetaData().getEjbName());
      try
      {
         NonSerializableFactory.unbind("ORB");
         log.debug("Unbound java:comp/ORB for EJB: " + getBeanMetaData().getEjbName());

         NonSerializableFactory.unbind("HandleDelegate");
         log.debug("Unbound java:comp/HandleDelegate for EJB: " + getBeanMetaData().getEjbName());
      }
      catch (NamingException ignored)
      {
      }
   }

   /**
    * The base class for container interceptors.
    *
    * <p>
    * All container interceptors perform the same basic functionality
    * and only differ slightly.
    */
   protected abstract class AbstractContainerInterceptor implements Interceptor
   {
      protected final Logger log = Logger.getLogger(this.getClass());

      public void setContainer(Container con)
      {
      }

      public void setNext(Interceptor interceptor)
      {
      }

      public Interceptor getNext()
      {
         return null;
      }

      public void create()
      {
      }

      public void start()
      {
      }

      public void stop()
      {
      }

      public void destroy()
      {
      }

      protected void rethrow(Exception e) throws Exception
      {
         if (e instanceof IllegalAccessException)
         {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
         }
         else if (e instanceof InvocationTargetException)
         {
            Throwable t = ((InvocationTargetException)e).getTargetException();

            if (t instanceof EJBException)
            {
               throw (EJBException)t;
            }
            else if (t instanceof Exception)
            {
               throw (Exception)t;
            }
            else if (t instanceof Error)
            {
               throw (Error)t;
            }
            else
            {
               throw new NestedError("Unexpected Throwable", t);
            }
         }

         throw e;
      }

      // Monitorable implementation ------------------------------------

      public void sample(Object s)
      {
         // Just here to because Monitorable request it but will be removed soon
      }

      public Map retrieveStatistic()
      {
         return null;
      }

      public void resetStatistic()
      {
      }
   }

   /** Perform the MBeanServer.invoke op in a PrivilegedExceptionAction if
    * running with a security manager.
    */
   class MBeanServerAction implements PrivilegedExceptionAction
   {
      private ObjectName target;
      String method;
      Object[] args;
      String[] sig;

      MBeanServerAction()
      {
      }

      MBeanServerAction(ObjectName target, String method, Object[] args, String[] sig)
      {
         this.target = target;
         this.method = method;
         this.args = args;
         this.sig = sig;
      }

      public Object run() throws Exception
      {
         Object rtnValue = server.invoke(target, method, args, sig);
         return rtnValue;
      }

      Object invoke(ObjectName target, String method, Object[] args, String[] sig) throws Exception
      {
         SecurityManager sm = System.getSecurityManager();
         Object rtnValue = null;
         if (sm == null)
         {
            // Direct invocation on MBeanServer
            rtnValue = server.invoke(target, method, args, sig);
         }
         else
         {
            try
            {
               // Encapsulate the invocation in a PrivilegedExceptionAction
               MBeanServerAction action = new MBeanServerAction(target, method, args, sig);
               rtnValue = AccessController.doPrivileged(action);
            }
            catch (PrivilegedActionException e)
            {
               Exception ex = e.getException();
               throw ex;
            }
         }
         return rtnValue;
      }
   }
}
