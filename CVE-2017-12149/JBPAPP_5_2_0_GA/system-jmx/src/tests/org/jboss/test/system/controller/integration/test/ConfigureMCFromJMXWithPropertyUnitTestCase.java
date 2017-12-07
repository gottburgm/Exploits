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
import org.jboss.test.system.controller.integration.support.SimpleStringBean;
import org.jboss.test.system.controller.support.Simple;

/**
 * ConfigureMCFromJMXUWithPropertynitTestCase.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class ConfigureMCFromJMXWithPropertyUnitTestCase extends AbstractIntegrationTest
{
   public static Test suite()
   {
      return suite(ConfigureMCFromJMXWithPropertyUnitTestCase.class);
   }

   public ConfigureMCFromJMXWithPropertyUnitTestCase(String name)
   {
      super(name);
   }

   public void testConfigureMCFromJMX() throws Throwable
   {
      ControllerContext mbeanContext = getControllerContext("jboss.test:type=test");
      assertNotNull(mbeanContext);
      Object mbean = mbeanContext.getTarget();
      assertNotNull(mbean);
      assertInstanceOf(Simple.class, mbean);
      Simple simple = (Simple)mbean;
      String string = simple.getAttribute1();
      assertEquals("someattrib", string);

      ControllerContext beanContext = getControllerContext("Test");
      assertNotNull(beanContext);
      Object bean = beanContext.getTarget();
      assertNotNull(bean);
      assertInstanceOf(SimpleStringBean.class, bean);

      SimpleStringBean simpleBean = (SimpleStringBean) bean;
      assertEquals(string, simpleBean.getString1());
      assertEquals(string, simpleBean.getString2());
   }
}
