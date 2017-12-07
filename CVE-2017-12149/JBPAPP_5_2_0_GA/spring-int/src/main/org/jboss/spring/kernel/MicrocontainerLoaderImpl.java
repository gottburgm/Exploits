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
package org.jboss.spring.kernel;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.spring.loader.AbstractBeanFactoryLoader;
import org.jboss.spring.factory.NamedXmlApplicationContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class MicrocontainerLoaderImpl extends AbstractBeanFactoryLoader
{

   protected BeanFactory createBeanFactory(String defaultName, Resource resource)
   {
      ConfigurableApplicationContext applicationContext = new NamedXmlApplicationContext(defaultName, resource, false);
      MicrocontainerConfigurer microcontainerPostProcessor = new MicrocontainerConfigurer();
      Locator locator = new NullLocator();
      KernelController controller = getKernelController();
      Kernel kernel = getKernel();
      if (controller != null)
      {
         locator = new ControllerLocator(controller);
      }
      else if (kernel != null)
      {
         locator = new KernelLocator(kernel);
      }
      microcontainerPostProcessor.setLocator(locator);
      applicationContext.addBeanFactoryPostProcessor(microcontainerPostProcessor);
      applicationContext.refresh();
      return applicationContext;
   }

   protected void doClose(BeanFactory beanFactory)
   {
      ((ConfigurableApplicationContext) beanFactory).close();
   }

   protected Class getExactBeanFactoryClass()
   {
      return ApplicationContext.class;
   }

   protected Kernel getKernel()
   {
      return null;
   }

   protected KernelController getKernelController()
   {
      return null;
   }

}
