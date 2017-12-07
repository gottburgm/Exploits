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
package test.implementation.interceptor;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.InvocationContext;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.service.ServiceConstants;

import test.implementation.interceptor.support.MySharedInterceptor;


/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $
 *   
 */
public class SharedInterceptorTEST extends TestCase
   implements ServerConstants, ServiceConstants
{
   public SharedInterceptorTEST(String s)
   {
      super(s);
   }
   
   
   public void testSharedInterceptor() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      
      MySharedInterceptor shared = new MySharedInterceptor();
      
      ObjectName oname = shared.register(server);
      
      assertTrue(server.isRegistered(new ObjectName(
            JBOSSMX_DOMAIN + ":" + "type=Interceptor,name=MySharedInterceptor,ID=0"
      )));
      
      InvocationContext ic = new InvocationContext();
      Invocation i = new Invocation();

      i.addContext(ic);
      i.setType("bloopah");
      
      server.invoke(oname, "invoke",
            new Object[] { i },
            new String[] { Invocation.class.getName() }
      );
      
      assertTrue(i.getType().equals("something"));
   }
   
   public void testIsShared() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      
      MySharedInterceptor shared = new MySharedInterceptor();
      
      assertTrue(shared.isShared() == false);
      
      shared.register(server);
      
      assertTrue(shared.isShared() == true);
   }
   
   public void testLifecycleCallbacks() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      
      MySharedInterceptor shared = new MySharedInterceptor();
      
      assertTrue(shared.isInit == false);
      assertTrue(shared.isStart == false);
      
      ObjectName oname = shared.register(server);
      
      assertTrue(shared.isInit == true);
      assertTrue(shared.isStart == true);
      
      assertTrue(shared.isStop == false);
      assertTrue(shared.isDestroy == false);
      
      server.unregisterMBean(oname);
      
      assertTrue(shared.isStop == true);
      assertTrue(shared.isDestroy = true);
   }
   

   
   
   
}


