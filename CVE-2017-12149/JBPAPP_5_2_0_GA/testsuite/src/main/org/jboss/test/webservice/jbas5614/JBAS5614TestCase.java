/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.webservice.jbas5614;

import junit.framework.Test;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import org.jboss.test.webservice.WebserviceTestBase;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class JBAS5614TestCase extends WebserviceTestBase
{
   private static HelloWorld port;

   public JBAS5614TestCase(String name)
   {
      super(name);  
   }

   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return getDeploySetup(JBAS5614TestCase.class, "webservice-jbas5614.ear");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getClientContext("jbossws-client");
         Object lookup = iniCtx.lookup("java:comp/env/service/HelloWorldService");
         log.info("Lookup: " + lookup + " [" + lookup.getClass() + "]");
         Service service = (Service)lookup;
         port = (HelloWorld)service.getPort(HelloWorld.class);
      }
   }

   public void testCall() throws Exception
   {
      String response = port.echo("Hello");
      assertEquals("Hello", response);
   }
}
