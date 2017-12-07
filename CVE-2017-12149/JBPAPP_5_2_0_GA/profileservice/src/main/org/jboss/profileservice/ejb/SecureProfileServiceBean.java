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
package org.jboss.profileservice.ejb;

import java.util.Collection;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * A secured ejb facade over the ProfileService interface
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90462 $
 */
@Stateless(name="SecureProfileService")
@SecurityDomain(value="jmx-console", unauthenticatedPrincipal="nobody")
@Remote(ProfileService.class)
@RolesAllowed({"JBossAdmin"})
public class SecureProfileServiceBean implements ProfileService
{
   @Resource(mappedName="ProfileService")
   private ProfileService delegate;
   @EJB(beanName="SecureManagementView") ManagementView mgtView;
   
   public void activateProfile(ProfileKey key) throws NoSuchProfileException, Exception
   {
      delegate.activateProfile(key);
   }
   public void deactivateProfile(ProfileKey key) throws NoSuchProfileException
   {
      delegate.deactivateProfile(key);  
   }
   public Profile getActiveProfile(ProfileKey key) throws NoSuchProfileException
   {
      return delegate.getActiveProfile(key);
   }
   public Collection<ProfileKey> getActiveProfileKeys()
   {
      return delegate.getActiveProfileKeys();
   }
   public DeploymentManager getDeploymentManager()
   {
      return delegate.getDeploymentManager();
   }
   public String[] getDomains()
   {
      return delegate.getDomains();
   }
   public Profile getProfile(ProfileKey key) throws NoSuchProfileException
   {
      return delegate.getProfile(key);
   }
   public Collection<ProfileKey> getProfileKeys()
   {
      return delegate.getProfileKeys();
   }
   public ManagementView getViewManager()
   {
      return delegate.getViewManager();
   }
   public void registerProfile(Profile profile) throws Exception
   {
      delegate.registerProfile(profile);
   }
   public void unregisterProfile(ProfileKey key) throws NoSuchProfileException
   {
      delegate.unregisterProfile(key);
   }
   public void validateProfile(ProfileKey key) throws Exception
   {
     delegate.validateProfile(key);
   }

}
