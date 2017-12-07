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
package org.jboss.test.securitymgr.test;

import java.net.URL;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Basic tests for web apps
 *
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 81036 $
 */
public class WarPermissionsUnitTestCase
      extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL(); 

   public WarPermissionsUnitTestCase(String name)
   {
      super(name);
   }

   public void testPackedAllowedPermissions()
      throws Exception
   {
      URL url = new URL(baseURL+"packed/FileAccessServlet?file=allow");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header hdr = request.getResponseHeader("X-CodeSource");
      log.info("X-CodeSource: "+hdr);
      assertTrue("X-CodeSource("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-RealPath");
      log.info("X-RealPath: "+hdr);
      assertTrue("X-RealPath("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-Exception");
      log.info("X-Exception: "+hdr);
      assertTrue("X-Exception("+hdr+") is null", hdr == null);
   }
   public void testUnpackedAllowedPermissions()
      throws Exception
   {
      URL url = new URL(baseURL+"unpacked/FileAccessServlet?file=allow");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header hdr = request.getResponseHeader("X-CodeSource");
      log.info("X-CodeSource: "+hdr);
      assertTrue("X-CodeSource("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-RealPath");
      log.info("X-RealPath: "+hdr);
      assertTrue("X-RealPath("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-Exception");
      log.info("X-Exception: "+hdr);
      assertTrue("X-Exception("+hdr+") is null", hdr == null);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(WarPermissionsUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            redeploy("securitymgr/unpacked.war");
            redeploy("securitymgr/packed.war");
         }
         protected void tearDown() throws Exception
         {
            undeploy("securitymgr/unpacked.war");
            undeploy("securitymgr/packed.war");
            super.tearDown();
         }
      };
      return wrapper;
   }

}
