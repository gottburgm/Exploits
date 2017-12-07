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
package org.jboss.test.proxyfactory;

import java.util.HashSet;
import java.util.Set;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.aop.proxy.container.AOPProxyFactory;
import org.jboss.aop.proxy.container.AOPProxyFactoryMixin;
import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.test.AbstractTestCaseWithSetup;
import org.jboss.test.AbstractTestDelegate;

/**
 * AbstractProxyTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public abstract class AbstractProxyTest extends AbstractTestCaseWithSetup
{
   /** The proxy factory */
   protected AOPProxyFactory proxyFactory;

   /**
    * Get the test delegate
    * 
    * @param clazz the test class
    * @return the delegate
    * @throws Exception for any error
    */
   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      return new AbstractProxyTestDelegate(clazz);
   }

   /**
    * Create a new AbstractProxyTest.
    * 
    * @param name the test name
    */
   public AbstractProxyTest(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      configureLogging();
      proxyFactory = new GeneratedAOPProxyFactory();
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createProxy(Object target) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setProxiedClass(target.getClass());
      params.setTarget(target);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateProxy(Object target, Class expected) throws Exception
   {
      Object proxy = createProxy(target);
      assertNotNull(proxy);
      assertTrue("Proxy " + proxy + " should implement " + expected.getName() + " interfaces=" + getInterfaces(proxy), expected.isInstance(proxy));
      return proxy;
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createProxy(Object target, Class[] interfaces) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setProxiedClass(target.getClass());
      params.setInterfaces(interfaces);
      params.setTarget(target);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param mixins the mixins
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createProxy(Object target, AOPProxyFactoryMixin[] mixins) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setProxiedClass(target.getClass());
      params.setMixins(mixins);
      params.setTarget(target);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @param mixins the mixins
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createProxy(Object target, Class[] interfaces, AOPProxyFactoryMixin[] mixins) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setProxiedClass(target.getClass());
      params.setInterfaces(interfaces);
      params.setMixins(mixins);
      params.setTarget(target);
      return proxyFactory.createAdvisedProxy(params);
   }
   /**
    * Create a proxy
    * 
    * @param target the target
    * @param mixins the mixins
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateProxy(Object target, AOPProxyFactoryMixin[] mixins, Class expected) throws Exception
   {
      Object proxy = createProxy(target, mixins);
      assertNotNull(proxy);
      assertTrue("Proxy " + proxy + " should implement " + expected.getName() + " interfaces=" + getInterfaces(proxy), expected.isInstance(proxy));
      return proxy;
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @param mixins the mixins
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateProxy(Object target, Class[] interfaces, AOPProxyFactoryMixin[] mixins, Class[] expected) throws Exception
   {
      Object proxy = createProxy(target, interfaces, mixins);
      assertNotNull(proxy);
      for (int i = 0 ; i < expected.length ; i++)
      {
         assertTrue("Proxy " + proxy + " should implement " + expected[i].getName() + " interfaces=" + getInterfaces(proxy), expected[i].isInstance(proxy));
      }
      return proxy;
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateProxy(Object target, Class[] interfaces, Class expected) throws Exception
   {
      Object proxy = createProxy(target, interfaces);
      assertNotNull(proxy);
      assertTrue("Proxy " + proxy + " should implement " + expected.getName() + " interfaces=" + getInterfaces(proxy), expected.isInstance(proxy));
      return proxy;
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @param metadata the metadata
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createProxy(Object target, Class[] interfaces, SimpleMetaData metaData) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setProxiedClass(target.getClass());
      params.setInterfaces(interfaces);
      params.setSimpleMetaData(metaData);
      params.setTarget(target);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @param metadata the metadata
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateProxy(Object target, Class[] interfaces, SimpleMetaData metaData, Class expected) throws Exception
   {
      Object proxy = createProxy(target, interfaces, metaData);
      assertNotNull(proxy);
      assertTrue("Proxy " + proxy + " should implement " + expected.getName() + " interfaces=" + getInterfaces(proxy), expected.isInstance(proxy));
      return proxy;
   }

   /**
    * Create a proxy
    * 
    * @param interfaces the interfaces
    * @param metaData the metadata
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createHollowProxy(Class[] interfaces, SimpleMetaData metaData) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setInterfaces(interfaces);
      params.setSimpleMetaData(metaData);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param mixins the mixins
    * @param metaData the metadata
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createHollowProxy(AOPProxyFactoryMixin[] mixins, SimpleMetaData metaData) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setMixins(mixins);
      params.setSimpleMetaData(metaData);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param mixins the mixins
    * @param metaData the metadata
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object createHollowProxy(Class[] interfaces, AOPProxyFactoryMixin[] mixins, SimpleMetaData metaData) throws Exception
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setInterfaces(interfaces);
      params.setMixins(mixins);
      params.setSimpleMetaData(metaData);
      return proxyFactory.createAdvisedProxy(params);
   }

   /**
    * Create a proxy
    * 
    * @param interfaces the interfaces
    * @param metadata the metadata
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateHollowProxy(Class[] interfaces, SimpleMetaData metaData, Class expected) throws Exception
   {
      Object proxy = createHollowProxy(interfaces, metaData);
      assertNotNull(proxy);
      assertTrue("Proxy " + proxy + " should implement " + expected.getName() + " interfaces=" + getInterfaces(proxy), expected.isInstance(proxy));
      return proxy;
   }
   
   /**
    * Create a proxy
    * 
    * @param interfaces the interfaces
    * @param metadata the metadata
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateHollowProxy(AOPProxyFactoryMixin[] mixins, SimpleMetaData metaData, Class expected) throws Exception
   {
      Object proxy = createHollowProxy(mixins, metaData);
      assertNotNull(proxy);
      assertTrue("Proxy " + proxy + " should implement " + expected.getName() + " interfaces=" + getInterfaces(proxy), expected.isInstance(proxy));
      return proxy;
   }

   /**
    * Create a proxy
    * 
    * @param target the target
    * @param interfaces the interfaces
    * @param mixins the mixins
    * @param expected the expected class
    * @return the proxy
    * @throws Exception for any error
    */
   protected Object assertCreateHollowProxy(Class[] interfaces, AOPProxyFactoryMixin[] mixins, SimpleMetaData metaData, Class[] expected) throws Exception
   {
      Object proxy = createHollowProxy(interfaces, mixins, metaData);
      assertNotNull(proxy);
      for (int i = 0 ; i < expected.length ; i++)
      {
         assertTrue("Proxy " + proxy + " should implement " + expected[i].getName() + " interfaces=" + getInterfaces(proxy), expected[i].isInstance(proxy));
      }
      return proxy;
   }
   /**
    * Get the interfaces for an object
    * 
    * @param object the object
    * @return the set of interfaces
    */
   protected Set getInterfaces(Object object)
   {
      Set interfaces = new HashSet();
      addInterfaces(interfaces, object.getClass());
      return interfaces;
   }
   
   /**
    * Add interfaces
    * 
    * @param interfaces the interfaces to add to
    * @param clazz the class
    */
   protected void addInterfaces(Set interfaces, Class clazz)
   {
      Class[] intfs = clazz.getInterfaces();
      for (int i = 0; i < intfs.length; ++i)
         interfaces.add(intfs[i]);
      Class superClass = clazz.getSuperclass();
      if (superClass != null)
         addInterfaces(interfaces, superClass);
   }
   
   /**
    * Get the delegate
    * 
    * @return the delegate
    */
   protected AbstractProxyTestDelegate getMCDelegate()
   {
      return (AbstractProxyTestDelegate) getDelegate();
   }

}
