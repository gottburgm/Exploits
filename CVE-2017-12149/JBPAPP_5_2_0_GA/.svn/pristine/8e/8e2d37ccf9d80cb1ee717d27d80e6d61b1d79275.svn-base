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

import java.text.MessageFormat;
import java.util.ArrayList;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.management.DeploymentTemplate;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.NameMatcher;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.profileservice.management.views.AbstractProfileView;
import org.jboss.profileservice.management.views.BootstrapProfileView;
import org.jboss.profileservice.management.views.PlatformMbeansView;
import org.jboss.profileservice.management.views.ProfileView;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.system.server.profileservice.attachments.AttachmentStore;

/**
 * A aggregating management view, handling profile views for all active profiles.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class AggregatingManagementView extends AbstractTemplateCreator implements ManagementView
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(AggregatingManagementView.class);
   
   /** The bundle name. */
   private static final String BUNDLE_NAME = "org.jboss.profileservice.management.messages";
   
   /** The internationalization resource bundle. */
   private ResourceBundle i18n;
   /** the Locale for the i18n messages. */
   private Locale currentLocale;
   /** The formatter used for i18n messages. */
   private MessageFormat formatter = new MessageFormat("");
   
   /** The profile service. */
   private ProfileService ps;
   
   /** The runtime component dispatcher. */
   private RuntimeComponentDispatcher dispatcher;
   private ManagedOperationProxyFactory proxyFactory;
   
   /** The main deployer. */
   private MainDeployer mainDeployer;
   
   /** The attachment store. */
   private AttachmentStore store;
   
   /** The bootstrap deployment name to ManagedDeployment map. */
   private Map<String, ManagedDeployment> bootstrapManagedDeployments = Collections.emptyMap(); 

   /** The deployment templates that have been registered with the MV. */
   private HashMap<String, DeploymentTemplate> templates = new HashMap<String, DeploymentTemplate>();
   
   /** The profile views. */
   private Map<ProfileKey, AbstractProfileView> profileViews = new ConcurrentHashMap<ProfileKey, AbstractProfileView>();
   
   public AggregatingManagementView()
   {
      currentLocale = Locale.getDefault();
      formatter.setLocale(currentLocale);
      i18n = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
   }
   
   public RuntimeComponentDispatcher getDispatcher()
   {
      return dispatcher;
   }
   
   public void setDispatcher(RuntimeComponentDispatcher dispatcher)
   {
      this.dispatcher = dispatcher;
   }
   
   public ProfileService getProfileService()
   {
      return ps;
   }

   public void setProfileService(ProfileService ps)
   {
      this.ps = ps;
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
   }
   
   public Map<String, ManagedDeployment> getBootstrapManagedDeployments()
   {
      return bootstrapManagedDeployments;
   }
   
   public void setBootstrapManagedDeployments(Map<String, ManagedDeployment> bootstrapManagedDeployments)
   {
      this.bootstrapManagedDeployments = bootstrapManagedDeployments;
   }
   
   public void start() throws Exception
   {
      if(this.proxyFactory == null)
         throw new IllegalStateException("proxy factory not injected");
      
      // Add the platform MBeans
      addView(new PlatformMbeansView(this.proxyFactory));
      
      // Add the bootstrap deployments
      if(this.bootstrapManagedDeployments != null)
      {
         addView(new BootstrapProfileView(this.proxyFactory,
                     this.bootstrapManagedDeployments.values()));
      }
   }
   
   public boolean load()
   {
      return loadProfiles(false);
   }
   
   public void reload()
   {
      loadProfiles(true);
   }
   
   public void process() throws Exception
   {
      // FIXME process
   }
   
   public void addView(AbstractProfileView view)
   {
      if(view == null)
         throw new IllegalArgumentException("null view");
      if(view.getProfileKey() == null)
         throw new IllegalArgumentException("null profile key");
      
      this.profileViews.put(view.getProfileKey(), view);
      log.debug("add view: " + view);
   }

   public void removeView(AbstractProfileView view)
   {
      if(view == null)
         throw new IllegalArgumentException("null view");
      if(view.getProfileKey() == null)
         throw new IllegalArgumentException("null profile key");
      
      this.profileViews.remove(view.getProfileKey());
      log.debug("remove view: " + view);
   }
   
   public void addTemplate(DeploymentTemplate template)
   {
      this.templates.put(template.getInfo().getName(), template);
      log.debug("addTemplate: " + template);
   }

   public void removeTemplate(DeploymentTemplate template)
   {
      this.templates.remove(template.getInfo().getName());
      log.debug("removeTemplate: " + template);
   }

   public void applyTemplate(String deploymentBaseName, DeploymentTemplateInfo info) throws Exception
   {
      if(deploymentBaseName == null)
         throw new IllegalArgumentException("Null deployment base name.");
      if(info == null)
         throw new IllegalArgumentException("Null template info.");
      
      DeploymentTemplate template = templates.get(info.getName());
      if( template == null )
      {
         formatter.applyPattern(i18n.getString("ManagementView.NoSuchTemplate"));
         Object[] args = {info.getName()};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }

      // Create a deployment base from the template
      if( log.isTraceEnabled() )
         log.trace("applyTemplate, deploymentBaseName=" + deploymentBaseName + ", info=" + info);
      
      // Apply the template
      super.applyTemplate(template, deploymentBaseName, info);
      
      // reload this profile
      this.profileViews.put(getDefaulProfiletKey(), createProfileView(getDefaulProfiletKey()));
   }

   public ManagedComponent getComponent(String name, ComponentType type) throws Exception
   {
      Set<ManagedComponent> components = getComponentsForType(type);
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
         log.debug("Component"
               +"(ops.size="
               +ops != null ? ops.size() : 0
               +",props.size=)"
               +props != null ? props.size() : 0);
      }
      return comp;
   }

   
   public Set<ComponentType> getComponentTypes()
   {
      Set<ComponentType> componentTypes = new HashSet<ComponentType>();
      for(AbstractProfileView view : profileViews.values())
      {
         componentTypes.addAll(view.getComponentTypes());
      }
      return componentTypes;
   }

   public Set<ManagedComponent> getComponentsForType(ComponentType type) throws Exception
   {
      Set<ManagedComponent> components = new HashSet<ManagedComponent>();
      for(AbstractProfileView view : profileViews.values())
      {
         components.addAll(view.getComponentsForType(type));
      }
      return components;
   }

   public ManagedDeployment getDeployment(String name) throws NoSuchDeploymentException
   {
      List<ManagedDeployment> deployments = new ArrayList<ManagedDeployment>();
      for(AbstractProfileView view : profileViews.values())
      {
         deployments.addAll(view.getDeployment(name));
      }
      if(deployments.size() == 0)
      {
         throw new NoSuchDeploymentException(name);
      }
      else if(deployments.size() > 1)
      {
         throw new NoSuchDeploymentException("multiple matching deployments found for name: "  + name 
               + ", available: " + deployments);
      }
      return deployments.get(0);
   }

   public Set<String> getDeploymentNames()
   {
      Set<String> deploymentNames = new TreeSet<String>();
      for(AbstractProfileView view : profileViews.values())
      {
         deploymentNames.addAll(view.getDeploymentNames());
      }
      return deploymentNames;
   }

   public Set<String> getDeploymentNamesForType(String type)
   {
      Set<String> deploymentNames = new TreeSet<String>();
      for(AbstractProfileView view : profileViews.values())
      {
         deploymentNames.addAll(view.getDeploymentNamesForType(type));
      }
      return deploymentNames;
   }

   public Set<ManagedDeployment> getDeploymentsForType(String type) throws Exception
   {
      Set<ManagedDeployment> deployments = new HashSet<ManagedDeployment>();
      for(AbstractProfileView view : profileViews.values())
      {
         deployments.addAll(view.getDeploymentsForType(type));
      }
      return deployments;
   }

   public Set<ManagedComponent> getMatchingComponents(String name, ComponentType type,
         NameMatcher<ManagedComponent> matcher)
      throws Exception
   {
      Set<ManagedComponent> components = getComponentsForType(type);
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
         log.debug("getComponents matched: "+matched);
      }
      return matched;
   }

   public Set<String> getMatchingDeploymentName(String regex)
      throws NoSuchDeploymentException
   {
      Set<String> names = getDeploymentNames();
      HashSet<String> matches = new HashSet<String>();
      Pattern p = Pattern.compile(regex);
      for(String name : names)
      {
         Matcher m = p.matcher(name);
         if( m.matches() )
            matches.add(name);
      }
      if( matches.size() == 0 )
      {
         formatter.applyPattern(i18n.getString("ManagementView.NoSuchDeploymentException")); //$NON-NLS-1$
         Object[] args = {regex};
         String msg = formatter.format(args);
         throw new NoSuchDeploymentException(msg);
      }
      return matches;
   }

   public Set<ManagedDeployment> getMatchingDeployments(String name, NameMatcher<ManagedDeployment> matcher)
         throws NoSuchDeploymentException, Exception
   {
      // FIXME getMatchingDeployments
      return new HashSet<ManagedDeployment>();
   }

   public DeploymentTemplateInfo getTemplate(String name)
      throws NoSuchDeploymentException
   {
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
      log.debug("getTemplate, "+info);
      return info;
   }

   public Set<String> getTemplateNames()
   {
      return new HashSet<String>(templates.keySet());
   }

   public void removeComponent(ManagedComponent comp) throws Exception
   {
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
      ManagedDeployment compMD = getDeployment(md.getName());
      log.debug("updateComponent, deploymentName="+name+": "+compMD);
      
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
      log.debug("remove component: " + comp + ", deployment: "+ profileDeployment);
      // Remove
      Profile profile = getProfileForDeployment(md.getName());
      this.store.removeComponent(comp.getDeployment().getName(), serverComp);      
      this.profileViews.put(profile.getKey(), createProfileView(profile));
   }

   public void updateComponent(ManagedComponent comp)
      throws Exception
   {
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
      ManagedDeployment compMD = getDeployment(md.getName());
      log.debug("updateComponent, deploymentName="+name+": "+compMD);
      
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
         log.debug("Name: "+comp.getName()+" does not map to existing ManagedComponet in ManagedDeployment: " + md.getName()
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
   //         || prop.isModified() == false
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
      Profile profile = getProfileForDeployment(md.getName());
      this.store.updateDeployment(comp.getDeployment().getName(), serverComp);
      this.profileViews.put(profile.getKey(), createProfileView(profile));
   }

   protected boolean loadProfiles(boolean forceReload)
   {
      boolean wasReloaded = false;
      Collection<ProfileKey> activeProfiles = ps.getActiveProfileKeys();
      for(ProfileKey key : activeProfiles)
      {
         if(loadProfile(key, forceReload))
            wasReloaded = true;
      }
      return wasReloaded;      
   }
   
   protected boolean loadProfile(ProfileKey key, boolean forceReload) 
   {
      boolean wasModified = false;
      try
      {
         // The active profile
         Profile profile = ps.getActiveProfile(key);
         AbstractProfileView view = this.profileViews.get(profile.getKey());
         
         // Check if we need to reload the profile
         wasModified = forceReload 
            || view == null
            || view.hasBeenModified(profile);
         
         if(wasModified)
         {
            this.profileViews.put(key, createProfileView(profile));
            wasModified = true;
         }         
      }
      catch(NoSuchProfileException e)
      {
         wasModified = profileViews.remove(key) != null;
         log.debug("Failed to load profile " + key);
      }
      return wasModified;
   }

   protected AbstractProfileView createProfileView(ProfileKey key) throws NoSuchProfileException
   {
      Profile profile = this.ps.getActiveProfile(key);
      return createProfileView(profile);
   }
   
   protected AbstractProfileView createProfileView(Profile profile)
   {
      return new ProfileView(profile, proxyFactory, mainDeployer);
   }
   
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
   
}
