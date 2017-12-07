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
 * JMXAnnotationTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class JMXAnnotationTestCase extends AbstractJMXAnnotationTest
{
   public static Test suite()
   {
      return suite(JMXAnnotationTestCase.class);
   }

   public JMXAnnotationTestCase(String name)
   {
      super(name);
   }

   public void testJMXAnnotationDependency() throws Throwable
   {
      KernelDeployment deployment = deployMC(getBeansURL());

      // mc
      ControllerContext tmMCContext = getControllerContext("TransactionManager2");
      assertNotNull(tmMCContext);
      assertEquals(ControllerState.INSTALLED, tmMCContext.getState());
      // jmx
      ControllerContext tmJMXContext = getControllerContext("jboss:service=TransactionManager2");
      assertNotNull(tmJMXContext);
      assertInstanceOf(tmJMXContext, ServiceControllerContext.class);
      assertEquals(ControllerState.INSTALLED, tmJMXContext.getState());
      Object bean = tmMCContext.getTarget();

      assertNotNull(bean);
      assertInstanceOf(MockTransactionManagerMBean.class, bean);

      ControllerContext mbeanContext = getControllerContext("jboss:service=invoker,type=unified");
      assertNotNull(mbeanContext);
      assertInstanceOf(mbeanContext, ServiceControllerContext.class);
      assertEquals(ControllerState.INSTALLED, mbeanContext.getState());
      Object mbean = mbeanContext.getTarget();
      assertNotNull(mbean);
      assertInstanceOf(MockUnifiedInvokerMBean.class, mbean);
      
      validate();
      validateMC();

      undeployMC(deployment);

      assertNullControllerContext("TransactionManager2");
      assertNullControllerContext("jboss:service=TransactionManager2");
   }
}
