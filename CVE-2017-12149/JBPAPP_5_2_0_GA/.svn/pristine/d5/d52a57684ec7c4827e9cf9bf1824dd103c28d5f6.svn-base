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
package org.jboss.test.system.controller.integration.support;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.LifecycleCallbackItem;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.event.KernelEvent;
import org.jboss.kernel.spi.event.KernelEventEmitter;
import org.jboss.kernel.spi.event.KernelEventListener;
import org.jboss.metadata.spi.MetaData;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ExposeJMXAttributeChecker implements KernelEventListener
{
   private KernelController controller;
   private KernelEventEmitter emitter;
   private TestServiceControllerLifecycleCallback service;

   public ExposeJMXAttributeChecker(
         KernelController controller,
         KernelEventEmitter emitter,
         TestServiceControllerLifecycleCallback service)
   {
      this.controller = controller;
      this.emitter = emitter;
      this.service = service;
   }

   public void create() throws Throwable
   {
      emitter.registerListener(this, null, null);
   }

   public void destroy() throws Throwable
   {
      emitter.unregisterListener(this, null, null);
   }

   @SuppressWarnings("deprecation")
   public void onEvent(KernelEvent event, Object o)
   {
      try
      {
         if (org.jboss.kernel.spi.registry.KernelRegistry.KERNEL_REGISTRY_REGISTERED.equals(event.getType()))
         {
            ControllerContext context = controller.getContext(event.getContext(), null);
            addControllerContext(context);
         }
      }
      catch (Throwable ignored)
      {
      }
   }

   private JMX readJmxAnnotation(ControllerContext context) throws Exception
   {
      MetaData metaData = context.getScopeInfo().getMetaData();
      if (metaData != null)
         return metaData.getAnnotation(JMX.class);
      return null;
   }

   public void addControllerContext(ControllerContext context) throws Throwable
   {
      JMX jmx = readJmxAnnotation(context);
      if (jmx != null)
      {
         DependencyInfo info = context.getDependencyInfo();
         info.addLifecycleCallback(new ServiceLifecycleCallback());
      }
   }

   private class ServiceLifecycleCallback implements LifecycleCallbackItem
   {
      public Object getBean()
      {
         return null;
      }

      public ControllerState getWhenRequired()
      {
         return ControllerState.INSTALLED;
      }

      public ControllerState getDependentState()
      {
         return ControllerState.INSTALLED;
      }

      public void install(ControllerContext ctx) throws Exception
      {
         service.install(ctx);
      }

      public void uninstall(ControllerContext ctx)
      {
         try
         {
            service.uninstall(ctx);
         }
         catch (Exception ignored)
         {
         }
      }
   }
}