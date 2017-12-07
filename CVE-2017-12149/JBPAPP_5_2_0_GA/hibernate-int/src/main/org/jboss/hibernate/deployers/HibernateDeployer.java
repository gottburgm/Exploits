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
package org.jboss.hibernate.deployers;

import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.hibernate.deployers.metadata.HibernateMetaData;
import org.jboss.hibernate.deployers.metadata.SessionFactoryMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * Hibernate deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class HibernateDeployer extends AbstractSimpleRealDeployer<HibernateMetaData>
{
   private boolean scanFromTop;

   public HibernateDeployer()
   {
      super(HibernateMetaData.class);
      setOutput(BeanMetaData.class);
   }

   public void deploy(DeploymentUnit unit, HibernateMetaData metaData) throws DeploymentException
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;

      List<SessionFactoryMetaData> sessionFactoryMetaDatas = metaData.getSessionFactories();
      if (sessionFactoryMetaDatas == null || sessionFactoryMetaDatas.isEmpty())
         return;

      VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit)unit;
      if (scanFromTop)
         vfsUnit = vfsUnit.getTopLevel();

      VirtualFile root = vfsUnit.getRoot();
      for (int i = 0; i < sessionFactoryMetaDatas.size(); i++)
      {
         // build the hibernate bean
         BeanMetaData beanMetaData = sessionFactoryMetaDatas.get(i).getBeanMetaData(root);
         vfsUnit.addAttachment(BeanMetaData.class + "$Hibernate#" + (i + 1), beanMetaData);
         log.debug("Created Hibernate bean: " + beanMetaData);
      }
   }

   /**
    * Do we scan for mapping from the top,
    * or from this deployment unit.
    *
    * @param scanFromTop true if we're scanning from the top
    */
   public void setScanFromTop(boolean scanFromTop)
   {
      this.scanFromTop = scanFromTop;
   }
}

