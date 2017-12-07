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
package org.jboss.test.proxyfactory.test;

import junit.framework.Test;

import org.jboss.test.proxyfactory.AbstractProxyTest;
import org.jboss.test.proxyfactory.support.InstanceInterceptor;
import org.jboss.test.proxyfactory.support.Simple;
import org.jboss.test.proxyfactory.support.SimpleBean;

/**
 * InstanceAdvisedTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class InstanceAdvisedTestCase extends AbstractProxyTest
{
   public void testInstanceAdvised() throws Exception
   {
      SimpleBean bean1 = new SimpleBean();
      Simple simple1 = (Simple) assertCreateProxy(bean1, Simple.class);
      SimpleBean bean2 = new SimpleBean();
      Simple simple2 = (Simple) assertCreateProxy(bean2, Simple.class);
      InstanceInterceptor.last = null;
      simple1.doSomething();
      assertTrue(bean1.invoked);
      Object instance1 = InstanceInterceptor.last;
      assertNotNull(instance1);
      InstanceInterceptor.last = null;
      simple2.doSomething();
      assertTrue(bean2.invoked);
      Object instance2 = InstanceInterceptor.last;
      assertNotNull(instance2);
      assertFalse(instance1 == instance2);
   }
   
   public static Test suite()
   {
      return suite(InstanceAdvisedTestCase.class);
   }

   public InstanceAdvisedTestCase(String name)
   {
      super(name);
   }
}
