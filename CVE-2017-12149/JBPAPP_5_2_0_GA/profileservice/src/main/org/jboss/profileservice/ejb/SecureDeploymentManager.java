/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.net.URL;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment.DeploymentPhase;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * A secured ejb facade over the DeploymentManager interface
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90462 $
 */
@Stateless(name="SecureDeploymentManager")
@SecurityDomain(value="jmx-console", unauthenticatedPrincipal="nobody")
@Remote(DeploymentManager.class)
@RolesAllowed({"JBossAdmin"})
public class SecureDeploymentManager implements DeploymentManager
{
   static Logger log = Logger.getLogger(SecureManagementView.class);

   /** The local ProfileService to obtain the DeploymentManager delegate from */
   @Resource(mappedName="ProfileService")
   private ProfileService profileService;
   private DeploymentManager delegate;

   @PostConstruct
   public void postConstruct()
   {
      log.debug("Looking up ProfileService.DeploymentManager");
      delegate = profileService.getDeploymentManager();
   }
   @PreDestroy
   public void preDestroy()
   {
      delegate = null;
   }
   public DeploymentProgress distribute(String name, URL contentURL, boolean copyContent) throws Exception
   {
      return delegate.distribute(name, contentURL, copyContent);
   }
   public DeploymentProgress distribute(String name, URL contentURL, DeploymentOption... options) throws Exception
   {
      return delegate.distribute(name, contentURL, options);
   }
   public Collection<ProfileKey> getProfiles()
   {
      return delegate.getProfiles();
   }
   public String[] getRepositoryNames(String[] names) throws Exception
   {
      return delegate.getRepositoryNames(names);
   }
   public boolean isRedeploySupported()
   {
      return delegate.isRedeploySupported();
   }
   public void loadProfile(ProfileKey key) throws Exception
   {
      delegate.loadProfile(key);
   }
   public DeploymentProgress prepare(String... names) throws Exception
   {
      return delegate.prepare(names);
   }
   public DeploymentProgress redeploy(String name) throws Exception
   {
      return delegate.redeploy(name);
   }
   public void releaseProfile() throws Exception
   {
      delegate.releaseProfile();
   }
   public DeploymentProgress remove(String... names) throws Exception
   {
      return delegate.remove(names);
   }
   public DeploymentProgress start(String... names) throws Exception
   {
      return delegate.start(names);
   }
   public DeploymentProgress stop(String... names) throws Exception
   {
      return delegate.stop(names);
   }
   
}
