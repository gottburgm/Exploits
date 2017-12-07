/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.clientmodule;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.naming.NameNotFoundException;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.annotation.IgnoreDependency;
import org.jboss.injection.EJBInjectionHandler;
import org.jboss.injection.EjbEncInjector;
import org.jboss.injection.EncInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.AbstractEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * Local override until the ejb3 code is released for EJBTHREE-1289.
 * 
 * Searches bean class for all @Inject and create Injectors
 * for a remote environment.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class EJBRemoteHandler<X extends RemoteEnvironment> extends EJBInjectionHandler<X>
{
   private static final Logger log = Logger.getLogger(EJBRemoteHandler.class);

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml != null)
      {
         log.trace("ejbRefs = " + xml.getEjbReferences());
         if (xml.getAnnotatedEjbReferences() != null)
            loadEjbRefXml(xml.getAnnotatedEjbReferences(), container);
         if (xml.getEjbReferences() != null)
            loadEjbRefXml(xml.getEjbReferences(), container);
      }
   }

   protected void loadEjbRefXml(AnnotatedEJBReferencesMetaData refs,
         InjectionContainer container)
   {
      for (AnnotatedEJBReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getBeanInterface().getName();
         String errorType = "@EJB";

         ejbRefXml(ref, interfaceName, container, errorType);
      }      
   }

   protected void loadEjbRefXml(Collection<EJBReferenceMetaData> refs, InjectionContainer container)
   {
      for (EJBReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getRemote();
         String errorType = "<ejb-ref>";

         ejbRefXml(ref, interfaceName, container, errorType);
      }
   }

   protected void ejbRefXml(AbstractEJBReferenceMetaData ref, String interfaceName, InjectionContainer container, String errorType)
   {
      String encName = "env/" + ref.getEjbRefName();
      InjectionUtil.injectionTarget(encName, ref, container, container.getEncInjections());
      if (container.getEncInjectors().containsKey(encName))
         return;

      String mappedName = ref.getMappedName();
      if (mappedName != null && mappedName.equals(""))
         mappedName = null;
      if(mappedName == null && ref.getResolvedJndiName() != null)
         mappedName = ref.getResolvedJndiName();

      String link = ref.getLink();
      if (link != null && link.trim().equals("")) link = null;

      Class<?> refClass = null;

      if (interfaceName != null)
      {
         try
         {
            refClass = container.getClassloader().loadClass(interfaceName);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException("could not find " + errorType + "'s local interface " + interfaceName + " in " + container.getDeploymentDescriptorType() + " of " + container.getIdentifier());
         }
      }
      
      //----- injectors

      if (mappedName == null && refClass == null && link == null)
      {
         // must be jboss.xml only with @EJB used to define reference.  jboss.xml used to tag for ignore dependency
         // i think it is ok to assume this because the ejb-jar.xml schema should handle any missing elements
      }
      else
      {
         ejbRefEncInjector(mappedName, encName, null, refClass, link, errorType, container);
         if (ref.getIgnoreDependency() != null)
         {
            log.debug("IGNORING <ejb-ref> DEPENDENCY: " + encName);
            return;
         }

         ejbRefDependency(mappedName, link, container, refClass, errorType, encName);
      }
   }

   protected void ejbRefDependency(String mappedName, String link, InjectionContainer container, Class<?> refClass, String errorType, String encName)
   {
      if(mappedName != null && mappedName.length() == 0) mappedName = null;
      if (refClass != null && (refClass.equals(Object.class) || refClass.equals(void.class))) refClass = null;
      
      if(mappedName != null)
      {
         addJNDIDependency(container, mappedName);
         return;
      }
      
      if (refClass != null)
      {
         if (link != null && !link.trim().equals(""))
         {
            addDependency(container, link, refClass);
         }
         else
         {
            addDependency(container, refClass);
         }
      }
      
      else
      {
         String msg = "IGNORING DEPENDENCY: unable to resolve dependency of EJB, there is too little information";
         log.warn(msg);
      }
   }

   protected void ejbRefEncInjector(String mappedName, String encName, String fieldName, Class refClass, String link, String errorType, InjectionContainer container)
   {
      if (refClass != null && (refClass.equals(Object.class) || refClass.equals(void.class)))
         refClass = null;
      if (mappedName != null && mappedName.trim().equals(""))
         mappedName = null;
      if (mappedName == null)
      {
         //
         AnnotatedEJBReferencesMetaData amds = container.getEnvironmentRefGroup().getAnnotatedEjbReferences();
         if(amds != null)
         {
            AnnotatedEJBReferenceMetaData amd = amds.get(encName);
            if(amd == null && fieldName != null)
               amd = amds.get(fieldName);
            if(amd != null)
            {
               mappedName = amd.getMappedName();
               if(mappedName == null)
                  mappedName = amd.getResolvedJndiName();
            }
         }
      }

      EncInjector injector = null;
      
      if (mappedName == null)
      {
         injector = new EjbEncInjector(encName, refClass, link, errorType);
      }
      else
      {
         injector = new EjbEncInjector(encName, mappedName, errorType);
      }

      container.getEncInjectors().put(encName, injector);
   }

   public static EJBContainer getEjbContainer(EJB ref, InjectionContainer container, Class<?> memberType)
   {
      EJBContainer rtn = null;

      if (ref.mappedName() != null && !"".equals(ref.mappedName()))
      {
         return null;
      }

      if (ref.beanName().equals("") && memberType == null)
         throw new RuntimeException("For deployment " + container.getIdentifier() + "not enough information for @EJB.  Please fill out the beanName and/or businessInterface attributes");

      Class<?> businessInterface = memberType;
      if (!ref.beanInterface().getName().equals(Object.class.getName()))
      {
         businessInterface = ref.beanInterface();
      }

      if (ref.beanName().equals(""))
      {
         try
         {
            rtn = (EJBContainer) container.resolveEjbContainer(businessInterface);
         }
         catch (NameNotFoundException e)
         {
            log.warn("For deployment " + container.getIdentifier() + " could not find jndi binding based on interface only for @EJB(" + businessInterface.getName() + ") " + e.getMessage());
         }
      }
      else
      {
         rtn = (EJBContainer) container.resolveEjbContainer(ref.beanName(), businessInterface);
      }

      return rtn;
   }

   public static String getJndiName(EJB ref, InjectionContainer container, Class<?> memberType)
   {
      String jndiName;

      if (ref.mappedName() != null && !"".equals(ref.mappedName()))
      {
         return ref.mappedName();
      }

      if (ref.beanName().equals("") && memberType == null)
         throw new RuntimeException("For deployment " + container.getIdentifier() + "not enough information for @EJB.  Please fill out the beanName and/or businessInterface attributes");

      Class<?> businessInterface = memberType;
      if (!ref.beanInterface().getName().equals(Object.class.getName()))
      {
         businessInterface = ref.beanInterface();
      }

      if (ref.beanName().equals(""))
      {
         try
         {
            jndiName = container.getEjbJndiName(businessInterface);
         }
         catch (NameNotFoundException e)
         {
            throw new RuntimeException("For deployment " + container.getIdentifier() + " could not find jndi binding based on interface only for @EJB(" + businessInterface.getName() + ") " + e.getMessage());
         }
         if (jndiName == null)
         {
            throw new RuntimeException("For deployment " + container.getIdentifier() + " could not find jndi binding based on interface only for @EJB(" + businessInterface.getName() + ")");
         }
      }
      else
      {
         jndiName = container.getEjbJndiName(ref.beanName(), businessInterface);
         if (jndiName == null)
         {
            throw new RuntimeException("For EJB " + container.getIdentifier() + "could not find jndi binding based on beanName and business interface for @EJB(" + ref.beanName() + ", " + businessInterface.getName() + ")");
         }
      }

      return jndiName;
   }

   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      throw new IllegalStateException("handleClassAnnotation cannot be used in a client");
   }

   protected void handleClassAnnotation(EJB ejb, Class<?> clazz, InjectionContainer container)
   {
      throw new IllegalStateException("handleClassAnnotation cannot be used in a client");
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("handleMethodAnnotations cannot be used in a client");
   }

   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("handleFieldAnnotations cannot be used in a client");
   }

   protected boolean isIgnoreDependency(InjectionContainer container, EJB ref)
   {
      RemoteEnvironment refGroup =  container.getEnvironmentRefGroup();
      
      if (refGroup != null)
      {
         if(refGroup.getEjbReferences() != null)
         for(EJBReferenceMetaData ejbRef : refGroup.getEjbReferences())
         {
            if (ejbRef.getEjbRefName().equals(ref.name()))
            {
               return ejbRef.getIgnoreDependency() != null;
            }
         }
      }
      
      // TODO: shouldn't we scan local ejb refs as well?
      
      return false;
   }
}