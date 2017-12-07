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
package org.jboss.profileservice.management;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.management.ContextStateMapper;
import org.jboss.deployers.spi.management.DeploymentTemplate;
import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.KnownDeploymentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.NameMatcher;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.RunState;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.ManagedDeploymentImpl;
import org.jboss.managed.plugins.jmx.ManagementFactoryUtils;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory;
import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory.MBeanComponent;
import org.jboss.profileservice.spi.ManagedMBeanDeploymentFactory.MBeanDeployment;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.system.server.profileservice.attachments.AttachmentStore;

/**
 * The default ManagementView implementation.
 *
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 113138 $
 */
@ManagementObject(name="ManagementView", componentType=@ManagementComponent(type="MCBean", subtype="*"),
      properties = ManagementProperties.EXPLICIT, description = "The ProfileService ManagementView")
public class ManagementViewImpl extends AbstractTemplateCreator implements ManagementView
{
   private static RuntimePermission MV_RUNTIME_PERMISSION = new RuntimePermission(ManagementView.class.getName());

   /** The logger. */
   private static Logger log = Logger.getLogger(ManagementViewImpl.class);
   private static final String BUNDLE_NAME = "org.jboss.profileservice.management.messages"; //$NON-NLS-1$

   /** The ProfileService for loading profiles */
   private ProfileService ps;
   /** The last modified cache for loaded profiles */
   private Map<ProfileKey, Long> lastModified = new HashMap<ProfileKey, Long>();
   /** Force a reload of ManagementView. */
   private volatile boolean forceReload;

   /** The MainDeployer only used to get the ManagedDeployments */
   private MainDeployer mainDeployer;
   /** The attachment store to persist the component changes. */
   private AttachmentStore store;

   /** The deployment templates that have been registered with the MV */
   private Map<String, DeploymentTemplate> templates = Collections.synchronizedMap(new HashMap<String, DeploymentTemplate>());

   /** The internationalization resource bundle */
   private ResourceBundle i18n;
   /** the Locale for the i18n messages */
   private Locale currentLocale;
   /** The formatter used for i18n messages */
   private MessageFormat formatter = new MessageFormat("");

   /** The bootstrap deployment name to ManagedDeployment map */
   private Map<String, ManagedDeployment> bootstrapManagedDeployments = Collections.emptyMap();

   /** The state mappings. */
   private static final ContextStateMapper<RunState> runStateMapper;
   private static final ContextStateMapper<DeploymentState> deploymentStateMapper;

   /** The dispatcher handles ManagedOperation dispatches */
   private RuntimeComponentDispatcher dispatcher;
   /** The managed operation proxy factory. */
   private ManagedOperationProxyFactory proxyFactory;

   /** A proxy for pure JMX dispatch */
   private ManagedOperationProxyFactory mbeanProxyFactory;

   /** . */
   private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();
   /** ManagedObjectFactory used for platform mbean ManagedObjects */
   ManagedObjectFactory managedObjFactory = ManagedObjectFactory.getInstance();
   /** A map of ManagedMBeanDeploymentFactory for proxying mbeans into the management layer */
   private Map<String, ManagedMBeanDeploymentFactory> mdfs =
      Collections.synchronizedMap(new HashMap<String, ManagedMBeanDeploymentFactory>());

   /** The JMX Kernel for non MC managed JMXobjects */
   private MBeanServer mbeanServer;

   /** An MO Factory using MBeanInfo */
   private MBeanManagedObjectFactory mbeanMOFactory = new MBeanManagedObjectFactory();

   /** The internal state for this view. */
   @SuppressWarnings("unused")
   private volatile ManagementViewState state = new ManagementViewState(null, null, null, runStateMapper, deploymentStateMapper);      
   private static final AtomicReferenceFieldUpdater<ManagementViewImpl, ManagementViewState> stateUpdater = AtomicReferenceFieldUpdater.newUpdater(ManagementViewImpl.class, ManagementViewState.class, "state");
   
   static
   {
      // Set default run state mappings for mc beans/mbeans
      Map<String, RunState> runStateMappings = new HashMap<String, RunState>();
      runStateMappings.put("**ERROR**", RunState.FAILED);
      runStateMappings.put("Not Installed", RunState.STOPPED);
      runStateMappings.put("PreInstall", RunState.STOPPED);
      runStateMappings.put("Described", RunState.STOPPED);
      runStateMappings.put("Instantiated", RunState.STOPPED);
      runStateMappings.put("Configured", RunState.STOPPED);
      runStateMappings.put("Create", RunState.STOPPED);
      runStateMappings.put("Start", RunState.STOPPED);
      runStateMappings.put("Installed", RunState.RUNNING);

      runStateMapper = new ContextStateMapper<RunState>(runStateMappings,
            RunState.STARTING, RunState.STOPPED, RunState.FAILED, RunState.UNKNOWN);

      Map<String, DeploymentState> deploymentMappings = new HashMap<String, DeploymentState>();
      deploymentMappings.put("**ERROR**", DeploymentState.FAILED);
      deploymentMappings.put("Not Installed", DeploymentState.STOPPED);
      deploymentMappings.put("Installed", DeploymentState.STARTED);

      deploymentStateMapper = new ContextStateMapper<DeploymentState>(deploymentMappings,
            DeploymentState.STARTING, DeploymentState.STOPPING, DeploymentState.FAILED, DeploymentState.UNKNOWN);
   }

