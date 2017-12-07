/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.client.test.main;

import java.net.URL;

import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;

/**
 * A javaee application client
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class Client
{
   private static Logger log = Logger.getLogger(Client.class);

   static void assertTrue(String msg, boolean exp)
   {
      if(exp == false)
         throw new IllegalStateException(msg);
   }
   /** Test that the client java:comp/env context contains what is expected
    * @throws Exception
    */ 
   public static void testENC() throws Exception
   {
      log.info("+++ testENC");
      Context enc = getENC();

      String str0 = (String) enc.lookup("String0");
      assertTrue("String0 == String0Value", str0.equals("String0Value"));

      Float flt0 = (Float) enc.lookup("Float0");
      assertTrue("Float0 == 3.14", flt0.equals(new Float("3.14")));

      Long long0 = (Long) enc.lookup("Long0");
      assertTrue("Long0 == 123456789", long0.equals(new Long(123456789)));

      StatelessSessionHome home = (StatelessSessionHome) enc.lookup("ejb/StatelessSessionBean");
      assertTrue("ejb/StatelessSessionBean isa StatelessSessionHome", home != null);

      URL jbossHome = (URL) enc.lookup("url/JBossHome");
      assertTrue("url/JBossHome == http://www.jboss.org",
         jbossHome.toString().equals("http://www.jboss.org"));

      URL indirectURL = (URL) enc.lookup("url/IndirectURL");
      assertTrue("url/IndirectURL == http://www.somesite.com",
         indirectURL.toString().equals("http://www.somesite.com"));

      Queue testQueue = (Queue) enc.lookup("jms/aQueue");
      assertTrue("jms/aQueue isa Queue", testQueue != null);

      Queue anotherQueue = (Queue) enc.lookup("jms/anotherQueue");
      assertTrue("jms/anotherQueue isa Queue", anotherQueue != null);

      Queue anotherQueue2 = (Queue) enc.lookup("jms/anotherQueue2");
      assertTrue("jms/anotherQueue2 isa Queue", anotherQueue2 != null);
   }
   /** Test access to EJBs located through the java:comp/env context
    * @throws Exception
    */ 
   static void testEjbs() throws Exception
   {
      log.info("+++ testEjbs");
      Context enc = getENC();      
      StatelessSessionHome home = (StatelessSessionHome) enc.lookup("ejb/StatelessSessionBean");
      StatelessSession session = home.create();
      session.method1("testEjbs");
      session.remove();
   }

   static Context getENC()
      throws NamingException
   {
      Context ic = new InitialContext();
      log.debug("Client, ENC.env:"+ic.getEnvironment());
      Context enc = (Context) ic.lookup("java:comp/env");
      return enc;
   }
   public static void main(String[] args)
      throws Exception
   {
      testENC();
      testEjbs();
   }
}
