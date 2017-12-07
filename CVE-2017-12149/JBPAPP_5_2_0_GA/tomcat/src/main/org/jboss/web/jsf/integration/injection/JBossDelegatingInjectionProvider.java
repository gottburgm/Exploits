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

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.InstanceManager;
import org.jboss.logging.Logger;
import org.jboss.web.tomcat.service.TomcatInjectionContainer;

import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderException;

/**
 * A JSF injection provider. This class delegates the injection to the <code>TomcatInjectionContainer</code>.
 * The processing of this injectionProvider is based on the <code>JBossWebMetaData</code>
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class JBossDelegatingInjectionProvider implements InjectionProvider
{
   /** The injection container. */
   protected TomcatInjectionContainer injectionContainer = null;
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(InjectionProvider.class);
   
   public JBossDelegatingInjectionProvider()
   {
      Object context = FacesContext.getCurrentInstance().getExternalContext().getContext();
      // In case of a servletContext - could maybe also be a PortletContext !?
      if(context instanceof ServletContext && context != null)
      {
         ServletContext servletContext = (ServletContext) (context);
         this.injectionContainer = (TomcatInjectionContainer) servletContext
                     .getAttribute(InstanceManager.class.getName());
         
      }
      if(injectionContainer == null)
         log.debug("JSF injection not available for this web deployment.");
   }
   
   protected JBossDelegatingInjectionProvider(TomcatInjectionContainer injectionContainer)
   {
      this.injectionContainer = injectionContainer;
      
      if(injectionContainer == null)
         log.debug("JSF injection not available for this web deployment.");      
   }
   
   public void inject(Object object) throws InjectionProviderException
   {
      if(! checkInjectionContainer()) return;
      try
      {
         injectionContainer.processInjectors(object);
      }
      catch(Throwable t)
      {
         throw new InjectionProviderException("unable to process injections.", t);
      }
   }

   public void invokePostConstruct(Object object) throws InjectionProviderException
   {
      if(! checkInjectionContainer()) return;
      try
      {
         injectionContainer.postConstruct(object);
      }
      catch(Throwable t)
      {
         throw new InjectionProviderException("unable to process invokePostConstruct.", t);
      }
   }

   public void invokePreDestroy(Object object) throws InjectionProviderException
   {
      if(! checkInjectionContainer()) return;
      try
      {
         injectionContainer.preDestroy(object);
      }
      catch(Throwable t)
      {
         throw new InjectionProviderException("unable to process invokePreDestroy.", t);
      }
   }

   private boolean checkInjectionContainer()
   {
      return injectionContainer != null;
   }
}

