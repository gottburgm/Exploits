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

package org.jboss.test.cluster.defaultcfg.simpleweb.test;

import junit.framework.TestCase;

import org.apache.catalina.Session;
import org.apache.catalina.connector.Response;
import org.jboss.logging.Logger;
import org.jboss.test.cluster.web.jvmroute.MockRequest;
import org.jboss.test.cluster.web.mocks.MockClusteredManager;
import org.jboss.test.cluster.web.mocks.MockValve;
import org.jboss.web.tomcat.service.session.JvmRouteValve;

/**
 * Tests of the JvmRouteValve.
 * 
 * @author Brian Stansberry
 */
public class JvmRouteValveUnitTestCase extends TestCase
{  
   private static final Logger log = Logger.getLogger(JvmRouteValveUnitTestCase.class);

   private static final String JVM_ROUTE = "node1";
   private static final String NON_FAILOVER_ID = "123." + JVM_ROUTE;
   private static final String FAILOVER_ID = "123.node2";
   private static final String NO_JVMROUTE_ID = "123";

   private static final String DOMAIN_JVM_ROUTE = "domain1.node1";
   private static final String DOMAIN_NON_FAILOVER_ID = "123." + DOMAIN_JVM_ROUTE;
   private static final String DOMAIN_FAILOVER_ID = "123.domain1.node2";
   
   /**
    * Create a new JvmRouteValueUnitTestCase.
    * 
    * @param name
    */
   public JvmRouteValveUnitTestCase(String name)
   {
      super(name);
   } 
   
   public void testNonFailover() throws Exception
   {
      log.info("Enter testNonFailover");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(session.getId());
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());
   }
   
   public void testFailover() throws Exception
   {
      log.info("Enter testFailover");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(session.getId());
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());
      
   }
   
   public void testFailoverFromURL() throws Exception
   {
      log.info("Enter testFailoverFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(session.getId());
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());      
   }
   
   public void testFailoverMismatchBadReq() throws Exception
   {
      log.info("Enter testFailoverMismatchBadReq");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(FAILOVER_ID);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());      
   }
   
   public void testFailoverMismatchBadReqFromURL() throws Exception
   {
      log.info("Enter testFailoverMismatchBadReqFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(FAILOVER_ID);
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());      
   }
   
   public void testFailoverMismatchBadSession() throws Exception
   {
      log.info("Enter testFailoverMismatchBadSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(NON_FAILOVER_ID);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());      
   }
   
   public void testFailoverMismatchBadSessionFromURL() throws Exception
   {
      log.info("Enter testFailoverMismatchBadSessionFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(NON_FAILOVER_ID);
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());      
   }
   
   public void testNoSession() throws Exception
   {
      log.info("Enter testNoSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      MockRequest req = new MockRequest();
      req.setRequestedSessionId(NON_FAILOVER_ID);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(null, mgr.findSession("123.node1"));
      assertEquals(null, mgr.getNewCookieIdSession());      
   }
   
   public void testNoSessionFromURL() throws Exception
   {
      log.info("Enter testNoSessionFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      MockRequest req = new MockRequest();
      req.setRequestedSessionId(NON_FAILOVER_ID);
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(null, mgr.findSession(NON_FAILOVER_ID));
      assertEquals(null, mgr.getNewCookieIdSession());      
   }
   
   public void testFailoverNoSession() throws Exception
   {
      log.info("Enter testFailoverNoSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      MockRequest req = new MockRequest();
      req.setRequestedSessionId(FAILOVER_ID);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(null, mgr.findSession(NON_FAILOVER_ID));
      assertEquals(null, mgr.findSession("123.node2"));
      assertEquals(null, mgr.getNewCookieIdSession());      
   }
   
   public void testNoSessionNoRequestedSession() throws Exception
   {
      log.info("Enter testNoSessionNoRequestedSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      MockRequest req = new MockRequest();
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(null, mgr.findSession(NON_FAILOVER_ID));
      assertEquals(null, mgr.findSession(FAILOVER_ID));
      assertEquals(null, mgr.getNewCookieIdSession());          
   }
   
   public void testSessionNoRequestedSession() throws Exception
   {
      log.info("Enter testSessionNoRequestedSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());
   }
   
   public void testSessionNoRequestedSessionFromURL() throws Exception
   {
      log.info("Enter testSessionNoRequestedSessionFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());
   }
   
   public void testFailoverSessionNoRequestedSession() throws Exception
   {
      log.info("Enter testFailoverSessionNoRequestedSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());
   }
   
   public void testFailoverSessionNoRequestedSessionFromURL() throws Exception
   {
      log.info("Enter testFailoverSessionNoRequestedSessionFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());
   }

   public void testNoJvmRouteSession() throws Exception
   {
      log.info("Enter testNoJvmRouteSession");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NO_JVMROUTE_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(session.getId());
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());
   }
   
   public void testNoJvmRouteSessionFromURL() throws Exception
   {
      log.info("Enter testNoJvmRouteSessionFromURL");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NO_JVMROUTE_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(session.getId());
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());
   }

   public void testNoJvmRouteRequest() throws Exception
   {
      log.info("Enter testNoJvmRouteRequest");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(NO_JVMROUTE_ID);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(NON_FAILOVER_ID, mgr.getNewCookieIdSession());
   }

   public void testNoJvmRouteRequestFromURL() throws Exception
   {
      log.info("Enter testNoJvmRouteRequest");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(NON_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(NO_JVMROUTE_ID);
      req.setRequestedSessionIdFromURL(true);
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(NON_FAILOVER_ID, session.getId());
      assertEquals(null, mgr.getNewCookieIdSession());
   }
   
   public void testJvmRouteIncludesDomain() throws Exception
   {
      log.info("Enter testJvmRouteIncludesDomain");
      
      MockClusteredManager mgr = new MockClusteredManager();
      mgr.setJvmRoute(DOMAIN_JVM_ROUTE);
       
      JvmRouteValve jvmRouteValve = new JvmRouteValve(mgr);
      
      MockValve mockValve = new MockValve();
      
      jvmRouteValve.setNext(mockValve);
      
      Session session = mgr.createSession(DOMAIN_FAILOVER_ID);
      MockRequest req = new MockRequest();
      req.setSession(session.getSession());
      req.setRequestedSessionId(session.getId());
      
      Response res = new Response();
      
      jvmRouteValve.invoke(req, res);
      
      assertSame(req, mockValve.getInvokedRequest());
      assertSame(res, mockValve.getInvokedResponse());
      assertEquals(DOMAIN_NON_FAILOVER_ID, session.getId());
      assertEquals(DOMAIN_NON_FAILOVER_ID, mgr.getNewCookieIdSession());
      
   }

}
