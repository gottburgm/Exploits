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
package org.jboss.test.security.test;

import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;
import org.jboss.logging.Logger;

/** Test of the secure remote password(SRP) session key to perform crypto
operations.
 
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class SRPUnitTestCase extends JBossTestCase
{
   static final String JAR = "security-srp.jar";
   static String username = "scott";
   static char[] password = "echoman".toCharArray();

   LoginContext lc;
   boolean loggedIn;

   public SRPUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that the echo method is secured by the SRPCacheLogin module
    */
   public void testEchoArgs() throws Exception
   {
      log.debug("+++ testEchoArgs");
      login("srp-test", username, password);
      Object obj = getInitialContext().lookup("srp-jce.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created srp-jce.StatelessSession");
      try
      {
         log.debug("Bean.echo('Hello') -> "+bean.echo("Hello"));
      }
      catch(Exception e)
      {
         Throwable t = e;
         if( e instanceof UndeclaredThrowableException )
         {
            UndeclaredThrowableException ex = (UndeclaredThrowableException) e;
            t = ex.getUndeclaredThrowable();
         }
         else if( e instanceof RemoteException )
         {
            RemoteException ex = (RemoteException) e;
            t = ex.detail;
         }

         log.error("echo failed", t);
         boolean failure = true;
         if( t instanceof SecurityException )
         {
            String msg = t.getMessage();
            if( msg.startsWith("Unsupported keysize") )
            {
               /* The size of the srp session key is bigger than the JCE version
               in use supports. Most likely the unlimited strength policy is
               not installed so don't fail the test.
               */
               failure = false;
               log.info("Not failing test due to key size issue");
            }
         }

         if( failure )
            fail("Call to echo failed: "+t.getMessage());
      }

      logout();
   }

   /** Test that the echo method is secured by the SRPCacheLogin module when
    * using multi-session srp with two threads
    */
   public void testMultiUserEchoArgs() throws Exception
   {
      log.debug("+++ testMultiUserEchoArgs");
      UserThread ut0 = new UserThread(log);
      UserThread ut1 = new UserThread(log);

      Thread t0 = new Thread(ut0, "UserThread#0");
      t0.setDaemon(true);
      t0.start();
      Thread t1 = new Thread(ut1, "UserThread#1");
      t1.setDaemon(true);
      t1.start();

      // Release the ut0 thread and wait for it to finish the first ejb call
      synchronized( ut0 )
      {
         ut0.semaphore = true;
         ut0.notify();
         log.info("waiting on ut0 #1");
         ut0.wait(5000);
      }
      log.info("released ut0 #1");
      // Release the ut1 thread and wait for it to finish the first ejb call
      synchronized( ut1 )
      {
         ut1.semaphore = true;
         ut1.notify();
         log.info("waiting on ut1 #1");
         ut1.wait(5000);
      }
      log.info("released ut1 #1");
      assertTrue("UserThread0.ex == null", ut0.ex == null);
         
      // Release the ut1 thread and wait for it to finish the second ejb call
      synchronized( ut1 )
      {
         ut1.semaphore = true;
         ut1.notify();
         log.info("waiting on ut1 #2");
         ut1.wait(5000);
      }
      log.info("released ut1 #2");
      assertTrue("UserThread1.ex == null", ut1.ex == null);

      // Release the ut0 thread and wait for it to finish the second ejb call
      synchronized( ut0 )
      {
         ut0.semaphore = true;
         ut0.notify();
         log.info("waiting on ut0 #2");
         ut0.wait(5000);
      }
      log.info("released ut0 #2");

      t0.join();
      log.debug("UserThread0.ex", ut0.ex);
      t1.join();
      log.debug("UserThread1.ex", ut1.ex);
      assertTrue("UserThread0.ex == null", ut0.ex == null);
      assertTrue("UserThread1.ex == null", ut1.ex == null);
   }

   /** Login using the given confName login configuration with the provided
    username and password credential.
    */
   private void login(String confName, String username, char[] password)
      throws Exception
   {
      if( loggedIn )
         return;

      lc = null;
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
      loggedIn = true;
   }
   private void logout() throws Exception
   {
      if( loggedIn )
      {
         loggedIn = false;
         lc.logout();
      }
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SRPUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy(JAR);
            // Establish the JAAS login config
            String authConfPath = super.getResourceURL("security-srp/auth.conf");
            System.setProperty("java.security.auth.login.config", authConfPath);
         }
         protected void tearDown() throws Exception
         {
            undeploy(JAR);
            super.tearDown();
         }
      };
      return wrapper;
   }

   class UserThread implements Runnable
   {
      boolean semaphore;
      Throwable ex;
      Logger log;
      UserThread(Logger log)
      {
         this.log = log;
      }

      public synchronized boolean semaphore()
      {
         semaphore = true;
         return semaphore;
      }

      public void run()
      {
         try
         {
            internalTestEchoArgs();
         }
         catch(Throwable t)
         {
            this.ex = t;
            t.printStackTrace();
         }
      }
      private synchronized void internalTestEchoArgs()
         throws Exception
      {
         log.debug("+++ internalTestEchoArgs");
         AppCallbackHandler handler = new AppCallbackHandler(username, password);
         log.debug("Creating LoginContext(srp-test-multi)");
         LoginContext lc = new LoginContext("srp-test-multi", handler);
         lc.login();
         log.debug("Created LoginContext, subject="+lc.getSubject());

         Object obj = getInitialContext().lookup("srp.StatelessSession");
         obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
         StatelessSessionHome home = (StatelessSessionHome) obj;
         // Wait for the test thread to tell use to continue
         log.debug("Enter wait");
         while( semaphore == false )
         {
            log.info("waiting for notification");
            wait(1000);
         }
         semaphore = false;
         log.debug("Notified, Found StatelessSessionHome");
         StatelessSession bean = home.create();
         log.debug("Created srp.StatelessSession");
         log.debug("Bean.echo('Hello') -> "+bean.echo("Hello"));
         notifyAll();
         log.debug("Notified all, enter wait#2");
         while( semaphore == false )
         {
            log.info("waiting for notification");
            wait(1000);
         }
         log.debug("Notified, Bean.echo('Hello#2') -> "+bean.echo("Hello#2"));
         notifyAll();
         log.debug("Notified all, logging out");
         lc.logout();
         log.debug("Logout");
      }
   }
}
