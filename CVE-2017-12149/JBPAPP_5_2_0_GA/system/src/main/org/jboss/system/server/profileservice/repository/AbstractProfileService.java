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
package org.jboss.system.server.profileservice.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.dependency.plugins.AbstractController;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerContextActions;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.ControllerStateModel;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.util.JBossStringBuilder;

/**
 * The ProfileService.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 91182 $
 */
public class AbstractProfileService implements ProfileService, ControllerContextActions
{
   /** The RuntimePermission required for accessing PS methods */
   private static RuntimePermission PS_RUNTIME_PERMISSION = new RuntimePermission(ProfileService.class.getName());

   /** The default profile. */
   private ProfileKey defaultProfile;
   
   /** The registered profiles. */
   private List<ProfileKey> profiles = new CopyOnWriteArrayList<ProfileKey>();
   
   /** The active profiles. */
   private List<ProfileKey> activeProfiles = new CopyOnWriteArrayList<ProfileKey>();
   
   /** The deployment manager. */
   private DeploymentManager deploymentManager;
   
   /** The management view. */
   private ManagementView managementView;
   
   /** The main deployer. */
   private MainDeployerAdapter deployer;
   
   /** The controller. */
   private Controller controller;
   
   /** The deploy state */
   public static final ControllerState DEPLOY_STATE = new ControllerState("Deploy"); 
   
   /** The profileActions. */
   private Map<ControllerState, AbstractProfileAction> profileActions = new HashMap<ControllerState, AbstractProfileAction>();
   
   /** The Logger. */
   private final static Logger log = Logger.getLogger(AbstractProfileService.class);
   
   public AbstractProfileService(AbstractController controller)
   {
      if(controller == null)
         throw new IllegalArgumentException("Null controller.");
      // Create a scoped controller
      this.controller = new ScopedProfileServiceController(controller);
   }
   
   public ProfileKey getDefaultProfile()
   {
      return defaultProfile;
   }
   
   public void setDefaultProfile(ProfileKey defaultProfile)
   {
      this.defaultProfile = defaultProfile;
   }
   
   public MainDeployerAdapter getDeployer()
   {
      return deployer;
   }
   
   public void setDeployer(MainDeployerAdapter deployer)
   {
      this.deployer = deployer;
   }

