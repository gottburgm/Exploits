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

import org.jboss.aop.proxy.container.AOPProxyFactoryMixin;
import org.jboss.test.proxyfactory.AbstractProxyTest;
import org.jboss.test.proxyfactory.support.Another;
import org.jboss.test.proxyfactory.support.AnotherMixin;
import org.jboss.test.proxyfactory.support.Other;
import org.jboss.test.proxyfactory.support.PlainBean;
import org.jboss.test.proxyfactory.support.ReturningInterceptor;
import org.jboss.test.proxyfactory.support.Simple;
import org.jboss.test.proxyfactory.support.SimpleInterceptor;
import org.jboss.test.proxyfactory.support.SimpleMixin;
import org.jboss.test.proxyfactory.support.Tagging;

/**
 * DataSourceTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class InterfaceAndMixinTestCase extends AbstractProxyTest
{
   public void testInterfaceAndMixin() throws Exception
   {
      SimpleMixin.invoked = false;
      SimpleInterceptor.invoked = null;
      ReturningInterceptor.invoked = null;
      AnotherMixin.invoked = false;

      PlainBean bean = new PlainBean();
      AOPProxyFactoryMixin[] mixins = {
            new AOPProxyFactoryMixin(SimpleMixin.class, new Class[]{Simple.class}),
            new AOPProxyFactoryMixin(AnotherMixin.class, new Class[] {Another.class})};
      Object proxy = assertCreateProxy(
            bean, 
            new Class[] {Other.class, Tagging.class}, 
            mixins, 
            new Class[] {Other.class, Simple.class, Tagging.class, Another.class});

      Simple simple = (Simple)proxy;
      simple.doSomething();
      assertTrue(SimpleMixin.invoked);
      assertNotNull(SimpleInterceptor.invoked);
      assertEquals("doSomething", SimpleInterceptor.invoked.getName());
      assertNull(ReturningInterceptor.invoked);
      assertFalse(AnotherMixin.invoked);
      
      SimpleInterceptor.invoked = null;
      SimpleMixin.invoked = false;
      Other other = (Other)proxy;
      other.otherMethod();
      assertFalse(SimpleMixin.invoked);
      assertNotNull(ReturningInterceptor.invoked);
      assertEquals("otherMethod", ReturningInterceptor.invoked.getName());
      assertNull(SimpleInterceptor.invoked);
      assertFalse(AnotherMixin.invoked);
      
      SimpleInterceptor.invoked = null;
      ReturningInterceptor.invoked = null;
      SimpleMixin.invoked = false;
      Another another = (Another)proxy;
      another.anotherMethod();
      assertFalse(SimpleMixin.invoked);
      assertNull(ReturningInterceptor.invoked);
      assertNull(SimpleInterceptor.invoked);
      assertTrue(AnotherMixin.invoked);
   }
   
   public static Test suite()
   {
      return suite(InterfaceAndMixinTestCase.class);
   }

   public InterfaceAndMixinTestCase(String name)
   {
      super(name);
   }
}
