/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;

import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.ee5client.client.HelloWorldClient;
import org.jboss.test.jbossmessaging.ra.RaJMSSessionUnitTestCase;

/**
 * A basic EE5 application client test case
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
public class AppClientUnitTestCase extends JBossJMSTestCase
{
   public AppClientUnitTestCase(String name)
   {
      super(name);
   }

   public void test1() throws Throwable
   {
      String mainClassName = HelloWorldClient.class.getName();
      // must match JNDI name in jboss-client.xml or display-name in application-client.xml
      String name = new Date().toString();
      String applicationClientName = "ee5client_test";
      String args[] = { name };
      
      ClientLauncher launcher = new ClientLauncher();
      Properties env = getENCProps(applicationClientName);
      launcher.launch(mainClassName, applicationClientName, args, env);
      
      Class<?> clientClass = ClientLauncher.getTheMainClass();
      Class<?> empty[] = {};
      {
         Method getResult = clientClass.getDeclaredMethod("getResult", empty);
         String actual = (String) getResult.invoke(null, null);
         String expected = "Hi " + name + ", how are you?";
         assertEquals(expected, actual);
      }
      
      {
         Method getPostConstructCalls = clientClass.getDeclaredMethod("getPostConstructCalls", empty);
         int actual = (Integer) getPostConstructCalls.invoke(null, null);
         int expected = 1;
         assertEquals("postConstruct should be called once", expected, actual);
      }
   }
   private Properties getENCProps(String applicationClientName)
   {
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
      env.setProperty(Context.PROVIDER_URL, "jnp://" + getServerHost() + ":1099");
      env.setProperty("j2ee.clientName", applicationClientName);
      return env;
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new JBossTestSetup(new TestSuite(AppClientUnitTestCase.class))
      {
          protected void setUp() throws Exception
          {
             super.setUp();
             AppClientUnitTestCase.deployQueue("messageReplier");
             deploy ("ee5client-test.ear");
          }
          protected void tearDown() throws Exception
          {
             undeploy ("ee5client-test.ear");
             RaJMSSessionUnitTestCase.undeployDestinations();
          }
      });

      return suite;
   }
}
