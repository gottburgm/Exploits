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
package org.jboss.test.jmx.test;


import java.lang.reflect.Proxy;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.*;

import org.jboss.test.JBossTestCase;
import org.jboss.system.server.jmx.JVMShutdown;
import org.jboss.system.server.jmx.JVMShutdownMBean;
import org.jboss.mx.util.ObjectNameFactory;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/** Tests of JVMShutdown
 *
 * @author <a href="mailto:csams@redhat.com">Chris Sams </a>
 */
public class JVMShutdownShutdownTestCase
   extends JBossTestCase
{
	
   private final ObjectName serverJMXName = ObjectNameFactory.create("jboss.system:type=JVMShutdown");
   private final String adapterName = "jmx/rmi/RMIAdaptor";
   public JVMShutdownShutdownTestCase(String name)
   {
      super(name);
		SecurityAssociation.setPrincipal(new SimplePrincipal("admin"));
		SecurityAssociation.setCredential("admin");
   }

	public void testShutdown() throws Exception
	{
      JVMShutdownMBean server = getJVMShutdown();
		server.shutdown();

      assertTrue("test:name=JVMShutdownShutdown", isShutDown());
      Thread.sleep(5000);
	}

   protected boolean isShutDown() throws Exception
   {
      //keep looking up until it fails.. we take this to mean the server shut down
      InitialContext ic = new InitialContext();
      boolean hasShutDown = false;
      int i = 0;
      while(i < 10 && !hasShutDown)
      {
         try
         {
            Object obj = ic.lookup(adapterName);
            Thread.sleep(1000);
         }catch(Exception e)
         {
            hasShutDown = true;
         }
         i++;
      }
      return hasShutDown;
   }

   protected JVMShutdownMBean getJVMShutdown() throws Exception
   {
      InitialContext ic = new InitialContext();
      MBeanServerConnection adaptor = (MBeanServerConnection)ic.lookup(adapterName);
      ServerProxyHandler handler = new ServerProxyHandler(adaptor, serverJMXName);
      Class<?>[] ifaces = {JVMShutdownMBean.class};
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      JVMShutdownMBean server = (JVMShutdownMBean) Proxy.newProxyInstance(tcl, ifaces, handler);
      return server;
	}

}
