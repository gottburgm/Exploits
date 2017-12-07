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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.transaction.UserTransaction;

import org.jboss.injection.EJBContextPropertyInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorFactory;
import org.jboss.injection.UserTransactionPropertyInjector;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.BeanPropertyFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.omg.CORBA.ORB;

/**
 * Handler for @Resources on the client
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ClientResourceHandler<X extends RemoteEnvironment>
   implements InjectionHandler<X>
{
   private static final Logger log = Logger.getLogger(ClientResourceHandler.class);
   private Class<?> clientClass;

   public ClientResourceHandler(Class<?> clientClass)
   {
      this.clientClass = clientClass;
   }
   
   private void loadEnvEntry(InjectionContainer container, Collection<EnvironmentEntryMetaData> envEntries)
   {
      for (EnvironmentEntryMetaData envEntry : envEntries)
      {
         String encName = "env/" + envEntry.getEnvEntryName();
         // 16.4.1.3: If the env-entry-value is not specified, no value will be injected and it
         // will not be initialized into the naming context.
         if(envEntry.getValue() == null)
         {
            log.debug("ignoring env-entry " + envEntry);
            continue;
         }
         Utils.injectionTarget(encName, envEntry, container);
      }
   }

   private void loadXmlResourceRefs(InjectionContainer container, Collection<ResourceReferenceMetaData> refs)
   {
      for (ResourceReferenceMetaData envRef : refs)
      {
         String encName = "env/" + envRef.getResourceRefName();

         if (envRef.getMappedName() == null || envRef.getMappedName().equals(""))
         {
            // Handle known injection types
            if (envRef.getResUrl() != null)
            {
               try
               {
                  URL resURL = new URL(envRef.getResUrl().trim());
                  URLInjectorFactory factory = new URLInjectorFactory(resURL);
                  Utils.createInjectors(container.getInjectors(), container.getClassloader(), factory, envRef.getInjectionTargets());
                  continue;
               }
               catch (MalformedURLException e)
               {
                  throw new RuntimeException(e);
               }
            }
            else if (UserTransaction.class.getName().equals(envRef.getType()))
            {
               final InjectionContainer ic = container;
               InjectorFactory<?> factory = new InjectorFactory<UserTransactionPropertyInjector>()
               {
                  public UserTransactionPropertyInjector create(BeanProperty property)
                  {
                     return new UserTransactionPropertyInjector(property, ic);
                  }
               };
               Utils.createInjectors(container.getInjectors(), container.getClassloader(), factory, envRef.getInjectionTargets());
               continue;
            }
            else if (ORB.class.getName().equals(envRef.getType()))
            {
               encName = "java:comp/ORB";
            }
            else
            {
               throw new RuntimeException("mapped-name is required for " + envRef.getResourceRefName() + " of deployment " + container.getIdentifier());
            }
         }
         Utils.injectionTarget(encName, envRef, container);
      }
   }

   private static void loadXmlResourceEnvRefs(InjectionContainer container, Collection<ResourceEnvironmentReferenceMetaData> refs)
   {
      for (ResourceEnvironmentReferenceMetaData envRef : refs)
      {
         // EJBTHREE-712
         String resTypeName = envRef.getType();
         String mappedName = envRef.getMappedName();
         try
         {
            if(resTypeName != null)
            {
               Class<?> resType = Class.forName(resTypeName);
               if (resType.equals(UserTransaction.class))
               {
                  final InjectionContainer ic = container;
                  InjectorFactory<?> factory = new InjectorFactory<UserTransactionPropertyInjector>()
                  {
                     public UserTransactionPropertyInjector create(BeanProperty property)
                     {
                        return new UserTransactionPropertyInjector(property, ic);
                     }
                  };
                  if(envRef.getInjectionTargets() != null)
                  {
                     Utils.createInjectors(container.getInjectors(), container.getClassloader(), factory, envRef.getInjectionTargets());
                     continue;
                  }
                  else
                  {
                     mappedName = "java:comp/UserTransaction";
                  }
               }
               else if (resType.equals(ORB.class))
               {
                  mappedName = "java:comp/ORB";
               }
            }
         }
         catch(ClassNotFoundException e)
         {
            throw new EJBException(e);
         }
         
         String encName = "env/" + envRef.getResourceEnvRefName();
         if (mappedName == null || mappedName.equals(""))
         {
            throw new RuntimeException("mapped-name is required for " + envRef.getResourceEnvRefName() + " of deployment " + container.getIdentifier());
         }
         Utils.injectionTarget(encName, envRef, container);
      }
   }

   private static void loadXmlMessageDestinationRefs(InjectionContainer container, Collection<MessageDestinationReferenceMetaData> refs)
   {
      for (MessageDestinationReferenceMetaData envRef : refs)
      {
         String encName = "env/" + envRef.getMessageDestinationRefName();
         String jndiName = envRef.getMappedName();
         if (jndiName == null || jndiName.equals(""))
         {
            // Look for a message-destination-link
            jndiName = envRef.getResolvedJndiName();
            if (jndiName == null)    
               throw new RuntimeException("message-destination has no jndi-name/resolved-jndi-name " + envRef);
         }
            
         Utils.injectionTarget(encName, envRef, container);
      }
   }

   public void loadXml(X xml, InjectionContainer container)
   {
      if (xml == null)
         return;
      if (xml.getMessageDestinationReferences() != null)
         loadXmlMessageDestinationRefs(container, xml.getMessageDestinationReferences());
      if (xml.getResourceEnvironmentReferences() != null)
         loadXmlResourceEnvRefs(container, xml.getResourceEnvironmentReferences());
      if (xml.getResourceReferences() != null)
         loadXmlResourceRefs(container, xml.getResourceReferences());
      if (xml.getEnvironmentEntries() != null)
         loadEnvEntry(container, xml.getEnvironmentEntries());
   }

   public void handleClassAnnotations(Class<?> clazz, InjectionContainer container)
   {
      throw new IllegalStateException("Annotations are not handled in the client");
   }
   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("Annotations are not handled in the client");
   }
   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      throw new IllegalStateException("Annotations are not handled in the client");
   }
}
