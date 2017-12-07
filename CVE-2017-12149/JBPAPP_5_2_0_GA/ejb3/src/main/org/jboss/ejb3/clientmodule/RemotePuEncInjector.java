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
package org.jboss.ejb3.clientmodule;

import javax.naming.NamingException;

import org.jboss.injection.EncInjector;
import org.jboss.injection.ExtendedInjectionContainer;
import org.jboss.injection.InjectionContainer;
import org.jboss.jpa.deployment.PersistenceUnitDeployment;
import org.jboss.jpa.remote.RemotelyInjectEntityManagerFactory;
import org.jboss.jpa.spi.PersistenceUnitRegistry;
import org.jboss.util.naming.Util;

/**
 * EXPERIMENTAL
 * 
 * Setup a remote entity manager factory.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class RemotePuEncInjector implements EncInjector
{
   private String encName;
   private String unitName;
   
   public RemotePuEncInjector(String encName, Class<?> injectionType, String unitName, String error)
   {
      assert encName != null : "encName is null";
      this.encName = encName;
      this.unitName = unitName;
   }
   
   public void inject(InjectionContainer c)
   {
      if(!(c instanceof ExtendedInjectionContainer))
         throw new UnsupportedOperationException("RemotePuEncInjector only works for ExtendedInjectionContainer");
      ExtendedInjectionContainer container = (ExtendedInjectionContainer) c;
      
      String name = container.resolvePersistenceUnitSupplier(unitName);
      PersistenceUnitDeployment deployment = ((PersistenceUnitDeployment) PersistenceUnitRegistry.getPersistenceUnit(name));
      RemotelyInjectEntityManagerFactory factory = new RemotelyInjectEntityManagerFactory(deployment.getXml(), "FIXME");
      
      try
      {
         Util.rebind(container.getEnc(), encName, factory);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
}
