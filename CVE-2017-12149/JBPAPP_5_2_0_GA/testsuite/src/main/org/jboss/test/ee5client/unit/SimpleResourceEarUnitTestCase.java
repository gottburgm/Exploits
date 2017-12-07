/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.test.ee5client.unit;

import java.net.URL;

import javax.naming.Context;
import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.JBossTestCase;
import org.jboss.test.ee5client.client.SimpleResourceClient;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class SimpleResourceEarUnitTestCase extends JBossTestCase
{

   public SimpleResourceEarUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test to see if the client deployer has setup enc env correctly.
    * 
    * @throws Exception
    */
   public void testJNDI() throws Exception
   {
      String clientName = "ee5client-simpleresource-client";
      Context ctx = getInitialContext();
      try
      {
         ctx = (Context) ctx.lookup(clientName);
      }
      catch(NameNotFoundException e)
      {
         fail(clientName + " not bound");
      }
      
      try
      {
         String value = (String) ctx.lookup("env/msg");
         assertEquals("Hello world", value);
      }
      catch(NameNotFoundException e)
      {
         fail("env/msg not bound");
      }
      
      // TODO: shouldn't org.jboss.ejb3.test.appclient.client.SimpleResourceClient/msg be bound?
      
//      NamingEnumeration<NameClassPair> e = ctx.list("env");
//      while(e.hasMore())
//      {
//         NameClassPair ncp = e.next();
//         System.out.println(ncp.getName());
//      }
//      try
//      {
//         
//      }
//      catch(NameNotFoundException e)
//      {
//         
//      }
   }
   
   public void testClientLauncher() throws Throwable
   {
      String mainClassName = SimpleResourceClient.class.getName();
      String applicationClientName = "ee5client-simpleresource-client"; // must match JNDI name in jboss-client.xml or display-name in application-client.xml
      String args[] = { };
      
      ClientLauncher launcher = new ClientLauncher();
      launcher.launch(mainClassName, applicationClientName, args);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(SimpleResourceEarUnitTestCase.class, "ee5client-simpleresource.ear");
   }

}
