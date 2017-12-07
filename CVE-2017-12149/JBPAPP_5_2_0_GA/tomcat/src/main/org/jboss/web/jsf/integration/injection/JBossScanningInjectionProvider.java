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
package org.jboss.web.jsf.integration.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.InstanceManager;
import org.jboss.logging.Logger;
import org.jboss.web.tomcat.service.TomcatInjectionContainer;

import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderException;

/**
 * A JSF injection provider.
 * The dynamic processing of annotations and injections is handled by the
 * <code>TomcatInjectionContainer</code>.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class JBossScanningInjectionProvider implements InjectionProvider
{

   /** The injection container. */
   protected TomcatInjectionContainer injectionContainer = null;

   /** The logger. */
   private static final Logger log = Logger.getLogger(InjectionProvider.class);

   public JBossScanningInjectionProvider()
   {
      Object context = FacesContext.getCurrentInstance().getExternalContext().getContext();
      // In case of a servletContext - could maybe also be a PortletContext !?
      if (context != null && context instanceof ServletContext)
      {
         ServletContext servletContext = (ServletContext) (context);
         this.injectionContainer = (TomcatInjectionContainer) servletContext
                     .getAttribute(InstanceManager.class.getName());
      }
      if (injectionContainer == null)
         log.debug("JSF injection not available for this web deployment.");
   }
   
   protected JBossScanningInjectionProvider(TomcatInjectionContainer injectionContainer)
   {
      this.injectionContainer = injectionContainer;
      
      if(injectionContainer == null)
         log.debug("JSF injection not available for this web deployment.");      
   }

   /**
    * Invoke a postConstruct method annotated with @PostConstruct
    * 
    * @param managedBean the managed bean
    * @throws InjectionProviderException
    */
   public void invokePostConstruct(Object managedBean) throws InjectionProviderException
   {
      try
      {
         Method postConstruct = getLifeCycleMethod(managedBean, PostConstruct.class);

         if (postConstruct != null)
         {
            boolean accessibility = postConstruct.isAccessible();
            postConstruct.setAccessible(true);
            postConstruct.invoke(managedBean);
            postConstruct.setAccessible(accessibility);
         }
      }
      catch (Exception e)
      {
         throw new InjectionProviderException("PostConstruct failed on managed bean.", e);
      }
   }
   
   /**
    * Invoke a preDestroy method annotated with @PreDestroy
    * 
    * @param managedBean the managed bean
    * @throws InjectionProviderException
    */
   public void invokePreDestroy(Object managedBean) throws InjectionProviderException
   {
      try
      {
         Method preDestroy = getLifeCycleMethod(managedBean, PreDestroy.class);

         if (preDestroy != null)
         {
            boolean accessibility = preDestroy.isAccessible();
            preDestroy.setAccessible(true);
            preDestroy.invoke(managedBean);
            preDestroy.setAccessible(accessibility);
         }
      }
      catch (Exception e)
      {
         throw new InjectionProviderException("PreDestroy failed on managed bean.", e);
      }
   }
   
   /**
    * Process annotations and injection for a managedBean.
    * This delegates the processing of annotations and injection
    * to the <code>TomcatInjectionContainer</code>.
    * 
    * @param managedBean the managed bean
    * @throws InjectionProviderException
    */
   public void inject(Object managedBean) throws InjectionProviderException
   {
      if(! checkInjectionContainer()) return;
      try
      {
         // Process annotations
         injectionContainer.processAnnotations(managedBean);
         
         // Process injectors
         injectionContainer.processInjectors(managedBean);
         
      }
      catch(Exception e)
      {
         throw new InjectionProviderException("Injection failed on managed bean.", e);
      }
   }

   private Method getLifeCycleMethod(Object managedBean, Class<? extends Annotation> annotation)
   {
      Method[] methods = managedBean.getClass().getDeclaredMethods();
      Method lifeCycleMethod = null;
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].isAnnotationPresent(annotation))
         {
            if ((lifeCycleMethod != null) || (methods[i].getParameterTypes().length != 0)
                  || (Modifier.isStatic(methods[i].getModifiers())) || (methods[i].getExceptionTypes().length > 0)
                  || (!methods[i].getReturnType().getName().equals("void")))
            {
               throw new IllegalArgumentException("Invalid PostConstruct method.");
            }
            lifeCycleMethod = methods[i];
         }
      }

      return lifeCycleMethod;
   }
   
   private boolean checkInjectionContainer()
   {
      return injectionContainer != null;
   }

}
