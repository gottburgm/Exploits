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
package org.jboss.ejb3.clientmodule;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;

import org.jboss.ejb3.Container;
import org.jboss.injection.EJBContextPropertyInjector;
import org.jboss.injection.EnvEntryEncInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorFactory;
import org.jboss.injection.JndiPropertyInjector;
import org.jboss.injection.LinkRefEncInjector;
import org.jboss.injection.TimerServicePropertyInjector;
import org.jboss.injection.UserTransactionPropertyInjector;
import org.jboss.injection.ValueEncInjector;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.FieldBeanProperty;
import org.jboss.injection.lang.reflect.MethodBeanProperty;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.reflect.plugins.ValueConvertor;
import org.omg.CORBA.ORB;

/**
 * Handling of resources in the server side setup of the client container.
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ResourceHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   private static final Logger log = Logger.getLogger(ResourceHandler.class);
   
   private boolean checkEncInjectors;

   public ResourceHandler()
   {
      this(true);
   }
   
   public ResourceHandler(boolean checkEncInjectors)
   {
      this.checkEncInjectors = checkEncInjectors;
   }
   
   private void createURLInjector(String encName, String mappedName, InjectionContainer container)
   {
      assert encName.length() > 0 : "encName is empty";
      assert mappedName.length() > 0 : "mappedName is empty";
      
      // Create a URL from the mappedName
      try
      {
         URL url = new URL(mappedName.trim());
         container.getEncInjectors().put(encName, new ValueEncInjector(encName, url, "@Resource"));
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private static void loadEnvEntry(InjectionContainer container, Collection<EnvironmentEntryMetaData> envEntries)
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
         InjectionUtil.injectionTarget(encName, envEntry, container, container.getEncInjections());
         if (container.getEncInjectors().containsKey(encName)) continue;
         log.trace("adding env-entry injector " + encName);
         container.getEncInjectors().put(encName, new EnvEntryEncInjector(encName, envEntry.getType(), envEntry.getValue()));
      }
   }

   private static void loadXmlResourceRefs(InjectionContainer container, Collection<ResourceReferenceMetaData> refs)
   {
      for (ResourceReferenceMetaData envRef : refs)
      {
         String encName = "env/" + envRef.getResourceRefName();
         if (container.getEncInjectors().containsKey(encName))
            continue;

         if (envRef.getMappedName() == null || envRef.getMappedName().equals(""))
         {
            if (envRef.getResUrl() != null)
            {
               try
               {
                  container.getEncInjectors().put(encName, new ValueEncInjector(encName, new URL(envRef.getResUrl().trim()), "<resource-ref>"));
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
               if(envRef.getInjectionTargets() != null)
               {
                  InjectionUtil.createInjectors(container.getInjectors(), container.getClassloader(), factory, envRef.getInjectionTargets());
                  continue;
               }
               else
               {
                  encName = "java:comp/UserTransaction";
               }
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
         else
         {
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, envRef.getMappedName(), "<resource-ref>"));
         }
         InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
      }
   }

   private static void loadXmlResourceEnvRefs(InjectionContainer container, Collection<ResourceEnvironmentReferenceMetaData> refs)
   {
      for (ResourceEnvironmentReferenceMetaData envRef : refs)
      {
         // EJBTHREE-712
         // TODO: refactor with handlePropertyAnnotation
         String resTypeName = envRef.getType();
         String mappedName = envRef.getMappedName();
         try
         {
            if(resTypeName != null)
            {
               Class<?> resType = Class.forName(resTypeName);
               if(EJBContext.class.isAssignableFrom(resType))
               {
                  InjectorFactory<?> factory = new InjectorFactory<EJBContextPropertyInjector>()
                  {
                     public EJBContextPropertyInjector create(BeanProperty property)
                     {
                        return new EJBContextPropertyInjector(property);
                     }
                  };
                  InjectionUtil.createInjectors(container.getInjectors(), container.getClassloader(), factory, envRef.getInjectionTargets());
                  continue;
               }
               else if (resType.equals(UserTransaction.class))
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
                     InjectionUtil.createInjectors(container.getInjectors(), container.getClassloader(), factory, envRef.getInjectionTargets());
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
         if (container.getEncInjectors().containsKey(encName)) continue;
         if (mappedName == null || mappedName.equals(""))
         {
            throw new RuntimeException("mapped-name is required for " + envRef.getResourceEnvRefName() + " of deployment " + container.getIdentifier());
         }
         container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, mappedName, "<resource-ref>"));
         InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
      }
   }

   private static void loadXmlMessageDestinationRefs(InjectionContainer container, Collection<MessageDestinationReferenceMetaData> refs)
   {
      for (MessageDestinationReferenceMetaData envRef : refs)
      {
         String encName = "env/" + envRef.getMessageDestinationRefName();
         if (container.getEncInjectors().containsKey(encName)) continue;
         String jndiName = envRef.getMappedName();
         if (jndiName == null || jndiName.equals(""))
         {
            // Look for a message-destination-link
            jndiName = envRef.getResolvedJndiName();
            if (jndiName == null)
            {
               throw new RuntimeException("message-destination has no jndi-name/resolved-jndi-name " + envRef);
               // TODO: add dependency
            }
         }
         container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, jndiName, "<message-destination-ref>"));
         InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
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
      Resources resources = container.getAnnotation(Resources.class, clazz);
      if (resources != null)
      {
      for (Resource ref : resources.value())
      {
         handleClassAnnotation(ref, container, clazz);
      }
      }
      Resource res = container.getAnnotation(Resource.class, clazz);
      if (res != null) handleClassAnnotation(res, container, clazz);
   }

   private void handleClassAnnotation(Resource ref, InjectionContainer container, Class<?> clazz)
   {
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         throw new RuntimeException("JBoss requires name() for class level @Resource");
      }
      encName = "env/" + ref.name();
      if (container.getEncInjectors().containsKey(encName)) return;

      String mappedName = ref.mappedName();
      if (mappedName == null || mappedName.equals(""))
      {
         // Handle class level @Resource(type=ORB.class)
         if(ORB.class.isAssignableFrom(ref.type()))
         {
            mappedName = "java:comp/ORB";
         }
         else if(UserTransaction.class.isAssignableFrom(ref.type()))
         {
            mappedName = "java:comp/UserTransaction";
         }
         else
         {
            throw new RuntimeException("You did not specify a @Resource.mappedName() for name: "
                  +ref.name()+", class: " + clazz.getName() + " and there is no binding for that enc name in XML");
         }
      }

      if (ref.type() == URL.class)
      {
         createURLInjector(encName, mappedName, container);
      }
      else
      {
         container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
      }
   }

   public void handleMethodAnnotations(Method method, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      Resource ref = container.getAnnotation(Resource.class, method);
      if (ref == null) return;

      log.trace("method " + method + " has @Resource");
      
      handlePropertyAnnotation(ref, new MethodBeanProperty(method), container, injectors);
      /*
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(method);
      }
      else
      {
         encName = "env/" + encName;
      }

      method.setAccessible(true);

      if (!method.getName().startsWith("set"))
         throw new RuntimeException("@Resource can only be used with a set method: " + method);
      if (method.getParameterTypes().length != 1)
         throw new RuntimeException("@Resource can only be used with a set method of one parameter: " + method);

      Class type = method.getParameterTypes()[0];
      if (!ref.type().equals(Object.class))
      {
         type = ref.type();
      }
      if (type.equals(UserTransaction.class))
      {
         injectors.put(method, new UserTransactionMethodInjector(method, container));
      }
      else if (type.equals(TimerService.class))
      {
         injectors.put(method, new TimerServiceMethodInjector(method, (Container) container)); // only EJBs
      }
      else if (EJBContext.class.isAssignableFrom(type))
      {
         injectors.put(method, new EJBContextMethodInjector(method));
      }
      else if (type.equals(WebServiceContext.class))
      {
         // FIXME: For now we skip it, and rely on the WS stack to perform the injection
      }
      else if (type.equals(String.class)
              || type.equals(Character.class)
              || type.equals(Byte.class)
              || type.equals(Short.class)
              || type.equals(Integer.class)
              || type.equals(Long.class)
              || type.equals(Boolean.class)
              || type.equals(Double.class)
              || type.equals(Float.class)
              || type.isPrimitive()
              )
      {

         // don't add an injector if no XML <env-entry is present as there will be no value to inject
         if (container.getEncInjectors().containsKey(encName))
         {
            injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
         }
         else if (ref.mappedName() != null && ref.mappedName().length() > 0)
         {
            // Use the mappedName as the string value
            String s = ref.mappedName().trim();
            try
            {
               Object value = ValueConvertor.convertValue(type, s);
               container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
               injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
            }
            catch(Throwable t)
            {
               throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
            }
         }
      }
      else
      {
         if (!container.getEncInjectors().containsKey(encName))
         {
            String mappedName = ref.mappedName();
            if (mappedName == null || mappedName.equals(""))
            {
              throw new RuntimeException("You did not specify a @Resource.mappedName() on " + method + " and there is no binding for that enc name in XML");
            }
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
         }
         injectors.put(method, new JndiMethodInjector(method, encName, container.getEnc()));
      }
      */
   }
   
   public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      Resource ref = container.getAnnotation(Resource.class, field);
      if (ref == null) return;

      log.trace("field " + field + " has @Resource");
      
      handlePropertyAnnotation(ref, new FieldBeanProperty(field), container, injectors);
      /*
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         encName = InjectionUtil.getEncName(field);
      }
      else
      {
         encName = "env/" + encName;
      }

      field.setAccessible(true);

      Class type = field.getType();
      if (!ref.type().equals(Object.class))
      {
         type = ref.type();
      }
      if (type.equals(UserTransaction.class))
      {
         injectors.put(field, new UserTransactionFieldInjector(field, container));
      }
      else if (type.equals(TimerService.class))
      {
         injectors.put(field, new TimerServiceFieldInjector(field, (Container) container)); // only EJBs
      }
      else if (EJBContext.class.isAssignableFrom(type))
      {
         injectors.put(field, new EJBContextFieldInjector(field));
      }
      else if (type.equals(WebServiceContext.class))
      {
         // FIXME: For now we skip it, and rely on the WS stack to perform the injection
      }
      else if (type.equals(String.class)
              || type.equals(Character.class)
              || type.equals(Byte.class)
              || type.equals(Short.class)
              || type.equals(Integer.class)
              || type.equals(Long.class)
              || type.equals(Boolean.class)
              || type.equals(Double.class)
              || type.equals(Float.class)
              || type.isPrimitive()
              )
      {
         // don't add an injector if no XML <env-entry is present as there will be no value to inject
         if (container.getEncInjectors().containsKey(encName))
         {
            injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
         }
         else if (ref.mappedName() != null && ref.mappedName().length() > 0)
         {
            // Use the mappedName as the string value
            String s = ref.mappedName().trim();
            try
            {
               Object value = ValueConvertor.convertValue(type, s);
               container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
               injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
            }
            catch(Throwable t)
            {
               throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
            }
         }
         else
         {
            log.warn("Not injecting " + field.getName() + ", no matching enc injector " + encName + " found");
         }
      }
      else
      {
         if (!container.getEncInjectors().containsKey(encName))
         {
            String mappedName = ref.mappedName();
            if (mappedName == null || mappedName.equals(""))
            {
              throw new RuntimeException("You did not specify a @Resource.mappedName() on " + field + " and there is no binding for enc name " + encName + " in XML");
            }
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, ref.mappedName(), "@Resource"));
         }
         injectors.put(field, new JndiFieldInjector(field, encName, container.getEnc()));
      }
      */
   }

   private void handlePropertyAnnotation(Resource ref, BeanProperty property, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
   {
      assert ref != null;
      assert property != null;
      assert container != null;
      assert injectors != null;
      
      String encName = ref.name();
      if (encName == null || encName.equals(""))
      {
         //encName = InjectionUtil.getEncName(field);
         encName = property.getDeclaringClass().getName() + "/" + property.getName();
      }
      if (!encName.startsWith("env/"))
      {
         encName = "env/" + encName;
      }

      AccessibleObject accObj = property.getAccessibleObject();
      
      Class<?> type = property.getType();
      if (!ref.type().equals(Object.class))
      {
         type = ref.type();
      }
      
      if (type.equals(UserTransaction.class))
      {
         injectors.put(accObj, new UserTransactionPropertyInjector(property, container));
      }
      else if (type.equals(TimerService.class))
      {
         injectors.put(accObj, new TimerServicePropertyInjector(property, (Container) container)); // only EJBs
      }
      else if(type.equals(URL.class) && ref.mappedName() != null && ref.mappedName().length() > 0)
      {
         createURLInjector(encName, ref.mappedName(), container);
         injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
      }
      else if (type.equals(String.class)
              || type.equals(Character.class)
              || type.equals(Byte.class)
              || type.equals(Short.class)
              || type.equals(Integer.class)
              || type.equals(Long.class)
              || type.equals(Boolean.class)
              || type.equals(Double.class)
              || type.equals(Float.class)
              || type.isPrimitive()
              )
      {
         // don't add an injector if no XML <env-entry is present as there will be no value to inject
         if (container.getEncInjectors().containsKey(encName))
         {
            injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
         }
         else if (ref.mappedName() != null && ref.mappedName().length() > 0)
         {
            // Use the mappedName as the string value
            String s = ref.mappedName().trim();
            try
            {
               Object value = ValueConvertor.convertValue(type, s);
               container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
               injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
            }
            catch(Throwable t)
            {
               throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
            }
         }
         else
         {
            log.warn("Not injecting " + property.getName() + ", no matching enc injector " + encName + " found");
         }
      }
      else
      {
         if (checkEncInjectors && !container.getEncInjectors().containsKey(encName))
         {
            String mappedName = ref.mappedName();
            if (mappedName == null || mappedName.equals(""))
            {
               // TODO: is this a nice trick?
//               if(ConnectionFactory.class.isAssignableFrom(type))
//               {
//                  // neat little trick
//                  mappedName = "java:/ConnectionFactory";
//               }
//               else
               if(ORB.class.isAssignableFrom(type))
                  mappedName = "java:comp/ORB";
               else if(EJBContext.class.isAssignableFrom(type))
                  mappedName = "java:comp/EJBContext";
               else
                  throw new RuntimeException("You did not specify a @Resource.mappedName() on " + accObj + " and there is no binding for enc name " + encName + " in XML");
            }
            container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, mappedName, "@Resource"));
         }
         injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
      }      
   }
}
