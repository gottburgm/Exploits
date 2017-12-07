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

import java.security.Principal;

import javax.ejb.EJBAccessException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.ejb3.RunAsSession;
import org.jboss.test.security.ejb3.SimpleSession;
import org.jboss.test.util.AppCallbackHandler;

/**
 * <p>
 * This {@code TestCase} validates the security behavior of protected EJB3 beans.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class EJB3SpecUnitTestCase extends JBossTestCase
{

   private LoginContext loginContext;

   /**
    * <p>
    * Creates an instance of {@code EJB3SpecUnitTestCase} with the specified name.
    * </p>
    * 
    * @param name a {@code String} that represents the name of the test case.
    */
   public EJB3SpecUnitTestCase(String name)
   {
      super(name);
      // set the login config file if it hasn't been set yet.
      if (System.getProperty("java.security.auth.login.config") == null)
         System.setProperty("java.security.auth.login.config", "output/resources/security/auth.conf");
   }

   /**
    * <p>
    * Tests accessing protected methods using a client that has the {@code Administrator} role.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testAdministratorMethodAccess() throws Exception
   {
      // login with a user that has the Administrator role.
      this.login("UserA", "PassA".toCharArray());

      // get a reference to the remote protected stateless session bean.
      Object obj = getInitialContext().lookup("SimpleStatelessSessionBean/remote");
      SimpleSession session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      // an administrator should have access to all methods but invokeUnavailableMethod.
      this.assertMethodAccessResults(session, true, true);

      // repeat the tests, this time calling a stateful session bean.
      obj = getInitialContext().lookup("SimpleStatefulSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);
      this.assertMethodAccessResults(session, true, true);

      this.logout();
   }

   /**
    * <p>
    * Tests accessing protected methods using a client that has the {@code RegularUser} role.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testRegularUserMethodAccess() throws Exception
   {
      // login with a user that has the RegularUser role.
      this.login("UserB", "PassB".toCharArray());

      // get a reference to the remote protected stateless session bean.
      Object obj = getInitialContext().lookup("SimpleStatelessSessionBean/remote");
      SimpleSession session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      // a regular user cannot access administrative methods.
      this.assertMethodAccessResults(session, true, false);

      // repeat the tests, this time calling a stateful session bean.
      obj = getInitialContext().lookup("SimpleStatefulSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);
      this.assertMethodAccessResults(session, true, false);

      this.logout();
   }

   /**
    * <p>
    * Tests accessing protected methods using a client that has the {@code Guest} role.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testGuestMethodAccess() throws Exception
   {
      // login with a user that has the Guest role.
      this.login("UserC", "PassC".toCharArray());

      // get a reference to the remote protected stateless session bean.
      Object obj = getInitialContext().lookup("SimpleStatelessSessionBean/remote");
      SimpleSession session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      // a guest user should have access to unprotected methods only.
      this.assertMethodAccessResults(session, false, false);

      // repeat the tests, this time calling a stateful session bean.
      obj = getInitialContext().lookup("SimpleStatefulSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);
      this.assertMethodAccessResults(session, false, false);

      this.logout();
   }

   /**
    * <p>
    * Tests accessing protected methods using an unauthenticated client.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testUnauthenticatedMethodAccess() throws Exception
   {
      // get a reference to the remote protected stateless session bean.
      Object obj = getInitialContext().lookup("SimpleStatelessSessionBean/remote");
      SimpleSession session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      // an unauthenticated user should have access to unprotected methods only.
      this.assertMethodAccessResults(session, false, false);

      // repeat the tests, this time calling a stateful session bean.
      obj = getInitialContext().lookup("SimpleStatefulSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);
      this.assertMethodAccessResults(session, false, false);
   }

   /**
    * <p>
    * Tests the following scenario:
    * <ol>
    * <li>a client associated with a {@code RegularUser} role invokes the {@code RunAsSession}.</li>
    * <li>the {@code RunAsSession#invokeRunAs()} method delegates the call to the {@code DelegateSession} using a
    * {@code @RunAs("Manager")} annotation.</li>
    * </ol>
    * 
    * {@code DelegateSession#invokeDelegate()} requires a role {@code Manager} to run. As the client doesn't have the
    * required role, the call will only succeed if the {@code RunAsSession} propagates an identity with the
    * {@code Manager} role using a {@code @RunAs} annotation.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testRunAsMethodAccess() throws Exception
   {
      // login with a user that has the RegularUser role.
      this.login("UserB", "PassB".toCharArray());

      // get a reference to the remote run-as session.
      Object obj = getInitialContext().lookup("RunAsSessionBean/remote");
      RunAsSession session = (RunAsSession) PortableRemoteObject.narrow(obj, RunAsSession.class);

      // invoke the session, that delegates the invocation to the delegate session using @RunAs.
      Principal principal = session.invokeRunAs();
      assertNotNull("Found unexpected null principal", principal);
      // run-as identity should be the default unauthenticated identity configured in the login module.
      assertEquals("anonymous", principal.getName());

      this.logout();
   }

   
   public void testDeclareRoles() throws Exception
   {
      // login with a user that has the RegularUser role.
      this.login("UserA", "PassA".toCharArray());

      // get a reference to the remote protected stateless session bean.
      Object obj = getInitialContext().lookup("SimpleStatelessSessionBean/remote");
      SimpleSession session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      if (!session.checkDeclaredRoles("Administrator", "RegularUser", "!negativeRole"))
    	  fail("UserA has to have both roles (Administrator, RegularUser, !negativeRole) for SimpleStatelessSessionBean");
      
      // repeat the tests, this time calling a stateful session bean.
      obj = getInitialContext().lookup("SimpleStatefulSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      if (!session.checkDeclaredRoles("Administrator", "RegularUser", "!negativeRole"))
    	  fail("UserA has to have both roles (Administrator, RegularUser, !negativeRole) for SimpleStatelessSessionBean");
      
      this.logout();

      // login with a user that has the RegularUser role.
      this.login("UserB", "PassB".toCharArray());

      // get a reference to the remote protected stateless session bean.
      obj = getInitialContext().lookup("SimpleStatelessSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      if (!session.checkDeclaredRoles("!Administrator", "RegularUser", "!negativeRole"))
    	  fail("UserB has to have both roles (!Administrator, RegularUser, !negativeRole) for SimpleStatelessSessionBean");
      
      // repeat the tests, this time calling a stateful session bean.
      obj = getInitialContext().lookup("SimpleStatefulSessionBean/remote");
      session = (SimpleSession) PortableRemoteObject.narrow(obj, SimpleSession.class);

      if (!session.checkDeclaredRoles("!Administrator", "RegularUser", "!negativeRole"))
    	  fail("UserB has to have both roles (Administrator, RegularUser, !negativeRole) for SimpleStatelessSessionBean");
      
      this.logout();
   
      
   }
   
   
   /**
    * <p>
    * Validates the results received when calling all methods on a {@code SimpleSession}.
    * </p>
    * 
    * @param session the {@code SimpleSession} to be called.
    * @param succeedRegular {@code true} if the call to {@code SimpleSession#invokeRegularMethod()} must succeed;
    *            {@code false} otherwise.
    * @param succeedAdministrative {@code true} if the call to {@code SimpleSession#invokeAdministrativeMethod()} must
    *            succeed; {@code false} otherwise.
    */
   private void assertMethodAccessResults(SimpleSession session, boolean succeedRegular, boolean succeedAdministrative)
   {
      // access to unprotected (@PermitAll) methods should always be granted.
      Principal principal = session.invokeUnprotectedMethod();
      assertNotNull("Found unexpected null principal", principal);

      // access to unavailable (@DenyAll) method should always fail.
      try
      {
         principal = session.invokeUnavailableMethod();
         fail("Client should not be able to invoke a method annotated with @DenyAll");
      }
      catch (EJBAccessException ex)
      {
         super.log.debug("Got expected exception: ", ex);
      }

      // check access to regular method.
      if (succeedRegular)
      {
         principal = session.invokeRegularMethod();
         assertNotNull("Found unexpected null principal", principal);
      }
      else
      {
         try
         {
            principal = session.invokeRegularMethod();
            fail("Client should not be able to invoke a regular method");
         }
         catch (EJBAccessException ex)
         {
            super.log.debug("Got expected exception: ", ex);
         }
      }

      // check access to administrative method.
      if (succeedAdministrative)
      {
         principal = session.invokeAdministrativeMethod();
         assertNotNull("Found unexpected null principal", principal);
      }
      else
      {
         try
         {
            principal = session.invokeAdministrativeMethod();
            fail("Client should not be able to invoke an administrative method");
         }
         catch (EJBAccessException ex)
         {
            super.log.debug("Got expected exception: ", ex);
         }
      }
   }

   /**
    * <p>
    * Authenticates the client identified by the given {@code username} using the specified {@code password}.
    * </p>
    * 
    * @param username a {@code String} that identifies the client that is being logged in.
    * @param password a {@code char[]} that contains the password that asserts the client's identity.
    * @throws LoginException if an error occurs while authenticating the client.
    */
   private void login(String username, char[] password) throws LoginException
   {
      // get the conf name from a system property - default is spec-test.
      String confName = System.getProperty("conf.name", "spec-test");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      this.loginContext = new LoginContext(confName, handler);
      this.loginContext.login();
   }

   /**
    * <p>
    * Perform a logout of the current user.
    * </p>
    * 
    * @throws LoginException if an error occurs while logging the user out.
    */
   private void logout() throws LoginException
   {
      this.loginContext.logout();
   }

   /**
    * <p>
    * Sets up the test suite.
    * </p>
    * 
    * @return a {@code TestSuite} that contains this test case.
    * @throws Exception if an error occurs while setting up the {@code TestSuite}.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(EJB3SpecUnitTestCase.class));

      TestSetup wrapper = new JBossTestSetup(suite)
      {
         /*
          * (non-Javadoc)
          * 
          * @see org.jboss.test.JBossTestSetup#setUp()
          */
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            // deploy the ejb3 test application.
            super.deploy("security-ejb3.jar");
         }

         /*
          * (non-Javadoc)
          * 
          * @see org.jboss.test.JBossTestSetup#tearDown()
          */
         @Override
         protected void tearDown() throws Exception
         {
            // undeploy the ejb3 test application.
            super.undeploy("security-ejb3.jar");
            // flush the authentication cache of the test domain.
            super.flushAuthCache("security-ejb3-test");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
