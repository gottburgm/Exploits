/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.deployers.tmp;

import java.util.Iterator;

import javax.naming.NameNotFoundException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.logging.Logger;

/**
 * Class to resolve EJB containers from ejb-ref or @EJB metadata
 * This class is overriden for specific behaviors, specifically whether or not
 * to resolve the EJB internally in a specific deployment or not.  There will be one for
 * an EJB jar deployment and a WAR deployment and any other deployment package that needs to
 * use @EJB.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 72637 $
 */
public abstract class DeploymentEjbResolver
{
   private static final Logger log = Logger.getLogger(DeploymentEjbResolver.class);
   
   protected DeploymentScope deploymentScope;
   protected String errorName;

   protected DeploymentEjbResolver(DeploymentScope deploymentScope, String errorName)
   {
      this.deploymentScope = deploymentScope;
      this.errorName = errorName;
   }

   protected abstract EJBContainer searchDeploymentInternally(String ejbLink, Class businessIntf);

   public EJBContainer getEjbContainer(String ejbLink, Class businessIntf)
   {
      int hashIndex = ejbLink.indexOf('#');
      if (hashIndex != -1)
      {
         if (deploymentScope == null)
         {
            log.warn("ejb link '" + ejbLink + "' is relative, but no deployment scope found");
            return null;
         }
         String relativePath = ejbLink.substring(0, hashIndex);
         Ejb3Deployment dep = deploymentScope.findRelativeDeployment(relativePath);
         if (dep == null)
         {
            log.warn("can't find a deployment for path '" + relativePath + "' of ejb link '" + ejbLink + "'");
            return null;
         }
         String ejbName = ejbLink.substring(hashIndex + 1);
         return dep.getEjbContainer(ejbName, businessIntf);
      }
      // look internally
      EJBContainer ejb = searchDeploymentInternally(ejbLink, businessIntf);
      if (ejb != null) return ejb;
      for (Object obj : Ejb3Registry.getContainers())
      {
         EJBContainer container = (EJBContainer) obj;
         if (container.getEjbName().equals(ejbLink))
         {
            return container;
         }
      }
      return null;
   }

   public String getEjbJndiName(String ejbLink, Class businessIntf)
   {
      EJBContainer container = getEjbContainer(ejbLink, businessIntf);
      if (container == null)
      {
         return null;
      }
      return ProxyFactoryHelper.getJndiName(container, businessIntf);
   }

   public EJBContainer getEjbContainer(Ejb3Deployment deployment, Class businessIntf) throws NameNotFoundException
   {
      EJBContainer container = null;
      // search in myself
      for (Object obj : deployment.getEjbContainers().values())
      {
         EJBContainer newContainer = (EJBContainer) obj;
         if (container == newContainer) continue;
         if (ProxyFactoryHelper.publishesInterface(newContainer, businessIntf))
         {
            if (container != null) throw new NameNotFoundException("duplicated in " + errorName);
            container = newContainer;
         }
      }
      return container;
   }

   public EJBContainer getEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      EJBContainer rtnContainer = null;
      // search in deployment first
      rtnContainer = searchForEjbContainerInternally(businessIntf);
      if (rtnContainer != null) return rtnContainer;
      // search in EAR
      String jarName = null;
      if (deploymentScope != null)
      {
         for (Ejb3Deployment deployment : deploymentScope.getEjbDeployments())
         {
            EJBContainer newContainer = getEjbContainer(deployment, businessIntf);
            if (rtnContainer == newContainer) continue; // don't check self
            if (rtnContainer != null && newContainer != null)
            {
               throw new NameNotFoundException("duplicated in .ear within " + jarName +
                       " and " + deployment.getDeploymentUnit().getShortName());
            }
            if (newContainer != null)
            {
               rtnContainer = newContainer;
               jarName = deployment.getDeploymentUnit().getShortName();
            }
         }
      }
      if (rtnContainer != null)
      {
         return rtnContainer;
      }
      // search everywhere
      Iterator containers = Ejb3Registry.getContainers().iterator();
      while (containers.hasNext())
      {
         Container container = (Container)containers.next();
         EJBContainer ejbContainer = (EJBContainer) container;
         if (ejbContainer == rtnContainer) continue;
         if (ProxyFactoryHelper.publishesInterface(container, businessIntf))
         {
            if (rtnContainer != null)
            {
               throw new NameNotFoundException("duplicated in " + ejbContainer.getDeployment().getDeploymentUnit().getShortName()
                       + " and " + jarName);
            }
            rtnContainer = ejbContainer;
            jarName = ejbContainer.getDeployment().getDeploymentUnit().getShortName();
         }
      }
      if (rtnContainer != null) return rtnContainer;
      throw new NameNotFoundException("not used by any EJBs");
   }

   protected abstract EJBContainer searchForEjbContainerInternally(Class businessIntf) throws NameNotFoundException;

   public String getEjbJndiName(Class businessIntf) throws NameNotFoundException
   {
      EJBContainer container = getEjbContainer(businessIntf);
      String jndiName = ProxyFactoryHelper.getJndiName(container, businessIntf);
      if (jndiName == null) throw new NameNotFoundException("not used by any EJBs");
      return jndiName;
   }
}
