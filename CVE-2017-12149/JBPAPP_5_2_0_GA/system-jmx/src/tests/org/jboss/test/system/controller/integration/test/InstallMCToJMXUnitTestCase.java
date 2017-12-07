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

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.test.system.controller.integration.support.SimpleBean;
import org.jboss.test.system.controller.support.SimpleMBean;

import junit.framework.Test;

/**
 * InstallMCToJMXUnitTestCase test of injecting an mc bean into a jmx mbean
 * as part of the mc bean install action.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class InstallMCToJMXUnitTestCase extends AbstractIntegrationTest
{
   public static Test suite()
   {
      return suite(InstallMCToJMXUnitTestCase.class);
   }

   public InstallMCToJMXUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testInstallMCToJMX() throws Throwable
   {
      checkInject();
   }

   /**
    * Validate that:
    * 
    * jboss.test:type=test mbean context exists
    * jboss.test:type=test target is not null and an instanceof SimpleMBean
    * Test bean context exists
    * Test bean target is not null and an instanceof SimpleBean
    * That the jboss.test:type=test.simpleBean property value injected was the Test instance
    * 
    * @throws Throwable
    */
   public void checkInject() throws Throwable
   {
      ControllerContext mbeanContext = getControllerContext("jboss.test:type=test");
      assertNotNull(mbeanContext);
      Object mbean = mbeanContext.getTarget();
      assertNotNull(mbean);
      assertTrue(mbean instanceof SimpleMBean);

      ControllerContext beanContext = getControllerContext("Test");
      assertNotNull(beanContext);
      Object bean = beanContext.getTarget();
      assertNotNull(bean);
      assertTrue(bean instanceof SimpleBean);
      
      SimpleMBean simpleMBean = (SimpleMBean) mbean;
      
      Object injected = simpleMBean.getSimpleBean();
      
      assertTrue("Bean was not injected: ", bean == injected);
   }

}
