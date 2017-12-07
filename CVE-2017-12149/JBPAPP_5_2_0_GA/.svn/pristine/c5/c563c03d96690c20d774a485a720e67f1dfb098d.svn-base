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
package org.jboss.test.ejb.proxy.test;

import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb.proxy.beans.StatefulCounter;
import org.jboss.test.ejb.proxy.beans.StatefulCounterHome;
import org.jboss.test.ejb.proxy.beans.HandleRetrievalStatefulSessionInterceptor.RetrievalMethodHandle;

/**
 * ProxyLogicTestCase.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class ProxyLogicTestCase extends JBossTestCase
{
   public ProxyLogicTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ProxyLogicTestCase.class, "ejbproxy-test.jar");
   }

   public void testEjbGetObjectRetrievalMethod() throws Exception
   {
      getLog().debug(getName());
      
      Object ref = getInitialContext().lookup("ejb/StatefulCounterEjb");
      StatefulCounterHome home = (StatefulCounterHome) PortableRemoteObject.narrow(ref, StatefulCounterHome.class);
      StatefulCounter counter = home.create();

      assertEquals(1, counter.count());
      assertEquals(2, counter.count());

      RetrievalMethodHandle handle = (RetrievalMethodHandle)counter.getHandle();
      counter = (StatefulCounter) handle.getEJBObject();
      
      assertEquals(3, counter.count());
      assertEquals(4, counter.count());
      
      assertTrue(handle.isGotEjbObjectViaJndi());
      assertFalse(handle.isGotEjbObjectViaInvoker());
      
      System.setProperty("org.jboss.ejb.sfsb.handle.V327", "whateveryouwant");
      
      handle = (RetrievalMethodHandle)counter.getHandle();
      counter = (StatefulCounter) handle.getEJBObject();

      assertEquals(5, counter.count());
      assertEquals(6, counter.count());
      
      assertFalse(handle.isGotEjbObjectViaJndi());
      assertTrue(handle.isGotEjbObjectViaInvoker());      
   }   
}