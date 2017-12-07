/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.commons_logging.jbpapp6523.test;

import java.io.File;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.test.JBossTestCase;

/**
 * Base test class for commons logging tests.
 * <p>
 * This class includes URL status checking funcitonality as well.
 *
 * @author jiwils
 */
public abstract class CommonsLoggingBaseTestCase extends JBossTestCase
{
   private final String DEPLOYMENT_URL = "jbpapp6523.war";

   public CommonsLoggingBaseTestCase(String name)
   {
      super(name);
   }

   /* TestCase Overrides */

   protected void setUp()
   throws Exception
   {
      super.setUp();
      deploy(DEPLOYMENT_URL);
   }

   protected void tearDown()
   throws Exception
   {
      super.tearDown();
      undeploy(DEPLOYMENT_URL);
   }
   
   /**
    * Retrieves the HTTP response code for the jbpapp6523 servlet.
    */
   protected int getHTTPResponseCode()
   throws IOException, MalformedURLException
   {
      // Set by the test framework.
      final String HTTP_HOST = System.getProperty("jbosstest.server.host");

      URL servletURL = new URL("http://" + HTTP_HOST + ":8080/jbpapp6523/");
      HttpURLConnection connection = (HttpURLConnection) servletURL.openConnection();
      int responseCode = connection.getResponseCode();
      connection.disconnect();

      return responseCode;
   }
}