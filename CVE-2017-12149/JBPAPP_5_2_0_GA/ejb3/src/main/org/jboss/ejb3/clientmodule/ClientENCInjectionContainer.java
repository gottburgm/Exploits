/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.clientmodule;

import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb3.*;
import org.jboss.ejb3.deployers.JBoss5DeploymentScope;
import org.jboss.ejb3.deployers.JBoss5DeploymentUnit;
import org.jboss.ejb3.deployers.JBossASDepdencyPolicy;
import org.jboss.ejb3.enc.DeploymentEjbResolver;
import org.jboss.ejb3.enc.MessageDestinationResolver;
import org.jboss.ejb3.javaee.AbstractJavaEEComponent;
import org.jboss.ejb3.javaee.SimpleJavaEEModule;
import org.jboss.ejb3.vfs.spi.UnifiedVirtualFileFactory;
import org.jboss.ejb3.vfs.spi.VirtualFile;
import org.jboss.injection.*;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.logging.Logger;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferencesMetaData;
import org.jboss.metadata.serviceref.ServiceReferenceHandler;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class builds up the java:comp namespace for JavaEE 5 application clients.
 * It uses the existing injection framework to get this done.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author adrian@jboss.org
 * @version $Revision: 104907 $
 */
public class ClientENCInjectionContainer extends AbstractJavaEEComponent implements ExtendedInjectionContainer
{
   private static final Logger log = Logger.getLogger(ClientENCInjectionContainer.class);

   private VFSDeploymentUnit deploymentUnit;
   private DeploymentUnit ejb3Unit;
   private JBossClientMetaData clientMetaData;
   private Class<?> mainClass;
   private String applicationClientName;
   private ClassLoader classLoader;

   // TODO: remove injectors, these are not supported
   private List<Injector> injectors = new ArrayList<Injector>();
   private Map<String, Map<AccessibleObject, Injector>> encInjections = new HashMap<String, Map<AccessibleObject, Injector>>();
   private Map<String, EncInjector> encInjectors = new HashMap<String, EncInjector>();

   private Context enc;

   private DeploymentEjbResolver ejbResolver;
   private DeploymentScope deploymentScope;
   private ObjectName objectName;
   private DependencyPolicy dependencyPolicy = new JBossASDepdencyPolicy(this);

   private MessageDestinationResolver messageDestinationResolver;
   
   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;

   public ClientENCInjectionContainer(VFSDeploymentUnit unit, JBossClientMetaData xml, Class<?> mainClass, String applicationClientName, ClassLoader classLoader,
         Context encCtx, PersistenceUnitDependencyResolver persistenceUnitDependencyResolver) throws NamingException
   {
      super(new SimpleJavaEEModule((unit.getParent() != null ? unit.getParent().getSimpleName() : null), unit.getSimpleName()));
      if (mainClass == null)
         throw new NullPointerException("mainClass is mandatory");
      if (applicationClientName == null)
         throw new NullPointerException("applicationClientName is mandatory");
      if (classLoader == null)
         throw new NullPointerException("classLoader is mandatory");

      this.deploymentUnit = unit;
      this.ejb3Unit = new JBoss5DeploymentUnit(unit);
      this.clientMetaData = xml;
      this.mainClass = mainClass;
      this.applicationClientName = applicationClientName;
      this.classLoader = classLoader;

      this.enc = encCtx;

      /*
       EAR ear = null;

       if (di.parent != null)
       {
       if (di.parent.shortName.endsWith(".ear") || di.parent.shortName.endsWith(".ear/"))
       {
       synchronized (di.parent.context)
       {
       ear = (EAR) di.parent.context.get("EJB3_EAR_METADATA");
       if (ear == null)
       {
       ear = new JmxEARImpl(di.parent.shortName);
       di.parent.context.put("EJB3_EAR_METADATA", ear);
       }
       }
       }
       }
       */

      //DeploymentScope scope = null;
      if (unit.getParent() != null)
      {
         boolean isEar = unit != unit.getTopLevel();
         this.deploymentScope = new JBoss5DeploymentScope(unit.getParent(), isEar);
      }

      ejbResolver = new ClientEjbResolver(deploymentScope, unit.getSimpleName());
      messageDestinationResolver = new MessageDestinationResolver(deploymentScope, xml.getMessageDestinations());

      String on = Ejb3Module.BASE_EJB3_JMX_NAME + createScopeKernelName(unit, deploymentScope) + ",name=" + applicationClientName;
      try
      {
         this.objectName = new ObjectName(on);
      }
      catch (MalformedObjectNameException e)
      {
         // should not happen
         throw new RuntimeException("Malformed object name " + on, e);
      }
      
      this.persistenceUnitDependencyResolver = persistenceUnitDependencyResolver;
      
      // The resolvers must be set by here.
      assert this.persistenceUnitDependencyResolver != null : "no persistenceUnitDependencyResolver specified";
      
      // Process meta data, so we have the right dependencies.
      processMetaData();
   }

