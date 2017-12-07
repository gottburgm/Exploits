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
package org.jboss.test.webservice.admindevel;

import junit.framework.Test;
import org.jboss.test.webservice.WebserviceTestBase;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

/** A test for the examples from the JBoss Admin Devel book.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81036 $
 */
public class ExampleTestCase extends WebserviceTestBase
{
   private static Hello helloPort;

   public ExampleTestCase(String name)
   {
      super(name);
   }

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ExampleTestCase.class, "ws4ee-admindevel.jar, ws4ee-admindevel-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (helloPort == null)
      {
         InitialContext iniCtx = getClientContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloService");
         helloPort = (Hello)service.getPort(Hello.class);
      }
   }

   public void testHelloString() throws Exception
   {

      String retStr = helloPort.helloString("Kermit");
      assertEquals("Hello Kermit!", retStr);
   }

   public void testHelloBean() throws Exception
   {
      HelloObj ho = new HelloObj("Kermit");
      HelloObj hro = helloPort.helloBean(ho);
      assertEquals("Hello Kermit!", hro.getMsg());
   }

   public void testHelloArray() throws Exception
   {
      HelloObj[] query = new HelloObj[3];
      HelloObj ho = new HelloObj();
      ho.setMsg("Kermit");
      query[0] = ho;
      ho = new HelloObj();
      ho.setMsg("Piggy");
      query[1] = ho;
      ho = new HelloObj();
      ho.setMsg("Fozzy");
      query[2] = ho;

      HelloObj[] reply = helloPort.helloArray(query);
      for (int i = 0; i < reply.length; i++)
      {
         HelloObj replyObj = reply[i];
         assertEquals("Hello " + query[i].getMsg() + "!", replyObj.getMsg());
      }
   }
}
