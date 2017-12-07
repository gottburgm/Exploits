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
package org.jboss.web.tomcat.service.injection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.JndiFieldInjector;
import org.jboss.injection.JndiMethodInjector;
import org.jboss.injection.ServiceRefInjector;
import org.jboss.injection.WebServiceRefHandler;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferencesMetaData;

/**
 * A WebServiceRef injection Handler.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class WebServiceRefInjectionHandler<X extends RemoteEnvironment> extends WebServiceRefHandler<X> 
{
   private static final Logger log = Logger.getLogger(WebServiceRefInjectionHandler.class);
   private Map<String, ServiceReferenceMetaData> srefMap = new HashMap<String, ServiceReferenceMetaData>();
   
   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null) return;
      ServiceReferencesMetaData serviceRefs = xml.getServiceReferences();
      if (serviceRefs == null) return;

      for (ServiceReferenceMetaData sref : serviceRefs)
      {
         log.debug("service-ref: " + sref);
         if (srefMap.get(sref.getServiceRefName()) != null)
               throw new IllegalStateException ("Duplicate <service-ref-name> in " + sref);
         
         srefMap.put(sref.getServiceRefName(), sref);

         String srefName = sref.getServiceRefName();
         String encName = "env/" + srefName;
         AnnotatedElement annotatedElement = sref.getAnnotatedElement();
         if(sref.getInjectionTargets() != null && sref.getInjectionTargets().size() > 0)
         {
            for(ResourceInjectionTargetMetaData trg : sref.getInjectionTargets())
            {
               // Find the annotatedElement
               annotatedElement = InjectionUtil.findInjectionTarget(container.getClassloader(), trg);
               
               // Add a JndiPropertyInjector
               addInjector(container, encName, annotatedElement);
               
               // Add the ServicerefEncInjector
               if(!container.getEncInjectors().containsKey(srefName))
                  container.getEncInjectors().put(srefName, new ServiceRefInjector(encName, annotatedElement, sref));
            }
         }
         else
         {
            if(container.getEncInjectors().containsKey(srefName))
               continue;
            
            // Add the ServicerefEncInjector only
            container.getEncInjectors().put(srefName, new ServiceRefInjector(encName, annotatedElement, sref));
         }
      }
   }
   
   private void addInjector(InjectionContainer container, String encName, AnnotatedElement annotatedElement)
   {
      Injector jndiInjector = null;
      if(annotatedElement instanceof Method)
      {
         Method method = (Method) annotatedElement; 
         jndiInjector = new JndiMethodInjector(method, encName, container.getEnc());
         addInjector(container, method, method.getDeclaringClass(), jndiInjector);
      }
      else if(annotatedElement instanceof Field)
      {
         Field field = (Field) annotatedElement;
         jndiInjector = new JndiFieldInjector(field, encName, container.getEnc());
         addInjector(container, field, field.getDeclaringClass(), jndiInjector);
      }
      else
         throw new IllegalStateException("Annotated element for '" + encName + "' is niether Method nor Field: " + annotatedElement);
   }
   
   private void addInjector(InjectionContainer container, AccessibleObject ao, Class<?> declaringClass, Injector injector)
   {
      if(injector == null)
         throw new IllegalArgumentException("null injector.");
      
      Map<AccessibleObject, Injector> map = container.getEncInjections().get(declaringClass.getName());
      if(map == null)
      {
         map = new HashMap<AccessibleObject, Injector>();
         container.getEncInjections().put(declaringClass.getName(), map);
      }
      map.put(ao, injector);      
   }
}