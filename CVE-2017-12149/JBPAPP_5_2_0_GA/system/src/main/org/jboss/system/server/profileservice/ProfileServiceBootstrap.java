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
package org.jboss.system.server.profileservice;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.bootstrap.spi.Bootstrap;
import org.jboss.bootstrap.spi.Server;
import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.bootstrap.spi.microcontainer.MCServer;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.managed.ManagedDeploymentCreator;
import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.deployer.kernel.KernelDeploymentDeployer.KernelDeploymentVisitor;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.registry.BeanKernelRegistryEntry;
import org.jboss.kernel.spi.config.KernelConfigurator;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedObject;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.DefaultFieldsImpl;
import org.jboss.managed.plugins.ManagedComponentImpl;
import org.jboss.managed.plugins.ManagedObjectImpl;
import org.jboss.managed.plugins.ManagedOperationImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.ArrayValueSupport;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.values.EnumValueSupport;
import org.jboss.profileservice.spi.MutableProfile;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.profileservice.spi.types.ControllerStateMetaType;
import org.jboss.system.server.profileservice.repository.AbstractBootstrapProfileFactory;

/**
 * Bootstraps the profile service
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89838 $
 */
public class ProfileServiceBootstrap implements Bootstrap, KernelRegistryPlugin
{
   /** The log */
   private static final Logger log = Logger.getLogger(ProfileServiceBootstrap.class);
   
   /** The root profile key. */
   protected ProfileKey profileKey;

   /** The loaded profiles. */
   private List<ProfileKey> bootstrapProfiles = new ArrayList<ProfileKey>();
   
   /** The server MainDeployer */
   protected MainDeployer mainDeployer;

   /** The server ProfileService */
   protected ProfileService profileService;

   /** The ManagedDeploymentCreator plugin */
   private ManagedDeploymentCreator mgtDeploymentCreator = null;
   /** The ManagedObjectFactory for building the bootstrap deployment ManagedObjects */
   private ManagedObjectFactory mof;
   /** The ManagedDeployment map for the MCServer KernelDeployments */
   private Map<String, ManagedDeployment> bootstrapMDs = new HashMap<String, ManagedDeployment>();
   
   /** The profile bootstrap factory */
   private AbstractBootstrapProfileFactory profileFactory; 

   /** Whether we are shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);
   /** */
   private Map<Object, KernelRegistryEntry> bootstrapEntries = new HashMap<Object, KernelRegistryEntry>();
   /** */
   private KernelConfigurator configurator;

   /**
    * Create a new ProfileServiceBootstrap.
    */
   public ProfileServiceBootstrap()
   {
   }

