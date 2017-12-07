/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.test.web.jbas8318.unit;

import junit.framework.Test;
import org.apache.commons.httpclient.HttpMethod;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;
import org.jboss.test.web.jbas8318.SimpleServlet;

import java.net.URL;

/**
 * Tests that Java EE resource injections on non-Java EE components are not processed and don't lead to deployment issues.
 *
 * @see https://issues.jboss.org/browse/JBAS-8318 for more details
 *      User: jpai
 */
public class ResourceInjectionOnNonJavaEEComponentsTestCase extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL();

   private static final org.jboss.logging.Logger logger = org.jboss.logging.Logger.getLogger(ResourceInjectionOnNonJavaEEComponentsTestCase.class);

   public ResourceInjectionOnNonJavaEEComponentsTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ResourceInjectionOnNonJavaEEComponentsTestCase.class, "jbas-8318.war");
   }


   /**
    * Test that Java EE resource injection in a servlet works as expected
    *
    * @throws Exception
    */
   public void testInjectionInServlet() throws Exception
   {
      URL url = new URL(baseURL + "jbas-8318/SimpleServlet");
      HttpMethod request = HttpUtils.accessURL(url);
      String response = request.getResponseBodyAsString();
      logger.info("Response for url " + url + " is " + response);
      assertEquals("Unexpected response from servlet", SimpleServlet.SUCCESS_MESSAGE, response);
   }

   /**
    * Tests that Java EE resource injection in a JSF managed bean works as expected
    *
    * @throws Exception
    */
   public void testInjectionInJSFManagedBean() throws Exception
   {
      URL url = new URL(baseURL + "jbas-8318/test-jsf-injection.jsf");
      HttpMethod request = HttpUtils.accessURL(url);
      int statusCode = request.getStatusCode();
      logger.info("Got status code: " + statusCode + " for URL " + url);
      String response = request.getResponseBodyAsString();
      logger.info("Response for URL " + url + " is " + response);
      // no exceptions == injection worked fine and test passed.
      // TODO: We might want to test for the html output to really make sure of the output.
   }

}
