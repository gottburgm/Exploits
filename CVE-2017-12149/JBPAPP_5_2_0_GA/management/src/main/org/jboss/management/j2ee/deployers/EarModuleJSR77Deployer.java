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
package org.jboss.management.j2ee.deployers;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.management.j2ee.J2EEApplication;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;

/**
 * Ear module jsr77 view.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class EarModuleJSR77Deployer extends AbstractVFSJSR77Deployer<JBossAppMetaData>
{
   public EarModuleJSR77Deployer()
   {
      super(JBossAppMetaData.class);
      setTopLevelOnly(true);
   }

   protected void deployJsr77(MBeanServer server, VFSDeploymentUnit unit, JBossAppMetaData metaData) throws Throwable
   {
      ObjectName jsr77Name = J2EEApplication.create(server, unit.getSimpleName(), unit.getRoot().toURL());
      if (jsr77Name != null)
      {
         putObjectName(unit, J2EEApplication.class.getName(), jsr77Name);
         log.debug("Created J2EEApplication: " + jsr77Name);
      }
   }

   protected void undeployJsr77(MBeanServer server, VFSDeploymentUnit unit, JBossAppMetaData metaData)
   {
      ObjectName jsr77Name = removeObjectName(unit, J2EEApplication.class.getName());
      if (jsr77Name != null)
      {
         J2EEApplication.destroy(server, jsr77Name);
         log.debug("Removed J2EEApplication: " + jsr77Name);
      }
   }
}
