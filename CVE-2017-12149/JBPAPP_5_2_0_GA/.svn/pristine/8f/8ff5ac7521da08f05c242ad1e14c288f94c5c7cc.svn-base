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
package org.jboss.test.system.controller.integration.test;

import junit.framework.Test;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.test.system.controller.integration.support.MockTransactionManagerMBean;
import org.jboss.test.system.controller.integration.support.MockUnifiedInvokerMBean;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * JMXAnnotationPluginTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class JMXAnnotationPluginTestCase extends AbstractJMXAnnotationTest
{
   public static Test suite()
   {
      return suite(JMXAnnotationPluginTestCase.class);
   }

   public JMXAnnotationPluginTestCase(String name)
   {
      super(name);
   }

   public void testJMXAnnotationPlugin() throws Throwable
   {
      KernelDeployment deployment = deployMC(getBeansURL());

      // getter
      ControllerContext mbeanContext = getControllerContext("jboss:service=invoker,type=getter");
      assertNotNull(mbeanContext);
      assertInstanceOf(mbeanContext, ServiceControllerContext.class);
      assertEquals(ControllerState.INSTALLED, mbeanContext.getState());
      assertInstanceOf(MockUnifiedInvokerMBean.class, mbeanContext.getTarget());
      // field
      mbeanContext = getControllerContext("jboss:service=invoker,type=field");
      assertNotNull(mbeanContext);
      assertInstanceOf(mbeanContext, ServiceControllerContext.class);
      assertEquals(ControllerState.INSTALLED, mbeanContext.getState());
      assertInstanceOf(MockUnifiedInvokerMBean.class, mbeanContext.getTarget());

      // getter
      ControllerContext tmJMXContext = getControllerContext("jboss.pojo:attribute=tm,name='ExposeJMXAttribute'");
      assertNotNull(tmJMXContext);
      assertInstanceOf(tmJMXContext, ServiceControllerContext.class);
      assertEquals(ControllerState.INSTALLED, tmJMXContext.getState());
      assertInstanceOf(MockTransactionManagerMBean.class, tmJMXContext.getTarget());
      // field
      tmJMXContext = getControllerContext("jboss.pojo:attribute=tmField,name='ExposeJMXAttribute'");
      assertNotNull(tmJMXContext);
      assertInstanceOf(tmJMXContext, ServiceControllerContext.class);
      assertEquals(ControllerState.INSTALLED, tmJMXContext.getState());
      assertInstanceOf(MockTransactionManagerMBean.class, tmJMXContext.getTarget());

      validate();
      validateMC();

      undeployMC(deployment);

      assertNullControllerContext("ExposeJMXAttribute");
      assertNullControllerContext("jboss:service=invoker,type=getter");
      assertNullControllerContext("jboss:service=invoker,type=field");
      assertNullControllerContext("jboss.pojo:name='ExposeJMXAttribute',attribute=tm");
      assertNullControllerContext("jboss.pojo:name='ExposeJMXAttribute',attribute=tmField");
   }
}