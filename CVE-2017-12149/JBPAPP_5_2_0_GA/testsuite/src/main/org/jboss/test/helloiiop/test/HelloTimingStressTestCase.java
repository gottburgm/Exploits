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
package org.jboss.test.helloiiop.test;


import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import org.jboss.test.JBossIIOPTestCase;
import org.jboss.test.helloiiop.interfaces.Hello;
import org.jboss.test.helloiiop.interfaces.HelloData;
import org.jboss.test.helloiiop.interfaces.HelloHome;


/** Simple tests of the Hello stateless session bean
 *
 *   @author Scott.Stark@jboss.org
 *   @version $Revision: 81036 $
 */
public class HelloTimingStressTestCase
   extends JBossIIOPTestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public HelloTimingStressTestCase(String name)
   {
      super(name);
   }


   // Public --------------------------------------------------------

   /**
    *   Lookup the bean, call it, remove it.
    *
    * @exception   Exception
    */
   public void testHello()
      throws Exception
   {
      HelloHome home = (HelloHome)PortableRemoteObject.narrow(
                               getInitialContext().lookup(HelloHome.JNDI_NAME),
                               HelloHome.class);
      Hello hello = home.create();
      getLog().debug(hello.hello("World"));
      hello.remove();
   }

   /**
    *   Test marshalling of custom data-holders.
    *
    * @exception   Exception
    */
   public void testData()
      throws Exception
   {
      HelloHome home = (HelloHome)PortableRemoteObject.narrow(
                               getInitialContext().lookup(HelloHome.JNDI_NAME),
                               HelloHome.class);
      Hello hello = home.create();
      HelloData name = new HelloData();
      name.setName("World");
      getLog().debug(hello.howdy(name));
      hello.remove();
   }

   /**
    *   This tests the speed of invocations
    *
    * @exception   Exception
    */
   public void testSpeed()
      throws Exception
   {
      long start = System.currentTimeMillis();
      HelloHome home = (HelloHome)PortableRemoteObject.narrow(
                               getInitialContext().lookup(HelloHome.JNDI_NAME),
                               HelloHome.class);
      Hello hello = home.create();
      for (int i = 0 ; i < getIterationCount(); i++)
      {
         hello.hello("Rickard");
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
   }

   /**
    *   This tests the speed of invocations
    *
    * @exception   Exception
    */
   public void testSpeed2()
      throws Exception
   {
      long start = System.currentTimeMillis();
      long start2 = start;
      HelloHome home = (HelloHome)PortableRemoteObject.narrow(
                               getInitialContext().lookup(HelloHome.JNDI_NAME),
                               HelloHome.class);
      Hello hello = home.create();
      for (int i = 0 ; i < getIterationCount(); i++)
      {
         hello.helloHello(hello);
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
   }

   /**
    *   This tests the speed of InitialContext lookups
    * including getting the initial context.
    * @exception   Exception
    */
   public void testContextSpeed()
      throws Exception
   {
      long start = System.currentTimeMillis();

      getLog().debug("Starting context lookup speed test");
      for (int i = 0; i < getIterationCount(); i++)
      {
         HelloHome home = (HelloHome)PortableRemoteObject.narrow(
                              getInitialContext().lookup(HelloHome.JNDI_NAME),
                              HelloHome.class);
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
   }

   /**
    *   This tests the speed of JNDI lookups
    *
    * @exception   Exception
    */
   public void testReusedContextSpeed()
      throws Exception
   {
      Context ctx = getInitialContext();
      long start = System.currentTimeMillis();

      getLog().debug("Starting context lookup speed test");
      for (int i = 0; i < getIterationCount(); i++)
      {
         HelloHome home = (HelloHome)PortableRemoteObject.narrow(
                                  ctx.lookup(HelloHome.JNDI_NAME),
                                  HelloHome.class);
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(HelloTimingStressTestCase.class, "helloiiop.jar");
   }

}
