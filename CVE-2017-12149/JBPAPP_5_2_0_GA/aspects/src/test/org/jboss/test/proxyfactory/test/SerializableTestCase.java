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

import java.io.Serializable;
import java.lang.reflect.Method;

import junit.framework.Test;

import org.jboss.test.proxyfactory.AbstractProxyTest;
import org.jboss.test.proxyfactory.support.SimpleInterceptor;
import org.jboss.test.proxyfactory.support.TestConnectionManager;
import org.jboss.test.proxyfactory.support.TestManagedConnectionFactory;

/**
 * SerializableTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class SerializableTestCase extends AbstractProxyTest
{
   public void testSerializable() throws Exception
   {
      Serializable object = (Serializable) assertCreateProxy(new TestManagedConnectionFactory(), Serializable.class);
      byte[] bytes = serialize(object);
      Object result = deserialize(bytes);
      assertNotNull(result);
      assertTrue(result instanceof TestManagedConnectionFactory);
      TestManagedConnectionFactory mcf = (TestManagedConnectionFactory) result;
      TestConnectionManager cm = new TestConnectionManager();
      Object cf = mcf.createConnectionFactory(cm);
      assertTrue(cm == cf);
      Method method = SimpleInterceptor.invoked;
      assertNotNull(method);
      assertEquals("createConnectionFactory", method.getName());
   }
   
   public static Test suite()
   {
      return suite(SerializableTestCase.class);
   }

   public SerializableTestCase(String name)
   {
      super(name);
   }
}
