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
package org.jboss.profileservice.management.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.ManagedDeploymentImpl;
import org.jboss.managed.plugins.jmx.ManagementFactoryUtils;
import org.jboss.profileservice.management.ManagedOperationProxyFactory;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * The PlatformMBean management view.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * 
 * @version $Revision$
 */
public class PlatformMbeansView extends AbstractProfileView
{

   /** The managed object factory. */
   private static final ManagedObjectFactory managedObjFactory = ManagedObjectFactory.getInstance();
   
   /** A fake profile key. */
   private static final ProfileKey key = new ProfileKey(PlatformMbeansView.class.getName());
   
   
   protected static ManagedDeployment getDeployment()
   {
      Map<String, ManagedObject> platformMBeanMOs = ManagementFactoryUtils.getPlatformMBeanMOs(managedObjFactory);
      ManagedDeploymentImpl platformMBeans = new ManagedDeploymentImpl("JDK PlatformMBeans", "PlatformMBeans", null,
            platformMBeanMOs);
      List<ManagedObject> gcMbeans = ManagementFactoryUtils.getGarbageCollectorMXBeans(managedObjFactory);
      Map<String, ManagedObject> gcMOs = new HashMap<String, ManagedObject>();
      for (ManagedObject mo : gcMbeans)
         gcMOs.put(mo.getName(), mo);
      List<ManagedObject> mmMbeans = ManagementFactoryUtils.getMemoryManagerMXBeans(managedObjFactory);
      Map<String, ManagedObject> mmMOs = new HashMap<String, ManagedObject>();
      for (ManagedObject mo : mmMbeans)
         mmMOs.put(mo.getName(), mo);
      List<ManagedObject> mpoolMBeans = ManagementFactoryUtils.getMemoryPoolMXBeans(managedObjFactory);
      Map<String, ManagedObject> mpoolMOs = new HashMap<String, ManagedObject>();
      for (ManagedObject mo : mpoolMBeans)
         mpoolMOs.put(mo.getName(), mo);
      ManagedDeploymentImpl gcMD = new ManagedDeploymentImpl("GarbageCollectorMXBeans", "GarbageCollectorMXBeans",
            null, gcMOs);
      platformMBeans.getChildren().add(gcMD);
      ManagedDeploymentImpl mmMD = new ManagedDeploymentImpl("MemoryManagerMXBeans", "MemoryManagerMXBeans", null, mmMOs);
      platformMBeans.getChildren().add(mmMD);
      ManagedDeploymentImpl mpoolMD = new ManagedDeploymentImpl("MemoryPoolMXBeans", "MemoryPoolMXBeans", null, mpoolMOs);
      platformMBeans.getChildren().add(mpoolMD);
      return platformMBeans;
   }
   
   public PlatformMbeansView(ManagedOperationProxyFactory proxyFactory) throws Exception
   {
      super(proxyFactory);
      processManagedDeployment(getDeployment(), DeploymentState.STARTED, 0, false);
   }

   @Override
   public ProfileKey getProfileKey()
   {
      return key;
   }
   
   @Override
   public boolean hasBeenModified(Profile profile)
   {
      return false;
   }
}

