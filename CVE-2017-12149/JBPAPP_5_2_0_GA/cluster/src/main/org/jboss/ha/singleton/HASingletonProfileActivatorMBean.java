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

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * StandardMBean interface for {@link HASingletonProfileActivator}.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public interface HASingletonProfileActivatorMBean
{

   /**
    * Gets the value that should be used for the 
    * {@link ProfileKey#getDomain() domain} portion of
    * the singleton @{link Profile}'s {@link #getProfileKey() ProfileKey}.
    * 
    * @return the domain, or <code>null</code> if not set
    */
   String getProfileDomain();

   /**
    * Gets the value that should be used for the 
    * {@link ProfileKey#getServer() server} portion of
    * the singleton @{link Profile}'s {@link #getProfileKey() ProfileKey}.
    * 
    * @return the server, or <code>null</code> if not set
    */
   String getProfileServer();

   /**
    * Gets the value that should be used for the 
    * {@link ProfileKey#getName() name} portion of
    * the singleton @{link Profile}'s {@link #getProfileKey() ProfileKey}.
    * 
    * @return the name, or {@link #DEFAULT_PROFILE_NAME} if not set
    */
   String getProfileName();

   /**
    * Gets whether this object has activated its profile.
    * 
    * @return <code>true</code> if {@link #activateProfile()} has successfully
    *         completed and {@link #releaseProfile()} has not been called;
    *         <code>false</code> otherwise.
    */
   boolean isActivated();

   /**
    * Tells the ProfileService to {@link ProfileService#activateProfile(ProfileKey) activate the profile}. 
    * Called by the HASingletonController when we become the singleton master.
    */
   void activateProfile() throws Exception;

   /**
    * Tells the ProfileService to {@link ProfileService#releaseProfile(ProfileKey) release the profile}. 
    * Called by the HASingletonController when we are no longer the singleton master.
    */
   void releaseProfile() throws Exception;

}