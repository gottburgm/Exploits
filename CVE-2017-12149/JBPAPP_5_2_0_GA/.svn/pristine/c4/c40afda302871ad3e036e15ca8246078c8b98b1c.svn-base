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

import java.net.URL;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.AbstractTestDelegate;

/**
 * Abstract JMX annotation test.
 * Keeps ref to mbean server.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractJMXAnnotationTest extends AbstractIntegrationTest
{
   protected AbstractJMXAnnotationTest(String name)
   {
      super(name);
   }

   protected URL getBeansURL() throws Exception
   {
      Class<?> clazz = getClass();
      String testName = clazz.getName();
      testName = testName.replace('.', '/') + "-beans.xml";
      return clazz.getClassLoader().getResource(testName);
   }

   protected void assertNullControllerContext(Object name) throws Exception
   {
      try
      {
         getControllerContext(name);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         assertInstanceOf(e, IllegalStateException.class);
      }
   }

   public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      return new JMXAwareTestDelegate(clazz);
   }

   private static class JMXAwareTestDelegate extends IntegrationTestDelegate
   {
      public JMXAwareTestDelegate(Class<?> clazz)
      {
         super(clazz);
      }

      protected MBeanServer createMBeanServer()
      {
         return MBeanServerFactory.createMBeanServer("jboss");
      }

      public void tearDown() throws Exception
      {
         MBeanServerLocator.setJBoss(null);
         MBeanServerFactory.releaseMBeanServer(getServer());
      }
   }
}