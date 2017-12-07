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

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.NameMatcher;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.ProfileService;

/**
 * A secured ejb facade over the ManagementView interface
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90462 $
 */
@Stateless
@SecurityDomain(value="jmx-console", unauthenticatedPrincipal="nobody")
@Remote(ManagementView.class)
@RolesAllowed({"JBossAdmin"})
public class SecureManagementView implements ManagementView
{
   static Logger log = Logger.getLogger(SecureManagementView.class);

   /** The local ProfileService to obtain the ManagementView delegate from */
   @Resource(mappedName="ProfileService")
   private ProfileService profileService;
   private ManagementView delegate;

   @PostConstruct
   public void postConstruct()
   {
      log.debug("Looking up ProfileService.ManagementView");
      delegate = profileService.getViewManager();
   }
   @PreDestroy
   public void preDestroy()
   {
      delegate = null;
   }

   public void applyTemplate(String deploymentBaseName,
         DeploymentTemplateInfo info) throws Exception
   {
      delegate.applyTemplate(deploymentBaseName, info);
   }

   public ManagedComponent getComponent(String name, ComponentType type)
         throws Exception
   {
      return delegate.getComponent(name, type);
   }

   public Set<ComponentType> getComponentTypes()
   {
      return delegate.getComponentTypes();
   }

   public Set<ManagedComponent> getComponentsForType(ComponentType type)
         throws Exception
   {
      return delegate.getComponentsForType(type);
   }

   public ManagedDeployment getDeployment(String name)
         throws NoSuchDeploymentException
   {
      return delegate.getDeployment(name);
   }

   public Set<String> getDeploymentNames()
   {
      return delegate.getDeploymentNames();
   }

   public Set<String> getDeploymentNamesForType(String type)
   {
      return delegate.getDeploymentNamesForType(type);
   }

   public Set<ManagedDeployment> getDeploymentsForType(String type)
         throws Exception
   {
      return delegate.getDeploymentsForType(type);
   }

   public Set<ManagedComponent> getMatchingComponents(String name,
         ComponentType type, NameMatcher<ManagedComponent> matcher)
         throws Exception
   {
      return delegate.getMatchingComponents(name, type, matcher);
   }

   public Set<String> getMatchingDeploymentName(String regex)
         throws NoSuchDeploymentException
   {
      return delegate.getMatchingDeploymentName(regex);
   }

   public Set<ManagedDeployment> getMatchingDeployments(String name,
         NameMatcher<ManagedDeployment> matcher)
         throws NoSuchDeploymentException, Exception
   {
      return delegate.getMatchingDeployments(name, matcher);
   }

   public DeploymentTemplateInfo getTemplate(String name)
         throws NoSuchDeploymentException
   {
      return delegate.getTemplate(name);
   }

   public Set<String> getTemplateNames()
   {
      return delegate.getTemplateNames();
   }

   public boolean load()
   {
      return delegate.load();
   }

   public void process() throws Exception
   {
      delegate.process();
   }

   public void reload()
   {
      delegate.reload();
   }

   public void updateComponent(ManagedComponent comp) throws Exception
   {
      delegate.updateComponent(comp);
   }
   
   public void removeComponent(ManagedComponent comp) throws Exception
   {
      delegate.removeComponent(comp);
   }
   
}
