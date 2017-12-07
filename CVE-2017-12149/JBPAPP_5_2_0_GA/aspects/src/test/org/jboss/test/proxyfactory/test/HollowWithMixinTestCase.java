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

import java.lang.reflect.Method;

import junit.framework.Test;

import org.jboss.aop.proxy.container.AOPProxyFactoryMixin;
import org.jboss.aop.proxy.container.Delegate;
import org.jboss.test.proxyfactory.AbstractProxyTest;
import org.jboss.test.proxyfactory.support.PlainBean;
import org.jboss.test.proxyfactory.support.Simple;
import org.jboss.test.proxyfactory.support.SimpleInterceptor;
import org.jboss.test.proxyfactory.support.SimpleMixin;
import org.jboss.test.proxyfactory.support.SimpleMixinWithConstructorAndDelegate;

/**
 * HollowTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class HollowWithMixinTestCase extends AbstractProxyTest
{
   public void testSimpleMixinDefaultConstructor() throws Exception
   {
      SimpleMixin.invoked = false;
      SimpleInterceptor.invoked = null;

      AOPProxyFactoryMixin[] mixins = {new AOPProxyFactoryMixin(SimpleMixin.class, new Class[]{Simple.class})};
      Object proxy = assertCreateHollowProxy(mixins, null, Simple.class);

      Simple simple = (Simple)proxy;
      simple.doSomething();
      assertTrue(SimpleMixin.invoked);
      assertNotNull(SimpleInterceptor.invoked);
      assertEquals("doSomething", SimpleInterceptor.invoked.getName());
   }
   
   public void testSimpleMixinConstructorProxyTarget() throws Exception
   {
      SimpleMixinWithConstructorAndDelegate.invoked = false;
      SimpleMixinWithConstructorAndDelegate.proxy = null;
      SimpleMixinWithConstructorAndDelegate.delegate = null;

      AOPProxyFactoryMixin[] mixins = {new AOPProxyFactoryMixin(SimpleMixinWithConstructorAndDelegate.class, new Class[]{Simple.class}, "this")};
      Object proxy = assertCreateHollowProxy(mixins, null, Simple.class);

      System.out.println(SimpleMixinWithConstructorAndDelegate.delegate);
      Simple simple = (Simple)proxy;
      simple.doSomething();
      assertTrue(SimpleMixinWithConstructorAndDelegate.invoked);
      assertNotNull(SimpleMixinWithConstructorAndDelegate.proxy);
      assertEquals(proxy, SimpleMixinWithConstructorAndDelegate.proxy);
      assertNotNull(SimpleMixinWithConstructorAndDelegate.delegate);
      assertTrue(SimpleMixinWithConstructorAndDelegate.delegate.getClass().equals(Object.class));
      assertNotNull(SimpleInterceptor.invoked);
      assertEquals("doSomething", SimpleInterceptor.invoked.getName());
      
      PlainBean bean = new PlainBean();
      ((Delegate)proxy).setDelegate(bean);
      assertNotNull(SimpleMixinWithConstructorAndDelegate.delegate);
      assertEquals(bean, SimpleMixinWithConstructorAndDelegate.delegate);
   }
   
   public static Test suite()
   {
      return suite(HollowWithMixinTestCase.class);
   }

   public HollowWithMixinTestCase(String name)
   {
      super(name);
   }
}
