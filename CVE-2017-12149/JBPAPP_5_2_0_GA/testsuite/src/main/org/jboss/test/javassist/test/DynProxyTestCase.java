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
package org.jboss.test.javassist.test;


import java.util.Set;
import java.util.HashSet;
import java.util.AbstractCollection;

import javassist.util.proxy.ProxyFactory;
import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.test.javassist.test.support.ThingMethodHandler;
import org.jboss.test.javassist.test.support.AThing;
import org.jboss.test.javassist.test.support.IThing;
import org.jboss.test.javassist.test.support.AbstractThing;
import org.jboss.test.javassist.test.support.MyCollectionHandler;

/**
 * Tests of the javassit proxy framework
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class DynProxyTestCase
   extends TestCase
{
   private static final Logger log = Logger.getLogger(DynProxyTestCase.class);

   public DynProxyTestCase(String name)
   {
      super(name);
   }

   /**
    * Test creating a proxy for an interface
    * @throws Exception
    */
   public void testInterfaceProxy() throws Exception
   {
      log.info("+++ testInterfaceProxy");
      ProxyFactory factory = new ProxyFactory();
      AThing athing = new AThing();
      ThingMethodHandler handler = new ThingMethodHandler(athing);
      factory.setHandler(handler);
      Class[] ifaces = {IThing.class};
      factory.setInterfaces(ifaces);
      Class[] sig = {};
      Object[] args = {};
      IThing proxy = (IThing) factory.create(sig, args);
      proxy.method1();
      assertEquals("method1Count", 1, athing.getMethod1Count());
      proxy.method2("testInterfaceProxy");
      assertEquals("method2Count", 1, athing.getMethod2Count());
      proxy.method3(proxy);
      assertEquals("method3Count", 1, athing.getMethod3Count());
      assertEquals("method2Count", 2, athing.getMethod2Count());
   }

   /**
    * Test creating a proxy for an abstract class
    * @throws Exception
    */
   public void testAbstractProxy() throws Exception
   {
      log.info("+++ testAbstractProxy");
      ProxyFactory factory = new ProxyFactory();
      AThing athing = new AThing();
      ThingMethodHandler handler = new ThingMethodHandler(athing);
      factory.setHandler(handler);
      factory.setSuperclass(AbstractThing.class);
      Class[] sig = {};
      Object[] args = {};
      AbstractThing proxy = (AbstractThing) factory.create(sig, args);
      proxy.method1();
      assertEquals("method1Count", 1, athing.getMethod1Count());
      proxy.method2("testInterfaceProxy");
      assertEquals("method2Count", 1, athing.getMethod2Count());
      proxy.method3(athing);
      assertEquals("method3Count", 1, athing.getMethod3Count());
      assertEquals("method2Count", 2, athing.getMethod2Count());
   }

   /**
    * Test creating a proxy for an abstract class with the abstract
    * superclass being from the jdk (java.util.AbstractCollection).
    * 
    * @throws Exception
    */
   public void testAbstractJDKClassProxy() throws Exception
   {
      log.info("+++ testAbstractJDKClassProxy");
      ProxyFactory factory = new ProxyFactory();
      HashSet aset = new HashSet();
      MyCollectionHandler handler = new MyCollectionHandler(aset);
      factory.setHandler(handler);
      factory.setSuperclass(java.util.AbstractCollection.class);
      Class[] sig = {};
      Object[] args = {};
      AbstractCollection proxy = (AbstractCollection) factory.create(sig, args);
      proxy.add("Add");
      assertEquals("size", 1, aset.size());
      proxy.remove("Add");
      assertEquals("size", 0, aset.size());
      assertEquals("isEmpty", true, proxy.isEmpty());
   }
}
