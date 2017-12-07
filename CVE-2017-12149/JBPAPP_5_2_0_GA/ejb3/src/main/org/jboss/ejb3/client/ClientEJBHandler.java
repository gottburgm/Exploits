/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.client;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.ejb.EJB;

import org.jboss.injection.AbstractHandler;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.Injector;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.AbstractEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ClientEJBHandler<X extends RemoteEnvironment>
   extends AbstractHandler<X>
{
   private static final Logger log = Logger.getLogger(ClientEJBHandler.class);

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml != null)
      {
         log.trace("ejbRefs = " + xml.getEjbReferences());
         try
         {
         if (xml.getAnnotatedEjbReferences() != null)
            loadEjbRefXml(xml.getAnnotatedEjbReferences(), container);
         if (xml.getEjbReferences() != null)
            loadEjbRefXml(xml.getEjbReferences(), container);
         }
         catch(Exception e)
         {
            throw new IllegalStateException(e);
         }
      }
   }

   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      throw new IllegalStateException("Annotations are not handled");
   }
   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("Annotations are not handled");
   }
   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("Annotations are not handled");
   }

   protected void loadEjbRefXml(AnnotatedEJBReferencesMetaData refs,
         InjectionContainer container)
      throws Exception
   {
      for (AnnotatedEJBReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getBeanInterface().getName();
         String errorType = "@EJB";

         ejbRefXml(ref, interfaceName, container, errorType);
      }      
   }

   protected void loadEjbRefXml(Collection<EJBReferenceMetaData> refs, InjectionContainer container)
      throws Exception
   {
      for (EJBReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getRemote();
         String errorType = "<ejb-ref>";

         ejbRefXml(ref, interfaceName, container, errorType);
      }
   }

   protected void ejbRefXml(AbstractEJBReferenceMetaData ref, String interfaceName, InjectionContainer container, String errorType)
      throws Exception
   {
      String encName = "env/" + ref.getEjbRefName();
      Utils.injectionTarget(encName, ref, container);

      /*
      String mappedName = ref.getMappedName();
      if (mappedName != null && mappedName.equals(""))
         mappedName = null;
      if(mappedName == null && ref.getResolvedJndiName() != null)
         mappedName = ref.getResolvedJndiName();

      if(mappedName == null)
         throw new IllegalStateException(ref+" has no mapped name");
      // Handle the injection targets
      ClassLoader loader = container.getClassloader();
      Set<ResourceInjectionTargetMetaData> targets = ref.getInjectionTargets();
      if(targets != null)
      {
         for(ResourceInjectionTargetMetaData target : targets)
         {
            String className = target.getInjectionTargetClass();
            String targetName = target.getInjectionTargetName();
            Class<?> c = loader.loadClass(className);
            AccessibleObject ao = Utils.getAccessibleObject(c, targetName);
            BeanProperty prop = BeanPropertyFactory.create(ao);
            JndiPropertyInjector propInjector = new JndiPropertyInjector(prop, encName, container.getEnc());
            container.getInjectors().add(propInjector);
         }
      }
      else
      {
         log.warn("No injection targets seen for: "+ref);
      }
      */
   }

   protected String getEncName(EJB ref, Field field)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = Utils.getEncName(field);
      }
      else
      {
         encName = "env/" + encName;
      }
      return encName;
   }
   
   protected String getEncName(EJB ref, Method method)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = Utils.getEncName(method);
      }
      else
      {
         encName = "env/" + encName;
      }
      return encName;
   }

}
