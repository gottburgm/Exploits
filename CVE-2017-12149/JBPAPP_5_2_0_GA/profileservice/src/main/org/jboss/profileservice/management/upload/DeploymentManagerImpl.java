/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.management.upload;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentTarget;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus.CommandType;
import org.jboss.logging.Logger;
import org.jboss.profileservice.management.client.upload.DeploymentProgressImpl;
import org.jboss.profileservice.management.client.upload.SerializableDeploymentID;
import org.jboss.profileservice.management.client.upload.StreamingDeploymentTarget;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileRepository;
import org.jboss.remoting.InvokerLocator;

/**
 * The remoting base DeploymentManager implementation.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 91161 $
 */
public class DeploymentManagerImpl implements DeploymentManager
{
   /** The logger. */
   private static Logger log = Logger.getLogger(DeploymentManagerImpl.class);
   
   /** The bundle name. */
   private static final String BUNDLE_NAME = "org.jboss.profileservice.management.upload.messages"; //$NON-NLS-1$

   /** The profile repository. */
   private ProfileRepository profileRepository;
   
   /** The default profile key to upload contents. */
   private ProfileKey defaultKey;
   /** The loaded profile key. */
   private ProfileKey activeProfileKey;

   /** The resource bundle. */
   private ResourceBundle i18n;
   /** The current locale. */
   private Locale currentLocale;
   /** The message formatter. */
   private MessageFormat formatter = new MessageFormat("");
   
   /** The invoker locator. */
   private InvokerLocator locator;
   /** The remoting subSystem. */
   private String remotingSubsystem = "DeploymentManager";

   public DeploymentManagerImpl()
   {
      currentLocale = Locale.getDefault();
      formatter.setLocale(currentLocale);
      i18n = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
   }

   public ProfileKey getDefaultProfileKey()
   {
      return defaultKey;
   }
   public void setDefaultProfileKey(ProfileKey defaultKey)
   {
      this.defaultKey = defaultKey;
   }
   
   public ProfileRepository getProfileRepository()
   {
      return profileRepository;
   }
   
   public void setProfileRepository(ProfileRepository profileRepository)
   {
      this.profileRepository = profileRepository;
   }

   public InvokerLocator getLocator()
   {
      return locator;
   }
   public void setLocator(InvokerLocator locator)
   {
      this.locator = locator;
   }

   public String getRemotingSubsystem()
   {
      return remotingSubsystem;
   }
   public void setRemotingSubsystem(String remotingSubsystem)
   {
      this.remotingSubsystem = remotingSubsystem;
   }

   /**
    * Get the available profiles with a DeploymentRepository.
    */
   public Collection<ProfileKey> getProfiles()
   {
      return new ArrayList<ProfileKey>(this.profileRepository.getProfileKeys()); 
   }
   
   public DeploymentProgress distribute(String name, URL contentURL)
      throws Exception
   {
      return distribute(name, contentURL, true);
   }

   public DeploymentProgress distribute(String name, URL contentURL, boolean copyContent)
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      if(contentURL == null)
         throw new IllegalArgumentException("Null content url.");
      
      if(getTargetProfile() == null)
      {
         formatter.applyPattern(i18n.getString("DeploymentManager.NoProfileLoadedException")); //$NON-NLS-1$
         Object[] args = {};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }

