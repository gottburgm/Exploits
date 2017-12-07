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
import org.jboss.test.system.controller.support.Simple;

/**
 * InjectJMXFromMCTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class InjectJMXFromMCTest extends AbstractIntegrationTest
{
   private static final String TEST = "Test";
   
   public InjectJMXFromMCTest(String name)
   {
      super(name);
   }
   
   protected String getContextName()
   {
      return TEST;
   }
   
   public void checkInject() throws Throwable
   {
      Simple simple = getSimple();

      ControllerContext beanContext = getControllerContext(getContextName());
      assertNotNull(beanContext);
      Object bean = beanContext.getTarget();
      assertNotNull(bean);
      assertTrue(bean instanceof SimpleBean);
      
      Object injected = simple.getSimpleBean();
      
      assertTrue("Bean was not injected: ", bean == injected);
   }
}