   private String createScopeKernelName(VFSDeploymentUnit unit, DeploymentScope ear)
   {
      String scopedKernelName = "";
      if (ear != null)
         scopedKernelName += ",ear=" + ear.getShortName();
      scopedKernelName += ",jar=" + unit.getSimpleName();
      return scopedKernelName;
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

   public VirtualFile getRootFile()
   {
      return ejb3Unit.getRootFile();
   }

   public ClassLoader getClassloader()
   {
      return classLoader;
   }

   public DependencyPolicy getDependencyPolicy()
   {
      return dependencyPolicy;
   }

   public String getDeploymentDescriptorType()
   {
      return "application-client.xml";
   }

   public String getEjbJndiName(Class businessInterface) throws NameNotFoundException
   {
      return ejbResolver.getEjbJndiName(businessInterface);
   }

   public String getEjbJndiName(String link, Class businessInterface)
   {
      return ejbResolver.getEjbJndiName(link, businessInterface);
   }

   public Context getEnc()
   {
      return enc;
   }

   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      return encInjections;
   }

   public Map<String, EncInjector> getEncInjectors()
   {
      return encInjectors;
   }

   public RemoteEnvironment getEnvironmentRefGroup()
   {
      return clientMetaData;
   }

   public String getIdentifier()
   {
      return applicationClientName;
   }

   /**
    * A client enc injection container doesn't support injectors, because
    * these must be run client side.
    */
   public List<Injector> getInjectors()
   {
      //throw new RuntimeException("not supported");
      return injectors;
   }

   public Class<?> getMainClass()
   {
      return mainClass;
   }

   public ObjectName getObjectName()
   {
      return objectName;
   }

   public boolean hasJNDIBinding(String jndiName)
   {
      return false;
   }

   private void populateEnc()
   {
      for (EncInjector injector : encInjectors.values())
      {
         log.trace("encInjector: " + injector);
         injector.inject(this);
      }
   }

   private void processMetaData()
   {
      if (clientMetaData.getDepends() != null)
         for (String dependency : clientMetaData.getDepends())
         {
            getDependencyPolicy().addDependency(dependency);
         }

      // TODO: check which handlers an application client should support
      Collection<InjectionHandler<JBossClientMetaData>> handlers = new ArrayList<InjectionHandler<JBossClientMetaData>>();
      handlers.add(new EJBRemoteHandler<JBossClientMetaData>());
      handlers.add(new DependsHandler<JBossClientMetaData>());
      //handlers.add(new JndiInjectHandler<JBossClientMetaData>());
      //handlers.add(new PersistenceContextHandler<JBossClientMetaData>());
      handlers.add(new RemotePersistenceUnitHandler<JBossClientMetaData>());
      handlers.add(new ResourceHandler<JBossClientMetaData>());
      handlers.add(new WebServiceRefHandler<JBossClientMetaData>());

      ClassLoader old = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler<JBossClientMetaData> handler : handlers)
            handler.loadXml(clientMetaData, this);
/*
         Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(this, handlers, getMainClass());
         injectors.addAll(tmp.values());
*/