   /**
    * Return the MainDeployer bean.
    * 
    * @return the MainDeployer bean if bootstrap succeeded, null otherwise.
    */
   public MainDeployer getMainDeployer()
   {
      return mainDeployer;
   }
   public void setMainDeployer(MainDeployer mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   public KernelConfigurator getConfigurator()
   {
      return configurator;
   }
   public void setConfigurator(KernelConfigurator configurator)
   {
      this.configurator = configurator;
   }

   /**
    * Return the ProfileService bean.
    * 
    * @return the ProfileService bean if bootstrap succeeded, null otherwise
    */
   public ProfileService getProfileService()
   {
      return profileService;
   }
   public void setProfileService(ProfileService profileService)
   {
      this.profileService = profileService;
   }

   public ProfileKey getProfileKey()
   {
      return profileKey;
   }
   
   public void setProfileKey(ProfileKey profileKey)
   {
      this.profileKey = profileKey;
   }

   public ManagedObjectFactory getMof()
   {
      return mof;
   }
   public void setMof(ManagedObjectFactory mof)
   {
      this.mof = mof;
   }

   public ManagedDeploymentCreator getMgtDeploymentCreator()
   {
      return mgtDeploymentCreator;
   }
   public void setMgtDeploymentCreator(ManagedDeploymentCreator mgtDeploymentCreator)
   {
      this.mgtDeploymentCreator = mgtDeploymentCreator;
   }

   public AbstractBootstrapProfileFactory getBootstrapProfileFactory()
   {
      return profileFactory;
   }
   
   public void setBootstrapProfileFactory(AbstractBootstrapProfileFactory profileFactory)
   {
      this.profileFactory = profileFactory;
   }
   
   public Map<String, ManagedDeployment> getBootstrapMDs()
   {
      return bootstrapMDs;
   }
   public void setBootstrapMDs(Map<String, ManagedDeployment> bootstrapMDs)
   {
      this.bootstrapMDs = bootstrapMDs;
   }

   /**
    * 
    */
   public void start(Server server)
      throws Exception
   {
      shutdown.set(false);

      if(profileService == null)
         throw new IllegalStateException("The ProfileService has not been injected"); 
      log.debug("Using ProfileService: " + profileService);
      if(mainDeployer == null)
         throw new IllegalStateException("The MainDeployer has not been injected"); 
      log.debug("Using MainDeployer: " + mainDeployer);

      // Validate that everything is ok
      mainDeployer.checkComplete();

      // Expose the bootstrap ManagedDeployments
      initBootstrapMDs(server);

      // Load the profiles
      if(this.profileKey == null)
         this.profileKey = new ProfileKey(server.getConfig().getServerName());
      
      // TODO check if there is a predetermined ProfileMetaData attachment
      // Map<String, Object> metaData = server.getMetaData();
      // ProfileMetaData pmd = (ProfileMetaData) metaData.get(ProfileMetaData.class.getName());
 
      // Register the profiles
      Collection<Profile> bootstrapProfiles = profileFactory.createProfiles(profileKey, null);
      for(Profile profile : bootstrapProfiles)
      {
         profileService.registerProfile(profile);
         // Add to loaded profiles
         if(this.profileKey.equals(profile.getKey()) == false)
            this.bootstrapProfiles.add(0, profile.getKey());
      }
      
      // Activate the root profile
      log.info("Loading profile: " + this.profileKey);
      this.profileService.activateProfile(this.profileKey);
      this.profileService.validateProfile(this.profileKey);
      
      try
      {
         // Check if everything is complete
         mainDeployer.checkComplete();
      }
      catch (IncompleteDeploymentException e)
      {
         log.error("Failed to load profile: " + e.getMessage());
      }
      catch (Exception e)
      {
         log.error("Failed to load profile: ", e);
      }
      
      // Enable modification checks on all mutable profiles 
      for(ProfileKey key : profileService.getActiveProfileKeys())
      {
         try
         {
            Profile profile = profileService.getActiveProfile(key);
            if(profile.isMutable() && profile instanceof MutableProfile)
               ((MutableProfile) profile).enableModifiedDeploymentChecks(true);
         }
         catch(NoSuchProfileException ignore) { }
      }
   }

   public void prepareShutdown(Server server)
   {
      shutdown.set(true);
      if (mainDeployer != null)
         mainDeployer.prepareShutdown();
   }

   public void shutdown(Server server)
   {
      // Disable modification checks on all mutable profiles
      for(ProfileKey key : profileService.getActiveProfileKeys())
      {
         try
         {
            Profile profile = profileService.getActiveProfile(key);
            if(profile.isMutable() && profile instanceof MutableProfile)
               ((MutableProfile) profile).enableModifiedDeploymentChecks(false);
         }
         catch(NoSuchProfileException ignore) { }
      }
      
      
      // Deactivate the root profile
      try
      {
         // Release 
         if(profileService.getActiveProfileKeys().contains(profileKey))
            profileService.deactivateProfile(profileKey);
      }
      catch(Throwable t)
      {
         log.warn("Error deactivating profile: " + this.profileKey, t);
      }
      try
      {
         // Unregister
         if(profileService.getProfileKeys().contains(profileKey))
            profileService.unregisterProfile(profileKey);
      }
      catch(Throwable t)
      {
         log.warn("Error unregistering profile: " + this.profileKey, t);         
      }
      
      // Deactivate all profiles we registered
      deactivateProfiles(this.bootstrapProfiles);
      
      // Deactivate all still active profiles
      deactivateProfiles(this.profileService.getActiveProfileKeys());
      
      // Unregister all profiles at once
      for(ProfileKey key : profileService.getProfileKeys())
      {
         try
         {
            profileService.unregisterProfile(key);
         }
         catch(Throwable t)
         {
            // Ignore
         }
      }
      
      try
      {
         mainDeployer.shutdown();
      }
      catch (Throwable t)
      {
         log.warn("Error shutting down the main deployer", t);
      }
   }
   
   public KernelRegistryEntry getEntry(Object name)
   {
      KernelRegistryEntry entry = bootstrapEntries.get(name);
      return entry;
   }

   protected void deactivateProfiles(Collection<ProfileKey> profiles)
   {
      if(profiles != null && profiles.isEmpty() == false)
      {
         for(ProfileKey key : profiles)
         {
            try
            {
               profileService.deactivateProfile(key);
            }
            catch(NoSuchProfileException e)
            {
               // ignore
            }
            catch(Throwable t)
            {
               log.warn("Error unloading profile: " + this.profileKey, t);
            }
         }
      }
   }

   /**
    * Create ManagedDeployments for the MCServer KernelDeployments. This allows
    * the bootstrap deployments outside of the profile service to be visible in
    * the ManagementView
    * @see {@link ManagementView}
    * 
    * @param server - the Bootstrap.start Server instance. This must be an
    * MCServer in order for there to be KernelDeployments available.
    */
   protected void initBootstrapMDs(Server server)
   {
      if(mof == null || mgtDeploymentCreator == null)
      {
         log.warn("Skipping ManagedDeployment creation due to missing mof, mgtDeploymentCreator");
         return;
      }

      Map<String, KernelDeployment> serverDeployments = null;
      if(server instanceof MCServer)
      {
         // Build ManagedDeployments for the KernelDeployments
         MCServer mcserver = MCServer.class.cast(server);
         Kernel kernel = mcserver.getKernel();
         serverDeployments = mcserver.getDeployments();
         ManagedDeployment firstDeployment = null;
         for(KernelDeployment kd : serverDeployments.values())
         {
            BootstrapDeployment deploymentUnit = new BootstrapDeployment(kd);
            KernelDeploymentVisitor visitor = new KernelDeploymentVisitor();
            try
            {
               visitor.deploy(deploymentUnit, kd);
            }
            catch(DeploymentException e)
            {
               log.debug("Failed to build ManagedDeployment for: "+kd, e);
               continue;
            }

            /* Create minimal deployment ManagedObject. Don't use the ManagedObjectFactory
             * as this will create ManagedObjects for the beans via the beansFactory
             * property. We handle the beans below.
            */
            Set<ManagedProperty> kdProperties = new HashSet<ManagedProperty>();
            HashSet<ManagedOperation> ops = null;
            ManagedObject kdMO = new ManagedObjectImpl(kd.getName(), "",
                  KernelDeployment.class.getName(),
                  kdProperties, ops, (Serializable) kd);
            Map<String, ManagedObject> kdMOs = new HashMap<String, ManagedObject>();
            kdMOs.put(kd.getName(), kdMO);

            // Traverse the deployment components 
            for(DeploymentUnit compUnit : deploymentUnit.getComponents())
            {
               BeanMetaData bmd = compUnit.getAttachment(BeanMetaData.class);
               ManagedObject bmdMO = mof.initManagedObject(bmd, compUnit.getMetaData());
               if(bmdMO == null)
                  continue;

               Map<String, Annotation> moAnns = bmdMO.getAnnotations();
               ManagementObject mo = (ManagementObject) moAnns.get(ManagementObject.class.getName());
               // Reset the name to the bean name rather than the attachment name
               if(bmdMO instanceof MutableManagedObject)
               {
                  MutableManagedObject mmo = (MutableManagedObject) bmdMO;
                  // Reset the name to the bean name if its the attachment name
                  if(mmo.getName().equals(mmo.getAttachmentName()))
                     mmo.setName(bmd.getName());
                  mmo.setParent(kdMO);
                  // Add an alias property
                  Set<Object> bmdAliases = bmd.getAliases();
                  Map<String, ManagedProperty> oldProps = mmo.getProperties();
                  Map<String, ManagedProperty> newProps = new HashMap<String, ManagedProperty>(oldProps);
                  if(bmdAliases != null && bmdAliases.size() > 0)
                  {
                     ArrayMetaType aliasType = new ArrayMetaType(SimpleMetaType.STRING, false);
                     DefaultFieldsImpl fields = getFields("alias", aliasType);
                     fields.setDescription("Aliases of the bean");
                     String[] aliases = new String[bmdAliases.size()];
                     Iterator<?> i = bmdAliases.iterator();
                     for(int n = 0; i.hasNext(); n++)
                     {
                        aliases[n] = i.next().toString();
                     }
                     ArrayValueSupport value = new ArrayValueSupport(aliasType, aliases);
                     fields.setValue(value);
                     ManagedPropertyImpl aliasesMP = new ManagedPropertyImpl(bmdMO, fields);
                     newProps.put("alias", aliasesMP);
                  }
                  // Add a state property
                  DefaultFieldsImpl stateFields = getFields("state", ControllerStateMetaType.TYPE);
                  stateFields.setViewUse(new ViewUse[]{ViewUse.STATISTIC});
                  EnumValue stateValue = getState(bmd.getName(), kernel);
                  stateFields.setValue(stateValue);
                  stateFields.setDescription("The bean controller state");
                  ManagedPropertyImpl stateMP = new ManagedPropertyImpl(mmo, stateFields);
                  newProps.put("state", stateMP);
                  // Update the properties
                  mmo.setProperties(newProps);
               }
               log.debug("Created ManagedObject: "+bmdMO+" for bean: "+bmd.getName());
               kdMOs.put(bmd.getName(), bmdMO);

            }
            // Create the ManagedDeployment
            ManagedDeployment md = mgtDeploymentCreator.build(deploymentUnit, kdMOs, null);
            if(firstDeployment == null)
               firstDeployment = md;
            // Create the ManagedComponents
            for(ManagedObject bmdMO : kdMOs.values())
            {
               if(bmdMO.getAttachmentName().equals(KernelDeployment.class.getName()))
                  continue;

               ComponentType type = KnownComponentTypes.MCBean.Any.getType();
               Map<String, Annotation> moAnns = bmdMO.getAnnotations();
               ManagementComponent mc = (ManagementComponent) moAnns.get(ManagementComponent.class.getName());
               if(mc != null)
               {
                  type = new ComponentType(mc.type(), mc.subtype());
               }
               ManagedComponentImpl comp = new ManagedComponentImpl(type, md, bmdMO);
               md.addComponent(bmdMO.getName(), comp);
               log.debug("Created ManagedComponent("+comp.getName()+") of type: "
                     +type
                     +" for MO: "+bmdMO.getName()
                     +", componentName: "+bmdMO.getComponentName());               
            }

            if(md != null)
               bootstrapMDs.put(kd.getName(), md);
         }

         // Add other Server managed objects
         if(firstDeployment != null)
         {
            ComponentType type = new ComponentType("MCBean", "MCServer");
            ManagedObject serverMO = mof.initManagedObject(mcserver, null);
            if (serverMO.getOperations() != null && serverMO.getOperations().size() == 0)
            {
               ManagedOperationImpl shutdown = new ManagedOperationImpl("Shutdown the server", "shutdown");
               if(serverMO instanceof MutableManagedObject)
               {
                  HashSet<ManagedOperation> ops = new HashSet<ManagedOperation>();
                  ops.add(shutdown);
                  MutableManagedObject mmo = MutableManagedObject.class.cast(serverMO);
                  mmo.setOperations(ops);
               }
            }
            ManagedComponentImpl serverComp = new ManagedComponentImpl(type, firstDeployment, serverMO);
            firstDeployment.addComponent("MCServer", serverComp);
            try
            {
               BeanInfo mcserverInfo = configurator.getBeanInfo(mcserver.getClass());
               BeanKernelRegistryEntry entry = new BeanKernelRegistryEntry(mcserver, mcserverInfo);
               bootstrapEntries.put(serverComp.getComponentName(), entry);
            }
            catch(Throwable t)
            {
               log.error("Failed to create BeanInfo for: "+serverComp, t);
            }
            // ServerConfig
            type = new ComponentType("MCBean", "ServerConfig");
            ServerConfig config = mcserver.getConfig();
            ManagedObject mo = mof.initManagedObject(config, null);
            ManagedComponentImpl configComp = new ManagedComponentImpl(type, firstDeployment, mo);
            firstDeployment.addComponent("ServerConfig", configComp);
            log.debug("Created ManagedComponent of type: "+type+" for ServerConfig");
         }
      }
   }

   /**
    * Create a DefaultFieldsImpl for the given property name and type
    * @param name - the property name
    * @param type - the property type
    * @return return the fields implementation
    */
   protected DefaultFieldsImpl getFields(String name, MetaType type)
   {
      DefaultFieldsImpl fields = new DefaultFieldsImpl();
      fields.setMetaType(type);
      fields.setName(name);
      fields.setField(Fields.MAPPED_NAME, name);
      fields.setMandatory(false);

      return fields;
   }

   /**
    * Get the state of a bean
    * 
    * @param name the bean name
    * @return state enum value
    */
   protected EnumValue getState(Object name, Kernel kernel)
   {
      KernelController controller = kernel.getController();
      ControllerContext context = controller.getContext(name, null);
      if (context == null)
         throw new IllegalStateException("Context not installed: " + name);

      ControllerState state = context.getState();
      return new EnumValueSupport(ControllerStateMetaType.TYPE, state.getStateString());
   }
}
