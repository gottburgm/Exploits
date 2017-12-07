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
package org.jboss.test.ejb3.iiop.unit;

import java.rmi.MarshalledObject;
import java.util.Date;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;

import junit.framework.Test;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossIIOPTestCase;
import org.jboss.test.ejb3.iiop.HomedStatelessHome;
import org.jboss.test.ejb3.iiop.MySession;
import org.jboss.test.ejb3.iiop.MyStateful;
import org.jboss.test.ejb3.iiop.MyStatefulHome;
import org.jboss.test.ejb3.iiop.TxTester;

/**
 * TODO: use JBossIIOPTestCase
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 97163 $
 */
public class IiopRemoteUnitTestCase extends JBossIIOPTestCase
{

   public IiopRemoteUnitTestCase(String name)
   {
      super(name);
   }

   /*
   @Override
   public InitialContext getInitialContext() throws NamingException
   {
      return new InitialContext(getJndiProperties());
   }
   
   private Properties getJndiProperties()
   {
      Properties props = new Properties();
      props.put("java.naming.factory.initial", "com.sun.jndi.cosnaming.CNCtxFactory");
      props.put("java.naming.provider.url", "corbaloc::localhost:3528/NameService");
      props.put("java.naming.factory.object", "org.jboss.tm.iiop.client.IIOPClientUserTransactionObjectFactory");
      props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming.client:org.jnp.interfaces");
      props.put("j2ee.clientName", "iiop-unit-test");
      
//      props.put(InitialContext.SECURITY_PRINCIPAL, "somebody");
//      props.put(InitialContext.SECURITY_CREDENTIALS, "password");
      
      return props;
//      return null;
   
   }
   */
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      SecurityAssociation.clear();
   }
   
