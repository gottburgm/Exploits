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
package org.jboss.web.tomcat.service;

import org.apache.InstanceManager;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.MappedReferenceMetaDataResolverDeployer;
import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.ejb3.javaee.AbstractJavaEEComponent;
import org.jboss.ejb3.javaee.SimpleJavaEEModule;
import org.jboss.ejb3.vfs.impl.vfs2.VirtualFileWrapper;
import org.jboss.ejb3.vfs.spi.VirtualFile;
import org.jboss.injection.*;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.web.WebApplication;
import org.jboss.web.tomcat.service.injection.TomcatInjectionUtils;
import org.jboss.web.tomcat.service.injection.WebEJBHandler;
import org.jboss.web.tomcat.service.injection.WebResourceHandler;
import org.jboss.web.tomcat.service.injection.WebServiceRefInjectionHandler;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The TomcatInjectionContainer.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 112152 $
 */
public class TomcatInjectionContainer extends AbstractJavaEEComponent implements ExtendedInjectionContainer, InstanceManager
{
   private static final Logger log = Logger.getLogger(TomcatInjectionContainer.class);

   private static class EncMap extends HashMap<String, EncInjector>
   {
      private HashMap<String, EncInjector> added;

      public void recordAdded()
      {
         added = new HashMap<String, EncInjector>();
      }

      public void clearAdded()
      {
         added = null;
      }

      public Map<String, EncInjector> getAdded()
      {
         return added;
      }

      @Override
      public EncInjector put(String key, EncInjector value)
      {
         if (added != null)
            added.put(key, value);
         return super.put(key, value);
      }

      @Override
      public void putAll(Map<? extends String, ? extends EncInjector> m)
      {
         if (added != null)
            added.putAll(m);
         super.putAll(m);
      }
   }

   protected EncMap encInjectors = new EncMap();
   protected Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();

   protected DependencyPolicy dependencyPolicy = new JBoss5DependencyPolicy(this);
   protected Collection<InjectionHandler<Environment>> handlers;
   protected DeploymentUnit unit;
   protected ClassLoader webLoader;
   protected WebApplication appInfo;
   protected JBossWebMetaData webDD;
   protected org.apache.catalina.Context catalinaContext;
   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;
   private DeploymentEndpointResolver deploymentEndpointResolver;
   private Map<String, ContainerDependencyMetaData> endpointMap;
   private static final Set<String> dynamicClassLoaders = new HashSet<String>();
   private static final Properties restrictedFilters = new Properties();
   private static final Properties restrictedListeners = new Properties();
   private static final Properties restrictedServlets = new Properties();

   static
   {
      try
      {
         InputStream is = TomcatInjectionContainer.class.getClassLoader().getResourceAsStream("org/apache/catalina/core/RestrictedServlets.properties");
         if (is != null)
         {
            restrictedServlets.load(is);
         }
         else
         {
            log.error("Could not load org/apache/catalina/core/RestrictedServlets.properties");
         }
      }
      catch (IOException e)
      {
         log.error("Error reading org/apache/catalina/core/RestrictedServlets.properties", e);
      }

      try
      {
         InputStream is = TomcatInjectionContainer.class.getClassLoader().getResourceAsStream("org/apache/catalina/core/RestrictedListeners.properties");
         if (is != null)
         {
            restrictedListeners.load(is);
         }
         else
         {
            log.error("Could not load org/apache/catalina/core/RestrictedListeners.properties");
         }
      }
      catch (IOException e)
      {
         log.error("Error reading org/apache/catalina/core/RestrictedListeners.properties", e);
      }
      try
      {
         InputStream is = TomcatInjectionContainer.class.getClassLoader().getResourceAsStream("org/apache/catalina/core/RestrictedFilters.properties");
         if (is != null)
         {
            restrictedFilters.load(is);
         }
         else
         {
            log.error("Could not load org/apache/catalina/core/RestrictedFilters.properties");
         }
      }
      catch (IOException e)
      {
         log.error("Error reading org/apache/catalina/core/RestrictedFilters.properties", e);
      }

      // 
      dynamicClassLoaders.add("org.apache.jasper.servlet.JasperLoader");
   }

