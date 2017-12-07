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
package org.jboss.web.tomcat.service;

import javax.ejb.EJBLocalObject;
import javax.naming.NameNotFoundException;

import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb.EjbUtil50;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.enc.DeploymentEjbResolver;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class WarEjbResolver extends DeploymentEjbResolver
{
   private VFSDeploymentUnit unit;
   private MainDeployerStructure mainDeployer;

   public WarEjbResolver(DeploymentScope deploymentScope, VFSDeploymentUnit unit,
         MainDeployerStructure mainDeployer)
   {
      super(deploymentScope, unit.getSimpleName());
      this.unit = unit;
      this.mainDeployer = mainDeployer;
   }

   
   @Override
   public String getEjbJndiName(String ejbLink, Class businessIntf)
   {
      String name = super.getEjbJndiName(ejbLink, businessIntf);
      if( name == null )
      {
         if( EJBLocalObject.class.isAssignableFrom(businessIntf) )
            name = EjbUtil50.findLocalEjbLink(mainDeployer, unit, ejbLink);
         else
            name = EjbUtil50.findEjbLink(mainDeployer, unit, ejbLink);
      }
      return name;
   }

   protected EJBContainer searchDeploymentInternally(String ejbLink, Class businessIntf)
   {
      return null;
   }

   protected EJBContainer searchForEjbContainerInternally(Class businessIntf)
      throws NameNotFoundException
   {
      return null;
   }
}
