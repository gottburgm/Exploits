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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.AccessException;
import java.rmi.ServerException;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.Handle;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.CalledSession;
import org.jboss.test.security.interfaces.CalledSessionHome;
import org.jboss.test.security.interfaces.StatefulSession;
import org.jboss.test.security.interfaces.StatefulSessionHome;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;
import org.jboss.test.security.interfaces.SecurityContext;
import org.jboss.test.security.interfaces.SecurityContextHome;
import org.jboss.test.security.ejb.jbas1852.SessionFacade;
import org.jboss.test.security.ejb.jbas1852.SessionFacadeHome;
import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test of EJB spec conformace using the security-spec.jar deployment unit. These test the basic role based access
 * model.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 108515 $
 */
public class EJBSpecUnitTestCase extends JBossTestCase
{
   static String username = "scott";

   static char[] password = "echoman".toCharArray();

   static String QUEUE_FACTORY = "ConnectionFactory";

   LoginContext lc;

   boolean loggedIn;

   public EJBSpecUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Validate that the users have the expected logins and roles.
    * 
    * @throws Exception
    */
   public void testSecurityDomain() throws Exception
   {
      log.info("+++ testSecurityDomain, domain=spec-test");
      MBeanServerConnection conn = getServer();
      ObjectName secMgrName = new ObjectName("jboss.security:service=JaasSecurityManager");
      JaasSecurityManagerServiceMBean secMgr = (JaasSecurityManagerServiceMBean) MBeanServerInvocationHandler
            .newProxyInstance(conn, secMgrName, JaasSecurityManagerServiceMBean.class, false);

      // Test the spec-test security domain
      String domain = "spec-test";
      SimplePrincipal user = new SimplePrincipal("scott");
      boolean isValid = secMgr.isValid(domain, user, password);
      assertTrue("scott password is echoman", isValid);
      HashSet testRole = new HashSet();
      testRole.add(new SimplePrincipal("Echo"));
      boolean hasRole;
      hasRole = secMgr.doesUserHaveRole(domain, user, password, testRole);
      assertTrue("scott has Echo role", hasRole);
      testRole.clear();
      testRole.add(new SimplePrincipal("EchoLocal"));
      hasRole = secMgr.doesUserHaveRole(domain, user, password, testRole);
      assertTrue("scott has EchoLocal role", hasRole);
      testRole.clear();
      testRole.add(new SimplePrincipal("ProjectUser"));
      hasRole = secMgr.doesUserHaveRole(domain, user, password, testRole);
      assertTrue("scott has ProjectUser role", hasRole);

      isValid = secMgr.isValid(domain, user, "badpass".toCharArray());
      assertTrue("badpass is an invalid password for scott", isValid == false);
      // Test the spec-test-domain security domain
      log.info("+++ testSecurityDomain, domain=spec-test-domain");
      domain = "spec-test-domain";
      isValid = secMgr.isValid(domain, user, password);
      assertTrue("scott password is echoman", isValid);
      hasRole = secMgr.doesUserHaveRole(domain, user, password, testRole);
      assertTrue("scott has Echo role", hasRole);
      testRole.clear();
      SimplePrincipal echoLocal = new SimplePrincipal("EchoLocal");
      testRole.add(echoLocal);
      hasRole = secMgr.doesUserHaveRole(domain, user, password, testRole);
      assertTrue("scott has EchoLocal role", hasRole);
      testRole.clear();
      SimplePrincipal projectUser = new SimplePrincipal("ProjectUser");
      testRole.add(projectUser);
      hasRole = secMgr.doesUserHaveRole(domain, user, password, testRole);
      assertTrue("scott has ProjectUser role", hasRole);
      Set roles = secMgr.getUserRoles(domain, user, password);
      assertTrue(roles != null);
      assertTrue("roles contains EchoLocal", roles.contains(echoLocal));
      assertTrue("roles contains ProjectUser", roles.contains(projectUser));

      isValid = secMgr.isValid(domain, user, "badpass".toCharArray());
      assertTrue("badpass is an invalid password for scott", isValid == false);
      
   }

