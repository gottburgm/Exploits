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
package org.jboss.test.jbossmx.implementation.util;

import org.jboss.test.jbossmx.implementation.TestCase;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.test.jbossmx.implementation.util.support.Trivial;
import org.jboss.test.jbossmx.implementation.util.support.TrivialMBean;

import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.AgentID;


public class MBeanProxyTestCase
   extends TestCase
{
   public MBeanProxyTestCase(String s)
   {
      super(s);
   }

   public void testCreate()
   {
      try 
      {   
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName oname   = new ObjectName("test:name=test");
      
         server.registerMBean(new Trivial(), oname);
      
         TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
               TrivialMBean.class, oname, AgentID.get(server));      
      }
      catch (Throwable t)
      {
         log.debug("failed", t);
         fail("unexpected error: " + t.toString());
      }
   }

   public void testProxyInvocations()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName oname   = new ObjectName("test:name=test");
         
         server.registerMBean(new Trivial(), oname);
         
         TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
               TrivialMBean.class, oname, AgentID.get(server));
         
         mbean.doOperation();
         mbean.setSomething("JBossMX");
         
         assertEquals("JBossMX", mbean.getSomething());
      }
      catch (Throwable t)
      {
         log.debug("failed", t);
         fail("unexpected error: " + t.toString());
      }
   }
}
