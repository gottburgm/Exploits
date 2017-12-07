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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Used as a post processor singleton declared directly in .xml descriptor file.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @see MicrocontainerConfigurer
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 */
public class ContextMicrocontainerConfigurer extends MicrocontainerConfigurer
      implements BeanNameAware, BeanFactoryAware
{

   private String beanName;
   private BeanFactory beanFactory;

   public ContextMicrocontainerConfigurer()
   {
      setLocator(new KernelBinderLocator());
   }

   /**
    * Check that we're not parsing our own bean definition,
    * to avoid failing on unresolvable placeholders in properties file locations.
    */
   protected boolean checkBean(ConfigurableBeanFactory beanFactoryToProcess, String beanName)
   {
      return !(beanName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory));
   }

   /**
    * Only necessary to check that we're not parsing our own bean definition,
    * to avoid failing on unresolvable placeholders in properties file locations.
    * The latter case can happen with placeholders for system properties in
    * resource locations.
    *
    * @see #setLocations
    * @see org.springframework.core.io.ResourceEditor
    */
   public void setBeanName(String beanName)
   {
      this.beanName = beanName;
   }

   /**
    * Only necessary to check that we're not parsing our own bean definition,
    * to avoid failing on unresolvable placeholders in properties file locations.
    * The latter case can happen with placeholders for system properties in
    * resource locations.
    *
    * @see #setLocations
    * @see org.springframework.core.io.ResourceEditor
    */
   public void setBeanFactory(BeanFactory beanFactory)
   {
      this.beanFactory = beanFactory;
   }

}