   /**
    * Test the use of getCallerPrincipal from within the ejbCreate in a stateful session bean
    */
   public void testStatefulCreateCaller() throws Exception
   {
      log.debug("+++ testStatefulCreateCaller");
      login();
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.StatefulSession");
      obj = PortableRemoteObject.narrow(obj, StatefulSessionHome.class);
      StatefulSessionHome home = (StatefulSessionHome) obj;
      log.debug("Found StatefulSessionHome");
      // The create should be allowed to call getCallerPrincipal
      StatefulSession bean = home.create("testStatefulCreateCaller");
      // Need to invoke a method to ensure an ejbCreate call
      bean.echo("testStatefulCreateCaller");
      log.debug("Bean.echo(), ok");

      logout();
   }

   /**
    * Test that: 1. SecureBean returns a non-null principal when getCallerPrincipal is called with a security context
    * and that this is propagated to its Entity bean ref.
    * 
    * 2. UnsecureBean throws an IllegalStateException when getCallerPrincipal is called without a security context.
    */
   public void testGetCallerPrincipal() throws Exception
   {
      logout();
      log.debug("+++ testGetCallerPrincipal()");
      Object obj = getInitialContext().lookup("spec.UnsecureStatelessSession2");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found Unsecure StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.UnsecureStatelessSession2");

      try
      {
         // This should fail because echo calls getCallerPrincipal()
         bean.echo("Hello from nobody?");
         fail("Was able to call StatelessSession.echo");
      }
      catch (RemoteException e)
      {
         log.debug("echo failed as expected");
      }
      bean.remove();

      login();
      obj = getInitialContext().lookup("spec.StatelessSession2");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      home = (StatelessSessionHome) obj;
      log.debug("Found spec.StatelessSession2");
      bean = home.create();
      log.debug("Created spec.StatelessSession2");
      // Test that the Entity bean sees username as its principal
      String echo = bean.echo(username);
      log.debug("bean.echo(username) = " + echo);
      assertTrue("username == echo", echo.equals(username));
      bean.remove();
   }

   /**
    * Test that a call interacting with different security domains does not change the
    * 
    * @throws Exception
    */
   public void testDomainInteraction() throws Exception
   {
      logout();
      login("testDomainInteraction", "testDomainInteraction".toCharArray());
      log.debug("+++ testDomainInteraction()");
      Object obj = getInitialContext().lookup("spec.UserInRoleContextSession");
      obj = PortableRemoteObject.narrow(obj, SecurityContextHome.class);
      SecurityContextHome home = (SecurityContextHome) obj;
      log.debug("Found UserInRoleContextSession");
      SecurityContext bean = home.create();
      log.debug("Created spec.UserInRoleContextSession");
      HashSet roles = new HashSet();
      roles.add("Role1");
      roles.add("Role2");
      bean.testDomainInteraction(roles);
      bean.remove();
   }

   /**
    * Test that the calling principal is propagated across bean calls.
    */
   public void testPrincipalPropagation() throws Exception
   {
      log.debug("+++ testPrincipalPropagation");
      logout();
      login();
      Object obj = getInitialContext().lookup("spec.UnsecureStatelessSession2");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found Unsecure StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.UnsecureStatelessSession2");
      log.debug("Bean.forward('testPrincipalPropagation') -> " + bean.forward("testPrincipalPropagation"));
      bean.remove();
   }

   /**
    * Test that the echo method is accessible by an Echo role. Since the noop() method of the StatelessSession bean was
    * not assigned any permissions it should be unchecked.
    */
   public void testMethodAccess() throws Exception
   {
      log.debug("+++ testMethodAccess");
      login();
      Object obj = getInitialContext().lookup("spec.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession");
      log.debug("Bean.echo('Hello') -> " + bean.echo("Hello"));

      try
      {
         // This should not be allowed
         bean.noop();
         fail("Was able to call StatelessSession.noop");
      }
      catch (RemoteException e)
      {
         log.debug("StatelessSession.noop failed as expected");
      }
      bean.remove();
   }

   /**
    * Test that the echo method is accessible by an Echo role. Since the excluded() method of the StatelessSession bean
    * has been placed into the excluded set it should not accessible by any user. This uses the security domain of the
    * JaasSecurityDomain service to test its use as an authentication mgr.
    */
   public void testDomainMethodAccess() throws Exception
   {
      log.debug("+++ testDomainMethodAccess");
      login();
      Object obj = getInitialContext().lookup("spec.StatelessSessionInDomain");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionInDomain home");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSessionInDomain");
      log.debug("Bean.echo('testDomainMethodAccess') -> " + bean.echo("testDomainMethodAccess"));