      List<DeploymentTarget> targets = getDeploymentTargets();
      SerializableDeploymentID deployment = new SerializableDeploymentID(name, getTargetProfile(), contentURL.toString());
      deployment.setContentURL(contentURL);
      deployment.setCopyContent(copyContent);
      return new DeploymentProgressImpl(targets, deployment, CommandType.DISTRIBUTE);
   }
   
   public DeploymentProgress distribute(String name, URL contentURL, DeploymentOption... options) throws Exception
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      if(contentURL == null)
         throw new IllegalArgumentException("Null content url.");
      if(options == null)
         options = new DeploymentOption[0];

      if(getTargetProfile() == null)
      {
         formatter.applyPattern(i18n.getString("DeploymentManager.NoProfileLoadedException")); //$NON-NLS-1$
         Object[] args = {};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }
      
      List<DeploymentTarget> targets = getDeploymentTargets();
      SerializableDeploymentID deployment = new SerializableDeploymentID(name, getTargetProfile(), contentURL.toString());
      deployment.setContentURL(contentURL);
      for(DeploymentOption option : options)
         deployment.addDeploymentOption(option);
      return new DeploymentProgressImpl(targets, deployment, CommandType.DISTRIBUTE);
   }

   public String[] getRepositoryNames(String[] names) throws Exception
   {
      List<DeploymentTarget> targets = getDeploymentTargets();
      SerializableDeploymentID deployment = new SerializableDeploymentID(names, getTargetProfile(), null);
      return targets.get(0).getRepositoryNames(deployment);
   }
   
   public boolean isRedeploySupported()
   {
      return (getTargetProfile() != null);
   }
   
   public void loadProfile(ProfileKey key) throws NoSuchProfileException
   {
      // Override a DEFAULT key with the injected default
      if(key.isDefaultKey() && this.defaultKey != null)
         key = this.defaultKey;
      
      // Check if we have a associated DeploymentRepository
      checkProfile(key);
      
      // Set the key
      this.activeProfileKey = key;      
   }
   
   public void releaseProfile()
   {
      this.activeProfileKey = null;
   }

   public DeploymentProgress redeploy(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      
      if(getTargetProfile() == null)
      {
         formatter.applyPattern(i18n.getString("DeploymentManager.NoProfileLoadedException")); //$NON-NLS-1$
         Object[] args = {};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }

      List<DeploymentTarget> targets = getDeploymentTargets();
      SerializableDeploymentID deployment = new SerializableDeploymentID(name, getTargetProfile(), null);
      return new DeploymentProgressImpl(targets, deployment, CommandType.REDEPLOY);
   }

   public DeploymentProgress prepare(String... names) throws Exception
   {
      return doProgress(CommandType.PREPARE, names);
   }

   public DeploymentProgress start(String... names) 
   {
      if(names == null)
         throw new IllegalArgumentException("Null names.");
      
      return doProgress(CommandType.START, names);
   }

   public DeploymentProgress stop(String... names) 
   {
      if(names == null)
         throw new IllegalArgumentException("Null names.");
      
      return doProgress(CommandType.STOP, names);
   }

   public DeploymentProgress remove(String... names)
   {
      if(names == null)
         throw new IllegalArgumentException("Null names.");
      
      return doProgress(CommandType.REMOVE, names);
   }

   protected DeploymentProgress doProgress(CommandType type, String... names)
   {
      if(getTargetProfile() == null)
      {
         formatter.applyPattern(i18n.getString("DeploymentManager.NoProfileLoadedException")); //$NON-NLS-1$
         Object[] args = {};
         String msg = formatter.format(args);
         throw new IllegalStateException(msg);
      }

      if (names == null || names.length == 0)
         log.warn("Null or empty names.");

      List<DeploymentTarget> targets = getDeploymentTargets();
      SerializableDeploymentID deployment = new SerializableDeploymentID(names, getTargetProfile(), null);
      return new DeploymentProgressImpl(targets, deployment, type);
   }

   /**
    * Check if the Profile has a associated DeploymentRepository.
    * This will be needed by the DeployHandler to add the deployment content.
    * 
    * @param key the profile key
    * @throws NoSuchProfileException if the profile does not exist or is not mutable
    */
   public void checkProfile(ProfileKey key) throws NoSuchProfileException
   {
      if(getProfiles().contains(key) == false)
      {
         formatter.applyPattern(i18n.getString("DeploymentManager.NoMutableProfileException")); //$NON-NLS-1$
         Object[] args = { key };
         String msg = formatter.format(args);
         throw new NoSuchProfileException(msg);
      }
   }
   
   /**
    * Get the target profile to upload contents.
    * If the DeploymentManager was not loaded, the
    * default key is used.
    * 
    * @return the target profile key
    */
   protected ProfileKey getTargetProfile()
   {
      if(this.activeProfileKey == null)
         return this.defaultKey;
      
      return this.activeProfileKey;
   }
   
   /**
    * TODO: should the targets include cluster info
    * @param name
    * @return
    */
   protected List<DeploymentTarget> getDeploymentTargets()
   {
      String targetName = locator.getHost();
      List<DeploymentTarget> targets = new ArrayList<DeploymentTarget>();
      StreamingDeploymentTarget hostTarget = new StreamingDeploymentTarget(locator, targetName, remotingSubsystem);
      targets.add(hostTarget);
      return targets;
   }
   
}