   public DeploymentManager getDeploymentManager()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      return this.deploymentManager;
   }
   
   public void setDeploymentManager(DeploymentManager deploymentManager)
   {
      this.deploymentManager = deploymentManager;
   }

   public ManagementView getViewManager()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      return this.managementView;
   }

   public void setViewManager(ManagementView managementView)
   {
      this.managementView = managementView;
   }
   
   public String[] getDomains()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      // TODO do we need that ? 
      Collection<String> domains = new ArrayList<String>();
      for(ProfileKey key : activeProfiles)
         domains.add(key.getDomain());
      return domains.toArray(new String[domains.size()]);
   }
   
   public String[] getProfileDeploymentNames(ProfileKey key) throws NoSuchProfileException
   {
      Profile profile = getActiveProfile(key);
      Collection<String> deploymentNames = profile.getDeploymentNames();
      return deploymentNames.toArray(new String[deploymentNames.size()]);
   }

   public Collection<ProfileKey> getProfileKeys()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      return Collections.unmodifiableCollection(this.profiles);
   }
   
   /**
    * Obtain the registered profile for the key.
    * 
    * @param key - the key for the profile
    * @return the matching profile.
    * @throws NoSuchProfileException if there is no such profile registered.
    */
   public Profile getProfile(ProfileKey key) throws NoSuchProfileException
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      if(key ==  null)
         throw new IllegalArgumentException("Null profile key.");

      // Get the profile
      ProfileContext profile = null;
      if(this.profiles.contains(key))
         profile = (ProfileContext) this.controller.getContext(key, null);
      
      // If the key is the default, fallback to the injected default key
      if(profile == null && key.isDefaultKey() && this.defaultProfile != null)
         profile = (ProfileContext) controller.getContext(this.defaultProfile, null);   
         
      if(profile == null)
         throw new NoSuchProfileException("No such profile: " + key);
      
      return profile.getProfile();
   }
   
   public Collection<ProfileKey> getActiveProfileKeys()
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      return Collections.unmodifiableCollection(this.activeProfiles);
   }
   
   /**
    * Obtain the active profile for the key.
    * 
    * @param key - the key for the profile
    * @return the matching active profile.
    * @throws NoSuchProfileException if there is no such profile active.
    */
   public Profile getActiveProfile(ProfileKey key) throws NoSuchProfileException
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      if(key ==  null)
         throw new IllegalArgumentException("Null profile key.");

      // Get the profile
      ProfileContext profile = null;
      if(this.activeProfiles.contains(key))
         profile = (ProfileContext) this.controller.getInstalledContext(key);
      
      // If the key is the default, fallback to the injected default key
      if(profile == null && key.isDefaultKey() && this.defaultProfile != null)
         profile = (ProfileContext) controller.getInstalledContext(this.defaultProfile);   
         
      if(profile == null)
         throw new NoSuchProfileException("No such profile: " + key);
      
      return profile.getProfile();
   }
   
   /**
    * Create the profile service actions.
    * 
    * @throws Exception
    */
   public void create() throws Exception
   {
      if(this.controller == null)
         throw new IllegalStateException("Null controller.");
      if(this.deployer == null)
         throw new IllegalStateException("Null deployer.");
      
      // TODO this should be moved to static actions
      this.profileActions.put(ControllerState.CREATE, new ProfileCreateAction());
      this.profileActions.put(ControllerState.START, new ProfileStartAction());
      this.profileActions.put(DEPLOY_STATE, new ProfileDeployAction(deployer));
      this.profileActions.put(ControllerState.INSTALLED, new ProfileInstallAction());
   }
   
   /**
    * Destroy the profileService.
    * 
    */
   public void destroy()
   {
      //
      this.profileActions.clear();
   }
   
   /**
    * Register a Profile.
    * 
    * @param profile the profile.
    * @throws Exception
    */
   public void registerProfile(Profile profile) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      if(profile == null)
         throw new IllegalArgumentException("Null profile.");
      
      ProfileKey key = profile.getKey();
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(this.profiles.contains(key))
      {
         log.debug("Profile already registered: " + profile);
         return;
      }
      
      if(controller.isShutdown())
         throw new IllegalStateException("Controller is shutdown.");
      
      log.debug("registering profile: " + profile);
      ProfileContext context = new ProfileContext(profile, this);
      try
      {
         controller.install(context);
         this.profiles.add(key);
      }
      catch(Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
   
   /**
    * Activate a registered profile.
    * 
    * @param key the profile key.
    * @throws NoSuchProfileException if there is no such profile registered. 
    * @throws Exception
    */
   public void activateProfile(ProfileKey key) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(controller.isShutdown())
         throw new IllegalStateException("Controller is shutdown.");
      if(this.activeProfiles.contains(key))
         return;

      ProfileContext context = (ProfileContext) this.controller.getContext(key, null);
      if(context == null)
         throw new NoSuchProfileException("No such profile: "+ key);

      try
      {
         log.debug("Activating profile: " + context.getProfile());
         controller.change(context, ControllerState.INSTALLED);         
      }
      catch(Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
   
   public void validateProfile(ProfileKey key) throws Exception
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);

      if(key ==  null)
         throw new IllegalArgumentException("Null profile key.");

      // Get the profile
      ProfileContext profile = null;
      if(this.profiles.contains(key))
         profile = (ProfileContext) this.controller.getContext(key, null);
      
      // If the key is the default, fallback to the injected default key
      if(profile == null && key.isDefaultKey() && this.defaultProfile != null)
         profile = (ProfileContext) controller.getContext(this.defaultProfile, null);   
         
      if(profile == null)
         throw new NoSuchProfileException("No such profile: " + key);
      
      validate(profile);
   }
   
   /**
    * Check if all dependencies are satisfied and the profile was installed successfully.
    * 
    * @param context the context to validate
    * @throws Exception
    */
   protected void validate(ControllerContext context) throws Exception
   {
      // 
      Set<String> errors = new HashSet<String>();
      Map<Object, String> map = new HashMap<Object, String>();
      // Validate the context, with it's dependencies
      internalValidateContext(context, errors, map);
      // Create and throw the Exception
      logErrors(errors, map.values());
   }
   
   public void install(ControllerContext context, ControllerState fromState, ControllerState toState) throws Throwable
   {
      if(context instanceof ProfileContext == false)
      {
         return;
      }

      AbstractProfileAction action = this.profileActions.get(toState);
      if(action != null)
      {
         action.install((ProfileContext) context);
      }
   }
   
   public void uninstall(ControllerContext context, ControllerState fromState, ControllerState toState)
   {
      if(context instanceof ProfileContext == false)
      {
         return;
      }

      AbstractProfileAction action = this.profileActions.get(fromState);
      if(action != null)
      {
         action.uninstall((ProfileContext) context);
      }
   }

   /**
    * Deactivate the profile.
    * 
    * @param key the profile key.
    * @throws NoSuchProfileException if the profile is not active.
    */
   public void deactivateProfile(ProfileKey key) throws NoSuchProfileException
   {
      SecurityManager sm = System.getSecurityManager();
      if(sm != null)
         sm.checkPermission(PS_RUNTIME_PERMISSION);
      
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      
      if(this.activeProfiles.contains(key) == false)
         throw new NoSuchProfileException("No active profile for: " + key);

      if(controller.isShutdown())
         return;
      
      ControllerContext context = controller.getInstalledContext(key);
      if(context == null)
         throw new IllegalStateException("Profile not installed: "+ key);
      try
      {
         log.debug("deactivating profile: " + key);
         controller.change(context, ControllerState.NOT_INSTALLED);
      }
      catch(Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
   
   /**
    * Unregister a profile.
    * 
    * @param key the profile key
    * @throws NoSuchProfileException if the profile is not registered.
    */
   public void unregisterProfile(ProfileKey key) throws NoSuchProfileException
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      
      if(this.activeProfiles.contains(key))
         throw new IllegalStateException("Cannot unregister active profile: "+ key);
      
      if(this.profiles.contains(key) == false)
         throw new NoSuchProfileException("Profile not registered: " + key);
      
      log.debug("unregistering profile: " + key);
      if(controller.isShutdown())
         return;
      
      controller.uninstall(key);
      this.profiles.remove(key);
   }
   
   /**
    * Delegates to unregisterProfile(ProfileKey)
    * 
    * @param profile
    * @throws NoSuchProfileException if the profile is not registered.
    */
   public void unregisterProfile(Profile profile) throws NoSuchProfileException
   {
      if(profile == null)
         throw new IllegalArgumentException("Null profile.");
      
      unregisterProfile(profile.getKey());
   }
   
   /**
    * Validate the context and create the error messages if needed.
    * 
    * TODO maybe recurse into dependent contexts.
    * 
    * @param ctx the context to validate
    * @param errors a set of errors
    * @param incomplete a set of incomplete contexts
    */
   protected void internalValidateContext(ControllerContext ctx, Set<String> errors, Map<Object, String> incomplete)
   {
      if (ctx.getState().equals(ControllerState.ERROR))
      {
         JBossStringBuilder builder = new JBossStringBuilder();
         builder.append("Profile: ").append(ctx.getName());
         builder.append(" in error due to ").append(ctx.getError().toString()); 
         errors.add(builder.toString());
      }
      else
      {
         Object name = ctx.getName();
         if(incomplete.containsKey(name))
            return;
         
         DependencyInfo dependsInfo = ctx.getDependencyInfo();
         Set<DependencyItem> depends = dependsInfo.getIDependOn(null);
         for (DependencyItem item : depends)
         {
            ControllerState dependentState = item.getDependentState();
            if (dependentState == null)
               dependentState = ControllerState.INSTALLED;
            
            ControllerState otherState = null;
            ControllerContext other = null; 
            Object iDependOn = item.getIDependOn();

            if (name.equals(iDependOn) == false)
            {
               if (iDependOn != null)
               {
                  other = controller.getContext(iDependOn, null);
                  if (other != null)
                     otherState = other.getState();
               }

               boolean print = true;
               if (otherState != null && otherState.equals(ControllerState.ERROR) == false)
               {
                  ControllerStateModel states = controller.getStates();
                  if (states.isBeforeState(otherState, dependentState) == false)
                     print = false;
               }

               if (print)
               {
                  JBossStringBuilder buffer = new JBossStringBuilder();
                  buffer.append(name).append(" is missing following dependencies: ");

                  buffer.append(iDependOn).append('{').append(dependentState.getStateString());
                  buffer.append(':');
                  if (iDependOn == null)
                  {
                     buffer.append("** UNRESOLVED " + item.toHumanReadableString() + " **");
                  }
                  else
                  {
                     if (other == null)
                        buffer.append("** NOT FOUND **");
                     else
                        buffer.append(otherState.getStateString());
                  }
                  buffer.append('}');

                  // Add Error message and check other context.
                  incomplete.put(name, buffer.toString());
                  if(other!= null && incomplete.containsKey(other) == false)
                  {
                     internalValidateContext(other, errors, incomplete);
                  }
               }
            }
         }  
      }
   }
   
   /**
    * This method just groups the errors and incomplete messages and throws an
    * Exception if there are errors or missing dependencies.
    * 
    * @param errors a set of errors
    * @param incomplete a set of missing dependencies
    * @throws Exception in case there are errors or missing dependencies
    */
   protected void logErrors(Set<String> errors, Collection<String> incomplete) throws Exception
   {
      if(errors.isEmpty() && incomplete.isEmpty())
         return;

      JBossStringBuilder buffer = new JBossStringBuilder();
      buffer.append("Incompletely deployed:\n");
      
      // Append errors
      if(errors.size() != 0)
      {
         buffer.append("\n*** PROFILES IN ERROR: Name -> Error\n");
         for(String error : errors)
            buffer.append(error).append('\n');
      }
      
      // Append missing dependencies
      if(incomplete.size() != 0)
      {
         buffer.append("\n*** PROFILES MISSING DEPENDENCIES: Name -> Dependency{Required State:Actual State}\n");
         for(String missing : incomplete)
            buffer.append(missing).append('\n');
      }
      
      // Fail
      throw new IllegalStateException(buffer.toString());
   }
   
   /**
    * A simple lifecycle action to add/remove a profile to the activeProfiles.  
    */
   private class ProfileInstallAction extends AbstractProfileAction
   {
      public void install(Profile profile) throws Exception
      {
         // activate profile
         activeProfiles.add(0, profile.getKey());
      }
      public void uninstall(Profile profile)
      {
         // release profile
         activeProfiles.remove(profile.getKey());
      }
   }
   
}
