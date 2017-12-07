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

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.SessionCookie;
import org.apache.tomcat.util.http.TomcatCookie;
import org.jboss.test.cluster.web.mocks.MockConnector;
import org.jboss.test.cluster.web.mocks.MockEngine;
import org.jboss.test.cluster.web.mocks.MockHost;
import org.jboss.test.cluster.web.mocks.MockJBossManager;
import org.jboss.test.cluster.web.mocks.MockResponse;
import org.jboss.web.tomcat.service.session.AbstractJBossManager;

/**
 * Test behavior of JBossManager.setNewSessionCookie()
 * 
 * @author Brian Stansberry
 */
public class SetNewSessionCookieUnitTestCase extends TestCase
{
   private static final String ROOT_PATH = "/";
   private static final String CONTEXT_PATH = "/test";
   private static final String DOMAIN = "jboss.org";
   private static final String SESSION_ID = "abc123";
   private static final String COMMENT = "A comment";
   
   /**
    * Create a new SetNewSessionCookieUnitTestCase.
    * 
    * @param name
    */
   public SetNewSessionCookieUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testBasic() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, CONTEXT_PATH, false);
   }
   
   public void testRootContext() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(ROOT_PATH));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, ROOT_PATH, false);
   }
   
   public void testEmptyContextPath() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(""));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, ROOT_PATH, false);
   }
   
   public void testSecureConnector() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH));
      
      Cookie cookie = validateResponse(executeRequest(mgr, true));
      validateCookie(cookie, CONTEXT_PATH, true);
   }
   
   public void testDomain() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH, null, null, DOMAIN, false, false));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, CONTEXT_PATH, null, DOMAIN, false, false);
   }
   
   public void testCookiePath() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH, ROOT_PATH, null, null, false, false));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, ROOT_PATH, false);
   }
   
   public void testComment() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH, null, COMMENT, null, false, false));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, CONTEXT_PATH, COMMENT, null, false, false);
   }
   
   public void testSecureContext() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH, null, null, null, false, true));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, CONTEXT_PATH, true);
   }
   
   public void testHttpOnly() throws Exception
   {
      AbstractJBossManager mgr = getManager(getContext(CONTEXT_PATH, null, null, null, true, false));
      
      Cookie cookie = validateResponse(executeRequest(mgr, false));
      validateCookie(cookie, CONTEXT_PATH, null, null, true, false);
   }
   
   private Context getContext(String contextName)
   {
      return getContext(contextName, null, null, null, false, false);
   }

   private Context getContext(String contextPath, String cookiePath, 
                              String cookieComment, String cookieDomain,
                              boolean httpOnly, boolean secure)
   {
      MockEngine engine = new MockEngine();
      MockHost host = new MockHost();
      engine.addChild(host);
      host.setName("localhost");
      StandardContext container = new StandardContext();
      container.setPath(contextPath);
      host.addChild(container);
      
      SessionCookie cookie = container.getSessionCookie();
      if (cookiePath != null)
         cookie.setPath(cookiePath);
      if (cookieComment != null)
         cookie.setComment(cookieComment);
      if (cookieDomain != null)
         cookie.setDomain(cookieDomain);
      cookie.setHttpOnly(httpOnly);
      cookie.setSecure(secure);
      
      return container;
   }
   
   private AbstractJBossManager getManager(Context context)
   {           
      MockJBossManager mgr = new MockJBossManager();
      context.setManager(mgr);   
      return mgr;      
   }
   
   private MockResponse executeRequest(AbstractJBossManager mgr, boolean secure) throws Exception
   {
      MockConnector connector = new MockConnector();
      connector.setSecure(secure);
      MockResponse response = new MockResponse(connector);
      mgr.setNewSessionCookie(SESSION_ID, response);
      return response;
   }
   
   private Cookie validateResponse(Response response)
   {
      Cookie[] cookies = response.getCookies();
      assertNotNull(cookies);
      assertEquals(1, cookies.length);
      return cookies[0];
   }
   
   private void validateCookie(Cookie cookie, String cookiePath, boolean secure)
   {
      validateCookie(cookie, cookiePath, null, null, false, secure);
   }
   
   private void validateCookie(Cookie cookie, String cookiePath, 
                              String cookieComment, String cookieDomain,
                              boolean httpOnly, boolean secure)
   {
      basicValidation(cookie);
      assertEquals("path", cookiePath, cookie.getPath());
      assertEquals("comment", cookieComment, cookie.getComment());
      assertEquals("domain", cookieDomain, cookie.getDomain());
      assertEquals("secure", secure, cookie.getSecure());
      if (cookie instanceof TomcatCookie)
      {
         assertEquals("httpOnly", httpOnly, ((TomcatCookie) cookie).getHttpOnly());
      }
   }

   private void basicValidation(Cookie cookie)
   {
      assertNotNull("cookie is null", cookie);
      assertEquals("name", Globals.SESSION_COOKIE_NAME, cookie.getName());
      assertEquals("value", SESSION_ID, cookie.getValue());
      assertEquals("version", 0, cookie.getVersion());
      assertEquals("maxAge", -1, cookie.getMaxAge());
   }
}