      try
      {
         // This should not be allowed
         bean.excluded();
         fail("Was able to call StatelessSession.excluded");
      }
      catch (RemoteException e)
      {
         log.debug("StatelessSession.excluded failed as expected");
      }
      bean.remove();
   }

   /**
    * Test that the permissions assigned to the stateless session bean: with
    * ejb-name=org/jboss/test/security/ejb/StatelessSession_test are read correctly.
    */
   public void testMethodAccess2() throws Exception
   {
      log.debug("+++ testMethodAccess2");
      login();
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.StatelessSession_test");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession_test");
      log.debug("Bean.echo('testMethodAccess2') -> " + bean.echo("testMethodAccess2"));
      bean.remove();
   }

   /**
    * Test a user with Echo and EchoLocal roles can access the CalleeBean through its local interface by calling the
    * CallerBean and that a user with only a EchoLocal cannot call the CallerBean.
    */
   public void testLocalMethodAccess() throws Exception
   {
      log.debug("+++ testLocalMethodAccess");
      login();
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.CallerBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found spec.CallerBean Home");
      CalledSession bean = home.create();
      log.debug("Created spec.CallerBean");
      log.debug("Bean.invokeEcho('testLocalMethodAccess') -> " + bean.invokeEcho("testLocalMethodAccess"));
      bean.remove();
   }

   /**
    * Test access to a bean with a mix of remote interface permissions and unchecked permissions with the unchecked
    * permissions declared first.
    * 
    * @throws Exception
    */
   public void testUncheckedRemote() throws Exception
   {
      log.debug("+++ testUncheckedRemote");
      login();
      Object obj = getInitialContext().lookup("spec.UncheckedSessionRemoteLast");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found UncheckedSessionRemoteLast");
      StatelessSession bean = home.create();
      log.debug("Created spec.UncheckedSessionRemoteLast");
      log.debug("Bean.echo('testUncheckedRemote') -> " + bean.echo("testUncheckedRemote"));
      try
      {
         bean.excluded();
         fail("Was able to call UncheckedSessionRemoteLast.excluded");
      }
      catch (RemoteException e)
      {
         log.debug("UncheckedSessionRemoteLast.excluded failed as expected");
      }
      bean.remove();
      logout();
   }

   /**
    * Test access to a bean with a mix of remote interface permissions and unchecked permissions with the unchecked
    * permissions declared last.
    * 
    * @throws Exception
    */
   public void testRemoteUnchecked() throws Exception
   {
      log.debug("+++ testRemoteUnchecked");
      login();
      Object obj = getInitialContext().lookup("spec.UncheckedSessionRemoteFirst");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found UncheckedSessionRemoteFirst");
      StatelessSession bean = home.create();
      log.debug("Created spec.UncheckedSessionRemoteFirst");
      log.debug("Bean.echo('testRemoteUnchecked') -> " + bean.echo("testRemoteUnchecked"));
      try
      {
         bean.excluded();
         fail("Was able to call UncheckedSessionRemoteFirst.excluded");
      }
      catch (RemoteException e)
      {
         log.debug("UncheckedSessionRemoteFirst.excluded failed as expected");
      }
      bean.remove();
      logout();
   }

   /**
    * Test that a user with a role that has not been assigned any method permissions in the ejb-jar descriptor is able
    * to access a method that has been marked as unchecked.
    */
   public void testUnchecked() throws Exception
   {
      log.debug("+++ testUnchecked");
      // Login as scott to create the bean
      login();
      Object obj = getInitialContext().lookup("spec.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found spec.StatelessSession Home");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession");
      // Logout and login back in as stark to test access to the unchecked method
      logout();
      login("stark", "javaman".toCharArray());
      bean.unchecked();
      log.debug("Called Bean.unchecked()");
      logout();
   }

   /**
    * Test that a user with a valid role is able to access a bean for which all methods have been marked as unchecked.
    */
   public void testUncheckedWithLogin() throws Exception
   {
      log.debug("+++ testUncheckedWithLogin");
      // Login as scott to see that a user with roles is allowed access
      login();
      Object obj = getInitialContext().lookup("spec.UncheckedSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found spec.StatelessSession Home");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession");
      bean.unchecked();
      log.debug("Called Bean.unchecked()");
      logout();
   }

   /**
    * Test that user scott who has the Echo role is not able to access the StatelessSession2.excluded method even though
    * the Echo role has been granted access to all methods of StatelessSession2 to test that the excluded-list takes
    * precendence over the method-permissions.
    */
   public void testExcluded() throws Exception
   {
      log.debug("+++ testExcluded");
      login();
      Object obj = getInitialContext().lookup("spec.StatelessSession2");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found spec.StatelessSession2 Home");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession2");
      try
      {
         bean.excluded();
         fail("Was able to call Bean.excluded()");
      }
      catch (Exception e)
      {
         log.debug("Bean.excluded() failed as expected");
         // This is what we expect
      }
      logout();
   }

   /**
    * This method tests the following call chains: 1. RunAsStatelessSession.echo() -> PrivateEntity.echo() 2.
    * RunAsStatelessSession.noop() -> RunAsStatelessSession.excluded() 3. RunAsStatelessSession.forward() ->
    * StatelessSession.echo() 1. Should succeed because the run-as identity of RunAsStatelessSession is valid for
    * accessing PrivateEntity. 2. Should succeed because the run-as identity of RunAsStatelessSession is valid for
    * accessing RunAsStatelessSession.excluded(). 3. Should fail because the run-as identity of RunAsStatelessSession is
    * not Echo.
    */
   public void testRunAs() throws Exception
   {
      log.debug("+++ testRunAs");
      login();
      Object obj = getInitialContext().lookup("spec.RunAsStatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found RunAsStatelessSession Home");
      StatelessSession bean = home.create();
      log.debug("Created spec.RunAsStatelessSession");
      log.debug("Bean.echo('testRunAs') -> " + bean.echo("testRunAs"));
      bean.noop();
      log.debug("Bean.noop(), ok");

      try
      {
         // This should not be allowed
         bean.forward("Hello");
         fail("Was able to call RunAsStatelessSession.forward");
      }
      catch (RemoteException e)
      {
         log.debug("StatelessSession.forward failed as expected");
      }
      bean.remove();
   }

   /**
    * This method tests the following call chain: Level1CallerBean.callEcho() -> Level2CallerBean.invokeEcho() ->
    * Level3CalleeBean.echo() The Level1CallerBean uses a run-as of InternalRole and the Level2CallerBean and
    * Level3CalleeBean are only accessible by InternalRole.
    */
   public void testDeepRunAs() throws Exception
   {
      log.debug("+++ testDeepRunAs");
      login();
      Object obj = getInitialContext().lookup("spec.Level1CallerBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found Level1CallerBean Home");
      CalledSession bean = home.create();
      log.debug("Created spec.Level1CallerBean");
      bean.callEcho();
      log.debug("Bean.callEcho() ok");
      bean.remove();

      // Make sure we cannot access Level2CallerBean remotely
      obj = getInitialContext().lookup("spec.Level2CallerBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      home = (CalledSessionHome) obj;
      log.debug("Found Level2CallerBean Home");
      try
      {
         bean = home.create();
         fail("Was able to create Level2CallerBean");
      }
      catch (ServerException e)
      {
         AccessException ae = (AccessException) e.detail;
         log.debug("Caught AccessException as expected", ae);
      }
      catch (AccessException e)
      {
         log.debug("Caught AccessException as expected", e);
      }
   }

   public void testRunAsSFSB() throws Exception
   {
      log.info("+++ testRunAsSFSB");
      login();
      Object obj = getInitialContext().lookup("spec.CallerFacadeBean-testRunAsSFSB");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found CallerFacadeBean-testRunAsSFSB Home");
      CalledSession bean = home.create();
      log.debug("Created spec.CallerFacadeBean-testRunAsSFSB");
      bean.invokeEcho("testRunAsSFSB");
      log.debug("Bean.invokeEcho() ok");
      bean.remove();
   }

   /**
    * Test the run-as side-effects raised in http://jira.jboss.com/jira/browse/JBAS-1852
    * 
    * @throws Exception
    */
   public void testJBAS1852() throws Exception
   {
      log.info("+++ testJBAS1852");
      login();
      Object obj = getInitialContext().lookup("spec.PublicSessionFacade");
      obj = PortableRemoteObject.narrow(obj, SessionFacadeHome.class);
      SessionFacadeHome home = (SessionFacadeHome) obj;
      log.debug("Found PublicSessionFacade home");
      SessionFacade bean = home.create();
      log.debug("Created PublicSessionFacade");
      log.debug("Bean.callEcho('testJBAS1852') -> " + bean.callEcho("testJBAS1852"));
      bean.remove();
   }

   /**
    * Test that an MDB with a run-as identity is able to access secure EJBs that require the identity.
    */
   public void testMDBRunAs() throws Exception
   { 
      log.debug("Running test testMDBRunAs");
      this.logout(); 
      Thread.sleep(1000);
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) getInitialContext().lookup(QUEUE_FACTORY);
      Queue queA = (Queue) getInitialContext().lookup("queue/QueueA");
      Queue queB = (Queue) getInitialContext().lookup("queue/QueueB");
      QueueConnection queueConn = null;
      QueueSession session = null;
      try
      {
         queueConn = queueFactory.createQueueConnection();
         session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         queueConn.start();
         // create a JMS message.
         Message msg = session.createMessage();
         msg.setStringProperty("arg", "testMDBRunAs");
         msg.setJMSReplyTo(queB);
         // send the constructed message to queue A.
         QueueSender sender = session.createSender(queA);
         sender.send(msg);
         sender.close();
         log.debug("Sent msg to queue/QueueA");
         // receive the message at queue B.
         QueueReceiver recv = session.createReceiver(queB);
         msg = recv.receive(15000);
         recv.close();
         log.debug("Recv msg: " + msg);
         // get the message's content.
         String info = msg.getStringProperty("reply");
         if (info == null || info.startsWith("Failed"))
         {
            fail("Received exception reply, info=" + info);
         }
      }
      finally
      {
         if (session != null)
            session.close();
         if (queueConn != null)
            queueConn.close();
      }
   }

   /**
    * Test that an MDB with a run-as identity is able to access secure EJBs that require the identity. DeepRunAsMDB ->
    * Level1MDBCallerBean.callEcho() -> Level2CallerBean.invokeEcho() -> Level3CalleeBean.echo() The MDB uses a run-as
    * of InternalRole and the Level2CallerBean and Level3CalleeBean are only accessible by InternalRole.
    */
   public void testMDBDeepRunAs() throws Exception
   {
      log.debug("Running test testMDBDeepRunAs");
      this.logout();
      Thread.sleep(1000);
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) getInitialContext().lookup(QUEUE_FACTORY);
      Queue queD = (Queue) getInitialContext().lookup("queue/QueueD");
      Queue queB = (Queue) getInitialContext().lookup("queue/QueueB");
      QueueConnection queueConn = null;
      QueueSession session = null;
      try
      {
         queueConn = queueFactory.createQueueConnection();
         session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         queueConn.start();
         // create a JMS message.
         Message msg = session.createMessage();
         msg.setStringProperty("arg", "testMDBDeepRunAs");
         msg.setJMSReplyTo(queB);
         // send the constructed message to queue D.
         QueueSender sender = session.createSender(queD);
         sender.send(msg);
         sender.close();
         log.debug("Sent msg to " + queD);
         // receive the message at queue B
         QueueReceiver recv = session.createReceiver(queB);
         msg = recv.receive(15000);
         recv.close();
         log.debug("Recv msg: " + msg);
         // get the message's content.
         String info = msg.getStringProperty("reply");
         if (info == null || info.startsWith("Failed"))
         {
            fail("Received exception reply, info=" + info);
         }
      }
      finally
      {
         if (session != null)
            session.close();
         if (queueConn != null)
            queueConn.close();
      }
   }

   /**
    * This method tests that the RunAsWithRolesMDB is assigned multiple roles within its onMessage so that it can call
    * into the ProjRepository session bean's methods that required ProjectAdmin, CreateFolder and DeleteFolder roles.
    */
   public void testRunAsWithRoles() throws Exception
   {
      log.debug("Running test testRunAsWithRoles");
      this.logout();
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) getInitialContext().lookup(QUEUE_FACTORY);
      Queue queC = (Queue) getInitialContext().lookup("queue/QueueC");
      Queue queB = (Queue) getInitialContext().lookup("queue/QueueB");
      QueueConnection queueConn = null;
      QueueSession session = null;

      try
      {
         queueConn = queueFactory.createQueueConnection();
         session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         queueConn.start();
         // create a JMS message.
         Message msg = session.createMessage();
         msg.setStringProperty("name", "testRunAsWithRoles");
         msg.setJMSReplyTo(queB);
         // send the constructed message to queue C.
         QueueSender sender = session.createSender(queC);
         sender.send(msg);
         sender.close();
         log.debug("Sent msg to queue/QueueC");
         // receive the message at queue B.
         QueueReceiver recv = session.createReceiver(queB);
         msg = recv.receive(5000);
         log.debug("Recv msg: " + msg);
         recv.close();
         // get the message's content.
         String info = msg.getStringProperty("reply");
         if (info == null || info.startsWith("Failed"))
         {
            fail("Received exception reply, info=" + info);
         }
      }
      finally
      {
         if (session != null)
            session.close();
         if (queueConn != null)
            queueConn.close();
      }
   }

   /**
    * Test the security behavior of handles. To obtain secured bean from a handle that the handle be
    */
   public void testHandle() throws Exception
   {
      log.debug("+++ testHandle");
      login();
      Object obj = getInitialContext().lookup("spec.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession");
      Handle h = bean.getHandle();
      log.debug("Obtained handle: " + h);
      bean = (StatelessSession) h.getEJBObject();
      log.debug("Obtained bean from handle: " + bean);
      log.debug("Bean.echo('testHandle') -> " + bean.echo("testHandle"));
      logout();

      /*
       * Attempting to obtain the EJB fron the handle without security association present should fail
       */
      try
      {
         bean = (StatelessSession) h.getEJBObject();
         fail("Should not be able to obtain a bean without login info");
      }
      catch (Exception e)
      {
         log.debug("Obtaining bean from handle failed as expected, e=" + e.getMessage());
      }

      // One should be able to obtain a handle without a login
      h = bean.getHandle();
      login();
      // Now we should be able to obtain and use the secure bean
      bean = (StatelessSession) h.getEJBObject();
      log.debug("Obtained bean from handle: " + bean);
      log.debug("Bean.echo('testHandle2') -> " + bean.echo("testHandle2"));
      logout();
   }

   /**
    * Test the security behavior of stateful handles. To obtain secured bean from a handle requires that there be a
    * security context to obtain the ejb.
    */
   public void testStatefulHandle() throws Exception
   {
      log.debug("+++ testStatefulHandle");
      login();
      Object obj = getInitialContext().lookup("spec.StatefulSession");
      obj = PortableRemoteObject.narrow(obj, StatefulSessionHome.class);
      StatefulSessionHome home = (StatefulSessionHome) obj;
      log.debug("Found StatefulSession");
      StatefulSession bean = home.create("testStatefulHandle");
      log.debug("Created spec.StatelessSession");
      Handle h = bean.getHandle();
      log.debug("Obtained handle: " + h);
      bean = (StatefulSession) h.getEJBObject();
      log.debug("Obtained bean from handle: " + bean);
      log.debug("Bean.echo('Hello') -> " + bean.echo("Hello"));
      logout();

      /*
       * Attempting to obtain the EJB fron the handle without security association present should fail
       */
      try
      {
         bean = (StatefulSession) h.getEJBObject();
         fail("Should not be able to obtain a bean without login info");
      }
      catch (Exception e)
      {
         log.debug("Obtaining bean from handle failed as expected, e=" + e.getMessage());
      }

      // One should be able to obtain a handle without a login
      h = bean.getHandle();
      login();
      // Now we should be able to obtain and use the secure bean
      bean = (StatefulSession) h.getEJBObject();
      log.debug("Obtained bean from handle: " + bean);
      log.debug("Bean.echo('Hello') -> " + bean.echo("Hello"));
      logout();
   }

   /**
    * Stress test declarative security.
    */
   public void testStress() throws Exception
   {
      log.debug("+++ testStress");
      int count = Integer.getInteger("jbosstest.threadcount", 2).intValue();
      int iterations = 10;
      /*
       * FIXME, Use a minimum of 100 iterations iterations = Integer.getInteger("jbosstest.iterationcount",
       * 5).intValue(); if( iterations < 100 ) iterations = 100;
       */
      log.info("Creating " + count + " threads doing " + iterations + " iterations");
      Thread[] testThreads = new Thread[count];
      StressTester[] testers = new StressTester[count];

      for (int t = 0; t < count; t++)
      {
         StressTester test = new StressTester(getInitialContext(), iterations);
         testers[t] = test;
         Thread thr = new Thread(test, "Tester#" + t);
         thr.start();
         testThreads[t] = thr;
      }

      int errorCount = 0;
      for (int t = 0; t < count; t++)
      {
         Thread thr = testThreads[t];
         thr.join();
         StressTester test = testers[t];
         if (test.error != null)
         {
            errorCount++;
         }
      }
      assertTrue("Thread error count == 0", errorCount == 0);
   }

   /**
    * Stress test declarative security with the JAAS cache disabled.
    */
   public void testStressNoJaasCache() throws Exception
   {
      log.info("+++ testStressNoJaasCache, domain=spec-test");
      // Disable caching for the spec-test domain
      MBeanServerConnection conn = getServer();
      ObjectName secMgrName = new ObjectName("jboss.security:service=JaasSecurityManager");
      JaasSecurityManagerServiceMBean secMgr = (JaasSecurityManagerServiceMBean) MBeanServerInvocationHandler
            .newProxyInstance(conn, secMgrName, JaasSecurityManagerServiceMBean.class, false);
      secMgr.setCacheTimeout("spec-test", 0, 0);

      Exception failed = null;
      try
      {
         // Now execute the testStress access
         testStress();
      }
      catch (Exception e)
      {
         failed = e;
      }

      secMgr.setCacheTimeout("spec-test", 60, 60);
      if (failed != null)
         throw failed;
   }

   private static class StressTester implements Runnable
   {
      InitialContext ctx;

      int iterations;

      Throwable error;

      StressTester(InitialContext ctx, int iterations) throws Exception
      {
         this.ctx = ctx;
         this.iterations = iterations;
      }

      public void run()
      {
         Thread t = Thread.currentThread();
         Logger log = Logger.getLogger(t.getName());
         log.info("Begin run, t=" + t);
         try
         {
            AppCallbackHandler handler = new AppCallbackHandler(EJBSpecUnitTestCase.username,
                  EJBSpecUnitTestCase.password);
            for (int i = 0; i < iterations; i++)
            {
               LoginContext lc = new LoginContext("spec-test-multi-threaded", handler);
               lc.login();
               Object obj = ctx.lookup("spec.StatelessSession");
               obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
               StatelessSessionHome home = (StatelessSessionHome) obj;
               log.debug("Found StatelessSessionHome");
               StatelessSession bean = home.create();
               log.debug("Created spec.StatelessSession");
               log.debug("Bean.echo('Hello') -> " + bean.echo("Hello"));
               bean.remove();
               log.debug("Removed bean");
               lc.logout();
            }
         }
         catch (Throwable e)
         {
            error = e;
            log.error("Security failure", e);
         }
         log.info("End run, t=" + Thread.currentThread());
      }
   }

   /**
    * Login as user scott using the conf.name login config or 'spec-test' if conf.name is not defined.
    */
   private void login() throws Exception
   {
      login(username, password);
   }

   private void login(String username, char[] password) throws Exception
   {
      if (loggedIn)
         return;

      lc = null;
      String confName = System.getProperty("conf.name", "spec-test");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext(" + confName + ")");
      lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject=" + lc.getSubject());
      loggedIn = true;
   }

   private void logout() throws Exception
   {
      if (loggedIn)
      {
         loggedIn = false;
         lc.logout();
      }
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      if (System.getProperty("java.security.auth.login.config") == null)
      {
         System.setProperty("java.security.auth.login.config", "output/resources/security/auth.conf");
      }
   }

   private static TestSuite fillTestSuiteConditionally(Class clazz) {

	   TestSuite suite = new TestSuite();
	   
	   String disabledMethods = System.getProperty("cc.disabled.test.methods" ,"");
	   
	   for (Method method: clazz.getMethods()) {
		   String name = method.getName(); 
		   if (disabledMethods.indexOf(name) == -1 && name.startsWith("test")) {
			   suite.addTest(TestSuite.createTest(clazz, name));
		   }
	   }
	   
	   return suite;
	   
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      //suite.addTest(new TestSuite(EJBSpecUnitTestCase.class));
      suite = fillTestSuiteConditionally(EJBSpecUnitTestCase.class);

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            JMSDestinationsUtil.setupBasicDestinations();
            JMSDestinationsUtil.deployQueue("QueueA");
            JMSDestinationsUtil.deployQueue("QueueB");
            JMSDestinationsUtil.deployQueue("QueueC");
            JMSDestinationsUtil.deployQueue("QueueD");
            redeploy("security-spec.jar");
            flushAuthCache();
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy("security-spec.jar");
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();

         }
      };
      return wrapper;
   }

}
