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

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ExposeJMXAttribute
{
   private MockUnifiedInvokerMBean invoker = new MockUnifiedInvoker();
   private MockTransactionManagerMBean tm = new MockTransactionManager();

   @JMX(exposedInterface = void.class, name="jboss:service=invoker,type=field", registerDirectly = true) private MockUnifiedInvokerMBean invokerField = new MockUnifiedInvoker();
   @JMX(exposedInterface = void.class, registerDirectly = true) private MockTransactionManagerMBean tmField = new MockTransactionManager();

   @JMX(exposedInterface = void.class, name="jboss:service=invoker,type=getter", registerDirectly = true)
   public MockUnifiedInvokerMBean getInvoker()
   {
      return invoker;
   }

   public void setInvoker(MockUnifiedInvokerMBean invoker)
   {
      this.invoker = invoker;
   }

   @JMX(exposedInterface = void.class, registerDirectly = true)
   public MockTransactionManagerMBean getTm()
   {
      return tm;
   }

   public void setTm(MockTransactionManagerMBean tm)
   {
      this.tm = tm;
   }
}