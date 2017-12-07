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
package org.jboss.test.client.test;

import java.util.Properties;
import java.net.URL;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.Queue;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.client.test.main.Client;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests of accessing a j2ee application client deployment
 * 
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 105321 $
 */
public class AppClientUnitTestCase extends JBossTestCase
{
   public AppClientUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Validate the ClientLauncher for the test-client deployment/main class
    * @throws Throwable
    */
   public void testClientLauncher()
      throws Throwable
   {
      String mainClassName = Client.class.getName();
      // must match JNDI name in jboss-client.xml or display-name in application-client.xml
      String applicationClientName = "test-client";
      String[] args = {};
      
      ClientLauncher launcher = new ClientLauncher();
      Properties env = getENCProps(applicationClientName);
      launcher.launch(mainClassName, applicationClientName, args, env);
   }

   /** Test that the client java:comp/env context contains what is expected
    * @throws Exception
    */ 
   public void testENC() throws Exception
   {
      Context enc = getENC();
      log.debug("testENC, ENC.env:"+enc.getEnvironment());

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
   public void testEjbs() throws Exception
   {
      Context enc = getENC();      
      StatelessSessionHome home = (StatelessSessionHome) enc.lookup("ejb/StatelessSessionBean");
      StatelessSession session = home.create();
      session.method1("testEjbs");
      session.remove();
   }

   /** Build the InitialContext factory 
    * @return
    * @throws NamingException
    */ 
   private Context getENC() throws NamingException
   {
      Properties env = getENCProps("test-client");
      InitialContext ctx = new InitialContext(env);
      Context enc = (Context) ctx.lookup("java:comp/env");
      return enc;
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
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy ("app-client.ear");
         }
         protected void tearDown() throws Exception
         {
            super.tearDown();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            undeploy ("app-client.ear");
            JMSDestinationsUtil.destroyDestinations();
         }
      });

      return suite;
   }
}
