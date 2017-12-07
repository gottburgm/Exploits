/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ha.singleton;

import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * Bean that activates a configurable Profile when notified by a singleton
 * controller, and releases it when notified.
 * 
 * TODO rename and move to another package, as there is nothing 
 * HASingleton-specific about what this bean does; it just exposes an
 * operation an HASingletonController can use.
 * 
 * @author Brian Stansberry
 * @version $Revision: 86190 $
 */
public class HASingletonProfileActivator implements HASingletonProfileActivatorMBean
{
   /** Default value for {@link #getProfileName()} */
   public static final String DEFAULT_PROFILE_NAME = "deploy-hasingleton";
   
   protected final Logger log = Logger.getLogger(getClass());
   
   /** Whether this node has activated its profile */
   private boolean activated;
   
   /** The profile service key domain */
   private String profileDomain;
   
   /** The profile service key name */
   private String profileServer;
   
   /** The profile service key name */
   private String profileName;
   
   /** The profile service key */
   private ProfileKey profileKey;
   
   /** The profile service */
   private ProfileService profileService;

   // ----------------------------------------------------------- Constructors

   /**
    * Create a new HASingletonProfileActivator.
    */
   public HASingletonProfileActivator()
   {
      super();
   }

   // ------------------------------------------------------------- Properties
   
   /**
    * Gets the ProfileService.
    * 
    * @return the profileService.
    */
   public ProfileService getProfileService()
   {
      return profileService;
   }

   /**
    * Sets the ProfileService reference.
    * 
    * @param profileService the profileService. Cannot be <code>null</code>
    * 
    * @throws IllegalArgumentException if <code>profileService</code> is <code>null</code>
    */
   public void setProfileService(ProfileService profileService)
   {
      if (profileService == null)
      {
         throw new IllegalArgumentException("profileService is null");
      }
      
      this.profileService = profileService;
   }

   /**
    * {@inheritDoc}
    */
   public String getProfileDomain()
   {
      return profileDomain;
   }

   /**
    * Sets the value that should be used for the 
    * {@link ProfileKey#getDomain() domain} portion of
    * the singleton @{link Profile}'s {@link #getProfileKey() ProfileKey}.
    * 
    * @param profileDomain the domain, or <code>null</code>
    */
   public void setProfileDomain(String profileDomain)
   {
      this.profileDomain = profileDomain;
   }

   /**
    * {@inheritDoc}
    */
   public String getProfileServer()
   {
      return profileServer;
   }

   /**
    * Sets the value that should be used for the 
    * {@link ProfileKey#getServer() server} portion of
    * the singleton @{link Profile}'s {@link #getProfileKey() ProfileKey}.
    * 
    * @param profileServer the server, or <code>null</code>
    */
   public void setProfileServer(String profileServer)
   {
      this.profileServer = profileServer;
   }

   /**
    * {@inheritDoc}
    */
   public String getProfileName()
   {
      return profileName == null ? DEFAULT_PROFILE_NAME : profileName;
   }

   /**
    * Sets the value that should be used for the 
    * {@link ProfileKey#getName() name} portion of
    * the singleton @{link Profile}'s {@link #getProfileKey() ProfileKey}.
    * 
    * @param profileName the name, or <code>null</code>
    */
   public void setProfileName(String profileName)
   {
      this.profileName = profileName;
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean isActivated()
   {
      return activated;
   }
   
   // -------------------------------------------------------------- Public

   /**
    * {@inheritDoc}
    */
   public synchronized void activateProfile() throws Exception
   {
      if (this.profileService == null)
      {
         throw new IllegalStateException("Must configure the ProfileService");
      }
      
      if (!this.activated)
      {         
         try
         {
            this.profileService.activateProfile(getProfileKey());
            // Validate if the activation was successful
            this.profileService.validateProfile(getProfileKey());
            
            this.activated = true;
         }
         catch (NoSuchProfileException e)
         {
            handleNoSuchProfileException(e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void releaseProfile() throws Exception
   {
      if (this.activated)
      {         
         try
         {
            this.profileService.deactivateProfile(getProfileKey());
         }
         catch (NoSuchProfileException e)
         {
            log.warn("No Profile is registered under key " + getProfileKey());
         }
         
         this.activated = false;
      }
   }
   
   /**
    * Gets the key for the {@link Profile} that we activate and release.
    * 
    * @return the key. Will not return <code>null</code>
    * 
    * @see HASingletonProfileActivator#getProfileDomain() 
    * @see HASingletonProfileActivator#getProfileServer() 
    * @see HASingletonProfileActivator#getProfileName()
    */
   public ProfileKey getProfileKey()
   {
      if (this.profileKey == null)
      {
         this.profileKey = new ProfileKey(getProfileDomain(), getProfileServer(), getProfileName());
      }
      return this.profileKey;
   }
   
   // -------------------------------------------------------------- Protected
   

   /**
    * Handle a NoSuchProfileException thrown in {@link #activateProfile()}.
    * This base implementation just logs a WARN.
    */
   protected void handleNoSuchProfileException(NoSuchProfileException e)
   {
      log.warn("No Profile has been registered under key " + getProfileKey() +
            " -- perhaps you have a deployed deploy-hasingleton-jboss-beans.xml " +
            " without any corresponding profile configured?");      
   }

}