   public ManagementViewImpl() throws IOException
   {
      currentLocale = Locale.getDefault();
      formatter.setLocale(currentLocale);
      i18n = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
   }

   public void start() throws Exception
   {
      // nothing
   }

   public void stop()
   {
      // Cleanup on stop
      release();
   }

   public boolean load()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      // Clear any thread interrupt
      boolean wasInterrupted = Thread.interrupted();
      if (wasInterrupted)
         log.debug("Cleared interrupted state of calling thread");

      try
      {
         final ManagementViewState oldState;
         synchronized (this)
         {
            // If the profile is not modified do nothing
            if (requiresReload() == false)
            {
               log.trace("Not reloading profiles.");
               return false;
            }
            this.forceReload = false;
            oldState = internalLoad();            
         }
         // Release the old state outside the lock          
         if (oldState != null)
         {
            oldState.release();
         }
      }
      finally
      {
         if (wasInterrupted)
         {
            Thread.currentThread().interrupt();
            log.debug("Restored interrupted state of calling thread");
         }
      }
      return true;
   }
      
   protected synchronized ManagementViewState internalLoad()
   {
      //
      boolean trace = log.isTraceEnabled();      
      final ManagementViewState newState = createNewState();      

      // load the profiles
      loadProfiles(newState, trace);

      // Process mbean components that need to be exposed as ManagedDeployment/ManagedComponent
      for(ManagedMBeanDeploymentFactory mdf : mdfs.values())
      {
         log.trace("Processing deployments for factory: "+mdf.getFactoryName());
         Collection<MBeanDeployment> deployments = mdf.getDeployments(mbeanServer);
         for(MBeanDeployment md : deployments)
         {
            log.trace("Saw MBeanDeployment: "+md);
            HashMap<String, ManagedObject> unitMOs = new HashMap<String, ManagedObject>();
            Collection<MBeanComponent> components = md.getComponents();
            if(components != null)
            {
               for(MBeanComponent comp : components)
               {
                  log.trace("Saw MBeanComponent: "+comp);
                  try
                  {
                     ManagedObject mo = createManagedObject(comp.getName(), mdf.getDefaultViewUse(), mdf.getPropertyMetaMappings());

                     String name = comp.getName().getCanonicalName();
                     ManagementObject moAnn = createMOAnnotation(name, comp.getType(), comp.getSubtype());

                     // Both the ManagementObject and ManagementComponent annotation need to be in the MO annotations
                     mo.getAnnotations().put(ManagementObject.class.getName(), moAnn);
                     ManagementComponent mcAnn = moAnn.componentType();
                     mo.getAnnotations().put(ManagementComponent.class.getName(), mcAnn);
                     unitMOs.put(name, mo);
                  }
                  catch(Exception e)
                  {
                     log.warn("Failed to create ManagedObject for: "+comp, e);
                  }
               }
            }
            ManagedDeploymentImpl mdi = new ManagedDeploymentImpl(md.getName(), md.getName(), null, unitMOs);
            mdi.setTypes(Collections.singleton("external-mbean"));
            try
            {
               newState.processManagedDeployment(mdi, null, DeploymentState.STARTED, 0, trace);
            }
            catch(Exception e)
            {
               log.warn("Failed to process ManagedDeployment for: " + md.getName(), e);
            }
         }
      }

      // Process the bootstrap deployments
      for(ManagedDeployment md : bootstrapManagedDeployments.values())
      {
         try
         {
            newState.processManagedDeployment(md, null, DeploymentState.STARTED, 0, trace);
         }
         catch(Exception e)
         {
            log.warn("Failed to process ManagedDeployment for: " + md.getName(), e);
         }
      }
      // Check if the MOs were merged successfully
      newState.checkRuntimeMOs();

      // Now create a ManagedDeployment for the platform beans
      Map<String, ManagedObject> platformMBeanMOs = ManagementFactoryUtils.getPlatformMBeanMOs(managedObjFactory);
      ManagedDeploymentImpl platformMBeans = new ManagedDeploymentImpl("JDK PlatformMBeans", "PlatformMBeans", null,
            platformMBeanMOs);
      List<ManagedObject> gcMbeans = ManagementFactoryUtils.getGarbageCollectorMXBeans(managedObjFactory);
      Map<String, ManagedObject> gcMOs = new HashMap<String, ManagedObject>();
      for (ManagedObject mo : gcMbeans)
         gcMOs.put(mo.getName(), mo);
      List<ManagedObject> mmMbeans = ManagementFactoryUtils.getMemoryManagerMXBeans(managedObjFactory);
      Map<String, ManagedObject> mmMOs = new HashMap<String, ManagedObject>();
      for (ManagedObject mo : mmMbeans)
         mmMOs.put(mo.getName(), mo);
      List<ManagedObject> mpoolMBeans = ManagementFactoryUtils.getMemoryPoolMXBeans(managedObjFactory);
      Map<String, ManagedObject> mpoolMOs = new HashMap<String, ManagedObject>();
      for (ManagedObject mo : mpoolMBeans)
         mpoolMOs.put(mo.getName(), mo);
      ManagedDeploymentImpl gcMD = new ManagedDeploymentImpl("GarbageCollectorMXBeans", "GarbageCollectorMXBeans",
            null, gcMOs);
      platformMBeans.getChildren().add(gcMD);
      ManagedDeploymentImpl mmMD = new ManagedDeploymentImpl("MemoryManagerMXBeans", "MemoryManagerMXBeans", null, mmMOs);
      platformMBeans.getChildren().add(mmMD);
      ManagedDeploymentImpl mpoolMD = new ManagedDeploymentImpl("MemoryPoolMXBeans", "MemoryPoolMXBeans", null, mpoolMOs);
      platformMBeans.getChildren().add(mpoolMD);

      try
      {
         // Create the ManagedComponents
         newState.processManagedDeployment(platformMBeans, null, DeploymentState.STARTED, 0, trace);
      }
      catch(Exception e)
      {
         log.warn("Failed to process ManagedDeployments for the platform beans", e);
      }
      // Swap the states
      return stateUpdater.getAndSet(this, newState);
   }

   @SuppressWarnings("all")
   private static final class ManagementObjectAnnotationImpl implements ManagementObject, Serializable
   {
      private static final long serialVersionUID=5355799336353299850L;

      private final String name;
      private final String type;
      private final String subtype;

      @SuppressWarnings("all")
      private final class ManagementComponentAnnotationImpl implements ManagementComponent, Serializable
      {
         private static final long serialVersionUID=5355799336353299850L;

         public String subtype()
         {
            return subtype;
         }

         public String type()
         {
            return type;
         }

         public Class<? extends Annotation> annotationType()
         {
            return ManagementComponent.class;
         }
      }

      private ManagementObjectAnnotationImpl(String name, String type, String subtype)
      {
         this.name=name;
         this.type=type;
         this.subtype=subtype;
      }

      public String attachmentName()
      {
         return "";
      }

      public ManagementProperty[] classProperties()
      {
         return new ManagementProperty[0];
      }

      public ManagementComponent componentType()
      {
         return new ManagementComponentAnnotationImpl();
      }

      public String description()
      {
         return "";
      }

      public boolean isRuntime()
      {
         return true;
      }

      public String name()
      {
         return name;
      }

      public ManagementOperation[] operations()
      {
         return new ManagementOperation[0];
      }

      public ManagementProperties properties()
      {
         return ManagementProperties.ALL;
      }

      public Class<?> targetInterface()
      {
         return Object.class;
      }

      public String type()
      {
         return "";
      }

      public Class<? extends Annotation> annotationType()
      {
         return ManagementObject.class;
      }
   }

   private ManagementObject createMOAnnotation(final String name, final String type, final String subtype)
   {
      return new ManagementObjectAnnotationImpl(name, type, subtype);
   }

   public void reload()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      forceReload = true;
      load();
   }

   public void release()
   {
      final ManagementViewState oldState;
      synchronized (this)
      {
         // Replace the existing view with an empty one
         oldState = stateUpdater.getAndSet(this, createNewState());
         lastModified.clear();
      }
      if (oldState != null)
      {
         oldState.release();
      }
   }

   protected void loadProfiles(final ManagementViewState viewState, final boolean trace)
   {
      log.trace("reloading profiles: "+ this.ps.getActiveProfileKeys());
      for(ProfileKey key : this.ps.getActiveProfileKeys())
      {
         try
         {
            // Get the active profile
            Profile profile = this.ps.getActiveProfile(key);
            // Get the deployments
            Collection<ProfileDeployment> deployments = profile.getDeployments();
            // Add the lastModified cache
            this.lastModified.put(key, profile.getLastModified());
            // Process the deployments
            for(ProfileDeployment deployment : deployments)
            {
               try
               {
                  try
                  {
                     ManagedDeployment md = getManagedDeployment(deployment);
                     viewState.processRootManagedDeployment(md, key, trace);

                     // Cache the deployment types
                     if(md.getTypes() != null && md.getTypes().isEmpty() == false)
                        deployment.addTransientAttachment(KnownDeploymentTypes.class.getName(), md.getTypes());
                  }
                  catch(DeploymentException e)
                  {
                     // FIXME Assume a undeployed (stopped) deployment
                     String deploymentName = deployment.getName();
                     ManagedDeployment md = new ManagedDeploymentImpl(deploymentName,
                           deployment.getRoot().getName());

                     //
                     md.setAttachment(Exception.class.getName(), e);
                     
                     // Try to get the cached deployment type
                     Collection<String> deploymentTypes = (Collection<String>) deployment
                           .getTransientAttachment(KnownDeploymentTypes.class.getName());

                     if(deploymentTypes != null)
                     {
                        md.setTypes(new HashSet<String>(deploymentTypes));
                     }
                     else
                     {
                        int i = deploymentName.lastIndexOf(".");
                        if(i != -1 && (i + 1) < deploymentName.length())
                        {
                           String guessedType = deploymentName.substring(i + 1, deploymentName.length());
                           if(guessedType.endsWith("/"))
                              guessedType = guessedType.substring(0, guessedType.length() -1 );
                           md.setTypes(new HashSet<String>(1));
                           md.addType(guessedType);
                        }
                     }

                     viewState.processManagedDeployment(md, key, DeploymentState.STOPPED, 0, trace);
                  }
               }
               catch(Exception e)
               {
                  log.warn("Failed to create ManagedDeployment for: " + deployment.getName(), e);
               }
            }
         }
         catch(Exception e)
         {
            log.debug("failed to load profile " + key, e);
         }
      }
   }

   protected boolean requiresReload()
   {
      if(forceReload == true)
      {
         forceReload = false;
         return true;
      }

      for(ProfileKey key : this.ps.getActiveProfileKeys())
      {
         if(this.lastModified.containsKey(key) == false)
            return true;

         try
         {
            Profile profile = this.ps.getActiveProfile(key);
            long lastModified = this.lastModified.get(key);
            if(profile.getLastModified() > lastModified)
               return true;
         }
         catch(Exception ignore) { /** . */ }
      }
      return false;
   }

   public Map<String, ManagedDeployment> getBootstrapManagedDeployments()
   {
      return bootstrapManagedDeployments;
   }
   public void setBootstrapManagedDeployments(
         Map<String, ManagedDeployment> bootstrapManagedDeployments)
   {
      this.bootstrapManagedDeployments = bootstrapManagedDeployments;
   }

   public ProfileService getProfileService()
   {
      return ps;
   }

   public void setProfileService(ProfileService ps)
   {
      this.ps = ps;
      if(log.isTraceEnabled())
         log.trace("setProfileService: "+ps);
   }

   public ManagedOperationProxyFactory getProxyFactory()
   {
      return proxyFactory;
   }

   public void setProxyFactory(ManagedOperationProxyFactory proxyFactory)
   {
      this.proxyFactory = proxyFactory;
   }

   public AttachmentStore getAttachmentStore()
   {
      return store;
   }

   public void setAttachmentStore(AttachmentStore store)
   {
      this.store = store;
   }

   public MainDeployer getMainDeployer()
   {
      return mainDeployer;
   }

   public void setMainDeployer(MainDeployer mainDeployer)
   {
      this.mainDeployer = mainDeployer;
      if(log.isTraceEnabled())
         log.trace("setMainDeployer: "+mainDeployer);
   }

   public MetaValueFactory getMetaValueFactory()
   {
      return metaValueFactory;
   }
   public void setMetaValueFactory(MetaValueFactory metaValueFactory)
   {
      this.metaValueFactory = metaValueFactory;
   }

   public ManagedObjectFactory getManagedObjFactory()
   {
      return managedObjFactory;
   }
   public void setManagedObjFactory(ManagedObjectFactory managedObjFactory)
   {
      this.managedObjFactory = managedObjFactory;
   }

   public void setDispatcher(RuntimeComponentDispatcher dispatcher)
   {
      this.dispatcher = dispatcher;
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public MBeanManagedObjectFactory getMbeanMOFactory()
   {
      return mbeanMOFactory;
   }

   public void setMbeanMOFactory(MBeanManagedObjectFactory mbeanMOFactory)
   {
      this.mbeanMOFactory = mbeanMOFactory;
   }

   /**
    * Get the names of the deployment in the profile.
    */
   public Set<String> getDeploymentNames()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);
      final ManagementViewState viewState = stateUpdater.get(this);
      return new TreeSet<String>(viewState.getManagedDeployments().keySet());
   }

   /**
    * Get the names of the deployment in the profile that have the
    * given deployment type.
    *
    * @param type - the deployment type
    */
   public Set<String> getDeploymentNamesForType(String type)
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

     Set<String> matches = new TreeSet<String>();
      final ManagementViewState viewState = stateUpdater.get(this);
      for(ManagedDeployment md : viewState.getManagedDeployments().values())
      {
         String name = md.getName();
         Set<String> types = md.getTypes();
         if(types != null)
         {
            if(types.contains(type))
            {
               if(log.isTraceEnabled())
                  log.trace(name+" matches type: "+type+", types:"+types);
               matches.add(name);
            }
         }
      }
      return matches;
   }

   public Set<String> getMatchingDeploymentName(String regex)
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
      {
         sm.checkPermission(MV_RUNTIME_PERMISSION);
      }
      if(regex == null)
      {
         throw new IllegalArgumentException("null regex");
      }
      Set<String> names = getDeploymentNames();
      HashSet<String> matches = new HashSet<String>();
      Pattern p = Pattern.compile(regex);
      for(String name : names)
      {
         Matcher m = p.matcher(name);
         if( m.matches() )
         {
            matches.add(name);
         }
      }
      return matches;
   }
   
   public Set<ManagedDeployment> getMatchingDeployments(String name, NameMatcher<ManagedDeployment> matcher)
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
      {
         sm.checkPermission(MV_RUNTIME_PERMISSION);
      }
      if(name == null)
      {
         throw new IllegalArgumentException("null deployment name");
      }
      if(matcher == null)
      {
         throw new IllegalArgumentException("null deployment matcher");
      }
      final ManagementViewState viewState = stateUpdater.get(this);
      Set<ManagedDeployment> matches = new HashSet<ManagedDeployment>();
      for(ManagedDeployment deployment : viewState.getManagedDeployments().values())
      {
         if(matcher.matches(deployment, name))
         {
            matches.add(deployment);
         }
      }
      return matches;
   }

   public Set<String> getTemplateNames()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      return new HashSet<String>(templates.keySet());
   }

   public void addManagedMBeanDeployments(ManagedMBeanDeploymentFactory factory)
   {
      log.trace("addManagedDeployment, "+factory);
      String name = factory.getFactoryName();
      this.mdfs.put(name, factory);
   }
   public void removeManagedMBeanDeployments(ManagedMBeanDeploymentFactory factory)
   {
      log.trace("removeManagedDeployment, "+factory);
      String name = factory.getFactoryName();
      this.mdfs.remove(name);
   }

   public void addTemplate(DeploymentTemplate template)
   {
      this.templates.put(template.getInfo().getName(), template);
      log.trace("addTemplate: "+template);
   }

   public void removeTemplate(DeploymentTemplate template)
   {
      this.templates.remove(template.getInfo().getName());
      log.trace("removeTemplate: "+template);
   }


   /**
    * Get the managed deployment.
    *
    * @param name the deployment name
    * @throws NoSuchDeploymentException if no matching deployment was found
    */
   public ManagedDeployment getDeployment(String name) throws NoSuchDeploymentException
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      if(name == null)
         throw new IllegalArgumentException("Null deployment name");

      final ManagementViewState viewState = stateUpdater.get(this);
      // Resolve internally.
      ManagedDeployment md = viewState.getManagedDeployments().get(name);
      if (md == null)
      {
         // Check the bootstrap deployments        
         md = this.bootstrapManagedDeployments.get(name);
      }

      // Check the file name
      if(md == null)
      {
         for(String deployment : viewState.getRootDeployments())
         {
            String fixedDeploymentName = deployment;
            if(deployment.endsWith("/"))
               fixedDeploymentName = deployment.substring(0, deployment.length() - 1);

            if(fixedDeploymentName.endsWith(name))
            {
               md = viewState.getManagedDeployments().get(deployment);
               break;
            }
         }
      }
      // Do not return null
      if (md == null)
         throw new NoSuchDeploymentException("Managed deployment: " + name + " not found.");

      return md;
   }

   /**
    *
    * @param key
    * @param type
    * @return
    * @throws NoSuchProfileException
    */
   public Set<ManagedDeployment> getDeploymentsForType(String type)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      Set<String> names = getDeploymentNamesForType(type);
      HashSet<ManagedDeployment> mds = new HashSet<ManagedDeployment>();
      for(String name : names)
      {
         ManagedDeployment md = getDeployment(name);
         mds.add(md);
      }
      return mds;
   }

   /**
    * Get a set of the component types in use in the profiles
    * @return set of component types in use
    */
   public Set<ComponentType> getComponentTypes()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);
      
      final ManagementViewState viewState = stateUpdater.get(this);
      HashSet<ComponentType> types = new HashSet<ComponentType>(viewState.getCompByCompType().keySet());
      return types;
   }

   /**
    *
    * @param key
    * @param type
    * @return
    * @throws NoSuchProfileException
    */
   public Set<ManagedComponent> getComponentsForType(ComponentType type)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

     Set<ManagedComponent> comps = null;
      // Check the any component type
      final ManagementViewState viewState = stateUpdater.get(this);
      if(type.equals(KnownComponentTypes.ANY_TYPE))
      {
         HashSet<ManagedComponent> all = new HashSet<ManagedComponent>();
         for(Set<ManagedComponent> typeComps : viewState.getCompByCompType().values())
         {
            for(ManagedComponent comp : typeComps)
            {
               all.add(comp);
            }
         }
         comps = all;
      }
      else
      {
        comps = viewState.getCompByCompType().get(type);
      }
      if(comps == null)
         comps = Collections.emptySet();
      return comps;
   }

   public ManagedComponent getComponent(String name, ComponentType type)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);
      
      final ManagementViewState viewState = stateUpdater.get(this);
      Set<ManagedComponent> components = viewState.getCompByCompType().get(type);
      ManagedComponent comp = null;
      if(components != null)
      {
         for(ManagedComponent mc : components)
         {
            if(mc.getName().equals(name))
            {
               comp = mc;
               break;
            }
         }
      }
      if(comp != null)
      {
         Map<String, ManagedProperty> props = comp.getProperties();
         Set<ManagedOperation> ops = comp.getOperations();
         if(log.isTraceEnabled())
            log.trace("Component"
               +"(ops.size="
               +ops != null ? ops.size() : 0
               +",props.size=)"
               +props != null ? props.size() : 0);
      }
      return comp;
   }
   public Set<ManagedComponent> getMatchingComponents(String name, ComponentType type,
         NameMatcher<ManagedComponent> matcher)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      final ManagementViewState viewState = stateUpdater.get(this);
      Set<ManagedComponent> components = viewState.getCompByCompType().get(type);
      Set<ManagedComponent> matched = new HashSet<ManagedComponent>();
      if(components != null)
      {
         for(ManagedComponent mc : components)
         {
            if(matcher.matches(mc, name))
               matched.add(mc);
         }
      }
      if(matched.size() > 0)
      {
         if(log.isTraceEnabled())
            log.trace("getComponents matched: "+matched);
      }
      return matched;
   }

   public DeploymentTemplateInfo getTemplate(String name)
      throws NoSuchDeploymentException
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      DeploymentTemplate template = templates.get(name);
      if( template == null )
      {
         formatter.applyPattern(i18n.getString("ManagementView.NoSuchTemplate")); //$NON-NLS-1$
         Object[] args = {name};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }

      // Make sure to return a copy to avoid call by reference uses modifying the template values
      DeploymentTemplateInfo info = template.getInfo();
      info = info.copy();
      if(log.isTraceEnabled())
         log.trace("getTemplate, "+info);
      return info;
   }

   public void applyTemplate(String deploymentBaseName, DeploymentTemplateInfo info) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      if (deploymentBaseName == null)
         throw new IllegalArgumentException("Null deployment base name.");
      if (info == null)
         throw new IllegalArgumentException("Null template info.");

      DeploymentTemplate template = templates.get(info.getName());
      if (template == null)
      {
         formatter.applyPattern(i18n.getString("ManagementView.NoSuchTemplate")); //$NON-NLS-1$
         Object[] args =
         {info.getName()};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }

      // Create a deployment base from the template
      if (log.isTraceEnabled())
         log.trace("applyTemplate, deploymentBaseName=" + deploymentBaseName + ", info=" + info);

      final ManagementViewState oldState;
      synchronized (this)
      {
         final ManagementViewState currentState = stateUpdater.get(this);
         final ManagementViewState newState = currentState.copy();
         // Create, distribute and start a deployment template
         String deploymentName = super.applyTemplate(template, deploymentBaseName, info);
         // Process the deployment
         ManagedDeployment md = getMainDeployer().getManagedDeployment(deploymentName);
         newState.processRootManagedDeployment(md, getDefaulProfiletKey(), log.isTraceEnabled());
         oldState = stateUpdater.getAndSet(this, newState);
      }
      if (oldState != null)
      {
         oldState.release();
      }
   }

   public void process() throws DeploymentException
   {
      //
   }

   /**
    * Process a component update.
    */
   public synchronized void updateComponent(ManagedComponent comp)
      throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      if(comp == null)
         throw new IllegalArgumentException("Null managed component.");
      // Find the comp deployment
      ManagedDeployment md = comp.getDeployment();

      // Get the parent
      while( md.getParent() != null )
         md = md.getParent();

      String name = md.getName();
      ProfileDeployment compDeployment = getProfileDeployment(name);
      if( compDeployment == null )
      {
         formatter.applyPattern(i18n.getString("ManagementView.NoSuchDeploymentException")); //$NON-NLS-1$
         Object[] args = {name};
         String msg = formatter.format(args);
         throw new NoSuchDeploymentException(msg);
      }

      // Apply the managed properties to the server ManagedDeployment/ManagedComponent
      final ManagementViewState viewState = stateUpdater.get(this);      
      ManagedDeployment compMD = viewState.getManagedDeployments().get(md.getName());
      log.trace("updateComponent, deploymentName="+name+": "+compMD);

      ManagedComponent serverComp = null;
      // Find the managed component again
      if(comp.getDeployment().getParent() == null)
      {
         serverComp = compMD.getComponent(comp.getName());
      }
      else
      {
         // Look at the children
         // TODO - support more levels of nested deployments ?
         if(compMD.getChildren() != null && compMD.getChildren().isEmpty() == false)
         {
            for(ManagedDeployment child : compMD.getChildren())
            {
               if(serverComp != null)
                  break;

               serverComp = child.getComponent(comp.getName());
            }
         }
      }
      if(serverComp == null)
      {
         log.debug("Name: "+comp.getName()+" does not map to existing ManagedComponet in ManagedDeployment: "+md.getName()
               + ", components: "+compMD.getComponents());
         formatter.applyPattern(i18n.getString("ManagementView.InvalidComponentName")); //$NON-NLS-1$
         Object[] args = {comp.getName(), md.getName()};
         String msg = formatter.format(args);
         throw new IllegalArgumentException(msg);
      }

      // Dispatch any runtime component property values
      for(ManagedProperty prop : comp.getProperties().values())
      {
         // Skip null values && non-CONFIGURATION values, unmodified values, and removed values
         boolean skip = prop.getValue() == null
            || prop.isReadOnly()
            || prop.hasViewUse(ViewUse.CONFIGURATION) == false
//            || prop.isModified() == false
            || prop.isRemoved() == true;
         if( skip )
         {
            if(log.isTraceEnabled())
               log.trace("Skipping component property: "+prop);
            continue;
         }

         ManagedProperty ctxProp = serverComp.getProperties().get(prop.getName());
         // Check for a mapped name
         if( ctxProp == null )
         {
            String mappedName = prop.getMappedName();
            if( mappedName != null )
               ctxProp = serverComp.getProperties().get(mappedName);
         }
         if( ctxProp == null )
         {
            formatter.applyPattern(i18n.getString("ManagementView.InvalidTemplateProperty")); //$NON-NLS-1$
            Object[] args = {prop.getName()};
            String msg = formatter.format(args);
            throw new IllegalArgumentException(msg);
         }
         // The property value must be a MetaValue
         Object value = prop.getValue();
         if ((value instanceof MetaValue) == false)
         {
            formatter.applyPattern(i18n.getString("ManagementView.InvalidPropertyValue")); //$NON-NLS-1$
            Object[] args = {prop.getName(), value.getClass()};
            String msg = formatter.format(args);
            throw new IllegalArgumentException(msg);
         }
         // Update the serverComp
         MetaValue metaValue = (MetaValue)value;
         ctxProp.setField(Fields.META_TYPE, metaValue.getMetaType());
         ctxProp.setValue(metaValue);

         // Dispatch any runtime component property values
         Object componentName = getComponentName(ctxProp);
         ActivationPolicy policy = ctxProp.getActivationPolicy();

         if (componentName != null && policy.equals(ActivationPolicy.IMMEDIATE))
         {
            AbstractRuntimeComponentDispatcher.setActiveProperty(ctxProp);
            dispatcher.set(componentName, ctxProp.getName(), metaValue);
         }
      }

      // Persist the changed values
      this.store.updateDeployment(comp.getDeployment().getName(), serverComp);
      // Force reload
      this.forceReload = true;
   }

   public synchronized void removeComponent(ManagedComponent comp) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(MV_RUNTIME_PERMISSION);

      if(comp == null)
         throw new IllegalArgumentException("null managed component.");
      //
      ManagedDeployment md = comp.getDeployment();

      // Get the parent
      while( md.getParent() != null )
         md = md.getParent();

      String name = md.getName();
      ProfileDeployment profileDeployment = getProfileDeployment(name);
      if( profileDeployment == null )
      {
         formatter.applyPattern(i18n.getString("ManagementView.NoSuchDeploymentException")); //$NON-NLS-1$
         Object[] args = {name};
         String msg = formatter.format(args);
         throw new NoSuchDeploymentException(msg);
      }

      // Apply the managed properties to the server ManagedDeployment/ManagedComponent
      final ManagementViewState viewState = stateUpdater.get(this);      
      ManagedDeployment compMD = viewState.getManagedDeployments().get(md.getName());
      log.trace("updateComponent, deploymentName="+name+": "+compMD);

      ManagedComponent serverComp = null;
      // Find the managed component again
      if(comp.getDeployment().getParent() == null)
      {
         serverComp = compMD.getComponent(comp.getName());
      }
      else
      {
         // Look at the children
         // TODO - support more levels of nested deployments ?
         if(compMD.getChildren() != null && compMD.getChildren().isEmpty() == false)
         {
            for(ManagedDeployment child : compMD.getChildren())
            {
               if(serverComp != null)
                  break;

               serverComp = child.getComponent(comp.getName());
            }
         }
      }
      if(serverComp == null)
      {
         log.debug("Name: "+comp.getName()+" does not map to existing ManagedComponet in ManagedDeployment: "+md.getName()
               + ", components: "+compMD.getComponents());
         formatter.applyPattern(i18n.getString("ManagementView.InvalidComponentName")); //$NON-NLS-1$
         Object[] args = {comp.getName(), md.getName()};
         String msg = formatter.format(args);
         throw new IllegalArgumentException(msg);
      }

      //
      log.trace("remove component: " + comp + ", deployment: "+ profileDeployment);
      // Remove
      this.store.removeComponent(comp.getDeployment().getName(), serverComp);
   }

   /**
    * Get the component name from managed property.
    *
    * @param property the managed property
    * @return component name or null if no coresponding component
    */
   protected Object getComponentName(ManagedProperty property)
   {
      // first check target
      ManagedObject targetObject = property.getTargetManagedObject();
      if (targetObject != null)
         return targetObject.getComponentName();

      // check owner
      targetObject = property.getManagedObject();
      return targetObject != null ? targetObject.getComponentName() : null;
   }

   private ManagedObject createManagedObject(ObjectName mbean, String defaultViewUse, Map<String, String> propertyMetaMappings)
      throws Exception
   {
      MBeanInfo info = mbeanServer.getMBeanInfo(mbean);
      ClassLoader mbeanLoader = mbeanServer.getClassLoaderFor(mbean);
      MetaData metaData = null;
      ViewUse[] viewUse = defaultViewUse == null ? null : new ViewUse[]{ViewUse.valueOf(defaultViewUse)};
      ManagedObject mo = mbeanMOFactory.getManagedObject(mbean, info, mbeanLoader, metaData, viewUse, propertyMetaMappings);
      return mo;
   }

   private ManagedDeployment getManagedDeployment(ProfileDeployment ctx) throws DeploymentException
   {
      return mainDeployer.getManagedDeployment(ctx.getName());
   }

   private ProfileKey getProfileKeyForDeployemnt(String name) throws NoSuchDeploymentException
   {
      ManagedDeployment md = getDeployment(name);
      return md.getAttachment(ProfileKey.class);
   }

   private Profile getProfileForDeployment(String name) throws Exception
   {
      ProfileKey key = getProfileKeyForDeployemnt(name);
      if(key == null)
         throw new NoSuchDeploymentException("No associated profile found for deployment:" + name);

      return this.ps.getActiveProfile(key);
   }

   private ProfileDeployment getProfileDeployment(String name) throws Exception
   {
      Profile profile = getProfileForDeployment(name);
      return profile.getDeployment(name);
   }

   public static void main(String[] args)
      throws Exception
   {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName("jboss.management.local:J2EEApplication=null,J2EEServer=Local,j2eeType=WebModule,*");
      Set<ObjectName> matches = server.queryNames(name, null);
      for(ObjectName on : matches)
      {
         System.err.println(on);
      }
   }

   public ManagedOperationProxyFactory getMbeanProxyFactory()
   {
      return mbeanProxyFactory;
   }

   public void setMbeanProxyFactory(ManagedOperationProxyFactory mbeanProxyFactory)
   {
      this.mbeanProxyFactory = mbeanProxyFactory;
   }

   /**
    * Create the internal view state.
    * 
    * @return the new view state
    */
   private ManagementViewState createNewState() {
      return new ManagementViewState(dispatcher, proxyFactory, mbeanProxyFactory, runStateMapper, deploymentStateMapper);      
   }
   
}
