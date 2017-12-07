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
package org.jboss.test.remoting.interceptor;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import org.apache.log4j.Level;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.marshal.serializable.SerializableUnMarshaller;
import org.jboss.remoting.network.NetworkRegistry;
import org.jboss.remoting.transport.Connector;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class ServerInterceptorTest
{
   private InvokerLocator locator;

   public void setLocator(InvokerLocator locator)
   {
      this.locator = locator;
   }

   protected void setup()
   {
      try
      {
         System.setProperty("jboss.identity", Identity.createUniqueID());
         MBeanServer server = MBeanServerFactory.createMBeanServer();

         System.out.println("my identity is: " + Identity.get(server));


         NetworkRegistry registry = NetworkRegistry.getInstance();
         server.registerMBean(registry, new ObjectName("remoting:type=NetworkRegistry"));

         //int port = PortUtil.findFreePort();

         Connector connector = new Connector();
         connector.setInvokerLocator(locator.getLocatorURI());
         ObjectName obj = new ObjectName("jboss.remoting:type=Connector,transport=" + locator.getProtocol());
         server.registerMBean(connector, obj);

         connector.start();

         connector.addInvocationHandler("test", new TestInvocationHandler());

//          MulticastDetector detector = new MulticastDetector();
//          server.registerMBean(detector, new ObjectName("remoting:type=Detector,transport=multicast"));
//          detector.start();

         // TODO: -TME Not needed unless want to make jmx invocation within handler
//         TestTarget target = new TestTarget();
//         ObjectName objName = new ObjectName("test:type=UnifiedInvoker");
//         server.registerMBean(target, objName);
//         Registry.bind("test:type=UnifiedInvoker", objName);

      }
      catch(Throwable e)
      {
         e.printStackTrace();
      }

   }

   public static void main(String[] args)
   {
      try
      {
         //org.apache.log4j.BasicConfigurator.configure();

         int port = 8081;
         String transport = "socket";
//         InvokerLocator locator = new InvokerLocator(transport + "://localhost:" + port + "/?" +
//               InvokerLocator.DATATYPE + "=" + SerializableUnMarshaller.DATATYPE);
         InvokerLocator locator = new InvokerLocator(transport + "://localhost:" + port + "/?" +
                                                     InvokerLocator.DATATYPE + "=" + SerializableUnMarshaller.DATATYPE);

         ServerInterceptorTest server = new ServerInterceptorTest();
         server.setLocator(locator);
         server.setup();

         while(true)
         {
            Thread.sleep(1000);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * When an object implementing interface <code>Runnable</code> is used
    * to create a thread, starting the thread causes the object's
    * <code>run</code> method to be called in that separately executing
    * thread.
    * <p/>
    * The general contract of the method <code>run</code> is that it may
    * take any action whatsoever.
    *
    * @see Thread#run()
    */
   public void run()
   {
      setup();
   }
}