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
package org.jboss.spring.factory;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.jboss.spring.io.VFSResourcePatternResolver;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class NamedXmlApplicationContext extends ClassPathXmlApplicationContext implements Nameable
{
   private String defaultName;
   private Resource resource;
   private NamedXmlBeanDefinitionReader beanDefinitionReader;

   public NamedXmlApplicationContext(String defaultName, Resource resource) throws BeansException
   {
      this(defaultName, resource, true);
   }

   public NamedXmlApplicationContext(String defaultName, Resource resource, boolean refresh) throws BeansException
   {
      //loading config from Resource
      super(new String[]{}, false);
      this.defaultName = defaultName;
      this.resource = resource;
      if (refresh)
      {
         refresh();
      }
   }

   protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException
   {
      // Create a new XmlBeanDefinitionReader for the given BeanFactory.
      beanDefinitionReader = new NamedXmlBeanDefinitionReader(beanFactory);

      // Configure the bean definition reader with this context's
      // resource loading environment.
      beanDefinitionReader.setResourceLoader(this);
      if (getClassLoader() != null)
         beanDefinitionReader.setBeanClassLoader(getClassLoader());
      beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

      // Allow a subclass to provide custom initialization of the reader,
      // then proceed with actually loading the bean definitions.
      initBeanDefinitionReader(beanDefinitionReader);
      loadBeanDefinitions(beanDefinitionReader);
   }

   protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException
   {
      reader.loadBeanDefinitions(resource);
   }

   public String getName()
   {
      String name = beanDefinitionReader.getName() != null ? beanDefinitionReader.getName() : defaultName;
      if (name == null)
         throw new IllegalArgumentException("Bean factory JNDI name must be set!");

      return name;
   }

   protected ResourcePatternResolver getResourcePatternResolver()
   {
      return new VFSResourcePatternResolver(getClassLoader());
   }
}
