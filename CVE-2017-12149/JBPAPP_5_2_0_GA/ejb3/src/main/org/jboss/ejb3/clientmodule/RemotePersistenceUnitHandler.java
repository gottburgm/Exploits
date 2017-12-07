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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.naming.NameNotFoundException;

import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.PersistenceUnitHandler;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * EXPERIMENTAL
 * 
 * Allow injection of a persistence unit into a component running outside
 * the VM where the peristence unit is deployed.
 * 
 * Currently this only runs for application client which does do any
 * annotation processing by itself. This is delegated to the AnnotationDeployer.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class RemotePersistenceUnitHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      throw new UnsupportedOperationException("metadata should have been complete");
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container,
         Map<AccessibleObject, Injector> injectors)
   {
      throw new UnsupportedOperationException("metadata should have been complete");
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container,
         Map<AccessibleObject, Injector> injectors)
   {
      throw new UnsupportedOperationException("metadata should have been complete");
   }

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getPersistenceUnitRefs() == null) return;

      for (PersistenceUnitReferenceMetaData ref : xml.getPersistenceUnitRefs())
      {
         String encName = "env/" + ref.getPersistenceUnitRefName();
         // we add injection target no matter what.  enc injection might be overridden but
         // XML injection cannot be overriden
         Class<?> injectionType = InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());
         if (container.getEncInjectors().containsKey(encName))
            return;
         container.getEncInjectors().put(encName, new RemotePuEncInjector(encName, injectionType, ref.getPersistenceUnitName(), "<persistence-unit-ref>"));
         try
         {
            PersistenceUnitHandler.addPUDependency(ref.getPersistenceUnitName(), container);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("Illegal <persistence-unit-ref> of " + ref.getPersistenceUnitRefName() + " :" + e.getMessage());
         }
      }
   }

}
