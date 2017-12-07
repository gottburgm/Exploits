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
package org.jboss.test.cts.test;


import java.rmi.server.UnicastRemoteObject;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.ejb.Handle;
import javax.ejb.CreateException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.cts.interfaces.StrictlyPooledSessionHome;
import org.jboss.test.cts.interfaces.StrictlyPooledSession;
import org.jboss.test.util.jms.JMSDestinationsUtil;

import EDU.oswego.cs.dl.util.concurrent.CountDown;

/** Basic conformance tests for stateless sessions
 *
 *  @author kimptoc
 *  @author d_jencks converted to JBossTestCase and logging.
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 105321 $
 */
public class StatelessSessionUnitTestCase
      extends JBossTestCase
{
   static final int MAX_SIZE = 20;
   StatelessSession sessionBean;

   public StatelessSessionUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      InitialContext ctx = new InitialContext();
      Object ref = ctx.lookup("ejbcts/StatelessSessionHome");
      StatelessSessionHome home = (StatelessSessionHome)
            PortableRemoteObject.narrow(ref, StatelessSessionHome.class);
      sessionBean = home.create();
   }

   protected void tearDown() throws Exception
   {
      if (sessionBean != null)
         sessionBean.remove();
   }

   /**
    * Method testBasicStatelessSession
    * @throws Exception
    */
   public void testBasicStatelessSession()
         throws Exception
   {
      getLog().debug("+++ testBasicStatelessSession()");
      String result = sessionBean.method1("testBasicStatelessSession");
      // Test response
      assertTrue(result.equals("testBasicStatelessSession"));
   }

   /** Test of handle
    * @throws Exception
    */
   public void testSessionHandle()
         throws Exception
   {
      getLog().debug("+++ testSessionHandle()");
      Handle beanHandle = sessionBean.getHandle();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(beanHandle);
      oos.flush();
      byte[] bytes = out.toByteArray();

      getLog().debug("Unserialize bean handle...");
      ByteArrayInputStream in = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(in);
      beanHandle = (Handle) ois.readObject();
      StatelessSession theSession = (StatelessSession) beanHandle.getEJBObject();
      theSession.method1("Hello");
      getLog().debug("Called method1 on handle session bean");
   }

   /** Test of handle that is unmarshalled in a environment where
    * new InitialContext() will not work. This must use the
    * @throws Exception
    */
   public void testSessionHandleNoDefaultJNDI()
         throws Exception
   {
      getLog().debug("+++ testSessionHandleNoDefaultJNDI()");

      /* We have to establish the JNDI env by creating a InitialContext with
      the org.jboss.naming.NamingContextFactory. Normally this would be done
      during the home lookup and session creation.
      */
      Properties homeProps = new Properties();
      homeProps.setProperty("java.naming.factory.initial", "org.jboss.naming.NamingContextFactory");
      InitialContext ic = new InitialContext(homeProps);
      Handle beanHandle = sessionBean.getHandle();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(beanHandle);
      oos.flush();
      byte[] bytes = out.toByteArray();

      Properties sysProps = System.getProperties();
      Properties newProps = new Properties(sysProps);
      newProps.setProperty("java.naming.factory.initial", "badFactory");
      newProps.setProperty("java.naming.provider.url", "jnp://badhost:12345");
      System.setProperties(newProps);
      try
      {
         getLog().debug("Unserialize bean handle...");
         ByteArrayInputStream in = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(in);
         beanHandle = (Handle) ois.readObject();
         StatelessSession theSession = (StatelessSession) beanHandle.getEJBObject();
         theSession.method1("Hello");
         getLog().debug("Called method1 on handle session bean");
      }
      finally
      {
         System.setProperties(sysProps);
      }
   }

   /** Test of accessing the home interface from the remote interface in an env
    * new InitialContext() will not work.
    * @throws Exception
    */
   public void testHomeFromRemoteNoDefaultJNDI()
         throws Exception
   {
      getLog().debug("+++ testHomeFromRemoteNoDefaultJNDI()");

      // Override the JNDI variables in the System properties
      Properties sysProps = System.getProperties();
      Properties newProps = new Properties(sysProps);
      newProps.setProperty("java.naming.factory.initial", "badFactory");
      newProps.setProperty("java.naming.provider.url", "jnp://badhost:12345");
      System.setProperties(newProps);

      // Do a lookup of the home and create a remote using a custom env
      Properties env = new Properties();
      env.setProperty("java.naming.factory.initial", super.getJndiInitFactory());
      env.setProperty("java.naming.provider.url", super.getJndiURL());
      try
      {
         InitialContext ctx = new InitialContext(env);
         Object ref = ctx.lookup("ejbcts/StatelessSessionHome");
         StatelessSessionHome home = (StatelessSessionHome)
               PortableRemoteObject.narrow(ref, StatelessSessionHome.class);
         sessionBean = home.create();
         StatelessSessionHome home2 = (StatelessSessionHome) sessionBean.getEJBHome();
         StatelessSession bean2 = home2.create();
         bean2.remove();
      }
      finally
      {
         System.setProperties(sysProps);
      }
   }

   public void testLocalStatelessSession()
         throws Exception
   {
      getLog().debug("+++ testLocalStatelessSession()");
      sessionBean.testLocalHome();
   }

   public void testClientCallback()
         throws Exception
   {
      getLog().debug("+++ testClientCallback()");
      ClientCallbackImpl callback = new ClientCallbackImpl();
      UnicastRemoteObject.exportObject(callback);
      sessionBean.callbackTest(callback, "testClientCallback");
      // Test callback data
      assertTrue(callback.wasCalled());
      UnicastRemoteObject.unexportObject(callback, true);
   }

   public void testStrictPooling() throws Exception
   {
      CountDown done = new CountDown(MAX_SIZE);
      InitialContext ctx = new InitialContext();
      StrictlyPooledSessionHome home = (StrictlyPooledSessionHome)
            ctx.lookup("ejbcts/StrictlyPooledStatelessBean");
      SessionInvoker[] threads = new SessionInvoker[MAX_SIZE];
      for (int n = 0; n < MAX_SIZE; n++)
      {
         SessionInvoker t = new SessionInvoker(home, n, done, getLog());
         threads[n] = t;
         t.start();
      }
      assertTrue("Acquired done", done.attempt(1500 * MAX_SIZE));

      for (int n = 0; n < MAX_SIZE; n++)
      {
         SessionInvoker t = threads[n];
         if (t.runEx != null)
         {
            t.runEx.printStackTrace();
            fail("SessionInvoker.runEx != null");
         }
      }
   }

   public void testStrictPoolingException() throws Exception
   {
      log.info("+++ testStrictPoolingException");
      InitialContext ctx = new InitialContext();
      StrictlyPooledSessionHome home = (StrictlyPooledSessionHome)
            ctx.lookup("ejbcts/StrictlyPooledCreateExceptionBean");

      try
      {
         StrictlyPooledSession bean = home.create();
         fail("Create did not fail, bean="+bean);
      }
      catch(CreateException e)
      {
         log.debug("Saw expected create failure", e);
      }

      for (int n = 0; n < MAX_SIZE; n++)
      {
         StrictlyPooledSession bean = home.create();
         bean.methodA();
         bean.remove();
      }
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(StatelessSessionUnitTestCase.class))
      {
         public void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy("cts.jar");
            
         }
         
         public void tearDown() throws Exception
         {
            undeploy("cts.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      };
      
      
   }

}
