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
package org.jboss.ejb3.client.injection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.ejb3.client.Utils;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.Injector;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * Injection of persistence units into application clients.
 * 
 * Only setup injection of persistence units, the enc setup is done on the server.
 * It's expected that the meta data is complete and no annotation handling should be performed.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class ClientPersistenceUnitHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      throw new IllegalStateException("Annotations are not handled");
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container,
         Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("Annotations are not handled");
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container,
         Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("Annotations are not handled");
   }

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null) return;
      if (xml.getPersistenceUnitRefs() == null) return;

      for (PersistenceUnitReferenceMetaData ref : xml.getPersistenceUnitRefs())
      {
         String encName = "env/" + ref.getPersistenceUnitRefName();
         Utils.injectionTarget(encName, ref, container);
      }
   }
}