//   public void test0() throws Exception
//   {
//      System.out.println("java: " + getInitialContext().lookup("java:comp/HandleDelegate"));
//   }
   
   public void test1() throws Exception
   {
      SimplePrincipal principal = new SimplePrincipal("somebody");
      SecurityAssociation.setPrincipal(principal);
      SecurityAssociation.setCredential("password".toCharArray());
      
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MySessionBean/remote");
      System.err.println(obj.getClass());
      MySession session = (MySession) PortableRemoteObject.narrow(obj, MySession.class);
      assertNotNull(session);
      String me = new Date().toString();
      String response = session.sayHelloTo(me);
      assertEquals("Hi " + me, response);
   }
   
   public void test2() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MyStatefulBean/home");
      MyStatefulHome home = (MyStatefulHome) PortableRemoteObject.narrow(obj, MyStatefulHome.class);
      //MyStateful bean1 = (MyStateful) PortableRemoteObject.narrow(obj, MyStateful.class);
      MyStateful bean1 = home.create();
      bean1.setName("bean1");
      String response = bean1.sayHello();
      assertEquals("Hello bean1", response);
      bean1.remove();
   }
   
   public void testGetEJBHome() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("HomedStatelessBean/home");
      HomedStatelessHome home = (HomedStatelessHome) PortableRemoteObject.narrow(obj, HomedStatelessHome.class);
      MySession session = home.create();
      Object o = session.getEJBHome();
      HomedStatelessHome home2 = (HomedStatelessHome) PortableRemoteObject.narrow(o, HomedStatelessHome.class);
      // TODO: check home2
   }
   
   public void testGetHandle() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("HomedStatelessBean/home");
      HomedStatelessHome home = (HomedStatelessHome) PortableRemoteObject.narrow(obj, HomedStatelessHome.class);
      MySession session = home.create();
      Handle h = session.getHandle();
      MarshalledObject mo = new MarshalledObject(h);
      Handle h2 = (Handle) mo.get();
      Object o = h2.getEJBObject();
      MySession session2 = (MySession) PortableRemoteObject.narrow(o, MySession.class);
      String me = new Date().toString();
      String response = session2.sayHelloTo(me);
      assertEquals("Hi " + me, response);
   }
   
   public void testGetHomeHandle() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("HomedStatelessBean/home");
      HomedStatelessHome home = (HomedStatelessHome) PortableRemoteObject.narrow(obj, HomedStatelessHome.class);
      HomeHandle h = home.getHomeHandle();
      MarshalledObject mo = new MarshalledObject(h);
      HomeHandle h2 = (HomeHandle) mo.get();
      Object o = h2.getEJBHome();
      HomedStatelessHome home2 = (HomedStatelessHome) PortableRemoteObject.narrow(o, HomedStatelessHome.class);
      // TODO: check home2
   }
   
   // IGNORE: EJB 3.0 Specification Violation 3.6.2.2: Session beans do not have a primary key
   public void _testGetPrimaryKeyAndRemove() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MyStatefulBean/home");
      MyStatefulHome home = (MyStatefulHome) PortableRemoteObject.narrow(obj, MyStatefulHome.class);
      MyStateful session = home.create();
      Object primaryKey = session.getPrimaryKey();
      assertNotNull(primaryKey);
      
      home.remove(primaryKey);
      try
      {
         session.sayHello();
         fail("should throw an exception");
      }
      catch(Exception e)
      {
         // TODO: check exception (NoSuchEJBException)
      }
   }
   
   public void testHomedStateless() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("HomedStatelessBean/home");
      HomedStatelessHome home = (HomedStatelessHome) PortableRemoteObject.narrow(obj, HomedStatelessHome.class);
      MySession session = home.create();
      String me = new Date().toString();
      String response = session.sayHelloTo(me);
      assertEquals("Hi " + me, response);
   }
   
   public void testIsIdentical() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MyStatefulBean/home");
      MyStatefulHome home = (MyStatefulHome) PortableRemoteObject.narrow(obj, MyStatefulHome.class);
      MyStateful session = home.create();
      Handle h = session.getHandle();
      MarshalledObject mo = new MarshalledObject(h);
      Handle h2 = (Handle) mo.get();
      Object o = h2.getEJBObject();
      MyStateful session2 = (MyStateful) PortableRemoteObject.narrow(o, MyStateful.class);
      assertTrue(session.isIdentical(session2));
   }
   
   public void testRemoveByHandle() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MyStatefulBean/home");
      MyStatefulHome home = (MyStatefulHome) PortableRemoteObject.narrow(obj, MyStatefulHome.class);
      MyStateful session = home.create();
      session.setName("Me");
      home.remove(session.getHandle());
      try
      {
         session.sayHello();
         fail("should throw an exception");
      }
      catch(Exception e)
      {
         // TODO: check exception (NoSuchEJBException)
      }
   }
   
   public void testSecurity() throws Exception
   {
      SimplePrincipal principal = new SimplePrincipal("somebody");
      SecurityAssociation.setPrincipal(principal);
      SecurityAssociation.setCredential("password".toCharArray());
      
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MySessionBean/remote");
      System.err.println(obj.getClass());
      MySession session = (MySession) PortableRemoteObject.narrow(obj, MySession.class);
      assertNotNull(session);
      String actual = session.getWhoAmI();
      System.err.println("whoAmI = " + actual);
      assertEquals(actual, "somebody");
   }
   
   // IGNORE: Service beans are no longer invokable through IIOP
   public void _testService() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("MyServiceBean/remote");
      MyStateful bean1 = (MyStateful) PortableRemoteObject.narrow(obj, MyStateful.class);
      bean1.setName("bean1");
      String response = bean1.sayHello();
      assertEquals("Hello bean1", response);
   }
   
   public void testTxPropegation() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("TxTesterBean/remote");
      TxTester session = (TxTester) PortableRemoteObject.narrow(obj, TxTester.class);
      assertNotNull(session);
      UserTransaction tx;
      try
      {
         tx = (UserTransaction) PortableRemoteObject.narrow(ctx.lookup("UserTransaction"), UserTransaction.class);
      }
      catch(NameNotFoundException e)
      {
         log.warn("Corba Transaction Service is not installed (not available with Arjuna, only with JBossTS)");
         return;
      }
      tx.begin();
      try
      {
         session.txMandatoryMethod();
      }
      finally
      {
         tx.rollback();
      }
      // If it doesn't throw an exception everything is fine.
   }

   public void testTxRequired() throws Exception
   {
      InitialContext ctx = getInitialContext();
      Object obj = ctx.lookup("TxTesterBean/remote");
      TxTester session = (TxTester) PortableRemoteObject.narrow(obj, TxTester.class);
      assertNotNull(session);
      try
      {
         session.txMandatoryMethod();
         fail("Expected an exception");
      }
      catch(Exception e)
      {
         //fail("TODO: check exception");
      }
      // TODO: throws an ugly exception, needs assertions to check
   }
   
   public static Test suite() throws Exception
   {
      try
      {
         System.err.println(IiopRemoteUnitTestCase.class.getClassLoader().getResource("jacorb.properties"));
      }
      catch(Throwable t)
      {
         t.printStackTrace();
      }
      return getDeploySetup(IiopRemoteUnitTestCase.class, "ejb3iiop.jar");
   }

}