   public TomcatInjectionContainer(WebApplication appInfo, DeploymentUnit unit, org.apache.catalina.Context catalinaContext,
         PersistenceUnitDependencyResolver resolver)
   {
      super(new SimpleJavaEEModule(appInfo.getName()));

      this.unit = unit;
      this.appInfo = appInfo;
      this.catalinaContext = catalinaContext;
      this.persistenceUnitDependencyResolver = resolver;
      this.deploymentEndpointResolver = unit.getAttachment(DeploymentEndpointResolver.class);
      this.endpointMap = unit.getTopLevel().getAttachment(MappedReferenceMetaDataResolverDeployer.ENDPOINT_MAP_KEY, Map.class);

      this.webDD = unit.getAttachment(JBossWebMetaData.class);
      assert this.webDD != null : "webDD is null (no JBossWebMetaData attachment in VFSDeploymentUnit)";

   }

   private void checkAccess(Class<?> clazz)
   {
      if (catalinaContext.getPrivileged())
         return;
      if (Filter.class.isAssignableFrom(clazz))
      {
         checkAccess(clazz, restrictedFilters);
      }
      else if (Servlet.class.isAssignableFrom(clazz))
      {
         checkAccess(clazz, restrictedServlets);
      }
      else
      {
         checkAccess(clazz, restrictedListeners);
      }
   }

   private void checkAccess(Class<?> clazz, Properties restricted)
   {
      while (clazz != null)
      {
         if ("restricted".equals(restricted.getProperty(clazz.getName())))
         {
            throw new SecurityException("Restricted class: " + clazz.getName());
         }
         clazz = clazz.getSuperclass();
      }
   }

   public Environment getEnvironmentRefGroup()
   {
      return webDD.getJndiEnvironmentRefsGroup();
   }

