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
package org.jboss.test.spring.facade;

import org.jboss.logging.Logger;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.spring.facade.KernelControllerListableBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SpringFacadeTester
{   
   private Logger log = Logger.getLogger(getClass());
   private KernelController controller;

   public SpringFacadeTester(KernelController controller)
   {
      this.controller = controller;
   }

   protected void testFacade() throws Throwable
   {
      log.warn("Testing Spring Facade ...");

      ListableBeanFactory factory = new KernelControllerListableBeanFactory(controller);
      // bean factory
      factory.getBean("MainDeployer");
      // TODO - other methods
      // listable
      if (factory.containsBeanDefinition("MainDeployer") == false)
         throw new IllegalArgumentException("Illegal impl");
      // TODO - other methods

      log.warn("Finished testing Spring Facade ...");
   }

   public void start() throws Throwable
   {
      testFacade();
   }
}