         //         initialiseInterceptors();
         //         for (InterceptorInfo interceptorInfo : applicableInterceptors)
         //         {
         //            for (InjectionHandler handler : handlers)
         //            {
         //               handler.loadXml(interceptorInfo.getXml(), this);
         //            }
         //         }
         //         for (InterceptorInfo interceptorInfo : applicableInterceptors)
         //         {
         //            Map<AccessibleObject, Injector> tmpInterceptor = InjectionUtil.processAnnotations(this, handlers, interceptorInfo.getClazz());
         //            InterceptorInjector injector = new InterceptorInjector(this, interceptorInfo, tmpInterceptor);
         //            interceptorInjectors.put(interceptorInfo.getClazz(), injector);
         //         }

         // When @WebServiceRef is not used service-ref won't be processed
         // In this case we process them late
         if (clientMetaData != null)
         {
            ServiceReferencesMetaData serviceRefs = clientMetaData.getServiceReferences();
            if (serviceRefs != null)
            {
               Iterator<ServiceReferenceMetaData> itRefs = serviceRefs.iterator();
               while (itRefs.hasNext())
               {
                  ServiceReferenceMetaData sref = itRefs.next();
                  try
                  {
                     String name = sref.getServiceRefName();
                     String encName = "env/" + name;
                     Context encCtx = getEnc();

                     UnifiedVirtualFile vfsRoot = UnifiedVirtualFileFactory.getInstance().create(getRootFile());
                     new ServiceReferenceHandler().bindServiceRef(encCtx, encName, vfsRoot, getClassloader(), sref);

                  }
                  catch (Exception e)
                  {
                     log.error("Failed to bind service-ref", e);
                  }
               }
            }
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   public Container resolveEjbContainer(String link, Class businessIntf)
   {
      return ejbResolver.getEjbContainer(link, businessIntf);
   }

   public Container resolveEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      return ejbResolver.getEjbContainer(businessIntf);
   }

   public String resolveMessageDestination(String link)
   {
      //      // FIXME: this is a copy of DeploymentEjbResolver & Ejb3Deployment.resolveMessageDestination
      //      int hashIndex = link.indexOf('#');
      //      if (hashIndex != -1)
      //      {
      //         if (deploymentScope == null)
      //         {
      //            log.warn("ejb link '" + link + "' is relative, but no deployment scope found");
      //            return null;
      //         }
      //         String relativePath = link.substring(0, hashIndex);
      //         Ejb3Deployment dep = deploymentScope.findRelativeDeployment(relativePath);
      //         if (dep == null)
      //         {
      //            log.warn("can't find a deployment for path '" + relativePath + "' of ejb link '" + link + "'");
      //            return null;
      //         }
      //         String name = link.substring(hashIndex + 1);
      //         // call resolve, because get is private (and should stay that way)
      //         return dep.resolveMessageDestination(name);
      //      }
      //      return getMessageDestination(link);
      return messageDestinationResolver.resolveMessageDestination(link);
   }

   public String resolvePersistenceUnitSupplier(String persistenceUnitName)
   {
      assert persistenceUnitDependencyResolver != null : "persistenceUnitDependencyResolver has not been injected";
      return persistenceUnitDependencyResolver.resolvePersistenceUnitSupplier(deploymentUnit, persistenceUnitName);
   }
   
   @Start
   public void start()
   {
      populateEnc();

      // Don't run any injectors, they must be run client side

      log.info("STARTED CLIENT ENC CONTAINER: " + applicationClientName);
   }

   @Stop
   public void stop()
   {
      log.info("STOPPED CLIENT ENC CONTAINER: " + applicationClientName);
   }
}