   public Object newInstance(String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
         ClassNotFoundException
   {
      ClassLoader loader = catalinaContext.getLoader().getClassLoader();
      return newInstance(className, loader);
   }

   public Object newInstance(String className, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException,
         InstantiationException, ClassNotFoundException
   {
      Class<?> clazz = classLoader.loadClass(className);
      checkAccess(clazz);
      Object instance = clazz.newInstance();
      newInstance(instance);
      return instance;
   }

   public void newInstance(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      processInjectors(instance);
      if (!catalinaContext.getIgnoreAnnotations())
      {
         processDynamicBeanAnnotations(instance);
         postConstruct(instance);
      }
   }

   public void destroyInstance(Object instance) throws IllegalAccessException, InvocationTargetException
   {
      if (!catalinaContext.getIgnoreAnnotations())
      {
         preDestroy(instance);
      }
   }

   /**
    * Process the @PostConstruct annotation
    * 
    * @param object the Object
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   public void postConstruct(Object object) throws IllegalAccessException, InvocationTargetException
   {
      if (webDD.getPostConstructs() == null)
         return;
      for (LifecycleCallbackMetaData metaData : webDD.getPostConstructs())
      {
         try
         {
            Class<?> clazz = webLoader.loadClass(metaData.getClassName());
            if (clazz.isAssignableFrom(object.getClass()))
            {
               // process LifecycleCallbackMetaData
               processesLifecycleCallbackMetaData(object, metaData);
            }
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException("Error invoking postConstruct method: " + metaData.getMethodName(), e);
         }
      }
   }

   /**
    * Process the @PreDestroy annotation.
    * 
    * @param object the Object
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   public void preDestroy(Object object) throws IllegalAccessException, InvocationTargetException
   {
      if (webDD.getPreDestroys() == null)
         return;
      for (LifecycleCallbackMetaData metaData : webDD.getPreDestroys())
      {
         try
         {
            //  
            Class<?> clazz = webLoader.loadClass(metaData.getClassName());
            if (clazz.isAssignableFrom(object.getClass()))
            {
               // process LifecycleCallbackMetaData
               processesLifecycleCallbackMetaData(object, metaData);
            }
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException("Error invoking postConstruct method: " + metaData.getMethodName(), e);
         }
      }
   }

   /**
    * Process the injectors. 
    * 
    * @param object the object
    */
   public void processInjectors(Object object)
   {
      final boolean trace = log.isTraceEnabled();

      Map<AccessibleObject, Injector> injectors = getEncInjectionsForObject(object);
      if (injectors == null || injectors.size() == 0)
      {
         if (trace)
            log.trace("-- no injectors found for: " + object);
         return;
      }
      if (trace)
         log.trace("-- doing injections for: " + object);
      for (Injector injector : injectors.values())
      {
         injector.inject(object);
      }
   }

   /**
    * Process annotations for dynamic beans only.
    * 
    * @param object the object
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    * @throws NamingException
    */
   protected void processDynamicBeanAnnotations(Object object) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      // Only process annotation on dynamic beans
      if (isDynamicBean(object))
         processAnnotations(object);
   }

   /**
    * When we get here, we are assuming that any XML defined injection has been already done.
    * We will set up more here if the class being processed is a dynamic class.
    *
    * @param object the Object
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    * @throws NamingException
    */
   public void processAnnotations(Object object) throws IllegalAccessException, InvocationTargetException, NamingException
   {
      final boolean trace = log.isTraceEnabled();

      Map<AccessibleObject, Injector> injectors = getEncInjectionsForClass(object.getClass(), true);
      if (injectors == null)
      {
         if (trace)
            log.trace("**************** Processing annotations for: " + object.getClass().getName());

         encInjectors.recordAdded();

         // Populate the encInjections
         TomcatInjectionUtils.processDynamicBeanAnnotations(this, handlers, object.getClass());

         // only execute injectors that were added additionally
         if (encInjectors.getAdded().size() > 0)
         {
            for (EncInjector encInjector : encInjectors.getAdded().values())
            {
               encInjector.inject(this);
            }
            encInjectors.clearAdded();
         }

         // Process the injectors
         processInjectors(object);
      }
   }

   /**
    * Get the the Injectors for a object and it's superclass.
    * 
    * @param object
    * @return
    */
   private Map<AccessibleObject, Injector> getEncInjectionsForObject(Object object)
   {
      if (object == null || object.getClass() == Object.class)
         return null;

      return getEncInjectionsForClass(object.getClass(), isDynamicBean(object));
   }

   private Map<AccessibleObject, Injector> getEncInjectionsForClass(Class<?> clazz, boolean isDynamic)
   {
      if (clazz == null || clazz == Object.class)
         return null;

      Map<AccessibleObject, Injector> injectors = encInjections.get(clazz.getName());
      Map<AccessibleObject, Injector> additionalInjectors = null;
      if (clazz.getSuperclass() != null && !isDynamic)
         additionalInjectors = getEncInjectionsForClass(clazz.getSuperclass(), isDynamic);

      if (injectors == null)
         return additionalInjectors;
      else if (additionalInjectors != null)
         injectors.putAll(additionalInjectors);

      return injectors;
   }

   /**
    * Check if the class is a dynamic bean.
    * 
    * @param object the Object
    * @return
    */
   private boolean isDynamicBean(Object object)
   {
      if (object == null)
         throw new IllegalArgumentException("null class");

      ClassLoader loader = object.getClass().getClassLoader();
      if (loader == null)
         return false;
      // Check if the object was loaded by a dynamic class loader (e.g. Jasper)
      String classLoaderName = loader.getClass().getName();
      if (dynamicClassLoaders.contains(classLoaderName))
         return true;

      return false;
   }

   public void populateEnc(ClassLoader loader)
   {
      for (EncInjector injector : encInjectors.values())
      {
         injector.inject(this);
      }
   }

   private void processesLifecycleCallbackMetaData(Object object, LifecycleCallbackMetaData lifeCycleMetaData) throws IllegalAccessException,
         InvocationTargetException
   {
      final Object args[] = null;
      Class<?> clazz = object.getClass();
      Method method = null;
      // Also check superClasses for private members
      while (clazz != null)
      {
         for (Method m : clazz.getDeclaredMethods())
         {
            if (m.getName().equals(lifeCycleMetaData.getMethodName()) && m.getParameterTypes().length == 0)
            {
               method = m;
            }
         }
         if (method != null)
            break;
         clazz = clazz.getSuperclass();
      }
      if (method == null)
         throw new IllegalStateException("Method: " + lifeCycleMetaData.getMethodName() + " not found.");

      boolean accessible = method.isAccessible();
      try
      {
         // Finally invoke the method
         method.setAccessible(true);
         method.invoke(object, args);
      }
      finally
      {
         method.setAccessible(accessible);
      }
   }

   /**
    * Process the meta data. There is no introspection needed, as the annotations 
    * were already processed. The handlers add the EjbEncInjectors to encInjectors.
    * Other injectors are added to the encInjections map.
    * <p/>
    * This must be called before container is registered with any microcontainer
    *
    */
   public void processMetadata()
   {
      // 
      InjectionHandler<Environment> webEjbHandler = new WebEJBHandler<Environment>(webDD, deploymentEndpointResolver, endpointMap, unit.getRelativePath());

      // todo injection handlers should be pluggable from XML
      handlers = new ArrayList<InjectionHandler<Environment>>();
      handlers.add(webEjbHandler);
      handlers.add(new DependsHandler<Environment>());
      handlers.add(new PersistenceContextHandler<Environment>());
      handlers.add(new PersistenceUnitHandler<Environment>());
      handlers.add(new WebResourceHandler<Environment>());
      handlers.add(new WebServiceRefInjectionHandler<Environment>());

      ClassLoader old = Thread.currentThread().getContextClassLoader();
      ClassLoader webLoader = getClassloader();
      Thread.currentThread().setContextClassLoader(webLoader);
      try
      {
         for (InjectionHandler<Environment> handler : handlers)
            handler.loadXml(webDD.getJndiEnvironmentRefsGroup(), this);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      return encInjections;
   }

   // EncInjectors/Handlers may need to add extra instance injectors
   public List<Injector> getInjectors()
   {
      return new ArrayList<Injector>(); // no equivalent in WAR
   }

   public VirtualFile getRootFile()
   {
      if (unit instanceof VFSDeploymentUnit)
         return new VirtualFileWrapper(((VFSDeploymentUnit)unit).getRoot());
      else
         return null;
   }

   public String getIdentifier()
   {
      return unit.getSimpleName();
   }

   public String getDeploymentDescriptorType()
   {
      return "web.xml";
   }

   public ClassLoader getClassloader()
   {
      return webLoader;
   }

   public void setClassLoader(ClassLoader loader)
   {
      this.webLoader = loader;
   }

   public Context getEnc()
   {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(getClassloader());
         try
         {
            return (Context)new InitialContext().lookup("java:comp");
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   public boolean hasJNDIBinding(String jndiName)
   {
      return false;
   }

   public Container resolveEjbContainer(String link, Class<?> businessIntf)
   {
      return null;
   }

   public Container resolveEjbContainer(Class<?> businessIntf) throws NameNotFoundException
   {
      return null;
   }

   public String getEjbJndiName(Class<?> businessIntf) throws NameNotFoundException
   {
      throw new IllegalStateException("Resolution should not happen via injection container");
   }

   public String getEjbJndiName(String link, Class<?> businessIntf)
   {
      throw new IllegalStateException("Resolution should not happen via injection container");
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz)
   {
      return clazz.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Method method)
   {
      return method.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Method method)
   {
      return method.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Class<?> clazz, Field field)
   {
      return field.getAnnotation(annotationType);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType, Field field)
   {
      return field.getAnnotation(annotationType);
   }

   public DependencyPolicy getDependencyPolicy()
   {
      return dependencyPolicy;
   }

   public String resolveMessageDestination(String link)
   {
      throw new IllegalStateException("Resolution should not happen via injection container");
   }

   public String resolvePersistenceUnitSupplier(String persistenceUnitName)
   {
      return persistenceUnitDependencyResolver.resolvePersistenceUnitSupplier(unit, persistenceUnitName);
   }
}